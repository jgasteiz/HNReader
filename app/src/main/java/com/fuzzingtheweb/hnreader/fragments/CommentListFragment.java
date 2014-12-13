package com.fuzzingtheweb.hnreader.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.fuzzingtheweb.hnreader.CommentsActivity;
import com.fuzzingtheweb.hnreader.Constants;
import com.fuzzingtheweb.hnreader.R;
import com.fuzzingtheweb.hnreader.interfaces.OnCommentsFetched;
import com.fuzzingtheweb.hnreader.models.Comment;
import com.fuzzingtheweb.hnreader.tasks.FetchCommentsTask;

import java.util.List;

public class CommentListFragment extends Fragment {

    private long mPostId;
    private ListView mListView;
    private static final String LOG_TAG = CommentListFragment.class.getSimpleName();

    public CommentListFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(Constants.KEY_ID)) {
            mPostId = getArguments().getLong(Constants.KEY_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_comment_list, container, false);

        mListView = (ListView) rootView.findViewById(android.R.id.list);
        OnCommentsFetched onCommentsFetched = new OnCommentsFetched() {
            @Override
            public void onCommentsFetched(List<Comment> commentList) {
                populateListView(commentList);
            }

            @Override
            public void onNoComments() {
                ((CommentsActivity) getActivity()).finishActivityNoComments();
            }
        };
        FetchCommentsTask fetchCommentsTask = new FetchCommentsTask(onCommentsFetched, mPostId);
        fetchCommentsTask.execute();

        return rootView;
    }

    public void populateListView(final List<Comment> commentList) {

        ArrayAdapter<Comment> commentListAdapter = new ArrayAdapter<Comment> (
                getActivity(),
                R.layout.comment_item,
                R.id.item_index,
                commentList)
        {
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);

                Comment comment = commentList.get(position);

                ((TextView) view.findViewById(R.id.item_index))
                        .setText(Integer.toString(comment.getIndex()));
                ((TextView) view.findViewById(R.id.item_by))
                        .setText(comment.getBy());
                ((TextView) view.findViewById(R.id.item_text))
                        .setText(comment.getText());

                return view;
            }
        };

        try {
            mListView.setAdapter(commentListAdapter);
        } catch (NullPointerException e) {
            Log.e(LOG_TAG, e.getMessage());
        }

    }
}