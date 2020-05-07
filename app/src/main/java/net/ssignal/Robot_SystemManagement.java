package net.ssignal;

import android.os.Message;

import net.ssignal.network.httpSetting;
import net.ssignal.protocols.ProtocolPath;
import net.ssignal.protocols.ProtocolParameters;
import net.ssignal.protocols.SSPackageReader;
import net.ssignal.util.Constants;
import net.ssignal.util.SharedMethod;

import static net.ssignal.User.当前用户;
import static net.ssignal.language.Text.界面文字;
import static net.ssignal.network.encodeURI.替换URI敏感字符;

public class Robot_SystemManagement extends Robot {

    Robot_SystemManagement(Fragment_Chating 聊天控件) {
        this.聊天控件 = 聊天控件;
        跨线程调用器 = new MyHandler(this);
    }

    @Override
    void 回答(String 用户输入, long 时间) {
        if (用户输入.equalsIgnoreCase(TaskName.任务名称_报表)) {
            获取报表(用户输入);
        } else if (用户输入.equalsIgnoreCase(TaskName.任务名称_新传送服务器)) {
            添加新传送服务器(用户输入);
        } else if (用户输入.equalsIgnoreCase(TaskName.任务名称_新大聊天群服务器)) {
            添加新大聊天群服务器(用户输入);
        } else if (用户输入.equalsIgnoreCase(TaskName.任务名称_小宇宙中心服务器)) {
            添加小宇宙中心服务器(用户输入);
        } else if (用户输入.equalsIgnoreCase(TaskName.任务名称_添加可注册者)) {
            添加可注册者(用户输入);
        } else if (用户输入.equalsIgnoreCase(TaskName.任务名称_移除可注册者)) {
            移除可注册者(用户输入);
        } else if (用户输入.equalsIgnoreCase(TaskName.任务名称_商品编辑者)) {
            设置商品编辑者(用户输入);
        } else if (用户输入.equalsIgnoreCase(TaskName.任务名称_取消)) {
            if (任务 != null) {
                任务.结束();
                任务 = null;
                说(界面文字.获取(16, "已取消。"));
            } else {
                说(界面文字.获取(93, "需要我做什么？"));
            }
        } else {
            任务接收用户输入(用户输入);
        }
    }

    private void 获取报表(String 用户输入) {
        if (任务 != null) { 任务.结束(); }
        任务 = new Task(用户输入, this);
        说(界面文字.获取(7, "请稍等。"));
        启动HTTPS访问线程(new httpSetting(ProtocolPath.获取中心服务器访问路径开头(当前用户.域名_英语) + "C=GetReport&UserID=" + 当前用户.编号 + "&Credential=" + 替换URI敏感字符(当前用户.凭据_中心服务器)));
    }

    private void 添加新传送服务器(String 用户输入) {
        if (任务 != null) { 任务.结束(); }
        if (SharedMethod.字符串未赋值或为空(当前用户.凭据_管理员)) {
            说(界面文字.获取(265, "请先登录管理中心。"));
            return;
        }
        任务 = new Task(用户输入, 输入框, this);
        任务.添加步骤(Task.任务步骤_传送服务器主机名, 界面文字.获取(103, "请输入主机名。要以#%开头，其后的2个字符是国家代码，接着是2个字符州省代码，最后是数字代表第几台服务器，如 #%cnbj01（域名：#%cnbj01.#%）。", new Object[] {ProtocolParameters.讯宝中心服务器主机名, ProtocolParameters.讯宝中心服务器主机名, ProtocolParameters.讯宝中心服务器主机名, 当前用户.域名_英语}));
        任务.添加步骤(Task.任务步骤_服务器网络地址, 界面文字.获取(270, "请输入服务器的IP地址。"));
        说(任务.获取当前步骤提示语());
    }

    private void 添加新大聊天群服务器(String 用户输入) {
        if (任务 != null) { 任务.结束(); }
        if (SharedMethod.字符串未赋值或为空(当前用户.凭据_管理员)) {
            说(界面文字.获取(265, "请先登录管理中心。"));
            return;
        }
        任务 = new Task(用户输入, 输入框, this);
        任务.添加步骤(Task.任务步骤_大聊天群服务器主机名, 界面文字.获取(103, "请输入主机名。要以#%开头，其后的2个字符是国家代码，接着是2个字符州省代码，最后是数字代表第几台服务器，如 #%cnbj01（域名：#%cnbj01.#%）。", new Object[] {ProtocolParameters.讯宝大聊天群服务器主机名前缀, ProtocolParameters.讯宝大聊天群服务器主机名前缀, ProtocolParameters.讯宝大聊天群服务器主机名前缀, 当前用户.域名_英语}));
        任务.添加步骤(Task.任务步骤_服务器网络地址, 界面文字.获取(270, "请输入服务器的IP地址。"));
        说(任务.获取当前步骤提示语());
    }

    private void 添加可注册者(String 用户输入) {
        if (任务 != null) { 任务.结束(); }
        if (SharedMethod.字符串未赋值或为空(当前用户.凭据_管理员)) {
            说(界面文字.获取(265, "请先登录管理中心。"));
            return;
        }
        任务 = new Task(用户输入, 输入框, this);
        任务.添加步骤(Task.任务步骤_添加移除可注册者, 界面文字.获取(301, "请输入将要获得注册权的电子邮箱地址。如果允许任何人都可注册，请输入*。"));
        说(任务.获取当前步骤提示语());
    }

    private void 移除可注册者(String 用户输入) {
        if (任务 != null) { 任务.结束(); }
        if (SharedMethod.字符串未赋值或为空(当前用户.凭据_管理员)) {
            说(界面文字.获取(265, "请先登录管理中心。"));
            return;
        }
        任务 = new Task(用户输入, 输入框, this);
        任务.添加步骤(Task.任务步骤_添加移除可注册者, 界面文字.获取(302, "请输入将要取消注册权的电子邮箱地址。如果不允许任何人都可注册，请输入*。"));
        说(任务.获取当前步骤提示语());
    }

    private void 设置商品编辑者(String 用户输入) {
        if (任务 != null) { 任务.结束(); }
        if (SharedMethod.字符串未赋值或为空(当前用户.凭据_管理员)) {
            说(界面文字.获取(265, "请先登录管理中心。"));
            return;
        }
        任务 = new Task(用户输入, 输入框, this);
        任务.添加步骤(Task.任务步骤_设置商品编辑者, 界面文字.获取(312, "请输入一个英语讯宝地址，他/她将拥有编辑商品的权限。"));
        说(任务.获取当前步骤提示语());
    }

    private void 添加小宇宙中心服务器(String 用户输入) {
        if (任务 != null) { 任务.结束(); }
        if (SharedMethod.字符串未赋值或为空(当前用户.凭据_管理员)) {
            说(界面文字.获取(265, "请先登录管理中心。"));
            return;
        }
        任务 = new Task(用户输入, 输入框, this);
        任务.添加步骤(Task.任务步骤_服务器网络地址, 界面文字.获取(270, "请输入服务器的IP地址。"));
        说(任务.获取当前步骤提示语());
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
                        启动HTTPS访问线程(任务.生成访问设置());
                    }
                } else {
                    说(结果);
                }
                return;
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
                        if (任务.名称.equalsIgnoreCase(TaskName.任务名称_添加可注册者) || 任务.名称.equalsIgnoreCase(TaskName.任务名称_移除可注册者)){
                            说(界面文字.获取(245, "完成。"));
                        } else if (任务.名称.equalsIgnoreCase(TaskName.任务名称_商品编辑者)) {
                            说(界面文字.获取(245, "完成。"));
                            说(界面文字.获取(327, "#% 须重新登录。", new Object[] {任务.获取某步骤的输入值(Task.任务步骤_设置商品编辑者)}));
                        } else if (任务.名称.equalsIgnoreCase(TaskName.任务名称_报表)) {
                            说((String)SS包解读器.读取_有标签("报表"));
                        } else if (任务.名称.equalsIgnoreCase(TaskName.任务名称_新传送服务器)) {
                            说(界面文字.获取(154, "服务器账号创建成功。#%.#% [#%]", new Object[] {任务.获取某步骤的输入值(Task.任务步骤_传送服务器主机名), 当前用户.域名_英语, 任务.获取某步骤的输入值(Task.任务步骤_服务器网络地址)}));
                        } else if (任务.名称.equalsIgnoreCase(TaskName.任务名称_新大聊天群服务器)) {
                            说(界面文字.获取(154, "服务器账号创建成功。#%.#% [#%]", new Object[] {任务.获取某步骤的输入值(Task.任务步骤_大聊天群服务器主机名), 当前用户.域名_英语, 任务.获取某步骤的输入值(Task.任务步骤_服务器网络地址)}));
                        } else if (任务.名称.equalsIgnoreCase(TaskName.任务名称_小宇宙中心服务器)) {
                            说(界面文字.获取(154, "服务器账号创建成功。#%.#% [#%]", new Object[] {ProtocolParameters.讯宝小宇宙中心服务器主机名, 当前用户.域名_英语, 任务.获取某步骤的输入值(Task.任务步骤_服务器网络地址)}));
                        }
                        break;
                    case ProtocolParameters.查询结果_无权操作:
                        说(界面文字.获取(154, "你无权进行此项操作。"));
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

}
