package com.fuzzingtheweb.hnreader;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.text.Html;
import android.util.Log;

import com.fuzzingtheweb.hnreader.data.PostDBAdapter;

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

public class Util {

    private PostDBAdapter mDbHelper;

    public Util(Context context) {
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

            mDbHelper.updateAllPostsIndexes();

            Cursor cursor;
            JSONArray jsonPosts;
            JSONObject post;
            int index;
            String postId, title, url, prettyUrl, points, author, postedAgo, numComments;

            try {
                jsonPosts = postData.getJSONArray(Constants.JSON_KEY_ITEMS);
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

                    cursor = null;
                    if (!postId.isEmpty()) {
                        cursor = mDbHelper.fetchPostByHNId(postId);
                    }

                    if (cursor != null && cursor.getCount() > 0) {
                        int idColIndex = cursor.getColumnIndex("_id");
                        long rowId = cursor.getLong(idColIndex);
                        mDbHelper.updatePost(rowId, index, postId, title, url, prettyUrl,
                                points, author, postedAgo, numComments);
                    } else {
                        mDbHelper.createPost(index, postId, title, url, prettyUrl,
                                points, author, postedAgo, numComments);
                    }
                }

                // TODO: bring this line back to life, but in a proper way.
                mDbHelper.deleteOldPosts();

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
                Log.v(MainActivity.TAG, "Response: " + jsonResponse);
            } else {
                Log.i(MainActivity.TAG, "Unsuccessful HTTP Response Code: " + responseCode);
            }
            Log.i(MainActivity.TAG, "Code: " + responseCode);
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
        int urlColIndex = cursor.getColumnIndex(Constants.KEY_URL);
        return cursor.getString(urlColIndex);
    }

    /**
     * Creates an action view intent for viewing a url in the browser.
     */
    public Intent getBrowserIntent(String url) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW);
        browserIntent.setData(Uri.parse(url));
        return browserIntent;
    }

    /**
     * Creates an action send intent for sharing the post url.
     */
    public Intent getShareIntent(String url) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, url);
        return shareIntent;
    }


    public String[] getAllPostsKeys() {
        String[] keys = { Constants.KEY_INDEX, Constants.KEY_TITLE, Constants.KEY_PRETTY_URL,
                Constants.KEY_SCORE, Constants.KEY_AUTHOR, Constants.KEY_POSTED_AGO,
                Constants.KEY_NUM_COMMENTS };
        return keys;
    }

    public String[] getFavoritePostsKeys() {
        String[] keys = { Constants.KEY_INDEX, Constants.KEY_TITLE, Constants.KEY_PRETTY_URL };
        return keys;
    }

    public int[] getAllPostsIds() {
        int[] ids = { R.id.item_index, R.id.item_title, R.id.item_url,
                R.id.item_score, R.id.item_author, R.id.item_posted_ago,
                R.id.item_num_comments };
        return ids;
    }

    public int[] getFavoritePostsIds() {
        int[] ids = { R.id.item_index, R.id.item_title, R.id.item_url };
        return ids;
    }

    /**
     * Local method for logging an exception.
     *
     * @param e exception to log
     */
    public void logException(Exception e) {
        Log.e(MainActivity.TAG, "Exception caught!", e);
    }

    public void markAsRead(long id) {
        mDbHelper.markAsRead(id);
    }

    public void markAsFavorite(long id, boolean favorite) {
        mDbHelper.markAsFavorite(id, favorite);
    }

    public Cursor fetchAllPosts() {
        return mDbHelper.fetchAllPosts();
    }

    public Cursor fetchFavoritePosts() {
        return mDbHelper.fetchFavoritePosts();
    }
}
