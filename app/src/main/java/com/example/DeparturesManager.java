package com.example;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.data.Departure;
import com.example.interfaces.DeparturesApiListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeparturesManager {

    //private static final String NS_API_KEY_JOURNEY_INFORMATION = "https://gateway.apiportal.ns.nl/reisinformatie-api/api/v2/departures?maxJourneys=5&lang=nl&station=ZVB";
    private static final String TAG = "DeparturesManager";

    private List<Departure> departures = new ArrayList<>();
    private Context context;
    private RequestQueue jsonList;
    private DeparturesApiListener listener;

    public DeparturesManager(Context context, DeparturesApiListener listener) {
        this.context = context;
        this.jsonList = Volley.newRequestQueue(this.context);
        this.listener = listener;
    }

    public List<Departure> getAllDepartures() {
        return departures;
    }

    private String buildURL(String station){
        return "https://gateway.apiportal.ns.nl/reisinformatie-api/api/v2/departures?maxJourneys=5&lang=nl&station=" + station;
    }

    public void getDepartures(String station){

        final JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                buildURL(station),
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, response.toString());

                        try {

                            JSONArray departures = response.getJSONObject("payload").getJSONArray("departures");

                            for (int i = 0; i < departures.length(); i++){

                                String direction = departures.getJSONObject(i).getString("direction");
                                String dateTime = departures.getJSONObject(i).getString("actualDateTime");

                                String[] dateTimeArray = dateTime.split("T");
                                String date = dateTimeArray[0];
                                String time = dateTimeArray[1];

                                String track = departures.getJSONObject(i).getString("plannedTrack");

                                Departure departure = new Departure(direction, date, time, track);
                                listener.onDeparturesAvailable(departure);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG, error.toString());
                        listener.onDeparturesError(new Error("Fout bij ophalen departures"));
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Ocp-Apim-Subscription-Key", "4460ba73963c48f595fb416b436d7636");
                return params;
            }

        };

        jsonList.add(request);
    }
}
