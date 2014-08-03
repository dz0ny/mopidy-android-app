package com.dz0ny.mopidy;

import android.content.Intent;

import com.dz0ny.mopidy.ui.PlayMopidy;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class PlayMopidyTest {

    @Test
    public void testYoutube() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_TEXT, "Netflix Ad - Revolusion - You Gotta Get It To Get… – http://youtu.be/W09bH1Aw8_Y");
        PlayMopidy activity = Robolectric
                .buildActivity(PlayMopidy.class)
                .withIntent(intent).get();
    }

    @Test
    public void testSoundcloud() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_TEXT, "http://soundcloud.com/nuclearblastrecords/threshold-watchtower-on-the-moon");
        PlayMopidy activity = Robolectric
                .buildActivity(PlayMopidy.class)
                .withIntent(intent).get();
    }

    @Test
    public void testSpotify() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_TEXT, "http://open.spotify.com/user/spotify/playlist/5ILSWr90l2Bgk89xuhsysy");
        PlayMopidy activity = Robolectric
                .buildActivity(PlayMopidy.class)
                .withIntent(intent).get();

    }
}
