package com.fuzzingtheweb.hnreader.fragments;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;

import com.fuzzingtheweb.hnreader.R;

/**
 * A placeholder fragment containing a simple view.
 */
public class WebViewFragment extends Fragment {

    public static final String KEY_URL = "url";
    protected String mUrl;

    public WebViewFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(KEY_URL)) {
            mUrl = getArguments().getString(KEY_URL);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_web_view, container, false);

        if (mUrl != null) {
            WebView webView = (WebView) rootView.findViewById(R.id.web_view);
            webView = setWebViewSettings(webView);
            webView.loadUrl(mUrl);

            final RelativeLayout progressLayout = (RelativeLayout) rootView.findViewById(R.id.progress_layout);
            webView.setWebViewClient(new WebViewClient() {
                public void onPageStarted(WebView view, String url, Bitmap favicon) {
                    progressLayout.setVisibility(View.VISIBLE);
                }
                public void onPageFinished(WebView view, String url) {
                    progressLayout.setVisibility(View.GONE);
                }
            });
        }

        return rootView;
    }

    public WebView setWebViewSettings(WebView webView) {
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);
        webView.setWebViewClient(new WebViewClient());
        return webView;
    }
}
