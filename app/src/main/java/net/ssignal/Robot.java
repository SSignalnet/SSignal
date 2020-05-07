package net.ssignal;

import android.os.Bundle;
import android.os.Message;
import android.widget.EditText;

import net.ssignal.network.MyX509TrustManager;
import net.ssignal.network.httpSetting;
import net.ssignal.protocols.ProtocolPath;
import net.ssignal.util.Constants;
import net.ssignal.util.MyDate;
import net.ssignal.util.SharedMethod;

import java.io.DataOutputStream;
import java.io.InputStream;
import java.net.URL;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

public class Robot {

    Fragment_Chating 聊天控件;
    EditText 输入框;
    Task 任务;
    protected MyHandler 跨线程调用器;
    boolean 正在输入密码 = false;

    void 说(String 文本) {
        说(文本, 0, null);
    }

    void 说(String 文本, String 颜色) {
        说(文本, 0, 颜色);
    }

    void 说(String 文本, long 时刻, String 颜色) {
        if (SharedMethod.字符串未赋值或为空((文本)))  {return;}
        if (聊天控件 == null) { return; }
        if (文本.contains("<a>")) {
            String 段[] = 文本.split("<a>");
            StringBuilder 字符串合并器 = new StringBuilder(文本.length() * 2);
            int i;
            for (i = 0; i < 段.length; i++) {
                if (段[i].contains("</a>") && !段[i].contains("<a ")) {
                    String 节[] = 段[i].split("</a>");
                    if (节.length == 1) {
                        字符串合并器.append("<span class='TaskName' onclick='ToRobot(\\\"" + 节[0] + "\\\")'>" + 节[0] + "</span>");
                    } else if (节.length == 2) {
                        字符串合并器.append("<span class='TaskName' onclick='ToRobot(\\\"" + 节[0] + "\\\")'>" + 节[0] + "</span>");
                        字符串合并器.append(节[1]);
                    } else {
                        字符串合并器.append(段[i]);
                    }
                } else {
                    字符串合并器.append(段[i]);
                }
            }
            文本 = 字符串合并器.toString();
        }
        String 谁;
        if (聊天控件.聊天对象.小聊天群 != null) {
            switch (聊天控件.聊天对象.讯友或群主.英语讯宝地址) {
                case Constants.机器人id_主控:
                case Constants.机器人id_系统管理:
                    谁 = 聊天控件.聊天对象.讯友或群主.英语讯宝地址;
                    break;
                default:
                    谁 = Constants.机器人id_主控;
            }
        } else {
            谁 = Constants.机器人id_主控;
        }
        if (SharedMethod.字符串未赋值或为空(颜色)) {
            聊天控件.发送JS("function(){ var who = \"" + 谁 + "\"; var text = \"" + 文本 + "\"; var time = \"" + 聊天控件.时间格式(new MyDate((时刻 == 0 ? SharedMethod.获取当前UTC时间() : 时刻))) + "\"; SSin_Text(who, \"0\", text, who + \".jpg\", time); }");
        } else {
            聊天控件.发送JS("function(){ var who = \"" + 谁 + "\"; var text = \"<span style='color:" + 颜色 + ";'>" + 文本 + "</span>\"; var time = \"" + 聊天控件.时间格式(new MyDate((时刻 == 0 ? SharedMethod.获取当前UTC时间() : 时刻))) + "\"; SSin_Text(who, \"0\", text, who + \".jpg\", time); }");
        }
    }

    void 回答(String 用户输入, long 时间) {

    }

    void 启动HTTPS访问线程(httpSetting 访问设置) {
        if (聊天控件 != null) { 聊天控件.按钮和机器人图标(true); }
        Thread 线程 = new Thread_httpsRequest(this, 访问设置);
        线程.start();
    }

    public void HTTPS访问(httpSetting 访问设置) {
        HttpsURLConnection HTTPS连接 = null;
        int 重试次数 = 0;
        String 提示信息 = "";
        byte 收到的字节数组[];
        int 收到的字节数 = 0, 收到的总字节数;
        do {
            收到的总字节数 = 0;
            收到的字节数组 = null;
            if (重试次数 > 0) {
                HTTPS请求失败(提示信息, false);
            }
            try {
                URL url = new URL(访问设置.获取路径());
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
                HTTPS连接.setConnectTimeout(访问设置.获取收发时限());
                HTTPS连接.setReadTimeout(访问设置.获取收发时限());
                HTTPS连接.setDoOutput(true);
                HTTPS连接.setDoInput((true));
                HTTPS连接.setUseCaches(false);
                HTTPS连接.setRequestMethod("POST");
                byte 附加数据[] = 访问设置.获取附加数据();
                if (附加数据 == null) {
                    HTTPS连接.setRequestProperty("Content-Type", "text/xml");
                    HTTPS连接.setRequestProperty("Content-Length", String.valueOf(0));
                } else {
                    HTTPS连接.setRequestProperty("Content-Type", "application/octet-stream");
                    HTTPS连接.setRequestProperty("Content-Length", String.valueOf(附加数据.length));
                }
                HTTPS连接.connect();
                if (附加数据 != null) {
                    DataOutputStream 数据输出流 = new DataOutputStream(HTTPS连接.getOutputStream());
                    数据输出流.write(附加数据);
                    数据输出流.flush();
                    数据输出流.close();
                }
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
                        if (收到的总字节数 == 收到的字节数组.length) {
                            HTTPS请求成功(收到的字节数组);
                            return;
                        }
                    }
                    HTTPS请求成功(null);
                    return;
                } else {
                    提示信息 = "HTTP error code: " + 响应代码;
                }
            } catch (Exception e) {
                String Message = e.getMessage();
                if (Message == null) {
                    提示信息 = e.toString();
                } else {
                    提示信息 = "HTTP error: " + Message;
                }
            } finally {
                if (HTTPS连接 != null) {HTTPS连接.disconnect();}
            }
            重试次数 += 1;
        } while (重试次数 <= 2);
        HTTPS请求失败(提示信息, true);
    }

    private void HTTPS请求成功(byte SS包[]) {
        Bundle 数据盒子 = new Bundle();
        数据盒子.putByteArray("SS包", SS包);
        Message 消息 = new Message();
        消息.what = 1;
        消息.setData(数据盒子);
        跨线程调用器.sendMessage(消息);
    }

    void 提示新任务() {
        if (聊天控件 != null) { 聊天控件.发送JS("function(){ NewTask(); }"); }
    }

    private void HTTPS请求失败(String 原因, boolean 结束) {
        Bundle 数据盒子 = new Bundle();
        数据盒子.putString("原因", 原因);
        数据盒子.putBoolean("结束", 结束);
        Message 消息 = new Message();
        消息.what = 2;
        消息.setData(数据盒子);
        跨线程调用器.sendMessage(消息);
    }

    void 发送短视频2(String 视频文件路径, short 预览图片宽度, short 预览图片高度) {

    }

    void 发送照片2(String 照片文件路径) {

    }

    public class Thread_httpsRequest extends Thread {

        private Robot 机器人;
        private httpSetting 访问设置;

        public Thread_httpsRequest(Robot 机器人, httpSetting 访问设置) {
            this.机器人 = 机器人;
            this.访问设置 = 访问设置;
        }

        public Thread_httpsRequest(Robot 机器人) {
            this.机器人 = 机器人;
        }

        @Override
        public void run() {
            if (访问设置 == null) {
                if (机器人 instanceof Robot_MainControl) {
                    ((Robot_MainControl)机器人).用http访问传送服务器();
                }
            } else {
                机器人.HTTPS访问(访问设置);
            }
        }

    }

}
