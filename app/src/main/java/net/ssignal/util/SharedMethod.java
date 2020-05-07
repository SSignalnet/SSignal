package net.ssignal.util;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import net.ssignal.R;
import net.ssignal.User;
import net.ssignal.language.Text;
import net.ssignal.protocols.ProtocolParameters;
import net.ssignal.protocols.SSPackageCreator;
import net.ssignal.protocols.SSPackageReader;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class SharedMethod {

    public static boolean 字符串未赋值或为空(String 字符串) {
        return 字符串 == null || 字符串.equals("");
    }

    public static byte[] 接收指定长度的数据(Socket 网络连接器, int 数据长度) throws Exception {
        if (数据长度 < 1) {return null;}
        byte[] 字节数组= new byte[数据长度];
        int 收到的数据的长度, 已收数据总长度=0;
        InputStream 输入流  = 网络连接器.getInputStream();
        do {
            收到的数据的长度 = 输入流.read(字节数组, 已收数据总长度, 数据长度);
            if (收到的数据的长度 > 0) {
                数据长度 -= 收到的数据的长度;
                if (数据长度 > 0) {
                    已收数据总长度 += 收到的数据的长度;}
                else {
                    return 字节数组;
                }
            } else {
                return null;
            }
        } while (数据长度 > 0);
        return 字节数组;
    }

    public static SSPackageReader 读取一个SS包(RandomAccessFile 文件随机访问器) throws Exception {
        long 位置 = 文件随机访问器.getFilePointer();
        if (位置 > 2) {
            文件随机访问器.seek(位置 - 2);
            short 长度 = 文件随机访问器.readShort();
            if (长度 > ProtocolParameters.SS包标识_有标签.length()) {
                文件随机访问器.seek(位置 - (长度 + 2));
                byte 字节数组[] = new byte[长度];
                文件随机访问器.read(字节数组, 0, 字节数组.length);
                SSPackageReader SS包读取器;
                try {
                    SS包读取器 = new SSPackageReader(字节数组);
                } catch (Exception e) {
                    文件随机访问器.seek(位置);
                    return null;
                }
                文件随机访问器.seek(位置 - (长度 + 2));
                return SS包读取器;
            }
        }
        return null;
    }

    public static SSPackageReader 读取一个SS包2(RandomAccessFile 文件随机访问器) throws Exception {
        byte 字节数组[] = ProtocolParameters.SS包标识_有标签.getBytes("ASCII");
        long 截止位置 = 文件随机访问器.getFilePointer();
        while (文件随机访问器.getFilePointer() - ProtocolParameters.SS包标识_有标签.length() > 0){
            long 位置 = 文件随机访问器.getFilePointer() - ProtocolParameters.SS包标识_有标签.length();
            文件随机访问器.seek(位置);
            if (文件随机访问器.readByte() == 字节数组[0]) {
                if (文件随机访问器.readByte() == 字节数组[1]) {
                    if (文件随机访问器.readByte() == 字节数组[2]) {
                        if (文件随机访问器.readByte() == 字节数组[3]) {
                            文件随机访问器.seek(位置);
                            byte 字节数组2[] = new byte[(int)(截止位置 - 文件随机访问器.getFilePointer())];
                            文件随机访问器.read(字节数组2, 0, 字节数组2.length);
                            SSPackageReader SS包读取器;
                            try {
                                SS包读取器 = new SSPackageReader(字节数组2);
                            } catch (Exception e) {
                                文件随机访问器.seek(位置 - 1);
                                continue;
                            }
                            文件随机访问器.seek(位置);
                            return SS包读取器;
                        } else {
                            if (文件随机访问器.getFilePointer() > 4) {
                                文件随机访问器.seek(位置 - 5);
                            } else {
                                return null;
                            }
                        }
                    } else {
                        if (文件随机访问器.getFilePointer() > 3) {
                            文件随机访问器.seek(位置 - 4);
                        } else {
                            return null;
                        }
                    }
                } else {
                    if (文件随机访问器.getFilePointer() > 2) {
                        文件随机访问器.seek(位置 - 3);
                    } else {
                        return null;
                    }
                }
            } else {
                if (文件随机访问器.getFilePointer() > 1) {
                    文件随机访问器.seek(位置 - 2);
                } else {
                    return null;
                }
            }
        }
        return null;
    }

    public static void 标为已删除(RandomAccessFile 文件随机访问器, long 开始位置, long 结束位置, SSPackageReader SS包读取器) throws Exception {
        SSPackageCreator SS包生成器 = new SSPackageCreator(SS包读取器);
        if (SS包生成器.修改某个标签的数据("删除", true)) {
            byte 字节数组[] = SS包生成器.生成SS包();
            if (字节数组.length == 结束位置 - 开始位置 - 2) {
                文件随机访问器.seek(开始位置);
                文件随机访问器.write(字节数组, 0, 字节数组.length);
            }
        }
    }

    public static void 删除相关文件(SSPackageReader SS包读取器, Context 运行环境, User 当前用户) throws Exception {
        byte 讯宝指令 = (byte) SS包读取器.读取_有标签("指令");
        switch (讯宝指令) {
            case ProtocolParameters.讯宝指令_发送语音:
            case ProtocolParameters.讯宝指令_发送图片:
            case ProtocolParameters.讯宝指令_发送短视频:
                String 文本 = (String)SS包读取器.读取_有标签("文本");
                if (!SharedMethod.字符串未赋值或为空(文本)) {
                    File 文件2 = new File(文本);
                    if (文件2.exists()) {
                        if (文件2.getAbsolutePath().startsWith(运行环境.getFilesDir().toString() + "/")) {
                            try {
                                文件2.delete();
                            } catch (Exception e) {}
                        }
                    } else {
                        文本 = 运行环境.getFilesDir().toString() + "/" + 当前用户.获取英语讯宝地址() + "/" + 文本;
                        文件2 = new File(文本);
                        if (文件2.exists()) {
                            try {
                                文件2.delete();
                            } catch (Exception e) {}
                        }
                    }
                    if (讯宝指令 == ProtocolParameters.讯宝指令_发送短视频) {
                        文件2 = new File(文本 + ".jpg");
                        if (文件2.exists()) {
                            try {
                                文件2.delete();
                            } catch (Exception e) {}
                        }
                    }
                }
                break;
        }
    }

    public static boolean 是否是手机号(String 字符串) {
        char 字符数组[] = 字符串.toCharArray();
        for (int i = 0; i < 字符数组.length; i++) {
            if (字符数组[i] < 48 || 字符数组[i] > 57) {
                return false;
            }
        }
        return true;
    }

    public static long 获取当前UTC时间() {
        return System.currentTimeMillis();
    }

    public static String 生成大小写英文字母与数字的随机字符串(int 长度) {
        if (长度 < 1) {长度=1;}
        byte 字节数组[] = new byte[长度];
        Random 随机数生成器 = new Random(System.currentTimeMillis());
        int 随机数;
        for (int i = 0; i < 长度; i++) {
            随机数 = 随机数生成器.nextInt(62);
            if (随机数 < 10) {
                字节数组[i] = (byte) (随机数生成器.nextInt(10) + 48);
            } else if (随机数 < 36) {
                字节数组[i] = (byte) (随机数生成器.nextInt(26) + 65);
            } else {
                字节数组[i] = (byte) (随机数生成器.nextInt(26) + 97);
            }
        }
        try {
            return new String(字节数组,"US-ASCII");
        }
        catch (UnsupportedEncodingException e) {
            return "";
        }
    }

    public static String 替换HTML和JS敏感字符(String 字符串) {
        if (SharedMethod.字符串未赋值或为空(字符串)) {
            return "";
        }
        StringBuffer 字符串合并器 = new StringBuffer(字符串.length() * 2);
        char 字符数组[] = 字符串.toCharArray();
        for (int i = 0; i < 字符数组.length; i++) {
            switch (字符数组[i]) {
                case '<':
                    字符串合并器.append("&lt;");
                    break;
                case '>':
                    字符串合并器.append("&gt;");
                    break;
                case '&':
                    字符串合并器.append("&amp;");
                    break;
                case 39:
                    字符串合并器.append("&apos;");
                    break;
                case 34:
                    字符串合并器.append("&quot;");
                    break;
                case 10:
                    字符串合并器.append("<br>");
                    break;
                case 13:
                    break;
                case '\\':
                    字符串合并器.append("\\\\");
                    break;
                default:
                    字符串合并器.append(字符数组[i]);
            }
        }
        return 字符串合并器.toString();
    }

    public static String 处理文件路径以用作JS函数参数(String 文件路径) {
        if (SharedMethod.字符串未赋值或为空(文件路径)) {
            return "";
        }
        StringBuffer 字符串合并器 = new StringBuffer(文件路径.length() * 2);
        char 字符数组[] = 文件路径.toCharArray();
        for (int i = 0; i < 字符数组.length; i++) {
            switch (字符数组[i]) {
                case 39:
                    字符串合并器.append("\\'");
                    break;
                case 34:
                    字符串合并器.append("\\\"");
                    break;
                case '\\':
                    字符串合并器.append("/");
                    break;
                default:
                    字符串合并器.append(字符数组[i]);
            }
        }
        return 字符串合并器.toString();
    }

    public static String 获取文件名(String 文件路径) {
        int i = 文件路径.lastIndexOf("/");
        if (i >= 0 && i < 文件路径.length() - 1) {
            return 文件路径.substring(i + 1);
        } else {
            return "";
        }
    }

    public static String 获取文件名不带扩展名(String 文件路径) {
        int i = 文件路径.lastIndexOf("/");
        if (i >= 0 && i < 文件路径.length() - 1) {
            String 文件名 = 文件路径.substring(i + 1);
            i = 文件名.lastIndexOf(".");
            if (i > 0) {
                return 文件名.substring(0, i);
            } else {
                return 文件名;
            }
        } else {
            i = 文件路径.lastIndexOf(".");
            if (i > 0) {
                return 文件路径.substring(0, i);
            } else {
                return 文件路径;
            }
        }
    }

    public static void 打开资源管理器并选中文件(Context 运行环境, String 文件路径) {
        File 文件 = new File(文件路径);
        if (文件 == null || !文件.exists()) {
            return;
        }
        try {
            Intent 意图 = new Intent(Intent.ACTION_GET_CONTENT);
            意图.addCategory(Intent.CATEGORY_OPENABLE);
            意图.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            意图.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            Uri uri = FileProvider.getUriForFile(运行环境, "net.ssignal.fileprovider", 文件);
            意图.setDataAndType(uri, "file/*");
            运行环境.startActivity(意图);
        } catch (Exception e) {
        }
    }

//    public static String 加上反斜杠(String 字符串) {
//        StringBuffer 字符串合并器 = new StringBuffer(字符串.length() * 2);
//        char 字符数组[] = 字符串.toCharArray();
//        for (int i = 0; i < 字符数组.length; i++) {
//            switch (字符数组[i]) {
//                case '\\':
//                    字符串合并器.append("\\\\");
//                    break;
//                default:
//                    字符串合并器.append(字符数组[i]);
//            }
//        }
//        return 字符串合并器.toString();
//    }

    public static void 载入界面文字(Context 运行环境) {
        Locale 本机设置 = Locale.getDefault();
        switch (本机设置.getISO3Language()) {
            case ProtocolParameters.语言代码_中文:
                Text.界面文字 = new Text(null, false);
                break;
            default:
                String 界面文本 = SharedMethod.获取附属文本文件(运行环境, ProtocolParameters.语言代码_英语 + ".txt");
                Text.界面文字 = new Text(界面文本, false);
        }
    }

    public static String 获取扩展名(String 路径) {
        int i = 路径.lastIndexOf('.');
        if (i >= 0 && i < 路径.length() - 1) {
            String 扩展名 = 路径.substring(i + 1);
            if (扩展名.length() < 10) {
                return 扩展名;
            }
        }
        return "";
    }

    public static boolean 是否是整数(String 字符串) {
        char 字符数组[] = 字符串.toCharArray();
        for (int i = 0; i < 字符数组.length; i++) {
            if (字符数组[i] < 48 || 字符数组[i] > 57) {
                return false;
            }
        }
        return true;
    }

    public static void 保存位图(Bitmap 位图, String 保存路径) throws Exception {
        File 文件 = new File(保存路径);
        文件.createNewFile();
        FileOutputStream 文件输出流 = new FileOutputStream(文件);
        位图.compress(Bitmap.CompressFormat.JPEG, 80, 文件输出流);
        文件输出流.flush();
        文件输出流.close();
    }

    public static boolean 服务是否运行(Context 场景, String 服务名称) {
        ActivityManager 活动管理器 = (ActivityManager)场景.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> 服务列表 = 活动管理器.getRunningServices(1000);
        if (服务列表.size() > 0) {
            服务名称 = 场景.getPackageName() + "." + 服务名称;
            for (int i = 0; i < 服务列表.size(); i++) {
                if (服务列表.get(i).service.getClassName().equals(服务名称)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean 是否是调试状态(Context 场景) {
        try {
            ApplicationInfo 应用程序信息 = 场景.getApplicationInfo();
            return (应用程序信息.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
        } catch (Exception e) {
            return false;
        }
    }

    public static void 弹出提示框(Context 场景, String 提示信息, int 时长) {
        View 布局视图 = LayoutInflater.from(场景).inflate(R.layout.toast, null);
        Toast 提示框 = new Toast(场景.getApplicationContext());
        提示框.setView(布局视图);
        TextView 文字 = (TextView) 布局视图.findViewById(R.id.文字);
        文字.setText(提示信息);
        提示框.setDuration(时长);
        提示框.setGravity(Gravity.CENTER, 0, 0);
        提示框.show();
    }

    public static void 复制文件(String 输入文件路径, String 输出文件路径) throws Exception {
        InputStream 输入流 = new FileInputStream(输入文件路径);
        FileOutputStream 文件输出流 = new FileOutputStream(输出文件路径);
        byte[] 字节数组 = new byte[1024 * 8];
        int 读取的字节数;
        while ((读取的字节数 = 输入流.read(字节数组)) != -1) {
            文件输出流.write(字节数组, 0, 读取的字节数);
        }
        输入流.close();
        文件输出流.flush();
        文件输出流.close();
    }

    public static String 获取附属文本文件(Context 运行环境, String 文件名) {
        AssetManager 附属文件管理器 = 运行环境.getAssets();
        ByteArrayOutputStream 字节数组输出流=null;
        String 文本="";
        try {
            InputStream 输入流 = 附属文件管理器.open(文件名);
            字节数组输出流 = new ByteArrayOutputStream();
            byte[] 字节数组 = new byte[1024];
            int 长度;
            while ((长度 = 输入流.read(字节数组)) != -1) {
                字节数组输出流.write(字节数组,0,长度);
            }
            字节数组输出流.close();
            输入流.close();
            文本=字节数组输出流.toString("UTF-8");
            if (!SharedMethod.字符串未赋值或为空(文本)) {
                if (文本.startsWith("\uFEFF")) {
                    if (文本.length() > 1) {
                        文本=文本.substring(1);
                    } else {
                        文本="";
                    }
                }
            }
        }
        catch (Exception e) {
            try {
                if (字节数组输出流 != null) {字节数组输出流.close();}
            } catch (Exception e2) {}
            return "";
        }
        return 文本;
    }

    public static void 保存文件的全部字节(String 文件路径, byte 字节数组[]) throws Exception {
        File 文件 = new File(文件路径);
        if (文件.exists()) {
            文件.delete();
        }
        if (文件.createNewFile()) {
            RandomAccessFile 文件随机访问器 = new RandomAccessFile(文件.getAbsolutePath(), "rwd");
            文件随机访问器.write(字节数组);
            文件随机访问器.close();
        }
    }

    public static byte[] 读取文件的全部字节(String 文件路径) throws Exception {
        byte[] 字节数组 = null;
        RandomAccessFile 文件随机访问器 = new RandomAccessFile(文件路径, "r");
        if (文件随机访问器.length() > 0) {
            字节数组 = new byte[(int) 文件随机访问器.length()];
            文件随机访问器.read(字节数组, 0, 字节数组.length);
        }
        文件随机访问器.close();
        return 字节数组;
    }

}
