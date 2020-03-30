package com.example.interfaces;


import com.example.data.Station;

public interface StationsApiListener {

    public void onStationAvailable(Station station);
    public void onStationError(Error error);
}
