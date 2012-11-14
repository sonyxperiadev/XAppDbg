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
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;

import javax.swing.JTextField;
import javax.swing.JToggleButton;

/**
 * UI Component to edit a boolean property
 */
@SuppressWarnings("serial")
class IntPropertyView extends PropertyView implements KeyListener, ActionListener {

    /** The text field containing the value */
    private JTextField mField;
    /** The button to toggle between decimal and hexadecimal view */
    private JToggleButton btnHex;
    /** The original value (the one received from the server at start) */
    private int mOrigValue;
    /** The cached value (the latest value) */
    private int mCachedValue;

    public IntPropertyView(Item item, Packet in, XAppDbgConnection connection) throws IOException {
        super(item, in.getChannel(), connection);

        // Setup UI
        if (item.isReadable()) {
            btnHex = new JToggleButton("H");
            add(btnHex, BorderLayout.EAST);
            btnHex.addActionListener(this);
            btnHex.setSelected(false);
        }

        mField = new JTextField();
        mField.setDisabledTextColor(Color.DARK_GRAY);
        add(mField, BorderLayout.CENTER);
        mField.addKeyListener(this);
        mField.setToolTipText(item.getDescription());

        if (item.isReadable()) {
            // Read initial value
            mCachedValue = mOrigValue = in.readInt();
            showValue();
        }

        if (!item.isWritable()) {
            mField.setEnabled(false);
        }

    }

    private void showValue() {
        String s = null;
        int value = mCachedValue;
        if (btnHex.isSelected()) {
            s = "0x" + Long.toString(value & 0xffffffffL, 16);
        } else {
            s = Integer.toString(value, 10);
        }
        mField.setText(s);
        if (value == mOrigValue) {
            mField.setBackground(Color.WHITE);
        } else {
            mField.setBackground(new Color(0xff8080));
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            // parse value
            String s = mField.getText();
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
    }

    private void setValue(int value) throws IOException {
        mItem.sendIntValueToServer(value, createPacket());
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnHex) {
            showValue();
        }
    }

    @Override
    public void readValue() throws IOException {
        if (mItem.isReadable()) {
            mCachedValue = mItem.readIntValue(createPacket());
            showValue();
        }
    }

}
