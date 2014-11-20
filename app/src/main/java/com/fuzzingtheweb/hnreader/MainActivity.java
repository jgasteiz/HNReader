package com.fuzzingtheweb.hnreader;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.fuzzingtheweb.hnreader.fragments.PostFragment;
import com.fuzzingtheweb.hnreader.fragments.WebViewFragment;
import com.fuzzingtheweb.hnreader.models.Post;


public class MainActivity extends ActionBarActivity implements PostFragment.Callbacks {

    public static final String TAG = MainActivity.class.getSimpleName();

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

        if (isNetworkAvailable() == false) {
            Toast.makeText(this, "Network is unavailable", Toast.LENGTH_LONG).show();
            return;
        }

        if (findViewById(R.id.web_view) != null) {
            mTwoPane = true;

            // In two-pane mode, list items should be given the
            // 'activated' state when touched.
            ((PostFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.post_list))
                    .setActivateOnItemClick(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        getMenuInflater().inflate(R.menu.post_fragment, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                PostFragment fragment = ((PostFragment) getSupportFragmentManager().findFragmentById(R.id.post_list));
                fragment.loadPosts();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemSelected(Post post, MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case PostFragment.SHARE_ID:
                intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT, post.getUrl());
                startActivity(Intent.createChooser(intent, getString(R.string.action_share_title)));
                break;
            case PostFragment.OPEN_IN_BROWSER_ID:
                intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(post.getUrl()));
                startActivity(intent);
                break;
            case PostFragment.VIEW_COMMENTS_ID:
                intent = new Intent(this, CommentsActivity.class);
                intent.putExtra("id", post.getId());
                startActivity(intent);
                break;
        }
    }

    /**
     * Callback method from {@link com.fuzzingtheweb.hnreader.fragments.PostFragment}
     * indicating that the post with the given url was selected.
     */
    @Override
    public void onItemClick(String postUrl) {

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
