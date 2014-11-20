package com.fuzzingtheweb.hnreader.tasks;

import android.os.AsyncTask;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.fuzzingtheweb.hnreader.fragments.CommentListFragment;
import com.fuzzingtheweb.hnreader.CommentsActivity;
import com.fuzzingtheweb.hnreader.Constants;
import com.fuzzingtheweb.hnreader.models.Comment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class FetchCommentsTask extends AsyncTask<Long, Void, Void> {

    private static final String LOG_TAG = FetchCommentsTask.class.getSimpleName();
    private long mPostId;
    private CommentListFragment mContext;

    public FetchCommentsTask(long postId, CommentListFragment context) {
        mPostId = postId;
        mContext = context;
    }

    @Override
    protected Void doInBackground(Long... params) {

        Firebase postRef = new Firebase(Constants.KEY_ITEM_URL + mPostId);

        postRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                HashMap<String, Object> post = (HashMap<String, Object>) snapshot.getValue();

                // Close the parent activity if there are no comments to show.
                if (post.containsKey(Constants.KEY_KIDS) == false) {
                    CommentsActivity activity = (CommentsActivity) mContext.getActivity();
                    activity.finishActivityNoComments();
                    return;
                }

                ArrayList<Long> kids = (ArrayList<Long>) post.get(Constants.KEY_KIDS);

                final ArrayList<Comment> commentList = new ArrayList<Comment>();

                final int[] index = {1};
                for(Iterator<Long> i = kids.iterator(); i.hasNext(); ) {
                    Firebase itemsRef = new Firebase(Constants.KEY_ITEM_URL + i.next());
                    itemsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snapshot) {

                            HashMap<String, Object> child = (HashMap<String, Object>) snapshot.getValue();

                            Comment comment = new Comment(
                                    index[0],
                                    (String) child.get(Constants.KEY_BY),
                                    (Long) child.get(Constants.KEY_ID),
                                    mPostId,
                                    (ArrayList<String>) child.get(Constants.KEY_KIDS),
                                    (Long) child.get(Constants.KEY_TIME),
                                    (String) child.get(Constants.KEY_TEXT));

                            commentList.add(comment);
                            index[0] = index[0] + 1;

                            mContext.populateListView(commentList);
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
