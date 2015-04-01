package com.fuzzingtheweb.hnreader.fragments;

import android.app.ListFragment;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.fuzzingtheweb.hnreader.CommentsActivity;
import com.fuzzingtheweb.hnreader.Constants;
import com.fuzzingtheweb.hnreader.R;
import com.fuzzingtheweb.hnreader.interfaces.OnCommentsFetched;
import com.fuzzingtheweb.hnreader.models.Comment;
import com.fuzzingtheweb.hnreader.tasks.FetchCommentsTask;

import java.util.ArrayList;
import java.util.List;

public class CommentListFragment extends ListFragment {

    private long mPostId;
    private ListView mListView;

    private ArrayList<Comment> mCommentList;
    private ArrayAdapter<Comment> mCommentListAdapter;

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
    public void onListItemClick(ListView listView, View view, final int position, long id) {
        super.onListItemClick(listView, view, position, id);
        Comment comment = mCommentList.get(position);

        // if there are kids, fetch them
        if (comment.getKids() != null && comment.getKids().size() > 0) {
            OnCommentsFetched onCommentsFetched = new OnCommentsFetched() {
                @Override
                public void onCommentsFetched(List<Comment> commentList) {
                    addChildrenComments(commentList, position);
                }

                @Override
                public void onNoComments() {
                    Toast.makeText(getActivity(), "No children comments here", Toast.LENGTH_SHORT).show();
                }
            };

            FetchCommentsTask fetchCommentsTask = new FetchCommentsTask(onCommentsFetched, comment.getId());
            fetchCommentsTask.execute();
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

        mCommentList = (ArrayList<Comment>) commentList;

        mCommentListAdapter = new ArrayAdapter<Comment> (
                getActivity(),
                R.layout.comment_item,
                R.id.item_index,
                mCommentList)
        {
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);

                Comment comment = mCommentList.get(position);

                ((TextView) view.findViewById(R.id.item_index))
                        .setText(Integer.toString(comment.getIndex()));
                ((TextView) view.findViewById(R.id.item_by))
                        .setText(comment.getBy());
                ((TextView) view.findViewById(R.id.item_text))
                        .setText(Html.fromHtml(comment.getText()));

                if (comment.getKids() != null && comment.getKids().size() > 0) {
                    TextView itemChildrenTextView = ((TextView) view.findViewById(R.id.item_children));
                    String childrenText = comment.getKids().size() + " replies to this post, tap here to load them";

                    itemChildrenTextView.setText(Html.fromHtml(childrenText));
                    itemChildrenTextView.setVisibility(View.VISIBLE);
                }

                return view;
            }
        };

        try {
            mListView.setAdapter(mCommentListAdapter);
        } catch (NullPointerException e) {
            Log.e(LOG_TAG, e.getMessage());
        }
    }

    public void addChildrenComments(final List<Comment> commentList, int parentPosition) {

        mCommentList.addAll(parentPosition, commentList);

        mCommentListAdapter.notifyDataSetChanged();
    }
}