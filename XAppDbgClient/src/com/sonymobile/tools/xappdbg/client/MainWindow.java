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

import com.sonymobile.tools.xappdbg.XAppDbgWidget;
import com.sonymobile.tools.xappdbg.client.ConnectionThread.Listener;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

/**
 * This is both the main window of the application as well as the main controller.
 */
@SuppressWarnings("serial")
public class MainWindow extends JFrame implements ActionListener, Listener {

    /** Tag used in log messages */
    private static final String TAG = "[XAppDbgClient] ";

    /** The currently used protocol version */
    private static final int PROTOCOL_VERSION = 0x00000001;

    /** Help text for remote connection */
    private static final String DESCR_REMOTE_CONN =
        "<html><body><i><small>" +
        "Use this option if you are connection to the phone over Wifi. " +
        "You need to know the IP address of the phone and write in the " +
        "address field below.<br>You need to change the port only if the " +
        "server changed it from the default value." +
        "</small></i></body></html>";

    /** Help text for local connection */
    private static final String DESCR_LOCAL_CONN =
        "<html><body><i><small>" +
        "Use this option if you are connection to the phone over USB. " +
        "Remember that you need to forward the given port using adb:<br>" +
        "<code>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;adb forward tcp:55011 tcp:55011</pre><br>" +
        "Change the port only if the server changed it from the default value." +
        "</small></i></body></html>";

    /** The connect button */
    private JButton mBtnConnect;
    /** The connect to local computer button */
    private JButton mBtnConnectLocal;
    /** The text field to edit the remote address */
    private JTextField mEditAddress;
    /** The text field to edit the remote port */
    private JTextField mEditPort;
    /** The text field to edit the local port */
    private JTextField mEditLocalPort;
    /** The disconnect button */
    private JButton mBtnDisconnect;
    /** The exit button */
    private JButton mBtnExit;
    /** The panel containing the debug interface */
    private JPanel mDbgPane;
    /** The panel containing the UI for the widgets (debug blocks) */
    private JPanel mWidgetsPane;
    /** The panel containing the UI for setting up the connection */
    private JPanel mConnPane;
    /** The connection thread */
    private ConnectionThread mConn;
    /** The output channel (towards the server) */
    private DataOutputStream mOs;
    /** The input channel (from the server) */
    private DataInputStream mIs;
    /** The set of widgets (debug blocks) used */
    private Vector<XAppDbgWidget> mWidgets = new Vector<XAppDbgWidget>();

    /**
     * Create and show the main window (and thus run the application).
     */
    public MainWindow() {
        setTitle("XAppDbg PC-Client");

        setDefaultCloseOperation(EXIT_ON_CLOSE);

        Container root = getContentPane();
        JPanel mainPane = new JBackgroundPanel("/bugbg.png");
        mainPane.setLayout(new BorderLayout());
        root.add(mainPane);

        System.out.println("When running the server in android, execute this command:");
        System.out.println("  adb forward tcp:55011 tcp:55011");

        ///////////////////////////////////////////////////////////
        // Connection controlls
        mConnPane = new JPanel();
        mainPane.add(mConnPane, BorderLayout.NORTH);
        mConnPane.setLayout(new BoxLayout(mConnPane, BoxLayout.Y_AXIS));

        JPanel connRemotePane = new JPanel();
        mConnPane.add(connRemotePane);
        Border baseBorder = new EtchedBorder(EtchedBorder.LOWERED);
        TitledBorder border = new TitledBorder(baseBorder, "Remote connection", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.TOP);
        connRemotePane.setBorder(border);
        connRemotePane.setLayout(new BorderLayout());

        JLabel descr = new JLabel(DESCR_REMOTE_CONN);
        connRemotePane.add(descr, BorderLayout.NORTH);

        JPanel subPane = new JPanel();
        connRemotePane.add(subPane, BorderLayout.CENTER);

        subPane.add(new JLabel("Address:"));

        mEditAddress = new JTextField("192.168.8.2");
        subPane.add(mEditAddress);

        subPane.add(new JLabel("Port:"));

        mEditPort = new JTextField("55011");
        subPane.add(mEditPort);

        mBtnConnect = new JButton("Connect");
        mBtnConnect.addActionListener(this);
        subPane.add(mBtnConnect);

        JPanel connLocalPane = new JPanel();
        mConnPane.add(connLocalPane);
        baseBorder = new EtchedBorder(EtchedBorder.LOWERED);
        border = new TitledBorder(baseBorder, "Local connection", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.TOP);
        connLocalPane.setBorder(border);
        connLocalPane.setLayout(new BorderLayout());

        descr = new JLabel(DESCR_LOCAL_CONN);
        connLocalPane.add(descr, BorderLayout.NORTH);

        subPane = new JPanel();
        connLocalPane.add(subPane, BorderLayout.CENTER);

        subPane.add(new JLabel("Port:"));

        mEditLocalPort = new JTextField("55011");
        subPane.add(mEditLocalPort);

        mBtnConnectLocal = new JButton("Connect localhost");
        mBtnConnectLocal.addActionListener(this);
        subPane.add(mBtnConnectLocal);

        ///////////////////////////////////////////////////////////
        // Debug controlls

        mDbgPane = new JPanel();
        mDbgPane.setVisible(false);
        mDbgPane.setLayout(new BoxLayout(mDbgPane, BoxLayout.Y_AXIS));
        mainPane.add(mDbgPane, BorderLayout.CENTER);

        mWidgetsPane = new JPanel();
        mWidgetsPane.setLayout(new BoxLayout(mWidgetsPane, BoxLayout.Y_AXIS));
        mDbgPane.add(mWidgetsPane);

        ///////////////////////////////////////////////////////////
        // Buttons
        JPanel btnPane = new JPanel();
        mainPane.add(btnPane, BorderLayout.SOUTH);

        mBtnDisconnect = new JButton("Disconnect");
        mBtnDisconnect.addActionListener(this);
        mBtnDisconnect.setVisible(false);
        btnPane.add(mBtnDisconnect);

        mBtnExit = new JButton("Exit");
        mBtnExit.addActionListener(this);
        btnPane.add(mBtnExit);

        // finish
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();

        if (src == mBtnExit) {
            System.exit(1);
            return;
        }

        if (src == mBtnDisconnect) {
            mConn.close(false);
            return;
        }

        if (src == mBtnConnect) {
            connect();
            return;
        }
        if (src == mBtnConnectLocal) {
            int port = Integer.parseInt(this.mEditLocalPort.getText());
            connect("localhost", port);
            return;
        }

    }

    private void connect() {
        String host = mEditAddress.getText();
        int port = Integer.parseInt(this.mEditPort.getText());
        connect(host, port);
    }

    private void connect(String host, int port) {
        System.out.println("Connecting to `" + host + "' port " + port);

        // Disable input fields
        enableConnectionUI(false);

        // Create connection thread
        mConn = new ConnectionThread(this, host, port);
        mConn.start();
    }

    @Override
    public void enableConnectionUI(boolean val) {
        this.mEditAddress.setEnabled(val);
        this.mEditPort.setEnabled(val);
        this.mEditLocalPort.setEnabled(val);
        this.mBtnConnect.setEnabled(val);
        this.mBtnConnectLocal.setEnabled(val);
        this.mBtnDisconnect.setVisible(!val);
    }

    @Override
    public void showDebugControls(boolean b) {
        mDbgPane.setVisible(b);
        mConnPane.setVisible(!b);
        pack();
    }

    public DataInputStream getIS() {
        return mIs;
    }

    public DataOutputStream getOS() {
        return mOs;
    }

    private void showWidget(XAppDbgWidget w, DataInputStream is, DataOutputStream os) throws IOException {
        JPanel panel = new JPanel();
        mWidgetsPane.add(panel);

        XAppDbgWidgetView widgetView = XAppDbgWidgetView.create(w, mConn);
        if (widgetView != null) {
            if (!w.isInline()) {
                // Just add a shortcut button
                JButton btn = new JButton();
                btn.setText(w.getName());
                btn.addActionListener(new InlineButtonHandler(widgetView));
                panel.add(btn);
            } else {
                widgetView.createUI(panel);
            }
        }

    }

    class InlineButtonHandler implements ActionListener {

        private XAppDbgWidgetView mWidgetView;

        public InlineButtonHandler(XAppDbgWidgetView widgetView) {
            mWidgetView = widgetView;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                mWidgetView.showWindow();
            } catch (IOException e1) {
                e1.printStackTrace();
                mConn.interrupt();
            }
        }

    }

    @Override
    public void onConnected(DataInputStream is, DataOutputStream os) throws IOException {
        this.mIs = is;
        this.mOs = os;

        // check versions
        os.writeInt(MainWindow.PROTOCOL_VERSION);
        os.flush();
        int serverVersion = is.readInt();
        if (serverVersion != PROTOCOL_VERSION) {
            System.err.println(TAG + "Connection refused due to version mismatch, server: " + Integer.toHexString(serverVersion) + " client: " + Integer.toHexString(PROTOCOL_VERSION));
            mConn.close(true);
        }


        System.out.println("# Reading widget list!");
        int cnt = is.readInt();
        mWidgets.clear();
        for (int i = 0; i < cnt; i++) {
            XAppDbgWidget w = XAppDbgWidget.readFromServer(is);
            mWidgets.add(w);
        }

        System.out.println("# Reading command list!");
        cnt = is.readInt();
        for (int i = 0; i < cnt; i++) {
            /* String cmdName = */ is.readUTF();
        }

        System.out.println("# Building UI");
        mWidgetsPane.removeAll();
        for (XAppDbgWidget w : mWidgets) {
            showWidget(w, is, os);
        }
        pack();

        System.out.println("# Server IO Loop started");
    }

    @Override
    public void onError(String msg) {
        JOptionPane.showMessageDialog(this, "Error connecting to server!", msg, JOptionPane.ERROR_MESSAGE);
    }

}
