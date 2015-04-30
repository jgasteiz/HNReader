package com.fuzzingtheweb.hnreader.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fuzzingtheweb.hnreader.CommentsActivity;
import com.fuzzingtheweb.hnreader.R;
import com.fuzzingtheweb.hnreader.interfaces.OnPostsFetched;
import com.fuzzingtheweb.hnreader.models.Post;
import com.fuzzingtheweb.hnreader.tasks.FetchPostsTask;

import java.util.ArrayList;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class PostFragment extends ListFragment {

    private ListView mListView;
    private RelativeLayout mProgressLayout;
    private ArrayList<Post> mPostList;

    private static final String LOG_TAG = PostFragment.class.getSimpleName();

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
        public void onItemClick(String postUrl);
    }

    /**
     * A dummy implementation of the {@link Callbacks} interface that does
     * nothing. Used only when this fragment is not attached to an activity.
     */
    private static Callbacks sPostCallbacks = new Callbacks() {

        @Override
        public void onItemClick(String postUrl) {

        }
    };

    public PostFragment() {
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

    public void loadPosts() {
        mListView.setVisibility(View.GONE);
        mProgressLayout.setVisibility(View.VISIBLE);
        OnPostsFetched onPostsFetched = new OnPostsFetched() {
            @Override
            public void onPostsFetched(List<Post> postList) {
                populateListView(postList);
            }
        };
        FetchPostsTask fetchPostsTask = new FetchPostsTask(onPostsFetched);
        fetchPostsTask.execute();
    }

    public void populateListView(final List<Post> postList) {

        mPostList = (ArrayList<Post>) postList;

        ArrayAdapter<Post> postListAdapter = new ArrayAdapter<Post> (
                getActivity(),
                R.layout.post_item,
                R.id.item_index,
                postList)
        {
            public View getView(final int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);

                final Post post = postList.get(position);

                ((TextView) view.findViewById(R.id.item_index))
                        .setText(Integer.toString(post.getIndex()));
                ((TextView) view.findViewById(R.id.item_title))
                        .setText(post.getTitle());
                ((TextView) view.findViewById(R.id.item_url))
                        .setText(post.getPrettyUrl());

                String postDetails = "";

                // Calculate postedAgo
                CharSequence postedAgo = DateUtils.getRelativeTimeSpanString(
                        post.getTime() * 1000,
                        System.currentTimeMillis(),
                        DateUtils.SECOND_IN_MILLIS);

                // Get num comments
                int numComments = 0;
                if (post.getKids() != null) {
                    numComments = post.getKids().size();
                }

                postDetails = postDetails + "by " + post.getBy();
                postDetails = postDetails + "  " + post.getScore() + " points";
                postDetails = postDetails + "  " + postedAgo;
                postDetails = postDetails + "  " + numComments + " comments";

                ((TextView) view.findViewById(R.id.post_details))
                        .setText(postDetails);

                view.findViewById(R.id.view_comments).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getActivity(), CommentsActivity.class);
                        intent.putExtra("id", post.getId());
                        startActivity(intent);
                    }
                });

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
