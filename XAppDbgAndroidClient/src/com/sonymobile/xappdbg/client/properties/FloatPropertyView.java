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

import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import java.io.IOException;

/**
 * UI Component to edit a floating point property
 */
class FloatPropertyView extends PropertyView implements OnEditorActionListener, OnValueRead {

    /** The text field containing the value */
    private EditText mField;
    /** The original value (the one received from the server at start) */
    private float mOrigValue;
    /** The cached value (the latest value) */
    private float mCachedValue;

    public FloatPropertyView(Item item, Packet in, XAppDbgClientImpl ctx) {
        super(item, in.getChannel(), ctx, R.layout.prop_float);

        // Setup UI
        mField = (EditText) mView.findViewById(R.id.edit);
        mField.setOnEditorActionListener(this);

        if (item.isReadable()) {
            // Read initial value
            try {
                mCachedValue = mOrigValue = in.readFloat();
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
        float value = mCachedValue;
        s = Float.toString(value);
        mField.setText(s);
        if (value == mOrigValue) {
            mField.setBackgroundColor(0xffffffff);
        } else {
            mField.setBackgroundColor(0xffff8080);
        }
    }

    private void setValue(float value) throws IOException {
        mCtx.sendFloatValueToServer(mItem, value, mChannel);
    }

    @Override
    public void readValue() throws IOException {
        if (mItem.isReadable()) {
            mCtx.readFloatValue(mItem, mChannel, this);
        }
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_NEXT || actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_GO) {
          // parse value
          String s = mField.getText().toString();
          try {
              float value = Float.parseFloat(s);
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

    @Override
    public void onValueRead(Object value) {
        mCachedValue = (Float)value;
        showValue();
    }

}
