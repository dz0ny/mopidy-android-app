package com.mopidy.dz0ny.mopidy.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.mopidy.dz0ny.mopidy.R;
import com.mopidy.dz0ny.mopidy.api.AutoUpdate;
import com.mopidy.dz0ny.mopidy.api.Mopidy;
import com.mopidy.dz0ny.mopidy.services.Discovery;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardArrayAdapter;
import it.gmariotti.cardslib.library.internal.CardHeader;
import it.gmariotti.cardslib.library.internal.CardThumbnail;
import it.gmariotti.cardslib.library.internal.base.BaseCard;
import it.gmariotti.cardslib.library.view.CardListView;


public class PlayerDiscovery extends Activity implements SwipeRefreshLayout.OnRefreshListener {

    ArrayList<Card> cards = new ArrayList<Card>();
    ArrayList<String> hosts = new ArrayList<String>();
    CardArrayAdapter mCardArrayAdapter;
    @InjectView(R.id.myList)
    CardListView listView;
    @InjectView(R.id.swipe_container)
    SwipeRefreshLayout swipe_container;
    private BroadcastReceiver OnRefreshReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Mopidy iapp = intent.getParcelableExtra("app");

            if (!hosts.contains(iapp.getURL())) {

                cards.add(PlayerCard(iapp));
                mCardArrayAdapter.notifyDataSetChanged();
                hosts.add(iapp.getURL());
            }


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
        swipe_container.setColorScheme(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        registerReceivers();

        AutoUpdate.check(this);

        mCardArrayAdapter = new CardArrayAdapter(this, cards);
        listView.setEmptyView(findViewById(R.id.myListinfo));
        listView.setAdapter(mCardArrayAdapter);
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
        Discovery.Start(this);
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
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onRefresh() {
        Discovery.Stop(this);
        hosts.clear();
        cards.clear();
        mCardArrayAdapter.notifyDataSetChanged();
        Discovery.Start(getContext());
    }
}
