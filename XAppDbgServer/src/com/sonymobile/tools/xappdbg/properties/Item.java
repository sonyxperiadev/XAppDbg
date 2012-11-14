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
import com.sonymobile.tools.xappdbg.XAppDbgCmdIDs;

import java.io.IOException;

/**
 * Item is the base class for all sort of properties
 */
public abstract class Item {

    /** Constant for field item type */
    public static final int ITEM_FIELD      = 1;
    /** Constant for property item type */
    public static final int ITEM_PROPERTY   = 2;
    /** Constant for command item type */
    public static final int ITEM_COMMAND    = 3;

    /** Constant for missing type */
    public static final int TYPE_NONE       = 0x00;
    /** Constant for boolean values */
    public static final int TYPE_BOOLEAN    = 0x01;
    /** Constant for integer values */
    public static final int TYPE_INT        = 0x11;
    /** Constant for floating point values */
    public static final int TYPE_FLOAT      = 0x15;
    /** Constant for string values */
    public static final int TYPE_STRING     = 0x21;

    /**
     * The name of the item
     */
    private String mName;

    /**
     * The type of the item (not the type of the value).
     * The value must be one of the ITEM_* constants
     */
    private int mItemType;

    /**
     * The type of the value it stores/gets/sends/etc.
     * The value of this field must be one of the TYPE_* constants.
     */
    private int mType;

    /**
     * The description of this property
     */
    private String mDescr;

    /**
     * The callback to be executed on the server side when the value changes
     */
    private Runnable mCB;

    /**
     * This field is set to true, then this is a proxy item (used by the client),
     * otherwise it's the stub item (used by the server)
     */
    private boolean mProxy;

    /**
     * The read(0)/write(1) bits
     */
    private int mRW;

    /**
     * Constructor
     * @param name The name of the field/property/etc
     * @param itemType The type of the item (field or property or etc)
     * @param type The type of the value accessed by this item
     * @param proxy If the instance is used by the client, then "true"
     */
    public Item(String name, int itemType, int type, boolean proxy) {
        mName = name;
        mItemType = itemType;
        mType = type;
        mProxy = proxy;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public int getItemType() {
        return mItemType;
    }

    public int getType() {
        return mType;
    }

    public String getDescription() {
        return mDescr;
    }

    public void setDescription(String descr) {
        mDescr = descr;
    }

    public void setCallback(Runnable cb) {
        mCB = cb;
    }

    protected void runCB() {
        if (mProxy) return;
        if (mCB == null) return;
        mCB.run();
    }

    protected void setReadable() {
        mRW |= 1;
    }

    public boolean isReadable() {
        return (mRW & 1) != 0;
    }

    protected void setWritable() {
        mRW |= 2;
    }

    public boolean isWritable() {
        return (mRW & 2) != 0;
    }

    public abstract int getIntValue(Object obj);

    public abstract boolean getBoolValue(Object obj);

    public abstract float getFloatValue(Object obj);

    public abstract String getStringValue(Object obj);

    public abstract boolean setIntValue(Object obj, int value);

    public abstract boolean setBoolValue(Object obj, boolean value);

    public abstract boolean setFloatValue(Object obj, float value);

    public abstract boolean setStringValue(Object obj, String value);

    public abstract boolean setVoidValue(Object obj);

    public void sendToClient(Packet out) throws IOException {
        if (mProxy != false) {
            throw new RuntimeException("This must be called by the server only!");
        }
        out.writeInt(mItemType);
        out.writeUTF(mName);
        Util.writeString(out, mDescr);
        out.writeInt(mType);
        out.writeInt(mRW);
    }

    public static Item readFromServer(Packet in) throws IOException {
        int itemType = in.readInt();
        String name = in.readUTF();
        String descr = Util.readString(in);
        int type = in.readInt();
        int rw = in.readInt();
        Item ret = null;
        switch (itemType) {
            case ITEM_FIELD:
                ret = FieldItem.readFromServer(in, name, type);
                break;
            case ITEM_PROPERTY:
                ret = PropertyItem.readFromServer(in, name, type);
                break;
            case ITEM_COMMAND:
                ret = CommandItem.readFromServer(in, name, type);
                break;
            default:
                throw new RuntimeException("Unknown item type: " + itemType);
        }
        ret.setDescription(descr);
        ret.mRW = rw;
        return ret;
    }

    public void sendValueToClient(Packet out, Object obj) throws IOException {
        switch (getType()) {
            case TYPE_BOOLEAN:
                out.writeBoolean(getBoolValue(obj));
                break;
            case TYPE_INT:
                out.writeInt(getIntValue(obj));
                break;
            case TYPE_FLOAT:
                out.writeFloat(getFloatValue(obj));
                break;
            case TYPE_STRING:
                Util.writeString(out, getStringValue(obj));
                break;
            case TYPE_NONE:
                // NOP
                break;
            default:
                throw new IllegalArgumentException();
        }
    }

    public void readValueFromClient(Packet is, Object obj) throws IOException {
        switch (getType()) {
            case TYPE_BOOLEAN:
                setBoolValue(obj, is.readBoolean());
                break;
            case TYPE_INT:
                setIntValue(obj, is.readInt());
                break;
            case TYPE_FLOAT:
                setFloatValue(obj, is.readFloat());
                break;
            case TYPE_STRING:
                setStringValue(obj, Util.readString(is));
                break;
            case TYPE_NONE:
                setVoidValue(obj);
                break;
            default:
                throw new IllegalArgumentException();
        }
        runCB();
    }

    public int readIntValue(Packet out) throws IOException {
        Packet in = sendReadReq(out);
        return in.readInt();
    }

    public float readFloatValue(Packet out) throws IOException {
        Packet in = sendReadReq(out);
        return in.readFloat();
    }

    public boolean readBoolValue(Packet out) throws IOException {
        Packet in = sendReadReq(out);
        return in.readBoolean();
    }

    public String readStringValue(Packet out) throws IOException {
        Packet in = sendReadReq(out);
        return Util.readString(in);
    }

    protected Packet sendReadReq(Packet out) throws IOException {
        out.writeUTF(mName);
        Packet in = out.sendAndReceive(XAppDbgCmdIDs.CMD_READ_PROP);
        int found = in.readInt();
        if (found == 0) {
            throw new RuntimeException("Property " + mName + " not found on server!");
        }
        return in;
    }

    public void sendIntValueToServer(int value, Packet out) throws IOException {
        out.writeUTF(mName);
        out.writeInt(value);
        out.sendAndReceive(XAppDbgCmdIDs.CMD_WRITE_PROP); // ignore reply
    }

    public void sendFloatValueToServer(float value, Packet out) throws IOException {
        out.writeUTF(mName);
        out.writeFloat(value);
        out.sendAndReceive(XAppDbgCmdIDs.CMD_WRITE_PROP); // ignore reply
    }

    public void sendBoolValueToServer(boolean value, Packet out) throws IOException {
        out.writeUTF(mName);
        out.writeBoolean(value);
        out.sendAndReceive(XAppDbgCmdIDs.CMD_WRITE_PROP); // ignore reply
    }

    public void sendStringValueToServer(String value, Packet out) throws IOException {
        out.writeUTF(mName);
        Util.writeString(out, value);
        out.sendAndReceive(XAppDbgCmdIDs.CMD_WRITE_PROP); // ignore reply
    }

    public void sendVoidValueToServer(Packet out) throws IOException {
        out.writeUTF(mName);
        out.sendAndReceive(XAppDbgCmdIDs.CMD_WRITE_PROP); // ignore reply
    }

}
