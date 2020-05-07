package net.ssignal;

import android.annotation.TargetApi;
import android.net.http.SslError;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import net.ssignal.protocols.ProtocolPath;

public class MyWebViewClient extends WebViewClient {

    Object 创建者;

    MyWebViewClient(Object 创建者1) {
        创建者 = 创建者1;
    }

//    @Override
//    public void onPageFinished(WebView view, String url) {
//        super.onPageFinished(view, url);
//    }

//    @Override
//    public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
//        if (创建者 instanceof bm_zct) {
//            ((bm_zct)创建者).onReceivedError(view, request, error);
//        }
//    }

    @TargetApi(21)
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        if (创建者 instanceof Fragment_TinyUniverse) {
            ((Fragment_TinyUniverse) 创建者).shouldOverrideUrlLoading(view, request.getUrl().toString());
        } else if (创建者 instanceof Fragment_Chating) {
            ((Fragment_Chating) 创建者).shouldOverrideUrlLoading(view, request.getUrl().toString());
        }
        return true;
    }

    @TargetApi(14)
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        if (创建者 instanceof Fragment_TinyUniverse) {
            ((Fragment_TinyUniverse) 创建者).shouldOverrideUrlLoading(view, url);
        } else if (创建者 instanceof Fragment_Chating) {
            ((Fragment_Chating) 创建者).shouldOverrideUrlLoading(view, url);
        }
        return true;
    }

    @Override
    public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
        if (BuildConfig.DEBUG && !ProtocolPath.调试时访问真实网站) {
            handler.proceed();
        } else {
            super.onReceivedSslError(view, handler, error);
        }
    }
}
