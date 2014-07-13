package com.fuzzingtheweb.hnreader;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;


public class MainActivity extends FragmentActivity implements PostFragment.Callbacks {

    public static final String TAG = MainActivity.class.getSimpleName();

    private PostUtils mPostUtils;
    private IntentManager mIntentManager;

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_main);

        mPostUtils = new PostUtils(this);
        mIntentManager = new IntentManager();

        if (findViewById(R.id.web_view) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-large and
            // res/values-sw600dp). If this view is present, then the
            // activity should be in two-pane mode.
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
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                refreshData();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        String postUrl = mPostUtils.getPostUrl(info.id);

        switch (item.getItemId()) {
            case PostFragment.SHARE_ID:
                Intent shareIntent = mIntentManager.getShareIntent(postUrl);
                startActivity(Intent.createChooser(shareIntent, getString(R.string.action_share_title)));
                break;
            case PostFragment.OPEN_IN_BROWSER_ID:
                Intent browserIntent = mIntentManager.getBrowserIntent(postUrl);
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
        String postUrl = mPostUtils.getPostUrl(id);
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

    public void onEmptyList() {
        refreshData();
    }

    /**
     * If the network is available, refresh the posts.
     */
    private void refreshData() {
        if (isNetworkAvailable()) {
            setProgressBarIndeterminateVisibility(true);
            GetPostsTask getBlogPostsTask = new GetPostsTask();
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
        PostFragment fragment = (PostFragment) getSupportFragmentManager().findFragmentById(R.id.post_list);
        fragment.populateListView();
    }

    /**
     * Asynctask for making a call to the API.
     */
    private class GetPostsTask extends AsyncTask<Object, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(Object[] params) {
            return mPostUtils.getAPIResponse();
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            setProgressBarIndeterminateVisibility(false);
            boolean response = mPostUtils.handleAPIResponse(result);
            if (response) {
                reloadPostFragment();
            } else {
                updateDisplayForError();
            }
        }
    }

}
