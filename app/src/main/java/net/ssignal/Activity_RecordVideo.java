package net.ssignal;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.MediaMetadataRetriever;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import net.ssignal.camera.AutoFitTextureView;
import net.ssignal.protocols.ProtocolParameters;
import net.ssignal.util.SharedMethod;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import static net.ssignal.User.当前用户;
import static net.ssignal.language.Text.界面文字;

public class Activity_RecordVideo extends AppCompatActivity implements View.OnClickListener, ActivityCompat.OnRequestPermissionsResultCallback {

    private static final int SENSOR_ORIENTATION_DEFAULT_DEGREES = 90;
    private static final int SENSOR_ORIENTATION_INVERSE_DEGREES = 270;
    private static final SparseIntArray DEFAULT_ORIENTATIONS = new SparseIntArray();
    private static final SparseIntArray INVERSE_ORIENTATIONS = new SparseIntArray();

//    private static final int REQUEST_VIDEO_PERMISSIONS = 1;
//    private static final String FRAGMENT_DIALOG = "dialog";

    static {
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_0, 90);
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_90, 0);
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_180, 270);
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    static {
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_0, 270);
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_90, 180);
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_180, 90);
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_270, 0);
    }

    private AutoFitTextureView 媒体呈现区;
    private android.hardware.camera2.CameraDevice 摄像头;
    private Integer mSensorOrientation;
    private Size 视频画面尺寸;
    private Size 预览画面尺寸;
    private MediaRecorder 媒体录制器;
    private Semaphore 摄像头开关锁 = new Semaphore(1);
    private CameraCaptureSession 预览会话;
    private CaptureRequest.Builder 预览构建器;
    private Surface 表面_录制器;
    private Handler 跨线程调用器_录像;
    private HandlerThread 后台线程;
    private MyHandler 跨线程调用器1;

    private boolean 正在录像 = false;
    private static boolean 前置摄像头 = false;

    private Timer 定时器;
    private TimerTask 定时任务;
    private int 最大录像时长, 剩余秒数;
    private TextView 文字_剩余秒数;

    static Robot 机器人;
    private static Activity_RecordVideo 本活动;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_camera);
        媒体呈现区 = (AutoFitTextureView) findViewById(R.id.媒体呈现区);
        Button 按钮 = (Button) findViewById(R.id.按钮_开始);
        按钮.setOnClickListener(this);
        按钮 = (Button) findViewById(R.id.按钮_取消);
        按钮.setText(界面文字.获取(311, "取消"));
        按钮.setOnClickListener(this);
        按钮 = (Button) findViewById(R.id.按钮_发送);
        按钮.setText(界面文字.获取(31, "发送"));
        按钮.setOnClickListener(this);
        TextView 文字 = (TextView) findViewById(R.id.文字_前后摄像头);
        if (!前置摄像头) {
            文字.setText(界面文字.获取(50, "后置摄像头"));
        } else {
            文字.setText(界面文字.获取(51, "前置摄像头"));
        }
        文字.setOnClickListener(this);
        定时器 = new Timer();
        文字_剩余秒数 = (TextView) findViewById(R.id.文字_剩余秒数);
        跨线程调用器1 = new MyHandler(this);
        本活动 = this;
    }

    @Override
    public void onResume() {
        super.onResume();
        启动后台线程();
        if (媒体呈现区.isAvailable()) {
            打开摄像头(媒体呈现区.getWidth(), 媒体呈现区.getHeight());
        } else {
            媒体呈现区.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
                @Override
                public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                    打开摄像头(width, height);
                }

                @Override
                public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
                    configureTransform(width, height);
                }

                @Override
                public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                    return true;
                }

                @Override
                public void onSurfaceTextureUpdated(SurfaceTexture surface) {

                }
            });
        }
    }

    @Override
    public void onPause() {
        关闭摄像头(false);
        停止后台线程();
        super.onPause();
    }

    public void onClick(View 控件) {
        switch (控件.getId()) {
            case R.id.按钮_开始:
                开始或停止录像();
                break;
            case R.id.按钮_发送:
                String 路径 = getFilesDir().toString() + "/" + 当前用户.获取英语讯宝地址();
                File 目录 = new File(路径);
                if (!目录.exists() || !目录.isDirectory()) {
                    目录.mkdir();
                }
                File 文件 = new File(getCacheDir().toString() + "/record.mp4");
                路径 += "/" + SharedMethod.生成大小写英文字母与数字的随机字符串(20) + ".mp4";
                File 目标文件 = new File(路径);
                if (目标文件.exists()) {目标文件.delete();}
                if (文件.renameTo(目标文件)) {
                    MediaMetadataRetriever 媒体数据提取器 = new MediaMetadataRetriever();
                    媒体数据提取器.setDataSource(目标文件.getAbsolutePath());
                    Bitmap 原图;
                    try {
                        原图 = 媒体数据提取器.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
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
                        文件 = new File(目标文件.getAbsolutePath() + ".jpg");
                        if (文件.exists()) {
                            文件.delete();
                        }
                        SharedMethod.保存位图(压缩后图片, 文件.getAbsolutePath());
                        机器人.发送短视频2(目标文件.getAbsolutePath(), (short) 压缩后图片.getWidth(), (short) 压缩后图片.getHeight());
                    } catch (Exception e) {
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
                        原图 = null;
                    }
                    媒体数据提取器.release();
                }
                finish();
                break;
            case R.id.按钮_取消:
                finish();
                break;
            case R.id.文字_前后摄像头:
                关闭摄像头2();
                if (!前置摄像头) {
                    前置摄像头 = true;
                    ((TextView)控件).setText(界面文字.获取(51, "前置摄像头"));
                } else {
                    前置摄像头 = false;
                    ((TextView)控件).setText(界面文字.获取(50, "后置摄像头"));
                }
                打开摄像头(媒体呈现区.getWidth(), 媒体呈现区.getHeight());
                break;
        }
    }

    private void 开始或停止录像() {
        if (!正在录像) {
            if (摄像头 == null) {
                Button 按钮 = (Button) findViewById(R.id.按钮_开始);
                按钮.setText(界面文字.获取(29, "开始"));
                按钮 = (Button) findViewById(R.id.按钮_取消);
                按钮.setVisibility(View.GONE);
                按钮 = (Button) findViewById(R.id.按钮_发送);
                按钮.setVisibility(View.GONE);
                TextView 文字 = (TextView) findViewById(R.id.文字_前后摄像头);
                文字.setVisibility(View.VISIBLE);
                文字_剩余秒数.setVisibility(View.GONE);
                File 文件 = new File(getCacheDir().toString() + "/record.mp4");
                if (文件.exists()) {
                    文件.delete();
                }
                打开摄像头(媒体呈现区.getWidth(), 媒体呈现区.getHeight());
                return;
            }
            开始录像();
        } else if (最大录像时长 - 剩余秒数 >= 1) {
            关闭摄像头(true);
        }
    }

    private void 打开摄像头(int 宽度, int 高度) {
        CameraManager 摄像头管理器 = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            if (!摄像头开关锁.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw new RuntimeException("对摄像头的开启进行尝试锁定时超时了。");
            }
            String[] 摄像头编号数组 = 摄像头管理器.getCameraIdList();
            CameraCharacteristics 摄像头特征 = null;
            int i;
            for (i = 0; i < 摄像头编号数组.length; i++) {
                摄像头特征 = 摄像头管理器.getCameraCharacteristics(摄像头编号数组[i]);
                if (!前置摄像头) {
                    if (摄像头特征.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK) {
                        break;
                    }
                } else {
                    if (摄像头特征.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT) {
                        break;
                    }
                }
            }
            if (i == 摄像头编号数组.length) { return; }
            StreamConfigurationMap 流配置对应表 = 摄像头特征.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            mSensorOrientation = 摄像头特征.get(CameraCharacteristics.SENSOR_ORIENTATION);
            视频画面尺寸 = 选择视频画面尺寸(流配置对应表.getOutputSizes(MediaRecorder.class));
            预览画面尺寸 = 选择最佳预览尺寸(流配置对应表.getOutputSizes(SurfaceTexture.class));
            int orientation = getResources().getConfiguration().orientation;
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                媒体呈现区.setAspectRatio(预览画面尺寸.getWidth(), 预览画面尺寸.getHeight());
            } else {
                媒体呈现区.setAspectRatio(预览画面尺寸.getHeight(), 预览画面尺寸.getWidth());
            }
            configureTransform(宽度, 高度);
            摄像头管理器.openCamera(摄像头编号数组[i], new CameraDevice.StateCallback() {
                @Override
                public void onOpened(@NonNull CameraDevice camera) {
                    摄像头 = camera;
                    预览开始();
                    摄像头开关锁.release();
                    if (媒体呈现区 != null) {
                        configureTransform(媒体呈现区.getWidth(), 媒体呈现区.getHeight());
                    }
                }

                @Override
                public void onDisconnected(@NonNull CameraDevice camera) {
                    摄像头开关锁.release();
                    camera.close();
                    摄像头 = null;
                }

                @Override
                public void onError(@NonNull CameraDevice camera, int error) {
                    摄像头开关锁.release();
                    camera.close();
                    摄像头 = null;
                    finish();
                }
            }, 跨线程调用器_录像);
        } catch (CameraAccessException e) {
            Toast.makeText(this, "无法连接摄像头", Toast.LENGTH_SHORT).show();
            finish();
        } catch (SecurityException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

//    private void 请求录像许可() {
//        if (shouldShowRequestPermissionRationale(VIDEO_PERMISSIONS)) {
//            new ConfirmationDialog().show(getSupportFragmentManager(), FRAGMENT_DIALOG);
//        } else {
//            ActivityCompat.requestPermissions(this, VIDEO_PERMISSIONS, REQUEST_VIDEO_PERMISSIONS);
//        }
//    }
//
//    private boolean shouldShowRequestPermissionRationale(String[] permissions) {
//        for (String permission : permissions) {
//            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
//                return true;
//            }
//        }
//        return false;
//    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        //Log.d(TAG, "onRequestPermissionsResult");
//        if (requestCode == REQUEST_VIDEO_PERMISSIONS) {
//            if (grantResults.length == VIDEO_PERMISSIONS.length) {
//                for (int result : grantResults) {
//                    if (result != PackageManager.PERMISSION_GRANTED) {
//                        ErrorDialog.newInstance("获取摄像头和麦克风的权限失败")
//                                .show(getSupportFragmentManager(), FRAGMENT_DIALOG);
//                        break;
//                    }
//                }
//            } else {
//                ErrorDialog.newInstance("获取摄像头和麦克风的权限失败")
//                        .show(getSupportFragmentManager(), FRAGMENT_DIALOG);
//            }
//        } else {
//            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        }
//    }

    private Size 选择视频画面尺寸(Size[] 可选尺寸) {
        Size 某一尺寸;
        for (int i = 可选尺寸.length - 1; i >= 0; i--) {
            某一尺寸 = 可选尺寸[i];
            if (某一尺寸.getHeight() >= 480 || 某一尺寸.getWidth() >= 480) {
                return 某一尺寸;
            }
        }
        return new Size(0, 0);
    }

    private Size 选择最佳预览尺寸(Size[] 可选尺寸) {
        for (Size 某一尺寸 : 可选尺寸) {
            if (某一尺寸.getHeight() >= 视频画面尺寸.getHeight() && ((double)某一尺寸.getHeight() / (double)某一尺寸.getWidth()) == ((double)视频画面尺寸.getHeight() / (double)视频画面尺寸.getWidth())) {
                return 某一尺寸;
            }
        }
        return new Size(0, 0);
    }

    private void configureTransform(int viewWidth, int viewHeight) {
        if (null == 媒体呈现区 || null == 预览画面尺寸) { return; }
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        Matrix matrix = new Matrix();
        RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
        RectF bufferRect = new RectF(0, 0, 预览画面尺寸.getHeight(), 预览画面尺寸.getWidth());
        float centerX = viewRect.centerX();
        float centerY = viewRect.centerY();
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
            float scale = Math.max(
                    (float) viewHeight / 预览画面尺寸.getHeight(),
                    (float) viewWidth / 预览画面尺寸.getWidth());
            matrix.postScale(scale, scale, centerX, centerY);
            matrix.postRotate(90 * (rotation - 2), centerX, centerY);
        }
        媒体呈现区.setTransform(matrix);
    }

    private void 预览开始() {
        if (摄像头 == null || !媒体呈现区.isAvailable() || 预览画面尺寸 == null) { return; }
        try {
            关闭预览会话();
            SurfaceTexture 表面纹理 = 媒体呈现区.getSurfaceTexture();
            assert 表面纹理 != null;
            表面纹理.setDefaultBufferSize(预览画面尺寸.getWidth(), 预览画面尺寸.getHeight());
            预览构建器 = 摄像头.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            Surface 预览表面 = new Surface(表面纹理);
            预览构建器.addTarget(预览表面);
            摄像头.createCaptureSession(Arrays.asList(预览表面), new CameraCaptureSession.StateCallback() {

                @Override
                public void onConfigured(CameraCaptureSession cameraCaptureSession) {
                    预览会话 = cameraCaptureSession;
                    更新预览画面();
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession cameraCaptureSession) {
                    Toast.makeText(本活动, "Failed", Toast.LENGTH_SHORT).show();
                }
            }, 跨线程调用器_录像);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void 更新预览画面() {
        if (摄像头 == null) { return; }
        try {
            预览构建器.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
            HandlerThread 线程 = new HandlerThread("CameraPreview");
            线程.start();
            预览会话.setRepeatingRequest(预览构建器.build(), null, 跨线程调用器_录像);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void 关闭预览会话() {
        if (预览会话 != null) {
            预览会话.close();
            预览会话 = null;
        }
    }

    private void 启动后台线程() {
        后台线程 = new HandlerThread("CameraBackground");
        后台线程.start();
        跨线程调用器_录像 = new Handler(后台线程.getLooper());
    }

    private void 停止后台线程() {
        if (后台线程 != null) {
            后台线程.quitSafely();
            try {
                后台线程.join();
                后台线程 = null;
                跨线程调用器_录像 = null;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void 关闭摄像头(boolean 发送) {
        Button 按钮 = (Button) findViewById(R.id.按钮_开始);
        按钮.setText(界面文字.获取(48, "重拍"));
        文字_剩余秒数.setText(界面文字.获取(315,  "#%秒", new Object[] {(最大录像时长 - 剩余秒数)}));
        关闭摄像头2();
        File 文件 = new File(getCacheDir().toString() + "/record.mp4");
        if (文件.exists()) {
            if (发送) {
                按钮 = (Button) findViewById(R.id.按钮_取消);
                按钮.setVisibility(View.VISIBLE);
                按钮 = (Button) findViewById(R.id.按钮_发送);
                按钮.setVisibility(View.VISIBLE);
            } else {
                文件.delete();
            }
        }
    }

    private void 关闭摄像头2() {
        try {
            摄像头开关锁.acquire();
            正在录像 = false;
            if (定时任务 != null) {
                定时任务.cancel();
                定时任务 = null;
            }
            关闭预览会话();
            if (摄像头 != null) {
                摄像头.close();
                摄像头 = null;
            }
            if (媒体录制器 != null) {
                媒体录制器.stop();
                媒体录制器.release();
                媒体录制器 = null;
            }
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(),Toast.LENGTH_LONG).show();
        } finally {
            摄像头开关锁.release();
        }
    }

    private void 开始录像() {
        Button 按钮 = (Button) findViewById(R.id.按钮_开始);
        按钮.setText(界面文字.获取(30, "停止"));
        TextView 文字 = (TextView) findViewById(R.id.文字_前后摄像头);
        文字.setVisibility(View.GONE);
        if (摄像头 == null || !媒体呈现区.isAvailable() || 预览画面尺寸 == null) { return; }
        try {
            关闭预览会话();
            File 文件 = new File(getCacheDir().toString() + "/record.mp4");
            if (文件.exists()) { 文件.delete(); }
            文件.createNewFile();
            媒体录制器 = new MediaRecorder();
            媒体录制器.setAudioSource(MediaRecorder.AudioSource.MIC);
            媒体录制器.setVideoSource(MediaRecorder.VideoSource.SURFACE);

            媒体录制器.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);

            媒体录制器.setVideoFrameRate(24);
            媒体录制器.setVideoSize(视频画面尺寸.getWidth(), 视频画面尺寸.getHeight());
            媒体录制器.setVideoEncodingBitRate(900 * 1024);
            媒体录制器.setVideoEncoder(MediaRecorder.VideoEncoder.H264);

            媒体录制器.setAudioEncodingBitRate(44100);
            媒体录制器.setAudioChannels(1);
            媒体录制器.setAudioSamplingRate(44100);
            媒体录制器.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);

            int rotation = getWindowManager().getDefaultDisplay().getRotation();
            switch (mSensorOrientation) {
                case SENSOR_ORIENTATION_DEFAULT_DEGREES:
                    媒体录制器.setOrientationHint(DEFAULT_ORIENTATIONS.get(rotation));
                    break;
                case SENSOR_ORIENTATION_INVERSE_DEGREES:
                    媒体录制器.setOrientationHint(INVERSE_ORIENTATIONS.get(rotation));
                    break;
            }
//            CamcorderProfile profile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
//            profile.fileFormat = MediaRecorder.OutputFormat.MPEG_4;
//            profile.videoCodec = MediaRecorder.VideoEncoder.H264;
//            profile.videoFrameWidth = 640;
//            profile.videoFrameHeight = 480;
//            profile.videoFrameRate = 30;
//            profile.videoBitRate = profile.videoFrameWidth * profile.videoFrameHeight * 4;
//            profile.audioCodec = MediaRecorder.AudioEncoder.AAC;
//            profile.audioSampleRate = 44100;
//            profile.audioBitRate = 44100;
//            profile.audioChannels = 1;
//            媒体录制器.setProfile(profile);

//            if (!长视频) {
//
//            } else {
//                CamcorderProfile profile;
//                if (!BuildConfig.DEBUG) {
//                    if (CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_480P)) {
//                        profile = CamcorderProfile.get(CamcorderProfile.QUALITY_480P);
//                        profile.fileFormat = MediaRecorder.OutputFormat.MPEG_4;
//                        profile.videoCodec = MediaRecorder.VideoEncoder.H264;
//                        profile.videoBitRate = 1800 * 1024;
//                        profile.videoFrameRate = 24;
//                        profile.audioCodec = MediaRecorder.AudioEncoder.AAC;
//                        profile.audioSampleRate = 44100;
//                        profile.audioBitRate = 44100;
//                        profile.audioChannels = 1;
//                    } else {
//                        ff.弹出提示框(活动, "无法录制480P视频", Toast.LENGTH_LONG);
//                        return;
//                    }
//                } else {
//                    profile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
//                    profile.audioCodec = MediaRecorder.AudioEncoder.AAC;
//                }
//                媒体录制器.setProfile(profile);
//            }

            媒体录制器.setOutputFile(文件.getAbsolutePath());
            最大录像时长 = ProtocolParameters.最大值_视频录制时长_秒;
            媒体录制器.setMaxDuration(最大录像时长 * 1000);
            媒体录制器.setOnInfoListener(new MediaRecorder.OnInfoListener() {
                @Override
                public void onInfo(MediaRecorder mr, int what, int extra) {
                    if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
                        关闭摄像头(true);
                    }
                }
            });
            媒体录制器.prepare();

            SurfaceTexture 表面纹理 = 媒体呈现区.getSurfaceTexture();
            assert 表面纹理 != null;
            表面纹理.setDefaultBufferSize(预览画面尺寸.getWidth(), 预览画面尺寸.getHeight());
            预览构建器 = 摄像头.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
            List<Surface> 表面列表 = new ArrayList<>();

            Surface 预览表面 = new Surface(表面纹理);
            表面列表.add(预览表面);
            预览构建器.addTarget(预览表面);

            表面_录制器 = 媒体录制器.getSurface();
            表面列表.add(表面_录制器);
            预览构建器.addTarget(表面_录制器);

            摄像头.createCaptureSession(表面列表, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    预览会话 = session;
                    更新预览画面();
                    本活动.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            剩余秒数 = 最大录像时长;
                            文字_剩余秒数.setText(String.valueOf(剩余秒数));
                            文字_剩余秒数.setVisibility(View.VISIBLE);
                            if (定时任务 != null) {
                                定时任务.cancel();
                            }
                            定时任务 = new MyTimerTask(跨线程调用器1, 1);
                            正在录像 = true;
                            媒体录制器.start();
                            定时器.schedule(定时任务, 1000, 1000);
                        }
                    });
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                    Toast.makeText(本活动, "Failed", Toast.LENGTH_SHORT).show();
                }
            }, 跨线程调用器_录像);
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    void 处理跨线程数据(Message msg) {
        switch (msg.what) {
            case 1:
                剩余秒数 -= 1;
                文字_剩余秒数.setText(String.valueOf(剩余秒数));
                if (剩余秒数 <= 0) {
                    if (定时任务 != null) {
                        定时任务.cancel();
                        定时任务 = null;
                    }
                }
                break;
        }
    }

//    private void 停止录像() {
//        正在录像 = false;
//        媒体录制器.stop();
//        媒体录制器.reset();
//        预览开始();
//    }
//
//    private static class CompareSizesByArea implements Comparator<Size> {
//
//        @Override
//        public int compare(Size lhs, Size rhs) {
//            // We cast here to ensure the multiplications won't overflow
//            return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
//                    (long) rhs.getWidth() * rhs.getHeight());
//        }
//    }
//
//    public static class ErrorDialog extends DialogFragment {
//
//        private static final String ARG_MESSAGE = "message";
//
//        public static ErrorDialog newInstance(String message) {
//            ErrorDialog dialog = new ErrorDialog();
//            Bundle args = new Bundle();
//            args.putString(ARG_MESSAGE, message);
//            dialog.setArguments(args);
//            return dialog;
//        }
//
//        @Override
//        public Dialog onCreateDialog(Bundle savedInstanceState) {
//            final Activity activity = getActivity();
//            return new AlertDialog.Builder(activity)
//                    .setMessage(getArguments().getString(ARG_MESSAGE))
//                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialogInterface, int i) {
//                            activity.finish();
//                        }
//                    })
//                    .create();
//        }
//
//    }
//
//    public static class ConfirmationDialog extends DialogFragment {
//
//        @Override
//        public Dialog onCreateDialog(Bundle savedInstanceState) {
//            return new AlertDialog.Builder(getActivity())
//                    .setMessage("获取摄像头和麦克风的权限失败")
//                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            ActivityCompat.requestPermissions(getActivity(), VIDEO_PERMISSIONS,
//                                    REQUEST_VIDEO_PERMISSIONS);
//                        }
//                    })
//                    .setNegativeButton(android.R.string.cancel,
//                            new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialog, int which) {
//                                    getActivity().finish();
//                                }
//                            })
//                    .create();
//        }
//
//    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (定时任务 != null) {
            定时任务.cancel();
            定时任务 = null;
        }
        File 文件 = new File(getCacheDir().toString() + "/record.mp4");
        if (文件.exists()) {
            文件.delete();
        }
    }

}
