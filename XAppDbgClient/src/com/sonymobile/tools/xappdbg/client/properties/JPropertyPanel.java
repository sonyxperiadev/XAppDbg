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
import com.sonymobile.tools.xappdbg.XAppDbgCmdIDs;
import com.sonymobile.tools.xappdbg.client.XAppDbgConnection;
import com.sonymobile.tools.xappdbg.client.XAppDbgWidgetView;
import com.sonymobile.tools.xappdbg.properties.Item;

import java.awt.Dimension;
import java.io.IOException;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

public class JPropertyPanel extends JTabbedPane {

    private static final long serialVersionUID = 1L;

    private XAppDbgWidgetView mWidgetView;
    private Vector<PropertyView> mItems = new Vector<PropertyView>();

    private JPanel mFieldPanel;
    private JPanel mPropertyPanel;
    private JPanel mCommandPanel;

    public JPropertyPanel(XAppDbgWidgetView widgetView) {
        mWidgetView = widgetView;

        mFieldPanel = newPanel("Fld");
        mPropertyPanel = newPanel("Prop");
        mCommandPanel = newPanel("Cmds");
    }

    private JPanel newPanel(String label) {
        JScrollPane scrollPropertyPane;

        scrollPropertyPane = new JScrollPane();
        scrollPropertyPane.setMinimumSize(new Dimension(100, 50));

        JPanel ret = new JPanel();
        ret.setLayout(new BoxLayout(ret, BoxLayout.Y_AXIS));

        addTab(label, scrollPropertyPane);
        scrollPropertyPane.getViewport().add(ret);
        return ret;
    }

    public void init() throws IOException {
        // Clear previous state
        mItems.clear();
        clearPanels();

        // Prepare the UI

        // Need to get a list of properties
        XAppDbgConnection connection = mWidgetView.getConnection();

        // Send command
        int channel = mWidgetView.getWidget().getChannel();
        Packet out = connection.createPacket(channel);
        Packet in = out.sendAndReceive(XAppDbgCmdIDs.CMD_LIST_PROP);

        // Read response
        int cnt = in.readInt();
        for (int i = 0; i < cnt; i++) {
            Item item = Item.readFromServer(in);
            if (item != null) {
                PropertyView view = PropertyView.create(item, in, connection);
                if (view != null) {
                    switch (item.getItemType()) {
                        case Item.ITEM_FIELD:
                            mFieldPanel.add(view);
                            break;
                        case Item.ITEM_PROPERTY:
                            mPropertyPanel.add(view);
                            break;
                        case Item.ITEM_COMMAND:
                            mCommandPanel.add(view);
                            break;
                    }
                    mItems.add(view);
                }
            }
        }

        // Select a tab which is not empty
        boolean empty = false;
        int sel = getSelectedIndex();
        if (sel == 0 && mFieldPanel.getComponentCount() == 0) {
            empty = true;
        } else if (sel == 1 && mPropertyPanel.getComponentCount() == 0) {
            empty = true;
        } else if (sel == 2 && mCommandPanel.getComponentCount() == 0) {
            empty = true;
        }

        if (empty) {
            if (mFieldPanel.getComponentCount() != 0) {
                setSelectedIndex(0);
            } else if (mPropertyPanel.getComponentCount() != 0) {
                setSelectedIndex(1);
            } else if (mCommandPanel.getComponentCount() != 0) {
                setSelectedIndex(2);
            }
        }

        revalidate();
        repaint();
    }

    private void clearPanels() {
        mFieldPanel.removeAll();
        mPropertyPanel.removeAll();
        mCommandPanel.removeAll();
    }

}
