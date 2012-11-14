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

import com.sonymobile.tools.xappdbg.XAppDbgServer;
import com.sonymobile.tools.xappdbg.Packet;
import com.sonymobile.tools.xappdbg.properties.XAppDbgTreeModule;

import android.os.Handler;
import android.view.View;

import java.io.IOException;

public class XAppDbgAndroidServer extends XAppDbgServer {

    private Handler mHandler = new Handler();

    public XAppDbgAndroidServer() {
    }

    @Override
    protected boolean handleCommand(String cmdId, Packet p) throws IOException {
        // Need to execute it in the ui thread
        UICmd task = new UICmd(cmdId, p);

        synchronized (task) {
            mHandler.post(task);
            try {
                task.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (task.mExcp != null) {
                throw task.mExcp;
            }
            return task.mRet;
        }
    }

    protected boolean doHandleCommand(String cmdId, Packet p) throws IOException {
        return super.handleCommand(cmdId, p);
    }

    public void addTreeView(final View view) {
        final XAppDbgTreeModule pdTreeMod = new XAppDbgTreeModuleAndroid(view, new AndroidViewTreeParser());
        pdTreeMod.addCommand("Relayout", new Runnable() {
            public void run() {
                Object obj = pdTreeMod.getSelectedNode();
                if (obj instanceof View) {
                    View curView = (View)obj;
                    if (curView != null) {
                        relayout(curView);
                    }
                }
            }
        });
        pdTreeMod.addCommand("Relayout All", new Runnable() {
            public void run() {
                relayout(view);
            }
        });
        pdTreeMod.addCommand("Redraw", new Runnable() {
            public void run() {
                Object obj = pdTreeMod.getSelectedNode();
                if (obj instanceof View) {
                    View curView = (View)pdTreeMod.getSelectedNode();
                    if (curView != null) {
                        curView.postInvalidate();
                    }
                }
            }
        });
        pdTreeMod.addCommand("Redraw All", new Runnable() {
            public void run() {
                view.postInvalidate();
            }
        });
        addModule(pdTreeMod);
    }

    protected void relayout(final View view) {
        view.post(new Runnable() {
            public void run() {
                view.requestLayout();
            }
        });
    }

    class UICmd implements Runnable {

        String mCmdId;
        Packet mPacket;
        IOException mExcp;
        boolean mRet;

        public UICmd(String cmdId, Packet p) {
            mCmdId = cmdId;
            mPacket = p;
        }

        public void run() {
            synchronized (this) {
                try {
                    mRet = doHandleCommand(mCmdId, mPacket);
                } catch (IOException e) {
                    mExcp = e;
                }
                notifyAll();
            }

        }

    }

}
