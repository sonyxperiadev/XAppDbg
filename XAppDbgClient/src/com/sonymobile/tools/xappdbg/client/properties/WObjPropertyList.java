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

import com.sonymobile.tools.xappdbg.XAppDbgWidget;
import com.sonymobile.tools.xappdbg.client.XAppDbgConnection;
import com.sonymobile.tools.xappdbg.client.XAppDbgWidgetView;

import java.awt.BorderLayout;
import java.io.IOException;

import javax.swing.JPanel;

/**
 * The UI interface for the "object's properties" widget.
 * This widget allows the changing of the properties of one object.
 */
public class WObjPropertyList extends XAppDbgWidgetView {

    public WObjPropertyList(XAppDbgWidget widget, XAppDbgConnection connection) throws IOException {
        super(widget, connection);
    }

    @Override
    public void createUI(JPanel panel) throws IOException {
        panel.setLayout(new BorderLayout());
        JPropertyPanel props = new JPropertyPanel(this);
        panel.add(props, BorderLayout.CENTER);

        props.init();
    }


}
