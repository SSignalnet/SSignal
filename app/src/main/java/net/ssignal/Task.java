package net.ssignal;

import android.graphics.Color;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

import net.ssignal.network.httpSetting;
import net.ssignal.protocols.ProtocolMethods;
import net.ssignal.protocols.ProtocolPath;
import net.ssignal.protocols.Contact;
import net.ssignal.structure.Domain;
import net.ssignal.protocols.GroupMember;
import net.ssignal.protocols.Group_Small;
import net.ssignal.protocols.ProtocolParameters;
import net.ssignal.util.Constants;
import net.ssignal.util.SharedMethod;

import java.net.InetAddress;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import static net.ssignal.User.当前用户;
import static net.ssignal.language.Text.界面文字;
import static net.ssignal.language.Text.组名_任务;
import static net.ssignal.network.encodeURI.替换URI敏感字符;

class Task {

    public static final byte 任务步骤_讯宝地址 = 1;
    public static final byte 任务步骤_密码 = 2;
    public static final byte 任务步骤_验证码 = 3;
    public static final byte 任务步骤_手机号或电子邮箱地址 = 4;
    public static final byte 任务步骤_重复密码 = 5;
    public static final byte 任务步骤_英语用户名 = 6;
    public static final byte 任务步骤_重复英语用户名 = 7;
    public static final byte 任务步骤_本国语用户名 = 8;
    public static final byte 任务步骤_重复本国语用户名 = 9;
    public static final byte 任务步骤_添加讯友 = 10;
    public static final byte 任务步骤_添加讯友备注 = 11;
    public static final byte 任务步骤_删除讯友 = 12;
    public static final byte 任务步骤_取消拉黑讯友 = 13;
    public static final byte 任务步骤_添加新标签 = 14;
    public static final byte 任务步骤_添加现有标签 = 15;
    public static final byte 任务步骤_移除标签 = 16;
    public static final byte 任务步骤_原标签名称 = 17;
    public static final byte 任务步骤_新标签名称 = 18;
    public static final byte 任务步骤_修改讯友备注 = 19;
    public static final byte 任务步骤_传送服务器主机名 = 20;
    public static final byte 任务步骤_服务器网络地址 = 21;
    public static final byte 任务步骤_小聊天群名称 = 22;
    public static final byte 任务步骤_小聊天群邀请 = 23;
    public static final byte 任务步骤_小聊天群删减成员 = 24;
    public static final byte 任务步骤_当前密码 = 25;
    public static final byte 任务步骤_手机号 = 26;
    public static final byte 任务步骤_电子邮箱地址 = 27;
    public static final byte 任务步骤_添加黑域 = 28;
    public static final byte 任务步骤_添加白域 = 29;
    public static final byte 任务步骤_移除黑白域 = 30;
    public static final byte 任务步骤_大聊天群名称 = 31;
    public static final byte 任务步骤_大聊天群估计成员数 = 32;
    public static final byte 任务步骤_大聊天群邀请 = 33;
    public static final byte 任务步骤_大聊天群删减成员 = 34;
    public static final byte 任务步骤_大聊天群服务器主机名 = 35;
    public static final byte 任务步骤_大聊天群修改角色 = 36;
    public static final byte 任务步骤_大聊天群某成员的新角色 = 37;
    public static final byte 任务步骤_大聊天群昵称 = 38;
    public static final byte 任务步骤_添加移除可注册者 = 39;
    public static final byte 任务步骤_域名 = 40;
    public static final byte 任务步骤_设置商品编辑者 = 41;

    private class 步骤_复合数据 {
        byte 步骤代码;
        String 提示语, 输入值;
        boolean 是空字符串;
    }

    String 名称;
    private 步骤_复合数据 步骤[];
    int 步骤数量;
    private EditText 输入框;
    private Robot 机器人;

    String 身份码类型;
    long 验证码添加时间;
    boolean 需要获取验证码图片;

    Task(String 名称, Robot 机器人) {
        this.名称 = 名称;
        this.机器人 = 机器人;
        机器人.提示新任务();
    }

    Task(String 名称, EditText 输入框, Robot 机器人) {
        this.名称 = 名称;
        this.输入框 = 输入框;
        this.机器人 = 机器人;
        步骤 = new 步骤_复合数据[10];
        机器人.提示新任务();
    }

    Task(String 名称, EditText 输入框, Robot 机器人, int 步骤数量) {
        this.名称 = 名称;
        this.输入框 = 输入框;
        this.机器人 = 机器人;
        步骤 = new 步骤_复合数据[步骤数量];
        机器人.提示新任务();
    }

    void 添加步骤(byte 步骤代码, String 提示语) {
        添加步骤(步骤代码, 提示语, null);
    }

    void 添加步骤(byte 步骤代码, String 提示语, String 输入值) {
        if (步骤数量 > 0 && SharedMethod.字符串未赋值或为空(输入值)) {
            for (int i = 0; i < 步骤数量; i++) {
                if (步骤[i].步骤代码 == 步骤代码) {
                    步骤[i].输入值 = null;
                    return;
                }
            }
        }
        步骤_复合数据 新步骤 = new 步骤_复合数据();
        新步骤.步骤代码 = 步骤代码;
        新步骤.提示语 = 提示语;
        新步骤.输入值 = 输入值;
        步骤[步骤数量] = 新步骤;
        步骤数量 += 1;
    }

    String 获取当前步骤提示语() {
        int i;
        for (i = 0; i < 步骤数量; i++) {
            if (SharedMethod.字符串未赋值或为空(步骤[i].输入值) && !步骤[i].是空字符串) {
                break;
            }
        }
        if (i < 步骤数量) {
            switch (步骤[i].步骤代码) {
                case Task.任务步骤_密码:
                case Task.任务步骤_重复密码:
                case Task.任务步骤_当前密码:
                    输入框.setInputType(EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_VARIATION_PASSWORD);
                    输入框.setBackgroundColor(Color.parseColor("#FF9999"));
                    机器人.正在输入密码 = true;
                    break;
                default:
                    输入框.setInputType(EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_VARIATION_NORMAL);
                    输入框.setBackgroundColor(Color.WHITE);
                    机器人.正在输入密码 = false;
            }
            if (步骤[i].步骤代码 == Task.任务步骤_验证码) {
                if (需要获取验证码图片) {
                    String 域名;
                    if (名称.equals(TaskName.任务名称_注册)) {
                        域名 = 获取某步骤的输入值(Task.任务步骤_域名);
                    } else {
                        String 讯宝地址 = 获取某步骤的输入值(Task.任务步骤_讯宝地址);
                        String 段[] = 讯宝地址.split(ProtocolParameters.讯宝地址标识);
                        域名 = 段[1];
                    }
                    机器人.启动HTTPS访问线程(new httpSetting(ProtocolPath.获取中心服务器访问路径开头(域名) + "C=GetVCodePicture"));
                    return 界面文字.获取(36, "正在获取验证码图片。");
                }
            }
            return 步骤[i].提示语;
        } else {
            return null;
        }
    }

    String 保存当前步骤输入值(String 文本) {
        int i;
        for (i = 0; i < 步骤数量; i++) {
            if (SharedMethod.字符串未赋值或为空(步骤[i].输入值)) {
                break;
            }
        }
        if (i < 步骤数量) {
            int j;
            switch (步骤[i].步骤代码) {
                case Task.任务步骤_添加讯友:
                    if (文本.length() > ProtocolParameters.最大值_讯宝和电子邮箱地址长度) {
                        return 界面文字.获取(3, "长度不能超过#%个字符。请重新输入。", new Object[]{ProtocolParameters.最大值_讯宝和电子邮箱地址长度});
                    }
                    if (!ProtocolMethods.是否是有效的讯宝或电子邮箱地址(文本)) {
                        return 界面文字.获取(8, "格式不正确。请重新输入。");
                    }
                    if (文本.equalsIgnoreCase(当前用户.获取英语讯宝地址()) || 文本.equals(当前用户.获取本国语讯宝地址())) {
                        return 界面文字.获取(106, "这是你自己的讯宝地址。");
                    }
                    if (当前用户.查找讯友(文本) != null) {
                        return 界面文字.获取(105, "此讯友已添加。请输入一个未添加的讯宝地址。");
                    }
                    break;
                case Task.任务步骤_删除讯友:
                    Contact 某一讯友;
                    if (SharedMethod.是否是整数(文本)) {
                        short 编号 = Short.parseShort(文本);
                        if (当前用户.讯友目录 == null) {
                            return 界面文字.获取(84, "请输入正确的编号。");
                        }
                        if (编号 < 1 || 编号 > 当前用户.讯友目录.length) {
                            return 界面文字.获取(84, "请输入正确的编号。");
                        }
                        某一讯友 = 当前用户.讯友目录[编号 - 1];
                    } else {
                        某一讯友 = 当前用户.查找讯友(文本);
                        if (某一讯友 == null) {
                            return 界面文字.获取(120, "在你的讯友录中不存在。");
                        }
                    }
                    Group_Small[] 加入的群 = 当前用户.加入的小聊天群;
                    if (加入的群 != null) {
                        for (j = 0; j < 加入的群.length; j++) {
                            if (加入的群[j].群主.英语讯宝地址.equals(某一讯友.英语讯宝地址)) {
                                return 界面文字.获取(177, "你加入了此讯友创建的聊天群。");
                            }
                        }
                    }
                    文本 = 某一讯友.英语讯宝地址;
                    break;
                case Task.任务步骤_添加讯友备注:
                    if (文本.length() > ProtocolParameters.最大值_讯友备注字符数) {
                        return 界面文字.获取(3, "长度不能超过#%个字符。请重新输入。", new Object[]{ProtocolParameters.最大值_讯友备注字符数});
                    }
                    break;
                case Task.任务步骤_添加现有标签:
                case Task.任务步骤_添加新标签:
                    if (文本.length() > ProtocolParameters.最大值_讯友标签字符数) {
                        return 界面文字.获取(3, "长度不能超过#%个字符。请重新输入。", new Object[]{ProtocolParameters.最大值_讯友标签字符数});
                    }
                    某一讯友 = 机器人.聊天控件.聊天对象.讯友或群主;
                    if (文本.equalsIgnoreCase(某一讯友.标签一) || 文本.equalsIgnoreCase(某一讯友.标签二)) {
                        return 界面文字.获取(132, "此标签已添加过了。请输入一个新的标签名称。");
                    }
                    Contact 讯友目录[] = 当前用户.讯友目录;
                    for (j = 0; j < 讯友目录.length; j++) {
                        if (文本.equalsIgnoreCase(讯友目录[j].标签一) || 文本.equalsIgnoreCase(讯友目录[j].标签二)) {
                            break;
                        }
                    }
                    if (步骤[i].步骤代码 == Task.任务步骤_添加现有标签) {
                        if (j == 讯友目录.length) {
                            return 界面文字.获取(139, "这不是现有的标签名称。");
                        }
                    } else {
                        if (j < 讯友目录.length) {
                            return 界面文字.获取(136, "这不是新标签名称。");
                        }
                    }
                    break;
                case Task.任务步骤_移除标签:
                    某一讯友 = 机器人.聊天控件.聊天对象.讯友或群主;
                    if (!文本.equalsIgnoreCase(某一讯友.标签一) && !文本.equalsIgnoreCase(某一讯友.标签二)) {
                        return 界面文字.获取(147, "请输入目前的标签名称。");
                    }
                    break;
                case Task.任务步骤_原标签名称:
                    讯友目录 = 当前用户.讯友目录;
                    for (j = 0; j < 讯友目录.length; j++) {
                        if (文本.equalsIgnoreCase(讯友目录[j].标签一) || 文本.equalsIgnoreCase(讯友目录[j].标签二)) {
                            break;
                        }
                    }
                    if (j == 讯友目录.length) {
                        return 界面文字.获取(146, "请选择已有的标签名称。");
                    }
                    break;
                case Task.任务步骤_新标签名称:
                    if (文本.length() > ProtocolParameters.最大值_讯友标签字符数) {
                        return 界面文字.获取(3, "长度不能超过#%个字符。请重新输入。", new Object[]{ProtocolParameters.最大值_讯友标签字符数});
                    }
                    break;
                case Task.任务步骤_修改讯友备注:
                    if (文本.length() > ProtocolParameters.最大值_讯友备注字符数) {
                        return 界面文字.获取(3, "长度不能超过#%个字符。请重新输入。", new Object[]{ProtocolParameters.最大值_讯友备注字符数});
                    }
                    if (文本.equals(机器人.聊天控件.聊天对象.讯友或群主.备注)) {
                        return 界面文字.获取(155, "新备注与当前的备注没有任何差异。请重新输入。");
                    }
                    break;
                case Task.任务步骤_取消拉黑讯友:
                    if (SharedMethod.是否是整数(文本)) {
                        short 编号 = Short.parseShort(文本);
                        if (当前用户.讯友目录 == null) {
                            return 界面文字.获取(84, "请输入正确的编号。");
                        }
                        if (编号 < 1 || 编号 > 当前用户.讯友目录.length) {
                            return 界面文字.获取(84, "请输入正确的编号。");
                        }
                        某一讯友 = 当前用户.讯友目录[编号 - 1];
                    } else {
                        某一讯友 = 当前用户.查找讯友(文本);
                        if (某一讯友 == null) {
                            return 界面文字.获取(120, "在你的讯友录中不存在。");
                        }
                    }
                    if (!某一讯友.拉黑) {
                        return 界面文字.获取(121, "你未将此讯友拉黑。");
                    }
                    文本 = 某一讯友.英语讯宝地址;
                    break;
                case Task.任务步骤_添加黑域:
                    Domain 可选域名[] = ((Robot_MainControl) 机器人).添加黑域时统计可选域名();
                    if (可选域名 == null) {
                        return 界面文字.获取(241, "请从上面列出的域名中选择。");
                    }
                    for (i = 0; i < 可选域名.length; i++) {
                        if (文本.equals(可选域名[i].英语)) {
                            break;
                        }
                    }
                    if (i == 可选域名.length) {
                        return 界面文字.获取(241, "请从上面列出的域名中选择。");
                    }
                    break;
                case Task.任务步骤_添加白域:
                    可选域名 = ((Robot_MainControl) 机器人).添加白域时统计可选域名();
                    if (可选域名 == null) {
                        return 界面文字.获取(241, "请从上面列出的域名中选择。");
                    }
                    for (i = 0; i < 可选域名.length; i++) {
                        if (文本.equals(可选域名[i].英语)) {
                            break;
                        }
                    }
                    if (i == 可选域名.length) {
                        return 界面文字.获取(241, "请从上面列出的域名中选择。");
                    }
                    break;
                case Task.任务步骤_小聊天群名称:
                    if (文本.length() > ProtocolParameters.最大值_群名称字符数) {
                        return 界面文字.获取(3, "长度不能超过#%个字符。请重新输入。", new Object[]{ProtocolParameters.最大值_群名称字符数});
                    }
                    加入的群 = 当前用户.加入的小聊天群;
                    if (加入的群 != null) {
                        String 英语讯宝地址 = 当前用户.获取英语讯宝地址();
                        for (j = 0; j < 加入的群.length; j++) {
                            if (英语讯宝地址.equals(加入的群[j].群主.英语讯宝地址)) {
                                if (文本.equalsIgnoreCase(加入的群[j].备注)) {
                                    return 界面文字.获取(184, "此名称已用于其它聊天群。");
                                }
                            }
                        }
                    }
                    break;
                case Task.任务步骤_大聊天群名称:
                    if (文本.length() > ProtocolParameters.最大值_群名称字符数) {
                        return 界面文字.获取(3, "长度不能超过#%个字符。请重新输入。", new Object[]{ProtocolParameters.最大值_群名称字符数});
                    }
                    break;
                case Task.任务步骤_大聊天群估计成员数:
                    if (!SharedMethod.是否是整数(文本)) {
                        return 界面文字.获取(298, "请输入一个正整数。");
                    }
                    int 成员数 = Integer.parseInt(文本);
                    if (成员数 < 1) {
                        return 界面文字.获取(298, "请输入一个正整数。");
                    }
                    文本 = String.valueOf(成员数);
                    break;
                case Task.任务步骤_小聊天群邀请:
                case Task.任务步骤_大聊天群邀请:
                    if (SharedMethod.是否是整数(文本)) {
                        short 编号 = Short.parseShort(文本);
                        if (当前用户.讯友目录 == null) {
                            return 界面文字.获取(84, "请输入正确的编号。");
                        }
                        if (编号 < 1 || 编号 > 当前用户.讯友目录.length) {
                            return 界面文字.获取(84, "请输入正确的编号。");
                        }
                        某一讯友 = 当前用户.讯友目录[编号 - 1];
                    } else {
                        某一讯友 = 当前用户.查找讯友(文本);
                        if (某一讯友 == null) {
                            return 界面文字.获取(120, "在你的讯友录中不存在。");
                        }
                    }
                    if (某一讯友.拉黑) {
                        return 界面文字.获取(160, "你已将此讯友拉黑。");
                    }
                    文本 = 某一讯友.英语讯宝地址;
                    GroupMember 群成员[];
                    if (步骤[i].步骤代码 == Task.任务步骤_小聊天群邀请) {
                        群成员 =  机器人.聊天控件.聊天对象.小聊天群.群成员;
                        for (j = 0; j < 群成员.length; j++) {
                            if (群成员[j].英语讯宝地址.equals(文本)) {
                                if (群成员[j].角色 == ProtocolParameters.群角色_邀请加入_可以发言) {
                                    return 界面文字.获取(191, "已给此讯友发送了邀请。要想再次发送，请先[#%]。", new Object[] {界面文字.获取(组名_任务, 12, "删减成员")});
                                } else {
                                    return 界面文字.获取(192, "此讯友已加入本群。");
                                }
                            }
                        }
                    }
                    break;
                case Task.任务步骤_小聊天群删减成员:
                    群成员 = 机器人.聊天控件.聊天对象.小聊天群.群成员;
                    for (j = 0; j < 群成员.length; j++) {
                        if (文本.equals(群成员[j].英语讯宝地址) || 文本.equals(群成员[j].本国语讯宝地址)) {
                            break;
                        }
                    }
                    if (j < 群成员.length) {
                        文本 = 群成员[j].英语讯宝地址;
                    } else {
                        return 界面文字.获取(290, "在群成员列表中找不到此讯宝地址。");
                    }
                    break;
                case Task.任务步骤_大聊天群昵称:
                    if (文本.length() > ProtocolParameters.最大值_讯友备注字符数) {
                        return 界面文字.获取(3, "长度不能超过#%个字符。请重新输入。", new Object[] {ProtocolParameters.最大值_讯友备注字符数});
                    }
                    break;
                case Task.任务步骤_大聊天群某成员的新角色:
                    if (!SharedMethod.是否是整数(文本)) {
                        return 界面文字.获取(292, "请输入括号内的数字。");
                    }
                    byte 角色 = Byte.parseByte(文本);
                    switch (角色) {
                        case ProtocolParameters.群角色_成员_不可发言:
                        case ProtocolParameters.群角色_成员_可以发言:
                        case ProtocolParameters.群角色_管理员:
                            break;
                        default:
                            return 界面文字.获取(292, "请输入括号内的数字。");
                    }
                    break;
                case Task.任务步骤_讯宝地址:
                    if (文本.length() > ProtocolParameters.最大值_讯宝和电子邮箱地址长度) {
                        return 界面文字.获取(3, "长度不能超过#%个字符。请重新输入。", new Object[] {ProtocolParameters.最大值_讯宝和电子邮箱地址长度});
                    }
                    if (!ProtocolMethods.是否是有效的讯宝或电子邮箱地址(文本)) {
                        return 界面文字.获取(8, "格式不正确。请重新输入。");
                    }
                    break;
                case Task.任务步骤_密码:
                case Task.任务步骤_当前密码:
                    if (文本.length() < ProtocolParameters.最小值_密码长度) {
                        return 界面文字.获取(10, "密码最少要有#%个字符。请重新输入。", new Object[] {ProtocolParameters.最小值_密码长度});
                    } else if (文本.length() > ProtocolParameters.最大值_密码长度) {
                        return 界面文字.获取(32, "密码最多只能有#%个字符。请重新输入。", new Object[]{ProtocolParameters.最大值_密码长度});
                    } else {
                        输入框.setInputType(EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_VARIATION_NORMAL);
                        输入框.setBackgroundColor(Color.WHITE);
                        机器人.正在输入密码 = false;
                    }
                    break;
                case Task.任务步骤_验证码:
                    if (文本.length() != ProtocolParameters.长度_验证码) {
                        return 界面文字.获取(11, "验证码有#%个字符。请重新输入。", new Object[] {ProtocolParameters.长度_验证码});
                    }
                    break;
                case Task.任务步骤_域名:
                    if (文本.contains(" ")) {
                        return 界面文字.获取(8, "格式不正确。请重新输入。");
                    }
                    String 段[] = 文本.split("\\.");
                    if (段.length < 2 || 段.length > 3) {
                        return 界面文字.获取(8, "格式不正确。请重新输入。");
                    }
                    for (j = 0; j < 段.length; j++) {
                        if (SharedMethod.字符串未赋值或为空(段[j])) {
                            return 界面文字.获取(8, "格式不正确。请重新输入。");
                        }
                    }
                    break;
                case Task.任务步骤_手机号或电子邮箱地址:
                    if (文本.contains("@")) {
                        if (文本.length() > ProtocolParameters.最大值_讯宝和电子邮箱地址长度) {
                            return 界面文字.获取(3, "长度不能超过#%个字符。请重新输入。", new Object[]{ProtocolParameters.最大值_讯宝和电子邮箱地址长度});
                        }
                        if (!ProtocolMethods.是否是有效的讯宝或电子邮箱地址(文本)) {
                            return 界面文字.获取(8, "格式不正确。请重新输入。");
                        }
                        身份码类型 = Constants.身份码类型_电子邮箱地址;
                    } else {
                        if (文本.length() > ProtocolParameters.最大值_手机号字符数) {
                            return 界面文字.获取(3, "长度不能超过#%个字符。请重新输入。", new Object[] {ProtocolParameters.最大值_手机号字符数});
                        }
                        if (!SharedMethod.是否是手机号(文本)) {
                            return 界面文字.获取(33, "这不是手机号码。请重新输入。");
                        }
                        身份码类型 = Constants.身份码类型_手机号;
                    }
                    break;
                case Task.任务步骤_重复密码:
                    if (!文本.equals(获取某步骤的输入值(Task.任务步骤_密码))) {
                        return 界面文字.获取(34, "与刚才输入的密码不一致。请重新输入。");
                    } else {
                        输入框.setInputType(EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_VARIATION_NORMAL);
                        输入框.setBackgroundColor(Color.WHITE);
                        机器人.正在输入密码 = false;
                    }
                    break;
                case Task.任务步骤_手机号:
                    if (文本.length() > ProtocolParameters.最大值_手机号字符数) {
                        return 界面文字.获取(3, "长度不能超过#%个字符。请重新输入。", new Object[] {ProtocolParameters.最大值_手机号字符数});
                    }
                    if (!SharedMethod.是否是手机号(文本)) {
                        return 界面文字.获取(33, "这不是手机号码。请重新输入。");
                    }
                    break;
                case Task.任务步骤_电子邮箱地址:
                    if (文本.length() > ProtocolParameters.最大值_讯宝和电子邮箱地址长度) {
                        return 界面文字.获取(3, "长度不能超过#%个字符。请重新输入。", new Object[]{ProtocolParameters.最大值_讯宝和电子邮箱地址长度});
                    }
                    if (!ProtocolMethods.是否是有效的讯宝或电子邮箱地址(文本)) {
                        return 界面文字.获取(8, "格式不正确。请重新输入。");
                    }
                    break;
                case Task.任务步骤_英语用户名:
                    if (文本.length() < ProtocolParameters.最小值_英语用户名长度) {
                        return 界面文字.获取(161, "英语用户名的长度不能少于#%个字符。请重新输入。", new Object[] {ProtocolParameters.最小值_英语用户名长度});
                    } else if (文本.length() > ProtocolParameters.最大值_英语用户名长度) {
                        return 界面文字.获取(3, "长度不能超过#%个字符。请重新输入。", new Object[] {ProtocolParameters.最大值_英语用户名长度});
                    }
                    if (!ProtocolMethods.是否是英文用户名(文本)) {
                        return 界面文字.获取(9, "此用户名格式不正确。请重新输入。");
                    }
                    if (!SharedMethod.字符串未赋值或为空(当前用户.电子邮箱地址)) {
                        if (当前用户.电子邮箱地址.endsWith(ProtocolParameters.讯宝地址标识 + 当前用户.域名_英语)) {
                            if (!当前用户.电子邮箱地址.startsWith(文本 + ProtocolParameters.讯宝地址标识)) {
                                return 界面文字.获取(303, "请让你的英语讯宝地址与你的电子邮箱地址一致。");
                            }
                        }
                    }
                    机器人.说(界面文字.获取(65, "你的英语讯宝地址将为 #% 。", new Object[] {文本 + ProtocolParameters.讯宝地址标识 + 当前用户.域名_英语}));
                    break;
                case Task.任务步骤_重复英语用户名:
                    if (!文本.equals(获取某步骤的输入值(Task.任务步骤_英语用户名))) {
                        return 界面文字.获取(64, "与刚才输入的用户名不一致。请重新输入。");
                    }
                    break;
                case Task.任务步骤_本国语用户名:
                    if (文本.length() < ProtocolParameters.最小值_本国语用户名长度) {
                        return 界面文字.获取(162, "中文用户名的长度不能少于#%个字符。请重新输入。", new Object[] {ProtocolParameters.最小值_本国语用户名长度});
                    } else if (文本.length() > ProtocolParameters.最大值_本国语用户名长度) {
                        return 界面文字.获取(3, "长度不能超过#%个字符。请重新输入。", new Object[] {ProtocolParameters.最大值_本国语用户名长度});
                    }
                    if (!ProtocolMethods.是否是中文用户名(文本)) {
                        return 界面文字.获取(9, "此用户名格式不正确。请重新输入。");
                    }
                    if (!SharedMethod.字符串未赋值或为空(当前用户.电子邮箱地址)) {
                        if (当前用户.电子邮箱地址.endsWith(ProtocolParameters.讯宝地址标识 + 当前用户.域名_本国语)) {
                            if (!当前用户.电子邮箱地址.startsWith(文本 + ProtocolParameters.讯宝地址标识)) {
                                return 界面文字.获取(304, "请让你的本国语讯宝地址与你的电子邮箱地址一致。");
                            }
                        }
                    }
                    机器人.说(界面文字.获取(66, "你的中文讯宝地址将为 #% 。", new Object[] {文本 + ProtocolParameters.讯宝地址标识 + 当前用户.域名_本国语}));;
                    break;
                case Task.任务步骤_重复本国语用户名:
                    if (!文本.equals(获取某步骤的输入值(Task.任务步骤_本国语用户名))) {
                        return 界面文字.获取(64, "与刚才输入的用户名不一致。请重新输入。");
                    }
                    break;
                case Task.任务步骤_添加移除可注册者:
                    if (!文本.equals("*")) {
                        if (文本.contains("@")) {
                            if (文本.length() > ProtocolParameters.最大值_讯宝和电子邮箱地址长度) {
                                return 界面文字.获取(3, "长度不能超过#%个字符。请重新输入。", new Object[]{ProtocolParameters.最大值_讯宝和电子邮箱地址长度});
                            }
                            if (!ProtocolMethods.是否是有效的讯宝或电子邮箱地址(文本)) {
                                return 界面文字.获取(8, "格式不正确。请重新输入。");
                            }
                        } else {
                            if (文本.length() > ProtocolParameters.最大值_手机号字符数) {
                                return 界面文字.获取(3, "长度不能超过#%个字符。请重新输入。", new Object[] {ProtocolParameters.最大值_手机号字符数});
                            }
                            if (!SharedMethod.是否是手机号(文本)) {
                                return 界面文字.获取(33, "这不是手机号码。请重新输入。");
                            }
                        }
                    }
                    break;
                case Task.任务步骤_设置商品编辑者:
                    if (!ProtocolMethods.是否是有效的讯宝或电子邮箱地址(文本)) {
                        return 界面文字.获取(8, "格式不正确。请重新输入。");
                    } else {
                        String 段2[] = 文本.split(ProtocolParameters.讯宝地址标识);
                        if (!ProtocolMethods.是否是英文用户名(段2[0])) {
                            return 界面文字.获取(8, "格式不正确。请重新输入。");
                        }
                    }
                    break;
                case Task.任务步骤_传送服务器主机名:
                case Task.任务步骤_大聊天群服务器主机名:
                    if (文本.length() > ProtocolParameters.最大值_主机名字符数) {
                        return 界面文字.获取(3, "长度不能超过#%个字符。请重新输入。", new Object[] {ProtocolParameters.最大值_主机名字符数});
                    }
                    switch (步骤[i].步骤代码) {
                        case Task.任务步骤_传送服务器主机名:
                            if (!文本.startsWith(ProtocolParameters.讯宝中心服务器主机名)) {
                                return 界面文字.获取(165, "主机名要以#%开头。请重新输入。", new Object[] {ProtocolParameters.讯宝中心服务器主机名});
                            }
                            break;
                        case Task.任务步骤_大聊天群服务器主机名:
                            if (!文本.startsWith(ProtocolParameters.讯宝大聊天群服务器主机名前缀)) {
                                return 界面文字.获取(165, "主机名要以#%开头。请重新输入。", new Object[] {ProtocolParameters.讯宝大聊天群服务器主机名前缀});
                            }
                            break;
                    }
                    break;
                case Task.任务步骤_服务器网络地址:
                    try {
                        InetAddress.getByName(文本);
                    } catch (Exception e) {
                        return 界面文字.获取(166, "这不是符合规则的IP地址。请重新输入。");
                    }
                    break;
            }
            步骤[i].输入值 = 文本;
        }
        return null;
    }

    String 获取某步骤的输入值(byte 步骤代码) {
        for (int i = 0; i < 步骤数量; i++) {
            if (步骤[i].步骤代码 == 步骤代码) {
                return 步骤[i].输入值;
            }
        }
        return null;
    }

    void 清除某步骤的输入值(byte 步骤代码) {
        if (步骤数量 > 0) {
            for (int i = 0; i < 步骤数量; i++) {
                if (步骤[i].步骤代码 == 步骤代码) {
                    步骤[i].输入值 = null;
                    步骤[i].是空字符串 = false;
                    return;
                }
            }
        }
    }

    void 清除所有步骤的输入值() {
        if (步骤数量 > 0) {
            for (int i = 0; i < 步骤数量; i++) {
                步骤[i].输入值 = null;
                步骤[i].是空字符串 = false;
            }
        }
    }

    httpSetting 生成访问设置() {
        String 访问路径 = "";
        int 收发时限 = Constants.收发时限;
        机器人.说(界面文字.获取(7, "请稍等。"));
        if (名称.equalsIgnoreCase(TaskName.任务名称_创建大聊天群)) {
            收发时限 = 20000;
            访问路径 = ProtocolPath.获取中心服务器访问路径开头(当前用户.域名_英语) + "C=CreateGroup&UserID=" + 当前用户.编号 + "&Credential=" + 替换URI敏感字符(当前用户.凭据_中心服务器) + "&Name=" + 替换URI敏感字符(获取某步骤的输入值(Task.任务步骤_大聊天群名称)) + "&Number=" + 获取某步骤的输入值(Task.任务步骤_大聊天群估计成员数);
        } else if (名称.equalsIgnoreCase(TaskName.任务名称_密码)) {
            收发时限 = 20000;
            访问路径 = ProtocolPath.获取中心服务器访问路径开头(当前用户.域名_英语) + "C=ChangePassword&UserID=" + 当前用户.编号 + "&Credential=" + 替换URI敏感字符(当前用户.凭据_中心服务器) + "&NewPassword=" + 替换URI敏感字符(获取某步骤的输入值(Task.任务步骤_密码)) + "&CurrentPassword=" + 替换URI敏感字符(获取某步骤的输入值(Task.任务步骤_当前密码));
        } else if (名称.equalsIgnoreCase(TaskName.任务名称_手机号)) {
            Calendar 日历=Calendar.getInstance();
            TimeZone 时区=日历.getTimeZone();
            int 本机时间偏移量_分钟= 时区.getOffset(System.currentTimeMillis()) / 1000 / 60;
            访问路径 = ProtocolPath.获取中心服务器访问路径开头(当前用户.域名_英语) + "C=NewPhoneNumber&UserID=" + 当前用户.编号 + "&Credential=" + 替换URI敏感字符(当前用户.凭据_中心服务器) + "&PhoneNumber=" + 替换URI敏感字符(获取某步骤的输入值(Task.任务步骤_手机号)) + "&Password=" + 替换URI敏感字符(获取某步骤的输入值(Task.任务步骤_当前密码)) + "&TimezoneOffset=" + 本机时间偏移量_分钟 + "&LanguageCode=" + Locale.getDefault().getISO3Language();
        } else if (名称.equalsIgnoreCase(TaskName.任务名称_邮箱地址)) {
            Calendar 日历=Calendar.getInstance();
            TimeZone 时区=日历.getTimeZone();
            int 本机时间偏移量_分钟= 时区.getOffset(System.currentTimeMillis()) / 1000 / 60;
            访问路径 = ProtocolPath.获取中心服务器访问路径开头(当前用户.域名_英语) + "C=NewEmailAddress&UserID=" + 当前用户.编号 + "&Credential=" + 替换URI敏感字符(当前用户.凭据_中心服务器) + "&EmailAddress=" + 替换URI敏感字符(获取某步骤的输入值(Task.任务步骤_电子邮箱地址)) + "&Password=" + 替换URI敏感字符(获取某步骤的输入值(Task.任务步骤_当前密码)) + "&TimezoneOffset=" + 本机时间偏移量_分钟 + "&LanguageCode=" + Locale.getDefault().getISO3Language();
        } else if (名称.equalsIgnoreCase(TaskName.任务名称_验证手机号)) {
            访问路径 = ProtocolPath.获取中心服务器访问路径开头(当前用户.域名_英语) + "C=VerifyNewPhoneNumber&UserID=" + 当前用户.编号 + "&Credential=" + 替换URI敏感字符(当前用户.凭据_中心服务器) + "&VCodeCreatedOn=" + 验证码添加时间 + "&VerificationCode=" + 替换URI敏感字符(获取某步骤的输入值(Task.任务步骤_验证码)) + "&PhoneNumber=" + 替换URI敏感字符(获取某步骤的输入值(Task.任务步骤_手机号)) + "&LanguageCode=" + Locale.getDefault().getISO3Language();
        } else if (名称.equalsIgnoreCase(TaskName.任务名称_验证邮箱地址)) {
            访问路径 = ProtocolPath.获取中心服务器访问路径开头(当前用户.域名_英语) + "C=VerifyNewEmailAddress&UserID=" + 当前用户.编号 + "&Credential=" + 替换URI敏感字符(当前用户.凭据_中心服务器) + "&VCodeCreatedOn=" + 验证码添加时间 + "&VerificationCode=" + 替换URI敏感字符(获取某步骤的输入值(Task.任务步骤_验证码)) + "&EmailAddress=" + 替换URI敏感字符(获取某步骤的输入值(Task.任务步骤_电子邮箱地址)) + "&LanguageCode=" + Locale.getDefault().getISO3Language();
        } else if (名称.equalsIgnoreCase(TaskName.任务名称_登录)) {
            String 讯宝地址 = 获取某步骤的输入值(Task.任务步骤_讯宝地址);
            String 段[] = 讯宝地址.split(ProtocolParameters.讯宝地址标识);
            Calendar 日历=Calendar.getInstance();
            TimeZone 时区=日历.getTimeZone();
            int 本机时间偏移量_分钟= 时区.getOffset(System.currentTimeMillis()) / 1000 / 60;
            访问路径 = ProtocolPath.获取中心服务器访问路径开头(段[1]) + "C=Login&SSAddress=" + 替换URI敏感字符(讯宝地址) + "&Password=" + 替换URI敏感字符(获取某步骤的输入值(Task.任务步骤_密码)) + "&DeviceType=" + ProtocolParameters.设备类型_文本_手机 + "&TimezoneOffset=" + 本机时间偏移量_分钟;
            String 验证码 = 获取某步骤的输入值(Task.任务步骤_验证码);
            if (!SharedMethod.字符串未赋值或为空(验证码)) {
                访问路径 += "&VerificationCode=" + 替换URI敏感字符(验证码) + "&VCodeCreatedOn=" + 验证码添加时间;
            }
        } else if (名称.equalsIgnoreCase(TaskName.任务名称_注册)) {
            Calendar 日历=Calendar.getInstance();
            TimeZone 时区=日历.getTimeZone();
            int 本机时间偏移量_分钟= 时区.getOffset(System.currentTimeMillis()) / 1000 / 60;
            访问路径 = ProtocolPath.获取中心服务器访问路径开头(获取某步骤的输入值(Task.任务步骤_域名)) + "C=Register&IDtype=" + 身份码类型 + "&ID=" + 替换URI敏感字符(获取某步骤的输入值(Task.任务步骤_手机号或电子邮箱地址)) + "&Password=" + 替换URI敏感字符(获取某步骤的输入值(Task.任务步骤_密码)) + "&VCodeCreatedOn=" + 验证码添加时间 + "&VerificationCode=" + 替换URI敏感字符(获取某步骤的输入值(Task.任务步骤_验证码)) + "&TimezoneOffset=" + 本机时间偏移量_分钟 + "&LanguageCode=" + Locale.getDefault().getISO3Language();
        } else if (名称.equalsIgnoreCase(TaskName.任务名称_验证)) {
            访问路径 = ProtocolPath.获取中心服务器访问路径开头(获取某步骤的输入值(Task.任务步骤_域名)) + "C=VerifyPhoneOrEmail&IDtype=" + 身份码类型 + "&ID=" + 替换URI敏感字符(获取某步骤的输入值(Task.任务步骤_手机号或电子邮箱地址)) + "&VCodeCreatedOn=" + 验证码添加时间 + "&VerificationCode=" + 替换URI敏感字符(获取某步骤的输入值(Task.任务步骤_验证码));
        } else if (名称.equalsIgnoreCase(TaskName.任务名称_用户名)) {
            访问路径 = ProtocolPath.获取中心服务器访问路径开头(当前用户.域名_英语) + "C=SetAccountName&IDtype=" + 身份码类型 + "&ID=" + 替换URI敏感字符(获取某步骤的输入值(Task.任务步骤_手机号或电子邮箱地址)) + "&VCodeCreatedOn=" + 验证码添加时间 + "&VerificationCode=" + 替换URI敏感字符(获取某步骤的输入值(Task.任务步骤_验证码)) + "&Password=" + 替换URI敏感字符(获取某步骤的输入值(Task.任务步骤_密码)) + "&English=" + 替换URI敏感字符(获取某步骤的输入值(Task.任务步骤_英语用户名)) + "&Native=" + 替换URI敏感字符(获取某步骤的输入值(Task.任务步骤_本国语用户名));
        } else if (名称.equalsIgnoreCase(TaskName.任务名称_忘记)) {
            String 讯宝地址 = 获取某步骤的输入值(Task.任务步骤_讯宝地址);
            String 段[] = 讯宝地址.split(ProtocolParameters.讯宝地址标识);
            Calendar 日历=Calendar.getInstance();
            TimeZone 时区=日历.getTimeZone();
            int 本机时间偏移量_分钟= 时区.getOffset(System.currentTimeMillis()) / 1000 / 60;
            访问路径 = ProtocolPath.获取中心服务器访问路径开头(段[1]) + "C=ForgotPassword&SSAddress=" + 替换URI敏感字符(讯宝地址) + "&VCodeCreatedOn=" + 验证码添加时间 + "&VerificationCode=" + 替换URI敏感字符(获取某步骤的输入值(Task.任务步骤_验证码)) + "&TimezoneOffset=" + 本机时间偏移量_分钟 + "&LanguageCode=" + Locale.getDefault().getISO3Language();
        } else if (名称.equalsIgnoreCase(TaskName.任务名称_重设)) {
            String 讯宝地址 = 获取某步骤的输入值(Task.任务步骤_讯宝地址);
            String 段[] = 讯宝地址.split(ProtocolParameters.讯宝地址标识);
            收发时限 = 20000;
            访问路径 = ProtocolPath.获取中心服务器访问路径开头(段[1]) + "C=ResetPassword&SSAddress=" + 替换URI敏感字符(讯宝地址) + "&VCodeCreatedOn=" + 验证码添加时间 + "&VerificationCode=" + 替换URI敏感字符(获取某步骤的输入值(Task.任务步骤_验证码)) + "&Password=" + 替换URI敏感字符(获取某步骤的输入值(Task.任务步骤_密码));
        } else if (名称.equalsIgnoreCase(TaskName.任务名称_添加可注册者)) {
            访问路径 = ProtocolPath.获取中心服务器访问路径开头(当前用户.域名_英语) + "C=AuthorizeRegister&UserID=" + 当前用户.编号 + "&Credential=" + 替换URI敏感字符(当前用户.凭据_中心服务器) + "&ID=" + 替换URI敏感字符(获取某步骤的输入值(Task.任务步骤_添加移除可注册者)) + "&Passcode=" + 替换URI敏感字符(当前用户.凭据_管理员);
        } else if (名称.equalsIgnoreCase(TaskName.任务名称_移除可注册者)) {
            访问路径 = ProtocolPath.获取中心服务器访问路径开头(当前用户.域名_英语) + "C=UnauthorizeRegister&UserID=" + 当前用户.编号 + "&Credential=" + 替换URI敏感字符(当前用户.凭据_中心服务器) + "&ID=" + 替换URI敏感字符(获取某步骤的输入值(Task.任务步骤_添加移除可注册者)) + "&Passcode=" + 替换URI敏感字符(当前用户.凭据_管理员);
        } else if (名称.equalsIgnoreCase(TaskName.任务名称_商品编辑者)) {
            收发时限 = 20000;
            访问路径 = ProtocolPath.获取中心服务器访问路径开头(当前用户.域名_英语) + "C=SetGoodsEditor&UserID=" + 当前用户.编号 + "&Credential=" + 替换URI敏感字符(当前用户.凭据_中心服务器) + "&EnglishSSAddress=" + 替换URI敏感字符(获取某步骤的输入值(Task.任务步骤_设置商品编辑者)) + "&Passcode=" + 替换URI敏感字符(当前用户.凭据_管理员);
        } else if (名称.equalsIgnoreCase(TaskName.任务名称_新传送服务器)) {
            访问路径 = ProtocolPath.获取中心服务器访问路径开头(当前用户.域名_英语) + "C=AddServer&UserID=" + 当前用户.编号 + "&Credential=" + 替换URI敏感字符(当前用户.凭据_中心服务器) + "&Name=" + 替换URI敏感字符(获取某步骤的输入值(Task.任务步骤_传送服务器主机名)) + "&IP=" + 替换URI敏感字符(获取某步骤的输入值(Task.任务步骤_服务器网络地址)) + "&Passcode=" + 替换URI敏感字符(当前用户.凭据_管理员);
        } else if (名称.equalsIgnoreCase(TaskName.任务名称_新大聊天群服务器)) {
            访问路径 = ProtocolPath.获取中心服务器访问路径开头(当前用户.域名_英语) + "C=AddServer&UserID=" + 当前用户.编号 + "&Credential=" + 替换URI敏感字符(当前用户.凭据_中心服务器) + "&Name=" + 替换URI敏感字符(获取某步骤的输入值(Task.任务步骤_大聊天群服务器主机名)) + "&IP=" + 替换URI敏感字符(获取某步骤的输入值(Task.任务步骤_服务器网络地址)) + "&Passcode=" + 替换URI敏感字符(当前用户.凭据_管理员);
        } else if (名称.equalsIgnoreCase(TaskName.任务名称_小宇宙中心服务器)) {
            访问路径 = ProtocolPath.获取中心服务器访问路径开头(当前用户.域名_英语) + "C=AddServer&UserID=" + 当前用户.编号 + "&Credential=" + 替换URI敏感字符(当前用户.凭据_中心服务器) + "&Name=" + 替换URI敏感字符(ProtocolParameters.讯宝小宇宙中心服务器主机名) + "&IP=" + 替换URI敏感字符(获取某步骤的输入值(Task.任务步骤_服务器网络地址)) + "&Passcode=" + 替换URI敏感字符(当前用户.凭据_管理员);
        }
        return new httpSetting(访问路径, 收发时限);
    }

    void 结束() {
        步骤数量 = 0;
        步骤 = null;
        机器人.提示新任务();
        if (当前用户.显示讯友临时编号) {
            当前用户.显示讯友临时编号 = false;
            if (Fragment_Main.主窗体 != null) {
                Fragment_Main.主窗体.刷新讯友录();
            }
        }
    }

}