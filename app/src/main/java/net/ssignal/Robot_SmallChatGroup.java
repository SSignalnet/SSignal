package net.ssignal;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Build;

import net.ssignal.protocols.ProtocolFormats;
import net.ssignal.protocols.ProtocolPath;
import net.ssignal.protocols.Contact;
import net.ssignal.protocols.GroupMember;
import net.ssignal.protocols.Group_Small;
import net.ssignal.protocols.ProtocolParameters;
import net.ssignal.protocols.SSPackageCreator;
import net.ssignal.util.Constants;
import net.ssignal.util.MyDate;
import net.ssignal.util.SharedMethod;

import java.io.File;

import static net.ssignal.Activity_Main.主活动;
import static net.ssignal.User.当前用户;
import static net.ssignal.Robot_MainControl.主控机器人;
import static net.ssignal.language.Text.界面文字;
import static net.ssignal.language.Text.组名_任务;

class Robot_SmallChatGroup extends Robot {

    boolean 原图发送;

    Robot_SmallChatGroup(Fragment_Chating 聊天控件) {
        this.聊天控件 = 聊天控件;
        跨线程调用器 = new MyHandler(this);
    }

    @Override
    void 回答(String 用户输入, long 时间) {
        if (聊天控件.聊天对象.小聊天群.群成员 == null) {
            说(界面文字.获取(187, "正在获取成员列表。请稍等。"));
            聊天控件.获取成员列表();
            return;
        }
        if (用户输入.equalsIgnoreCase(TaskName.任务名称_小宇宙)) {
            打开小宇宙页面();
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
        } else if (用户输入.equalsIgnoreCase(TaskName.任务名称_删减成员)) {
            删减成员(用户输入);
        } else if (用户输入.equalsIgnoreCase(TaskName.任务名称_群名称)) {
            群名称(用户输入);
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
//            当前用户.记录运行步骤("运行到：4");
            任务接收用户输入(用户输入, 时间);
        }
    }

    private void 打开小宇宙页面() {
        说(界面文字.获取(7, "请稍等。"));
        if (聊天控件.小宇宙控件 != null) {
            聊天控件.小宇宙控件.网页载入完毕 = false;
            聊天控件.小宇宙控件.网页浏览器.loadUrl(ProtocolPath.获取讯友小宇宙的访问路径(聊天控件.聊天对象.讯友或群主.英语讯宝地址, 当前用户.获取英语讯宝地址()));
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
        String 文件路径, 英语讯宝地址;
        int 宽度, 高度;
        英语讯宝地址 = 聊天控件.聊天对象.讯友或群主.英语讯宝地址;
        byte 群编号 = 聊天控件.聊天对象.小聊天群.编号;
        long 当前UTC时刻 = SharedMethod.获取当前UTC时间();
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
                聊天控件.发送JS("function(){var imgsrc = \"" + SharedMethod.处理文件路径以用作JS函数参数(文件路径) + "\"; var iconsrc =\"" + ProtocolPath.获取我的头像路径(当前用户.英语用户名, 当前用户.主机名, 当前用户.头像更新时间, 当前用户.域名_英语) + "\"; var time = \"" + 聊天控件.时间格式(new MyDate(当前UTC时刻)) + "\"; SSout_Img(\"" + 当前UTC时刻 + "\", imgsrc, \"" + 宽度 + "\", \"" + 高度 + "\", iconsrc, time); }");
            } catch (Exception e) {
                说(e.getMessage());
                return;
            }
            if (主控机器人.数据库_保存要发送的小聊天群讯宝(英语讯宝地址, 群编号, 当前UTC时刻, ProtocolParameters.讯宝指令_发送图片, 文件路径, (short)宽度, (short)高度, (byte)0)) {
                主控机器人.发送讯宝(false);
            }
            当前UTC时刻 += 1;
        }
        if (主控机器人.数据库_更新最近互动讯友排名(英语讯宝地址, 群编号)) {
            if (当前用户.讯友录当前显示范围 == Constants.讯友录显示范围_最近) {
                Fragment_Main.主窗体.刷新讯友录();
            }
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
        String 文件路径, 英语讯宝地址;
        int 宽度, 高度;
        英语讯宝地址 = 聊天控件.聊天对象.讯友或群主.英语讯宝地址;
        byte 群编号 = 聊天控件.聊天对象.小聊天群.编号;
        long 当前UTC时刻 = SharedMethod.获取当前UTC时间();
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
            聊天控件.发送JS("function(){var imgsrc = \"" + SharedMethod.处理文件路径以用作JS函数参数(文件路径) + "\"; var iconsrc =\"" + ProtocolPath.获取我的头像路径(当前用户.英语用户名, 当前用户.主机名, 当前用户.头像更新时间, 当前用户.域名_英语) + "\"; var time = \"" + 聊天控件.时间格式(new MyDate(当前UTC时刻)) + "\"; SSout_Img(\"" + 当前UTC时刻 + "\", imgsrc, \"" + 宽度 + "\", \"" + 高度 + "\", iconsrc, time); }");
        } catch (Exception e) {
            说(e.getMessage());
            return;
        }
        if (主控机器人.数据库_保存要发送的小聊天群讯宝(英语讯宝地址, 群编号, 当前UTC时刻, ProtocolParameters.讯宝指令_发送图片, 文件路径, (short)宽度, (short)高度, (byte)0)) {
            主控机器人.发送讯宝(false);
        }
        if (主控机器人.数据库_更新最近互动讯友排名(英语讯宝地址, 群编号)) {
            if (当前用户.讯友录当前显示范围 == Constants.讯友录显示范围_最近) {
                Fragment_Main.主窗体.刷新讯友录();
            }
        }
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
        聊天控件.发送JS("function(){var videosrc = \"" + SharedMethod.处理文件路径以用作JS函数参数(视频文件路径) + "\"; var iconsrc =\"" + ProtocolPath.获取我的头像路径(当前用户.英语用户名, 当前用户.主机名, 当前用户.头像更新时间, 当前用户.域名_英语) + "\"; var time = \"" + 聊天控件.时间格式(new MyDate(当前UTC时刻)) + "\"; SSout_Video(\"" + 当前UTC时刻 + "\", videosrc, \"" + 预览图片宽度 + "\", \"" + 预览图片高度 + "\", iconsrc, time); }");
        String 英语讯宝地址 = 聊天控件.聊天对象.讯友或群主.英语讯宝地址;
        byte 群编号 = 聊天控件.聊天对象.小聊天群.编号;
        if (主控机器人.数据库_保存要发送的小聊天群讯宝(英语讯宝地址, 群编号, 当前UTC时刻, ProtocolParameters.讯宝指令_发送短视频, 视频文件路径, 预览图片宽度, 预览图片高度, (byte)0)) {
            主控机器人.发送讯宝(false);
        }
        if (主控机器人.数据库_更新最近互动讯友排名(英语讯宝地址, 群编号)) {
            if (当前用户.讯友录当前显示范围 == Constants.讯友录显示范围_最近) {
                Fragment_Main.主窗体.刷新讯友录();
            }
        }
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
        try {
            聊天控件.发送JS("function(){var text = \"" + SharedMethod.处理文件路径以用作JS函数参数(SharedMethod.获取文件名(文件路径)) + "\"; var filesrc = \"" + SharedMethod.处理文件路径以用作JS函数参数(文件路径) + "\"; var iconsrc =\"" + ProtocolPath.获取我的头像路径(当前用户.英语用户名, 当前用户.主机名, 当前用户.头像更新时间, 当前用户.域名_英语) + "\"; var time = \"" + 聊天控件.时间格式(new MyDate(当前UTC时刻)) + "\"; SSout_File(\"" + 当前UTC时刻 + "\", text, filesrc, iconsrc, time); }");
        } catch (Exception e) {
            说(e.getMessage());
            return;
        }
        if (主控机器人.数据库_保存要发送的小聊天群讯宝(聊天控件.聊天对象.讯友或群主.英语讯宝地址, 聊天控件.聊天对象.小聊天群.编号, 当前UTC时刻, ProtocolParameters.讯宝指令_发送文件, 文件路径, (short)0, (short)0, (byte)0)) {
            主控机器人.发送讯宝(false);
        }
        if (主控机器人.数据库_更新最近互动讯友排名(聊天控件.聊天对象.讯友或群主.英语讯宝地址, 聊天控件.聊天对象.小聊天群.编号)) {
            if (当前用户.讯友录当前显示范围 == Constants.讯友录显示范围_最近) {
                Fragment_Main.主窗体.刷新讯友录();
            }
        }
    }

    private void 邀请(String 用户输入) {
        if (任务 != null) { 任务.结束(); }
        if (聊天控件.聊天对象.小聊天群.群成员.length >= ProtocolParameters.最大值_小聊天群成员数量) {
            说(界面文字.获取(171, "群成员数量已达上限。"));
            return;
        }
        当前用户.显示讯友临时编号 = true;
        Fragment_Main.主窗体.刷新讯友录(Constants.讯友录显示范围_讯友);
        任务 = new Task(用户输入, 输入框, this);
        任务.添加步骤(Task.任务步骤_小聊天群邀请, 界面文字.获取(82, "请输入讯友的讯宝地址或临时编号（讯友备注行括号内的数字）。"));
        说(任务.获取当前步骤提示语());
    }

    private void 退出聊天群(String 用户输入) {
        if (任务 != null) { 任务.结束(); }
        任务 = new Task(用户输入, this);
        说(界面文字.获取(176, "你要退出此聊天群吗？请选择<a>#%</a>或者<a>#%</a>。", new Object[] {界面文字.获取(组名_任务, 0, "是"), 界面文字.获取(组名_任务, 1, "否")}));
    }

    private boolean 退出聊天群2(String 用户输入) {
        if (用户输入.equalsIgnoreCase(界面文字.获取(组名_任务, 0, "是"))) {
            if (主控机器人.数据库_保存要发送的小聊天群讯宝(聊天控件.聊天对象.讯友或群主.英语讯宝地址, 聊天控件.聊天对象.小聊天群.编号, SharedMethod.获取当前UTC时间(), ProtocolParameters.讯宝指令_退出小聊天群, null, (short) 0, (short) 0, (byte) 0)) {
                当前用户.清除小聊天群成员列表(聊天控件.聊天对象.小聊天群);
                说(界面文字.获取(7, "请稍等。"));
                主控机器人.发送讯宝(false);
            }
            return true;
        } else if (用户输入.equalsIgnoreCase(界面文字.获取(组名_任务, 1, "否"))) {
            回答(TaskName.任务名称_取消, 0);
            return true;
        }
        return false;
    }

    private void 删减成员(String 用户输入) {
        if (任务 != null) { 任务.结束(); }
        if (聊天控件.聊天对象.小聊天群.群成员.length == 1) {
            说(界面文字.获取(170, "除你之外，聊天群里没有其他人。"));
            return;
        }
        任务 = new Task(用户输入, 输入框, this);
        任务.添加步骤(Task.任务步骤_小聊天群删减成员, 界面文字.获取(180, "请输入成员的讯宝地址或临时编号（成员备注行括号内的数字）。"));
        说(任务.获取当前步骤提示语());
    }

    private void 群名称(String 用户输入) {
        if (任务 != null) { 任务.结束(); }
        任务 = new Task(用户输入, 输入框, this);
        任务.添加步骤(Task.任务步骤_小聊天群名称, 界面文字.获取(183, "请为输入群的新名称。（不超过#%个字符）", new Object[]{ProtocolParameters.最大值_群名称字符数}));
        说(任务.获取当前步骤提示语());
    }

    private void 解散聊天群(String 用户输入) {
        if (任务 != null) { 任务.结束(); }
        if (聊天控件.聊天对象.小聊天群.群成员.length > 1) {
            说(界面文字.获取(182, "无法解散还有成员的群。"));
            return;
        }
        任务 = new Task(用户输入, this);
        说(界面文字.获取(181, "你要解散此聊天群吗？请选择<a>#%</a>或者<a>#%</a>。", new Object[] {界面文字.获取(组名_任务, 0, "是"), 界面文字.获取(组名_任务, 1, "否")}));
    }

    private boolean 解散聊天群2(String 用户输入) {
        if (用户输入.equalsIgnoreCase(界面文字.获取(组名_任务, 0, "是"))) {
            if (主控机器人.数据库_保存要发送的小聊天群讯宝(聊天控件.聊天对象.讯友或群主.英语讯宝地址, 聊天控件.聊天对象.小聊天群.编号, SharedMethod.获取当前UTC时间(), ProtocolParameters.讯宝指令_解散小聊天群, null, (short) 0, (short) 0, (byte) 0)) {
                当前用户.清除小聊天群成员列表(聊天控件.聊天对象.小聊天群);
                说(界面文字.获取(7, "请稍等。"));
                主控机器人.发送讯宝(false);
            }
            return true;
        } else if (用户输入.equalsIgnoreCase(界面文字.获取(组名_任务, 1, "否"))) {
            回答(TaskName.任务名称_取消, 0);
            return true;
        }
        return false;
    }

    private void 任务接收用户输入(String 用户输入, long 时间) {
        Contact 群主 = 聊天控件.聊天对象.讯友或群主;
        Group_Small 群 = 聊天控件.聊天对象.小聊天群;
        if (任务 != null) {
            if (任务.步骤数量 > 0) {
//                当前用户.记录运行步骤("运行到：e1");
                String 结果 = 任务.保存当前步骤输入值(用户输入);
                if (SharedMethod.字符串未赋值或为空(结果)) {
                    结果 = 任务.获取当前步骤提示语();
                    if (!SharedMethod.字符串未赋值或为空(结果)) {
                        说(结果);
                    } else {
                        if (任务.名称.equals(TaskName.任务名称_邀请)) {
                            Contact 某一讯友 = 当前用户.查找讯友(任务.获取某步骤的输入值(Task.任务步骤_小聊天群邀请));
                            SSPackageCreator SS包生成器 = new SSPackageCreator();
                            String 文本 = ProtocolFormats.生成文本_邀请加入小聊天群(群.编号, 群.备注);
                            if (SharedMethod.字符串未赋值或为空(文本)) { return; }
                            if (主控机器人.数据库_保存要发送的一对一讯宝(某一讯友.英语讯宝地址, 时间, ProtocolParameters.讯宝指令_邀请加入小聊天群, 文本, (short)0, (short)0, (byte)0)) {
                                任务.结束();
                                说(界面文字.获取(85, "已给 #% 发送了邀请。[<a>#%</a>]", new Object[]{(SharedMethod.字符串未赋值或为空(某一讯友.本国语讯宝地址) ? "" : 某一讯友.本国语讯宝地址 + " / ") + 某一讯友.英语讯宝地址, TaskName.任务名称_邀请}));
                                GroupMember 新成员 = new GroupMember();
                                新成员.英语讯宝地址 = 某一讯友.英语讯宝地址;
                                新成员.本国语讯宝地址 = 某一讯友.本国语讯宝地址;
                                新成员.主机名 = 某一讯友.主机名;
                                新成员.位置号 = 某一讯友.位置号;
                                新成员.角色 = ProtocolParameters.群角色_邀请加入_可以发言;
                                新成员.所属的群 = 群;
                                GroupMember 群成员[] = 群.群成员;
                                GroupMember 群成员2[] = new GroupMember[群成员.length + 1];
                                System.arraycopy(群成员, 0, 群成员2, 0, 群成员.length);
                                群成员 = 群成员2;
                                群成员[群成员.length - 1] = 新成员;
                                byte k = 1;
                                for (int i = 0; i < 群成员.length; i++) {
                                    if (群成员[i].角色 != ProtocolParameters.群角色_群主) {
                                        群成员[i].临时编号 = k;
                                        k += 1;
                                    }
                                }
                                群.群成员 = 群成员;
                                主控机器人.数据库_更新最近互动讯友排名(群主.英语讯宝地址, 群.编号);
                                主控机器人.发送讯宝(false);
                            }
                        } else if (任务.名称.equals(TaskName.任务名称_删减成员)) {
                            if (主控机器人.数据库_保存要发送的小聊天群讯宝( 群主.英语讯宝地址, 群.编号, SharedMethod.获取当前UTC时间(), ProtocolParameters.讯宝指令_删减聊天群成员, 任务.获取某步骤的输入值(Task.任务步骤_小聊天群删减成员), (short)0, (short)0, (byte)0)) {
                                任务.结束();
                                说(界面文字.获取(7, "请稍等。"));
                                if (主控机器人.数据库_更新最近互动讯友排名(群主.英语讯宝地址, 群.编号)) {
                                    if (当前用户.讯友录当前显示范围 == Constants.讯友录显示范围_最近) {
                                        Fragment_Main.主窗体.刷新讯友录();
                                    }
                                }
                                主控机器人.发送讯宝(false);
                            }
                        } else if (任务.名称.equals(TaskName.任务名称_群名称)) {
                            String 群的新名称 = 任务.获取某步骤的输入值(Task.任务步骤_小聊天群名称);
                            if (主控机器人.数据库_保存要发送的小聊天群讯宝( 群主.英语讯宝地址, 群.编号, SharedMethod.获取当前UTC时间(), ProtocolParameters.讯宝指令_修改聊天群名称, 群的新名称, (short)0, (short)0, (byte)0)) {
                                任务.结束();
                                Group_Small 加入的群[] = 当前用户.加入的小聊天群;
                                if (加入的群 != null) {
                                    String 群主英语讯宝地址 = 群主.英语讯宝地址;
                                    byte 群编号 = 群.编号;
                                    for (int i = 0; i < 加入的群.length; i++) {
                                        if (群编号 == 加入的群[i].编号 && 群主英语讯宝地址.equals(加入的群[i].群主.英语讯宝地址)) {
                                            加入的群[i].备注 = 群的新名称;
                                            Fragment_Main.主窗体.刷新讯友录();
                                        }
                                    }
                                }
                                if (主控机器人.数据库_更新最近互动讯友排名(群主.英语讯宝地址, 群.编号)) {
                                    if (当前用户.讯友录当前显示范围 == Constants.讯友录显示范围_最近) {
                                        Fragment_Main.主窗体.刷新讯友录();
                                    }
                                }
                                主控机器人.发送讯宝(false);
                            }
                        }
                    }
                } else {
                    说(结果);
                }
                return;
            }
        }
//        当前用户.记录运行步骤("运行到：5");
        if (主控机器人.数据库_保存要发送的小聊天群讯宝( 群主.英语讯宝地址, 群.编号, 时间, ProtocolParameters.讯宝指令_发送文字, 用户输入, (short)0, (short)0, (byte)0)) {
//            当前用户.记录运行步骤("运行到：6");
            if (主控机器人.数据库_更新最近互动讯友排名(群主.英语讯宝地址, 群.编号)) {
                if (当前用户.讯友录当前显示范围 == Constants.讯友录显示范围_最近) {
                    Fragment_Main.主窗体.刷新讯友录();
                }
            }
            主控机器人.发送讯宝(false);
        }
    }

}
