package com.sonymobile.xappdbg.client;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

public class XAppDbgClient extends Activity implements OnClickListener {

    private static final String KEY_HOST = "host";
    private static final String DEF_HOST = "192.168.43.1";
    private static final String KEY_PORT = "port";
    private static final String DEF_PORT = "55011";
    private EditText mEditHost;
    private EditText mEditPort;
    private SharedPreferences mPrefs;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);

        mEditHost = (EditText) findViewById(R.id.editHost);
        mEditPort = (EditText) findViewById(R.id.editPort);
        mPrefs = getSharedPreferences("prefs", MODE_PRIVATE);
        mEditHost.setText(mPrefs.getString(KEY_HOST, DEF_HOST));
        mEditPort.setText(mPrefs.getString(KEY_PORT, DEF_PORT));
        findViewById(R.id.btnConnect).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
    }

}
