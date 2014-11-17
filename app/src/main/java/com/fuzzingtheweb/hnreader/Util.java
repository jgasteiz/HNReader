package com.fuzzingtheweb.hnreader;

import android.content.Intent;
import android.net.Uri;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class Util {

    private static PostFragment mFragment;

    public Util() {

    }

    public void setFragment(PostFragment fragment) {
        mFragment = fragment;
    }

    public void refreshPosts() {

        mFragment.hideListView();

        final ArrayList<Post> postList = new ArrayList<Post>();

        Firebase topStoriesRef = new Firebase("https://hacker-news.firebaseio.com/v0/topstories");
        Query queryRef = topStoriesRef.limitToFirst(30);

        queryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                ArrayList children = (ArrayList) snapshot.getValue();
                System.out.println("Data changed!");

                final int[] index = {1};
                for(Iterator<Long> i = children.iterator(); i.hasNext(); ) {
                    Firebase itemsRef = new Firebase("https://hacker-news.firebaseio.com/v0/item/" + i.next());
                    itemsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snapshot) {

                            HashMap<String, Object> item = (HashMap<String, Object>) snapshot.getValue();

                            Post post = new Post((Long) item.get(Constants.KEY_ID), index[0]);
                            post.setBy((String) item.get(Constants.KEY_BY));
                            post.setKids((ArrayList<String>) item.get(Constants.KEY_KIDS));
                            post.setScore((Long) item.get(Constants.KEY_SCORE));
                            post.setTime((Long) item.get(Constants.KEY_TIME));
                            post.setTitle((String) item.get(Constants.KEY_TITLE));
                            post.setType((String) item.get(Constants.KEY_TYPE));
                            post.setUrl((String) item.get(Constants.KEY_URL));

                            postList.add(post);
                            index[0] = index[0] + 1;

                            mFragment.populateListView(postList);
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
}
