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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * A packet is used to conveniently send multiplexed data to and from the server.
 */
public class Packet {

    private boolean mReadable;
    private boolean mWritable;
    private int mLen;
    private int mChannel;
    private byte[] mData;
    private ByteArrayOutputStream mBuff;
    private DataOutputStream mBuffOS;
    private DataOutputStream mOS;
    private DataInputStream mBuffIS;
    private DataInputStream mIS;
    private boolean mSent;

    public Packet(int channel, DataInputStream is, DataOutputStream os) {
        mReadable = false;
        mWritable = true;
        mChannel = channel;
        mBuff = new ByteArrayOutputStream();
        mBuffOS = new DataOutputStream(mBuff);
        mIS = is;
        mOS = os;
    }

    /**
     * Create an incomming packet
     */
    /* package */ Packet(DataInputStream is, DataOutputStream os) throws IOException {
        mReadable = true;
        mWritable = false;
        // Read packet size
        mLen = is.readInt();
        // Read channel
        mChannel = is.readInt();
        // Read data
        mData = new byte[mLen];
        is.readFully(mData);
        mBuffIS = new DataInputStream(new ByteArrayInputStream(mData));
        // Store the output stream for reference
        mOS = os;
    }

    /* package */ void send(DataOutputStream os) throws IOException {
        if (mSent) {
            throw new RuntimeException("Packet already sent, refusing to send again!");
        }
        if (!mWritable) {
            throw new RuntimeException("Cannot send a read-only packet");
        }
        byte data[] = mBuff.toByteArray();
        // Write packet size
        os.writeInt(data.length);
        // Write channel
        os.writeInt(mChannel);
        // Write data
        os.write(data);
        // Make sure the data is sent
        os.flush();
        mSent = true;
    }

    public int getChannel() {
        return mChannel;
    }

    public Packet createReply() {
        return new Packet(mChannel, null, mOS);
    }

    private Packet getReply() throws IOException {
        return new Packet(mIS, mOS);
    }

    public void send() throws IOException {
        send(mOS);
    }

    public Packet sendAndReceive(String cmd) throws IOException {
        mOS.writeUTF(cmd);
        send(mOS);
        return getReply();
    }

    public int readInt() throws IOException {
        if (!mReadable) {
            throw new RuntimeException("Cannot read from a write-only packet");
        }
        return mBuffIS.readInt();
    }

    public float readFloat() throws IOException {
        if (!mReadable) {
            throw new RuntimeException("Cannot read from a write-only packet");
        }
        return mBuffIS.readFloat();
    }

    public boolean readBoolean() throws IOException {
        if (!mReadable) {
            throw new RuntimeException("Cannot read from a write-only packet");
        }
        return mBuffIS.readBoolean();
    }

    public String readUTF() throws IOException {
        if (!mReadable) {
            throw new RuntimeException("Cannot read from a write-only packet");
        }
        return mBuffIS.readUTF();
    }

    public void writeInt(int value) throws IOException {
        if (!mWritable) {
            throw new RuntimeException("Cannot send a read-only packet");
        }
        mBuffOS.writeInt(value);
    }

    public void writeFloat(float value) throws IOException {
        if (!mWritable) {
            throw new RuntimeException("Cannot send a read-only packet");
        }
        mBuffOS.writeFloat(value);
    }

    public void writeBoolean(boolean value) throws IOException {
        if (!mWritable) {
            throw new RuntimeException("Cannot send a read-only packet");
        }
        mBuffOS.writeBoolean(value);
    }

    public void writeUTF(String value) throws IOException {
        if (!mWritable) {
            throw new RuntimeException("Cannot send a read-only packet");
        }
        mBuffOS.writeUTF(value);
    }

}
