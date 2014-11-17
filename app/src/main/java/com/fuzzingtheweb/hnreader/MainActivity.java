package com.fuzzingtheweb.hnreader;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.Toast;

import com.firebase.client.Firebase;


public class MainActivity extends ActionBarActivity implements PostFragment.Callbacks {

    public static final String TAG = MainActivity.class.getSimpleName();

    private Util mUtil;

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Firebase.setAndroidContext(this);
        setContentView(R.layout.activity_main);
        mUtil = new Util(this);

        if (findViewById(R.id.web_view) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-large and
            // res/values-sw600dp). If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;

            // In two-pane mode, list items should be given the
            // 'activated' state when touched.
            ((PostFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.container))
                    .setActivateOnItemClick(true);
        }

        onRefreshPosts();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        getMenuInflater().inflate(R.menu.post_fragment, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        String postUrl = mUtil.getPostUrl(info.id);

        switch (item.getItemId()) {
            case PostFragment.SHARE_ID:
                Intent shareIntent = mUtil.getShareIntent(postUrl);
                startActivity(Intent.createChooser(shareIntent, getString(R.string.action_share_title)));
                break;
            case PostFragment.OPEN_IN_BROWSER_ID:
                Intent browserIntent = mUtil.getBrowserIntent(postUrl);
                startActivity(browserIntent);
                break;
        }
    }

    /**
     * Callback method from {@link com.fuzzingtheweb.hnreader.PostFragment}
     * indicating that the post with the given url was selected.
     */
    @Override
    public void onItemClick(long id) {
        String postUrl = mUtil.getPostUrl(id);
        Log.d(TAG, "Url to load: " + postUrl);

        if (mTwoPane) {
            Bundle arguments = new Bundle();
            arguments.putString(WebViewFragment.KEY_URL, postUrl);
            WebViewFragment fragment = new WebViewFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.web_view, fragment)
                    .commit();
        } else {
            Intent intent = new Intent(this, WebViewActivity.class);
            intent.setData(Uri.parse(postUrl));
            startActivity(intent);
        }
    }

    @Override
    public void onEmptyList() {

    }

    /**
     * If the network is available, refresh the posts.
     */
    public void onRefreshPosts() {
        if (isNetworkAvailable()) {
            mUtil.setFragment((PostFragment) getSupportFragmentManager().findFragmentById(R.id.container));
            mUtil.refreshPosts();
        } else {
            Toast.makeText(this, "Network is unavailable", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Instantiate a connectivity manager and check if there is a network interface
     * and if it's available.
     *
     * @return - true if the network is available
     */
    public boolean isNetworkAvailable() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

}
