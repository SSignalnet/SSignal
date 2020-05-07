package net.ssignal;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import net.ssignal.protocols.Group_Large;
import net.ssignal.protocols.ProtocolParameters;
import net.ssignal.structure.HostName;
import net.ssignal.util.Constants;
import net.ssignal.util.SharedMethod;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.ssignal.User.当前用户;
import static net.ssignal.language.Text.界面文字;

public class Fragment_TaskNameList extends Fragment implements AdapterView.OnItemClickListener, View.OnClickListener {

    View 第一控件;
    private ListView 任务列表控件;
    private Adapter_TaskName 任务列表适配器1;
    Robot 当前机器人;
    private TextView 标签_最近表情, 标签_全部表情;
    private ArrayList<View> 左右滑动页列表;
    ViewPager 左右滑动页容器;
    private int 当前选中的标签 = 0;
    private GridView 最近表情列表控件, 全部表情列表控件;
    private Adapter_Emoji 最近表情列表适配器1, 全部表情列表适配器1;
    private boolean 载入了全部表情列表 = false, 载入了最近表情列表 = false, 需保存 = false;
    private String 最近表情字符;

    @Override
    public View onCreateView(LayoutInflater 布局扩充器, ViewGroup 控件容器, Bundle 已保存的实例状态) {
        第一控件 = 布局扩充器.inflate(R.layout.tasknamelist, 控件容器, false);
        任务列表控件 = (ListView) 第一控件.findViewById(R.id.列表_任务);
        任务列表控件.setOnItemClickListener(this);
        ImageView 机器人图片 = (ImageView) 第一控件.findViewById(R.id.图片_机器人);
        机器人图片.setOnClickListener(this);
        TextView 文字控件 = (TextView) 第一控件.findViewById(R.id.文字);
        文字控件.setText(界面文字.获取(13, 文字控件.getText().toString()));
        if (当前用户.已登录()) {
            byte 字节数组[] = null;
            try {
                File 文件 = new File(getActivity().getFilesDir().toString() + "/" + 当前用户.获取英语讯宝地址() + "/latelyemojis.txt");
                if (文件.exists()) {
                    字节数组 = SharedMethod.读取文件的全部字节(文件.getAbsolutePath());
                    if (字节数组 != null) {
                        最近表情字符 = new String(字节数组, "UTF8");
                        String 表情字符[] = 最近表情字符.split(";");
                        int i;
                        for (i = 0; i < 表情字符.length; i++) {
                            if (SharedMethod.字符串未赋值或为空(表情字符[i])) {
                                break;
                            } else if (表情字符[i].length() > 2) {
                                break;
                            }
                        }
                        if (i < 表情字符.length) {
                            最近表情字符 = null;
                        }
                    }
                }
            } catch (Exception e) {
            }
        }
        标签_最近表情 = (TextView) 第一控件.findViewById(R.id.文字_最近表情);
        标签_最近表情.setText(界面文字.获取(328, 标签_最近表情.getText().toString()));
        标签_最近表情.setOnClickListener(this);
        标签_全部表情 = (TextView) 第一控件.findViewById(R.id.文字_全部表情);
        标签_全部表情.setText(界面文字.获取(329, 标签_全部表情.getText().toString()));
        标签_全部表情.setOnClickListener(this);
        View 最近表情 = 布局扩充器.inflate(R.layout.emojilist, null);
        最近表情列表控件 = 最近表情.findViewById(R.id.表情列表);
        最近表情列表控件.setOnItemClickListener(this);
        View 全部表情 = 布局扩充器.inflate(R.layout.emojilist, null);
        全部表情列表控件 = 全部表情.findViewById(R.id.表情列表);
        全部表情列表控件.setOnItemClickListener(this);
        左右滑动页列表 = new ArrayList<View>();
        左右滑动页列表.add(最近表情);
        左右滑动页列表.add(全部表情);
        PagerAdapter 页面适配器 = new PagerAdapter() {
            @Override
            public int getCount() {
                return 左右滑动页列表.size();
            }

            @Override
            public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
                return view == o;
            }

            @Override
            public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
                container.removeView(左右滑动页列表.get(position));
            }

            @NonNull
            @Override
            public Object instantiateItem(@NonNull ViewGroup container, int position) {
                container.addView(左右滑动页列表.get(position));
                return 左右滑动页列表.get(position);
            }

            @Override
            public void setPrimaryItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
                switch (position) {
                    case 0:
                        if (!载入了最近表情列表) {
                            载入了最近表情列表 = true;
                            载入最近表情字符();
                        }
                        break;
                    case 1:
                        if (!载入了全部表情列表) {
                            载入了全部表情列表 = true;
                            载入全部表情字符();
                        }
                        break;
                }
            }
        };
        左右滑动页容器 = (ViewPager) 第一控件.findViewById(R.id.左右滑动页容器);
        左右滑动页容器.setAdapter(页面适配器);
        左右滑动页容器.addOnPageChangeListener(new MyOnPageChangeListener());
        if (!SharedMethod.字符串未赋值或为空(最近表情字符)) {
            左右滑动页容器.setCurrentItem(0);
        } else {
            左右滑动页容器.setCurrentItem(1);
        }
        return 第一控件;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        载入任务名称(null);
    }

    void 载入任务名称(Robot 机器人) {
        if (机器人 == null) {
            机器人 = 当前机器人;
            当前机器人 = null;
        }
        List<Map<String, Object>> 任务列表 = new ArrayList<Map<String, Object>>();
        添加列表项(任务列表, TaskName.任务名称_取消);
        if (机器人 instanceof Robot_OneOnOne) {
            if (!机器人.聊天控件.需要重新载入) {
                if (当前机器人 != null) {
                    if (当前机器人 instanceof Robot_OneOnOne) { return; }
                }
            } else {
                机器人.聊天控件.需要重新载入 = false;
            }
            添加列表项(任务列表, TaskName.任务名称_小宇宙);
            if (!机器人.聊天控件.发送语音) {
                添加列表项(任务列表, TaskName.任务名称_发送语音);
            } else {
                添加列表项(任务列表, TaskName.任务名称_发送文字);
            }
            添加列表项(任务列表, TaskName.任务名称_发送图片);
            添加列表项(任务列表, TaskName.任务名称_发送原图);
            添加列表项(任务列表, TaskName.任务名称_发送照片);
            添加列表项(任务列表, TaskName.任务名称_发送短视频);
            添加列表项(任务列表, TaskName.任务名称_发送文件);
            添加列表项(任务列表, TaskName.任务名称_添加新标签);
            添加列表项(任务列表, TaskName.任务名称_添加现有标签);
            添加列表项(任务列表, TaskName.任务名称_移除标签);
            添加列表项(任务列表, TaskName.任务名称_备注);
            添加列表项(任务列表, TaskName.任务名称_拉黑);
        } else if (机器人 instanceof Robot_SmallChatGroup) {
            if (!机器人.聊天控件.需要重新载入) {
                if (当前机器人 != null) {
                    if (当前机器人 instanceof Robot_SmallChatGroup) { return; }
                }
            } else {
                机器人.聊天控件.需要重新载入 = false;
            }
            添加列表项(任务列表, TaskName.任务名称_小宇宙);
            if (!机器人.聊天控件.发送语音) {
                添加列表项(任务列表, TaskName.任务名称_发送语音);
            } else {
                添加列表项(任务列表, TaskName.任务名称_发送文字);
            }
            添加列表项(任务列表, TaskName.任务名称_发送图片);
            添加列表项(任务列表, TaskName.任务名称_发送原图);
            添加列表项(任务列表, TaskName.任务名称_发送照片);
            添加列表项(任务列表, TaskName.任务名称_发送短视频);
            添加列表项(任务列表, TaskName.任务名称_发送文件);
            if (机器人.聊天控件.聊天对象.讯友或群主.英语讯宝地址.equals(当前用户.获取英语讯宝地址())) {
                添加列表项(任务列表, TaskName.任务名称_邀请);
                添加列表项(任务列表, TaskName.任务名称_删减成员);
                添加列表项(任务列表, TaskName.任务名称_群名称);
                添加列表项(任务列表, TaskName.任务名称_解散聊天群);
            } else {
                添加列表项(任务列表, TaskName.任务名称_退出聊天群);
            }
        } else if (机器人 instanceof Robot_LargeChatGroup) {
            Group_Large 大聊天群 = 机器人.聊天控件.聊天对象.大聊天群;
            if (大聊天群.我的角色 > ProtocolParameters.群角色_成员_不可发言) {
                添加列表项(任务列表, TaskName.任务名称_小宇宙);
                if (!机器人.聊天控件.发送语音) {
                    添加列表项(任务列表, TaskName.任务名称_发送语音);
                } else {
                    添加列表项(任务列表, TaskName.任务名称_发送文字);
                }
                添加列表项(任务列表, TaskName.任务名称_发送图片);
                添加列表项(任务列表, TaskName.任务名称_发送原图);
                添加列表项(任务列表, TaskName.任务名称_发送照片);
                添加列表项(任务列表, TaskName.任务名称_发送短视频);
                添加列表项(任务列表, TaskName.任务名称_发送文件);
                添加列表项(任务列表, TaskName.任务名称_昵称);
                switch (大聊天群.我的角色) {
                    case ProtocolParameters.群角色_群主:
                        添加列表项(任务列表, TaskName.任务名称_邀请);
                        添加列表项(任务列表, TaskName.任务名称_修改角色);
                        添加列表项(任务列表, TaskName.任务名称_删减成员);
                        添加列表项(任务列表, TaskName.任务名称_群名称);
                        添加列表项(任务列表, TaskName.任务名称_图标);
                        添加列表项(任务列表, TaskName.任务名称_解散聊天群);
                        break;
                    case ProtocolParameters.群角色_管理员:
                        添加列表项(任务列表, TaskName.任务名称_邀请);
                        添加列表项(任务列表, TaskName.任务名称_修改角色);
                        添加列表项(任务列表, TaskName.任务名称_删减成员);
                        添加列表项(任务列表, TaskName.任务名称_退出聊天群);
                        break;
                    default:
                        添加列表项(任务列表, TaskName.任务名称_退出聊天群);
                }
            } else if (大聊天群.我的角色 == ProtocolParameters.群角色_成员_不可发言) {
                添加列表项(任务列表, TaskName.任务名称_小宇宙);
                添加列表项(任务列表, TaskName.任务名称_昵称);
                添加列表项(任务列表, TaskName.任务名称_退出聊天群);
            } else {
                添加列表项(任务列表, TaskName.任务名称_退出聊天群);
            }
        } else if (机器人 instanceof Robot_MainControl) {
            if (当前机器人 != null) {
                if (当前机器人 instanceof Robot_MainControl) { return; }
            }
            if (当前用户.已登录()) {
                添加列表项(任务列表, TaskName.任务名称_小宇宙);
                添加列表项(任务列表, TaskName.任务名称_添加讯友);
                添加列表项(任务列表, TaskName.任务名称_删除讯友);
                添加列表项(任务列表, TaskName.任务名称_清理黑名单);
                添加列表项(任务列表, TaskName.任务名称_添加黑域);
                添加列表项(任务列表, TaskName.任务名称_添加白域);
                添加列表项(任务列表, TaskName.任务名称_重命名标签);
                添加列表项(任务列表, TaskName.任务名称_创建小聊天群);
                添加列表项(任务列表, TaskName.任务名称_创建大聊天群);
                添加列表项(任务列表, TaskName.任务名称_账户);
                添加列表项(任务列表, TaskName.任务名称_图标);
                添加列表项(任务列表, TaskName.任务名称_密码);
                添加列表项(任务列表, TaskName.任务名称_手机号);
                添加列表项(任务列表, TaskName.任务名称_邮箱地址);
                添加列表项(任务列表, TaskName.任务名称_无法及时收到消息);
                添加列表项(任务列表, TaskName.任务名称_注销);
            } else {
                添加列表项(任务列表, TaskName.任务名称_登录);
                添加列表项(任务列表, TaskName.任务名称_注册);
                添加列表项(任务列表, TaskName.任务名称_忘记);
            }
        } else if (机器人 instanceof Robot_SystemManagement) {
            if (当前机器人 != null) {
                if (当前机器人 instanceof Robot_SystemManagement) { return; }
            }
            添加列表项(任务列表, TaskName.任务名称_报表);
            if (当前用户.职能.contains(Constants.职能_管理员)) {
                添加列表项(任务列表, TaskName.任务名称_新传送服务器);
                添加列表项(任务列表, TaskName.任务名称_新大聊天群服务器);
                添加列表项(任务列表, TaskName.任务名称_小宇宙中心服务器);
                添加列表项(任务列表, TaskName.任务名称_添加可注册者);
                添加列表项(任务列表, TaskName.任务名称_移除可注册者);
                添加列表项(任务列表, TaskName.任务名称_商品编辑者);
            }
        }
        String[] FROM = new String[] {"任务名称"};
        int[] TO = new int[]{R.id.文字_任务名称};
        任务列表适配器1 = new Adapter_TaskName(getActivity(), 任务列表, R.layout.taskname, FROM, TO);
        任务列表控件.setAdapter(任务列表适配器1);
        当前机器人 = 机器人;
    }

    private void 添加列表项(List<Map<String, Object>> 任务列表, String 任务名称) {
        Map<String, Object> 对应表 = new HashMap<String, Object>();
        对应表.put("任务名称", 任务名称);
        任务列表.add(对应表);
    }

    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (parent instanceof ListView) {
            Map<String, Object> 对应表 = (Map<String, Object>) 任务列表适配器1.getItem(position);
            当前机器人.聊天控件.对机器人说((String) 对应表.get("任务名称"));
        } else if (parent instanceof GridView) {
            Map<String, Object> 对应表;
            if (当前选中的标签 == 0) {
                对应表 = (Map<String, Object>) 最近表情列表适配器1.getItem(position);
            } else {
                对应表 = (Map<String, Object>) 全部表情列表适配器1.getItem(position);
            }
            String 表情字符 = (String) 对应表.get("表情字符");
            if (!SharedMethod.字符串未赋值或为空(最近表情字符)) {
                if (!最近表情字符.startsWith(表情字符)) {
                    最近表情字符 = 表情字符 + ";" + 最近表情字符.replace(表情字符 + ";", "");
                    String 表情字符数组[] = 最近表情字符.split(";");
                    if (表情字符数组.length > 30) {
                        StringBuffer 字符串合并器 = new StringBuffer(表情字符数组.length * 3);
                        for (int i = 0; i < 30; i++) {
                            字符串合并器.append(表情字符数组[i]);
                            字符串合并器.append(";");
                        }
                        最近表情字符 = 字符串合并器.toString();
                    }
                    载入最近表情字符();
                    if (!需保存) {
                        需保存 = true;
                    }
                }
            } else {
                最近表情字符 = 表情字符 + ";";
                载入最近表情字符();
                if (!需保存) {
                    需保存 = true;
                }
            }
            当前机器人.聊天控件.插入表情字符(表情字符);
        }
        ((Activity_Main) getActivity()).关闭任务名称列表();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (当前用户 == null) {
            return;
        }
        if (当前用户.已登录() && !SharedMethod.字符串未赋值或为空(最近表情字符)) {
            try {
                byte 字节数组[] = 最近表情字符.getBytes("UTF8");
                SharedMethod.保存文件的全部字节(getActivity().getFilesDir().toString() + "/" + 当前用户.获取英语讯宝地址() + "/latelyemojis.txt", 字节数组);
            } catch (Exception e) {
            }
        }
    }

    private void 载入最近表情字符() {
        if (SharedMethod.字符串未赋值或为空(最近表情字符)) {
            return;
        }
        List<Map<String, Object>> 表情列表 = new ArrayList<Map<String, Object>>();
        String 表情字符[] = 最近表情字符.split(";");
        for (int i = 0; i < 表情字符.length; i++) {
            添加表情字符(表情列表, 表情字符[i]);
        }
        String[] FROM = new String[] {"表情字符"};
        int[] TO = new int[]{R.id.文字_表情};
        最近表情列表适配器1 = new Adapter_Emoji(getActivity(), 表情列表, R.layout.emoji, FROM, TO);
        最近表情列表控件.setAdapter(最近表情列表适配器1);
    }

    private void 载入全部表情字符() {
        final String 表情字符串 = "😀;😁;😂;🤣;😃;😄;😅;😆;😉;😊;" +
                                 "😋;😎;😍;😘;😗;😙;😚;☺;🙂;🤗;" +
                                 "🤩;🤔;🤨;😐;😑;😶;🙄;😏;😣;😥;" +
                                 "😮;🤐;😯;😪;😫;😴;😌;😛;😜;😝;" +
                                 "🤤;😒;😓;😔;😕;🙃;🤑;😲;☹;🙁;" +
                                 "😖;😞;😟;😤;😢;😭;😦;😧;😨;😩;" +
                                 "🤯;😬;😰;😱;😳;🤪;😵;😠;😡;🤬;" +
                                 "😷;🤒;🤕;🤢;🤧;😇;🤠;🤡;🤥;🤫;" +
                                 "🧐;🤓;💀;☠;💪;✌;🤞;🖖;🤘;🤙;" +
                                 "🖐;✋;👌;👍;👎;✊;👊;🤚;🤟;✍;" +
                                 "👐;🙌;🤲;🙏;🤝;🎈;🎃;🎄;🛒;🏀;" +
                                 "⚽;🔪;🍕;🍔;🍉;🍋;🍌;🍍;🍎;🍓;" +
                                 "🥜;💐;🌸;🏵;🌹;🌺;🌻;🌼;🌷;🥀;" +
                                 "☘;🌱;🌲;🌳;🍁;🚗;🚲;🛵;🏍;✈;" +
                                 "🚀;❤;💔;❣;💕;💞;💓;💗;💖;💘;" +
                                 "💝;💟;💌;☢;☣;🐷;❀;🔪;🐂;";
        List<Map<String, Object>> 表情列表 = new ArrayList<Map<String, Object>>();
        String 表情字符[] = 表情字符串.split(";");
        for (int i = 0; i < 表情字符.length; i++) {
            添加表情字符(表情列表, 表情字符[i]);
        }
        String[] FROM = new String[] {"表情字符"};
        int[] TO = new int[]{R.id.文字_表情};
        全部表情列表适配器1 = new Adapter_Emoji(getActivity(), 表情列表, R.layout.emoji, FROM, TO);
        全部表情列表控件.setAdapter(全部表情列表适配器1);
    }

    private void 添加表情字符(List<Map<String, Object>> 表情列表, String 表情字符) {
        Map<String, Object> 对应表 = new HashMap<String, Object>();
        对应表.put("表情字符", 表情字符);
        表情列表.add(对应表);
    }

    @Override
    public void onClick(View 控件) {
        switch (控件.getId()) {
            case R.id.图片_机器人:
                ((Activity_Main)getActivity()).关闭任务名称列表();
                break;
            case R.id.文字_最近表情:
                左右滑动页容器.setCurrentItem(0);
                break;
            case R.id.文字_全部表情:
                左右滑动页容器.setCurrentItem(1);
                break;
        }
    }

    public class MyOnPageChangeListener implements ViewPager.OnPageChangeListener {
        @Override
        public void onPageSelected(int i) {
            if (当前选中的标签 >= 0) {
                switch (当前选中的标签) {
                    case 0:
                        标签_最近表情.setTextColor(Color.BLACK);
                        break;
                    case 1:
                        标签_全部表情.setTextColor(Color.BLACK);
                        break;
                }
            }
            当前选中的标签 = i;
            switch (当前选中的标签) {
                case 0:
                    标签_最近表情.setTextColor(Color.RED);
                    break;
                case 1:
                    标签_全部表情.setTextColor(Color.RED);
                    break;
            }
        }

        @Override
        public void onPageScrolled(int i, float v, int i1) {

        }

        @Override
        public void onPageScrollStateChanged(int i) {

        }
    }

}
