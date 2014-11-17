package com.fuzzingtheweb.hnreader;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleCursorAdapter;

/**
 * A placeholder fragment containing a simple view.
 */
public class PostFragment extends ListFragment {

    private View mRootView;
    private Activity mActivity;
    private Util mUtil;
    private ListView mListView;
    private RelativeLayout mProgressLayout;

    public static final int OPEN_IN_BROWSER_ID = Menu.FIRST + 1;
    public static final int SHARE_ID = Menu.FIRST + 2;
    private static final String LOG_TAG = PostFragment.class.getSimpleName();

    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static PostFragment newInstance(int sectionNumber) {
        PostFragment fragment = new PostFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * The fragment's current callback object, which is notified of list item
     * clicks.
     */
    private Callbacks mCallbacks = sPostCallbacks;

    /**
     * Turns on activate-on-click mode. When this mode is on, list items will be
     * given the 'activated' state when touched.
     */
    public void setActivateOnItemClick(boolean activateOnItemClick) {
        // When setting CHOICE_MODE_SINGLE, ListView will automatically
        // give items the 'activated' state when touched.
        getListView().setChoiceMode(activateOnItemClick
                ? ListView.CHOICE_MODE_SINGLE
                : ListView.CHOICE_MODE_NONE);
    }

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callbacks {
        public void onItemSelected(MenuItem item);
        public void onItemClick(long id);
        void onEmptyList();
        void onRefreshPosts();
    }

    /**
     * A dummy implementation of the {@link Callbacks} interface that does
     * nothing. Used only when this fragment is not attached to an activity.
     */
    private static Callbacks sPostCallbacks = new Callbacks() {
        @Override
        public void onItemSelected(MenuItem item) {
        }

        @Override
        public void onItemClick(long id) {

        }

        @Override
        public void onEmptyList() {

        }

        @Override
        public void onRefreshPosts() {

        }
    };

    public PostFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        mActivity = getActivity();
        mUtil = new Util(mActivity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_post_list, container, false);

        // Initialize listview and empty relative layout.
        mListView = (ListView) mRootView.findViewById(android.R.id.list);
        mProgressLayout = (RelativeLayout) mRootView.findViewById(android.R.id.empty);

        return mRootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        registerForContextMenu(getListView());
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
        mCallbacks = sPostCallbacks;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                mCallbacks.onRefreshPosts();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
        super.onListItemClick(listView, view, position, id);
        mCallbacks.onItemClick(id);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, OPEN_IN_BROWSER_ID, 0, R.string.open_browser);
        menu.add(0, SHARE_ID, 0, R.string.action_share);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        boolean result = super.onContextItemSelected(item);
        mCallbacks.onItemSelected(item);
        return result;
    }

    public void hideListView() {
        mListView.setVisibility(View.GONE);
        mProgressLayout.setVisibility(View.VISIBLE);
    }

    public void showListView() {
        mProgressLayout.setVisibility(View.GONE);
        mListView.setVisibility(View.VISIBLE);
    }

    /**
     * Populate the main list view with the database content.
     */
    public void populateListView() {
        Log.v(LOG_TAG, "Populating list view");
        Cursor postsCursor;
        int postItemLayout;
        String[] keys;
        int[] ids;

        postsCursor = mUtil.fetchAllPosts();
        keys = mUtil.getAllPostsKeys();
        ids = mUtil.getAllPostsIds();
        postItemLayout = R.layout.post_item;
        mActivity.startManagingCursor(postsCursor);

        // TODO: use custom adapter
        // http://stackoverflow.com/questions/10828657/how-to-mark-views-in-a-listview
        SimpleCursorAdapter posts =
                new SimpleCursorAdapter(mActivity, postItemLayout, postsCursor, keys, ids);

        mListView.setAdapter(posts);

        if (posts.isEmpty()) {
            mCallbacks.onEmptyList();
        }
    }

}
