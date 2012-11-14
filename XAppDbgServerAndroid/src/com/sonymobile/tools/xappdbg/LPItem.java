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
package com.sonymobile.tools.xappdbg;

import com.sonymobile.tools.xappdbg.Packet;
import com.sonymobile.tools.xappdbg.properties.Item;

import android.view.View;

import java.io.IOException;

public class LPItem extends Item {

    private Item mItem;

    public LPItem(Item item) {
        super(item.getName(), item.getItemType(), item.getType(), false);
        mItem = item;
        String newName = "layout_" + mItem.getName();
        mItem.setName(newName);
        setName(newName);
    }

    @Override
    public boolean getBoolValue(Object obj) {
        obj = ((View)obj).getLayoutParams();
        return mItem.getBoolValue(obj);
    }

    @Override
    public float getFloatValue(Object obj) {
        obj = ((View)obj).getLayoutParams();
        return mItem.getFloatValue(obj);
    }

    @Override
    public int getIntValue(Object obj) {
        obj = ((View)obj).getLayoutParams();
        return mItem.getIntValue(obj);
    }

    @Override
    public String getStringValue(Object obj) {
        obj = ((View)obj).getLayoutParams();
        return mItem.getStringValue(obj);
    }

    @Override
    public boolean setBoolValue(Object obj, boolean value) {
        obj = ((View)obj).getLayoutParams();
        return mItem.setBoolValue(obj, value);
    }

    @Override
    public boolean setFloatValue(Object obj, float value) {
        obj = ((View)obj).getLayoutParams();
        return mItem.setFloatValue(obj, value);
    }

    @Override
    public boolean setIntValue(Object obj, int value) {
        obj = ((View)obj).getLayoutParams();
        return mItem.setIntValue(obj, value);
    }

    @Override
    public boolean setStringValue(Object obj, String value) {
        obj = ((View)obj).getLayoutParams();
        return mItem.setStringValue(obj, value);
    }

    @Override
    public boolean setVoidValue(Object obj) {
        obj = ((View)obj).getLayoutParams();
        return mItem.setVoidValue(obj);
    }

    @Override
    public void sendToClient(Packet out) throws IOException {
        mItem.sendToClient(out);
    }

    @Override
    public boolean isReadable() {
        return mItem.isReadable();
    }

    @Override
    public boolean isWritable() {
        return mItem.isWritable();
    }

    @Override
    public String getDescription() {
        return mItem.getDescription();
    }

    @Override
    public void setDescription(String descr) {
        mItem.setDescription(descr);
    }

}
