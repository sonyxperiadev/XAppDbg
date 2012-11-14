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
import com.sonymobile.tools.xappdbg.properties.XAppDbgPropDescr;
import com.sonymobile.tools.xappdbg.properties.XAppDbgPropertiesModule;
import com.sonymobile.tools.xappdbg.trigger.XAppDbgTriggerModule;

/**
 * This application demonstrates how multiple modules can be used.
 */
public class TestMulti {

    /**
     * @param args
     */
    public static void main(String[] args) {
        XAppDbgServer server = new XAppDbgServer();
        // Add first module
        XAppDbgTriggerModule mod = new XAppDbgTriggerModule();
        mod.addCommand("foo", new Runnable() {
            @Override
            public void run() {
                System.out.println("!!! Foo[1] pressed");
            }
        });
        mod.addCommand("bar", new Runnable() {
            @Override
            public void run() {
                System.out.println("!!! Bar[1] pressed");
            }
        });
        server.addModule(mod);
        // Add second module
        mod = new XAppDbgTriggerModule();
        mod.addCommand("foo", new Runnable() {
            @Override
            public void run() {
                System.out.println("!!! Foo[2] pressed");
            }
        });
        mod.addCommand("bar", new Runnable() {
            @Override
            public void run() {
                System.out.println("!!! Bar[2] pressed");
            }
        });
        server.addModule(mod);
        // Add first properties module
        server.addModule(new XAppDbgPropertiesModule(new SubClass1()));
        // Add second properties module
        server.addModule(new XAppDbgPropertiesModule(new SubClass2()));
        // Start server
        server.start();
    }

    public static class SubClass1 {
        @XAppDbgPropDescr("Just some property from the SubClass1 class")
        public String field = "SubClass1";
    }

    public static class SubClass2 {
        @XAppDbgPropDescr("Just some property from the SubClass2 class")
        public String field = "SubClass2";
    }

}
