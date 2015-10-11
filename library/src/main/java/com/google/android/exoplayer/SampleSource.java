/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.android.exoplayer;

import java.io.IOException;

/**
 * A source of media samples.
 * <p>
 * A {@link SampleSource} may expose one or multiple tracks. The number of tracks and information
 * about each can be queried using {@link SampleSourceReader#getTrackCount()} and
 * {@link SampleSourceReader#getTrackInfo(int)} respectively.
 */
public interface SampleSource {

  /**
   * The end of stream has been reached.
   */
  public static final int END_OF_STREAM = -1;
  /**
   * Neither a sample nor a format was read in full. This may be because insufficient data is
   * buffered upstream. If multiple tracks are enabled, this return value may indicate that the
   * next piece of data to be returned from the {@link SampleSource} corresponds to a different
   * track than the one for which data was requested.
   */
  public static final int NOTHING_READ = -2;
  /**
   * A sample was read.
   */
  public static final int SAMPLE_READ = -3;
  /**
   * A format was read.
   */
  public static final int FORMAT_READ = -4;
  /**
   * A discontinuity in the sample stream.
   */
  public static final int DISCONTINUITY_READ = -5;

  /**
   * A consumer of samples should call this method to register themselves and gain access to the
   * source through the returned {@link SampleSourceReader}.
   * <p>
   * {@link SampleSourceReader#release()} should be called on the returned object when access is no
   * longer required.
   *
   * @return A {@link SampleSourceReader} that provides access to the source.
   */
  public SampleSourceReader register();

  /**
   * An interface providing read access to a {@link SampleSource}.
   */
  public interface SampleSourceReader {

    /**
     * Prepares the source.
     * <p>
     * Preparation may require reading from the data source (e.g. to determine the available tracks
     * and formats). If insufficient data is available then the call will return {@code false}
     * rather than block. The method can be called repeatedly until the return value indicates
     * success.
     *
     * @param positionUs The player's current playback position.
     * @return True if the source was prepared successfully, false otherwise.
     */
    public boolean prepare(long positionUs);

    /**
     * Returns the number of tracks exposed by the source.
     *
     * @return The number of tracks.
     */
    public int getTrackCount();

    /**
     * Returns information about the specified track.
     * <p>
     * This method should not be called until after the source has been successfully prepared.
     *
     * @return Information about the specified track.
     */
    public TrackInfo getTrackInfo(int track);

    /**
     * Enable the specified track. This allows the track's format and samples to be read from
     * {@link #readData(int, long, MediaFormatHolder, SampleHolder, boolean)}.
     * <p>
     * This method should not be called until after the source has been successfully prepared.
     *
     * @param track The track to enable.
     * @param positionUs The player's current playback position.
     */
    public void enable(int track, long positionUs);

    /**
     * Disable the specified track.
     * <p>
     * This method should not be called until after the source has been successfully prepared.
     *
     * @param track The track to disable.
     */
    public void disable(int track);

    /**
     * If the source is currently having difficulty preparing or loading samples, then this method
     * throws the underlying error. Otherwise does nothing.
     *
     * @throws IOException The underlying error.
     */
    public void maybeThrowError() throws IOException;

    /**
     * Indicates to the source that it should still be buffering data for the specified track.
     *
     * @param track The track to continue buffering.
     * @param positionUs The current playback position.
     * @return True if the track has available samples, or if the end of the stream has been
     *     reached. False if more data needs to be buffered for samples to become available.
     */
    public boolean continueBuffering(int track, long positionUs);

    /**
     * Attempts to read either a sample, a new format or or a discontinuity from the source.
     * <p>
     * This method should not be called until after the source has been successfully prepared.
     * <p>
     * Note that where multiple tracks are enabled, {@link #NOTHING_READ} may be returned if the
     * next piece of data to be read from the {@link SampleSource} corresponds to a different track
     * than the one for which data was requested.
     *
     * @param track The track from which to read.
     * @param positionUs The current playback position.
     * @param formatHolder A {@link MediaFormatHolder} object to populate in the case of a new
     *     format.
     * @param sampleHolder A {@link SampleHolder} object to populate in the case of a new sample.
     *     If the caller requires the sample data then it must ensure that {@link SampleHolder#data}
     *     references a valid output buffer.
     * @param onlyReadDiscontinuity Whether to only read a discontinuity. If true, only
     *     {@link #DISCONTINUITY_READ} or {@link #NOTHING_READ} can be returned.
     * @return The result, which can be {@link #SAMPLE_READ}, {@link #FORMAT_READ},
     *     {@link #DISCONTINUITY_READ}, {@link #NOTHING_READ} or {@link #END_OF_STREAM}.
     */
    public int readData(int track, long positionUs, MediaFormatHolder formatHolder,
        SampleHolder sampleHolder, boolean onlyReadDiscontinuity);

    /**
     * Seeks to the specified time in microseconds.
     * <p>
     * This method should not be called until after the source has been successfully prepared.
     *
     * @param positionUs The seek position in microseconds.
     */
    public void seekToUs(long positionUs);

    /**
     * Returns an estimate of the position up to which data is buffered.
     * <p>
     * This method should not be called until after the source has been successfully prepared.
     *
     * @return An estimate of the absolute position in microseconds up to which data is buffered,
     *     or {@link TrackRenderer#END_OF_TRACK_US} if data is buffered to the end of the stream,
     *     or {@link TrackRenderer#UNKNOWN_TIME_US} if no estimate is available.
     */
    public long getBufferedPositionUs();

    /**
     * Releases the {@link SampleSourceReader}.
     * <p>
     * This method should be called when access to the {@link SampleSource} is no longer required.
     */
    public void release();

  }

}
