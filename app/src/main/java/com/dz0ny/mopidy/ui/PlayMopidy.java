package com.dz0ny.mopidy.ui;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.dz0ny.mopidy.R;
import com.dz0ny.mopidy.api.Mopidy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.HashSet;

import butterknife.ButterKnife;
import butterknife.InjectView;
import timber.log.Timber;



public class PlayMopidy extends Activity implements AdapterView.OnItemSelectedListener {
    @InjectView(R.id.to)
    Spinner players_select;
    @InjectView(R.id.send)
    ImageView send;
    @InjectView(R.id.selector)
    LinearLayout selector;
    @InjectView(R.id.refresher)
    LinearLayout refresher;

    MopidyAdapter players;

    private void addManualApps(ArrayAdapter holder) {
        SharedPreferences settings = getSharedPreferences("apps", 0);
        HashSet<String> saved_apps = (HashSet<String>) settings.getStringSet("hosts", null);
        players.clear();
        if (saved_apps != null) {
            for (String saved_app : saved_apps) {
                String app_string = settings.getString(saved_app, null);
                if (app_string != null) {
                    Timber.i("Saved app %s", app_string);
                    Gson gson = new GsonBuilder().create();
                    Mopidy app = gson.fromJson(app_string, Mopidy.class);
                    holder.add(app);
                }

            }
            holder.notifyDataSetChanged();
            if (players.isEmpty()){
                send.setVisibility(View.GONE);
                selector.setVisibility(View.GONE);
                refresher.setVisibility(View.VISIBLE);
            }else{
                send.setVisibility(View.VISIBLE);
                selector.setVisibility(View.VISIBLE);
                refresher.setVisibility(View.GONE);
            }
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_mopidy);
        ButterKnife.inject(this);
        players = new MopidyAdapter(this);
        players_select.setAdapter(players);
        players_select.setOnItemSelectedListener(this);
        players.setDropDownViewResource(R.layout.player_card);
        addManualApps(players);
    }

    @Override
    protected void onResume() {
        super.onResume();
        addManualApps(players);
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        send.setEnabled(true);
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        send.setEnabled(false);
    }
}

class MopidyAdapter extends ArrayAdapter<Mopidy> {
    public MopidyAdapter(Context context) {
        super(context, R.layout.mopidy_item_simple);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Mopidy app = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.mopidy_item_simple, parent, false);
        }
        // Lookup view for data population
        TextView playerName = (TextView) convertView.findViewById(R.id.name);
        // Populate the data into the template view using the data object
        playerName.setText(app.getName());
        // Return the completed view to render on screen
        return convertView;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Mopidy app = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.mopidy_item_dropdown, parent, false);
        }
        // Lookup view for data population
        TextView playerName = (TextView) convertView.findViewById(R.id.name);
        TextView playerAddress = (TextView) convertView.findViewById(R.id.address);
        // Populate the data into the template view using the data object
        playerName.setText(app.getName());
        playerAddress.setText(app.getHost());
        // Return the completed view to render on screen
        return convertView;
    }
}