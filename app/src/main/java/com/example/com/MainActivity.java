package com.example.com;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "FingerModule test";

    protected static final FileDescriptor NULL = null;

    private Serial FingerModule = new Serial();

    private FileDescriptor mFd = null;
    private FileInputStream mFileInputStream;
    private FileOutputStream mFileOutputStream;


    //serial sending thread
    private SendingThread mSendingThread;
    //serial receiving thread
    private ReadingThread mReadingThread;
    //sending문자열
    private byte[] mWBuffer;
    private byte[] mRBuffer;

    private EditText mReception;
    private EditText mSendText;

    private Button mBtOpen;
    private Button mBtGetVer;
    private Button mBtEnroll;
    private Button mBtVerify;
    private Button mBtClose;
    private Button mBtClear;
    private Button mBtQuit;

    byte[] abGetVer = { 0x75, 0x0a, 0x00, 0x00 , 0x0a, (byte)0x95, };
    byte[] abEnroll = { 0x75, 0x01, 0x00, 0x00 , 0x01, (byte)0x95, };
    byte[] abVerify = { 0x75, 0x02, 0x00, 0x00 , 0x02, (byte)0x95, };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mReception = (EditText)findViewById(R.id.et_recive);
        mSendText = (EditText)findViewById(R.id.et_Send);


        mBtGetVer = (Button)findViewById(R.id.bt_getver);
        mBtGetVer.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if( mFd == null) {
                    return;
                }
                if (mSendingThread == null) {
                    mSendingThread = null;
                }
                //String stringToConvert = mSendText.getText().toString();
                //mWBuffer = stringToConvert.getBytes();

                mWBuffer = abGetVer;

                mSendText.setText("=>" + byteArrayToHex(abGetVer, abGetVer.length));

                mSendingThread = new SendingThread();
                mSendingThread.start();
            }
        });

        mBtEnroll = (Button)findViewById(R.id.bt_enroll);
        mBtEnroll.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if( mFd == null) {
                    return;
                }
                if (mSendingThread == null) {
                    mSendingThread = null;
                }

                mWBuffer = abEnroll;

                mSendText.setText("=>" + byteArrayToHex(abEnroll, abEnroll.length));

                mSendingThread = new SendingThread();
                mSendingThread.start();
            }
        });

        mBtVerify = (Button)findViewById(R.id.bt_verify);
        mBtVerify.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if( mFd == null) {
                    return;
                }
                if (mSendingThread == null) {
                    mSendingThread = null;
                }

                mWBuffer = abVerify;

                mSendText.setText("=>" + byteArrayToHex(abVerify, abVerify.length));

                mSendingThread = new SendingThread();
                mSendingThread.start();
            }
        });


        mBtOpen = (Button)findViewById(R.id.bt_open);
        mBtOpen.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if( mFd == NULL ) {
                    mFd = FingerModule.Open(1, 9600, 0);
                    if( mFd != NULL ) {
                        mFileInputStream = new FileInputStream(mFd);
                        mFileOutputStream = new FileOutputStream(mFd);

                        if (mReadingThread == null) {
                            mReadingThread = null;
                        }
                        mReadingThread = new ReadingThread();
                        mReadingThread.start();
                    }

                }
            }
        });

        mBtClose = (Button)findViewById(R.id.bt_close);
        mBtClose.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

                mReadingThread.interrupt();

                if( mFd != null ) {
                    FingerModule.Close();
                    mFd = null;
                }


            }
        });

        mBtClear = (Button)findViewById(R.id.bt_clear);
        mBtClear.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                mReception.setText("");
                mSendText.setText("");
            }
        });

        mBtQuit = (Button)findViewById(R.id.bt_quit);
        mBtQuit.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                mReadingThread.interrupt();

                if( mFd != null ) {
                    FingerModule.Close();
                    mFd = null;
                }

                MainActivity.this.finish();
            }
        });
    }
    String byteArrayToHex(byte[] a, int nSize) {
        StringBuilder sb = new StringBuilder();
        for(int i=0;i<nSize;i++)
            sb.append(String.format("%02x ", a[i]&0xff));
        sb.append(String.format("\r\n"));
        return sb.toString();
    }
    private class SendingThread extends Thread {
        @Override
        public void run() {
//			while (!isInterrupted()) {
            try {
                if (mFileOutputStream != null) {
                    mFileOutputStream.write(mWBuffer);
                } else {
                    return;
                }
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
//			}
        }
    }

    private class ReadingThread extends Thread {
        @Override
        public void run() {
            super.run();
            while(!isInterrupted()) {
                int size;
                try {
                    byte[] buffer = new byte[256];
                    if (mFileInputStream == null) return;
                    size = mFileInputStream.read(buffer);
                    if (size > 0) {
                        onDataReceived(buffer, size);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
            }
        }
    }

    protected void onDataReceived(final byte[] buffer, final int size) {
        runOnUiThread(new Runnable() {
            public void run() {
                if (mReception != null) {
                    //mReception.append(new String(buffer, 0, size));
                    String sRecived = "<=" + byteArrayToHex(buffer, size);
                    mReception.append( sRecived);
                }
            }
        });
    }
}
