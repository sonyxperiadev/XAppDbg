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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Vector;

/**
 * This class stores all the items connected to one single class.
 * For example it can contain all the properties of android.widget.TextView.
 * Note that it does not care about other classes, like the super class,
 * so inherited properties will be listed here as well.
 */
public class ClassItems {

    /** The set of items connected to this class */
    private Vector<Item> mItems = new Vector<Item>();

    private Comparator<? super Item> mCmp = new Comparator<Item>() {
        @Override
        public int compare(Item item1, Item item2) {
            return item1.getName().compareToIgnoreCase(item2.getName());
        }
    };

    /**
     * Constructs an empty instance of ClassItems
     * (only the class name will be set)
     * @param fullClassName The class name including the package
     */
    public ClassItems(String fullClassName) {
    }

    /**
     * Scans the given class and collects it's attributes
     * @param clazz The class to scan
     * @return An instance of ClassItems with autodetected fields, properties, etc.
     */
    public static ClassItems scanClass(Class<?> clazz) {
        long t0 = System.currentTimeMillis();

        ClassItems ret = new ClassItems(clazz.getName());
        long t1 = System.currentTimeMillis();

        // Scan the fields
        for (Field f : clazz.getFields()) {
            FieldItem item = FieldItem.scan(f, clazz);
            if (item != null) {
                ret.mItems.add(item);
            }
        }
        long t2 = System.currentTimeMillis();

        // Chache the list of methods. Store only candidate methods here
        System.out.println("all.methods.size=" + clazz.getMethods().length);
        HashMap<String, Method> methods = new HashMap<String, Method>();
        for (Method m : clazz.getMethods()) {
            if (isCandidate(m)) {
                methods.put(m.getName(), m);
            }
        }
        long t3 = System.currentTimeMillis();
        System.out.println("methods.size=" + methods.size());

        // Scan the methods for properties
        for (Method m : clazz.getMethods()) {
            PropertyItem item = PropertyItem.scan(m, methods);
            if (item != null) {
                ret.mItems.add(item);
            }
        }
        long t4 = System.currentTimeMillis();

        // Scan for commands in the remaining methods
        for (Method m : methods.values()) {
            if (m == null) continue;
            CommandItem item = CommandItem.scan(m);
            if (item != null) {
                ret.mItems.add(item);
            }
        }
        long t5 = System.currentTimeMillis();

        // Sort the items
        ret.sort();
        long t6 = System.currentTimeMillis();

        System.out.println("Class scan times t1=" + (t1 - t0) + "ms t2=" + (t2-t0) + "ms t3=" + (t3 - t0) + "ms t4=" + (t4 - t0) + "ms t5=" + (t5 - t0) + "ms t6=" + (t6 - t0) + "ms");
        return ret;
    }

    private static boolean isCandidate(Method m) {
        int mod = m.getModifiers();
        if (0 == (mod & Modifier.PUBLIC)) {
            // Not public? The for get it!
            return false;
        }

        Class<?>[] params = m.getParameterTypes();
        if (params.length > 1) {
            // Too many arguments, we cannot handle it...
            return false;
        }

        return true;
    }

    public int getItemCount() {
        return mItems.size();
    }

    public Item getItem(int idx) {
        return mItems.get(idx);
    }

    public void addItem(Item item) {
        mItems.add(item);
    }

    public void sort() {
        Collections.sort(mItems, mCmp);
    }

}
