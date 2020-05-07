package net.ssignal;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

class MyHandler extends Handler {

    boolean 丢弃 = false;
    private Object 创建者;

    MyHandler() {}
    MyHandler(Object 创建者1) {
        this.创建者 = 创建者1;
    }
    MyHandler(Looper L) {super(L);}

    @Override
    public void handleMessage(Message msg) {
        if (丢弃) { return; }
        super.handleMessage(msg);
        if (创建者 != null) {
            if (创建者 instanceof Robot_MainControl) {
                ((Robot_MainControl) 创建者).处理跨线程数据(msg);
            } else if (创建者 instanceof Robot_OneOnOne) {
                ((Robot_OneOnOne) 创建者).处理跨线程数据(msg);
            } else if (创建者 instanceof Robot_LargeChatGroup) {
                ((Robot_LargeChatGroup) 创建者).处理跨线程数据(msg);
            } else if (创建者 instanceof Fragment_Main) {
                ((Fragment_Main) 创建者).处理跨线程数据(msg);
            } else if (创建者 instanceof Fragment_Chating) {
                ((Fragment_Chating) 创建者).处理跨线程数据(msg);
            } else if (创建者 instanceof Audio_Playing) {
                ((Audio_Playing) 创建者).处理跨线程数据(msg);
            } else if (创建者 instanceof Fragment_TinyUniverse) {
                ((Fragment_TinyUniverse) 创建者).处理跨线程数据(msg);
            } else if (创建者 instanceof Fragment_ViewPicture) {
                ((Fragment_ViewPicture) 创建者).处理跨线程数据(msg);
            } else if (创建者 instanceof Fragment_DownloadFile) {
                ((Fragment_DownloadFile) 创建者).处理跨线程数据(msg);
            } else if (创建者 instanceof Activity_TakePhoto) {
                ((Activity_TakePhoto) 创建者).处理跨线程数据(msg);
            } else if (创建者 instanceof Activity_RecordVideo) {
                ((Activity_RecordVideo) 创建者).处理跨线程数据(msg);
            } else if (创建者 instanceof Robot_SystemManagement) {
                ((Robot_SystemManagement) 创建者).处理跨线程数据(msg);
            }
        }
    }

}
