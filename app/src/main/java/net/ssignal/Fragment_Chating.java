package net.ssignal;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import net.ssignal.network.httpSetting;
import net.ssignal.protocols.ProtocolMethods;
import net.ssignal.protocols.ProtocolPath;
import net.ssignal.structure.ChatWith;
import net.ssignal.protocols.Contact;
import net.ssignal.protocols.GroupMember;
import net.ssignal.protocols.Group_Large;
import net.ssignal.protocols.Group_Small;
import net.ssignal.protocols.ProtocolParameters;
import net.ssignal.structure.HostName;
import net.ssignal.util.Constants;
import net.ssignal.util.MyDate;
import net.ssignal.protocols.SSPackageCreator;
import net.ssignal.protocols.SSPackageReader;
import net.ssignal.util.SharedMethod;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.Timer;
import java.util.TimerTask;

import static net.ssignal.User.当前用户;
import static net.ssignal.Robot_MainControl.主控机器人;
import static net.ssignal.language.Text.界面文字;
import static net.ssignal.language.Text.组名_任务;
import static net.ssignal.network.encodeURI.替换URI敏感字符;
import static net.ssignal.util.SharedMethod.替换HTML和JS敏感字符;

public class Fragment_Chating extends Fragment implements View.OnClickListener, View.OnTouchListener {

    private class 话语_复合数据 {
        String 文本;
        long 时间;
    }

    private class 讯宝_复合数据 {
        String 收发者讯宝地址, 文本;
        boolean 是接收者, 已收听;
        byte 指令, 秒数;
        long 发送序号, 发送时间;
        short 宽度, 高度;
    }

    ChatWith 聊天对象;
    Robot 机器人;
    private View 第一控件;
    Button 按钮_说话, 按钮_刷新;
    EditText 输入框;
    Fragment_TinyUniverse 小宇宙控件;
    long 最近活动时间;
    private long 起始位置 = -1, 结束位置 = 0;

    private WebView 浏览器_聊天;
    private boolean 网页载入完毕 = false;

    private Timer 定时器_录音, 定时器_机器人回答;
    private TimerTask 定时任务_录音, 定时任务_机器人回答;
    private MyHandler 跨线程调用器;
    private 话语_复合数据 话语;

    private final String 起始字符串 = "";

    boolean 发送语音 = false;
    boolean 需要重新载入 = false;
    private MediaRecorder 媒体录制器;
    private float 录音剩余秒数, 按下位置Y;
    private Audio_Playing 播音类;
    boolean 载入了陌生人讯宝 = false;

//    private TimerTask 定时任务2;
//    private int 计数;

    @Override
    public View onCreateView(LayoutInflater 布局扩充器, ViewGroup 控件容器, Bundle 已保存的实例状态) {
        第一控件 = 布局扩充器.inflate(R.layout.chating, 控件容器, false);
        if (聊天对象 == null) {
            return 第一控件;
        }
        按钮_说话 = (Button) 第一控件.findViewById(R.id.按钮_说话);
        按钮_说话.setOnClickListener(this);
        if (聊天对象.小聊天群 != null) {
            按钮_说话.setText(界面文字.获取(1, 按钮_说话.getText().toString()));
            按钮_说话.setVisibility(View.GONE);
        } else if (聊天对象.大聊天群 != null) {
            按钮_说话.setText(界面文字.获取(2, "刷新"));
            按钮_说话.setVisibility(View.VISIBLE);
            按钮_刷新 = (Button) 第一控件.findViewById(R.id.按钮_刷新);
            按钮_刷新.setOnClickListener(this);
            按钮_刷新.setText(界面文字.获取(2, 按钮_说话.getText().toString()));
            按钮_刷新.setVisibility(View.VISIBLE);
        }
        TextView 文字控件 = (TextView) 第一控件.findViewById(R.id.文字_开始录音);
        文字控件.setOnTouchListener(this);
        文字控件.setText(界面文字.获取(259, "按住，开始录音") + "\n" + 界面文字.获取(260, "上滑，取消录音"));
        文字控件.setTextColor(Color.BLACK);
        ImageView 机器人图片 = (ImageView) 第一控件.findViewById(R.id.图片_机器人);
        机器人图片.setOnClickListener(this);
        输入框 = (EditText) 第一控件.findViewById(R.id.输入框);
        InputFilter 输入过滤器[] = {new InputFilter.LengthFilter(ProtocolParameters.最大值_讯宝文本长度)};
        输入框.setFilters(输入过滤器);
        输入框.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String 文本 = 输入框.getText().toString();
                if (!(机器人 instanceof Robot_LargeChatGroup)) {
                    if (SharedMethod.字符串未赋值或为空(文本.trim())) {
                        按钮_说话.setVisibility(View.GONE);
                    } else {
                        按钮_说话.setVisibility(View.VISIBLE);
                    }
                } else {
                    if (SharedMethod.字符串未赋值或为空(文本.trim())) {
                        按钮_说话.setText(界面文字.获取(2, "刷新"));
                    } else {
                        按钮_说话.setText(界面文字.获取(1, "说话"));
                    }
                }
                if (机器人 != null) {
                    if (机器人.正在输入密码) {
                        if (文本.length() == ProtocolParameters.最小值_密码长度 || 文本.length() == ProtocolParameters.最大值_密码长度) {
                            输入框.setBackgroundColor(Color.parseColor("#00FF7F"));
                        } else if (文本.length() == ProtocolParameters.最小值_密码长度 - 1 || 文本.length() == ProtocolParameters.最大值_密码长度 + 1 || 文本.length() == 0) {
                            输入框.setBackgroundColor(Color.parseColor("#FF9999"));
                        }
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        跨线程调用器 = new MyHandler(this);
        return 第一控件;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (聊天对象 == null) {
            return;
        }
        浏览器_聊天 = (WebView) 第一控件.findViewById(R.id.浏览器_聊天);
        WebSettings 浏览器设置 = 浏览器_聊天.getSettings();
        浏览器设置.setJavaScriptEnabled(true);
        浏览器设置.setSupportMultipleWindows(false);
        if (Build.VERSION.SDK_INT >= 21) {
            浏览器设置.setMixedContentMode(0);
            浏览器_聊天.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        } else if (Build.VERSION.SDK_INT >= 19) {
            浏览器_聊天.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        } else {
            浏览器_聊天.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
        浏览器_聊天.setWebChromeClient(new MyWebChromeClient(this));
        浏览器_聊天.setWebViewClient(new MyWebViewClient(this));
        浏览器_聊天.setDownloadListener(new MyDownloadListener(getActivity(), this));
        浏览器_聊天.addJavascriptInterface(this, "external");
//        浏览器_聊天.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View view) {
//                return true;
//            }
//        });
        浏览器_聊天.loadUrl("file:///android_asset/Chat.html");
    }

    void shouldOverrideUrlLoading(WebView view, String 链接) {
        Intent 意图 = new Intent(Intent.ACTION_VIEW);
        Uri uri = Uri.parse(链接);
        意图.setData(uri);
        startActivity(意图);
    }

    void onProgressChanged(WebView view, int newProgress) {
        if (newProgress == 100) {
            if (网页载入完毕) { return; }
            网页载入完毕 = true;
            机器人.输入框 = 输入框;
            发送JS("function(){ MenuText('" + 界面文字.获取(326, "编辑") + "', '" + 界面文字.获取(206, "复制") + "', '" + 界面文字.获取(193, "撤回") + "', '" + 界面文字.获取(194, "删除") + "');}");
            if (机器人 instanceof Robot_SmallChatGroup) {
                载入最近聊天记录();
                if (聊天对象.小聊天群.群成员 == null) {
                    获取成员列表();
                }
            } else if (机器人 instanceof Robot_LargeChatGroup) {
                if (SharedMethod.字符串未赋值或为空(聊天对象.大聊天群.连接凭据)) {
                    获取连接凭据(false);
                    return;
                }
                载入最近聊天记录();
            } else if (机器人 instanceof Robot_MainControl) {
                Robot_MainControl 主控机器人 = (Robot_MainControl) 机器人;
                if (主控机器人.从未自检) {
                    主控机器人.自检();
                    主控机器人.从未自检 = false;
                } else {
                    载入最近聊天记录();
                }
            } else {
                载入最近聊天记录();
            }
        }
    }

    void 获取连接凭据(boolean 不使用本机缓存) {
        byte 字节数组[];
        if (!不使用本机缓存) {
            当前用户.读取大聊天群凭据(聊天对象.大聊天群);
            if (!SharedMethod.字符串未赋值或为空(聊天对象.大聊天群.连接凭据)) {
                if (!((Robot_LargeChatGroup) 机器人).已载入最近聊天记录) {
                    载入最近聊天记录();
                    ((Robot_LargeChatGroup) 机器人).已载入最近聊天记录 = true;
                }
                return;
            }
        } else {
            当前用户.清除大聊天群凭据(聊天对象.大聊天群);
        }
        try {
            SSPackageCreator SS包生成器 = new SSPackageCreator();
            SS包生成器.添加_有标签("发送序号", 当前用户.讯宝发送序号);
            SS包生成器.添加_有标签("子域名", 聊天对象.大聊天群.子域名);
            SS包生成器.添加_有标签("群编号", 聊天对象.大聊天群.编号);
            字节数组 = SS包生成器.生成SS包(当前用户.AES加密器);
        } catch (Exception e) {
            机器人.说(e.getMessage());
            return;
        }
        if (机器人.任务 != null) {
            机器人.任务.结束();
        }
        机器人.任务 = new Task(TaskName.任务名称_加入大聊天群, 机器人);
        机器人.说(界面文字.获取(281, "正在获取连接凭据。请稍等。"));
        机器人.启动HTTPS访问线程(new httpSetting(ProtocolPath.获取传送服务器访问路径开头(当前用户.主机名, 当前用户.域名_英语, false) + "C=JoinLargeGroup&UserID=" + 当前用户.编号 + "&Position=" + 当前用户.位置号 + "&DeviceType=" + ProtocolParameters.设备类型_文本_手机, 20000, 字节数组));
    }

    void 获取成员列表() {
        当前用户.读取小聊天群成员列表(聊天对象.小聊天群);
        if (聊天对象.小聊天群.群成员 != null) {
            return;
        }
        if (主控机器人.数据库_保存要发送的小聊天群讯宝(聊天对象.讯友或群主.英语讯宝地址, 聊天对象.小聊天群.编号, SharedMethod.获取当前UTC时间(), ProtocolParameters.讯宝指令_获取小聊天群成员列表, (聊天对象.小聊天群.待加入确认 ? 聊天对象.小聊天群.备注 : null), (short) 0, (short) 0, (byte) 0)) {
            主控机器人.发送讯宝(false);
        }
    }

    void 载入最近聊天记录() {
        try {
            if (机器人 instanceof Robot_OneOnOne || 机器人 instanceof Robot_SmallChatGroup) {
                final int 最大值 = 20;
                讯宝_复合数据 讯宝[] = 数据库_读取旧讯宝(最大值);
                讯宝_复合数据 某一讯宝;
                if (讯宝 != null) {
                    if (讯宝.length < 最大值) {
                        起始位置 = 0;
                    }
                    int i;
                    if (聊天对象.小聊天群.编号 == 0) {
                        for (i = 讯宝.length - 1; i >= 0; i--) {
                            某一讯宝 = 讯宝[i];
                            if (!某一讯宝.是接收者) {
                                讯友说(某一讯宝.收发者讯宝地址, 某一讯宝.发送时间, 某一讯宝.发送序号, 某一讯宝.指令, 某一讯宝.文本, 某一讯宝.宽度, 某一讯宝.高度, 某一讯宝.秒数, 某一讯宝.已收听, null);
                            } else {
                                switch (某一讯宝.指令) {
                                    case ProtocolParameters.讯宝指令_发送文字:
                                        发送JS("function(){var text = \"" + 替换HTML和JS敏感字符(某一讯宝.文本) + "\"; var iconsrc =\"" + ProtocolPath.获取我的头像路径(当前用户.英语用户名, 当前用户.主机名, 当前用户.头像更新时间, 当前用户.域名_英语) + "\"; var time = \"" + 时间格式(new MyDate(某一讯宝.发送时间)) + "\"; SSout_Text(\"" + 某一讯宝.发送时间 + "\", text, iconsrc, time); }");
                                        break;
                                    case ProtocolParameters.讯宝指令_发送图片:
                                    case ProtocolParameters.讯宝指令_发送语音:
                                    case ProtocolParameters.讯宝指令_发送短视频:
                                        String 路径;
                                        if (!某一讯宝.文本.contains(ProtocolParameters.特征字符_下划线)) {
                                            路径 = SharedMethod.处理文件路径以用作JS函数参数(某一讯宝.文本);
                                        } else {
                                            路径 = ProtocolPath.获取传送服务器访问路径开头(当前用户.主机名, 当前用户.域名_英语, true) + "Position=" + 当前用户.位置号 + "&EnglishSSAddress=" + 替换URI敏感字符(当前用户.获取英语讯宝地址()) + "&FileName=" + 替换URI敏感字符(某一讯宝.文本);
                                        }
                                        switch (某一讯宝.指令) {
                                            case ProtocolParameters.讯宝指令_发送图片:
                                                发送JS("function(){var imgsrc = \"" + 路径 + "\"; var iconsrc =\"" + ProtocolPath.获取我的头像路径(当前用户.英语用户名, 当前用户.主机名, 当前用户.头像更新时间, 当前用户.域名_英语) + "\"; var time = \"" + 时间格式(new MyDate(某一讯宝.发送时间)) + "\"; SSout_Img(\"" + 某一讯宝.发送时间 + "\", imgsrc, \"" + 某一讯宝.宽度 + "\", \"" + 某一讯宝.高度 + "\", iconsrc, time); }");
                                                break;
                                            case ProtocolParameters.讯宝指令_发送语音:
                                                String 文本 = 界面文字.获取(258, "语音：#% 秒", new Object[]{某一讯宝.秒数});
                                                发送JS("function(){var text = \"" + 替换HTML和JS敏感字符(文本) + "\"; var voicesrc = \"" + 路径 + "\"; var iconsrc =\"" + ProtocolPath.获取我的头像路径(当前用户.英语用户名, 当前用户.主机名, 当前用户.头像更新时间, 当前用户.域名_英语) + "\"; var time = \"" + 时间格式(new MyDate(某一讯宝.发送时间)) + "\"; SSout_Voice(\"" + 某一讯宝.发送时间 + "\", text, voicesrc, iconsrc, time); }");
                                                break;
                                            case ProtocolParameters.讯宝指令_发送短视频:
                                                发送JS("function(){var videosrc = \"" + 路径 + "\"; var iconsrc =\"" + ProtocolPath.获取我的头像路径(当前用户.英语用户名, 当前用户.主机名, 当前用户.头像更新时间, 当前用户.域名_英语) + "\"; var time = \"" + 时间格式(new MyDate(某一讯宝.发送时间)) + "\"; SSout_Video(\"" + 某一讯宝.发送时间 + "\", videosrc, \"" + 某一讯宝.宽度 + "\", \"" + 某一讯宝.高度 + "\", iconsrc, time); }");
                                                break;
                                        }
                                        break;
                                    case ProtocolParameters.讯宝指令_发送文件:
                                        String 原始文件名;
                                        if (!某一讯宝.文本.startsWith(ProtocolParameters.SS包标识_纯文本)) {
                                            原始文件名 = SharedMethod.获取文件名(某一讯宝.文本);
                                            路径 = SharedMethod.处理文件路径以用作JS函数参数(某一讯宝.文本);
                                        } else {
                                            try {
                                                SSPackageReader SS包解读器2 = new SSPackageReader();
                                                SS包解读器2.解读纯文本(某一讯宝.文本);
                                                原始文件名 = (String)SS包解读器2.读取_有标签("O");
                                                String 存储文件名 = (String)SS包解读器2.读取_有标签("S");
                                                路径 = ProtocolPath.获取传送服务器访问路径开头(当前用户.主机名, 当前用户.域名_英语, true) + "Position=" + 当前用户.位置号 + "&EnglishSSAddress=" + 替换URI敏感字符(当前用户.获取英语讯宝地址()) + "&FileName=" + 替换URI敏感字符(存储文件名);
                                            } catch (Exception e) {
                                                continue;
                                            }
                                        }
                                        发送JS("function(){var text = \"" + SharedMethod.处理文件路径以用作JS函数参数(原始文件名) + "\"; var filesrc = \"" + 路径 + "\"; var iconsrc =\"" + ProtocolPath.获取我的头像路径(当前用户.英语用户名, 当前用户.主机名, 当前用户.头像更新时间, 当前用户.域名_英语) + "\"; var time = \"" + 时间格式(new MyDate(某一讯宝.发送时间)) + "\"; SSout_File(\"" + 某一讯宝.发送时间 + "\", text, filesrc, iconsrc, time); }");
                                        break;
                                }
                            }
                        }
                    } else {
                        String 英语讯宝地址 = 当前用户.获取英语讯宝地址();
                        for (i = 讯宝.length - 1; i >= 0; i--) {
                            某一讯宝 = 讯宝[i];
                            switch (某一讯宝.指令) {
                                case ProtocolParameters.讯宝指令_某人加入聊天群:
                                    机器人.说(界面文字.获取(175, "#% 加入了本群。", new Object[]{替换HTML和JS敏感字符(某一讯宝.文本)}), 某一讯宝.发送时间, null);
                                    break;
                                case ProtocolParameters.讯宝指令_退出小聊天群:
                                    机器人.说(界面文字.获取(178, "#% 离开了本群。", new Object[]{替换HTML和JS敏感字符(某一讯宝.文本)}), 某一讯宝.发送时间, null);
                                    break;
                                case ProtocolParameters.讯宝指令_删减聊天群成员:
                                    机器人.说(界面文字.获取(190, "群主让 #% 离开了本群。", new Object[]{替换HTML和JS敏感字符(某一讯宝.文本)}), 某一讯宝.发送时间, null);
                                    break;
                                case ProtocolParameters.讯宝指令_修改聊天群名称:
                                    机器人.说(界面文字.获取(185, "本群名称更改为 #%。", new Object[]{替换HTML和JS敏感字符(某一讯宝.文本)}), 某一讯宝.发送时间, null);
                                    break;
                                default:
                                    if (!某一讯宝.收发者讯宝地址.equals(英语讯宝地址)) {
                                        讯友说(某一讯宝.收发者讯宝地址, 某一讯宝.发送时间, 某一讯宝.发送序号, 某一讯宝.指令, 某一讯宝.文本, 某一讯宝.宽度, 某一讯宝.高度, 某一讯宝.秒数, 某一讯宝.已收听, null);
                                    } else {
                                        switch (某一讯宝.指令) {
                                            case ProtocolParameters.讯宝指令_发送文字:
                                                发送JS("function(){var text = \"" + 替换HTML和JS敏感字符(某一讯宝.文本) + "\"; var iconsrc =\"" + ProtocolPath.获取我的头像路径(当前用户.英语用户名, 当前用户.主机名, 当前用户.头像更新时间, 当前用户.域名_英语) + "\"; var time = \"" + 时间格式(new MyDate(某一讯宝.发送时间)) + "\"; SSout_Text(\"" + 某一讯宝.发送时间 + "\", text, iconsrc, time); }");
                                                break;
                                            case ProtocolParameters.讯宝指令_发送图片:
                                            case ProtocolParameters.讯宝指令_发送语音:
                                            case ProtocolParameters.讯宝指令_发送短视频:
                                                String 路径;
                                                if (!某一讯宝.文本.contains(ProtocolParameters.特征字符_下划线)) {
                                                    路径 = SharedMethod.处理文件路径以用作JS函数参数(某一讯宝.文本);
                                                } else {
                                                    路径 = ProtocolPath.获取传送服务器访问路径开头(当前用户.主机名, 当前用户.域名_英语, true) + "Position=" + 当前用户.位置号 + "&EnglishSSAddress=" + 替换URI敏感字符(当前用户.获取英语讯宝地址()) + "&FileName=" + 替换URI敏感字符(某一讯宝.文本);
                                                }
                                                switch (某一讯宝.指令) {
                                                    case ProtocolParameters.讯宝指令_发送图片:
                                                        发送JS("function(){var imgsrc = \"" + 路径 + "\"; var iconsrc =\"" + ProtocolPath.获取我的头像路径(当前用户.英语用户名, 当前用户.主机名, 当前用户.头像更新时间, 当前用户.域名_英语) + "\"; var time = \"" + 时间格式(new MyDate(某一讯宝.发送时间)) + "\"; SSout_Img(\"" + 某一讯宝.发送时间 + "\", imgsrc, \"" + 某一讯宝.宽度 + "\", \"" + 某一讯宝.高度 + "\", iconsrc, time); }");
                                                        break;
                                                    case ProtocolParameters.讯宝指令_发送语音:
                                                        String 文本 = 界面文字.获取(258, "语音：#% 秒", new Object[]{某一讯宝.秒数});
                                                        发送JS("function(){var text = \"" + 替换HTML和JS敏感字符(文本) + "\"; var voicesrc = \"" + 路径 + "\"; var iconsrc =\"" + ProtocolPath.获取我的头像路径(当前用户.英语用户名, 当前用户.主机名, 当前用户.头像更新时间, 当前用户.域名_英语) + "\"; var time = \"" + 时间格式(new MyDate(某一讯宝.发送时间)) + "\"; SSout_Voice(\"" + 某一讯宝.发送时间 + "\", text, voicesrc, \"" + 某一讯宝.宽度 + "\", \"" + 某一讯宝.高度 + "\", iconsrc, time); }");
                                                        break;
                                                    case ProtocolParameters.讯宝指令_发送短视频:
                                                        发送JS("function(){var videosrc = \"" + 路径 + "\"; var iconsrc =\"" + ProtocolPath.获取我的头像路径(当前用户.英语用户名, 当前用户.主机名, 当前用户.头像更新时间, 当前用户.域名_英语) + "\"; var time = \"" + 时间格式(new MyDate(某一讯宝.发送时间)) + "\"; SSout_Video(\"" + 某一讯宝.发送时间 + "\", videosrc, \"" + 某一讯宝.宽度 + "\", \"" + 某一讯宝.高度 + "\", iconsrc, time); }");
                                                        break;
                                                }
                                                break;
                                            case ProtocolParameters.讯宝指令_发送文件:
                                                String 原始文件名;
                                                if (!某一讯宝.文本.startsWith(ProtocolParameters.SS包标识_纯文本)) {
                                                    原始文件名 = SharedMethod.获取文件名(某一讯宝.文本);
                                                    路径 = SharedMethod.处理文件路径以用作JS函数参数(某一讯宝.文本);
                                                } else {
                                                    try {
                                                        SSPackageReader SS包解读器2 = new SSPackageReader();
                                                        SS包解读器2.解读纯文本(某一讯宝.文本);
                                                        原始文件名 = (String)SS包解读器2.读取_有标签("O");
                                                        String 存储文件名 = (String)SS包解读器2.读取_有标签("S");
                                                        路径 = ProtocolPath.获取传送服务器访问路径开头(当前用户.主机名, 当前用户.域名_英语, true) + "Position=" + 当前用户.位置号 + "&EnglishSSAddress=" + 替换URI敏感字符(当前用户.获取英语讯宝地址()) + "&FileName=" + 替换URI敏感字符(存储文件名);
                                                    } catch (Exception e) {
                                                        continue;
                                                    }
                                                }
                                                发送JS("function(){var text = \"" + SharedMethod.处理文件路径以用作JS函数参数(原始文件名) + "\"; var filesrc = \"" + 路径 + "\"; var iconsrc =\"" + ProtocolPath.获取我的头像路径(当前用户.英语用户名, 当前用户.主机名, 当前用户.头像更新时间, 当前用户.域名_英语) + "\"; var time = \"" + 时间格式(new MyDate(某一讯宝.发送时间)) + "\"; SSout_File(\"" + 某一讯宝.发送时间 + "\", text, filesrc, iconsrc, time); }");
                                                break;
                                        }
                                    }
                            }
                        }
                    }
                } else {
                    起始位置 = 0;
                }
                发送JS("function(){ LoadEnd(false); }");
            } else if (机器人 instanceof Robot_LargeChatGroup) {
                final int 最大值 = 20;
                讯宝_复合数据 讯宝[] = 数据库_读取旧讯宝(最大值);
                讯宝_复合数据 某一讯宝;
                if (讯宝 != null) {
                    if (讯宝.length < 最大值) {
                        起始位置 = 0;
                    }
                    聊天对象.大聊天群.最新讯宝的发送时间 = 讯宝[0].发送时间;
                    String 英语讯宝地址 = 当前用户.获取英语讯宝地址();
                    int i;
                    long 发送时间;
                    String 时间提示文本;
                    final int 六万毫秒 = 60 * 1000;
                    for (i = 讯宝.length - 1; i >= 0; i--) {
                        某一讯宝 = 讯宝[i];
                        发送时间 = ProtocolMethods.转换成Java相对时间2(某一讯宝.发送时间);
                        if (i > 0) {
                            if (Math.abs(某一讯宝.发送时间 - 讯宝[i - 1].发送时间) > 六万毫秒 ) {
                                时间提示文本 = 时间格式(new MyDate(发送时间));
                            } else {
                                时间提示文本 = "";    //不能赋值为 null
                            }
                        } else {
                            时间提示文本 = 时间格式(new MyDate(发送时间));
                        }
                        if (!某一讯宝.收发者讯宝地址.equals(英语讯宝地址)) {
                            讯友说(某一讯宝.收发者讯宝地址, 某一讯宝.发送时间, 某一讯宝.发送序号, 某一讯宝.指令, 某一讯宝.文本, 某一讯宝.宽度, 某一讯宝.高度, 某一讯宝.秒数, 某一讯宝.已收听, 时间提示文本);
                        } else {
                            switch (某一讯宝.指令) {
                                case ProtocolParameters.讯宝指令_发送文字:
                                    发送JS("function(){var text = \"" + 替换HTML和JS敏感字符(某一讯宝.文本) + "\"; var iconsrc =\"" + ProtocolPath.获取我的头像路径(当前用户.英语用户名, 当前用户.主机名, 当前用户.头像更新时间, 当前用户.域名_英语) + "\"; var time = \"" + 时间提示文本 + "\"; SSout_Text(\"" + 某一讯宝.发送时间 + "\", text, iconsrc, time); }");
                                    break;
                                case ProtocolParameters.讯宝指令_发送图片:
                                case ProtocolParameters.讯宝指令_发送语音:
                                case ProtocolParameters.讯宝指令_发送短视频:
                                    String 路径;
                                    if (!某一讯宝.文本.contains(ProtocolParameters.特征字符_下划线)) {
                                        路径 = SharedMethod.处理文件路径以用作JS函数参数(某一讯宝.文本);
                                    } else {
                                        路径 = ProtocolPath.获取大聊天群服务器访问路径开头(聊天对象.大聊天群.子域名, true) + "GroupID=" + 聊天对象.大聊天群.编号 + "&FileName=" + 替换URI敏感字符(某一讯宝.文本);
                                    }
                                    switch (某一讯宝.指令) {
                                        case ProtocolParameters.讯宝指令_发送图片:
                                            发送JS("function(){var imgsrc = \"" + 路径 + "\"; var iconsrc =\"" + ProtocolPath.获取我的头像路径(当前用户.英语用户名, 当前用户.主机名, 当前用户.头像更新时间, 当前用户.域名_英语) + "\"; var time = \"" + 时间提示文本 + "\"; SSout_Img(\"" + 某一讯宝.发送时间 + "\", imgsrc, \"" + 某一讯宝.宽度 + "\", \"" + 某一讯宝.高度 + "\", iconsrc, time, \"true\"); }");
                                            break;
                                        case ProtocolParameters.讯宝指令_发送语音:
                                            String 文本 = 界面文字.获取(258, "语音：#% 秒", new Object[]{某一讯宝.秒数});
                                            发送JS("function(){var text = \"" + 替换HTML和JS敏感字符(文本) + "\"; var voicesrc = \"" + 路径 + "\"; var iconsrc =\"" + ProtocolPath.获取我的头像路径(当前用户.英语用户名, 当前用户.主机名, 当前用户.头像更新时间, 当前用户.域名_英语) + "\"; var time = \"" + 时间提示文本 + "\"; SSout_Voice(\"" + 某一讯宝.发送时间 + "\", text, voicesrc, \"" + 某一讯宝.宽度 + "\", \"" + 某一讯宝.高度 + "\", iconsrc, time); }");
                                            break;
                                        case ProtocolParameters.讯宝指令_发送短视频:
                                            发送JS("function(){var videosrc = \"" + 路径 + "\"; var iconsrc =\"" + ProtocolPath.获取我的头像路径(当前用户.英语用户名, 当前用户.主机名, 当前用户.头像更新时间, 当前用户.域名_英语) + "\"; var time = \"" + 时间提示文本 + "\"; SSout_Video(\"" + 某一讯宝.发送时间 + "\", videosrc, \"" + 某一讯宝.宽度 + "\", \"" + 某一讯宝.高度 + "\", iconsrc, time); }");
                                            break;
                                    }
                                    break;
                                case ProtocolParameters.讯宝指令_发送文件:
                                    String 原始文件名;
                                    if (!某一讯宝.文本.startsWith(ProtocolParameters.SS包标识_纯文本)) {
                                        原始文件名 = SharedMethod.获取文件名(某一讯宝.文本);
                                        路径 = SharedMethod.处理文件路径以用作JS函数参数(某一讯宝.文本);
                                    } else {
                                        try {
                                            SSPackageReader SS包解读器2 = new SSPackageReader();
                                            SS包解读器2.解读纯文本(某一讯宝.文本);
                                            原始文件名 = (String)SS包解读器2.读取_有标签("O");
                                            String 存储文件名 = (String)SS包解读器2.读取_有标签("S");
                                            路径 = ProtocolPath.获取大聊天群服务器访问路径开头(聊天对象.大聊天群.子域名, true) + "GroupID=" + 聊天对象.大聊天群.编号 + "&FileName=" + 替换URI敏感字符(存储文件名);
                                        } catch (Exception e) {
                                            continue;
                                        }
                                    }
                                    发送JS("function(){var text = \"" + SharedMethod.处理文件路径以用作JS函数参数(原始文件名) + "\"; var filesrc = \"" + 路径 + "\"; var iconsrc =\"" + ProtocolPath.获取我的头像路径(当前用户.英语用户名, 当前用户.主机名, 当前用户.头像更新时间, 当前用户.域名_英语) + "\"; var time = \"" + 时间格式(new MyDate(某一讯宝.发送时间)) + "\"; SSout_File(\"" + 某一讯宝.发送时间 + "\", text, filesrc, iconsrc, time); }");
                                    break;
                            }
                        }
                    }
                } else {
                    起始位置 = 0;
                }
                说话();
                发送JS("function(){ LoadEnd(true); }");
            } else if (机器人 instanceof Robot_MainControl) {
                final int 最大值 = 20;
                讯宝_复合数据 讯宝[] = 数据库_读取旧讯宝(最大值);
                讯宝_复合数据 某一讯宝;
                if (讯宝 != null) {
                    if (讯宝.length < 最大值) {
                        起始位置 = 0;
                    }
                    int i;
                    for (i = 讯宝.length - 1; i >= 0; i--) {
                        某一讯宝 = 讯宝[i];
                        陌生人说(某一讯宝.收发者讯宝地址, 某一讯宝.发送时间, 某一讯宝.发送序号, 某一讯宝.指令, 某一讯宝.文本, null);
                    }
                } else {
                    起始位置 = 0;
                }
                发送JS("function(){ LoadEnd(false); }");
            } else {
                发送JS("function(){ LoadEnd(false); }");
            }
        } catch (Exception e) {
            机器人.说(e.getMessage());
        }
    }

    private 讯宝_复合数据[] 数据库_读取旧讯宝(int 最大值) {
        讯宝_复合数据 讯宝[] = new 讯宝_复合数据[最大值];
        讯宝_复合数据 一个讯宝;
        int 讯宝数量 = 0;
        try {
            String 路径 = getActivity().getFilesDir().toString() + "/" + 当前用户.获取英语讯宝地址();
            if (机器人 instanceof Robot_OneOnOne) {
                File 文件 = new File(路径 + "/" + 聊天对象.讯友或群主.英语讯宝地址 + ".sscj");
                if (文件.exists() && !文件.isDirectory()) {
                    RandomAccessFile 文件随机访问器 = new RandomAccessFile(文件.getAbsolutePath(), "r");
                    if (起始位置 < 0) {
                        文件随机访问器.seek(文件随机访问器.length());
                    } else {
                        文件随机访问器.seek(起始位置);
                    }
                    SSPackageReader SS包读取器;
                    do {
                        SS包读取器 = SharedMethod.读取一个SS包(文件随机访问器);
                        if (SS包读取器 == null) {
                            SS包读取器 = SharedMethod.读取一个SS包2(文件随机访问器);
                        }
                        if (SS包读取器 != null) {
                            if (!(boolean) SS包读取器.读取_有标签("删除")) {
                                一个讯宝 = new 讯宝_复合数据();
                                一个讯宝.收发者讯宝地址 = 聊天对象.讯友或群主.英语讯宝地址;
                                一个讯宝.是接收者 = (boolean)SS包读取器.读取_有标签("是接收者");
                                一个讯宝.发送序号 = (long)SS包读取器.读取_有标签("发送序号");
                                一个讯宝.指令 = (byte)SS包读取器.读取_有标签("指令");
                                Object 值 = SS包读取器.读取_有标签("文本");
                                if (值 != null) { 一个讯宝.文本 = (String)值; }
                                值 = SS包读取器.读取_有标签("宽度");
                                if (值 != null) { 一个讯宝.宽度 = (short)值; }
                                值 = SS包读取器.读取_有标签("高度");
                                if (值 != null) { 一个讯宝.高度 = (short)值; }
                                值 = SS包读取器.读取_有标签("秒数");
                                if (值 != null) {
                                    一个讯宝.秒数 = (byte)值;
                                    值 = SS包读取器.读取_有标签("已收听");
                                    if (值 != null) { 一个讯宝.已收听 = (boolean)值; }
                                }
                                一个讯宝.发送时间 = (long)SS包读取器.读取_有标签("发送时间");
                                讯宝[讯宝数量] = 一个讯宝;
                                讯宝数量 += 1;
                                if (讯宝数量 == 讯宝.length) {
                                    起始位置 = 文件随机访问器.getFilePointer();
                                    文件随机访问器.close();
                                    return 讯宝;
                                }
                            }
                        }
                    } while (SS包读取器 != null);
                    起始位置 = 文件随机访问器.getFilePointer();
                    文件随机访问器.close();
                }
            } else if (机器人 instanceof Robot_SmallChatGroup) {
                File 文件 = new File(路径 + "/" + 聊天对象.讯友或群主.英语讯宝地址 + "#" + 聊天对象.小聊天群.编号 + ".sscj");
                if (文件.exists() && !文件.isDirectory()) {
                    RandomAccessFile 文件随机访问器 = new RandomAccessFile(文件.getAbsolutePath(), "r");
                    if (起始位置 < 0) {
                        文件随机访问器.seek(文件随机访问器.length());
                    } else {
                        文件随机访问器.seek(起始位置);
                    }
                    SSPackageReader SS包读取器;
                    do {
                        SS包读取器 = SharedMethod.读取一个SS包(文件随机访问器);
                        if (SS包读取器 == null) {
                            SS包读取器 = SharedMethod.读取一个SS包2(文件随机访问器);
                        }
                        if (SS包读取器 != null) {
                            if (!(boolean) SS包读取器.读取_有标签("删除")) {
                                一个讯宝 = new 讯宝_复合数据();
                                一个讯宝.收发者讯宝地址 = (String)SS包读取器.读取_有标签("发送者");
                                一个讯宝.发送序号 = (long)SS包读取器.读取_有标签("发送序号");
                                一个讯宝.指令 = (byte)SS包读取器.读取_有标签("指令");
                                Object 值 = SS包读取器.读取_有标签("文本");
                                if (值 != null) { 一个讯宝.文本 = (String)值; }
                                值 = SS包读取器.读取_有标签("宽度");
                                if (值 != null) { 一个讯宝.宽度 = (short)值; }
                                值 = SS包读取器.读取_有标签("高度");
                                if (值 != null) { 一个讯宝.高度 = (short)值; }
                                值 = SS包读取器.读取_有标签("秒数");
                                if (值 != null) {
                                    一个讯宝.秒数 = (byte)值;
                                    值 = SS包读取器.读取_有标签("已收听");
                                    if (值 != null) { 一个讯宝.已收听 = (boolean)值; }
                                }
                                一个讯宝.发送时间 = (long)SS包读取器.读取_有标签("发送时间");
                                讯宝[讯宝数量] = 一个讯宝;
                                讯宝数量 += 1;
                                if (讯宝数量 == 讯宝.length) {
                                    起始位置 = 文件随机访问器.getFilePointer();
                                    文件随机访问器.close();
                                    return 讯宝;
                                }
                            }
                        }
                    } while (SS包读取器 != null);
                    起始位置 = 文件随机访问器.getFilePointer();
                    文件随机访问器.close();
                }
            } else if (机器人 instanceof Robot_LargeChatGroup) {
                File 文件 = new File(路径 + "/" + 聊天对象.大聊天群.子域名 + "#" + 聊天对象.大聊天群.编号 + ".sscj");
                if (文件.exists() && !文件.isDirectory()) {
                    RandomAccessFile 文件随机访问器 = new RandomAccessFile(文件.getAbsolutePath(), "r");
                    if (起始位置 < 0) {
                        文件随机访问器.seek(文件随机访问器.length());
                    } else {
                        文件随机访问器.seek(起始位置);
                    }
                    SSPackageReader SS包读取器;
                    do {
                        SS包读取器 = SharedMethod.读取一个SS包(文件随机访问器);
                        if (SS包读取器 == null) {
                            SS包读取器 = SharedMethod.读取一个SS包2(文件随机访问器);
                        }
                        if (SS包读取器 != null) {
                            if (!(boolean) SS包读取器.读取_有标签("删除")) {
                                一个讯宝 = new 讯宝_复合数据();
                                一个讯宝.收发者讯宝地址 = (String)SS包读取器.读取_有标签("发送者");
                                一个讯宝.指令 = (byte)SS包读取器.读取_有标签("指令");
                                Object 值 = SS包读取器.读取_有标签("文本");
                                if (值 != null) { 一个讯宝.文本 = (String)值; }
                                值 = SS包读取器.读取_有标签("宽度");
                                if (值 != null) { 一个讯宝.宽度 = (short)值; }
                                值 = SS包读取器.读取_有标签("高度");
                                if (值 != null) { 一个讯宝.高度 = (short)值; }
                                值 = SS包读取器.读取_有标签("秒数");
                                if (值 != null) {
                                    一个讯宝.秒数 = (byte)值;
                                    值 = SS包读取器.读取_有标签("已收听");
                                    if (值 != null) { 一个讯宝.已收听 = (boolean)值; }
                                }
                                一个讯宝.发送时间 = (long)SS包读取器.读取_有标签("发送时间");
                                讯宝[讯宝数量] = 一个讯宝;
                                讯宝数量 += 1;
                                if (讯宝数量 == 讯宝.length) {
                                    if (起始位置 < 0) {
                                        结束位置 = 文件随机访问器.length();
                                    }
                                    起始位置 = 文件随机访问器.getFilePointer();
                                    文件随机访问器.close();
                                    return 讯宝;
                                }
                            }
                        }
                    } while (SS包读取器 != null);
                    if (起始位置 < 0) {
                        结束位置 = 文件随机访问器.length();
                    }
                    起始位置 = 文件随机访问器.getFilePointer();
                    文件随机访问器.close();
                }
            } else if (机器人 instanceof Robot_MainControl) {
                File 文件 = new File(路径 + "/strangers.sscj");
                if (文件.exists() && !文件.isDirectory()) {
                    RandomAccessFile 文件随机访问器 = new RandomAccessFile(文件.getAbsolutePath(), "r");
                    if (起始位置 < 0) {
                        文件随机访问器.seek(文件随机访问器.length());
                    } else {
                        文件随机访问器.seek(起始位置);
                    }
                    SSPackageReader SS包读取器;
                    do {
                        SS包读取器 = SharedMethod.读取一个SS包(文件随机访问器);
                        if (SS包读取器 == null) {
                            SS包读取器 = SharedMethod.读取一个SS包2(文件随机访问器);
                        }
                        if (SS包读取器 != null) {
                            if (!(boolean) SS包读取器.读取_有标签("删除")) {
                                一个讯宝 = new 讯宝_复合数据();
                                一个讯宝.收发者讯宝地址 = (String)SS包读取器.读取_有标签("讯宝地址");
                                一个讯宝.发送序号 = (long)SS包读取器.读取_有标签("发送序号");
                                一个讯宝.指令 = (byte)SS包读取器.读取_有标签("指令");
                                Object 值 = SS包读取器.读取_有标签("文本");
                                if (值 != null) { 一个讯宝.文本 = (String)值; }
                                一个讯宝.发送时间 = (long)SS包读取器.读取_有标签("发送时间");
                                讯宝[讯宝数量] = 一个讯宝;
                                讯宝数量 += 1;
                                if (讯宝数量 == 讯宝.length) {
                                    起始位置 = 文件随机访问器.getFilePointer();
                                    文件随机访问器.close();
                                    return 讯宝;
                                }
                            }
                        }
                    } while (SS包读取器 != null);
                    起始位置 = 文件随机访问器.getFilePointer();
                    文件随机访问器.close();
                }
            }
            if (讯宝数量 > 0) {
                if (讯宝数量 < 讯宝.length) {
                    讯宝_复合数据 讯宝2[] = new 讯宝_复合数据[讯宝数量];
                    System.arraycopy(讯宝, 0, 讯宝2, 0, 讯宝数量);
                    讯宝 = 讯宝2;
                }
                return 讯宝;
            } else {
                return null;
            }
        } catch (Exception e) {
            机器人.说(e.getMessage());
            return null;
        }
    }

    private 讯宝_复合数据[] 数据库_读取新讯宝() {
        讯宝_复合数据 讯宝[] = new 讯宝_复合数据[100];
        讯宝_复合数据 一个讯宝;
        int 讯宝数量 = 0;
        try {
            String 路径 = getActivity().getFilesDir().toString() + "/" + 当前用户.获取英语讯宝地址();
            File 文件 = new File(路径 + "/" + 聊天对象.大聊天群.子域名 + "#" + 聊天对象.大聊天群.编号 + ".sscj");
            if (文件.exists() && !文件.isDirectory()) {
                RandomAccessFile 文件随机访问器 = new RandomAccessFile(文件.getAbsolutePath(), "r");
                文件随机访问器.seek(文件随机访问器.length());
                SSPackageReader SS包读取器;
                do {
                    SS包读取器 = SharedMethod.读取一个SS包(文件随机访问器);
                    if (SS包读取器 == null) {
                        SS包读取器 = SharedMethod.读取一个SS包2(文件随机访问器);
                    }
                    if (SS包读取器 != null) {
                        if (!(boolean) SS包读取器.读取_有标签("删除")) {
                            一个讯宝 = new 讯宝_复合数据();
                            一个讯宝.收发者讯宝地址 = (String)SS包读取器.读取_有标签("发送者");
                            一个讯宝.指令 = (byte)SS包读取器.读取_有标签("指令");
                            Object 值 = SS包读取器.读取_有标签("文本");
                            if (值 != null) { 一个讯宝.文本 = (String)值; }
                            值 = SS包读取器.读取_有标签("宽度");
                            if (值 != null) { 一个讯宝.宽度 = (short)值; }
                            值 = SS包读取器.读取_有标签("高度");
                            if (值 != null) { 一个讯宝.高度 = (short)值; }
                            值 = SS包读取器.读取_有标签("秒数");
                            if (值 != null) {
                                一个讯宝.秒数 = (byte)值;
                                值 = SS包读取器.读取_有标签("已收听");
                                if (值 != null) { 一个讯宝.已收听 = (boolean)值; }
                            }
                            一个讯宝.发送时间 = (long)SS包读取器.读取_有标签("发送时间");
                            if (一个讯宝.发送时间 <= 聊天对象.大聊天群.最新讯宝的发送时间) {
                                break;
                            }
                            讯宝[讯宝数量] = 一个讯宝;
                            讯宝数量 += 1;
                            if (讯宝数量 == 讯宝.length) {
                                讯宝_复合数据 讯宝2[] = new 讯宝_复合数据[讯宝数量 * 2];
                                System.arraycopy(讯宝, 0, 讯宝2, 0, 讯宝数量);
                                讯宝 = 讯宝2;
                            }
                        }
                    }
                } while (SS包读取器 != null && 文件随机访问器.getFilePointer() > 结束位置);
                文件随机访问器.close();
            }
            if (讯宝数量 > 0) {
                if (讯宝数量 < 讯宝.length) {
                    讯宝_复合数据 讯宝2[] = new 讯宝_复合数据[讯宝数量];
                    System.arraycopy(讯宝, 0, 讯宝2, 0, 讯宝数量);
                    讯宝 = 讯宝2;
                }
                return 讯宝;
            } else {
                return null;
            }
        } catch (Exception e) {
            机器人.说(e.getMessage());
            return null;
        }
    }

    void 讯友说(String 讯宝地址, long 发送时间, long 发送序号, byte 讯宝指令, String 文本,
             short 宽度, short 高度, byte 秒数, boolean 已收听, String 时间提示文本) {
        if (SharedMethod.字符串未赋值或为空(文本)) {
            return;
        }
        String 头像路径, 主机名 = null;
        short 位置号 = -1;
        if (聊天对象.小聊天群 != null) {
            if (聊天对象.小聊天群.编号 == 0) {
                头像路径 = 获取头像路径(讯宝地址);
            } else {
                GroupMember 群成员[] = 聊天对象.小聊天群.群成员;
                if (群成员 != null) {
                    int i;
                    for (i = 0; i < 群成员.length; i++) {
                        if (群成员[i].英语讯宝地址.equals(讯宝地址)) {
                            break;
                        }
                    }
                    if (i < 群成员.length) {
                        GroupMember 某一成员 = 群成员[i];
                        头像路径 = ProtocolPath.获取讯友头像路径(讯宝地址, 某一成员.主机名, 0);
                        主机名 = 某一成员.主机名;
                        位置号 = 某一成员.位置号;
                    } else {
                        头像路径 = 获取头像路径(讯宝地址);
                    }
                } else {
                    头像路径 = 获取头像路径(讯宝地址);
                }
            }
            if (时间提示文本 == null) {
                时间提示文本 = 时间格式(new MyDate(发送时间));
            }
            String 段[] = 讯宝地址.split(ProtocolParameters.讯宝地址标识);
            switch (讯宝指令) {
                case ProtocolParameters.讯宝指令_发送文字:
                    发送JS("function(){ var who = \"" + 讯宝地址 + "\"; var index = \"" + 发送序号 + "\"; var text = \"" + 替换HTML和JS敏感字符(文本) + "\"; var iconsrc = \"" + 头像路径 + "\"; var time = \"" + 时间提示文本 + "\"; SSin_Text(who, index, text, iconsrc, time); }");
                    break;
                case ProtocolParameters.讯宝指令_发送图片:
                case ProtocolParameters.讯宝指令_发送语音:
                case ProtocolParameters.讯宝指令_发送短视频:
                    String 路径;
                    if (聊天对象.小聊天群.编号 == 0) {
                        路径 = ProtocolPath.获取传送服务器访问路径开头(聊天对象.讯友或群主.主机名, 段[1], true) + "Position=" + 聊天对象.讯友或群主.位置号 + "&EnglishSSAddress=" + 替换URI敏感字符(聊天对象.讯友或群主.英语讯宝地址) + "&FileName=" + 替换URI敏感字符(文本);
                    } else {
                        路径 = ProtocolPath.获取传送服务器访问路径开头(主机名, 段[1], true) + "Position=" + 位置号 + "&EnglishSSAddress=" + 替换URI敏感字符(讯宝地址) + "&FileName=" + 替换URI敏感字符(文本);
                    }
                    switch (讯宝指令) {
                        case ProtocolParameters.讯宝指令_发送图片:
                            发送JS("function(){ var who = \"" + 讯宝地址 + "\"; var imgsrc = \"" + SharedMethod.处理文件路径以用作JS函数参数(路径) + "\"; var iconsrc =\"" + 头像路径 + "\"; var time = \"" + 时间提示文本 + "\"; SSin_Img(who, \"" + 发送序号 + "\", imgsrc, \"" + 宽度 + "\", \"" + 高度 + "\", iconsrc, time); }");
                            break;
                        case ProtocolParameters.讯宝指令_发送语音:
                            String 文本2 = 界面文字.获取(258, "语音：#% 秒", new Object[] { 秒数});
                            发送JS("function(){var who = \"" + 讯宝地址 + "\"; var text = \"" + 替换HTML和JS敏感字符(文本2) + "\"; var voicesrc = \"" + SharedMethod.处理文件路径以用作JS函数参数(路径) + "\"; var iconsrc =\"" + 头像路径 + "\"; var time = \"" + 时间提示文本 + "\"; SSin_Voice(who, \"" + 发送序号 + "\", text, voicesrc, iconsrc, time" + (已收听? ", \"true\"":"") + "); }");
                            break;
                        case ProtocolParameters.讯宝指令_发送短视频:
                            发送JS("function(){ var who = \"" + 讯宝地址 + "\"; var videosrc = \"" + SharedMethod.处理文件路径以用作JS函数参数(路径) + "\"; var iconsrc =\"" + 头像路径 + "\"; var time = \"" + 时间提示文本 + "\"; SSin_Video(who, \"" + 发送序号 + "\", videosrc, \"" + 宽度 + "\", \"" + 高度 + "\", iconsrc, time); }");
                            break;
                    }
                    break;
                case ProtocolParameters.讯宝指令_发送文件:
                    try {
                        SSPackageReader SS包解读器2 = new SSPackageReader();
                        SS包解读器2.解读纯文本(文本);
                        String 原始文件名 = (String) SS包解读器2.读取_有标签("O");
                        String 存储文件名 = (String) SS包解读器2.读取_有标签("S");
                        if (聊天对象.小聊天群.编号 == 0) {
                            路径 = ProtocolPath.获取传送服务器访问路径开头(聊天对象.讯友或群主.主机名, 段[1], true) + "Position=" + 聊天对象.讯友或群主.位置号 + "&EnglishSSAddress=" + 替换URI敏感字符(聊天对象.讯友或群主.英语讯宝地址) + "&FileName=" + 替换URI敏感字符(存储文件名);
                        } else {
                            路径 = ProtocolPath.获取传送服务器访问路径开头(主机名, 段[1], true) + "Position=" + 位置号 + "&EnglishSSAddress=" + 替换URI敏感字符(讯宝地址) + "&FileName=" + 替换URI敏感字符(存储文件名);
                        }
                        发送JS("function(){var who = \"" + 讯宝地址 + "\"; var text = \"" + SharedMethod.处理文件路径以用作JS函数参数(原始文件名) + "\"; var filesrc = \"" + SharedMethod.处理文件路径以用作JS函数参数(路径) + "\"; var iconsrc =\"" + 头像路径 + "\"; var time = \"" + 时间提示文本 + "\"; SSin_File(who, \"" + 发送序号 + "\", text, filesrc, iconsrc, time); }");
                    } catch (Exception e) {
                    }
                    break;
                case ProtocolParameters.讯宝指令_邀请加入小聊天群:
                    SSPackageReader SS包解读器2 = new SSPackageReader();
                    byte 小聊天群编号;
                    String 群名称;
                    try {
                        SS包解读器2.解读纯文本(文本);
                        小聊天群编号 = (byte) SS包解读器2.读取_有标签("I");
                        群名称 = (String) SS包解读器2.读取_有标签("N");
                    } catch (Exception e) {
                        机器人.说(e.getMessage());
                        return;
                    }
                    文本 = 替换HTML和JS敏感字符(界面文字.获取(172, "我创建了一个小聊天群[#%]，希望你加入。", new Object[] {群名称})) + "&nbsp;<span class='TaskName' onclick='ToRobot2(\\\"JoinSmallGroup\\\", \\\"" + 群名称 + "\\\", \\\"" + 小聊天群编号 + "\\\")'>" + 界面文字.获取(173, "加入") + "</span>";
                    发送JS("function(){ var who = \"" + 讯宝地址 + "\"; var index = \"" + 发送序号 + "\"; var text = \"" + 文本 + "\"; var iconsrc = \"" + 头像路径 + "\"; var time = \"" + 时间提示文本 + "\"; SSin_Text(who, index, text, iconsrc, time); }");
                    break;
                case ProtocolParameters.讯宝指令_邀请加入大聊天群:
                    SS包解读器2 = new SSPackageReader();
                    String 子域名;
                    long 大聊天群编号;
                    try {
                        SS包解读器2.解读纯文本(文本);
                        子域名 = (String) SS包解读器2.读取_有标签("D");
                        大聊天群编号 = (long) SS包解读器2.读取_有标签("I");
                        群名称 = (String) SS包解读器2.读取_有标签("N");
                    } catch (Exception e) {
                        机器人.说(e.getMessage());
                        return;
                    }
                    文本 = 界面文字.获取(289, "我邀请你加入大聊天群[#%]。", new Object[] {替换HTML和JS敏感字符(群名称)}) + "&nbsp;<span class='TaskName' onclick='ToRobot2(\\\"JoinLargeGroup\\\", \\\"" + 子域名 + "\\\", \\\"" + 大聊天群编号 + "\\\")'>" + 界面文字.获取(173, "加入") + "</span>";
                    发送JS("function(){ var who = \"" + 讯宝地址 + "\"; var index = \"" + 发送序号 + "\"; var text = \"" + 文本 + "\"; var iconsrc = \"" + 头像路径 + "\"; var time = \"" + 时间提示文本 + "\"; SSin_Text(who, index, text, iconsrc, time); }");
                    break;
            }
        } else if (聊天对象.大聊天群 != null) {
            Contact 讯友 = 当前用户.查找讯友(讯宝地址);
            if (讯友 != null) {
                头像路径 = ProtocolPath.获取讯友头像路径(讯宝地址, 讯友.主机名, 0);
            } else {
                主机名 = 数据库_获取主机名(讯宝地址);
                if (!SharedMethod.字符串未赋值或为空(主机名)) {
                    头像路径 = ProtocolPath.获取讯友头像路径(讯宝地址, 主机名, 0);
                } else {
                    头像路径 = ProtocolPath.获取陌生人头像路径();
                }
            }
            if (时间提示文本 == null) {
                时间提示文本 = 时间格式(new MyDate(发送时间));
            }
            switch (讯宝指令) {
                case ProtocolParameters.讯宝指令_发送文字:
                    发送JS("function(){ var who = \"" + 讯宝地址 + "\"; var index = \"" + 发送时间 + "\"; var text = \"" + 替换HTML和JS敏感字符(文本) + "\"; var iconsrc = \"" + 头像路径 + "\"; var time = \"" + 时间提示文本 + "\"; SSin_Text(who, index, text, iconsrc, time); }");
                    break;
                case ProtocolParameters.讯宝指令_发送图片:
                case ProtocolParameters.讯宝指令_发送语音:
                case ProtocolParameters.讯宝指令_发送短视频:
                    String 路径 = ProtocolPath.获取大聊天群服务器访问路径开头(聊天对象.大聊天群.子域名, true) + "GroupID=" + 聊天对象.大聊天群.编号 + "&FileName=" + 替换URI敏感字符(文本);
                    switch (讯宝指令) {
                        case ProtocolParameters.讯宝指令_发送图片:
                            发送JS("function(){ var who = \"" + 讯宝地址 + "\"; var imgsrc = \"" + SharedMethod.处理文件路径以用作JS函数参数(路径) + "\"; var iconsrc =\"" + 头像路径 + "\"; var time = \"" + 时间提示文本 + "\"; SSin_Img(who, \"" + 发送时间 + "\", imgsrc, \"" + 宽度 + "\", \"" + 高度 + "\", iconsrc, time); }");
                            break;
                        case ProtocolParameters.讯宝指令_发送语音:
                            String 文本2 = 界面文字.获取(258, "语音：#% 秒", new Object[] { 秒数});
                            发送JS("function(){var who = \"" + 讯宝地址 + "\"; var text = \"" + 替换HTML和JS敏感字符(文本2) + "\"; var voicesrc = \"" + SharedMethod.处理文件路径以用作JS函数参数(路径) + "\"; var iconsrc =\"" + 头像路径 + "\"; var time = \"" + 时间提示文本 + "\"; SSin_Voice(who, \"" + 发送时间 + "\", text, voicesrc, iconsrc, time" + (已收听? ", \"true\"":"") + "); }");
                            break;
                        case ProtocolParameters.讯宝指令_发送短视频:
                            发送JS("function(){ var who = \"" + 讯宝地址 + "\"; var videosrc = \"" + SharedMethod.处理文件路径以用作JS函数参数(路径) + "\"; var iconsrc =\"" + 头像路径 + "\"; var time = \"" + 时间提示文本 + "\"; SSin_Video(who, \"" + 发送时间 + "\", videosrc, \"" + 宽度 + "\", \"" + 高度 + "\", iconsrc, time); }");
                            break;
                    }
                    break;
                case ProtocolParameters.讯宝指令_发送文件:
                    try {
                        SSPackageReader SS包解读器2 = new SSPackageReader();
                        SS包解读器2.解读纯文本(文本);
                        String 原始文件名 = (String) SS包解读器2.读取_有标签("O");
                        String 存储文件名 = (String) SS包解读器2.读取_有标签("S");
                        路径 = ProtocolPath.获取大聊天群服务器访问路径开头(聊天对象.大聊天群.子域名, true) + "GroupID=" + 聊天对象.大聊天群.编号 + "&FileName=" + 替换URI敏感字符(存储文件名);
                        发送JS("function(){var who = \"" + 讯宝地址 + "\"; var text = \"" + SharedMethod.处理文件路径以用作JS函数参数(原始文件名) + "\"; var filesrc = \"" + SharedMethod.处理文件路径以用作JS函数参数(路径) + "\"; var iconsrc =\"" + 头像路径 + "\"; var time = \"" + 时间提示文本 + "\"; SSin_File(who, \"" + 发送序号 + "\", text, filesrc, iconsrc, time); }");
                    } catch (Exception e) {
                    }
                    break;
            }
        }
    }

    private String 获取头像路径(String 讯宝地址) {
        Contact 讯友目录[] = 当前用户.讯友目录;
        if (讯友目录 != null) {
            int i;
            for (i = 0; i < 讯友目录.length; i++) {
                if (讯友目录[i].英语讯宝地址.equals(讯宝地址)) {
                    break;
                }
            }
            if (i < 讯友目录.length) {
                Contact 某一讯友 = 讯友目录[i];
                return ProtocolPath.获取讯友头像路径(某一讯友.英语讯宝地址, 某一讯友.主机名, 某一讯友.头像更新时间);
            } else {
                return ProtocolPath.获取陌生人头像路径();
            }
        } else {
            return ProtocolPath.获取陌生人头像路径();
        }
    }

    private String 数据库_获取主机名(String 英语讯宝地址) {
        HostName 主机名数组[] = null;
        if (当前用户.主机名数组 == null) {
            byte 字节数组[] = null;
            try {
                File 文件 = new File(getActivity().getFilesDir().toString() + "/" + 当前用户.获取英语讯宝地址() + "/hostname.txt");
                if (文件.exists()) {
                    字节数组 = SharedMethod.读取文件的全部字节(文件.getAbsolutePath());
                    if (字节数组 != null) {
                        String 文本 = new String(字节数组, "UTF8");
                        String 行[] = 文本.split(";");
                        主机名数组 = new HostName[行.length * 2];
                        for (int i = 0; i < 行.length; i++) {
                            String 段[] = 行[i].split("#");
                            if (段.length == 2) {
                                主机名数组[i] = new HostName();
                                主机名数组[i].英语讯宝地址 = 段[0];
                                主机名数组[i].主机名 = 段[1];
                            }
                        }
                        当前用户.主机名数组 = 主机名数组;
                        当前用户.主机名数量 = 行.length;
                    }
                }
            } catch (Exception e) {
            }
            if (主机名数组 == null) {
                主机名数组 = new HostName[5];
                当前用户.主机名数组 = 主机名数组;
            }
        } else {
            主机名数组 = 当前用户.主机名数组;
        }
        for (int i = 0; i < 当前用户.主机名数量; i++) {
            if (主机名数组[i].英语讯宝地址.equals(英语讯宝地址)) {
                return 主机名数组[i].主机名;
            }
        }
        return "";
    }

    void 陌生人说(String 讯宝地址, long 发送时间, long 发送序号, byte 讯宝指令, String 文本, String 时间提示文本) {
        if (SharedMethod.字符串未赋值或为空(文本)) {
            return;
        }
        if (时间提示文本 == null) {
            时间提示文本 = 时间格式(new MyDate(发送时间));
        }
        if (讯宝指令 == ProtocolParameters.讯宝指令_邀请加入大聊天群) {
            try {
                SSPackageReader SS包解读器2 = new SSPackageReader();
                SS包解读器2.解读纯文本(文本);
                String 子域名 = (String)SS包解读器2.读取_有标签("D");
                long 大聊天群编号 = (long)SS包解读器2.读取_有标签("I");
                String 群名称 = (String)SS包解读器2.读取_有标签("N");
                文本 = 界面文字.获取(289, "我邀请你加入大聊天群[#%]。", new Object[] {SharedMethod.替换HTML和JS敏感字符(群名称)}) + "&nbsp;<span class='TaskName' onclick='ToRobot2(\\\"JoinLargeGroup\\\", \\\"" + 子域名 + "\\\", \\\"" + 大聊天群编号 + "\\\")'>" + 界面文字.获取(173, "加入") + "</span>";
            } catch (Exception e) {
                return;
            }
        } else {
            文本 = SharedMethod.替换HTML和JS敏感字符(文本);
        }
        String 陌生人;
        Contact 讯友 = 当前用户.查找讯友(讯宝地址);
        if (讯友 == null) {
            陌生人 = 界面文字.获取(76, "陌生人") + "&nbsp;" + 讯宝地址 + "<br><span class='TaskName' onclick='ToRobot2(\\\"AddContact\\\", \\\"" + 讯宝地址 + "\\\")'>" + 界面文字.获取(35, "添加为讯友") + "</span>&nbsp;<span class='TaskName' onclick='ToRobot2(\\\"Block\\\", \\\"" + 讯宝地址 + "\\\")'>" + 界面文字.获取(60, "添加至黑名单") + "</span>";
        } else if (!讯友.拉黑) {
            陌生人 = "[" + 界面文字.获取(58, "已成为讯友") + "]";
        } else {
            陌生人 = "[" + 界面文字.获取(43, "已拉黑") + "]";
        }
        发送JS("function(){ var who = \"" + 讯宝地址 + "\"; var index = \"" + 发送序号 + "\"; var text = \"" + 替换HTML和JS敏感字符(文本) + "\"; var iconsrc = \"" + ProtocolPath.获取陌生人头像路径() + "\"; var time = \"" + 时间提示文本 + "\"; SSin_Text(who, index, text, iconsrc, time, \"" + 陌生人 + "\"); }");
    }

    void 显示同步的讯宝(long 发送时间, long 发送序号, byte 讯宝指令, String 文本, short 宽度, short 高度, byte 秒数) {
        try {
            switch (讯宝指令) {
                case ProtocolParameters.讯宝指令_发送文字:
                    发送JS("function(){var text = \"" + 替换HTML和JS敏感字符(文本) + "\"; var iconsrc =\"" + ProtocolPath.获取我的头像路径(当前用户.英语用户名, 当前用户.主机名, 当前用户.头像更新时间, 当前用户.域名_英语) + "\"; var time = \"" + 时间格式(new MyDate(发送时间)) + "\"; SSout_Text(\"" + 发送时间 + "\", text, iconsrc, time); }");
                    break;
                case ProtocolParameters.讯宝指令_发送图片:
                case ProtocolParameters.讯宝指令_发送语音:
                case ProtocolParameters.讯宝指令_发送短视频:
                    String 路径 = ProtocolPath.获取传送服务器访问路径开头(当前用户.主机名, 当前用户.域名_英语, true) + "Position=" + 当前用户.位置号 + "&EnglishSSAddress=" + 替换URI敏感字符(当前用户.获取英语讯宝地址()) + "&FileName=" + 替换URI敏感字符(文本);
                    switch (讯宝指令) {
                        case ProtocolParameters.讯宝指令_发送图片:
                            发送JS("function(){var imgsrc = \"" + SharedMethod.处理文件路径以用作JS函数参数(路径) + "\"; var iconsrc =\"" + ProtocolPath.获取我的头像路径(当前用户.英语用户名, 当前用户.主机名, 当前用户.头像更新时间, 当前用户.域名_英语) + "\"; var time = \"" + 时间格式(new MyDate(发送时间)) + "\"; SSout_Img(\"" + 发送时间 + "\", imgsrc, \"" + 宽度 + "\", \"" + 高度 + "\", iconsrc, time); }");
                            break;
                        case ProtocolParameters.讯宝指令_发送语音:
                            String 文本2 = 界面文字.获取(258, "语音：#% 秒", new Object[] { 秒数});
                            发送JS("function(){var text = \"" + 替换HTML和JS敏感字符(文本2) + "\"; var voicesrc = \"" + SharedMethod.处理文件路径以用作JS函数参数(路径) + "\"; var iconsrc =\"" + ProtocolPath.获取我的头像路径(当前用户.英语用户名, 当前用户.主机名, 当前用户.头像更新时间, 当前用户.域名_英语) + "\"; var time = \"" + 时间格式(new MyDate(发送时间)) + "\"; SSout_Voice(\"" + 发送时间 + "\", text, voicesrc, iconsrc, time); }");
                            break;
                        case ProtocolParameters.讯宝指令_发送短视频:
                            发送JS("function(){var videosrc = \"" + SharedMethod.处理文件路径以用作JS函数参数(路径) + "\"; var iconsrc =\"" + ProtocolPath.获取我的头像路径(当前用户.英语用户名, 当前用户.主机名, 当前用户.头像更新时间, 当前用户.域名_英语) + "\"; var time = \"" + 时间格式(new MyDate(发送时间)) + "\"; SSout_Video(\"" + 发送时间 + "\", videosrc, \"" + 宽度 + "\", \"" + 高度 + "\", iconsrc, time); }");
                            break;
                    }
                    break;
                case ProtocolParameters.讯宝指令_发送文件:
                    SSPackageReader SS包解读器2 = new SSPackageReader();
                    SS包解读器2.解读纯文本(文本);
                    String 原始文件名 = (String)SS包解读器2.读取_有标签("O");
                    String 存储文件名 = (String)SS包解读器2.读取_有标签("S");
                    路径 = ProtocolPath.获取传送服务器访问路径开头(当前用户.主机名, 当前用户.域名_英语, true) + "Position=" + 当前用户.位置号 + "&EnglishSSAddress=" + 替换URI敏感字符(当前用户.获取英语讯宝地址()) + "&FileName=" + 替换URI敏感字符(存储文件名);
                    发送JS("function(){var text = \"" + SharedMethod.处理文件路径以用作JS函数参数(原始文件名) + "\"; var filesrc = \"" + 路径 + "\"; var iconsrc =\"" + ProtocolPath.获取我的头像路径(当前用户.英语用户名, 当前用户.主机名, 当前用户.头像更新时间, 当前用户.域名_英语) + "\"; var time = \"" + 时间格式(new MyDate(发送时间)) + "\"; SSout_File(\"" + 发送时间 + "\", text, filesrc, iconsrc, time); }");
                    break;
            }
        } catch (Exception e) {
        }
    }

    void 加载大聊天群新讯宝(long 最新讯宝的时刻) {
        讯宝_复合数据 讯宝[] = 数据库_读取新讯宝();
        讯宝_复合数据 某一讯宝;
        if (讯宝 != null) {
            if (聊天对象.大聊天群.最新讯宝的发送时间 < 讯宝[0].发送时间) {
                聊天对象.大聊天群.最新讯宝的发送时间 = 讯宝[0].发送时间;
            }
            发送JS("function(){ LoadLaterStart(); }");
            String 英语讯宝地址 = 当前用户.获取英语讯宝地址();
            int i;
            long 发送时间;
            String 时间提示文本;
            final int 六万毫秒 = 60 * 1000;
            for (i = 讯宝.length - 1; i >= 0; i--) {
                某一讯宝 = 讯宝[i];
                发送时间 = ProtocolMethods.转换成Java相对时间2(某一讯宝.发送时间);
                if (i > 0) {
                    if (Math.abs(某一讯宝.发送时间 - 讯宝[i - 1].发送时间) > 六万毫秒 ) {
                        时间提示文本 = 时间格式(new MyDate(发送时间));
                    } else {
                        时间提示文本 = "";    //不能赋值为 null
                    }
                } else {
                    时间提示文本 = 时间格式(new MyDate(发送时间));
                }
                if (!某一讯宝.收发者讯宝地址.equals(英语讯宝地址)) {
                    讯友说(某一讯宝.收发者讯宝地址, 某一讯宝.发送时间, 某一讯宝.发送序号, 某一讯宝.指令, 某一讯宝.文本, 某一讯宝.宽度, 某一讯宝.高度, 某一讯宝.秒数, 某一讯宝.已收听, 时间提示文本);
                } else {
                    switch (某一讯宝.指令) {
                        case ProtocolParameters.讯宝指令_发送文字:
                            发送JS("function(){var text = \"" + 替换HTML和JS敏感字符(某一讯宝.文本) + "\"; var iconsrc =\"" + ProtocolPath.获取我的头像路径(当前用户.英语用户名, 当前用户.主机名, 当前用户.头像更新时间, 当前用户.域名_英语) + "\"; var time = \"" + 时间提示文本 + "\"; SSout_Text(\"" + 某一讯宝.发送时间 + "\", text, iconsrc, time); }");
                            break;
                        case ProtocolParameters.讯宝指令_发送图片:
                        case ProtocolParameters.讯宝指令_发送语音:
                        case ProtocolParameters.讯宝指令_发送短视频:
                            String 路径;
                            if (!某一讯宝.文本.contains(ProtocolParameters.特征字符_下划线)) {
                                路径 = SharedMethod.处理文件路径以用作JS函数参数(某一讯宝.文本);
                            } else {
                                路径 = ProtocolPath.获取大聊天群服务器访问路径开头(聊天对象.大聊天群.子域名, true) + "GroupID=" + 聊天对象.大聊天群.编号 + "&FileName=" + 替换URI敏感字符(某一讯宝.文本);
                            }
                            switch (某一讯宝.指令) {
                                case ProtocolParameters.讯宝指令_发送图片:
                                    发送JS("function(){var imgsrc = \"" + 路径 + "\"; var iconsrc =\"" + ProtocolPath.获取我的头像路径(当前用户.英语用户名, 当前用户.主机名, 当前用户.头像更新时间, 当前用户.域名_英语) + "\"; var time = \"" + 时间提示文本 + "\"; SSout_Img(\"" + 某一讯宝.发送时间 + "\", imgsrc, \"" + 某一讯宝.宽度 + "\", \"" + 某一讯宝.高度 + "\", iconsrc, time, \"true\"); }");
                                    break;
                                case ProtocolParameters.讯宝指令_发送语音:
                                    String 文本 = 界面文字.获取(258, "语音：#% 秒", new Object[]{某一讯宝.秒数});
                                    发送JS("function(){var text = \"" + 替换HTML和JS敏感字符(文本) + "\"; var voicesrc = \"" + 路径 + "\"; var iconsrc =\"" + ProtocolPath.获取我的头像路径(当前用户.英语用户名, 当前用户.主机名, 当前用户.头像更新时间, 当前用户.域名_英语) + "\"; var time = \"" + 时间提示文本 + "\"; SSout_Voice(\"" + 某一讯宝.发送时间 + "\", text, voicesrc, iconsrc, time); }");
                                    break;
                                case ProtocolParameters.讯宝指令_发送短视频:
                                    发送JS("function(){var videosrc = \"" + 路径 + "\"; var iconsrc =\"" + ProtocolPath.获取我的头像路径(当前用户.英语用户名, 当前用户.主机名, 当前用户.头像更新时间, 当前用户.域名_英语) + "\"; var time = \"" + 时间提示文本 + "\"; SSout_Video(\"" + 某一讯宝.发送时间 + "\", videosrc, \"" + 某一讯宝.宽度 + "\", \"" + 某一讯宝.高度 + "\", iconsrc, time); }");
                                    break;
                            }
                            break;
                        case ProtocolParameters.讯宝指令_发送文件:
                            String 原始文件名;
                            if (!某一讯宝.文本.startsWith(ProtocolParameters.SS包标识_纯文本)) {
                                原始文件名 = SharedMethod.获取文件名(某一讯宝.文本);
                                路径 = SharedMethod.处理文件路径以用作JS函数参数(某一讯宝.文本);
                            } else {
                                try {
                                    SSPackageReader SS包解读器2 = new SSPackageReader();
                                    SS包解读器2.解读纯文本(某一讯宝.文本);
                                    原始文件名 = (String)SS包解读器2.读取_有标签("O");
                                    String 存储文件名 = (String)SS包解读器2.读取_有标签("S");
                                    路径 = ProtocolPath.获取大聊天群服务器访问路径开头(聊天对象.大聊天群.子域名, true) + "GroupID=" + 聊天对象.大聊天群.编号 + "&FileName=" + 替换URI敏感字符(存储文件名);
                                } catch (Exception e) {
                                    continue;
                                }
                            }
                            发送JS("function(){var text = \"" + SharedMethod.处理文件路径以用作JS函数参数(原始文件名) + "\"; var filesrc = \"" + 路径 + "\"; var iconsrc =\"" + ProtocolPath.获取我的头像路径(当前用户.英语用户名, 当前用户.主机名, 当前用户.头像更新时间, 当前用户.域名_英语) + "\"; var time = \"" + 时间格式(new MyDate(某一讯宝.发送时间)) + "\"; SSout_File(\"" + 某一讯宝.发送时间 + "\", text, filesrc, iconsrc, time); }");
                            break;
                    }
                }
            }
            发送JS("function(){ LoadEnd(true); }");
        }
        if (聊天对象.大聊天群.最新讯宝的发送时间 < 最新讯宝的时刻) {
            聊天对象.大聊天群.最新讯宝的发送时间 = 最新讯宝的时刻;
        }
    }

    @Override
    public void onClick(View 控件) {
        switch (控件.getId()) {
            case R.id.按钮_说话:
                关闭键盘();
                说话();
                break;
            case R.id.按钮_刷新:
                ((Robot_LargeChatGroup)机器人).刷新();
                break;
            case R.id.图片_机器人:
                关闭键盘();
                ((Activity_Main)getActivity()).弹出任务名称列表();
                break;
            default:
                LinearLayout 说话对象的容器 = (LinearLayout) 第一控件.findViewById(R.id.说话对象的容器);
                说话对象的容器.removeView(控件);
                int i, 字数 = 0;
                TextView 文字控件;
                for (i = 0; i < 说话对象的容器.getChildCount(); i++) {
                    文字控件 = (TextView)说话对象的容器.getChildAt(i);
                    字数 += 文字控件.getText().toString().length() + 2;
                }
                if (字数 > 0) {
                    InputFilter 输入过滤器[] = {new InputFilter.LengthFilter(ProtocolParameters.最大值_讯宝文本长度 - 字数 - 20)};
                    输入框.setFilters(输入过滤器);
                } else {
                    InputFilter 输入过滤器[] = {new InputFilter.LengthFilter(ProtocolParameters.最大值_讯宝文本长度)};
                    输入框.setFilters(输入过滤器);
                }
        }
    }

    void 点击群成员(String who) {
        if (SharedMethod.字符串未赋值或为空(who)) {
            return;
        }
        if ((聊天对象.小聊天群 != null && 聊天对象.小聊天群.编号 > 0) || 聊天对象.大聊天群 != null) {
            String 段[] = who.split("/");
            String 用户英语讯宝地址 = 当前用户.获取英语讯宝地址();
            int i;
            for (i = 0; i < 段.length; i++) {
                if (段[i].trim().equals(用户英语讯宝地址)) {
                    return;
                }
            }
            LinearLayout 说话对象的容器 = (LinearLayout) 第一控件.findViewById(R.id.说话对象的容器);
            if (说话对象的容器.getChildCount() >= 10) {
                return;
            }
            TextView 文字控件;
            for (i = 0; i < 说话对象的容器.getChildCount(); i++) {
                文字控件 = (TextView)说话对象的容器.getChildAt(i);
                if (文字控件.getText().toString().equals(who)) {
                    return;
                }
            }
            文字控件 = new TextView(getActivity());
            文字控件.setText(who);
            文字控件.setTextColor(Color.RED);
            文字控件.setPadding(5, 10, 5, 10);
            说话对象的容器.addView(文字控件);
            文字控件.setOnClickListener(this);
            int 字数 = 0;
            for (i = 0; i < 说话对象的容器.getChildCount(); i++) {
                文字控件 = (TextView)说话对象的容器.getChildAt(i);
                字数 += 文字控件.getText().toString().length() + 2;
            }
            if (字数 > 0) {
                InputFilter 输入过滤器[] = {new InputFilter.LengthFilter(ProtocolParameters.最大值_讯宝文本长度 - 字数 - 20)};
                输入框.setFilters(输入过滤器);
            } else {
                InputFilter 输入过滤器[] = {new InputFilter.LengthFilter(ProtocolParameters.最大值_讯宝文本长度)};
                输入框.setFilters(输入过滤器);
            }
        }
    }

    private void 说话() {
//        计数 = 100;
//        定时任务2 = new MyTimerTask(跨线程调用器, 10);
//        if (定时器_录音 == null) {
//            定时器_录音 = new Timer();
//        }
//        定时器_录音.schedule(定时任务2, 2000, 2000);
//        return;

        String 文本 = 输入框.getText().toString().trim();
        输入框.setText("");
        if (!(机器人 instanceof Robot_LargeChatGroup)) {
            if (SharedMethod.字符串未赋值或为空(文本)) { return; }
            LinearLayout 说话对象的容器 = (LinearLayout) 第一控件.findViewById(R.id.说话对象的容器);
            if (说话对象的容器.getChildCount() > 0) {
                StringBuffer 字符串合并器 = new StringBuffer(100 * 说话对象的容器.getChildCount());
                for (int i = 0; i < 说话对象的容器.getChildCount(); i++) {
                    TextView 文字控件 = (TextView)说话对象的容器.getChildAt(i);
                    字符串合并器.append(文字控件.getText().toString() + "\n");
                }
                文本 = "★★★\n" + 字符串合并器.toString() + "\n" + 文本;
            }
            对机器人说(文本, true, false);
        } else {
            if (SharedMethod.字符串未赋值或为空(文本)) {
                ((Robot_LargeChatGroup) 机器人).刷新();
            } else {
                LinearLayout 说话对象的容器 = (LinearLayout) 第一控件.findViewById(R.id.说话对象的容器);
                if (说话对象的容器.getChildCount() > 0) {
                    StringBuffer 字符串合并器 = new StringBuffer(100 * 说话对象的容器.getChildCount());
                    for (int i = 0; i < 说话对象的容器.getChildCount(); i++) {
                        TextView 文字控件 = (TextView)说话对象的容器.getChildAt(i);
                        字符串合并器.append(文字控件.getText().toString() + "\n");
                    }
                    文本 = "★★★\n" + 字符串合并器.toString() + "\n" + 文本;
                }
                对机器人说(文本, true, false);
            }
        }
    }

    @Override
    public boolean onTouch(View 控件, MotionEvent motionEvent) {
        if (!发送语音) { return false; }
        switch (控件.getId()) {
            case R.id.文字_开始录音:
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        按下位置Y = motionEvent.getY();
                        try {
                            媒体录制器 = new MediaRecorder();
                            媒体录制器.reset();
                            File 文件 = new File(getActivity().getCacheDir().toString() + "/record.amr");
                            if (文件.exists()) {
                                文件.delete();
                            }
                            文件.createNewFile();
                            媒体录制器.setAudioSource(MediaRecorder.AudioSource.MIC);
                            媒体录制器.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
                            媒体录制器.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                            媒体录制器.setAudioChannels(1);
                            媒体录制器.setOutputFile(文件.getAbsolutePath());
                            媒体录制器.prepare();
                            if (定时任务_录音 != null) {
                                定时任务_录音.cancel();
                            }
                            录音剩余秒数 = ProtocolParameters.最大值_语音时长_秒;

                            定时任务_录音 = new MyTimerTask(跨线程调用器, 5);
                            媒体录制器.start();
                            if (定时器_录音 == null) {
                                定时器_录音 = new Timer();
                            }
                            定时器_录音.schedule(定时任务_录音, 100, 100);
                            TextView 文字控件 = (TextView) 第一控件.findViewById(R.id.文字_开始录音);
                            文字控件.setText(界面文字.获取(261, "正在录音") + "\n" + 界面文字.获取(260, "上滑，取消录音"));
                            文字控件.setTextColor(Color.RED);
                        } catch (Exception e) {
                            机器人.说(e.getMessage());
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        if (定时任务_录音 != null) {
                            if ((按下位置Y - motionEvent.getY()) < 50.0) {
                                停止录音(false);
                            } else {
                                停止录音(true);
                            }
                        }
                        break;
                }
                break;
        }
        return true;
    }

    private void 停止录音(boolean 取消) {
        if (媒体录制器 != null) {
            媒体录制器.stop();
            媒体录制器.release();
            媒体录制器 = null;
        }
        定时任务_录音.cancel();
        定时任务_录音 = null;
        TextView 文字控件 = (TextView) 第一控件.findViewById(R.id.文字_开始录音);
        文字控件.setText(界面文字.获取(259, "按住，开始录音") + "\n" + 界面文字.获取(260, "上滑，取消录音"));
        文字控件.setTextColor(Color.BLACK);
        if (取消) {
            return;
        }
        float 录音时间长度 = ((float) ProtocolParameters.最大值_语音时长_秒) - 录音剩余秒数;
        if (录音时间长度 < 1.0) {
            return;
        }
        String 路径 = getActivity().getFilesDir().toString() + "/" + 当前用户.获取英语讯宝地址();
        File 目录 = new File(路径);
        if (!目录.exists() || !目录.isDirectory()) {
            目录.mkdir();
        }
        路径 += "/" + SharedMethod.生成大小写英文字母与数字的随机字符串(20) + ".amr";
        File 目标文件 = new File(路径);
        if (目标文件.exists()) {目标文件.delete();}
        File 文件 = new File(getActivity().getCacheDir().toString() + "/record.amr");
        if (文件.renameTo(目标文件)) {
            String 文本 = 界面文字.获取(258, "语音：#% 秒", new Object[] { (byte)录音时间长度});
            long 当前UTC时刻 = SharedMethod.获取当前UTC时间();
            发送JS("function(){var text = \"" + 替换HTML和JS敏感字符(文本) + "\"; var voicesrc = \"" + SharedMethod.处理文件路径以用作JS函数参数(目标文件.getAbsolutePath()) + "\"; var iconsrc =\"" + ProtocolPath.获取我的头像路径(当前用户.英语用户名, 当前用户.主机名, 当前用户.头像更新时间, 当前用户.域名_英语) + "\"; var time = \"" + 时间格式(new MyDate(当前UTC时刻)) + "\"; SSout_Voice(\"" + 当前UTC时刻 + "\", text, voicesrc, iconsrc, time); }");
            Contact 当前讯友 = 聊天对象.讯友或群主;
            if (聊天对象.小聊天群 != null) {
                if (聊天对象.小聊天群.编号 == 0) {
                    if (主控机器人.数据库_保存要发送的一对一讯宝( 当前讯友.英语讯宝地址, 当前UTC时刻, ProtocolParameters.讯宝指令_发送语音, 目标文件.getAbsolutePath(), (short)0, (short)0, (byte)录音时间长度)) {
                        if (主控机器人.数据库_更新最近互动讯友排名(当前讯友.英语讯宝地址, (byte) 0)) {
                            if (当前用户.讯友录当前显示范围 == Constants.讯友录显示范围_最近) {
                                Fragment_Main.主窗体.刷新讯友录();
                            }
                        }
                        主控机器人.发送讯宝(false);
                    }
                } else {
                    if (主控机器人.数据库_保存要发送的小聊天群讯宝( 当前讯友.英语讯宝地址, 聊天对象.小聊天群.编号, 当前UTC时刻, ProtocolParameters.讯宝指令_发送语音, 目标文件.getAbsolutePath(), (short)0, (short)0, (byte)录音时间长度)) {
                        if (主控机器人.数据库_更新最近互动讯友排名(当前讯友.英语讯宝地址, 聊天对象.小聊天群.编号)) {
                            if (当前用户.讯友录当前显示范围 == Constants.讯友录显示范围_最近) {
                                Fragment_Main.主窗体.刷新讯友录();
                            }
                        }
                        主控机器人.发送讯宝(false);
                    }
                }
            } else if (聊天对象.大聊天群 != null) {
                byte 字节数组[] = null;
                try {
                    字节数组 = SharedMethod.读取文件的全部字节(路径);
                } catch (Exception e) {
                    机器人.说(e.getMessage());
                    return;
                }
                ((Robot_LargeChatGroup) 机器人).发送或接收(ProtocolParameters.讯宝指令_发送语音, SharedMethod.获取扩展名(路径), (short) 0, (short) 0, (byte) 录音时间长度, 字节数组, 当前UTC时刻, 路径, null);
                if (主控机器人.数据库_更新最近互动讯友排名(聊天对象.大聊天群.子域名, 聊天对象.大聊天群.编号)) {
                    if (当前用户.讯友录当前显示范围 == Constants.讯友录显示范围_最近) {
                        Fragment_Main.主窗体.刷新讯友录();
                    }
                }
            }
        }
    }

    void 播音开始(String VoiceID) {
        发送JS("function(){ VoiceStarted('" + VoiceID + "'); }");
    }

    private void 播放语音(Fragment_Chating 聊天控件, String 文件路径, String VoiceID, String IsNew) {
        if (播音类 == null) {
            播音类 = new Audio_Playing(getActivity().getApplicationContext(), this);
        } else if (播音类.正在播放()) {
            播音类.停止播放();
            播音完毕();
            if (文件路径.equals(播音类.原始路径)) {
                return;
            }
        }
        if (播音类.开始播放AMR(文件路径, true)) {
            聊天控件.播音开始(VoiceID);
            if (IsNew.equalsIgnoreCase("true")) {
                数据库_标为已收听(聊天控件, VoiceID);
            }
        }
    }

    private void 数据库_标为已收听(Fragment_Chating 聊天控件, String VoiceID) {
        String 段[] = VoiceID.split(":");
        if (段.length != 3) {
            return;
        }
        try {
            long 发送序号 = Long.parseLong(段[2]);
            String 路径 = getActivity().getFilesDir().toString() + "/" + 当前用户.获取英语讯宝地址();
            if (聊天控件.聊天对象.小聊天群.编号 == 0) {
                File 文件 = new File(路径 + "/" + 段[1] + ".sscj");
                if (文件.exists() && !文件.isDirectory()) {
                    RandomAccessFile 文件随机访问器 = new RandomAccessFile(文件.getAbsolutePath(), "rwd");
                    long SS包位置 = 文件随机访问器.length();
                    文件随机访问器.seek(SS包位置);
                    SSPackageReader SS包读取器;
                    do {
                        SS包读取器 = SharedMethod.读取一个SS包(文件随机访问器);
                        if (SS包读取器 == null) {
                            SS包读取器 = SharedMethod.读取一个SS包2(文件随机访问器);
                        }
                        if (SS包读取器 != null) {
                            if (!(boolean) SS包读取器.读取_有标签("是接收者")) {
                                long 发送序号2 = (long)SS包读取器.读取_有标签("发送序号");
                                if (发送序号2 == 发送序号) {
                                    Object 值 = SS包读取器.读取_有标签("已收听");
                                    if (值 != null) {
                                        if (!(boolean) 值) {
                                            标为已收听(文件随机访问器, 文件随机访问器.getFilePointer(), SS包位置, SS包读取器);
                                        }
                                    }
                                    return;
                                }
                            }
                            SS包位置 = 文件随机访问器.getFilePointer();
                        }
                    } while (SS包读取器 != null);
                    文件随机访问器.close();
                }
            } else {
                File 文件 = new File(路径 + "/" + 聊天控件.聊天对象.讯友或群主.英语讯宝地址 + "#" + 聊天控件.聊天对象.小聊天群.编号 + ".sscj");
                if (文件.exists() && !文件.isDirectory()) {
                    String 发送者英语讯宝地址 = 段[1];
                    RandomAccessFile 文件随机访问器 = new RandomAccessFile(文件.getAbsolutePath(), "rwd");
                    long SS包位置 = 文件随机访问器.length();
                    文件随机访问器.seek(SS包位置);
                    SSPackageReader SS包读取器;
                    do {
                        SS包读取器 = SharedMethod.读取一个SS包(文件随机访问器);
                        if (SS包读取器 == null) {
                            SS包读取器 = SharedMethod.读取一个SS包2(文件随机访问器);
                        }
                        if (SS包读取器 != null) {
                            String 发送者 = (String) SS包读取器.读取_有标签("发送者");
                            if (发送者英语讯宝地址.equals(发送者)) {
                                long 发送序号2 = (long)SS包读取器.读取_有标签("发送序号");
                                if (发送序号2 == 发送序号) {
                                    Object 值 = SS包读取器.读取_有标签("已收听");
                                    if (值 != null) {
                                        if (!(boolean) 值) {
                                            标为已收听(文件随机访问器, 文件随机访问器.getFilePointer(), SS包位置, SS包读取器);
                                        }
                                    }
                                    return;
                                }
                            }
                            SS包位置 = 文件随机访问器.getFilePointer();
                        }
                    } while (SS包读取器 != null);
                    文件随机访问器.close();
                }
            }
        } catch (Exception e) {
        }
    }

    private void 标为已收听(RandomAccessFile 文件随机访问器, long 开始位置, long 结束位置, SSPackageReader SS包读取器) throws Exception {
        SSPackageCreator SS包生成器 = new SSPackageCreator(SS包读取器);
        if (SS包生成器.修改某个标签的数据("已收听", true)) {
            byte 字节数组[] = SS包生成器.生成SS包();
            if (字节数组.length == 结束位置 - 开始位置 - 2) {
                文件随机访问器.seek(开始位置);
                文件随机访问器.write(字节数组, 0, 字节数组.length);
            }
        }
    }

    void 播音完毕() {
        发送JS("function(){ VoiceEnded(); }");
    }

    void 对机器人说(String 文本) {
        对机器人说(文本, false, false);
    }

    void 对机器人说(String 文本, boolean 来自输入框, boolean 不使用定时器) {
        if (SharedMethod.字符串未赋值或为空(文本)) { return; }
//        当前用户.记录运行步骤("对机器人说：" + 文本);
        if (定时任务_机器人回答 != null) {
            定时任务_机器人回答.cancel();
            定时任务_机器人回答 = null;
            机器人.回答(话语.文本, 话语.时间);
        }
        long 当前UTC时刻 = SharedMethod.获取当前UTC时间();
        if (!机器人.正在输入密码) {
            发送JS("function(){var text = \"" + 替换HTML和JS敏感字符(文本) + "\"; var iconsrc =\"" + ProtocolPath.获取我的头像路径(当前用户.英语用户名, 当前用户.主机名, 当前用户.头像更新时间, 当前用户.域名_英语) + "\"; var time = \"" + 时间格式(new MyDate(当前UTC时刻)) + "\"; SSout_Text(\"" + 当前UTC时刻 + "\", text, iconsrc, time); }");
        } else {
            发送JS("function(){var text = \"" + 替换HTML和JS敏感字符(界面文字.获取(128, "[密码]")) + "\"; var iconsrc =\"" + ProtocolPath.获取我的头像路径(当前用户.英语用户名, 当前用户.主机名, 当前用户.头像更新时间, 当前用户.域名_英语) + "\"; var time = \"" + 时间格式(new MyDate(当前UTC时刻)) + "\"; SSout_Text(\"" + 当前UTC时刻 + "\", text, iconsrc, time); }");
        }
        if (!不使用定时器) {
//            当前用户.记录运行步骤("运行到：1");
            话语 = new 话语_复合数据();
            话语.文本 = 文本;
            话语.时间 = 当前UTC时刻;
            定时任务_机器人回答 = new MyTimerTask(跨线程调用器, 1);
            if (定时器_机器人回答 == null) {
                定时器_机器人回答 = new Timer();
            }
//            当前用户.记录运行步骤("运行到：2");
            定时器_机器人回答.schedule(定时任务_机器人回答, 600);
        } else {
            机器人.回答(文本, 当前UTC时刻);
        }
    }

    void 插入表情字符(String 表情字符) {
        if (!发送语音) {
            int 光标位置 = 输入框.getSelectionStart();
            Editable 可编辑文字 = 输入框.getEditableText();
            if (光标位置 < 0 || 光标位置 >= 可编辑文字.length()) {
                可编辑文字.append(表情字符);
            } else {
                可编辑文字.insert(光标位置, 表情字符);
            }
        }
    }

    void 按钮和机器人图标(boolean 禁用) {
        按钮_说话.setEnabled(!禁用);
        if (按钮_刷新 != null) {
            按钮_刷新.setEnabled(!禁用);
        }
        ImageView 机器人图片 = (ImageView) 第一控件.findViewById(R.id.图片_机器人);
        机器人图片.setEnabled(!禁用);
    }

    String 时间格式(MyDate UTC时间) {
        MyDate 当前UTC时间 = new MyDate(SharedMethod.获取当前UTC时间());
        if (UTC时间.年().equals(当前UTC时间.年())) {
            if (UTC时间.月().equals(当前UTC时间.月())) {
                if (UTC时间.日().equals(当前UTC时间.日())) {
                    return UTC时间.获取("HH:mm");
                } else {
                    return UTC时间.获取("dd HH:mm");
                }
            } else {
                return UTC时间.获取("MM-dd HH:mm");
            }
        } else {
            return UTC时间.获取("yyyy-MM-dd HH:mm");
        }
    }

    private void 关闭键盘() {
        InputMethodManager 输入法管理器 = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (输入法管理器 != null) { 输入法管理器.hideSoftInputFromWindow(getActivity().getWindow().getDecorView().getWindowToken(), 0); }
    }

    @JavascriptInterface
    public void PlayVoice(String VoiceSrc, String VoiceID, String IsNew) {
        Message 消息 = new Message();
        消息.what = 6;
        Bundle 数据盒子 = new Bundle();
        数据盒子.putString("文件路径", VoiceSrc);
        数据盒子.putString("VoiceID", VoiceID);
        数据盒子.putString("IsNew", IsNew);
        消息.setData(数据盒子);
        跨线程调用器.sendMessage(消息);
    }

    @JavascriptInterface
    public void ToRobot(String 文本) {
        Message 消息 = new Message();
        消息.what = 2;
        Bundle 数据盒子 = new Bundle();
        数据盒子.putString("文本", 文本);
        消息.setData(数据盒子);
        跨线程调用器.sendMessage(消息);
    }

    @JavascriptInterface
    public void ToRobot2(String 指令名称, String 参数1, String 参数2) {
        Message 消息 = new Message();
        消息.what = 3;
        Bundle 数据盒子 = new Bundle();
        数据盒子.putString("指令名称", 指令名称);
        数据盒子.putString("参数1", 参数1);
        数据盒子.putString("参数2", 参数2);
        消息.setData(数据盒子);
        跨线程调用器.sendMessage(消息);
    }

    private void ToRobot3(String 指令名称, String 参数1, String 参数2) {
        if (机器人 instanceof Robot_MainControl) {
            if (指令名称.equals("AddContact")) {
                Contact 讯友 = 当前用户.查找讯友(参数1);
                if (讯友 == null) {
                    对机器人说(TaskName.任务名称_添加讯友, false, true);
                    对机器人说(参数1);
                } else {
                    机器人.说(界面文字.获取(119, "已在你的讯友录中。"));
                }
            } else if (指令名称.equals("Block")) {
                Contact 讯友 = 当前用户.查找讯友(参数1);
                if (讯友 == null) {
                    对机器人说(TaskName.任务名称_拉黑, false, true);
                    对机器人说(参数1);
                } else if (!讯友.拉黑) {
                    对机器人说(TaskName.任务名称_拉黑, false, true);
                    对机器人说(参数1);
                } else {
                    机器人.说(界面文字.获取(110, "已在黑名单中。"));
                }
            } else if (指令名称.equals("DeleteSS")) {
                删除讯宝(Long.parseLong(参数1), 参数2);
            } else if (指令名称.equals("CopyText")) {
                try {
                    ClipboardManager 剪贴板管理器 = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                    剪贴板管理器.setPrimaryClip(ClipData.newPlainText("SS", 参数1.trim()));
                } catch (Exception e) {}
            } else if (指令名称.equals("JoinLargeGroup")) {
                加入大聊天群(参数1, 参数2);
            } else if (指令名称.equals("TermsOfUse")) {
                Intent 意图 = new Intent(Intent.ACTION_VIEW);
                Uri uri = Uri.parse(ProtocolPath.获取主站首页的访问路径() + "TermsOfUse.html");
                意图.setData(uri);
                startActivity(意图);
            } else if (指令名称.equals("PrivacyPolicy")) {
                Intent 意图 = new Intent(Intent.ACTION_VIEW);
                Uri uri = Uri.parse(ProtocolPath.获取主站首页的访问路径() + "PrivacyPolicy.html");
                意图.setData(uri);
                startActivity(意图);
            }
        } else {
            if (指令名称.equals("ClickImage")) {
                ((Activity_Main)getActivity()).弹出查看图片的窗体(参数1);
            } else if (指令名称.equals("ClickVideo")) {
                ((Activity_Main)getActivity()).弹出播放视频的窗体(参数1);
            } else if (指令名称.equals("DownloadFile")) {
                if (参数2.startsWith("https://") || 参数2.startsWith("http://")) {
                    String 保存路径 = getActivity().getExternalFilesDir(null).toString() + "/download";
                    File 文件 = new File(保存路径);
                    if (!文件.exists() || !文件.isDirectory()) {
                        文件.mkdir();
                    }
                    String 段[] = 参数2.split("&");
                    final String 字符串 = "FileName=";
                    int i;
                    for (i = 段.length - 1; i >= 0; i--) {
                        if (段[i].startsWith(字符串)) {
                            break;
                        }
                    }
                    if (i < 0) {
                        保存路径 += "/" + 参数1;
                    } else {
                        int j = 段[i].lastIndexOf(ProtocolParameters.特征字符_下划线);
                        if (j > 0 && j < 段[i].length() - 1) {
                            保存路径 += "/" + SharedMethod.获取文件名不带扩展名(参数1) + 段[i].substring(j);
                        } else {
                            保存路径 += "/" + 参数1;
                        }
                    }
                    文件 = new File(保存路径);
                    if (!文件.exists()) {
                        ((Activity_Main) getActivity()).弹出下载文件的窗体(参数2, 保存路径);
                    } else {
                        Toast.makeText(getActivity(), 界面文字.获取(317, "已下载至 #%", new Object[] {保存路径}), Toast.LENGTH_LONG).show();
//                        SharedMethod.打开资源管理器并选中文件(getActivity().getApplicationContext(), 保存路径);
                    }
//                } else {
//                    SharedMethod.打开资源管理器并选中文件(getActivity().getApplicationContext(), 参数2);
                }
            } else if (指令名称.equals("DeleteSS")) {
                删除讯宝(Long.parseLong(参数1), 参数2);
            } else if (指令名称.equals("CancelSS")) {
                撤回讯宝(Long.parseLong(参数1));
            } else if (指令名称.equals("CopyText")) {
                try {
                    ClipboardManager 剪贴板管理器 = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                    剪贴板管理器.setPrimaryClip(ClipData.newPlainText("SS", 参数1.trim()));
                } catch (Exception e) {}
            } else if (指令名称.equals("ClickIcon")) {
                if (!参数1.contains(ProtocolParameters.讯宝地址标识)) {
                    return;
                }
                if (当前用户.获取英语讯宝地址().equals(参数1)) {
                    return;
                }
                Contact 讯友目录[] = 当前用户.讯友目录;
                if (讯友目录 != null) {
                    int i;
                    for (i = 0; i < 讯友目录.length; i++) {
                        if (讯友目录[i].英语讯宝地址.equals(参数1)) {
                            break;
                        }
                    }
                    if (i < 讯友目录.length) {
                        String 地址;
                        Contact 某一讯友 = 讯友目录[i];
                        if (!SharedMethod.字符串未赋值或为空(某一讯友.本国语讯宝地址)) {
                            地址 = 某一讯友.本国语讯宝地址 + " / " + 某一讯友.英语讯宝地址;
                        } else {
                            地址 = 某一讯友.英语讯宝地址;
                        }
                        点击群成员(地址);
                    } else {
                        点击群成员(参数1);
                    }
                } else {
                    点击群成员(参数1);
                }
            } else if (指令名称.equals("JoinSmallGroup")) {
                byte 群编号 = Byte.parseByte(参数2);
                if (群编号 == 0) {
                    return;
                }
                String 群主英语讯宝地址 = 聊天对象.讯友或群主.英语讯宝地址;
                Group_Small 加入的群[] = 当前用户.加入的小聊天群;
                if (加入的群 != null) {
                    for (int i = 0; i < 加入的群.length; i++) {
                        if (加入的群[i].编号 == 群编号 && 加入的群[i].群主.英语讯宝地址.equals(群主英语讯宝地址)) {
                            机器人.说(界面文字.获取(102, "你已加入该聊天群。"));
                            return;
                        }
                    }
                    if (加入的群.length >= ProtocolParameters.最大值_每个用户可加入的小聊天群数量) {
                        机器人.说(界面文字.获取(174, "你加入的小聊天群数量已达上限。"));
                        return;
                    }
                }
                Group_Small 新群 = new Group_Small();
                新群.群主 = 聊天对象.讯友或群主;
                新群.备注 = 参数1;
                新群.编号 = 群编号;
                新群.待加入确认 = true;
                if (加入的群 != null) {
                    Group_Small 加入的群2[] = new Group_Small[加入的群.length + 1];
                    System.arraycopy(加入的群, 0, 加入的群2, 0, 加入的群.length);
                    加入的群2[加入的群2.length - 1] = 新群;
                    加入的群 = 加入的群2;
                } else {
                    加入的群 = new Group_Small[1];
                    加入的群[0] = 新群;
                }
                当前用户.加入的小聊天群 = 加入的群;
                ChatWith 聊天对象2 = new ChatWith();
                聊天对象2.讯友或群主 = 新群.群主;
                聊天对象2.小聊天群 = 新群;
                Fragment_Main.主窗体.添加聊天控件(聊天对象2);
                主控机器人.数据库_更新最近互动讯友排名(群主英语讯宝地址, 群编号);
            } else if (指令名称.equals("JoinLargeGroup")) {
                加入大聊天群(参数1, 参数2);
            }
        }
    }

    private void 加入大聊天群(String 参数1, String 参数2) {
        long 群编号 = Long.parseLong(参数2);
        if (群编号 == 0) {
            return;
        }
        if (当前用户.加入的大聊天群 != null) {
            Group_Large 加入的大聊天群[] = 当前用户.加入的大聊天群;
            int j;
            for (j = 0; j < 加入的大聊天群.length; j++) {
                if (加入的大聊天群[j].子域名.equals(参数1) && 加入的大聊天群[j].编号 == 群编号) {
                    机器人.说(界面文字.获取(102, "你已加入该聊天群。"));
                    return;
                }
            }
            if (加入的大聊天群.length >= ProtocolParameters.最大值_每个用户可加入的大聊天群数量) {
                机器人.说(界面文字.获取(279, "你加入的大聊天群数量已达上限。"));
                return;
            }
        }
        byte 字节数组[] = null;
        try {
            SSPackageCreator SS包生成器 = new SSPackageCreator();
            SS包生成器.添加_有标签("发送序号", 当前用户.讯宝发送序号);
            SS包生成器.添加_有标签("子域名", 参数1);
            SS包生成器.添加_有标签("群编号", 群编号);
            字节数组 = SS包生成器.生成SS包(当前用户.AES加密器);
        } catch (Exception e) {
            机器人.说(e.getMessage());
            return;
        }
        if (机器人.任务 != null) {
            机器人.任务.结束();
        }
        机器人.任务 = new Task(TaskName.任务名称_加入大聊天群, 机器人);
        机器人.说(界面文字.获取(7, "请稍等。"));
        机器人.启动HTTPS访问线程(new httpSetting(ProtocolPath.获取传送服务器访问路径开头(当前用户.主机名, 当前用户.域名_英语, false) + "C=JoinLargeGroup&UserID=" + 当前用户.编号 + "&Position=" + 当前用户.位置号 + "&DeviceType=" + ProtocolParameters.设备类型_文本_手机, 20000, 字节数组));
    }


    void 发送JS(String JS) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                浏览器_聊天.evaluateJavascript("(" + JS + ")()", new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String value) {

                    }
                });
            } else {
                浏览器_聊天.loadUrl("javascript:" + JS);
            }
        } catch (Exception e) {
        }
    }

    void 处理跨线程数据(Message msg) {
        switch (msg.what) {
            case 5:
                录音剩余秒数 -= 0.1;
                if (录音剩余秒数 <= 0) {
                    if (定时任务_录音 != null) {
                        停止录音(false);
                    }
                }
                break;
            case 6:
                String 文件路径 = msg.getData().getString("文件路径");
                String VoiceID = msg.getData().getString("VoiceID");
                String IsNew = msg.getData().getString("IsNew");
                播放语音(this, 文件路径, VoiceID, IsNew);
                break;
            case 1:
//                当前用户.记录运行步骤("运行到：3");
                定时任务_机器人回答 = null;
                机器人.回答(话语.文本, 话语.时间);
                break;
            case 2:
                对机器人说(msg.getData().getString("文本"));
                break;
            case 3:
                String 指令名称 = msg.getData().getString("指令名称");
                String 参数1 = msg.getData().getString("参数1");
                String 参数2 = msg.getData().getString("参数2");
                ToRobot3(指令名称, 参数1, 参数2);
                break;
            case 4:
                ReachTop2();
                break;

//            case 10:
//                计数 += 1;
//                对机器人说(String.valueOf(计数), true, true);
//                break;
        }
    }

    @JavascriptInterface
    public void ReachTop() {
        Message 消息 = new Message();
        消息.what = 4;
        跨线程调用器.sendMessage(消息);
    }

    private void ReachTop2() {
        if (起始位置 > 0) {
            final int 最大值 = 20;
            讯宝_复合数据 讯宝[] = 数据库_读取旧讯宝(最大值);
            讯宝_复合数据 某一讯宝;
            if (讯宝 != null) {
                if (讯宝.length < 最大值) {
                    起始位置 = 0;
                }
                发送JS("function(){ LoadEarlierStart(); }");
                int i, 上限;
                上限 = 讯宝.length - 1;
                String 时间提示文本;
                final int 六万毫秒 = 60 * 1000;
                if (!(机器人 instanceof Robot_MainControl)) {
                    if (聊天对象.小聊天群 != null) {
                        if (聊天对象.小聊天群.编号 == 0) {
                            for (i = 0; i <= 上限; i++) {
                                某一讯宝 = 讯宝[i];
                                if (i < 上限) {
                                    if (Math.abs(某一讯宝.发送时间 - 讯宝[i + 1].发送时间) > 六万毫秒 ) {
                                        时间提示文本 = 时间格式(new MyDate(某一讯宝.发送时间));
                                    } else {
                                        时间提示文本 = "";    //不能赋值为 null
                                    }
                                } else {
                                    时间提示文本 = 时间格式(new MyDate(某一讯宝.发送时间));
                                }
                                if (!某一讯宝.是接收者) {
                                    讯友说(某一讯宝.收发者讯宝地址, 某一讯宝.发送时间, 某一讯宝.发送序号, 某一讯宝.指令, 某一讯宝.文本, 某一讯宝.宽度, 某一讯宝.高度, 某一讯宝.秒数, 某一讯宝.已收听, 时间提示文本);
                                } else {
                                    switch (某一讯宝.指令) {
                                        case ProtocolParameters.讯宝指令_发送文字:
                                            发送JS("function(){var text = \"" + 替换HTML和JS敏感字符(某一讯宝.文本) + "\"; var iconsrc =\"" + ProtocolPath.获取我的头像路径(当前用户.英语用户名, 当前用户.主机名, 当前用户.头像更新时间, 当前用户.域名_英语) + "\"; var time = \"" + 时间提示文本 + "\"; SSout_Text(\"" + 某一讯宝.发送时间 + "\", text, iconsrc, time); }");
                                            break;
                                        case ProtocolParameters.讯宝指令_发送图片:
                                        case ProtocolParameters.讯宝指令_发送语音:
                                        case ProtocolParameters.讯宝指令_发送短视频:
                                            String 路径;
                                            if (!某一讯宝.文本.contains(ProtocolParameters.特征字符_下划线)) {
                                                路径 = SharedMethod.处理文件路径以用作JS函数参数(某一讯宝.文本);
                                            } else {
                                                路径 = ProtocolPath.获取传送服务器访问路径开头(当前用户.主机名, 当前用户.域名_英语, true) + "Position=" + 当前用户.位置号 + "&EnglishSSAddress=" + 替换URI敏感字符(当前用户.英语用户名 + ProtocolParameters.讯宝地址标识 + 当前用户.域名_英语) + "&FileName=" + 替换URI敏感字符(某一讯宝.文本);
                                            }
                                            switch (某一讯宝.指令) {
                                                case ProtocolParameters.讯宝指令_发送图片:
                                                    发送JS("function(){var imgsrc = \"" + 路径 + "\"; var iconsrc =\"" + ProtocolPath.获取我的头像路径(当前用户.英语用户名, 当前用户.主机名, 当前用户.头像更新时间, 当前用户.域名_英语) + "\"; var time = \"" + 时间提示文本 + "\"; SSout_Img(\"" + 某一讯宝.发送时间 + "\", imgsrc, \"" + 某一讯宝.宽度 + "\", \"" + 某一讯宝.高度 + "\", iconsrc, time); }");
                                                    break;
                                                case ProtocolParameters.讯宝指令_发送语音:
                                                    String 文本 = 界面文字.获取(258, "语音：#% 秒", new Object[] { 某一讯宝.秒数});
                                                    发送JS("function(){var text = \"" + 替换HTML和JS敏感字符(文本) + "\"; var voicesrc = \"" + 路径 + "\"; var iconsrc =\"" + ProtocolPath.获取我的头像路径(当前用户.英语用户名, 当前用户.主机名, 当前用户.头像更新时间, 当前用户.域名_英语) + "\"; var time = \"" + 时间格式(new MyDate(某一讯宝.发送时间)) + "\"; SSout_Voice(\"" + 某一讯宝.发送时间 + "\", text, voicesrc, iconsrc, time); }");
                                                    break;
                                                case ProtocolParameters.讯宝指令_发送短视频:
                                                    发送JS("function(){var videosrc = \"" + 路径 + "\"; var iconsrc =\"" + ProtocolPath.获取我的头像路径(当前用户.英语用户名, 当前用户.主机名, 当前用户.头像更新时间, 当前用户.域名_英语) + "\"; var time = \"" + 时间提示文本 + "\"; SSout_Video(\"" + 某一讯宝.发送时间 + "\", videosrc, \"" + 某一讯宝.宽度 + "\", \"" + 某一讯宝.高度 + "\", iconsrc, time); }");
                                                    break;
                                            }
                                            break;
                                        case ProtocolParameters.讯宝指令_发送文件:
                                            String 原始文件名;
                                            if (!某一讯宝.文本.startsWith(ProtocolParameters.SS包标识_纯文本)) {
                                                原始文件名 = SharedMethod.获取文件名(某一讯宝.文本);
                                                路径 = SharedMethod.处理文件路径以用作JS函数参数(某一讯宝.文本);
                                            } else {
                                                try {
                                                    SSPackageReader SS包解读器2 = new SSPackageReader();
                                                    SS包解读器2.解读纯文本(某一讯宝.文本);
                                                    原始文件名 = (String)SS包解读器2.读取_有标签("O");
                                                    String 存储文件名 = (String)SS包解读器2.读取_有标签("S");
                                                    路径 = ProtocolPath.获取传送服务器访问路径开头(当前用户.主机名, 当前用户.域名_英语, true) + "Position=" + 当前用户.位置号 + "&EnglishSSAddress=" + 替换URI敏感字符(当前用户.获取英语讯宝地址()) + "&FileName=" + 替换URI敏感字符(存储文件名);
                                                } catch (Exception e) {
                                                    continue;
                                                }
                                            }
                                            发送JS("function(){var text = \"" + SharedMethod.处理文件路径以用作JS函数参数(原始文件名) + "\"; var filesrc = \"" + 路径 + "\"; var iconsrc =\"" + ProtocolPath.获取我的头像路径(当前用户.英语用户名, 当前用户.主机名, 当前用户.头像更新时间, 当前用户.域名_英语) + "\"; var time = \"" + 时间格式(new MyDate(某一讯宝.发送时间)) + "\"; SSout_File(\"" + 某一讯宝.发送时间 + "\", text, filesrc, iconsrc, time); }");
                                            break;
                                    }
                                }
                            }
                        } else {
                            String 英语讯宝地址 = 当前用户.获取英语讯宝地址();
                            for (i = 0; i <= 上限; i++) {
                                某一讯宝 = 讯宝[i];
                                switch (某一讯宝.指令) {
                                    case ProtocolParameters.讯宝指令_某人加入聊天群:
                                        机器人.说(界面文字.获取(175, "#% 加入了本群。", new Object[] {替换HTML和JS敏感字符(某一讯宝.文本)}), 某一讯宝.发送时间, null);
                                        break;
                                    case ProtocolParameters.讯宝指令_退出小聊天群:
                                        机器人.说(界面文字.获取(178, "#% 离开了本群。", new Object[] {替换HTML和JS敏感字符(某一讯宝.文本)}), 某一讯宝.发送时间, null);
                                        break;
                                    case ProtocolParameters.讯宝指令_删减聊天群成员:
                                        机器人.说(界面文字.获取(190, "群主让 #% 离开了本群。", new Object[] {替换HTML和JS敏感字符(某一讯宝.文本)}), 某一讯宝.发送时间, null);
                                        break;
                                    case ProtocolParameters.讯宝指令_修改聊天群名称:
                                        机器人.说(界面文字.获取(185, "本群名称更改为 #%。", new Object[] {替换HTML和JS敏感字符(某一讯宝.文本)}), 某一讯宝.发送时间, null);
                                        break;
                                    default:
                                        if (i < 上限) {
                                            if (Math.abs(某一讯宝.发送时间 - 讯宝[i + 1].发送时间) > 六万毫秒 ) {
                                                时间提示文本 = 时间格式(new MyDate(某一讯宝.发送时间));
                                            } else {
                                                时间提示文本 = "";    //不能赋值为 null
                                            }
                                        } else {
                                            时间提示文本 = 时间格式(new MyDate(某一讯宝.发送时间));
                                        }
                                        if (!某一讯宝.收发者讯宝地址.equals(英语讯宝地址)) {
                                            讯友说(某一讯宝.收发者讯宝地址, 某一讯宝.发送时间, 某一讯宝.发送序号, 某一讯宝.指令, 某一讯宝.文本, 某一讯宝.宽度, 某一讯宝.高度, 某一讯宝.秒数, 某一讯宝.已收听, 时间提示文本);
                                        } else {
                                            switch (某一讯宝.指令) {
                                                case ProtocolParameters.讯宝指令_发送文字:
                                                    发送JS("function(){var text = \"" + 替换HTML和JS敏感字符(某一讯宝.文本) + "\"; var iconsrc =\"" + ProtocolPath.获取我的头像路径(当前用户.英语用户名, 当前用户.主机名, 当前用户.头像更新时间, 当前用户.域名_英语) + "\"; var time = \"" + 时间提示文本 + "\"; SSout_Text(\"" + 某一讯宝.发送时间 + "\", text, iconsrc, time); }");
                                                    break;
                                                case ProtocolParameters.讯宝指令_发送图片:
                                                case ProtocolParameters.讯宝指令_发送语音:
                                                case ProtocolParameters.讯宝指令_发送短视频:
                                                    String 路径;
                                                    if (!某一讯宝.文本.contains(ProtocolParameters.特征字符_下划线)) {
                                                        路径 = SharedMethod.处理文件路径以用作JS函数参数(某一讯宝.文本);
                                                    } else {
                                                        路径 = ProtocolPath.获取传送服务器访问路径开头(当前用户.主机名, 当前用户.域名_英语, true) + "Position=" + 当前用户.位置号 + "&EnglishSSAddress=" + 替换URI敏感字符(当前用户.获取英语讯宝地址()) + "&FileName=" + 替换URI敏感字符(某一讯宝.文本);
                                                    }
                                                    switch (某一讯宝.指令) {
                                                        case ProtocolParameters.讯宝指令_发送图片:
                                                            发送JS("function(){var imgsrc = \"" + 路径 + "\"; var iconsrc =\"" + ProtocolPath.获取我的头像路径(当前用户.英语用户名, 当前用户.主机名, 当前用户.头像更新时间, 当前用户.域名_英语) + "\"; var time = \"" + 时间提示文本 + "\"; SSout_Img(\"" + 某一讯宝.发送时间 + "\", imgsrc, \"" + 某一讯宝.宽度 + "\", \"" + 某一讯宝.高度 + "\", iconsrc, time); }");
                                                            break;
                                                        case ProtocolParameters.讯宝指令_发送语音:
                                                            String 文本 = 界面文字.获取(258, "语音：#% 秒", new Object[] { 某一讯宝.秒数});
                                                            发送JS("function(){var text = \"" + 替换HTML和JS敏感字符(文本) + "\"; var voicesrc = \"" + 路径 + "\"; var iconsrc =\"" + ProtocolPath.获取我的头像路径(当前用户.英语用户名, 当前用户.主机名, 当前用户.头像更新时间, 当前用户.域名_英语) + "\"; var time = \"" + 时间格式(new MyDate(某一讯宝.发送时间)) + "\"; SSout_Voice(\"" + 某一讯宝.发送时间 + "\", text, voicesrc, iconsrc, time); }");
                                                            break;
                                                        case ProtocolParameters.讯宝指令_发送短视频:
                                                            发送JS("function(){var videosrc = \"" + 路径 + "\"; var iconsrc =\"" + ProtocolPath.获取我的头像路径(当前用户.英语用户名, 当前用户.主机名, 当前用户.头像更新时间, 当前用户.域名_英语) + "\"; var time = \"" + 时间提示文本 + "\"; SSout_Video(\"" + 某一讯宝.发送时间 + "\", videosrc, \"" + 某一讯宝.宽度 + "\", \"" + 某一讯宝.高度 + "\", iconsrc, time); }");
                                                            break;
                                                    }
                                                    break;
                                                case ProtocolParameters.讯宝指令_发送文件:
                                                    String 原始文件名;
                                                    if (!某一讯宝.文本.startsWith(ProtocolParameters.SS包标识_纯文本)) {
                                                        原始文件名 = SharedMethod.获取文件名(某一讯宝.文本);
                                                        路径 = SharedMethod.处理文件路径以用作JS函数参数(某一讯宝.文本);
                                                    } else {
                                                        try {
                                                            SSPackageReader SS包解读器2 = new SSPackageReader();
                                                            SS包解读器2.解读纯文本(某一讯宝.文本);
                                                            原始文件名 = (String)SS包解读器2.读取_有标签("O");
                                                            String 存储文件名 = (String)SS包解读器2.读取_有标签("S");
                                                            路径 = ProtocolPath.获取传送服务器访问路径开头(当前用户.主机名, 当前用户.域名_英语, true) + "Position=" + 当前用户.位置号 + "&EnglishSSAddress=" + 替换URI敏感字符(当前用户.获取英语讯宝地址()) + "&FileName=" + 替换URI敏感字符(存储文件名);
                                                        } catch (Exception e) {
                                                            continue;
                                                        }
                                                    }
                                                    发送JS("function(){var text = \"" + SharedMethod.处理文件路径以用作JS函数参数(原始文件名) + "\"; var filesrc = \"" + 路径 + "\"; var iconsrc =\"" + ProtocolPath.获取我的头像路径(当前用户.英语用户名, 当前用户.主机名, 当前用户.头像更新时间, 当前用户.域名_英语) + "\"; var time = \"" + 时间格式(new MyDate(某一讯宝.发送时间)) + "\"; SSout_File(\"" + 某一讯宝.发送时间 + "\", text, filesrc, iconsrc, time); }");
                                                    break;     }
                                        }
                                }
                            }
                        }
                        发送JS("function(){ LoadEnd(false); }");
                    } else {
                        String 英语讯宝地址 = 当前用户.获取英语讯宝地址();
                        long 发送时间;
                        for (i = 0; i <= 上限; i++) {
                            某一讯宝 = 讯宝[i];
                            发送时间 = ProtocolMethods.转换成Java相对时间2(某一讯宝.发送时间);
                            if (i < 上限) {
                                if (Math.abs(某一讯宝.发送时间 - 讯宝[i + 1].发送时间) > 六万毫秒 ) {
                                    时间提示文本 = 时间格式(new MyDate(发送时间));
                                } else {
                                    时间提示文本 = "";    //不能赋值为 null
                                }
                            } else {
                                时间提示文本 = 时间格式(new MyDate(发送时间));
                            }
                            if (!某一讯宝.收发者讯宝地址.equals(英语讯宝地址)) {
                                讯友说(某一讯宝.收发者讯宝地址, 某一讯宝.发送时间, 某一讯宝.发送序号, 某一讯宝.指令, 某一讯宝.文本, 某一讯宝.宽度, 某一讯宝.高度, 某一讯宝.秒数, 某一讯宝.已收听, 时间提示文本);
                            } else {
                                switch (某一讯宝.指令) {
                                    case ProtocolParameters.讯宝指令_发送文字:
                                        发送JS("function(){var text = \"" + 替换HTML和JS敏感字符(某一讯宝.文本) + "\"; var iconsrc =\"" + ProtocolPath.获取我的头像路径(当前用户.英语用户名, 当前用户.主机名, 当前用户.头像更新时间, 当前用户.域名_英语) + "\"; var time = \"" + 时间提示文本 + "\"; SSout_Text(\"" + 某一讯宝.发送时间 + "\", text, iconsrc, time); }");
                                        break;
                                    case ProtocolParameters.讯宝指令_发送图片:
                                    case ProtocolParameters.讯宝指令_发送语音:
                                    case ProtocolParameters.讯宝指令_发送短视频:
                                        String 路径;
                                        if (!某一讯宝.文本.contains(ProtocolParameters.特征字符_下划线)) {
                                            路径 = SharedMethod.处理文件路径以用作JS函数参数(某一讯宝.文本);
                                        } else {
                                            路径 = ProtocolPath.获取大聊天群服务器访问路径开头(聊天对象.大聊天群.子域名, true) + "GroupID=" + 聊天对象.大聊天群.编号 + "&FileName=" + 替换URI敏感字符(某一讯宝.文本);
                                        }
                                        switch (某一讯宝.指令) {
                                            case ProtocolParameters.讯宝指令_发送图片:
                                                发送JS("function(){var imgsrc = \"" + 路径 + "\"; var iconsrc =\"" + ProtocolPath.获取我的头像路径(当前用户.英语用户名, 当前用户.主机名, 当前用户.头像更新时间, 当前用户.域名_英语) + "\"; var time = \"" + 时间提示文本 + "\"; SSout_Img(\"" + 某一讯宝.发送时间 + "\", imgsrc, \"" + 某一讯宝.宽度 + "\", \"" + 某一讯宝.高度 + "\", iconsrc, time, \"true\"); }");
                                                break;
                                            case ProtocolParameters.讯宝指令_发送语音:
                                                String 文本 = 界面文字.获取(258, "语音：#% 秒", new Object[] { 某一讯宝.秒数});
                                                发送JS("function(){var text = \"" + 替换HTML和JS敏感字符(文本) + "\"; var voicesrc = \"" + 路径 + "\"; var iconsrc =\"" + ProtocolPath.获取我的头像路径(当前用户.英语用户名, 当前用户.主机名, 当前用户.头像更新时间, 当前用户.域名_英语) + "\"; var time = \"" + 时间提示文本 + "\"; SSout_Voice(\"" + 某一讯宝.发送时间 + "\", text, voicesrc, iconsrc, time); }");
                                                break;
                                            case ProtocolParameters.讯宝指令_发送短视频:
                                                发送JS("function(){var videosrc = \"" + 路径 + "\"; var iconsrc =\"" + ProtocolPath.获取我的头像路径(当前用户.英语用户名, 当前用户.主机名, 当前用户.头像更新时间, 当前用户.域名_英语) + "\"; var time = \"" + 时间提示文本 + "\"; SSout_Video(\"" + 某一讯宝.发送时间 + "\", videosrc, \"" + 某一讯宝.宽度 + "\", \"" + 某一讯宝.高度 + "\", iconsrc, time); }");
                                                break;
                                        }
                                        break;
                                    case ProtocolParameters.讯宝指令_发送文件:
                                        String 原始文件名;
                                        if (!某一讯宝.文本.startsWith(ProtocolParameters.SS包标识_纯文本)) {
                                            原始文件名 = SharedMethod.获取文件名(某一讯宝.文本);
                                            路径 = SharedMethod.处理文件路径以用作JS函数参数(某一讯宝.文本);
                                        } else {
                                            try {
                                                SSPackageReader SS包解读器2 = new SSPackageReader();
                                                SS包解读器2.解读纯文本(某一讯宝.文本);
                                                原始文件名 = (String)SS包解读器2.读取_有标签("O");
                                                String 存储文件名 = (String)SS包解读器2.读取_有标签("S");
                                                路径 = ProtocolPath.获取大聊天群服务器访问路径开头(聊天对象.大聊天群.子域名, true) + "GroupID=" + 聊天对象.大聊天群.编号 + "&FileName=" + 替换URI敏感字符(存储文件名);
                                            } catch (Exception e) {
                                                continue;
                                            }
                                        }
                                        发送JS("function(){var text = \"" + SharedMethod.处理文件路径以用作JS函数参数(原始文件名) + "\"; var filesrc = \"" + 路径 + "\"; var iconsrc =\"" + ProtocolPath.获取我的头像路径(当前用户.英语用户名, 当前用户.主机名, 当前用户.头像更新时间, 当前用户.域名_英语) + "\"; var time = \"" + 时间格式(new MyDate(某一讯宝.发送时间)) + "\"; SSout_File(\"" + 某一讯宝.发送时间 + "\", text, filesrc, iconsrc, time); }");
                                        break;
                                }
                            }
                        }
                        发送JS("function(){ LoadEnd(true); }");
                    }
                } else {
                    for (i = 0; i <= 上限; i++) {
                        某一讯宝 = 讯宝[i];
                        if (i < 上限) {
                            if (Math.abs(某一讯宝.发送时间 - 讯宝[i + 1].发送时间) > 六万毫秒 ) {
                                时间提示文本 = 时间格式(new MyDate(某一讯宝.发送时间));
                            } else {
                                时间提示文本 = "";    //不能赋值为 null
                            }
                        } else {
                            时间提示文本 = 时间格式(new MyDate(某一讯宝.发送时间));
                        }
                        陌生人说(某一讯宝.收发者讯宝地址, 某一讯宝.发送时间, 某一讯宝.发送序号, 某一讯宝.指令, 某一讯宝.文本, 时间提示文本);
                    }
                    发送JS("function(){ LoadEnd(false); }");
                }
            } else {
                起始位置 = 0;
            }
        }
    }


    private void 撤回讯宝(long 发送序号或存储时间) {
        try {
            if (聊天对象.小聊天群 != null) {
                File 文件 = new File( getActivity().getFilesDir().toString() + "/" + 当前用户.获取英语讯宝地址() + "/" + Constants.文件名起始字符串 + 发送序号或存储时间 + Constants.文件名结束字符串);
                if (文件.exists()) {
                    文件.delete();
                    发送JS("function(){ RemoveSS(\"" + 发送序号或存储时间 + "\"); }");
                    return;
                }
                long 发送序号 = 数据库_我撤回讯宝(聊天对象.小聊天群.编号, 聊天对象.讯友或群主.英语讯宝地址, 发送序号或存储时间);
                if (发送序号 > 0) {
                    if (聊天对象.小聊天群.编号 == 0) {
                        if (主控机器人.数据库_保存要发送的一对一讯宝( 聊天对象.讯友或群主.英语讯宝地址, SharedMethod.获取当前UTC时间(), ProtocolParameters.讯宝指令_撤回, String.valueOf(发送序号), (short)0, (short)0, (byte)0)) {
                            主控机器人.发送讯宝(false);
                            发送JS("function(){ RemoveSS(\"" + 发送序号或存储时间 + "\"); }");
                        }
                    } else {
                        if (主控机器人.数据库_保存要发送的小聊天群讯宝(聊天对象.讯友或群主.英语讯宝地址, 聊天对象.小聊天群.编号, SharedMethod.获取当前UTC时间(), ProtocolParameters.讯宝指令_撤回, String.valueOf(发送序号), (short) 0, (short) 0, (byte) 0)) {
                            主控机器人.发送讯宝(false);
                            发送JS("function(){ RemoveSS(\"" + 发送序号或存储时间 + "\"); }");
                        }
                    }
                }
            } else if (聊天对象.大聊天群 != null) {
                数据库_我撤回大聊天群讯宝(聊天对象.大聊天群.编号, 聊天对象.大聊天群.子域名, 发送序号或存储时间);
                发送JS("function(){ RemoveSS(\"" + 发送序号或存储时间 + "\"); }");
                ((Robot_LargeChatGroup)机器人).发送或接收(ProtocolParameters.讯宝指令_撤回, String.valueOf(发送序号或存储时间));
            }
        } catch (Exception e) {
            机器人.说(e.getMessage());
        }
    }

    private long 数据库_我撤回讯宝(byte 群编号, String 讯友或群主英语讯宝地址, long 发送序号或存储时间) {
        try {
            String 路径 = getActivity().getFilesDir().toString() + "/" + 当前用户.获取英语讯宝地址();
            if (群编号 == 0) {
                File 文件 = new File(路径 + "/" + 讯友或群主英语讯宝地址 + ".sscj");
                if (文件.exists() && !文件.isDirectory()) {
                    RandomAccessFile 文件随机访问器 = new RandomAccessFile(文件.getAbsolutePath(), "rwd");
                    long SS包位置 = 文件随机访问器.length();
                    文件随机访问器.seek(SS包位置);
                    SSPackageReader SS包读取器;
                    do {
                        SS包读取器 = SharedMethod.读取一个SS包(文件随机访问器);
                        if (SS包读取器 == null) {
                            SS包读取器 = SharedMethod.读取一个SS包2(文件随机访问器);
                        }
                        if (SS包读取器 != null) {
                            if ((boolean) SS包读取器.读取_有标签("是接收者")) {
                                if ((long) SS包读取器.读取_有标签("发送时间") == 发送序号或存储时间) {
                                    if (!(boolean)SS包读取器.读取_有标签("删除")) {
                                        SharedMethod.标为已删除(文件随机访问器, 文件随机访问器.getFilePointer(), SS包位置, SS包读取器);
                                        SharedMethod.删除相关文件(SS包读取器, getActivity(), 当前用户);
                                    }
                                    return (long)SS包读取器.读取_有标签("发送序号");
                                }
                            }
                            SS包位置 = 文件随机访问器.getFilePointer();
                        }
                    } while (SS包读取器 != null);
                    文件随机访问器.close();
                }
            } else {
                File 文件 = new File(路径 + "/" + 讯友或群主英语讯宝地址 + "#" + 群编号 + ".sscj");
                if (文件.exists() && !文件.isDirectory()) {
                    RandomAccessFile 文件随机访问器 = new RandomAccessFile(文件.getAbsolutePath(), "rwd");
                    long SS包位置 = 文件随机访问器.length();
                    文件随机访问器.seek(SS包位置);
                    SSPackageReader SS包读取器;
                    do {
                        SS包读取器 = SharedMethod.读取一个SS包(文件随机访问器);
                        if (SS包读取器 == null) {
                            SS包读取器 = SharedMethod.读取一个SS包2(文件随机访问器);
                        }
                        if (SS包读取器 != null) {
                            if (((String) SS包读取器.读取_有标签("发送者")).equals(当前用户.获取英语讯宝地址())) {
                                if ((long) SS包读取器.读取_有标签("发送时间") == 发送序号或存储时间) {
                                    if (!(boolean)SS包读取器.读取_有标签("删除")) {
                                        SharedMethod.标为已删除(文件随机访问器, 文件随机访问器.getFilePointer(), SS包位置, SS包读取器);
                                        SharedMethod.删除相关文件(SS包读取器, getActivity(), 当前用户);
                                    }
                                    return (long)SS包读取器.读取_有标签("发送序号");
                                }
                            }
                            SS包位置 = 文件随机访问器.getFilePointer();
                        }
                    } while (SS包读取器 != null);
                    文件随机访问器.close();
                }
            }
        } catch (Exception e) {
        }
        return 0;
    }

    private void 数据库_我撤回大聊天群讯宝(long 群编号, String 子域名, long 发送时间) {
        try {
            String 路径 = getActivity().getFilesDir().toString() + "/" + 当前用户.获取英语讯宝地址();
            File 文件 = new File(路径 + "/" + 子域名 + "#" + 群编号 + ".sscj");
            if (文件.exists() && !文件.isDirectory()) {
                RandomAccessFile 文件随机访问器 = new RandomAccessFile(文件.getAbsolutePath(), "rwd");
                long SS包位置 = 文件随机访问器.length();
                文件随机访问器.seek(SS包位置);
                SSPackageReader SS包读取器;
                do {
                    SS包读取器 = SharedMethod.读取一个SS包(文件随机访问器);
                    if (SS包读取器 == null) {
                        SS包读取器 = SharedMethod.读取一个SS包2(文件随机访问器);
                    }
                    if (SS包读取器 != null) {
                        if (((String) SS包读取器.读取_有标签("发送者")).equals(当前用户.获取英语讯宝地址())) {
                            if ((long) SS包读取器.读取_有标签("发送时间") == 发送时间) {
                                if (!(boolean)SS包读取器.读取_有标签("删除")) {
                                    SharedMethod.标为已删除(文件随机访问器, 文件随机访问器.getFilePointer(), SS包位置, SS包读取器);
                                    SharedMethod.删除相关文件(SS包读取器, getActivity(), 当前用户);
                                }
                            }
                        }
                        SS包位置 = 文件随机访问器.getFilePointer();
                    }
                } while (SS包读取器 != null);
                文件随机访问器.close();
            }
        } catch (Exception e) {
        }
    }

    private void 删除讯宝(long 发送序号或存储时间2, String 发送者2) {
        final long 发送序号或存储时间 = 发送序号或存储时间2;
        final String 发送者 = 发送者2;
        AlertDialog.Builder 对话框 = new AlertDialog.Builder(getActivity());
        对话框.setTitle(界面文字.获取(194, "删除"));
        对话框.setMessage(界面文字.获取(195, "删除吗？"));
        对话框.setPositiveButton(界面文字.获取(组名_任务, 0, "是"), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    if (!(机器人 instanceof Robot_MainControl)) {
                        if (聊天对象.小聊天群 != null) {
                            if (SharedMethod.字符串未赋值或为空(发送者)) {
                                File 文件 = new File( getActivity().getFilesDir().toString() + "/" + 当前用户.获取英语讯宝地址() + "/" + Constants.文件名起始字符串 + 发送序号或存储时间 + Constants.文件名结束字符串);
                                if (文件.exists()) {
                                    文件.delete();
                                }
                            }
                            if (数据库_删除讯宝(发送者, 聊天对象.小聊天群.编号, 聊天对象.讯友或群主.英语讯宝地址, 发送序号或存储时间)) {
                                String id;
                                if (SharedMethod.字符串未赋值或为空(发送者)) {
                                    id = String.valueOf(发送序号或存储时间);
                                } else {
                                    id = 发送者 + ":" + 发送序号或存储时间;
                                }
                                发送JS("function(){ RemoveSS(\"" + id + "\"); }");
                            }
                        } else if (聊天对象.大聊天群 != null) {
                            if (数据库_删除大聊天群讯宝(发送者, 聊天对象.大聊天群.编号, 聊天对象.大聊天群.子域名, 发送序号或存储时间)) {
                                String id;
                                if (SharedMethod.字符串未赋值或为空(发送者)) {
                                    id = String.valueOf(发送序号或存储时间);
                                } else {
                                    id = 发送者 + ":" + 发送序号或存储时间;
                                }
                                发送JS("function(){ RemoveSS(\"" + id + "\"); }");
                            }
                        }
                    } else {
                        if (数据库_删除陌生人讯宝(发送者)) {
                            发送JS("function(){ RemoveStrangerSS(\"" + 发送者 + "\"); }");
                        }
                    }
                } catch (Exception e) {
                    机器人.说(e.getMessage());
                }
            }
        });
        对话框.setNegativeButton(界面文字.获取(组名_任务, 1, "否"), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) { }
        });
        对话框.show();
    }

    private boolean 数据库_删除讯宝(String 英语讯宝地址, byte 群编号, String 讯友或群主英语讯宝地址, long 发送序号或存储时间) {
        try {
            String 路径 = getActivity().getFilesDir().toString() + "/" + 当前用户.获取英语讯宝地址();
            if (群编号 == 0) {
                File 文件 = new File(路径 + "/" + 讯友或群主英语讯宝地址 + ".sscj");
                if (文件.exists() && !文件.isDirectory()) {
                    RandomAccessFile 文件随机访问器 = new RandomAccessFile(文件.getAbsolutePath(), "rwd");
                    long SS包位置 = 文件随机访问器.length();
                    文件随机访问器.seek(SS包位置);
                    SSPackageReader SS包读取器;
                    long 发送序号;
                    do {
                        SS包读取器 = SharedMethod.读取一个SS包(文件随机访问器);
                        if (SS包读取器 == null) {
                            SS包读取器 = SharedMethod.读取一个SS包2(文件随机访问器);
                        }
                        if (SS包读取器 != null) {
                            if (SharedMethod.字符串未赋值或为空(英语讯宝地址)) {
                                if ((boolean) SS包读取器.读取_有标签("是接收者")) {
                                    if ((long) SS包读取器.读取_有标签("发送时间") == 发送序号或存储时间) {
                                        if (!(boolean)SS包读取器.读取_有标签("删除")) {
                                            SharedMethod.标为已删除(文件随机访问器, 文件随机访问器.getFilePointer(), SS包位置, SS包读取器);
                                            SharedMethod.删除相关文件(SS包读取器, getActivity(), 当前用户);
                                        }
                                        return true;
                                    }
                                }
                            } else {
                                if (!(boolean) SS包读取器.读取_有标签("是接收者")) {
                                    发送序号 = (long) SS包读取器.读取_有标签("发送序号");
                                    if (发送序号 == 发送序号或存储时间) {
                                        if (!(boolean) SS包读取器.读取_有标签("删除")) {
                                            SharedMethod.标为已删除(文件随机访问器, 文件随机访问器.getFilePointer(), SS包位置, SS包读取器);
                                            SharedMethod.删除相关文件(SS包读取器, getActivity(), 当前用户);
                                        }
                                        return true;
                                    } else if (发送序号 < 发送序号或存储时间) {
                                        return false;
                                    }
                                }
                            }
                            SS包位置 = 文件随机访问器.getFilePointer();
                        }
                    } while (SS包读取器 != null);
                    文件随机访问器.close();
                }
            } else {
                File 文件 = new File(路径 + "/" + 讯友或群主英语讯宝地址 + "#" + 群编号 + ".sscj");
                if (文件.exists() && !文件.isDirectory()) {
                    RandomAccessFile 文件随机访问器 = new RandomAccessFile(文件.getAbsolutePath(), "rwd");
                    long SS包位置 = 文件随机访问器.length();
                    文件随机访问器.seek(SS包位置);
                    SSPackageReader SS包读取器;
                    long 发送序号;
                    do {
                        SS包读取器 = SharedMethod.读取一个SS包(文件随机访问器);
                        if (SS包读取器 == null) {
                            SS包读取器 = SharedMethod.读取一个SS包2(文件随机访问器);
                        }
                        if (SS包读取器 != null) {
                            if (SharedMethod.字符串未赋值或为空(英语讯宝地址)) {
                                if (((String) SS包读取器.读取_有标签("发送者")).equals(当前用户.获取英语讯宝地址())) {
                                    if ((long) SS包读取器.读取_有标签("发送时间") == 发送序号或存储时间) {
                                        if (!(boolean)SS包读取器.读取_有标签("删除")) {
                                            SharedMethod.标为已删除(文件随机访问器, 文件随机访问器.getFilePointer(), SS包位置, SS包读取器);
                                            SharedMethod.删除相关文件(SS包读取器, getActivity(), 当前用户);
                                        }
                                        return true;
                                    }
                                }
                            } else {
                                if (((String) SS包读取器.读取_有标签("发送者")).equals(英语讯宝地址)) {
                                    发送序号 = (long) SS包读取器.读取_有标签("发送序号");
                                    if (发送序号 == 发送序号或存储时间) {
                                        if (!(boolean) SS包读取器.读取_有标签("删除")) {
                                            SharedMethod.标为已删除(文件随机访问器, 文件随机访问器.getFilePointer(), SS包位置, SS包读取器);
                                            SharedMethod.删除相关文件(SS包读取器, getActivity(), 当前用户);
                                        }
                                        return true;
                                    } else if (发送序号 < 发送序号或存储时间) {
                                        return false;
                                    }
                                }
                            }
                            SS包位置 = 文件随机访问器.getFilePointer();
                        }
                    } while (SS包读取器 != null);
                    文件随机访问器.close();
                }
            }
        } catch (Exception e) {
        }
        return false;
    }

    private boolean 数据库_删除大聊天群讯宝(String 英语讯宝地址, long 群编号, String 子域名, long 发送时间) {
        try {
            String 路径 = getActivity().getFilesDir().toString() + "/" + 当前用户.获取英语讯宝地址();
            File 文件 = new File(路径 + "/" + 子域名 + "#" + 群编号 + ".sscj");
            if (文件.exists() && !文件.isDirectory()) {
                RandomAccessFile 文件随机访问器 = new RandomAccessFile(文件.getAbsolutePath(), "rwd");
                long SS包位置 = 文件随机访问器.length();
                文件随机访问器.seek(SS包位置);
                SSPackageReader SS包读取器;
                do {
                    SS包读取器 = SharedMethod.读取一个SS包(文件随机访问器);
                    if (SS包读取器 == null) {
                        SS包读取器 = SharedMethod.读取一个SS包2(文件随机访问器);
                    }
                    if (SS包读取器 != null) {
                        if (SharedMethod.字符串未赋值或为空(英语讯宝地址)) {
                            if (((String) SS包读取器.读取_有标签("发送者")).equals(当前用户.获取英语讯宝地址())) {
                                if ((long) SS包读取器.读取_有标签("发送时间") == 发送时间) {
                                    if (!(boolean)SS包读取器.读取_有标签("删除")) {
                                        SharedMethod.标为已删除(文件随机访问器, 文件随机访问器.getFilePointer(), SS包位置, SS包读取器);
                                        SharedMethod.删除相关文件(SS包读取器, getActivity(), 当前用户);
                                    }
                                    return true;
                                }
                            }
                        } else {
                            if (((String) SS包读取器.读取_有标签("发送者")).equals(英语讯宝地址)) {
                                if ((long) SS包读取器.读取_有标签("发送时间") == 发送时间) {
                                    if (!(boolean)SS包读取器.读取_有标签("删除")) {
                                        SharedMethod.标为已删除(文件随机访问器, 文件随机访问器.getFilePointer(), SS包位置, SS包读取器);
                                        SharedMethod.删除相关文件(SS包读取器, getActivity(), 当前用户);
                                    }
                                    return true;
                                }
                            }
                        }
                        SS包位置 = 文件随机访问器.getFilePointer();
                    }
                } while (SS包读取器 != null);
                文件随机访问器.close();
            }
        } catch (Exception e) {
        }
        return false;
    }

    private boolean 数据库_删除陌生人讯宝(String 英语讯宝地址) {
        try {
            String 路径 = getActivity().getFilesDir().toString() + "/" + 当前用户.获取英语讯宝地址();
            File 文件 = new File(路径 + "/strangers.sscj");
            if (文件.exists() && !文件.isDirectory()) {
                RandomAccessFile 文件随机访问器 = new RandomAccessFile(文件.getAbsolutePath(), "rwd");
                long SS包位置 = 文件随机访问器.length();
                long 开始位置;
                文件随机访问器.seek(SS包位置);
                SSPackageReader SS包读取器;
                int 总数 = 0, 已删除的数量 = 0;
                do {
                    SS包读取器 = SharedMethod.读取一个SS包(文件随机访问器);
                    if (SS包读取器 == null) {
                        SS包读取器 = SharedMethod.读取一个SS包2(文件随机访问器);
                    }
                    if (SS包读取器 != null) {
                        总数 += 1;
                        if (!(boolean) SS包读取器.读取_有标签("删除")) {
                            if (英语讯宝地址.equals((String) SS包读取器.读取_有标签("讯宝地址"))) {
                                已删除的数量 += 1;
                            }
                        } else {
                            已删除的数量 += 1;
                        }
                        SS包位置 = 文件随机访问器.getFilePointer();
                    }
                } while (SS包读取器 != null);
                if (总数 > 已删除的数量) {
                    SS包位置 = 文件随机访问器.length();
                    文件随机访问器.seek(SS包位置);
                    do {
                        SS包读取器 = SharedMethod.读取一个SS包(文件随机访问器);
                        if (SS包读取器 == null) {
                            SS包读取器 = SharedMethod.读取一个SS包2(文件随机访问器);
                        }
                        if (SS包读取器 != null) {
                            总数 += 1;
                            if (!(boolean) SS包读取器.读取_有标签("删除")) {
                                if (英语讯宝地址.equals((String) SS包读取器.读取_有标签("讯宝地址"))) {
                                    开始位置 = 文件随机访问器.getFilePointer();
                                    SharedMethod.标为已删除(文件随机访问器, 开始位置, SS包位置, SS包读取器);
                                    已删除的数量 += 1;
                                    文件随机访问器.seek(开始位置);
                                }
                            } else {
                                已删除的数量 += 1;
                            }
                            SS包位置 = 文件随机访问器.getFilePointer();
                        }
                    } while (SS包读取器 != null);
                } else {
                    文件.delete();
                }
                文件随机访问器.close();
                return true;
            }
        } catch (Exception e) {
        }
        return false;
    }

    void 发送者撤回(String 发送者英语讯宝地址, long 发送序号) {
        发送JS("function(){var id = \"" + 发送者英语讯宝地址 + ":" + 发送序号 + "\"; RemoveSS(id); }");
        if (聊天对象.小聊天群.编号 == 0) {
            机器人.说(界面文字.获取(196, "讯友撤回了一条讯宝。"));
        } else {
            GroupMember 群成员[] = 聊天对象.小聊天群.群成员;
            int i;
            if (群成员 != null) {
                for (i = 0; i < 群成员.length; i++) {
                    if (群成员[i].英语讯宝地址.equals(发送者英语讯宝地址)) {
                        break;
                    }
                }
                if (i < 群成员.length) {
                    String 谁 = (SharedMethod.字符串未赋值或为空(群成员[i].本国语讯宝地址)? "" : 群成员[i].本国语讯宝地址 + " / ") + 发送者英语讯宝地址;
                    机器人.说(界面文字.获取(197, "#% 撤回了一条讯宝。", new Object[] {谁}));
                } else {
                    机器人.说(界面文字.获取(197, "#% 撤回了一条讯宝。", new Object[] {发送者英语讯宝地址}));
                }
            } else if (当前用户.讯友目录 != null) {
                Contact 讯友目录[] = 当前用户.讯友目录;
                for (i = 0; i < 讯友目录.length; i++) {
                    if (讯友目录[i].英语讯宝地址.equals(发送者英语讯宝地址)) {
                        break;
                    }
                }
                if (i < 讯友目录.length) {
                    String 谁 = (SharedMethod.字符串未赋值或为空(讯友目录[i].本国语讯宝地址)? "" : 讯友目录[i].本国语讯宝地址 + " / ") + 发送者英语讯宝地址;
                    机器人.说(界面文字.获取(197, "#% 撤回了一条讯宝。", new Object[] {谁}));
                } else {
                    机器人.说(界面文字.获取(197, "#% 撤回了一条讯宝。", new Object[] {发送者英语讯宝地址}));
                }
            } else {
                机器人.说(界面文字.获取(197, "#% 撤回了一条讯宝。", new Object[] {发送者英语讯宝地址}));
            }
        }
    }

    void 发送语音还是文字(boolean 语音) {
        if (发送语音 != 语音) {
            发送语音 = 语音;
            需要重新载入 = true;
        }
        LinearLayout 队列布局_文字 = (LinearLayout) 第一控件.findViewById(R.id.队列布局_文字);
        LinearLayout 队列布局_语音 = (LinearLayout) 第一控件.findViewById(R.id.队列布局_语音);
        if (语音) {
            队列布局_语音.setVisibility(View.VISIBLE);
            队列布局_文字.setVisibility(View.GONE);
        } else {
            队列布局_文字.setVisibility(View.VISIBLE);
            队列布局_语音.setVisibility(View.GONE);
        }
    }

    void 收到小宇宙的连接凭据(String 子域名, String 连接凭据, boolean 是商品编辑, boolean 是写入凭据) {
        if (!是写入凭据) {
            if (机器人 instanceof Robot_MainControl) {
                小宇宙控件.发送JS("function(){ ReadCredentialReady('" + 子域名 + "', '" + 替换HTML和JS敏感字符(连接凭据) + "', '" + 当前用户.英语用户名 + "', '" + 替换HTML和JS敏感字符(当前用户.获取英语讯宝地址()) + "', '" + (是商品编辑?"true":"false") + "'); }");
            } else if (机器人 instanceof Robot_LargeChatGroup) {
                if (子域名.startsWith("localhost:")) {
                    子域名 = ProtocolPath.IP地址_调试 + ":" + ProtocolPath.测试域名1小宇宙中心服务器本机IIS调试端口_SSL;
                }
                小宇宙控件.发送JS("function(){ ReadCredentialReady('" + 子域名 + "', '" + 替换HTML和JS敏感字符(连接凭据) + "'); }");
            } else {
                String 段[] = 聊天对象.讯友或群主.英语讯宝地址.split(ProtocolParameters.讯宝地址标识);
                小宇宙控件.发送JS("function(){ ReadCredentialReady('" + 子域名 + "', '" + 替换HTML和JS敏感字符(连接凭据) + "', '" + 段[0] + "', '" + 替换HTML和JS敏感字符(当前用户.获取英语讯宝地址()) + "'); }");
            }
        } else {
            小宇宙控件.发送JS("function(){ WriteCredentialReady('" + 子域名 + "', '" + 替换HTML和JS敏感字符(连接凭据) + "'); }");
        }
    }

    void 加载小聊天群的成员列表() {
        if (小宇宙控件 != null) {
            GroupMember 群成员[] = 聊天对象.小聊天群.群成员;
            if (群成员 != null) {
                GroupMember 某一成员;
                StringBuffer 字符串合并器 = new StringBuffer(200 * 群成员.length);
                字符串合并器.append("<xml><MEMBERS>");
                for (int i = 0; i < 群成员.length; i++) {
                    某一成员 = 群成员[i];
                    字符串合并器.append("<MEMBER>");
                    字符串合并器.append("<ENGLISH>" + 某一成员.英语讯宝地址 + "</ENGLISH>");
                    if (!SharedMethod.字符串未赋值或为空(某一成员.本国语讯宝地址)) {
                        字符串合并器.append("<NATIVE>" + 某一成员.本国语讯宝地址 + "</NATIVE>");
                    }
                    字符串合并器.append("<ROLE>" + 某一成员.角色 + "</ROLE>");
                    字符串合并器.append("<ICON>" + ProtocolPath.获取讯友头像路径(某一成员.英语讯宝地址, 某一成员.主机名, 0) + "</ICON>");
                    字符串合并器.append("</MEMBER>");
                }
                字符串合并器.append("</MEMBERS></xml>");
                小宇宙控件.发送JS("function(){ ListMembers('" + 字符串合并器.toString() + "'); }");
            } else {
                小宇宙控件.发送JS("function(){ ListMembers(''); }");
            }
        }
    }

}
