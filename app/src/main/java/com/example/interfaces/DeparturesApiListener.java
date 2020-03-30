package com.example.interfaces;

import com.example.data.Departure;

public interface DeparturesApiListener {

    public void onDeparturesAvailable(Departure departure);
    public void onDeparturesError(Error error);
}
