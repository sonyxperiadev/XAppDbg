/*
 * Copyright (C) 2012 Sony Mobile Communications AB
 *
 * This file is part of XAppDbg.
 *
 * XAppDbg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * XAppDbg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with XAppDbg.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.sonymobile.xappdbg.client.properties;

import com.sonymobile.tools.xappdbg.Packet;
import com.sonymobile.tools.xappdbg.properties.Item;
import com.sonymobile.xappdbg.client.OnValueRead;
import com.sonymobile.xappdbg.client.R;
import com.sonymobile.xappdbg.client.XAppDbgClientImpl;

import android.text.InputType;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.ToggleButton;

import java.io.IOException;

/**
 * UI Component to edit a boolean property
 */
class IntPropertyView extends PropertyView implements OnEditorActionListener, OnValueRead, OnClickListener  {

    /** The text field containing the value */
    private EditText mField;
    /** The button to toggle between decimal and hexadecimal view */
    private ToggleButton mBtnHex;
    /** The original value (the one received from the server at start) */
    private int mOrigValue;
    /** The cached value (the latest value) */
    private int mCachedValue;

    public IntPropertyView(Item item, Packet in, XAppDbgClientImpl ctx) {
        super(item, in.getChannel(), ctx, R.layout.prop_int);

        // Setup UI
        mBtnHex = (ToggleButton) mView.findViewById(R.id.btnHex);
        mBtnHex.setOnClickListener(this);
        mBtnHex.setChecked(false);
        if (!item.isReadable()) {
            mBtnHex.setVisibility(View.GONE);
        }

        mField = (EditText) mView.findViewById(R.id.edit);
        mField.setOnEditorActionListener(this);

        if (item.isReadable()) {
            // Read initial value
            try {
                mCachedValue = mOrigValue = in.readInt();
            } catch (IOException e) {
                e.printStackTrace();
            }
            showValue();
        }

        if (!item.isWritable()) {
            mField.setEnabled(false);
        }

    }

    private void showValue() {
        String s = null;
        int value = mCachedValue;
        if (mBtnHex.isChecked()) {
            s = "0x" + Long.toString(value & 0xffffffffL, 16);
            mField.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        } else {
            s = Integer.toString(value, 10);
            mField.setInputType(InputType.TYPE_NUMBER_FLAG_SIGNED);
        }
        mField.setText(s);
        if (value == mOrigValue) {
            mField.setBackgroundColor(0xffffffff);
        } else {
            mField.setBackgroundColor(0xffff8080);
        }
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_NEXT || actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_GO) {
            // parse value
            String s = mField.getText().toString();
            try {
                int value;
                if (s.startsWith("0x")) {
                    value = (int)Long.parseLong(s.substring(2), 16);
                } else {
                    value = Integer.parseInt(s);
                }

                setValue(value);
                readValue();
            } catch (NumberFormatException nfe) {
                nfe.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
        return true;
    }

    private void setValue(int value) throws IOException {
        mCtx.sendIntValueToServer(mItem, value, mChannel);
    }

    @Override
    public void readValue() throws IOException {
        if (mItem.isReadable()) {
            mCtx.readIntValue(mItem, mChannel, this);
        }
    }

    @Override
    public void onClick(View v) {
        showValue();
    }

    @Override
    public void onValueRead(Object value) {
        mCachedValue = (Integer)value;
        showValue();
    }

}
