package net.ssignal;

import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.PointF;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

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

public class Fragment_ViewPicture extends Fragment implements View.OnTouchListener {

    private View 第一控件;
    private MyHandler 跨线程调用器;
    private int 下载进度 = -1;
    private TextView 文字控件;
    private ImageView 图片控件;
    String 文件路径;
    private boolean 取消下载 = false;

    private static final byte 操作_无 = 0;
    private static final byte 操作_拖动 = 1;
    private static final byte 操作_缩放 = 2;
    private byte 当前操作 = 操作_无;

    private Matrix 当前绘图参数 = new Matrix();
    private Matrix 初始绘图参数 = new Matrix();

    private PointF 触摸起始点 = new PointF();
    private PointF 触摸点的中间点 = new PointF();

    private float 触摸点初始距离 = 1f;

    @Override
    public View onCreateView(LayoutInflater 布局扩充器, ViewGroup 控件容器, Bundle 已保存的实例状态) {
        第一控件 = 布局扩充器.inflate(R.layout.viewpicture, 控件容器, false);
        跨线程调用器 = new MyHandler(this);
        文字控件 = 第一控件.findViewById(R.id.下载进度);
        图片控件 = 第一控件.findViewById(R.id.图片);
        return 第一控件;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (!SharedMethod.字符串未赋值或为空(文件路径)) {
            if (文件路径.startsWith("https://") || 文件路径.startsWith("http://")) {
                int i = 文件路径.lastIndexOf("/");
                if (i < 0) {
                    return;
                }
                String 段[] = 文件路径.substring(i + 1).split("&");
                if (段.length <= 0) {
                    return;
                }
                final String 参数名 = "FileName=";
                for (i = 段.length - 1; i >= 0; i--) {
                    if (段[i].startsWith(参数名)) {
                        break;
                    }
                }
                if (i < 0) {
                    return;
                }
                String 保存路径 = getActivity().getFilesDir().toString() + "/" + 当前用户.获取英语讯宝地址() + "/" + 段[i].substring(参数名.length());
                File 文件 = new File(保存路径);
                if (!文件.exists()) {
                    下载进度 = 0;
                    文字控件.setVisibility(View.VISIBLE);
                    Thread 线程 = new Thread_DownloadFile(文件路径, 保存路径, this);
                    线程.start();
                } else {
                    文字控件.setVisibility(View.GONE);
                    图片控件.setImageURI(Uri.fromFile(文件));
                    让图片居中显示();
                    图片控件.setOnTouchListener(this);
                }
            } else {
                File 文件 = new File(文件路径);
                if (文件.exists()) {
                    文字控件.setVisibility(View.GONE);
                    图片控件.setImageURI(Uri.fromFile(文件));
                    让图片居中显示();
                    图片控件.setOnTouchListener(this);
                }
            }
        }
    }

    public void 下载(String 下载路径, String 保存路径) {
        HttpsURLConnection HTTPS连接 = null;
        int 重试次数 = 0;
        int 进度;
        do {
            下载进度 = 0;
            更新下载进度(下载进度);
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
                        int 总字节数 = HTTPS连接.getContentLength();
                        收到的字节数组 = new byte[总字节数];
                        int 收到的字节数 = 0;
                        InputStream 输入流 = HTTPS连接.getInputStream();
                        byte 字节数组[] = new byte[8192];
                        do {
                            收到的字节数 = 输入流.read(字节数组, 0, 字节数组.length);
                            if (收到的字节数 > 0) {
                                System.arraycopy(字节数组, 0, 收到的字节数组, 收到的总字节数, 收到的字节数);
                                收到的总字节数 += 收到的字节数;
                                进度 = (int)(((double) 收到的总字节数 / (double) 总字节数) * 100);
                                if (进度 - 下载进度 > 5) {
                                    下载进度 = 进度;
                                    更新下载进度(进度);
                                }
                            }
                        } while (收到的字节数 > 0 && !取消下载);
                        输入流.close();
                    }
                    if (收到的字节数组 != null) {
                        if (收到的总字节数 == 收到的字节数组.length) {
                            SharedMethod.保存文件的全部字节(保存路径, 收到的字节数组);
                            Bundle 数据盒子 = new Bundle();
                            数据盒子.putString("保存路径", 保存路径);
                            Message 消息 = new Message();
                            消息.what = 1;
                            消息.setData(数据盒子);
                            跨线程调用器.sendMessage(消息);
                        }
                    }
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
    }

    private void 更新下载进度(int 进度) {
        Bundle 数据盒子 = new Bundle();
        数据盒子.putInt("进度", 进度);
        Message 消息 = new Message();
        消息.what = 2;
        消息.setData(数据盒子);
        跨线程调用器.sendMessage(消息);
    }

    void 处理跨线程数据(Message msg) {
        switch (msg.what) {
            case 2:
                int 进度 = msg.getData().getInt("进度");
                文字控件.setText(进度 + " %");
                break;
            case 1:
                文字控件.setVisibility(View.GONE);
                String 保存路径 = msg.getData().getString("保存路径");
                File 文件 = new File(保存路径);
                if (文件.exists()) {
                    图片控件.setImageURI(Uri.fromFile(文件));
                    让图片居中显示();
                    图片控件.setOnTouchListener(this);
                }
                break;
        }
    }

    void 取消下载() {
        取消下载 = true;
    }

    private void 让图片居中显示() {
        int 图片宽度 = 图片控件.getDrawable().getIntrinsicWidth();
        int 图片高度 = 图片控件.getDrawable().getIntrinsicHeight();
        Point 宽高 = ((Activity_Main) getActivity()).获取宽高();
        int 图片控件宽度 = 宽高.x;
        int 图片控件高度 = 宽高.y;
        当前绘图参数.postTranslate((图片控件宽度 - 图片宽度) / 2, (图片控件高度 - 图片高度) / 2);
        if (图片宽度 > 图片控件宽度) {
            if (图片高度 > 图片控件高度) {
                if ((double) 图片宽度 / (double) 图片高度 > (double) 图片控件宽度 / (double) 图片控件高度) {
                    float 缩放比例 = (float)((double)图片控件宽度 / (double)图片宽度);
                    当前绘图参数.postScale(缩放比例, 缩放比例, 图片控件宽度 / 2, 图片控件高度 / 2);
                } else {
                    float 缩放比例 = (float)((double)图片控件高度 / (double)图片高度);
                    当前绘图参数.postScale(缩放比例, 缩放比例, 图片控件宽度 / 2, 图片控件高度 / 2);
                }
            } else {
                float 缩放比例 = (float)((double)图片控件宽度 / (double)图片宽度);
                当前绘图参数.postScale(缩放比例, 缩放比例, 图片控件宽度 / 2, 图片控件高度 / 2);
            }
        } else if (图片高度 > 图片控件高度) {
            float 缩放比例 = (float)((double)图片控件高度 / (double)图片高度);
            当前绘图参数.postScale(缩放比例, 缩放比例, 图片控件宽度 / 2, 图片控件高度 / 2);
        }
        图片控件.setImageMatrix(当前绘图参数);
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                当前绘图参数.set(图片控件.getImageMatrix());
                初始绘图参数.set(当前绘图参数);
                触摸起始点.set(motionEvent.getX(), motionEvent.getY());
                当前操作 = 操作_拖动;
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                触摸点初始距离 = 计算两个触摸点的间距(motionEvent);
                if (触摸点初始距离 > 10f) {
                    初始绘图参数.set(当前绘图参数);
                    计算两个触摸点连线的中间点(motionEvent);
                    当前操作 = 操作_缩放;
                }
                break;
            case MotionEvent.ACTION_UP:
                if (当前操作 == 操作_拖动) {
                    if (motionEvent.getX() == 触摸起始点.x && motionEvent.getY() == 触摸起始点.y) {
                        ((Activity_Main) getActivity()).关闭查看图片的窗体();
                    }
                }
            case MotionEvent.ACTION_POINTER_UP:
                当前操作 = 操作_无;
                break;
            case MotionEvent.ACTION_MOVE:
                switch (当前操作) {
                    case 操作_拖动:
                        当前绘图参数.set(初始绘图参数);
                        当前绘图参数.postTranslate(motionEvent.getX() - 触摸起始点.x, motionEvent.getY() - 触摸起始点.y);
                        break;
                    case 操作_缩放:
                        float 触摸点的当前距离 = 计算两个触摸点的间距(motionEvent);
                        if (触摸点的当前距离 > 10f) {
                            当前绘图参数.set(初始绘图参数);
                            float 缩放比例 = 触摸点的当前距离 / 触摸点初始距离;
                            当前绘图参数.postScale(缩放比例, 缩放比例, 触摸点的中间点.x, 触摸点的中间点.y);
                        }
                        break;
                }
                break;
        }
        图片控件.setImageMatrix(当前绘图参数);
        return true;
    }

    private float 计算两个触摸点的间距(MotionEvent motionEvent) {
        float x = motionEvent.getX(0) - motionEvent.getX(1);
        float y = motionEvent.getY(0) - motionEvent.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    private void 计算两个触摸点连线的中间点(MotionEvent motionEvent) {
        float x = motionEvent.getX(0) + motionEvent.getX(1);
        float y = motionEvent.getY(0) + motionEvent.getY(1);
        触摸点的中间点.set(x / 2, y /2);
    }

    private class Thread_DownloadFile extends Thread {

        private Fragment_ViewPicture 创建者;
        String 下载路径, 保存路径;

        public Thread_DownloadFile(String 下载路径, String 保存路径, Fragment_ViewPicture 创建者) {
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
