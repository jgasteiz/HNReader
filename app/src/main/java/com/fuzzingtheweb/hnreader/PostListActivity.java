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
import android.widget.SimpleAdapter;
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
import java.util.ArrayList;
import java.util.HashMap;


public class PostListActivity extends ListActivity {

    private PostDBAdapter mDbHelper;

    private static final String Y_COMBINATOR_URL = "news.ycombinator.com";
    private static final String HTTPS_Y_COMBINATOR_URL = "https://news.ycombinator.com/item?id=";
    protected JSONObject mPostData;
    protected ProgressBar mProgressBar;
    protected Button mRefreshButton;
    public static final String TAG = PostListActivity.class.getSimpleName();

    private final String KEY_INDEX = "index";
    private final String KEY_TITLE = "title";
    private final String KEY_URL = "url";
    private final String KEY_POINTS = "points";
    private final String KEY_AUTHOR = "postedBy";
    private final String KEY_POSTED_AGO = "postedAgo";

    private static final String API_URL = "http://api.ihackernews.com/page";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_list);

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

//        refreshData();
        fillData();
    }

    private void fillData() {
        // Get all of the rows from the database and create the item list
        Cursor postsCursor = mDbHelper.fetchAllPosts();
        startManagingCursor(postsCursor);

        String[] keys = { "_id", KEY_TITLE, KEY_URL, KEY_POINTS,
                KEY_AUTHOR, KEY_POSTED_AGO };
        int[] ids = { R.id.item_index, R.id.item_title, R.id.item_url,
                R.id.item_points, R.id.item_author, R.id.item_posted_ago };

        // Now create a simple cursor adapter and set it to display
        SimpleCursorAdapter posts =
                new SimpleCursorAdapter(this, R.layout.activity_post_item, postsCursor, keys, ids);
        setListAdapter(posts);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Cursor cursor = mDbHelper.fetchPost(id);
        int rowIndex = cursor.getColumnIndex("url");
        String postUrl = cursor.getString(rowIndex);

        // Some urls are ycombinator internal urls.
        // Need new attribute for hn posts - postId
//        if (postUrl.startsWith("/")) {
//            int postId = jsonPost.getInt("id");
//            postUrl = HTTPS_Y_COMBINATOR_URL + postId;
//        }

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

    private void savePost(String title, String url, String points,
                          String author, String postedAgo) {
        long id = mDbHelper.createPost(title, url, points, author, postedAgo);
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
            ArrayList<HashMap<String, String>> blogPosts;
            JSONObject post;
            HashMap<String, String> blogPost;
            String index, title, url, points, author, postedAgo;

            try {
                jsonPosts = mPostData.getJSONArray("items");
                blogPosts = new ArrayList<HashMap<String, String>>();
                for (int i = 0; i < jsonPosts.length(); i++) {
                    post = jsonPosts.getJSONObject(i);

                    index = Integer.toString(i + 1);
                    title = Html.fromHtml(post.getString(KEY_TITLE)).toString();
                    url = post.getString(KEY_URL);
                    points = post.getString(KEY_POINTS);
                    author = post.getString(KEY_AUTHOR);
                    postedAgo = post.getString(KEY_POSTED_AGO);

                    blogPost = new HashMap<String, String>();
                    blogPost.put(KEY_INDEX, index);
                    blogPost.put(KEY_TITLE, title);
                    blogPost.put(KEY_URL, formatUrl(url));
                    blogPost.put(KEY_POINTS, points);
                    blogPost.put(KEY_AUTHOR, author);
                    blogPost.put(KEY_POSTED_AGO, postedAgo);

                    blogPosts.add(blogPost);

                    savePost(title, url, points, author, postedAgo);
                }

                String[] keys = { KEY_INDEX, KEY_TITLE, KEY_URL, KEY_POINTS,
                        KEY_AUTHOR, KEY_POSTED_AGO };
                int[] ids = { R.id.item_index, R.id.item_title, R.id.item_url,
                        R.id.item_points, R.id.item_author, R.id.item_posted_ago };

                SimpleAdapter adapter = new SimpleAdapter(this, blogPosts,
                        R.layout.activity_post_item, keys, ids);
                setListAdapter(adapter);

            } catch (JSONException e) {
                logException(e);
            }
        }
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
     * @param e - exception to log
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

                    JSONArray jsonPosts = jsonResponse.getJSONArray("items");
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
