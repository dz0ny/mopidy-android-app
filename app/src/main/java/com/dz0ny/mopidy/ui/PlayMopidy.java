package com.dz0ny.mopidy.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.dz0ny.mopidy.R;
import com.dz0ny.mopidy.api.Mopidy;
import com.dz0ny.mopidy.resolvers.Resolver;
import com.dz0ny.mopidy.resolvers.SoundCloud;
import com.dz0ny.mopidy.resolvers.Spotify;
import com.dz0ny.mopidy.resolvers.Youtube;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
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

    @InjectView(R.id.clear)
    CheckBox clear;

    MopidyAdapter players;
    private List<Resolver> resolvers = new ArrayList<Resolver>() {};
    private String content_url;
    private static Pattern URL_PATTERN = Pattern.compile("((?:http|https)(?::\\/{2}[\\w]+)(?:[\\/|\\.]?)(?:[^\\s\"]*))", Pattern.CASE_INSENSITIVE);
    private Mopidy selected_app;

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
            if (players.isEmpty()) {
                send.setVisibility(View.GONE);
                selector.setVisibility(View.GONE);
                refresher.setVisibility(View.VISIBLE);
            } else {
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

        // Get intent, action and MIME type
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
                Timber.i("Text for this intent is '%s'", sharedText);
                // http://regex101.com/r/uY9tV4/2
                content_url = getUrlFromText(sharedText);
                Timber.i("Url for this intent is %s", content_url);
            }
        } else {
            Toast.makeText(this, "This content is not supported!", Toast.LENGTH_LONG).show();
            finish();
        }


        players = new MopidyAdapter(this);
        players_select.setAdapter(players);
        players_select.setOnItemSelectedListener(this);
        players.setDropDownViewResource(R.layout.player_card);
        addManualApps(players);
        resolvers.add(new Youtube(getContext()));
        resolvers.add(new SoundCloud(getContext()));
        resolvers.add(new Spotify(getContext()));
    }

    private Context getContext() {
        return this;
    }

    public String getUrlFromText(String sharedText) {
        Matcher m = URL_PATTERN.matcher(sharedText);
        if (m.find())
            return m.group(1);
        else
            return null;
    }

    @Override
    protected void onResume() {
        super.onResume();
        addManualApps(players);
    }

    @OnClick(R.id.send)
    public  void submit() {
        if (clear.isChecked()){
            selected_app.tracklistClear(getContext());
        }
        selected_app.tracklistAdd(getContext(), content_url);
        selected_app.play(getContext());
        finish();
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

        for (Resolver site : resolvers) {
            if (site.canResolve(URI.create(content_url))) {
                if (site.canPlay(players.getItem(i))) {
                    selected_app = players.getItem(i);
                    send.setVisibility(View.VISIBLE);
                    content_url = site.resolvedURI(content_url);
                    return;
                }
            }
        }
        send.setVisibility(View.GONE);
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        send.setVisibility(View.GONE);
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