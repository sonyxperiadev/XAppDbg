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
import com.sonymobile.tools.xappdbg.properties.Util;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.JToggleButton;

/**
 * UI Component to edit a boolean property
 */
@SuppressWarnings("serial")
class StringPropertyView extends PropertyView implements KeyListener, ActionListener {

    /** The text field containing the value */
    private JTextField mField;
    /** This button allows the user to set the value to null */
    private JButton mBtnSetNull;
    /** This button shows if the current value is null or not */
    private JToggleButton mBtnNull;
    /** The original value (the one received from the server at start) */
    private String mOrigValue;
    /** The cached value (the latest value) */
    private String mCachedValue;

    public StringPropertyView(Item item, Packet in, XAppDbgConnection connection) throws IOException {
        super(item, in.getChannel(), connection);

        // Setup UI
        if (item.isReadable()) {
            mBtnNull = new JToggleButton("");
            add(mBtnNull, BorderLayout.EAST);
            mBtnNull.addActionListener(this);
            mBtnNull.setSelected(false);
        } else {
            mBtnSetNull = new JButton("Set NULL");
            add(mBtnSetNull, BorderLayout.EAST);
            mBtnSetNull.addActionListener(this);
        }

        mField = new JTextField();
        mField.setDisabledTextColor(Color.DARK_GRAY);
        add(mField, BorderLayout.CENTER);
        mField.addKeyListener(this);
        mField.setToolTipText(item.getDescription());

        if (item.isReadable()) {
            // Read initial value
            mCachedValue = mOrigValue = Util.readString(in);
            showValue();
        }

        if (!item.isWritable()) {
            mField.setEnabled(false);
            mBtnNull.setEnabled(false);
        }
    }

    private void showValue() {
        String value = mCachedValue;
        mField.setText(value);
        if (value == null) {
            mBtnNull.setText("NULL");
            mBtnNull.setSelected(true);
        } else {
            mBtnNull.setText("----");
            mBtnNull.setSelected(false);
        }
        if (isSame(value, mOrigValue)) {
            mField.setBackground(Color.WHITE);
        } else {
            mField.setBackground(new Color(0xff8080));
        }
    }

    private boolean isSame(String s1, String s2) {
        if (s1 == null) {
            return s2 == null;
        }
        return s1.equals(s2);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            // parse value
            updateValue(mField.getText());
        }
    }

    private void updateValue(String value) {
        try {
            setValue(value);
            readValue();
        } catch (NumberFormatException nfe) {
            nfe.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    private void setValue(String value) throws IOException {
        mItem.sendStringValueToServer(value, createPacket());
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == mBtnNull) {
            boolean isNull = mBtnNull.isSelected();
            mField.setText("");
            updateValue(isNull ? null : "");
        } else if (e.getSource() == mBtnSetNull) {
            mField.setText("");
            updateValue(null);
        }
    }

    @Override
    public void readValue() throws IOException {
        if (mItem.isReadable()) {
            mCachedValue = mItem.readStringValue(createPacket());
            showValue();
        }
    }

}
