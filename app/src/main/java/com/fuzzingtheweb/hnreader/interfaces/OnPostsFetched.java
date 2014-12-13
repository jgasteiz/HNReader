package com.fuzzingtheweb.hnreader.interfaces;

import com.fuzzingtheweb.hnreader.models.Post;

import java.util.List;

public interface OnPostsFetched {
    void onPostsFetched(List<Post> postList);
}
