package com.fuzzingtheweb.hnreader;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;
import com.fuzzingtheweb.hnreader.data.PostDBAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class Util {

    private PostDBAdapter mDbHelper;

    public Util(Context context) {
        mDbHelper = new PostDBAdapter(context);
        mDbHelper.open();
    }

    public void createPost(Post post) {
        mDbHelper.createPost(post.getIndex(), post.getId());
    }

    public void updatePost(Post post) {
        int numComments = 0;
        if (post.getKids() != null) {
            numComments = post.getKids().size();
        }

        mDbHelper.updatePost(post.getId(),
            post.getTitle(),
            post.getUrl(),
            post.getUrl(),
            post.getScore(),
            post.getBy(),
            post.getTime(),
            numComments);
    }

    public void refreshPosts(final PostFragment fragment) {

        mDbHelper.deleteAllPosts();

        Firebase topStoriesRef = new Firebase("https://hacker-news.firebaseio.com/v0/topstories");
        Query queryRef = topStoriesRef.limitToFirst(30);

        queryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                ArrayList children = (ArrayList) snapshot.getValue();
                System.out.println("Data changed!");

                final ArrayList<Post> postList = new ArrayList<Post>();

                int index = 1;
                for(Iterator<Long> i = children.iterator(); i.hasNext(); ) {
                    Long item = i.next();

                    Post post = new Post(item, index);
                    createPost(post);
                    index++;


                    Firebase itemsRef = new Firebase("https://hacker-news.firebaseio.com/v0/item/" + item);

                    itemsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snapshot) {

                            HashMap item = (HashMap) snapshot.getValue();

                            Post post = new Post((Long) item.get("id"), 0);
                            post.setBy((String) item.get("by"));
                            post.setKids((ArrayList<String>) item.get("kids"));
                            post.setScore((Long) item.get("score"));
                            post.setTime((Long) item.get("time"));
                            post.setTitle((String) item.get("title"));
                            post.setType((String) item.get("type"));
                            post.setUrl((String) item.get("url"));

                            updatePost(post);
                            postList.add(post);

                            fragment.populateListView();
                        }

                        @Override
                        public void onCancelled(FirebaseError firebaseError) {
                            System.out.println("The read failed: " + firebaseError.getMessage());
                        }
                    });
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                System.out.println("The read failed: " + firebaseError.getMessage());
            }
        });
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

    public int[] getAllPostsIds() {
        int[] ids = { R.id.item_index, R.id.item_title, R.id.item_url,
                R.id.item_score, R.id.item_author, R.id.item_posted_ago,
                R.id.item_num_comments };
        return ids;
    }

    public Cursor fetchAllPosts() {
        return mDbHelper.fetchAllPosts();
    }
}
