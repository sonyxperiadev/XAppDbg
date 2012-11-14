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
package com.sonymobile.tools.xappdbg.properties;

import com.sonymobile.tools.xappdbg.Packet;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class CommandItem extends Item {

    private static final String mIgnoreList = ",equals,getClass,hashCode,notify,notifyAll,wait,toString,";

    /** The method which can be used to write the value (or null for read only properties) */
    private Method mSetter;

    public CommandItem(Method m, String name, int type, boolean proxy) {
        super(name, ITEM_COMMAND, type, proxy);
        mSetter = m;
        setWritable();
        if (m != null) {
            readDescription(m);
        }
    }

    public static CommandItem scan(Method m) {
        CommandItem ret = null;

        int mod = m.getModifiers();
        String name = m.getName();
        Class<?>[] params = m.getParameterTypes();
        int type = TYPE_NONE;

        if (0 == (mod & Modifier.PUBLIC)) {
            // This is not public, so leave it alone
            return null;
        }

        if (params.length > 1) {
            // Too many parameters, we can handle only 0 or 1
            return null;
        }

        if (params.length == 1) {
            Class<?> javaType = params[0];
            type = Util.javaTypeToType(javaType);
            if (type == TYPE_NONE) {
                // We cannot handle this kind of parameter
                return null;
            }
        }

        if (mIgnoreList.indexOf("," + name + ",") >= 0) {
            // We should ignore these methods
            return null;
        }

        // Create the item
        ret = new CommandItem(m, name, type, false);

        return ret;
    }

    private void readDescription(Method m) {
        XAppDbgPropDescr ann = m.getAnnotation(XAppDbgPropDescr.class);
        if (ann != null) {
            setDescription(ann.value());
        }
    }

    @Override
    public boolean getBoolValue(Object obj) {
        throw new IllegalArgumentException();
    }

    @Override
    public float getFloatValue(Object obj) {
        throw new IllegalArgumentException();
    }

    @Override
    public int getIntValue(Object obj) {
        throw new IllegalArgumentException();
    }

    @Override
    public String getStringValue(Object obj) {
        throw new IllegalArgumentException();
    }

    @Override
    public boolean setVoidValue(Object obj) {
        if (getType() != TYPE_NONE || mSetter == null) throw new IllegalArgumentException();
        try {
            mSetter.invoke(obj);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public boolean setBoolValue(Object obj, boolean value) {
        if (getType() != TYPE_BOOLEAN || mSetter == null) throw new IllegalArgumentException();
        try {
            mSetter.invoke(obj, value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public boolean setFloatValue(Object obj, float value) {
        if (getType() != TYPE_FLOAT || mSetter == null) throw new IllegalArgumentException();
        try {
            mSetter.invoke(obj, value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public boolean setIntValue(Object obj, int value) {
        if (getType() != TYPE_INT || mSetter == null) throw new IllegalArgumentException();
        try {
            mSetter.invoke(obj, value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public boolean setStringValue(Object obj, String value) {
        if (getType() != TYPE_STRING || mSetter == null) throw new IllegalArgumentException();
        try {
            mSetter.invoke(obj, value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public void sendToClient(Packet out) throws IOException {
        super.sendToClient(out);
    }

    public static CommandItem readFromServer(Packet in, String name, int type) throws IOException {
        CommandItem ret = new CommandItem(null, name, type, true);
        return ret;
    }

}
