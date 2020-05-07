package net.ssignal.protocols;

public class ProtocolFormats {

    public static void 添加数据_检查大聊天群新讯宝数量(SSPackageCreator SS包生成器, String 子域名, Group_Large 加入的大聊天群[]) throws Exception {
        for (int i = 0; i < 加入的大聊天群.length; i++) {
            if (加入的大聊天群[i].子域名.equals(子域名)) {
                SSPackageCreator SS包生成器3 = new SSPackageCreator();
                SS包生成器3.添加_有标签("GI", 加入的大聊天群[i].编号);    //GroupIndex
                SS包生成器3.添加_有标签("LT", 加入的大聊天群[i].最新讯宝的发送时间);    //LaterThan
                SS包生成器.添加_有标签("GP", SS包生成器3);    //Group
            }
        }
    }

    public static byte[] 添加数据_大聊天群发送或接收讯宝(byte 讯宝指令, String 文字,
            short 宽度, short 高度, byte 秒数, byte[] 文件数据, String 子域名,
            Group_Large 加入的大聊天群[], byte[] 视频预览图片数据) {
        SSPackageCreator SS包生成器 = new SSPackageCreator();
        SSPackageCreator SS包生成器2;
        if (讯宝指令 != ProtocolParameters.讯宝指令_无) {
            try {
                SS包生成器2 = new SSPackageCreator();
                SS包生成器2.添加_有标签("CM", 讯宝指令);    //Command
                SS包生成器2.添加_有标签("TX", 文字);    //Text
                if (讯宝指令 == ProtocolParameters.讯宝指令_发送语音) {
                    SS包生成器2.添加_有标签("SC", 秒数);    //Seconds
                }
                switch (讯宝指令) {
                    case ProtocolParameters.讯宝指令_发送图片:
                    case ProtocolParameters.讯宝指令_发送短视频:
                        SS包生成器2.添加_有标签("WD", 宽度);    //Width
                        SS包生成器2.添加_有标签("HT", 高度);    //Height
                        break;
                }
                if (讯宝指令 == ProtocolParameters.讯宝指令_发送短视频) {
                    SS包生成器2.添加_有标签("PV", 视频预览图片数据);    //Preview
                }
                switch (讯宝指令) {
                    case ProtocolParameters.讯宝指令_发送图片:
                    case ProtocolParameters.讯宝指令_发送语音:
                    case ProtocolParameters.讯宝指令_发送短视频:
                    case ProtocolParameters.讯宝指令_发送文件:
                        SS包生成器2.添加_有标签("FD", 文件数据);    //FileData
                        break;
                }
                SS包生成器.添加_有标签("POST", SS包生成器2);
            } catch (Exception e) {
                return null;
            }
        }
        byte 字节数组[];
        try {
            SS包生成器2 = new SSPackageCreator();
            SSPackageCreator SS包生成器3;
            for (int i = 0; i < 加入的大聊天群.length; i++) {
                if (子域名.equals(加入的大聊天群[i].子域名)) {
                    SS包生成器3 = new SSPackageCreator();
                    SS包生成器3.添加_有标签("GI", 加入的大聊天群[i].编号);    //GroupIndex
                    SS包生成器3.添加_有标签("LT", 加入的大聊天群[i].最新讯宝的发送时间);    //LaterThan
                    SS包生成器2.添加_有标签("GP", SS包生成器3);    //Group
                }
            }
            if (SS包生成器2.获取SS包数据数量() > 0) {
                SS包生成器.添加_有标签("GET", SS包生成器2);
            }
            字节数组 = SS包生成器.生成SS包();
        } catch (Exception e) {
            return null;
        }
        return 字节数组;
    }

    public static String 生成文本_邀请加入小聊天群(byte 群编号, String 群备注) {
        try {
            SSPackageCreator SS包生成器 = new SSPackageCreator();
            SS包生成器.添加_有标签("I", 群编号);    //Index
            SS包生成器.添加_有标签("N", 群备注);    //Name
            return SS包生成器.生成纯文本();
        } catch (Exception e) {
            return null;
        }
    }

    public static String 生成文本_邀请加入大聊天群(String 子域名, long 群编号, String 群名称) {
        try {
            SSPackageCreator SS包生成器 = new SSPackageCreator();
            SS包生成器.添加_有标签("D", 子域名);    //Domain
            SS包生成器.添加_有标签("I", 群编号);    //Index
            SS包生成器.添加_有标签("N", 群名称);    //Name
            return SS包生成器.生成纯文本();
        } catch (Exception e) {
            return null;
        }
    }

}
