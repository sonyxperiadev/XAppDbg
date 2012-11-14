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
import com.sonymobile.tools.xappdbg.XAppDbgCommand;
import com.sonymobile.tools.xappdbg.XAppDbgModule;
import com.sonymobile.tools.xappdbg.XAppDbgWidget;
import com.sonymobile.tools.xappdbg.XAppDbgWidgetIDs;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.HashMap;

/**
 * The XAppDbgTreeModule module allows the server to expose the
 * properties of several objects, so the user can see and even change them.
 * The objects are organized in a tree structure.
 */
public class XAppDbgTreeModule extends XAppDbgModule {

    private XAppDbgCommand[] mCommands = {
            new XAppDbgCmdTree(this),
            new XAppDbgCmdTreeSelect(this),
            new XAppDbgCmdTreeList(this),
            new XAppDbgCmdTreeRead(this),
            new XAppDbgCmdTreeWrite(this),
            new XAppDbgCmdTreeDo(this),
    };

    private String mParams = "";
    private Object mTree;
    private XAppDbgTreeParser mTreeParser;

    private Object mSelectedObj;
    private ClassItems mSelectedItems;

    private HashMap<Integer, WeakReference<Object>> mCache = new HashMap<Integer, WeakReference<Object>>();

    private HashMap<String, Runnable> mCBs = new HashMap<String, Runnable>();

    public XAppDbgTreeModule(Object tree, XAppDbgTreeParser treeParser) {
        mTree = tree;
        mTreeParser = treeParser;
    }

    public void addCommand(String label, Runnable cb) {
        mCBs.put(label, cb);
        mParams = mParams + label + "|";
    }

    @Override
    public XAppDbgCommand[] getCommands() {
        return mCommands;
    }

    @Override
    public XAppDbgWidget[] getWidgets() {
        return new XAppDbgWidget[] {
                new XAppDbgWidget(XAppDbgWidgetIDs.W_OBJ_TREE, mParams, true, this),
        };
    }

    public boolean cmdTree(Packet in, Packet out) throws IOException {
        mCache.clear();
        dumpNode(mTree, out);
        return true;
    }

    private void dumpNode(Object node, Packet out) throws IOException {
        // for each node we write:
        //    - the hash code as int
        //    - the string representation
        //    - the number of children
        int cnt = mTreeParser.getChildCount(node);
        int hashCode = node.hashCode();

        // Store in the hash map, so we can look up based on the hashcode
        mCache.put(hashCode, new WeakReference<Object>(node));

        out.writeInt(hashCode);
        out.writeUTF(mTreeParser.getText(node));
        out.writeInt(cnt);

        for (int i = 0; i < cnt; i++) {
            Object child = mTreeParser.getChild(node, i);
            dumpNode(child, out);
        }
    }

    public boolean cmdTreeList(Packet in, Packet out) throws IOException {
        if (mSelectedItems == null) {
            out.writeInt(0);
            return false;
        }
        int cnt = mSelectedItems.getItemCount();
        out.writeInt(cnt);
        for (int i = 0; i < cnt; i++) {
            Item item = mSelectedItems.getItem(i);
            item.sendToClient(out);
            if (item.isReadable()) {
                item.sendValueToClient(out, mSelectedObj);
            }
        }
        return true;
    }

    public boolean cmdTreeSelect(Packet in, Packet out) throws IOException {
        int hashCode = in.readInt();
        mSelectedItems = null;
        WeakReference<Object> ref = mCache.get(hashCode);
        if (ref != null) {
            mSelectedObj = ref.get();
            if (mSelectedObj != null) {
                mSelectedItems = getPropertiesOf(mSelectedObj);
            }
        }
        return false;
    }

    protected ClassItems getPropertiesOf(Object obj) {
        return ClassItems.scanClass(obj.getClass());
    }

    public boolean cmdTreeRead(Packet in, Packet out) throws IOException {
        // Read and lookup property name
        String name = in.readUTF();
        Item item = find(name);
        if (item == null || mSelectedObj == null) {
            out.writeInt(0); // not found
            return false;
        } else {
            out.writeInt(1); // found
        }

        // Read value from property
        item.sendValueToClient(out, mSelectedObj);
        return true;
    }

    public boolean cmdTreeWrite(Packet in, Packet out) throws IOException {
        // Read and lookup property name
        String name = in.readUTF();
        Item item = find(name);

        // Read value into property
        item.readValueFromClient(in, mSelectedObj);
        return true;
    }

    private Item find(String name) {
        int cnt = mSelectedItems.getItemCount();
        for (int i = 0; i < cnt; i++) {
            Item item = mSelectedItems.getItem(i);
            if (item.getName().equals(name)) {
                return item;
            }
        }
        return null;
    }

    public boolean cmdDo(Packet in, Packet out) throws IOException {
        String cmd = in.readUTF();
        Runnable cb = mCBs.get(cmd);
        cb.run();
        return true;
    }

    public Object getSelectedNode() {
        return mSelectedObj;
    }

}
