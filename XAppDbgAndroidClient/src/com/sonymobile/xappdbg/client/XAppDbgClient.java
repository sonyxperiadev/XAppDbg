package com.sonymobile.xappdbg.client;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Toast;

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
        String host = mEditHost.getText().toString();
        String port = mEditPort.getText().toString();
        if (TextUtils.isEmpty(host)) {
            Toast.makeText(this, "Host cannot be empty!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(port)) {
            Toast.makeText(this, "Port cannot be empty!", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(this, XAppDbgClientImpl.class);
        intent.putExtra(XAppDbgClientImpl.EXTRA_HOST, host);
        intent.putExtra(XAppDbgClientImpl.EXTRA_PORT, port);
        startActivity(intent);

        mPrefs.edit().putString(KEY_HOST, host).putString(KEY_PORT, port).apply();
    }

}
