package com.fuzzingtheweb.hnreader;

import android.app.Activity;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.fuzzingtheweb.hnreader.data.PostDBAdapter;

/**
 * A placeholder fragment containing a simple view.
 */
public class PostFragment extends ListFragment {

    private View mRootView;
    private Activity mActivity;
    private Util mUtil;
    private int mSectionNumber;

    public static final int FAVORITE_ID = Menu.FIRST;
    public static final int OPEN_IN_BROWSER_ID = Menu.FIRST + 1;
    public static final int SHARE_ID = Menu.FIRST + 2;
    public static final int REMOVE_FAVORITE_ID = Menu.FIRST + 3;

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

        populateListView();
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

        mSectionNumber = getArguments().getInt(ARG_SECTION_NUMBER);
        ((MainActivity) activity).onSectionAttached(mSectionNumber);

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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (mSectionNumber == Constants.ALL_ITEMS) {
            inflater.inflate(R.menu.post_fragment, menu);
        }
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
        if (mSectionNumber == Constants.ALL_ITEMS) {
            menu.add(0, FAVORITE_ID, 0, R.string.menu_favorite);
        } else {
            menu.add(0, REMOVE_FAVORITE_ID, 0, R.string.menu_remove_favorite);
        }
        menu.add(0, OPEN_IN_BROWSER_ID, 0, R.string.open_browser);
        menu.add(0, SHARE_ID, 0, R.string.action_share);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        boolean result = super.onContextItemSelected(item);
        mCallbacks.onItemSelected(item);
        return result;
    }

    /**
     * Populate the main list view with the database content.
     */
    public void populateListView() {
        Cursor postsCursor;
        int postItemLayout;
        String[] keys;
        int[] ids;

        // Depending on the section we are in, show a different item layout
        if (mSectionNumber == Constants.ALL_ITEMS) {
            postsCursor = mUtil.fetchAllPosts();
            keys = mUtil.getAllPostsKeys();
            ids = mUtil.getAllPostsIds();
            postItemLayout = R.layout.post_item;
        } else {
            postsCursor = mUtil.fetchFavoritePosts();
            keys = mUtil.getFavoritePostsKeys();
            ids = mUtil.getFavoritePostsIds();
            postItemLayout = R.layout.post_fav_item;
        }
        mActivity.startManagingCursor(postsCursor);

        // TODO: use custom adapter
        // http://stackoverflow.com/questions/10828657/how-to-mark-views-in-a-listview
        SimpleCursorAdapter posts =
                new SimpleCursorAdapter(mActivity, postItemLayout, postsCursor, keys, ids);

        ListView listView = (ListView) mRootView.findViewById(android.R.id.list);
        listView.setAdapter(posts);

        if (posts.isEmpty()) {
            mCallbacks.onEmptyList();
        }
    }

}
