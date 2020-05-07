package net.ssignal;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Build;
import android.os.Message;

import net.ssignal.protocols.Group_Large;
import net.ssignal.protocols.ProtocolPath;
import net.ssignal.protocols.Contact;
import net.ssignal.protocols.ProtocolParameters;
import net.ssignal.protocols.SSPackageCreator;
import net.ssignal.protocols.SSPackageReader;
import net.ssignal.structure.ChatWith;
import net.ssignal.util.Constants;
import net.ssignal.util.MyDate;
import net.ssignal.util.SharedMethod;

import java.io.File;
import java.util.Arrays;

import static net.ssignal.Activity_Main.主活动;
import static net.ssignal.User.当前用户;
import static net.ssignal.Robot_MainControl.主控机器人;
import static net.ssignal.language.Text.界面文字;
import static net.ssignal.language.Text.组名_任务;

public class Robot_OneOnOne extends Robot {

    boolean 原图发送;

    Robot_OneOnOne(Fragment_Chating 聊天控件) {
        this.聊天控件 = 聊天控件;
        跨线程调用器 = new MyHandler(this);
    }

    @Override
    void 回答(String 用户输入, long 时间) {
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
        } else if (用户输入.equalsIgnoreCase(TaskName.任务名称_添加新标签)) {
            添加新标签(用户输入);
        } else if (用户输入.equalsIgnoreCase(TaskName.任务名称_添加现有标签)) {
            添加现有标签(用户输入);
        } else if (用户输入.equalsIgnoreCase(TaskName.任务名称_移除标签)) {
            移除标签(用户输入);
        } else if (用户输入.equalsIgnoreCase(TaskName.任务名称_备注)) {
            修改备注(用户输入);
        } else if (用户输入.equalsIgnoreCase(TaskName.任务名称_拉黑)) {
            拉黑(用户输入);
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
                if (任务.名称.equalsIgnoreCase(TaskName.任务名称_拉黑)) {
                    if (拉黑2(用户输入)) { return; }
                }
            }
            任务接收用户输入(用户输入, 时间);
        }
    }

    private void 打开小宇宙页面() {
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

    private void 列出讯友当前标签() {
        String 当前标签 = null;
        Contact 当前讯友 = 聊天控件.聊天对象.讯友或群主;
        if (!SharedMethod.字符串未赋值或为空(当前讯友.标签一)) {
            当前标签 = 当前讯友.标签一;
        }
        if (!SharedMethod.字符串未赋值或为空(当前讯友.标签二)) {
            if (SharedMethod.字符串未赋值或为空(当前标签)) {
                当前标签 = 当前讯友.标签二;
            } else {
                当前标签 += ", " + 当前讯友.标签二;
            }
        }
        if (!SharedMethod.字符串未赋值或为空(当前标签)) {
            说(界面文字.获取(129, "目前的标签：#%", new Object[] {当前标签}));
        } else {
            说(界面文字.获取(88, "目前没有标签"));
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
        if (聊天控件.聊天对象.讯友或群主.拉黑) {
            说(界面文字.获取(160, "你已将此讯友拉黑。"));
            return;
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
        String 英语讯宝地址 = 聊天控件.聊天对象.讯友或群主.英语讯宝地址;
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
            if (主控机器人.数据库_保存要发送的一对一讯宝(英语讯宝地址, 当前UTC时刻, ProtocolParameters.讯宝指令_发送图片, 文件路径, (short)宽度, (short)高度, (byte)0)) {
                主控机器人.发送讯宝(false);
            }
            当前UTC时刻 += 1;
        }
        if (主控机器人.数据库_更新最近互动讯友排名(英语讯宝地址, (byte) 0)) {
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
        if (聊天控件.聊天对象.讯友或群主.拉黑) {
            说(界面文字.获取(160, "你已将此讯友拉黑。"));
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
        String 英语讯宝地址 = 聊天控件.聊天对象.讯友或群主.英语讯宝地址;
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
        if (主控机器人.数据库_保存要发送的一对一讯宝(英语讯宝地址, 当前UTC时刻, ProtocolParameters.讯宝指令_发送图片, 文件路径, (short)宽度, (short)高度, (byte)0)) {
            主控机器人.发送讯宝(false);
        }
        if (主控机器人.数据库_更新最近互动讯友排名(英语讯宝地址, (byte) 0)) {
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
        if (聊天控件.聊天对象.讯友或群主.拉黑) {
            说(界面文字.获取(160, "你已将此讯友拉黑。"));
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
        if (主控机器人.数据库_保存要发送的一对一讯宝(英语讯宝地址, 当前UTC时刻, ProtocolParameters.讯宝指令_发送短视频, 视频文件路径, 预览图片宽度, 预览图片高度, (byte)0)) {
            主控机器人.发送讯宝(false);
        }
        if (主控机器人.数据库_更新最近互动讯友排名(英语讯宝地址, (byte) 0)) {
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
        if (聊天控件.聊天对象.讯友或群主.拉黑) {
            说(界面文字.获取(160, "你已将此讯友拉黑。"));
            return;
        }
        说(界面文字.获取(305, "请选择一个文件。"));
        主活动.选择文件(this);
    }

    void 发送文件2(String 文件路径) {
        File 文件 = new File(文件路径);
        boolean 存在 = 文件.exists();
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
        if (主控机器人.数据库_保存要发送的一对一讯宝(聊天控件.聊天对象.讯友或群主.英语讯宝地址, 当前UTC时刻, ProtocolParameters.讯宝指令_发送文件, 文件路径, (short)0, (short)0, (byte)0)) {
            主控机器人.发送讯宝(false);
        }
        if (主控机器人.数据库_更新最近互动讯友排名(聊天控件.聊天对象.讯友或群主.英语讯宝地址, (byte) 0)) {
            if (当前用户.讯友录当前显示范围 == Constants.讯友录显示范围_最近) {
                Fragment_Main.主窗体.刷新讯友录();
            }
        }
    }

    private void 添加新标签(String 用户输入) {
        if (任务 != null) { 任务.结束(); }
        Contact 当前讯友 = 聊天控件.聊天对象.讯友或群主;
        if (当前讯友.拉黑) {
            说(界面文字.获取(160, "你已将此讯友拉黑。"));
            return;
        }
        列出讯友当前标签();
        if (!SharedMethod.字符串未赋值或为空(当前讯友.标签一) && !SharedMethod.字符串未赋值或为空(当前讯友.标签二)) {
            说(界面文字.获取(134, "无法添加更多标签。每个讯友最多可以添加两个标签。"));
            return;
        }
        任务 = new Task(用户输入, 输入框, this);
        任务.添加步骤(Task.任务步骤_添加新标签, 界面文字.获取(130, "你可以用标签对讯友进行分类。请输入一个新的标签名称，如同学、市场部等。（不超过#%个字符）", new Object[] {ProtocolParameters.最大值_讯友标签字符数}));
        说(任务.获取当前步骤提示语());
    }

    private void 添加现有标签(String 用户输入) {
        if (任务 != null) { 任务.结束(); }
        Contact 当前讯友 = 聊天控件.聊天对象.讯友或群主;
        if (当前讯友.拉黑) {
            说(界面文字.获取(160, "你已将此讯友拉黑。"));
            return;
        }
        列出讯友当前标签();
        if (!SharedMethod.字符串未赋值或为空(当前讯友.标签一) && !SharedMethod.字符串未赋值或为空(当前讯友.标签二)) {
            说(界面文字.获取(134, "无法添加更多标签。每个讯友最多可以添加两个标签。"));
            return;
        }
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
                    if (!讯友标签[i].equals(当前讯友.标签一) && !讯友标签[i].equalsIgnoreCase(当前讯友.标签二)) {
                        if (i > 0) {
                            字符串合并器.append(", ");
                        }
                        字符串合并器.append("<a>" + 讯友标签[i] + "</a>");
                    }
                }
                if (字符串合并器.length() > 0) {
                    任务 = new Task(用户输入, 输入框, this);
                    任务.添加步骤(Task.任务步骤_添加现有标签, 界面文字.获取(138, "请选择现有标签：#%", new Object[] {字符串合并器.toString()}));
                    说(任务.获取当前步骤提示语());
                    return;
                }
            }
        }
        说(界面文字.获取(137, "没有可选的标签。"));
    }

    private void 移除标签(String 用户输入) {
        if (任务 != null) { 任务.结束(); }
        Contact 当前讯友 = 聊天控件.聊天对象.讯友或群主;
        if (当前讯友.拉黑) {
            说(界面文字.获取(160, "你已将此讯友拉黑。"));
            return;
        }
        列出讯友当前标签();
        if (SharedMethod.字符串未赋值或为空(当前讯友.标签一) && SharedMethod.字符串未赋值或为空(当前讯友.标签二)) {
            return;
        }
        任务 = new Task(用户输入, 输入框, this);
        任务.添加步骤(Task.任务步骤_移除标签, 界面文字.获取(140, "请输入要移除的标签名称。"));
        说(任务.获取当前步骤提示语());
    }

    private void 修改备注(String 用户输入) {
        if (任务 != null) { 任务.结束(); }
        if (聊天控件.聊天对象.讯友或群主.拉黑) {
            说(界面文字.获取(160, "你已将此讯友拉黑。"));
            return;
        }
        说(界面文字.获取(152, "目前的备注：#%", new Object[] {聊天控件.聊天对象.讯友或群主.备注}));
        任务 = new Task(用户输入, 输入框, this);
        任务.添加步骤(Task.任务步骤_修改讯友备注, 界面文字.获取(151, "请输入新的备注。（不超过#%个字符）", new Object[] {ProtocolParameters.最大值_讯友备注字符数}));
        说(任务.获取当前步骤提示语());
    }

    private void 拉黑(String 用户输入) {
        if (任务 != null) { 任务.结束(); }
        if (聊天控件.聊天对象.讯友或群主.拉黑) {
            说(界面文字.获取(160, "你已将此讯友拉黑。"));
            return;
        }
        任务 = new Task(用户输入, this);
        说(界面文字.获取(124, "你要添加此讯友至黑名单吗？请选择<a>#%</a>或者<a>#%</a>。", new Object[] {界面文字.获取(组名_任务, 0, "是"), 界面文字.获取(组名_任务, 1, "否")}));
    }

    private boolean 拉黑2(String 用户输入) {
        if (用户输入.equalsIgnoreCase(界面文字.获取(组名_任务, 0, "是"))) {
            SSPackageCreator SS包生成器 = new SSPackageCreator();
            try {
                SS包生成器.添加_有标签("英语讯宝地址", 聊天控件.聊天对象.讯友或群主.英语讯宝地址);
                SS包生成器.添加_有标签("拉黑", true);
                if (!主控机器人.数据库_保存要发送的一对一讯宝(当前用户.获取英语讯宝地址(), SharedMethod.获取当前UTC时间(), ProtocolParameters.讯宝指令_拉黑取消拉黑讯友, SS包生成器.生成纯文本(), (short) 0, (short) 0, (byte) 0)) {
                    return true;
                }
            } catch (Exception e) {
                return true;
            }
            主控机器人.发送讯宝(false);
            聊天控件.聊天对象.讯友或群主.拉黑 = true;
            switch (当前用户.讯友录当前显示范围) {
                case Constants.讯友录显示范围_最近:
                case Constants.讯友录显示范围_讯友:
                case Constants.讯友录显示范围_某标签:
                case Constants.讯友录显示范围_黑名单:
                    Fragment_Main.主窗体.刷新讯友录();
            }
            说(界面文字.获取(160, "你已将此讯友拉黑。"));
            return true;
        } else if (用户输入.equalsIgnoreCase(界面文字.获取(组名_任务, 1, "否"))) {
            回答(TaskName.任务名称_取消, 0);
            return true;
        }
        return false;
    }

    private void 任务接收用户输入(String 用户输入, long 时间) {
        if (任务 != null) {
            if (任务.步骤数量 > 0) {
                String 结果 = 任务.保存当前步骤输入值(用户输入);
                if (SharedMethod.字符串未赋值或为空(结果)) {
                    结果 = 任务.获取当前步骤提示语();
                    if (!SharedMethod.字符串未赋值或为空(结果)) {
                        说(结果);
                    } else {
                        if (任务.名称.equalsIgnoreCase(TaskName.任务名称_备注)) {
                            String 备注 = 任务.获取某步骤的输入值(Task.任务步骤_修改讯友备注);
                            SSPackageCreator SS包生成器 = new SSPackageCreator();
                            try {
                                SS包生成器.添加_有标签("英语讯宝地址", 聊天控件.聊天对象.讯友或群主.英语讯宝地址);
                                SS包生成器.添加_有标签("备注", 备注);
                                if (!主控机器人.数据库_保存要发送的一对一讯宝(当前用户.获取英语讯宝地址(), SharedMethod.获取当前UTC时间(), ProtocolParameters.讯宝指令_修改讯友备注, SS包生成器.生成纯文本(), (short) 0, (short) 0, (byte) 0)) {
                                    return;
                                }
                            } catch (Exception e) {
                                return;
                            }
                            主控机器人.发送讯宝(false);
                            任务.结束();
                        } else if (任务.名称.equalsIgnoreCase(TaskName.任务名称_添加新标签) || 任务.名称.equalsIgnoreCase(TaskName.任务名称_添加现有标签)) {
                            String 标签名称;
                            if (任务.名称.equalsIgnoreCase(TaskName.任务名称_添加新标签)) {
                                标签名称 = 任务.获取某步骤的输入值(Task.任务步骤_添加新标签);
                            } else {
                                标签名称 = 任务.获取某步骤的输入值(Task.任务步骤_添加现有标签);
                            }
                            SSPackageCreator SS包生成器 = new SSPackageCreator();
                            try {
                                SS包生成器.添加_有标签("英语讯宝地址", 聊天控件.聊天对象.讯友或群主.英语讯宝地址);
                                SS包生成器.添加_有标签("标签名称", 标签名称);
                                if (!主控机器人.数据库_保存要发送的一对一讯宝(当前用户.获取英语讯宝地址(), SharedMethod.获取当前UTC时间(), ProtocolParameters.讯宝指令_给讯友添加标签, SS包生成器.生成纯文本(), (short) 0, (short) 0, (byte) 0)) {
                                    return;
                                }
                            } catch (Exception e) {
                                return;
                            }
                            主控机器人.发送讯宝(false);
                            任务.结束();
                        } else if (任务.名称.equalsIgnoreCase(TaskName.任务名称_移除标签)) {
                            String 标签名称 = 任务.获取某步骤的输入值(Task.任务步骤_移除标签);
                            SSPackageCreator SS包生成器 = new SSPackageCreator();
                            try {
                                SS包生成器.添加_有标签("英语讯宝地址", 聊天控件.聊天对象.讯友或群主.英语讯宝地址);
                                SS包生成器.添加_有标签("标签名称", 标签名称);
                                if (!主控机器人.数据库_保存要发送的一对一讯宝(当前用户.获取英语讯宝地址(), SharedMethod.获取当前UTC时间(), ProtocolParameters.讯宝指令_移除讯友标签, SS包生成器.生成纯文本(), (short) 0, (short) 0, (byte) 0)) {
                                    return;
                                }
                            } catch (Exception e) {
                                return;
                            }
                            主控机器人.发送讯宝(false);
                            任务.结束();
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
        Contact 当前讯友 = 聊天控件.聊天对象.讯友或群主;
        if (当前讯友.拉黑) {
            说(界面文字.获取(160, "你已将此讯友拉黑。"));
            return;
        }
        if (主控机器人.数据库_保存要发送的一对一讯宝( 当前讯友.英语讯宝地址, 时间, ProtocolParameters.讯宝指令_发送文字, 用户输入, (short)0, (short)0, (byte)0)) {
            if (主控机器人.数据库_更新最近互动讯友排名(当前讯友.英语讯宝地址, (byte) 0)) {
                if (当前用户.讯友录当前显示范围 == Constants.讯友录显示范围_最近) {
                    Fragment_Main.主窗体.刷新讯友录();
                }
            }
            主控机器人.发送讯宝(false);
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
                        任务.结束();
                        任务 = null;
                    }
                }
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
                        if (任务.名称.equalsIgnoreCase(TaskName.任务名称_加入大聊天群)) {
                            加入大聊天群成功(SS包解读器);
                        }
                        break;
                    case ProtocolParameters.查询结果_某标签讯友数满了:
                        说(界面文字.获取(135, "失败，因为每个标签最多只能标记 #% 个讯友。", new Object[]{ProtocolParameters.最大值_每个标签讯友数量}));
                        break;
                    case ProtocolParameters.查询结果_不是群成员:
                        说(界面文字.获取(83, "你不是当前聊天群的成员。"));
                        break;
                    case ProtocolParameters.查询结果_稍后重试:
                        说(界面文字.获取(20, "你的操作过于频繁，请#%分钟后再尝试。", new Object[] {Constants.最近操作次数统计时间_分钟}));
                        break;
                    case ProtocolParameters.查询结果_凭据无效:
                        说(界面文字.获取(229, "请注销，然后重新登录。"));
                        break;
                    case ProtocolParameters.查询结果_账号停用:
                        说(界面文字.获取(15, "账号已停用。"));
                        break;
                    case ProtocolParameters.查询结果_系统维护:
                        说(界面文字.获取(14, "由于服务器正在维护中，暂停服务。"));
                        break;
                    case ProtocolParameters.查询结果_出错:
                        说(界面文字.获取(108, "出错 #%", new Object[] {SS包解读器.获取出错提示文本()}));
                        break;
                    case ProtocolParameters.查询结果_失败:
                        说(界面文字.获取(148, "由于未知原因，操作失败。"));
                        break;
                    case ProtocolParameters.查询结果_数据库未就绪:
                        说(界面文字.获取(141, "数据库未就绪。"));
                        break;
                    case ProtocolParameters.查询结果_服务器未就绪:
                        说(界面文字.获取(269, "服务器还未就绪。请稍后重试。"));
                        break;
                    default :
                        说(界面文字.获取(108, "出错 #%", new Object[] {SS包解读器.获取查询结果()}));
                }
            } catch (Exception e) {
                说(e.getMessage());
            }
        }
        任务.结束();
        任务 = null;
    }

    void 添加标签成功(SSPackageReader SS包解读器) throws Exception {
        String 标签名称 = (String) SS包解读器.读取_有标签("标签名称");
        long 讯友录更新时间 = (long) SS包解读器.读取_有标签("时间");
        Contact 当前讯友 = 聊天控件.聊天对象.讯友或群主;
        if (SharedMethod.字符串未赋值或为空(当前讯友.标签一)) {
            当前讯友.标签一 = 标签名称;
        } else if (SharedMethod.字符串未赋值或为空(当前讯友.标签二)) {
            当前讯友.标签二 = 标签名称;
        }
        if (讯友录更新时间 > 0) { 当前用户.讯友录有变动(讯友录更新时间); }
        列出讯友当前标签();
    }

    void 移除标签成功(SSPackageReader SS包解读器) throws Exception {
        String 标签名称 = (String) SS包解读器.读取_有标签("标签名称");
        long 讯友录更新时间 = (long) SS包解读器.读取_有标签("时间");
        Contact 当前讯友 = 聊天控件.聊天对象.讯友或群主;
        if (标签名称.equalsIgnoreCase(当前讯友.标签一)) {
            当前讯友.标签一 = null;
        }if (标签名称.equalsIgnoreCase(当前讯友.标签二)) {
            当前讯友.标签二 = null;
        }
        if (讯友录更新时间 > 0) { 当前用户.讯友录有变动(讯友录更新时间); }
        列出讯友当前标签();
    }

    void 修改备注成功(SSPackageReader SS包解读器) throws Exception {
        聊天控件.聊天对象.讯友或群主.备注 = (String)SS包解读器.读取_有标签("备注");
        long 讯友录更新时间 = (long) SS包解读器.读取_有标签("时间");
        if (讯友录更新时间 > 0) { 当前用户.讯友录有变动(讯友录更新时间); }
        switch (当前用户.讯友录当前显示范围) {
            case Constants.讯友录显示范围_最近:
            case Constants.讯友录显示范围_讯友:
            case Constants.讯友录显示范围_某标签:
            case Constants.讯友录显示范围_黑名单:
                Fragment_Main.主窗体.刷新讯友录();
        }
        说(界面文字.获取(156, "备注已修改为：#%。", new Object[] {聊天控件.聊天对象.讯友或群主.备注}));
    }

    void 拉黑讯友成功(SSPackageReader SS包解读器) throws Exception {
        聊天控件.聊天对象.讯友或群主.拉黑 = true;
        long 讯友录更新时间 = (long) SS包解读器.读取_有标签("时间");
        if (讯友录更新时间 > 0) { 当前用户.讯友录有变动(讯友录更新时间); }
        switch (当前用户.讯友录当前显示范围) {
            case Constants.讯友录显示范围_最近:
            case Constants.讯友录显示范围_讯友:
            case Constants.讯友录显示范围_某标签:
            case Constants.讯友录显示范围_黑名单:
                Fragment_Main.主窗体.刷新讯友录();
        }
        说(界面文字.获取(160, "你已将此讯友拉黑。"));
    }

    void 加入大聊天群成功(SSPackageReader SS包解读器) throws Exception {
        String 子域名 = (String) SS包解读器.读取_有标签("子域名");
        long 群编号 = (long) SS包解读器.读取_有标签("群编号");
        String 群名称 = (String) SS包解读器.读取_有标签("群名称");
        long 图标更新时间 = (long) SS包解读器.读取_有标签("图标更新时间");
        String 连接凭据 = (String) SS包解读器.读取_有标签("连接凭据");
        byte 角色 = (byte) SS包解读器.读取_有标签("角色");
        String 本国语域名 = (String) SS包解读器.读取_有标签("本国语域名");
        Group_Large 大聊天群 = new Group_Large();
        大聊天群.子域名 = 子域名;
        大聊天群.编号 = 群编号;
        大聊天群.名称 = 群名称;
        大聊天群.图标更新时间 = 图标更新时间;
        大聊天群.连接凭据 = 连接凭据;
        大聊天群.我的角色 = 角色;
        大聊天群.本国语域名 = 本国语域名;
        int i = 子域名.indexOf(".");
        大聊天群.主机名 = 子域名.substring(0, i);
        大聊天群.英语域名 = 子域名.substring(i + 1);
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
        当前用户.保存大聊天群连接凭据(大聊天群);
        ChatWith 聊天对象2 = new ChatWith();
        聊天对象2.大聊天群 = 大聊天群;
        Fragment_Main.主窗体.添加聊天控件(聊天对象2);
        if (主控机器人.数据库_更新最近互动讯友排名(子域名, 群编号)) {
            if (Fragment_Main.主窗体 != null) {
                Fragment_Main.主窗体.刷新讯友录(Constants.讯友录显示范围_聊天群);
            }
        }
    }
}
