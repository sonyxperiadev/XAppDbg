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

package com.sonymobile.tools.xappdbg.client.properties;

/**
 * A simple class which represents a remote node object.
 * The object is identified by the hashcode (of the remote object)
 */
public class NodeWrapper {

    /** The remote object's hash code */
    private int mHashCode;
    /** The remote object's label */
    private String mLabel;

    public NodeWrapper(int hashCode, String str) {
        this.mHashCode = hashCode;
        this.mLabel = str;
    }

    public int getHashCode() {
        return mHashCode;
    }

    @Override
    public String toString() {
        return mLabel;
    }

}
