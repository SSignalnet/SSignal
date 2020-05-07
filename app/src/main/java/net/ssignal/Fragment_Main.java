package net.ssignal;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import net.ssignal.protocols.Contact;
import net.ssignal.protocols.Group_Large;
import net.ssignal.protocols.Group_Small;
import net.ssignal.protocols.ProtocolParameters;
import net.ssignal.protocols.ProtocolPath;
import net.ssignal.protocols.SSPackageReader;
import net.ssignal.structure.ChatWith;
import net.ssignal.structure.Domain;
import net.ssignal.structure.GroupWithNewSS;
import net.ssignal.util.Constants;
import net.ssignal.util.SharedMethod;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

import static net.ssignal.Activity_Main.主活动;
import static net.ssignal.User.当前用户;
import static net.ssignal.Robot_MainControl.主控机器人;
import static net.ssignal.language.Text.界面文字;
import static net.ssignal.language.Text.组名_任务;

public class Fragment_Main extends Fragment implements View.OnClickListener {

    static Fragment_Main 主窗体;

    private class 最近讯友_复合数据 {
        String 英语讯宝地址;
        long 群编号;
        short 新SS数量;
    }

    private View 第一控件;
    private TextView 标签_讯友录, 标签_小宇宙;
    TextView 标签_聊天;
    ViewPager 左右滑动页容器;
    private ArrayList<View> 左右滑动页列表;
    private int 当前选中的标签 = 0;
    private WebView 浏览器_讯友录;
    private boolean 网页载入完毕 = false;
    private MyHandler 跨线程调用器1;
    private Object 当前讯友录[];
    Fragment_Chating 聊天控件[];
    int 聊天控件数 = 0;
    Fragment_Chating 当前聊天控件;
    private Fragment_TinyUniverse 当前小宇宙控件;
    private Timer 定时器;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater 布局扩充器, @Nullable ViewGroup 控件容器, @Nullable Bundle 已保存的实例状态) {
        第一控件 = 布局扩充器.inflate(R.layout.main, 控件容器, false);
        跨线程调用器1 = new MyHandler(this);
        定时器 = new Timer();
        标签_讯友录 = (TextView) 第一控件.findViewById(R.id.文字_讯友录);
        标签_讯友录.setText(界面文字.获取(74, 标签_讯友录.getText().toString()));
        标签_讯友录.setOnClickListener(this);
        标签_聊天 = (TextView) 第一控件.findViewById(R.id.文字_聊天);
        标签_聊天.setText(界面文字.获取(202, 标签_聊天.getText().toString()));
        标签_聊天.setOnClickListener(this);
        标签_小宇宙 = (TextView) 第一控件.findViewById(R.id.文字_小宇宙);
        标签_小宇宙.setText(界面文字.获取(203, 标签_小宇宙.getText().toString()));
        标签_小宇宙.setOnClickListener(this);
        View 讯友录 = 布局扩充器.inflate(R.layout.contacts, null);
        View 聊天 = 布局扩充器.inflate(R.layout.chat, null);
        View 小宇宙 = 布局扩充器.inflate(R.layout.tinyuniverse, null);
        左右滑动页列表 = new ArrayList<View>();
        左右滑动页列表.add(讯友录);
        左右滑动页列表.add(聊天);
        左右滑动页列表.add(小宇宙);
        PagerAdapter 页面适配器 = new PagerAdapter() {
            @Override
            public int getCount() {
                return 左右滑动页列表.size();
            }

            @Override
            public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
                return view == o;
            }

            @Override
            public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
                container.removeView(左右滑动页列表.get(position));
            }

            @NonNull
            @Override
            public Object instantiateItem(@NonNull ViewGroup container, int position) {
                container.addView(左右滑动页列表.get(position));
                return 左右滑动页列表.get(position);
            }

            @Override
            public void setPrimaryItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
                switch (position) {
                    case 0:
                        if (浏览器_讯友录 == null) {
                            添加讯友录浏览器();
                        }
                        break;
                    case 1:
                        if (当前聊天控件 == null) {
                            添加聊天控件(Constants.机器人id_主控);
                        }
                        break;
                    case 2:
                        显示小宇宙控件();
                        break;
                }
            }
        };
        左右滑动页容器 = (ViewPager) 第一控件.findViewById(R.id.左右滑动页容器);
        左右滑动页容器.setAdapter(页面适配器);
        左右滑动页容器.addOnPageChangeListener(new MyOnPageChangeListener());
        左右滑动页容器.setCurrentItem(当前选中的标签);
        return 第一控件;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SharedPreferences 共享的设置 = getActivity().getSharedPreferences("appsettings", getActivity().MODE_PRIVATE);
        if (共享的设置 != null) {
            boolean 后台自启 = 共享的设置.getBoolean("AutoStart", false);
            if (!后台自启 && 当前用户.已登录()) {
                TextView 文字控件 = (TextView) 第一控件.findViewById(R.id.文字_自启动提醒);
                文字控件.setText(界面文字.获取(313, "请允许本应用自启动。（它被清理后将无法接收消息）"));
                文字控件.setVisibility(View.VISIBLE);
                文字控件.setOnClickListener(this);
            }
        }
        主窗体 = this;
    }

    private void 添加讯友录浏览器() {
        浏览器_讯友录 = (WebView) 第一控件.findViewById(R.id.浏览器_讯友录);
        WebSettings 浏览器设置 = 浏览器_讯友录.getSettings();
        浏览器设置.setJavaScriptEnabled(true);
        浏览器设置.setSupportMultipleWindows(false);
        if (Build.VERSION.SDK_INT >= 21) {
            浏览器设置.setMixedContentMode(0);
            浏览器_讯友录.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        } else if (Build.VERSION.SDK_INT >= 19) {
            浏览器_讯友录.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        } else {
            浏览器_讯友录.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
        浏览器_讯友录.setWebChromeClient(new MyWebChromeClient(this));
        浏览器_讯友录.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                if (BuildConfig.DEBUG && !ProtocolPath.调试时访问真实网站) {
                    handler.proceed();
                }
            }
        });           //这一句不能少，可以禁止WebView调用外部浏览器
        浏览器_讯友录.addJavascriptInterface(this, "external");
        浏览器_讯友录.loadUrl("file:///android_asset/Contacts.html");
    }

    private void 显示小宇宙控件() {
        if (当前聊天控件 == null) {
            return;
        }
        if (当前聊天控件.小宇宙控件 != null) {
            if (当前聊天控件.小宇宙控件.equals(当前小宇宙控件)) {
                return;
            }
            FragmentManager 版面管理器 = getActivity().getSupportFragmentManager();
            FragmentTransaction 版面切换器 = 版面管理器.beginTransaction();
            版面切换器.hide(当前小宇宙控件);
            版面切换器.show(当前聊天控件.小宇宙控件);
            版面切换器.commitAllowingStateLoss();
            当前小宇宙控件 = 当前聊天控件.小宇宙控件;
        } else {
            Fragment_TinyUniverse 新小宇宙控件 = new Fragment_TinyUniverse();
            新小宇宙控件.机器人 = 当前聊天控件.机器人;
            FragmentManager 版面管理器 = getActivity().getSupportFragmentManager();
            FragmentTransaction 版面切换器 = 版面管理器.beginTransaction();
            if (当前小宇宙控件 != null) {
                版面切换器.hide(当前小宇宙控件);
                版面切换器.add(R.id.小宇宙, 新小宇宙控件);
            } else {
                版面切换器.replace(R.id.小宇宙, 新小宇宙控件);
            }
            版面切换器.commitAllowingStateLoss();
            当前聊天控件.小宇宙控件 = 新小宇宙控件;
            当前小宇宙控件 = 新小宇宙控件;
        }
    }

    void onProgressChanged(WebView view, int newProgress) {
        if (newProgress == 100) {
            if (网页载入完毕) {
                return;
            }
            网页载入完毕 = true;
            String html = "<div class='RedNotify' style='display:none;'></div>" + 界面文字.获取(142, "讯宝机器人") + "<br><span class='Address'>" + ProtocolParameters.保留用户名_机器人 + ProtocolParameters.讯宝地址标识 + ProtocolParameters.讯宝网络域名_本国语 + " / " + ProtocolParameters.保留用户名_robot + ProtocolParameters.讯宝地址标识 + ProtocolParameters.讯宝网络域名_英语 + "</span>";
            发送JS(view, "function(){ var html = \"" + html + "\"; ShowRobot1(html); }");
            刷新讯友录(Constants.讯友录显示范围_最近, null, true);
            if (主控机器人 == null) {
                TimerTask 定时任务 = new MyTimerTask(跨线程调用器1, 1);
                定时器.schedule(定时任务, 1000);
            } else {
                主控机器人.陌生人新讯宝数量 = 主控机器人.数据库_获取陌生人新讯宝数量();
                if (主控机器人.陌生人新讯宝数量 > 0) {
                    发送JS(浏览器_讯友录, "function(){ var id = \"" + Constants.机器人id_主控 + "\"; var num = \"" + 主控机器人.陌生人新讯宝数量 + "\"; NewSSNumber(id, num); }");
                }
                Fragment_Main.主窗体.显示隐藏系统管理机器人();
            }
        }
    }

    @JavascriptInterface
    public void ClickAContact(String id) {
        Message 消息 = new Message();
        消息.what = 2;
        Bundle 数据盒子 = new Bundle();
        数据盒子.putString("id", id);
        消息.setData(数据盒子);
        跨线程调用器1.sendMessage(消息);
    }

    private void 点击某一讯友(String id) {
        String 英语讯宝地址 = null, 子域名 = null;
        long 群编号 = 0;
        switch (id.substring(0, 1)) {
            case "c":
                英语讯宝地址 = ((Contact) 当前讯友录[Integer.parseInt(id.substring(1))]).英语讯宝地址;
                break;
            case "s":
                Group_Small 小聊天群 = (Group_Small) 当前讯友录[Integer.parseInt(id.substring(1))];
                英语讯宝地址 = 小聊天群.群主.英语讯宝地址;
                群编号 = 小聊天群.编号;
                break;
            case "l":
                Group_Large 大聊天群 = (Group_Large) 当前讯友录[Integer.parseInt(id.substring(1))];
                子域名 = 大聊天群.子域名;
                群编号 = 大聊天群.编号;
                break;
            case "r":
                英语讯宝地址 = id;
                break;
            case "d":
                显示主控机器人聊天窗体();
                if (当前用户.讯友录当前显示范围 == Constants.讯友录显示范围_黑域) {
                    主控机器人.移除黑域((Domain) 当前讯友录[Integer.parseInt(id.substring(1))]);
                } else {
                    主控机器人.移除白域((Domain) 当前讯友录[Integer.parseInt(id.substring(1))]);
                }
                return;
            default:
                return;
        }
        ChatWith 聊天对象;
        boolean 是新控件 = false;
        int i;
        for (i = 0; i < 聊天控件数; i++) {
            聊天对象 = 聊天控件[i].聊天对象;
            if (聊天对象.小聊天群 != null) {
                if (SharedMethod.字符串未赋值或为空(子域名)) {
                    if (聊天对象.小聊天群.编号 == 群编号 && 聊天对象.讯友或群主.英语讯宝地址.equals(英语讯宝地址)) {
                        break;
                    }
                }
            } else if (聊天对象.大聊天群 != null) {
                if (!SharedMethod.字符串未赋值或为空(子域名)) {
                    if (聊天对象.大聊天群.编号 == 群编号 && 聊天对象.大聊天群.子域名.equals(子域名)) {
                        break;
                    }
                }
            }
        }
        if (i < 聊天控件数) {
            if (!聊天控件[i].equals(当前聊天控件)) {
                左右滑动页容器.setCurrentItem(1);
                FragmentManager 版面管理器 = getActivity().getSupportFragmentManager();
                FragmentTransaction 版面切换器 = 版面管理器.beginTransaction();
                版面切换器.hide(当前聊天控件);
                版面切换器.show(聊天控件[i]);
                版面切换器.commitAllowingStateLoss();
                当前聊天控件 = 聊天控件[i];
            } else {
                左右滑动页容器.setCurrentItem(1);
            }
            聊天对象 = 当前聊天控件.聊天对象;
            if (i > 0) {
                for (i = i; i > 0; i--) {
                    聊天控件[i] = 聊天控件[i - 1];
                }
                聊天控件[0] = 当前聊天控件;
            }
            当前聊天控件.最近活动时间 = System.currentTimeMillis();
        } else {
            添加聊天控件(id);
            左右滑动页容器.setCurrentItem(1);
            聊天对象 = 当前聊天控件.聊天对象;
            是新控件 = true;
        }
        if (!(当前聊天控件.机器人 instanceof Robot_MainControl)) {
            if (聊天对象.小聊天群 != null) {
                if (群编号 == 0) {
                    Contact 某一讯友 = 聊天对象.讯友或群主;
                    if (某一讯友.新讯宝数量 != 0) {
                        某一讯友.新讯宝数量 = 0;
                        主控机器人.数据库_更新新讯宝数量(某一讯友.英语讯宝地址, 群编号, (short) 0);
                        发送JS(浏览器_讯友录, "function(){ var id = \"" + 获取id(某一讯友.英语讯宝地址) + "\"; NewSSNumber(id, 0); }");
                    }
                } else {
                    Group_Small 某一群 = 聊天对象.小聊天群;
                    if (某一群.新讯宝数量 != 0) {
                        某一群.新讯宝数量 = 0;
                        发送JS(浏览器_讯友录, "function(){ var id = \"" + 获取id(某一群.群主.英语讯宝地址, 某一群.编号) + "\"; NewSSNumber(id, 0); }");
                        主控机器人.数据库_更新新讯宝数量(某一群.群主.英语讯宝地址, 群编号, (short) 0);
                    }
                }
            } else if (聊天对象.大聊天群 != null) {
                Group_Large 某一群 = 聊天对象.大聊天群;
                if (某一群.新讯宝数量 != 0) {
                    某一群.新讯宝数量 = 0;
                    发送JS(浏览器_讯友录, "function(){ var id = \"" + 获取id(某一群.子域名, 某一群.编号) + "\"; NewSSNumber(id, 0); }");
                    主控机器人.数据库_更新新讯宝数量(某一群.子域名, 群编号, (short) 0);
                    if (!是新控件) {
                        ((Robot_LargeChatGroup) 当前聊天控件.机器人).刷新();
                    }
                }
            }
        } else {
            if (主控机器人.陌生人新讯宝数量 != 0) {
                主控机器人.陌生人新讯宝数量 = 0;
                发送JS(浏览器_讯友录, "function(){ var id = \"" + Constants.机器人id_主控 + "\"; NewSSNumber(id, 0); }");
                主控机器人.数据库_更新陌生人新讯宝数量(主控机器人.陌生人新讯宝数量);
            }
        }
    }

    private void 添加聊天控件(String id) {
        ChatWith 聊天对象 = new ChatWith();
        switch (id.substring(0, 1)) {
            case "c":
                聊天对象.小聊天群 = new Group_Small();
                聊天对象.讯友或群主 = (Contact) 当前讯友录[Integer.parseInt(id.substring(1))];
                break;
            case "s":
                聊天对象.小聊天群 = (Group_Small) 当前讯友录[Integer.parseInt(id.substring(1))];
                聊天对象.讯友或群主 = 聊天对象.小聊天群.群主;
                break;
            case "l":
                聊天对象.大聊天群 = (Group_Large) 当前讯友录[Integer.parseInt(id.substring(1))];
                break;
            default:
                switch (id) {
                    case Constants.机器人id_主控:
                        聊天对象.讯友或群主 = new Contact();
                        聊天对象.讯友或群主.英语讯宝地址 = Constants.机器人id_主控;
                        聊天对象.小聊天群 = new Group_Small();
                        break;
                    case Constants.机器人id_系统管理:
                        聊天对象.讯友或群主 = new Contact();
                        聊天对象.讯友或群主.英语讯宝地址 = Constants.机器人id_系统管理;
                        聊天对象.小聊天群 = new Group_Small();
                        break;
                    default:
                        return;
                }
        }
        添加聊天控件(聊天对象);
    }

    void 添加聊天控件(ChatWith 聊天对象) {
        if (聊天控件 == null) {
            聊天控件 = new Fragment_Chating[5];
            聊天控件数 = 0;
        } else {
            Fragment_Chating 聊天控件2[];
            if (聊天控件.length == 聊天控件数) {
                聊天控件2 = new Fragment_Chating[聊天控件数 * 2];
            } else {
                聊天控件2 = new Fragment_Chating[聊天控件.length];
            }
            for (int i = 聊天控件数 - 1; i >= 0; i--) {
                聊天控件2[i + 1] = 聊天控件[i];
            }
            聊天控件 = 聊天控件2;
        }
        Fragment_Chating 新聊天控件 = new Fragment_Chating();
        新聊天控件.聊天对象 = 聊天对象;
        新聊天控件.最近活动时间 = System.currentTimeMillis();
        聊天控件[0] = 新聊天控件;
        聊天控件数 += 1;
        if (聊天对象.小聊天群 != null) {
            switch (聊天对象.讯友或群主.英语讯宝地址) {
                case Constants.机器人id_主控:
                    if (主控机器人 == null) {
                        主控机器人 = new Robot_MainControl(getActivity().getApplicationContext(), 新聊天控件);
                    } else {
                        主控机器人.聊天控件 = 新聊天控件;
                    }
                    新聊天控件.机器人 = 主控机器人;
                    break;
                case Constants.机器人id_系统管理:
                    新聊天控件.机器人 = new Robot_SystemManagement(新聊天控件);
                    break;
                default:
                    if (聊天对象.小聊天群.编号 == 0) {
                        新聊天控件.机器人 = new Robot_OneOnOne(新聊天控件);
                    } else {
                        新聊天控件.机器人 = new Robot_SmallChatGroup(新聊天控件);
                    }
            }
        } else if (聊天对象.大聊天群 != null) {
            新聊天控件.机器人 = new Robot_LargeChatGroup(getActivity().getApplicationContext(), 新聊天控件);
        }
        FragmentManager 版面管理器 = getActivity().getSupportFragmentManager();
        FragmentTransaction 版面切换器 = 版面管理器.beginTransaction();
        if (当前聊天控件 != null) {
            版面切换器.hide(当前聊天控件);
            版面切换器.add(R.id.聊天区, 新聊天控件);
        } else {
            版面切换器.replace(R.id.聊天区, 新聊天控件);
        }
        版面切换器.commitAllowingStateLoss();
        当前聊天控件 = 新聊天控件;
    }

    @JavascriptInterface
    public void SelectRange() {
        Message 消息 = new Message();
        消息.what = 3;
        跨线程调用器1.sendMessage(消息);
    }

    private void 显示可选范围() {
        String 讯友标签[] = null;
        int 讯友标签数 = 0, i;
        Contact 讯友目录[] = 当前用户.讯友目录;
        if (讯友目录 != null) {
            讯友标签 = new String[讯友目录.length * 2];
            for (i = 0; i < 讯友目录.length; i++) {
                if (User.收集标签(讯友目录[i].标签一, 讯友标签, 讯友标签数)) {
                    讯友标签数 += 1;
                }
                if (User.收集标签(讯友目录[i].标签二, 讯友标签, 讯友标签数)) {
                    讯友标签数 += 1;
                }
            }
            if (讯友标签数 > 0) {
                if (讯友标签数 < 讯友标签.length) {
                    String 讯友标签2[] = new String[讯友标签数];
                    System.arraycopy(讯友标签, 0, 讯友标签2, 0, 讯友标签数);
                    讯友标签 = 讯友标签2;
                    Arrays.sort(讯友标签);
                }
            }
        }
        StringBuffer 字符串合并器;
        if (讯友标签数 > 0) {
            字符串合并器 = new StringBuffer(500 * (讯友标签数 + 4));
        } else {
            字符串合并器 = new StringBuffer(500 * 4);
        }
        字符串合并器.append(生成讯友录范围html(Constants.讯友录显示范围_最近, null));
        字符串合并器.append(生成讯友录范围html(Constants.讯友录显示范围_讯友, null));
        字符串合并器.append(生成讯友录范围html(Constants.讯友录显示范围_聊天群, null));
        字符串合并器.append(生成讯友录范围html(Constants.讯友录显示范围_黑名单, null));
        字符串合并器.append(生成讯友录范围html(Constants.讯友录显示范围_黑域, null));
        字符串合并器.append(生成讯友录范围html(Constants.讯友录显示范围_白域, null));
        if (讯友标签数 > 0) {
            for (i = 0; i < 讯友标签数; i++) {
                字符串合并器.append(生成讯友录范围html(Constants.讯友录显示范围_某标签, 讯友标签[i]));
            }
        }
        String A = 字符串合并器.toString();
        发送JS(浏览器_讯友录, "function(){ var html = \"" + 字符串合并器.toString() + "\"; ShowRange(html); }");
    }

    private String 生成讯友录范围html(byte 范围, String 标签) {
        String id;
        switch (范围) {
            case Constants.讯友录显示范围_最近:
                id = "lately";
                break;
            case Constants.讯友录显示范围_讯友:
                id = "contacts";
                break;
            case Constants.讯友录显示范围_聊天群:
                id = "groups";
                break;
            case Constants.讯友录显示范围_某标签:
                id = "tag_" + 标签;
                break;
            case Constants.讯友录显示范围_黑名单:
                id = "blacklist";
                break;
            case Constants.讯友录显示范围_黑域:
                id = "blackdomains";
                break;
            case Constants.讯友录显示范围_白域:
                id = "whitedomains";
                break;
            default:
                id = "contacts";
        }
        return "<div id='" + id + "' onclick='ClickARange(\\\"" + id + "\\\")' class='Range'>" + 获取范围的名称(范围, 标签) + "</div>";
    }

    private String 获取范围的名称(byte 范围) {
        return 获取范围的名称(范围, null);
    }

    private String 获取范围的名称(byte 范围, String 标签名称) {
        switch (范围) {
            case Constants.讯友录显示范围_最近:
                return 界面文字.获取(73, "最近");
            case Constants.讯友录显示范围_讯友:
                return 界面文字.获取(74, "讯友");
            case Constants.讯友录显示范围_聊天群:
                return 界面文字.获取(86, "聊天群");
            case Constants.讯友录显示范围_某标签:
                return 界面文字.获取(264, "#% 标签", new Object[]{SharedMethod.替换HTML和JS敏感字符(标签名称)}, false);
            case Constants.讯友录显示范围_黑名单:
                return 界面文字.获取(75, "黑名单");
            case Constants.讯友录显示范围_黑域:
                return 界面文字.获取(49, "黑域");
            case Constants.讯友录显示范围_白域:
                return 界面文字.获取(237, "白域");
            default:
                return 界面文字.获取(74, "讯友");
        }
    }

    @JavascriptInterface
    public void ClickARange(String id) {
        Message 消息 = new Message();
        消息.what = 4;
        Bundle 数据盒子 = new Bundle();
        数据盒子.putString("id", id);
        消息.setData(数据盒子);
        跨线程调用器1.sendMessage(消息);
    }

    void 刷新讯友录() {
        刷新讯友录(Constants.讯友录显示范围_未指定, null, false);
    }

    void 刷新讯友录(byte 范围) {
        刷新讯友录(范围, null, false);
    }

    void 刷新讯友录(byte 范围, String 标签名称, boolean 自动跳转) {
        if (范围 == Constants.讯友录显示范围_未指定) {
            范围 = 当前用户.讯友录当前显示范围;
            if (范围 == Constants.讯友录显示范围_某标签) {
                标签名称 = 当前用户.讯友录当前显示标签;
            }
        }
        当前讯友录 = null;
        int i, 当前讯友录数量 = 0;
        StringBuffer 字符串合并器 = new StringBuffer(2000);
        switch (范围) {
            case Constants.讯友录显示范围_最近:
                最近讯友_复合数据 最近讯友[] = 数据库_获取互动讯友排名();
                if (最近讯友 != null) {
                    当前讯友录 = new Object[最近讯友.length];
                    最近讯友_复合数据 某一最近讯友;
                    Contact 讯友目录[] = 当前用户.讯友目录;
                    Group_Small 加入的小聊天群[] = 当前用户.加入的小聊天群;
                    Group_Large 加入的大聊天群[] = 当前用户.加入的大聊天群;
                    int j;
                    for (i = 0; i < 最近讯友.length; i++) {
                        某一最近讯友 = 最近讯友[i];
                        if (SharedMethod.字符串未赋值或为空(某一最近讯友.英语讯宝地址)) {
                            continue;
                        }
                        if (某一最近讯友.英语讯宝地址.contains(ProtocolParameters.讯宝地址标识)) {
                            if (某一最近讯友.群编号 == 0 && 讯友目录 != null) {
                                for (j = 0; j < 讯友目录.length; j++) {
                                    if (某一最近讯友.英语讯宝地址.equals(讯友目录[j].英语讯宝地址)) {
                                        讯友目录[j].新讯宝数量 = 某一最近讯友.新SS数量;
                                        break;
                                    }
                                }
                                if (j < 讯友目录.length) {
                                    当前讯友录[当前讯友录数量] = 讯友目录[j];
                                    字符串合并器.append(生成讯友html(讯友目录[j], 当前讯友录数量));
                                    当前讯友录数量 += 1;
                                }
                            } else if (加入的小聊天群 != null) {
                                for (j = 0; j < 加入的小聊天群.length; j++) {
                                    if (某一最近讯友.英语讯宝地址.equals(加入的小聊天群[j].群主.英语讯宝地址) && 某一最近讯友.群编号 == 加入的小聊天群[j].编号) {
                                        加入的小聊天群[j].新讯宝数量 = 某一最近讯友.新SS数量;
                                        break;
                                    }
                                }
                                if (j < 加入的小聊天群.length) {
                                    当前讯友录[当前讯友录数量] = 加入的小聊天群[j];
                                    字符串合并器.append(生成小聊天群html(加入的小聊天群[j], 当前讯友录数量));
                                    当前讯友录数量 += 1;
                                }
                            }
                        } else if (加入的大聊天群 != null) {
                            for (j = 0; j < 加入的大聊天群.length; j++) {
                                if (某一最近讯友.英语讯宝地址.equals(加入的大聊天群[j].子域名) && 某一最近讯友.群编号 == 加入的大聊天群[j].编号) {
                                    加入的大聊天群[j].新讯宝数量 = 某一最近讯友.新SS数量;
                                    break;
                                }
                            }
                            if (j < 加入的大聊天群.length) {
                                当前讯友录[当前讯友录数量] = 加入的大聊天群[j];
                                字符串合并器.append(生成大聊天群html(加入的大聊天群[j], 当前讯友录数量));
                                当前讯友录数量 += 1;
                            }
                        }
                    }
                }
                if (字符串合并器.length() == 0 && 自动跳转) {
                    范围 = Constants.讯友录显示范围_讯友;
                } else {
                    break;
                }
            case Constants.讯友录显示范围_讯友:
                if (当前用户.讯友目录 != null) {
                    Contact 讯友目录[] = 当前用户.讯友目录;
                    当前讯友录 = new Object[讯友目录.length];
                    for (i = 0; i < 讯友目录.length; i++) {
                        if (!讯友目录[i].拉黑) {
                            当前讯友录[当前讯友录数量] = 讯友目录[i];
                            字符串合并器.append(生成讯友html(讯友目录[i], 当前讯友录数量));
                            当前讯友录数量 += 1;
                        }
                    }
                }
                break;
            case Constants.讯友录显示范围_聊天群:
                Group_Small 加入的小聊天群[] = 当前用户.加入的小聊天群;
                Group_Large 加入的大聊天群[] = 当前用户.加入的大聊天群;
                if (加入的小聊天群 != null || 加入的大聊天群 != null) {
                    if (加入的小聊天群 != null && 加入的大聊天群 != null) {
                        当前讯友录 = new Object[加入的小聊天群.length + 加入的大聊天群.length];
                    } else if (加入的小聊天群 != null) {
                        当前讯友录 = new Object[加入的小聊天群.length];
                    } else if (加入的大聊天群 != null) {
                        当前讯友录 = new Object[加入的大聊天群.length];
                    }
                    if (加入的小聊天群 != null) {
                        for (i = 0; i < 加入的小聊天群.length; i++) {
                            当前讯友录[当前讯友录数量] = 加入的小聊天群[i];
                            字符串合并器.append(生成小聊天群html(加入的小聊天群[i], 当前讯友录数量));
                            当前讯友录数量 += 1;
                        }
                    }
                    if (加入的大聊天群 != null) {
                        for (i = 0; i < 加入的大聊天群.length; i++) {
                            当前讯友录[当前讯友录数量] = 加入的大聊天群[i];
                            字符串合并器.append(生成大聊天群html(加入的大聊天群[i], 当前讯友录数量));
                            当前讯友录数量 += 1;
                        }
                    }
                }
                break;
            case Constants.讯友录显示范围_某标签:
                if (当前用户.讯友目录 != null) {
                    Contact 讯友目录[] = 当前用户.讯友目录;
                    if (!SharedMethod.字符串未赋值或为空(标签名称)) {
                        当前讯友录 = new Object[讯友目录.length];
                        for (i = 0; i < 讯友目录.length; i++) {
                            if (标签名称.equals(讯友目录[i].标签一) || 标签名称.equals(讯友目录[i].标签二)) {
                                当前讯友录[当前讯友录数量] = 讯友目录[i];
                                字符串合并器.append(生成讯友html(讯友目录[i], 当前讯友录数量));
                                当前讯友录数量 += 1;
                            }
                        }

                    }
                }
                break;
            case Constants.讯友录显示范围_黑名单:
                if (当前用户.讯友目录 != null) {
                    Contact 讯友目录[] = 当前用户.讯友目录;
                    当前讯友录 = new Object[讯友目录.length];
                    for (i = 0; i < 讯友目录.length; i++) {
                        if (讯友目录[i].拉黑) {
                            当前讯友录[当前讯友录数量] = 讯友目录[i];
                            字符串合并器.append(生成讯友html(讯友目录[i], 当前讯友录数量));
                            当前讯友录数量 += 1;
                        }
                    }
                }
                break;
            case Constants.讯友录显示范围_黑域:
                if (当前用户.黑域 != null) {
                    Domain 黑域[] = 当前用户.黑域;
                    当前讯友录 = new Object[黑域.length];
                    for (i = 0; i < 黑域.length; i++) {
                        if (黑域[i].英语.equals(ProtocolParameters.黑域_全部)) {
                            当前讯友录[当前讯友录数量] = 黑域[i];
                            字符串合并器.append(生成域名html(黑域[i], 当前讯友录数量));
                            当前讯友录数量 += 1;
                            break;
                        }
                    }
                    for (i = 0; i < 黑域.length; i++) {
                        if (!黑域[i].英语.equals(ProtocolParameters.黑域_全部)) {
                            当前讯友录[当前讯友录数量] = 黑域[i];
                            字符串合并器.append(生成域名html(黑域[i], 当前讯友录数量));
                            当前讯友录数量 += 1;
                        }
                    }
                }
                break;
            case Constants.讯友录显示范围_白域:
                if (当前用户.白域 != null) {
                    Domain 白域[] = 当前用户.白域;
                    当前讯友录 = new Object[白域.length];
                    for (i = 0; i < 白域.length; i++) {
                        当前讯友录[当前讯友录数量] = 白域[i];
                        字符串合并器.append(生成域名html(白域[i], 当前讯友录数量));
                        当前讯友录数量 += 1;
                    }
                }
                break;
            default:
                return;
        }
        if (当前讯友录数量 > 0) {
            if (当前讯友录数量 < 当前讯友录.length) {
                Object 当前讯友录2[] = new Object[当前讯友录数量];
                System.arraycopy(当前讯友录, 0, 当前讯友录2, 0, 当前讯友录数量);
                当前讯友录 = 当前讯友录2;
            }
        } else {
            当前讯友录 = null;
        }
        if (字符串合并器.length() > 0) {
            if (当前聊天控件 != null) {
                ChatWith 聊天对象 = 当前聊天控件.聊天对象;
                if (聊天对象.小聊天群 != null) {
                    发送JS(浏览器_讯友录, "function(){ var range = \"" + 获取范围的名称(范围, 标签名称) + "\"; var html = \"" + 字符串合并器.toString() + "\"; var id = \"" + 获取id(聊天对象.讯友或群主.英语讯宝地址, 聊天对象.小聊天群.编号) + "\"; LoadContactList(range, html, id); }");
                } else if (聊天对象.大聊天群 != null) {
                    发送JS(浏览器_讯友录, "function(){ var range = \"" + 获取范围的名称(范围, 标签名称) + "\"; var html = \"" + 字符串合并器.toString() + "\"; var id = \"" + 获取id(聊天对象.大聊天群.子域名, 聊天对象.大聊天群.编号) + "\"; LoadContactList(range, html, id); }");
                }
            } else {
                发送JS(浏览器_讯友录, "function(){ var range = \"" + 获取范围的名称(范围, 标签名称) + "\"; var html = \"" + 字符串合并器.toString() + "\"; LoadContactList(range, html); }");
            }
        } else {
            发送JS(浏览器_讯友录, "function(){ var range = \"" + 获取范围的名称(范围, 标签名称) + "\"; LoadContactList(range); }");
        }
        当前用户.讯友录当前显示范围 = 范围;
        if (范围 == Constants.讯友录显示范围_某标签) {
            当前用户.讯友录当前显示标签 = 标签名称;
        } else {
            当前用户.讯友录当前显示标签 = null;
        }
    }

    private 最近讯友_复合数据[] 数据库_获取互动讯友排名() {
        try {
            String 路径 = getActivity().getFilesDir().toString() + "/" + 当前用户.获取英语讯宝地址();
            File 文件 = new File(路径 + "/activity.sspk");
            if (!文件.exists() || 文件.isDirectory()) {
                return null;
            }
            byte 字节数组[] = SharedMethod.读取文件的全部字节(文件.getAbsolutePath());
            if (字节数组 != null) {
                SSPackageReader SS包解读器 = new SSPackageReader(字节数组);
                Object SS包解读器2[] = SS包解读器.读取_重复标签("对象");
                if (SS包解读器2.length > 0) {
                    最近讯友_复合数据 最近讯友[] = new 最近讯友_复合数据[SS包解读器2.length];
                    最近讯友_复合数据 某一最近讯友;
                    Object 值;
                    for (int i = 0; i < SS包解读器2.length; i++) {
                        SSPackageReader SS包解读器3 = (SSPackageReader) SS包解读器2[i];
                        某一最近讯友 = new 最近讯友_复合数据();
                        某一最近讯友.英语讯宝地址 = (String) SS包解读器3.读取_有标签("地址");
                        某一最近讯友.群编号 = (long) SS包解读器3.读取_有标签("群编号");
                        值 = SS包解读器3.读取_有标签("新讯宝数量");
                        if (值 == null) {
                            某一最近讯友.新SS数量 = 0;
                        } else {
                            某一最近讯友.新SS数量 = (short) 值;
                        }
                        最近讯友[i] = 某一最近讯友;
                    }
                    return 最近讯友;
                }
            }
        } catch (Exception e) {
            主控机器人.说(e.getMessage());
        }
        return null;
    }

    private String 生成讯友html(Contact 讯友, int 索引) {
        String 备注 = "";
        if (当前用户.显示讯友临时编号) {
            备注 += "(" + 讯友.临时编号 + ")&nbsp;";
        }
        if (讯友.新讯宝数量 > 0) {
            备注 += "<div class='RedNotify' style='display:inline-block;'>" + 讯友.新讯宝数量 + "</div>";
        } else {
            备注 += "<div class='RedNotify' style='display:none;'></div>";
        }
        if (!SharedMethod.字符串未赋值或为空(讯友.备注)) {
            备注 += 讯友.备注;
        }
        String 地址;
        if (!SharedMethod.字符串未赋值或为空(讯友.本国语讯宝地址)) {
            地址 = 讯友.本国语讯宝地址 + " / " + 讯友.英语讯宝地址;
        } else {
            地址 = 讯友.英语讯宝地址;
        }
        return "<div id='c" + 索引 + "' onclick='ClickAContact(\\\"c" + 索引 + "\\\")'><table><tr><td class='td_SSicon' valign='top'><img class='SSicon' src='" + ProtocolPath.获取讯友头像路径(讯友.英语讯宝地址, 讯友.主机名, 讯友.头像更新时间) + "'/></td><td valign='top' class='Contact'>" + 备注 + "<br><span class='Address'>" + 地址 + "</span></td></tr></table></div>";
    }

    private String 生成小聊天群html(Group_Small 群, int 索引) {
        String 名称 = 界面文字.获取(95, "[小群] #%", new Object[]{群.备注});
        String 备注;
        if (群.新讯宝数量 > 0) {
            备注 = "<div class='RedNotify' style='display:inline-block;'>" + 群.新讯宝数量 + "</div>" + 名称;
        } else {
            备注 = "<div class='RedNotify' style='display:none;'></div>" + 名称;
        }
        String 地址;
        if (!SharedMethod.字符串未赋值或为空(群.群主.本国语讯宝地址)) {
            地址 = 群.群主.本国语讯宝地址 + " / " + 群.群主.英语讯宝地址;
        } else {
            地址 = 群.群主.英语讯宝地址;
        }
        return "<div id='s" + 索引 + "' onclick='ClickAContact(\\\"s" + 索引 + "\\\")'><table><tr><td class='td_SSicon' valign='top'><img class='SSicon' src='" + ProtocolPath.获取讯友头像路径(群.群主.英语讯宝地址, 群.群主.主机名, 群.群主.头像更新时间) + "'/></td><td valign='top' class='Contact'>" + 备注 + "<br><span class='Address'>" + 地址 + "</span></td></tr></table></div>";
    }

    private String 生成大聊天群html(Group_Large 群, int 索引) {
        String 名称 = 界面文字.获取(275, "[大群] #%", new Object[]{群.名称});
        String 备注;
        if (群.新讯宝数量 > 0) {
            备注 = "<div class='RedNotify' style='display:inline-block;'>" + 群.新讯宝数量 + "</div>" + 名称;
        } else {
            备注 = "<div class='RedNotify' style='display:none;'></div>" + 名称;
        }
        String 域名 = 群.主机名 + "." + 群.英语域名;
        if (!SharedMethod.字符串未赋值或为空(群.本国语域名)) {
            域名 += " / " + 群.本国语域名;
        }
        return "<div id='l" + 索引 + "' onclick='ClickAContact(\\\"l" + 索引 + "\\\")'><table><tr><td class='td_SSicon' valign='top'><img class='SSicon' src='" + ProtocolPath.获取大聊天群图标路径(群.子域名, 群.编号, 群.图标更新时间) + "'/></td><td valign='top' class='Contact'>" + 备注 + "<br><span class='Address'>" + 域名 + "</span></td></tr></table></div>";
    }

    private String 生成域名html(Domain 某一黑域, int 索引) {
        String 域名;
        if (某一黑域.英语.equals(ProtocolParameters.黑域_全部)) {
            域名 = 界面文字.获取(236, "所有域") + "<br>" + ProtocolParameters.黑域_全部;
        } else {
            if (!SharedMethod.字符串未赋值或为空(某一黑域.本国语)) {
                域名 = 某一黑域.本国语 + "<br>" + 某一黑域.英语;
            } else {
                域名 = 某一黑域.英语;
            }
        }
        return "<div id='d" + 索引 + "' onclick='ClickAContact(\\\"d" + 索引 + "\\\")'><table><tr><td valign='top'>" + 域名 + "</td></tr></table></div>";
    }

    void 关闭聊天控件(String id, long 群编号) {
        if (Constants.机器人id_主控.equals(id) || 当前聊天控件 == null) {
            return;
        }
        if (当前聊天控件.聊天对象.小聊天群 != null) {
            if (!当前聊天控件.聊天对象.讯友或群主.英语讯宝地址.equals(Constants.机器人id_主控)) {
                显示主控机器人聊天窗体();
            }
        } else {
            显示主控机器人聊天窗体();
        }
        if (聊天控件数 > 1) {
            int i, j;
            FragmentManager 版面管理器 = getActivity().getSupportFragmentManager();
            FragmentTransaction 版面切换器 = 版面管理器.beginTransaction();
            boolean 移除 = false;
            ChatWith 聊天对象;
            while (true) {
                if (id.contains(ProtocolParameters.讯宝地址标识)) {
                    for (i = 0; i < 聊天控件数; i++) {
                        聊天对象 = 聊天控件[i].聊天对象;
                        if (聊天对象.小聊天群 != null) {
                            if (聊天对象.小聊天群.编号 == 群编号 && 聊天对象.讯友或群主.英语讯宝地址.equals(id)) {
                                break;
                            }
                        }
                    }
                } else {
                    for (i = 0; i < 聊天控件数; i++) {
                        聊天对象 = 聊天控件[i].聊天对象;
                        if (聊天对象.大聊天群 != null) {
                            if (聊天对象.大聊天群.编号 == 群编号 && 聊天对象.大聊天群.子域名.equals(id)) {
                                break;
                            }
                        }
                    }
                }
                if (i < 聊天控件数) {
                    版面切换器.remove(聊天控件[i]);
                    if (聊天控件[i].小宇宙控件 != null) {
                        版面切换器.remove(聊天控件[i].小宇宙控件);
                    }
                    移除 = true;
                    if (i < 聊天控件数 - 1) {
                        for (j = i; j < 聊天控件数 - 1; j++) {
                            聊天控件[j] = 聊天控件[j + 1];
                        }
                    }
                    聊天控件数 -= 1;
                } else {
                    break;
                }
            }
            if (移除) {
                版面切换器.commit();
            }
        }
    }

    private void 显示主控机器人聊天窗体() {
        发送JS(浏览器_讯友录, "function(){ ClickAContact('" + Constants.机器人id_主控 + "'); }");
    }

    private void 发送JS(WebView view, String JS) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            view.evaluateJavascript("(" + JS + ")()", new ValueCallback<String>() {
                @Override
                public void onReceiveValue(String value) {

                }
            });
        } else {
            view.loadUrl("javascript:" + JS);
        }
    }

    void 显示隐藏系统管理机器人() {
        if (!SharedMethod.字符串未赋值或为空(当前用户.职能)) {
            发送JS(浏览器_讯友录, "function(){ var html = \"" + 界面文字.获取(133, "系统管理机器人") + "<br><span class='Address'>" + 界面文字.获取(113, "管理用户、聊天群和服务器") + "</span>\"; ShowRobot0(html); }");
        } else {
            发送JS(浏览器_讯友录, "function(){ HideRobot0(); }");
            关闭聊天控件(Constants.机器人id_系统管理, (long) 0);
        }
    }

    private String 获取id(String 讯宝地址或域名) {
        return 获取id(讯宝地址或域名, (long) 0);
    }

    private String 获取id(String 讯宝地址或域名, long 群编号) {
        if (当前讯友录 == null) {
            return "";
        }
        if (!SharedMethod.字符串未赋值或为空(讯宝地址或域名)) {
            if (讯宝地址或域名.contains(ProtocolParameters.讯宝地址标识)) {
                if (群编号 == 0) {
                    for (int i = 0; i < 当前讯友录.length; i++) {
                        if (当前讯友录[i] instanceof Contact) {
                            if (讯宝地址或域名.equals(((Contact) 当前讯友录[i]).英语讯宝地址)) {
                                return "c" + i;
                            }
                        }
                    }
                } else {
                    for (int i = 0; i < 当前讯友录.length; i++) {
                        if (当前讯友录[i] instanceof Group_Small) {
                            if (讯宝地址或域名.equals(((Group_Small) 当前讯友录[i]).群主.英语讯宝地址) && 群编号 == ((Group_Small) 当前讯友录[i]).编号) {
                                return "s" + i;
                            }
                        }
                    }
                }
            } else {
                for (int i = 0; i < 当前讯友录.length; i++) {
                    if (当前讯友录[i] instanceof Group_Large) {
                        if (讯宝地址或域名.equals(((Group_Large) 当前讯友录[i]).子域名) && 群编号 == ((Group_Large) 当前讯友录[i]).编号) {
                            return "l" + i;
                        }
                    }
                }
            }
        }
        return "";
    }

    void 处理跨线程数据(Message msg) {
        switch (msg.what) {
            case 2:
                if (!((Activity_Main) getActivity()).已暂停) {
                    点击某一讯友(msg.getData().getString("id"));
                }
                break;
            case 3:
                显示可选范围();
                break;
            case 4:
                String id = msg.getData().getString("id");
                switch (id) {
                    case "lately":
                        刷新讯友录(Constants.讯友录显示范围_最近);
                        break;
                    case "contacts":
                        刷新讯友录(Constants.讯友录显示范围_讯友);
                        break;
                    case "groups":
                        刷新讯友录(Constants.讯友录显示范围_聊天群);
                        break;
                    case "blacklist":
                        刷新讯友录(Constants.讯友录显示范围_黑名单);
                        break;
                    case "blackdomains":
                        刷新讯友录(Constants.讯友录显示范围_黑域);
                        break;
                    case "whitedomains":
                        刷新讯友录(Constants.讯友录显示范围_白域);
                        break;
                    default:
                        if (id.startsWith("tag_")) {
                            刷新讯友录(Constants.讯友录显示范围_某标签, id.substring(4), false);
                        }
                }
                break;
            case 1:
                显示主控机器人聊天窗体();
                break;
        }
    }


    void 显示收到的讯友讯宝(String 发送者英语讯宝地址, byte 群编号, String 群主英语讯宝地址,
                   long 发送时间, long 序号, byte 讯宝指令, String 文本, short 宽度, short 高度, byte 秒数, boolean 刷新) {
        int i;
        ChatWith 聊天对象;
        if (群编号 == 0) {
            for (i = 0; i < 聊天控件数; i++) {
                聊天对象 = 聊天控件[i].聊天对象;
                if (聊天对象.小聊天群 != null) {
                    if (聊天对象.小聊天群.编号 == 0 && 聊天对象.讯友或群主.英语讯宝地址.equals(发送者英语讯宝地址)) {
                        break;
                    }
                }
            }
        } else {
            for (i = 0; i < 聊天控件数; i++) {
                聊天对象 = 聊天控件[i].聊天对象;
                if (聊天对象.小聊天群 != null) {
                    if (聊天对象.小聊天群.编号 == 群编号 && 聊天对象.讯友或群主.英语讯宝地址.equals(群主英语讯宝地址)) {
                        break;
                    }
                }
            }
        }
        if (i < 聊天控件数) {
            Fragment_Chating 某一控件 = 聊天控件[i];
            switch (讯宝指令) {
                case ProtocolParameters.讯宝指令_某人加入聊天群:
                    某一控件.机器人.说(界面文字.获取(175, "#% 加入了本群。", new Object[]{SharedMethod.替换HTML和JS敏感字符(文本)}), 发送时间, null);
                    break;
                case ProtocolParameters.讯宝指令_退出小聊天群:
                    某一控件.机器人.说(界面文字.获取(178, "#% 离开了本群。", new Object[]{SharedMethod.替换HTML和JS敏感字符(文本)}), 发送时间, null);
                    break;
                case ProtocolParameters.讯宝指令_删减聊天群成员:
                    某一控件.机器人.说(界面文字.获取(190, "群主让 #% 离开了本群。", new Object[]{SharedMethod.替换HTML和JS敏感字符(文本)}), 发送时间, null);
                    break;
                case ProtocolParameters.讯宝指令_修改聊天群名称:
                    某一控件.机器人.说(界面文字.获取(185, "本群名称更改为 #%。", new Object[]{SharedMethod.替换HTML和JS敏感字符(文本)}), 发送时间, null);
                    break;
                default:
                    某一控件.讯友说(发送者英语讯宝地址, 发送时间, 序号, 讯宝指令, 文本, 宽度, 高度, 秒数, false, null);
            }
            if (i > 0) {
                for (i = i; i > 0; i--) {
                    聊天控件[i] = 聊天控件[i - 1];
                }
                聊天控件[0] = 某一控件;
            }
            if (!某一控件.equals(当前聊天控件)) {
                if (刷新) {
                    刷新讯友录(Constants.讯友录显示范围_最近);
                } else if (当前用户.讯友录当前显示范围 != Constants.讯友录显示范围_最近) {
                    刷新讯友录(Constants.讯友录显示范围_最近);
                }
            } else {
                if (刷新 && 当前用户.讯友录当前显示范围 == Constants.讯友录显示范围_最近) {
                    刷新讯友录();
                }
            }
            if (!某一控件.equals(当前聊天控件) || 当前选中的标签 != 1 || 主活动.已暂停) {
                if (群编号 == 0) {
                    Contact 某一讯友 = 某一控件.聊天对象.讯友或群主;
                    if (某一讯友.新讯宝数量 < 999) {
                        某一讯友.新讯宝数量 += 1;
                        主控机器人.数据库_更新新讯宝数量(某一讯友.英语讯宝地址, 群编号, 某一讯友.新讯宝数量);
                        发送JS(浏览器_讯友录, "function(){ var id = \"" + 获取id(发送者英语讯宝地址) + "\"; var num = \"" + 某一讯友.新讯宝数量 + "\"; NewSSNumber(id, num); }");
                    }
                } else {
                    Group_Small 某一群 = 某一控件.聊天对象.小聊天群;
                    if (某一群.新讯宝数量 < 999) {
                        某一群.新讯宝数量 += 1;
                        主控机器人.数据库_更新新讯宝数量(某一群.群主.英语讯宝地址, 群编号, 某一群.新讯宝数量);
                        发送JS(浏览器_讯友录, "function(){ var id = \"" + 获取id(群主英语讯宝地址, 群编号) + "\"; var num = \"" + 某一群.新讯宝数量 + "\"; NewSSNumber(id, num); }");
                    }
                }
            }
        } else {
            if (刷新) {
                刷新讯友录(Constants.讯友录显示范围_最近);
            } else if (当前用户.讯友录当前显示范围 != Constants.讯友录显示范围_最近) {
                刷新讯友录(Constants.讯友录显示范围_最近);
            }
            if (群编号 == 0) {
                Contact 某一讯友 = 当前用户.查找讯友(发送者英语讯宝地址);
                if (某一讯友 != null) {
                    if (某一讯友.新讯宝数量 < 999) {
                        某一讯友.新讯宝数量 += 1;
                        主控机器人.数据库_更新新讯宝数量(某一讯友.英语讯宝地址, 群编号, 某一讯友.新讯宝数量);
                        发送JS(浏览器_讯友录, "function(){ var id = \"" + 获取id(发送者英语讯宝地址) + "\"; var num = \"" + 某一讯友.新讯宝数量 + "\"; NewSSNumber(id, num); }");
                    }
                }
            } else {
                Group_Small 某一群 = 当前用户.查找小聊天群(群主英语讯宝地址, 群编号);
                if (某一群 != null) {
                    if (某一群.新讯宝数量 < 999) {
                        某一群.新讯宝数量 += 1;
                        主控机器人.数据库_更新新讯宝数量(某一群.群主.英语讯宝地址, 群编号, 某一群.新讯宝数量);
                        发送JS(浏览器_讯友录, "function(){ var id = \"" + 获取id(群主英语讯宝地址, 群编号) + "\"; var num = \"" + 某一群.新讯宝数量 + "\"; NewSSNumber(id, num); }");
                    }
                }
            }
        }
    }

    void 显示同步的讯宝(byte 群编号, String 群主英语讯宝地址, long 发送时间, long 序号,
                 byte 讯宝指令, String 文本, short 宽度, short 高度, byte 秒数, boolean 刷新) {
        int i;
        ChatWith 聊天对象;
        if (群编号 == 0) {
            for (i = 0; i < 聊天控件数; i++) {
                聊天对象 = 聊天控件[i].聊天对象;
                if (聊天对象.小聊天群 != null) {
                    if (聊天对象.小聊天群.编号 == 0 && 聊天对象.讯友或群主.英语讯宝地址.equals(群主英语讯宝地址)) {
                        break;
                    }
                }
            }
        } else {
            for (i = 0; i < 聊天控件数; i++) {
                聊天对象 = 聊天控件[i].聊天对象;
                if (聊天对象.小聊天群 != null) {
                    if (聊天对象.小聊天群.编号 == 群编号 && 聊天对象.讯友或群主.英语讯宝地址.equals(群主英语讯宝地址)) {
                        break;
                    }
                }
            }
        }
        if (i < 聊天控件数) {
            Fragment_Chating 某一控件 = 聊天控件[i];
            某一控件.显示同步的讯宝(发送时间, 序号, 讯宝指令, 文本, 宽度, 高度, 秒数);
            if (i > 0) {
                for (; i >= 1; i--) {
                    聊天控件[i] = 聊天控件[i - 1];
                }
                聊天控件[0] = 某一控件;
            }
            if (!某一控件.equals(当前聊天控件)) {
                if (刷新) {
                    刷新讯友录(Constants.讯友录显示范围_最近);
                } else if (当前用户.讯友录当前显示范围 != Constants.讯友录显示范围_最近) {
                    刷新讯友录(Constants.讯友录显示范围_最近);
                }
            } else {
                if (刷新 && 当前用户.讯友录当前显示范围 == Constants.讯友录显示范围_最近) {
                    刷新讯友录();
                }
            }
        } else {
            if (刷新) {
                刷新讯友录(Constants.讯友录显示范围_最近);
            } else if (当前用户.讯友录当前显示范围 != Constants.讯友录显示范围_最近) {
                刷新讯友录(Constants.讯友录显示范围_最近);
            }
        }
    }

    void 显示收到的陌生人讯宝(String 英语讯宝地址, long 时间, long 序号, byte 讯宝指令, String 文本) {
        if (!主控机器人.聊天控件.equals(当前聊天控件) || 当前选中的标签 != 1) {
            if (主控机器人.陌生人新讯宝数量 < 999) {
                主控机器人.陌生人新讯宝数量 += 1;
                主控机器人.数据库_更新陌生人新讯宝数量(主控机器人.陌生人新讯宝数量);
                发送JS(浏览器_讯友录, "function(){ var id = \"" + Constants.机器人id_主控 + "\"; var num = \"" + 主控机器人.陌生人新讯宝数量 + "\"; NewSSNumber(id, num); }");
            }
        }
        主控机器人.聊天控件.陌生人说(英语讯宝地址, 时间, 序号, 讯宝指令, 文本, null);
    }

    void 提示讯宝发送失败(String 发送者英语讯宝地址, byte 群编号, String 群主英语讯宝地址, byte 讯宝指令, long 发送序号, String 讯宝文本) {
        int i;
        ChatWith 聊天对象;
        if (讯宝指令 != ProtocolParameters.讯宝指令_被邀请加入大聊天群者未添加我为讯友) {
            if (群编号 == 0) {
                for (i = 0; i < 聊天控件数; i++) {
                    聊天对象 = 聊天控件[i].聊天对象;
                    if (聊天对象.小聊天群 != null) {
                        if (聊天对象.小聊天群.编号 == 0 && 聊天对象.讯友或群主.英语讯宝地址.equals(发送者英语讯宝地址)) {
                            break;
                        }
                    }
                }
            } else {
                for (i = 0; i < 聊天控件数; i++) {
                    聊天对象 = 聊天控件[i].聊天对象;
                    if (聊天对象.小聊天群 != null) {
                        if (聊天对象.小聊天群.编号 == 群编号 && 聊天对象.讯友或群主.英语讯宝地址.equals(群主英语讯宝地址)) {
                            break;
                        }
                    }
                }
            }
        } else {
            String 子域名;
            long 大聊天群编号;
            try {
                SSPackageReader SS解读器 = new SSPackageReader();
                SS解读器.解读纯文本(讯宝文本);
                子域名 = (String) SS解读器.读取_有标签("D");
                大聊天群编号 = (long) SS解读器.读取_有标签("I");
            } catch (Exception e) {
                return;
            }
            for (i = 0; i < 聊天控件数; i++) {
                聊天对象 = 聊天控件[i].聊天对象;
                if (聊天对象.大聊天群 != null) {
                    if (聊天对象.大聊天群.编号 == 大聊天群编号 && 聊天对象.大聊天群.子域名.equals(子域名)) {
                        break;
                    }
                }
            }
        }
        if (i < 聊天控件数) {
            String 文本;
            switch (讯宝指令) {
                case ProtocolParameters.讯宝指令_已是群成员:
                    文本 = 界面文字.获取(89, "他/她已加入当前聊天群。");
                    break;
                case ProtocolParameters.讯宝指令_不是群成员:
                    文本 = 界面文字.获取(83, "你不是当前聊天群的成员。");
                    break;
                case ProtocolParameters.讯宝指令_群成员数量已达上限:
                    文本 = 界面文字.获取(171, "群成员数量已达上限。");
                    break;
                case ProtocolParameters.讯宝指令_对方未添加我为讯友:
                case ProtocolParameters.讯宝指令_被邀请加入小聊天群者未添加我为讯友:
                case ProtocolParameters.讯宝指令_被邀请加入大聊天群者未添加我为讯友:
                    文本 = 界面文字.获取(153, "发送失败。#%未添加你为讯友。", new Object[]{发送者英语讯宝地址});
                    break;
                case ProtocolParameters.讯宝指令_对方把我拉黑了:
                    文本 = 界面文字.获取(52, "发送失败。你已被其列入黑名单。");
                    break;
                case ProtocolParameters.讯宝指令_讯宝地址不存在:
                    文本 = 界面文字.获取(131, "发送失败。讯宝地址已不存在。");
                    break;
                case ProtocolParameters.讯宝指令_群里没有成员:
                    文本 = 界面文字.获取(170, "除你之外，聊天群里没有其他人。");
                    break;
                case ProtocolParameters.讯宝指令_群里还有成员:
                    文本 = 界面文字.获取(182, "无法解散还有成员的群。");
                    break;
                case ProtocolParameters.讯宝指令_加入的群数量已达上限:
                    文本 = 界面文字.获取(174, "你加入的小聊天群数量已达上限。");
                    break;
                case ProtocolParameters.讯宝指令_本小时发送的讯宝数量已达上限:
                    文本 = 界面文字.获取(266, "本小时发送的讯宝数量已达上限。");
                    break;
                case ProtocolParameters.讯宝指令_今日发送的讯宝数量已达上限:
                    文本 = 界面文字.获取(267, "本日发送的讯宝数量已达上限。");
                    break;
                case ProtocolParameters.讯宝指令_数据传送失败:
                    文本 = 界面文字.获取(198, "数据传送失败。");
                    break;
                case ProtocolParameters.讯宝指令_HTTP数据错误:
                    文本 = 界面文字.获取(200, "HTTP数据错误。");
                    break;
                case ProtocolParameters.讯宝指令_目标服务器程序出错:
                    文本 = 界面文字.获取(201, "目标服务器程序出错。");
                    break;
                default:
                    文本 = 界面文字.获取(108, "出错 #%", new Object[]{讯宝指令});
                    break;
            }
            聊天控件[i].机器人.说(文本);
        }
    }

    void 显示收到的大聊天群讯宝(String 子域名, GroupWithNewSS 有新讯宝的群[], boolean 刷新) {
        long 群编号;
        int i, j, k;
        GroupWithNewSS 某一群;
        ChatWith 聊天对象;
        for (i = 0; i < 有新讯宝的群.length; i++) {
            某一群 = 有新讯宝的群[i];
            群编号 = 某一群.编号;
            for (j = 0; j < 聊天控件数; j++) {
                聊天对象 = 聊天控件[j].聊天对象;
                if (聊天对象.大聊天群 != null) {
                    if (聊天对象.大聊天群.编号 == 群编号 && 聊天对象.大聊天群.子域名.equals(子域名)) {
                        break;
                    }
                }
            }
            if (j < 聊天控件数) {
                Fragment_Chating 某一控件 = 聊天控件[j];
                if (某一群.撤回的讯宝数量 > 0) {
                    for (k = 0; k < 某一群.撤回的讯宝数量; k++) {
                        某一控件.发送JS("function(){ RemoveSS('" + 某一群.撤回的讯宝[k] + "'); }");
                    }
                }
                某一控件.加载大聊天群新讯宝(某一群.时间);
                if (j > 0) {
                    for (j = j; j > 0; j--) {
                        聊天控件[j] = 聊天控件[j - 1];
                    }
                    聊天控件[0] = 某一控件;
                }
                if (!某一控件.equals(当前聊天控件) || 当前选中的标签 != 1 || 主活动.已暂停) {
                    if (刷新) {
                        刷新讯友录(Constants.讯友录显示范围_最近);
                    } else if (当前用户.讯友录当前显示范围 != Constants.讯友录显示范围_最近) {
                        刷新讯友录(Constants.讯友录显示范围_最近);
                    }
                    Group_Large 某一大聊天群 = 当前用户.查找大聊天群(子域名, 群编号);
                    if (某一大聊天群 != null) {
                        if (某一大聊天群.新讯宝数量 + 某一群.新讯宝数量 < 1000) {
                            某一大聊天群.新讯宝数量 += 某一群.新讯宝数量;
                            主控机器人.数据库_更新新讯宝数量(子域名, 群编号, 某一大聊天群.新讯宝数量);
                            发送JS(浏览器_讯友录, "function(){ var id = \"" + 获取id(子域名, 群编号) + "\"; var num = \"" + 某一大聊天群.新讯宝数量 + "\"; NewSSNumber(id, num); }");
                        }
                    }
                } else {
                    if (刷新 && 当前用户.讯友录当前显示范围 == Constants.讯友录显示范围_最近) {
                        刷新讯友录();
                    }
                }
            } else {
                if (刷新) {
                    刷新讯友录(Constants.讯友录显示范围_最近);
                } else if (当前用户.讯友录当前显示范围 != Constants.讯友录显示范围_最近) {
                    刷新讯友录(Constants.讯友录显示范围_最近);
                }
                Group_Large 某一大聊天群 = 当前用户.查找大聊天群(子域名, 群编号);
                if (某一大聊天群 != null) {
                    if (某一大聊天群.新讯宝数量 + 某一群.新讯宝数量 < 1000) {
                        某一大聊天群.新讯宝数量 += 某一群.新讯宝数量;
                        主控机器人.数据库_更新新讯宝数量(子域名, 群编号, 某一大聊天群.新讯宝数量);
                        发送JS(浏览器_讯友录, "function(){ var id = \"" + 获取id(子域名, 群编号) + "\"; var num = \"" + 某一大聊天群.新讯宝数量 + "\"; NewSSNumber(id, num); }");
                    }
                }

            }
        }
    }

    boolean 事件同步(byte 同步事件, String 英语讯宝地址, SSPackageReader SS包解读器) throws Exception {
        int i;
        for (i = 0; i < 聊天控件数; i++) {
            if (聊天控件[i].聊天对象.小聊天群.编号 == 0 && 聊天控件[i].聊天对象.讯友或群主.英语讯宝地址.equals(英语讯宝地址)) {
                break;
            }
        }
        if (i < 聊天控件数) {
            switch (同步事件) {
                case ProtocolParameters.同步事件_讯友添加标签:
                    ((Robot_OneOnOne) 聊天控件[i].机器人).添加标签成功(SS包解读器);
                    break;
                case ProtocolParameters.同步事件_讯友移除标签:
                    ((Robot_OneOnOne) 聊天控件[i].机器人).移除标签成功(SS包解读器);
                    break;
                case ProtocolParameters.同步事件_修改讯友备注:
                    ((Robot_OneOnOne) 聊天控件[i].机器人).修改备注成功(SS包解读器);
                    break;
                case ProtocolParameters.同步事件_拉黑讯友:
                    ((Robot_OneOnOne) 聊天控件[i].机器人).拉黑讯友成功(SS包解读器);
                    break;
            }
            return true;
        } else {
            return false;
        }
    }

    void 发送者撤回(String 发送者英语讯宝地址, byte 群编号, String 群主英语讯宝地址, long 发送序号) {
        int i;
        if (群编号 == 0) {
            for (i = 0; i < 聊天控件数; i++) {
                if (聊天控件[i].聊天对象.小聊天群.编号 == 0 && 聊天控件[i].聊天对象.讯友或群主.英语讯宝地址.equals(发送者英语讯宝地址)) {
                    break;
                }
            }
        } else {
            for (i = 0; i < 聊天控件数; i++) {
                if (聊天控件[i].聊天对象.小聊天群.编号 == 群编号 && 聊天控件[i].聊天对象.讯友或群主.英语讯宝地址.equals(群主英语讯宝地址)) {
                    break;
                }
            }
        }
        if (i < 聊天控件数) {
            聊天控件[i].发送者撤回(发送者英语讯宝地址, 发送序号);
        }
    }

    void 收到加入的小聊天群(Object SS包解读器3[]) throws Exception {
        主控机器人.收到加入的小聊天群(SS包解读器3, 聊天控件, 聊天控件数);
    }

    void 收到加入的大聊天群(Object SS包解读器3[]) throws Exception {
        主控机器人.收到加入的大聊天群(SS包解读器3, 聊天控件, 聊天控件数);
    }

    void 显示大聊天群新讯宝数量(String 子域名, long 群编号, int 新讯宝数量) {
        刷新讯友录(Constants.讯友录显示范围_最近);
        发送JS(浏览器_讯友录, "function(){ var id = \"" + 获取id(子域名, 群编号) + "\"; var num = \"" + 新讯宝数量 + "\"; NewSSNumber(id, num); }");
    }

    void 小聊天群成员有变化(String 群主英语讯宝地址, byte 群编号) {
        Group_Small 某一小聊天群;
        int i;
        for (i = 0; i < 聊天控件数; i++) {
            某一小聊天群 = 聊天控件[i].聊天对象.小聊天群;
            if (某一小聊天群 != null) {
                if (某一小聊天群.编号 == 群编号 && 某一小聊天群.群主.英语讯宝地址.equals(群主英语讯宝地址)) {
                    break;
                }
            }
        }
        if (i < 聊天控件数) {
            聊天控件[i].加载小聊天群的成员列表();
        }
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.文字_讯友录:
                左右滑动页容器.setCurrentItem(0);
                break;
            case R.id.文字_聊天:
                左右滑动页容器.setCurrentItem(1);
                break;
            case R.id.文字_自启动提醒:
                已设置为自启动(view);
                break;
            default:
                左右滑动页容器.setCurrentItem(2);
        }
    }

    private void 已设置为自启动(View view) {
        final View 控件 = view;
        AlertDialog.Builder 对话框 = new AlertDialog.Builder(getActivity());
        对话框.setTitle(界面文字.获取(318, "询问"));
        对话框.setMessage(界面文字.获取(319, "你已允许本应用自启动吗？"));
        对话框.setPositiveButton(界面文字.获取(组名_任务, 0, "是"), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SharedPreferences 共享的设置 = 主活动.getSharedPreferences("appsettings", 主活动.MODE_PRIVATE);
                if (共享的设置 != null) {
                    boolean 后台自启 = 共享的设置.getBoolean("AutoStart", false);
                    if (!后台自启) {
                        SharedPreferences.Editor 编辑器 = 共享的设置.edit();
                        编辑器.putBoolean("AutoStart", true);
                        编辑器.commit();
                    }
                }
                控件.setVisibility(View.GONE);
            }
        });
        对话框.setNegativeButton(界面文字.获取(组名_任务, 1, "否"), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        对话框.show();
    }

    public class MyOnPageChangeListener implements ViewPager.OnPageChangeListener {
        @Override
        public void onPageSelected(int i) {
            if (当前选中的标签 >= 0) {
                switch (当前选中的标签) {
                    case 0:
                        标签_讯友录.setTextColor(Color.BLACK);
                        break;
                    case 1:
                        标签_聊天.setTextColor(Color.BLACK);
                        break;
                    default:
                        标签_小宇宙.setTextColor(Color.BLACK);
                }
            }
            当前选中的标签 = i;
            switch (当前选中的标签) {
                case 0:
                    标签_讯友录.setTextColor(Color.RED);
                    break;
                case 1:
                    标签_聊天.setTextColor(Color.RED);
                    break;
                default:
                    标签_小宇宙.setTextColor(Color.RED);
            }
        }

        @Override
        public void onPageScrolled(int i, float v, int i1) {

        }

        @Override
        public void onPageScrollStateChanged(int i) {

        }
    }

    void 显示自启动提示() {
        SharedPreferences 共享的设置 = getActivity().getSharedPreferences("appsettings", getActivity().MODE_PRIVATE);
        if (共享的设置 != null) {
            boolean 后台自启 = 共享的设置.getBoolean("AutoStart", false);
            if (!后台自启) {
                TextView 文字控件 = (TextView) 第一控件.findViewById(R.id.文字_自启动提醒);
                文字控件.setText(界面文字.获取(313, "请允许本应用自启动。（它被清理后将无法接收消息）"));
                文字控件.setVisibility(View.VISIBLE);
                文字控件.setOnClickListener(this);
            }
        }
    }

}
