package com.fuzzingtheweb.hnreader;

import android.content.Intent;
import android.net.Uri;

/**
 * Created by javiman on 11/07/2014.
 */
public class IntentManager {
    /**
     * Creates an action view intent for viewing a url in the browser.
     */
    protected Intent getBrowserIntent(String url) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW);
        browserIntent.setData(Uri.parse(url));
        return browserIntent;
    }

    /**
     * Creates an action send intent for sharing the post url.
     */
    protected Intent getShareIntent(String url) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, url);
        return shareIntent;
    }
}
