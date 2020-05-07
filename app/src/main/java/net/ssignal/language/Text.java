package net.ssignal.language;

import net.ssignal.util.SharedMethod;

public class Text {//界面文字

    public static Text 界面文字;

    private class 复合数据_文字行 {
        public String 代码, 文本;
    }

    private class 复合数据_文字组 {
        public String 组名;
        public 复合数据_文字行 文字[];
        public int 文字数量;
    }

    private final String 替换标识 = "#%";
    private final String 单复数标识_前面 = "\\$%";
    private final String 单复数标识_后面 = "\\?%";
    //举例：There is$% are$% #% apple/% apples/% on the table.

    public final String 组名_一般 = "一般";
    public static final String 组名_任务 = "任务";

    复合数据_文字组 数据[];
    boolean 繁体;

    public Text(String 文本, boolean 繁体) {
        this.数据 = null;
        this.繁体 = 繁体;
        if (SharedMethod.字符串未赋值或为空(文本)) {return;}
        char 字符[] = 文本.toCharArray();
        char D;
        String A = "";
        String 组名;
        int 界面文字组数 = 0, j;
        boolean 是数字编号 = false;

        for (int i=0;i<字符.length;i++) {
            if (字符[i]=='[') {
                界面文字组数 += 1;
            }
        }
        数据 = new 复合数据_文字组[界面文字组数];
        界面文字组数=0;

        for (int i=0;i<字符.length;i++) {
            D = 字符[i];
            if (D==13) {
                A=A.trim();
                if (A.startsWith("[")) {
                    if (A.endsWith("]")) {
                        if (A.length()>2) {
                            组名 = A.substring(1,A.length()-1);
                            复合数据_文字组 新组=new 复合数据_文字组();
                            数据[界面文字组数]=新组;
                            新组.组名=组名;
                            是数字编号 = false;
                            switch (组名) {
                                case 组名_一般 :
                                    j = 500;
                                    是数字编号 = true;
                                    break;
                                case 组名_任务 :
                                    j=50;
                                    是数字编号 = true;
                                    break;
                                default:
                                    j=100;
                            }
                            新组.文字=new 复合数据_文字行[j];
                            界面文字组数 += 1;
                        }
                    }
                }
                else if (A.startsWith("<")) {
                    j = A.indexOf('>');
                    if (j>1) {
                        j += 1;
                        复合数据_文字组 当前组 = 数据[界面文字组数-1];
                        复合数据_文字行 新行 = new 复合数据_文字行();
                        新行.代码 = A.substring(1, j - 1);
                        if (A.length()>j) {
                            新行.文本 = A.substring(j);
                        }
                        if (是数字编号){
                            if (Integer.parseInt(新行.代码) != 当前组.文字数量) {
                                数据 = null;
                                return;
                            }
                        }
                        当前组.文字[当前组.文字数量] = 新行;
                        当前组.文字数量 += 1;
                    }
                }
                A = "";
            }
            else if (D != 10) {
                A += D;
            }
        }
        if (界面文字组数>0) {
            for (int i=0;i<界面文字组数;i++) {
                复合数据_文字组 当前组 = 数据[i];
                if (当前组.文字数量<当前组.文字.length) {
                    复合数据_文字行 文字[]=new 复合数据_文字行[当前组.文字数量];
                    System.arraycopy(当前组.文字,0,文字,0,当前组.文字数量);
                    当前组.文字=文字;
                }
            }
        }
        else {
            数据=null;
        }
    }

    public boolean 有数据() {
        if (数据==null) {
            return false;
        }
        else {
            return true;
        }
    }

    public boolean 是繁体() {
        return 繁体;
    }

    public String 获取(int 索引, String 现有文字) {
        return 获取(索引,现有文字,null,false);
    }

    public String 获取(int 索引, String 现有文字, Object 要插入的文本[]) {
        return 获取(索引,现有文字,要插入的文本,false);
    }

    public String 获取(int 索引, String 现有文字, Object 要插入的文本[],boolean 不转为繁体) {
        if (数据 !=null && (索引>0)) {
            int i;
            for (i=0;i<数据.length;i++) {
                if (数据[i].组名.equals(组名_一般)) {break;}
            }
            if (i<数据.length) {
                复合数据_文字组 当前组 = 数据[i];
                if (索引<当前组.文字数量) {
                    if (!繁体) {
                        if (要插入的文本 == null) {
                            return 当前组.文字[索引].文本;
                        }
                        else {
                            return 插入文本(当前组.文字[索引].文本,要插入的文本);
                        }
                    }
                    else {
                        if (要插入的文本 == null) {
                            return 简体转繁体(当前组.文字[索引].文本);
                        }
                        else if (!不转为繁体) {
                            return 简体转繁体(插入文本(当前组.文字[索引].文本,要插入的文本));
                        }
                        else {
                            return 插入文本(简体转繁体(当前组.文字[索引].文本),要插入的文本);
                        }
                    }
                }
            }
        }
        if (!繁体) {
            if (要插入的文本 == null) {
                return 现有文字;
            } else {
                return 插入文本(现有文字,要插入的文本);
            }
        } else {
            if (要插入的文本 == null) {
                return 简体转繁体(现有文字);
            }
            else if (!不转为繁体) {
                return 简体转繁体(插入文本(现有文字,要插入的文本));
            }
            else {
                return 插入文本(简体转繁体(现有文字),要插入的文本);
            }
        }
    }

    public String 获取(String 组名, String 代码, String 现有文字, Object 要插入的文本[], boolean 繁体2) {
        if (数据==null && !SharedMethod.字符串未赋值或为空(代码)) {
            int i;
            for (i=0;i<数据.length;i++) {
                if (数据[i].组名.equals(组名)) {break;}
            }
            if (i<数据.length) {
                复合数据_文字组 当前组 = 数据[i];
                int j;
                for (j=0;j<当前组.文字数量;j++) {
                    if (当前组.文字[j].代码.equals(代码)) {break;}
                }
                if (j<当前组.文字数量) {
                    if (!繁体 && !繁体2) {
                        if (要插入的文本 ==null) {
                            return 当前组.文字[j].文本;
                        }else {
                            return 插入文本(当前组.文字[j].文本,要插入的文本);
                        }
                    } else {
                        if (要插入的文本 ==null) {
                            return 简体转繁体(当前组.文字[j].文本);
                        } else {
                            return 简体转繁体(插入文本(当前组.文字[j].文本,要插入的文本));
                        }
                    }
                }
            }
        }
        if (!繁体 && !繁体2) {
            if (要插入的文本 ==null) {
                return 现有文字;
            } else {
                return 插入文本(现有文字,要插入的文本);
            }
        } else {
            if (要插入的文本 ==null) {
                return 简体转繁体(现有文字);
            } else {
                return 简体转繁体(插入文本(现有文字,要插入的文本));
            }
        }
    }

    public String 获取(String 组名, byte 序号, String 现有文字) {
        return 获取(组名,序号,现有文字,null,false);
    }

    public String 获取(String 组名, int 序号, String 现有文字) {
        return 获取(组名,序号,现有文字,null,false);
    }

    public String 获取(String 组名, int 序号, String 现有文字, Object 要插入的文本[], boolean 繁体2) {
        if (数据 !=null && 序号 >=0) {
            int i;
            for (i=0;i<数据.length;i++) {
                if (数据[i].组名.equals(组名)) {break;}
            }
            if (i<数据.length) {
                复合数据_文字组 当前组 = 数据[i];
                if (序号<当前组.文字数量) {
                    if (!繁体 && !繁体2) {
                        if (要插入的文本 ==null) {
                            return 当前组.文字[序号].文本;
                        }else {
                            return 插入文本(当前组.文字[序号].文本,要插入的文本);
                        }
                    } else {
                        if (要插入的文本 ==null) {
                            return 简体转繁体(当前组.文字[序号].文本);
                        } else {
                            return 简体转繁体(插入文本(当前组.文字[序号].文本,要插入的文本));
                        }
                    }
                }
            }
        }
        if (!繁体 && !繁体2) {
            if (要插入的文本 ==null) {
                return 现有文字;
            } else {
                return 插入文本(现有文字,要插入的文本);
            }
        } else {
            if (要插入的文本 ==null) {
                return 简体转繁体(现有文字);
            } else {
                return 简体转繁体(插入文本(现有文字,要插入的文本));
            }
        }
    }

    private String 插入文本(String 原文本, Object 要插入的文本[]) {
        String 段[] = 分段(原文本, 替换标识);
        if (段.length > 1) {
            int k;
            String 替换后的文本 = "";
            for (int i = 0; i < 段.length; i++) {
                if (i == 0) {
                    if (i < 要插入的文本.length) {
                        if (!段[i].contains(单复数标识_前面)) {
                            替换后的文本 = 段[i];
                        } else {
                            String 段1[] = 分段(段[i], 单复数标识_前面);
                            if (段1.length > 1) {
                                k = 0;
                                if (段1.length > 2) {
                                    for (k = 0; k < 段1.length - 2; k++) {
                                        替换后的文本 += 段1[k];
                                    }
                                }
                                switch (Integer.parseInt((String) 要插入的文本[i])) {
                                    case 1:
                                    case 0:
                                        替换后的文本 += 段1[k];
                                        break;
                                    default:
                                        替换后的文本 += 段1[k + 1];
                                }
                            } else {
                                替换后的文本 = 段[i];
                            }
                        }
                    } else {
                        替换后的文本 = 段[i];
                    }
                } else {
                    k = i - 1;
                    if (k < 要插入的文本.length) {
                        if (段[i].indexOf((单复数标识_后面)) > 0) {
                            String 段1[] = 分段(段[i], 单复数标识_后面);
                            if (段1.length > 1) {
                                switch (Integer.parseInt((String) 要插入的文本[k])) {
                                    case 1:
                                    case 0:
                                        替换后的文本 += 要插入的文本[k] + 段1[0];
                                        break;
                                    default:
                                        替换后的文本 += 要插入的文本[k] + 段1[1];
                                }
                                if (段1.length > 2) {
                                    for (k = 2; k < 段1.length; k++) {
                                        替换后的文本 += 段1[k];
                                    }
                                }
                            } else {
                                替换后的文本 += 要插入的文本[k] + 段[i];
                            }
                        } else {
                            if (i < 要插入的文本.length) {
                                if (段[i].contains(单复数标识_前面)) {
                                    String 段1[] = 分段(段[i], 单复数标识_前面);
                                    if (段1.length > 1) {
                                        k = 0;
                                        if (段1.length > 2) {
                                            for (k = 0; k < 段1.length - 2; k++) {
                                                替换后的文本 += 段1[k];
                                            }
                                        }
                                        switch (Integer.parseInt((String) 要插入的文本[i])) {
                                            case 1:
                                            case 0:
                                                替换后的文本 += 段1[k];
                                                break;
                                            default:
                                                替换后的文本 += 段1[k + 1];
                                        }
                                    } else {
                                        替换后的文本 += 要插入的文本[k] + 段[i];
                                    }
                                } else {
                                    替换后的文本 += 要插入的文本[k] + 段[i];
                                }
                            } else {
                                替换后的文本 += 要插入的文本[k] + 段[i];
                            }
                        }
                    } else {
                        替换后的文本 += 替换标识 + 段[i];
                    }
                }
            }
            return 替换后的文本;
        } else {
            return 原文本;
        }
    }

    private String[] 分段(String 字符串, String 分隔字符串) {
        int i = 0, j = 0;
        while (true){
            i = 字符串.indexOf(分隔字符串, i);
            if (i < 0) { break; }
            j += 1;
            i = i + 分隔字符串.length();
        }
        j += 1;
        String[] 段 = new String[j];
        i = 0;
        j = 0;
        int k;
        while (true) {
            k = 字符串.indexOf(分隔字符串, i);
            if (k < 0) { break; }
            if (k > i) {
                段[j] = 字符串.substring(i, k);
            } else {
                段[j] = "";
            }
            j += 1;
            i = k + 分隔字符串.length();
        }
        if (i < 字符串.length()) {
            段[j] = 字符串.substring(i);
        } else {
            段[j] = "";
        }
        return 段;
    }

    private String 简体转繁体(String 简体) {
        if (Chinese.简繁转换器 != null) {
            return Chinese.简繁转换器.转换成繁体(简体);
        }
        return 简体;
    }

}
