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

/**
 * A XAppDbgModule handles a set of XAppDbgCommands which are related to each other
 * and a set of XAppDbgWidgets which represents the UI to be shown to the user.
 *
 * For example a module might contain a command to list a set of flags and another command
 * to change a certain flags, as well as a widget to show the flags as a list of properties.
 */
public abstract class XAppDbgModule {

    private int mChannel;

    public abstract XAppDbgCommand[] getCommands();

    public abstract XAppDbgWidget[] getWidgets();

    /* package */ void setChannel(int channel) {
        mChannel = channel;
    }

    public int getChannel() {
        return mChannel;
    }

    public XAppDbgCommand findCommand(String cmdId) {
        for (XAppDbgCommand cmd : getCommands()) {
            if (cmd.getID().equals(cmdId)) {
                return cmd;
            }
        }
        return null;
    }
}
