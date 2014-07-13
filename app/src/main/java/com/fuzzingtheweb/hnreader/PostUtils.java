package com.fuzzingtheweb.hnreader;

import android.content.Context;
import android.database.Cursor;
import android.text.Html;
import android.util.Log;

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

public class PostUtils {

    private PostDBAdapter mDbHelper;

    public PostUtils(Context context) {
        mDbHelper = new PostDBAdapter(context);
        mDbHelper.open();
    }

    /**
     * Handles API response parsing the JSON and loading the list of posts in the main layout.
     */
    public boolean handleAPIResponse(JSONObject postData) {
        if (postData == null) {
            return false;
        } else {

            // TODO: don't delete all posts every time we refresh the post list.
            mDbHelper.deleteAllPosts();

            JSONArray jsonPosts;
            JSONObject post;
            int index;
            String postId, title, url, prettyUrl, points, author, postedAgo, numComments;

            try {
                jsonPosts = postData.getJSONArray("links");
                for (int i = 0; i < jsonPosts.length(); i++) {
                    post = jsonPosts.getJSONObject(i);

                    index = post.getInt(Constants.KEY_POST_INDEX);
                    postId = post.getString(Constants.KEY_POST_ID);
                    title = Html.fromHtml(post.getString(Constants.KEY_TITLE)).toString();
                    url = post.getString(Constants.KEY_URL);
                    prettyUrl = getUrlHostname(url);
                    points = post.getString(Constants.KEY_SCORE);
                    author = post.getString(Constants.KEY_AUTHOR);
                    postedAgo = post.getString(Constants.KEY_POSTED_AGO);
                    numComments = post.getString(Constants.KEY_NUM_COMMENTS);

                    // TODO: consider updating existing posts, only creating when they are new.
                    mDbHelper.createPost(index, postId, title, url, prettyUrl,
                            points, author, postedAgo, numComments);
                }

            } catch (JSONException e) {
                logException(e);
                return false;
            }

            return true;
        }
    }

    /**
     * Return the hostname of a given url.
     *
     * @param url which host we want to return
     * @return the hostname of a url
     */
    private String getUrlHostname(String url) {
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
     * Get a json object from the API.
     */
    public JSONObject getAPIResponse() {
        int responseCode = -1;
        JSONObject jsonResponse = null;
        StringBuilder builder = new StringBuilder();
        HttpClient client = new DefaultHttpClient();

        HttpGet httpget = new HttpGet(Constants.API_URL);

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

                jsonResponse = new JSONObject(builder.toString());
                Log.v(NewMainActivity.TAG, "Response: " + jsonResponse);
            } else {
                Log.i(NewMainActivity.TAG, "Unsuccessful HTTP Response Code: " + responseCode);
            }
            Log.i(NewMainActivity.TAG, "Code: " + responseCode);
        } catch (MalformedURLException e) {
            logException(e);
        } catch (IOException e) {
            logException(e);
        } catch (Exception e) {
            logException(e);
        }

        return jsonResponse;
    }

    /**
     * Given an item id, return the url of the item.
     *
     * @param id in the database for the selected item.
     * @return the item url
     */
    public String getPostUrl(long id) {
        Cursor cursor = mDbHelper.fetchPost(id);
        int urlColIndex = cursor.getColumnIndex("url");
        return cursor.getString(urlColIndex);
    }

    /**
     * Local method for logging an exception.
     *
     * @param e exception to log
     */
    public void logException(Exception e) {
        Log.e(NewMainActivity.TAG, "Exception caught!", e);
    }
}
