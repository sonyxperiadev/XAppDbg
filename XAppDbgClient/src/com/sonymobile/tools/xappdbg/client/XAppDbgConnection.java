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

/**
 * Simple communication interface.
 * It supports simple multiplexing by allowing the creation of packets with
 * different channel numbers.
 */
public interface XAppDbgConnection {

    /**
     * Request to close the connection.
     * @param error If set to true, the connection is closed due to a detected error
     */
    public void close(boolean error);

    /**
     * Create a packet to be sent on this connection.
     * @param channel The channel id
     * @return The new empty packet
     */
    public Packet createPacket(int channel);

}
