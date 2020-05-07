package net.ssignal.test;

import android.content.Context;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

public class ExceptionHandler implements Thread.UncaughtExceptionHandler
{
	private static ExceptionHandler exceptionHandler;
	public static ExceptionHandler getInstance()
	{
		if(exceptionHandler==null)
			exceptionHandler=new ExceptionHandler();
		return exceptionHandler;
	}
	private boolean inited=false;
	private Context ctx;
	private Thread.UncaughtExceptionHandler defaultHandler;
	public void init(Context ctx)
	{
		if(inited)return;
		this.ctx=ctx;
		defaultHandler=Thread.getDefaultUncaughtExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler(this);
	}
	@Override
	public void uncaughtException(Thread p1,Throwable p2)
	{
//p1:出错线程
//p2:错误
		StringWriter stringWriter=new StringWriter();
		PrintWriter printWriter=new PrintWriter(stringWriter);
		p2.printStackTrace(printWriter);
//保存错误
		String filename="error.txt";
		File parent=ctx.getExternalCacheDir();
		if (!parent.exists() || !parent.isDirectory()) {
			parent.mkdir();
		}
		File file=new File(parent,filename);
		try{
			if (file.exists()) { file.delete(); }
			FileOutputStream fos=new FileOutputStream(file);
			String e = stringWriter.toString();
			fos.write(e.getBytes());
			fos.close();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		defaultHandler.uncaughtException(p1,p2);
	}
}
