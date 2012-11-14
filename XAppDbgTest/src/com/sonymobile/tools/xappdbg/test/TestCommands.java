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
package com.sonymobile.tools.xappdbg.test;

import com.sonymobile.tools.xappdbg.XAppDbgServer;
import com.sonymobile.tools.xappdbg.trigger.XAppDbgTriggerModule;

/**
 * Simple test application which will expose two buttons to trigger some action.
 */
public class TestCommands {

    public static void main(String[] args) {
        XAppDbgServer server = new XAppDbgServer();
        XAppDbgTriggerModule mod = new XAppDbgTriggerModule();
        mod.addCommand("foo", new Runnable() {
            @Override
            public void run() {
                System.out.println("!!! Foo pressed");
            }
        });
        mod.addCommand("bar", new Runnable() {
            @Override
            public void run() {
                System.out.println("!!! Bar pressed");
            }
        });
        server.addModule(mod);
        server.start();
    }

}
