package com.example.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import com.example.MainActivity;
import com.example.interfaces.OnFragmentInteractionListener;
import com.example.treintracker.R;

import java.util.Locale;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SettingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SettingsFragment extends Fragment {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private RadioButton lang;
    private RadioButton theme;

    private MainActivity mainActivity;

    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    private OnFragmentInteractionListener mListener;

    public SettingsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment SettingsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SettingsFragment newInstance() {
        SettingsFragment fragment = new SettingsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        // Creating a variable for the MainActivity class, which well get used later on to call some methods in MainActivity.
        mainActivity = ((MainActivity)getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstance) {
        super.onViewCreated(view, savedInstance);

        // Initializing the RadioGroups which contains the RadioButttons used for selecting the language and the Colorscheme
        RadioGroup radiogroupLanguage = view.findViewById(R.id.radioGroupLanguage);
        RadioGroup radioGroupColor = view.findViewById(R.id.radioGroupColor);

        preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        SharedPreferences.Editor editor = preferences.edit();

        String language = preferences.getString("language", null);
        if (language != null){
            Locale locale = new Locale(language);
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            getActivity().getBaseContext().getResources().updateConfiguration(config, getActivity().getBaseContext().getResources().getDisplayMetrics());

            if (language.equals("nl")){
                lang = radiogroupLanguage.findViewById(R.id.radioBtnNL);
                lang.toggle();
            } else {
                lang = radiogroupLanguage.findViewById(R.id.radioBtnEng);
                lang.toggle();
            }

        }

        String color = preferences.getString("color", null);
        if (color != null){
            Locale locale2 = new Locale(color);
            Locale.setDefault(locale2);
            Configuration config2 = new Configuration();
            config2.locale = locale2;
            getActivity().getBaseContext().getResources().updateConfiguration(config2, getActivity().getBaseContext().getResources().getDisplayMetrics());

            if (color.equals("dark")){
                theme = radioGroupColor.findViewById(R.id.radioBtnDark);
                theme.toggle();
            } else {
                theme = radioGroupColor.findViewById(R.id.radioBtnWhite);
                theme.toggle();
            }
        }


        // Setting up the onCheckedChangeListener, in which you can program what should be done when which button has been checked.
        radiogroupLanguage.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedID) {
                // Get the RadioButton that has been selected.
                lang = (RadioButton) radioGroup.findViewById(checkedID);


                // Confirm that a RadioButton has been selected
                if(lang!=null) {

                    // Switch case which handles the different buttons
                    switch(checkedID) {
                        case R.id.radioBtnEng:
                            // Changing the locale in the settings to English, as that is the button which has been selected if this code runs.
                            Locale localeEN = new Locale("en");
                            Locale.setDefault(localeEN);
                            Configuration configEN = new Configuration();
                            configEN.locale = localeEN;
                            getActivity().getBaseContext().getResources().updateConfiguration(configEN, getActivity().getBaseContext().getResources().getDisplayMetrics());

                            editor.putString("language", "en");
                            editor.commit();

                            // Display a Toast for debug purposes, which shows to which language the Locale has been changed.
                            Toast.makeText(getActivity().getBaseContext(), getResources().getString(R.string.currentLanguagePopup), Toast.LENGTH_SHORT).show();

                            // Calling the method in the MainActivity class, which can be used to update all the textvalues in the program to the ones matching the new Locale.
                            mainActivity.languageUpdate();
                            break;
                        case R.id.radioBtnNL:
                            // Changing the locale in the settings to Dutch, as that is the button which has been selected if this code runs.
                            Locale localeNL = new Locale("nl");
                            Locale.setDefault(localeNL);
                            Configuration configNL = new Configuration();
                            configNL.locale = localeNL;
                            getActivity().getBaseContext().getResources().updateConfiguration(configNL, getActivity().getBaseContext().getResources().getDisplayMetrics());

                            editor.putString("language", "nl");
                            editor.commit();

                            // Display a Toast for debug purposes, which shows to which language the Locale has been changed.
                            Toast.makeText(getActivity().getBaseContext(), getResources().getString(R.string.currentLanguagePopup), Toast.LENGTH_SHORT).show();

                            // Calling the method in the MainActivity class, which can be used to update all the textvalues in the program to the ones matching the new Locale.
                            mainActivity.languageUpdate();
                            break;
                        default:
                            break;
                    }
                }
            }
        });

        // Setting up the onCheckedChangeListener, in which you can program what should be done when which button has been checked.
        radioGroupColor.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                // Get the RadioButton that has been selected
                theme = (RadioButton) radioGroupColor.findViewById(i);

                // Confirm that a RadioButton has been selected
                if(theme!=null) {

                    // Switch case which handles the different buttons
                    switch(i) {
                        case R.id.radioBtnDark:
                            // Calling the method in the MainActivity class, which can be used to update all the Colorvalues in the program to the ones matching the new Colorscheme (black/white)
                            editor.putString("color", "dark");
                            editor.commit();
                            mainActivity.colorChange(true);
                            break;
                        case R.id.radioBtnWhite:
                            // Calling the method in the MainActivity class, which can be used to update all the Colorvalues in the program to the ones matching the new Colorscheme (black/white)
                            editor.putString("color", "white");
                            editor.commit();
                            mainActivity.colorChange(false);
                            break;
                        default:
                            break;
                    }
                }
            }
        });
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
