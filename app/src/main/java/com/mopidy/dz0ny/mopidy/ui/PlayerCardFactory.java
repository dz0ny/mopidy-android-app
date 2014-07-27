package com.mopidy.dz0ny.mopidy.ui;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mopidy.dz0ny.mopidy.R;

import butterknife.ButterKnife;
import butterknife.InjectView;
import it.gmariotti.cardslib.library.internal.Card;

public class PlayerCardFactory extends Card {

    @InjectView(R.id.inner_title)
    TextView inner_title;
    @InjectView(R.id.secondary_title)
    TextView secondary_title;
    private String secondaryTitle;
    private String primaryTitle;

    public PlayerCardFactory(Context context) {
        this(context, R.layout.player_card);
    }

    public PlayerCardFactory(Context context, int innerLayout) {
        super(context, innerLayout);
    }

    @Override
    public void setupInnerViewElements(ViewGroup parent, View view) {
        ButterKnife.inject(this, view);
        inner_title.setText(primaryTitle);
        secondary_title.setText(secondaryTitle);
    }


    public void setSecondaryTitle(String secondaryTitle) {
        this.secondaryTitle = secondaryTitle;
    }

    public String getTitle() {
        return this.primaryTitle;
    }

    public void setTitle(String primaryTitle) {
        this.primaryTitle = primaryTitle;
    }
}