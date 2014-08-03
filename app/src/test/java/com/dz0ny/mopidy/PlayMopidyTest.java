package com.dz0ny.mopidy;

import android.content.Intent;
import android.widget.ImageView;

import com.dz0ny.mopidy.ui.PlayMopidy;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static junit.framework.Assert.assertEquals;
import static org.fest.assertions.api.ANDROID.assertThat;

@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class PlayMopidyTest {

    private static String spotify = "http://open.spotify.com/user/spotify/playlist/5ILSWr90l2Bgk89xuhsysy";
    private static String soundcloud = "http://soundcloud.com/nuclearblastrecords/threshold-watchtower-on-the-moon";
    private static String youtube = "Netflix Ad - Revolusion - You Gotta Get It To Get… – http://youtu.be/W09bH1Aw8_Y";

    @Test
    public void testUrlRegExp() {
        PlayMopidy activity = Robolectric
                .buildActivity(PlayMopidy.class).get();

        assertEquals("http://youtu.be/W09bH1Aw8_Y", activity.getUrlFromText(youtube));
        assertEquals(spotify, activity.getUrlFromText(spotify));
        assertEquals(soundcloud, activity.getUrlFromText(soundcloud));

    }

    @Test
    public void testYoutube() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_TEXT, "Netflix Ad - Revolusion - You Gotta Get It To Get… – http://youtu.be/W09bH1Aw8_Y");
        PlayMopidy activity = Robolectric
                .buildActivity(PlayMopidy.class)
                .withIntent(intent).create().start().resume().visible().get();
        assertThat(activity.findViewById(R.id.send)).isVisible();
    }

    @Test
    public void testSoundcloud() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_TEXT, "http://soundcloud.com/nuclearblastrecords/threshold-watchtower-on-the-moon");
        PlayMopidy activity = Robolectric
                .buildActivity(PlayMopidy.class)
                .withIntent(intent).create().start().resume().visible().get();
        assertThat(activity.findViewById(R.id.send)).isVisible();
    }

    @Test
    public void testSpotify() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_TEXT, "http://open.spotify.com/user/spotify/playlist/5ILSWr90l2Bgk89xuhsysy");
        PlayMopidy activity = Robolectric
                .buildActivity(PlayMopidy.class)
                .withIntent(intent).create().start().resume().visible().get();
        assertThat(activity.findViewById(R.id.send)).isVisible();

    }

    @Test
    public void should_finish_when_not_passing_intent_data() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_TEXT, "http://open.spotify.com/user/spotify/playlist/5ILSWr90l2Bgk89xuhsysy");
        PlayMopidy activity = Robolectric
                .buildActivity(PlayMopidy.class)
                .withIntent(intent).create().start().resume().visible().get();
        assertThat(activity).isFinishing();

    }
    @Test
    public void should_finish_when__intent_data_without_url() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_TEXT, "Some random text from some random app");
        PlayMopidy activity = Robolectric
                .buildActivity(PlayMopidy.class)
                .withIntent(intent).create().start().resume().visible().get();
        assertThat(activity).isFinishing();

    }
}
