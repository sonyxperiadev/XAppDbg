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
import java.awt.Dimension;
import java.io.IOException;

import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Generic base class for UI components to edit a property
 */
@SuppressWarnings("serial")
public abstract class PropertyView extends JPanel {

    /** The communication channel id */
    private int mChannel;
    /** The property to show and change */
    protected Item mItem;
    /** The connection to the server */
    protected XAppDbgConnection mConnection;
    /** The UI label of this component */
    protected JLabel mLabel;

    public PropertyView(Item item, int channel, XAppDbgConnection connection) {
        mItem = item;
        mChannel = channel;
        mConnection = connection;

        setLayout(new BorderLayout());

        mLabel = new JLabel(item.getName() + " : ");
        mLabel.setSize(new Dimension(100, 25));
        add(mLabel, BorderLayout.WEST);

        setMaximumSize(new Dimension(1000, 25));

        setToolTipText(item.getDescription());
    }

    public Item getItem() {
        return mItem;
    }

    public static PropertyView create(Item item, Packet in, XAppDbgConnection connection) throws IOException {
        switch (item.getType()) {
            case Item.TYPE_NONE:
                return new VoidPropertyView(item, in, connection);
            case Item.TYPE_BOOLEAN:
                return new BoolPropertyView(item, in, connection);
            case Item.TYPE_INT:
                return new IntPropertyView(item, in, connection);
            case Item.TYPE_FLOAT:
                return new FloatPropertyView(item, in, connection);
            case Item.TYPE_STRING:
                return new StringPropertyView(item, in, connection);
            default:
                System.out.println("Cannot create viewer for property of type " + item.getType());
        }
        return null;
    }

    protected Packet createPacket() {
        return mConnection.createPacket(mChannel);
    }

    public abstract void readValue() throws IOException;

}
