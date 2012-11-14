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
package com.sonymobile.tools.xappdbg;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * The XAppDbgWidget represents a UI component which is placed on the main window
 * to trigger actions in a certain module.
 */
public final class XAppDbgWidget {

    private String mName;
    private String mParam;
    private boolean mInline;
    private XAppDbgModule mModule;
    private int mId;
    private int mChannel;
    private static int lastId = 1;

    public XAppDbgWidget(String name, String param, boolean inline, XAppDbgModule mod) {
        mName = name;
        mParam = param;
        mInline = inline;
        mModule = mod;
        mId = lastId++;
    }

    public void writeToClient(DataOutputStream os) throws IOException {
        os.writeUTF(mName);
        os.writeUTF(mParam);
        os.writeBoolean(mInline);
        os.writeInt(mId);
        os.writeInt(getChannel());
    }

    public static XAppDbgWidget readFromServer(DataInputStream is) throws IOException {
        String name = is.readUTF();
        String param = is.readUTF();
        boolean inline = is.readBoolean();
        XAppDbgWidget ret = new XAppDbgWidget(name, param, inline, null);
        ret.mId = is.readInt();
        ret.mChannel = is.readInt();
        return ret;
    }

    public boolean isInline() {
        return mInline;
    }

    public String getName() {
        return mName;
    }

    public String getParams() {
        return mParam;
    }

    public int getId() {
        return mId;
    }

    public int getChannel() {
        return mModule == null ? mChannel : mModule.getChannel();
    }
}
