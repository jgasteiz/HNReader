package com.fuzzingtheweb.hnreader;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;


public class MainActivity extends ListActivity {

    private PostDBAdapter mDbHelper;
    private IntentManager mIntentManager;

    protected JSONObject mPostData;
    protected Button mRefreshButton;
    public static final String TAG = MainActivity.class.getSimpleName();

    private final String KEY_INDEX = "postIndex";
    private final String KEY_POST_INDEX = "index";
    private final String KEY_POST_ID = "hn_id";
    private final String KEY_TITLE = "title";
    private final String KEY_URL = "url";
    private final String KEY_PRETTY_URL = "prettyUrl";
    private final String KEY_SCORE = "score";
    private final String KEY_AUTHOR = "author";
    private final String KEY_POSTED_AGO = "posted_ago";
    private static final String KEY_NUM_COMMENTS = "comments";

    private static final int FAVORITE_ID = Menu.FIRST;
    private static final int OPEN_IN_BROWSER_ID = Menu.FIRST + 1;
    private static final int SHARE_ID = Menu.FIRST + 2;

    private static final String API_URL = "http://api-hnreader.rhcloud.com/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_main);

        mIntentManager = new IntentManager();

        mDbHelper = new PostDBAdapter(this);
        mDbHelper.open();

        mRefreshButton = (Button) findViewById(R.id.refresh_button);

        mRefreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshData();
            }
        });

        populateListView();
        registerForContextMenu(getListView());
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        Log.d(TAG, "Item clicked");
        Log.d(TAG, "Position: " + position);
        Log.d(TAG, "Id: " + id);

        String postUrl = getPostUrl(id);

        Intent intent = new Intent(this, WebViewActivity.class);
        intent.setData(Uri.parse(postUrl));
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int itemId = item.getItemId();

        if (itemId == R.id.action_refresh) {
            refreshData();
        } else if (itemId == R.id.action_new_main) {
            Intent intent = new Intent(this, NewMainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, FAVORITE_ID, 0, R.string.menu_favorite);
        menu.add(0, OPEN_IN_BROWSER_ID, 0, R.string.open_browser);
        menu.add(0, SHARE_ID, 0, R.string.action_share);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        boolean result = super.onContextItemSelected(item);

        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        String postUrl = getPostUrl(info.id);

        switch (item.getItemId()) {
            case SHARE_ID:
                Intent shareIntent = mIntentManager.getShareIntent(postUrl);
                startActivity(Intent.createChooser(shareIntent, getString(R.string.action_share_title)));
                break;
            case OPEN_IN_BROWSER_ID:
                Intent browserIntent = mIntentManager.getBrowserIntent(postUrl);
                startActivity(browserIntent);
                break;
        }
        return result;
    }

    /**
     * Populate the main list view with the database content.
     */
    private void populateListView() {
        // Get all of the rows from the database and create the item list
        Cursor postsCursor = mDbHelper.fetchAllPosts();
        startManagingCursor(postsCursor);

        String[] keys = { KEY_INDEX, KEY_TITLE, KEY_PRETTY_URL, KEY_SCORE,
                KEY_AUTHOR, KEY_POSTED_AGO, KEY_NUM_COMMENTS };
        int[] ids = { R.id.item_index, R.id.item_title, R.id.item_url,
                R.id.item_score, R.id.item_author, R.id.item_posted_ago, R.id.item_num_comments };

        // Now create a simple cursor adapter and set it to display
        SimpleCursorAdapter posts =
                new SimpleCursorAdapter(this, R.layout.post_item, postsCursor, keys, ids);
        setListAdapter(posts);
    }

    /**
     * Given an item id, return the url of the item.
     *
     * @param id in the database for the selected item.
     * @return the item url
     */
    private String getPostUrl(long id) {
        Cursor cursor = mDbHelper.fetchPost(id);
        int urlColIndex = cursor.getColumnIndex("url");
        return cursor.getString(urlColIndex);
    }

    /**
     * Save a post in the database.
     *
     * @param index
     * @param postId
     * @param title
     * @param url
     * @param prettyUrl
     * @param points
     * @param author
     * @param postedAgo
     * @param numComments
     */
    private void savePost(int index, String postId, String title, String url,
                          String prettyUrl, String points, String author,
                          String postedAgo, String numComments) {

        mDbHelper.createPost(index, postId, title, url, prettyUrl,
                points, author, postedAgo, numComments);
    }

    /**
     * Delete all posts from the database.
     *
     * @return true if everything went fine
     */
    private boolean deleteAllPosts() {
        return mDbHelper.deleteAllPosts();
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
     * Handles API response parsing the JSON and loading the list of posts in the main layout.
     */
    private void handleAPIResponse() {
        setProgressBarIndeterminateVisibility(false);

        if (mPostData == null) {
            updateDisplayForError();
        } else {

            deleteAllPosts();

            JSONArray jsonPosts;
            JSONObject post;
            int index;
            String postId, title, url, prettyUrl, points, author, postedAgo, numComments;

            try {
                jsonPosts = mPostData.getJSONArray("links");
                for (int i = 0; i < jsonPosts.length(); i++) {
                    post = jsonPosts.getJSONObject(i);

                    index = post.getInt(KEY_POST_INDEX);
                    postId = post.getString(KEY_POST_ID);
                    title = Html.fromHtml(post.getString(KEY_TITLE)).toString();
                    url = post.getString(KEY_URL);
                    prettyUrl = formatUrl(url);
                    points = post.getString(KEY_SCORE);
                    author = post.getString(KEY_AUTHOR);
                    postedAgo = post.getString(KEY_POSTED_AGO);
                    numComments = post.getString(KEY_NUM_COMMENTS);

                    savePost(index, postId, title, url, prettyUrl, points, author, postedAgo, numComments);
                }

            } catch (JSONException e) {
                logException(e);
            }
        }

        populateListView();
    }

    private String formatUrl(String url) {
        String formattedUrl = url;
        try {
            URI uri = new URI(formattedUrl);
            formattedUrl = uri.getHost();
        } catch (URISyntaxException e) {
            logException(e);
        }
        return formattedUrl;
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

        TextView emptyTextView = (TextView) getListView().getEmptyView();
        emptyTextView.setText(getString(R.string.no_items));
    }

    /**
     * Local method for logging an exception.
     *
     * @param e exception to log
     */
    private void logException(Exception e) {
        Log.e(TAG, "Exception caught!", e);
    }

    /**
     * Asynctask for making a call to the API.
     */
    private class GetPostsTask extends AsyncTask<Object, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(Object[] params) {
            int responseCode = -1;
            JSONObject jsonResponse = null;
            StringBuilder builder = new StringBuilder();
            HttpClient client = new DefaultHttpClient();
            HttpGet httpget = new HttpGet(API_URL);

            try {
                HttpResponse response = client.execute(httpget);
                StatusLine statusLine = response.getStatusLine();
                responseCode = statusLine.getStatusCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    HttpEntity entity = response.getEntity();
                    InputStream content = entity.getContent();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(content));

                    String line;
                    while((line = reader.readLine()) != null){
                        builder.append(line);
                    }

                    jsonResponse =new JSONObject(builder.toString());
                    Log.v(TAG, "Response: " + jsonResponse);

                    JSONArray jsonPosts = jsonResponse.getJSONArray("links");
                    for (int i = 0; i < jsonPosts.length(); i++) {
                        JSONObject jsonPost = jsonPosts.getJSONObject(i);
                        String title = jsonPost.getString(KEY_TITLE);
                        Log.i(TAG, "Post " + i + ": " + title);
                    }

                } else {
                    Log.i(TAG, "Unsuccessful HTTP Response Code: " + responseCode);
                }
                Log.i(TAG, "Code: " + responseCode);
            } catch (MalformedURLException e) {
                logException(e);
            } catch (IOException e) {
                logException(e);
            } catch (Exception e) {
                logException(e);
            }

            return jsonResponse;
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            mPostData = result;
            handleAPIResponse();
        }
    }

}
