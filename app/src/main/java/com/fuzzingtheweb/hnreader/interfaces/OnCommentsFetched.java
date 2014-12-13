package com.fuzzingtheweb.hnreader.interfaces;

import com.fuzzingtheweb.hnreader.models.Comment;

import java.util.List;

public interface OnCommentsFetched {
    void onCommentsFetched(List<Comment> commentList);

    void onNoComments();
}
