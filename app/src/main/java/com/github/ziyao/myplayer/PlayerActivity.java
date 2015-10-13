package com.github.ziyao.myplayer;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gc.materialdesign.views.ProgressBarCircularIndeterminate;
import com.gc.materialdesign.views.Slider;
import com.gc.materialdesign.widgets.Dialog;
import com.github.ziyao.myplayer.player.DashRendererBuilder;
import com.github.ziyao.myplayer.player.DemoPlayer;
import com.github.ziyao.myplayer.player.ExtractorRendererBuilder;
import com.github.ziyao.myplayer.player.HlsRendererBuilder;
import com.github.ziyao.myplayer.player.SmoothStreamingRendererBuilder;
import com.google.android.exoplayer.AspectRatioFrameLayout;
import com.google.android.exoplayer.audio.AudioCapabilities;
import com.google.android.exoplayer.audio.AudioCapabilitiesReceiver;
import com.google.android.exoplayer.text.Cue;
import com.google.android.exoplayer.util.Util;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.List;
import java.util.Map;


public class PlayerActivity extends Activity
        implements AudioCapabilitiesReceiver.Listener,
        DemoPlayer.Listener,
        DemoPlayer.CaptionListener,
        SurfaceHolder.Callback,
        View.OnClickListener,
        DemoPlayer.Id3MetadataListener{


    public static final int TYPE_DASH = 0;
    public static final int TYPE_SS = 1;
    public static final int TYPE_HLS = 2;
    public static final int TYPE_OTHER = 3;

    private static final int UPDATE_MSG = 5;


    public static final String CONTENT_TYPE_EXTRA = "content_type";
    public static final String CONTENT_ID_EXTRA = "content_id";

    private static final CookieManager defaultCookieManager;

    static{
        defaultCookieManager = new CookieManager();
        defaultCookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ORIGINAL_SERVER);
    }


    //view
    private AspectRatioFrameLayout videoFrame;
    private SurfaceView surfaceView;
    private TextView timeIndicatorView;
    private FrameLayout controlView;
    private ImageView forwardButton;
    private ImageView stopButton;
    private ImageView rewindButton;
    private ImageView settingButton;
    private Slider progressSlider;
    private TextView currentTimeText;
    private TextView endTimeText;
    private ProgressBarCircularIndeterminate loadingProgress;
    private ImageView hintButton;
    private TextView hintText;
    private LinearLayout hintContainer;
    private LinearLayout audioControlView;
    private LinearLayout brightnessControlView;

    private Handler progressUpdater = new Handler(){
        @Override
        public void handleMessage(Message msg){
            super.handleMessage(msg);
            //Log.d("TAG", "Duration: " + player.getDuration() + " position: " + playerPosition);
            if( playDuration == 0 || player == null ){
                return;
            }
            if( msg.what == UPDATE_MSG ){
                playerPosition = player.getCurrentPosition();
                int value = ( int ) playerPosition;
                progressSlider.setValue(value);
                progressSlider.invalidate();

                currentTimeText.setText(millsToString(playerPosition));

                int bufferPercentage = player.getBufferedPercentage();
                Log.d("TAG", "buffer: " + bufferPercentage);
                if( playerPosition<playDuration ){
                    progressUpdater.sendEmptyMessageDelayed(UPDATE_MSG, 500);
                }
            }
        }
    };


    //info
    private Uri contentUri;
    private int contentType;
    private String contentId;

    private long playDuration;

    //third part component
    private AudioCapabilitiesReceiver audioCapabilitiesReceiver;
    private AudioCapabilities audioCapabilities;

    private DemoPlayer player;

    private EventLogger eventLogger;

    //control value
    private boolean enableBackgroundAudio = false;
    private boolean playerNeedsPrepare;
    private long playerPosition = 0;

    private boolean isControlViewShowing = true;
    private final int AUDIO_SHOWING = 0;
    private final int LIGHT_SHOWING = 1;
    private final int RELOAD_SHOWING = 2;
    private int hintShowStatus;

    private float currentVolume;
    private float currentBrightness;

    //audio and light setting
    AudioManager audioManager;

    //other
    int audioControlWidth;
    int audioControlHeight;
    int screenWidth;
    int screenHeight;


    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        findView();

        setClickListener();

        getInforFromIntent();

        init();
    }

    public void findView(){
        videoFrame = ( AspectRatioFrameLayout ) findViewById(R.id.video_frame);
        surfaceView = ( SurfaceView ) findViewById(R.id.surface_view);
        timeIndicatorView = ( TextView ) findViewById(R.id.time_indicator);
        controlView = ( FrameLayout ) findViewById(R.id.control_view);
        forwardButton = ( ImageView ) findViewById(R.id.forward_btn);
        stopButton = ( ImageView ) findViewById(R.id.start_stop_btn);
        rewindButton = ( ImageView ) findViewById(R.id.rewind_btn);
        settingButton = (ImageView)findViewById(R.id.setting_btn);
        progressSlider = ( Slider ) findViewById(R.id.slider);
        currentTimeText = ( TextView ) findViewById(R.id.current_time);
        endTimeText = ( TextView ) findViewById(R.id.end_time);
        loadingProgress = ( ProgressBarCircularIndeterminate ) findViewById(R.id.loading_progress);
        hintButton = ( ImageView ) findViewById(R.id.hint_image);
        hintContainer = ( LinearLayout ) findViewById(R.id.hint_container);
        hintText = ( TextView )findViewById( R.id.hint_text );
        audioControlView = (LinearLayout)findViewById(R.id.audio_control);
        brightnessControlView = ( LinearLayout )findViewById( R.id.light_control );




    }

    public void getInforFromIntent(){
        Intent intent = getIntent();
        contentUri = intent.getData();
        contentType = intent.getIntExtra(CONTENT_TYPE_EXTRA, -1);
        contentId = intent.getStringExtra(CONTENT_ID_EXTRA);
    }

    public void init(){
        audioCapabilitiesReceiver = new AudioCapabilitiesReceiver(getApplicationContext(), this);

        CookieHandler currentHandler = CookieHandler.getDefault();
        if( currentHandler != defaultCookieManager ){
            CookieHandler.setDefault(defaultCookieManager);
        }

        audioManager = (AudioManager) getSystemService(Service.AUDIO_SERVICE);
        currentVolume = audioManager.getStreamVolume( AudioManager.STREAM_MUSIC );
        currentBrightness = 50;

        audioControlWidth = getResources().getDimensionPixelSize( R.dimen.audio_light_control_width );
        audioControlHeight = getResources().getDimensionPixelSize(R.dimen.audio_light_control_width);

        WindowManager wm = this.getWindowManager();
        screenWidth = wm.getDefaultDisplay().getWidth();
        screenHeight = wm.getDefaultDisplay().getHeight();
    }

    public void setClickListener(){
        videoFrame.setOnClickListener(this);
        forwardButton.setOnClickListener(this);
        stopButton.setOnClickListener(this);
        rewindButton.setOnClickListener(this);
        surfaceView.setOnClickListener(this);
        settingButton.setOnClickListener(this);

        brightnessControlView.setOnTouchListener(new View.OnTouchListener(){
            private float y1;


            @Override
            public boolean onTouch(View v, MotionEvent event){
                Log.d("audio-my", "touch");
                if( !isControlViewShowing ){
                    return false;
                }
                Log.d("audio-my", "touch2");
                float y2 = event.getY();
                switch( event.getAction() ){
                    case MotionEvent.ACTION_DOWN:
                        hintButton.setImageResource(R.mipmap.ic_brightness_7_white_36dp);
                        hintText.setText( String.valueOf( (int)(currentBrightness) ));
                        hintShowStatus = LIGHT_SHOWING;
                        hintContainer.setVisibility(View.VISIBLE);
                        y1 = y2;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        Log.d("audio-my", "before: " + currentVolume );
                        currentBrightness = currentBrightness + (2*(y1-y2)/screenHeight);
                        currentBrightness = currentBrightness>100?100:currentBrightness;
                        currentBrightness = currentBrightness<0?0:currentBrightness;
                        changeAppBrightness( (int)currentBrightness );
                        hintText.setText( String.valueOf( (int)(currentBrightness) ));
                        Log.d("audio-my", "after: " + currentVolume );
                        break;
                    case MotionEvent.ACTION_UP:
                        hintContainer.setVisibility( View.INVISIBLE );
                        break;
                    default:
                        break;
                }

                return true;
            }


        });
        audioControlView.setOnTouchListener(new View.OnTouchListener(){
            private float y1;


            @Override
            public boolean onTouch(View v, MotionEvent event){
                Log.d("audio-my", "touch");
                if( !isControlViewShowing ){
                    return false;
                }
                Log.d("audio-my", "touch2");
                float y2 = event.getY();
                switch( event.getAction() ){
                    case MotionEvent.ACTION_DOWN:
                        hintButton.setImageResource(R.mipmap.ic_volume_up_white_36dp);
                        hintText.setText( String.valueOf( (int)(currentVolume*100/15) ));
                        hintShowStatus = AUDIO_SHOWING;
                        hintContainer.setVisibility(View.VISIBLE);
                        y1 = y2;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        Log.d("audio-my", "before: " + currentVolume );
                        currentVolume = currentVolume + (2*(y1-y2)/screenHeight);
                        currentVolume = currentVolume>15?15:currentVolume;
                        currentVolume = currentVolume<0?0:currentVolume;
                        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, ( int ) currentVolume, 0);
                        hintText.setText(String.valueOf(( int ) (currentVolume * 100 / 15)));
                        Log.d("audio-my", "after: " + currentVolume );
                        break;
                    case MotionEvent.ACTION_UP:
                        hintContainer.setVisibility( View.INVISIBLE );
                        break;
                    default:
                        break;
                }

                return true;
            }


        });
        hintButton.setOnClickListener(this);
        progressSlider.setOnValueChangedListener(new Slider.OnValueChangedListener(){
            @Override
            public void onValueChanged(int value){
                player.seekTo(value * playDuration / progressSlider.getMax());
                timeIndicatorView.setText(millsToString(value));

            }
        });

        progressSlider.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event){
                if( event.getAction() == MotionEvent.ACTION_DOWN ){
                    timeIndicatorView.setVisibility(View.VISIBLE);
                    int value = (( Slider ) v).getValue();
                    timeIndicatorView.setText(millsToString(value));
                }
                if( event.getAction() == MotionEvent.ACTION_UP ){
                    timeIndicatorView.setVisibility(View.INVISIBLE);
                }
                return false;
            }
        });

    }


    public void changeAppBrightness(int brightness) {
        Window window = this.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        if (brightness == -1) {
            lp.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE;
        } else {
            lp.screenBrightness = (brightness <= 0 ? 1 : brightness) / 100f;
        }
        window.setAttributes(lp);
    }

    @Override
    public void onResume(){
        super.onResume();
        audioCapabilitiesReceiver.register();
    }

    @Override
    public void onPause(){
        super.onPause();
        if( !enableBackgroundAudio ){
            releasePlayer();
        }else{
            player.setBackgrounded(true);
        }
        audioCapabilitiesReceiver.unregister();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        releasePlayer();
    }

    private void releasePlayer(){
        if( player != null ){
            playerPosition = player.getCurrentPosition();
            player.release();
            player = null;
            eventLogger.endSession();
            eventLogger = null;
        }
    }

    private void preparePlayer(){
        if( player == null ){
            player = new DemoPlayer(getRendererBuilder());
            player.addListener(this);
            player.setCaptionListener(this);
            player.setMetadataListener(this);
            player.seekTo(playerPosition);
            playerNeedsPrepare = true;
            /*
            mediaController.setMediaPlayer(player.getPlayerControl());
            mediaController.setEnabled(true);
            */
            eventLogger = new EventLogger();
            eventLogger.startSession();
            player.addListener(eventLogger);
            player.setInfoListener(eventLogger);
            player.setInternalErrorListener(eventLogger);
            /*
            debugViewHelper = new DebugTextViewHelper(player, debugTextView);
            debugViewHelper.start();
            */
        }

        if( playerNeedsPrepare ){
            player.prepare();
            playerNeedsPrepare = false;
            //updateButtonVisibilities();
        }
        player.setSurface(surfaceView.getHolder().getSurface());
        player.setPlayWhenReady(true);

    }

    private DemoPlayer.RendererBuilder getRendererBuilder(){
        String userAgent = Util.getUserAgent(this, "MyPlayer");
        switch( contentType ){
            case TYPE_SS:
                return new SmoothStreamingRendererBuilder(this, userAgent, contentUri.toString(),
                        new SmoothStreamingTestMediaDrmCallback());
            case TYPE_DASH:
                return new DashRendererBuilder(this, userAgent, contentUri.toString(),
                        new WidevineTestMediaDrmCallback(contentId), audioCapabilities);
            case TYPE_HLS:
                return new HlsRendererBuilder(this, userAgent, contentUri.toString(), audioCapabilities);
            case TYPE_OTHER:
                return new ExtractorRendererBuilder(this, userAgent, contentUri);
            default:
                throw new IllegalStateException("Unsupported type: " + contentType);
        }
    }

    @Override
    public void onAudioCapabilitiesChanged(AudioCapabilities audioCapabilities){
        boolean audioCapabilitiesChanged = !audioCapabilities.equals(this.audioCapabilities);
        if( player == null || audioCapabilitiesChanged ){
            this.audioCapabilities = audioCapabilities;
            releasePlayer();
            preparePlayer();
        }else if( player != null ){
            player.setBackgrounded(false);
        }
    }

    @Override
    public void onCues(List< Cue > cues){

    }

    @Override
    public void onStateChanged(boolean playWhenReady, int playbackState){
        if( playbackState == DemoPlayer.STATE_ENDED ){
            stopButton.setImageResource(R.mipmap.ic_play_circle_filled_white_36dp);
            showControlView();
        }

        if( playbackState == DemoPlayer.STATE_IDLE ){
            hintButton.setImageResource( R.mipmap.ic_replay_white_36dp );
            hintText.setText(R.string.load_error);
            hintShowStatus = RELOAD_SHOWING;
            hintContainer.setVisibility(View.VISIBLE);
            loadingProgress.setVisibility(View.INVISIBLE);
        }

        if( playbackState == DemoPlayer.STATE_BUFFERING || playbackState == DemoPlayer.STATE_PREPARING ){
            //showLoading
            if( timeIndicatorView.getVisibility()!=View.VISIBLE ){
                loadingProgress.setVisibility(View.VISIBLE);
            }
        }

        if( playbackState == DemoPlayer.STATE_READY ){
            playDuration = player.getDuration();
            progressSlider.setMax(( int ) playDuration);
            endTimeText.setText(millsToString(playDuration));
            loadingProgress.setVisibility(View.INVISIBLE);
            hideControlView();
            progressUpdater.sendEmptyMessageDelayed(UPDATE_MSG, 1000);
        }

        //updateButtonVisibilities();
    }

    private String millsToString(long time){
        int total = ( int ) (time / 1000);
        int seconds = total % 60;
        int hours = total / 3600;
        int minutes = (total % 3600) / 60;
        StringBuilder sb = new StringBuilder();
        if( hours<10 ){
            sb.append('0');
        }
        sb.append(":");
        if( minutes<10 ){
            sb.append('0');
        }
        sb.append(minutes);
        sb.append(":");
        if( seconds<10 ){
            sb.append('0');
        }
        sb.append(seconds);
        return sb.toString();
    }

    private void showControlView(){
        if( controlView != null ){
            controlView.setVisibility(View.VISIBLE);
            isControlViewShowing = true;
        }
    }

    private void hideControlView(){
        if( controlView != null ){
            controlView.setVisibility(View.INVISIBLE);
            isControlViewShowing = false;
        }
    }

    @Override
    public void onError(Exception e){
        /**
         if (e instanceof UnsupportedDrmException ) {
         // Special case DRM failures.
         UnsupportedDrmException unsupportedDrmException = (UnsupportedDrmException) e;
         int stringId = Util.SDK_INT < 18 ? R.string.drm_error_not_supported
         : unsupportedDrmException.reason == UnsupportedDrmException.REASON_UNSUPPORTED_SCHEME
         ? R.string.drm_error_unsupported_scheme : R.string.drm_error_unknown;
         Toast.makeText(getApplicationContext(), stringId, Toast.LENGTH_LONG).show();
         }
         */
        playerNeedsPrepare = true;
        //updateButtonVisibilities();
        showControlView();
    }

    @Override
    public void onVideoSizeChanged(int width, int height, float pixelWidthHeightRatio){
        videoFrame.setAspectRatio(
                height == 0 ? 1 : (width * pixelWidthHeightRatio) / height);
    }

    @Override
    public void onId3Metadata(Map< String, Object > metadata){

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder){
        if( player != null ){
            player.setSurface(holder.getSurface());
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height){

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder){
        if( player != null ){
            player.blockingClearSurface();
        }
    }

    @Override
    public void onClick(View v){
        Log.d("TAG", "Duration: " + player.getDuration() + " position: " + playerPosition);
        playerPosition = player.getCurrentPosition();
        switch( v.getId() ){
            case R.id.surface_view:
                if( isControlViewShowing ){
                    hideControlView();
                }else{
                    showControlView();
                }
                break;
            case R.id.rewind_btn:
                player.seekTo(playerPosition - 5000);
                break;
            case R.id.forward_btn:
                player.seekTo(playerPosition + 5000);
                break;
            case R.id.start_stop_btn:
                if( player.getPlayWhenReady() ){
                    player.setPlayWhenReady(false);
                    stopButton.setImageResource(R.mipmap.ic_play_circle_filled_white_36dp);
                }else{
                    player.setPlayWhenReady(true);
                    stopButton.setImageResource(R.mipmap.ic_pause_circle_filled_white_36dp);
                }
                break;
            case R.id.hint_image:
                if( hintShowStatus==RELOAD_SHOWING ){
                    preparePlayer();
                    hintContainer.setVisibility(View.INVISIBLE);
                }

                break;
            case R.id.setting_btn:
                showDialog();
                break;

        }
        progressSlider.setValue(( int ) playerPosition);
        progressSlider.invalidate();
    }

    public void showDialog(){
        Dialog dialog = new Dialog(this,"Setting", "hello");
        dialog.show();
    }

}
