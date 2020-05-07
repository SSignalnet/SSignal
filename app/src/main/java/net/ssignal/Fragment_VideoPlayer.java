package net.ssignal;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.VideoView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import net.ssignal.protocols.ProtocolPath;

import static net.ssignal.Activity_Main.主活动;

public class Fragment_VideoPlayer extends Fragment implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener {

    private View 第一控件;
    private VideoView 播放器;
    private double 视频高宽比例;
    String 文件路径;

    @Override
    public View onCreateView(LayoutInflater 查找器, ViewGroup 控件容器, Bundle 已保存的实例状态) {
        第一控件 = 查找器.inflate(R.layout.video, 控件容器, false);
        return 第一控件;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        播放器 = (VideoView)第一控件.findViewById(R.id.播放器);
        播放器.setOnPreparedListener(this);
        播放器.setOnCompletionListener(this);
        final ViewTreeObserver 控件树观察器 = 播放器.getViewTreeObserver();
        控件树观察器.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                控件树观察器.removeOnGlobalLayoutListener(this);
                调整播放器布局();
            }
        });
        播放器.setMediaController(new MediaController(getActivity()));
        if (文件路径.startsWith("https://") || 文件路径.startsWith("http://")) {
            if (BuildConfig.DEBUG && !ProtocolPath.调试时访问真实网站) {
                if (文件路径.startsWith("https://" + ProtocolPath.IP地址_调试 + ":" + ProtocolPath.测试域名1传送服务器本机IIS调试端口_SSL + "/")) {
                    文件路径 = 文件路径.replace("https://" + ProtocolPath.IP地址_调试 + ":" + ProtocolPath.测试域名1传送服务器本机IIS调试端口_SSL + "/", "http://" + ProtocolPath.IP地址_调试 + ":" + ProtocolPath.测试域名1传送服务器本机IIS调试端口 + "/");
                } else if (文件路径.startsWith("https://localhost:" + ProtocolPath.测试域名1小宇宙中心服务器本机IIS调试端口_SSL + "/")) {
                    文件路径 = 文件路径.replace("https://localhost:" + ProtocolPath.测试域名1小宇宙中心服务器本机IIS调试端口_SSL + "/", "http://" + ProtocolPath.IP地址_调试 + ":" + ProtocolPath.测试域名1小宇宙中心服务器本机IIS调试端口 + "/");
                } else if (文件路径.startsWith("https://" + ProtocolPath.IP地址_调试 + ":" + ProtocolPath.测试域名1大群聊服务器本机IIS调试端口_SSL + "/")) {
                    文件路径 = 文件路径.replace("https://" + ProtocolPath.IP地址_调试 + ":" + ProtocolPath.测试域名1大群聊服务器本机IIS调试端口_SSL + "/", "http://" + ProtocolPath.IP地址_调试 + ":" + ProtocolPath.测试域名1大群聊服务器本机IIS调试端口 + "/");
                }
            }
            播放器.setVideoURI(Uri.parse(文件路径));
        } else {
            播放器.setVideoPath(文件路径);
        }
        播放器.start();
    }

    public void onPrepared(MediaPlayer mp) {
        if (视频高宽比例 == 0) {
            视频高宽比例 = (double)mp.getVideoHeight() / (double)mp.getVideoWidth();
            调整播放器布局();
        }
    }

    private void 调整播放器布局() {
        if (视频高宽比例 > 0) {
            int 容器宽度 = this.getView().getMeasuredWidth();
            int 容器高度 = this.getView().getMeasuredHeight();
            RelativeLayout.LayoutParams 布局参数;
            if (容器高度 > 容器宽度) {
                int 播放器高度 = (int)(容器宽度 * 视频高宽比例);
                布局参数 = new RelativeLayout.LayoutParams(容器宽度, 播放器高度);
                播放器.setX(0);
                播放器.setY((容器高度 - 播放器高度) / 2);
            } else {
                int 播放器宽度 = (int)(容器高度 / 视频高宽比例);
                布局参数 = new RelativeLayout.LayoutParams(播放器宽度, 容器高度);
                播放器.setY(0);
                播放器.setX((容器宽度 - 播放器宽度) / 2);
            }
            播放器.setLayoutParams(布局参数);
        }
    }

    public void onCompletion(MediaPlayer mp) {
        主活动.关闭播放视频的窗体();
    }

    @Override
    public void onResume() {
        super.onResume();
        播放器.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        播放器.pause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (播放器 != null) {
            播放器.suspend();
            播放器 = null;
        }
    }

}
