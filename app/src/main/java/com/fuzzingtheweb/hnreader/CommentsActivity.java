package com.fuzzingtheweb.hnreader;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.widget.Toast;

import com.fuzzingtheweb.hnreader.fragments.CommentListFragment;


public class CommentsActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Create an intent for displaying the url in a webView
        Intent intent = getIntent();
        long postId = intent.getLongExtra(Constants.KEY_ID, -1);

        if (savedInstanceState == null) {
            Bundle arguments = new Bundle();
            arguments.putLong(Constants.KEY_ID, postId);
            CommentListFragment fragment = new CommentListFragment();
            fragment.setArguments(arguments);
            getFragmentManager().beginTransaction()
                    .add(R.id.container, fragment)
                    .commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == android.R.id.home) {
            this.finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void finishActivityNoComments() {
        Toast.makeText(this, "No comments for that post", Toast.LENGTH_LONG).show();
        this.finish();
    }
}
