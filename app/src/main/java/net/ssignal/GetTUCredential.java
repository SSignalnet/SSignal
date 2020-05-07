package net.ssignal;

import android.os.Bundle;
import android.os.Message;

import net.ssignal.network.MyX509TrustManager;
import net.ssignal.protocols.ProtocolParameters;
import net.ssignal.protocols.ProtocolPath;
import net.ssignal.protocols.SSPackageCreator;

import java.io.DataOutputStream;
import java.io.InputStream;
import java.net.URL;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import static net.ssignal.User.当前用户;

public class GetTUCredential {

    long 创建时刻;
    Fragment_Chating 聊天控件[];
    String 英语子域名;
    boolean 是写入凭据;
    private byte[] 字节数组;
    private MyHandler 跨线程调用器;

    public GetTUCredential(Fragment_Chating 聊天控件1, String 英语子域名1, boolean 是写入凭据1, MyHandler 跨线程调用器1) {
        聊天控件 = new Fragment_Chating[1];
        聊天控件[0] = 聊天控件1;
        英语子域名 = 英语子域名1;
        是写入凭据 = 是写入凭据1;
        跨线程调用器 = 跨线程调用器1;
        try {
            SSPackageCreator SS包生成器 = new SSPackageCreator();
            SS包生成器.添加_有标签("发送序号", 当前用户.讯宝发送序号);
            SS包生成器.添加_有标签("子域名", 英语子域名1);
            字节数组 = SS包生成器.生成SS包(当前用户.AES加密器);
        } catch (Exception e) {
        }
        创建时刻 = System.currentTimeMillis();
    }

    void 获取() {
        Thread 线程 = new Thread_GetTUCredential(this);
        线程.start();
    }

    public void HTTPS访问() {
        HttpsURLConnection HTTPS连接 = null;
        int 重试次数 = 0;
        byte 收到的字节数组[];
        int 收到的字节数 = 0, 收到的总字节数;
        do {
            收到的总字节数 = 0;
            收到的字节数组 = null;
            try {
                URL url = new URL(ProtocolPath.获取传送服务器访问路径开头(当前用户.主机名, 当前用户.域名_英语, false) + "C=EnterTinyUniverse&UserID=" + 当前用户.编号 + "&Position=" + 当前用户.位置号 + "&DeviceType=" + ProtocolParameters.设备类型_文本_手机);
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
                HTTPS连接.setConnectTimeout(20000);
                HTTPS连接.setReadTimeout(20000);
                HTTPS连接.setDoInput((true));
                HTTPS连接.setUseCaches(false);
                HTTPS连接.setRequestMethod("POST");
                HTTPS连接.setRequestProperty("Content-Type", "application/octet-stream");
                HTTPS连接.setRequestProperty("Content-Length", String.valueOf(字节数组.length));
                HTTPS连接.connect();
                DataOutputStream 数据输出流 = new DataOutputStream(HTTPS连接.getOutputStream());
                数据输出流.write(字节数组);
                数据输出流.flush();
                数据输出流.close();
                int 响应代码 = HTTPS连接.getResponseCode();
                if (响应代码 == 200) { //HTTP_OK = 200
                    if (HTTPS连接.getContentLength() > 0) {
                        收到的字节数组 = new byte[HTTPS连接.getContentLength()];
                        InputStream 输入流 = HTTPS连接.getInputStream();
                        do {
                            收到的字节数 = 输入流.read(收到的字节数组, 收到的总字节数, 收到的字节数组.length - 收到的总字节数);
                            if (收到的字节数 > 0) {
                                收到的总字节数 += 收到的字节数;
                                if (收到的总字节数 >= 收到的字节数组.length) {
                                    break;
                                }
                            }
                        } while (收到的字节数 > 0);
                        输入流.close();
                    }
                    if (收到的字节数组 != null) {
                        if (收到的总字节数 < 收到的字节数组.length) {
                            收到的字节数组 = null;
                        }
                    }
                }
            } catch (Exception e) {
            } finally {
                if (HTTPS连接 != null) {HTTPS连接.disconnect();}
            }
            重试次数 += 1;
        } while (重试次数 <= 2);
        Bundle 数据盒子 = new Bundle();
        数据盒子.putLong("创建时刻", 创建时刻);
        if (收到的字节数组 != null) {
            数据盒子.putByteArray("字节数组", 收到的字节数组);
        }
        Message 消息 = new Message();
        消息.what = 8;
        消息.setData(数据盒子);
        if (跨线程调用器 != null) {
            跨线程调用器.sendMessage(消息);
        }

    }

    void 添加聊天控件(Fragment_Chating 聊天控件1) {
        for (int i = 0; i < 聊天控件.length; i++) {
            if (聊天控件[i].equals(聊天控件1)) {
                return;
            }
        }
        Fragment_Chating 聊天控件2[] = new Fragment_Chating[聊天控件.length + 1];
        System.arraycopy(聊天控件, 0, 聊天控件2, 0, 聊天控件.length);
        聊天控件2[聊天控件2.length - 1] = 聊天控件1;
        聊天控件 = 聊天控件2;
    }

    boolean 查找聊天控件(Fragment_Chating 聊天控件1) {
        for (int i = 0; i < 聊天控件.length; i++) {
            if (聊天控件[i].equals(聊天控件1)) {
                return true;
            }
        }
        return false;
    }


    private class Thread_GetTUCredential extends Thread {

        private GetTUCredential 创建者;

        public Thread_GetTUCredential(GetTUCredential 创建者) {
            this.创建者 = 创建者;
        }

        @Override
        public void run() {
            创建者.HTTPS访问();
        }

    }

}
