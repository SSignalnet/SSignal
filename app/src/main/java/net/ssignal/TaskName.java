package net.ssignal;

import static net.ssignal.language.Text.界面文字;
import static net.ssignal.language.Text.组名_任务;

class TaskName {

    static String 任务名称_登录,
                  任务名称_注册,
                  任务名称_验证,
                  任务名称_忘记,
                  任务名称_重设;

    static String 任务名称_注销,
                  任务名称_用户名,
                  任务名称_账户,
                  任务名称_图标,
                  任务名称_密码,
                  任务名称_邮箱地址,
                  任务名称_手机号,
                  任务名称_验证邮箱地址,
                  任务名称_验证手机号,
                  任务名称_获取密钥,
                  任务名称_添加黑域,
                  任务名称_添加白域,
                  任务名称_移除黑域,
                  任务名称_移除白域,
                  任务名称_无法及时收到消息;

    static String 任务名称_添加讯友,
                  任务名称_删除讯友,
                  任务名称_清理黑名单,
                  任务名称_重命名标签;

    static String 任务名称_发送图片,
                  任务名称_发送原图,
                  任务名称_发送照片,   //摄像头拍摄后立即发送
                  任务名称_发送短视频,   //摄像头录制后立即发送
                  任务名称_发送文件,
                  任务名称_添加新标签,
                  任务名称_添加现有标签,
                  任务名称_移除标签,
                  任务名称_备注,
                  任务名称_拉黑;

    static String 任务名称_创建小聊天群,
                  任务名称_创建大聊天群,
                  任务名称_加入大聊天群,
                  任务名称_邀请,
                  任务名称_退出聊天群,
                  任务名称_昵称,
                  任务名称_删减成员,
                  任务名称_群名称,
                  任务名称_修改角色,
                  任务名称_解散聊天群;

    static String 任务名称_发送语音,
                  任务名称_发送文字;

    static String 任务名称_连接传送服务器,
                  任务名称_小宇宙,
                  任务名称_发流星语,
                  任务名称_发布商品;

    static String 任务名称_取消,
                  任务名称_添加可注册者,
                  任务名称_移除可注册者,
                  任务名称_商品编辑者;
                  //任务名称_关闭;

    static String 任务名称_报表,
                  任务名称_新传送服务器,
                  任务名称_新大聊天群服务器,
                  任务名称_小宇宙中心服务器;

    TaskName() {
        任务名称_登录 = 界面文字.获取(组名_任务,2, "登录");
        任务名称_注册 = 界面文字.获取(组名_任务, 3, "注册");
        任务名称_验证 = "激活";       //无需其它语种
        任务名称_忘记 = 界面文字.获取(组名_任务, 4, "忘记");
        任务名称_重设 = "重设";      //无需其它语种

        任务名称_用户名 = "用户名";       //无需其它语种
        任务名称_账户 = 界面文字.获取(组名_任务, 8, "账户");
        任务名称_图标 = 界面文字.获取(组名_任务, 9, "图标");
        任务名称_密码 = 界面文字.获取(组名_任务, 15, "密码");
        任务名称_邮箱地址 = 界面文字.获取(组名_任务, 11, "邮箱地址");
        任务名称_手机号 = 界面文字.获取(组名_任务, 33, "手机号");
        任务名称_验证邮箱地址 = "验证邮箱地址";       //无需其它语种
        任务名称_验证手机号 = "验证手机号";       //无需其它语种
        任务名称_获取密钥 = "获取密钥";       //无需其它语种
        任务名称_添加黑域 = 界面文字.获取(组名_任务, 34, "添加黑域");
        任务名称_添加白域 = 界面文字.获取(组名_任务, 35, "添加白域");
        任务名称_移除黑域 = "移除黑域";       //无需其它语种
        任务名称_移除白域 = "移除白域";       //无需其它语种
        任务名称_无法及时收到消息 = 界面文字.获取(组名_任务, 44, "无法及时收到消息");

        任务名称_添加讯友 = 界面文字.获取(组名_任务, 16, "添加讯友");
        任务名称_删除讯友 = 界面文字.获取(组名_任务, 17, "删除讯友");
        任务名称_清理黑名单 = 界面文字.获取(组名_任务, 18, "清理黑名单");
        任务名称_重命名标签 = 界面文字.获取(组名_任务, 19, "重命名标签");

        任务名称_发送图片 = 界面文字.获取(组名_任务, 20, "发送图片");
        任务名称_发送原图 = 界面文字.获取(组名_任务, 21, "发送原图");
        任务名称_发送照片 = 界面文字.获取(组名_任务, 22, "发送照片");
        任务名称_发送短视频 = 界面文字.获取(组名_任务, 23, "发送短视频");
        任务名称_发送文件 = 界面文字.获取(组名_任务, 24, "发送文件");
        任务名称_添加新标签 = 界面文字.获取(组名_任务, 25, "添加新标签");
        任务名称_添加现有标签 = 界面文字.获取(组名_任务, 26, "添加现有标签");
        任务名称_移除标签 = 界面文字.获取(组名_任务, 27, "移除标签");
        任务名称_备注 = 界面文字.获取(组名_任务, 28, "备注");
        任务名称_拉黑 = 界面文字.获取(组名_任务, 29, "拉黑");

        任务名称_创建小聊天群 = 界面文字.获取(组名_任务, 30, "小聊天群");
        任务名称_创建大聊天群 = 界面文字.获取(组名_任务, 31, "大聊天群");
        任务名称_加入大聊天群 = "加入大聊天群";       //无需其它语种
        任务名称_邀请 = 界面文字.获取(组名_任务, 32, "邀请");
        任务名称_退出聊天群 = 界面文字.获取(组名_任务, 10, "退出聊天群");
        任务名称_昵称 = 界面文字.获取(组名_任务, 39, "昵称");
        任务名称_删减成员 = 界面文字.获取(组名_任务, 12, "删减成员");
        任务名称_群名称 = 界面文字.获取(组名_任务, 13, "群名称");
        任务名称_修改角色 = 界面文字.获取(组名_任务, 40, "修改角色");
        任务名称_解散聊天群 = 界面文字.获取(组名_任务, 14, "解散聊天群");

        任务名称_发送语音 = 界面文字.获取(组名_任务, 36, "发送语音");
        任务名称_发送文字 = 界面文字.获取(组名_任务, 37, "发送文字");

        任务名称_连接传送服务器 = "连接传送服务器";       //无需其它语种
        任务名称_小宇宙 = 界面文字.获取(组名_任务, 38, "小宇宙");
        任务名称_发流星语 = "发流星语";       //无需其它语种
        任务名称_发布商品 = "发布商品";       //无需其它语种

        任务名称_取消 = 界面文字.获取(组名_任务, 6, "取消");
        任务名称_添加可注册者 = 界面文字.获取(组名_任务, 41, "添加可注册者");
        任务名称_移除可注册者 = 界面文字.获取(组名_任务, 42, "移除可注册者");
        任务名称_商品编辑者 = 界面文字.获取(组名_任务, 43, "商品编辑者");
        任务名称_注销 = 界面文字.获取(组名_任务, 5, "注销");
//        任务名称_关闭 = 界面文字.获取(Text.组名_任务, 7, "关闭");

        任务名称_报表 = 界面文字.获取(组名_任务, 45, "报表");;       //无需其它语种
        任务名称_新传送服务器 = 界面文字.获取(组名_任务, 46, "新传送服务器");       //无需其它语种
        任务名称_新大聊天群服务器 = 界面文字.获取(组名_任务, 47, "新大聊天群服务器");       //无需其它语种
        任务名称_小宇宙中心服务器 = 界面文字.获取(组名_任务, 48, "小宇宙中心服务器");       //无需其它语种
    }

}
