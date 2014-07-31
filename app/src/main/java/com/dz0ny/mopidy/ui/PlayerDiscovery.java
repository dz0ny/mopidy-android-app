package com.dz0ny.mopidy.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.EditText;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.dz0ny.mopidy.R;
import com.dz0ny.mopidy.api.AutoUpdate;
import com.dz0ny.mopidy.api.Mopidy;
import com.dz0ny.mopidy.services.Discovery;
import com.nhaarman.listviewanimations.swinginadapters.AnimationAdapter;
import com.nhaarman.listviewanimations.swinginadapters.prepared.SwingBottomInAnimationAdapter;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;

import butterknife.ButterKnife;
import butterknife.InjectView;
import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardArrayAdapter;
import it.gmariotti.cardslib.library.internal.CardHeader;
import it.gmariotti.cardslib.library.internal.CardThumbnail;
import it.gmariotti.cardslib.library.internal.base.BaseCard;
import it.gmariotti.cardslib.library.view.CardListView;
import timber.log.Timber;


public class PlayerDiscovery extends Activity implements SwipeRefreshLayout.OnRefreshListener, Card.OnSwipeListener {

    ArrayList<Card> cards = new ArrayList<Card>();
    HashSet<String> hosts = new HashSet<String>();
    HashSet<String> manually_added = new HashSet<String>();
    CardArrayAdapter mCardArrayAdapter;
    @InjectView(R.id.myList)
    CardListView listView;
    @InjectView(R.id.swipe_container)
    SwipeRefreshLayout swipe_container;
    private BroadcastReceiver OnRefreshReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Mopidy iapp = intent.getParcelableExtra("app");
            addAppToList(iapp, false);
        }
    };
    private BroadcastReceiver OnStartReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            swipe_container.setRefreshing(true);
        }
    };
    private BroadcastReceiver OnStopReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            swipe_container.setRefreshing(false);
        }
    };

    private void addAppToList(Mopidy app, boolean save) {
        if (!hosts.contains(app.getURL())) {

            cards.add(PlayerCard(app));
            mCardArrayAdapter.notifyDataSetChanged();
            hosts.add(app.getURL());
            if (save) {
                manually_added.add(app.getURL());
                SharedPreferences settings = getSharedPreferences("apps", 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString(app.getURL(), app.getJSON());
                editor.putStringSet("hosts", manually_added);
                // Commit the edits!
                editor.apply();
            }
        }
    }

    private Card PlayerCard(final Mopidy app) {

        PlayerCardFactory card = new PlayerCardFactory(this);

        CardHeader header = new CardHeader(getContext());
        header.setButtonOverflowVisible(true);
        header.setTitle(app.getName());
        header.setPopupMenu(R.menu.player_discovery, new CardHeader.OnClickCardHeaderPopupMenuListener() {
            @Override
            public void onMenuItemClick(BaseCard baseCard, MenuItem menuItem) {
                Uri path;
                switch (menuItem.getItemId()) {
                    case R.id.action_browser:
                        path = Uri.parse(app.getURL());
                        break;
                    case R.id.action_qr:
                        path = Uri.parse(getQR(app.getURL()));
                        break;
                    default:
                        return;
                }
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, path);
                startActivity(browserIntent);
            }
        });

        card.addCardHeader(header);


        CardThumbnail cardThumbnail = new CardThumbnail(this);
        cardThumbnail.setDrawableResource(R.drawable.ic_launcher);
        card.addCardThumbnail(cardThumbnail);

        card.setTitle(app.getURL());
        card.setSecondaryTitle(app.getVersion(getContext()));
        card.setSwipeable(true);
        card.setShadow(true);
        card.setClickable(true);
        card.setOnClickListener(new Card.OnCardClickListener() {
            @Override
            public void onClick(Card card, View view) {
                Intent i = new Intent(getContext(), Browser.class);
                i.putExtra("app", app);
                startActivity(i);
            }
        });
        card.setOnSwipeListener(this);
        return card;
    }

    private String getQR(String url) {
        return "http://chart.apis.google.com/chart?cht=qr&chs=400x400&chl=" + url;
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discovery);
        ButterKnife.inject(this);

        swipe_container.setOnRefreshListener(this);
        swipe_container.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);


        mCardArrayAdapter = new CardArrayAdapter(this, cards);
        listView.setEmptyView(findViewById(R.id.myListinfo));
        AnimationAdapter animCardArrayAdapter = new SwingBottomInAnimationAdapter(mCardArrayAdapter);
        animCardArrayAdapter.setAbsListView(listView);
        listView.setExternalAdapter(animCardArrayAdapter, mCardArrayAdapter);
        addManualApps();
        registerReceivers();
        AutoUpdate.check(this);
    }

    private void addManualApps() {
        SharedPreferences settings = getSharedPreferences("apps", 0);
        HashSet<String> saved_apps = (HashSet<String>) settings.getStringSet("hosts", null);
        if (saved_apps != null) {
            for (String saved_app : saved_apps) {
                String app_string = settings.getString(saved_app, null);
                if (app_string != null) {
                    Timber.i("Saved app %s", app_string);
                    Gson gson = new GsonBuilder().create();
                    Mopidy app = gson.fromJson(app_string, Mopidy.class);
                    addAppToList(app, true);
                }

            }
        }

    }

    private void registerReceivers() {
        LocalBroadcastManager.getInstance(this).registerReceiver(
                OnRefreshReceiver, new IntentFilter(Discovery.OnRefresh));
        LocalBroadcastManager.getInstance(this).registerReceiver(
                OnStopReceiver, new IntentFilter(Discovery.OnStop));
        LocalBroadcastManager.getInstance(this).registerReceiver(
                OnStartReceiver, new IntentFilter(Discovery.OnStart));
    }

    @Override
    protected void onResume() {
        super.onResume();
        //Discovery.Start(this);
    }

    @Override
    protected void onPause() {
        Discovery.Stop(this);
        super.onPause();
    }

    public Context getContext() {
        return this;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.discovery, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_add:
                showAddDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showAddDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        @SuppressLint("InflateParams") View add_view = getLayoutInflater().inflate(R.layout.add_player, null);
        final EditText player_name = ButterKnife.findById(add_view, R.id.name);
        final EditText player_host = ButterKnife.findById(add_view, R.id.host);
        builder.setView(add_view);
        player_name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            // after every change has been made to this editText, we would like to check validity
            public void afterTextChanged(Editable s) {
                isValidName(player_name);
            }
        });
        player_host.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            // after every change has been made to this editText, we would like to check validity
            public void afterTextChanged(Editable s) {
                isValidHost(player_host);
            }
        });
        builder.setPositiveButton("Add", null);
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });

        builder.setTitle("Add Mopidy");
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isValidHost(player_host) && isValidName(player_name)) {
                    try {
                        URI host = URI.create(player_host.getText().toString());
                        Mopidy app = new Mopidy(
                                player_name.getText().toString(),
                                host.getHost(),
                                host.getPort()
                        );
                        if (app.getVersion(getContext()).equalsIgnoreCase("Unknown")) {
                            player_host.setError("Mopidy is not responding!");
                        } else {
                            addAppToList(app, true);
                            alertDialog.dismiss();
                        }

                    } catch (Exception e) {
                        player_host.setError("Invalid URL!");
                    }
                }

            }
        });

    }

    private boolean isValidHost(EditText e) {
        String text = e.getText().toString().trim();
        e.setError(null);

        // length 0 means there is no text
        if (text.length() == 0) {
            e.setError("Required!");
        }


        if (!URLUtil.isValidUrl(text)){
            e.setError("Invalid URL!");
        }

        return e.getError() == null;

    }

    private boolean isValidName(EditText e) {
        String text = e.getText().toString().trim();
        e.setError(null);

        // length 0 means there is no text
        if (text.length() == 0) {
            e.setError("Required!");
        }
        return e.getError() == null;
    }

    @Override
    public void onRefresh() {
        Discovery.Stop(this);
        hosts.clear();
        cards.clear();
        addManualApps();
        mCardArrayAdapter.notifyDataSetChanged();
        Discovery.Start(getContext());
    }

    @Override
    public void onSwipe(Card card) {
        if (manually_added.contains(card.getTitle())){
            manually_added.remove(card.getTitle());
            SharedPreferences settings = getSharedPreferences("apps", 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.remove(card.getTitle());
            editor.putStringSet("hosts", manually_added);
            // Commit the edits!
            editor.apply();
        }
    }
}
