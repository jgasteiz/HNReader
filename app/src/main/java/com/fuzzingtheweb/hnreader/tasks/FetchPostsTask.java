package com.fuzzingtheweb.hnreader.tasks;

import android.os.AsyncTask;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;
import com.fuzzingtheweb.hnreader.Constants;
import com.fuzzingtheweb.hnreader.interfaces.OnPostsFetched;
import com.fuzzingtheweb.hnreader.models.Post;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class FetchPostsTask extends AsyncTask<Long, Void, Void> {

    private static final String LOG_TAG = FetchPostsTask.class.getSimpleName();
    private OnPostsFetched mListener;
    private static final String YCOMBINATOR_ITEM_URL = "https://news.ycombinator.com/item?id=";

    public FetchPostsTask(OnPostsFetched listener) {
        mListener = listener;
    }

    @Override
    protected Void doInBackground(Long... params) {

        Firebase topStoriesRef = new Firebase(Constants.KEY_TOP_STORIES_URL);
        Query queryRef = topStoriesRef.limitToFirst(30);

        queryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                ArrayList children = (ArrayList) snapshot.getValue();

                final ArrayList<Post> postList = new ArrayList<Post>();

                final int[] index = {1};
                for(Iterator<Long> i = children.iterator(); i.hasNext(); ) {
                    Firebase itemsRef = new Firebase(Constants.KEY_ITEM_URL + i.next());
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

                            String url = (String) item.get(Constants.KEY_URL);
                            if (url.isEmpty()) {
                                url = YCOMBINATOR_ITEM_URL + post.getId();
                            }
                            post.setUrl(url);

                            String prettyUrl = url;
                            String[] splitUrl = prettyUrl.split("/");
                            if (splitUrl.length > 2) {
                                prettyUrl = splitUrl[2];
                            }
                            post.setPrettyUrl(prettyUrl);

                            postList.add(post);
                            index[0] = index[0] + 1;

                            mListener.onPostsFetched(postList);
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

        return null;
    }
}
