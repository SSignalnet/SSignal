package net.ssignal;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import net.ssignal.protocols.ProtocolPath;
import net.ssignal.protocols.SSPackageCreator;
import net.ssignal.structure.ChatWith;
import net.ssignal.protocols.Contact;
import net.ssignal.protocols.Group_Large;
import net.ssignal.protocols.ProtocolParameters;
import net.ssignal.util.Constants;
import net.ssignal.util.SharedMethod;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.Arrays;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import static net.ssignal.Activity_Main.主活动;
import static net.ssignal.Fragment_Main.主窗体;
import static net.ssignal.User.当前用户;
import static net.ssignal.language.Text.界面文字;

public class Fragment_TinyUniverse extends Fragment {

    Robot 机器人;
    private View 第一控件;
    private ProgressBar 进度条;
    WebView 网页浏览器;
    private MyHandler 跨线程调用器;
    boolean 网页载入完毕 = false;
    private boolean 正在选择视频预览图片 = false;
    static String 初始路径;

    private String id_DOM;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater 布局扩充器, @Nullable ViewGroup 控件容器, @Nullable Bundle savedInstanceState) {
        第一控件 = 布局扩充器.inflate(R.layout.browser, 控件容器, false);
        跨线程调用器 = new MyHandler(this);
        return 第一控件;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (机器人 == null) {
            return;
        }
        进度条 = (ProgressBar) 第一控件.findViewById(R.id.进度条);
        进度条.setTop(0);
        进度条.bringToFront();
        网页浏览器 = (WebView) 第一控件.findViewById(R.id.网页浏览器);
        WebSettings 浏览器设置 = 网页浏览器.getSettings();
        浏览器设置.setJavaScriptEnabled(true);
        浏览器设置.setSupportMultipleWindows(false);
        浏览器设置.setDomStorageEnabled(true);
        浏览器设置.setAllowFileAccess(false);
        浏览器设置.setAppCachePath(getActivity().getCacheDir().getPath());
        浏览器设置.setAppCacheEnabled(true);
        if (Build.VERSION.SDK_INT >= 21) {
            浏览器设置.setMixedContentMode(0);
            网页浏览器.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        } else if (Build.VERSION.SDK_INT >= 19) {
            网页浏览器.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        } else {
            网页浏览器.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
        网页浏览器.setWebChromeClient(new MyWebChromeClient(this));
        网页浏览器.setWebViewClient(new MyWebViewClient(this));
        网页浏览器.setDownloadListener(new MyDownloadListener(getActivity(), this));
        网页浏览器.addJavascriptInterface(this, "external");
        if (SharedMethod.字符串未赋值或为空(初始路径)) {
            ChatWith 聊天对象 = 机器人.聊天控件.聊天对象;
            if (聊天对象.小聊天群 != null) {
                switch (聊天对象.讯友或群主.英语讯宝地址) {
                    case Constants.机器人id_主控:
                        if (当前用户.已登录()) {
                            if (!SharedMethod.字符串未赋值或为空(当前用户.英语用户名)) {
                                网页浏览器.loadUrl(ProtocolPath.获取当前用户小宇宙的访问路径(当前用户.英语用户名, 当前用户.域名_英语));
                            } else {
                                网页浏览器.loadUrl(ProtocolPath.获取主站首页的访问路径());
                            }
                        } else {
                            网页浏览器.loadUrl(ProtocolPath.获取主站首页的访问路径());
                        }
                        break;
                    case Constants.机器人id_系统管理:
                        网页浏览器.loadUrl(ProtocolPath.获取系统管理页面的访问路径(当前用户.域名_英语));
                        break;
                    default:
                        网页浏览器.loadUrl(ProtocolPath.获取讯友小宇宙的访问路径(聊天对象.讯友或群主.英语讯宝地址, 当前用户.获取英语讯宝地址()));
                }
            } else if (聊天对象.大聊天群 != null) {
                if (SharedMethod.字符串未赋值或为空(聊天对象.大聊天群.连接凭据)) {
                    网页浏览器.loadUrl("about:blank");
                } else {
                    网页浏览器.loadUrl(ProtocolPath.获取大聊天群小宇宙的访问路径(聊天对象.大聊天群.子域名));
                }
            } else {
                网页浏览器.loadUrl("about:blank");
            }
        } else {
            网页浏览器.loadUrl(初始路径);
            初始路径 = null;
        }
    }

    void shouldOverrideUrlLoading(WebView view, String 链接) {
        Intent 意图 = new Intent(Intent.ACTION_VIEW);
        Uri uri = Uri.parse(链接);
        意图.setData(uri);
        startActivity(意图);
    }

    void onProgressChanged(WebView view, int newProgress) {
        if (newProgress == 100) {
            进度条.setVisibility(View.GONE);
            if (网页载入完毕) { return; }
            网页载入完毕 = true;
            if (机器人 instanceof Robot_SmallChatGroup) {
                机器人.聊天控件.加载小聊天群的成员列表();
            } else if (机器人 instanceof Robot_LargeChatGroup) {
                Group_Large 大聊天群 = 机器人.聊天控件.聊天对象.大聊天群;
                if (!SharedMethod.字符串未赋值或为空(大聊天群.连接凭据)) {
                    发送JS("function(){ CredentialReady('" + SharedMethod.替换HTML和JS敏感字符(当前用户.获取英语讯宝地址()) + "', '" + SharedMethod.替换HTML和JS敏感字符(大聊天群.连接凭据) + "', '" + 大聊天群.编号 + "'); }");
                }
            } else if (机器人 instanceof Robot_SystemManagement) {
                发送JS("function(){ UserIDCredential('" + 当前用户.编号 + "', '" + 当前用户.凭据_中心服务器 + "'); }");
            }
        } else {
            if (newProgress < 10) {
                进度条.setProgress(10);
            } else {
                进度条.setProgress(newProgress);
            }
            if (进度条.getVisibility() == View.GONE) {
                进度条.setVisibility(View.VISIBLE);
                进度条.bringToFront();
            }
        }
    }

    @JavascriptInterface
    public void RequestReadCredential(String 子域名) {
        Message 消息 = new Message();
        消息.what = 1;
        Bundle 数据盒子 = new Bundle();
        数据盒子.putString("子域名", 子域名);
        消息.setData(数据盒子);
        跨线程调用器.sendMessage(消息);
    }

    @JavascriptInterface
    public void RequestWriteCredential(String 子域名) {
        Message 消息 = new Message();
        消息.what = 2;
        Bundle 数据盒子 = new Bundle();
        数据盒子.putString("子域名", 子域名);
        消息.setData(数据盒子);
        跨线程调用器.sendMessage(消息);
    }

    @JavascriptInterface
    public void RequestReadCredential2() {
        Message 消息 = new Message();
        消息.what = 10;
        跨线程调用器.sendMessage(消息);
    }

    @JavascriptInterface
    public void PlayVideo(String url) {
        Message 消息 = new Message();
        消息.what = 11;
        Bundle 数据盒子 = new Bundle();
        数据盒子.putString("url", url);
        消息.setData(数据盒子);
        跨线程调用器.sendMessage(消息);
    }

    @JavascriptInterface
    public void ClickAMember(String who) {
        Message 消息 = new Message();
        消息.what = 14;
        Bundle 数据盒子 = new Bundle();
        数据盒子.putString("who", who);
        消息.setData(数据盒子);
        跨线程调用器.sendMessage(消息);
    }

    @JavascriptInterface
    public void RequestMemberList() {
        Message 消息 = new Message();
        消息.what = 15;
        跨线程调用器.sendMessage(消息);
    }

    @JavascriptInterface
    public void SelectImage(String id) {
        Message 消息 = new Message();
        消息.what = 3;
        Bundle 数据盒子 = new Bundle();
        数据盒子.putString("id", id);
        消息.setData(数据盒子);
        跨线程调用器.sendMessage(消息);
    }

    @JavascriptInterface
    public void SelectVideo() {
        Message 消息 = new Message();
        消息.what = 12;
        跨线程调用器.sendMessage(消息);
    }

    @JavascriptInterface
    public void SelectVideoPreview() {
        Message 消息 = new Message();
        消息.what = 13;
        跨线程调用器.sendMessage(消息);
    }

    @JavascriptInterface
    public void GetTags(String id) {
        Message 消息 = new Message();
        消息.what = 4;
        Bundle 数据盒子 = new Bundle();
        数据盒子.putString("id", id);
        消息.setData(数据盒子);
        跨线程调用器.sendMessage(消息);
    }

    @JavascriptInterface
    public void PublishMeteorRain(String XML) {
        Message 消息 = new Message();
        消息.what = 5;
        Bundle 数据盒子 = new Bundle();
        数据盒子.putString("XML", XML);
        消息.setData(数据盒子);
        跨线程调用器.sendMessage(消息);
    }

    @JavascriptInterface
    public void PublishGoods(String XML) {
        Message 消息 = new Message();
        消息.what = 9;
        Bundle 数据盒子 = new Bundle();
        数据盒子.putString("XML", XML);
        消息.setData(数据盒子);
        跨线程调用器.sendMessage(消息);
    }

    @JavascriptInterface
    public void ServerInfo(String info) {
        Message 消息 = new Message();
        消息.what = 6;
        Bundle 数据盒子 = new Bundle();
        数据盒子.putString("服务器信息", info);
        消息.setData(数据盒子);
        跨线程调用器.sendMessage(消息);
    }

    @JavascriptInterface
    public void AdminLogin(String Passcode) {
        Message 消息 = new Message();
        消息.what = 7;
        Bundle 数据盒子 = new Bundle();
        数据盒子.putString("Passcode", Passcode);
        消息.setData(数据盒子);
        跨线程调用器.sendMessage(消息);
    }

    void 处理跨线程数据(Message msg) {
        switch (msg.what) {
            case 11:
                主活动.弹出播放视频的窗体(msg.getData().getString("url"));
                break;
            case 14:
                机器人.聊天控件.点击群成员(msg.getData().getString("who"));
                break;
            case 15:
                机器人.聊天控件.加载小聊天群的成员列表();
                break;
            case 1:
                当前用户.获取小宇宙凭据(机器人.聊天控件, msg.getData().getString("子域名"), false, 跨线程调用器);
                break;
            case 2:
                当前用户.获取小宇宙凭据(机器人.聊天控件, msg.getData().getString("子域名"), true, 跨线程调用器);
                break;
            case 8:
                当前用户.获取小宇宙凭据结束(msg.getData().getLong("创建时刻"), msg.getData().getByteArray("字节数组"));
                break;
            case 10:
                ((Robot_LargeChatGroup)机器人).请求讯宝中心小宇宙分配读取服务器();
                break;
            case 3:
                正在选择视频预览图片 = false;
                选择图片(msg.getData().getString("id"));
                break;
            case 4:
                获取讯友标签(msg.getData().getString("id"));
                break;
            case 12:
                选择视频();
                break;
            case 13:
                正在选择视频预览图片 = true;
                选择图片(null);
                break;
            case 5:
                发布流星语(msg.getData().getString("XML"));
                break;
            case 9:
                发布商品(msg.getData().getString("XML"));
                break;
            case 6:
                机器人.说(msg.getData().getString("服务器信息"));
                主窗体.左右滑动页容器.setCurrentItem(1);
                break;
            case 7:
                当前用户.凭据_管理员 = msg.getData().getString("Passcode");
                break;
        }
    }

    private void 选择图片(String id) {
        if (Build.VERSION.SDK_INT >= 23) {
            if (!主活动.选取文件前检查读取存储卡的权限()) {
                主活动.选取图片前请求读取存储卡的权限(this);
                return;
            }
        }
        id_DOM = id;
        主活动.选择图片(this);
    }

    void 选中图片(String 图片路径, String 保存路径) {
        String DataURL;
        try {
            Bitmap 原图 = BitmapFactory.decodeFile(图片路径);
            Bitmap 压缩后图片;
            if (原图.getWidth() > ProtocolParameters.最大值_讯宝预览图片宽高_像素 || 原图.getHeight() > ProtocolParameters.最大值_讯宝预览图片宽高_像素) {
                double 缩小比例;
                if (原图.getHeight() > 原图.getWidth()) {
                    缩小比例 = (double) ProtocolParameters.最大值_讯宝预览图片宽高_像素 / (double)原图.getHeight();
                } else {
                    缩小比例 = (double) ProtocolParameters.最大值_讯宝预览图片宽高_像素 / (double)原图.getWidth();
                }
                压缩后图片 = Bitmap.createBitmap((int)((double)原图.getWidth() * 缩小比例), (int)((double)原图.getHeight() * 缩小比例), Bitmap.Config.ARGB_8888);
            } else {
                压缩后图片 = Bitmap.createBitmap(原图.getWidth(), 原图.getHeight(), Bitmap.Config.ARGB_8888);
            }
            Canvas 绘图器 = new Canvas(压缩后图片);
            绘图器.drawBitmap(原图, new Rect(0, 0, 原图.getWidth(), 原图.getHeight()), new Rect(0, 0, 压缩后图片.getWidth(), 压缩后图片.getHeight()), null);
            保存路径 += "/" + 当前用户.获取英语讯宝地址();
            File 目录 = new File(保存路径);
            if (!目录.exists() || !目录.isDirectory()) {
                目录.mkdir();
            }
            保存路径 += "/tu.jpg";
            SharedMethod.保存位图(压缩后图片, 保存路径);
            byte 字节数组[] = SharedMethod.读取文件的全部字节(保存路径);
            if (字节数组 != null) {
                DataURL = "data:image/jpg;base64," + Base64.encodeToString(字节数组, Base64.DEFAULT);
            } else {
                return;
            }
        } catch (Exception e) {
            ((Robot_MainControl) 机器人).说(e.getMessage());
            return;
        }
        DataURL = DataURL.replace("\n", "");
        if (!正在选择视频预览图片) {
            if (SharedMethod.字符串未赋值或为空(id_DOM)) {
                发送JS("function(){ InsertImage('" + SharedMethod.处理文件路径以用作JS函数参数(图片路径) + "', '" + DataURL + "'); }");
            } else {
                发送JS("function(){ ReviseImage('" + id_DOM + "', '" + SharedMethod.处理文件路径以用作JS函数参数(图片路径) + "', '" + DataURL + "'); }");
            }
        } else {
            发送JS("function(){ InsertVideoPreview('" + SharedMethod.处理文件路径以用作JS函数参数(图片路径) + "', '" + DataURL + "'); }");
        }
    }

    private void 选择视频() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (!主活动.选取文件前检查读取存储卡的权限()) {
                主活动.选取图片前请求读取存储卡的权限(this);
                return;
            }
        }
        主活动.选择视频(this);
    }

    void 选中视频(String 视频路径, String 保存路径) {
        MediaMetadataRetriever 媒体数据提取器 = new MediaMetadataRetriever();
        媒体数据提取器.setDataSource(视频路径);
        String DataURL;
        try {
            Bitmap 原图 = 媒体数据提取器.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
            Bitmap 压缩后图片;
            if (原图.getWidth() > ProtocolParameters.最大值_讯宝预览图片宽高_像素 || 原图.getHeight() > ProtocolParameters.最大值_讯宝预览图片宽高_像素) {
                double 缩小比例;
                if (原图.getHeight() > 原图.getWidth()) {
                    缩小比例 = (double) ProtocolParameters.最大值_讯宝预览图片宽高_像素 / (double)原图.getHeight();
                } else {
                    缩小比例 = (double) ProtocolParameters.最大值_讯宝预览图片宽高_像素 / (double)原图.getWidth();
                }
                压缩后图片 = Bitmap.createBitmap((int)((double)原图.getWidth() * 缩小比例), (int)((double)原图.getHeight() * 缩小比例), Bitmap.Config.ARGB_8888);
            } else {
                压缩后图片 = Bitmap.createBitmap(原图.getWidth(), 原图.getHeight(), Bitmap.Config.ARGB_8888);
            }
            Canvas 绘图器 = new Canvas(压缩后图片);
            绘图器.drawBitmap(原图, new Rect(0, 0, 原图.getWidth(), 原图.getHeight()), new Rect(0, 0, 压缩后图片.getWidth(), 压缩后图片.getHeight()), null);
            保存路径 += "/" + 当前用户.获取英语讯宝地址();
            File 目录 = new File(保存路径);
            if (!目录.exists() || !目录.isDirectory()) {
                目录.mkdir();
            }
            保存路径 += "/tu.jpg";
            SharedMethod.保存位图(压缩后图片, 保存路径);
            byte 字节数组[] = SharedMethod.读取文件的全部字节(保存路径);
            if (字节数组 != null) {
                DataURL = "data:image/jpg;base64," + Base64.encodeToString(字节数组, Base64.DEFAULT);
            } else {
                return;
            }
        } catch (Exception e) {
            ((Robot_MainControl) 机器人).说(e.getMessage());
            return;
        }
        DataURL = DataURL.replace("\n", "");
        发送JS("function(){ InsertVideo('" + SharedMethod.处理文件路径以用作JS函数参数(视频路径) + "', '" + SharedMethod.处理文件路径以用作JS函数参数(保存路径) + "', '" + DataURL + "'); }");
    }

    private void 获取讯友标签(String id) {
        if (当前用户.讯友目录 == null) {
            return;
        }
        Contact 讯友目录[] = 当前用户.讯友目录;
        String 讯友标签[] = new String[讯友目录.length * 2];
        int 讯友标签数 = 0;
        int i;
        for (i = 0; i < 讯友目录.length; i++) {
            if (User.收集标签(讯友目录[i].标签一, 讯友标签, 讯友标签数)) {
                讯友标签数 += 1;
            }
            if (User.收集标签(讯友目录[i].标签二, 讯友标签, 讯友标签数)) {
                讯友标签数 += 1;
            }
        }
        if (讯友标签数 > 0) {
            if (讯友标签数 < 讯友标签.length) {
                String 讯友标签2[] = new String[讯友标签数];
                System.arraycopy(讯友标签, 0, 讯友标签2, 0, 讯友标签数);
                讯友标签 = 讯友标签2;
            }
            Arrays.sort(讯友标签);
            StringBuffer 字符串合并器 = new StringBuffer(讯友标签数 * (ProtocolParameters.最大值_讯友标签字符数 + 17));
            for (i = 0; i < 讯友标签数; i++) {
                字符串合并器.append("<option>" + SharedMethod.替换HTML和JS敏感字符(讯友标签[i]) + "</option>");
            }
            发送JS("function(){ SSPalTags('" + id_DOM + "', '" + 字符串合并器.toString() + "'); }");
        }
    }

    private void 发布流星语(String XML) {
        byte SS包[];
        SSPackageCreator SS包生成器 = new SSPackageCreator();
        try {
            Element 元素 = 获取XML文档根元素(XML);
            if (!元素.getTagName().equals("MeteorRain")) {
                return;
            }
            NodeList 元素列表 = 元素.getElementsByTagName("Type");
            byte 类型 = Byte.parseByte(((Element) 元素列表.item(0)).getTextContent());
            SS包生成器.添加_有标签("类型", 类型);
            元素列表 = 元素.getElementsByTagName("Title");
            String 标题 = ((Element) 元素列表.item(0)).getTextContent();
            SS包生成器.添加_有标签("标题", 标题);
            元素列表 = 元素.getElementsByTagName("Permission");
            if (元素列表.getLength() > 0) {
                byte 访问权限 = Byte.parseByte(((Element) 元素列表.item(0)).getTextContent());
                SS包生成器.添加_有标签("访问权限", 访问权限);
                if (访问权限 == ProtocolParameters.流星语访问权限_某标签讯友) {
                    元素列表 = 元素.getElementsByTagName("Tag");
                    String 讯友标签 = ((Element) 元素列表.item(0)).getTextContent();
                    SS包生成器.添加_有标签("讯友标签", 讯友标签);
                }
            }
            元素列表 = 元素.getElementsByTagName("Domain_Read");
            if (元素列表.getLength() > 0) {
                String 域名_读取 = ((Element) 元素列表.item(0)).getTextContent();
                SS包生成器.添加_有标签("域名_读取", 域名_读取);
            }
            元素列表 = 元素.getElementsByTagName("Style");
            byte 样式 = Byte.parseByte(((Element) 元素列表.item(0)).getTextContent());
            SS包生成器.添加_有标签("样式", 样式);
            Element 元素2;
            byte 字节数组[];
            switch (类型) {
                case ProtocolParameters.流星语类型_图文:
                    元素列表 = 元素.getElementsByTagName("Body");
                    元素列表 = ((Element) 元素列表.item(0)).getChildNodes();
                    SSPackageCreator SS包生成器2;
                    int i;
                    for (i = 0; i < 元素列表.getLength(); i++) {
                        元素2 = ((Element) 元素列表.item(i));
                        switch (元素2.getTagName()) {
                            case "Text":
                                SS包生成器2 = new SSPackageCreator();
                                SS包生成器2.添加_有标签("是图片", false);
                                SS包生成器2.添加_有标签("文本", 元素2.getTextContent());
                                SS包生成器.添加_有标签("段落", SS包生成器2);
                                break;
                            case "Image":
                                SS包生成器2 = new SSPackageCreator();
                                SS包生成器2.添加_有标签("是图片", true);
                                String 路径 = 元素2.getTextContent();
                                SS包生成器2.添加_有标签("扩展名", SharedMethod.获取扩展名(路径));
                                字节数组 = SharedMethod.读取文件的全部字节(路径);
                                SS包生成器2.添加_有标签("图片数据", 字节数组);
                                SS包生成器.添加_有标签("段落", SS包生成器2);
                                break;
                        }
                    }
                    break;
                case ProtocolParameters.流星语类型_视频:
                    元素列表 = 元素.getElementsByTagName("Video");
                    String 路径 = ((Element) 元素列表.item(0)).getTextContent();
                    字节数组 = SharedMethod.读取文件的全部字节(路径);
                    SS包生成器.添加_有标签("视频数据", 字节数组);
                    元素列表 = 元素.getElementsByTagName("Image");
                    路径 = ((Element) 元素列表.item(0)).getTextContent();
                    字节数组 = SharedMethod.读取文件的全部字节(路径);
                    SS包生成器.添加_有标签("预览图片", 字节数组);
                    break;
                default:
                    if (机器人 instanceof Robot_MainControl) {
                        ((Robot_MainControl) 机器人).流星语发布结束(false);
                    } else if (机器人 instanceof Robot_LargeChatGroup) {
                        ((Robot_LargeChatGroup) 机器人).流星语发布结束(false);
                    }
                    return;
            }
            SS包 = SS包生成器.生成SS包();
        } catch (Exception e) {
            if (机器人 instanceof Robot_MainControl) {
                ((Robot_MainControl) 机器人).说(e.getMessage());
                ((Robot_MainControl) 机器人).流星语发布结束(false);
            } else if (机器人 instanceof Robot_LargeChatGroup) {
                ((Robot_LargeChatGroup) 机器人).说(e.getMessage());
                ((Robot_LargeChatGroup) 机器人).流星语发布结束(false);
            }
            return;
        }
        final int 最大兆数 = ProtocolParameters.最大值_小宇宙文件数据长度_兆;
        if (SS包.length > 最大兆数 * 1024 * 1024) {
            if (机器人 instanceof Robot_MainControl) {
                ((Robot_MainControl) 机器人).说(界面文字.获取(96, "总数据量不可以超过#%MB。", new Object[] {最大兆数}));
                ((Robot_MainControl) 机器人).流星语发布结束(false);
            } else if (机器人 instanceof Robot_LargeChatGroup) {
                ((Robot_LargeChatGroup) 机器人).说(界面文字.获取(96, "总数据量不可以超过#%MB。", new Object[] {最大兆数}));
                ((Robot_LargeChatGroup) 机器人).流星语发布结束(false);
            }
            return;
        }
        if (机器人 instanceof Robot_MainControl) {
            ((Robot_MainControl) 机器人).发布流星语(SS包);
        } else if (机器人 instanceof Robot_LargeChatGroup) {
            ((Robot_LargeChatGroup) 机器人).发布流星语(SS包);
        }
    }

    private void 发布商品(String XML) {
        byte SS包[];
        SSPackageCreator SS包生成器 = new SSPackageCreator();
        try {
            Element 元素 = 获取XML文档根元素(XML);
            if (!元素.getTagName().equals("Goods")) {
                return;
            }
            NodeList 元素列表 = 元素.getElementsByTagName("Title");
            String 标题 = ((Element) 元素列表.item(0)).getTextContent();
            SS包生成器.添加_有标签("标题", 标题);
            元素列表 = 元素.getElementsByTagName("Domain_Read");
            String 域名_读取 = ((Element) 元素列表.item(0)).getTextContent();
            SS包生成器.添加_有标签("域名_读取", 域名_读取);
            元素列表 = 元素.getElementsByTagName("Style");
            byte 样式 = Byte.parseByte(((Element) 元素列表.item(0)).getTextContent());
            SS包生成器.添加_有标签("样式", 样式);
            元素列表 = 元素.getElementsByTagName("Price");
            double 价格 = Double.parseDouble(((Element) 元素列表.item(0)).getTextContent());
            SS包生成器.添加_有标签("价格", 价格);
            元素列表 = 元素.getElementsByTagName("Currency");
            String 币种 = ((Element) 元素列表.item(0)).getTextContent();
            SS包生成器.添加_有标签("币种", 币种);
            元素列表 = 元素.getElementsByTagName("Buy");
            String 购买链接 = ((Element) 元素列表.item(0)).getTextContent();
            SS包生成器.添加_有标签("购买链接", 购买链接);
            元素列表 = 元素.getElementsByTagName("Body");
            元素列表 = ((Element) 元素列表.item(0)).getChildNodes();
            Element 元素2;
            SSPackageCreator SS包生成器2;
            byte 字节数组[];
            int i;
            for (i = 0; i < 元素列表.getLength(); i++) {
                元素2 = ((Element) 元素列表.item(i));
                switch (元素2.getTagName()) {
                    case "Text":
                        SS包生成器2 = new SSPackageCreator();
                        SS包生成器2.添加_有标签("是图片", false);
                        SS包生成器2.添加_有标签("文本", 元素2.getTextContent());
                        SS包生成器.添加_有标签("段落", SS包生成器2);
                        break;
                    case "Image":
                        SS包生成器2 = new SSPackageCreator();
                        SS包生成器2.添加_有标签("是图片", true);
                        String 路径 = 元素2.getTextContent();
                        SS包生成器2.添加_有标签("扩展名", SharedMethod.获取扩展名(路径));
                        字节数组 = SharedMethod.读取文件的全部字节(路径);
                        if (字节数组 == null) {
                            return;
                        }
                        SS包生成器2.添加_有标签("图片数据", 字节数组);
                        SS包生成器.添加_有标签("段落", SS包生成器2);
                        break;
                }
            }

            SS包 = SS包生成器.生成SS包();
        } catch (Exception e) {
            ((Robot_MainControl) 机器人).说(e.getMessage());
            ((Robot_MainControl) 机器人).商品发布结束(false);
            return;
        }
        final int 最大兆数 = ProtocolParameters.最大值_小宇宙文件数据长度_兆;
        if (SS包.length > 最大兆数 * 1024 * 1024) {
            ((Robot_MainControl) 机器人).说(界面文字.获取(96, "总数据量不可以超过#%MB。", new Object[] {最大兆数}));
            ((Robot_MainControl) 机器人).商品发布结束(false);
            return;
        }
        ((Robot_MainControl) 机器人).发布商品(SS包);
    }

    private Element 获取XML文档根元素(String XML文本) throws Exception {
        DocumentBuilderFactory 文档建造工厂 = DocumentBuilderFactory.newInstance();
        DocumentBuilder 文档建造器 = 文档建造工厂.newDocumentBuilder();
        ByteArrayInputStream 字节数组输入流 = null;
        字节数组输入流 = new ByteArrayInputStream(XML文本.getBytes());
        Document 文档 = 文档建造器.parse(字节数组输入流);
        字节数组输入流.close();
        return 文档.getDocumentElement();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (网页浏览器 != null) {
            网页浏览器.onResume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (网页浏览器 != null) {
            网页浏览器.onPause();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (网页浏览器 != null) {
            网页浏览器.removeAllViews();
            网页浏览器.destroy();
            网页浏览器 = null;
        }
    }

    void 发送JS(String JS) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            网页浏览器.evaluateJavascript("(" + JS + ")()", new ValueCallback<String>() {
                @Override
                public void onReceiveValue(String value) {

                }
            });
        } else {
            网页浏览器.loadUrl("javascript:" + JS);
        }
    }

}
