package net.ssignal;

import android.Manifest;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import net.ssignal.language.Text;
import net.ssignal.util.SharedMethod;

import static net.ssignal.Fragment_Main.主窗体;
import static net.ssignal.User.当前用户;
import static net.ssignal.Robot_MainControl.主控机器人;

public class Activity_Main extends AppCompatActivity {

    static Activity_Main 主活动;

    private Fragment_TaskNameList 任务名称列表;
    private Fragment_DownloadFile 下载文件的窗体;
    private Fragment_ViewPicture 查看图片的窗体;
    private Fragment_VideoPlayer 播放视频的窗体;

    private final byte 窗体代码_主窗体 = 0;
    private final byte 窗体代码_任务名称列表 = 1;
    private final byte 窗体代码_下载文件的窗体 = 2;
    private final byte 窗体代码_查看图片的窗体 = 3;
    private final byte 窗体代码_播放视频的窗体 = 4;
    private byte 当前窗体 = 窗体代码_主窗体;

    private Object 请求者;
    private final int 请求码_拍照 = 1;
    private final int 请求码_选取图片 = 2;
    private final int 请求码_录音 = 3;
    private final int 请求码_选取文件 = 4;
    private final int 请求码_录像 = 5;

    static boolean 已暂停 = true;
    static boolean 有新消息 = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        主活动 = this;
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        setContentView(R.layout.activity_main);
        Fragment_Main 主窗体 = new Fragment_Main();
        FragmentManager 版面管理器 = getSupportFragmentManager();
        FragmentTransaction 版面切换器 = 版面管理器.beginTransaction();
        版面切换器.add(R.id.主活动, 主窗体);
        版面切换器.commit();
        if (Text.界面文字 == null) { SharedMethod.载入界面文字(this); }
        TaskName 类 = new TaskName();
        类 = null;
        if (当前用户 == null) { 当前用户 = new User(getApplicationContext()); }
    }

    void 弹出任务名称列表() {
        FragmentManager 版面管理器 = getSupportFragmentManager();
        FragmentTransaction 版面切换器 = 版面管理器.beginTransaction();
        版面切换器.hide(主窗体);
        if (任务名称列表 == null) {
            任务名称列表 = new Fragment_TaskNameList();
            任务名称列表.当前机器人 = 主窗体.当前聊天控件.机器人;
            版面切换器.add(R.id.主活动, 任务名称列表);
        } else {
            任务名称列表.载入任务名称(主窗体.当前聊天控件.机器人);
            版面切换器.show(任务名称列表);
        }
        版面切换器.commit();
        当前窗体 = 窗体代码_任务名称列表;
    }

    void 关闭任务名称列表() {
        FragmentManager 版面管理器 = getSupportFragmentManager();
        FragmentTransaction 版面切换器 = 版面管理器.beginTransaction();
        版面切换器.hide(任务名称列表);
        版面切换器.show(主窗体);
        版面切换器.commit();
        当前窗体 = 窗体代码_主窗体;
    }

    void 弹出下载文件的窗体(String 下载路径, String 保存路径) {
        FragmentManager 版面管理器 = getSupportFragmentManager();
        FragmentTransaction 版面切换器 = 版面管理器.beginTransaction();
        版面切换器.hide(主窗体);
        if (下载文件的窗体 == null) {
            下载文件的窗体 = new Fragment_DownloadFile();
            下载文件的窗体.新下载任务(下载路径, 保存路径, true);
            版面切换器.add(R.id.主活动, 下载文件的窗体);
        } else {
            下载文件的窗体.新下载任务(下载路径, 保存路径, false);
            版面切换器.show(下载文件的窗体);
        }
        版面切换器.commitAllowingStateLoss();
        当前窗体 = 窗体代码_下载文件的窗体;
    }

    private void 关闭下载文件的窗体() {
        FragmentManager 版面管理器 = getSupportFragmentManager();
        FragmentTransaction 版面切换器 = 版面管理器.beginTransaction();
        版面切换器.hide(下载文件的窗体);
        版面切换器.show(主窗体);
        版面切换器.commit();
        当前窗体 = 窗体代码_主窗体;
    }

    void 弹出查看图片的窗体(String 文件路径) {
        FragmentManager 版面管理器 = getSupportFragmentManager();
        FragmentTransaction 版面切换器 = 版面管理器.beginTransaction();
        版面切换器.hide(主窗体);
        查看图片的窗体 = new Fragment_ViewPicture();
        查看图片的窗体.文件路径 = 文件路径;
        版面切换器.add(R.id.主活动, 查看图片的窗体);
        版面切换器.commit();
        当前窗体 = 窗体代码_查看图片的窗体;
    }

    void 关闭查看图片的窗体() {
        查看图片的窗体.取消下载();
        FragmentManager 版面管理器 = getSupportFragmentManager();
        FragmentTransaction 版面切换器 = 版面管理器.beginTransaction();
        版面切换器.remove(查看图片的窗体);
        版面切换器.show(主窗体);
        版面切换器.commit();
        当前窗体 = 窗体代码_主窗体;
        查看图片的窗体 = null;
    }

    void 弹出播放视频的窗体(String 文件路径) {
        FragmentManager 版面管理器 = getSupportFragmentManager();
        FragmentTransaction 版面切换器 = 版面管理器.beginTransaction();
        版面切换器.hide(主窗体);
        播放视频的窗体 = new Fragment_VideoPlayer();
        播放视频的窗体.文件路径 = 文件路径;
        版面切换器.add(R.id.主活动, 播放视频的窗体);
        版面切换器.commit();
        当前窗体 = 窗体代码_播放视频的窗体;
    }

    void 关闭播放视频的窗体() {
        FragmentManager 版面管理器 = getSupportFragmentManager();
        FragmentTransaction 版面切换器 = 版面管理器.beginTransaction();
        版面切换器.remove(播放视频的窗体);
        版面切换器.show(主窗体);
        版面切换器.commit();
        当前窗体 = 窗体代码_主窗体;
        播放视频的窗体 = null;
    }

    Point 获取宽高() {
        RelativeLayout 相对布局 = (RelativeLayout) findViewById(R.id.主活动);
        return new Point(相对布局.getMeasuredWidth(), 相对布局.getMeasuredHeight());
    }

    @Override
    protected void onPause() {
        super.onPause();
        已暂停 = true;
        Context 运行环境 = getApplicationContext();
        AlarmManager 定时任务管理器 = (AlarmManager) 运行环境.getSystemService(运行环境.ALARM_SERVICE);
        long 间隔 = 600000;
        Intent 意图 = new Intent(运行环境, AlarmReceiver.class);
        意图.setAction("net.ssignal.Activity_Main");
        PendingIntent 待定意图 = PendingIntent.getBroadcast(运行环境, 1, 意图, 0);
        定时任务管理器.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + 间隔, 间隔, 待定意图);
    }

    @Override
    protected void onResume() {
        super.onResume();
        已暂停 = false;
        停止闹钟();
        if (有新消息) {
            有新消息 = false;
            if (主控机器人 != null) {
                NotificationManager 通知管理器 = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                通知管理器.cancel(主控机器人.通知编号);
            }
        }
    }

    @Override
    protected void onDestroy() {
        停止闹钟();
        if (主控机器人 != null) {
            主控机器人.聊天控件 = null;
            主控机器人.输入框 = null;
            主控机器人.不再提示 = false;
        }
        if (当前用户 != null) {
            if (!当前用户.已登录()) {
                主控机器人 = null;
                当前用户 = null;
            }
        }
        主活动 = null;
        主窗体 = null;
        super.onDestroy();
    }

    private void 停止闹钟() {
        Context 运行环境 = getApplicationContext();
        AlarmManager 定时任务管理器 = (AlarmManager) 运行环境.getSystemService(运行环境.ALARM_SERVICE);
        Intent 意图 = new Intent(运行环境, AlarmReceiver.class);
        意图.setAction("net.ssignal.Activity_Main");
        PendingIntent 待定意图 = PendingIntent.getBroadcast(运行环境, 1, 意图, 0);
        定时任务管理器.cancel(待定意图);
    }

    void 终止活动() {
        finish();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            switch (当前窗体) {
                case 窗体代码_任务名称列表:
                    关闭任务名称列表();
                    break;
                case 窗体代码_下载文件的窗体:
                    关闭下载文件的窗体();
                    break;
                case 窗体代码_查看图片的窗体:
                    关闭查看图片的窗体();
                    break;
                case 窗体代码_播放视频的窗体:
                    关闭播放视频的窗体();
                    break;
                default:
                    moveTaskToBack(true);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    void 登录成功() {
        if (任务名称列表 != null) {
            任务名称列表.当前机器人 = null;
        }
        当前用户.保存登录信息();
    }

    void 注销成功() {
        if (任务名称列表 != null) {
            任务名称列表.当前机器人 = null;
        }
        当前用户.注销();
        主控机器人.关闭与传送服务器的连接();
        Context 运行环境 = getApplicationContext();
        当前用户 = new User(运行环境);
        if (SharedMethod.服务是否运行(运行环境, "Service_SSignal")) {
            Intent 意图 = new Intent(运行环境, Service_SSignal.class);
            stopService(意图);
        }
        finish();
    }

    boolean 选取文件前检查读取存储卡的权限() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            return false;
        } else {
            return true;
        }
    }

    void 选取图片前请求读取存储卡的权限(Object 请求者) {
        this.请求者 = 请求者;
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 请求码_选取图片);
    }

    void 选取文件前请求读取存储卡的权限(Object 请求者) {
        this.请求者 = 请求者;
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 请求码_选取文件);
    }

    void 选择图片(Object 请求者) {
        this.请求者 = 请求者;
        Intent 意图 = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        意图.setType("image/*");
        startActivityForResult(意图, 请求码_选取图片);
//        if (请求者 instanceof Robot_MainControl) {
//        } else {
//            Intent 意图 = new Intent();
//            意图.setType("image/*");
//            意图.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
//            意图.setAction(Intent.ACTION_GET_CONTENT);
//            startActivityForResult(Intent.createChooser(意图, "11"), 请求码_选取图片);
//        }
    }

    void 选择文件(Object 请求者) {
        this.请求者 = 请求者;
        Intent 意图 = new Intent(Intent.ACTION_GET_CONTENT);
        意图.setType("*/*");
        意图.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(意图, 请求码_选取文件);
    }

    void 选择视频(Object 请求者) {
        this.请求者 = 请求者;
        Intent 意图 = new Intent(Intent.ACTION_GET_CONTENT);
        意图.setType("video/*.mp4");
        意图.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(意图, 请求码_选取文件);
    }

    boolean 检查录音权限() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            return false;
        } else {
            return true;
        }
    }

    void 请求录音权限(Robot 机器人) {
        请求者 = 机器人;
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 请求码_录音);
    }

    boolean 检查拍照的权限() {
        final String[] VIDEO_PERMISSIONS = {
                Manifest.permission.CAMERA,
        };
        for (String permission : VIDEO_PERMISSIONS) {
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    void 请求拍照的权限(Robot 机器人) {
        请求者 = 机器人;
        final String[] VIDEO_PERMISSIONS = {
                Manifest.permission.CAMERA,
        };
        ActivityCompat.requestPermissions(this, VIDEO_PERMISSIONS, 请求码_拍照);
    }

    boolean 检查录音录像的权限() {
        final String[] VIDEO_PERMISSIONS = {
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO,
        };
        for (String permission : VIDEO_PERMISSIONS) {
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    void 请求录音录像的权限(Robot 机器人) {
        请求者 = 机器人;
        final String[] VIDEO_PERMISSIONS = {
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO,
        };
        ActivityCompat.requestPermissions(this, VIDEO_PERMISSIONS, 请求码_录像);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 请求码_录音:
                if (grantResults.length == 1) {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        if (请求者 instanceof Robot_OneOnOne) {
                            ((Robot_OneOnOne) 请求者).发送语音还是文字(true);
                        } else if (请求者 instanceof Robot_SmallChatGroup) {
                            ((Robot_SmallChatGroup) 请求者).发送语音还是文字(true);
                        }
                    }
                }
                break;
            case 请求码_拍照:
                if (grantResults.length == 2) {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                        if (请求者 instanceof Robot_OneOnOne) {
                            ((Robot_OneOnOne) 请求者).拍照();
                        } else if (请求者 instanceof Robot_SmallChatGroup) {
                            ((Robot_SmallChatGroup) 请求者).拍照();
                        } else if (请求者 instanceof Robot_LargeChatGroup) {
                            ((Robot_LargeChatGroup) 请求者).拍照();
                        }
                    }
                }
                break;
            case 请求码_选取图片:
                if (grantResults.length == 2) {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                        选择图片(请求者);
                    }
                }
                break;
            case 请求码_选取文件:
                if (grantResults.length == 2) {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                        if (!(请求者 instanceof Fragment_TinyUniverse)) {
                            选择视频(请求者);
                        } else {
                            选择文件(请求者);
                        }
                    }
                }
                break;
            case 请求码_录像:
                if (grantResults.length == 2) {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                        if (请求者 instanceof Robot_OneOnOne) {
                            ((Robot_OneOnOne) 请求者).录制短视频();
                        } else if (请求者 instanceof Robot_SmallChatGroup) {
                            ((Robot_SmallChatGroup) 请求者).录制短视频();
                        } else if (请求者 instanceof Robot_LargeChatGroup) {
                            ((Robot_LargeChatGroup) 请求者).录制短视频();
                        }
                    }
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 请求码_选取图片:
                if (resultCode == Activity.RESULT_OK) {
                    if (data != null) {
                        String[] filePathColumn = {MediaStore.Images.Media.DATA};
                        Cursor cursor = getContentResolver().query(data.getData(), filePathColumn, null, null, null);
                        cursor.moveToFirst();
                        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                        String 图片路径 = cursor.getString(columnIndex);
                        cursor.close();
                        if (请求者 instanceof Robot_OneOnOne) {
                            ((Robot_OneOnOne) 请求者).发送图片2(new String[] {图片路径}, getFilesDir().toString());
                        } else if (请求者 instanceof Robot_SmallChatGroup) {
                            ((Robot_SmallChatGroup) 请求者).发送图片2(new String[] {图片路径}, getFilesDir().toString());
                        } else if (请求者 instanceof Robot_LargeChatGroup) {
                            if (!((Robot_LargeChatGroup) 请求者).正在选择群图标) {
                                ((Robot_LargeChatGroup) 请求者).发送图片2(new String[]{图片路径}, getFilesDir().toString());
                            } else {
                                ((Robot_LargeChatGroup) 请求者).选择图标图片2(图片路径, getFilesDir().toString());
                            }
                        } else if (请求者 instanceof Fragment_TinyUniverse) {
                            ((Fragment_TinyUniverse) 请求者).选中图片(图片路径, getCacheDir().toString());
                        } else if (请求者 instanceof Robot_MainControl) {
                            ((Robot_MainControl) 请求者).选择头像图片2(图片路径, getFilesDir().toString());
                        }
//                            ClipData cd = data.getClipData();
//                            if (cd != null) {
//                                String 图片路径[] = new String[cd.getItemCount()];
//                                for (int i = 0; i < cd.getItemCount(); i++) {
//                                    cd.getItemAt(i).getUri();
//                                    图片路径[i] = cd.getItemAt(i).getUri().toString();
//                                }
////                                if (请求者 instanceof Robot_OneOnOne) {
////                                    ((Robot_OneOnOne) 请求者).发送图片2(图片路径, getFilesDir().toString());
////                                } else if (请求者 instanceof Robot_SmallChatGroup) {
////                                    ((Robot_SmallChatGroup) 请求者).发送图片2(图片路径, getFilesDir().toString());
////                                }
//                            }
                    }
                }
                break;
            case 请求码_选取文件:
                if (resultCode == Activity.RESULT_OK) {
                    if (data != null) {
                        Uri uri = data.getData();
                        if (uri != null) {
                            String 文件路径 = getPath(uri);
                            if (文件路径 != null) {
                                if (请求者 instanceof Robot_OneOnOne) {
                                    ((Robot_OneOnOne) 请求者).发送文件2(文件路径);
                                } else if (请求者 instanceof Robot_SmallChatGroup) {
                                    ((Robot_SmallChatGroup) 请求者).发送文件2(文件路径);
                                } else if (请求者 instanceof Robot_LargeChatGroup) {
                                    ((Robot_LargeChatGroup) 请求者).发送文件2(文件路径);
                                } else if (请求者 instanceof Fragment_TinyUniverse) {
                                    ((Fragment_TinyUniverse) 请求者).选中视频(文件路径, getCacheDir().toString());
                                }
                            }
                        }
                    }
                }
                break;
        }
    }

    public String getPath(final Uri uri) {
        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
        if (isKitKat && DocumentsContract.isDocumentUri(this, uri)) {
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            }
            else if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId( Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
                return getDataColumn(contentUri, null, null);
            } // MediaProvider
            else if (isMediaDocument(uri)) {
                // Log.i(TAG,"isMediaDocument***"+uri.toString());
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{split[1]};
                return getDataColumn(contentUri, selection, selectionArgs);
            }
        } // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            // Log.i(TAG,"content***"+uri.toString());
            return getDataColumn(uri, null, null);
        } // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            // Log.i(TAG,"file***"+uri.toString());
            return uri.getPath();
        }
        return null;
    }

    public String getDataColumn(Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null; final String column = "_data";
        final String[] projection = {column};
        try {
            cursor = getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return null;
    }

    public boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    public boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    public boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    //    private void 检查故障信息() {
//        File 文件 = new File(getExternalCacheDir() + "/error.txt");
//        if (!文件.exists() || !文件.isFile()) {
//            return;
//        }
//        String 文件内容 = null;
//        try {
//            RandomAccessFile 文件随机访问器 = new RandomAccessFile(文件, "r");
//            if (文件随机访问器.length() > 0) {
//                byte 字节数组[] = new byte[(int) 文件随机访问器.length()];
//                文件随机访问器.read(字节数组, 0, 字节数组.length);
//                文件随机访问器.close();
//                文件内容 = new String(字节数组, "UTF-8");
//            } else {
//                文件随机访问器.close();
//                文件.delete();
//                return;
//            }
//        } catch (Exception e) {
//            return;
//        }
//        跨线程调用器 = new MyHandler(this);
//        httpConnection HTTP连接 = new httpConnection(跨线程调用器);
//        String URL参数 = "?C=ReportAppErrors&UserID=" + SharedMethod.登录ID + "&Credential=" + SharedMethod.登录凭据;
//        HTTP连接.发送("ReportAppErrors", URL参数, 文件内容);
//    }

}
