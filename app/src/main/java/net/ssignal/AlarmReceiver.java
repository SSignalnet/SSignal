package net.ssignal;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import static net.ssignal.Activity_Main.主活动;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            if (intent.getAction().equals("net.ssignal.Activity_Main")) {
                if (主活动 != null) {
                    主活动.终止活动();
                }
            }
        } catch (Exception e) {
        }
    }

}
