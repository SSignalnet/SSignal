package net.ssignal.protocols;

public class ProtocolParameters {//常量

    public static final String 讯宝网络域名_英语 = "ssignal.net";
    public static final String 讯宝网络域名_本国语 = "讯宝.网络";

    public static final String 讯宝地址标识 = "@";

    public static final String 讯宝移动主站服务器主机名 = "m";   //必须为英语小写
    public static final String 讯宝中心服务器主机名 = "ss";   //必须为英语小写
    public static final String 讯宝大聊天群服务器主机名前缀 = "cg";   //必须为英语小写
    public static final String 讯宝小宇宙中心服务器主机名 = "tu";   //必须为英语小写
    public static final String 讯宝小宇宙写入服务器主机名前缀 = "tuw";   //必须为英语小写
    public static final String 讯宝小宇宙读取服务器主机名前缀 = "tur";   //必须为英语小写
    public static final String 讯宝视频通话服务器主机名前缀 = "vc";   //必须为英语小写

    public static final String 设备类型_文本_电脑 = "PC";
    public static final String 设备类型_文本_手机 = "MP";

    public static final String 语言代码_英语 = "eng";   //必须为小写
    public static final String 语言代码_中文 = "zho";   //必须为小写

    public static final String 保留用户名_robot = "robot";
    public static final String 保留用户名_机器人 = "机器人";

    public static final short 端口_传送服务器 = 8000;

    public static final String 特征字符_下划线 = "_";

    public static final String 黑域_全部 = "###";

    //服务器以long数据类型表示的时间采用绝对时间，即从公元1年1月1日0时0分0秒开始计时的时间，而非从公元1970年1月1日0时0分0秒开始计时的相对时间。
    //所以来自服务器的时间long值须先转换成Java的相对时间。

    public static final byte 最大值_子域名长度 = 40;
    public static final byte 最大值_主机名字符数 = 12;
    public static final byte 最大值_域名长度 = 最大值_子域名长度 - 最大值_主机名字符数 - 1;
    public static final byte 最大值_讯宝和电子邮箱地址长度 = 最大值_子域名长度;
    public static final short 最大值_讯宝文本长度 = 4000;
    public static final short 最大值_讯宝预览图片宽高_像素 = 200;
    public static final short 最大值_讯宝图片宽高_像素 = 2000;
    public static final byte 最大值_小聊天群成员数量 = 10;
    public static final byte 最大值_讯宝可撤回的时限_秒 = 120;
    public static final byte 最大值_每个用户可加入的小聊天群数量 = 10;
    public static final byte 最大值_语音时长_秒 = 60;
    public static final byte 最大值_群名称字符数 = 20;
    public static final short 最大值_流星语评论和回复的文字长度 = 200;
    public static final byte 最大值_流星语标题字符数 = 50;

    //以上是跨域使用的，不可自定义
    //以下是域内使用的，可自定义

    public static final short 最大值_每小时可发送讯宝数量 = 360;
    public static final short 最大值_每天可发送讯宝数量 = 最大值_每小时可发送讯宝数量 * 10;
    public static final short 最大值_每人每小时可发送讯宝数量_大聊天群 = 120;
    public static final short 最大值_每人每天可发送讯宝数量_大聊天群 = 最大值_每人每小时可发送讯宝数量_大聊天群 * 2;
    public static final byte 最大值_讯宝文件数据长度_兆 = 5;      //更该此值须更新大聊天群服务Web.config文件的maxRequestLength值 =（讯宝文件数据长度_兆 + 1） * 1024
    public static final byte 最大值_小宇宙文件数据长度_兆 = 5;      //更该此值须更新小宇宙写入服务器Web.config文件的maxRequestLength值 =（最大值_小宇宙文件数据长度_兆 + 1） * 1024
    public static final byte 最大值_英语用户名长度 = 10;     //要保证讯宝地址总长度不得超过40
    public static final byte 最大值_本国语用户名长度 = 6;    //目前是汉语
    public static final byte 最大值_密码长度 = 20;
    public static final short 最大值_讯友数量 = 10000;
    public static final byte 最大值_讯友备注字符数 = 20;
    public static final byte 最大值_讯友标签字符数 = 6;
    public static final byte 最大值_每个标签讯友数量 = 100;    //不要太大，否则在重命名标签时会影响数据库性能
    public static final byte 最大值_每个用户可创建的小聊天群数量 = 3;
    public static final byte 最大值_每个用户可拥有的大聊天群数量 = 1;
    public static final byte 最大值_每个用户可加入的大聊天群数量 = 5;
    public static final byte 最大值_手机号字符数 = 11;
    public static final byte 最大值_选择的图片数量 = 10;
    public static final byte 最大值_视频录制时长_秒 = 10;


    public static final byte 最小值_密码长度 = 12;
    public static final byte 最小值_英语用户名长度 = 3;
    public static final byte 最小值_本国语用户名长度 = 2;


    public static final byte 长度_图标宽高_像素 = 60;
    public static final byte 长度_验证码 = 6;


    public static final byte 讯宝指令_无 = 0;
    public static final byte 讯宝指令_撤回 = 1;      //1-20为一个区间
    public static final byte 讯宝指令_发送文字 = 2;
    public static final byte 讯宝指令_发送语音 = 3;
    public static final byte 讯宝指令_发送图片 = 4;
    public static final byte 讯宝指令_发送短视频 = 5;
    public static final byte 讯宝指令_发送文件 = 6;
    public static final byte 讯宝指令_发送所在位置 = 7;
    public static final byte 讯宝指令_发送红包 = 8;
    public static final byte 讯宝指令_发送名片 = 9;

    public static final byte 讯宝指令_域内自定义二级讯宝指令集1 = 20;

    public static final byte 讯宝指令_视频通话请求 = 21;      //21-30为一个区间

    public static final byte 讯宝指令_创建小聊天群 = 31;      //31-40为一个区间
    public static final byte 讯宝指令_修改聊天群名称 = 32;
    public static final byte 讯宝指令_邀请加入小聊天群 = 33;
    public static final byte 讯宝指令_获取小聊天群成员列表 = 34;
    public static final byte 讯宝指令_退出小聊天群 = 35;
    public static final byte 讯宝指令_删减聊天群成员 = 36;
    public static final byte 讯宝指令_解散小聊天群 = 37;
    public static final byte 讯宝指令_邀请加入大聊天群 = 38;
    public static final byte 讯宝指令_退出大聊天群 = 39;

    public static final byte 讯宝指令_修改图标 = 41;      //41-60为一个区间
    public static final byte 讯宝指令_添加黑域 = 42;
    public static final byte 讯宝指令_移除黑域 = 43;
    public static final byte 讯宝指令_添加白域 = 44;
    public static final byte 讯宝指令_移除白域 = 45;
    public static final byte 讯宝指令_删除讯友 = 46;
    public static final byte 讯宝指令_给讯友添加标签 = 47;
    public static final byte 讯宝指令_移除讯友标签 = 48;
    public static final byte 讯宝指令_重命名讯友标签 = 49;
    public static final byte 讯宝指令_修改讯友备注 = 50;
    public static final byte 讯宝指令_拉黑取消拉黑讯友 = 51;

    public static final byte 讯宝指令_确认收到 = 59;
    public static final byte 讯宝指令_域内自定义二级讯宝指令集2 = 60;
    //以下为服务器反馈给用户的SS
    public static final byte 讯宝指令_某人加入聊天群 = 61;      //61-70为一个区间
    public static final byte 讯宝指令_某人在聊天群的角色改变 = 62;
    public static final byte 讯宝指令_聊天群图标改变 = 63;

    public static final byte 讯宝指令_某域内自定义二级讯宝指令集3 = 70;

    public static final byte 讯宝指令_手机和电脑同步 = 71;     //71-127为一个区间
    public static final byte 讯宝指令_对方未添加我为讯友 = 72;
    public static final byte 讯宝指令_对方把我拉黑了 = 73;
    public static final byte 讯宝指令_讯宝地址不存在 = 74;
    public static final byte 讯宝指令_群里没有成员 = 75;
    public static final byte 讯宝指令_被邀请加入小聊天群者未添加我为讯友 = 76;
    public static final byte 讯宝指令_被邀请加入大聊天群者未添加我为讯友 = 77;
    public static final byte 讯宝指令_已是群成员 = 78;
    public static final byte 讯宝指令_群成员数量已达上限 = 79;
    public static final byte 讯宝指令_不是群成员 = 80;
    public static final byte 讯宝指令_群里还有成员 = 81;
    public static final byte 讯宝指令_加入的群数量已达上限 = 82;
    public static final byte 讯宝指令_本小时发送的讯宝数量已达上限 = 83;
    public static final byte 讯宝指令_今日发送的讯宝数量已达上限 = 84;
    public static final byte 讯宝指令_对讯友录的编辑过于频繁 = 85;

    public static final byte 讯宝指令_域内自定义二级讯宝指令集4 = 100;

    public static final byte 讯宝指令_从客户端发送至服务器成功 = 122;
    public static final byte 讯宝指令_用http访问我 = 123;
    public static final byte 讯宝指令_数据传送失败 = 124;
    public static final byte 讯宝指令_HTTP数据错误 = 125;
    public static final byte 讯宝指令_目标服务器程序出错 = 126;
    public static final byte 讯宝指令_我方服务器程序出错 = 127;   //有符号byte数据最大值    public static final byte 长度信息字节数_零字节 =0;


    public static final byte 域内自定义二级讯宝指令_无 = 0;    //只在本域内使用
    //'域内自定义二级讯宝指令最小值 1
    //'域内自定义二级讯宝指令最大值 127（有符号byte数据最大值）


    public static final short 查询结果_无 = 0;
    public static final short 查询结果_出错 = 1;
    public static final short 查询结果_数据库未就绪 = 2;
    public static final short 查询结果_服务器未就绪 = 3;
    public static final short 查询结果_系统维护 = 4;
    public static final short 查询结果_HTTP数据错误 = 5;
    public static final short 查询结果_成功 = 6;
    public static final short 查询结果_失败 = 7;
    public static final short 查询结果_稍后重试 = 8;
    public static final short 查询结果_未知IP地址 = 9;
    public static final short 查询结果_凭据有效 = 10;
    public static final short 查询结果_凭据无效 = 11;
    public static final short 查询结果_发送序号不一致 = 12;

    public static final short 查询结果_对方未添加我为讯友 = 101;
    public static final short 查询结果_已被对方拉黑 = 102;
    public static final short 查询结果_讯宝地址不存在 = 103;
    public static final short 查询结果_被邀请加入小聊天群者未添加我为讯友 = 104;
    public static final short 查询结果_被邀请加入大聊天群者未添加我为讯友 = 105;
    public static final short 查询结果_不是群成员 = 106;
    public static final short 查询结果_不是正式群成员 = 107;
    public static final short 查询结果_某人离开了小聊天群 = 108;
    public static final short 查询结果_本小时发送的讯宝数量已达上限 = 109;
    public static final short 查询结果_今日发送的讯宝数量已达上限 = 110;
    public static final short 查询结果_不可发言 = 111;
    public static final short 查询结果_无权操作 = 112;
    public static final short 查询结果_大聊天群服务器用户数已满 = 113;
    public static final short 查询结果_加入的大聊天群数量已达上限 = 114;
    public static final short 查询结果_大聊天群名称已存在 = 115;
    public static final short 查询结果_今日发布流星语的次数已达上限 = 116;

    //10001以前是跨域使用的，不可自定义
    //10000以后是域内使用的，可自定义

    public static final short 查询结果_已停用 = 10001;
    public static final short 查询结果_需要添加连接凭据 = 10002;
    public static final short 查询结果_没有可用的传送服务器 = 10003;
    public static final short 查询结果_传送服务器上没有空位置 = 10004;
    public static final short 查询结果_获取A记录失败 = 10005;
    public static final short 查询结果_不是新版本 = 10006;
    public static final short 查询结果_没有可用的大聊天群服务器 = 10007;
    public static final short 查询结果_群名称已存在 = 10008;
    public static final short 查询结果_拥有的大聊天群数量已达上限 = 10009;

    public static final short 查询结果_不正确 = 10101;

    public static final short 查询结果_验证码 = 10103;
    public static final short 查询结果_验证码不匹配 = 10104;
    public static final short 查询结果_获取验证码次数过多 = 10105;
    public static final short 查询结果_暂停发送验证码 = 10106;

    public static final short 查询结果_账号停用 = 10151;
    public static final short 查询结果_手机号未验证 = 10152;
    public static final short 查询结果_电子邮箱地址未验证 = 10153;
    public static final short 查询结果_手机号已绑定 = 10154;
    public static final short 查询结果_电子邮箱地址已绑定 = 10155;
    public static final short 查询结果_英语用户名已注册 = 10156;
    public static final short 查询结果_本国语用户名已注册 = 10157;
    public static final short 查询结果_无注册许可 = 10158;

    public static final short 查询结果_讯友录满了 = 10201;
    public static final short 查询结果_某标签讯友数满了 = 10202;
    public static final short 查询结果_用户不存在 = 10203;


    public static final byte 同步事件_添加讯友 = 1;
    public static final byte 同步事件_删除讯友 = 2;
    public static final byte 同步事件_修改讯友备注 = 3;
    public static final byte 同步事件_讯友添加标签 = 4;
    public static final byte 同步事件_讯友移除标签 = 5;
    public static final byte 同步事件_拉黑讯友 = 6;
    public static final byte 同步事件_取消拉黑讯友 = 7;
    public static final byte 同步事件_重命名标签 = 8;
    public static final byte 同步事件_手机上线 = 9;
    public static final byte 同步事件_电脑上线 = 10;
    public static final byte 同步事件_添加黑域 = 11;
    public static final byte 同步事件_添加白域 = 12;
    public static final byte 同步事件_移除黑域 = 13;
    public static final byte 同步事件_移除白域 = 14;
    public static final byte 同步事件_修改群名称 = 15;
    public static final byte 同步事件_加入小聊天群 = 16;
    public static final byte 同步事件_加入大聊天群 = 17;
    public static final byte 同步事件_退出小聊天群 = 18;
    public static final byte 同步事件_退出大聊天群 = 19;


    public static final byte 设备类型_数字_全部 = 0;
    public static final byte 设备类型_数字_电脑 = 1;
    public static final byte 设备类型_数字_手机 = 2;


    public static final byte 群角色_邀请加入_不可发言 = 1;    //大聊天群才有
    public static final byte 群角色_邀请加入_可以发言 = 2;
    public static final byte 群角色_成员_不可发言 = 3;    //大聊天群才有
    public static final byte 群角色_成员_可以发言 = 4;
    public static final byte 群角色_管理员 = 99;    //大聊天群才有
    public static final byte 群角色_群主 = 100;


    public static final byte 服务器类别_传送服务器 = 1;
    public static final byte 服务器类别_大聊天群服务器 = 2;    //11-10000人
    public static final byte 服务器类别_超级大聊天群服务器 = 3;     //10001-1000000人
    public static final byte 服务器类别_小宇宙中心服务器 = 4;
    public static final byte 服务器类别_小宇宙写入服务器 = 5;
    public static final byte 服务器类别_小宇宙读取服务器 = 6;
    public static final byte 服务器类别_视频通话服务器 = 7;
    public static final byte 服务器类别_电子邮件服务器 = 8;


    public static final byte 流星语类型_图文 = 1;
    public static final byte 流星语类型_视频 = 2;
    public static final byte 流星语类型_转发 = 10;

    public static final byte 流星语访问权限_任何人 = 1;
    public static final byte 流星语访问权限_全部讯友 = 2;
    public static final byte 流星语访问权限_某标签讯友 = 3;
    public static final byte 流星语访问权限_只有我 = 4;


    public static final String SS包标识_无标签 = "SSNT";
    public static final String SS包标识_有标签 = "SSTG";
    public static final String SS包标识_纯文本 = "SSTX";
    public static final char SS包高低位标识_L = 'L';    //Little
    public static final char SS包高低位标识_B = 'B';    //Big
    public static final String 问号_SS包保留标签 = "?";    //查询
    public static final String 冒号_SS包保留标签 = ":";    //结果
    public static final String 感叹号_SS包保留标签 = "!";    //出错
    public static final String 井号_SS包保留标签 = "#";
    public static final String 星号_SS包保留标签 = "*";

    public static final byte 长度信息字节数_零字节 = 0;
    public static final byte 长度信息字节数_两字节 = 2;
    public static final byte 长度信息字节数_四字节 = 4;

    public static final byte SS包数据类型_无 = 0;
    public static final byte SS包数据类型_真假值 = 1;
    public static final byte SS包数据类型_字节 = 2;
    public static final byte SS包数据类型_有符号短整数 = 3;
    public static final byte SS包数据类型_有符号整数 = 4;
    public static final byte SS包数据类型_有符号长整数 = 5;
    public static final byte SS包数据类型_单精度浮点数 = 6;
    public static final byte SS包数据类型_双精度浮点数 = 7;
    public static final byte SS包数据类型_字符串 = 8;
    public static final byte SS包数据类型_字节数组 = 9;
    public static final byte SS包数据类型_子SS包 = 10;

    public static final byte SS包编码_ASCII = 1;
    public static final byte SS包编码_UTF7 = 7;
    public static final byte SS包编码_UTF8 = 8;
    public static final byte SS包编码_Unicode_LittleEndian = 16;
    public static final byte SS包编码_Unicode_BigEndian = 17;
    public static final byte SS包编码_UTF32 = 32;

}
