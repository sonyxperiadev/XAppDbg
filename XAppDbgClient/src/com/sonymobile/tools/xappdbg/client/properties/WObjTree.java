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

import com.sonymobile.tools.xappdbg.XAppDbgCmdIDs;
import com.sonymobile.tools.xappdbg.XAppDbgWidget;
import com.sonymobile.tools.xappdbg.Packet;
import com.sonymobile.tools.xappdbg.client.XAppDbgConnection;
import com.sonymobile.tools.xappdbg.client.XAppDbgWidgetView;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

/**
 * The UI interface for the "tree of object's properties" widget.
 * This widget allows the changing of the properties of several objects
 * organized in a tree structure.
 */
public class WObjTree extends XAppDbgWidgetView implements ActionListener, TreeSelectionListener {

    private JTree mTree;
    private DefaultTreeModel mTreeModel;
    private DefaultMutableTreeNode mRootTreeNode;
    private JPropertyPanel mPropertyPane;
    private JButton mBtnReload;
    private JButton mBtnFind;
    private JButton mBtnFindNext;
    private JTextField mTextToFind;
    private long mNodeIdToFind;
    private String mNodeStr;
    private Enumeration<?> mSearchEnum;
    private int mSelectedHashCode;

    private Vector<PropertyView> mProperties = new Vector<PropertyView>();

    public WObjTree(XAppDbgWidget widget, XAppDbgConnection connection) throws IOException {
        super(widget, connection);
    }

    @Override
    public void createUI(JPanel rootPane) throws IOException {
        // Create the UI
        rootPane.setLayout(new BorderLayout());

        Container toolbar = new Container();
        toolbar.setLayout(new FlowLayout());
        rootPane.add(toolbar, BorderLayout.NORTH);

        mBtnReload = new JButton("Reload");
        mBtnReload.addActionListener(this);
        toolbar.add(mBtnReload);

        mBtnFind = new JButton("Find:");
        mBtnFind.addActionListener(this);
        toolbar.add(mBtnFind);

        mTextToFind = new JTextField("", 20);
        mTextToFind.selectAll();
        toolbar.add(mTextToFind);

        mBtnFindNext = new JButton("Find next");
        mBtnFindNext.addActionListener(this);
        toolbar.add(mBtnFindNext);

        toolbar.add(new JSeparator(SwingConstants.VERTICAL));

        XAppDbgWidget w = getWidget();
        String params[] = w.getParams().split("\\|");
        for (String param : params) {
            if (param.length() == 0) continue;
            JButton btn = new JButton();
            btn.setText(param);
            toolbar.add(btn);
            btn.addActionListener(this);
        }

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setPreferredSize(new Dimension(800, 400));
        splitPane.setDividerLocation(500);
        rootPane.add(splitPane, BorderLayout.CENTER);

        JScrollPane scrollTreePane = new JScrollPane();
        scrollTreePane.setMinimumSize(new Dimension(100, 50));
        splitPane.setLeftComponent(scrollTreePane);

        // fill data
        mRootTreeNode = readTree();
        mTreeModel = new DefaultTreeModel(mRootTreeNode);
        mTree = new JTree(mTreeModel);
        mTree.addTreeSelectionListener(this);
        mTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        scrollTreePane.getViewport().add(mTree);

        // prepare property list
        mPropertyPane = new JPropertyPanel(this);
        splitPane.setRightComponent(mPropertyPane);
    }

    private DefaultMutableTreeNode readTree() throws IOException {
        Packet out = createPacket();
        Packet in = out.sendAndReceive(XAppDbgCmdIDs.CMD_TREE);
        return createTreeStructure(in, null);
    }


    private DefaultMutableTreeNode createTreeStructure(Packet in, DefaultMutableTreeNode parent) throws IOException {
        // for each node we read:
        //    - the hash code as int
        //    - the string representation
        //    - the number of children
        int hashCode = in.readInt();
        String str = in.readUTF();
        int count = in.readInt();

        DefaultMutableTreeNode treeNode = new DefaultMutableTreeNode(str);
        treeNode.setUserObject(new NodeWrapper(hashCode, str));

        if (parent != null) {
            parent.add(treeNode);
        }

        for (int i = 0; i < count; i++) {
            createTreeStructure(in, treeNode);
        }

        return treeNode;
    }


    @Override
    public void valueChanged(TreeSelectionEvent event) {
        JTree tree = (JTree)event.getSource();
        DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();

        if (treeNode == null) return;

        selectNode(treeNode);

        listProperties();
    }


    private void selectNode(DefaultMutableTreeNode treeNode) {
        // Select the node
        NodeWrapper node = (NodeWrapper)treeNode.getUserObject();
        mSelectedHashCode = node.getHashCode();

        // Send selection to server
        Packet out = createPacket();
        try {
            out.writeInt(mSelectedHashCode);
            out.sendAndReceive(XAppDbgCmdIDs.CMD_TREE_SELECT); // Ignore reply packet
        } catch (IOException e) {
            e.printStackTrace();
            getConnection().close(true);
        }
    }

    private void listProperties() {
        // clear property list
        mProperties.clear();

        try {
            mPropertyPane.init();
        } catch (IOException e) {
            e.printStackTrace();
            getConnection().close(true);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();

        if (src == mBtnReload) {
            reloadTree();
            return;
        }

        if (src == mBtnFind) {
            DefaultMutableTreeNode node = searchNode(Long.MAX_VALUE, mTextToFind.getText());
            if (node != null) {
                showTreeNode(node);
            } else {
                System.out.println("Node not found!");
            }
            return;
        }

        if (src == mBtnFindNext) {
            DefaultMutableTreeNode node = searchNextNode();
            if (node != null) {
                showTreeNode(node);
            } else {
                System.out.println("Node not found!");
            }
            return;
        }

        // If we got here, then it must be a custom button
        JButton btn = (JButton)e.getSource();
        String cmd = btn.getText();

        Packet out = createPacket();
        try {
            out.writeUTF(cmd);
            out.sendAndReceive(XAppDbgCmdIDs.CMD_TREE_CMD); // ignore reply packet
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    private void reloadTree() {
        try {
            mRootTreeNode = readTree();
            mTreeModel = new DefaultTreeModel(mRootTreeNode);
            mTree.setModel(mTreeModel);
        } catch (IOException e) {
            e.printStackTrace();
            getConnection().close(true);
        }
    }

    public void findNodeId(long nodeToFindId) {
        DefaultMutableTreeNode node = searchNode(nodeToFindId, null);
        if (node != null) {
            showTreeNode(node);
        } else {
            System.out.println("Focused node not found!");
        }
    }


    private void showTreeNode(DefaultMutableTreeNode node) {
        if (node == null) return;
        TreeNode[] nodes = mTreeModel.getPathToRoot(node);
        if (nodes == null) return;
        TreePath path = new TreePath(nodes);
        mTree.scrollPathToVisible(path);
        mTree.setSelectionPath(path);
    }

    public DefaultMutableTreeNode searchNode(long nodeIdToFind, String nodeStr) {
        this.mNodeIdToFind = nodeIdToFind;
        this.mNodeStr = nodeStr;

        // Get the enumeration
        mSearchEnum = mRootTreeNode.breadthFirstEnumeration();

        return searchNextNode();
    }

    public DefaultMutableTreeNode searchNextNode() {
        if (mSearchEnum == null) {
            System.out.println("No initial search was done!");
            return null;
        }

        // iterate through the enumeration
        while (mSearchEnum.hasMoreElements()) {
            // get the node
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)mSearchEnum.nextElement();

            if (mNodeIdToFind != Long.MAX_VALUE) {
                NodeWrapper nw = (NodeWrapper)node.getUserObject();
                if (nw.getHashCode() == mNodeIdToFind) {
                    return node;
                }
            }

            // match the string with the user-object of the node
            if (mNodeStr != null && node.getUserObject().toString().contains(mNodeStr)) {
                // tree node with string found
                return node;
            }
        }

        // tree node with string node found return null
        return null;
    }


}
