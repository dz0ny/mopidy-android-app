package com.mopidy.dz0ny.mopidy.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;

import com.mopidy.dz0ny.mopidy.R;
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
import timber.log.Timber;


public class PlayerDiscovery extends Activity {

    private BroadcastReceiver appNewMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ArrayList<Mopidy> devices = intent.getParcelableArrayListExtra("devices");
            cards.clear();
            for (Mopidy app : devices) {
                cards.add(PlayerCard(app));
            }
            mCardArrayAdapter.notifyDataSetChanged();

        }
    };
    ArrayList<Card> cards = new ArrayList<Card>();
    CardArrayAdapter mCardArrayAdapter;
    @InjectView(R.id.myList)
    CardListView listView;

    private Card PlayerCard(final Mopidy app) {

        PlayerCardFactory card = new PlayerCardFactory(this);

        CardHeader header = new CardHeader(getContext());
        header.setButtonOverflowVisible(true);
        header.setTitle(app.getName());
        header.setPopupMenu(R.menu.player_discovery, new CardHeader.OnClickCardHeaderPopupMenuListener() {
            @Override
            public void onMenuItemClick(BaseCard baseCard, MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.action_browser) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(app.getURL()));
                    startActivity(browserIntent);
                }

                if (menuItem.getItemId() == R.id.action_qr) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getQR(app.getURL())));
                    startActivity(browserIntent);
                }

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
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discovery);
        ButterKnife.inject(this);

        registerReceivers();

        mCardArrayAdapter = new CardArrayAdapter(this, cards);
        listView.setEmptyView(findViewById(R.id.empty_list_item));
        listView.setAdapter(mCardArrayAdapter);
    }

    private void registerReceivers() {
        setProgressBarIndeterminateVisibility(true);
        LocalBroadcastManager.getInstance(this).registerReceiver(
                appNewMessageReceiver, new IntentFilter(Discovery.OnRefresh));
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = new Intent(this, Discovery.class);
        startService(intent);
    }

    public Context getContext() {
        return this;
    }

}
