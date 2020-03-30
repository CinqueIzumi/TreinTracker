package com.example.data;

public class Departure {

    private String direction;
    private String date;
    private String time;
    private String track;

    public Departure(String direction, String date, String time, String track) {
        this.direction = direction;
        this.date = date;
        this.time = time;
        this.track = track;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getTrack() {
        return track;
    }

    public void setTrack(String track) {
        this.track = track;
    }
}
