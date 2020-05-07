package net.ssignal;

import android.app.Application;
import android.content.Intent;
import android.content.IntentFilter;

import net.ssignal.test.ExceptionHandler;

//import com.squareup.leakcanary.LeakCanary;

public class myapp extends Application
{
	@Override
	public void onCreate()
	{
		super.onCreate();
//		if (LeakCanary.isInAnalyzerProcess(this)) {
//			return;
//		}
//		LeakCanary.install(this);

		ExceptionHandler.getInstance().init(this);

		registerReceiver(new MyBroadcastReceiver(), new IntentFilter(Intent.ACTION_TIME_TICK));
	}
}
