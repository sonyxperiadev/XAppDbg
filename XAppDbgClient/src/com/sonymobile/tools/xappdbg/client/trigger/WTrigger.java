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
package com.sonymobile.tools.xappdbg.client.trigger;

import com.sonymobile.tools.xappdbg.Packet;
import com.sonymobile.tools.xappdbg.XAppDbgCmdIDs;
import com.sonymobile.tools.xappdbg.XAppDbgWidget;
import com.sonymobile.tools.xappdbg.client.XAppDbgConnection;
import com.sonymobile.tools.xappdbg.client.XAppDbgWidgetView;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JPanel;

/**
 * A widget view to show a list of buttons to execute various commands on the server.
 * The client receives a pipe separated list of texts, will create a button for each
 * of them, and whenever the user clicks on them, it will send the button name back
 * to the server.
 */
public class WTrigger extends XAppDbgWidgetView implements ActionListener {

    public WTrigger(XAppDbgWidget widget, XAppDbgConnection connection) throws IOException {
        super(widget, connection);
    }

    @Override
    public void createUI(JPanel panel) throws IOException {
        XAppDbgWidget w = getWidget();
        String params[] = w.getParams().split("\\|");
        for (String param : params) {
            if (param.length() == 0) continue;
            JButton btn = new JButton();
            btn.setText(param);
            panel.add(btn);
            btn.addActionListener(this);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JButton btn = (JButton)e.getSource();
        String cmd = btn.getText();

        try {
            Packet out = getConnection().createPacket(getWidget().getChannel());
            out.writeUTF(cmd);
            out.sendAndReceive(XAppDbgCmdIDs.CMD_TRIGGER); // ignore reply packet
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

}
