package net.ssignal.protocols;

import android.util.Base64;

import net.ssignal.util.SSSocketException;
import net.ssignal.util.SharedMethod;

import java.net.Socket;

import javax.crypto.Cipher;

public class SSPackageReader {

    public byte[] 要解读的SS包;
    SSPackageCell 有标签数据[];
    int 数据数量;
    private int 已读取字节数 = 0, 起始索引;
    private boolean 无标签 = false, 需反转 = false;
    byte 编码_数值;
    private String 编码_文本;

    public SSPackageReader() {

    }

    public SSPackageReader(Socket 网络连接器, Cipher AES解密器, boolean 不解读) throws Exception {
        byte[] 字节数组 = SharedMethod.接收指定长度的数据(网络连接器, 5);
        if (字节数组 == null) {
            throw new SSSocketException("接收SS包长度信息失败。");
        }
        int 长度;
        switch (字节数组[0]) {
            case ProtocolParameters.SS包高低位标识_L:
                长度= Convert.toint32(Convert.反转(字节数组, 1, 4), 0);
                break;
            case ProtocolParameters.SS包高低位标识_B:
                长度= Convert.toint32(字节数组, 1);
                break;
            default:
                throw new Exception("无法判断是高位在前还是低位在前。");
        }
        if (长度 < ProtocolParameters.SS包标识_有标签.length()) {
            throw new Exception("未接收到SS包。");
        }
        字节数组 = SharedMethod.接收指定长度的数据(网络连接器, 长度);
        if (字节数组 == null) {
            throw new SSSocketException("接收SS包失败。");
        }
        if (AES解密器 == null) {
            this.要解读的SS包 = 字节数组;
        } else {
            this.要解读的SS包 = AES解密器.doFinal(字节数组);
        }
        if (!不解读) {
            解读(字节数组, AES解密器);
        }
    }

    public SSPackageReader(byte[] 要解读的SS包) throws Exception {
        解读(要解读的SS包, null);
    }

    public SSPackageReader(byte[] 要解读的SS包, Cipher AES解密器) throws Exception {
        解读(要解读的SS包, AES解密器);
    }

    private void 解读(byte[] 要解读的SS包, Cipher AES解密器) throws Exception {
        if (AES解密器 == null) {
            this.要解读的SS包 = 要解读的SS包;
        } else {
            this.要解读的SS包 = AES解密器.doFinal(要解读的SS包);
        }
        switch (new String(this.要解读的SS包, 0, ProtocolParameters.SS包标识_有标签.length(), "ASCII")) {
            case ProtocolParameters.SS包标识_无标签:
                已读取字节数 = ProtocolParameters.SS包标识_有标签.length();
                switch (this.要解读的SS包[已读取字节数]) {
                    case ProtocolParameters.SS包高低位标识_L:
                        需反转 = true;
                        break;
                    case ProtocolParameters.SS包高低位标识_B:
                        break;
                    default:
                        throw new Exception("无法判断是高位在前还是低位在前。");
                }
                已读取字节数 += 1;
                编码_数值 = this.要解读的SS包[已读取字节数];
                已读取字节数 += 1;
                int 总长度;
                if (需反转) {
                    总长度= Convert.toint32(Convert.反转(this.要解读的SS包, 已读取字节数, 4), 0);
                } else {
                    总长度= Convert.toint32(this.要解读的SS包, 已读取字节数);
                }
                if (总长度 <= 0) {
                    throw new Exception("数据损坏。");
                }
                已读取字节数 += 4;
                if (已读取字节数 + 总长度 != this.要解读的SS包.length) {
                    throw new Exception("数据损坏。");
                }
                选择编码();
                无标签 = true;
                break;
            case ProtocolParameters.SS包标识_有标签:
                已读取字节数 = ProtocolParameters.SS包标识_有标签.length();
                switch (this.要解读的SS包[已读取字节数]) {
                    case ProtocolParameters.SS包高低位标识_L:
                        需反转 = true;
                        break;
                    case ProtocolParameters.SS包高低位标识_B:
                        break;
                    default:
                        throw new Exception("无法判断是高位在前还是低位在前。");
                }
                已读取字节数 += 1;
                编码_数值 = this.要解读的SS包[已读取字节数];
                已读取字节数 += 1;
                if (需反转) {
                    数据数量 = Convert.toint32(Convert.反转(this.要解读的SS包, 已读取字节数, 4) , 0);
                } else {
                    数据数量 = Convert.toint32(this.要解读的SS包, 已读取字节数);
                }
                if (数据数量 <= 0) {
                    throw new Exception("数据损坏。");
                }
                已读取字节数 += 4;
                选择编码();
                无标签 = true;
                有标签数据 = new SSPackageCell[数据数量];
                SSPackageCell 某一数据;
                int i = 0;
                while (未读数据长度() > 0) {
                    某一数据 = new SSPackageCell();
                    某一数据.标签 = 读取字符串();
                    某一数据.类型 = 读取字节();
                    switch (某一数据.类型) {
                        case ProtocolParameters.SS包数据类型_字符串:
                            某一数据.长度信息字节数 = ProtocolParameters.长度信息字节数_四字节;
                            某一数据.数据 = 读取字符串(某一数据.长度信息字节数);
                            break;
                        case ProtocolParameters.SS包数据类型_有符号长整数:
                            某一数据.数据 = 读取长整数();
                            break;
                        case ProtocolParameters.SS包数据类型_有符号整数:
                            某一数据.数据 = 读取整数();
                            break;
                        case ProtocolParameters.SS包数据类型_有符号短整数:
                            某一数据.数据 = 读取短整数();
                            break;
                        case ProtocolParameters.SS包数据类型_子SS包:
                            某一数据.长度信息字节数 = ProtocolParameters.长度信息字节数_四字节;
                            byte 字节数组[] = 读取_根据长度信息(某一数据.长度信息字节数);
                            if (字节数组 != null) {
                                某一数据.数据 = new SSPackageReader(字节数组);
                            }
                            break;
                        case ProtocolParameters.SS包数据类型_字节数组:
                            某一数据.长度信息字节数 = ProtocolParameters.长度信息字节数_四字节;
                            某一数据.数据 = 读取_根据长度信息(某一数据.长度信息字节数);
                            break;
                        case ProtocolParameters.SS包数据类型_真假值:
                            某一数据.数据 = 读取真假值();
                            break;
                        case ProtocolParameters.SS包数据类型_字节:
                            某一数据.数据 = 读取字节();
                            break;
                        case ProtocolParameters.SS包数据类型_单精度浮点数:
                            某一数据.数据 = 读取单精度浮点数();
                            break;
                        case ProtocolParameters.SS包数据类型_双精度浮点数:
                            某一数据.数据 = 读取双精度浮点数();
                            break;
                    }
                    有标签数据[i] = 某一数据;
                    i += 1;
                    if (i >= 数据数量) {
                        break;
                    }
                }
                无标签 = false;
                起始索引 = 0;
                if (数据数量 != i) {
                    数据数量 = i;
                    throw new Exception(("数据损坏。"));
                }
                break;
            default:
                throw new Exception("这不是SS包");
        }
    }

    private void 选择编码() {
        switch (编码_数值) {
            case ProtocolParameters.SS包编码_Unicode_LittleEndian:
                编码_文本 = "UNICODELITTLEUNMARKED";
                break;
            case ProtocolParameters.SS包编码_Unicode_BigEndian:
                编码_文本 = "UNICODE";
                break;
            case ProtocolParameters.SS包编码_UTF8:
                编码_文本 = "UTF8";
                break;
            case ProtocolParameters.SS包编码_ASCII:
                编码_文本 = "ASCII";
                break;
            case ProtocolParameters.SS包编码_UTF32:
                编码_文本 = "UTF32";
                break;
            case ProtocolParameters.SS包编码_UTF7:
                编码_文本 = "UTF7";
                break;
            default:
                编码_文本 = "UTF8";        }
    }

    public void 解读纯文本(String 文本) throws Exception {
        if (SharedMethod.字符串未赋值或为空(文本)) {
            return;
        }
        String 行[] = 文本.split("\n");
        if (行.length > 1) {
            if (!ProtocolParameters.SS包标识_纯文本.equals(行[0])) {
                throw new Exception("找不到SS包标识：" + ProtocolParameters.SS包标识_纯文本);
            }
            解读纯文本(行, 1, 0);
        }
    }

    public int 解读纯文本(String 行[], int 起始行, int 层级) throws Exception {
        有标签数据 = new SSPackageCell[20];
        String 某一行, 标签, 空格字符串 = null;
        byte 类型;
        Object 数据;
        int i, j, k;
        if (层级 > 0) {
            StringBuffer 字符串合并器2 = new StringBuffer();
            for (i = 0; i < 层级; i++) {
                字符串合并器2.append(" ");
            }
            空格字符串 = 字符串合并器2.toString();
        } else if (层级 < 0) {
            层级 = 0;
        }
        for (i = 起始行; i < 行.length; i++) {
            某一行 = 行[i];
            if (层级 > 0) {
                if (!某一行.startsWith(空格字符串)) {
                    return i;
                }
                某一行 = 某一行.substring(空格字符串.length());
            }
            if (某一行.startsWith(" ")) {
                throw new Exception("数据有错误，在第" + (i + 1) + "行");
            }
            k = 某一行.indexOf(":", 1);
            if (k < 0) {
                throw new Exception("数据有错误，在第" + (i + 1) + "行");
            }
            switch (某一行.substring(0, k)) {
                case "S" :
                    类型 = ProtocolParameters.SS包数据类型_字符串;
                    break;
                case "8" :
                    类型 = ProtocolParameters.SS包数据类型_有符号长整数;
                    break;
                case "4" :
                    类型 = ProtocolParameters.SS包数据类型_有符号整数;
                    break;
                case "2" :
                    类型 = ProtocolParameters.SS包数据类型_有符号短整数;
                    break;
                case "SS" :
                    类型 = ProtocolParameters.SS包数据类型_子SS包;
                    break;
                case "BT" :
                    类型 = ProtocolParameters.SS包数据类型_字节数组;
                    break;
                case "BL" :
                    类型 = ProtocolParameters.SS包数据类型_真假值;
                    break;
                case "1" :
                    类型 = ProtocolParameters.SS包数据类型_字节;
                    break;
                case "4F" :
                    类型 = ProtocolParameters.SS包数据类型_单精度浮点数;
                    break;
                case "8F" :
                    类型 = ProtocolParameters.SS包数据类型_双精度浮点数;
                    break;
                default:
                    throw new Exception("数据有错误，在第" + (i + 1) + "行");
            }
            j = 某一行.indexOf("=", k + 2);
            if (j < 0) {
                throw new Exception("数据有错误，在第" + (i + 1) + "行");
            }
            标签 = 某一行.substring(k + 1, j);
            if (类型 != ProtocolParameters.SS包数据类型_子SS包) {
                if (类型 != ProtocolParameters.SS包数据类型_字符串) {
                    if (类型 != ProtocolParameters.SS包数据类型_字节数组) {
                        if (j == 某一行.length() - 1) {
                            throw new Exception("数据有错误，在第" + (i + 1) + "行");
                        }
                        switch (类型) {
                            case ProtocolParameters.SS包数据类型_有符号长整数:
                                数据 = Long.parseLong(某一行.substring(j + 1));
                                break;
                            case ProtocolParameters.SS包数据类型_有符号整数:
                                数据 = Integer.parseInt(某一行.substring(j + 1));
                                break;
                            case ProtocolParameters.SS包数据类型_有符号短整数:
                                数据 = Short.parseShort(某一行.substring(j + 1));
                                break;
                            case ProtocolParameters.SS包数据类型_真假值:
                                if ((某一行.substring(j + 1)).equalsIgnoreCase("true")) {
                                    数据 = true;
                                } else {
                                    数据 = false;
                                }
                                break;
                            case ProtocolParameters.SS包数据类型_字节:
                                数据 = Byte.parseByte(某一行.substring(j + 1));
                                break;
                            case ProtocolParameters.SS包数据类型_单精度浮点数:
                                数据 = Float.parseFloat(某一行.substring(j + 1));
                                break;
                            case ProtocolParameters.SS包数据类型_双精度浮点数:
                                数据 = Double.parseDouble(某一行.substring(j + 1));
                                break;
                            default:
                                throw new Exception("数据有错误，在第" + (i + 1) + "行");
                        }
                    } else {
                        if (j < 某一行.length() - 1) {
                            数据 = Base64.decode(某一行.substring(j + 1), Base64.DEFAULT);
                        } else {
                            数据 = null;
                        }
                    }
                } else {
                    if (j < 某一行.length() - 1) {
                        数据 = 某一行.substring(j + 1).replace("&;", "\n").replace("&amp;", "&");
                    } else {
                        数据 = "";
                    }
                }
                添加有标签数据(标签, 类型, 数据);
            } else {
                SSPackageReader SS包解读器 = new SSPackageReader();
                j = SS包解读器.解读纯文本(行, i + 1, 层级 + 1);
                添加有标签数据(标签, 类型, SS包解读器);
                if (j < 行.length) {
                    i = j - 1;
                } else {
                    i = j;
                    break;
                }
            }
        }
        无标签 = false;
        return i;
    }

    private void 添加有标签数据(String 标签, byte 类型, Object 数据) {
        if (有标签数据.length == 数据数量) {
            SSPackageCell 有标签数据2[] = new SSPackageCell[数据数量 * 2];
            System.arraycopy(有标签数据, 0, 有标签数据2, 0, 数据数量);
            有标签数据 = 有标签数据2;
        }
        SSPackageCell 新数据 = new SSPackageCell();
        新数据.标签 = 标签;
        新数据.类型 = 类型;
        新数据.数据 = 数据;
        有标签数据[数据数量] = 新数据;
        数据数量 += 1;
    }

    public byte[] 读取_指定长度(int 长度) throws Exception {
        if (!无标签) { throw new Exception("此为有标签SS包"); }
        if (长度 < 1) {
            return null;
        } else {
            if (已读取字节数 + 长度 > 要解读的SS包.length) {
                throw new Exception("数据长度不足");
            } else {
                byte[] 字节数组=new byte[长度];
                System.arraycopy(要解读的SS包,已读取字节数,字节数组,0,长度);
                已读取字节数 += 长度;
                return 字节数组;
            }
        }
    }

    public byte[] 读取_根据长度信息(byte 长度信息字节数) throws Exception {
        if (!无标签) { throw new Exception("此为有标签SS包"); }
        if (长度信息字节数 < ProtocolParameters.长度信息字节数_两字节) {
            throw new Exception("长度信息字节数不合法");
        } else {
            if (已读取字节数 + 长度信息字节数 > 要解读的SS包.length) {
                throw new Exception("数据长度不足");
            } else {
                int 长度;
                switch (长度信息字节数) {
                    case ProtocolParameters.长度信息字节数_两字节:
                        if (需反转) {
                            长度 = Convert.toint16(Convert.反转(要解读的SS包, 已读取字节数, 2), 0);
                        } else {
                            长度 = Convert.toint16(要解读的SS包, 已读取字节数);
                        }
                        已读取字节数 += 长度信息字节数;
                        return 读取_指定长度(长度);
                    case ProtocolParameters.长度信息字节数_四字节:
                        if (需反转) {
                            长度 = Convert.toint32(Convert.反转(要解读的SS包, 已读取字节数, 4), 0);
                        } else {
                            长度 = Convert.toint32(要解读的SS包, 已读取字节数);
                        }
                        已读取字节数 += 长度信息字节数;
                        return 读取_指定长度(长度);
                    default:
                        throw new Exception("长度信息字节数不合法");
                }
            }
        }
    }

    public boolean 读取真假值() throws Exception {
        if (!无标签) { throw new Exception("此为有标签SS包"); }
        byte[] 字节数组 = 读取_指定长度(1);
        if (字节数组[0] == 0) {
            return false;
        } else {
            return true;
        }
    }

    public byte 读取字节() throws Exception {
        if (!无标签) { throw new Exception("此为有标签SS包"); }
        byte[] 字节数组 = 读取_指定长度(1);
        return 字节数组[0];
    }

    public short 读取短整数() throws Exception {
        if (!无标签) { throw new Exception("此为有标签SS包"); }
        byte[] 字节数组 = 读取_指定长度(2);
        if (需反转) {
            return Convert.toint16(Convert.反转(字节数组), 0);
        } else {
            return Convert.toint16(字节数组,0);
        }
    }

    public int 读取整数() throws Exception {
        if (!无标签) { throw new Exception("此为有标签SS包"); }
        byte[] 字节数组 = 读取_指定长度(4);
        if (需反转) {
            return Convert.toint32(Convert.反转(字节数组),0);
        } else {
            return Convert.toint32(字节数组,0);
        }
    }

    public long 读取长整数() throws Exception {
        if (!无标签) { throw new Exception("此为有标签SS包"); }
        byte[] 字节数组 = 读取_指定长度(8);
        if (需反转) {
            return Convert.toint64(Convert.反转(字节数组),0);
        } else {
            return Convert.toint64(字节数组,0);
        }
    }

    public float 读取单精度浮点数() throws Exception {
        if (!无标签) { throw new Exception("此为有标签SS包"); }
        byte[] 字节数组 = 读取_指定长度(4);
        if (需反转) {
            return Convert.tofloat(Convert.反转(字节数组),0);
        } else {
            return Convert.tofloat(字节数组,0);
        }
    }

    public double 读取双精度浮点数() throws Exception {
        if (!无标签) { throw new Exception("此为有标签SS包"); }
        byte[] 字节数组 = 读取_指定长度(8);
        if (需反转) {
            return Convert.todouble(Convert.反转(字节数组),0);
        } else {
            return Convert.todouble(字节数组,0);
        }
    }

    public String 读取字符串() throws Exception {
        if (!无标签) { throw new Exception("此为有标签SS包"); }
        return 读取字符串(ProtocolParameters.长度信息字节数_两字节);
    }

    public String 读取字符串(byte 长度信息字节数) throws Exception {
        if (!无标签) { throw new Exception("此为有标签SS包"); }
        byte[] 字节数组 = 读取_根据长度信息(长度信息字节数);
        if (字节数组 != null) {
            if (编码_文本.startsWith("UNICODE")) {
                if (字节数组[0] != -1 || 字节数组[1] != -2) {
                    byte[] 字节数组2 = new byte[字节数组.length + 2];
                    字节数组2[0] = -1;
                    字节数组2[1] = -2;
                    System.arraycopy(字节数组, 0, 字节数组2, 2, 字节数组.length);
                    字节数组 = 字节数组2;
                }
            }
            String 字符串 = new String(字节数组, 编码_文本);
            if (字符串.startsWith("\uFEFF")) {
                if (字符串.length() > 1) {
                    return 字符串.substring(1);
                } else {
                    return "";
                }
            } else {
                return 字符串;
            }
        } else {
            return null;
        }
    }

    public Object 读取_有标签(String 标签, Object 默认值) throws Exception {
        if (无标签) { throw new Exception("此为无标签SS包"); }
        Object 数据 = 读取_有标签(标签);
        if (数据 != null) {
            return 数据;
        } else {
            return 默认值;
        }
    }

    public Object 读取_有标签(String 标签) throws Exception {
        if (无标签) { throw new Exception("此为无标签SS包"); }
        if (数据数量 > 0) {
            int i;
            for (i = 起始索引; i < 数据数量; i++) {
                if (标签.equals(有标签数据[i].标签)) {
                    起始索引 = i + 1;
                    return 有标签数据[i].数据;
                }
            }
            if (起始索引 > 0) {
                for (i = 0; i < 起始索引; i++) {
                    if (标签.equals(有标签数据[i].标签)) {
                        起始索引 = i + 1;
                        return 有标签数据[i].数据;
                    }
                }
            }
        }
        return null;
    }

    public Object[] 读取_重复标签(String 标签) throws Exception {
        if (无标签) { throw new Exception("此为无标签SS包"); }
        if (数据数量 > 0) {
            int i, j, 索引[];
            j = 0;
            索引 = new int[数据数量];
            for (i =0;i<数据数量;i++) {
                if (标签.equals(有标签数据[i].标签)) {
                    索引[j] = i;
                    j += 1;
                }
            }
            if (j > 0) {
                Object 对象[] = new Object[j];
                for (i = 0; i < j; i++) {
                    对象[i] = 有标签数据[索引[i]].数据;
                }
                return 对象;
            }
        }
        return null;
    }

    public int 未读数据长度() {
        if (要解读的SS包 != null) {
            return 要解读的SS包.length - 已读取字节数;
        } else {
            return 0;
        }
    }

    public int 已读数据长度() {
        return 已读取字节数;
    }

    public short 获取查询结果() throws Exception {
        if (!无标签) {
            return (short)读取_有标签(ProtocolParameters.冒号_SS包保留标签, ProtocolParameters.查询结果_无);
        } else {
            throw new Exception("此为无标签SS包");
        }
    }

    public String 获取出错提示文本() throws Exception {
        if (!无标签) {
            return (String)读取_有标签(ProtocolParameters.感叹号_SS包保留标签, "");
        } else {
            throw new Exception("此为无标签SS包");
        }
    }

}
