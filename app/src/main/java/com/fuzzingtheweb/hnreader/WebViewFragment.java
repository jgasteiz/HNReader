package com.fuzzingtheweb.hnreader;

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
            WebSettings webSettings = webView.getSettings();
            webSettings.setJavaScriptEnabled(true);
            webView.setWebViewClient(new WebViewClient());
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

}
