package net.ssignal;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.webkit.DownloadListener;

public class MyDownloadListener implements DownloadListener {

    Activity 场景;
    Object 创建者;

    MyDownloadListener(Activity 场景1, Object 创建者1) {
        场景 = 场景1;
        创建者 = 创建者1;
    }

    @Override
    public void onDownloadStart(String s, String s1, String s2, String s3, long l) {
        Uri uri = Uri.parse(s);
        Intent 意图 = new Intent(Intent.ACTION_VIEW, uri);
        场景.startActivity(意图);
    }

}
