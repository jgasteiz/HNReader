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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
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


public class PostListActivity extends ListActivity {

    private PostDBAdapter mDbHelper;

    private ListView mListView;
    private int mScrollPosition;

    private static final String Y_COMBINATOR_URL = "news.ycombinator.com";
    private static final String HTTPS_Y_COMBINATOR_URL = "https://news.ycombinator.com/";
    protected JSONObject mPostData;
    protected ProgressBar mProgressBar;
    protected Button mRefreshButton;
    public static final String TAG = PostListActivity.class.getSimpleName();

    private final String KEY_INDEX = "postIndex";
    private final String KEY_POST_INDEX = "index";
    private final String KEY_POST_ID = "id";
    private final String KEY_TITLE = "title";
    private final String KEY_URL = "url";
    private final String KEY_PRETTY_URL = "prettyUrl";
    private final String KEY_SCORE = "score";
    private final String KEY_AUTHOR = "author";
    private final String KEY_POSTED_AGO = "postedAgo";
    private static final String KEY_NUM_COMMENTS = "comments";

    private static final String API_URL = "http://api-hnreader.rhcloud.com/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_list);

        if (savedInstanceState != null) {
            mScrollPosition = savedInstanceState.getInt("mScrollPosition");
        } else {
            mScrollPosition = 0;
        }

        mListView = (ListView) findViewById(android.R.id.list);

        mDbHelper = new PostDBAdapter(this);
        mDbHelper.open();

        mProgressBar = (ProgressBar) findViewById(R.id.progressBar1);
        mRefreshButton = (Button) findViewById(R.id.refresh_button);

        mRefreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshData();
            }
        });

        populateListView();
        restoreScroll(savedInstanceState);
    }

    private void populateListView() {
        // Get all of the rows from the database and create the item list
        Cursor postsCursor = mDbHelper.fetchAllPosts();
        startManagingCursor(postsCursor);

        String[] keys = { KEY_INDEX, KEY_TITLE, KEY_PRETTY_URL, KEY_SCORE,
                KEY_AUTHOR, KEY_NUM_COMMENTS };
        int[] ids = { R.id.item_index, R.id.item_title, R.id.item_url,
                R.id.item_score, R.id.item_author, R.id.item_num_comments };

        // Now create a simple cursor adapter and set it to display
        SimpleCursorAdapter posts =
                new SimpleCursorAdapter(this, R.layout.activity_post_item, postsCursor, keys, ids);
        setListAdapter(posts);

        mListView.setSelection(mScrollPosition);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        log("Item clicked");
        log("Position: " + position);
        log("Id: " + id);

        Cursor cursor = mDbHelper.fetchPost(id);
        int urlColIndex = cursor.getColumnIndex("url");
        String postUrl = cursor.getString(urlColIndex);

        // Some urls are ycombinator internal urls.
        // Need new attribute for hn posts - postId
        if (postUrl.startsWith("item")) {
            postUrl = HTTPS_Y_COMBINATOR_URL + postUrl;
        }

        mScrollPosition = mListView.getFirstVisiblePosition();

        Intent intent = new Intent(this, WebViewActivity.class);
        intent.setData(Uri.parse(postUrl));
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.post_list, menu);
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
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putInt("mScrollPosition", mScrollPosition);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        restoreScroll(savedInstanceState);
    }

    /**
     * Restore the scroll position on screen
     *
     * @param savedInstanceState saved state of the activity
     */
    private void restoreScroll(Bundle savedInstanceState) {
        if (savedInstanceState != null && savedInstanceState.containsKey("mScrollPosition"))
        {
            mScrollPosition = savedInstanceState.getInt("mScrollPosition");
        }
    }

    private void savePost(String index, String postId, String title, String url,
                          String prettyUrl, String points, String author,
                          String postedAgo, String numComments) {
        long id = mDbHelper.createPost(index, postId, title, url, prettyUrl,
                points, author, postedAgo, numComments);
    }

    private boolean deleteAllPosts() {
        return mDbHelper.deleteAllPosts();
    }

    /**
     * If the network is available, refresh the posts.
     */
    private void refreshData() {
        if (isNetworkAvailable()) {
            mProgressBar.setVisibility(View.VISIBLE);
            GetPostsTask getBlogPostsTask = new GetPostsTask();
            getBlogPostsTask.execute();
        } else {
            mProgressBar.setVisibility(View.GONE);
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
        mProgressBar.setVisibility(View.GONE);

        if (mPostData == null) {
            updateDisplayForError();
        } else {

            deleteAllPosts();

            JSONArray jsonPosts;
            JSONObject post;
            String index, postId, title, url, prettyUrl, points, author, postedAgo, numComments;

            try {
                jsonPosts = mPostData.getJSONArray("links");
                for (int i = 0; i < jsonPosts.length(); i++) {
                    post = jsonPosts.getJSONObject(i);

                    index = post.getString(KEY_POST_INDEX);
                    postId = "";
                    title = Html.fromHtml(post.getString(KEY_TITLE)).toString();
                    url = post.getString(KEY_URL);
                    prettyUrl = formatUrl(url);
                    points = post.getString(KEY_SCORE);
                    author = post.getString(KEY_AUTHOR);
                    postedAgo = "";
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
            if (formattedUrl == null) {
                formattedUrl = Y_COMBINATOR_URL;
            }
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
     * Local method for debug logging
     * @param message string to log
     */
    private void log(String message) {
        Log.d(TAG, message);
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
