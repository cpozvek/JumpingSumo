package com.parrot.sdksample.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Context;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.text.InputFilter;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.parrot.arsdk.arcommands.ARCOMMANDS_JUMPINGSUMO_MEDIARECORDEVENT_PICTUREEVENTCHANGED_ERROR_ENUM;
import com.parrot.arsdk.arcontroller.ARCONTROLLER_DEVICE_STATE_ENUM;
import com.parrot.arsdk.arcontroller.ARControllerCodec;
import com.parrot.arsdk.arcontroller.ARFrame;
import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceService;
import com.parrot.sdksample.R;
import com.parrot.sdksample.drone.JSDrone;
import com.parrot.sdksample.view.JSVideoView;
import com.support.DataSingelton;
import com.support.InputFilterMinMax;
import com.support.JoystickView;
import com.support.SelectListener;

import java.io.File;

public class JSActivity extends AppCompatActivity {
    private static final String TAG = "JSActivity";
    private JSDrone mJSDrone;

    private ProgressDialog mConnectionProgressDialog;
    private ProgressDialog mDownloadProgressDialog;

    private JSVideoView mVideoView;

    private TextView mBatteryLabel;
    private TextView mPicCount;
    private JoystickView joystickView;


    private int mNbMaxDownload;
    private int mCurrentDownloadIndex;
    private int GpicCount;
    private int GbatteryPercentage;


    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_js);

        Spinner spinner = (Spinner) findViewById(R.id.SoundSm);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.Sounds, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        initIHM();
        mPicCount = (TextView) findViewById(R.id.PicCount);

        Intent intent = getIntent();
        ARDiscoveryDeviceService service = intent.getParcelableExtra(DeviceListActivity.EXTRA_DEVICE_SERVICE);
        mJSDrone = new JSDrone(this, service);
        mJSDrone.addListener(mJSListener);

        SelectListener selectL = new SelectListener();
        selectL.setSelectListener(mJSDrone);
        spinner.setOnItemSelectedListener(selectL);

        joystickView=(JoystickView)findViewById(R.id.joystickviewJ);
        joystickView.setJoystickChangeListener(new JoystickView.JoystickChangeListener() {
            @Override
            public void onJoystickChanged(int power, int degree) {
                Log.i(TAG, "power:".concat(String.valueOf(power).concat(" - ")).concat("degree:").concat(String.valueOf(degree)));
            }
        });

        DataSingelton dataSingelton = DataSingelton.getInstance();
        dataSingelton.setmJSDrone(mJSDrone);
        joystickView.setSelectListener();
    }

    @Override
    protected void onStart() {
        super.onStart();

        // show a loading view while the JumpingSumo drone is connecting
        if ((mJSDrone != null) && !(ARCONTROLLER_DEVICE_STATE_ENUM.ARCONTROLLER_DEVICE_STATE_RUNNING.equals(mJSDrone.getConnectionState())))
        {
            mConnectionProgressDialog = new ProgressDialog(this, R.style.AppCompatAlertDialogStyle);
            mConnectionProgressDialog.setIndeterminate(true);
            mConnectionProgressDialog.setMessage("Connecting ...");
            mConnectionProgressDialog.show();

            // if the connection to the Jumping fails, finish the activity
            if (!mJSDrone.connect()) {
                finish();
            }
            ((TextView) findViewById(R.id.VolumeSw)).setText("Volume OFF");
            mJSDrone.volumeOnOff((byte) 0);
        }
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Log.i(TAG, "landscape");
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            Log.i(TAG, "flip to portrait");
            if (mJSDrone != null)
            {
                Toast.makeText(getBaseContext(), "Disconnecting ...", Toast.LENGTH_SHORT).show();

                if (!mJSDrone.disconnect()) {
                    finish();
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (mJSDrone != null)
        {
            mConnectionProgressDialog = new ProgressDialog(this, R.style.AppCompatAlertDialogStyle);
            mConnectionProgressDialog.setIndeterminate(true);
            mConnectionProgressDialog.setMessage("Disconnecting ...");
            mConnectionProgressDialog.show();

            if (!mJSDrone.disconnect()) {
                finish();
            }
        }
    }
    int i = 0;
    private void initIHM() {
        mVideoView = (JSVideoView) findViewById(R.id.videoView);

        findViewById(R.id.takePictureJBt).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mJSDrone.takePicture();
                Toast.makeText(getBaseContext(), "Picture was taken", Toast.LENGTH_LONG).show();
                Log.i(TAG, "take Picture");
            }
        });

        findViewById(R.id.Left90Bt).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                mJSDrone.turndegree(-(float) Math.PI / 2);
                mJSDrone.setFlag((byte) 1);
                mJSDrone.setFlag((byte) 0);
                Log.i(TAG, "turn left 90");
            }
        });

        findViewById(R.id.Left180Bt).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                mJSDrone.turndegree(-(float) Math.PI);
                mJSDrone.setFlag((byte) 1);
                mJSDrone.setFlag((byte) 0);
                Log.i(TAG, "turn left 90");
            }
        });

        findViewById(R.id.Right90Bt).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                mJSDrone.turndegree((float) Math.PI / 2);
                mJSDrone.setFlag((byte) 1);
                mJSDrone.setFlag((byte) 0);
                Log.i(TAG, "turn left 90");
            }
        });

        findViewById(R.id.Right180Bt).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                mJSDrone.turndegree((float) Math.PI);
                mJSDrone.setFlag((byte) 1);
                mJSDrone.setFlag((byte) 0);
                Log.i(TAG, "turn left 90");
            }
        });

        findViewById(R.id.PanoramaBt).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                ((Button)findViewById(R.id.PanoramaBt)).setText("working");
                Toast.makeText(getBaseContext(),"Recording Pics for Panorama",Toast.LENGTH_LONG).show();
                Handler handler = new Handler();
                for (int i = 1; i < 13; i++) {
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Log.i(TAG, "take a Panorama...");
                            mJSDrone.takePicture();

                            mJSDrone.turndegree((float) Math.PI / 6);
                            mJSDrone.setFlag((byte) 1);
                            mJSDrone.setFlag((byte) 0);

                        }
                    }, 1500 * i);
                }
                Toast.makeText(getBaseContext(),"Done Pics for Panorama",Toast.LENGTH_LONG).show();
                ((Button)findViewById(R.id.PanoramaBt)).setText("Panorama");
            }
        });

        findViewById(R.id.recordBt).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                i++;
                Runnable r = new Runnable() {
                    @Override
                    public void run() {
                        i = 0;
                    }
                };
                if (i == 1) {
                    mJSDrone.record();
                    Toast.makeText(getBaseContext(), "Recording a Video", Toast.LENGTH_LONG).show();
                    ((Button) findViewById(R.id.recordBt)).setText("STOP");

                } else if (i == 2) {
                    i = 0;
                    mJSDrone.stop_recording();
                    Toast.makeText(getBaseContext(), "Video stopped", Toast.LENGTH_LONG).show();
                    ((TextView) findViewById(R.id.recordBt)).setText("Record");
                }


                Log.i(TAG, "record a Video");
            }
        });

        findViewById(R.id.jump_longJBt).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mJSDrone.jump_long();
            }
        });

        findViewById(R.id.jump_highJBt).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mJSDrone.jump_high();
            }
        });

        findViewById(R.id.VolumeSw).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String txt = (String) ((TextView) findViewById(R.id.VolumeSw)).getText();
                Boolean SoundOn = txt.compareTo(" Volume ON") == 0;
                if (SoundOn) {
                    ((TextView) findViewById(R.id.VolumeSw)).setText(" Volume OFF");
                    mJSDrone.volumeOnOff((byte) 0);
                } else {
                    ((TextView) findViewById(R.id.VolumeSw)).setText(" Volume ON");
                    mJSDrone.volumeOnOff((byte) 100);
                }
            }
        });

        findViewById(R.id.JoystickBt).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                Intent intent = new Intent(JSActivity.this, JSJoystickActivity.class);
                int[] sendData = new int[]{GbatteryPercentage, GpicCount};
                intent.putExtra("data", sendData);

                if (intent != null) {
                    startActivity(intent);
                }
            }
        });


        findViewById(R.id.turnaroundBt).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                i++;
                Runnable r = new Runnable() {
                    @Override
                    public void run() {
                        i = 0;
                    }
                };
                if (i == 1) {
                    mJSDrone.turnaround();
                }else if(i == 2){
                    i = 0;
                    mJSDrone.turnaround_2();
                }

            }
        });

        findViewById(R.id.animationBt).setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                mJSDrone.animation();
            }
        });

        findViewById(R.id.downloadBt).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mJSDrone.getLastFlightMedias();

                mDownloadProgressDialog = new ProgressDialog(JSActivity.this, R.style.AppCompatAlertDialogStyle);
                mDownloadProgressDialog.setIndeterminate(true);
                mDownloadProgressDialog.setMessage("Fetching medias");
                mDownloadProgressDialog.setCancelable(false);
                mDownloadProgressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mJSDrone.cancelGetLastFlightMedias();
                    }
                });
                mDownloadProgressDialog.show();
            }
        });

        findViewById(R.id.forwardBt).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                EditText et = (EditText) findViewById(R.id.SpeedEt);
                et.setFilters(new InputFilter[]{new InputFilterMinMax("1", "100")});
                byte speed = (byte) Integer.parseInt(et.getText().toString());
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        v.setPressed(true);
                        mJSDrone.setSpeed((byte) speed);
                        mJSDrone.setFlag((byte) 1);
                        break;

                    case MotionEvent.ACTION_UP:
                        v.setPressed(false);
                        mJSDrone.setSpeed((byte) 0);
                        mJSDrone.setFlag((byte) 0);
                        break;

                    default:

                        break;
                }

                return true;
            }
        });

        findViewById(R.id.backwardBt).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                byte speed = (byte) Integer.parseInt(((TextView) findViewById(R.id.SpeedEt)).getText().toString());
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        v.setPressed(true);
                        mJSDrone.setSpeed((byte) -speed);
                        mJSDrone.setFlag((byte) 1);
                        break;

                    case MotionEvent.ACTION_UP:
                        v.setPressed(false);
                        mJSDrone.setSpeed((byte) 0);
                        mJSDrone.setFlag((byte) 0);
                        break;

                    default:

                        break;
                }

                return true;
            }
        });

        findViewById(R.id.leftBt).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        v.setPressed(true);
                        mJSDrone.setTurn((byte) -50);
                        mJSDrone.setFlag((byte) 1);
                        break;

                    case MotionEvent.ACTION_UP:
                        v.setPressed(false);
                        mJSDrone.setTurn((byte) 0);
                        mJSDrone.setFlag((byte) 0);
                        break;

                    default:

                        break;
                }

                return true;
            }
        });

        findViewById(R.id.rightBt).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        v.setPressed(true);
                        mJSDrone.setTurn((byte) 50);
                        mJSDrone.setFlag((byte) 1);
                        break;

                    case MotionEvent.ACTION_UP:
                        v.setPressed(false);
                        mJSDrone.setTurn((byte) 0);
                        mJSDrone.setFlag((byte) 0);
                        break;

                    default:

                        break;
                }

                return true;
            }
        });

        mBatteryLabel = (TextView) findViewById(R.id.batteryLabelJ);
    }

    private final JSDrone.Listener mJSListener = new JSDrone.Listener() {
        @Override
        public void onDroneConnectionChanged(ARCONTROLLER_DEVICE_STATE_ENUM state) {
            switch (state)
            {
                case ARCONTROLLER_DEVICE_STATE_RUNNING:
                    mConnectionProgressDialog.dismiss();
                    break;

                case ARCONTROLLER_DEVICE_STATE_STOPPED:
                    // if the deviceController is stopped, go back to the previous activity
                    mConnectionProgressDialog.dismiss();
                    finish();
                    break;

                default:
                    break;
            }
        }

        @Override
        public void onBatteryChargeChanged(int batteryPercentage) {
            mBatteryLabel.setText(String.format("%d%%", batteryPercentage));
            Log.i(TAG, "battery: " + String.format("%d%%", batteryPercentage));
            GbatteryPercentage = batteryPercentage;
        }

        @Override
        public void onPictureTaken(ARCOMMANDS_JUMPINGSUMO_MEDIARECORDEVENT_PICTUREEVENTCHANGED_ERROR_ENUM error) {
        }

        public void onPictureCount(int picCount) {
            GpicCount = picCount;
            runThread();
        }

        private void runThread(){
            runOnUiThread(new Thread(new Runnable() {
                public void run() {
                        mPicCount.setText(String.format("%d", GpicCount));
                    }
            }));
        }

        @Override
        public void configureDecoder(ARControllerCodec codec) {
        }

        @Override
        public void onFrameReceived(ARFrame frame) {
            mVideoView.displayFrame(frame);
        }

        @Override
        public void onMatchingMediasFound(int nbMedias) {
            mDownloadProgressDialog.dismiss();

            mNbMaxDownload = nbMedias;
            mCurrentDownloadIndex = 1;

            if (nbMedias > 0) {
                mDownloadProgressDialog = new ProgressDialog(JSActivity.this, R.style.AppCompatAlertDialogStyle);
                mDownloadProgressDialog.setIndeterminate(false);
                mDownloadProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                mDownloadProgressDialog.setMessage("Downloading medias");
                mDownloadProgressDialog.setMax(mNbMaxDownload * 100);
                mDownloadProgressDialog.setSecondaryProgress(mCurrentDownloadIndex * 100);
                mDownloadProgressDialog.setProgress(0);
                mDownloadProgressDialog.setCancelable(false);
                mDownloadProgressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mJSDrone.cancelGetLastFlightMedias();
                    }
                });
                mDownloadProgressDialog.show();
            }
        }

        @Override
        public void onDownloadProgressed(String mediaName, int progress) {
            mDownloadProgressDialog.setProgress(((mCurrentDownloadIndex - 1) * 100) + progress);
        }

        @Override
        public void onDownloadComplete(String mediaName) {
            mCurrentDownloadIndex++;
            mDownloadProgressDialog.setSecondaryProgress(mCurrentDownloadIndex * 100);

            if (mCurrentDownloadIndex > mNbMaxDownload) {
                mDownloadProgressDialog.dismiss();
                mDownloadProgressDialog = null;
            }

            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            //TODO: Pfadangabe nur an einer Stelle! (SDCardModule.java Zeile 34)
            File f = new File(Environment.getExternalStorageDirectory() + "/JumpingSumo/" + mediaName);
            Log.i(TAG, "MediaScanIntent file: " + f.toString());
            Uri contentUri = Uri.fromFile(f);
            mediaScanIntent.setData(contentUri);
            sendBroadcast(mediaScanIntent);
        }
    };
}
