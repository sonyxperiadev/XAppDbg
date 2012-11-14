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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JButton;

/**
 * This is a UI component to change a "void" property, which means simply calling a method
 * without parameters.
 */
@SuppressWarnings("serial")
class VoidPropertyView extends PropertyView implements ActionListener {

    /** The button to trigger the method call */
    private JButton mButton;

    public VoidPropertyView(Item item, Packet in, XAppDbgConnection connection) {
        super(item, in.getChannel(), connection);

        // Setup the ui
        mButton = new JButton("Execute");
        add(mButton, BorderLayout.CENTER);
        mButton.addActionListener(this);

        // This is always non-readable, so don't read value
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == mButton) {
            try {
                setValue();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    private void setValue() throws IOException {
        mItem.sendVoidValueToServer(createPacket());
    }

    @Override
    public void readValue() throws IOException {
        // NOP
    }

}
