package com.example;

import android.content.Context;
import android.util.Base64;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.Volley;
import com.example.data.Station;
import com.example.interfaces.StationsApiListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class StationManager {

    //private  static final String NS_API_KEY_STATION = "https://gateway.apiportal.ns.nl/places-api/v2/places?lat=51.641664&lng=4.609161&type=stationV2&limit=1&radius=100000&lang=nl&screen-density=ios-2.0&details=False";
    private Context context;
    private RequestQueue jsonList;
    StationsApiListener listener;

    public StationManager(Context context, StationsApiListener listener) {
        this.context = context;
        this.jsonList = Volley.newRequestQueue(this.context);
        this.listener = listener;
    }

    private String buildURL(double lat, double lng){
        return "https://gateway.apiportal.ns.nl/places-api/v2/places?lat=" + lat + "&lng=" + lng + "&type=stationV2&limit=1&radius=100000&lang=nl&screen-density=ios-2.0&details=False";
    }

    public void getClosestStation(double lat, double lng){

        final JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                buildURL(lat, lng),
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("VOLLEY_TAG_STATION", response.toString());

                        try {

                            JSONObject payload = response.getJSONArray("payload").getJSONObject(0);
                            String code = payload.getJSONArray("locations").getJSONObject(0).getString("code");
                            double lat2 = payload.getJSONArray("locations").getJSONObject(0).getDouble("lat");
                            double lng2 = payload.getJSONArray("locations").getJSONObject(0).getDouble("lng");
                            String fullName = payload.getJSONArray("locations").getJSONObject(0).getString("name");


                            Log.d("VOLLEY_TAG_STATION_CODE", code);
                            Log.d("VOLLEY_TAG_STATION_LAT", lat2 + "");
                            Log.d("VOLLEY_TAG_STATION_LNG", lng2 + "");
                            Log.d("VOLLEY_TAG_STATION_NAME", fullName);

                            Station station = new Station(code, lat2, lng2, fullName);

                            listener.onStationAvailable(station);

                        }
                        catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("VOLLEY_TAG_STATION", error.toString());
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
