package net.ssignal;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import static net.ssignal.User.当前用户;

public class ScreenStatusReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            switch (intent.getAction()) {
                case "android.intent.action.SCREEN_ON":
                    Service_SSignal.屏幕亮了 = System.currentTimeMillis();
//                    User.记录运行步骤("屏幕亮了", context);
                    break;
                case "android.intent.action.SCREEN_OFF":
                    Service_SSignal.屏幕亮了 = 0;
//                    User.记录运行步骤("屏幕关闭", context);
                    break;
            }
        } catch (Exception e) {
        }
    }
}
