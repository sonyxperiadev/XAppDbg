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
package com.sonymobile.xappdbg.client.properties;

import com.sonymobile.tools.xappdbg.Packet;
import com.sonymobile.tools.xappdbg.properties.Item;
import com.sonymobile.xappdbg.client.R;
import com.sonymobile.xappdbg.client.XAppDbgClientImpl;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import java.io.IOException;

/**
 * Generic base class for UI components to edit a property
 */
public abstract class PropertyView {

    /** The communication channel id */
    protected int mChannel;
    /** The property to show and change */
    protected Item mItem;
    /** Context */
    protected XAppDbgClientImpl mCtx;
    /** The UI label of this component */
    protected TextView mLabel;
    /** The UI */
    protected View mView;

    public PropertyView(Item item, int channel, XAppDbgClientImpl ctx, int layoutRes) {
        mItem = item;
        mChannel = channel;
        mCtx = ctx;

        mView = LayoutInflater.from(ctx).inflate(layoutRes, null);

        mLabel = (TextView) mView.findViewById(R.id.label);
        if (mLabel != null) {
            mLabel.setText(item.getName() + " : ");
        }
    }

    public Item getItem() {
        return mItem;
    }

    public View getView() {
        return mView;
    }

    public static PropertyView create(Item item, Packet in, XAppDbgClientImpl ctx) {
        switch (item.getType()) {
//            case Item.TYPE_NONE:
//                return new VoidPropertyView(item, in, connection);
//            case Item.TYPE_BOOLEAN:
//                return new BoolPropertyView(item, in, connection);
            case Item.TYPE_INT:
                return new IntPropertyView(item, in, ctx);
            case Item.TYPE_FLOAT:
                return new FloatPropertyView(item, in, ctx);
//            case Item.TYPE_STRING:
//                return new StringPropertyView(item, in, connection);
            default:
                System.out.println("Cannot create viewer for property of type " + item.getType());
        }
        return null;
    }

    public abstract void readValue() throws IOException;

}
