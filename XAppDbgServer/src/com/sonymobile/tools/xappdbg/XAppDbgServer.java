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

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Vector;

public class XAppDbgServer {

    private static final int PROTOCOL_VERSION = 0x00000001;

    private int mPort = 55011;
    private DebugServerThread mThread;
    private Vector<XAppDbgModule> mModules = new Vector<XAppDbgModule>();
    private HashMap<String,XAppDbgCommand> mCommands = new HashMap<String, XAppDbgCommand>();
    private Vector<XAppDbgWidget> mWidgets = new Vector<XAppDbgWidget>();
    private int mNextChannel = 1;

    public XAppDbgServer() {
    }

    public void addModule(XAppDbgModule mod) {
        // Assign a unique channel id
        int channel = mNextChannel;
        mNextChannel++;
        mod.setChannel(channel);

        mModules.add(mod);
        for (XAppDbgCommand cmd : mod.getCommands()) {
            mCommands.put(cmd.getID(), cmd);
        }
        for (XAppDbgWidget w : mod.getWidgets()) {
            mWidgets.add(w);
        }
    }

    public XAppDbgCommand getCommand(String cmdId, int channel) {
        // First find the module
        XAppDbgModule mod = findModuleByChannel(channel);
        if (mod == null) {
            return null;
        }
        // Then find the command in the module
        return mod.findCommand(cmdId);
    }

    private XAppDbgModule findModuleByChannel(int channel) {
        for (XAppDbgModule mod : mModules) {
            if (mod.getChannel() == channel) {
                return mod;
            }
        }
        return null;
    }

    protected boolean handleCommand(String cmdId, Packet packet) throws IOException {
        long t0 = System.currentTimeMillis();
        XAppDbgCommand cmd = getCommand(cmdId, packet.getChannel());
        if (cmd != null) {
            Packet reply = packet.createReply();
            cmd.exec(packet, reply);
            reply.send();
        } else {
            System.out.println("PANIC: Unknown command '" + cmdId + "'");
            return true;
        }
        long t1 = System.currentTimeMillis();
        System.out.println("Executed command '" + cmdId + "' in " + (t1 - t0) + "ms");
        return false;
    }

    public void listWidgets(DataOutputStream os) throws IOException {
        System.out.println("# Sending widget list");
        os.writeInt(mWidgets.size());
        for (XAppDbgWidget w : mWidgets) {
            w.writeToClient(os);
        }
    }

    public void listCommands(DataOutputStream os) throws IOException {
        System.out.println("# Sending command list");
        Collection<XAppDbgCommand> cmds = mCommands.values();
        os.writeInt(cmds.size());
        for (XAppDbgCommand cmd : cmds) {
            os.writeUTF(cmd.getID());
        }
    }

    public void start() {
        mThread = new DebugServerThread();
        mThread.setName("XAppDbgServer");
        mThread.start();
    }

    public void stop() {
        mThread.stopThread();
    }

    class DebugServerThread extends Thread {

        private ServerSocket ss;

        @Override
        public void run() {
            for (int tries = 0; tries < 10; tries++) {
                try {
                    ss = new ServerSocket(mPort);
                    break; // connected
                } catch (Exception e) {
                    System.out.println("Error creating server socket: " + e.getMessage() + ", try#" + tries);
                    try {
                        Thread.sleep(5*1000); // sleep 5 seconds
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                }
            }
            if (ss == null) {
                System.out.println("Error creating server socket, giving up!");
                return; // give up
            }

            // Just for debugging, list all network interfaces
            System.out.println("Network interfaces:");
            try {
                for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                    NetworkInterface intf = en.nextElement();
                    for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                        InetAddress inetAddress = enumIpAddr.nextElement();
                        if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                            System.out.println("  - " + inetAddress);
                        }
                    }
                }
            } catch (SocketException e) {
                // ignore, this block is used only for debugging
            }

            try {
                while (mThread != null) {
                    System.out.println("Waiting for connection...");
                    Socket sock = ss.accept();
                    System.out.println("Client connected!");

                    // start client handling thread
                    new XAppDbgServerClientThread(sock, PROTOCOL_VERSION, XAppDbgServer.this);

                    synchronized (connectedSync) {
                        connected = true;
                        connectedSync.notify();
                    }

                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (ss != null) {
                    try {
                        ss.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                ss = null;
            }
        }

        public void stopThread() {
            mThread = null;
            if (ss != null) {
                try {
                    ss.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    private Object connectedSync = new Object();
    private boolean connected = false;

    public void waitForConnection() throws InterruptedException {
        synchronized (connectedSync) {
            if (!connected) {
                connectedSync.wait();
            }
        }

    }

}
