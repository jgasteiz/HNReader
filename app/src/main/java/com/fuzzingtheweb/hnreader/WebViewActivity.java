package com.fuzzingtheweb.hnreader;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;

public class WebViewActivity extends ActionBarActivity {

    protected String mUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);

        // Add the `back` icon to the action bar
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        // Create an intent for displaying the url in a webView
        Intent intent = getIntent();
        Uri blogUri = intent.getData();
        mUrl = blogUri.toString();

        // Create the webView and load the url content
        WebView webView = (WebView) findViewById(R.id.webView);
        webView.loadUrl(mUrl);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.web_view, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int itemId = item.getItemId();

        if (itemId == R.id.action_share) {
            sharePost();
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Creates a share intent for sharing the post url.
     */
    private void sharePost() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, mUrl);
        startActivity(Intent.createChooser(shareIntent, getString(R.string.action_share_title)));
    }
}
