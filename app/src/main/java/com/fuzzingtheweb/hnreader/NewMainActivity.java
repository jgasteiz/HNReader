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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;


public class NewMainActivity extends FragmentActivity implements PostFragment.Callbacks {

    private static final String API_URL = "http://api-hnreader.rhcloud.com/";
    public static final String TAG = NewMainActivity.class.getSimpleName();

    private Utils mUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_new_main);

        mUtils = new Utils(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.new_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int itemId = item.getItemId();

        if (itemId == R.id.action_refresh) {
            // Refresh fragment data
            refreshData();
            getFragmentManager();
        } else if (itemId == R.id.action_main) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Callback method from {@link com.fuzzingtheweb.hnreader.PostFragment}
     * indicating that the post with the given url was selected.
     */
    @Override
    public void onItemSelected(String postUrl) {
        Intent intent = new Intent(this, WebViewActivity.class);
        intent.setData(Uri.parse(postUrl));
        startActivity(intent);
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
                    Log.v(NewMainActivity.TAG, "Response: " + jsonResponse);

                    JSONArray jsonPosts = jsonResponse.getJSONArray("links");
                    for (int i = 0; i < jsonPosts.length(); i++) {
                        JSONObject jsonPost = jsonPosts.getJSONObject(i);
                        String title = jsonPost.getString(Constants.KEY_TITLE);
                        Log.i(NewMainActivity.TAG, "Post " + i + ": " + title);
                    }

                } else {
                    Log.i(NewMainActivity.TAG, "Unsuccessful HTTP Response Code: " + responseCode);
                }
                Log.i(NewMainActivity.TAG, "Code: " + responseCode);
            } catch (MalformedURLException e) {
                mUtils.logException(e);
            } catch (IOException e) {
                mUtils.logException(e);
            } catch (Exception e) {
                mUtils.logException(e);
            }

            return jsonResponse;
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            setProgressBarIndeterminateVisibility(false);
            boolean response = mUtils.handleAPIResponse(result);
            if (response) {
                reloadPostFragment();
            } else {
                updateDisplayForError();
            }
        }
    }

}
