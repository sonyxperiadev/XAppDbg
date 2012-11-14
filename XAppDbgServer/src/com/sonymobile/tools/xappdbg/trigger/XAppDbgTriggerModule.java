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
package com.sonymobile.tools.xappdbg.trigger;

import com.sonymobile.tools.xappdbg.Packet;
import com.sonymobile.tools.xappdbg.XAppDbgCommand;
import com.sonymobile.tools.xappdbg.XAppDbgModule;
import com.sonymobile.tools.xappdbg.XAppDbgWidget;
import com.sonymobile.tools.xappdbg.XAppDbgWidgetIDs;

import java.io.IOException;
import java.util.HashMap;

/**
 * The XAppDbgTriggerModule module allows the server to show a set of buttons to the user
 * which will trigger certain commands in the server.
 */
public class XAppDbgTriggerModule extends XAppDbgModule {

    private XAppDbgCommand[] mCommands = {
            new XAppDbgCmdTrigger(this),
    };

    private String mParams = "";

    private HashMap<String, Runnable> mCBs = new HashMap<String, Runnable>();

    public XAppDbgTriggerModule() {
    }

    public void addCommand(String label, Runnable cb) {
        mCBs.put(label, cb);
        mParams = mParams + label + "|";
    }

    @Override
    public XAppDbgCommand[] getCommands() {
        return mCommands;
    }

    @Override
    public XAppDbgWidget[] getWidgets() {
        return new XAppDbgWidget[] {
                new XAppDbgWidget(XAppDbgWidgetIDs.W_TRIGGER, mParams, true, this),
        };
    }

    public boolean cmdDo(Packet in, Packet out) throws IOException {
        String cmd = in.readUTF();
        Runnable cb = mCBs.get(cmd);
        cb.run();
        return true;
    }
}
