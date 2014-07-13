package com.fuzzingtheweb.hnreader;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

/**
 * A placeholder fragment containing a simple view.
 */
public class PostFragment extends ListFragment {

    private View mRootView;
    private PostDBAdapter mDbHelper;
    private Activity mActivity;

    /**
     * The fragment's current callback object, which is notified of list item
     * clicks.
     */
    private Callbacks mCallbacks = sDummyCallbacks;

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callbacks {
        /**
         * Callback for when an item has been selected.
         */
        public void onItemSelected(String id);
    }

    /**
     * A dummy implementation of the {@link Callbacks} interface that does
     * nothing. Used only when this fragment is not attached to an activity.
     */
    private static Callbacks sDummyCallbacks = new Callbacks() {
        @Override
        public void onItemSelected(String id) {
        }
    };

    public PostFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mDbHelper = new PostDBAdapter(getActivity());
        mDbHelper.open();

        mActivity = getActivity();

        mRootView = inflater.inflate(R.layout.fragment_post_list, container, false);

        populateListView();

        return mRootView;
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Activities containing this fragment must implement its callbacks.
        if (!(activity instanceof Callbacks)) {
            throw new IllegalStateException("Activity must implement fragment's callbacks.");
        }

        mCallbacks = (Callbacks) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();

        // Reset the active callbacks interface to the dummy implementation.
        mCallbacks = sDummyCallbacks;
    }

    @Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
        super.onListItemClick(listView, view, position, id);

        String postUrl = getPostUrl(id);

        // Notify the active callbacks interface (the activity, if the
        // fragment is attached to one) that an item has been selected.
        mCallbacks.onItemSelected(postUrl);
    }

    /**
     * Populate the main list view with the database content.
     */
    public void populateListView() {
        Cursor postsCursor = mDbHelper.fetchAllPosts();
        mActivity.startManagingCursor(postsCursor);

        String[] keys = {Constants.KEY_INDEX, Constants.KEY_TITLE, Constants.KEY_PRETTY_URL,
                Constants.KEY_SCORE, Constants.KEY_AUTHOR, Constants.KEY_POSTED_AGO, Constants.KEY_NUM_COMMENTS};

        int[] ids = { R.id.item_index, R.id.item_title, R.id.item_url,
                R.id.item_score, R.id.item_author, R.id.item_posted_ago, R.id.item_num_comments };

        SimpleCursorAdapter posts =
                new SimpleCursorAdapter(mActivity, R.layout.post_item, postsCursor, keys, ids);

        ListView listView = (ListView) mRootView.findViewById(android.R.id.list);
        listView.setAdapter(posts);
    }

    /**
     * Given an item id, return the url of the item.
     *
     * @param id in the database for the selected item.
     * @return the item url
     */
    private String getPostUrl(long id) {
        Cursor cursor = mDbHelper.fetchPost(id);
        int urlColIndex = cursor.getColumnIndex("url");
        return cursor.getString(urlColIndex);
    }

}
