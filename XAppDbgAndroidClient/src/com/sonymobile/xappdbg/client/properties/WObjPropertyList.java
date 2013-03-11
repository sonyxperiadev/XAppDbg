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

import com.sonymobile.tools.xappdbg.XAppDbgWidget;
import com.sonymobile.xappdbg.client.XAppDbgClientImpl;
import com.sonymobile.xappdbg.client.XAppDbgWidgetView;

import android.view.ViewGroup;

/**
 * The UI interface for the "object's properties" widget.
 * This widget allows the changing of the properties of one object.
 */
public class WObjPropertyList extends XAppDbgWidgetView {

    public WObjPropertyList(XAppDbgWidget widget, XAppDbgClientImpl ctx) {
        super(widget, ctx);
    }

    @Override
    public void createUI(XAppDbgClientImpl ctx, ViewGroup parent) {
        new PropertyPanel(this, ctx, parent);
    }

}
