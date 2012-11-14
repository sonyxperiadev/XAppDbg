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
import java.util.HashMap;

public class PropertyItem extends Item {

    /** The method which can be used to read the value */
    private Method mGetter;
    /** The method which can be used to write the value (or null for read only properties) */
    private Method mSetter;

    public PropertyItem(String name, int type, boolean proxy) {
        super(name, ITEM_PROPERTY, type, proxy);
    }

    public static PropertyItem scan(Method m, HashMap<String,Method> methods) {
        PropertyItem ret = null;

        int mod = m.getModifiers();
        String name = m.getName();
        Class<?> javaType = m.getReturnType();
        int type = Util.javaTypeToType(javaType);
        Class<?>[] params = m.getParameterTypes();

        if (type != TYPE_NONE) {
            if (0 != (mod & Modifier.PUBLIC)) {
                // check for getters
                String name1 = Util.getFirstToken(name);
                if (name1.equals("is") || name1.equals("get")) {
                    if (params.length == 0) {
                        // We got ourselves a getter :-)
                        String propName = name.substring(name1.length(), name.length());

                        // Create the reader part
                        ret = new PropertyItem(propName, type, false);
                        ret.addGetter(m);
                        methods.put(m.getName(), null);

                        // Now check if we have a setter as well
                        Method setter = findSetter(methods, propName, javaType);
                        if (setter != null) {
                            ret.addSetter(setter);
                            methods.put(setter.getName(), null);
                        }
                    }
                }
            }
        }

        return ret;
    }

    private static Method findSetter(HashMap<String,Method> methods, String propName, Class<?> javaType) {
        String name = "set" + propName;
        Method m = methods.get(name);
        if (m != null) {
            Class<?> type2 = m.getReturnType();
            Class<?>[] params2 = m.getParameterTypes();
            if (type2 == Void.TYPE) {
                if (params2.length == 1 && params2[0] == javaType) {
                    return m;
                }
            }
        }
        return null;
    }

    public void addGetter(Method m) {
        mGetter = m;
        setReadable();
        readDescription(m);
    }

    public void addSetter(Method m) {
        mSetter = m;
        setWritable();
        readDescription(m);
    }

    private void readDescription(Method m) {
        XAppDbgPropDescr ann = m.getAnnotation(XAppDbgPropDescr.class);
        if (ann != null) {
            setDescription(ann.value());
        }
    }

    @Override
    public boolean getBoolValue(Object obj) {
        if (getType() != TYPE_BOOLEAN || mGetter == null) throw new IllegalArgumentException();
        try {
            return (Boolean)mGetter.invoke(obj);
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public float getFloatValue(Object obj) {
        if (getType() != TYPE_FLOAT || mGetter == null) throw new IllegalArgumentException();
        try {
            return (Float)mGetter.invoke(obj);
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public int getIntValue(Object obj) {
        if (getType() != TYPE_INT || mGetter == null) throw new IllegalArgumentException();
        try {
            return (Integer)mGetter.invoke(obj);
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public String getStringValue(Object obj) {
        if (getType() != TYPE_STRING || mGetter == null) throw new IllegalArgumentException();
        try {
            return (String)mGetter.invoke(obj);
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
    public boolean setVoidValue(Object obj) {
        throw new IllegalArgumentException();
    }

    @Override
    public void sendToClient(Packet out) throws IOException {
        super.sendToClient(out);
    }

    public static PropertyItem readFromServer(Packet in, String name, int type) throws IOException {
        PropertyItem ret = new PropertyItem(name, type, true);
        return ret;
    }

}
