package dev.jojo.agilus;

import android.Manifest;
import android.animation.Animator;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.usb.UsbDevice;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

//import com.devpaul.bluetoothutillib.SimpleBluetooth;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.serenegiant.common.BaseActivity;
import com.serenegiant.opencv.ImageProcessor;
import com.serenegiant.usb.CameraDialog;
import com.serenegiant.usb.USBMonitor;
import com.serenegiant.usb.UVCCamera;
import com.serenegiant.usbcameracommon.UVCCameraHandlerMultiSurface;
import com.serenegiant.utils.CpuMonitor;
import com.serenegiant.utils.ViewAnimationHelper;
import com.serenegiant.widget.UVCCameraTextureView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.video.Video;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import dev.jojo.agilus.adapters.LegendsAdapter;
import dev.jojo.agilus.objects.LegendObject;

public class VideoStream extends BaseActivity
        implements CameraDialog.CameraDialogParent {

    private static final boolean DEBUG = true;    // TODO set false on release
    private static final String TAG = "VideoStream";

    /**
     * For gathering GPS coordinates
     */
    //private SimpleBluetooth simpleBluetooth;
    private Handler h;
    private StringBuilder sb; // For containing the gps coordinates

    /**
     * set true if you want to record movie using MediaSurfaceEncoder
     * (writing frame data into Surface camera from MediaCodec
     *  by almost same way as USBCameratest2)
     * set false if you want to record movie using MediaVideoEncoder
     */
    private static final boolean USE_SURFACE_ENCODER = false;

    /**
     * preview resolution(width)
     * if your camera does not support specific resolution and mode,
     * {@link UVCCamera#setPreviewSize(int, int, int)} throw exception
     */
    private static final int PREVIEW_WIDTH = 640;
    /**
     * preview resolution(height)
     * if your camera does not support specific resolution and mode,
     * {@link UVCCamera#setPreviewSize(int, int, int)} throw exception
     */
    private static final int PREVIEW_HEIGHT = 480;
    /**
     * preview mode
     * if your camera does not support specific resolution and mode,
     * {@link UVCCamera#setPreviewSize(int, int, int)} throw exception
     * 0:YUYV, other:MJPEG
     */
    private static final int PREVIEW_MODE = 1;

    protected static final int SETTINGS_HIDE_DELAY_MS = 2500;

    /**
     * for accessing USB
     */
    private USBMonitor mUSBMonitor;
    /**
     * Handler to execute camera related methods sequentially on private thread
     */
    private UVCCameraHandlerMultiSurface mCameraHandler;
    /**
     * for camera preview display
     */
    private UVCCameraTextureView mUVCCameraView;
    /**
     * for display resulted images
     */
    protected SurfaceView mResultView;
    /**
     * for open&start / stop&close camera preview
     */
    private ToggleButton mCameraButton;
    /**
     * button for start/stop recording
     */
    private ImageButton mCaptureButton;

    private View mBrightnessButton, mContrastButton;
    private View mResetButton;
    private View mToolsLayout, mValueLayout;
    private SeekBar mSettingSeekbar;

    protected ImageProcessor mImageProcessor;
    private TextView mCpuLoadTv;
    private TextView mFpsTv;
    private final CpuMonitor cpuMonitor = new CpuMonitor();

    //Haar cascade classifier
    private CascadeClassifier mClassifier;

    //Scan mode toggle
    private ToggleButton mScanModeButton;

    @BindView(R.id.fabPinLoc) FloatingActionButton fPinLoc;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (DEBUG) Log.v(TAG, "onCreate:");
        setContentView(R.layout.activity_video_stream);

        ButterKnife.bind(this);

        mCameraButton = findViewById(R.id.camera_button);
        mCameraButton.setOnCheckedChangeListener(mOnCheckedChangeListener);
        mCaptureButton = findViewById(R.id.capture_button);
        mCaptureButton.setOnClickListener(mOnClickListener);
        mCaptureButton.setVisibility(View.INVISIBLE);

        mScanModeButton = findViewById(R.id.tbSurveyMode);
        mScanModeButton.setOnCheckedChangeListener(scanMode);

        mUVCCameraView = findViewById(R.id.camera_view);
        mUVCCameraView.setOnLongClickListener(mOnLongClickListener);
        mUVCCameraView.setAspectRatio(PREVIEW_WIDTH / (float) PREVIEW_HEIGHT);

        mResultView = findViewById(R.id.result_view);

        mBrightnessButton = findViewById(R.id.brightness_button);
        mBrightnessButton.setOnClickListener(mOnClickListener);
        mContrastButton = findViewById(R.id.contrast_button);
        mContrastButton.setOnClickListener(mOnClickListener);
        mResetButton = findViewById(R.id.reset_button);
        mResetButton.setOnClickListener(mOnClickListener);
        mSettingSeekbar = findViewById(R.id.setting_seekbar);
        mSettingSeekbar.setOnSeekBarChangeListener(mOnSeekBarChangeListener);

        mToolsLayout = findViewById(R.id.tools_layout);
        mToolsLayout.setVisibility(View.INVISIBLE);
        mValueLayout = findViewById(R.id.value_layout);
        mValueLayout.setVisibility(View.INVISIBLE);

        mCpuLoadTv = findViewById(R.id.cpu_load_textview);
        mCpuLoadTv.setTypeface(Typeface.MONOSPACE);
        //
        mFpsTv = findViewById(R.id.fps_textview);
        mFpsTv.setText(null);
        mFpsTv.setTypeface(Typeface.MONOSPACE);

        mUSBMonitor = new USBMonitor(this, mOnDeviceConnectListener);
        mCameraHandler = UVCCameraHandlerMultiSurface.createHandler(this, mUVCCameraView,
                USE_SURFACE_ENCODER ? 0 : 1, PREVIEW_WIDTH, PREVIEW_HEIGHT, PREVIEW_MODE);

        initializeMiniMap(savedInstanceState);

        initListener();
    }

    private CompoundButton.OnCheckedChangeListener scanMode = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if(isChecked){
                mResultView.setVisibility(SurfaceView.VISIBLE);
            }
            else{
                mResultView.setVisibility(SurfaceView.INVISIBLE);
            }
        }
    };

    private void initListener(){

        fPinLoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pinLocDialog();
            }
        });
    }

    private void pinLocDialog(){

        AlertDialog.Builder pl = new AlertDialog.Builder(VideoStream.this);

        pl.setTitle("Location Source");

        String[] choices = {"Phone GPS","Drone GPS"};

        pl.setSingleChoiceItems(choices, 0, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                switch (which){
                    case 0:
                        pinLocPhoneGPS();

                        break;
                    case 1:
                        pinLocDroneGPS();
                        break;
                }

            }
        });

        pl.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        pl.create().show();
    }

    private void pinLocPhoneGPS(){
        showPinLegendType();
    }

    private void pinLocDroneGPS(){
        showPinLegendType();
    }

    private void showPinLegendType(){

        AlertDialog.Builder ab = new AlertDialog.Builder(VideoStream.this);
        ab.setTitle("Select Type");

        View vv = this.getLayoutInflater().inflate(R.layout.layout_dialog_legend_list,null);

        ListView dataList = vv.findViewById(R.id.lvLegendList);

        String[] legends = {"Healthy Survivor","Minor Injury","Major Injury","Casualty","Supplies Needed","Trapped Surviors"};
        int[] colors = {R.drawable.ic_person_pin_circle_green,
                R.drawable.ic_person_pin_circle_yellow,
                R.drawable.ic_person_pin_circle_red,
                R.drawable.ic_person_pin_circle_black,
                R.drawable.ic_person_pin_circle_brown,
                R.drawable.ic_person_pin_circle_gray};

        List<LegendObject> lObjs = new ArrayList<LegendObject>();

        for(int i=0;i<legends.length;i++){
            lObjs.add(new LegendObject(colors[i],legends[i]));
        }

        LegendsAdapter legendsAdapter = new LegendsAdapter(lObjs,VideoStream.this);

        dataList.setAdapter(legendsAdapter);

        dataList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });

        ab.setView(vv);

        ab.create().show();

    }


    @Override
    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d("OpenCV", "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d("OpenCV", "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    /**
     * OPENCV Stuff
     */
    private void initializeOpenCVDependencies() {

        try {
            // Copy the resource into a temp file so OpenCV can load it
            InputStream is = getResources().openRawResource(R.raw.haarcascade_fullbody);
            File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
            File mCascadeFile = new File(cascadeDir, "cascade.xml");
            FileOutputStream os = new FileOutputStream(mCascadeFile);

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            is.close();
            os.close();

            // Load the cascade classifier
            mClassifier = new CascadeClassifier(mCascadeFile.getAbsolutePath());

            Log.d("CLASSIFIER", "Loaded classifier.");

        } catch (Exception e) {
            Log.e("OpenCVActivity", "Error loading cascade", e);
        }

    }

    /**
     * Callback for loading opencv dependencies.
     */
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i("OpenCV", "OpenCV loaded successfully");
                    //imageMat=new Mat();
                    initializeOpenCVDependencies();
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        if (DEBUG) Log.v(TAG, "onStart:");
        mUSBMonitor.register();
        queueEvent(mCPUMonitorTask, 1000);
        runOnUiThread(mFpsTask, 1000);
    }

    @Override
    protected void onStop() {
        if (DEBUG) Log.v(TAG, "onStop:");
        removeEvent(mCPUMonitorTask);
        removeFromUiThread(mFpsTask);
        stopPreview();
        mCameraHandler.close();
        setCameraButton(false);
        super.onStop();
    }

    @Override
    public void onDestroy() {
        if (DEBUG) Log.v(TAG, "onDestroy:");
        if (mCameraHandler != null) {
            mCameraHandler.release();
            mCameraHandler = null;
        }
        if (mUSBMonitor != null) {
            mUSBMonitor.destroy();
            mUSBMonitor = null;
        }
        mUVCCameraView = null;
        mCameraButton = null;
        mCaptureButton = null;
        super.onDestroy();
    }

    /**
     * event handler when click camera / capture button
     */
    private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View view) {
            switch (view.getId()) {
                case R.id.capture_button:
                    if (mCameraHandler.isOpened()) {
                        if (checkPermissionWriteExternalStorage() && checkPermissionAudio()) {
                            if (!mCameraHandler.isRecording()) {
                                mCaptureButton.setColorFilter(0xffff0000);    // turn red
                                mCameraHandler.startRecording();

                            } else {
                                mCaptureButton.setColorFilter(0);    // return to default color
                                mCameraHandler.stopRecording();
                            }
                        }
                    }
                    break;
                case R.id.brightness_button:
                    showSettings(UVCCamera.PU_BRIGHTNESS);
                    break;
                case R.id.contrast_button:
                    showSettings(UVCCamera.PU_CONTRAST);
                    break;
                case R.id.reset_button:
                    resetSettings();
                    break;
            }
        }
    };

    private final CompoundButton.OnCheckedChangeListener mOnCheckedChangeListener
            = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(
                final CompoundButton compoundButton, final boolean isChecked) {

            switch (compoundButton.getId()) {
                case R.id.camera_button:
                    if (isChecked && !mCameraHandler.isOpened()) {
                        CameraDialog.showDialog(VideoStream.this);
                    } else {
                        stopPreview();
                    }
                    break;
            }
        }
    };

    /**
     * capture still image when you long click on preview image(not on buttons)
     */
    private final View.OnLongClickListener mOnLongClickListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(final View view) {
            switch (view.getId()) {
                case R.id.camera_view:
                    if (mCameraHandler.isOpened()) {
                        if (checkPermissionWriteExternalStorage()) {
                            mCameraHandler.captureStill();
                        }
                        return true;
                    }
            }
            return false;
        }
    };

    private void setCameraButton(final boolean isOn) {
        if (DEBUG) Log.v(TAG, "setCameraButton:isOn=" + isOn);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mCameraButton != null) {
                    try {
                        mCameraButton.setOnCheckedChangeListener(null);
                        mCameraButton.setChecked(isOn);
                    } finally {
                        mCameraButton.setOnCheckedChangeListener(mOnCheckedChangeListener);
                    }
                }
                if (!isOn && (mCaptureButton != null)) {
                    mCaptureButton.setVisibility(View.INVISIBLE);
                }
            }
        }, 0);
        updateItems();
    }

    private int mPreviewSurfaceId;

    private void startPreview() {
        if (DEBUG) Log.v(TAG, "startPreview:");
        mUVCCameraView.resetFps();
        mCameraHandler.startPreview();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    final SurfaceTexture st = mUVCCameraView.getSurfaceTexture();
                    if (st != null) {
                        final Surface surface = new Surface(st);
                        mPreviewSurfaceId = surface.hashCode();
                        mCameraHandler.addSurface(mPreviewSurfaceId, surface, false);
                    }
                    mCaptureButton.setVisibility(View.VISIBLE);
                    startImageProcessor(PREVIEW_WIDTH, PREVIEW_HEIGHT);
                } catch (final Exception e) {
                    Log.w(TAG, e);
                }
            }
        });
        updateItems();
    }

    private void stopPreview() {
        if (DEBUG) Log.v(TAG, "stopPreview:");
        stopImageProcessor();
        if (mPreviewSurfaceId != 0) {
            mCameraHandler.removeSurface(mPreviewSurfaceId);
            mPreviewSurfaceId = 0;
        }
        mCameraHandler.close();
        setCameraButton(false);
    }

    private final USBMonitor.OnDeviceConnectListener mOnDeviceConnectListener
            = new USBMonitor.OnDeviceConnectListener() {

        @Override
        public void onAttach(final UsbDevice device) {
            Toast.makeText(VideoStream.this,
                    "USB_DEVICE_ATTACHED", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onConnect(final UsbDevice device,
                              final USBMonitor.UsbControlBlock ctrlBlock, final boolean createNew) {

            if (DEBUG) Log.v(TAG, "onConnect:");
            mCameraHandler.open(ctrlBlock);
            startPreview();
            updateItems();
        }

        @Override
        public void onDisconnect(final UsbDevice device,
                                 final USBMonitor.UsbControlBlock ctrlBlock) {

            if (DEBUG) Log.v(TAG, "onDisconnect:");
            if (mCameraHandler != null) {
                queueEvent(new Runnable() {
                    @Override
                    public void run() {
                        stopPreview();
                    }
                }, 0);
                updateItems();
            }
        }

        @Override
        public void onDettach(final UsbDevice device) {
            Toast.makeText(VideoStream.this,
                    "USB_DEVICE_DETACHED", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCancel(final UsbDevice device) {
            setCameraButton(false);
        }
    };

    /**
     * to access from CameraDialog
     * @return
     */
    @Override
    public USBMonitor getUSBMonitor() {
        return mUSBMonitor;
    }

    @Override
    public void onDialogResult(boolean canceled) {
        if (DEBUG) Log.v(TAG, "onDialogResult:canceled=" + canceled);
        if (canceled) {
            setCameraButton(false);
        }
    }

    //================================================================================
    private boolean isActive() {
        return mCameraHandler != null && mCameraHandler.isOpened();
    }

    private boolean checkSupportFlag(final int flag) {
        return mCameraHandler != null && mCameraHandler.checkSupportFlag(flag);
    }

    private int getValue(final int flag) {
        return mCameraHandler != null ? mCameraHandler.getValue(flag) : 0;
    }

    private int setValue(final int flag, final int value) {
        return mCameraHandler != null ? mCameraHandler.setValue(flag, value) : 0;
    }

    private int resetValue(final int flag) {
        return mCameraHandler != null ? mCameraHandler.resetValue(flag) : 0;
    }

    private void updateItems() {
        runOnUiThread(mUpdateItemsOnUITask, 100);
    }

    private final Runnable mUpdateItemsOnUITask = new Runnable() {
        @Override
        public void run() {
            if (isFinishing()) return;
            final int visible_active = isActive() ? View.VISIBLE : View.INVISIBLE;
            //mToolsLayout.setVisibility(visible_active);
            ///mBrightnessButton.setVisibility(
            //  checkSupportFlag(UVCCamera.PU_BRIGHTNESS)
            //        ? visible_active : View.INVISIBLE);
            //mContrastButton.setVisibility(
            //      checkSupportFlag(UVCCamera.PU_CONTRAST)
            //            ? visible_active : View.INVISIBLE);
        }
    };

    private int mSettingMode = -1;

    /**
     * show setting view
     * @param mode
     */
    private final void showSettings(final int mode) {
        if (DEBUG) Log.v(TAG, String.format("showSettings:%08x", mode));
        hideSetting(false);
        if (isActive()) {
            switch (mode) {
                case UVCCamera.PU_BRIGHTNESS:
                case UVCCamera.PU_CONTRAST:
                    mSettingMode = mode;
                    mSettingSeekbar.setProgress(getValue(mode));
                    ViewAnimationHelper.fadeIn(mValueLayout, -1, 0, mViewAnimationListener);
                    break;
            }
        }
    }

    private void resetSettings() {
        if (isActive()) {
            switch (mSettingMode) {
                case UVCCamera.PU_BRIGHTNESS:
                case UVCCamera.PU_CONTRAST:
                    mSettingSeekbar.setProgress(resetValue(mSettingMode));
                    break;
            }
        }
        mSettingMode = -1;
        ViewAnimationHelper.fadeOut(mValueLayout, -1, 0, mViewAnimationListener);
    }

    /**
     * hide setting view
     * @param fadeOut
     */
    protected final void hideSetting(final boolean fadeOut) {
        removeFromUiThread(mSettingHideTask);
        if (fadeOut) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ViewAnimationHelper.fadeOut(mValueLayout, -1, 0, mViewAnimationListener);
                }
            }, 0);
        } else {
            try {
                mValueLayout.setVisibility(View.GONE);
            } catch (final Exception e) {
                // ignore
            }
            mSettingMode = -1;
        }
    }

    protected final Runnable mSettingHideTask = new Runnable() {
        @Override
        public void run() {
            hideSetting(true);
        }
    };

    /**
     * callback listener to change camera control values
     */
    private final SeekBar.OnSeekBarChangeListener mOnSeekBarChangeListener
            = new SeekBar.OnSeekBarChangeListener() {

        @Override
        public void onProgressChanged(final SeekBar seekBar,
                                      final int progress, final boolean fromUser) {

            if (fromUser) {
                runOnUiThread(mSettingHideTask, SETTINGS_HIDE_DELAY_MS);
            }
        }

        @Override
        public void onStartTrackingTouch(final SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(final SeekBar seekBar) {
            runOnUiThread(mSettingHideTask, SETTINGS_HIDE_DELAY_MS);
            if (isActive() && checkSupportFlag(mSettingMode)) {
                switch (mSettingMode) {
                    case UVCCamera.PU_BRIGHTNESS:
                    case UVCCamera.PU_CONTRAST:
                        setValue(mSettingMode, seekBar.getProgress());
                        break;
                }
            }    // if (active)
        }
    };

    private final ViewAnimationHelper.ViewAnimationListener
            mViewAnimationListener = new ViewAnimationHelper.ViewAnimationListener() {
        @Override
        public void onAnimationStart(@NonNull final Animator animator,
                                     @NonNull final View target, final int animationType) {

//			if (DEBUG) Log.v(TAG, "onAnimationStart:");
        }

        @Override
        public void onAnimationEnd(@NonNull final Animator animator,
                                   @NonNull final View target, final int animationType) {

            final int id = target.getId();
            switch (animationType) {
                case ViewAnimationHelper.ANIMATION_FADE_IN:
                case ViewAnimationHelper.ANIMATION_FADE_OUT: {
                    final boolean fadeIn = animationType == ViewAnimationHelper.ANIMATION_FADE_IN;
                    if (id == R.id.value_layout) {
                        if (fadeIn) {
                            runOnUiThread(mSettingHideTask, SETTINGS_HIDE_DELAY_MS);
                        } else {
                            mValueLayout.setVisibility(View.GONE);
                            mSettingMode = -1;
                        }
                    } else if (!fadeIn) {
//					target.setVisibility(View.GONE);
                    }
                    break;
                }
            }
        }

        @Override
        public void onAnimationCancel(@NonNull final Animator animator,
                                      @NonNull final View target, final int animationType) {

//			if (DEBUG) Log.v(TAG, "onAnimationStart:");
        }
    };

    //================================================================================
    private final Runnable mCPUMonitorTask = new Runnable() {
        @Override
        public void run() {
            if (cpuMonitor.sampleCpuUtilization()) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mCpuLoadTv.setText(String.format(Locale.US, "CPU:%3d/%3d/%3d",
                                cpuMonitor.getCpuCurrent(),
                                cpuMonitor.getCpuAvg3(),
                                cpuMonitor.getCpuAvgAll()));
                    }
                });
            }
            queueEvent(this, 1000);
        }
    };

    private final Runnable mFpsTask = new Runnable() {
        @Override
        public void run() {
            float srcFps, resultFps;
            if (mUVCCameraView != null) {
                mUVCCameraView.updateFps();
                srcFps = mUVCCameraView.getFps();
            } else {
                srcFps = 0.0f;
            }
            if (mImageProcessor != null) {
                mImageProcessor.updateFps();
                resultFps = mImageProcessor.getFps();
            } else {
                resultFps = 0.0f;
            }
            mFpsTv.setText(String.format(Locale.US, "FPS:%4.1f->%4.1f", srcFps, resultFps));
            runOnUiThread(this, 1000);
        }
    };

    //================================================================================
    private volatile boolean mIsRunning;
    private int mImageProcessorSurfaceId;

    /**
     * start image processing
     * @param processing_width
     * @param processing_height
     */
    protected void startImageProcessor(final int processing_width, final int processing_height) {
        if (DEBUG) Log.v(TAG, "startImageProcessor:");
        mIsRunning = true;
        if (mImageProcessor == null) {
            mImageProcessor = new ImageProcessor(PREVIEW_WIDTH, PREVIEW_HEIGHT,    // src size
                    new MyImageProcessorCallback(processing_width, processing_height));    // processing size
            mImageProcessor.start(processing_width, processing_height);    // processing size
            final Surface surface = mImageProcessor.getSurface();
            mImageProcessorSurfaceId = surface != null ? surface.hashCode() : 0;
            if (mImageProcessorSurfaceId != 0) {
                mCameraHandler.addSurface(mImageProcessorSurfaceId, surface, false);
            }
        }
    }

    /**
     * stop image processing
     */
    protected void stopImageProcessor() {
        if (DEBUG) Log.v(TAG, "stopImageProcessor:");
        if (mImageProcessorSurfaceId != 0) {
            mCameraHandler.removeSurface(mImageProcessorSurfaceId);
            mImageProcessorSurfaceId = 0;
        }
        if (mImageProcessor != null) {
            mImageProcessor.release();
            mImageProcessor = null;
        }
    }

    /**
     * callback listener from `ImageProcessor`
     */
    protected class MyImageProcessorCallback implements ImageProcessor.ImageProcessorCallback {
        private final int width, height;
        private final Matrix matrix = new Matrix();

        private Bitmap mFrame;

        protected MyImageProcessorCallback(
                final int processing_width, final int processing_height) {

            width = processing_width;
            height = processing_height;
        }

        @Override
        public void onFrame(final ByteBuffer frame) {
            if (mResultView != null) {
                final SurfaceHolder holder = mResultView.getHolder();
                if ((holder == null)
                        || (holder.getSurface() == null)
                        || (frame == null)) return;

//--------------------------------------------------------------------------------
// Using SurfaceView and Bitmap to draw resulted images is inefficient way,
// but functions onOpenCV are relatively heavy and expect slower than source
// frame rate. So currently just use the way to simply this sample app.
// If you want to use much efficient way, try to use as same way as
// UVCCamera class use to receive images from UVC camera.
//--------------------------------------------------------------------------------
                if (mFrame == null) {
                    mFrame = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                    final float scaleX = mResultView.getWidth() / (float) width;
                    final float scaleY = mResultView.getHeight() / (float) height;
                    matrix.reset();
                    matrix.postScale(scaleX, scaleY);
                }
                try {
                    frame.clear();
                    mFrame.copyPixelsFromBuffer(frame);
                    Mat mat = new Mat();
                    Bitmap bmp32 = mFrame.copy(Bitmap.Config.ARGB_8888, true);
                    Utils.bitmapToMat(bmp32, mat);

                    if (mat.empty()) {
                        Log.d("BITMAP_PROC", "Mat is empty pa din!!!!");
                    }

//					Imgproc.putText(mat, "TEST",
//							new Point( 40, 40),
//							Core.FONT_HERSHEY_SIMPLEX, 3.0, new Scalar(255));
//					Imgproc.rectangle(mat,new Point(30,30),new Point(40,40),new Scalar(255),4);

                    final Scalar FACE_RECT_COLOR = new Scalar(0, 255, 0, 255);

                    MatOfRect faces = new MatOfRect();

                    mClassifier.detectMultiScale(mat, faces, 1.1, 2, 2, // TODO: objdetect.CV_HAAR_SCALE_IMAGE
                            new Size(50, 80), new Size());

                    Rect[] facesArray = faces.toArray();

                    Log.d("DETECTED", Integer.valueOf(facesArray.length).toString());
//
                    Imgproc.putText(mat, "AGILUS v1.1\nDETECTION MODE",
                            new Point(10, 40),
                            Core.FONT_HERSHEY_SIMPLEX, 1.0, FACE_RECT_COLOR);

                    //Core.putText();


                    for (int i = 0; i < facesArray.length; i++) {
                        Imgproc.rectangle(mat, facesArray[i].tl(), facesArray[i].br(),
                                FACE_RECT_COLOR, 3);

                    }

                    mFrame = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

                    Utils.matToBitmap(mat, mFrame);


                    final Canvas canvas = holder.lockCanvas();
                    if (canvas != null) {
                        try {
                            canvas.drawBitmap(mFrame, matrix, null);
                        } catch (final Exception e) {
                            Log.w(TAG, e);
                        } finally {
                            holder.unlockCanvasAndPost(canvas);
                        }
                    }
                } catch (final Exception e) {
                    Log.w(TAG, e);
                }
            }
        }

        @Override
        public void onResult(final int type, final float[] result) {
            // do something
        }

    }

    private void initializeMiniMap(Bundle savedInstanceState) {

        final MapView mapView = findViewById(R.id.minimap);

        mapView.onCreate(savedInstanceState);


        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(final GoogleMap googleMap) {
//                VideoStream.this.gMap = googleMap;

//                if (ActivityCompat.checkSelfPermission(VideoStream.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//                    // TODO: Consider calling
//                    //    ActivityCompat#requestPermissions
//                    // here to request the missing permissions, and then overriding
//                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//                    //                                          int[] grantResults)
//                    // to handle the case where the user grants the permission. See the documentation
//                    // for ActivityCompat#requestPermissions for more details.
//                    return;
//                }
                //googleMap.setMyLocationEnabled(true);

                int height = 100;
                int width = 100;
                BitmapDrawable bitmapdraw=(BitmapDrawable)getResources().getDrawable(R.drawable.agilus_graphic);
                Bitmap b=bitmapdraw.getBitmap();
                final Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);

                // Add a marker in Sydney and move the camera
                final LatLng sydney = new LatLng(-34, 151);

                //googleMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
                googleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(sydney,12.0f));

                // Add a marker in Sydney and move the camera
                final LatLng sydney2 = new LatLng(-34 + (Math.random()), 151 + (Math.random()));

                //animateMarkerToGB(new MarkerOptions()
                // .position(sydney2)
                //.title("Marker in Sydney: ")
                //.icon(BitmapDescriptorFactory.fromBitmap(smallMarker)),);

//                h.postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        animateMarker(0,sydney,sydney2,false,googleMap,smallMarker);
//                    }
//                },10000);


                mapView.onResume();
            }
        });
    }

}
