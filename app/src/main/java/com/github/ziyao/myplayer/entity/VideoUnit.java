package com.github.ziyao.myplayer.entity;

import java.io.Serializable;

/**
 * Created by Ziyao on 2015/9/14.
 */
public class VideoUnit implements Serializable{
    private int id;
    private String path;
    private String displayName;
    private long duration;

    public long getDuration(){
        return duration;
    }

    public void setDuration(long duration){
        this.duration = duration;
    }

    public long getDate(){
        return date;
    }

    public void setDate(long date){
        this.date = date;
    }

    private long date;

    public int getId(){
        return id;
    }

    public void setId(int id){
        this.id = id;
    }

    public String getPath(){
        return path;
    }

    public void setPath(String path){
        this.path = path;
    }

    public String getDisplayName(){
        return displayName;
    }

    public void setDisplayName(String displayName){
        this.displayName = displayName;
    }
}
