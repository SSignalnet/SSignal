package net.ssignal;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.os.PowerManager;
import android.util.Base64;
import android.view.inputmethod.EditorInfo;

import net.ssignal.network.MyX509TrustManager;
import net.ssignal.network.httpSetting;
import net.ssignal.protocols.Convert;
import net.ssignal.protocols.Group_Large;
import net.ssignal.protocols.ProtocolFormats;
import net.ssignal.protocols.ProtocolMethods;
import net.ssignal.protocols.ProtocolPath;
import net.ssignal.structure.Domain;
import net.ssignal.structure.ChatWith;
import net.ssignal.protocols.Contact;
import net.ssignal.protocols.GroupMember;
import net.ssignal.protocols.Group_Small;
import net.ssignal.protocols.ProtocolParameters;
import net.ssignal.protocols.SSPackageCreator;
import net.ssignal.protocols.SSPackageReader;
import net.ssignal.structure.SS_Sending;
import net.ssignal.util.Constants;
import net.ssignal.util.MyDate;
import net.ssignal.util.SSSocketException;
import net.ssignal.util.SharedMethod;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import static net.ssignal.Activity_Main.主活动;
import static net.ssignal.Fragment_Main.主窗体;
import static net.ssignal.User.当前用户;
import static net.ssignal.language.Text.界面文字;
import static net.ssignal.language.Text.组名_任务;
import static net.ssignal.network.encodeURI.替换URI敏感字符;

public class Robot_MainControl extends Robot {

    protected static Robot_MainControl 主控机器人;
    private Context 运行环境;

    Thread 线程_传送服务器;
    boolean 正在连接传送服务器 = false;
    boolean 从未自检 = true;
    boolean 不再提示 = false;
    byte 重试次数;
    final int 通知编号 = 1;

    private SS_Sending 正在发送的讯宝;
    private Thread 线程_发送SS;
    private Timer 定时器;
    private TimerTask 定时任务;
    private byte 读取未推送的讯宝的结果 = 0;

    private long 心跳确认时间 = 0;
    short 陌生人新讯宝数量 = 0;

    private PowerManager.WakeLock 防休眠锁;

    Robot_MainControl(Context 运行环境) {
        this.运行环境 = 运行环境;
        定时器 = new Timer();
        跨线程调用器 = new MyHandler(this);
    }

    Robot_MainControl(Context 运行环境, Fragment_Chating 聊天控件) {
        this.运行环境 = 运行环境;
        this.聊天控件 = 聊天控件;
        定时器 = new Timer();
        跨线程调用器 = new MyHandler(this);
    }

    boolean 自检() {
       return 自检(false, false);
    }

    private boolean 自检(boolean 询问, boolean 取消) {
        if (任务 != null) {
            任务.结束();
            任务 = null;
            if (取消) {
                输入框.setInputType(EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_VARIATION_NORMAL);
                输入框.setBackgroundColor(Color.WHITE);
                正在输入密码 = false;
                说(界面文字.获取(16, "已取消。"));
            }
        } else if (取消) {
            询问 = true;
        }
        if (当前用户.已登录()) {
            if (!自检_已登录()) {
                return false;
            }
            if (询问) {
                说(界面文字.获取(93, "需要我做什么？"));
            }
        } else {
            说(界面文字.获取(280, "欢迎使用讯宝！你还未登录，请<a>#%</a>。如果没有账号，可以<a>#%</a>一个。如果<a>#%</a>密码了，可以重设密码。", new Object[] {TaskName.任务名称_登录, TaskName.任务名称_注册, TaskName.任务名称_忘记}));
        }
        return true;
    }

    boolean 自检_已登录() {
        if (!当前用户.获取了账户信息) {
            获取账户信息(true);
            return false;
        }
        if (!当前用户.获取了密钥) {
            获取密钥();
            return false;
        }
        if (当前用户.网络连接器 == null) {
//            当前用户.记录运行步骤("连接传送服务器1");
            启动访问线程_传送服务器();
            return false;
        }
        return true;
    }

    void 获取密钥() {
        byte 字节数组[] = null;
        try {
            字节数组 = SharedMethod.读取文件的全部字节(运行环境.getFilesDir().toString() + "/" + 当前用户.获取英语讯宝地址() + "/keyiv.sspk");
            if (字节数组 != null) {
                收到密钥(new SSPackageReader(字节数组), true);
                return;
            }
        } catch (Exception e) {
        }
        if (!不再提示) {
            说(界面文字.获取(70, "正在获取密钥。请稍等。"));
        }
        任务 = new Task(TaskName.任务名称_获取密钥, this);
        启动HTTPS访问线程(new httpSetting(ProtocolPath.获取中心服务器访问路径开头(当前用户.域名_英语) + "C=GetKeyIV&UserID=" + 当前用户.编号 + "&Credential=" + 替换URI敏感字符(当前用户.凭据_中心服务器), 20000));
    }

    @Override
    void 回答(String 用户输入, long 时间) {
        if (当前用户.已登录()) {
            登录后回答(用户输入);
        } else {
            登录前回答(用户输入);
        }
    }

    private void 登录前回答(String 用户输入) {
        if (用户输入.equalsIgnoreCase(TaskName.任务名称_登录)) {
            任务 = new Task(用户输入, 输入框, this);
            任务.添加步骤(Task.任务步骤_讯宝地址, 界面文字.获取(4, "请输入你的讯宝地址。"));
            任务.添加步骤(Task.任务步骤_密码, 界面文字.获取(5, "请输入密码。"));
            说(任务.获取当前步骤提示语());
        } else if (用户输入.equalsIgnoreCase(TaskName.任务名称_注册)) {
            任务 = new Task(用户输入, 输入框, this);
            任务.添加步骤(Task.任务步骤_域名, 界面文字.获取(164, "请输入域名（如果你未收到来自某域名的邀请，可以输入域名 #% 或 #% 创建一个临时账号）。", new Object[] {ProtocolParameters.讯宝网络域名_英语, ProtocolParameters.讯宝网络域名_本国语}));
            任务.添加步骤(Task.任务步骤_手机号或电子邮箱地址, 界面文字.获取(26, "请输入你的电子邮箱地址，以用于接收验证码。请务必先了解一下我们的#%使用条款#%和#%隐私政策#%。", new Object[] {"<span class='TaskName' onclick='ToRobot2(\\\"TermsOfUse\\\")'>", "</span>", "<span class='TaskName' onclick='ToRobot2(\\\"PrivacyPolicy\\\")'>", "</span>"}));
            任务.添加步骤(Task.任务步骤_密码, 界面文字.获取(27, "请为你的账号设置密码。(最少#%个字符，最多#%个字符)", new Object[] {ProtocolParameters.最小值_密码长度, ProtocolParameters.最大值_密码长度}));
            任务.添加步骤(Task.任务步骤_重复密码, 界面文字.获取(28, "请再次输入相同的密码。"));
            任务.添加步骤(Task.任务步骤_验证码, 界面文字.获取(21, "请输入验证码。"));
            任务.需要获取验证码图片 = true;
            说(任务.获取当前步骤提示语());
        } else if (用户输入.equalsIgnoreCase(TaskName.任务名称_忘记)) {
            任务 = new Task(用户输入, 输入框, this);
            任务.添加步骤(Task.任务步骤_讯宝地址, 界面文字.获取(4, "请输入你的讯宝地址。"));
            任务.添加步骤(Task.任务步骤_验证码, 界面文字.获取(21, "请输入验证码。"));
            任务.需要获取验证码图片 = true;
            说(任务.获取当前步骤提示语());
        } else if (用户输入.equalsIgnoreCase(TaskName.任务名称_取消)) {
            自检(false, true);
        } else {
            任务接收用户输入(用户输入);
        }
    }

    private void 登录后回答(String 用户输入) {
        if (用户输入.equalsIgnoreCase(TaskName.任务名称_小宇宙)) {
            打开小宇宙页面();
        } else if (用户输入.equalsIgnoreCase(TaskName.任务名称_添加讯友)) {
            添加讯友(用户输入);
        } else if (用户输入.equalsIgnoreCase(TaskName.任务名称_删除讯友)) {
            删除讯友(用户输入);
        } else if (用户输入.equalsIgnoreCase(TaskName.任务名称_清理黑名单)) {
            取消拉黑讯友(用户输入);
        } else if (用户输入.equalsIgnoreCase(TaskName.任务名称_添加黑域)) {
            添加黑域(用户输入);
        } else if (用户输入.equalsIgnoreCase(TaskName.任务名称_添加白域)) {
            添加白域(用户输入);
        } else if (用户输入.equalsIgnoreCase(TaskName.任务名称_重命名标签)) {
            重命名标签(用户输入);
        } else if (用户输入.equalsIgnoreCase(TaskName.任务名称_拉黑)) {
            拉黑陌生人(用户输入);
        } else if (用户输入.equalsIgnoreCase(TaskName.任务名称_创建小聊天群)) {
            小聊天群(用户输入);
        } else if (用户输入.equalsIgnoreCase(TaskName.任务名称_创建大聊天群)) {
            大聊天群(用户输入);
        } else if (用户输入.equalsIgnoreCase(TaskName.任务名称_账户)) {
            获取账户信息(false);
        } else if (用户输入.equalsIgnoreCase(TaskName.任务名称_图标)) {
            选择头像图片(用户输入);
        } else if (用户输入.equalsIgnoreCase(TaskName.任务名称_密码)) {
            修改密码(用户输入);
        } else if (用户输入.equalsIgnoreCase(TaskName.任务名称_邮箱地址)) {
            修改邮箱地址(用户输入);
        } else if (用户输入.equalsIgnoreCase(TaskName.任务名称_手机号)) {
            修改手机号(用户输入);
        } else if (用户输入.equalsIgnoreCase(TaskName.任务名称_取消)) {
            自检(false, true);
        } else if (用户输入.equalsIgnoreCase(TaskName.任务名称_无法及时收到消息)) {
            说(界面文字.获取(313, "请允许本应用自启动。（它被清理后将无法接收消息）"));
            说(界面文字.获取(320, "请将本应用的省电策略改为无限制。（本应用的耗电量是非常小的）"));
            说(界面文字.获取(321, "请允许本应用显示通知。（某些手机是默认禁止应用通知的）"));
            说(界面文字.获取(322, "位置：设置>应用管理>讯宝"));
        } else if (用户输入.equalsIgnoreCase(TaskName.任务名称_注销)) {
            注销(用户输入);
        } else if (用户输入.equalsIgnoreCase(TaskName.任务名称_登录) ||
                   用户输入.equalsIgnoreCase(TaskName.任务名称_注册) ||
                   用户输入.equalsIgnoreCase(TaskName.任务名称_忘记)) {
            说(界面文字.获取(167, "你已登录。"));
        } else {
            if (任务 != null) {
                if (任务.名称.equalsIgnoreCase(TaskName.任务名称_移除黑域)) {
                    if (移除黑域2(用户输入)) { return; }
                } else if (任务.名称.equalsIgnoreCase(TaskName.任务名称_移除白域)) {
                    if (移除白域2(用户输入)) { return; }
                } else if (任务.名称.equalsIgnoreCase(TaskName.任务名称_注销)) {
                    if (注销2(用户输入)) { return; }
                }
            }
            任务接收用户输入(用户输入);
        }
    }

    private void 打开小宇宙页面() {
        if (SharedMethod.字符串未赋值或为空(当前用户.英语用户名)) {
            return;
        }
        if (聊天控件.小宇宙控件 != null) {
            聊天控件.小宇宙控件.网页载入完毕 = false;
            聊天控件.小宇宙控件.网页浏览器.loadUrl(ProtocolPath.获取当前用户小宇宙的访问路径(当前用户.英语用户名, 当前用户.域名_英语));
        }
        主窗体.左右滑动页容器.setCurrentItem(2);
    }

    private void 添加讯友(String 用户输入) {
        if (任务 != null) { 任务.结束(); }
        任务 = new Task(用户输入, 输入框, this);
        任务.添加步骤(Task.任务步骤_添加讯友, 界面文字.获取(98, "请输入一个讯宝地址。"));
        任务.添加步骤(Task.任务步骤_添加讯友备注, 界面文字.获取(100, "请为此讯友添加一个备注。如姓名、电话号码等。（不超过#%个字符）", new Object[] {ProtocolParameters.最大值_讯友备注字符数}));
        说(任务.获取当前步骤提示语());
    }

    private void 删除讯友(String 用户输入) {
        if (任务 != null) { 任务.结束(); }
        当前用户.显示讯友临时编号 = true;
        主窗体.刷新讯友录(Constants.讯友录显示范围_讯友);
        任务 = new Task(用户输入, 聊天控件.输入框, this);
        任务.添加步骤(Task.任务步骤_删除讯友, 界面文字.获取(82, "请输入讯友的讯宝地址或临时编号（讯友备注行括号内的数字）。"));
        说(任务.获取当前步骤提示语());
    }

    private void 取消拉黑讯友(String 用户输入) {
        if (任务 != null) { 任务.结束(); }
        Contact 讯友目录[] = 当前用户.讯友目录;
        if (讯友目录 != null) {
            int i;
            for (i = 0; i < 讯友目录.length; i++) {
                if (讯友目录[i].拉黑) {
                    break;
                }
            }
            if (i < 讯友目录.length) {
                当前用户.显示讯友临时编号 = true;
                主窗体.刷新讯友录(Constants.讯友录显示范围_黑名单);
                任务 = new Task(用户输入, 聊天控件.输入框, this);
                任务.添加步骤(Task.任务步骤_取消拉黑讯友, 界面文字.获取(82, "请输入讯友的讯宝地址或临时编号（讯友备注行括号内的数字）。"));
                说(任务.获取当前步骤提示语());
                return;
            }
        }
        说(界面文字.获取(116, "目前没有黑名单。"));
    }

    private void 添加黑域(String 用户输入) {
        if (任务 != null) { 任务.结束(); }
        if (当前用户.黑域 != null) {
            Domain 黑域[] = 当前用户.黑域;

            for (int i = 0; i < 黑域.length; i++) {
                if (黑域[i].equals(ProtocolParameters.黑域_全部)) {
                    说(界面文字.获取(243, "你已添加#%为黑域。", new Object[] {ProtocolParameters.黑域_全部}));
                    return;
                }
            }
        }
        Domain 可选域名[] = 添加黑域时统计可选域名();
        if (可选域名 != null) {
            Domain 某一可选域名;
            StringBuffer 字符串合并器 = new StringBuffer(可选域名.length * ProtocolParameters.最大值_域名长度);
            for (int i = 0; i < 可选域名.length; i++) {
                某一可选域名 = 可选域名[i];
                字符串合并器.append("<br>");
                字符串合并器.append("<a>" + 某一可选域名.英语 + "</a>");
                if (!某一可选域名.英语.equals(ProtocolParameters.黑域_全部)) {
                    if (!SharedMethod.字符串未赋值或为空(某一可选域名.本国语)) {
                        字符串合并器.append(" / " + 某一可选域名.本国语);
                    }
                } else {
                    字符串合并器.append(" (" + 界面文字.获取(236, "所有域")  + ")");
                }
            }
            任务 = new Task(用户输入, 输入框, this);
            任务.添加步骤(Task.任务步骤_添加黑域, 界面文字.获取(239, "陌生人给你发送文字讯宝时（陌生人只能发送文字），如来自黑域将会被屏蔽。请选择你不信任的域：#%", new Object[]{字符串合并器.toString()}));
            说(任务.获取当前步骤提示语());
        } else {
            说(界面文字.获取(238, "没有可选的域名。"));
        }
    }

    Domain[] 添加黑域时统计可选域名() {
        int i, j = 0, 可选域名数 = 0;
        Contact 讯友目录[] = 当前用户.讯友目录;
        if (讯友目录 != null) {
            for (i = 0; i < 讯友目录.length; i++) {
                if (讯友目录[i].拉黑) {
                    j++;
                }
            }
        }
        Domain 可选域名[] = new Domain[j + 1];
        Domain 黑域[] = 当前用户.黑域;
        if (黑域 != null) {
            for (i = 0; i < 黑域.length; i++) {
                if (黑域[i].英语.equals(ProtocolParameters.黑域_全部)) {
                    break;
                }
            }
            if (i == 黑域.length) {
                可选域名[可选域名数] = new Domain();
                可选域名[可选域名数].英语 = ProtocolParameters.黑域_全部;
                可选域名数++;
            }
        } else {
            可选域名[可选域名数] = new Domain();
            可选域名[可选域名数].英语 = ProtocolParameters.黑域_全部;
            可选域名数++;
        }
        if (j > 0) {
            Domain 白域[] = 当前用户.白域;
            String 段[], 域名;
            for (i = 0; i < 讯友目录.length; i++) {
                if (讯友目录[i].拉黑) {
                    段 = 讯友目录[i].英语讯宝地址.split(ProtocolParameters.讯宝地址标识);
                    域名 = 段[1];
                    if (白域 != null) {
                        for (j = 0; j < 白域.length; j++) {
                            if (白域[j].英语.equalsIgnoreCase(域名)) {
                                break;
                            }
                        }
                        if (j < 白域.length) {
                            continue;
                        }
                    }
                    if (黑域 != null) {
                        for (j = 0; j < 黑域.length; j++) {
                            if (黑域[j].英语.equalsIgnoreCase(域名)) {
                                break;
                            }
                        }
                        if (j < 黑域.length) {
                            continue;
                        }
                    }
                    if (可选域名数 > 0) {
                        for (j = 0; j < 可选域名数; j++) {
                            if (可选域名[j].英语.equals(域名)) {
                                break;
                            }
                        }
                        if (j < 可选域名.length) {
                            continue;
                        }
                    }
                    可选域名[可选域名数] = new Domain();
                    可选域名[可选域名数].英语 = 域名;
                    if (!SharedMethod.字符串未赋值或为空(讯友目录[i].本国语讯宝地址)) {
                        段 = 讯友目录[i].本国语讯宝地址.split(ProtocolParameters.讯宝地址标识);
                        可选域名[可选域名数].本国语 = 段[1];
                    }
                    可选域名数++;
                }
            }
        }
        if (可选域名数 > 0) {
            if (可选域名数 < 可选域名.length) {
                Domain 可选域名2[] = new Domain[可选域名数];
                System.arraycopy(可选域名, 0, 可选域名2, 0, 可选域名.length);
                return 可选域名2;
            } else {
                return 可选域名;
            }
        } else {
            return null;
        }
    }

    private void 添加白域(String 用户输入) {
        if (任务 != null) { 任务.结束(); }
        Domain 黑域[] = 当前用户.黑域;
        if (黑域 != null) {
            int i;
            for (i = 0; i < 黑域.length; i++) {
                if (黑域[i].英语.equals(ProtocolParameters.黑域_全部)) {
                    break;
                }
            }
            if (i == 黑域.length) {
                说(界面文字.获取(242, "未添加#%为黑域时，没有必要添加白域。", new Object[]{ProtocolParameters.黑域_全部}));
                return;
            }
        } else {
            说(界面文字.获取(242, "未添加#%为黑域时，没有必要添加白域。", new Object[]{ProtocolParameters.黑域_全部}));
            return;
        }
        Domain 可选域名[] = 添加白域时统计可选域名();
        if (可选域名 != null) {
            Domain 某一可选域名;
            StringBuffer 字符串合并器 = new StringBuffer(可选域名.length * ProtocolParameters.最大值_域名长度);
            for (int i = 0; i < 可选域名.length; i++) {
                某一可选域名 = 可选域名[i];
                字符串合并器.append("<br>");
                字符串合并器.append("<a>" + 某一可选域名.英语 + "</a>");
                if (!SharedMethod.字符串未赋值或为空(某一可选域名.本国语)) {
                    字符串合并器.append(" / " + 某一可选域名.本国语);
                }
            }
            任务 = new Task(用户输入, 输入框, this);
            任务.添加步骤(Task.任务步骤_添加白域, 界面文字.获取(240, "陌生人给你发送文字讯宝时（陌生人只能发送文字），如来自白域将不会被屏蔽。请选你信任的域：#%", new Object[]{字符串合并器.toString()}));
            说(任务.获取当前步骤提示语());
        } else {
            说(界面文字.获取(238, "没有可选的域名。"));
        }
    }

    Domain[] 添加白域时统计可选域名() {
        Contact 讯友目录[] = 当前用户.讯友目录;
        if (讯友目录 == null) {
            return null;
        }
        Domain 可选域名[] = null;
        int i, j = 0, 可选域名数 = 0;
        for (i = 0; i < 讯友目录.length; i++) {
            if (!讯友目录[i].拉黑) {
                j++;
            }
        }
        if (j > 0) {
            Domain 黑域[] = 当前用户.黑域;
            Domain 白域[] = 当前用户.白域;
            String 段[], 域名;
            可选域名 = new Domain[j];
            for (i = 0; i < 讯友目录.length; i++) {
                if (!讯友目录[i].拉黑) {
                    段 = 讯友目录[i].英语讯宝地址.split(ProtocolParameters.讯宝地址标识);
                    域名 = 段[1];
                    if (白域 != null) {
                        for (j = 0; j < 白域.length; j++) {
                            if (白域[j].英语.equalsIgnoreCase(域名)) {
                                break;
                            }
                        }
                        if (j < 白域.length) {
                            continue;
                        }
                    }
                    if (黑域 != null) {
                        for (j = 0; j < 黑域.length; j++) {
                            if (黑域[j].英语.equalsIgnoreCase(域名)) {
                                break;
                            }
                        }
                        if (j < 黑域.length) {
                            continue;
                        }
                    }
                    if (可选域名数 > 0) {
                        for (j = 0; j < 可选域名数; j++) {
                            if (可选域名[j].英语.equals(域名)) {
                                break;
                            }
                        }
                        if (j < 可选域名.length) {
                            continue;
                        }
                    }
                    可选域名[可选域名数] = new Domain();
                    可选域名[可选域名数].英语 = 域名;
                    if (!SharedMethod.字符串未赋值或为空(讯友目录[i].本国语讯宝地址)) {
                        段 = 讯友目录[i].本国语讯宝地址.split(ProtocolParameters.讯宝地址标识);
                        可选域名[可选域名数].本国语 = 段[1];
                    }
                    可选域名数++;
                }
            }
        }
        if (可选域名数 > 0) {
            if (可选域名数 < 可选域名.length) {
                Domain 可选域名2[] = new Domain[可选域名数];
                System.arraycopy(可选域名, 0, 可选域名2, 0, 可选域名.length);
                return 可选域名2;
            } else {
                return 可选域名;
            }
        } else {
            return null;
        }
    }

    void 移除黑域(Domain 域名) {
        if (任务 != null) { 任务.结束(); }
        String 名称 = 域名.英语;
        if (!SharedMethod.字符串未赋值或为空(域名.本国语)) {
            名称 += "/" + 域名.本国语;
        }
        任务 = new Task(TaskName.任务名称_移除黑域, 输入框, this);
        任务.添加步骤(Task.任务步骤_移除黑白域, "", 域名.英语);
        说(界面文字.获取(244, "你要将域[#%]从列表中移除吗？请选择<a>#%</a>或者<a>#%</a>。", new Object[]{名称, 界面文字.获取(组名_任务, 0, "是"), 界面文字.获取(组名_任务, 1, "否")}));
    }

    private boolean 移除黑域2(String 用户输入) {
        if (用户输入.equalsIgnoreCase(界面文字.获取(组名_任务, 0, "是"))) {
            if (数据库_保存要发送的一对一讯宝( 当前用户.获取英语讯宝地址(), SharedMethod.获取当前UTC时间(), ProtocolParameters.讯宝指令_移除黑域, 任务.获取某步骤的输入值(Task.任务步骤_移除黑白域), (short)0, (short)0, (byte)0)) {
                发送讯宝(false);
            }
            任务.结束();
            return true;
        } else if (用户输入.equalsIgnoreCase(界面文字.获取(组名_任务, 1, "否"))) {
            回答(TaskName.任务名称_取消, 0);
            return true;
        }
        return false;
    }

    void 移除白域(Domain 域名) {
        if (任务 != null) { 任务.结束(); }
        String 名称 = 域名.英语;
        if (!SharedMethod.字符串未赋值或为空(域名.本国语)) {
            名称 += "/" + 域名.本国语;
        }
        任务 = new Task(TaskName.任务名称_移除白域, 输入框, this);
        任务.添加步骤(Task.任务步骤_移除黑白域, "", 域名.英语);
        说(界面文字.获取(244, "你要将域[#%]从列表中移除吗？请选择<a>#%</a>或者<a>#%</a>。", new Object[]{名称, 界面文字.获取(组名_任务, 0, "是"), 界面文字.获取(组名_任务, 1, "否")}));
    }

    private boolean 移除白域2(String 用户输入) {
        if (用户输入.equalsIgnoreCase(界面文字.获取(组名_任务, 0, "是"))) {
            if (数据库_保存要发送的一对一讯宝( 当前用户.获取英语讯宝地址(), SharedMethod.获取当前UTC时间(), ProtocolParameters.讯宝指令_移除白域, 任务.获取某步骤的输入值(Task.任务步骤_移除黑白域), (short)0, (short)0, (byte)0)) {
                发送讯宝(false);
            }
            任务.结束();
            return true;
        } else if (用户输入.equalsIgnoreCase(界面文字.获取(组名_任务, 1, "否"))) {
            回答(TaskName.任务名称_取消, 0);
            return true;
        }
        return false;
    }

    private void 重命名标签(String 用户输入) {
        if (任务 != null) { 任务.结束(); }
        Contact 讯友目录[] = 当前用户.讯友目录;
        if (讯友目录 != null) {
            String 讯友标签[] = new String[讯友目录.length * 2];
            int 讯友标签数 = 0;
            for (int i = 0; i < 讯友目录.length; i++) {
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
                }
                Arrays.sort(讯友标签);
                StringBuffer 字符串合并器 = new StringBuffer(讯友标签数 * ProtocolParameters.最大值_讯友标签字符数);
                for (int i = 0; i < 讯友标签数; i++) {
                    if (i > 0) {
                        字符串合并器.append(", ");
                    }
                    字符串合并器.append("<a>" + 讯友标签[i] + "</a>");
                }
                任务 = new Task(用户输入, 输入框, this);
                任务.添加步骤(Task.任务步骤_原标签名称, 界面文字.获取(143, "请选择你要重命名的标签名称：#%", new Object[] {字符串合并器.toString()}));
                任务.添加步骤(Task.任务步骤_新标签名称, 界面文字.获取(144, "请输入新名称。（不超过#%个字符）", new Object[] {ProtocolParameters.最大值_讯友标签字符数}));
                说(任务.获取当前步骤提示语());
                return;
            }
        }
        说(界面文字.获取(88, "目前没有标签"));
    }

    private void 拉黑陌生人(String 用户输入) {
        if (任务 != null) { 任务.结束(); }
        任务 = new Task(用户输入, 输入框, this);
        任务.添加步骤(Task.任务步骤_添加讯友, 界面文字.获取(98, "请输入一个讯宝地址。"));
        任务.添加步骤(Task.任务步骤_添加讯友备注, 界面文字.获取(99, "请添加一个备注。（不超过#%个字符）", new Object[] {ProtocolParameters.最大值_讯友备注字符数}));
        说(任务.获取当前步骤提示语());
    }

    void 获取账户信息(boolean 读取本机缓存数据) {
        if (读取本机缓存数据) {
            byte 字节数组[] = null;
            try {
                字节数组 = SharedMethod.读取文件的全部字节(运行环境.getFilesDir().toString() + "/accountinfo.sspk");
                if (字节数组 != null) {
                    解读账户和登录信息(字节数组, true);
                    自检(false, false);
                    return;
                }
            } catch (Exception e) {
            }
        }
        if (任务 != null) { 任务.结束(); }
        任务 = new Task(TaskName.任务名称_账户, this);
        说(界面文字.获取(77, "正在获取账户信息。"));
        Calendar 日历 = Calendar.getInstance();
        TimeZone 时区 = 日历.getTimeZone();
        int 本机时间偏移量_分钟 = 时区.getOffset(System.currentTimeMillis()) / 1000 / 60;
        启动HTTPS访问线程(new httpSetting(ProtocolPath.获取中心服务器访问路径开头(当前用户.域名_英语) + "C=AccountInfo&UserID=" + 当前用户.编号 + "&Credential=" + 替换URI敏感字符(当前用户.凭据_中心服务器) + "&TimezoneOffset=" + 本机时间偏移量_分钟));
    }

    private void 小聊天群(String 用户输入) {
        if (任务 != null) { 任务.结束(); }
        Group_Small 加入的群[] = 当前用户.加入的小聊天群;
        if (加入的群 != null) {
            String 英语讯宝地址 = 当前用户.获取英语讯宝地址();
            int i, j = 0;
            for (i = 0; i < 加入的群.length; i++) {
                if (英语讯宝地址.equals(加入的群[i].群主.英语讯宝地址)) {
                    j += 1;
                }
            }
            if (j >= ProtocolParameters.最大值_每个用户可创建的小聊天群数量) {
                说(界面文字.获取(126, "你创建的小聊天群数量已达上限。"));
                return;
            }
        }
        任务 = new Task(用户输入, 输入框, this);
        任务.添加步骤(Task.任务步骤_小聊天群名称, 界面文字.获取(87, "你将创建一个小聊天群（最多#%个成员；新讯宝由服务器实时推送给成员）。请为其输入一个名称。（不超过#%个字符）", new Object[] {ProtocolParameters.最大值_小聊天群成员数量, ProtocolParameters.最大值_群名称字符数}));
        说(任务.获取当前步骤提示语());
    }

    private void 大聊天群(String 用户输入) {
        if (任务 != null) { 任务.结束(); }
        任务 = new Task(用户输入, 输入框, this);
        任务.添加步骤(Task.任务步骤_大聊天群名称, 界面文字.获取(271, "你将创建一个大聊天群（成员数没有限制，取决于服务器的容量；新讯宝不会被服务器实时推送给成员，而是由成员的客户端每隔几分钟接收一次）。请为其输入一个名称。（不超过#%个字符）", new Object[] {ProtocolParameters.最大值_群名称字符数}));
        任务.添加步骤(Task.任务步骤_大聊天群估计成员数, 界面文字.获取(273, "你的新群预计会有多少成员？"));
        说(任务.获取当前步骤提示语());
    }

    private void 选择头像图片(String 用户输入) {
        if (任务 != null) { 任务.结束(); }
        if (Build.VERSION.SDK_INT >= 23) {
            if (!主活动.选取文件前检查读取存储卡的权限()) {
                说(界面文字.获取(262, "请赋予我读取存储卡的权限。"));
                主活动.选取图片前请求读取存储卡的权限(this);
                return;
            }
        }
        说(界面文字.获取(59, "请选择一幅图片。"));
        主活动.选择图片(this);
    }

    void 选择头像图片2(String 图片路径, String 保存路径) {
        String 文件路径;
        try {
            Bitmap 位图 = BitmapFactory.decodeFile(图片路径);
            if (位图.getWidth() < ProtocolParameters.长度_图标宽高_像素 || 位图.getHeight() < ProtocolParameters.长度_图标宽高_像素) {
                说(界面文字.获取(168, "图片太小。"));
                return;
            }
            Bitmap 位图2 = Bitmap.createBitmap(ProtocolParameters.长度_图标宽高_像素, ProtocolParameters.长度_图标宽高_像素, Bitmap.Config.ARGB_8888);
            Canvas 绘图器 = new Canvas(位图2);
            Rect 矩形1, 矩形2;
            if (位图.getHeight() > 位图.getWidth()) {
                int 上 = (int) ((位图.getHeight() - 位图.getWidth()) / 2);
                矩形1 = new Rect(0, 上, 位图.getWidth(), 上 + 位图.getWidth());
            } else {
                int 左 = (int) ((位图.getWidth() - 位图.getHeight()) / 2);
                矩形1 = new Rect(左, 0, 左 + 位图.getHeight(), 位图.getHeight());
            }
            矩形2 = new Rect(0, 0, 位图2.getWidth(), 位图2.getHeight());
            绘图器.drawBitmap(位图, 矩形1, 矩形2, null);
            文件路径 = 保存路径 + "/" + 当前用户.获取英语讯宝地址();
            File 目录 = new File(文件路径);
            if (!目录.exists() || !目录.isDirectory()) {
                目录.mkdir();
            }
            文件路径 += "/" + SharedMethod.生成大小写英文字母与数字的随机字符串(20) + ".jpg";
            SharedMethod.保存位图(位图2, 文件路径);
        } catch (Exception e) {
            说(e.getMessage());
            return;
        }
        if (数据库_保存要发送的一对一讯宝(当前用户.获取英语讯宝地址(), SharedMethod.获取当前UTC时间(), ProtocolParameters.讯宝指令_修改图标, 文件路径, (short)0, (short)0, (byte)0)) {
            说(界面文字.获取(7, "请稍等。"));
            发送讯宝(false);
        }
    }

    private void 修改密码(String 用户输入) {
        if (任务 != null) { 任务.结束(); }
        任务 = new Task(用户输入, 输入框, this);
        任务.添加步骤(Task.任务步骤_密码, 界面文字.获取(225, "你要修改密码吗？请输入新密码。（最少#%个字符，最多#%个字符）", new Object[] {ProtocolParameters.最小值_密码长度, ProtocolParameters.最大值_密码长度}));
        任务.添加步骤(Task.任务步骤_重复密码, 界面文字.获取(28, "请再次输入相同的密码。"));
        任务.添加步骤(Task.任务步骤_当前密码, 界面文字.获取(228, "请输入当前密码。"));
        说(任务.获取当前步骤提示语());
    }

    private void 修改邮箱地址(String 用户输入) {
        if (任务 != null) { 任务.结束(); }
        任务 = new Task(用户输入, 输入框, this);
        任务.添加步骤(Task.任务步骤_电子邮箱地址, 界面文字.获取(226, "你要修改电子邮箱地址吗？请输入新的电子邮箱地址。"));
        任务.添加步骤(Task.任务步骤_当前密码, 界面文字.获取(228, "请输入当前密码。"));
        说(任务.获取当前步骤提示语());
    }

    private void 修改手机号(String 用户输入) {
        if (任务 != null) { 任务.结束(); }
        任务 = new Task(用户输入, 输入框, this);
        任务.添加步骤(Task.任务步骤_手机号, 界面文字.获取(227, "你要修改手机号吗？请输入新的手机号。"));
        任务.添加步骤(Task.任务步骤_当前密码, 界面文字.获取(228, "请输入当前密码。"));
        说(任务.获取当前步骤提示语());
    }

    private void 注销(String 用户输入) {
        if (任务 != null) { 任务.结束(); }
        任务 = new Task(用户输入, this);
        说(界面文字.获取(122, "你要注销吗？请选择<a>#%</a>或者<a>#%</a>。", new Object[] {界面文字.获取(组名_任务, 0, "是"), 界面文字.获取(组名_任务, 1, "否")}));
    }

    private boolean 注销2(String 用户输入) {
        if (用户输入.equalsIgnoreCase(界面文字.获取(组名_任务, 0, "是"))) {
            说(界面文字.获取(7, "请稍等。"));
            任务 = new Task(TaskName.任务名称_注销, 输入框, this);
            启动HTTPS访问线程(new httpSetting(ProtocolPath.获取中心服务器访问路径开头(当前用户.域名_英语) + "C=Logout&UserID=" + 当前用户.编号 + "&Credential=" + 替换URI敏感字符(当前用户.凭据_中心服务器), 20000));
            return true;
        } else if (用户输入.equalsIgnoreCase(界面文字.获取(组名_任务, 1, "否"))) {
            回答(TaskName.任务名称_取消, 0);
            return true;
        } else {
            return false;
        }
    }

    private void 任务接收用户输入(String 用户输入) {
        if (任务 != null) {
            if (任务.步骤数量 > 0) {
                String 结果 = 任务.保存当前步骤输入值(用户输入);
                if (SharedMethod.字符串未赋值或为空(结果)) {
                    结果 = 任务.获取当前步骤提示语();
                    if (!SharedMethod.字符串未赋值或为空(结果)) {
                        说(结果);
                    } else {
                        if (任务.名称.equalsIgnoreCase(TaskName.任务名称_添加讯友) || 任务.名称.equalsIgnoreCase(TaskName.任务名称_拉黑)) {
                            SSPackageCreator SS包生成器 = new SSPackageCreator();
                            try {
                                SS包生成器.添加_有标签("发送序号", 当前用户.讯宝发送序号);
                                SS包生成器.添加_有标签("讯宝地址", 任务.获取某步骤的输入值(Task.任务步骤_添加讯友));
                                SS包生成器.添加_有标签("备注", 任务.获取某步骤的输入值(Task.任务步骤_添加讯友备注));
                                if (任务.名称.equalsIgnoreCase(TaskName.任务名称_拉黑)) {
                                    SS包生成器.添加_有标签("拉黑", true);
                                    任务.名称 = TaskName.任务名称_添加讯友;
                                } else {
                                    SS包生成器.添加_有标签("拉黑", false);
                                }
                                说(界面文字.获取(7, "请稍等。"));
                                启动HTTPS访问线程(new httpSetting(ProtocolPath.获取传送服务器访问路径开头(当前用户.主机名, 当前用户.域名_英语, false) + "C=AddContact&UserID=" + 当前用户.编号 + "&Position=" + 当前用户.位置号 + "&DeviceType=" + ProtocolParameters.设备类型_文本_手机, 20000, SS包生成器.生成SS包(当前用户.AES加密器)));
                            } catch (Exception e) {
                                return;
                            }
                        } else if (任务.名称.equalsIgnoreCase(TaskName.任务名称_删除讯友)) {
                            Contact 讯友目录[] = 当前用户.讯友目录;
                            if (讯友目录 == null) { return; }
                            String 英语讯宝地址 = 任务.获取某步骤的输入值(Task.任务步骤_删除讯友);
                            int i;
                            for (i = 0; i < 讯友目录.length; i++) {
                                if (讯友目录[i].英语讯宝地址.equals(英语讯宝地址)) {
                                    break;
                                }
                            }
                            if (i < 讯友目录.length) {
                                if (数据库_保存要发送的一对一讯宝( 当前用户.获取英语讯宝地址(), SharedMethod.获取当前UTC时间(), ProtocolParameters.讯宝指令_删除讯友, 英语讯宝地址, (short)0, (short)0, (byte)0)) {
                                    发送讯宝(false);
                                }
                            }
                            任务.结束();
                        } else if (任务.名称.equalsIgnoreCase(TaskName.任务名称_清理黑名单)) {
                            String 英语讯宝地址 = 任务.获取某步骤的输入值(Task.任务步骤_取消拉黑讯友);
                            Contact 讯友 = 当前用户.查找讯友(英语讯宝地址);
                            if (讯友 != null) {
                                SSPackageCreator SS包生成器 = new SSPackageCreator();
                                try {
                                    SS包生成器.添加_有标签("英语讯宝地址", 英语讯宝地址);
                                    SS包生成器.添加_有标签("拉黑", false);
                                    if (!数据库_保存要发送的一对一讯宝(当前用户.获取英语讯宝地址(), SharedMethod.获取当前UTC时间(), ProtocolParameters.讯宝指令_拉黑取消拉黑讯友, SS包生成器.生成纯文本(), (short) 0, (short) 0, (byte) 0)) {
                                        return;
                                    }
                                } catch (Exception e) {
                                    return;
                                }
                                发送讯宝(false);
                            }
                            任务.结束();
                        } else if (任务.名称.equalsIgnoreCase(TaskName.任务名称_重命名标签)) {
                            String 原标签名称 = 任务.获取某步骤的输入值(Task.任务步骤_原标签名称);
                            String 新标签名称 = 任务.获取某步骤的输入值(Task.任务步骤_新标签名称);
                            SSPackageCreator SS包生成器 = new SSPackageCreator();
                            try {
                                SS包生成器.添加_有标签("原标签名称", 原标签名称);
                                SS包生成器.添加_有标签("新标签名称", 新标签名称);
                                if (!数据库_保存要发送的一对一讯宝(当前用户.获取英语讯宝地址(), SharedMethod.获取当前UTC时间(), ProtocolParameters.讯宝指令_重命名讯友标签, SS包生成器.生成纯文本(), (short) 0, (short) 0, (byte) 0)) {
                                    return;
                                }
                            } catch (Exception e) {
                                return;
                            }
                            发送讯宝(false);
                            任务.结束();
                        } else if (任务.名称.equalsIgnoreCase(TaskName.任务名称_添加黑域)) {
                            if (数据库_保存要发送的一对一讯宝( 当前用户.获取英语讯宝地址(), SharedMethod.获取当前UTC时间(), ProtocolParameters.讯宝指令_添加黑域, 任务.获取某步骤的输入值(Task.任务步骤_添加黑域), (short)0, (short)0, (byte)0)) {
                                发送讯宝(false);
                            }
                            任务.结束();
                        } else if (任务.名称.equalsIgnoreCase(TaskName.任务名称_添加白域)) {
                            if (数据库_保存要发送的一对一讯宝( 当前用户.获取英语讯宝地址(), SharedMethod.获取当前UTC时间(), ProtocolParameters.讯宝指令_添加白域, 任务.获取某步骤的输入值(Task.任务步骤_添加白域), (short)0, (short)0, (byte)0)) {
                                发送讯宝(false);
                            }
                            任务.结束();
                        } else if (任务.名称.equalsIgnoreCase(TaskName.任务名称_创建小聊天群)) {
                            if (数据库_保存要发送的一对一讯宝( 当前用户.获取英语讯宝地址(), SharedMethod.获取当前UTC时间(), ProtocolParameters.讯宝指令_创建小聊天群, 任务.获取某步骤的输入值(Task.任务步骤_小聊天群名称), (short)0, (short)0, (byte)0)) {
                                说(界面文字.获取(7, "请稍等。"));
                                发送讯宝(false);
                            }
                        } else {
                            启动HTTPS访问线程(任务.生成访问设置());
                        }
                    }
                } else {
                    说(结果);
                }
                return;
            }
        }
        if (当前用户.已登录()) {
            自检(true, false);
        } else {
            任务 = new Task(TaskName.任务名称_登录, 输入框, this);
            任务.添加步骤(Task.任务步骤_讯宝地址, 界面文字.获取(4, "请输入你的讯宝地址。"));
            任务.添加步骤(Task.任务步骤_密码, 界面文字.获取(5, "请输入密码。"));
            String 结果 = 任务.保存当前步骤输入值(用户输入);
            if (SharedMethod.字符串未赋值或为空(结果)) {
                说(任务.获取当前步骤提示语());
            } else {
                任务 = null;
                自检(true, false);
            }
        }
    }


    void 处理跨线程数据(Message msg) {
        switch (msg.what) {
            case 5:
                byte SS包[] = msg.getData().getByteArray("SS包");
                try {
                    SSPackageReader SS包解读器 = new SSPackageReader(SS包);
                    收到讯宝(SS包解读器, true);
                } catch (Exception e) {
                    说(e.getMessage());
                    return;
                }
                break;
            case 6:
                线程_发送SS = null;
                boolean 继续 = msg.getData().getBoolean("继续");
                if (继续) {
                    发送讯宝(true);
                } else {
                    正在发送的讯宝 = null;
                    防止休眠_结束();
                }
                break;
            case 9:
                if (定时任务 != null) {
                    定时任务.cancel();
                }
                定时任务 = new MyTimerTask(跨线程调用器, 10);
                定时器.schedule(定时任务, 20000);
                线程_发送SS = null;
                break;
            case 7:
                String 子域名 = msg.getData().getString("子域名");
                long 群编号 = msg.getData().getLong("群编号");
                int 新讯宝数量 = msg.getData().getInt("新讯宝数量");
                数据库_更新最近互动讯友排名(子域名, 群编号);
                数据库_更新新讯宝数量(子域名, 群编号, (short) 新讯宝数量);
                Group_Large 加入的大聊天群[] = 当前用户.加入的大聊天群;
                for (int i = 0; i < 加入的大聊天群.length; i++) {
                    if (加入的大聊天群[i].编号 == 群编号 && 加入的大聊天群[i].子域名.equals(子域名)) {
                        加入的大聊天群[i].新讯宝数量 = (short) 新讯宝数量;
                        break;
                    }
                }
                if (主窗体 != null) {
                    主窗体.显示大聊天群新讯宝数量(子域名, 群编号, 新讯宝数量);
                }
                break;
            case 1:
                HTTPS请求成功(msg.getData().getByteArray("SS包"));
                break;
            case 2:
                String 原因 = SharedMethod.替换HTML和JS敏感字符(msg.getData().getString("原因"));
                boolean 结束 = msg.getData().getBoolean("结束");
                if (!结束) {
                    说(界面文字.获取(12, "#% 正在重试", new Object[] {原因}));
                } else {
                    if (聊天控件 != null) { 聊天控件.按钮和机器人图标(false); }
                    说(原因);
                    if (任务 != null) {
                        if (任务.名称.equals(TaskName.任务名称_发流星语)) {
                            流星语发布结束(false);
                        } else if (任务.名称.equals(TaskName.任务名称_发布商品)) {
                            商品发布结束(false);
                        } else if (任务.名称.equals(TaskName.任务名称_注销)) {
                            if (主活动 != null) {
                                主活动.注销成功();
                            }
                        }
                        任务.结束();
                        任务 = null;
                    }
                }
                break;
            case 3:
                正在连接传送服务器 = false;
                if (聊天控件 != null) {
                    聊天控件.按钮和机器人图标(false);
                }
                if (主窗体 != null) {
                    主窗体.刷新讯友录(Constants.讯友录显示范围_最近, null, true);
                }
                if (!不再提示) {
                    boolean 在线 = msg.getData().getBoolean("在线");
                    if (在线) {
                        说(界面文字.获取(204, "你的电脑也在线。"));
                    } else {
                        说(界面文字.获取(205, "你的电脑不在线。"));
                    }
                }
                boolean 成功 = msg.getData().getBoolean("成功");
                if (成功) {
                    if (!不再提示) {
                        说(界面文字.获取(56, "你可以收发讯宝了。（每小时最多可发送#%条讯宝，每天最多可发送#%条讯宝）", new Object[] {ProtocolParameters.最大值_每小时可发送讯宝数量, ProtocolParameters.最大值_每天可发送讯宝数量}));
                        if (当前用户.黑域 != null) {
                            Domain 黑域[] = 当前用户.黑域;
                            for (int i = 0; i < 黑域.length; i++) {
                                if (黑域[i].英语.equals(ProtocolParameters.黑域_全部)) {
                                    说(界面文字.获取(72, "目前拒绝除白域以外任何陌生人发来的讯宝。"));
                                    break;
                                }
                            }
                        }
                        if (当前用户.域名_英语.equals(ProtocolParameters.讯宝网络域名_英语)) {
                            说("温馨提示：你注册的账号只是用于体验的临时账号，会在注册成功1个小时后失效。");
                        }
                    }
                    if (主窗体 != null) {
                        主窗体.显示自启动提示();
                    }
                    if (!不再提示) {
                        不再提示 = true;
                    }
                }
                提示新任务();
                if (成功) {
                    if (聊天控件 != null) {
                        if (!聊天控件.载入了陌生人讯宝) {
                            聊天控件.载入了陌生人讯宝 = true;
                            聊天控件.载入最近聊天记录();
                        }
                    }
                    Context 运行环境2 = 运行环境.getApplicationContext();
                    Service_SSignal.启动服务(运行环境2, 1);
                    boolean 打开小宇宙 = msg.getData().getBoolean("打开小宇宙");
                    if (打开小宇宙 && 聊天控件 != null) {
                        if (聊天控件.小宇宙控件 != null) {
                            聊天控件.小宇宙控件.网页浏览器.loadUrl(ProtocolPath.获取当前用户小宇宙的访问路径(当前用户.英语用户名, 当前用户.域名_英语));
                        }
                    }
                    发送讯宝(false);
                } else {
//                    当前用户.记录运行步骤("连接传送服务器2");
                    启动访问线程_传送服务器();
                }
                break;
            case 11:
                SS包 = msg.getData().getByteArray("SS包");
                try {
                    SSPackageReader SS包解读器 = new SSPackageReader(SS包);
                    Object SS包解读器2[] = SS包解读器.读取_重复标签("讯宝");
                    int 数量 = 0;
                    for (int i = 0; i < SS包解读器2.length; i++) {
                        if (收到讯宝((SSPackageReader) SS包解读器2[i], false)) {
                            数量 += 1;
                        }
                    }
                    if (数量 > 1) {
                        提示有新消息(界面文字.获取(186, "#% 条新消息", new Object[] {数量}));
                    }
                    读取未推送的讯宝的结果 = 1;
                } catch (Exception e) {
                    读取未推送的讯宝的结果 = -1;
                    说(e.getMessage());
                }
                break;
            case 4:
                正在连接传送服务器 = false;
                if (当前用户.已登录()) {
                    原因 = SharedMethod.替换HTML和JS敏感字符(msg.getData().getString("原因"));
                    if (!SharedMethod.字符串未赋值或为空(原因) && !不再提示) {
                        说(原因);
                    }
                    结束 = msg.getData().getBoolean("结束");
                    if (!结束) {
                        if (任务 != null) {
                            if (任务.名称.equals(TaskName.任务名称_注销)) {
                                return;
                            }
                        }
//                        当前用户.记录运行步骤("连接传送服务器3 " + 原因);
                        启动访问线程_传送服务器();
                    } else {
                        if (聊天控件 != null) { 聊天控件.按钮和机器人图标(false); }
                        提示新任务();
                    }
                }
                break;
            case 10:
                if (线程_发送SS == null) {
//                    当前用户.记录运行步骤("发送失败");
                    关闭网络连接器(1);
                }
                break;
            case 8:
                正在连接传送服务器 = false;
                自检();
                break;
        }
    }

    private void HTTPS请求成功(byte SS包[]) {
        if (聊天控件 != null) { 聊天控件.按钮和机器人图标(false); }
        if (任务 == null) {
            return;
        }
        if (任务.名称.equalsIgnoreCase(TaskName.任务名称_注销)) {
            if (主活动 != null) {
                主活动.注销成功();
            }
        } else {
            if (SS包 != null) {
                SSPackageReader SS包解读器;
                try {
                    SS包解读器 = new SSPackageReader(SS包);
                    switch (SS包解读器.获取查询结果()) {
                        case ProtocolParameters.查询结果_成功:
                            if (任务.名称.equalsIgnoreCase(TaskName.任务名称_发流星语)) {
                                流星语发布结束(true);
                                任务.结束();
                                任务 = null;
                                return;
                            } else if (任务.名称.equalsIgnoreCase(TaskName.任务名称_添加讯友)) {
                                添加讯友成功(SS包解读器);
                            } else if (任务.名称.equalsIgnoreCase(TaskName.任务名称_创建大聊天群)) {
                                大聊天群创建成功(SS包解读器);
                            } else if (任务.名称.equalsIgnoreCase(TaskName.任务名称_加入大聊天群)) {
                                加入大聊天群成功(SS包解读器);
                            } else if (任务.名称.equalsIgnoreCase(TaskName.任务名称_账户)) {
                                收到账户信息(SS包解读器);
                                return;
                            } else if (任务.名称.equalsIgnoreCase(TaskName.任务名称_发布商品)) {
                                商品发布结束(true);
                                任务.结束();
                                任务 = null;
                                return;
                            } else if (任务.名称.equalsIgnoreCase(TaskName.任务名称_获取密钥)) {
                                收到密钥(SS包解读器, false);
                                return;
                            } else if (任务.名称.equalsIgnoreCase(TaskName.任务名称_密码)) {
                                密码修改成功();
                            } else if (任务.名称.equalsIgnoreCase(TaskName.任务名称_手机号)) {
                                等待验证手机号(SS包解读器);
                                return;
                            } else if (任务.名称.equalsIgnoreCase(TaskName.任务名称_邮箱地址)) {
                                等待验证电子邮箱地址(SS包解读器);
                                return;
                            } else if (任务.名称.equalsIgnoreCase(TaskName.任务名称_验证手机号)) {
                                手机号修改成功();
                            } else if (任务.名称.equalsIgnoreCase(TaskName.任务名称_验证邮箱地址)) {
                                电子邮箱地址修改成功();
                            } else if (任务.名称.equalsIgnoreCase(TaskName.任务名称_登录)) {
                                登录成功(SS包解读器);
                                return;
                            } else if (任务.名称.equalsIgnoreCase(TaskName.任务名称_注销)) {
                                if (主活动 != null) {
                                    主活动.注销成功();
                                }
                                return;
                            } else if (任务.名称.equalsIgnoreCase(TaskName.任务名称_用户名)) {
                                设置用户名成功(SS包解读器);
                                return;
                            } else if (任务.名称.equalsIgnoreCase(TaskName.任务名称_注册)) {
                                等待验证(SS包解读器);
                                return;
                            } else if (任务.名称.equalsIgnoreCase(TaskName.任务名称_验证)) {
                                验证成功(SS包解读器);
                                return;
                            } else if (任务.名称.equalsIgnoreCase(TaskName.任务名称_忘记)) {
                                等待重设密码(SS包解读器);
                                return;
                            } else if (任务.名称.equalsIgnoreCase(TaskName.任务名称_重设)) {
                                重设密码();
                                return;
                            }
                            break;
                        case ProtocolParameters.查询结果_发送序号不一致:
                            关闭网络连接器(2);
                            break;
                        case ProtocolParameters.查询结果_拥有的大聊天群数量已达上限:
                            if (!大聊天群创建成功(SS包解读器)) {
                                说(界面文字.获取(272, "你创建的大聊天群数量已达上限。"));
                            }
                            break;
                        case ProtocolParameters.查询结果_没有可用的大聊天群服务器:
                            说(界面文字.获取(274, "没有可用的大聊天群服务器。"));
                            break;
                        case ProtocolParameters.查询结果_不是群成员:
                            说(界面文字.获取(83, "你不是当前聊天群的成员。"));
                            break;
                        case ProtocolParameters.查询结果_验证码:
                            收到验证码图片(SS包解读器);
                            return;
                        case ProtocolParameters.查询结果_讯宝地址不存在:
                            说(界面文字.获取(109, "此讯宝地址不存在。[<a>#%</a>]", new Object[] {TaskName.任务名称_添加讯友}));
                            break;
                        case ProtocolParameters.查询结果_讯友录满了:
                            说(界面文字.获取(104, "失败，因为最多只能添加#%个讯友。", new Object[] {ProtocolParameters.最大值_讯友数量}));
                            break;
                        case ProtocolParameters.查询结果_某标签讯友数满了:
                            说(界面文字.获取(135, "失败，因为每个标签最多只能标记 #% 个讯友。", new Object[] {ProtocolParameters.最大值_每个标签讯友数量}));
                            break;
                        case ProtocolParameters.查询结果_稍后重试:
                            说(界面文字.获取(20, "你的操作过于频繁，请#%分钟后再尝试。", new Object[] {Constants.最近操作次数统计时间_分钟}));
                            break;
                        case ProtocolParameters.查询结果_凭据无效:
                            说(界面文字.获取(229, "请注销，然后重新登录。"));
                            return;
                        case ProtocolParameters.查询结果_服务器未就绪:
                            说(界面文字.获取(269, "服务器还未就绪。请稍后重试。"));
                            break;
                        case ProtocolParameters.查询结果_获取A记录失败:
                            说(界面文字.获取(111, "获取域名A记录失败。"));
                            break;
                        case ProtocolParameters.查询结果_不正确:
                            用户名或密码不正确();
                            return;
                        case ProtocolParameters.查询结果_手机号已绑定:
                            说(界面文字.获取(39, "手机号已绑定在其它账号上了。"));
                            break;
                        case ProtocolParameters.查询结果_电子邮箱地址已绑定:
                            说(界面文字.获取(40, "电子邮箱地址已绑定在其它账号上了。"));
                            break;
                        case ProtocolParameters.查询结果_验证码不匹配:
                            说(界面文字.获取(44, "你提交的验证码与服务器上的不匹配。"));
                            break;
                        case ProtocolParameters.查询结果_英语用户名已注册:
                            英语用户名已注册();
                            return;
                        case ProtocolParameters.查询结果_本国语用户名已注册:
                            本国语用户名已注册();
                            return;
                        case ProtocolParameters.查询结果_暂停发送验证码:
                            说(界面文字.获取(20, "你的操作过于频繁，请#%分钟后再尝试。", new Object[] {Constants.验证码的时间间隔_分钟}));
                            break;
                        case ProtocolParameters.查询结果_获取验证码次数过多:
                            说(界面文字.获取(20, "你的操作过于频繁，请#%分钟后再尝试。", new Object[] {Constants.验证码的有效时间_分钟}));
                            break;
                        case ProtocolParameters.查询结果_手机号未验证:
                            说(界面文字.获取(18, "此手机号是未验证的。"));
                            break;
                        case ProtocolParameters.查询结果_电子邮箱地址未验证:
                            说(界面文字.获取(19, "此电子邮箱地址是未验证的。"));
                            break;
                        case ProtocolParameters.查询结果_无注册许可:
                            说(界面文字.获取(300, "无法注册，因为你并非受邀用户。"));
                            break;
                        case ProtocolParameters.查询结果_没有可用的传送服务器:
                            说(界面文字.获取(118, "没有可用的传送服务器。"));
                            break;
                        case ProtocolParameters.查询结果_传送服务器上没有空位置:
                            说(界面文字.获取(125, "传送服务器上没有空位置。"));
                            break;
                        case ProtocolParameters.查询结果_账号停用:
                            说(界面文字.获取(15, "账号已停用。"));
                            break;
                        case ProtocolParameters.查询结果_系统维护:
                            说(界面文字.获取(14, "由于服务器正在维护中，暂停服务。"));
                            break;
                        case ProtocolParameters.查询结果_出错:
                            说(界面文字.获取(108, "出错 #%", new Object[] {SS包解读器.获取查询结果()}));
                            break;
                        case ProtocolParameters.查询结果_失败:
                            说(界面文字.获取(148, "由于未知原因，操作失败。"));
                            if (任务.名称.equalsIgnoreCase(TaskName.任务名称_获取密钥)) {
                                if (重试次数 < 3) {
                                    重试次数 += 1;
                                    任务.结束();
                                    获取密钥();
                                    return;
                                } else {
                                    重试次数 = 0;
                                }
                            }
                            break;
                        case ProtocolParameters.查询结果_数据库未就绪:
                            说(界面文字.获取(141, "数据库未就绪。"));
                            break;
                        default :
                            说(界面文字.获取(108, "出错 #%", new Object[] {SS包解读器.获取查询结果()}));
                    }
                    if (任务.名称.equalsIgnoreCase(TaskName.任务名称_发流星语)) {
                        流星语发布结束(false);
                    } else if (任务.名称.equalsIgnoreCase(TaskName.任务名称_发布商品)) {
                        商品发布结束(false);
                    }
                } catch (Exception e) {
                    说(e.getMessage());
                }
            }
            任务.结束();
            任务 = null;
        }
    }

    private void 添加讯友成功(SSPackageReader SS包解读器) throws Exception {
        Contact 新讯友 = new Contact();
        新讯友.英语讯宝地址 = (String)SS包解读器.读取_有标签("英语讯宝地址");
        if (!SharedMethod.字符串未赋值或为空(新讯友.英语讯宝地址)) {
            新讯友.备注 = (String)SS包解读器.读取_有标签("备注");
            新讯友.主机名 = (String)SS包解读器.读取_有标签("主机名");
            新讯友.位置号 = (short)SS包解读器.读取_有标签("位置号");
            Object 值 = SS包解读器.读取_有标签("本国语讯宝地址");
            if (值 != null) {
                新讯友.本国语讯宝地址 = (String)值;
            }
            新讯友.拉黑 = (boolean)SS包解读器.读取_有标签("拉黑");
            long 讯友录更新时间 = (long) SS包解读器.读取_有标签("时间");
            Contact 讯友目录[] = 当前用户.讯友目录;
            if (讯友目录 == null) {
                讯友目录 = new Contact[1];
                新讯友.临时编号 = 1;
                讯友目录[0] = 新讯友;
                当前用户.讯友目录 = 讯友目录;
                if (!新讯友.拉黑) {
                    if (当前用户.讯友录当前显示范围 == Constants.讯友录显示范围_讯友) {
                        if (主窗体 != null) {
                            主窗体.刷新讯友录();
                        }
                    }
                } else {
                    switch (当前用户.讯友录当前显示范围) {
                        case Constants.讯友录显示范围_最近:
                        case Constants.讯友录显示范围_讯友:
                        case Constants.讯友录显示范围_某标签:
                        case Constants.讯友录显示范围_黑名单:
                            if (主窗体 != null) {
                                主窗体.刷新讯友录();
                            }
                    }
                }
            } else {
                int i;
                for (i = 0; i < 讯友目录.length; i++) {
                    if (讯友目录[i].英语讯宝地址.equals(新讯友.英语讯宝地址)) {
                        break;
                    }
                }
                if (i == 讯友目录.length) {
                    for (i = 0; i < 讯友目录.length; i++) {
                        if (讯友目录[i].英语讯宝地址.compareTo(新讯友.英语讯宝地址) > 0) {
                            break;
                        }
                    }
                    Contact 讯友目录2[] = new Contact[讯友目录.length + 1];
                    if (i < 讯友目录.length) {
                        if (i > 0) {
                            System.arraycopy(讯友目录, 0, 讯友目录2, 0, i);
                        }
                        讯友目录2[i] = 新讯友;
                        System.arraycopy(讯友目录, i, 讯友目录2, i + 1, 讯友目录.length - i);
                    } else {
                        System.arraycopy(讯友目录, 0, 讯友目录2, 0, 讯友目录.length);
                        讯友目录2[讯友目录2.length - 1] = 新讯友;
                    }
                    讯友目录 = 讯友目录2;
                    for (short j = 0; j < 讯友目录.length; j++) {
                        讯友目录[j].临时编号 = j + 1;
                    }
                    当前用户.讯友目录 = 讯友目录;
                    if (!新讯友.拉黑) {
                        if (当前用户.讯友录当前显示范围 == Constants.讯友录显示范围_讯友) {
                            if (主窗体 != null) {
                                主窗体.刷新讯友录();
                            }
                        }
                    } else {
                        switch (当前用户.讯友录当前显示范围) {
                            case Constants.讯友录显示范围_最近:
                            case Constants.讯友录显示范围_讯友:
                            case Constants.讯友录显示范围_某标签:
                            case Constants.讯友录显示范围_黑名单:
                                if (主窗体 != null) {
                                    主窗体.刷新讯友录();
                                }
                        }
                    }
                }
            }
            if (讯友录更新时间 > 0) { 当前用户.讯友录有变动(讯友录更新时间); }
            if (!新讯友.拉黑) {
                说(界面文字.获取(107, "讯友 #% 添加成功。[<a>#%</a>]", new Object[]{新讯友.英语讯宝地址, TaskName.任务名称_添加讯友}));
            } else {
                说(界面文字.获取(97, "你将不会收到 #% 的任何消息。", new Object[] {新讯友.英语讯宝地址}));
            }
        }
    }

    private boolean 大聊天群创建成功(SSPackageReader SS包解读器) throws Exception {
        Object SS包解读器2[] = SS包解读器.读取_重复标签("群");
        if (SS包解读器2 == null) {
            return false;
        }
        SSPackageReader SS包解读器3;
        String 主机名, 群名称, 子域名;
        long 群编号;
        int i, j;
        for (i = 0; i < SS包解读器2.length; i++) {
            SS包解读器3 = (SSPackageReader) SS包解读器2[i];
            主机名 = (String)SS包解读器3.读取_有标签("主机名");
            群编号 = (long)SS包解读器3.读取_有标签("群编号");
            群名称 = (String)SS包解读器3.读取_有标签("群名称");
            子域名 = 主机名 + "." + 当前用户.域名_英语;
            if (当前用户.加入的大聊天群 != null) {
                Group_Large 加入的大聊天群[] = 当前用户.加入的大聊天群;
                for (j = 0; j < 加入的大聊天群.length; j++) {
                    if (加入的大聊天群[j].子域名.equals(子域名) && 加入的大聊天群[j].编号 == 群编号) {
                        return false;
                    }
                }
            }
            说(界面文字.获取(276, "大聊天群 [#%] 创建成功。地址是 #% 。", new Object[] {群名称, 子域名 + "/" + 群编号}) + "&nbsp;<span class='TaskName' onclick='ToRobot2(\\\"JoinLargeGroup\\\", \\\"" + 子域名 + "\\\", \\\"" + 群编号 + "\\\")'>" + 界面文字.获取(173, "加入") + "</span>");
        }
        return true;
    }

    private void 加入大聊天群成功(SSPackageReader SS包解读器) throws Exception {
        String 子域名 = (String)SS包解读器.读取_有标签("子域名");
        long 群编号 = (long)SS包解读器.读取_有标签("群编号");
        String 群名称 = (String)SS包解读器.读取_有标签("群名称");
        long 图标更新时间 = (long)SS包解读器.读取_有标签("图标更新时间");
        String 连接凭据 = (String)SS包解读器.读取_有标签("连接凭据");
        byte 角色 = (byte)SS包解读器.读取_有标签("角色");
        String 本国语域名 = (String)SS包解读器.读取_有标签("本国语域名");
        int i = 子域名.indexOf(".");
        Group_Large 大聊天群 = new Group_Large();
        大聊天群.编号 = 群编号;
        大聊天群.名称 = 群名称;
        大聊天群.图标更新时间 = 图标更新时间;
        大聊天群.主机名 = 子域名.substring(0, i);
        大聊天群.英语域名 = 子域名.substring(i + 1);
        大聊天群.本国语域名 = 本国语域名;
        大聊天群.子域名 = 子域名;
        大聊天群.连接凭据 = 连接凭据;
        大聊天群.我的角色 = 角色;
        Group_Large 加入的大聊天群[];
        if (当前用户.加入的大聊天群 != null) {
            加入的大聊天群 = 当前用户.加入的大聊天群;
            for (int j = 0; j < 加入的大聊天群.length; j++) {
                if (加入的大聊天群[j].子域名.equals(子域名) && 加入的大聊天群[j].编号 == 群编号) {
                    return;
                }
            }
            Group_Large 加入的大聊天群2[] = new Group_Large[加入的大聊天群.length + 1];
            System.arraycopy(加入的大聊天群, 0, 加入的大聊天群2, 0, 加入的大聊天群.length);
            加入的大聊天群2[加入的大聊天群2.length - 1] = 大聊天群;
            当前用户.加入的大聊天群 = 加入的大聊天群2;
        } else {
            当前用户.加入的大聊天群 = new Group_Large[1];
            当前用户.加入的大聊天群[0] = 大聊天群;
        }
        加入的大聊天群 = 当前用户.加入的大聊天群;
        Group_Large 某一群;
        for (i = 0; i < 加入的大聊天群.length; i++) {
            某一群 = 加入的大聊天群[i];
            if (某一群.子域名.equals(子域名) && 某一群.编号 != 群编号) {
                if (!SharedMethod.字符串未赋值或为空(某一群.连接凭据)) {
                    某一群.连接凭据 = 连接凭据;
                }
            }
        }
        ChatWith 聊天对象2 = new ChatWith();
        聊天对象2.大聊天群 = 大聊天群;
        主窗体.添加聊天控件(聊天对象2);
        if (数据库_更新最近互动讯友排名(子域名, 群编号)) {
            if (主窗体 != null) {
                主窗体.刷新讯友录(Constants.讯友录显示范围_聊天群);
            }
        }
    }

    private void 收到账户信息(SSPackageReader SS包解读器) throws Exception {
        byte SS包[] = (byte[])SS包解读器.读取_有标签("用户信息");
        解读账户和登录信息(SS包, false);
        自检(false, false);
    }

    private void 收到密钥(SSPackageReader SS包解读器, boolean 是本机缓存) throws Exception {
        重试次数 = 0;
        当前用户.主机名 = (String)SS包解读器.读取_有标签("主机名");
        当前用户.位置号 = (short)SS包解读器.读取_有标签("位置号");
        byte 对称密钥字节数组[] = (byte[]) SS包解读器.读取_有标签("对称密钥");
        byte 初始向量字节数组[] = (byte[]) SS包解读器.读取_有标签("初始向量");
        SecretKeySpec AES密钥 = new SecretKeySpec(对称密钥字节数组,"AES");
        IvParameterSpec 初始向量 = new IvParameterSpec(初始向量字节数组);
        当前用户.AES加密器 = Cipher.getInstance("AES/CBC/PKCS7Padding");
        当前用户.AES加密器.init(Cipher.ENCRYPT_MODE, AES密钥, 初始向量);
        当前用户.AES解密器 =Cipher.getInstance("AES/CBC/PKCS7Padding");
        当前用户.AES解密器.init(Cipher.DECRYPT_MODE, AES密钥, 初始向量);
        当前用户.密钥创建时间 = (long)SS包解读器.读取_有标签("时间");
        当前用户.获取了密钥 = true;
        if (!是本机缓存) {
            String 路径 = 运行环境.getFilesDir().toString() + "/" + 当前用户.获取英语讯宝地址();
            File 目录 = new File(路径);
            if (!目录.exists() || !目录.isDirectory()) {
                目录.mkdir();
            }
            SharedMethod.保存文件的全部字节(路径 + "/keyiv.sspk", SS包解读器.要解读的SS包);
            if (!不再提示) {
                说(界面文字.获取(246, "密钥收到。"));
            }
        }
        自检();
    }

    private void 密码修改成功() {
        说(界面文字.获取(230, "密码修改成功。"));
    }

    private void 等待验证手机号(SSPackageReader SS包解读器) throws Exception {
        Task 之前任务 = 任务;
        任务 = new Task(TaskName.任务名称_验证手机号, 聊天控件.输入框, this);
        任务.添加步骤(Task.任务步骤_手机号, "", 之前任务.获取某步骤的输入值(Task.任务步骤_手机号));
        之前任务.结束();
        任务.验证码添加时间 = (long)SS包解读器.读取_有标签("验证码添加时间");
        任务.添加步骤(Task.任务步骤_验证码, 界面文字.获取(232, "验证码已发送至你的新手机号。请在#%分钟内输入。", new Object[] {Constants.验证码的有效时间_分钟}));
        说(任务.获取当前步骤提示语());
    }

    private void 等待验证电子邮箱地址(SSPackageReader SS包解读器) throws Exception {
        Task 之前任务 = 任务;
        任务 = new Task(TaskName.任务名称_验证邮箱地址, 聊天控件.输入框, this);
        任务.添加步骤(Task.任务步骤_电子邮箱地址, "", 之前任务.获取某步骤的输入值(Task.任务步骤_电子邮箱地址));
        之前任务.结束();
        任务.验证码添加时间 = (long)SS包解读器.读取_有标签("验证码添加时间");
        任务.添加步骤(Task.任务步骤_验证码, 界面文字.获取(233, "验证码已发送至你的新电子邮箱地址。请在#%分钟内输入。", new Object[] {Constants.验证码的有效时间_分钟}));
        说(任务.获取当前步骤提示语());
    }

    private void 手机号修改成功() {
        说(界面文字.获取(234, "手机号修改成功。"));
    }

    private void 电子邮箱地址修改成功() {
        说(界面文字.获取(235, "电子邮箱地址修改成功。"));
    }

    private void 登录成功(SSPackageReader SS包解读器) throws Exception {
        当前用户.编号 = (long)SS包解读器.读取_有标签("用户编号");
        当前用户.凭据_中心服务器 = (String)SS包解读器.读取_有标签("连接凭据");
        byte SS包[] = (byte[])SS包解读器.读取_有标签("用户信息");
        解读账户和登录信息(SS包, false);
        if (主活动 != null) {
            主活动.登录成功();
        }
        自检();
    }

    private void 解读账户和登录信息(byte SS包[], boolean 是本机缓存) throws Exception {
        SSPackageReader SS包解读器 = new SSPackageReader(SS包);
        当前用户.域名_英语 = (String)SS包解读器.读取_有标签("英语域名");
        当前用户.域名_本国语 = (String)SS包解读器.读取_有标签("本国语域名");
        当前用户.英语用户名 = (String)SS包解读器.读取_有标签("英语用户名");
        当前用户.本国语用户名 = (String)SS包解读器.读取_有标签("本国语用户名");
        Object 值 = SS包解读器.读取_有标签("手机号");
        long 手机号;
        if (值 == null) {
            手机号 = 0;
        } else {
            手机号 = (long) 值;
        }
        当前用户.电子邮箱地址 = (String)SS包解读器.读取_有标签("电子邮箱地址");
        当前用户.职能 = (String)SS包解读器.读取_有标签("职能");
        String 登录时间_电脑 = (String)SS包解读器.读取_有标签("登录时间_电脑");
        String 登录时间_手机 = (String)SS包解读器.读取_有标签("登录时间_手机");
        String 网络地址_电脑 = (String)SS包解读器.读取_有标签("网络地址_电脑");
        String 网络地址_手机 = (String)SS包解读器.读取_有标签("网络地址_手机");
        if (!是本机缓存) {
            SharedMethod.保存文件的全部字节(主活动.getFilesDir().toString() + "/accountinfo.sspk", SS包);
            StringBuffer 字符串合并器 = new StringBuffer(1000);
            if (!SharedMethod.字符串未赋值或为空(当前用户.英语用户名)) {
                字符串合并器.append(界面文字.获取(68, "你的英语讯宝地址为 #% 。", new Object[] {当前用户.获取英语讯宝地址()}));
            }
            if (!SharedMethod.字符串未赋值或为空(当前用户.本国语用户名)) {
                if (字符串合并器.length() > 0) { 字符串合并器.append("<br>");   }
                字符串合并器.append(界面文字.获取(69, "你的中文讯宝地址为 #% 。", new Object[] {当前用户.获取本国语讯宝地址()}));
            }
            if (手机号 > 0) {
                if (字符串合并器.length() > 0) { 字符串合并器.append("<br>");   }
                字符串合并器.append(界面文字.获取(78, "你的手机号为 #% 。", new Object[] {手机号}));
            }
            if (!SharedMethod.字符串未赋值或为空(当前用户.电子邮箱地址)) {
                if (字符串合并器.length() > 0) { 字符串合并器.append("<br>");   }
                字符串合并器.append(界面文字.获取(79, "你的电子邮箱地址为 #% 。", new Object[] {当前用户.电子邮箱地址}));
            }
            if (!SharedMethod.字符串未赋值或为空(登录时间_电脑)) {
                if (字符串合并器.length() > 0) { 字符串合并器.append("<br>");   }
                字符串合并器.append(界面文字.获取(80, "电脑登录时间 #% ，登录IP #% 。", new Object[] {登录时间_电脑, 网络地址_电脑}));
            }
            if (!SharedMethod.字符串未赋值或为空(登录时间_手机)) {
                if (字符串合并器.length() > 0) { 字符串合并器.append("<br>");   }
                字符串合并器.append(界面文字.获取(81, "手机登录时间 #% ，登录IP #% 。", new Object[] {登录时间_手机, 网络地址_手机}));
            }
            说(字符串合并器.toString());
        }
        当前用户.获取了账户信息 = true;
        if (主窗体 != null) {
            主窗体.显示隐藏系统管理机器人();
        }
    }

    private void 用户名或密码不正确() {
        if (任务.名称.equals(TaskName.任务名称_密码) || 任务.名称.equals(TaskName.任务名称_手机号) || 任务.名称.equals(TaskName.任务名称_邮箱地址)) {
            说(界面文字.获取(231, "当前密码不正确。"));
        } else {
            说(界面文字.获取(17, "用户名或密码不正确。"));
        }
        任务.清除所有步骤的输入值();
        任务.需要获取验证码图片 = true;
        说(任务.获取当前步骤提示语());
    }

    private void 英语用户名已注册() {
        说(界面文字.获取(37, "英语用户名已被其他人注册了。"));
        任务.清除某步骤的输入值(Task.任务步骤_英语用户名);
        任务.清除某步骤的输入值(Task.任务步骤_重复英语用户名);
        说(任务.获取当前步骤提示语());
    }

    private void 本国语用户名已注册() {
        说(界面文字.获取(38, "中文用户名已被其他人注册了。"));
        任务.清除某步骤的输入值(Task.任务步骤_本国语用户名);
        任务.清除某步骤的输入值(Task.任务步骤_重复本国语用户名);
        说(任务.获取当前步骤提示语());
    }

    private void 等待验证(SSPackageReader SS包解读器) throws Exception {
        Task 之前任务 = 任务;
        任务 = new Task(TaskName.任务名称_验证, 输入框, this);
        任务.添加步骤(Task.任务步骤_域名, "", 之前任务.获取某步骤的输入值(Task.任务步骤_域名));
        任务.添加步骤(Task.任务步骤_手机号或电子邮箱地址, "", 之前任务.获取某步骤的输入值(Task.任务步骤_手机号或电子邮箱地址));
        任务.添加步骤(Task.任务步骤_密码, "", 之前任务.获取某步骤的输入值(Task.任务步骤_密码));
        任务.身份码类型 = 之前任务.身份码类型;
        之前任务.结束();
        任务.验证码添加时间 = (long)SS包解读器.读取_有标签("验证码添加时间");
        if (任务.身份码类型.equals(Constants.身份码类型_手机号)) {
            任务.添加步骤(Task.任务步骤_验证码, 界面文字.获取(41, "验证码已发送至你的手机号。请在#%分钟内输入。", new Object[] {Constants.验证码的有效时间_分钟}));
        } else {
            任务.添加步骤(Task.任务步骤_验证码, 界面文字.获取(42, "验证码已发送至你的电子邮箱地址。请在#%分钟内输入。", new Object[] {Constants.验证码的有效时间_分钟}));
        }
        说(任务.获取当前步骤提示语());
    }

    private void 验证成功(SSPackageReader SS包解读器) throws Exception {
        Task 之前任务 = 任务;
        当前用户.域名_英语 = (String)SS包解读器.读取_有标签("英语域名");
        当前用户.域名_本国语 = (String)SS包解读器.读取_有标签("本国语域名");
        任务 = new Task(TaskName.任务名称_用户名, 输入框, this);
        任务.添加步骤(Task.任务步骤_手机号或电子邮箱地址, "", 之前任务.获取某步骤的输入值(Task.任务步骤_手机号或电子邮箱地址));
        任务.添加步骤(Task.任务步骤_密码, "", 之前任务.获取某步骤的输入值(Task.任务步骤_密码));
        任务.添加步骤(Task.任务步骤_验证码, "", 之前任务.获取某步骤的输入值(Task.任务步骤_验证码));
        任务.身份码类型 = 之前任务.身份码类型;
        任务.验证码添加时间 = 之前任务.验证码添加时间;
        之前任务.结束();
        任务.添加步骤(Task.任务步骤_英语用户名, 界面文字.获取(24, "请输入你想要拥有的英语用户名（只能有英语字母 a-z、数字 0-9 和下划线 _ ，最多#%个字符）。", new Object[]{ProtocolParameters.最大值_英语用户名长度}));
        if (SharedMethod.字符串未赋值或为空(当前用户.域名_本国语)) {
            任务.添加步骤(Task.任务步骤_重复英语用户名, 界面文字.获取(61, "再次输入相同的英语用户名将提交至服务器。你将永久无法修改。"));
        } else {
            任务.添加步骤(Task.任务步骤_重复英语用户名, 界面文字.获取(62, "请再次输入相同的英语用户名。"));
            任务.添加步骤(Task.任务步骤_本国语用户名, 界面文字.获取(25, "请输入你想要拥有的汉语用户名（只能有汉字、数字 0-9 和下划线 _ ，最多#%个字符）。", new Object[] {ProtocolParameters.最大值_本国语用户名长度}));
            任务.添加步骤(Task.任务步骤_重复本国语用户名, 界面文字.获取(63, "再次输入相同的汉语用户名将提交至服务器。你将永久无法修改。"));
        }
        说(任务.获取当前步骤提示语());
    }

    private void 设置用户名成功(SSPackageReader SS包解读器) throws Exception {
        当前用户.英语用户名 = (String)SS包解读器.读取_有标签("英语用户名");
        当前用户.本国语用户名 = (String)SS包解读器.读取_有标签("本国语用户名");
        String 英语讯宝地址 = 当前用户.英语用户名 + ProtocolParameters.讯宝地址标识 + 当前用户.域名_英语;
        说(界面文字.获取(68, "你的英语讯宝地址为 #% 。", new Object[] {英语讯宝地址}));
        if (!SharedMethod.字符串未赋值或为空(当前用户.本国语用户名)) {
            说(界面文字.获取(69, "你的中文讯宝地址为 #% 。", new Object[] {当前用户.本国语用户名 + ProtocolParameters.讯宝地址标识 + 当前用户.域名_本国语}));
        }
        说(界面文字.获取(45, "你的账号创建成功！现在你可以<a>#%</a>了。", new Object[]{TaskName.任务名称_登录}));
        Task 之前任务 = 任务;
        任务 = new Task(TaskName.任务名称_登录, 聊天控件.输入框, this);
        任务.添加步骤(Task.任务步骤_讯宝地址, "", 英语讯宝地址);
        任务.添加步骤(Task.任务步骤_密码, "", 之前任务.获取某步骤的输入值(Task.任务步骤_密码));
        之前任务.结束();
        启动HTTPS访问线程(任务.生成访问设置());
    }

    private void 等待重设密码(SSPackageReader SS包解读器) throws Exception {
        Task 之前任务 = 任务;
        任务 = new Task(TaskName.任务名称_重设, 输入框, this);
        任务.添加步骤(Task.任务步骤_讯宝地址, "", 之前任务.获取某步骤的输入值(Task.任务步骤_讯宝地址));
        之前任务.结束();
        任务.验证码添加时间 = (long)SS包解读器.读取_有标签("验证码添加时间");
        boolean 发送至手机 = (boolean)SS包解读器.读取_有标签("发送至手机");
        if (发送至手机) {
            任务.添加步骤(Task.任务步骤_验证码, 界面文字.获取(41, "验证码已发送至你的手机号。请在#%分钟内输入。", new Object[] {Constants.验证码的有效时间_分钟}));
        } else {
            任务.添加步骤(Task.任务步骤_验证码, 界面文字.获取(42, "验证码已发送至你的电子邮箱地址。请在#%分钟内输入。", new Object[] {Constants.验证码的有效时间_分钟}));
        }
        任务.添加步骤(Task.任务步骤_密码, 界面文字.获取(27, "请为你的账号设置密码（最少#%个字符，最多#%个字符）。", new Object[] {ProtocolParameters.最小值_密码长度, ProtocolParameters.最大值_密码长度}));
        任务.添加步骤(Task.任务步骤_重复密码, 界面文字.获取(28, "请再次输入相同的密码。"));
        说(任务.获取当前步骤提示语());
    }

    private void 重设密码() {
        说(界面文字.获取(55, "你的账号密码已更换。现在你可以<a>#%</a>了。", new Object[] {TaskName.任务名称_登录}));
        Task 之前任务 = 任务;
        任务 = new Task(TaskName.任务名称_登录, 输入框, this);
        任务.添加步骤(Task.任务步骤_讯宝地址, "", 之前任务.获取某步骤的输入值(Task.任务步骤_讯宝地址));
        任务.添加步骤(Task.任务步骤_密码, "", 之前任务.获取某步骤的输入值(Task.任务步骤_密码));
        之前任务.结束();
        启动HTTPS访问线程(任务.生成访问设置());
    }

    private void 收到验证码图片(SSPackageReader SS包解读器) throws Exception {
        任务.验证码添加时间 = (long)SS包解读器.读取_有标签("验证码添加时间");
        byte 图片字节数组[] = (byte[]) SS包解读器.读取_有标签("图片");
        if (图片字节数组 != null && 聊天控件 != null) {
            聊天控件.发送JS("function(){ var ImgSrc = \"data:image/jpg;base64," + Base64.encodeToString(图片字节数组, Base64.DEFAULT).replaceAll("[ \t\n\r]", "") + "\"; SSin_Base64Img(ImgSrc, \"" + Constants.机器人id_主控 + ".jpg\"); }");
            任务.添加步骤(Task.任务步骤_验证码, 界面文字.获取(21, "请输入验证码。"));
            任务.需要获取验证码图片 = false;
            说(任务.获取当前步骤提示语());
        }
    }


    void 启动访问线程_传送服务器() {
        if (正在连接传送服务器) { return; }
        正在连接传送服务器 = true;
        if (聊天控件 != null) {
            聊天控件.按钮和机器人图标(true);
            if (!不再提示) {
                说(界面文字.获取(46, "正在连接传送服务器。"));
            }
        }
//        当前用户.记录运行步骤("连接传送服务器");
        线程_传送服务器 = new Thread_ConnectTransportServer(this);
        线程_传送服务器.start();
    }

    public void 连接传送服务器() {
        防止休眠_开始();
        String 终止原因 = null;
        boolean 失败 = false;
        try {
            Socket 网络连接器 = new Socket();
            当前用户.网络连接器 = 网络连接器;
            网络连接器.setSoTimeout(Constants.收发时限);
            网络连接器.setKeepAlive(true);
            SocketAddress 服务器地址和端口;
            if (!BuildConfig.DEBUG || ProtocolPath.调试时访问真实网站) {
                InetAddress 地址[] = InetAddress.getAllByName(当前用户.主机名 + "." + 当前用户.域名_英语);
                服务器地址和端口 = new InetSocketAddress(地址[0], ProtocolParameters.端口_传送服务器);
            } else {
                服务器地址和端口 = new InetSocketAddress(ProtocolPath.IP地址_调试, ProtocolParameters.端口_传送服务器);
            }
            网络连接器.connect(服务器地址和端口, Constants.收发时限);
            SSPackageCreator SS包生成器1 = new SSPackageCreator(true);
            SS包生成器1.添加_无标签(当前用户.位置号);
            SS包生成器1.添加_无标签(ProtocolParameters.设备类型_数字_手机);
            SS包生成器1.添加_无标签(当前用户.密钥创建时间);
            if (当前用户.讯友录更新时间 < 0) {
                try {
                    byte 字节数组[] = SharedMethod.读取文件的全部字节(运行环境.getFilesDir().toString() + "/" + 当前用户.获取英语讯宝地址() + "/sspal.sspk");
                    if (字节数组 != null) {
                        读取讯友录数据(new SSPackageReader(字节数组), true);
                    }
                } catch (Exception e) {
                }
            }
            SSPackageCreator SS包生成器2 = new SSPackageCreator();
            SS包生成器2.添加_有标签("用户编号", 当前用户.编号);
            当前用户.传送服务器验证码 = SharedMethod.生成大小写英文字母与数字的随机字符串(ProtocolParameters.长度_验证码);
            SS包生成器2.添加_有标签("验证码", 当前用户.传送服务器验证码);
            SS包生成器2.添加_有标签("讯友录更新时间", 当前用户.讯友录更新时间);
            SS包生成器1.添加_无标签(SS包生成器2.生成SS包(当前用户.AES加密器), ProtocolParameters.长度信息字节数_两字节);
            if (SS包生成器1.发送SS包(网络连接器)) {
                byte 字节数组[] = SharedMethod.接收指定长度的数据(网络连接器, 1);
                switch (new String(字节数组, "ASCII")) {
                    case "Y":
                        SSPackageReader SS包解读器 = new SSPackageReader(网络连接器, 当前用户.AES解密器, false);
                        String 验证码 = (String) SS包解读器.读取_有标签("验证码");
                        if (当前用户.传送服务器验证码.equals(验证码)) {
                            long 讯友录更新时间 = 当前用户.讯友录更新时间;
                            当前用户.讯友录更新时间 = (long) SS包解读器.读取_有标签("讯友录更新时间");
                            当前用户.头像更新时间 = (long) SS包解读器.读取_有标签("头像更新时间");
                            SSPackageReader SS包解读器2;
                            Object SS包解读器3[];
                            if (讯友录更新时间 != 当前用户.讯友录更新时间) {
                                读取讯友录数据(SS包解读器, false);
                                当前用户.讯友录有变动(当前用户.讯友录更新时间);
                            }
                            SS包解读器2 = (SSPackageReader) SS包解读器.读取_有标签("小聊天群");
                            if (SS包解读器2 != null) {
                                SS包解读器3 = SS包解读器2.读取_重复标签("群");
                                if (SS包解读器3 != null) {
                                    if (主窗体 != null) {
                                        主窗体.收到加入的小聊天群(SS包解读器3);
                                    } else {
                                        收到加入的小聊天群(SS包解读器3, null, 0);
                                    }
                                }
                            }
                            SS包解读器2 = (SSPackageReader) SS包解读器.读取_有标签("大聊天群");
                            if (SS包解读器2 != null) {
                                SS包解读器3 = SS包解读器2.读取_重复标签("群");
                                if (SS包解读器3 != null) {
                                    if (主窗体 != null) {
                                        主窗体.收到加入的大聊天群(SS包解读器3);
                                    } else {
                                        收到加入的大聊天群(SS包解读器3, null, 0);
                                    }
                                }
                            }
                            SS包解读器2 = (SSPackageReader) SS包解读器.读取_有标签("新讯宝");
                            int 讯宝数量 = 0;
                            if (SS包解读器2 != null) {
                                SS包解读器3 = SS包解读器2.读取_重复标签("讯宝");
                                if (SS包解读器3 != null) {
                                    讯宝数量 = SS包解读器3.length;
                                    读取未推送的讯宝的结果 = 0;
                                    Bundle 数据盒子 = new Bundle();
                                    数据盒子.putByteArray("SS包", SS包解读器2.要解读的SS包);
                                    Message 消息 = new Message();
                                    消息.what = 11;
                                    消息.setData(数据盒子);
                                    跨线程调用器.sendMessage(消息);
                                    int 等待毫秒 = 0;
                                    do {
                                        Thread.sleep(100);
                                        等待毫秒 += 100;
                                        if (等待毫秒 >= 5000) {
                                            throw new Exception("Failed to read SS");
                                        }
                                    } while (读取未推送的讯宝的结果 == 0);
                                    if (读取未推送的讯宝的结果 < 0) {
                                        throw new Exception("Failed to read SS");
                                    }
                                }
                            }
                            当前用户.讯宝发送序号 = (long) SS包解读器.读取_有标签("发送序号");
                            boolean 在线 = (boolean) SS包解读器.读取_有标签("在线");
                            SSPackageCreator SS包生成器 = new SSPackageCreator();
                            if (讯友录更新时间 != 当前用户.讯友录更新时间) {
                                SS包生成器.添加_有标签("讯友数量", (int) (当前用户.讯友目录 != null ? 当前用户.讯友目录.length : 0));
                                SS包生成器.添加_有标签("小聊天群数量", (int) (当前用户.加入的小聊天群 != null ? 当前用户.加入的小聊天群.length : 0));
                                SS包生成器.添加_有标签("大聊天群数量", (int) (当前用户.加入的大聊天群 != null ? 当前用户.加入的大聊天群.length : 0));
                            }
                            SS包生成器.添加_有标签("讯宝数量", 讯宝数量);
                            if (SS包生成器.发送SS包(网络连接器, 当前用户.AES加密器)) {
                                连接传送服务器成功或失败(true, 在线, (讯友录更新时间 < 0 ? true : false));
                                网络连接器.setSoTimeout(0);
                                重试次数 = 0;
                                心跳确认时间 = System.currentTimeMillis();
                                while (true) {
                                    SS包解读器 = new SSPackageReader(网络连接器, 当前用户.AES解密器, true);
                                    Bundle 数据盒子 = new Bundle();
                                    数据盒子.putByteArray("SS包", SS包解读器.要解读的SS包);
                                    Message 消息 = new Message();
                                    消息.what = 5;
                                    消息.setData(数据盒子);
                                    跨线程调用器.sendMessage(消息);
                                }
                            } else {
                                连接传送服务器成功或失败(false, 在线, false);
                                失败 = true;
                            }
                        } else {
                            终止原因 = 界面文字.获取(53, "验证码不匹配。");
                            失败 = true;
                        }
                        break;
                    case "N":
                        关闭网络连接器(3);
                        当前用户.获取了密钥 = false;
                        当前用户.清除密钥();
                        访问传送服务器失败2();
                        return;
                    default:
                        失败 = true;
                }
            } else {
                失败 = true;
            }
        } catch (SocketTimeoutException | SocketException | UnknownHostException | SSSocketException e) {
            关闭网络连接器(4);
            if (重试次数 < 3) {
                重试次数 += 1;
                用http访问传送服务器();
                访问传送服务器失败(e.getMessage(), false);
            } else {
                重试次数 = 0;
                访问传送服务器失败(e.getMessage(), true);
            }
        } catch (BadPaddingException e) {
            关闭网络连接器(5);
            当前用户.获取了密钥 = false;
            当前用户.清除密钥();
            访问传送服务器失败2();
        } catch (Exception e) {
            关闭网络连接器(6);
            访问传送服务器失败(e.getMessage(), true);
        }
        if (失败) {
            关闭网络连接器(7);
            if (SharedMethod.字符串未赋值或为空(终止原因)) {
                终止原因 = 界面文字.获取(112, "与传送服务器的连接中断了。");
            }
            访问传送服务器失败(终止原因, true);
        }
    }

    private void 读取讯友录数据(SSPackageReader SS包解读器, boolean 读取更新时间) throws Exception {
        if (读取更新时间) {
            当前用户.讯友录更新时间 = (long) SS包解读器.读取_有标签("讯友录更新时间");
        }
        SSPackageReader SS包解读器2 = (SSPackageReader) SS包解读器.读取_有标签("讯友录");
        if (SS包解读器2 != null) {
            Object SS包解读器3[] = SS包解读器2.读取_重复标签("讯友");
            if (SS包解读器3 != null) {
                Contact 讯友2[] = new Contact[SS包解读器3.length];
                Contact 某一讯友;
                SSPackageReader SS包解读器4;
                for (int i = 0; i < SS包解读器3.length; i++) {
                    SS包解读器4 = (SSPackageReader) SS包解读器3[i];
                    某一讯友 = new Contact();
                    某一讯友.英语讯宝地址 = (String) SS包解读器4.读取_有标签("英语讯宝地址");
                    某一讯友.本国语讯宝地址 = (String) SS包解读器4.读取_有标签("本国语讯宝地址");
                    某一讯友.备注 = (String) SS包解读器4.读取_有标签("备注");
                    某一讯友.标签一 = (String) SS包解读器4.读取_有标签("标签一");
                    某一讯友.标签二 = (String) SS包解读器4.读取_有标签("标签二");
                    某一讯友.主机名 = (String) SS包解读器4.读取_有标签("主机名");
                    某一讯友.拉黑 = (boolean) SS包解读器4.读取_有标签("拉黑");
                    某一讯友.位置号 = (short) SS包解读器4.读取_有标签("位置号");
                    某一讯友.临时编号 = (short) (i + 1);
                    讯友2[i] = 某一讯友;
                }
                当前用户.讯友目录 = 讯友2;
            }
        }
        SS包解读器2 = (SSPackageReader) SS包解读器.读取_有标签("白域");
        if (SS包解读器2 != null) {
            Object SS包解读器3[] = SS包解读器2.读取_重复标签("域名");
            if (SS包解读器3 != null) {
                Domain 白域[] = new Domain[SS包解读器3.length];
                SSPackageReader SS包解读器4;
                Object 值;
                for (int i = 0; i < SS包解读器3.length; i++) {
                    SS包解读器4 = (SSPackageReader) SS包解读器3[i];
                    白域[i] = new Domain();
                    白域[i].英语 = (String) SS包解读器4.读取_有标签("英语");
                    值 = SS包解读器4.读取_有标签("本国语");
                    if (值 != null) {
                        白域[i].本国语 = (String) 值;
                    }
                }
                当前用户.白域 = 白域;
            }
        }
        SS包解读器2 = (SSPackageReader) SS包解读器.读取_有标签("黑域");
        if (SS包解读器2 != null) {
            Object SS包解读器3[] = SS包解读器2.读取_重复标签("域名");
            if (SS包解读器3 != null) {
                Domain 黑域[] = new Domain[SS包解读器3.length];
                SSPackageReader SS包解读器4;
                Object 值;
                for (int i = 0; i < SS包解读器3.length; i++) {
                    SS包解读器4 = (SSPackageReader) SS包解读器3[i];
                    黑域[i] = new Domain();
                    黑域[i].英语 = (String) SS包解读器4.读取_有标签("英语");
                    值 = SS包解读器4.读取_有标签("本国语");
                    if (值 != null) {
                        黑域[i].本国语 = (String) 值;
                    }
                }
                当前用户.黑域 = 黑域;
            }
        }
    }

    void 收到加入的小聊天群(Object SS包解读器3[], Fragment_Chating 聊天控件2[], int 聊天控件数) throws Exception {
        Group_Small 加入的群2[] = new Group_Small[SS包解读器3.length];
        Group_Small 某一群;
        byte 群编号;
        String 群主英语讯宝地址, 群备注;
        SSPackageReader SS包解读器4;
        Contact 讯友目录[] = 当前用户.讯友目录;
        int j;
        for (int i = 0; i < SS包解读器3.length; i++) {
            SS包解读器4 = (SSPackageReader) SS包解读器3[i];
            群主英语讯宝地址 = (String) SS包解读器4.读取_有标签("群主");
            群编号 = (byte) SS包解读器4.读取_有标签("群编号");
            群备注 = (String) SS包解读器4.读取_有标签("群备注");
            for (j = 0; j < 聊天控件数; j++) {
                if (群主英语讯宝地址.equals(聊天控件2[j].聊天对象.讯友或群主.英语讯宝地址) &&
                        群编号 == 聊天控件2[j].聊天对象.小聊天群.编号) {
                    break;
                }
            }
            if (j < 聊天控件数) {
                某一群 = 聊天控件2[j].聊天对象.小聊天群;
                某一群.备注 = 群备注;
            } else {
                某一群 = new Group_Small();
                某一群.编号 = 群编号;
                某一群.备注 = 群备注;
                if (群主英语讯宝地址.equals(当前用户.获取英语讯宝地址())) {
                    Contact 我 = new Contact();
                    我.英语讯宝地址 = 当前用户.获取英语讯宝地址();
                    if (!SharedMethod.字符串未赋值或为空(当前用户.域名_本国语)) {
                        我.本国语讯宝地址 = 当前用户.获取本国语讯宝地址();
                    }
                    我.主机名 = 当前用户.主机名;
                    我.头像更新时间 = 当前用户.头像更新时间;
                    某一群.群主 = 我;
                } else {
                    for (j = 0; j < 讯友目录.length; j++) {
                        if (群主英语讯宝地址.equals(讯友目录[j].英语讯宝地址)) {
                            break;
                        }
                    }
                    if (j < 讯友目录.length) {
                        某一群.群主 = 讯友目录[j];
                    }
                }
            }
            加入的群2[i] = 某一群;
        }
        当前用户.加入的小聊天群 = 加入的群2;
    }

    void 收到加入的大聊天群(Object SS包解读器3[], Fragment_Chating 聊天控件2[], int 聊天控件数) throws Exception {
        Group_Large 加入的群2[] = new Group_Large[SS包解读器3.length];
        Group_Large 某一群;
        long 群编号;
        String 主机名, 英语域名, 本国语域名, 子域名, 群名称;
        SSPackageReader SS包解读器4;
        int j;
        for (int i = 0; i < SS包解读器3.length; i++) {
            SS包解读器4 = (SSPackageReader) SS包解读器3[i];
            主机名 = (String) SS包解读器4.读取_有标签("主机名");
            英语域名 = (String) SS包解读器4.读取_有标签("英语域名");
            本国语域名 = (String) SS包解读器4.读取_有标签("本国语域名");
            群编号 = (long) SS包解读器4.读取_有标签("群编号");
            群名称 = (String) SS包解读器4.读取_有标签("群名称");
            子域名 = 主机名 + "." + 英语域名;
            ChatWith 聊天对象;
            for (j = 0; j < 聊天控件数; j++) {
                聊天对象 = 聊天控件2[j].聊天对象;
                if (聊天对象.大聊天群 != null) {
                    if (子域名.equals(聊天对象.大聊天群.子域名) &&
                            群编号 == 聊天对象.大聊天群.编号) {
                        break;
                    }
                }
            }
            if (j < 聊天控件数) {
                某一群 = 聊天控件2[j].聊天对象.大聊天群;
                某一群.名称 = 群名称;
            } else {
                某一群 = new Group_Large();
                某一群.主机名 = 主机名;
                某一群.英语域名 = 英语域名;
                某一群.本国语域名 = 本国语域名;
                某一群.子域名 = 子域名;
                某一群.编号 = 群编号;
                某一群.名称 = 群名称;
            }
            加入的群2[i] = 某一群;
        }
        当前用户.加入的大聊天群 = 加入的群2;
    }

    private void 连接传送服务器成功或失败(boolean 成功, boolean 在线, boolean 打开小宇宙) {
        防止休眠_结束();
        Bundle 数据盒子 = new Bundle();
        数据盒子.putBoolean("成功", 成功);
        数据盒子.putBoolean("在线", 在线);
        数据盒子.putBoolean("打开小宇宙", 打开小宇宙);
        Message 消息 = new Message();
        消息.what = 3;
        消息.setData(数据盒子);
        跨线程调用器.sendMessage(消息);
    }

    private void 访问传送服务器失败(String 原因, boolean 结束) {
        防止休眠_结束();
        Bundle 数据盒子 = new Bundle();
        数据盒子.putString("原因", 原因);
        数据盒子.putBoolean("结束", 结束);
        Message 消息 = new Message();
        消息.what = 4;
        消息.setData(数据盒子);
        跨线程调用器.sendMessage(消息);
    }

    private void 访问传送服务器失败2() {
        防止休眠_结束();
        Message 消息 = new Message();
        消息.what = 8;
        跨线程调用器.sendMessage(消息);
    }

    private void 防止休眠_开始() {
        try {
            if (防休眠锁 == null) {
                PowerManager 电源管理器 = (PowerManager) 运行环境.getSystemService(Context.POWER_SERVICE);
                防休眠锁 = 电源管理器.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "net.ssignal:Robot");
                if (防休眠锁 != null) {
                    防休眠锁.acquire();
                }
            }
        } catch (Exception e) {
        }
    }

    private void 防止休眠_结束() {
        try {
            if (防休眠锁 != null) {
                if (防休眠锁.isHeld()) { 防休眠锁.release(); }
                防休眠锁 = null;
            }
        } catch (Exception e) {
        }
    }

    boolean 收到讯宝(SSPackageReader SS包解读器, boolean 是即时推送讯宝) throws Exception {
        long 发送时间, 发送序号 = 0;
        byte 讯宝指令;
        String 群主英语讯宝地址 = "", 发送者英语讯宝地址, 讯宝文本;
        short 宽度 = 0, 高度 = 0;
        byte 秒数 = 0, 群编号;
        讯宝指令 = (byte)SS包解读器.读取_有标签("指令");
        if (讯宝指令 == ProtocolParameters.讯宝指令_从客户端发送至服务器成功) {
            if (是即时推送讯宝) {
                发送序号 = (long)SS包解读器.读取_有标签("发送序号");
                if (正在发送的讯宝 != null) {
                    if (正在发送的讯宝.发送序号 == 发送序号) {
//                        if (正在发送的讯宝.指令 == ProtocolParameters.讯宝指令_发送文字) {
//                            当前用户.记录运行步骤("发送成功：" + 正在发送的讯宝.文本);
//                        }
                        if (定时任务 != null) {
                            定时任务.cancel();
                            定时任务 = null;
                        }
                        心跳确认时间 = System.currentTimeMillis();
                        发送完毕(true);
//                        当前用户.记录运行步骤("传送服务器已收到");
//                        if (正在发送的讯宝.指令 == ProtocolParameters.讯宝指令_无) {
//                            当前用户.记录运行步骤("★收到心跳确认包");
//                        } else {
//                            当前用户.记录运行步骤("发送成功" + 发送序号);
//                        }
                    }
                }
            }
            return false;
//        } else if (是即时推送讯宝) {
//            当前用户.记录运行步骤("收到讯宝" + 讯宝指令);
        }
        发送者英语讯宝地址 = (String)SS包解读器.读取_有标签("发送者");
        if (SharedMethod.字符串未赋值或为空(发送者英语讯宝地址)) { return false; }
        发送时间 = (long)SS包解读器.读取_有标签("发送时间");
        if (发送时间 == 0) { return false; }
        发送时间 = ProtocolMethods.转换成Java相对时间(发送时间);
        if (讯宝指令 == ProtocolParameters.讯宝指令_用http访问我) {
            if (是即时推送讯宝) {
                Thread 线程 = new Thread_httpsRequest(this);
                线程.start();
            }
            return false;
        }
        if (讯宝指令 <= ProtocolParameters.讯宝指令_手机和电脑同步 && 是即时推送讯宝) {
            发送序号 = (long)SS包解读器.读取_有标签("发送序号");
            SSPackageCreator SS包生成器 = new SSPackageCreator();
            SS包生成器.添加_有标签("发送者", 发送者英语讯宝地址);
            SS包生成器.添加_有标签("发送序号", 发送序号);
            if (数据库_保存要发送的一对一讯宝(当前用户.获取英语讯宝地址(), SharedMethod.获取当前UTC时间(), ProtocolParameters.讯宝指令_确认收到, SS包生成器.生成纯文本(), (short)0, (short)0, (byte)0)) {
                发送讯宝(false);
            }
        }
        Object 值 = SS包解读器.读取_有标签("群编号");
        if (值 != null) {
            群编号 = (byte)值;
        } else {
            群编号 = 0;
        }
        值 = SS包解读器.读取_有标签("群主");
        if (值 != null) {
            群主英语讯宝地址 = (String)值;
        }
        if (群编号 > 0) {
            if (SharedMethod.字符串未赋值或为空(群主英语讯宝地址)) { return false; }
        }
        if (讯宝指令 < ProtocolParameters.讯宝指令_手机和电脑同步) {
            if (发送序号 == 0) {
                发送序号 = (long)SS包解读器.读取_有标签("发送序号");
                if (发送序号 == 0) { return false; }
            }
            讯宝文本 = (String)SS包解读器.读取_有标签("文本");
            switch (讯宝指令) {
                case ProtocolParameters.讯宝指令_发送图片:
                case ProtocolParameters.讯宝指令_发送短视频:
                    宽度 = (short)SS包解读器.读取_有标签("宽度");
                    if (宽度 < 1) { return false; }
                    高度 = (short)SS包解读器.读取_有标签("高度");
                    if (高度 < 1) { return false; }
                    break;
            }
            if (讯宝指令 == ProtocolParameters.讯宝指令_发送语音) {
                秒数 = (byte)SS包解读器.读取_有标签("秒数");
                if (秒数 == 0) { return false; }
            }
            switch (讯宝指令) {
                case ProtocolParameters.讯宝指令_撤回:
                    long 发送序号_撤回的讯宝 = Long.parseLong(讯宝文本);
                    if (数据库_发送者撤回讯宝(发送者英语讯宝地址, 群编号, 群主英语讯宝地址, 发送序号_撤回的讯宝, 发送时间)) {
                        if (主窗体 != null) {
                            主窗体.发送者撤回(发送者英语讯宝地址, 群编号, 群主英语讯宝地址, 发送序号_撤回的讯宝);
                        }
                    }
                    break;
                case ProtocolParameters.讯宝指令_获取小聊天群成员列表:
                    if (!是即时推送讯宝) { return false; }
                    try {
                        SSPackageReader SS包解读器2 = new SSPackageReader();
                        SS包解读器2.解读纯文本(讯宝文本);
                        Object SS包解读器3[] = SS包解读器2.读取_重复标签("M");
                        if (SS包解读器3 != null) {
                            GroupMember 群成员[] = new GroupMember[SS包解读器3.length];
                            GroupMember 某一成员;
                            SSPackageReader SS包解读器4;
                            for (int i = 0; i < SS包解读器3.length; i++) {
                                SS包解读器4 = (SSPackageReader)SS包解读器3[i];
                                某一成员 = new GroupMember();
                                某一成员.英语讯宝地址 = (String)SS包解读器4.读取_有标签("E");
                                某一成员.主机名 = (String)SS包解读器4.读取_有标签("H");
                                某一成员.位置号 = (short)SS包解读器4.读取_有标签("P");
                                某一成员.角色 = (byte)SS包解读器4.读取_有标签("R");
                                某一成员.本国语讯宝地址 = (String)SS包解读器4.读取_有标签("N");
                                群成员[i] = 某一成员;
                            }
                            Group_Small 加入的群[] = 当前用户.加入的小聊天群;
                            if (加入的群 != null) {
                                Group_Small 某一群;
                                for (int i = 0; i < 加入的群.length; i++) {
                                    某一群 = 加入的群[i];
                                    if (某一群.群主.英语讯宝地址.equals(群主英语讯宝地址) && 某一群.编号 == 群编号) {
                                        byte k = 1;
                                        for (int j = 0; j < 群成员.length; j++) {
                                            群成员[j].所属的群 = 某一群;
                                            if (群成员[j].角色 != ProtocolParameters.群角色_群主) {
                                                群成员[j].临时编号 = k;
                                                k += 1;
                                            }
                                        }
                                        某一群.群成员 = 群成员;
                                        某一群.待加入确认 = false;
                                        当前用户.保存小聊天群成员列表(某一群);
                                        break;
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        说(e.getMessage());
                        return false;
                    }
                    break;
                case ProtocolParameters.讯宝指令_修改图标:
                    if (!是即时推送讯宝) { return false; }
                    当前用户.头像更新时间 = Long.parseLong(讯宝文本);
                    Group_Small 加入的群[] = 当前用户.加入的小聊天群;
                    if (加入的群 != null) {
                        String 英语讯宝地址 = 当前用户.获取英语讯宝地址();
                        for (int i = 0; i < 加入的群.length; i++) {
                            if (加入的群[i].群主.英语讯宝地址.equals(英语讯宝地址)) {
                                加入的群[i].群主.头像更新时间 = 当前用户.头像更新时间;
                            }
                        }
                    }
                    说(界面文字.获取(169, "你的账号图标已更新。"));
                    break;
                case ProtocolParameters.讯宝指令_创建小聊天群:
                    if (!是即时推送讯宝) { return false; }
                    Contact 我 = new Contact();
                    我.英语讯宝地址 = 当前用户.获取英语讯宝地址();
                    if (!SharedMethod.字符串未赋值或为空(当前用户.域名_本国语)) {
                        我.本国语讯宝地址 = 当前用户.获取本国语讯宝地址();
                    }
                    我.主机名 = 当前用户.主机名;
                    我.头像更新时间 = 当前用户.头像更新时间;
                    Group_Small 新群 = new Group_Small();
                    新群.群主 = 我;
                    新群.备注 = 讯宝文本;
                    新群.编号 = 群编号;
                    GroupMember 某一成员 = new GroupMember();
                    某一成员.英语讯宝地址 = 我.英语讯宝地址;
                    某一成员.本国语讯宝地址 = 我.本国语讯宝地址;
                    某一成员.主机名 = 当前用户.主机名;
                    某一成员.位置号 = 当前用户.位置号;
                    某一成员.角色 = ProtocolParameters.群角色_群主;
                    某一成员.所属的群 = 新群;
                    新群.群成员 = new GroupMember[1];
                    新群.群成员[0] = 某一成员;
                    加入的群 = 当前用户.加入的小聊天群;
                    if (加入的群 == null) {
                        加入的群 = new Group_Small[1];
                        加入的群[0] = 新群;
                        当前用户.加入的小聊天群 = 加入的群;
                    } else {
                        Group_Small 加入的群2[] = new Group_Small[加入的群.length + 1];
                        System.arraycopy(加入的群, 0, 加入的群2, 0, 加入的群.length);
                        加入的群2[加入的群2.length - 1] = 新群;
                        当前用户.加入的小聊天群 = 加入的群2;
                    }
                    ChatWith 聊天对象 = new ChatWith();
                    聊天对象.讯友或群主 = 我;
                    聊天对象.小聊天群 = 新群;
                    if (任务 != null) { 任务.结束(); }
                    if (主窗体 != null) {
                        主窗体.添加聊天控件(聊天对象);
                    }
                    if (数据库_更新最近互动讯友排名(我.英语讯宝地址, 群编号)) {
                        if (主窗体 != null) {
                            主窗体.刷新讯友录(Constants.讯友录显示范围_聊天群);
                        }
                    }
                    break;
                case ProtocolParameters.讯宝指令_解散小聊天群:
                    if (!是即时推送讯宝) { return false; }
                    if (群主英语讯宝地址.equals(当前用户.获取英语讯宝地址())) {
                        退出小聊天群(群主英语讯宝地址, 群编号);
                    }
                    break;
                default:
                    switch (讯宝指令) {
                        case ProtocolParameters.讯宝指令_某人加入聊天群:
                            String 英语讯宝地址, 本国语讯宝地址;
                            SSPackageReader SS包解读器2;
                            try {
                                SS包解读器2 = new SSPackageReader();
                                SS包解读器2.解读纯文本(讯宝文本);
                                英语讯宝地址 = (String)SS包解读器2.读取_有标签("E");
                                本国语讯宝地址 = (String)SS包解读器2.读取_有标签("N");
                            } catch (Exception e) {
                                说(e.getMessage());
                                return false;
                            }
                            讯宝文本 = (SharedMethod.字符串未赋值或为空(本国语讯宝地址) ? "" : 本国语讯宝地址 + " / ") + 英语讯宝地址;
                            if (是即时推送讯宝 && !英语讯宝地址.equals(当前用户.获取英语讯宝地址())) {
                                聊天群成员增加(群主英语讯宝地址, 群编号, SS包解读器2, 英语讯宝地址, 本国语讯宝地址);
                            }
                            break;
                        case ProtocolParameters.讯宝指令_退出小聊天群:
                            try {
                                SS包解读器2 = new SSPackageReader();
                                SS包解读器2.解读纯文本(讯宝文本);
                                英语讯宝地址 = (String)SS包解读器2.读取_有标签("E");
                                值 = SS包解读器2.读取_有标签("N");
                                if (值 != null) {
                                    本国语讯宝地址 = (String) 值;
                                } else {
                                    本国语讯宝地址 = null;
                                }
                            } catch (Exception e) {
                                说(e.getMessage());
                                return false;
                            }
                            if (是即时推送讯宝) {
                                if (当前用户.获取英语讯宝地址().equals(英语讯宝地址)) {
                                    退出小聊天群(群主英语讯宝地址, 群编号);
                                    return false;
                                } else {
                                    聊天群成员减少(群主英语讯宝地址, 群编号, 英语讯宝地址);
                                }
                            }
                            讯宝文本 = (SharedMethod.字符串未赋值或为空(本国语讯宝地址) ? "" : 本国语讯宝地址 + " / ") + 英语讯宝地址;
                            break;
                        case ProtocolParameters.讯宝指令_删减聊天群成员:
                            try {
                                SS包解读器2 = new SSPackageReader();
                                SS包解读器2.解读纯文本(讯宝文本);
                                英语讯宝地址 = (String)SS包解读器2.读取_有标签("E");
                                值 = SS包解读器2.读取_有标签("N");
                                if (值 != null) {
                                    本国语讯宝地址 = (String) 值;
                                } else {
                                    本国语讯宝地址 = null;
                                }
                            } catch (Exception e) {
                                说(e.getMessage());
                                return false;
                            }
                            if (!是即时推送讯宝) {
                                聊天群成员减少(群主英语讯宝地址, 群编号, 英语讯宝地址);
                            }
                            讯宝文本 = (SharedMethod.字符串未赋值或为空(本国语讯宝地址) ? "" : 本国语讯宝地址 + " / ") + 英语讯宝地址;
                            break;
                        case ProtocolParameters.讯宝指令_修改聊天群名称:
                            if (!是即时推送讯宝) {
                                加入的群 = 当前用户.加入的小聊天群;
                                if (加入的群 == null) { return false; }
                                for (int i = 0; i < 加入的群.length; i++) {
                                    if (加入的群[i].群主.英语讯宝地址.equals(群主英语讯宝地址) && 加入的群[i].编号 == 群编号) {
                                        加入的群[i].备注 = 讯宝文本;
                                        switch (当前用户.讯友录当前显示范围) {
                                            case Constants.讯友录显示范围_聊天群:
                                            case Constants.讯友录显示范围_最近:
                                                if (主窗体 != null) {
                                                    主窗体.刷新讯友录();
                                                }
                                                break;
                                        }
                                        break;
                                    }
                                }
                            }
                            break;
                    }
                    if (群编号 == 0) {
                        if (!发送者英语讯宝地址.equals(当前用户.获取英语讯宝地址())) {
                            if (当前用户.查找讯友(发送者英语讯宝地址) != null) {
                                if (数据库_保存收到的一对一讯宝(发送者英语讯宝地址, false, 发送序号, 发送时间, 讯宝指令, 讯宝文本, 宽度, 高度, 秒数)) {
                                    boolean 刷新 = 数据库_更新最近互动讯友排名(发送者英语讯宝地址, (long) 0);
                                    if (主窗体 != null) {
                                        主窗体.显示收到的讯友讯宝(发送者英语讯宝地址, 群编号, 群主英语讯宝地址, 发送时间, 发送序号, 讯宝指令, 讯宝文本, 宽度, 高度, 秒数, 刷新);
//                                        当前用户.记录运行步骤("新讯宝已显示");
                                    } else {
                                        short 新讯宝数量 = 数据库_获取新讯宝数量(发送者英语讯宝地址, 群编号);
                                        if (新讯宝数量 < 999) {
                                            新讯宝数量 += 1;
                                            数据库_更新新讯宝数量(发送者英语讯宝地址, 群编号, 新讯宝数量);
                                        }
                                    }
                                    if (是即时推送讯宝) {
                                        提示有新消息(讯宝指令, 讯宝文本, 秒数);
                                    } else {
                                        return true;
                                    }
                                }
                            } else {
                                if (数据库_保存收到的陌生人讯宝(发送者英语讯宝地址, 发送序号, 发送时间, 讯宝指令, 讯宝文本)) {
                                    if (主窗体 != null) {
                                        主窗体.显示收到的陌生人讯宝(发送者英语讯宝地址, 发送时间, 发送序号, 讯宝指令, 讯宝文本);
                                    } else {
                                        short 新讯宝数量 = 数据库_获取陌生人新讯宝数量();
                                        if (新讯宝数量 < 999) {
                                            新讯宝数量 += 1;
                                            数据库_更新陌生人新讯宝数量(新讯宝数量);
                                        }
                                    }
                                    if (是即时推送讯宝) {
                                        提示有新消息(讯宝指令, 讯宝文本, (byte) 0);
                                    } else {
                                        return true;
                                    }
                                }
                            }
                        } else {
                            if (数据库_保存收到的一对一讯宝(群主英语讯宝地址, true, 发送序号, 发送时间, 讯宝指令, 讯宝文本, 宽度, 高度, 秒数)) {
                                boolean 刷新 = 数据库_更新最近互动讯友排名(群主英语讯宝地址, (long) 0);
                                if (主窗体 != null) {
                                    主窗体.显示同步的讯宝(群编号, 群主英语讯宝地址, 发送时间, 发送序号, 讯宝指令, 讯宝文本, 宽度, 高度, 秒数, 刷新);
                                }
                            }
                        }
                    } else {
                        if (!发送者英语讯宝地址.equals(当前用户.获取英语讯宝地址())) {
                            if (当前用户.查找讯友(群主英语讯宝地址) != null || 群主英语讯宝地址.equals(当前用户.获取英语讯宝地址())) {
                                if (数据库_保存收到的小聊天群讯宝(群主英语讯宝地址, 群编号, 发送者英语讯宝地址, 发送序号, 发送时间, 讯宝指令, 讯宝文本, 宽度, 高度, 秒数)) {
                                    boolean 刷新 = 数据库_更新最近互动讯友排名(群主英语讯宝地址, 群编号);
                                    if (主窗体 != null) {
                                        主窗体.显示收到的讯友讯宝(发送者英语讯宝地址, 群编号, 群主英语讯宝地址, 发送时间, 发送序号, 讯宝指令, 讯宝文本, 宽度, 高度, 秒数, 刷新);
                                    } else {
                                        short 新讯宝数量 = 数据库_获取新讯宝数量(群主英语讯宝地址, 群编号);
                                        if (新讯宝数量 < 999) {
                                            新讯宝数量 += 1;
                                            数据库_更新新讯宝数量(群主英语讯宝地址, 群编号, 新讯宝数量);
                                        }
                                    }
                                    if (是即时推送讯宝) {
                                        提示有新消息(讯宝指令, 讯宝文本, 秒数);
                                    } else {
                                        return true;
                                    }
                                }
                            }
                        } else {
                            if (数据库_保存收到的小聊天群讯宝(群主英语讯宝地址, 群编号, 发送者英语讯宝地址, 发送序号, 发送时间, 讯宝指令, 讯宝文本, 宽度, 高度, 秒数)) {
                                boolean 刷新 = 数据库_更新最近互动讯友排名(群主英语讯宝地址, 群编号);
                                if (主窗体 != null) {
                                    主窗体.显示同步的讯宝(群编号, 群主英语讯宝地址, 发送时间, 发送序号, 讯宝指令, 讯宝文本, 宽度, 高度, 秒数, 刷新);
                                }
                            }
                        }
                    }
            }
        } else if (是即时推送讯宝) {
            if (讯宝指令 == ProtocolParameters.讯宝指令_手机和电脑同步) {
                讯宝文本 = (String) SS包解读器.读取_有标签("文本");
                try {
                    SSPackageReader SS包解读器2 = new SSPackageReader();
                    SS包解读器2.解读纯文本(讯宝文本);
                    byte 同步事件 = (byte) SS包解读器2.读取_有标签("事件");
                    switch (同步事件) {
                        case ProtocolParameters.同步事件_电脑上线:
                            说(界面文字.获取(248, "你的电脑上线了。"));
                            break;
                        case ProtocolParameters.同步事件_讯友添加标签:
                        case ProtocolParameters.同步事件_讯友移除标签:
                        case ProtocolParameters.同步事件_修改讯友备注:
                        case ProtocolParameters.同步事件_拉黑讯友:
                            String 英语讯宝地址 = (String) SS包解读器2.读取_有标签("英语讯宝地址");
                            if (!SharedMethod.字符串未赋值或为空(英语讯宝地址)) {
                                boolean 有控件 = false;
                                if (主窗体 != null) {
                                    有控件 = 主窗体.事件同步(同步事件, 英语讯宝地址, SS包解读器2);
                                }
                                if (!有控件 && 当前用户.讯友目录 != null) {
                                    事件同步(同步事件, 英语讯宝地址, SS包解读器2);
                                }
                            }
                            break;
                        case ProtocolParameters.同步事件_添加讯友:
                            添加讯友成功(SS包解读器2);
                            break;
                        case ProtocolParameters.同步事件_删除讯友:
                            删除讯友成功(SS包解读器2);
                            break;
                        case ProtocolParameters.同步事件_重命名标签:
                            重命名标签成功(SS包解读器2);
                            break;
                        case ProtocolParameters.同步事件_取消拉黑讯友:
                            取消拉黑讯友成功(SS包解读器2);
                            break;
                        case ProtocolParameters.同步事件_修改群名称:
                            群名称修改了(SS包解读器2);
                            break;
                        case ProtocolParameters.同步事件_添加黑域:
                            增加黑白域(true, SS包解读器2);
                            break;
                        case ProtocolParameters.同步事件_添加白域:
                            增加黑白域(false, SS包解读器2);
                            break;
                        case ProtocolParameters.同步事件_移除黑域:
                            移除黑白域(true, SS包解读器2);
                            break;
                        case ProtocolParameters.同步事件_移除白域:
                            移除黑白域(false, SS包解读器2);
                            break;
                        case ProtocolParameters.同步事件_加入小聊天群:
                            加入小聊天群(SS包解读器2);
                            break;
                        case ProtocolParameters.同步事件_加入大聊天群:
                            加入大聊天群(SS包解读器2);
                            break;
                        case ProtocolParameters.同步事件_退出小聊天群:

                            break;
                        case ProtocolParameters.同步事件_退出大聊天群:
                            退出大聊天群(SS包解读器2);
                            break;
                    }
                } catch (Exception e) {
                    说(e.getMessage());
                    return false;
                }
            } else if (讯宝指令 == ProtocolParameters.讯宝指令_对讯友录的编辑过于频繁) {
                说(界面文字.获取(297, "请不要过于频繁地编辑讯友录。"));
            } else {
                if (发送序号 == 0) {
                    发送序号 = (long)SS包解读器.读取_有标签("发送序号");
                    if (发送序号 == 0) { return false; }
                }
                if (讯宝指令 == ProtocolParameters.讯宝指令_被邀请加入大聊天群者未添加我为讯友) {
                    讯宝文本 = (String) SS包解读器.读取_有标签("文本");
                }
                数据库_删除发送失败的讯宝(发送者英语讯宝地址, 群编号, 群主英语讯宝地址, 讯宝指令, 发送序号);
                if (主窗体 != null) {
                    主窗体.提示讯宝发送失败(发送者英语讯宝地址, 群编号, 群主英语讯宝地址, 讯宝指令, 发送序号, null);
                }
            }
        }
        return false;
    }

    private boolean 数据库_保存收到的一对一讯宝(String 讯宝地址, boolean 是接收者, long 发送序号, long 发送时间, byte 讯宝指令, String 文本, short 宽度, short 高度, byte 秒数) {
        try {
            String 路径 = 运行环境.getFilesDir().toString() + "/" + 当前用户.获取英语讯宝地址();
            File 目录 = new File(路径);
            if (!目录.exists() || !目录.isDirectory()) {
                目录.mkdir();
            }
            File 文件 = new File(路径 + "/" + 讯宝地址 + ".sscj");
            if (!文件.exists() || 文件.isDirectory()) {
                文件.createNewFile();
            }
            RandomAccessFile 文件随机访问器 = new RandomAccessFile(文件.getAbsolutePath(), "rwd");
            文件随机访问器.seek(文件随机访问器.length());
            SSPackageReader SS包读取器;
            long 发送序号2;
            do {
                SS包读取器 = SharedMethod.读取一个SS包(文件随机访问器);
                if (SS包读取器 == null) {
                    SS包读取器 = SharedMethod.读取一个SS包2(文件随机访问器);
                }
                if (SS包读取器 != null) {
                    if (!(boolean) SS包读取器.读取_有标签("是接收者")) {
                        发送序号2 = (long) SS包读取器.读取_有标签("发送序号");
                        if (发送序号2 == 发送序号) {
                            文件随机访问器.close();
                            return false;
                        } else if (发送序号2 < 发送序号) {
                            break;
                        }
                    }
                }
            } while (SS包读取器 != null);
            SSPackageCreator SS包生成器 = new SSPackageCreator();
            SS包生成器.添加_有标签("删除", false);
            SS包生成器.添加_有标签("是接收者", 是接收者);
            SS包生成器.添加_有标签("发送序号", 发送序号);
            SS包生成器.添加_有标签("指令", 讯宝指令);
            if (!SharedMethod.字符串未赋值或为空(文本)) {
                SS包生成器.添加_有标签("文本", 文本);
            }
            if (宽度 > 0) {
                SS包生成器.添加_有标签("宽度", 宽度);
            }
            if (高度 > 0) {
                SS包生成器.添加_有标签("高度", 高度);
            }
            if (秒数 > 0) {
                SS包生成器.添加_有标签("秒数", 秒数);
                SS包生成器.添加_有标签("已收听", false);
            }
            SS包生成器.添加_有标签("发送时间", 发送时间);
            byte 字节数组1[] = SS包生成器.生成SS包();
            byte 字节数组2[] = Convert.getbytes((short) 字节数组1.length);
            SSPackageCreator SS包生成器2 = new SSPackageCreator(true);
            SS包生成器2.添加_无标签(字节数组1, ProtocolParameters.长度信息字节数_零字节);
            SS包生成器2.添加_无标签(字节数组2, ProtocolParameters.长度信息字节数_零字节);
            文件随机访问器.seek(文件随机访问器.length());
            文件随机访问器.write(SS包生成器2.生成字节数组());
            文件随机访问器.close();
            return true;
        } catch (Exception e) {
            说(e.getMessage());
            return false;
        }
    }

    private boolean 数据库_保存收到的小聊天群讯宝(String 群主讯宝地址, byte 群编号, String 发送者讯宝地址, long 发送序号, long 发送时间, byte 讯宝指令, String 文本, short 宽度, short 高度, byte 秒数) {
        try {
            String 路径 = 运行环境.getFilesDir().toString() + "/" + 当前用户.获取英语讯宝地址();
            File 目录 = new File(路径);
            if (!目录.exists() || !目录.isDirectory()) {
                目录.mkdir();
            }
            File 文件 = new File(路径 + "/" + 群主讯宝地址 + "#" + 群编号 + ".sscj");
            if (!文件.exists() || 文件.isDirectory()) {
                文件.createNewFile();
            }
            RandomAccessFile 文件随机访问器 = new RandomAccessFile(文件.getAbsolutePath(), "rwd");
            文件随机访问器.seek(文件随机访问器.length());
            SSPackageReader SS包读取器;
            long 发送序号2;
            do {
                SS包读取器 = SharedMethod.读取一个SS包(文件随机访问器);
                if (SS包读取器 == null) {
                    SS包读取器 = SharedMethod.读取一个SS包2(文件随机访问器);
                }
                if (SS包读取器 != null) {
                    if (发送者讯宝地址.equals((String) SS包读取器.读取_有标签("发送者"))) {
                        发送序号2 = (long) SS包读取器.读取_有标签("发送序号");
                        if (发送序号2 == 发送序号) {
                            文件随机访问器.close();
                            return false;
                        } else if (发送序号2 < 发送序号) {
                            break;
                        }
                    }
                }
            } while (SS包读取器 != null);
            SSPackageCreator SS包生成器 = new SSPackageCreator();
            SS包生成器.添加_有标签("删除", false);
            SS包生成器.添加_有标签("发送者", 发送者讯宝地址);
            SS包生成器.添加_有标签("发送序号", 发送序号);
            SS包生成器.添加_有标签("指令", 讯宝指令);
            if (!SharedMethod.字符串未赋值或为空(文本)) {
                SS包生成器.添加_有标签("文本", 文本);
            }
            if (宽度 > 0) {
                SS包生成器.添加_有标签("宽度", 宽度);
            }
            if (高度 > 0) {
                SS包生成器.添加_有标签("高度", 高度);
            }
            if (秒数 > 0) {
                SS包生成器.添加_有标签("秒数", 秒数);
                SS包生成器.添加_有标签("已收听", false);
            }
            SS包生成器.添加_有标签("发送时间", 发送时间);
            byte 字节数组1[] = SS包生成器.生成SS包();
            byte 字节数组2[] = Convert.getbytes((short) 字节数组1.length);
            SSPackageCreator SS包生成器2 = new SSPackageCreator(true);
            SS包生成器2.添加_无标签(字节数组1, ProtocolParameters.长度信息字节数_零字节);
            SS包生成器2.添加_无标签(字节数组2, ProtocolParameters.长度信息字节数_零字节);
            文件随机访问器.seek(文件随机访问器.length());
            文件随机访问器.write(SS包生成器2.生成字节数组());
            文件随机访问器.close();
            return true;
        } catch (Exception e) {
            说(e.getMessage());
            return false;
        }
    }

    private boolean 数据库_保存收到的陌生人讯宝(String 讯宝地址, long 发送序号, long 发送时间, byte 讯宝指令, String 文本) {
        try {
            String 路径 = 运行环境.getFilesDir().toString() + "/" + 当前用户.获取英语讯宝地址();
            File 目录 = new File(路径);
            if (!目录.exists() || !目录.isDirectory()) {
                目录.mkdir();
            }
            File 文件 = new File(路径 + "/strangers.sscj");
            if (!文件.exists() || 文件.isDirectory()) {
                文件.createNewFile();
            }
            RandomAccessFile 文件随机访问器 = new RandomAccessFile(文件.getAbsolutePath(), "rwd");
            文件随机访问器.seek(文件随机访问器.length());
            SSPackageReader SS包读取器;
            String 讯宝地址2;
            long 发送序号2;
            do {
                SS包读取器 = SharedMethod.读取一个SS包(文件随机访问器);
                if (SS包读取器 == null) {
                    SS包读取器 = SharedMethod.读取一个SS包2(文件随机访问器);
                }
                if (SS包读取器 != null) {
                    讯宝地址2 = (String) SS包读取器.读取_有标签("讯宝地址");
                    if (讯宝地址.equals(讯宝地址2)) {
                        发送序号2 = (long) SS包读取器.读取_有标签("发送序号");
                        if (发送序号2 == 发送序号) {
                            文件随机访问器.close();
                            return false;
                        } else if (发送序号2 < 发送序号) {
                            break;
                        }
                    }
                }
            } while (SS包读取器 != null);
            SSPackageCreator SS包生成器 = new SSPackageCreator();
            SS包生成器.添加_有标签("删除", false);
            SS包生成器.添加_有标签("讯宝地址", 讯宝地址);
            SS包生成器.添加_有标签("发送序号", 发送序号);
            SS包生成器.添加_有标签("指令", 讯宝指令);
            SS包生成器.添加_有标签("文本", 文本);
            SS包生成器.添加_有标签("发送时间", 发送时间);
            byte 字节数组1[] = SS包生成器.生成SS包();
            byte 字节数组2[] = Convert.getbytes((short) 字节数组1.length);
            SSPackageCreator SS包生成器2 = new SSPackageCreator(true);
            SS包生成器2.添加_无标签(字节数组1, ProtocolParameters.长度信息字节数_零字节);
            SS包生成器2.添加_无标签(字节数组2, ProtocolParameters.长度信息字节数_零字节);
            文件随机访问器.seek(文件随机访问器.length());
            文件随机访问器.write(SS包生成器2.生成字节数组());
            文件随机访问器.close();
            return true;
        } catch (Exception e) {
            说(e.getMessage());
            return false;
        }
    }

    private void 事件同步(byte 同步事件, String 英语讯宝地址, SSPackageReader SS包解读器) throws Exception {
        Contact 讯友目录[] = 当前用户.讯友目录;
        int i;
        for (i = 0; i < 讯友目录.length; i++) {
            if (讯友目录[i].英语讯宝地址.equals(英语讯宝地址)) {
                break;
            }
        }
        if (i < 讯友目录.length) {
            switch (同步事件) {
                case ProtocolParameters.同步事件_讯友添加标签:
                    String 标签名称 = (String) SS包解读器.读取_有标签("标签名称");
                    Contact 当前讯友 = 讯友目录[i];
                    if (SharedMethod.字符串未赋值或为空(当前讯友.标签一)) {
                        当前讯友.标签一 = 标签名称;
                    } else if (SharedMethod.字符串未赋值或为空(当前讯友.标签二)) {
                        当前讯友.标签二 = 标签名称;
                    }
                    break;
                case ProtocolParameters.同步事件_讯友移除标签:
                    标签名称 = (String) SS包解读器.读取_有标签("标签名称");
                    当前讯友 = 讯友目录[i];
                    if (标签名称.equalsIgnoreCase(当前讯友.标签一)) {
                        当前讯友.标签一 = null;
                    }if (标签名称.equalsIgnoreCase(当前讯友.标签二)) {
                    当前讯友.标签二 = null;
                }
                    break;
                case ProtocolParameters.同步事件_修改讯友备注:
                    讯友目录[i].备注 = (String)SS包解读器.读取_有标签("备注");
                    switch (当前用户.讯友录当前显示范围) {
                        case Constants.讯友录显示范围_最近:
                        case Constants.讯友录显示范围_讯友:
                        case Constants.讯友录显示范围_某标签:
                        case Constants.讯友录显示范围_黑名单:
                            if (主窗体 != null) {
                                主窗体.刷新讯友录();
                            }
                    }
                    break;
                case ProtocolParameters.同步事件_拉黑讯友:
                    讯友目录[i].拉黑 = true;
                    switch (当前用户.讯友录当前显示范围) {
                        case Constants.讯友录显示范围_最近:
                        case Constants.讯友录显示范围_讯友:
                        case Constants.讯友录显示范围_某标签:
                        case Constants.讯友录显示范围_黑名单:
                            if (主窗体 != null) {
                                主窗体.刷新讯友录();
                            }
                    }
                    break;
            }
        }
    }

    private void 退出小聊天群(String 群主英语讯宝地址, byte 群编号) {
        Group_Small 加入的群[] = 当前用户.加入的小聊天群;
        if (加入的群 == null) { return; }
        int i;
        for (i = 0; i < 加入的群.length; i++) {
            if (加入的群[i].群主.英语讯宝地址.equals(群主英语讯宝地址) && 加入的群[i].编号 == 群编号) {
                break;
            }
        }
        if (i < 加入的群.length) {
            if (加入的群.length > 1) {
                Group_Small 加入的群2[] = new Group_Small[加入的群.length - 1];
                if (i > 0) {
                    System.arraycopy(加入的群, 0, 加入的群2, 0, i);
                }
                if (i < 加入的群.length - 1) {
                    System.arraycopy(加入的群, i + 1, 加入的群2, i, 加入的群.length - i - 1);
                }
                当前用户.加入的小聊天群 = 加入的群2;
            } else {
                当前用户.加入的小聊天群 = null;
            }
            if (主窗体 != null) {
                主窗体.关闭聊天控件(群主英语讯宝地址, 群编号);
                主窗体.刷新讯友录(Constants.讯友录显示范围_聊天群);
            }
            清除小聊天群数据(群主英语讯宝地址, 群编号);
        }
    }

    private void 聊天群成员增加(String 群主英语讯宝地址, byte 群编号, SSPackageReader SS包解读器, String 英语讯宝地址, String 本国语讯宝地址) throws Exception {
        Group_Small 加入的群[] = 当前用户.加入的小聊天群;
        if (加入的群 == null) { return; }
        int i;
        for (i = 0; i < 加入的群.length; i++) {
            if (加入的群[i].群主.英语讯宝地址.equals(群主英语讯宝地址) && 加入的群[i].编号 == 群编号) {
                break;
            }
        }
        if (i < 加入的群.length) {
            GroupMember 群成员[] = 加入的群[i].群成员;
            if (群成员 == null) { return; }
            String 主机名 = (String)SS包解读器.读取_有标签("H");
            short 位置号 = (short)SS包解读器.读取_有标签("P");
            int j;
            for (j = 0; j < 群成员.length; j++) {
                if (群成员[j].英语讯宝地址.equals(英语讯宝地址)) {
                    break;
                }
            }
            if (j == 群成员.length) {
                GroupMember 新成员 = new GroupMember();
                新成员.英语讯宝地址 = 英语讯宝地址;
                新成员.本国语讯宝地址 = 本国语讯宝地址;
                新成员.主机名 = 主机名;
                新成员.位置号 = 位置号;
                新成员.角色 = ProtocolParameters.群角色_成员_可以发言;
                新成员.所属的群 = 加入的群[i];
                GroupMember 群成员2[] = new GroupMember[群成员.length + 1];
                System.arraycopy(群成员, 0, 群成员2, 0, 群成员.length);
                群成员2[群成员2.length - 1] = 新成员;
                群成员 = 群成员2;
                byte k = 1;
                for (j = 0; j < 群成员.length; j++) {
                    if (群成员[j].角色 != ProtocolParameters.群角色_群主) {
                        群成员[j].临时编号 = k;
                        k += 1;
                    }
                }
                加入的群[i].群成员 = 群成员;
                当前用户.保存小聊天群成员列表(加入的群[i]);
                if (主窗体 != null) {
                    主窗体.小聊天群成员有变化(群主英语讯宝地址, 群编号);
                }
            } else {
                if (群成员[j].角色 == ProtocolParameters.群角色_邀请加入_可以发言) {
                    群成员[j].角色 = ProtocolParameters.群角色_成员_可以发言;
                    当前用户.保存小聊天群成员列表(加入的群[i]);
                    if (主窗体 != null) {
                        主窗体.小聊天群成员有变化(群主英语讯宝地址, 群编号);
                    }
                }
            }
        }
    }

    private void 聊天群成员减少(String 群主英语讯宝地址, byte 群编号, String 英语讯宝地址) {
        Group_Small 加入的群[] = 当前用户.加入的小聊天群;
        if (加入的群 == null) { return; }
        int i;
        for (i = 0; i < 加入的群.length; i++) {
            if (加入的群[i].群主.英语讯宝地址.equals(群主英语讯宝地址) && 加入的群[i].编号 == 群编号) {
                break;
            }
        }
        if (i < 加入的群.length) {
            GroupMember 群成员[] = 加入的群[i].群成员;
            if (群成员 == null) { return; }
            int j;
            for (j = 0; j < 群成员.length; j++) {
                if (群成员[j].英语讯宝地址.equals(英语讯宝地址)) {
                    break;
                }
            }
            if (j < 群成员.length) {
                群成员[j] = null;
                GroupMember 群成员2[] = new GroupMember[群成员.length - 1];
                int m = 0;
                byte k = 1;
                for (j = 0; j < 群成员.length; j++) {
                    if (群成员[j] != null) {
                        群成员2[m] = 群成员[j];
                        if (群成员2[m].角色 != ProtocolParameters.群角色_群主) {
                            群成员2[m].临时编号 = k;
                            k += 1;
                        }
                        m += 1;
                    }
                }
                加入的群[i].群成员 = 群成员2;
                当前用户.保存小聊天群成员列表(加入的群[i]);
                if (主窗体 != null) {
                    主窗体.小聊天群成员有变化(群主英语讯宝地址, 群编号);
                }
            }
        }
    }

    public void 用http访问传送服务器() {
        HttpsURLConnection HTTPS连接 = null;
        try {
            URL url = new URL(ProtocolPath.获取传送服务器访问路径开头(当前用户.主机名, 当前用户.域名_英语, false));
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
            HTTPS连接.setUseCaches(false);
            HTTPS连接.setRequestMethod("GET");
            HTTPS连接.setRequestProperty("Content-Type", "text/xml");
            HTTPS连接.setRequestProperty("Content-Length", String.valueOf(0));
            HTTPS连接.connect();
            if (HTTPS连接.getResponseCode() == 200) { //HTTP_OK = 200
                if (HTTPS连接.getContentLength() > 0) {
                }
            }
        } catch (Exception e) {
        } finally {
            if (HTTPS连接 != null) {HTTPS连接.disconnect();}
        }
    }

    private void 群名称修改了(SSPackageReader SS包解读器) throws Exception {
        if (当前用户.加入的小聊天群 == null) {
            return;
        }
        byte 群编号 = (byte) SS包解读器.读取_有标签("群编号");
        String 群备注 = (String) SS包解读器.读取_有标签("群备注");
        Group_Small 加入的群[] = 当前用户.加入的小聊天群;
        String 用户英语讯宝地址 = 当前用户.获取英语讯宝地址();
        for (int i = 0; i < 加入的群.length; i++) {
            if (加入的群[i].编号 == 群编号 && 加入的群[i].群主.英语讯宝地址.equals(用户英语讯宝地址)) {
                加入的群[i].备注 = 群备注;
                if (当前用户.讯友录当前显示范围 == Constants.讯友录显示范围_聊天群) {
                    if (主窗体 != null) {
                        主窗体.刷新讯友录();
                    }
                }
                break;
            }
        }
    }

    private void 增加黑白域(boolean 是黑域, SSPackageReader SS包解读器) throws Exception {
        String 英语域名 = (String)SS包解读器.读取_有标签("英语域名");
        Object 值 = SS包解读器.读取_有标签("本国语域名");
        long 讯友录更新时间 = (long) SS包解读器.读取_有标签("时间");
        String 本国语域名;
        if (值 != null) {
            本国语域名 = (String) 值;
        } else {
            本国语域名 = null;
        }
        if (!是黑域 || !英语域名.equals(ProtocolParameters.黑域_全部)) {
            if (当前用户.讯友目录 == null) {
                return;
            }
            if (本国语域名 == null) {
                Contact 讯友目录[] = 当前用户.讯友目录;
                String 字符串 = ProtocolParameters.讯宝地址标识 + 英语域名;
                int i;
                for (i = 0; i < 讯友目录.length; i++) {
                    if (讯友目录[i].英语讯宝地址.endsWith(字符串)) {
                        break;
                    }
                }
                if (i == 讯友目录.length) {
                    return;
                }
                if (!SharedMethod.字符串未赋值或为空(讯友目录[i].本国语讯宝地址)) {
                    String 段[] = 讯友目录[i].本国语讯宝地址.split(ProtocolParameters.讯宝地址标识);
                    本国语域名 = 段[1];
                } else {
                    本国语域名 = null;
                }
            }
        } else {
            本国语域名 = null;
        }
        if (是黑域) {
            Domain 黑域2[];
            if (当前用户.黑域 != null) {
                黑域2 = new Domain[当前用户.黑域.length + 1];
                System.arraycopy(当前用户.黑域, 0, 黑域2, 0, 当前用户.黑域.length);
            } else {
                黑域2 = new Domain[1];
            }
            Domain 某一域 = new Domain();
            某一域.英语 = 英语域名;
            某一域.本国语 = 本国语域名;
            黑域2[黑域2.length - 1] = 某一域;
            当前用户.黑域 = 黑域2;
            主窗体.刷新讯友录(Constants.讯友录显示范围_黑域, null, false);
        } else {
            Domain 白域2[];
            if (当前用户.白域 != null) {
                白域2 = new Domain[当前用户.白域.length + 1];
                System.arraycopy(当前用户.白域, 0, 白域2, 0, 当前用户.白域.length);
            } else {
                白域2 = new Domain[1];
            }
            Domain 某一域 = new Domain();
            某一域.英语 = 英语域名;
            某一域.本国语 = 本国语域名;
            白域2[白域2.length - 1] = 某一域;
            当前用户.白域 = 白域2;
            主窗体.刷新讯友录(Constants.讯友录显示范围_白域, null, false);
        }
        if (讯友录更新时间 > 0) { 当前用户.讯友录有变动(讯友录更新时间); }
        String 域名;
        if (SharedMethod.字符串未赋值或为空(本国语域名)) {
            域名 = 英语域名;
        } else {
            域名 = 本国语域名 + " / " + 英语域名;
        }
        if (是黑域) {
            说(界面文字.获取(249, "域名[#%]被添加至黑域列表。", new Object[]{域名}));
        } else {
            说(界面文字.获取(250, "域名[#%]被添加至白域列表。", new Object[] {域名}));
        }
    }

    private void 移除黑白域(boolean 是黑域, SSPackageReader SS包解读器) throws Exception {
        String 英语域名 = (String)SS包解读器.读取_有标签("英语域名");
        long 讯友录更新时间 = (long) SS包解读器.读取_有标签("时间");
        if (是黑域) {
            if (当前用户.黑域 != null) {
                if (当前用户.黑域.length > 1) {
                    Domain 黑域2[] = new Domain[当前用户.黑域.length - 1];
                    Domain 黑域[] = 当前用户.黑域;
                    int i, j = 0;
                    for (i = 0; i < 黑域.length; i++) {
                        if (!黑域[i].英语.equals(英语域名)) {
                            黑域2[j] = 黑域[i];
                            j++;
                        }
                    }
                    当前用户.黑域 = 黑域2;
                } else {
                    当前用户.黑域 = null;
                }
                if (讯友录更新时间 > 0) { 当前用户.讯友录有变动(讯友录更新时间); }
                主窗体.刷新讯友录(Constants.讯友录显示范围_黑域, null, false);
            }
            说(界面文字.获取(251, "域名[#%]从黑域列表移除了。", new Object[] {英语域名}));
        } else {
            if (当前用户.白域 != null) {
                if (当前用户.白域.length > 1) {
                    Domain 白域2[] = new Domain[当前用户.白域.length - 1];
                    Domain 白域[] = 当前用户.白域;
                    int i, j = 0;
                    for (i = 0; i < 白域.length; i++) {
                        if (!白域[i].equals(英语域名)) {
                            白域2[j] = 白域[i];
                            j++;
                        }
                    }
                    当前用户.白域 = 白域2;
                } else {
                    当前用户.白域 = null;
                }
                if (讯友录更新时间 > 0) { 当前用户.讯友录有变动(讯友录更新时间); }
                主窗体.刷新讯友录(Constants.讯友录显示范围_白域, null, false);
            }
            说(界面文字.获取(252, "域名[#%]从白域列表移除了。", new Object[] {英语域名}));
        }
    }

    private void 加入小聊天群(SSPackageReader SS包解读器) throws Exception {
        if (当前用户.讯友目录 == null) {
            return;
        }
        String 群主讯宝地址 = (String) SS包解读器.读取_有标签("群主讯宝地址");
        byte 群编号 = (byte) SS包解读器.读取_有标签("群编号");
        String 群备注 = (String) SS包解读器.读取_有标签("群备注");
        Group_Small 加入的群[] = 当前用户.加入的小聊天群;
        if (加入的群 != null) {
            for (int i = 0; i < 加入的群.length; i++) {
                if (加入的群[i].编号 == 群编号 && 加入的群[i].群主.英语讯宝地址.equals(群主讯宝地址)) {
                    return;
                }
            }
        }
        Contact 讯友目录[] = 当前用户.讯友目录;
        int i;
        for (i = 0; i < 讯友目录.length; i++) {
            if (讯友目录[i].英语讯宝地址.equals(群主讯宝地址)) {
                break;
            }
        }
        if (i == 讯友目录.length) {
            return;
        }
        Group_Small 新群 = new Group_Small();
        新群.群主 = 讯友目录[i];
        新群.备注 = 群备注;
        新群.编号 = 群编号;
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
        if (主窗体 != null) {
            主窗体.刷新讯友录(Constants.讯友录显示范围_聊天群);
        }
        数据库_更新最近互动讯友排名(群主讯宝地址, 群编号);
    }

    private void 加入大聊天群(SSPackageReader SS包解读器) throws Exception {
        String 主机名 = (String) SS包解读器.读取_有标签("主机名");
        String 英语域名 = (String) SS包解读器.读取_有标签("英语域名");
        String 本国语域名 = (String) SS包解读器.读取_有标签("本国语域名");
        long 群编号 = (long) SS包解读器.读取_有标签("群编号");
        String 群名称 = (String) SS包解读器.读取_有标签("群名称");
        String 子域名 = 主机名 + "." + 英语域名;
        Group_Large 加入的大聊天群[] = 当前用户.加入的大聊天群;
        if (加入的大聊天群 != null) {
            for (int i = 0; i < 加入的大聊天群.length; i++) {
                if (加入的大聊天群[i].编号 == 群编号 && 加入的大聊天群[i].子域名.equals(子域名)) {
                    return;
                }
            }
        }
        Group_Large 新群 = new Group_Large();
        新群.主机名 = 主机名;
        新群.英语域名 = 英语域名;
        新群.本国语域名 = 本国语域名;
        新群.子域名 = 子域名;
        新群.编号 = 群编号;
        新群.名称 = 群名称;
        if (加入的大聊天群 != null) {
            Group_Large 加入的大聊天群2[] = new Group_Large[加入的大聊天群.length + 1];
            System.arraycopy(加入的大聊天群, 0, 加入的大聊天群2, 0, 加入的大聊天群.length);
            加入的大聊天群2[加入的大聊天群2.length - 1] = 新群;
            当前用户.加入的大聊天群 = 加入的大聊天群2;
        } else {
            加入的大聊天群 = new Group_Large[1];
            加入的大聊天群[0] = 新群;
            当前用户.加入的大聊天群 = 加入的大聊天群;
        }
        if (主窗体 != null) {
            主窗体.刷新讯友录(Constants.讯友录显示范围_聊天群);
        }
        数据库_更新最近互动讯友排名(子域名, 群编号);
    }

    private void 退出大聊天群(SSPackageReader SS包解读器) throws Exception {
        if (当前用户.加入的大聊天群 == null) {
            return;
        }
        String 英语域名 = (String) SS包解读器.读取_有标签("英语域名");
        String 主机名 = (String) SS包解读器.读取_有标签("主机名");
        long 群编号 = (long) SS包解读器.读取_有标签("群编号");
        String 子域名 = 主机名 + "." + 英语域名;
        Group_Large 加入的大聊天群[] = 当前用户.加入的大聊天群;
        int i;
        for (i = 0; i < 加入的大聊天群.length; i++) {
            if (子域名.equals(加入的大聊天群[i].子域名) && 群编号 == 加入的大聊天群[i].编号) {
                break;
            }
        }
        if (i < 加入的大聊天群.length) {
            if (加入的大聊天群.length > 1) {
                Group_Large 加入的群2[] = new Group_Large[加入的大聊天群.length - 1];
                if (i > 0) {
                    System.arraycopy(加入的大聊天群, 0, 加入的群2, 0, i);
                }
                if (i < 加入的大聊天群.length - 1) {
                    System.arraycopy(加入的大聊天群, i + 1, 加入的群2, i, 加入的大聊天群.length - i - 1);
                }
                加入的大聊天群 = 加入的群2;
            } else {
                加入的大聊天群 = null;
            }
            当前用户.加入的大聊天群 = 加入的大聊天群;
            if (主窗体 != null) {
                主窗体.关闭聊天控件(子域名, 群编号);
                主窗体.刷新讯友录(Constants.讯友录显示范围_聊天群);
            }
            清除大聊天群数据(子域名, 群编号);
        }
    }

    private void 重命名标签成功(SSPackageReader SS包解读器) throws Exception {
        String 原标签名称 = (String)SS包解读器.读取_有标签("旧名称");
        String 新标签名称 = (String)SS包解读器.读取_有标签("新名称");
        long 讯友录更新时间 = (long) SS包解读器.读取_有标签("时间");
        boolean 是合并 = false;
        Contact 讯友目录[] = 当前用户.讯友目录;
        for (int i = 0; i < 讯友目录.length; i++) {
            if (新标签名称.equalsIgnoreCase(讯友目录[i].标签一)) {
                是合并 = true;
                break;
            } else if (新标签名称.equalsIgnoreCase(讯友目录[i].标签二)) {
                是合并 = true;
                break;
            }
        }
        for (int i = 0; i < 讯友目录.length; i++) {
            if (原标签名称.equalsIgnoreCase(讯友目录[i].标签一)) {
                讯友目录[i].标签一 = 新标签名称;
            } else if (原标签名称.equalsIgnoreCase(讯友目录[i].标签二)) {
                讯友目录[i].标签二 = 新标签名称;
            }
            if (!SharedMethod.字符串未赋值或为空(讯友目录[i].标签一)) {
                if (讯友目录[i].标签一.equalsIgnoreCase(讯友目录[i].标签二)) {
                    讯友目录[i].标签二 = null;
                }
            }
        }
        if (讯友录更新时间 > 0) { 当前用户.讯友录有变动(讯友录更新时间); }
        if (当前用户.讯友录当前显示范围 == Constants.讯友录显示范围_某标签) {
            if (主窗体 != null) {
                主窗体.刷新讯友录();
            }
        }
        if (!是合并) {
            说(界面文字.获取(149, "标签 #% 已重命名为 #%。", new Object[] {原标签名称, 新标签名称}));
        } else {
            说(界面文字.获取(150, "标签 #% 里的讯友已并入标签 #%。", new Object[] {原标签名称, 新标签名称}));
        }
    }

    private void 删除讯友成功(SSPackageReader SS包解读器) throws Exception {
        Contact 讯友目录[] = 当前用户.讯友目录;
        if (讯友目录 == null) { return; }
        String 英语讯宝地址 = (String)SS包解读器.读取_有标签("英语讯宝地址");;
        long 讯友录更新时间 = (long) SS包解读器.读取_有标签("时间");
        int i;
        for (i = 0; i < 讯友目录.length; i++) {
            if (讯友目录[i].英语讯宝地址.equals(英语讯宝地址)) {
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
            if (讯友目录.length > 1) {
                Contact 讯友目录2[] = new Contact[讯友目录.length - 1];
                if (i > 0) {
                    System.arraycopy(讯友目录, 0, 讯友目录2, 0, i);
                }
                if (i < 讯友目录.length - 1) {
                    System.arraycopy(讯友目录, i + 1, 讯友目录2, i, 讯友目录.length - i - 1);
                }
                讯友目录 = 讯友目录2;
                for (i = 0; i < 讯友目录.length; i++) {
                    讯友目录[i].临时编号 = i + 1;
                }
                当前用户.讯友目录 = 讯友目录;
            } else {
                当前用户.讯友目录 = null;
            }
            if (讯友录更新时间 > 0) { 当前用户.讯友录有变动(讯友录更新时间); }
            if (主窗体 != null) {
                主窗体.关闭聊天控件(英语讯宝地址, (long) 0);
                主窗体.刷新讯友录();
            }
            清除讯友数据(英语讯宝地址);
            说(界面文字.获取(115, "讯友 #% 删除成功。[<a>#%</a>]", new Object[] {地址, TaskName.任务名称_删除讯友}));
        }
    }

    private void 取消拉黑讯友成功(SSPackageReader SS包解读器) throws Exception {
        String 英语讯宝地址 = (String) SS包解读器.读取_有标签("英语讯宝地址");
        long 讯友录更新时间 = (long) SS包解读器.读取_有标签("时间");
        Contact 讯友 = 当前用户.查找讯友(英语讯宝地址);
        if (讯友 != null) {
            讯友.拉黑 = false;
            if (讯友录更新时间 > 0) { 当前用户.讯友录有变动(讯友录更新时间); }
            if (主窗体 != null) {
                switch (当前用户.讯友录当前显示范围) {
                    case Constants.讯友录显示范围_最近:
                    case Constants.讯友录显示范围_讯友:
                    case Constants.讯友录显示范围_某标签:
                    case Constants.讯友录显示范围_黑名单:
                        主窗体.刷新讯友录();
                }
            }
            String 地址;
            if (!SharedMethod.字符串未赋值或为空(讯友.本国语讯宝地址)) {
                地址 = 讯友.本国语讯宝地址 + " / " + 讯友.英语讯宝地址;
            } else {
                地址 = 讯友.英语讯宝地址;
            }
            说(界面文字.获取(127, "已将 #% 移出黑名单。", new Object[] {地址}));
        }
    }

    void 关闭与传送服务器的连接() {
        关闭网络连接器(8);
        线程_传送服务器 = null;

    }

    void 关闭网络连接器(int 代码) {
        if (定时任务 != null) {
            定时任务.cancel();
            定时任务 = null;
        }
        if (当前用户.网络连接器 != null) {
            Socket 网络连接器 = 当前用户.网络连接器;
            当前用户.网络连接器 = null;
            try {
//                当前用户.记录运行步骤("        关闭网络连接器" + 代码);
                网络连接器.close();
            } catch (Exception e) {}
        }
    }


    void 发送讯宝(boolean 继续) {
        if (!继续) {
            if (当前用户.网络连接器 == null) {
//                当前用户.记录运行步骤("连接传送服务器4");
                启动访问线程_传送服务器();
                return;
            }
            if (定时任务 != null) {
//                当前用户.记录运行步骤("运行到：e3");
                return;
            }
            防止休眠_开始();
        }
        try {
            if (继续 && 正在发送的讯宝 != null) {
                if (正在发送的讯宝.指令 != ProtocolParameters.讯宝指令_无) {
                    if (正在发送的讯宝.指令 < ProtocolParameters.讯宝指令_视频通话请求) {
                        File 文件2 = new File(正在发送的讯宝.文件路径);
                        if (文件2.exists()) {
                            文件2.delete();
                            if (正在发送的讯宝.群编号 == 0) {
                                数据库_保存收到的一对一讯宝(正在发送的讯宝.讯宝地址, true, 正在发送的讯宝.发送序号, 正在发送的讯宝.存储时间, 正在发送的讯宝.指令, 正在发送的讯宝.文本, 正在发送的讯宝.宽度, 正在发送的讯宝.高度, 正在发送的讯宝.秒数);
                            } else {
                                数据库_保存收到的小聊天群讯宝(正在发送的讯宝.讯宝地址, 正在发送的讯宝.群编号, 当前用户.获取英语讯宝地址(), 正在发送的讯宝.发送序号, 正在发送的讯宝.存储时间, 正在发送的讯宝.指令, 正在发送的讯宝.文本, 正在发送的讯宝.宽度, 正在发送的讯宝.高度, 正在发送的讯宝.秒数);
                            }
                        } else {
                            if (正在发送的讯宝.群编号 == 0) {
                                数据库_保存要发送的一对一讯宝( 正在发送的讯宝.讯宝地址, SharedMethod.获取当前UTC时间(), ProtocolParameters.讯宝指令_撤回, String.valueOf(正在发送的讯宝.发送序号), (short)0, (short)0, (byte)0);
                            } else {
                                数据库_保存要发送的小聊天群讯宝(正在发送的讯宝.讯宝地址, 正在发送的讯宝.群编号, SharedMethod.获取当前UTC时间(), ProtocolParameters.讯宝指令_撤回, String.valueOf(正在发送的讯宝.发送序号), (short) 0, (short) 0, (byte) 0);
                            }
                        }
                    } else {
                        File 文件2 = new File(正在发送的讯宝.文件路径);
                        if (文件2.exists()) {
                            文件2.delete();
                        }
                    }
                }
            }
            SS_Sending 要发送的讯宝 = null;
            File 文件 = new File(运行环境.getFilesDir().toString() + "/" + 当前用户.获取英语讯宝地址());
            if (文件.exists() && 文件.isDirectory()) {
                FileFilter 文件过滤器 = new FileFilter() {
                    @Override
                    public boolean accept(File pathname) {
                        String 文件名 = pathname.getName();
                        if (文件名.endsWith(Constants.文件名结束字符串)) {
                            if (文件名.startsWith(Constants.文件名起始字符串)) {
                                return true;
                            } else {
                                return false;
                            }
                        } else {
                            return false;
                        }
                    }
                };
                File[] 文件数组 = 文件.listFiles(文件过滤器);
                List 文件列表 = Arrays.asList(文件数组);
                Collections.sort(文件列表, new Comparator<File>() {
                    @Override
                    public int compare(File f1, File f2) {
                        return f1.getName().compareTo(f2.getName());
                    }
                });
//                当前用户.记录运行步骤("有" + 文件目录.length + "个待发送的讯宝");
                if (文件数组.length > 0) {
                    for (int i = 0; i < 文件数组.length; i++) {
                        try {
                            byte 字节数组[] = SharedMethod.读取文件的全部字节(文件数组[i].getAbsolutePath());
                            if (字节数组 != null) {
                                SSPackageReader SS包解读器 = new SSPackageReader(字节数组);
                                要发送的讯宝 = new SS_Sending();
                                要发送的讯宝.文件路径 = 文件数组[i].getAbsolutePath();
                                要发送的讯宝.存储时间 = Long.parseLong(文件数组[i].getName().replace(Constants.文件名起始字符串,"").replace(Constants.文件名结束字符串, ""));
                                要发送的讯宝.讯宝地址 = (String) SS包解读器.读取_有标签("地址");
                                要发送的讯宝.群编号 = (byte)SS包解读器.读取_有标签("群编号");
                                要发送的讯宝.指令 = (byte)SS包解读器.读取_有标签("指令");
                                Object 值 = SS包解读器.读取_有标签("文本");
                                if (值 != null) { 要发送的讯宝.文本 = (String) 值; }
                                值 = SS包解读器.读取_有标签("宽度");
                                if (值 != null) { 要发送的讯宝.宽度 = (short) 值; }
                                值 = SS包解读器.读取_有标签("高度");
                                if (值 != null) { 要发送的讯宝.高度 = (short) 值; }
                                值 = SS包解读器.读取_有标签("秒数");
                                if (值 != null) { 要发送的讯宝.秒数 = (byte) 值; }
                                break;
                            } else {
                                File 文件2 = new File(文件数组[i].toString());
                                文件2.delete();
                            }
                        } catch (Exception e) {
                            File 文件2 = new File(文件数组[i].getAbsolutePath());
                            文件2.delete();
                        }
                    }
                }
            }
            if (要发送的讯宝 != null) {
//                if (要发送的讯宝.指令 == ProtocolParameters.讯宝指令_发送文字) {
//                    当前用户.记录运行步骤("运行到：7");
//                }
                正在发送的讯宝 = 要发送的讯宝;
            } else {
//                当前用户.记录运行步骤("运行到：e4");
                正在发送的讯宝 = null;
                线程_发送SS = null;
                防止休眠_结束();
                return;
            }
            if (当前用户.讯宝发送序号 < Long.MAX_VALUE) {
                当前用户.讯宝发送序号 += 1;
            }
            正在发送的讯宝.发送序号 = 当前用户.讯宝发送序号;
            switch (正在发送的讯宝.指令) {
                case ProtocolParameters.讯宝指令_发送语音:
                case ProtocolParameters.讯宝指令_发送图片:
                case ProtocolParameters.讯宝指令_发送短视频:
                case ProtocolParameters.讯宝指令_发送文件:
                case ProtocolParameters.讯宝指令_修改图标:
                    if (!SharedMethod.字符串未赋值或为空(正在发送的讯宝.文本)) {
                        正在发送的讯宝.文件字节数组 = SharedMethod.读取文件的全部字节(正在发送的讯宝.文本);
                        if (正在发送的讯宝.指令 == ProtocolParameters.讯宝指令_发送短视频) {
                            正在发送的讯宝.视频预览图片数据 = SharedMethod.读取文件的全部字节(正在发送的讯宝.文本 + ".jpg");
                        }
                    }
                    break;
            }
        } catch (Exception e) {
//            当前用户.记录运行步骤("运行到：e5");
            防止休眠_结束();
            正在发送的讯宝 = null;
            线程_发送SS = null;
            说(e.getMessage());
            return;
        }
        if (线程_发送SS == null) {
            线程_发送SS = new Thread_SendSS(this);
            线程_发送SS.start();
        }
    }

    void 发送() {
        try {
            SSPackageCreator SS包生成器 = new SSPackageCreator();
            SS包生成器.添加_有标签("序号", 正在发送的讯宝.发送序号);
            SS包生成器.添加_有标签("指令", 正在发送的讯宝.指令);
            if (正在发送的讯宝.指令 != ProtocolParameters.讯宝指令_无) {
//                当前用户.记录运行步骤("发送SS包" + 正在发送的讯宝.发送序号);
                SS包生成器.添加_有标签("地址", 正在发送的讯宝.讯宝地址);
                if (正在发送的讯宝.群编号 > 0) {
                    SS包生成器.添加_有标签("群编号", 正在发送的讯宝.群编号);
                }
                switch (正在发送的讯宝.指令) {
                    case ProtocolParameters.讯宝指令_确认收到:
                    case ProtocolParameters.讯宝指令_发送文字:
                    case ProtocolParameters.讯宝指令_撤回:
                    case ProtocolParameters.讯宝指令_邀请加入小聊天群:
                    case ProtocolParameters.讯宝指令_删减聊天群成员:
                    case ProtocolParameters.讯宝指令_修改聊天群名称:
                    case ProtocolParameters.讯宝指令_创建小聊天群:
                    case ProtocolParameters.讯宝指令_添加黑域:
                    case ProtocolParameters.讯宝指令_添加白域:
                    case ProtocolParameters.讯宝指令_移除黑域:
                    case ProtocolParameters.讯宝指令_移除白域:
                    case ProtocolParameters.讯宝指令_邀请加入大聊天群:
                    case ProtocolParameters.讯宝指令_退出大聊天群:
                    case ProtocolParameters.讯宝指令_删除讯友:
                    case ProtocolParameters.讯宝指令_给讯友添加标签:
                    case ProtocolParameters.讯宝指令_移除讯友标签:
                    case ProtocolParameters.讯宝指令_修改讯友备注:
                    case ProtocolParameters.讯宝指令_拉黑取消拉黑讯友:
                    case ProtocolParameters.讯宝指令_重命名讯友标签:
                        if (SharedMethod.字符串未赋值或为空(正在发送的讯宝.文本)) {
                            发送完毕(true);
                            return;
                        }
                        SS包生成器.添加_有标签("文本", 正在发送的讯宝.文本);
                        break;
                    case ProtocolParameters.讯宝指令_发送语音:
                        if (SharedMethod.字符串未赋值或为空(正在发送的讯宝.文本) || 正在发送的讯宝.文件字节数组 == null) {
                            发送完毕(true);
                            return;
                        }
                        SS包生成器.添加_有标签("文本", SharedMethod.获取扩展名(正在发送的讯宝.文本));
                        SS包生成器.添加_有标签("秒数", 正在发送的讯宝.秒数);
                        SS包生成器.添加_有标签("文件", 正在发送的讯宝.文件字节数组);
                        break;
                    case ProtocolParameters.讯宝指令_发送图片:
                    case ProtocolParameters.讯宝指令_发送短视频:
                        if (正在发送的讯宝.宽度 < 1 || 正在发送的讯宝.宽度 > ProtocolParameters.最大值_讯宝图片宽高_像素 ||
                                正在发送的讯宝.高度 < 1 || 正在发送的讯宝.高度 > ProtocolParameters.最大值_讯宝图片宽高_像素 ||
                                SharedMethod.字符串未赋值或为空(正在发送的讯宝.文本) || 正在发送的讯宝.文件字节数组 == null) {
                            发送完毕(true);
                            return;
                        }
                        SS包生成器.添加_有标签("文本", SharedMethod.获取扩展名(正在发送的讯宝.文本));
                        if (正在发送的讯宝.指令 == ProtocolParameters.讯宝指令_发送短视频) {
                            if (正在发送的讯宝.视频预览图片数据 == null) {
                                发送完毕(true);
                                return;
                            }
                            SS包生成器.添加_有标签("预览", 正在发送的讯宝.视频预览图片数据);
                        }
                        SS包生成器.添加_有标签("宽度", 正在发送的讯宝.宽度);
                        SS包生成器.添加_有标签("高度", 正在发送的讯宝.高度);
                        SS包生成器.添加_有标签("文件", 正在发送的讯宝.文件字节数组);
                        break;
                    case ProtocolParameters.讯宝指令_发送文件:
                        if (SharedMethod.字符串未赋值或为空(正在发送的讯宝.文本) || 正在发送的讯宝.文件字节数组 == null) {
                            发送完毕(true);
                            return;
                        }
                        SS包生成器.添加_有标签("文本", SharedMethod.获取文件名(正在发送的讯宝.文本));
                        SS包生成器.添加_有标签("文件", 正在发送的讯宝.文件字节数组);
                        break;
                    case ProtocolParameters.讯宝指令_获取小聊天群成员列表:
                        if (!SharedMethod.字符串未赋值或为空(正在发送的讯宝.文本)) {
                            SS包生成器.添加_有标签("文本", 正在发送的讯宝.文本);
                        }
                        break;
                    case ProtocolParameters.讯宝指令_修改图标:
                        if (正在发送的讯宝.文件字节数组 == null) {
                            发送完毕(true);
                            return;
                        }
                        SS包生成器.添加_有标签("文件", 正在发送的讯宝.文件字节数组);
                        break;
                }
            }
            if (SS包生成器.发送SS包(当前用户.网络连接器, 当前用户.AES加密器)) {
//                if (正在发送的讯宝.指令 < ProtocolParameters.讯宝指令_视频通话请求) {
//                    当前用户.记录运行步骤(正在发送的讯宝.指令 + "_" + 正在发送的讯宝.文本 + " (" + 正在发送的讯宝.发送序号 + ")");
//                }
                等待确认();
            } else {
//                当前用户.记录运行步骤("运行到：e6");
                关闭网络连接器(9);
                发送完毕(false);
            }
        } catch (Exception e) {
//            当前用户.记录运行步骤(e.getMessage());
            关闭网络连接器(10);
            发送完毕(false);
        }
    }

    private void 发送完毕(boolean 继续) {
        Bundle 数据盒子 = new Bundle();
        数据盒子.putBoolean("继续", 继续);
        Message 消息 = new Message();
        消息.what = 6;
        消息.setData(数据盒子);
        跨线程调用器.sendMessage(消息);
    }

    private void 等待确认() {
        Message 消息 = new Message();
        消息.what = 9;
        跨线程调用器.sendMessage(消息);
    }

    void 心跳() {
        if (心跳确认时间 > 0) {
            long 时间间隔 = System.currentTimeMillis() - 心跳确认时间;
            if (线程_发送SS == null) {
                final long 等待时间;
                if (Service_SSignal.屏幕亮了 > 0) {
                    if (System.currentTimeMillis() - Service_SSignal.屏幕亮了 < 1000) {
                        等待时间 = 300000;
                    } else {
                        等待时间 = 120000;
                    }
                } else {
                    等待时间 = 300000;
                }
                if (时间间隔 > 等待时间) {
//                    当前用户.记录运行步骤("心跳间隔时间过久（" + 时间间隔 + ">" + 等待时间 + "），重新连接传送服务器");
                    if (定时任务 == null) {
                        关闭网络连接器(11);
                    }
                    return;
                }
            }
        }
        if (正在发送的讯宝 == null) {
//            当前用户.记录运行步骤("发送心跳包");
            正在发送的讯宝 = new SS_Sending();
            正在发送的讯宝.指令 = ProtocolParameters.SS包数据类型_无;
        } else if (定时任务 != null) {
            return;
        }
        if (当前用户.讯宝发送序号 < Long.MAX_VALUE) {
            当前用户.讯宝发送序号 += 1;
        }
        正在发送的讯宝.发送序号 = 当前用户.讯宝发送序号;
        if (线程_发送SS == null) {
            线程_发送SS = new Thread_SendSS(this);
            线程_发送SS.start();
        }
    }

    void 检查大聊天群是否有新消息() {
        Group_Large 加入的大聊天群[] = 当前用户.加入的大聊天群;
        LargeChatGroupServer 大聊天群服务器[] = new LargeChatGroupServer[加入的大聊天群.length];
        int i, j, 大聊天群服务器数量 = 0;
        String 子域名;
        Group_Large 某一大聊天群;
        for (i = 0; i < 加入的大聊天群.length; i++) {
            某一大聊天群 = 加入的大聊天群[i];
            if (某一大聊天群.最新讯宝的发送时间 == 0) {
                某一大聊天群.最新讯宝的发送时间 = 数据库_获取最新讯宝的发送时间(某一大聊天群.子域名, 某一大聊天群.编号);
                if (某一大聊天群.最新讯宝的发送时间 == 0) {
                    某一大聊天群.最新讯宝的发送时间 = 1;
                }
            }
            if (某一大聊天群.检查时间 == 0 || (System.currentTimeMillis() - 某一大聊天群.检查时间) / 60000 >= 10) {
                子域名 = 某一大聊天群.子域名;
                if (大聊天群服务器数量 > 0) {
                    for (j = 0; j < 大聊天群服务器数量; j++) {
                        if (子域名.equals(大聊天群服务器[j].子域名)) {
                            break;
                        }
                    }
                    if (j == 大聊天群服务器数量) {
                        大聊天群服务器[大聊天群服务器数量] = new LargeChatGroupServer(子域名);
                        大聊天群服务器数量 += 1;
                    }
                } else {
                    大聊天群服务器[0] = new LargeChatGroupServer(子域名);
                    大聊天群服务器数量 = 1;
                }
            }
        }
        if (大聊天群服务器数量 > 0) {
            for (i = 0; i < 大聊天群服务器数量; i++) {
                子域名 = 大聊天群服务器[i].子域名;
                for (j = 0; j < 加入的大聊天群.length; j++) {
                    if (子域名.equals(加入的大聊天群[j].子域名)) {
                        某一大聊天群 = 加入的大聊天群[j];
                        if (!SharedMethod.字符串未赋值或为空(某一大聊天群.连接凭据)) {
                            SSPackageCreator SS包生成器 = new SSPackageCreator();
                            try {
                                ProtocolFormats.添加数据_检查大聊天群新讯宝数量(SS包生成器, 子域名, 加入的大聊天群);
                                启动HTTPS访问线程2(new httpSetting(ProtocolPath.获取大聊天群服务器访问路径开头(子域名, false) + "C=CheckNewSS&EnglishSSAddress=" + 替换URI敏感字符(当前用户.获取英语讯宝地址()) + "&Credential=" + 替换URI敏感字符(某一大聊天群.连接凭据), 0, SS包生成器.生成SS包(), 大聊天群服务器[i]));
                            } catch (Exception e) {
                                return;
                            }
                        } else {
                            当前用户.读取大聊天群凭据(某一大聊天群);
                            if (!SharedMethod.字符串未赋值或为空(某一大聊天群.连接凭据)) {
                                SSPackageCreator SS包生成器 = new SSPackageCreator();
                                try {
                                    ProtocolFormats.添加数据_检查大聊天群新讯宝数量(SS包生成器, 子域名, 加入的大聊天群);
                                    启动HTTPS访问线程2(new httpSetting(ProtocolPath.获取大聊天群服务器访问路径开头(子域名, false) + "C=CheckNewSS&EnglishSSAddress=" + 替换URI敏感字符(当前用户.获取英语讯宝地址()) + "&Credential=" + 替换URI敏感字符(某一大聊天群.连接凭据), 0, SS包生成器.生成SS包(), 大聊天群服务器[i]));
                                } catch (Exception e) {
                                    return;
                                }
                                return;
                            }
                            大聊天群服务器[i].无连接凭据 = true;
                            SSPackageCreator SS包生成器 = new SSPackageCreator();
                            try {
                                SS包生成器.添加_有标签("发送序号", 当前用户.讯宝发送序号);
                                SS包生成器.添加_有标签("子域名", 子域名);
                                SS包生成器.添加_有标签("群编号", 某一大聊天群.编号);
                                启动HTTPS访问线程2(new httpSetting(ProtocolPath.获取传送服务器访问路径开头(当前用户.主机名, 当前用户.域名_英语, false) + "C=JoinLargeGroup&UserID=" + 当前用户.编号 + "&Position=" + 当前用户.位置号 + "&DeviceType=" + ProtocolParameters.设备类型_文本_手机, 20000, SS包生成器.生成SS包(当前用户.AES加密器), 大聊天群服务器[i]));
                            } catch (Exception e) {
                                return;
                            }
                        }
                    }
                }
            }
        }
    }

    private long 数据库_获取最新讯宝的发送时间(String 服务器子域名, long 群编号) {
        long 发送时间 = 0;
        try {
            String 路径 = 运行环境.getFilesDir().toString() + "/" + 当前用户.获取英语讯宝地址();
            File 文件 = new File(路径 + "/" + 服务器子域名 + "#" + 群编号 + ".sscj");
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
                            发送时间 = (long)SS包读取器.读取_有标签("发送时间");
                            break;
                        }
                    }
                } while (SS包读取器 != null);
                文件随机访问器.close();
            }
        } catch (Exception e) {
        }
        return 发送时间;
    }

    private void 启动HTTPS访问线程2(httpSetting 访问设置) {
        访问设置.大聊天群服务器.线程 = new Thread_CheckLargeChatServer(this, 访问设置);
        访问设置.大聊天群服务器.线程.start();
    }

    public void HTTPS访问2(httpSetting 访问设置) {
        HttpsURLConnection HTTPS连接 = null;
        int 重试次数 = 0;
        byte 收到的字节数组[];
        int 收到的字节数 = 0, 收到的总字节数;
        do {
            收到的总字节数 = 0;
            收到的字节数组 = null;
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
                            HTTPS请求成功(访问设置.大聊天群服务器, 收到的字节数组);
                        }
                    }
                    return;
                }
            } catch (Exception e) {
            } finally {
                if (HTTPS连接 != null) {HTTPS连接.disconnect();}
            }
            重试次数 += 1;
        } while (重试次数 <= 2);
    }

    private void HTTPS请求成功(LargeChatGroupServer 大聊天群服务器, byte SS包[]) {
        SSPackageReader SS包解读器;
        try {
            SS包解读器 = new SSPackageReader(SS包);
            if (SS包解读器.获取查询结果() == ProtocolParameters.查询结果_成功) {
                if (!大聊天群服务器.无连接凭据) {
                    Object SS包解读器2[] = SS包解读器.读取_重复标签("GP");
                    if (SS包解读器2 != null) {
                        long 群编号;
                        int 新讯宝数量 = 0;
                        SSPackageReader SS包解读器3;
                        for (int i = 0; i < SS包解读器2.length; i++) {
                            SS包解读器3 = (SSPackageReader) SS包解读器2[i];
                            群编号 = (long)SS包解读器3.读取_有标签("GI");
                            新讯宝数量 = (int)SS包解读器3.读取_有标签("SN");
                            if (新讯宝数量 > 0) {
                                Bundle 数据盒子 = new Bundle();
                                数据盒子.putString("子域名", 大聊天群服务器.子域名);
                                数据盒子.putLong("群编号", 群编号);
                                数据盒子.putInt("新讯宝数量", 新讯宝数量);
                                Message 消息 = new Message();
                                消息.what = 7;
                                消息.setData(数据盒子);
                                跨线程调用器.sendMessage(消息);
                            }
                        }
                        if (新讯宝数量 > 0) {
                            提示有新消息(界面文字.获取(186, "#% 条新消息", new Object[] {新讯宝数量}));
                        }
                    }
                    Group_Large 加入的大聊天群[] = 当前用户.加入的大聊天群;
                    long 检查时间 = System.currentTimeMillis();
                    for (int i = 0; i < 加入的大聊天群.length; i++) {
                        if (加入的大聊天群[i].子域名.equals(大聊天群服务器.子域名)) {
                            加入的大聊天群[i].检查时间 = 检查时间;
                        }
                    }
                } else {
                    String 子域名 = (String)SS包解读器.读取_有标签("子域名");
                    String 连接凭据 = (String)SS包解读器.读取_有标签("连接凭据");
                    if (!大聊天群服务器.子域名.equals(子域名)) {
                        return;
                    }
                    Group_Large 加入的大聊天群[] = 当前用户.加入的大聊天群;
                    for (int i = 0; i < 加入的大聊天群.length; i++) {
                        if (加入的大聊天群[i].子域名.equals(子域名)) {
                            if (!SharedMethod.字符串未赋值或为空(加入的大聊天群[i].连接凭据)) {
                                加入的大聊天群[i].连接凭据 = 连接凭据;
                            }
                        }
                    }
                    大聊天群服务器.无连接凭据 = false;
                    SSPackageCreator SS包生成器 = new SSPackageCreator();
                    ProtocolFormats.添加数据_检查大聊天群新讯宝数量(SS包生成器, 子域名, 加入的大聊天群);
                    启动HTTPS访问线程2(new httpSetting(ProtocolPath.获取大聊天群服务器访问路径开头(大聊天群服务器.子域名, false) + "C=CheckNewSS&EnglishSSAddress=" + 替换URI敏感字符(当前用户.获取英语讯宝地址()) + "&Credential=" + 替换URI敏感字符(连接凭据), 0, SS包生成器.生成SS包(), 大聊天群服务器));
                }
            }
        } catch (Exception e) {
        }
    }



    void 发布流星语(byte SS包[]) {
        任务 = new Task(TaskName.任务名称_发流星语,this);
        说(界面文字.获取(159, "正在发流星语。请稍等。"));
        String 域名;
        if (!BuildConfig.DEBUG || ProtocolPath.调试时访问真实网站) {
            域名 = 当前用户.子域名_小宇宙写入;
        } else {
            域名 = ProtocolPath.IP地址_调试 + ":" + ProtocolPath.测试域名1小宇宙中心服务器本机IIS调试端口_SSL;
        }
        启动HTTPS访问线程(new httpSetting("https://" + 域名 + "/?C=PostMeteorRain&EnglishSSAddress=" + 替换URI敏感字符(当前用户.获取英语讯宝地址()) + "&Credential=" + 替换URI敏感字符(当前用户.小宇宙写入凭据), SS包));
    }

    void 流星语发布结束(boolean 成功) {
        if (成功) {
            说(界面文字.获取(268, "流星语发布成功。"));
            聊天控件.小宇宙控件.发送JS("function(){ PublishSuccessful(); }");
        } else {
            聊天控件.小宇宙控件.发送JS("function(){ PublishFailed(); }");
        }
    }

    void 发布商品(byte SS包[]) {
        任务 = new Task(TaskName.任务名称_发布商品,this);
        说(界面文字.获取(101, "正在发布商品。请稍等。"));
        String 域名;
        if (!BuildConfig.DEBUG || ProtocolPath.调试时访问真实网站) {
            域名 = 当前用户.子域名_小宇宙写入;
        } else {
            域名 = ProtocolPath.IP地址_调试 + ":" + ProtocolPath.测试域名1小宇宙中心服务器本机IIS调试端口_SSL;
        }
        启动HTTPS访问线程(new httpSetting("https://" + 域名 + "/?C=PostGoods&EnglishSSAddress=" + 替换URI敏感字符(当前用户.获取英语讯宝地址()) + "&Credential=" + 替换URI敏感字符(当前用户.小宇宙写入凭据), SS包));
    }

    void 商品发布结束(boolean 成功) {
        if (成功) {
            说(界面文字.获取(57, "商品发布成功。"));
            聊天控件.小宇宙控件.发送JS("function(){ PublishSuccessful(); }");
        } else {
            聊天控件.小宇宙控件.发送JS("function(){ PublishFailed(); }");
        }
    }


    boolean 数据库_保存要发送的一对一讯宝(String 讯宝地址, long 时间, byte 讯宝指令, String 文本, short 宽度, short 高度, byte 秒数) {
        try {
            String 路径 = 运行环境.getFilesDir().toString() + "/" + 当前用户.获取英语讯宝地址();
            File 目录 = new File(路径);
            if (!目录.exists() || !目录.isDirectory()) {
                目录.mkdir();
            }
            SSPackageCreator SS包生成器 = new SSPackageCreator();
            SS包生成器.添加_有标签("地址", 讯宝地址);
            SS包生成器.添加_有标签("群编号", (byte)0);
            SS包生成器.添加_有标签("指令", 讯宝指令);
            if (!SharedMethod.字符串未赋值或为空(文本)) {
                SS包生成器.添加_有标签("文本", 文本);
            }
            if (宽度 > 0) {
                SS包生成器.添加_有标签("宽度", 宽度);
            }
            if (高度 > 0) {
                SS包生成器.添加_有标签("高度", 高度);
            }
            if (秒数 > 0) {
                SS包生成器.添加_有标签("秒数", 秒数);
                SS包生成器.添加_有标签("已收听", true);
            }
            SharedMethod.保存文件的全部字节(路径 + "/" + Constants.文件名起始字符串 + 时间 + Constants.文件名结束字符串, SS包生成器.生成SS包());
            return true;
        } catch (Exception e) {
            说(e.getMessage());
            return false;
        }
    }

    boolean 数据库_保存要发送的小聊天群讯宝(String 群主英语讯宝地址, byte 群编号, long 时间, byte 讯宝指令, String 文本, short 宽度, short 高度, byte 秒数) {
        try {
            String 路径 = 运行环境.getFilesDir().toString() + "/" + 当前用户.获取英语讯宝地址();
            File 目录 = new File(路径);
            if (!目录.exists() || !目录.isDirectory()) {
                目录.mkdir();
            }
            SSPackageCreator SS包生成器 = new SSPackageCreator();
            SS包生成器.添加_有标签("地址", 群主英语讯宝地址);
            SS包生成器.添加_有标签("群编号", 群编号);
            SS包生成器.添加_有标签("指令", 讯宝指令);
            if (!SharedMethod.字符串未赋值或为空(文本)) {
                SS包生成器.添加_有标签("文本", 文本);
            }
            if (宽度 > 0) {
                SS包生成器.添加_有标签("宽度", 宽度);
            }
            if (高度 > 0) {
                SS包生成器.添加_有标签("高度", 高度);
            }
            if (秒数 > 0) {
                SS包生成器.添加_有标签("秒数", 秒数);
                SS包生成器.添加_有标签("已收听", true);
            }
            SharedMethod.保存文件的全部字节(路径 + "/" + Constants.文件名起始字符串 + 时间 + Constants.文件名结束字符串, SS包生成器.生成SS包());
            return true;
        } catch (Exception e) {
            说(e.getMessage());
            return false;
        }
    }

    void 数据库_更新新讯宝数量(String 英语讯宝地址, long 群编号, short 数量) {
        try {
            String 路径 = 运行环境.getFilesDir().toString() + "/" + 当前用户.获取英语讯宝地址();
            File 文件 = new File(路径 + "/activity.sspk");
            if (!文件.exists() || 文件.isDirectory()) {
                return;
            }
            byte 字节数组[] = SharedMethod.读取文件的全部字节(文件.getAbsolutePath());
            if (字节数组 != null) {
                SSPackageReader SS包解读器 = new SSPackageReader(字节数组);
                Object SS包解读器2[] = SS包解读器.读取_重复标签("对象");
                if (SS包解读器2.length > 0) {
                    int i;
                    for (i = 0; i < SS包解读器2.length; i++) {
                        SSPackageReader SS包解读器3 = (SSPackageReader) SS包解读器2[i];
                        String 英语讯宝地址2 = (String) SS包解读器3.读取_有标签("地址");
                        if (英语讯宝地址.equals(英语讯宝地址2)) {
                            long 群编号2 = (long) SS包解读器3.读取_有标签("群编号");
                            if (群编号 == 群编号2) {
                                break;
                            }
                        }
                    }
                    if (i < SS包解读器2.length) {
                        SSPackageCreator SS包生成器2 = new SSPackageCreator();
                        for (int j = 0; j < SS包解读器2.length; j++) {
                            if (j != i) {
                                SS包生成器2.添加_有标签("对象", (SSPackageReader) SS包解读器2[j]);
                            } else {
                                SSPackageCreator SS包生成器 = new SSPackageCreator();
                                SS包生成器.添加_有标签("地址", 英语讯宝地址);
                                SS包生成器.添加_有标签("群编号", 群编号);
                                SS包生成器.添加_有标签("新讯宝数量", 数量);
                                SS包生成器2.添加_有标签("对象", SS包生成器);
                            }
                        }
                        RandomAccessFile 文件随机访问器 = new RandomAccessFile(文件.getAbsolutePath(), "rwd");
                        字节数组 = SS包生成器2.生成SS包();
                        文件随机访问器.write(字节数组);
                        if (文件随机访问器.length() != 字节数组.length) {
                            文件随机访问器.setLength(字节数组.length);
                        }
                        文件随机访问器.close();
                    }
                }
            }
        } catch (Exception e) {
            说(e.getMessage());
//            当前用户.记录运行步骤(e.getMessage());
        }
    }

    private short 数据库_获取新讯宝数量(String 英语讯宝地址, long 群编号) {
        try {
            String 路径 = 运行环境.getFilesDir().toString() + "/" + 当前用户.获取英语讯宝地址();
            File 文件 = new File(路径 + "/activity.sspk");
            if (!文件.exists() || 文件.isDirectory()) {
                return 0;
            }
            byte 字节数组[] = SharedMethod.读取文件的全部字节(文件.getAbsolutePath());
            if (字节数组 != null) {
                SSPackageReader SS包解读器 = new SSPackageReader(字节数组);
                Object SS包解读器2[] = SS包解读器.读取_重复标签("对象");
                if (SS包解读器2.length > 0) {
                    int i;
                    for (i = 0; i < SS包解读器2.length; i++) {
                        SSPackageReader SS包解读器3 = (SSPackageReader) SS包解读器2[i];
                        String 英语讯宝地址2 = (String) SS包解读器3.读取_有标签("地址");
                        if (英语讯宝地址.equals(英语讯宝地址2)) {
                            long 群编号2 = (long) SS包解读器3.读取_有标签("群编号");
                            if (群编号 == 群编号2) {
                                return (short) SS包解读器3.读取_有标签("新讯宝数量");
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            说(e.getMessage());
        }
        return 0;
    }

    void 数据库_更新陌生人新讯宝数量(short 数量) {
        try {
            String 路径 = 运行环境.getFilesDir().toString() + "/" + 当前用户.获取英语讯宝地址();
            File 文件 = new File(路径 + "/strangerss.txt");
            if (数量 > 0) {
                if (!文件.exists() || 文件.isDirectory()) {
                    if (!文件.createNewFile()) {
                        return;
                    }
                }
                byte 字节数组[] = String.valueOf(数量).getBytes("UTF8");
                RandomAccessFile 文件随机访问器 = new RandomAccessFile(文件.getAbsolutePath(), "rwd");
                文件随机访问器.write(字节数组);
                文件随机访问器.setLength(字节数组.length);
                文件随机访问器.close();
            } else if (文件.exists()) {
                文件.delete();
            }
        } catch (Exception e) {
            说(e.getMessage());
        }
    }

    short 数据库_获取陌生人新讯宝数量() {
        try {
            String 路径 = 运行环境.getFilesDir().toString() + "/" + 当前用户.获取英语讯宝地址();
            File 文件 = new File(路径 + "/strangerss.txt");
            if (!文件.exists() || 文件.isDirectory()) {
                return 0;
            }
            byte 字节数组[] = SharedMethod.读取文件的全部字节(文件.getAbsolutePath());
            if (字节数组 != null) {
                return Short.valueOf(new String(字节数组, "UTF8"));
            }
        } catch (Exception e) {
            说(e.getMessage());
        }
        return 0;
    }

    private void 数据库_删除发送失败的讯宝(String 发送者英语讯宝地址, byte 群编号, String 群主讯宝地址, byte 类型, long 发送序号) {
        String 路径 = 运行环境.getFilesDir().toString() + "/" + 当前用户.获取英语讯宝地址();
        File 目录 = new File(路径);
        if (!目录.exists() || !目录.isDirectory()) {
            return;
        }
        File 文件;
        boolean 一对一;
        if (群编号 == 0 || 类型 == ProtocolParameters.讯宝指令_被邀请加入小聊天群者未添加我为讯友) {
            一对一 = true;
            文件 = new File(路径 + "/" + 发送者英语讯宝地址 + ".sscj");
        } else {
            一对一 = false;
            文件 = new File(路径 + "/" + 群主讯宝地址 + "#" + 群编号 + ".sscj");
        }
        if (!文件.exists() || 文件.isDirectory()) {
            return;
        }
        try {
            RandomAccessFile 文件随机访问器 = new RandomAccessFile(文件.getAbsolutePath(), "rwd");
            if (文件随机访问器.length() > 0) {
                文件随机访问器.seek(文件随机访问器.length());
                long SS包位置 = 文件随机访问器.length();
                文件随机访问器.seek(SS包位置);
                SSPackageReader SS包读取器;
                do {
                    SS包读取器 = SharedMethod.读取一个SS包(文件随机访问器);
                    if (SS包读取器 == null) {
                        SS包读取器 = SharedMethod.读取一个SS包2(文件随机访问器);
                    }
                    if (SS包读取器 != null) {
                        if (发送序号 == (long)SS包读取器.读取_有标签("发送序号")) {
                            if (一对一) {
                                if ((boolean)SS包读取器.读取_有标签("是接收者")) {
                                    if (!(boolean)SS包读取器.读取_有标签("删除")) {
                                        SharedMethod.标为已删除(文件随机访问器, 文件随机访问器.getFilePointer(), SS包位置, SS包读取器);
                                    }
                                    break;
                                }
                            } else {
                                if (发送者英语讯宝地址.equals((String) SS包读取器.读取_有标签("发送者"))) {
                                    if (!(boolean)SS包读取器.读取_有标签("删除")) {
                                        SharedMethod.标为已删除(文件随机访问器, 文件随机访问器.getFilePointer(), SS包位置, SS包读取器);
                                    }
                                    break;
                                }
                            }
                        }
                        SS包位置 = 文件随机访问器.getFilePointer();
                    }
                } while (SS包读取器 != null);
            }
            文件随机访问器.close();
        } catch (Exception e) {
            说(e.getMessage());
            return;
        }
    }

    boolean 数据库_更新最近互动讯友排名(String 英语讯宝地址, long 群编号) {
        if (SharedMethod.字符串未赋值或为空(英语讯宝地址)) {
            return false;
        }
        try {
            String 路径 = 运行环境.getFilesDir().toString() + "/" + 当前用户.获取英语讯宝地址();
            File 目录 = new File(路径);
            if (!目录.exists() || !目录.isDirectory()) {
                目录.mkdir();
            }
            File 文件 = new File(路径 + "/activity.sspk");
            if (!文件.exists() || 文件.isDirectory()) {
                文件.createNewFile();
            }
            byte 字节数组[] = SharedMethod.读取文件的全部字节(文件.getAbsolutePath());
            if (字节数组 != null) {
                SSPackageReader SS包解读器 = new SSPackageReader(字节数组);
                Object SS包解读器2[] = SS包解读器.读取_重复标签("对象");
                if (SS包解读器2.length > 0) {
                    int i;
                    for (i = 0; i < SS包解读器2.length; i++) {
                        SSPackageReader SS包解读器3 = (SSPackageReader)SS包解读器2[i];
                        String 英语讯宝地址2 = (String)SS包解读器3.读取_有标签("地址");
                        if (英语讯宝地址.equals(英语讯宝地址2)) {
                            long 群编号2 = (long)SS包解读器3.读取_有标签("群编号");
                            if (群编号 == 群编号2) {
                                break;
                            }
                        }
                    }
                    SSPackageCreator SS包生成器2;
                    if (i < SS包解读器2.length) {
                        if (i > 0) {
                            SS包生成器2 = new SSPackageCreator();
                            SS包生成器2.添加_有标签("对象", (SSPackageReader)SS包解读器2[i]);
                            for (int j = 0; j < SS包解读器2.length; j++) {
                                if (j != i) {
                                    SS包生成器2.添加_有标签("对象", (SSPackageReader)SS包解读器2[j]);
                                }
                            }
                        } else {
                            return false;
                        }
                    } else {
                        SSPackageCreator SS包生成器 = new SSPackageCreator();
                        SS包生成器.添加_有标签("地址", 英语讯宝地址);
                        SS包生成器.添加_有标签("群编号", 群编号);
                        SS包生成器2 = new SSPackageCreator();
                        SS包生成器2.添加_有标签("对象", SS包生成器);
                        for (i = 0; i < SS包解读器2.length; i++) {
                            SS包生成器2.添加_有标签("对象", (SSPackageReader)SS包解读器2[i]);
                            if (i >= 49) {
                                break;
                            }
                        }
                    }
                    RandomAccessFile 文件随机访问器 = new RandomAccessFile(文件.getAbsolutePath(), "rwd");
                    字节数组 = SS包生成器2.生成SS包();
                    文件随机访问器.write(字节数组);
                    if (文件随机访问器.length() != 字节数组.length) {
                        文件随机访问器.setLength(字节数组.length);
                    }
                    文件随机访问器.close();
                    return true;
                }
            }
            SSPackageCreator SS包生成器 = new SSPackageCreator();
            SS包生成器.添加_有标签("地址", 英语讯宝地址);
            SS包生成器.添加_有标签("群编号", 群编号);
            SSPackageCreator SS包生成器2 = new SSPackageCreator();
            SS包生成器2.添加_有标签("对象", SS包生成器);
            RandomAccessFile 文件随机访问器 = new RandomAccessFile(文件.getAbsolutePath(), "rwd");
            字节数组 = SS包生成器2.生成SS包();
            文件随机访问器.write(字节数组);
            if (文件随机访问器.length() != 字节数组.length) {
                文件随机访问器.setLength(字节数组.length);
            }
            文件随机访问器.close();
            return true;
        } catch (Exception e) {
            说(e.getMessage());
            return false;
        }
    }

    private boolean 数据库_发送者撤回讯宝(String 发送者英语讯宝地址, byte 群编号, String 群主英语讯宝地址, long 发送序号, long 发送时间) {
        try {
            String 路径 = 运行环境.getFilesDir().toString() + "/" + 当前用户.获取英语讯宝地址();
            if (群编号 == 0) {
                File 文件 = new File(路径 + "/" + 发送者英语讯宝地址 + ".sscj");
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
                            long 发送时间2 = (long)SS包读取器.读取_有标签("发送时间");
                            if ((发送时间 - 发送时间2) / 1000 > ProtocolParameters.最大值_讯宝可撤回的时限_秒 + 30) {
                                return false;
                            }
                            if (!(boolean) SS包读取器.读取_有标签("是接收者")) {
                                long 发送序号2 = (long)SS包读取器.读取_有标签("发送序号");
                                if (发送序号2 == 发送序号) {
                                    if (!(boolean)SS包读取器.读取_有标签("删除")) {
                                        SharedMethod.标为已删除(文件随机访问器, 文件随机访问器.getFilePointer(), SS包位置, SS包读取器);
                                        SharedMethod.删除相关文件(SS包读取器, 运行环境, 当前用户);
                                    }
                                    return true;
                                }
                            }
                            SS包位置 = 文件随机访问器.getFilePointer();
                        }
                    } while (SS包读取器 != null);
                    文件随机访问器.close();
                }
            } else {
                File 文件 = new File(路径 + "/" + 群主英语讯宝地址 + "#" + 群编号 + ".sscj");
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
                            long 发送时间2 = (long)SS包读取器.读取_有标签("发送时间");
                            if ((发送时间 - 发送时间2) / 1000 > ProtocolParameters.最大值_讯宝可撤回的时限_秒 + 30) {
                                return false;
                            }
                            String 发送者 = (String) SS包读取器.读取_有标签("发送者");
                            if (发送者英语讯宝地址.equals(发送者)) {
                                long 发送序号2 = (long)SS包读取器.读取_有标签("发送序号");
                                if (发送序号2 == 发送序号) {
                                    if (!(boolean)SS包读取器.读取_有标签("删除")) {
                                        SharedMethod.标为已删除(文件随机访问器, 文件随机访问器.getFilePointer(), SS包位置, SS包读取器);
                                        SharedMethod.删除相关文件(SS包读取器, 运行环境, 当前用户);
                                    }
                                    return true;
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

    private void 清除讯友数据(String 英语讯宝地址) {
        try {
            File 文件 = new File(运行环境.getFilesDir().toString() + "/" + 当前用户.获取英语讯宝地址() + "/" + 英语讯宝地址 + ".sscj");
            if (文件.exists()) {
                文件.delete();
            }
            数据库_删除最近互动讯友(英语讯宝地址, (byte)0);
        } catch (Exception e) {
            说(e.getMessage());
        }
    }

    private void 清除小聊天群数据(String 群主英语讯宝地址, byte 群编号) {
        try {
            File 文件 = new File(运行环境.getFilesDir().toString() + "/" + 当前用户.获取英语讯宝地址() + "/" + 群主英语讯宝地址 + "#" + 群编号 + ".sscj");
            if (文件.exists()) {
                文件.delete();
            }
            数据库_删除最近互动讯友(群主英语讯宝地址, 群编号);
        } catch (Exception e) {
            说(e.getMessage());
        }
    }

    void 清除大聊天群数据(String 子域名, long 群编号) {
        try {
            File 文件 = new File(运行环境.getFilesDir().toString() + "/" + 当前用户.获取英语讯宝地址() + "/" + 子域名 + "#" + 群编号 + ".sscj");
            if (文件.exists()) {
                文件.delete();
            }
            数据库_删除最近互动讯友(子域名, 群编号);
        } catch (Exception e) {
            说(e.getMessage());
        }
    }

    private void 数据库_删除最近互动讯友(String 英语讯宝地址, long 群编号) {
        try {
            String 路径 = 运行环境.getFilesDir().toString() + "/" + 当前用户.获取英语讯宝地址();
            File 文件 = new File(路径 + "/activity.sspk");
            if (!文件.exists() || 文件.isDirectory()) {
                return;
            }
            byte 字节数组[] = SharedMethod.读取文件的全部字节(文件.getAbsolutePath());
            if (字节数组 != null) {
                SSPackageReader SS包解读器 = new SSPackageReader(字节数组);
                Object SS包解读器2[] = SS包解读器.读取_重复标签("对象");
                if (SS包解读器2.length > 0) {
                    int i;
                    for (i = 0; i < SS包解读器2.length; i++) {
                        SSPackageReader SS包解读器3 = (SSPackageReader) SS包解读器2[i];
                        String 英语讯宝地址2 = (String) SS包解读器3.读取_有标签("地址");
                        if (英语讯宝地址.equals(英语讯宝地址2)) {
                            long 群编号2 = (long) SS包解读器3.读取_有标签("群编号");
                            if (群编号 == 群编号2) {
                                break;
                            }
                        }
                    }
                    if (i < SS包解读器2.length) {
                        SSPackageCreator SS包生成器2 = new SSPackageCreator();
                        for (int j = 0; j < SS包解读器2.length; j++) {
                            if (j != i) {
                                SS包生成器2.添加_有标签("对象", (SSPackageReader) SS包解读器2[j]);
                            }
                        }
                        if (SS包生成器2.获取SS包数据数量() > 0) {
                            RandomAccessFile 文件随机访问器 = new RandomAccessFile(文件.getAbsolutePath(), "rwd");
                            字节数组 = SS包生成器2.生成SS包();
                            文件随机访问器.write(字节数组);
                            if (文件随机访问器.length() != 字节数组.length) {
                                文件随机访问器.setLength(字节数组.length);
                            }
                            文件随机访问器.close();
                        } else {
                            文件.delete();
                        }
                    }
                }
            }
        } catch (Exception e) {
            说(e.getMessage());
        }
    }

    private void 删除SS包(RandomAccessFile 文件随机访问器, long 开始位置, long 结束位置) throws Exception {
        if (结束位置 < 文件随机访问器.length()) {
            byte 字节数组[] = new byte[(int)(文件随机访问器.length() - 结束位置)];
            文件随机访问器.seek(结束位置);
            文件随机访问器.read(字节数组, 0, 字节数组.length);
            文件随机访问器.seek(开始位置);
            文件随机访问器.write(字节数组, 0, 字节数组.length);
            文件随机访问器.setLength(开始位置 + 字节数组.length);
        } else {
            文件随机访问器.setLength(开始位置);
        }
    }

    private void 提示有新消息(String 内容) {
        提示有新消息(null,内容, false);
    }

    private void 提示有新消息(byte 讯宝指令, String 文本, byte 秒数) {
        switch (讯宝指令) {
            case ProtocolParameters.讯宝指令_发送文字:
                提示有新消息(文本);
                break;
            case ProtocolParameters.讯宝指令_发送语音:
                提示有新消息("[" + 界面文字.获取(258, "语音：#% 秒", new Object[]{秒数}) + "]");
                break;
            case ProtocolParameters.讯宝指令_发送图片:
                提示有新消息("[" + 界面文字.获取(323, "图片") + "]");
                break;
            case ProtocolParameters.讯宝指令_发送短视频:
                提示有新消息("[" + 界面文字.获取(324, "短视频") + "]");
                break;
            case ProtocolParameters.讯宝指令_发送文件:
                提示有新消息("[" + 界面文字.获取(325, "文件") + "]");
                break;
            default:
                提示有新消息(new MyDate(System.currentTimeMillis()).获取("HH:mm:ss"));
        }
    }

    private void 提示有新消息(String 标题, String 内容, boolean 一定显示通知) {
        Uri 路径 = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Ringtone 铃声播放器 = RingtoneManager.getRingtone(运行环境.getApplicationContext(), 路径);
        铃声播放器.play();
        if (!Activity_Main.已暂停 && !一定显示通知) { return; }
        try {
            NotificationManager 通知管理器 = (NotificationManager) 运行环境.getSystemService(运行环境.NOTIFICATION_SERVICE);
            PendingIntent 意图 = PendingIntent.getActivity(运行环境, 1, new Intent(运行环境, Activity_Main.class), PendingIntent.FLAG_CANCEL_CURRENT);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                String 渠道编号 = 运行环境.getPackageName() + ".message";
                Notification.Builder 通知创建器 = new Notification
                        .Builder(运行环境, 渠道编号)
                        .setContentTitle((SharedMethod.字符串未赋值或为空(标题)?界面文字.获取(123, "讯宝网络有新消息"):标题))
                        .setContentText(内容)
                        .setWhen(System.currentTimeMillis())
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setLargeIcon(BitmapFactory.decodeResource(运行环境.getResources(), R.mipmap.ic_launcher))
                        .setContentIntent(意图)
                        .setVisibility(Notification.VISIBILITY_PUBLIC)
                        .setAutoCancel(true);
                NotificationChannel 通知的渠道 = new NotificationChannel(渠道编号, "New messages", NotificationManager.IMPORTANCE_HIGH);
                通知的渠道.setSound(null, Notification.AUDIO_ATTRIBUTES_DEFAULT);
                通知管理器.createNotificationChannel(通知的渠道);
                通知管理器.notify(通知编号, 通知创建器.build());
            } else {
                Notification.Builder 通知创建器 = new Notification
                        .Builder(运行环境)
                        .setContentTitle((SharedMethod.字符串未赋值或为空(标题) ? 界面文字.获取(123, "讯宝网络有新消息") : 标题))
                        .setContentText(内容)
                        .setWhen(System.currentTimeMillis())
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentIntent(意图)
                        .setPriority(Notification.PRIORITY_HIGH)
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setVisibility(Notification.VISIBILITY_PUBLIC)
                        .setSound(null)
                        .setAutoCancel(true);
                通知管理器.notify(通知编号, 通知创建器.build());
            }
            Activity_Main.有新消息 = true;
        } catch (Exception e) {
//            当前用户.记录运行步骤(e.getMessage());
            return;
        }
    }


    private class Thread_CheckLargeChatServer extends Thread {

        Robot_MainControl 主控机器人;
        httpSetting 访问设置;

        public Thread_CheckLargeChatServer(Robot_MainControl 主控机器人1, httpSetting 访问设置1) {
            主控机器人 = 主控机器人1;
            访问设置 = 访问设置1;
        }

        @Override
        public void run() {
            主控机器人.HTTPS访问2(访问设置);
        }
    }

    private class Thread_ConnectTransportServer extends Thread {

        private Robot_MainControl 主控机器人;

        public Thread_ConnectTransportServer(Robot_MainControl 主控机器人) {
            this.主控机器人 = 主控机器人;
        }

        @Override
        public void run() {
            主控机器人.连接传送服务器();
        }

    }

    private class Thread_SendSS extends Thread {

        Robot_MainControl 主控机器人;

        public Thread_SendSS(Robot_MainControl 主控机器人) {
            this.主控机器人 = 主控机器人;
        }

        @Override
        public void run() {
            主控机器人.发送();
        }
    }

}
