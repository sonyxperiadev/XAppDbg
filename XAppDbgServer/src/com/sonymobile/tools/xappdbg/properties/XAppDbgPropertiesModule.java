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

import com.sonymobile.tools.xappdbg.XAppDbgCommand;
import com.sonymobile.tools.xappdbg.XAppDbgModule;
import com.sonymobile.tools.xappdbg.XAppDbgWidget;
import com.sonymobile.tools.xappdbg.Packet;
import com.sonymobile.tools.xappdbg.XAppDbgWidgetIDs;

import java.io.IOException;
import java.lang.reflect.Field;

/**
 * The XAppDbgPropertiesModule module allows the server to expose the
 * properties of an object, so the user can see and even change them.
 */
public class XAppDbgPropertiesModule extends XAppDbgModule {

    private XAppDbgCommand mCmds[] = {
            new XAppDbgCmdListProp(this),
            new XAppDbgCmdWriteProp(this),
            new XAppDbgCmdReadProp(this),
    };

    private XAppDbgWidget[] mWidget = {
            new XAppDbgWidget(XAppDbgWidgetIDs.W_OBJ_PROP_LIST, "", true, this),
    };

    private Object mObj;
    private ClassItems mItems;


    public XAppDbgPropertiesModule(Object obj) {
        mObj = obj;
        if (mObj == null) {
            mItems = new ClassItems(null);
        } else {
            mItems = ClassItems.scanClass(mObj.getClass());
        }
    }

    public XAppDbgPropertiesModule(Class<?> cls) {
        mObj = null;;
        mItems = ClassItems.scanClass(cls);
    }

    public void addFieldItem(Class<?> cls, String name, String fieldName, String descr, Runnable cb) {
        if (fieldName == null) {
            fieldName = name;
        }
        Field field;
        try {
            field = cls.getField(fieldName);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        if (field == null) {
            System.out.println("Cannot find field " + fieldName + " in class " + cls);
            return;
        }
        int type =  Util.javaTypeToType(field.getType());
        if (type == Item.TYPE_NONE) {
            System.out.println("Cannot handle field(" + fieldName + ") type " + field.getType());
            return;
        }
        FieldItem item = new FieldItem(field, fieldName, type, false);
        item.setDescription(descr);
        item.setCallback(cb);

        // Now add the item
        mItems.addItem(item);
    }

    @Override
    public XAppDbgCommand[] getCommands() {
        return mCmds;
    }

    public boolean cmdListProp(Packet in, Packet out) throws IOException {
        int cnt = mItems.getItemCount();
        out.writeInt(cnt);
        for (int i = 0; i < cnt; i++) {
            Item item = mItems.getItem(i);
            item.sendToClient(out);
            if (item.isReadable()) {
                item.sendValueToClient(out, mObj);
            }
        }
        return true;
    }

    public boolean cmdReadProp(Packet in, Packet out) throws IOException {
        // Read and lookup property name
        String name = in.readUTF();
        Item item = find(name);
        if (item == null) {
            out.writeInt(0); // not found
            return false;
        } else {
            out.writeInt(1); // found
        }

        // Read value from property
        item.sendValueToClient(out, mObj);
        return true;
    }

    public boolean cmdWriteProp(Packet in, Packet out) throws IOException {
        // Read and lookup property name
        String name = in.readUTF();
        Item item = find(name);

        // Read value into property
        item.readValueFromClient(in, mObj);
        return true;
    }

    private Item find(String name) {
        int cnt = mItems.getItemCount();
        for (int i = 0; i < cnt; i++) {
            Item item = mItems.getItem(i);
            if (item.getName().equals(name)) {
                return item;
            }
        }
        return null;
    }

    @Override
    public XAppDbgWidget[] getWidgets() {
        return mWidget;
    }

}
