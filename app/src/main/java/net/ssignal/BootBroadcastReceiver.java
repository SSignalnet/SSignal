package net.ssignal;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
//            User.记录运行步骤("开机自启", context);
            if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
                Service_SSignal.启动服务(context.getApplicationContext(), 3);
            }
        } catch (Exception e) {
        }
    }

}
