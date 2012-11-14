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

import com.sonymobile.tools.xappdbg.properties.XAppDbgTreeParser;

import android.view.View;
import android.view.ViewGroup;

public class AndroidViewTreeParser implements XAppDbgTreeParser {

    public Object getChild(Object parent, int idx) {
        // Lookup child
        if (parent instanceof ViewGroup) {
            ViewGroup view = (ViewGroup)parent;
            return view.getChildAt(idx);
        }
        return null;
    }

    public int getChildCount(Object parent) {
        // Fetch the number of real children
        if (parent instanceof ViewGroup) {
            ViewGroup view = (ViewGroup)parent;
            return view.getChildCount();
        }

        return 0;
    }

    public String getText(Object node) {
        StringBuffer sb = new StringBuffer();
        String className = node.getClass().getName();
        if (className.startsWith("android.widget.")) {
            className = className.substring(15);
        } else if (className.startsWith("android.view.")) {
            className = className.substring(13);
        }
        sb.append(className);
        sb.append('@');
        sb.append(node.hashCode());

        if (node instanceof View) {
            View view = (View)node;
            int x = view.getLeft();
            int y = view.getTop();
            int w = view.getWidth();
            int h = view.getHeight();
            sb.append('(');
            sb.append(x);
            sb.append(',');
            sb.append(y);
            sb.append(' ');
            sb.append(w);
            sb.append('*');
            sb.append(h);
            sb.append(')');

            if (view.getVisibility() == View.INVISIBLE) {
                sb.append("[INVIS]");
            } else if (view.getVisibility() == View.GONE) {
                sb.append("[GONE]");
            }
        }

        return sb.toString();
    }

}

