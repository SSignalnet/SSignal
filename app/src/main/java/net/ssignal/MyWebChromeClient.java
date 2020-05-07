package net.ssignal;

import android.webkit.WebChromeClient;
import android.webkit.WebView;

public class MyWebChromeClient extends WebChromeClient {

    Object 创建者;

    MyWebChromeClient(Object 创建者) {
        this.创建者 = 创建者;
    }

    @Override
    public void onProgressChanged(WebView view, int newProgress) {
        super.onProgressChanged(view, newProgress);
        if (创建者 instanceof Fragment_TinyUniverse) {
            ((Fragment_TinyUniverse)创建者).onProgressChanged(view, newProgress);
        } else if (创建者 instanceof Fragment_Chating) {
            ((Fragment_Chating)创建者).onProgressChanged(view, newProgress);
        } else if (创建者 instanceof Fragment_DownloadFile) {
            ((Fragment_DownloadFile)创建者).onProgressChanged(view, newProgress);
        } else if (创建者 instanceof Fragment_Main) {
            ((Fragment_Main)创建者).onProgressChanged(view, newProgress);
        }
    }

}
