package net.ssignal;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;

import net.ssignal.network.MyX509TrustManager;
import net.ssignal.network.httpSetting;
import net.ssignal.protocols.Convert;
import net.ssignal.protocols.ProtocolFormats;
import net.ssignal.protocols.ProtocolPath;
import net.ssignal.protocols.Contact;
import net.ssignal.structure.GroupWithNewSS;
import net.ssignal.protocols.Group_Large;
import net.ssignal.protocols.ProtocolParameters;
import net.ssignal.protocols.SSPackageCreator;
import net.ssignal.protocols.SSPackageReader;
import net.ssignal.structure.HostName;
import net.ssignal.util.Constants;
import net.ssignal.util.MyDate;
import net.ssignal.util.SharedMethod;

import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.URL;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import static net.ssignal.Activity_Main.主活动;
import static net.ssignal.User.当前用户;
import static net.ssignal.Robot_MainControl.主控机器人;
import static net.ssignal.language.Text.界面文字;
import static net.ssignal.language.Text.组名_任务;
import static net.ssignal.network.encodeURI.替换URI敏感字符;

class Robot_LargeChatGroup extends Robot {

    private Context 运行环境;
    private class 需删除的讯宝_复合数据 {
        long 时间 = 0;
        String 文件路径;
        boolean 是视频 = false;
    }

    private 需删除的讯宝_复合数据 需删除;
    private boolean 正在退出聊天群;
    boolean 原图发送, 正在选择群图标 = false, 已载入最近聊天记录 = false;

    Robot_LargeChatGroup(Context 运行环境, Fragment_Chating 聊天控件) {
        this.运行环境 = 运行环境;
        this.聊天控件 = 聊天控件;
        跨线程调用器 = new MyHandler(this);
    }

    @Override
    void 回答(String 用户输入, long 时间) {
        if (用户输入.equals(TaskName.任务名称_退出聊天群)) {
            正在退出聊天群 = true;
        } else if (正在退出聊天群) {
            正在退出聊天群 = false;
        }
        if (SharedMethod.字符串未赋值或为空(聊天控件.聊天对象.大聊天群.连接凭据)) {
            聊天控件.获取连接凭据(false);
            return;
        }
        if (用户输入.equalsIgnoreCase(TaskName.任务名称_小宇宙)) {
            打开群小宇宙页面();
        } else if (用户输入.equalsIgnoreCase(TaskName.任务名称_发送语音)) {
            发送语音还是文字(true);
        } else if (用户输入.equalsIgnoreCase(TaskName.任务名称_发送文字)) {
            发送语音还是文字(false);
        } else if (用户输入.equalsIgnoreCase(TaskName.任务名称_发送图片)) {
            发送图片(用户输入, false);
        } else if (用户输入.equalsIgnoreCase(TaskName.任务名称_发送原图)) {
            发送图片(用户输入, true);
        } else if (用户输入.equalsIgnoreCase(TaskName.任务名称_发送照片)) {
            发送照片(用户输入);
        } else if (用户输入.equalsIgnoreCase(TaskName.任务名称_发送短视频)) {
            发送短视频(用户输入);
        } else if (用户输入.equalsIgnoreCase(TaskName.任务名称_发送文件)) {
            发送文件(用户输入);
        } else if (用户输入.equalsIgnoreCase(TaskName.任务名称_邀请)) {
            邀请(用户输入);
        } else if (用户输入.equalsIgnoreCase(TaskName.任务名称_退出聊天群)) {
            退出聊天群(用户输入);
        } else if (用户输入.equalsIgnoreCase(TaskName.任务名称_昵称)) {
            昵称(用户输入);
        } else if (用户输入.equalsIgnoreCase(TaskName.任务名称_修改角色)) {
            修改角色(用户输入);
        } else if (用户输入.equalsIgnoreCase(TaskName.任务名称_删减成员)) {
            删减成员(用户输入);
        } else if (用户输入.equalsIgnoreCase(TaskName.任务名称_群名称)) {
            群名称(用户输入);
        } else if (用户输入.equalsIgnoreCase(TaskName.任务名称_图标)) {
            选择图标图片(用户输入);
        } else if (用户输入.equalsIgnoreCase(TaskName.任务名称_解散聊天群)) {
            解散聊天群(用户输入);
        } else if (用户输入.equalsIgnoreCase(TaskName.任务名称_取消)) {
            if (任务 != null) {
                任务.结束();
                任务 = null;
                说(界面文字.获取(16, "已取消。"));
            } else {
                说(界面文字.获取(93, "需要我做什么？"));
            }
        } else {
            if (任务 != null) {
                if (任务.名称.equalsIgnoreCase(TaskName.任务名称_退出聊天群)) {
                    if (退出聊天群2(用户输入)) { return; }
                } else if (任务.名称.equalsIgnoreCase(TaskName.任务名称_解散聊天群)) {
                    if (解散聊天群2(用户输入)) { return; }
                }
            }
            任务接收用户输入(用户输入, 时间);
        }
    }

    private void 打开群小宇宙页面() {
        说(界面文字.获取(7, "请稍等。"));
        if (聊天控件.小宇宙控件 != null) {
            聊天控件.小宇宙控件.网页载入完毕 = false;
            聊天控件.小宇宙控件.网页浏览器.loadUrl(ProtocolPath.获取大聊天群小宇宙的访问路径(聊天控件.聊天对象.大聊天群.子域名));
        }
        Fragment_Main.主窗体.左右滑动页容器.setCurrentItem(2);
    }

    void 发送语音还是文字(boolean 语音) {
        if (语音) {
            if (Build.VERSION.SDK_INT >= 23) {
                if (!主活动.检查录音权限()) {
                    说(界面文字.获取(263, "请赋予我录音的权限。"));
                    主活动.请求录音权限(this);
                    return;
                }
            }
            聊天控件.发送语音还是文字(语音);
            说(界面文字.获取(253, "已切换至语音模式。"));
        } else {
            聊天控件.发送语音还是文字(语音);
            说(界面文字.获取(254, "已切换至文字模式。"));
        }
    }

    private void 发送图片(String 用户输入, boolean 原图发送) {
        if (任务 != null) { 任务.结束(); }
        正在选择群图标 = false;
        if (Build.VERSION.SDK_INT >= 23) {
            if (!主活动.选取文件前检查读取存储卡的权限()) {
                说(界面文字.获取(262, "请赋予我读取存储卡的权限。"));
                主活动.选取图片前请求读取存储卡的权限(this);
                return;
            }
        }
        if (!原图发送) {
            说(界面文字.获取(23, "请选择图片（最多#%幅）。", new Object[] {ProtocolParameters.最大值_选择的图片数量}));
        } else {
            说(界面文字.获取(47, "请选择图片（最多#%幅）。图片不会被转换成jpg格式。", new Object[] {ProtocolParameters.最大值_选择的图片数量}));
        }
        this.原图发送 = 原图发送;
        主活动.选择图片(this);
    }

    void 发送图片2(String 图片路径[], String 保存路径) {
        if (原图发送) {
            long 最大值 = 1024 * 1024 * ProtocolParameters.最大值_讯宝文件数据长度_兆;
            for (int i = 0; i < 图片路径.length; i++) {
                File 文件 = new File(图片路径[i]);
                if (文件.length() > 最大值) {
                    说(界面文字.获取(145, "文件的大小超过#%兆了。", new Object[] {ProtocolParameters.最大值_讯宝文件数据长度_兆}));
                    return;
                }
            }
        }
        String 文件路径;
        int 宽度, 高度;
        long 当前UTC时刻 = SharedMethod.获取当前UTC时间();
        byte 字节数组[] = null;
        for (int i = 0; i < 图片路径.length; i++) {
            try {
                文件路径 = 图片路径[i];
                Bitmap 原图 = BitmapFactory.decodeFile(文件路径);
                if (!原图发送) {
                    Bitmap 压缩后图片;
                    if (原图.getWidth() > ProtocolParameters.最大值_讯宝图片宽高_像素 || 原图.getHeight() > ProtocolParameters.最大值_讯宝图片宽高_像素) {
                        double 缩小比例;
                        if (原图.getHeight() > 原图.getWidth()) {
                            缩小比例 = (double) ProtocolParameters.最大值_讯宝图片宽高_像素 / (double)原图.getHeight();
                        } else {
                            缩小比例 = (double) ProtocolParameters.最大值_讯宝图片宽高_像素 / (double)原图.getWidth();
                        }
                        压缩后图片 = Bitmap.createBitmap((int)((double)原图.getWidth() * 缩小比例), (int)((double)原图.getHeight() * 缩小比例), Bitmap.Config.ARGB_8888);
                    } else {
                        压缩后图片 = Bitmap.createBitmap(原图.getWidth(), 原图.getHeight(), Bitmap.Config.ARGB_8888);
                    }
                    Canvas 绘图器 = new Canvas(压缩后图片);
                    绘图器.drawBitmap(原图, new Rect(0, 0, 原图.getWidth(), 原图.getHeight()), new Rect(0, 0, 压缩后图片.getWidth(), 压缩后图片.getHeight()), null);
                    文件路径 = 保存路径 + "/" + 当前用户.获取英语讯宝地址();
                    File 目录 = new File(文件路径);
                    if (!目录.exists() || !目录.isDirectory()) {
                        目录.mkdir();
                    }
                    文件路径 += "/" + SharedMethod.生成大小写英文字母与数字的随机字符串(20) + ".jpg";
                    SharedMethod.保存位图(压缩后图片, 文件路径);
                }
                if (原图.getWidth() > ProtocolParameters.最大值_讯宝预览图片宽高_像素 || 原图.getHeight() > ProtocolParameters.最大值_讯宝预览图片宽高_像素) {
                    double 缩小比例;
                    if (原图.getHeight() > 原图.getWidth()) {
                        缩小比例 = (double) ProtocolParameters.最大值_讯宝预览图片宽高_像素 / (double)原图.getHeight();
                    } else {
                        缩小比例 = (double) ProtocolParameters.最大值_讯宝预览图片宽高_像素 / (double)原图.getWidth();
                    }
                    宽度 = (int)((double)原图.getWidth() * 缩小比例);
                    高度 = (int)((double)原图.getHeight() * 缩小比例);
                } else {
                    宽度 = 原图.getWidth();
                    高度 = 原图.getHeight();
                }
                字节数组 = SharedMethod.读取文件的全部字节(文件路径);
            } catch (Exception e) {
                说(e.getMessage());
                return;
            }
            if (字节数组 == null) {
                continue;
            }
            聊天控件.发送JS("function(){var imgsrc = \"" + SharedMethod.处理文件路径以用作JS函数参数(文件路径) + "\"; var iconsrc =\"" + ProtocolPath.获取我的头像路径(当前用户.英语用户名, 当前用户.主机名, 当前用户.头像更新时间, 当前用户.域名_英语) + "\"; var time = \"" + 聊天控件.时间格式(new MyDate(当前UTC时刻)) + "\"; SSout_Img(\"" + 当前UTC时刻 + "\", imgsrc, \"" + 宽度 + "\", \"" + 高度 + "\", iconsrc, time); }");
            if (!原图发送) {
                发送或接收(ProtocolParameters.讯宝指令_发送图片, SharedMethod.获取扩展名(文件路径), (short)宽度, (short)高度, (byte)0, 字节数组, 当前UTC时刻, 文件路径, null);
            } else {
                发送或接收(ProtocolParameters.讯宝指令_发送图片, SharedMethod.获取扩展名(文件路径), (short)宽度, (short)高度, (byte)0, 字节数组, 当前UTC时刻, null, null);
            }
            当前UTC时刻 += 1;
        }
    }

    private void 发送照片(String 用户输入) {
        if (任务 != null) { 任务.结束(); }
        if (!主活动.检查拍照的权限()) {
            说(界面文字.获取(316, "请赋予我拍照的权限。"));
            主活动.请求拍照的权限(this);
            return;
        }
        拍照();
    }

    void 拍照() {
        主活动.startActivity(new Intent("net.ssignal.TakePhoto"));
        Activity_TakePhoto.机器人 = this;
    }

    @Override
    void 发送照片2(String 照片文件路径) {
        String 文件路径;
        int 宽度, 高度;
        long 当前UTC时刻 = SharedMethod.获取当前UTC时间();
        byte 字节数组[] = null;
        try {
            Bitmap 原图 = BitmapFactory.decodeFile(照片文件路径);
            Bitmap 压缩后图片;
            if (原图.getWidth() > ProtocolParameters.最大值_讯宝图片宽高_像素 || 原图.getHeight() > ProtocolParameters.最大值_讯宝图片宽高_像素) {
                double 缩小比例;
                if (原图.getHeight() > 原图.getWidth()) {
                    缩小比例 = (double) ProtocolParameters.最大值_讯宝图片宽高_像素 / (double)原图.getHeight();
                } else {
                    缩小比例 = (double) ProtocolParameters.最大值_讯宝图片宽高_像素 / (double)原图.getWidth();
                }
                压缩后图片 = Bitmap.createBitmap((int)((double)原图.getWidth() * 缩小比例), (int)((double)原图.getHeight() * 缩小比例), Bitmap.Config.ARGB_8888);
            } else {
                压缩后图片 = Bitmap.createBitmap(原图.getWidth(), 原图.getHeight(), Bitmap.Config.ARGB_8888);
            }
            Canvas 绘图器 = new Canvas(压缩后图片);
            绘图器.drawBitmap(原图, new Rect(0, 0, 原图.getWidth(), 原图.getHeight()), new Rect(0, 0, 压缩后图片.getWidth(), 压缩后图片.getHeight()), null);
            文件路径 = 主活动.getFilesDir().toString() + "/" + 当前用户.获取英语讯宝地址();
            File 目录 = new File(文件路径);
            if (!目录.exists() || !目录.isDirectory()) {
                目录.mkdir();
            }
            文件路径 += "/" + SharedMethod.生成大小写英文字母与数字的随机字符串(20) + ".jpg";
            SharedMethod.保存位图(压缩后图片, 文件路径);
            if (原图.getWidth() > ProtocolParameters.最大值_讯宝预览图片宽高_像素 || 原图.getHeight() > ProtocolParameters.最大值_讯宝预览图片宽高_像素) {
                double 缩小比例;
                if (原图.getHeight() > 原图.getWidth()) {
                    缩小比例 = (double) ProtocolParameters.最大值_讯宝预览图片宽高_像素 / (double)原图.getHeight();
                } else {
                    缩小比例 = (double) ProtocolParameters.最大值_讯宝预览图片宽高_像素 / (double)原图.getWidth();
                }
                宽度 = (int)((double)原图.getWidth() * 缩小比例);
                高度 = (int)((double)原图.getHeight() * 缩小比例);
            } else {
                宽度 = 原图.getWidth();
                高度 = 原图.getHeight();
            }
            字节数组 = SharedMethod.读取文件的全部字节(文件路径);
        } catch (Exception e) {
            说(e.getMessage());
            return;
        }
        聊天控件.发送JS("function(){var imgsrc = \"" + SharedMethod.处理文件路径以用作JS函数参数(文件路径) + "\"; var iconsrc =\"" + ProtocolPath.获取我的头像路径(当前用户.英语用户名, 当前用户.主机名, 当前用户.头像更新时间, 当前用户.域名_英语) + "\"; var time = \"" + 聊天控件.时间格式(new MyDate(当前UTC时刻)) + "\"; SSout_Img(\"" + 当前UTC时刻 + "\", imgsrc, \"" + 宽度 + "\", \"" + 高度 + "\", iconsrc, time); }");
        发送或接收(ProtocolParameters.讯宝指令_发送图片, SharedMethod.获取扩展名(文件路径), (short)宽度, (short)高度, (byte)0, 字节数组, 当前UTC时刻, 文件路径, null);
    }

    private void 发送短视频(String 用户输入) {
        if (任务 != null) { 任务.结束(); }
        if (!主活动.检查录音录像的权限()) {
            说(界面文字.获取(314, "请赋予我录制音视频的权限。"));
            主活动.请求录音录像的权限(this);
            return;
        }
        录制短视频();
    }

    void 录制短视频() {
        主活动.startActivity(new Intent("net.ssignal.RecordVideo"));
        Activity_RecordVideo.机器人 = this;
    }

    @Override
    void 发送短视频2(String 视频文件路径, short 预览图片宽度, short 预览图片高度) {
        long 当前UTC时刻 = SharedMethod.获取当前UTC时间();
        byte 字节数组[] = null;
        byte 视频预览图片数据[] = null;
        try {
            字节数组 = SharedMethod.读取文件的全部字节(视频文件路径);
            if (字节数组 == null) {
                return;
            }
            视频预览图片数据 = SharedMethod.读取文件的全部字节(视频文件路径 + ".jpg");
            if (视频预览图片数据 == null) {
                return;
            }
        } catch (Exception e) {
            说(e.getMessage());
            return;
        }
        聊天控件.发送JS("function(){var videosrc = \"" + SharedMethod.处理文件路径以用作JS函数参数(视频文件路径) + "\"; var iconsrc =\"" + ProtocolPath.获取我的头像路径(当前用户.英语用户名, 当前用户.主机名, 当前用户.头像更新时间, 当前用户.域名_英语) + "\"; var time = \"" + 聊天控件.时间格式(new MyDate(当前UTC时刻)) + "\"; SSout_Video(\"" + 当前UTC时刻 + "\", videosrc, \"" + 预览图片宽度 + "\", \"" + 预览图片高度 + "\", iconsrc, time); }");
        发送或接收(ProtocolParameters.讯宝指令_发送短视频, SharedMethod.获取扩展名(视频文件路径), 预览图片宽度, 预览图片高度, (byte)0, 字节数组, 当前UTC时刻, 视频文件路径, 视频预览图片数据);
    }

    private void 发送文件(String 用户输入) {
        if (任务 != null) { 任务.结束(); }
        if (Build.VERSION.SDK_INT >= 23) {
            if (!主活动.选取文件前检查读取存储卡的权限()) {
                说(界面文字.获取(262, "请赋予我读取存储卡的权限。"));
                主活动.选取文件前请求读取存储卡的权限(this);
                return;
            }
        }
        说(界面文字.获取(305, "请选择一个文件。"));
        主活动.选择文件(this);
    }

    void 发送文件2(String 文件路径) {
        File 文件 = new File(文件路径);
        if (文件.length() > 1024 * 1024 * ProtocolParameters.最大值_讯宝文件数据长度_兆) {
            说(界面文字.获取(145, "文件的大小超过#%兆了。", new Object[] {ProtocolParameters.最大值_讯宝文件数据长度_兆}));
            return;
        }
        long 当前UTC时刻 = SharedMethod.获取当前UTC时间();
        byte 字节数组[] = null;
        try {
            字节数组 = SharedMethod.读取文件的全部字节(文件路径);
            if (字节数组 == null) {
                return;
            }
            聊天控件.发送JS("function(){var text = \"" + SharedMethod.处理文件路径以用作JS函数参数(SharedMethod.获取文件名(文件路径)) + "\"; var filesrc = \"" + SharedMethod.处理文件路径以用作JS函数参数(文件路径) + "\"; var iconsrc =\"" + ProtocolPath.获取我的头像路径(当前用户.英语用户名, 当前用户.主机名, 当前用户.头像更新时间, 当前用户.域名_英语) + "\"; var time = \"" + 聊天控件.时间格式(new MyDate(当前UTC时刻)) + "\"; SSout_File(\"" + 当前UTC时刻 + "\", text, filesrc, iconsrc, time); }");
        } catch (Exception e) {
            说(e.getMessage());
            return;
        }
        发送或接收(ProtocolParameters.讯宝指令_发送文件, SharedMethod.获取文件名(文件路径), (short)0, (short)0, (byte)0, 字节数组, 当前UTC时刻, null, null);
    }

    private void 邀请(String 用户输入) {
        if (任务 != null) { 任务.结束(); }
        当前用户.显示讯友临时编号 = true;
        Fragment_Main.主窗体.刷新讯友录(Constants.讯友录显示范围_讯友);
        任务 = new Task(用户输入, 输入框, this);
        任务.添加步骤(Task.任务步骤_大聊天群邀请, 界面文字.获取(82, "请输入讯友的讯宝地址或临时编号（讯友备注行括号内的数字）。"));
        说(任务.获取当前步骤提示语());
    }

    private void 退出聊天群(String 用户输入) {
        if (任务 != null) { 任务.结束(); }
        任务 = new Task(用户输入, this);
        说(界面文字.获取(176, "你要退出此聊天群吗？请选择<a>#%</a>或者<a>#%</a>。", new Object[] {界面文字.获取(组名_任务, 0, "是"), 界面文字.获取(组名_任务, 1, "否")}));
    }

    private boolean 退出聊天群2(String 用户输入) {
        if (用户输入.equalsIgnoreCase(界面文字.获取(组名_任务, 0, "是"))) {
            说(界面文字.获取(7, "请稍等。"));
            Group_Large 大聊天群 = 聊天控件.聊天对象.大聊天群;
            启动HTTPS访问线程(new httpSetting(ProtocolPath.获取大聊天群服务器访问路径开头(大聊天群.子域名, false) + "C=LeaveGroup&EnglishSSAddress=" + 替换URI敏感字符(当前用户.获取英语讯宝地址()) + "&Credential=" + 替换URI敏感字符(大聊天群.连接凭据) + "&GroupID=" + 大聊天群.编号));
            return true;
        } else if (用户输入.equalsIgnoreCase(界面文字.获取(组名_任务, 1, "否"))) {
            回答(TaskName.任务名称_取消, 0);
            return true;
        }
        return false;
    }

    private void 昵称(String 用户输入) {
        if (任务 != null) { 任务.结束(); }
        任务 = new Task(用户输入, 输入框, this);
        任务.添加步骤(Task.任务步骤_大聊天群昵称, 界面文字.获取(296, "请输入你在本聊天群的新昵称（不超过#%个字符）。", new Object[] {ProtocolParameters.最大值_讯友备注字符数}));
        说(任务.获取当前步骤提示语());
    }

    private void 修改角色(String 用户输入) {
        if (任务 != null) { 任务.结束(); }
        任务 = new Task(用户输入, 输入框, this);
        任务.添加步骤(Task.任务步骤_大聊天群修改角色, 界面文字.获取(180, "请输入成员的讯宝地址或临时编号（成员备注行括号内的数字）。"));
        任务.添加步骤(Task.任务步骤_大聊天群某成员的新角色, 界面文字.获取(291, "请输入括号内的数字：不可发言的普通成员（#%）、可发言的普通成员（#%）或管理员（#%）。", new Object[] {ProtocolParameters.群角色_成员_不可发言, ProtocolParameters.群角色_成员_可以发言, ProtocolParameters.群角色_管理员}));
        说(任务.获取当前步骤提示语());
    }

    private void 删减成员(String 用户输入) {
        if (任务 != null) { 任务.结束(); }
        任务 = new Task(用户输入, 输入框, this);
        任务.添加步骤(Task.任务步骤_大聊天群删减成员, 界面文字.获取(180, "请输入成员的讯宝地址或临时编号（成员备注行括号内的数字）。"));
        说(任务.获取当前步骤提示语());
    }

    private void 群名称(String 用户输入) {
        if (任务 != null) { 任务.结束(); }
        任务 = new Task(用户输入, 输入框, this);
        任务.添加步骤(Task.任务步骤_大聊天群名称, 界面文字.获取(183, "请为输入群的新名称。（不超过#%个字符）", new Object[]{ProtocolParameters.最大值_群名称字符数}));
        说(任务.获取当前步骤提示语());
    }

    private void 选择图标图片(String 用户输入) {
        if (任务 != null) { 任务.结束(); }
        正在选择群图标 = true;
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

    void 选择图标图片2(String 图片路径, String 保存路径) {
        Group_Large 大聊天群 = 聊天控件.聊天对象.大聊天群;
        byte 字节数组[] = null;
        try {
            Bitmap 位图 = BitmapFactory.decodeFile(图片路径);
            if (位图.getWidth() < ProtocolParameters.长度_图标宽高_像素 || 位图.getHeight() < ProtocolParameters.长度_图标宽高_像素) {
                说(界面文字.获取(168, "图片太小。"));
                return;
            }
            Bitmap 位图2 = Bitmap.createBitmap((int)(ProtocolParameters.长度_图标宽高_像素), (int)(ProtocolParameters.长度_图标宽高_像素), Bitmap.Config.ARGB_8888);
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
            保存路径 += "/" + 当前用户.获取英语讯宝地址();
            File 目录 = new File(保存路径);
            if (!目录.exists() || !目录.isDirectory()) {
                目录.mkdir();
            }
            保存路径 += "/" + 大聊天群.编号 + ".jpg";
            SharedMethod.保存位图(位图2, 保存路径);
            字节数组 = SharedMethod.读取文件的全部字节(保存路径);
        } catch (Exception e) {
            说(e.getMessage());
            return;
        }
        if (字节数组 == null) {
            return;
        }
        任务 = new Task(TaskName.任务名称_图标, this);
        说(界面文字.获取(7, "请稍等。"));
        启动HTTPS访问线程(new httpSetting(ProtocolPath.获取大聊天群服务器访问路径开头(大聊天群.子域名, false) + "C=ChangeIcon&EnglishSSAddress=" + 替换URI敏感字符(当前用户.获取英语讯宝地址()) + "&Credential=" + 替换URI敏感字符(大聊天群.连接凭据) + "&GroupID=" + 大聊天群.编号, 字节数组));
    }

    private void 解散聊天群(String 用户输入) {
        if (任务 != null) { 任务.结束(); }
        任务 = new Task(用户输入, this);
        说(界面文字.获取(181, "你要解散此聊天群吗？请选择<a>#%</a>或者<a>#%</a>。", new Object[] {界面文字.获取(组名_任务, 0, "是"), 界面文字.获取(组名_任务, 1, "否")}));
    }

    private boolean 解散聊天群2(String 用户输入) {
        if (用户输入.equalsIgnoreCase(界面文字.获取(组名_任务, 0, "是"))) {
            说(界面文字.获取(7, "请稍等。"));
            Group_Large 大聊天群 = 聊天控件.聊天对象.大聊天群;
            启动HTTPS访问线程(new httpSetting(ProtocolPath.获取中心服务器访问路径开头(当前用户.域名_英语) + "C=DeleteGroup&UserID=" + 当前用户.编号 + "&Credential=" + 替换URI敏感字符(当前用户.凭据_中心服务器) + "&HostName=" + 大聊天群.主机名 + "&GroupID=" + 大聊天群.编号, 20000));
            return true;
        } else if (用户输入.equalsIgnoreCase(界面文字.获取(组名_任务, 1, "否"))) {
            回答(TaskName.任务名称_取消, 0);
            return true;
        }
        return false;
    }

    private void 任务接收用户输入(String 用户输入, long 时间) {
        Group_Large 大聊天群 = 聊天控件.聊天对象.大聊天群;
        if (任务 != null) {
            if (任务.步骤数量 > 0) {
                String 结果 = 任务.保存当前步骤输入值(用户输入);
                if (SharedMethod.字符串未赋值或为空(结果)) {
                    结果 = 任务.获取当前步骤提示语();
                    if (!SharedMethod.字符串未赋值或为空(结果)) {
                        说(结果);
                    } else {
                        if (任务.名称.equals(TaskName.任务名称_修改角色)) {
                            说(界面文字.获取(7, "请稍等。"));
                            启动HTTPS访问线程(new httpSetting(ProtocolPath.获取大聊天群服务器访问路径开头(大聊天群.子域名, false) + "C=ChangeRole&EnglishSSAddress=" + 替换URI敏感字符(当前用户.获取英语讯宝地址()) + "&Credential=" + 替换URI敏感字符(大聊天群.连接凭据) + "&GroupID=" + 大聊天群.编号 + "&MEnglishSSAddress=" + 替换URI敏感字符(任务.获取某步骤的输入值(Task.任务步骤_大聊天群修改角色)) + "&NewRole=" + 任务.获取某步骤的输入值(Task.任务步骤_大聊天群某成员的新角色)));
                        } else if (任务.名称.equals(TaskName.任务名称_邀请)) {
                            Contact 某一讯友 = 当前用户.查找讯友(任务.获取某步骤的输入值(Task.任务步骤_大聊天群邀请));
                            说(界面文字.获取(7, "请稍等。"));
                            启动HTTPS访问线程(new httpSetting(ProtocolPath.获取大聊天群服务器访问路径开头(大聊天群.子域名, false) + "C=AddMember&EnglishSSAddress=" + 替换URI敏感字符(当前用户.获取英语讯宝地址()) + "&Credential=" + 替换URI敏感字符(大聊天群.连接凭据) + "&GroupID=" + 大聊天群.编号 + "&MEnglishSSAddress=" + 替换URI敏感字符(某一讯友.英语讯宝地址) + "&MNativeSSAddress=" + 替换URI敏感字符(某一讯友.本国语讯宝地址) + "&CanSpeak=true&HostName=" + 某一讯友.主机名 + "&Position=" + 某一讯友.位置号));
                        } else if (任务.名称.equals(TaskName.任务名称_昵称)) {
                            说(界面文字.获取(7, "请稍等。"));
                            启动HTTPS访问线程(new httpSetting(ProtocolPath.获取大聊天群服务器访问路径开头(大聊天群.子域名, false) + "C=ChangeNickname&EnglishSSAddress=" + 替换URI敏感字符(当前用户.获取英语讯宝地址()) + "&Credential=" + 替换URI敏感字符(大聊天群.连接凭据) + "&GroupID=" + 大聊天群.编号 + "&Nickname=" + 替换URI敏感字符(任务.获取某步骤的输入值(Task.任务步骤_大聊天群昵称))));
                        } else if (任务.名称.equals(TaskName.任务名称_删减成员)) {
                            说(界面文字.获取(7, "请稍等。"));
                            启动HTTPS访问线程(new httpSetting(ProtocolPath.获取大聊天群服务器访问路径开头(大聊天群.子域名, false) + "C=RemoveMember&EnglishSSAddress=" + 替换URI敏感字符(当前用户.获取英语讯宝地址()) + "&Credential=" + 替换URI敏感字符(大聊天群.连接凭据) + "&GroupID=" + 大聊天群.编号 + "&MEnglishSSAddress=" + 替换URI敏感字符(任务.获取某步骤的输入值(Task.任务步骤_大聊天群删减成员))));
                        } else if (任务.名称.equals(TaskName.任务名称_群名称)) {
                            说(界面文字.获取(7, "请稍等。"));
                            启动HTTPS访问线程(new httpSetting(ProtocolPath.获取大聊天群服务器访问路径开头(大聊天群.子域名, false) + "C=ChangeGroupName&EnglishSSAddress=" + 替换URI敏感字符(当前用户.获取英语讯宝地址()) + "&Credential=" + 替换URI敏感字符(大聊天群.连接凭据) + "&GroupID=" + 大聊天群.编号 + "&NewName=" + 替换URI敏感字符(任务.获取某步骤的输入值(Task.任务步骤_大聊天群名称))));
                        }
                    }
                } else {
                    说(结果);
                }
                return;
            }
        }
        发送或接收(ProtocolParameters.讯宝指令_发送文字, 用户输入, (short)0, (short)0, (byte)0, null, 时间, null, null);
    }

    void 刷新() {
        if (SharedMethod.字符串未赋值或为空(聊天控件.聊天对象.大聊天群.连接凭据)) {
            聊天控件.获取连接凭据(false);
            return;
        }
        if (任务 != null) {
            任务.结束();
            任务 = null;
        }
        发送或接收(ProtocolParameters.讯宝指令_无, null, (short)0, (short)0, (byte)0, null, 0, null, null);
    }

    void 发送或接收(byte 讯宝指令, String 文字) {
        发送或接收(讯宝指令, 文字, (short)0, (short)0, (byte)0, null, 0, null, null);
    }

    void 发送或接收(byte 讯宝指令, String 文字, short 宽度, short 高度, byte 秒数, byte[] 文件数据, long 时间, String 文件路径, byte[] 视频预览图片数据) {
        需删除 = new 需删除的讯宝_复合数据();
        需删除.时间 = 时间;
        需删除.文件路径 = 文件路径;
        if (视频预览图片数据 != null) {
            需删除.是视频 = true;
        }
        Group_Large 大聊天群 = 聊天控件.聊天对象.大聊天群;
        byte 字节数组[] = ProtocolFormats.添加数据_大聊天群发送或接收讯宝(讯宝指令, 文字, 宽度, 高度, 秒数, 文件数据, 大聊天群.子域名, 当前用户.加入的大聊天群, 视频预览图片数据);
        if (字节数组 == null) { return; }
        启动HTTPS访问线程(new httpSetting(ProtocolPath.获取大聊天群服务器访问路径开头(大聊天群.子域名, false) + "C=PostOrGet&EnglishSSAddress=" + 替换URI敏感字符(当前用户.获取英语讯宝地址()) + "&Credential=" + 替换URI敏感字符(大聊天群.连接凭据) + "&GroupID=" + 大聊天群.编号, 字节数组));
        if (主控机器人.数据库_更新最近互动讯友排名(大聊天群.子域名, 大聊天群.编号)) {
            if (当前用户.讯友录当前显示范围 == Constants.讯友录显示范围_最近) {
                Fragment_Main.主窗体.刷新讯友录();
            }
        }
    }


    void 处理跨线程数据(Message msg) {
        switch (msg.what) {
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
                        }
                        任务.结束();
                        任务 = null;
                    }
                }
                break;
            case 3:
                当前用户.获取小宇宙凭据(聊天控件, msg.getData().getString("子域名"), false, 跨线程调用器);
                break;
            case 8:
                当前用户.获取小宇宙凭据结束(msg.getData().getLong("创建时刻"), msg.getData().getByteArray("字节数组"));
                break;
        }
    }

    private void HTTPS请求成功(byte SS包[]) {
        if (聊天控件 != null) { 聊天控件.按钮和机器人图标(false); }
        if (SS包 != null) {
            SSPackageReader SS包解读器;
            try {
                SS包解读器 = new SSPackageReader(SS包);
                switch (SS包解读器.获取查询结果()) {
                    case ProtocolParameters.查询结果_成功:
                        if (任务 == null) {
                            发送或接收成功(SS包解读器);
                        } else {
                            if (任务.名称.equalsIgnoreCase(TaskName.任务名称_发流星语)) {
                                流星语发布结束(true);
                                任务.结束();
                                任务 = null;
                                return;
                            } else if (任务.名称.equalsIgnoreCase(TaskName.任务名称_加入大聊天群)) {
                                获取连接凭据成功(SS包解读器);
                            } else if (任务.名称.equalsIgnoreCase(TaskName.任务名称_退出聊天群)) {
                                退出聊天群成功();
                            } else if (任务.名称.equalsIgnoreCase(TaskName.任务名称_邀请)) {
                                添加成员成功();
                            } else if (任务.名称.equalsIgnoreCase(TaskName.任务名称_群名称)) {
                                群名称修改成功();
                            } else if (任务.名称.equalsIgnoreCase(TaskName.任务名称_图标)) {
                                修改图标成功(SS包解读器);
                            } else if (任务.名称.equalsIgnoreCase(TaskName.任务名称_解散聊天群)) {
                                退出聊天群成功();
                            } else {
                                说(界面文字.获取(245, "完成。"));
                            }
                        }
                        break;
                    case ProtocolParameters.查询结果_不可发言:
                        说(界面文字.获取(299, "抱歉，你不可以发言。"));
                        break;
                    case ProtocolParameters.查询结果_发送序号不一致:
                        if (主控机器人 != null) {
                            主控机器人.关闭网络连接器(12);
                        }
                        break;
                    case ProtocolParameters.查询结果_本小时发送的讯宝数量已达上限:
                        说(界面文字.获取(266, "本小时发送的讯宝数量已达上限。"));
                        break;
                    case ProtocolParameters.查询结果_今日发送的讯宝数量已达上限:
                        说(界面文字.获取(267, "今日发送的讯宝数量已达上限。"));
                        break;
                    case ProtocolParameters.查询结果_凭据无效:
                        说(界面文字.获取(282, "连接凭据已过期。"));
                        聊天控件.聊天对象.大聊天群.连接凭据 = null;
                        聊天控件.获取连接凭据(true);
                        return;
                    case ProtocolParameters.查询结果_稍后重试:
                        说(界面文字.获取(20, "你的操作过于频繁，请#%分钟后再尝试。", new Object[] {Constants.最近操作次数统计时间_分钟}));
                        break;
                    case ProtocolParameters.查询结果_大聊天群服务器用户数已满:
                        说(界面文字.获取(287, "大聊天群服务器的用户数已满。"));
                        break;
                    case ProtocolParameters.查询结果_大聊天群名称已存在:
                        说(界面文字.获取(286, "其它群已使用此名称。"));
                        break;
                    case ProtocolParameters.查询结果_不是群成员:
                        if (!正在退出聊天群) {
                            说(界面文字.获取(83, "你不是当前聊天群的成员。"));
                        } else {
                            退出聊天群成功();
                        }
                        break;
                    case ProtocolParameters.查询结果_不是正式群成员:
                        说(界面文字.获取(285, "请在对方接受你的邀请后再进行此操作。"));
                        break;
                    case ProtocolParameters.查询结果_无权操作:
                        说(界面文字.获取(154, "你无权进行此项操作。"));
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
                        break;
                    case ProtocolParameters.查询结果_服务器未就绪:
                        说(界面文字.获取(269, "服务器还未就绪。请稍后重试。"));
                        break;
                    case ProtocolParameters.查询结果_数据库未就绪:
                        说(界面文字.获取(141, "数据库未就绪。"));
                        break;
                    default :
                        说(界面文字.获取(108, "出错 #%", new Object[] {SS包解读器.获取查询结果()}));
                }
                if (任务 != null) {
                    if (任务.名称.equalsIgnoreCase(TaskName.任务名称_发流星语)) {
                        流星语发布结束(false);
                    }
                }
            } catch (Exception e) {
                说(e.getMessage());
            }
        }
        if (任务 != null) {
            任务.结束();
            任务 = null;
        }
    }


    private void 发送或接收成功(SSPackageReader SS包解读器) throws Exception {
        if (当前用户.加入的大聊天群 == null) {
            return;
        }
        Object SS包解读器2[] = SS包解读器.读取_重复标签("GP");
        if (SS包解读器2 != null) {
            String 子域名 = 聊天控件.聊天对象.大聊天群.子域名;
            long 群编号, 时间 = 0;
            String 发送者英语地址, 文本, 英语讯宝地址, 本国语讯宝地址, 主机名;
            byte 讯宝指令, 秒数, 角色;
            short 宽度, 高度, 位置号;
            GroupWithNewSS 群[] = new GroupWithNewSS[SS包解读器2.length];
            Object SS包解读器3[];
            SSPackageReader SS包解读器4, SS包解读器5;
            Group_Large 大聊天群;
            boolean 图标或名称有变动 = false;
            Group_Large 加入的大聊天群[] = 当前用户.加入的大聊天群;
            boolean 有新讯宝 = false, 有新主机名 = false;
            int i, j, k;
            for (i = 0; i < SS包解读器2.length; i++) {
                SS包解读器5 = (SSPackageReader)SS包解读器2[i];
                群编号 = (long) SS包解读器5.读取_有标签("GI");
                for (j = 0; j < 加入的大聊天群.length; j++) {
                    if (加入的大聊天群[j].编号 == 群编号 && 加入的大聊天群[j].子域名.equals(子域名)) {
                        break;
                    }
                }
                if (j < 加入的大聊天群.length) {
                    大聊天群 = 加入的大聊天群[j];
                } else {
                    continue;
                }
                SS包解读器3 = SS包解读器5.读取_重复标签("SS");
                if (SS包解读器3 != null) {
                    int 新讯宝数量 = 0;
                    long 撤回的讯宝[] = null;
                    int 撤回的讯宝数量 = 0;
                    for (j = 0; j < SS包解读器3.length; j++) {
                        SS包解读器5 = (SSPackageReader)SS包解读器3[j];
                        时间 = (long) SS包解读器5.读取_有标签("DT");
                        发送者英语地址 = (String) SS包解读器5.读取_有标签("FR");
                        主机名 = (String) SS包解读器5.读取_有标签("HN");
                        讯宝指令 = (byte) SS包解读器5.读取_有标签("CM");
                        文本 = (String) SS包解读器5.读取_有标签("TX");
                        Object 值 = SS包解读器5.读取_有标签("WD");
                        if (值 != null) {
                            宽度 = (short) 值;
                        } else {
                            宽度 = 0;
                        }
                        值 = SS包解读器5.读取_有标签("HT");
                        if (值 != null) {
                            高度 = (short) 值;
                        } else {
                            高度 = 0;
                        }
                        值 = SS包解读器5.读取_有标签("SC");
                        if (值 != null) {
                            秒数 = (byte) 值;
                        } else {
                            秒数 = 0;
                        }
                        switch (讯宝指令) {
                            case ProtocolParameters.讯宝指令_撤回:
                                long 发送时间 = Long.parseLong(文本);
                                if (数据库_撤回讯宝(子域名, 群编号, 发送时间)) {
                                    if (撤回的讯宝数量 > 0) {
                                        撤回的讯宝[撤回的讯宝数量] = 发送时间;
                                        撤回的讯宝数量 += 1;
                                    } else {
                                        撤回的讯宝 = new long[SS包解读器3.length];
                                        撤回的讯宝[0] = 发送时间;
                                        撤回的讯宝数量 = 1;
                                    }
                                }
                                continue;
                            case ProtocolParameters.讯宝指令_某人在聊天群的角色改变:
                                try {
                                    SS包解读器5 = new SSPackageReader();
                                    SS包解读器5.解读纯文本(文本);
                                    英语讯宝地址 = (String) SS包解读器5.读取_有标签("E");
                                    if (SharedMethod.字符串未赋值或为空(英语讯宝地址)) {
                                        continue;
                                    }
                                    角色 = (byte) SS包解读器5.读取_有标签("R");
                                } catch (Exception e) {
                                    continue;
                                }
                                if (当前用户.获取英语讯宝地址().equals(英语讯宝地址)) {
                                    大聊天群.我的角色 = 角色;
                                }
                                continue;
                            case ProtocolParameters.讯宝指令_修改聊天群名称:
                                大聊天群.名称 = 文本;
                                图标或名称有变动 = true;
                                continue;
                            case ProtocolParameters.讯宝指令_聊天群图标改变:
                                try {
                                    大聊天群.图标更新时间 = Long.parseLong(文本);
                                } catch (Exception e) {
                                    continue;
                                }
                                图标或名称有变动 = true;
                                continue;
                        }
                        if (!SharedMethod.字符串未赋值或为空(主机名) && !发送者英语地址.equals(当前用户.获取英语讯宝地址())) {
                            if (数据库_保存主机名(发送者英语地址, 主机名)) {
                                有新主机名 = true;
                            }
                        }
                        if (!数据库_保存收到的大聊天群讯宝(子域名, 群编号, 发送者英语地址, 时间, 讯宝指令, 文本, 宽度, 高度, 秒数)) {
                            return;
                        }
                        新讯宝数量 += 1;
                        if (!有新讯宝) {
                            if (!发送者英语地址.equals(当前用户.获取英语讯宝地址())) {
                                有新讯宝 = true;
                            }
                        }
                    }
                    GroupWithNewSS 某一群 = new GroupWithNewSS();
                    某一群.编号 = 群编号;
                    某一群.时间 = 时间;
                    某一群.新讯宝数量 = 新讯宝数量;
                    某一群.撤回的讯宝 = 撤回的讯宝;
                    某一群.撤回的讯宝数量 = 撤回的讯宝数量;
                    群[i] = 某一群;
                }
            }
            if (有新主机名) {
                数据库_保存主机名2();
            }
            if (群.length > 1) {
                for (i = 0; i < 群.length - 1; i++) {
                    for (j = i + 1; j < 群.length; j++) {
                        if (群[i].时间 > 群[j].时间) {
                            时间 = 群[i].时间;
                            群[i].时间 = 群[j].时间;
                            群[j].时间 = 时间;
                        }
                    }
                }
            }
            boolean 刷新 = false;
            for (i = 0; i < 群.length; i++) {
                if (群[i].新讯宝数量 > 0) {
                    if (主控机器人.数据库_更新最近互动讯友排名(子域名, 群[i].编号)) {
                        刷新 = true;
                    }
                }
            }
            if (需删除 != null) {
                if (!SharedMethod.字符串未赋值或为空(需删除.文件路径)) {
                    File 文件 = new File(需删除.文件路径);
                    if (文件.exists()) {
                        文件.delete();
                    }
                    if (需删除.是视频) {
                        文件 = new File(需删除.文件路径 + ".jpg");
                        if (文件.exists()) {
                            文件.delete();
                        }
                    }
                }
                聊天控件.发送JS("function(){ RemoveSS('" + 需删除.时间 + "'); }");
                需删除 = null;
            }
            if (Fragment_Main.主窗体 != null) {
                Fragment_Main.主窗体.显示收到的大聊天群讯宝(子域名, 群, 刷新);
            }
            if (图标或名称有变动) {
                if (当前用户.讯友录当前显示范围 == Constants.讯友录显示范围_聊天群) {
                    Fragment_Main.主窗体.刷新讯友录();
                }
            }
            long 检查时间 = System.currentTimeMillis();
            for (i = 0; i < 加入的大聊天群.length; i++) {
                if (加入的大聊天群[i].子域名.equals(子域名)) {
                    加入的大聊天群[i].检查时间 = 检查时间;
                }
            }
            if (有新讯宝) {
                Uri 路径 = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                Ringtone 铃声播放器 = RingtoneManager.getRingtone(运行环境.getApplicationContext(), 路径);
                铃声播放器.play();
            }
        }
    }

    private boolean 数据库_保存主机名(String 发送者讯宝地址, String 主机名) {
        HostName 主机名数组[] = null;
        byte 字节数组[] = null;
        if (当前用户.主机名数组 == null) {
            try {
                字节数组 = SharedMethod.读取文件的全部字节(运行环境.getFilesDir().toString() + "/" + 当前用户.获取英语讯宝地址() + "/hostname.txt");
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
            if (主机名数组[i].英语讯宝地址.equals(发送者讯宝地址)) {
                return false;
            }
        }
        HostName 主机名数组2[];
        if (当前用户.主机名数量 < 主机名数组.length) {
            主机名数组2 = new HostName[主机名数组.length];
        } else {
            主机名数组2 = new HostName[主机名数组.length * 2];
        }
        主机名数组2[0] = new HostName();
        主机名数组2[0].英语讯宝地址 = 发送者讯宝地址;
        主机名数组2[0].主机名 = 主机名;
        if (当前用户.主机名数量 > 0) {
            int 最大长度;
            if (当前用户.主机名数量 < 999) {
                最大长度 = 当前用户.主机名数量;
            } else {
                最大长度 = 999;
            }
            System.arraycopy(主机名数组, 0, 主机名数组2, 1, 最大长度);
        }
        当前用户.主机名数组 = 主机名数组2;
        if (当前用户.主机名数量 < 1000) {
            当前用户.主机名数量 += 1;
        }
        return true;
    }

    private void 数据库_保存主机名2() {
        StringBuffer 字符串合并器 = new StringBuffer(当前用户.主机名数量 * 55);
        HostName 主机名数组[] = 当前用户.主机名数组;
        for (int i = 0; i < 当前用户.主机名数量; i++) {
            字符串合并器.append(主机名数组[i].英语讯宝地址);
            字符串合并器.append("#");
            字符串合并器.append(主机名数组[i].主机名);
            字符串合并器.append(";");
        }
        try {
            byte 字节数组[] = 字符串合并器.toString().getBytes("UTF8");
            SharedMethod.保存文件的全部字节(运行环境.getFilesDir().toString() + "/" + 当前用户.获取英语讯宝地址() + "/hostname.txt", 字节数组);
        } catch (Exception e) {
            说(e.getMessage());
            return;
        }
    }

    private boolean 数据库_保存收到的大聊天群讯宝(String 子域名, long 群编号, String 发送者讯宝地址, long 发送时间, byte 讯宝指令, String 文本, short 宽度, short 高度, byte 秒数) {
        try {
            String 路径 = 运行环境.getFilesDir().toString() + "/" + 当前用户.获取英语讯宝地址();
            File 目录 = new File(路径);
            if (!目录.exists() || !目录.isDirectory()) {
                目录.mkdir();
            }
            File 文件 = new File(路径 + "/" + 子域名 + "#" + 群编号 + ".sscj");
            if (!文件.exists() || 文件.isDirectory()) {
                文件.createNewFile();
            }
            SSPackageCreator SS包生成器 = new SSPackageCreator();
            SS包生成器.添加_有标签("删除", false);
            SS包生成器.添加_有标签("发送者", 发送者讯宝地址);
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
            RandomAccessFile 文件随机访问器 = new RandomAccessFile(文件.getAbsolutePath(), "rwd");
            文件随机访问器.seek(文件随机访问器.length());
            文件随机访问器.write(SS包生成器2.生成字节数组());
            文件随机访问器.close();
            return true;
        } catch (Exception e) {
            说(e.getMessage());
            return false;
        }
    }

    private boolean 数据库_撤回讯宝(String 子域名, long 群编号, long 发送时间) {
        try {
            String 路径 = 运行环境.getFilesDir().toString() + "/" + 当前用户.获取英语讯宝地址();
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
                        long 发送时间2 = (long)SS包读取器.读取_有标签("发送时间");
                        if (发送时间2 == 发送时间) {
                            if (!(boolean)SS包读取器.读取_有标签("删除")) {
                                SharedMethod.标为已删除(文件随机访问器, 文件随机访问器.getFilePointer(), SS包位置, SS包读取器);
                                SharedMethod.删除相关文件(SS包读取器, 运行环境, 当前用户);
                            }
                            return true;
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

    private void 获取连接凭据成功(SSPackageReader SS包解读器) throws Exception {
        Group_Large 大聊天群 = 聊天控件.聊天对象.大聊天群;
        String 子域名 = (String) SS包解读器.读取_有标签("子域名");
        if (!大聊天群.子域名.equals(子域名)) { return; }
        long 群编号 = (long) SS包解读器.读取_有标签("群编号");
        if (大聊天群.编号 != 群编号) { return; }
        大聊天群.名称 = (String) SS包解读器.读取_有标签("群名称");
        大聊天群.图标更新时间 = (long) SS包解读器.读取_有标签("图标更新时间");
        大聊天群.连接凭据 = (String) SS包解读器.读取_有标签("连接凭据");
        大聊天群.我的角色 = (byte) SS包解读器.读取_有标签("角色");
        大聊天群.本国语域名 = (String) SS包解读器.读取_有标签("本国语域名");
        int i = 子域名.indexOf(".");
        大聊天群.主机名 = 子域名.substring(0, i);
        大聊天群.英语域名 = 子域名.substring(i + 1);
        Group_Large 加入的大聊天群[] = 当前用户.加入的大聊天群;
        Group_Large 某一大聊天群;
        for (i = 0; i < 加入的大聊天群.length; i++) {
            某一大聊天群 = 加入的大聊天群[i];
            if (某一大聊天群.编号 != 群编号 && 某一大聊天群.equals(子域名)) {
                if (!SharedMethod.字符串未赋值或为空(某一大聊天群.连接凭据)) {
                    某一大聊天群.连接凭据 = 大聊天群.连接凭据;
                }
            }
        }
        当前用户.保存大聊天群连接凭据(大聊天群);
        说(界面文字.获取(283, "收到新的连接凭据。"));
        if (!已载入最近聊天记录) {
            聊天控件.载入最近聊天记录();
            已载入最近聊天记录 = true;
        }
        if (聊天控件.小宇宙控件 != null) {
            聊天控件.小宇宙控件.网页浏览器.loadUrl(ProtocolPath.获取大聊天群小宇宙的访问路径(聊天控件.聊天对象.大聊天群.子域名));
        }
    }

    private void 添加成员成功() {
        Contact 某一讯友 = 当前用户.查找讯友(任务.获取某步骤的输入值(Task.任务步骤_大聊天群邀请));
        if (某一讯友 == null) { return; }
        Group_Large 大聊天群 = 聊天控件.聊天对象.大聊天群;
        String 文本 = ProtocolFormats.生成文本_邀请加入大聊天群(大聊天群.子域名, 大聊天群.编号, 大聊天群.名称);
        if (SharedMethod.字符串未赋值或为空(文本)) { return; }
        if (主控机器人.数据库_保存要发送的一对一讯宝(某一讯友.英语讯宝地址, SharedMethod.获取当前UTC时间(), ProtocolParameters.讯宝指令_邀请加入大聊天群, 文本, (short) 0, (short) 0, (byte) 0)) {
            主控机器人.发送讯宝(false);
            说(界面文字.获取(85, "已给 #% 发送了邀请。[<a>#%</a>]", new Object[] {(SharedMethod.字符串未赋值或为空(某一讯友.本国语讯宝地址)? "": 某一讯友.本国语讯宝地址 + " / ") + 某一讯友.英语讯宝地址, TaskName.任务名称_邀请}));
            主控机器人.数据库_更新最近互动讯友排名(大聊天群.子域名, 大聊天群.编号);
        }
    }

    private void 群名称修改成功() {
        聊天控件.聊天对象.大聊天群.名称 = 任务.获取某步骤的输入值(Task.任务步骤_大聊天群名称);
        if (当前用户.讯友录当前显示范围 == Constants.讯友录显示范围_聊天群) {
            Fragment_Main.主窗体.刷新讯友录();
        }
        说(界面文字.获取(245, "完成。"));
    }

    private void 修改图标成功(SSPackageReader SS包解读器) throws Exception {
        聊天控件.聊天对象.大聊天群.图标更新时间 = (long) SS包解读器.读取_有标签("图标更新时间");
        说(界面文字.获取(284, "群图标已更新。"));
        if (当前用户.讯友录当前显示范围 == Constants.讯友录显示范围_聊天群) {
            Fragment_Main.主窗体.刷新讯友录();
        }
    }

    private void 退出聊天群成功() {
        Group_Large 大聊天群 = 聊天控件.聊天对象.大聊天群;
        String 文本;
        try {
            SSPackageCreator SS包生成器 = new SSPackageCreator();
            SS包生成器.添加_有标签("英语域名", 大聊天群.英语域名);
            SS包生成器.添加_有标签("主机名", 大聊天群.主机名);
            SS包生成器.添加_有标签("群编号", 大聊天群.编号);
            文本 = SS包生成器.生成纯文本();
        } catch (Exception e) {
            说(e.getMessage());
            return;
        }
        if (主控机器人.数据库_保存要发送的一对一讯宝(当前用户.获取英语讯宝地址(), SharedMethod.获取当前UTC时间(), ProtocolParameters.讯宝指令_退出大聊天群, 文本, (short)0, (short)0, (byte)0)) {
            主控机器人.发送讯宝(false);
            if (当前用户.加入的大聊天群 == null) {
                return;
            }
            String 子域名 = 大聊天群.子域名;
            long 群编号 = 大聊天群.编号;
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
                    System.arraycopy(加入的大聊天群, i + 1, 加入的群2, i, 加入的大聊天群.length - i - 1);
                    加入的大聊天群 = 加入的群2;
                } else {
                    加入的大聊天群 = null;
                }
                当前用户.加入的大聊天群 = 加入的大聊天群;
            }
            if (Fragment_Main.主窗体 != null) {
                Fragment_Main.主窗体.关闭聊天控件(子域名, 群编号);
                Fragment_Main.主窗体.刷新讯友录(Constants.讯友录显示范围_聊天群);
            }
            主控机器人.清除大聊天群数据(子域名, 群编号);
            当前用户.清除大聊天群凭据(大聊天群);
        }
    }


    void 发布流星语(byte[] SS包) {
        任务 = new Task(TaskName.任务名称_发流星语, this);
        说(界面文字.获取(159, "正在发流星语。请稍等。"));
        Group_Large 大聊天群 = 聊天控件.聊天对象.大聊天群;
        启动HTTPS访问线程(new httpSetting(ProtocolPath.获取大聊天群服务器访问路径开头(大聊天群.子域名, false) + "C=PostMeteorRain&EnglishSSAddress=" + 替换URI敏感字符(当前用户.获取英语讯宝地址()) + "&Credential=" + 替换URI敏感字符(大聊天群.连接凭据) + "&GroupID=" + 大聊天群.编号, SS包));
    }

    void 流星语发布结束(boolean 成功) {
        if (成功) {
            说(界面文字.获取(268, "流星语发布成功。"));
            聊天控件.小宇宙控件.发送JS("function(){ PublishSuccessful(); }");
        } else {
            聊天控件.小宇宙控件.发送JS("function(){ PublishFailed(); }");
        }
    }

    void 请求讯宝中心小宇宙分配读取服务器() {
        Group_Large 大聊天群 = 聊天控件.聊天对象.大聊天群;
        httpSetting 访问设置 = new httpSetting(ProtocolPath.获取大聊天群服务器访问路径开头(大聊天群.子域名, false) + "C=GetAServerForRead&EnglishSSAddress=" + 替换URI敏感字符(当前用户.获取英语讯宝地址()) + "&Credential=" + 替换URI敏感字符(大聊天群.连接凭据) + "&GroupID=" + 大聊天群.编号);
        Thread 线程 = new Thread_httpsRequest2(this, 访问设置);
        线程.start();
    }

    private void HTTPS静默访问(httpSetting 访问设置) {
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
                HTTPS连接.setRequestProperty("Content-Type", "text/xml");
                HTTPS连接.setRequestProperty("Content-Length", String.valueOf(0));
                HTTPS连接.connect();
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
                            String 子域名 = null;
                            try {
                                SSPackageReader SS包解读器 = new SSPackageReader(收到的字节数组);
                                if (SS包解读器.获取查询结果() == ProtocolParameters.查询结果_成功) {
                                    子域名 = (String) SS包解读器.读取_有标签("子域名");
                                }
                            } catch (Exception e) {
                            }
                            if (!SharedMethod.字符串未赋值或为空(子域名)) {
                                Bundle 数据盒子 = new Bundle();
                                数据盒子.putString("子域名", 子域名);
                                Message 消息 = new Message();
                                消息.what = 3;
                                消息.setData(数据盒子);
                                跨线程调用器.sendMessage(消息);
                            }
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

    public class Thread_httpsRequest2 extends Thread {

        private Robot_LargeChatGroup 机器人;
        private httpSetting 访问设置;

        public Thread_httpsRequest2(Robot_LargeChatGroup 机器人, httpSetting 访问设置) {
            this.机器人 = 机器人;
            this.访问设置 = 访问设置;
        }

        @Override
        public void run() {
            机器人.HTTPS静默访问(访问设置);
        }

    }

}
