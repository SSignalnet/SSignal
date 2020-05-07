package net.ssignal.protocols;

import net.ssignal.BuildConfig;
import net.ssignal.util.SharedMethod;

import java.util.Locale;

import static net.ssignal.network.encodeURI.替换URI敏感字符;

public class ProtocolPath {

//    public final static String IP地址_调试 = "10.0.2.2";   //用模拟器测试
    public final static String IP地址_调试 = "192.168.43.48";   //用于安卓真机测试。电脑连接安卓手机的热点，用实际局域网IP地址替换前面的IP地址
    public final static String HTTP域名_调试 = "https://" + IP地址_调试 + ":";

    public final static boolean 调试时访问真实网站 = true;

    public final static int 测试域名1中心服务器本机IIS调试端口_SSL = 44327;
    public final static int 测试域名1传送服务器本机IIS调试端口_SSL = 44341;
    public final static int 测试域名1传送服务器本机IIS调试端口 = 49677;
    public final static int 测试域名1小宇宙中心服务器本机IIS调试端口_SSL = 44367;
    public final static int 测试域名1小宇宙中心服务器本机IIS调试端口 = 49681;
    final static int 测试域名1主站服务器本机IIS调试端口_SSL = 44325;
    public final static int 测试域名1大群聊服务器本机IIS调试端口_SSL = 44351;
    public final static int 测试域名1大群聊服务器本机IIS调试端口 = 49679;

    public static String 获取中心服务器访问路径开头(String 域名) {
        if (!BuildConfig.DEBUG || 调试时访问真实网站) {
            return "https://" + ProtocolParameters.讯宝中心服务器主机名 + "." + 域名 + "/?";
        } else {
            return HTTP域名_调试 + 测试域名1中心服务器本机IIS调试端口_SSL + "/?";
        }
    }

    public static String 获取传送服务器访问路径开头(String 主机名, String 域名, boolean 媒体) {
        if (!媒体) {
            if (!BuildConfig.DEBUG || 调试时访问真实网站) {
                return "https://" + 主机名 + "." + 域名 + "/?";
            } else {
                return HTTP域名_调试 + 测试域名1传送服务器本机IIS调试端口_SSL + "/?";
            }
        } else {
            if (!BuildConfig.DEBUG || 调试时访问真实网站) {
                return "https://" + 主机名 + "." + 域名 + "/media/?";
            } else {
                return HTTP域名_调试 + 测试域名1传送服务器本机IIS调试端口_SSL + "/media/?";
            }
        }
    }

    public static String 获取大聊天群服务器访问路径开头(String 子域名, boolean 媒体) {
        if (!媒体) {
            if (!BuildConfig.DEBUG || 调试时访问真实网站) {
                return "https://" + 子域名 + "/?";
            } else {
                return HTTP域名_调试 + 测试域名1大群聊服务器本机IIS调试端口_SSL + "/?";
            }
        } else {
            if (!BuildConfig.DEBUG || 调试时访问真实网站) {
                return "https://" + 子域名 + "/media/?";
            } else {
                return HTTP域名_调试 + 测试域名1大群聊服务器本机IIS调试端口_SSL + "/media/?";
            }
        }
    }

    public static String 获取讯友头像路径(String 讯宝地址, String 主机名, long 头像更新时间) {
        String 段[] = 讯宝地址.split(ProtocolParameters.讯宝地址标识);
        String 图标路径;
        if (!BuildConfig.DEBUG || 调试时访问真实网站) {
            图标路径 = "https://" + 主机名 + "." + 段[1];
        } else {
            图标路径 = HTTP域名_调试 + 测试域名1传送服务器本机IIS调试端口_SSL;
        }
        return 图标路径 + "/icons/" + 段[0] + ".jpg" + (头像更新时间 == 0? "":"?v=" + 头像更新时间);
    }

    public static String 获取大聊天群图标路径(String 子域名, long 群编号, long 图标更新时间) {
        String 图标路径;
        if (!BuildConfig.DEBUG || 调试时访问真实网站) {
            图标路径 = "https://" + 子域名;
        } else {
            图标路径 = HTTP域名_调试 + 测试域名1大群聊服务器本机IIS调试端口_SSL;
        }
        return 图标路径 + "/icons/" + 群编号 + ".jpg" + (图标更新时间 == 0? "":"?v=" + 图标更新时间);
    }

    public static String 获取陌生人头像路径() {
        return "ss.jpg";
    }

    public static String 获取我的头像路径(String 英语用户名, String 主机名, long 头像更新时间, String 域名_英语) {
        if (!SharedMethod.字符串未赋值或为空(英语用户名)) {
            String 图标路径;
            if (!BuildConfig.DEBUG || 调试时访问真实网站) {
                图标路径 = "https://" + 主机名 + "." + 域名_英语;
            } else {
                图标路径 = HTTP域名_调试 + 测试域名1传送服务器本机IIS调试端口_SSL;
            }
            return 图标路径 + "/icons/" + 英语用户名 + ".jpg" + (头像更新时间 == 0 ? "" : "?v=" + 头像更新时间);
        } else {
            return 获取陌生人头像路径();
        }
    }

    public static String 获取当前用户小宇宙的访问路径(String 当前用户英语用户名, String 域名_英语) {
        String 路径;
        if (!BuildConfig.DEBUG || 调试时访问真实网站) {
            路径 = "https://" + ProtocolParameters.讯宝小宇宙中心服务器主机名 + "." + 域名_英语;
        } else {
            路径 = HTTP域名_调试 + 测试域名1小宇宙中心服务器本机IIS调试端口_SSL;
        }
        return 路径 + "/mytu/?LanguageCode=" + Locale.getDefault().getISO3Language() + "&EnglishUsername=" + 替换URI敏感字符(当前用户英语用户名);
    }

    public static String 获取讯友小宇宙的访问路径(String 讯友英语讯宝地址, String 当前用户英语讯宝地址) {
        String 段[] = 讯友英语讯宝地址.split(ProtocolParameters.讯宝地址标识);
        String 路径;
        if (!BuildConfig.DEBUG || 调试时访问真实网站) {
            路径 = "https://" + ProtocolParameters.讯宝小宇宙中心服务器主机名 + "." + 段[1];
        } else {
            路径 = HTTP域名_调试 + 测试域名1小宇宙中心服务器本机IIS调试端口_SSL;
        }
        return 路径 + "/tu/?LanguageCode=" + Locale.getDefault().getISO3Language() + "&EnglishSSAddress=" + 替换URI敏感字符(当前用户英语讯宝地址);
    }

    public static String 获取大聊天群小宇宙的访问路径(String 子域名) {
        String 路径;
        if (!BuildConfig.DEBUG || 调试时访问真实网站) {
            路径 = "https://" + 子域名;
        } else {
            路径 = HTTP域名_调试 + 测试域名1大群聊服务器本机IIS调试端口_SSL;
        }
        return 路径 + "/tu/?LanguageCode=" + Locale.getDefault().getISO3Language();
    }

    public static String 获取主站首页的访问路径() {
        if (!BuildConfig.DEBUG || 调试时访问真实网站) {
            return "https://" + ProtocolParameters.讯宝移动主站服务器主机名 + "." + ProtocolParameters.讯宝网络域名_英语 + "/" ;
        } else {
            return HTTP域名_调试 + 测试域名1主站服务器本机IIS调试端口_SSL + "/";
        }
    }

    public static String 获取系统管理页面的访问路径(String 域名_英语) {
        String 路径;
        if (!BuildConfig.DEBUG || ProtocolPath.调试时访问真实网站) {
            路径 = "https://" + ProtocolParameters.讯宝中心服务器主机名 + "." + 域名_英语 ;
        } else {
            路径 = ProtocolPath.HTTP域名_调试 + ProtocolPath.测试域名1中心服务器本机IIS调试端口_SSL;
        }
        return 路径 + "/Management.aspx";
    }

}
