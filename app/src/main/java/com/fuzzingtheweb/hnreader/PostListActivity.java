package com.fuzzingtheweb.hnreader;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
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
import java.util.ArrayList;
import java.util.HashMap;


public class PostListActivity extends ListActivity {

    protected JSONObject mPostData;
    protected ProgressBar mProgressBar;
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

        mProgressBar = (ProgressBar) findViewById(R.id.progressBar1);

        if (isNetworkAvailable()) {
            mProgressBar.setVisibility(View.VISIBLE);
            GetPostsTask getBlogPostsTask = new GetPostsTask();
            getBlogPostsTask.execute();
        } else {
            Toast.makeText(this, "Network is unavailable", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        try {
            JSONArray jsonPosts = mPostData.getJSONArray("items");
            JSONObject jsonPost = jsonPosts.getJSONObject(position);
            String blogUrl = jsonPost.getString("url");

            Intent intent = new Intent(this, WebViewActivity.class);
            intent.setData(Uri.parse(blogUrl));
            startActivity(intent);
        } catch (JSONException e) {
            logException(e);
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

            JSONArray jsonPosts;
            ArrayList<HashMap<String, String>> blogPosts;
            JSONObject post;
            HashMap<String, String> blogPost;
            String title, url, points, author, postedAgo;

            try {
                jsonPosts = mPostData.getJSONArray("items");
                blogPosts = new ArrayList<HashMap<String, String>>();
                for (int i = 0; i < jsonPosts.length(); i++) {
                    post = jsonPosts.getJSONObject(i);

                    title = Html.fromHtml(post.getString(KEY_TITLE)).toString();
                    url = post.getString(KEY_URL);
                    points = post.getString(KEY_POINTS);
                    author = post.getString(KEY_AUTHOR);
                    postedAgo = post.getString(KEY_POSTED_AGO);

                    blogPost = new HashMap<String, String>();
                    blogPost.put(KEY_INDEX, Integer.toString(i + 1));
                    blogPost.put(KEY_TITLE, title);
                    blogPost.put(KEY_URL, url);
                    blogPost.put(KEY_POINTS, points);
                    blogPost.put(KEY_AUTHOR, author);
                    blogPost.put(KEY_POSTED_AGO, postedAgo);

                    blogPosts.add(blogPost);
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
