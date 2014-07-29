package com.fuzzingtheweb.hnreader;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;


public class MainActivity
        extends FragmentActivity
        implements PostFragment.Callbacks, NavigationDrawerFragment.NavigationDrawerCallbacks {

    public static final String TAG = MainActivity.class.getSimpleName();

    private Util mUtil;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;

    private int mActiveSection = 1;

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        mUtil = new Util(this);

        mTitle = getTitle();

        setContentView(R.layout.activity_main);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            restoreActionBar();
            return true;
        }
        return true;
    }


    @Override
    public void onItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        String postUrl = mUtil.getPostUrl(info.id);

        switch (item.getItemId()) {
            case PostFragment.FAVORITE_ID:
                mUtil.markAsFavorite(info.id, true);
                Toast.makeText(this, "Post marked as favourite", Toast.LENGTH_LONG).show();
                break;
            case PostFragment.REMOVE_FAVORITE_ID:
                mUtil.markAsFavorite(info.id, false);
                reloadPostFragment();
                Toast.makeText(this, "Post removed from favourites", Toast.LENGTH_LONG).show();
                break;
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

        mUtil.markAsRead(id);
    }

    public void onEmptyList() {
        if (mActiveSection == Constants.ALL_ITEMS) {
            onRefreshPosts();
        }
    }

    /**
     * If the network is available, refresh the posts.
     */
    public void onRefreshPosts() {
        if (isNetworkAvailable()) {
            setProgressBarIndeterminateVisibility(true);
            FetchPostsTask getBlogPostsTask = new FetchPostsTask();
            getBlogPostsTask.execute();
        } else {
            setProgressBarIndeterminateVisibility(false);
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

    /**
     * Shows an notification in the screen for an error related to non existing items.
     */
    private void updateDisplayForError() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.error_title));
        builder.setMessage(getString(R.string.error_message));
        builder.setPositiveButton(android.R.string.ok, null);
        AlertDialog dialog = builder.create();
        dialog.show();

        ListView listView = (ListView) findViewById(android.R.id.list);
        TextView emptyTextView = (TextView) listView.getEmptyView();
        emptyTextView.setText(getString(R.string.no_items));
    }

    /**
     * Tell the PostFragment to reload its listview.
     */
    private void reloadPostFragment() {
        PostFragment fragment = (PostFragment) getSupportFragmentManager().findFragmentById(R.id.container);
        fragment.populateListView();
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, PostFragment.newInstance(position + 1))
                .commit();
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case Constants.ALL_ITEMS:
                mTitle = "All items";
                mActiveSection = Constants.ALL_ITEMS;
                break;
            case Constants.FAVOURITE_ITEMS:
                mTitle = "Favourite items";
                mActiveSection = Constants.FAVOURITE_ITEMS;
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }

    /**
     * Asynctask for making a call to the API.
     */
    private class FetchPostsTask extends AsyncTask<Object, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(Object[] params) {
            return mUtil.getAPIResponse();
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            setProgressBarIndeterminateVisibility(false);
            boolean response = mUtil.handleAPIResponse(result);
            if (response) {
                reloadPostFragment();
            } else {
                updateDisplayForError();
            }
        }
    }

}
