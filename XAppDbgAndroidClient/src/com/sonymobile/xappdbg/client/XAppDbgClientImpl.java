package com.sonymobile.xappdbg.client;

import com.sonymobile.tools.xappdbg.Packet;
import com.sonymobile.tools.xappdbg.XAppDbgWidget;
import com.sonymobile.tools.xappdbg.properties.Item;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Vector;

public class XAppDbgClientImpl extends Activity {

    private static final String TAG = "XappDbgClient";

    /** The currently used protocol version */
    private static final int PROTOCOL_VERSION = 0x00000001;

    public static final String EXTRA_HOST = "host";
    public static final String EXTRA_PORT = "port";

    enum MM {
        CONNECTED,
        CONNECT_FAILED,
        DISCONNECTED,
        CREATE_UI,
    };

    enum IOM {
        CONNECT,
        DISCONENCT,
    };

    private String mHost;
    private int mPort;

    private Handler mMainThread;
    private HandlerThread mIOThreadHandler;
    private Handler mIOThread;

    private AlertDialog mDialog;

    private Socket mSock;
    private DataInputStream mIs;
    private DataOutputStream mOs;

    private FrameLayout mRoot;

    /** Called when the activity mIs first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mRoot = new FrameLayout(this);
        setContentView(mRoot);

        Intent intent = getIntent();
        mHost = intent.getStringExtra(EXTRA_HOST);
        mPort = Integer.parseInt(intent.getStringExtra(EXTRA_PORT));

        mMainThread = new Handler(new MainThreadReceiver());
        mIOThreadHandler = new HandlerThread("IO");
        mIOThreadHandler.start();
        mIOThread = new Handler(mIOThreadHandler.getLooper(), new IOThreadReceiver());

    }

    @Override
    protected void onStart() {
        super.onStart();

        showBusyDialog();
        sendIO(IOM.CONNECT, mHost, mPort, 0);
    }

    @Override
    protected void onStop() {
        sendIO(IOM.DISCONENCT);
        mIOThreadHandler.quit();
        super.onStop();
    }

    private void sendIO(IOM msg) {
        mIOThread.sendEmptyMessage(msg.ordinal());
    }

    private void sendIO(IOM msg, Object obj, int arg1, int arg2) {
        mIOThread.sendMessage(mIOThread.obtainMessage(msg.ordinal(), arg1, arg2, obj));
    }

    public void postIO(Runnable r) {
        mIOThread.post(r);
    }

    private void sendMain(MM msg) {
        mMainThread.sendEmptyMessage(msg.ordinal());
    }

    private void sendMain(MM msg, Object obj, int arg1, int arg2) {
        mMainThread.sendMessage(mMainThread.obtainMessage(msg.ordinal(), arg1, arg2, obj));
    }

    public void postMain(Runnable r) {
        mMainThread.post(r);
    }

    @MainThread
    private void showBusyDialog() {
        AlertDialog.Builder b = new AlertDialog.Builder(XAppDbgClientImpl.this);
        b.setTitle("Connecting...");
        b.setMessage("Please wait...");
        b.setOnCancelListener(new OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                sendIO(IOM.DISCONENCT);
                dialog.dismiss();
                finish();
            }
        });
        mDialog = b.create();
        mDialog.show();
    }

    @MainThread
    private void closeBusyDialog() {
        mDialog.dismiss();
    }

    @IOThread
    public void doConnect(String host, int port) {
        if (doConnectImpl(host, port)) {
            sendMain(MM.CONNECTED);
        } else {
            sendMain(MM.CONNECT_FAILED);
        }
    }

    @IOThread
    private boolean doConnectImpl(String host, int port) {
        try {
            Log.v(TAG, "Connecting to server...");
            mSock = new Socket(mHost, mPort);
            mIs = new DataInputStream(mSock.getInputStream());
            mOs = new DataOutputStream(mSock.getOutputStream());
            Log.v(TAG, "Connected, executing handshake...");

            // check versions
            mOs.writeInt(PROTOCOL_VERSION);
            mOs.flush();
            int serverVersion = mIs.readInt();
            if (serverVersion != PROTOCOL_VERSION) {
                System.err.println(TAG + "Connection refused due to version mismatch, server: " + Integer.toHexString(serverVersion) + " client: " + Integer.toHexString(PROTOCOL_VERSION));
                close();
                return false;
            }


            Log.v(TAG, "Reading widget list...");
            int cnt = mIs.readInt();
            Vector<XAppDbgWidget> widgets = new Vector<XAppDbgWidget>();
            for (int i = 0; i < cnt; i++) {
                XAppDbgWidget w = XAppDbgWidget.readFromServer(mIs);
                widgets.add(w);
            }

            Log.v(TAG, "Reading command list...");
            cnt = mIs.readInt();
            for (int i = 0; i < cnt; i++) {
                /* String cmdName = */ mIs.readUTF();
            }

            sendMain(MM.CREATE_UI, widgets, 0, 0);

            Log.v(TAG, "Server IO Loop started");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @MainThread
    public void createUi(Vector<XAppDbgWidget> widgets) {
        System.out.println("# Building UI");
        for (XAppDbgWidget w : widgets) {
            createWidget(w);
        }
    }

    @MainThread
    private void createWidget(XAppDbgWidget w) {
        // TODO
        XAppDbgWidgetView widgetView = XAppDbgWidgetView.create(w, this);
        if (widgetView != null) {
            // FIXME: non-inline widgets are not supported yet
            widgetView.createUI(this, mRoot);
        }
    }

    @IOThread
    public void close() {
        try {
            mIs.close();
        } catch (IOException e) { }
        try {
            mOs.close();
        } catch (IOException e) { }
        try {
            mSock.close();
        } catch (IOException e) { }
    }

    @MainThread
    public void onConnected() {
        closeBusyDialog();
        // TODO Auto-generated method stub

    }

    @MainThread
    public void onConnectionFailed() {
        closeBusyDialog();
        Toast.makeText(getApplication(), "Connection failed!", Toast.LENGTH_SHORT).show();
        finish();
    }

    class MainThreadReceiver implements Handler.Callback {

        @SuppressWarnings("unchecked")
        @Override
        public boolean handleMessage(Message msg) {
            MM m = MM.values()[msg.what];
            Log.v(TAG, "MainThread: " + m);
            switch (m) {
                case CONNECTED:
                    onConnected();
                    return true;
                case CONNECT_FAILED:
                    onConnectionFailed();
                    return true;
                case DISCONNECTED:
                    // NOP
                    return true;
                case CREATE_UI:
                    createUi((Vector<XAppDbgWidget>) msg.obj);
                    return true;
            }
            return false;
        }

    }

    class IOThreadReceiver implements Handler.Callback {

        @Override
        public boolean handleMessage(Message msg) {
            IOM m = IOM.values()[msg.what];
            Log.v(TAG, "IOThread: " + m);
            switch (m) {
                case CONNECT:
                    doConnect((String) msg.obj, msg.arg1);
                    return true;
                case DISCONENCT:
                    close();
                    sendMain(MM.DISCONNECTED);
                    break;
            }
            return false;
        }

    }

    public Packet createPacket(int channel) {
        return new Packet(channel, mIs, mOs);
    }

    public void sendFloatValueToServer(final Item item, final float value, final int channel) {
        mIOThread.post(new Runnable() {
            @Override
            public void run() {
                try {
                    item.sendFloatValueToServer(value, createPacket(channel));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void readFloatValue(final Item item, final int channel, final OnValueRead cb) {
        mIOThread.post(new Runnable() {
            @Override
            public void run() {
                try {
                    final float val = item.readFloatValue(createPacket(channel));
                    mMainThread.post(new Runnable() {
                        @Override
                        public void run() {
                            cb.onValueRead(val);
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void sendIntValueToServer(final Item item, final int value, final int channel) {
        mIOThread.post(new Runnable() {
            @Override
            public void run() {
                try {
                    item.sendIntValueToServer(value, createPacket(channel));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void readIntValue(final Item item, final int channel, final OnValueRead cb) {
        mIOThread.post(new Runnable() {
            @Override
            public void run() {
                try {
                    final int val = item.readIntValue(createPacket(channel));
                    mMainThread.post(new Runnable() {
                        @Override
                        public void run() {
                            cb.onValueRead(val);
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

}
