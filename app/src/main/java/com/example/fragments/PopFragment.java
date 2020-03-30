package com.example.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.DepartureAdapter;
import com.example.DeparturesManager;
import com.example.MainActivity;
import com.example.StationManager;
import com.example.data.Departure;
import com.example.data.Station;
import com.example.interfaces.DeparturesApiListener;
import com.example.interfaces.OnFragmentInteractionListener;
import com.example.interfaces.StationsApiListener;
import com.example.treintracker.R;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link PopFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PopFragment extends Fragment implements DeparturesApiListener, StationsApiListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private TextView title;
    private TextView stationName;
    private RecyclerView departureRecyclerView;
    private Button navigateButton;

    private MainActivity mainActivity;

    private Station closestStation;
    ArrayList<Departure> departures;

    private OnFragmentInteractionListener mListener;

    DepartureAdapter adapter;
    private StationManager stationManager;
    private DeparturesManager departuresManager;

    public PopFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment PopFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PopFragment newInstance(String param1, String param2) {
        PopFragment fragment = new PopFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mainActivity = ((MainActivity)getActivity());

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_pop, container, false);
        super.onViewCreated(view, savedInstanceState);

        departures = new ArrayList<>();
        this.title = view.findViewById(R.id.closestStationTextView);
        this.stationName = view.findViewById(R.id.StationNameTextView);
        this.departureRecyclerView = view.findViewById(R.id.departuresRecyclerView);
        this.navigateButton = view.findViewById(R.id.navigateButton);

        navigateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO: Add working listener for MainActivity
                mListener.onFragmentInteraction(closestStation);
                }
                //close
        });




        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onDeparturesAvailable(Departure departure) {
        departures.add(departure);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onDeparturesError(Error error) {
        error.printStackTrace();
    }

    @Override
    public void onStationAvailable(Station station) {
        this.closestStation = station;

        departuresManager = new DeparturesManager(getContext(), this);
        departuresManager.getDepartures(closestStation.getCode());

        stationName.setText(closestStation.getFullName());

        departureRecyclerView.setLayoutManager(new GridLayoutManager(
                getContext(), 1, GridLayoutManager.VERTICAL, false)
        );
        adapter = new DepartureAdapter(departures);
        departureRecyclerView.setAdapter(adapter);


    }

    @Override
    public void onStationError(Error error) {
        error.printStackTrace();
    }

    public void sendStation(Station station){

        this.departures.clear();

        this.stationManager = new StationManager(getContext(), this);
        stationManager.getClosestStation(station.getLat(), station.getLng());

        if (closestStation != null){
            title.setText(closestStation.getFullName());
        }

    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
}
