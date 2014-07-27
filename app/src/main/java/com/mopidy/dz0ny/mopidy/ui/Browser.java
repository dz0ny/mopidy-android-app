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
import android.widget.Toast;

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


public class Browser extends Activity {

    ArrayList<Mopidy> devices = new ArrayList<Mopidy>();
    private BroadcastReceiver appNewMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Mopidy app = intent.getParcelableExtra("app");
            Timber.i("New device %s version '%s'.", app.getName(), app.getVersion(getContext()));
            for (Mopidy iapp : devices) {
                if (iapp.getURL().equalsIgnoreCase(app.getURL())) {
                    return;
                }
            }
            devices.add(app);
            cards.add(PlayerCard(app.getName(), app.getVersion(getContext()), app.getURL()));
            mCardArrayAdapter.notifyDataSetChanged();

        }
    };
    ArrayList<Card> cards = new ArrayList<Card>();
    CardArrayAdapter mCardArrayAdapter;
    @InjectView(R.id.myList)
    CardListView listView;

    private Card PlayerCard(String name, String version, final String url) {

        PlayerCardFactory card = new PlayerCardFactory(this);

        CardHeader header = new CardHeader(getContext());
        header.setButtonOverflowVisible(true);
        header.setTitle(name);
        header.setPopupMenu(R.menu.browser, new CardHeader.OnClickCardHeaderPopupMenuListener() {
            @Override
            public void onMenuItemClick(BaseCard baseCard, MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.action_browser) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(browserIntent);
                }

                if (menuItem.getItemId() == R.id.action_qr) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getQR(url)));
                    startActivity(browserIntent);
                }

            }
        });

        card.addCardHeader(header);


        CardThumbnail cardThumbnail = new CardThumbnail(this);
        cardThumbnail.setDrawableResource(R.drawable.ic_launcher);
        card.addCardThumbnail(cardThumbnail);

        card.setTitle(url);
        card.setSecondaryTitle(version);

        card.setShadow(true);
        card.setClickable(true);
        card.setOnClickListener(new Card.OnCardClickListener() {
            @Override
            public void onClick(Card card, View view) {
                Toast.makeText(getContext(), "Click Listener card=" + card.getTitle(), Toast.LENGTH_SHORT).show();
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
        setContentView(R.layout.activity_browser);
        ButterKnife.inject(this);

        registerReceivers();

        mCardArrayAdapter = new CardArrayAdapter(this, cards);
        listView.setEmptyView(findViewById(R.id.empty_list_item));
        listView.setAdapter(mCardArrayAdapter);
    }

    private void registerReceivers() {
        setProgressBarIndeterminateVisibility(true);
        LocalBroadcastManager.getInstance(this).registerReceiver(
                appNewMessageReceiver, new IntentFilter(Discovery.Added));
    }

    public Context getContext() {
        return this;
    }

}
