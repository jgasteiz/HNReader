package com.fuzzingtheweb.hnreader.tasks;

import android.os.AsyncTask;

import com.fuzzingtheweb.hnreader.interfaces.OnCommentsFetched;
import com.fuzzingtheweb.hnreader.models.Comment;
import com.fuzzingtheweb.hnreader.utils.AlgoliaCommentsParser;
import com.fuzzingtheweb.hnreader.utils.JSONParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class FetchCommentsTask extends AsyncTask<Long, Void, List<Comment>> {

    private static final String LOG_TAG = FetchCommentsTask.class.getSimpleName();
    private long mPostId;
    private OnCommentsFetched mListener;

    public FetchCommentsTask(OnCommentsFetched listener, long postId) {
        mListener = listener;
        mPostId = postId;
    }

    @Override
    protected List<Comment> doInBackground(Long... params) {

        String url = "http://hn.algolia.com/api/v1/search?tags=comment,story_" + mPostId;

        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject = jsonParser.getJSONFromUrl(url);
        AlgoliaCommentsParser algoliaCommentsParser = new AlgoliaCommentsParser();

        try {
            List<Comment> commentList = algoliaCommentsParser.getCommentsList(jsonObject);
            return commentList;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    protected void onPostExecute(List<Comment> commentList) {
        mListener.onCommentsFetched(commentList);
    }

//        Firebase postRef = new Firebase(Constants.KEY_ITEM_URL + mPostId);
//
//        postRef.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot snapshot) {
//                HashMap<String, Object> post = (HashMap<String, Object>) snapshot.getValue();
//
//                // Close the parent activity if there are no comments to show.
//                if (!post.containsKey(Constants.KEY_KIDS)) {
//                    mListener.onNoComments();
//                    return;
//                }
//
//                ArrayList<Long> kids = (ArrayList<Long>) post.get(Constants.KEY_KIDS);
//
//                final ArrayList<Comment> commentList = new ArrayList<>();
//
//                final int[] index = {1};
//                for (Long kid : kids) {
//                    Firebase itemsRef = new Firebase(Constants.KEY_ITEM_URL + kid);
//                    itemsRef.addListenerForSingleValueEvent(new ValueEventListener() {
//                        @Override
//                        public void onDataChange(DataSnapshot snapshot) {
//
//                            HashMap<String, Object> child = (HashMap<String, Object>) snapshot.getValue();
//
//                            Comment comment = new Comment(
//                                    index[0],
//                                    (String) child.get(Constants.KEY_BY),
//                                    (Long) child.get(Constants.KEY_ID),
//                                    mPostId,
//                                    (ArrayList<String>) child.get(Constants.KEY_KIDS),
//                                    (Long) child.get(Constants.KEY_TIME),
//                                    (String) child.get(Constants.KEY_TEXT));
//
//                            commentList.add(comment);
//                            index[0] = index[0] + 1;
//
//                            mListener.onCommentsFetched(commentList);
//                        }
//
//                        @Override
//                        public void onCancelled(FirebaseError firebaseError) {
//                            System.out.println("The read failed: " + firebaseError.getMessage());
//                        }
//                    });
//                }
//            }
//
//            @Override
//            public void onCancelled(FirebaseError firebaseError) {
//                System.out.println("The read failed: " + firebaseError.getMessage());
//            }
//        });
//
//        return null;
//    }
}
