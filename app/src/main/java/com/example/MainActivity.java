package com.example;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.data.Station;
import com.example.fragments.HelpFragment;
import com.example.fragments.PopFragment;
import com.example.fragments.SettingsFragment;
import com.example.interfaces.OnFragmentInteractionListener;
import com.example.treintracker.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements OnFragmentInteractionListener,
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{

    private Context context;

    private static final String TAG = MainActivity.class.getSimpleName();
    // The Google Maps Directions API will only work with a Server key.
    //private  static final String MY_API_KEY = "Zet hier je eigen Google Directions API key neer\n";
//    private  static final String MY_API_KEY = "b4d288e3-cf59-4234-9949-cc97a74aeb74\n"; //Onze eigen key
    private  static final String MY_API_KEY = "b9b76fa5-992f-4271-bdb3-3a5e2ebc0bf3\n"; //Test key
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    private GoogleMap mMap;
    private ArrayList<LatLng> markerPoints;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private Marker mCurrLocationMarker;

    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    private FragmentManager fragmentManager;

    private Fragment helpFragment;
    private Fragment settingsFragment;
    private Fragment popFragment;

    private Button closestStationButton;
    private Button changeLocationButton;
    private Boolean locationChangable;

    private ColorStateList clrStateListDark;
    private ColorStateList clrStateListNavLight;
    private ColorStateList clrStateListButtonLight;

    private BottomNavigationView bottomNavigationView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        helpFragment = (HelpFragment) getSupportFragmentManager().findFragmentById(R.id.helpFragment);
        settingsFragment = (SettingsFragment) getSupportFragmentManager().findFragmentById(R.id.settingsFragment);
        popFragment = (PopFragment) getSupportFragmentManager().findFragmentById(R.id.popFragment);

        this.locationChangable = false;

        this.context = this;

        changeLocationButton = findViewById(R.id.differentLocationButton);
        changeLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                locationChangable = !locationChangable;
            }
        });

        fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().hide(helpFragment).commit();
        fragmentManager.beginTransaction().hide(settingsFragment).commit();
        fragmentManager.beginTransaction().hide(popFragment).commit();
        checkLocationPermission();

        // Initializing
        markerPoints = new ArrayList<>();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        createLocationCallback();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapFragment);
        mapFragment.getMapAsync(this);

        initNavBar();
        initClrStateLists();

        closestStationButton = findViewById(R.id.closestStationButton);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String color = preferences.getString("color", null);
        if (color != null){
            if (color.equals("dark")){
                colorChange(true);
            } else {
                colorChange(false);
            }
        }

        String language = preferences.getString("language", null);
        if (language != null) {
            Locale locale = new Locale(language);
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            this.getBaseContext().getResources().updateConfiguration(config, this.getBaseContext().getResources().getDisplayMetrics());
        }

        languageUpdate();


        closestStationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mLastLocation != null) {

                    Station testStation = new Station("testStation", mLastLocation.getLatitude(), mLastLocation.getLongitude(), "testStation");

                    PopFragment popFragment1 = (PopFragment) popFragment;

                    popFragment1.sendStation(testStation);

                    fragmentManager.beginTransaction().show(popFragment).commit();
                    closestStationButton.setVisibility(View.INVISIBLE);
                    changeLocationButton.setVisibility(View.INVISIBLE);
                }
            }
        });



    }

    @Override
    public void onFragmentInteraction(Station station) {

        fragmentManager.beginTransaction().hide(popFragment).commit();
        closestStationButton.setVisibility(View.VISIBLE);
        changeLocationButton.setVisibility(View.VISIBLE);

        markerPoints = new ArrayList<>();

        // Creating MarkerOptions
        MarkerOptions optionsStart = new MarkerOptions();
        MarkerOptions optionsEnd = new MarkerOptions();

        LatLng startPoint = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
        LatLng endPoint = new LatLng(station.getLat(), station.getLng());

        // Setting the position of the marker
        optionsStart.position(startPoint);
        optionsEnd.position(endPoint);

        // Already one locations
        if (markerPoints.size() > 0) {
            markerPoints.clear();
            mMap.clear();
        }

        // Adding new item to the ArrayList
        markerPoints.add(startPoint);
        markerPoints.add(endPoint);

        /**
         * For the start location, the color of marker is GREEN and
         * for the end location, the color of marker is RED.
         */


        optionsStart.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        optionsEnd.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));



        // Add new marker to the Google Map Android API V2
        mMap.addMarker(optionsStart);
        mMap.addMarker(optionsEnd);

        // Checks, whether start and end locations are captured
        if (markerPoints.size() >= 2) {
            LatLng origin = markerPoints.get(0);
            LatLng dest = markerPoints.get(1);

            String url = getUrl(origin, dest);

            // Getting URL to the Google Directions API
            Log.d("onMapClick", url);
            FetchUrl FetchUrl = new FetchUrl();

            // Start downloading json data from Google Directions API
            FetchUrl.execute(url);
            //move map camera
//                    mMap.moveCamera(CameraUpdateFactory.newLatLng(origin));
//                    mMap.animateCamera(CameraUpdateFactory.zoomTo(18));

            // Zoom naar de bounding box (latlng1 is linksboven en latlng2 is rechtsonder:
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            LatLngBounds bounds = builder.include(origin).include(dest).build();
            int margin = 50;
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, margin));
        }
    }

    // Initializing the navigation bar and the respective onNavigationItemSelected listener
    public void initNavBar() {
        bottomNavigationView = (BottomNavigationView) findViewById(R.id.nav_view_main);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {

            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {

                    case R.id.navigation_map:
                        fragmentManager.beginTransaction().hide(helpFragment).commit();
                        fragmentManager.beginTransaction().hide(settingsFragment).commit();
                        fragmentManager.beginTransaction().hide(popFragment).commit();
                        closestStationButton.setVisibility(View.VISIBLE);
                        changeLocationButton.setVisibility(View.VISIBLE);
                        break;
                    case R.id.navigation_help:
                        fragmentManager.beginTransaction().show(helpFragment).commit();
                        fragmentManager.beginTransaction().hide(settingsFragment).commit();
                        fragmentManager.beginTransaction().hide(popFragment).commit();
                        closestStationButton.setVisibility(View.INVISIBLE);
                        changeLocationButton.setVisibility(View.INVISIBLE);
                        break;
                    case R.id.navigation_settings:
                        fragmentManager.beginTransaction().show(settingsFragment).commit();
                        fragmentManager.beginTransaction().hide(helpFragment).commit();
                        fragmentManager.beginTransaction().hide(popFragment).commit();
                        closestStationButton.setVisibility(View.INVISIBLE);
                        changeLocationButton.setVisibility(View.INVISIBLE);
                        break;
                }
                return true;
            }

        });
    }

    // Initializing the ColorStateLists which are used for the ColorScheme updates.
    public void initClrStateLists() {
        bottomNavigationView = (BottomNavigationView) findViewById(R.id.nav_view_main);

        String darkThemePurple = "#BB86FC";
        String darkThemeTeal = "#03DAC5";

        clrStateListDark = new ColorStateList(
                new int[][]{
                        new int[]{-android.R.attr.state_checked},
                        new int[]{android.R.attr.state_checked}
                },
                new int[]{
                        Color.parseColor(darkThemePurple),
                        Color.parseColor(darkThemeTeal)
                });
        clrStateListNavLight = bottomNavigationView.getItemIconTintList();

        clrStateListButtonLight = new ColorStateList(
                new int[][]{
                        new int[]{-android.R.attr.state_checked},
                        new int[]{android.R.attr.state_checked}
                },
                new int[]{

                        Color.DKGRAY
                        , Color.rgb (242,81,112),
                }
        );
    }

    // Method which gets called when the TextViews need to be refreshed to update the text to the current Locale.
    public void languageUpdate() {
        TextView testViewLanguageDesc = (TextView) findViewById(R.id.txtLanguagePicker);
        testViewLanguageDesc.setText(getString(R.string.textViewLanguageDesc));

        TextView textViewColorDesc = (TextView) findViewById(R.id.txtColorPicker);
        textViewColorDesc.setText(getString(R.string.textViewColorDesc));

//        TextView helloFrag = (TextView) findViewById(R.id.textViewHelpFrag);
//        helloFrag.setText(getString(R.string.textViewHelpFragment));

        TextView settingsDesc = (TextView)findViewById(R.id.txtViewSettingsDesc);
        settingsDesc.setText(getString(R.string.textViewHelpDescription));

        TextView settingsLangSupport = (TextView)findViewById(R.id.txtViewSettingsLangSupport);
        settingsLangSupport.setText(getString(R.string.textViewHelpLanguageSupport));

        TextView settingsColorSupport = (TextView)findViewById(R.id.txtViewSettingsColorSupport);
        settingsColorSupport.setText(getString(R.string.textViewHelpColorSupport));

        TextView settingsLocationChange = (TextView)findViewById(R.id.txtViewSettingsLocationChange);
        settingsLocationChange.setText(getString(R.string.textViewHelpChangeLocation));

        RadioButton radioBtnDark = (RadioButton) findViewById(R.id.radioBtnDark);
        radioBtnDark.setText(getString(R.string.radioBtnDark));

        RadioButton radioBtnWhite = (RadioButton) findViewById(R.id.radioBtnWhite);
        radioBtnWhite.setText(getString(R.string.radioBtnWhite));

        RadioButton radioBtnDutch = (RadioButton) findViewById(R.id.radioBtnNL);
        radioBtnDutch.setText(getString(R.string.radioBtnNl));

        RadioButton radioBtnEng = (RadioButton) findViewById(R.id.radioBtnEng);
        radioBtnEng.setText(getString(R.string.radioBtnEng));

        changeLocationButton.setText(getString(R.string.locationBtn));
        closestStationButton.setText(getString(R.string.closestBtn));

        TextView closestStation = findViewById(R.id.closestStationTextView);
        closestStation.setText(getString(R.string.closestBtn));

        closestStationButton.setText(getString(R.string.closestBtn));

    }

    // Methode which changes the color scheme of every item in the app. The boolean isDark helps decide whether the ColorScheme that gets used is the one for dark or for light mode.
    public void colorChange(Boolean isDark) {
        if(isDark) {
            helpDarkMode();
            actionBarDarkMode();
            navBarDarkMode();
            settingsDarkMode();
            popDarkMode();
        } else {
            helpLightMode();
            actionBarLightMode();
            navBarLightMode();
            settingsLightMode();
            popWhiteMode();
        }
    }

    // Several methodes which change the [name] fragments/items to their new ColorScheme.
    // For example: navBarDarkMode will change the navBar to the ColorScheme for Dark Mode.
    public void navBarDarkMode() {
        BottomNavigationView botNav = findViewById(R.id.nav_view_main);
        botNav.setBackgroundColor(ContextCompat.getColor(this, R.color.darkThemeNavBar));
        botNav.setItemIconTintList(clrStateListDark);
        botNav.setItemTextColor(clrStateListDark);
    }

    public void popDarkMode(){
        ConstraintLayout popSettings = findViewById(R.id.constraintLayoutPop);

        TextView closestStation = findViewById(R.id.closestStationTextView);
        TextView stationName = findViewById(R.id.StationNameTextView);

        RecyclerView recyclerView = findViewById(R.id.departuresRecyclerView);

        recyclerView.setBackgroundTintList(clrStateListButtonLight);
        recyclerView.setBackgroundColor(0);

        closestStation.setTextColor(ContextCompat.getColor(this, R.color.darkThemeText));
        stationName.setTextColor(ContextCompat.getColor(this, R.color.darkThemeText));

        popSettings.setBackgroundColor(ContextCompat.getColor(this, R.color.darkThemeBackground));
    }

    public void popWhiteMode(){
        ConstraintLayout popSettings = findViewById(R.id.constraintLayoutPop);

        TextView closestStation = findViewById(R.id.closestStationTextView);
        TextView stationName = findViewById(R.id.StationNameTextView);

        RecyclerView recyclerView = findViewById(R.id.departuresRecyclerView);

        recyclerView.setBackgroundTintList(clrStateListButtonLight);

        closestStation.setTextColor(ContextCompat.getColor(this, R.color.whiteThemeText));
        stationName.setTextColor(ContextCompat.getColor(this, R.color.whiteThemeText));

        popSettings.setBackgroundColor(ContextCompat.getColor(this, R.color.whiteThemeBackground));
    }

    public void settingsDarkMode() {
        ConstraintLayout layoutSettings = findViewById(R.id.constraintLayoutSettings);

        TextView textLanguage = findViewById(R.id.txtLanguagePicker);
        TextView textColor = findViewById(R.id.txtColorPicker);

        RadioButton radioBtn1 = (RadioButton) findViewById(R.id.radioBtnWhite);
        RadioButton radioBtn2 = (RadioButton) findViewById(R.id.radioBtnDark);
        RadioButton radioBtn3 = (RadioButton) findViewById(R.id.radioBtnEng);
        RadioButton radioBtn4 = (RadioButton) findViewById(R.id.radioBtnNL);

        radioBtn1.setButtonTintList(clrStateListDark);
        radioBtn2.setButtonTintList(clrStateListDark);
        radioBtn3.setButtonTintList(clrStateListDark);
        radioBtn4.setButtonTintList(clrStateListDark);

        radioBtn1.setTextColor(ContextCompat.getColor(this, R.color.darkThemeText));
        radioBtn2.setTextColor(ContextCompat.getColor(this, R.color.darkThemeText));
        radioBtn3.setTextColor(ContextCompat.getColor(this, R.color.darkThemeText));
        radioBtn4.setTextColor(ContextCompat.getColor(this, R.color.darkThemeText));


        layoutSettings.setBackgroundColor(ContextCompat.getColor(this, R.color.darkThemeBackground));
        textColor.setTextColor(ContextCompat.getColor(this, R.color.darkThemeText));
        textLanguage.setTextColor(ContextCompat.getColor(this, R.color.darkThemeText));
    }

    public void actionBarDarkMode() {
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(this, R.color.darkThemeNavBar)));
    }

    public void helpDarkMode() {
        ConstraintLayout layoutHelp = findViewById(R.id.constraintLayoutHelp);
        layoutHelp.setBackgroundColor(ContextCompat.getColor(this, R.color.darkThemeBackground));

        TextView txtViewSettingsDesc = findViewById(R.id.txtViewSettingsDesc);
        txtViewSettingsDesc.setTextColor(ContextCompat.getColor(this, R.color.darkThemeText));

        TextView txtViewSettingsLangSupport = findViewById(R.id.txtViewSettingsLangSupport);
        txtViewSettingsLangSupport.setTextColor(ContextCompat.getColor(this, R.color.darkThemeText));

        TextView txtViewSettingsColorSupport = findViewById(R.id.txtViewSettingsColorSupport);
        txtViewSettingsColorSupport.setTextColor(ContextCompat.getColor(this, R.color.darkThemeText));

        TextView txtViewSettingsLocationChange = findViewById(R.id.txtViewSettingsLocationChange);
        txtViewSettingsLocationChange.setTextColor(ContextCompat.getColor(this, R.color.darkThemeText));

        TextView txtViewMapHeader = findViewById(R.id.txtViewMapHeader);
        txtViewMapHeader.setTextColor(ContextCompat.getColor(this, R.color.darkThemeText));

        TextView txtViewSettingsHeader = findViewById(R.id.txtViewSettingsHeader);
        txtViewSettingsHeader.setTextColor(ContextCompat.getColor(this, R.color.darkThemeText));
    }

    public void navBarLightMode() {
        BottomNavigationView botNav = findViewById(R.id.nav_view_main);

        botNav.setBackgroundColor(ContextCompat.getColor(this, R.color.lightThemeNavBar));
        botNav.setItemIconTintList(clrStateListNavLight);
        botNav.setItemTextColor(clrStateListNavLight);
    }

    public void settingsLightMode() {
        ConstraintLayout layoutSettings = findViewById(R.id.constraintLayoutSettings);

        TextView textLanguage = findViewById(R.id.txtLanguagePicker);
        TextView textColor = findViewById(R.id.txtColorPicker);

        RadioButton radioBtn1 = (RadioButton) findViewById(R.id.radioBtnWhite);
        RadioButton radioBtn2 = (RadioButton) findViewById(R.id.radioBtnDark);
        RadioButton radioBtn3 = (RadioButton) findViewById(R.id.radioBtnEng);
        RadioButton radioBtn4 = (RadioButton) findViewById(R.id.radioBtnNL);

        radioBtn1.setButtonTintList(clrStateListButtonLight);
        radioBtn2.setButtonTintList(clrStateListButtonLight);
        radioBtn3.setButtonTintList(clrStateListButtonLight);
        radioBtn4.setButtonTintList(clrStateListButtonLight);

        radioBtn1.setTextColor(ContextCompat.getColor(this, R.color.whiteThemeText));
        radioBtn2.setTextColor(ContextCompat.getColor(this, R.color.whiteThemeText));
        radioBtn3.setTextColor(ContextCompat.getColor(this, R.color.whiteThemeText));
        radioBtn4.setTextColor(ContextCompat.getColor(this, R.color.whiteThemeText));


        layoutSettings.setBackgroundColor(ContextCompat.getColor(this, R.color.whiteThemeBackground));
        textColor.setTextColor(ContextCompat.getColor(this, R.color.whiteThemeText));
        textLanguage.setTextColor(ContextCompat.getColor(this, R.color.whiteThemeText));
    }

    public void actionBarLightMode() {
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(this, R.color.colorPrimary)));
    }

    public void helpLightMode() {
        ConstraintLayout layoutHelp = findViewById(R.id.constraintLayoutHelp);
        layoutHelp.setBackgroundColor(ContextCompat.getColor(this, R.color.whiteThemeBackground));

        TextView txtViewSettingsDesc = findViewById(R.id.txtViewSettingsDesc);
        txtViewSettingsDesc.setTextColor(ContextCompat.getColor(this, R.color.whiteThemeText));

        TextView txtViewSettingsLangSupport = findViewById(R.id.txtViewSettingsLangSupport);
        txtViewSettingsLangSupport.setTextColor(ContextCompat.getColor(this, R.color.whiteThemeText));

        TextView txtViewSettingsColorSupport = findViewById(R.id.txtViewSettingsColorSupport);
        txtViewSettingsColorSupport.setTextColor(ContextCompat.getColor(this, R.color.whiteThemeText));

        TextView txtViewSettingsLocationChange = findViewById(R.id.txtViewSettingsLocationChange);
        txtViewSettingsLocationChange.setTextColor(ContextCompat.getColor(this, R.color.whiteThemeText));

        TextView txtViewMapHeader = findViewById(R.id.txtViewMapHeader);
        txtViewMapHeader.setTextColor(ContextCompat.getColor(this, R.color.whiteThemeText));

        TextView txtViewSettingsHeader = findViewById(R.id.txtViewSettingsHeader);
        txtViewSettingsHeader.setTextColor(ContextCompat.getColor(this, R.color.whiteThemeText));
    }

    @Override
    public void onPause() {
        super.onPause();

        //stop location updates when Activity is no longer active
        if (fusedLocationClient != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        UiSettings settings = mMap.getUiSettings();
        settings.setZoomControlsEnabled(true);
        settings.setTiltGesturesEnabled(true);
        settings.setMyLocationButtonEnabled(true);

        // Add a marker in current location and move the camera
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }

        // Setting onclick event listener for the map
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng point) {


                if (locationChangable){

                    // Creating MarkerOptions
                    MarkerOptions options = new MarkerOptions();

                    // Setting the position of the marker
                    options.position(point);

                    // Already one locations
                    if (markerPoints.size() > 0) {
                        markerPoints.clear();
                        mMap.clear();
                    }

                    // Adding new item to the ArrayList
                    markerPoints.add(point);
                    // Creating MarkerOptions
                    Log.d("CHANGE_LOCATION", "changing current location to lat/lng: " + point.latitude + " " + point.longitude);

                    // Setting the location coordinate
                    mLastLocation.setLongitude(point.longitude);
                    mLastLocation.setLatitude(point.latitude);
                    options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                    mMap.addMarker(options);

                }
            }
        });
    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Asking user if explanation is needed
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                //Prompt the user once explanation has been shown
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted. Do the
                    // contacts-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }

                } else {

                    // Permission denied, Disable the functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }

            // other 'case' lines to check for other permissions this app might request.
            // You can add here other case statements according to your requirement.
        }
    }

    // todo - migrate GoogleApiClient to GoogleApi
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        locationRequest = new LocationRequest()
                .setInterval(1000)
                .setFastestInterval(1000)
                .setSmallestDisplacement(5)
                .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            Task<Void> task = fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
            Log.d(TAG, "onConnected: requestLocationUpdates is " + task.isSuccessful());

        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }


    /**
     * Creates a callback for receiving location events.
     */
    private void createLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (!locationChangable){
                    for (Location location : locationResult.getLocations()) {
                        Log.i("MapsActivity", "Location: " + location.getLatitude() + " " + location.getLongitude());
                        mLastLocation = location;
                        if (mCurrLocationMarker != null) {
                            mCurrLocationMarker.remove();
                        }

                        //Place current location marker
                        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                        MarkerOptions markerOptions = new MarkerOptions();
                        markerOptions.position(latLng);
                        markerOptions.title("Current Position");
                        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
                        mCurrLocationMarker = mMap.addMarker(markerOptions);

                        //move map camera
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

                    }
                }

            }
        };
    }

    private String getUrl(LatLng origin, LatLng dest) {
        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // Detination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

        String trafficMode = "mode=walking";
//        String trafficMode = "mode=driving";


        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + trafficMode + "&key=" + MY_API_KEY;

        // Output format
        String output = "/json";

        // Building the url to the web service
        //String url = "https://maps.googleapis.com/maps/api/directions" + output + "?" + parameters;
        String url = "http://145.48.6.80:3000/directions" + "?" + parameters; // Via onze eigen quota server


        return url;
    }

    private class FetchUrl extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try {
                // Fetching the data from web service
                data = downloadUrl(url[0]);
                Log.d("Background Task data", data);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);

        }


        private String downloadUrl(String strUrl) throws IOException {
            String data = "";
            InputStream iStream = null;
            HttpURLConnection urlConnection = null;
            try {
                URL url = new URL(strUrl);

                // Creating an http connection to communicate with url
                urlConnection = (HttpURLConnection) url.openConnection();

                // Connecting to url
                urlConnection.connect();

                // Reading data from url
                iStream = urlConnection.getInputStream();

                BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

                StringBuffer sb = new StringBuffer();

                String line = "";
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }

                data = sb.toString();
                Log.d("downloadUrl", data);
                br.close();

            } catch (Exception e) {
                Log.d("Exception", e.toString());
            } finally {
                iStream.close();
                urlConnection.disconnect();
            }
            return data;
        }

        private class ParserTask extends AsyncTask<String, Integer, List<List<LatLng>>> {

            // Parsing the data in non-ui thread
            @Override
            protected List<List<LatLng>> doInBackground(String... jsonData) {

                JSONObject jObject;
                List<List<LatLng>> routeData = null;

                try {
                    jObject = new JSONObject(jsonData[0]);

                    Log.d("ParserTask", jsonData[0]);
                    DataParser parser = new DataParser();
                    Log.d("ParserTask", parser.toString());

                    // Starts parsing routes data
                    routeData = parser.parseRoutesInfo(jObject);
                    Log.d("ParserTask", "Executing routes");
                    Log.d("ParserTask-routes: ", routeData.toString());

                } catch (Exception e) {
                    Log.d("ParserTask", e.toString());
                    e.printStackTrace();
                }

                return routeData;
            }

            @Override
            protected void onPostExecute(List<List<LatLng>> result) {
                if (result == null)
                    return;

                PolylineOptions lineOptions = new PolylineOptions();

                LatLng northEast = result.get(0).get(0);
                LatLng southWest = result.get(0).get(1);

                result.remove(0);

                for (List<LatLng> leg : result) {
                    lineOptions.addAll(leg);
                }

                lineOptions.width(12);
                lineOptions.color(Color.rgb(153, 187, 255));

                Log.d("onPostExecute", "onPostExecute lineoptions decoded");

                if (lineOptions != null) {
                    LatLngBounds bounds = new LatLngBounds(southWest, northEast);
                    mMap.addPolyline(lineOptions);
                } else {
                    Log.d("onPostExecute", "without Polylines drawn");
                }
            }

        }

}
}