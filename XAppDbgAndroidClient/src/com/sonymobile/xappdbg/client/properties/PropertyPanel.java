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

import com.sonymobile.tools.xappdbg.Packet;
import com.sonymobile.tools.xappdbg.XAppDbgCmdIDs;
import com.sonymobile.tools.xappdbg.properties.Item;
import com.sonymobile.xappdbg.client.R;
import com.sonymobile.xappdbg.client.XAppDbgClientImpl;
import com.sonymobile.xappdbg.client.XAppDbgWidgetView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TabHost;
import android.widget.TabHost.TabContentFactory;
import android.widget.TabHost.TabSpec;

import java.io.IOException;
import java.util.Vector;

public class PropertyPanel implements TabContentFactory, Runnable {

    private static final String PANEL_FIELDS = "Fld";
    private static final String PANEL_PROPS = "Prop";
    private static final String PANEL_CMDS = "Cmds";

    private XAppDbgWidgetView mWidgetView;
    private Vector<PropertyView> mItems = new Vector<PropertyView>();

    private LinearLayout mFieldPanel;
    private LinearLayout mPropertyPanel;
    private LinearLayout mCommandPanel;

    private XAppDbgClientImpl mCtx;

    private TabHost mTabHost;

    public PropertyPanel(XAppDbgWidgetView widgetView, XAppDbgClientImpl ctx, ViewGroup parent) {
        mWidgetView = widgetView;
        mCtx = ctx;

        View view = LayoutInflater.from(ctx).inflate(R.layout.prop_tabs, parent);
        mTabHost = (TabHost) view.findViewById(R.id.tabHost);
        mTabHost.setup();

        mFieldPanel = (LinearLayout) view.findViewById(R.id.contentFields);
        mPropertyPanel = (LinearLayout) view.findViewById(R.id.contentProps);
        mCommandPanel = (LinearLayout) view.findViewById(R.id.contentCmds);

        initPanel(PANEL_FIELDS, R.id.tabFields);
        initPanel(PANEL_PROPS, R.id.tabProps);
        initPanel(PANEL_CMDS, R.id.tabCmds);

        ctx.postIO(this);
    }

    private void initPanel(String label, int tabViewId) {
        TabSpec tab = mTabHost.newTabSpec(label);
        tab.setContent(tabViewId);
        tab.setIndicator(label);
        mTabHost.addTab(tab);
    }

    @Override
    public void run() {
        try {
            init();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void init() throws IOException {
        // Clear previous state
        mItems.clear();

        // Send command
        int channel = mWidgetView.getWidget().getChannel();
        Packet out = mCtx.createPacket(channel);
        final Packet in = out.sendAndReceive(XAppDbgCmdIDs.CMD_LIST_PROP);

        // Read response
        // Now we actually finished IO, so let's process the reply in the main thread
        mCtx.postMain(new Runnable() {
            @Override
            public void run() {
                try {
                    int cnt = in.readInt();
                    for (int i = 0; i < cnt; i++) {
                        final Item item = Item.readFromServer(in);
                        if (item != null) {
                            PropertyView view = PropertyView.create(item, in, mCtx);
                            if (view != null) {
                                switch (item.getItemType()) {
                                    case Item.ITEM_FIELD:
                                        mFieldPanel.addView(view.getView());
                                        break;
                                    case Item.ITEM_PROPERTY:
                                        mPropertyPanel.addView(view.getView());
                                        break;
                                    case Item.ITEM_COMMAND:
                                        mCommandPanel.addView(view.getView());
                                        break;
                                }
                                mItems.add(view);
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void clearPanels() {
        mFieldPanel.removeAllViews();
        mPropertyPanel.removeAllViews();
        mCommandPanel.removeAllViews();
    }

    @Override
    public View createTabContent(String tag) {
        if (PANEL_FIELDS.equals(tag)) return (View) mFieldPanel.getParent();
        if (PANEL_PROPS.equals(tag)) return (View) mPropertyPanel.getParent();
        if (PANEL_CMDS.equals(tag)) return (View) mCommandPanel.getParent();
        return null;
    }

}
