package net.ssignal;

import android.content.Context;
import android.content.SharedPreferences;

import net.ssignal.protocols.GroupMember;
import net.ssignal.protocols.ProtocolMethods;
import net.ssignal.protocols.SSPackageCreator;
import net.ssignal.structure.Domain;
import net.ssignal.protocols.Contact;
import net.ssignal.protocols.Group_Large;
import net.ssignal.protocols.Group_Small;
import net.ssignal.protocols.ProtocolParameters;
import net.ssignal.protocols.SSPackageReader;
import net.ssignal.structure.HostName;
import net.ssignal.util.MyDate;
import net.ssignal.util.SharedMethod;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.net.Socket;

import javax.crypto.Cipher;

import static net.ssignal.Robot_MainControl.主控机器人;

public class User {

    private class 小宇宙凭据_复合数据 {
        String 英语子域名, 连接凭据;
        boolean 是商品编辑;
    }

    protected static User 当前用户;

    String 域名_英语, 域名_本国语;

    String 英语用户名, 本国语用户名, 职能, 电子邮箱地址;
    String 凭据_中心服务器, 凭据_管理员;
    long 编号, 头像更新时间, 密钥创建时间, 讯友录更新时间 = -1;
    String 主机名, 传送服务器验证码;
    short 位置号;
    Cipher AES加密器, AES解密器;

    boolean 获取了密钥 = false, 获取了账户信息 = false, 显示讯友临时编号 = false;
    Contact 讯友目录[];
    byte 讯友录当前显示范围;
    String 讯友录当前显示标签;
    Group_Small 加入的小聊天群[];
    Group_Large 加入的大聊天群[];

    Socket 网络连接器;
    long 讯宝发送序号;

    Domain 白域[], 黑域[];

    private 小宇宙凭据_复合数据 小宇宙读取凭据[];
    private GetTUCredential 小宇宙凭据获取器[];
    String 子域名_小宇宙写入, 小宇宙写入凭据;

    HostName 主机名数组[];
    int 主机名数量 = 0;

    Context 运行环境;

    User(Context 运行环境) {
        this.运行环境 = 运行环境;
        SharedPreferences 共享的设置 = 运行环境.getSharedPreferences("login", 运行环境.MODE_PRIVATE);
        if (共享的设置 != null) {
            编号 = 共享的设置.getLong("UserID", 0);
            凭据_中心服务器 = 共享的设置.getString("CenterCredential", null);
            域名_英语 = 共享的设置.getString("Domain", null);
        }
    }

    public String 获取英语讯宝地址() {
        return 英语用户名 + ProtocolParameters.讯宝地址标识 + 域名_英语;
    }

    public String 获取本国语讯宝地址() {
        return 本国语用户名 + ProtocolParameters.讯宝地址标识 + 域名_本国语;
    }

    public Contact 查找讯友(String 讯宝地址) {
        if (讯友目录 != null) {
            for (int i = 0; i < 讯友目录.length; i++) {
                if (讯宝地址.equals(讯友目录[i].英语讯宝地址) || 讯宝地址.equals(讯友目录[i].本国语讯宝地址)) {
                    return 讯友目录[i];
                }
            }
        }
        return null;
    }

    Group_Small 查找小聊天群(String 讯宝地址, byte 群编号) {
        if (加入的小聊天群 != null) {
            for (int i = 0; i < 加入的小聊天群.length; i++) {
                if (讯宝地址.equals(加入的小聊天群[i].群主.英语讯宝地址) && 加入的小聊天群[i].编号 == 群编号) {
                    return 加入的小聊天群[i];
                }
            }
        }
        return null;
    }

    Group_Large 查找大聊天群(String 子域名, long 群编号) {
        if (加入的大聊天群 != null) {
            for (int i = 0; i < 加入的大聊天群.length; i++) {
                if (子域名.equals(加入的大聊天群[i].子域名) && 加入的大聊天群[i].编号 == 群编号) {
                    return 加入的大聊天群[i];
                }
            }
        }
        return null;
    }

    boolean 已登录() {
        if (编号 > 0 && !SharedMethod.字符串未赋值或为空(凭据_中心服务器) && ProtocolMethods.检查英语域名(域名_英语)) {
            return true;
        } else {
            return false;
        }
    }

    void 获取小宇宙凭据(Fragment_Chating 聊天控件, String 英语子域名, boolean 是写入凭据, MyHandler 跨线程调用器) {
        if (是写入凭据) {
            if (!SharedMethod.字符串未赋值或为空(小宇宙写入凭据) && 英语子域名.equals(子域名_小宇宙写入)) {
                聊天控件.收到小宇宙的连接凭据(英语子域名, 小宇宙写入凭据, false, 是写入凭据);
                return;
            }
        }
        if (小宇宙读取凭据 != null) {
            int i;
            for (i = 0; i < 小宇宙读取凭据.length; i++) {
                if (英语子域名.equals(小宇宙读取凭据[i].英语子域名)) {
                    break;
                }
            }
            if (i < 小宇宙读取凭据.length) {
                小宇宙凭据_复合数据 小宇宙凭据2 = 小宇宙读取凭据[i];
                if (i > 0) {
                    for (i = i; i > 0; i--) {
                        小宇宙读取凭据[i] = 小宇宙读取凭据[i - 1];
                    }
                    小宇宙读取凭据[0] = 小宇宙凭据2;
                }
                if (是写入凭据) {
                    子域名_小宇宙写入 = 英语子域名;
                    小宇宙写入凭据 = 小宇宙凭据2.连接凭据;
                }
                聊天控件.收到小宇宙的连接凭据(英语子域名, 小宇宙凭据2.连接凭据, 小宇宙凭据2.是商品编辑, 是写入凭据);
                return;
            }
        }
        if (小宇宙凭据获取器 != null) {
            int i;
            for (i = 0; i < 小宇宙凭据获取器.length; i++) {
                if (英语子域名.equals(小宇宙凭据获取器[i].英语子域名)) {
                    break;
                }
            }
            if (i < 小宇宙凭据获取器.length) {
                小宇宙凭据获取器[i].添加聊天控件(聊天控件);
            } else {
                GetTUCredential 获取器 = new GetTUCredential(聊天控件, 英语子域名, 是写入凭据, 跨线程调用器);
                GetTUCredential 小宇宙凭据获取器2[] = new GetTUCredential[小宇宙凭据获取器.length + 1];
                System.arraycopy(小宇宙凭据获取器, 0, 小宇宙凭据获取器2, 0, 小宇宙凭据获取器.length);
                小宇宙凭据获取器2[小宇宙凭据获取器2.length - 1] = 获取器;
                小宇宙凭据获取器 = 小宇宙凭据获取器2;
                获取器.获取();
            }
        } else {
            GetTUCredential 获取器 = new GetTUCredential(聊天控件, 英语子域名, 是写入凭据, 跨线程调用器);
            小宇宙凭据获取器 = new GetTUCredential[1];
            小宇宙凭据获取器[0] = 获取器;
            获取器.获取();
        }
    }

//    boolean 是否正在获取小宇宙凭据(Fragment_Chating 聊天控件) {
//        if (小宇宙凭据获取器 == null) {
//            return false;
//        }
//        int i;
//        for (i = 0; i < 小宇宙凭据获取器.length; i++) {
//            if (小宇宙凭据获取器[i].查找聊天控件(聊天控件)) {
//                return true;
//            }
//        }
//        return false;
//    }

    void 获取小宇宙凭据结束(long 创建时刻, byte 字节数组[]) {
        if (小宇宙凭据获取器 == null) {
            return;
        }
        int i;
        for (i = 0; i < 小宇宙凭据获取器.length; i++) {
            if (小宇宙凭据获取器[i].创建时刻 == 创建时刻) {
                break;
            }
        }
        GetTUCredential 获取器;
        if (i < 小宇宙凭据获取器.length) {
            获取器 = 小宇宙凭据获取器[i];
            if (小宇宙凭据获取器.length > 1) {
                GetTUCredential 小宇宙凭据获取器2[] = new GetTUCredential[小宇宙凭据获取器.length - 1];
                int j, k = 0;
                for (j = 0; j < 小宇宙凭据获取器.length; j++) {
                    if (j != i) {
                        小宇宙凭据获取器2[k] = 小宇宙凭据获取器[j];
                        k++;
                    }
                }
                小宇宙凭据获取器 = 小宇宙凭据获取器2;
            } else {
                小宇宙凭据获取器 = null;
            }
        } else {
            return;
        }
        if (字节数组 != null) {
            try {
                SSPackageReader SS包解读器 = new SSPackageReader(字节数组);
                if (SS包解读器.获取查询结果() == ProtocolParameters.查询结果_成功) {
                    String 子域名 = (String) SS包解读器.读取_有标签("子域名");
                    String 连接凭据 = (String) SS包解读器.读取_有标签("连接凭据");
                    boolean 是商品编辑 = (boolean) SS包解读器.读取_有标签("是商品编辑");
                    if (!获取器.是写入凭据) {
                        if (小宇宙读取凭据 != null) {
                            for (i = 0; i < 小宇宙读取凭据.length; i++) {
                                if (子域名.equals(小宇宙读取凭据[i].英语子域名)) {
                                    break;
                                }
                            }
                            if (i == 小宇宙读取凭据.length) {
                                小宇宙凭据_复合数据 小宇宙读取凭据2[] = new 小宇宙凭据_复合数据[小宇宙读取凭据.length + 1];
                                System.arraycopy(小宇宙读取凭据, 0, 小宇宙读取凭据2, 0, 小宇宙读取凭据.length);
                                小宇宙凭据_复合数据 小宇宙凭据 = new 小宇宙凭据_复合数据();
                                小宇宙凭据.英语子域名 = 子域名;
                                小宇宙凭据.连接凭据 = 连接凭据;
                                小宇宙凭据.是商品编辑 = 是商品编辑;
                                小宇宙读取凭据2[小宇宙读取凭据2.length - 1] = 小宇宙凭据;
                                小宇宙读取凭据 = 小宇宙读取凭据2;
                            }
                        } else {
                            小宇宙读取凭据 = new 小宇宙凭据_复合数据[1];
                            小宇宙凭据_复合数据 小宇宙凭据 = new 小宇宙凭据_复合数据();
                            小宇宙凭据.英语子域名 = 子域名;
                            小宇宙凭据.连接凭据 = 连接凭据;
                            小宇宙凭据.是商品编辑 = 是商品编辑;
                            小宇宙读取凭据[0] = 小宇宙凭据;
                        }
                    } else {
                        子域名_小宇宙写入 = 子域名;
                        小宇宙写入凭据 = 连接凭据;
                    }
                    for (i = 0; i < 获取器.聊天控件.length; i++) {
                        获取器.聊天控件[i].收到小宇宙的连接凭据(子域名, 连接凭据, 是商品编辑, 获取器.是写入凭据);
                    }
                } else if (SS包解读器.获取查询结果() == ProtocolParameters.查询结果_发送序号不一致) {
                    if (主控机器人 != null) {
                        主控机器人.关闭网络连接器(13);
                    }
                } else if (!SharedMethod.字符串未赋值或为空(SS包解读器.获取出错提示文本())) {
                    for (i = 0; i < 获取器.聊天控件.length; i++) {
                        获取器.聊天控件[i].机器人.说(SS包解读器.获取出错提示文本());
                    }
                }
            } catch (Exception e) {

            }
        }
    }

    static boolean 收集标签(String 某一标签, String 讯友标签[], int 讯友标签数) {
        if (SharedMethod.字符串未赋值或为空(某一标签)) {
            return false;
        }
        int j = 0;
        if (讯友标签数 > 0) {
            for (j = 0; j < 讯友标签数; j++) {
                if (某一标签.equalsIgnoreCase(讯友标签[j])) {
                    return false;
                }
            }
        }
        讯友标签[j] = 某一标签;
        return true;
    }

    void 讯友录有变动(long 讯友录更新时间) {
        this.讯友录更新时间 = 讯友录更新时间;
        try {
            SSPackageCreator SS包生成器 = new SSPackageCreator();
            SS包生成器.添加_有标签("讯友录更新时间", this.讯友录更新时间);
            if (讯友目录 != null) {
                SSPackageCreator SS包生成器2 = new SSPackageCreator();
                Contact 某一讯友;
                for (int i = 0; i < 讯友目录.length; i++) {
                    某一讯友 = 讯友目录[i];
                    SSPackageCreator SS包生成器3 = new SSPackageCreator();
                    SS包生成器3.添加_有标签("英语讯宝地址", 某一讯友.英语讯宝地址);
                    if (!SharedMethod.字符串未赋值或为空(某一讯友.本国语讯宝地址)) {
                        SS包生成器3.添加_有标签("本国语讯宝地址", 某一讯友.本国语讯宝地址);
                    }
                    SS包生成器3.添加_有标签("备注", 某一讯友.备注);
                    if (!SharedMethod.字符串未赋值或为空(某一讯友.标签一)) {
                        SS包生成器3.添加_有标签("标签一", 某一讯友.标签一);
                    }
                    if (!SharedMethod.字符串未赋值或为空(某一讯友.标签二)) {
                        SS包生成器3.添加_有标签("标签二", 某一讯友.标签二);
                    }
                    SS包生成器3.添加_有标签("主机名", 某一讯友.主机名);
                    SS包生成器3.添加_有标签("拉黑", 某一讯友.拉黑);
                    SS包生成器3.添加_有标签("位置号", 某一讯友.位置号);
                    SS包生成器2.添加_有标签("讯友", SS包生成器3);
                }
                SS包生成器.添加_有标签("讯友录", SS包生成器2);
            }
            if (白域 != null) {
                SSPackageCreator SS包生成器2 = new SSPackageCreator();
                Domain 某一白域;
                for (int i = 0; i < 白域.length; i++) {
                    某一白域 = 白域[i];
                    SSPackageCreator SS包生成器3 = new SSPackageCreator();
                    SS包生成器3.添加_有标签("英语", 某一白域.英语);
                    if (!SharedMethod.字符串未赋值或为空(某一白域.本国语)) {
                        SS包生成器3.添加_有标签("本国语", 某一白域.本国语);
                    }
                    SS包生成器2.添加_有标签("域名", SS包生成器3);
                }
                SS包生成器.添加_有标签("白域", SS包生成器2);
            }
            if (黑域 != null) {
                SSPackageCreator SS包生成器2 = new SSPackageCreator();
                Domain 某一黑域;
                for (int i = 0; i < 黑域.length; i++) {
                    某一黑域 = 黑域[i];
                    SSPackageCreator SS包生成器3 = new SSPackageCreator();
                    SS包生成器3.添加_有标签("英语", 某一黑域.英语);
                    if (!SharedMethod.字符串未赋值或为空(某一黑域.本国语)) {
                        SS包生成器3.添加_有标签("本国语", 某一黑域.本国语);
                    }
                    SS包生成器2.添加_有标签("域名", SS包生成器3);
                }
                SS包生成器.添加_有标签("黑域", SS包生成器2);
            }
            File 文件 = new File(运行环境.getFilesDir().toString() + "/" + 获取英语讯宝地址() + "/sspal.sspk");
            if (文件.exists()) {
                文件.delete();
            }
            File 目录 = 文件.getParentFile();
            if (!目录.exists() || !目录.isDirectory()) {
                目录.mkdir();
            }
            SharedMethod.保存文件的全部字节(文件.getAbsolutePath(), SS包生成器.生成SS包());
        } catch (Exception e) {
        }
    }

    void 保存登录信息() {
        SharedPreferences 共享的设置 = 运行环境.getSharedPreferences("login", 运行环境.MODE_PRIVATE);
        SharedPreferences.Editor 编辑器 = 共享的设置.edit();
        编辑器.putLong("UserID", 编号);
        编辑器.putString("CenterCredential", 凭据_中心服务器);
        编辑器.putString("Domain", 域名_英语);
        编辑器.commit();
    }

    void 注销() {
        SharedPreferences 共享的设置 = 运行环境.getSharedPreferences("login", 运行环境.MODE_PRIVATE);
        SharedPreferences.Editor 编辑器 = 共享的设置.edit();
        编辑器.putLong("UserID", 0);
        编辑器.putString("CenterCredential", "");
        编辑器.putString("Domain", "");
        编辑器.commit();
        File 文件 = new File(运行环境.getFilesDir().toString() + "/accountinfo.sspk");
        if (文件.exists()) {
            文件.delete();
        }
        文件 = new File(运行环境.getFilesDir().toString() + "/" + 获取英语讯宝地址() + "/sspal.sspk");
        if (文件.exists()) {
            文件.delete();
        }
        清除密钥();
        if (加入的小聊天群 != null) {
            for (int i = 0; i < 加入的小聊天群.length; i++) {
                清除小聊天群成员列表(加入的小聊天群[i]);
            }
        }
        if (加入的大聊天群 != null) {
            for (int i = 0; i < 加入的大聊天群.length; i++) {
                清除大聊天群凭据(加入的大聊天群[i]);
            }
        }
    }

    void 清除密钥() {
        File 文件 = new File(运行环境.getFilesDir().toString() + "/" + 获取英语讯宝地址() + "/keyiv.sspk");
        if (文件.exists()) {
            文件.delete();
        }
    }

    void 保存大聊天群连接凭据(Group_Large 大聊天群) {
        try {
            SSPackageCreator SS包生成器 = new SSPackageCreator();
            SS包生成器.添加_有标签("子域名", 大聊天群.子域名);
            SS包生成器.添加_有标签("群编号", 大聊天群.编号);
            SS包生成器.添加_有标签("群名称", 大聊天群.名称);
            SS包生成器.添加_有标签("图标更新时间", 大聊天群.图标更新时间);
            SS包生成器.添加_有标签("连接凭据", 大聊天群.连接凭据);
            SS包生成器.添加_有标签("角色", 大聊天群.我的角色);
            SS包生成器.添加_有标签("本国语域名", 大聊天群.本国语域名);
            File 文件 = new File(运行环境.getFilesDir() + "/" + 获取英语讯宝地址() + "/largegroup/" + 大聊天群.子域名 + "#" + 大聊天群.编号 + ".sspk");
            if (文件.exists()) {
                文件.delete();
            }
            File 目录 = 文件.getParentFile();
            if (!目录.exists()) {
                目录.mkdir();
            }
            SharedMethod.保存文件的全部字节(文件.getAbsolutePath(), SS包生成器.生成SS包());
        } catch (Exception e) {
        }
    }

    void 读取大聊天群凭据(Group_Large 大聊天群) {
        File 文件 = new File(运行环境.getFilesDir() + "/" + 获取英语讯宝地址() + "/largegroup/" + 大聊天群.子域名 + "#" + 大聊天群.编号 + ".sspk");
        if (文件.exists()) {
            try {
                byte 字节数组[] = SharedMethod.读取文件的全部字节(文件.getAbsolutePath());
                if (字节数组 != null) {
                    SSPackageReader SS包解读器 = new SSPackageReader(字节数组);
                    String 子域名 = (String) SS包解读器.读取_有标签("子域名");
                    if (!大聊天群.子域名.equals(子域名)) {
                        return;
                    }
                    long 群编号 = (long) SS包解读器.读取_有标签("群编号");
                    if (大聊天群.编号 != 群编号) {
                        return;
                    }
                    大聊天群.名称 = (String) SS包解读器.读取_有标签("群名称");
                    大聊天群.图标更新时间 = (long) SS包解读器.读取_有标签("图标更新时间");
                    大聊天群.连接凭据 = (String) SS包解读器.读取_有标签("连接凭据");
                    大聊天群.我的角色 = (byte) SS包解读器.读取_有标签("角色");
                    大聊天群.本国语域名 = (String) SS包解读器.读取_有标签("本国语域名");
                    int i = 子域名.indexOf(".");
                    大聊天群.主机名 = 子域名.substring(0, i);
                    大聊天群.英语域名 = 子域名.substring(i + 1);
                }
            } catch (Exception e) {
            }
        }
    }

    void 清除大聊天群凭据(Group_Large 大聊天群) {
        File 文件 = new File(运行环境.getFilesDir() + "/" + 获取英语讯宝地址() + "/largegroup/" + 大聊天群.子域名 + "#" + 大聊天群.编号 + ".sspk");
        if (文件.exists()) {
            文件.delete();
        }
    }

    void 保存小聊天群成员列表(Group_Small 小聊天群) {
        GroupMember 群成员[] = 小聊天群.群成员;
        try {
            GroupMember 某一成员;
            SSPackageCreator SS包生成器 = new SSPackageCreator();
            for (int i = 0; i < 群成员.length; i++) {
                某一成员 = 群成员[i];
                SSPackageCreator SS包生成器2 = new SSPackageCreator();
                SS包生成器2.添加_有标签("英语讯宝地址", 某一成员.英语讯宝地址);
                SS包生成器2.添加_有标签("主机名", 某一成员.主机名);
                SS包生成器2.添加_有标签("位置号", 某一成员.位置号);
                SS包生成器2.添加_有标签("角色", 某一成员.角色);
                if (!SharedMethod.字符串未赋值或为空(某一成员.本国语讯宝地址)) {
                    SS包生成器2.添加_有标签("本国语讯宝地址", 某一成员.本国语讯宝地址);
                }
                SS包生成器.添加_有标签("成员", SS包生成器2);
            }
            File 文件 = new File(运行环境.getFilesDir() + "/" + 获取英语讯宝地址() + "/smallgroup/" + 小聊天群.群主.英语讯宝地址 + "#" + 小聊天群.编号 + ".sspk");
            if (文件.exists()) {
                文件.delete();
            }
            File 目录 = 文件.getParentFile();
            if (!目录.exists()) {
                目录.mkdir();
            }
            SharedMethod.保存文件的全部字节(文件.getAbsolutePath(), SS包生成器.生成SS包());
        } catch (Exception e) {
        }
    }

    void 读取小聊天群成员列表(Group_Small 小聊天群) {
        File 文件 = new File(运行环境.getFilesDir() + "/" + 获取英语讯宝地址() + "/smallgroup/" + 小聊天群.群主.英语讯宝地址 + "#" + 小聊天群.编号 + ".sspk");
        if (文件.exists()) {
            try {
                byte 字节数组[] = SharedMethod.读取文件的全部字节(文件.getAbsolutePath());
                if (字节数组 != null) {
                    SSPackageReader SS包解读器 = new SSPackageReader(字节数组);
                    Object SS包解读器3[] = SS包解读器.读取_重复标签("成员");
                    if (SS包解读器3 != null) {
                        GroupMember 群成员[] = new GroupMember[SS包解读器3.length];
                        GroupMember 某一成员;
                        SSPackageReader SS包解读器4;
                        for (int i = 0; i < SS包解读器3.length; i++) {
                            SS包解读器4 = (SSPackageReader) SS包解读器3[i];
                            某一成员 = new GroupMember();
                            某一成员.英语讯宝地址 = (String) SS包解读器4.读取_有标签("英语讯宝地址");
                            某一成员.主机名 = (String) SS包解读器4.读取_有标签("主机名");
                            某一成员.位置号 = (short) SS包解读器4.读取_有标签("位置号");
                            某一成员.角色 = (byte) SS包解读器4.读取_有标签("角色");
                            某一成员.本国语讯宝地址 = (String) SS包解读器4.读取_有标签("本国语讯宝地址");
                            群成员[i] = 某一成员;
                        }
                        byte k = 1;
                        for (int j = 0; j < 群成员.length; j++) {
                            群成员[j].所属的群 = 小聊天群;
                            if (群成员[j].角色 != ProtocolParameters.群角色_群主) {
                                群成员[j].临时编号 = k;
                                k += 1;
                            }
                        }
                        小聊天群.群成员 = 群成员;
                        小聊天群.待加入确认 = false;
                    }
                }
            } catch (Exception e) {
            }
        }
    }

    void 清除小聊天群成员列表(Group_Small 小聊天群) {
        File 文件 = new File(运行环境.getFilesDir() + "/" + 获取英语讯宝地址() + "/smallgroup/" + 小聊天群.群主.英语讯宝地址 + "#" + 小聊天群.编号 + ".sspk");
        if (文件.exists()) {
            文件.delete();
        }
    }

//    public void 记录运行步骤(String 文字) {
//        记录运行步骤(文字, 运行环境);
//    }
//
//    static void 记录运行步骤(String 文字, Context 运行环境) {
//        FileWriter 文件写入器;
//        try {
//            MyDate 当前UTC时间 = new MyDate(SharedMethod.获取当前UTC时间());
//            File 文件 = new File(运行环境.getExternalCacheDir() + "/records.txt");
//            文件写入器 = new FileWriter(文件, true);
//            PrintWriter 打印器 = new PrintWriter(文件写入器);
//            打印器.println(当前UTC时间.获取("HH:mm:ss") + " " + 文字);
//            打印器.flush();
//            文件写入器.flush();
//            打印器.close();
//            文件写入器.close();
//        } catch (Exception e) {
//        }
//    }

}
