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
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class FieldItem extends Item {

    private Field mField;

    public FieldItem(Field field, String name, int type, boolean proxy) {
        super(name, ITEM_FIELD, type, proxy);
        mField = field;
        setReadable();
        setWritable();

        if (mField != null) {
            for (Annotation ann : mField.getAnnotations()) {
                if (ann instanceof XAppDbgPropDescr) {
                    XAppDbgPropDescr descr = mField.getAnnotation(XAppDbgPropDescr.class);
                    setDescription(descr.value());
                }
            }
        }
    }

    public static FieldItem scan(Field f, Class<?> clazz) {
        FieldItem ret = null;

        int mod = f.getModifiers();
        String name = f.getName();
        int type = TYPE_NONE;
        Class<?> javaType = f.getType();
        if (0 != (mod & Modifier.PUBLIC) && 0 == (mod & Modifier.FINAL)) {
            type = Util.javaTypeToType(javaType);
        }

        if (type != TYPE_NONE) {
            ret = new FieldItem(f, name, type, false);
        }

        return ret;
    }

    @Override
    public boolean getBoolValue(Object obj) {
        if (getType() != TYPE_BOOLEAN) throw new IllegalArgumentException();
        try {
            return mField.getBoolean(obj);
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public float getFloatValue(Object obj) {
        if (getType() != TYPE_FLOAT) throw new IllegalArgumentException();
        try {
            return mField.getFloat(obj);
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public int getIntValue(Object obj) {
        if (getType() != TYPE_INT) throw new IllegalArgumentException();
        try {
            return mField.getInt(obj);
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public String getStringValue(Object obj) {
        if (getType() != TYPE_STRING) throw new IllegalArgumentException();
        try {
            Object ret = mField.get(obj);
            return ret == null ? null : ret.toString();
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public boolean setBoolValue(Object obj, boolean value) {
        if (getType() != TYPE_BOOLEAN) throw new IllegalArgumentException();
        try {
            mField.setBoolean(obj, value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public boolean setFloatValue(Object obj, float value) {
        if (getType() != TYPE_FLOAT) throw new IllegalArgumentException();
        try {
            mField.setFloat(obj, value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public boolean setIntValue(Object obj, int value) {
        if (getType() != TYPE_INT) throw new IllegalArgumentException();
        try {
            mField.setInt(obj, value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public boolean setStringValue(Object obj, String value) {
        if (getType() != TYPE_STRING) throw new IllegalArgumentException();
        try {
            mField.set(obj, value);
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
        // No extra data needs to be send
    }

    public static FieldItem readFromServer(Packet in, String name, int type) {
        FieldItem ret = new FieldItem(null, name, type, true);
        // No extra data needs to be received
        return ret;
    }

}
