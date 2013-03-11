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
package com.sonymobile.xappdbg.client;

import com.sonymobile.tools.xappdbg.Packet;
import com.sonymobile.tools.xappdbg.XAppDbgWidget;
import com.sonymobile.tools.xappdbg.XAppDbgWidgetIDs;
import com.sonymobile.xappdbg.client.properties.WObjPropertyList;

import android.view.ViewGroup;

/**
 * Base class for UI components representing widgets (debug blocks)
 */
public abstract class XAppDbgWidgetView {

    private XAppDbgWidget mWidget;
    private XAppDbgClientImpl mCtx;

    public XAppDbgWidgetView(XAppDbgWidget widget, XAppDbgClientImpl ctx) {
        mWidget = widget;
        mCtx = ctx;
    }

    public static XAppDbgWidgetView create(XAppDbgWidget w, XAppDbgClientImpl ctx) {
        String name = w.getName();
        if (name.equals(XAppDbgWidgetIDs.W_OBJ_PROP_LIST)) {
            return new WObjPropertyList(w, ctx);
        }
        // TODO
//        if (name.equals(XAppDbgWidgetIDs.W_OBJ_TREE)) {
//            return new WObjTree(w, connection);
//        }
//        if (name.equals(XAppDbgWidgetIDs.W_TRIGGER)) {
//            return new WTrigger(w, connection);
//        }
        System.out.println("Don't know how to create widget " + name);
        return null;
    }

    public XAppDbgWidget getWidget() {
        return mWidget;
    }

    public abstract void createUI(XAppDbgClientImpl ctx, ViewGroup panel);

    public XAppDbgClientImpl getCtx() {
        return mCtx;
    }

    protected Packet createPacket() {
        return mCtx.createPacket(mWidget.getChannel());
    }

}
