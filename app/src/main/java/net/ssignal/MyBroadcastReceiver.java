package net.ssignal;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class MyBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            if (intent.getAction().equals(Intent.ACTION_TIME_TICK)) {
                Service_SSignal.启动服务(context.getApplicationContext(), 2);
            }
        } catch (Exception e) {
        }
    }
}
