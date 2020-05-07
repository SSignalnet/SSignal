package net.ssignal;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;

import androidx.annotation.Nullable;

import net.ssignal.language.Text;
import net.ssignal.util.SharedMethod;

import static net.ssignal.Activity_Main.主活动;
import static net.ssignal.User.当前用户;
import static net.ssignal.Robot_MainControl.主控机器人;
import static net.ssignal.protocols.ProtocolPath.调试时访问真实网站;

public class Service_SSignal extends Service {

    private static boolean 启动服务 = false;
    private static long 上次时间 = 0;
    static long 屏幕亮了 = 0;

    private ScreenStatusReceiver 屏幕状态广播接收器;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String 渠道编号 = getPackageName() + ".service";
            NotificationManager 通知管理器 = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            NotificationChannel 通知的渠道 = new NotificationChannel(渠道编号, "Background Service", NotificationManager.IMPORTANCE_LOW);
            通知管理器.createNotificationChannel(通知的渠道);
            Notification.Builder 通知创建器 = new Notification.Builder(getApplicationContext(), 渠道编号);
            startForeground(100, 通知创建器.build());
        }
        if (启动服务) {
            启动服务 = false;
        } else {
            Intent 意图 = new Intent(getApplicationContext(), Service_SSignal.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                getApplicationContext().startForegroundService(意图);
            } else {
                getApplicationContext().startService(意图);
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Context 运行环境 = getApplicationContext();
        if (当前用户 == null) { 当前用户 = new User(运行环境); }
        if (当前用户.已登录()) {
            if (Text.界面文字 == null) { SharedMethod.载入界面文字(运行环境); }
            TaskName 类 = new TaskName();
            类 = null;
            if (主控机器人 == null) {
                主控机器人 = new Robot_MainControl(运行环境);
                主控机器人.自检_已登录();
                主控机器人.从未自检 = false;
            }
            PowerManager 电源管理器 = (PowerManager) getSystemService(Context.POWER_SERVICE);
            if (电源管理器.isInteractive()) {
                屏幕亮了 = System.currentTimeMillis();
            } else {
                屏幕亮了 = 0;
            }
            屏幕状态广播接收器 = new ScreenStatusReceiver();
            IntentFilter 意图过滤器 = new IntentFilter();
            意图过滤器.addAction(Intent.ACTION_SCREEN_ON);
            意图过滤器.addAction(Intent.ACTION_SCREEN_OFF);
            registerReceiver(屏幕状态广播接收器, 意图过滤器);
            return START_STICKY;
        } else {
            stopSelf();
            return START_NOT_STICKY;
        }
    }

    @Override
    public void onDestroy() {
        if (屏幕状态广播接收器 != null) {
            unregisterReceiver(屏幕状态广播接收器);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            stopForeground(true);
        }
        super.onDestroy();
    }

    static void 启动服务(Context 运行环境, int 代码) {
        long 本次时间 = System.currentTimeMillis();
        if (上次时间 > 0) {
            if (本次时间 - 上次时间 < 50000) {
                return;
            }
        }
        上次时间 = 本次时间;
        ConnectivityManager 网络连接管理器 = ((ConnectivityManager) 运行环境.getSystemService(Context.CONNECTIVITY_SERVICE));
        if (网络连接管理器 != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                NetworkCapabilities 网络访问能力 = 网络连接管理器.getNetworkCapabilities(网络连接管理器.getActiveNetwork());
                if (网络访问能力 != null) {
                    if (!网络访问能力.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) &&
                        !网络访问能力.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) &&
                        !网络访问能力.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
//                        User.记录运行步骤("没有网络", 运行环境);
                        return;
                    }
                } else {
                    if (!BuildConfig.DEBUG || 调试时访问真实网站) {
//                        User.记录运行步骤("没有网络", 运行环境);
                        return;
                    }
                }
            } else {
                NetworkInfo 网络信息 = 网络连接管理器.getActiveNetworkInfo();
                if (网络信息 != null) {
                    if (!网络信息.isConnected()) {
//                        User.记录运行步骤("没有网络", 运行环境);
                        return;
                    }
                }
            }
        }
        if (!SharedMethod.服务是否运行(运行环境, "Service_SSignal")) {
//            User.记录运行步骤("服务未运行。启动服务", 运行环境);
            if (代码 == 2 && 主活动 == null) {
                SharedPreferences 共享的设置 = 运行环境.getSharedPreferences("appsettings", 运行环境.MODE_PRIVATE);
                if (共享的设置 != null) {
                    boolean 后台自启 = 共享的设置.getBoolean("AutoStart", false);
                    if (!后台自启) {
                        SharedPreferences.Editor 编辑器 = 共享的设置.edit();
                        编辑器.putBoolean("AutoStart", true);
                        编辑器.commit();
                    }
                }
            }
            启动服务 = true;
            Intent 意图 = new Intent(运行环境, Service_SSignal.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                运行环境.startForegroundService(意图);
            } else {
                运行环境.startService(意图);
            }
        } else if (主控机器人 != null) {
            if (!主控机器人.自检_已登录()) {
                return;
            }
//            User.记录运行步骤("心跳", 运行环境);
            主控机器人.心跳();
            if (当前用户.加入的大聊天群 != null) {
                主控机器人.检查大聊天群是否有新消息();
            }
        }
    }

}
