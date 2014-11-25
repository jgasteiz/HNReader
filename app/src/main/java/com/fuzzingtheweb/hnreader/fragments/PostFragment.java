package com.fuzzingtheweb.hnreader.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fuzzingtheweb.hnreader.R;
import com.fuzzingtheweb.hnreader.models.Post;
import com.fuzzingtheweb.hnreader.tasks.FetchPostsTask;

import java.util.ArrayList;

/**
 * A placeholder fragment containing a simple view.
 */
public class PostFragment extends ListFragment {

    private ListView mListView;
    private RelativeLayout mProgressLayout;
    private ArrayList<Post> mPostList;

    public static final int OPEN_IN_BROWSER_ID = Menu.FIRST + 1;
    public static final int SHARE_ID = Menu.FIRST + 2;
    public static final int VIEW_COMMENTS_ID = Menu.FIRST + 3;
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
        public void onItemSelected(Post post, MenuItem item);
        public void onItemClick(String postUrl);
    }

    /**
     * A dummy implementation of the {@link Callbacks} interface that does
     * nothing. Used only when this fragment is not attached to an activity.
     */
    private static Callbacks sPostCallbacks = new Callbacks() {
        @Override
        public void onItemSelected(Post post, MenuItem item) {
        }

        @Override
        public void onItemClick(String postUrl) {

        }
    };

    public PostFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_post_list, container, false);
        mListView = (ListView) rootView.findViewById(android.R.id.list);
        mProgressLayout = (RelativeLayout) rootView.findViewById(android.R.id.empty);

        loadPosts();

        return rootView;
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
    public void onListItemClick(ListView listView, View view, int position, long id) {
        super.onListItemClick(listView, view, position, id);
        Post post = mPostList.get(position);
        mCallbacks.onItemClick(post.getUrl());
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, OPEN_IN_BROWSER_ID, 0, R.string.open_browser);
        menu.add(0, SHARE_ID, 0, R.string.action_share);
        menu.add(0, VIEW_COMMENTS_ID, 0, R.string.action_view_comments);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        boolean result = super.onContextItemSelected(item);
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        Post post = mPostList.get(info.position);
        mCallbacks.onItemSelected(post, item);
        return result;
    }

    public void loadPosts() {
        mListView.setVisibility(View.GONE);
        mProgressLayout.setVisibility(View.VISIBLE);
        FetchPostsTask fetchPostsTask = new FetchPostsTask(this);
        fetchPostsTask.execute();
    }

    public void populateListView(final ArrayList<Post> postList) {

        mPostList = postList;

        ArrayAdapter<Post> postListAdapter = new ArrayAdapter<Post> (
                getActivity(),
                R.layout.post_item,
                R.id.item_index,
                postList)
        {
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);

                Post post = postList.get(position);

                ((TextView) view.findViewById(R.id.item_index))
                        .setText(Integer.toString(post.getIndex()));
                ((TextView) view.findViewById(R.id.item_title))
                        .setText(post.getTitle());
                ((TextView) view.findViewById(R.id.item_url))
                        .setText(post.getPrettyUrl());

                ((TextView) view.findViewById(R.id.item_author))
                        .setText("by " + post.getBy());
                ((TextView) view.findViewById(R.id.item_score))
                        .setText(post.getScore() + " points");

                // Calculate postedAgo
                CharSequence postedAgo = DateUtils.getRelativeTimeSpanString(
                        post.getTime() * 1000,
                        System.currentTimeMillis(),
                        DateUtils.SECOND_IN_MILLIS);
                ((TextView) view.findViewById(R.id.item_posted_ago))
                        .setText(postedAgo);

                int numComments = 0;
                if (post.getKids() != null) {
                    numComments = post.getKids().size();
                }
                ((TextView) view.findViewById(R.id.item_num_comments))
                        .setText(numComments + " comments");

                return view;
            }
        };

        try {
            mListView.setAdapter(postListAdapter);
        } catch (NullPointerException e) {
            Log.e(LOG_TAG, e.getMessage());
        }
    }

}
