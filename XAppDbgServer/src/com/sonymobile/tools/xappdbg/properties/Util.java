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

public class Util {

    /**
     * Return the first token from a method name.
     * Examples:
     *    isValid -> is
     *    getLength -> get
     *    FOO_BAR -> F
     *
     * @param name The name of a method, following the java naming convention
     * @return The first word from the method name
     */
    public static String getFirstToken(String name) {
        for (int i = 1; i < name.length(); i++) {
            char c = name.charAt(i);
            if (c >= 'A' && c <= 'Z') {
                return name.substring(0, i);
            }
        }
        return name;
    }

    /**
     * Convert a java type to our internal type constants.
     * @param javaType The java type
     * @return Our matching internal type constant, or Item.TYPE_NONE if we cannot handle it
     */
    public static int javaTypeToType(Class<?> javaType) {
        if (javaType == Integer.TYPE) {
            return Item.TYPE_INT;
        } else if (javaType == Boolean.TYPE) {
            return Item.TYPE_BOOLEAN;
        } else if (javaType == Float.TYPE) {
            return Item.TYPE_FLOAT;
        } else if (javaType == String.class) {
            return Item.TYPE_STRING;
        } else if (javaType == CharSequence.class) {
            return Item.TYPE_STRING;
        } else {
            return Item.TYPE_NONE;
        }
    }

    /**
     * Write a string to a data stream, handling the special null value as well
     */
    public static void writeString(Packet os, String value) throws IOException {
        if (value == null) {
            os.writeInt(0);
        } else {
            os.writeInt(1);
            os.writeUTF(value);
        }
    }

    /**
     * Read a string from a data stream, handling the special null value as well
     */
    public static String readString(Packet is) throws IOException {
        String ret = null;
        int isSet = is.readInt();
        if (isSet != 0) {
            ret = is.readUTF();
        }
        return ret;
    }


}
