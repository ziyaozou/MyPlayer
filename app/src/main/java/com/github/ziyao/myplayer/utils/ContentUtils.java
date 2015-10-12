package com.github.ziyao.myplayer.utils;


import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.Environment;
import android.provider.MediaStore;
import com.github.ziyao.myplayer.entity.VideoUnit;

import java.io.File;
import java.io.FileFilter;
import java.sql.Date;
import java.util.ArrayList;

/**
 * Created by Ziyao on 2015/9/15.
 */
public class ContentUtils{

    private ContentResolver mContentResolver;


    public ContentUtils(Context context){
        mContentResolver = context.getContentResolver();
    }

    public void getImageContent(long from, long to, ArrayList< VideoUnit > list){

        if( list == null ) return;
        list.clear();

        String[] projections = {
                MediaStore.Video.Media.DATA,
                MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.DATE_TAKEN,
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.DURATION,
                MediaStore.Video.Thumbnails.DATA
        };
        Cursor cursor = mContentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projections,
                projections[2] + ">" + from + " and " + projections[2] + "<" + to + " and " + projections[0] + " like '%DCIM%'",
                null,
                projections[2] + " DESC");
        if( cursor.moveToFirst() ){

            int idxColumn = cursor.getColumnIndex(MediaStore.Video.Media._ID);
            int dateColumn = cursor.getColumnIndex(MediaStore.Video.Media.DATE_TAKEN);
            int dataCulumn = cursor.getColumnIndex(MediaStore.Video.Media.DATA);
            int displayColumn = cursor.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME);
            int durationColumn = cursor.getColumnIndex(MediaStore.Video.Media.DURATION);
            do{
                VideoUnit img = new VideoUnit();
                img.setId(cursor.getInt(idxColumn));
                img.setDisplayName(cursor.getString(displayColumn));
                img.setPath(cursor.getString(dataCulumn));
                img.setDate(cursor.getLong(dateColumn));
                img.setDuration( cursor.getLong( durationColumn ));
                list.add(img);

                Date date = new Date(img.getDate());
                /*
                Log.d(MainActivity.TAG, "id->" + img.getId() + " displayName->" + img.getDisplayName() +
                        " date->" + (date.getYear() + 1900) + "-" + (date.getMonth() + 1) + "-" + date.getDate() +
                        " date->" + img.getDate() +
                        " imagePath->" + img.getPath());
                */
            }while( cursor.moveToNext() );

            cursor.close();
        }
    }


    public void getVideoFile( final ArrayList< VideoUnit > list,File file ) {// 获得视频文件

        file.listFiles(new FileFilter() {

            @Override
            public boolean accept(File file) {
                // sdCard找到视频名称
                String name = file.getName();

                int i = name.indexOf('.');
                if (i != -1) {
                    name = name.substring(i);
                    if (name.equalsIgnoreCase(".mp4")
                            || name.equalsIgnoreCase(".3gp")
                            || name.equalsIgnoreCase(".wmv")
                            || name.equalsIgnoreCase(".ts")
                            || name.equalsIgnoreCase(".rmvb")
                            || name.equalsIgnoreCase(".mov")
                            || name.equalsIgnoreCase(".m4v")
                            || name.equalsIgnoreCase(".avi")
                            || name.equalsIgnoreCase(".m3u8")
                            || name.equalsIgnoreCase(".3gpp")
                            || name.equalsIgnoreCase(".3gpp2")
                            || name.equalsIgnoreCase(".mkv")
                            || name.equalsIgnoreCase(".flv")
                            || name.equalsIgnoreCase(".divx")
                            || name.equalsIgnoreCase(".f4v")
                            || name.equalsIgnoreCase(".rm")
                            || name.equalsIgnoreCase(".asf")
                            || name.equalsIgnoreCase(".ram")
                            || name.equalsIgnoreCase(".mpg")
                            || name.equalsIgnoreCase(".v8")
                            || name.equalsIgnoreCase(".swf")
                            || name.equalsIgnoreCase(".m2v")
                            || name.equalsIgnoreCase(".asx")
                            || name.equalsIgnoreCase(".ra")
                            || name.equalsIgnoreCase(".ndivx")
                            || name.equalsIgnoreCase(".xvid")) {
                        VideoUnit unit = new VideoUnit();
                        unit.setDisplayName(file.getName());
                        unit.setPath(file.getAbsolutePath());
                        list.add(unit);
                        return true;
                    }
                } else if (file.isDirectory()) {
                    getVideoFile(list, file);
                }
                return false;
            }
        });
    }


    

    

}
