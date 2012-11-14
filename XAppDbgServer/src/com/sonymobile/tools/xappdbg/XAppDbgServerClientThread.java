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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;

/**
 * This thread handles the connection with one connected client
 */
public class XAppDbgServerClientThread extends Thread {

    private DataInputStream mIS;
    private DataOutputStream mOS;
    private XAppDbgServer mServer;

    public XAppDbgServerClientThread(Socket sock, int version, XAppDbgServer debugServer) throws IOException {
        mServer = debugServer;
        setName("XAppDbgClient");

        // verify versions
        mOS = new DataOutputStream(sock.getOutputStream());
        mIS = new DataInputStream(sock.getInputStream());
        mOS.writeInt(version);
        mOS.flush();
        int clientVersion = mIS.readInt();
        if (clientVersion != version) {
            System.err.println("[DebugServer] Connection refused due to version mismatch, server: " +
                    Integer.toHexString(version) + " client: " +
                    Integer.toHexString(clientVersion));
            sock.close();
            return;
        }

        start();
    }

    @Override
    public void run() {
        try {
            // First we send the list of widgets
            mServer.listWidgets(mOS);
            mServer.listCommands(mOS);

            // And now we start processing the requests from the client
            System.out.println("# Starting IO loop");
            while (true) {
                String cmd = mIS.readUTF();
                Packet packet = new Packet(mIS, mOS);

                if (cmd == null) {
                    System.out.println("Connection lost");
                    break;
                }
                if (mServer.handleCommand(cmd, packet)) {
                    break;
                }
            }
        } catch (EOFException eof) {
            System.out.println("Connection lost");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
