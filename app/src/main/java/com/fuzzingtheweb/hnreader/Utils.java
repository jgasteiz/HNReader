package com.fuzzingtheweb.hnreader;

import android.content.Context;
import android.text.Html;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;

public class Utils {

    private PostDBAdapter mDbHelper;

    public Utils(Context context) {
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

            deleteAllPosts();

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
                    prettyUrl = formatUrl(url);
                    points = post.getString(Constants.KEY_SCORE);
                    author = post.getString(Constants.KEY_AUTHOR);
                    postedAgo = post.getString(Constants.KEY_POSTED_AGO);
                    numComments = post.getString(Constants.KEY_NUM_COMMENTS);

                    savePost(index, postId, title, url, prettyUrl, points, author, postedAgo, numComments);
                }

            } catch (JSONException e) {
                logException(e);
                return false;
            }

            return true;
        }
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
     * Local method for logging an exception.
     *
     * @param e exception to log
     */
    public void logException(Exception e) {
        Log.e(NewMainActivity.TAG, "Exception caught!", e);
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
}
