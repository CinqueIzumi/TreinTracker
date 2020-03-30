package com.example;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.data.Departure;
import com.example.interfaces.DeparturesApiListener;
import com.example.treintracker.R;

import java.util.ArrayList;

public class DepartureAdapter extends RecyclerView.Adapter<DepartureAdapter.MyViewHolder> {

    private ArrayList<Departure> dataset;

    public DepartureAdapter(ArrayList<Departure> dataset) {
        this.dataset = dataset;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.departure_item, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Departure departure = dataset.get(position);

        holder.direction.setText(departure.getDirection());
        holder.time.setText(departure.getTime());
        holder.track.setText(departure.getTrack());
    }

    @Override
    public int getItemCount() {
        return dataset.size();
    }


    public class MyViewHolder extends RecyclerView.ViewHolder {

        public TextView direction;
        public TextView time;
        public TextView track;

        public View layout;

        // Dit is een rij in de recycler view
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            this.layout = itemView;
            direction = (TextView) itemView.findViewById(R.id.departureNameTextView);
            time = (TextView) itemView.findViewById(R.id.timeTextView);
            track = (TextView) itemView.findViewById(R.id.trackTextView);
        }
    }
}
