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
package com.sonymobile.tools.xappdbg.client;

import com.sonymobile.tools.xappdbg.Packet;
import com.sonymobile.tools.xappdbg.XAppDbgWidget;
import com.sonymobile.tools.xappdbg.XAppDbgWidgetIDs;
import com.sonymobile.tools.xappdbg.client.properties.WObjPropertyList;
import com.sonymobile.tools.xappdbg.client.properties.WObjTree;
import com.sonymobile.tools.xappdbg.client.trigger.WTrigger;

import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * Base class for UI components representing widgets (debug blocks)
 */
public abstract class XAppDbgWidgetView {

    private XAppDbgWidget mWidget;
    private JFrame mWin;
    private XAppDbgConnection mConnection;

    public XAppDbgWidgetView(XAppDbgWidget widget, XAppDbgConnection connection) throws IOException {
        mWidget = widget;
        mConnection = connection;
    }

    public static XAppDbgWidgetView create(XAppDbgWidget w, XAppDbgConnection connection) throws IOException {
        String name = w.getName();
        if (name.equals(XAppDbgWidgetIDs.W_OBJ_PROP_LIST)) {
            return new WObjPropertyList(w, connection);
        }
        if (name.equals(XAppDbgWidgetIDs.W_OBJ_TREE)) {
            return new WObjTree(w, connection);
        }
        if (name.equals(XAppDbgWidgetIDs.W_TRIGGER)) {
            return new WTrigger(w, connection);
        }
        System.out.println("Don't know how to create widget " + name);
        return null;
    }

    public void showWindow() throws IOException {
        if (mWin == null) {
            mWin = new JFrame();
            JPanel panel = new JPanel();
            mWin.getContentPane().add(panel);
            createUI(panel);
            mWin.pack();
            mWin.setLocationRelativeTo(null);
        }
        mWin.setVisible(true);
    }

    public XAppDbgWidget getWidget() {
        return mWidget;
    }

    public abstract void createUI(JPanel panel) throws IOException;

    public XAppDbgConnection getConnection() {
        return mConnection;
    }

    protected Packet createPacket() {
        return mConnection.createPacket(mWidget.getChannel());
    }

}
