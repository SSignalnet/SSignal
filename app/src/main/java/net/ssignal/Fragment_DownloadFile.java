package net.ssignal;

import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import net.ssignal.network.MyX509TrustManager;
import net.ssignal.protocols.ProtocolPath;
import net.ssignal.util.SharedMethod;

import java.io.InputStream;
import java.net.URL;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import static net.ssignal.language.Text.界面文字;

public class Fragment_DownloadFile extends Fragment {

    private class 下载任务_复合数据 {
        String 下载路径, 保存路径;
        int 任务编号;
    }

    private View 第一控件;
    private MyHandler 跨线程调用器;
    private WebView 浏览器;
    private boolean 网页载入完毕 = false;
    int 第几个任务;
    private int 下载进度;
    private 下载任务_复合数据 下载任务[];
    private Thread 线程;
    private boolean 取消下载 = false;
    private String 下载路径2, 保存路径2;

    @Override
    public View onCreateView(LayoutInflater 布局扩充器, ViewGroup 控件容器, Bundle 已保存的实例状态) {
        第一控件 = 布局扩充器.inflate(R.layout.download, 控件容器, false);
        跨线程调用器 = new MyHandler(this);
        return 第一控件;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        浏览器 = (WebView) 第一控件.findViewById(R.id.浏览器);
        WebSettings 浏览器设置 = 浏览器.getSettings();
        浏览器设置.setJavaScriptEnabled(true);
        浏览器设置.setSupportMultipleWindows(false);
        if (Build.VERSION.SDK_INT >= 21) {
            浏览器设置.setMixedContentMode(0);
            浏览器.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        } else if (Build.VERSION.SDK_INT >= 19) {
            浏览器.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        } else {
            浏览器.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
        浏览器.setWebChromeClient(new MyWebChromeClient(this));
        浏览器.setWebViewClient(new MyWebViewClient(this));
        浏览器.setDownloadListener(new MyDownloadListener(getActivity(), this));
        浏览器.addJavascriptInterface(this, "external");
        浏览器.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                return true;
            }
        });
        浏览器.loadUrl("file:///android_asset/Download.html");
        TextView 文字 = 第一控件.findViewById(R.id.文字_保存路径);
        文字.setText(getActivity().getExternalFilesDir(null).toString() + "/download/");
    }

    void onProgressChanged(WebView view, int newProgress) {
        if (newProgress == 100) {
            if (网页载入完毕) { return; }
            网页载入完毕 = true;
            发送JS("function(){ MenuText('" + 界面文字.获取(311, "取消") + "');}");
            下载任务_复合数据 新下载任务 = 下载任务[0];
            发送JS("function(){ NewTask('" + 新下载任务.任务编号 + "', '" + SharedMethod.获取文件名(新下载任务.保存路径) + "', '" + SharedMethod.替换HTML和JS敏感字符(新下载任务.下载路径) + "', '" + 界面文字.获取(309, "等待中") + "');}");
            线程 = new Thread_DownloadFile(新下载任务.下载路径, 新下载任务.保存路径, this);
            线程.start();
        }
    }

    void 新下载任务(String 下载路径, String 保存路径, boolean 不开始) {
        if (下载任务 == null) {
            第几个任务 += 1;
            下载任务 = new 下载任务_复合数据[1];
            下载任务_复合数据 新下载任务 = new 下载任务_复合数据();
            新下载任务.下载路径 = 下载路径;
            新下载任务.保存路径 = 保存路径;
            新下载任务.任务编号 = 第几个任务;
            下载任务[0] = 新下载任务;
            if (!不开始) {
                发送JS("function(){ NewTask('" + 新下载任务.任务编号 + "', '" + SharedMethod.获取文件名(新下载任务.保存路径) + "', '" + SharedMethod.替换HTML和JS敏感字符(新下载任务.下载路径) + "', '" + 界面文字.获取(309, "等待中") + "');}");
                线程 = new Thread_DownloadFile(新下载任务.下载路径, 新下载任务.保存路径, this);
                线程.start();
            }
        } else {
            for (int i = 0; i < 下载任务.length; i++) {
                if (下载路径.equals(下载任务[i].下载路径)) {
                    return;
                }
            }
            第几个任务 += 1;
            下载任务_复合数据 下载任务2[] = new 下载任务_复合数据[下载任务.length + 1];
            System.arraycopy(下载任务, 0, 下载任务2, 0, 下载任务.length);
            下载任务_复合数据 新下载任务 = new 下载任务_复合数据();
            新下载任务.下载路径 = 下载路径;
            新下载任务.保存路径 = 保存路径;
            新下载任务.任务编号 = 第几个任务;
            下载任务2[下载任务2.length - 1] = 新下载任务;
            下载任务 = 下载任务2;
            发送JS("function(){ NewTask('" + 新下载任务.任务编号 + "', '" + SharedMethod.获取文件名(新下载任务.保存路径) + "', '" + SharedMethod.替换HTML和JS敏感字符(新下载任务.下载路径) + "', '" + 界面文字.获取(309, "等待中") + "');}");
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
                            数据盒子.putBoolean("成功", true);
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
        Bundle 数据盒子 = new Bundle();
        数据盒子.putBoolean("成功", false);
        Message 消息 = new Message();
        消息.what = 1;
        消息.setData(数据盒子);
        跨线程调用器.sendMessage(消息);
    }

    private void 更新下载进度(int 进度) {
        Bundle 数据盒子 = new Bundle();
        数据盒子.putInt("进度", 进度);
        Message 消息 = new Message();
        消息.what = 2;
        消息.setData(数据盒子);
        跨线程调用器.sendMessage(消息);
    }

    @JavascriptInterface
    public void Cancel(String id) {
        Bundle 数据盒子 = new Bundle();
        数据盒子.putInt("任务编号", Integer.parseInt(id));
        Message 消息 = new Message();
        消息.what = 3;
        消息.setData(数据盒子);
        跨线程调用器.sendMessage(消息);
    }

    @JavascriptInterface
    public void LocateFile(String FileName) {
        Bundle 数据盒子 = new Bundle();
        数据盒子.putString("文件名", FileName);
        Message 消息 = new Message();
        消息.what = 4;
        消息.setData(数据盒子);
        跨线程调用器.sendMessage(消息);
    }

    void 处理跨线程数据(Message msg) {
        switch (msg.what) {
            case 2:
                int 进度 = msg.getData().getInt("进度");
                发送JS("function(){ ProgressChanged('" + 下载任务[0].任务编号 + "', '" + 进度 + "');}");
                break;
            case 1:
                boolean 成功 = msg.getData().getBoolean("成功");
                if (成功) {
                    发送JS("function(){ Succeeded('" + 下载任务[0].任务编号 + "', '" + 界面文字.获取(307, "完毕") + "');}");
                } else {
                    发送JS("function(){ Failed('" + 下载任务[0].任务编号 + "', '" + 界面文字.获取(308, "失败") + "');}");
                }
                if (下载任务.length == 1) {
                    下载任务 = null;
                } else {
                    下载任务_复合数据 下载任务2[] = new 下载任务_复合数据[下载任务.length - 1];
                    int i, j = 0;
                    for (i = 1; i < 下载任务.length - 1; i++) {
                        下载任务2[j] = 下载任务[i];
                        j++;
                    }
                    下载任务 = 下载任务2;
                    线程 = new Thread_DownloadFile(下载任务[0].下载路径, 下载任务[0].保存路径, this);
                    线程.start();
                }
                break;
            case 3:
                int 任务编号 = msg.getData().getInt("任务编号");
                取消下载(任务编号);
                break;
            case 4:
//                String 文件名 = msg.getData().getString("文件名");
//                String 保存路径 = getActivity().getExternalFilesDir(null).toString() + "/download/" + 文件名;
//                SharedMethod.打开资源管理器并选中文件(getActivity().getApplicationContext(), 保存路径);
                break;
        }
    }

    private void 取消下载(int 任务编号) {
        if (下载任务 == null) {
            return;
        }
        int i;
        for (i = 0; i < 下载任务.length; i++) {
            if (下载任务[i].任务编号 == 任务编号) {
                break;
            }
        }
        if (i < 下载任务.length) {
            if (i == 0) {
                if (线程 != null) {
                    取消下载 = true;
                    int j = 0;
                    try {
                        do {
                            Thread.sleep(100);
                            j += 100;
                        } while (线程 != null && j < 5000);
                    } catch (Exception e) {
                    }
                    取消下载 = false;
                }
            }
            if (下载任务.length == 1) {
                下载任务 = null;
            } else {
                下载任务_复合数据 下载任务2[] = new 下载任务_复合数据[下载任务.length - 1];
                int j, k = 0;
                for (j = 0; j < 下载任务.length; j++) {
                    if (j != i) {
                        下载任务2[k] = 下载任务[j];
                        k += 1;
                    }
                }
                下载任务 = 下载任务2;
                线程 = new Thread_DownloadFile(下载任务[0].下载路径, 下载任务[0].保存路径, this);
                线程.start();
            }
        }
    }

    void 发送JS(String JS) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            浏览器.evaluateJavascript("(" + JS + ")()", new ValueCallback<String>() {
                @Override
                public void onReceiveValue(String value) {

                }
            });
        } else {
            浏览器.loadUrl("javascript:" + JS);
        }
    }


    private class Thread_DownloadFile extends Thread {

        private Fragment_DownloadFile 创建者;
        String 下载路径, 保存路径;

        public Thread_DownloadFile(String 下载路径, String 保存路径, Fragment_DownloadFile 创建者) {
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
