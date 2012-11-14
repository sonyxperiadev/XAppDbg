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
package com.sonymobile.tools.xappdbg.client.properties;

import com.sonymobile.tools.xappdbg.Packet;
import com.sonymobile.tools.xappdbg.client.XAppDbgConnection;
import com.sonymobile.tools.xappdbg.properties.Item;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

/**
 * UI Component to edit a boolean property
 */
@SuppressWarnings("serial")
class BoolPropertyView extends PropertyView implements ActionListener {

    /** The UI button to toggle the value of the boolean property */
    private JToggleButton mField;
    /** The original value (the one received from the server at start) */
    private boolean mOrigValue;
    /** The cached value (the latest value) */
    private boolean mCachedValue;
    /** The button to set the value to true, in case of a write only property */
    private JButton mBtnTurnOn;
    /** The button to set the value to false, in case of a write only property */
    private JButton mBtnTurnOff;

    public BoolPropertyView(Item item, Packet in, XAppDbgConnection connection) throws IOException {
        super(item, in.getChannel(), connection);

        // Setup the ui
        if (item.isReadable()) {
            mField = new JToggleButton("");
            add(mField, BorderLayout.CENTER);
            mField.addActionListener(this);

            // Read initial value
            mCachedValue = mOrigValue = in.readBoolean();
            showValue();
        } else {
            JPanel pane = new JPanel();
            pane.setLayout(new BoxLayout(pane, BoxLayout.X_AXIS));

            mBtnTurnOn = new JButton("TRUE");
            mBtnTurnOn.addActionListener(this);
            pane.add(mBtnTurnOn);

            mBtnTurnOff = new JButton("FALSE");
            mBtnTurnOff.addActionListener(this);
            pane.add(mBtnTurnOff);

            add(pane, BorderLayout.CENTER);
        }

        if (!item.isWritable()) {
            mField.setEnabled(false);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            if (e.getSource() == mField) {
                boolean value = mField.isSelected();
                setValue(value);
                readValue();
            } else if (e.getSource() == mBtnTurnOn) {
                setValue(true);
            } else if (e.getSource() == mBtnTurnOff) {
                setValue(false);
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    private void setValue(boolean value) throws IOException {
        mItem.sendBoolValueToServer(value, createPacket());
    }

    private void showValue() {
        boolean value = mCachedValue;
        mField.setSelected(value);
        mField.setText(value ? "TRUE" : "FALSE");
        if (value == mOrigValue) {
            mField.setForeground(Color.BLACK);
        } else {
            mField.setForeground(new Color(0xff0000));
        }
    }

    @Override
    public void readValue() throws IOException {
        if (mItem.isReadable()) {
            mCachedValue = mItem.readBoolValue(createPacket());
            showValue();
        }
    }

}
