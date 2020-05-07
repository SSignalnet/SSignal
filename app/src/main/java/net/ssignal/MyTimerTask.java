package net.ssignal;

import android.os.Message;

import java.util.TimerTask;

class MyTimerTask extends TimerTask {

    private MyHandler 跨线程调用器;
    private int 消息代号;

    MyTimerTask(MyHandler 跨线程调用器, int 消息代号) {
        this.跨线程调用器 = 跨线程调用器;
        this.消息代号 = 消息代号;
    }

    @Override
    public void run() {
        if (跨线程调用器 != null) {
            Message 消息 = new Message();
            消息.what = 消息代号;
            跨线程调用器.sendMessage(消息);
        }
    }

    @Override
    public boolean cancel() {
        跨线程调用器 = null;
        return super.cancel();
    }

}
