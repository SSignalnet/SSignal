package net.ssignal;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Message;

import net.ssignal.network.MyX509TrustManager;
import net.ssignal.protocols.ProtocolPath;
import net.ssignal.util.SharedMethod;

import java.io.File;
import java.io.InputStream;
import java.net.URL;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import static net.ssignal.User.当前用户;

public class Audio_Playing implements MediaPlayer.OnCompletionListener {

    private Context 运行环境;
    private Fragment_Chating 聊天控件;
    private MediaPlayer 媒体播放器;
    String 原始路径;
    private Thread 线程;
    HttpsURLConnection HTTPS连接 = null;
    private MyHandler 跨线程调用器;
    private boolean 播放完关闭 = false;

    public Audio_Playing(Context 运行环境) {
        this.运行环境 = 运行环境;
    }

    public Audio_Playing(Context 运行环境, Fragment_Chating 聊天控件) {
        this.运行环境 = 运行环境;
        this.聊天控件 = 聊天控件;
    }

    boolean 正在播放() {
        if (媒体播放器 != null) {
            if (媒体播放器.isPlaying()) {
                return true;
            }
        }
        if (线程 == null) {
            return false;
        } else {
            return true;
        }
    }

    boolean 开始播放AMR(String 路径, boolean 是原始路径) {
        String 文件路径 = 路径;
        int i = 文件路径.lastIndexOf(".");
        if (i < 0) { return false; }
        if (!文件路径.substring(i + 1).equalsIgnoreCase("amr")) { return false; }
        if (文件路径.startsWith("https://") || 文件路径.startsWith("http://")) {
            i = 文件路径.lastIndexOf("/");
            if (i < 0) { return false; }
            String 段[] = 文件路径.split("&");
            if (段.length <= 0) { return false; }
            final String 参数名 = "FileName=";
            for (i = 段.length - 1; i >= 0; i--) {
                if (段[i].startsWith(参数名)) {
                    break;
                }
            }
            if (i < 0) { return false; }
            String 目录路径 = 运行环境.getFilesDir().toString() + "/" + 当前用户.获取英语讯宝地址();
            File 目录 = new File(目录路径);
            if (!目录.exists() || !目录.isDirectory()) {
                目录.mkdir();
            }
            String 文件路径3 = 目录路径 + "/" + 段[i].substring(参数名.length());
            File 文件 = new File(文件路径3);
            if (!文件.exists() || !文件.isFile()) {
                if (跨线程调用器 == null) {
                    跨线程调用器 = new MyHandler(this);
                }
                线程 = new Thread_DownloadAudio(文件路径, 文件路径3, this);
                线程.start();
                return true;
            } else {
                文件路径 = 文件路径3;
            }
        }
        if (媒体播放器 != null) {
            媒体播放器.release();
        }
        媒体播放器 = new MediaPlayer();
        媒体播放器.setOnCompletionListener(this);
        try {
            媒体播放器.setDataSource(文件路径);
            媒体播放器.prepare();
        } catch (Exception e) {
            return false;
        }
        if (是原始路径) {
            原始路径 = 路径;
        }
        媒体播放器.start();
        return true;
    }

    public void 下载(String 下载路径, String 保存路径) {
        int 重试次数 = 0;
        do {
            try {
                URL url = new URL(下载路径);
                HTTPS连接 = (HttpsURLConnection) url.openConnection();
                if (BuildConfig.DEBUG && !ProtocolPath.调试时访问真实网站) {
                    TrustManager tm[] = {new MyX509TrustManager()};
                    SSLContext SSLC = SSLContext.getInstance("SSL");
                    SSLC.init(null, tm, new java.security.SecureRandom());
                    SSLSocketFactory ssf = SSLC.getSocketFactory();
                    HTTPS连接.setSSLSocketFactory(ssf);
                    HTTPS连接.setHostnameVerifier(new HostnameVerifier() {
                        @Override
                        public boolean verify(String s, SSLSession sslSession) {
                            return true;
                        }
                    });
                }
                HTTPS连接.setConnectTimeout(10000);
                HTTPS连接.setReadTimeout(10000);
                HTTPS连接.setDoOutput(true);
                HTTPS连接.setDoInput((true));
                HTTPS连接.setRequestMethod("GET");
                HTTPS连接.setRequestProperty("Content-Type", "text/xml");
                HTTPS连接.setRequestProperty("Content-Length", String.valueOf(0));
                HTTPS连接.connect();
                int 响应代码 = HTTPS连接.getResponseCode();
                if (响应代码 == 200) { //HTTP_OK = 200
                    byte 收到的字节数组[] = null;
                    int 收到的总字节数 = 0;
                    if (HTTPS连接.getContentLength() > 0) {
                        收到的字节数组 = new byte[HTTPS连接.getContentLength()];
                        int 收到的字节数 = 0;
                        InputStream 输入流 = HTTPS连接.getInputStream();
                        byte 字节数组[] = new byte[8192];
                        do {
                            收到的字节数 = 输入流.read(字节数组, 0, 字节数组.length);
                            if (收到的字节数 > 0) {
                                System.arraycopy(字节数组, 0, 收到的字节数组, 收到的总字节数, 收到的字节数);
                                收到的总字节数 += 收到的字节数;
                            }
                        } while (收到的字节数 > 0);
                        输入流.close();
                    }
                    if (收到的字节数组 != null) {
                        if (收到的总字节数 == 收到的字节数组.length) {
                            Bundle 数据盒子 = new Bundle();
                            数据盒子.putByteArray("AMR", 收到的字节数组);
                            数据盒子.putString("保存路径", 保存路径);
                            Message 消息 = new Message();
                            消息.what = 1;
                            消息.setData(数据盒子);
                            跨线程调用器.sendMessage(消息);
                        }
                    }
                    线程 = null;
                    return;
                }
            } catch (Exception e) {
            } finally {
                if (HTTPS连接 != null) {
                    try {
                        HTTPS连接.disconnect();
                    } catch (Exception e2) {
                    }
                    HTTPS连接 = null;
                }
            }
            重试次数 += 1;
        } while (重试次数 <= 2);
        线程 = null;
    }

    void 开始播放assetMP3(String 文件名) {
        播放完关闭 = true;
        if (媒体播放器 != null) {
            媒体播放器.release();
        }
        媒体播放器 = new MediaPlayer();
        媒体播放器.setOnCompletionListener(this);
        try {
            AssetFileDescriptor 附带文件描述器 = 运行环境.getAssets().openFd(文件名);
            媒体播放器.reset();
            媒体播放器.setDataSource(附带文件描述器.getFileDescriptor(), 附带文件描述器.getStartOffset(), 附带文件描述器.getLength());
            媒体播放器.prepare();
        } catch (Exception e) {
            return;
        }
        媒体播放器.start();
    }

    void 处理跨线程数据(Message msg) {
        switch (msg.what) {
            case 1:
                byte AMR[] = msg.getData().getByteArray("AMR");
                String 保存路径 = msg.getData().getString("保存路径");
                try {
                    SharedMethod.保存文件的全部字节(保存路径, AMR);
                } catch (Exception e) {
                    return;
                }
                开始播放AMR(保存路径, false);
                break;
        }
    }

    void 停止播放() {
        if (HTTPS连接 != null) {
            try {
                HTTPS连接.disconnect();
            } catch (Exception e2) {
            }
            HTTPS连接 = null;
        }
        if (媒体播放器 != null) {
            if (媒体播放器.isPlaying()) {
                媒体播放器.stop();
            }
        }
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        if (!播放完关闭) {
            聊天控件.播音完毕();
        } else {
            关闭();
        }
    }

    void 关闭() {
        if (HTTPS连接 != null) {
            try {
                HTTPS连接.disconnect();
            } catch (Exception e2) {
            }
            HTTPS连接 = null;
        }
        if (媒体播放器 != null) {
            媒体播放器.release();
            媒体播放器 = null;
        }
    }


    private class Thread_DownloadAudio extends Thread {

        private Audio_Playing 创建者;
        String 下载路径, 保存路径;

        public Thread_DownloadAudio(String 下载路径, String 保存路径, Audio_Playing 创建者) {
            this.下载路径 = 下载路径;
            this.保存路径 = 保存路径;
            this.创建者 = 创建者;
        }

        @Override
        public void run() {
            创建者.下载(下载路径, 保存路径);
            创建者 = null;
        }

    }

}
