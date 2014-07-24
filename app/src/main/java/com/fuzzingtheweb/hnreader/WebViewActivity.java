package com.fuzzingtheweb.hnreader;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;

public class WebViewActivity extends FragmentActivity {

    protected String mUrl;
    private Util mUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);

        // Create an intent for displaying the url in a webView
        Intent intent = getIntent();
        Uri blogUri = intent.getData();
        mUrl = blogUri.toString();

        mUtil = new Util(this);

        // Create the fragment
        Bundle arguments = new Bundle();
        arguments.putString(WebViewFragment.KEY_URL, mUrl);
        WebViewFragment fragment = new WebViewFragment();
        fragment.setArguments(arguments);
        getSupportFragmentManager().beginTransaction()
                .add(R.id.web_view, fragment)
                .commit();
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

        switch (itemId) {
            case R.id.action_share:
                Intent shareIntent = mUtil.getShareIntent(mUrl);
                startActivity(Intent.createChooser(shareIntent, getString(R.string.action_share_title)));
                break;
            case R.id.open_browser:
                Intent browserIntent = mUtil.getBrowserIntent(mUrl);
                startActivity(browserIntent);
                break;
            case android.R.id.home:
                this.finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
