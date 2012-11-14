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

/**
 * A simple example which exposes the fields of an object.
 */
public class TestSimple {

    public static void main(String[] args) {
        XAppDbgServer server = new XAppDbgServer();
        server.addModule(new XAppDbgPropertiesModule(new TestSimple()));
        server.start();
    }

    // Add some fields and properties to test the server

    @XAppDbgPropDescr("This is a simple int field")
    public int intField1 = 42;
    public int intField2 = 24;
    public static int staticIntField1 = 142;

    public float floatField1 = 12.3f;

    public String strField1 = "Hello world!";

    @XAppDbgPropDescr("A string field which is null by defauult")
    public String strField2 = null;

    @XAppDbgPropDescr("A CharSequence field which should behave like a string field")
    public CharSequence strField3 = new StringBuffer("Foobar");

    public boolean boolField1 = true;

    private int mIntField = 111;
    private static int mStaticIntField2 = 143;

    @XAppDbgPropDescr("This int field is access using a getter and setter")
    public int getIntField() {
        return mIntField;
    }

    public void setIntField(int v) {
        mIntField = v;
    }

    public static int getStaticIntField() {
        return mStaticIntField2;
    }

    public static void setStaticIntField(int v) {
        mStaticIntField2 = v;
    }

    public String getReadOnlyField() {
        return "You cannot change this";
    }

    public boolean getReadOnlyBooleanField() {
        return false;
    }

    public int getReadOnlyIntField() {
        return 42;
    }

    public float getReadOnlyFloatField() {
        return 4.2f;
    }

    public final int finalIntField = 666;

    public void setSomething(int value) {
        System.out.println(">>> you entered: " + value);
    }

    public void doSomething() {
        System.out.println(">>> Ok, i'm doing something!");
    }

    public void doSomethingElse(String cmd) {
        System.out.println(">>> Ok, i'm doing " + cmd);
    }

    public void doSomethingStatic(String cmd) {
        System.out.println(">>> [S] Ok, i'm doing " + cmd);
    }

    public void doBoolean(boolean value) {
        System.out.println(">>> [boolean] you entered: " + value);
    }

    public void doFloat(float value) {
        System.out.println(">>> [float] you entered: " + value);
    }
}
