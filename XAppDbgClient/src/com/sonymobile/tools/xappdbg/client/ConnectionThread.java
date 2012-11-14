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
package com.sonymobile.tools.xappdbg.client;

import com.sonymobile.tools.xappdbg.Packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Handles the communication with the server.
 * It sends commands to the server and reads the replies.
 */
public class ConnectionThread extends Thread implements XAppDbgConnection {

    /** Callbacks are sent via this */
    private Listener mListener;
    /** The server address */
    private String mHost;
    /** The server port */
    private int mPort;
    /** Incomming data channel */
    private DataInputStream mIs;
    /** Outgoing data channel */
    private DataOutputStream mOs;
    /** Connection socket */
    private Socket mSock;
    /** Set to true while the thread is alive */
    private boolean mRunning;

    /**
     * Listener interface to receive callbacks from this class
     */
    public interface Listener {

        /**
         * Called when the connection is established.
         * The main application can then initiate the higher level handshake.
         * @param is The input channel
         * @param os The output channel
         * @throws IOException
         */
        public void onConnected(DataInputStream is, DataOutputStream os) throws IOException;

        /**
         * Request to show the UI for setting up the connection
         * @param show If true, the connection UI should be shown, otherwise it should be hidden
         */
        public void enableConnectionUI(boolean show);

        /**
         * Request to show the UI for debugging the application
         * @param show If true, the debug UI should be shown, otherwise it should be hidden
         */
        public void showDebugControls(boolean show);

        /**
         * Called when a connection error happened.
         * @param msg The error message
         */
        public void onError(String msg);

    }

    /**
     * Creates a thread to create the connection to the server.
     * @param listener The receiver of the callbacks
     * @param host The server's address
     * @param port The server's port
     */
    public ConnectionThread(Listener listener, String host, int port) {
        this.mListener = listener;
        this.mHost = host;
        this.mPort = port;
        setName("XAppDbgClient");
    }

    private void connect() throws IOException {
        mSock = new Socket(mHost, mPort);
        mIs = new DataInputStream(mSock.getInputStream());
        mOs = new DataOutputStream(mSock.getOutputStream());

        System.out.println("# Connected!");

        mListener.onConnected(mIs, mOs);
    }

    @Override
    public void run() {
        // Connect
        try {
            connect();
        } catch (IOException e) {
            e.printStackTrace();
            mListener.onError("Connection failed");
            // re-enable controls
            mListener.enableConnectionUI(true);
            return;
        }

        // Run
        try {
            executeModule();
            // disconnect
            mSock.close();
        } catch (IOException e) {
            e.printStackTrace();
            close(true);
        }
        // hide debug controls
        mListener.showDebugControls(false);
        // re-enable controls
        mListener.enableConnectionUI(true);
    }

    private void executeModule() {
        // show debugging controls
        mListener.showDebugControls(true);

        mRunning = true;

        // just wait for now
        while (mRunning) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
        }
    }

    @Override
    public void close(boolean error) {
        if (mRunning) {
            try {
                mIs.close();
            } catch (IOException e) { }
            try {
                mOs.close();
            } catch (IOException e) { }
            mRunning = false;
            if (error) {
                mListener.onError("Connection failed");
            }
        }
    }

    @Override
    public Packet createPacket(int channel) {
        return new Packet(channel, mIs, mOs);
    }

}
