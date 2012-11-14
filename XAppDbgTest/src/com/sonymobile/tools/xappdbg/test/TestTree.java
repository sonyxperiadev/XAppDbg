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
package com.sonymobile.tools.xappdbg.test;

import com.sonymobile.tools.xappdbg.XAppDbgServer;
import com.sonymobile.tools.xappdbg.properties.XAppDbgPropDescr;
import com.sonymobile.tools.xappdbg.properties.XAppDbgTreeModule;
import com.sonymobile.tools.xappdbg.properties.XAppDbgTreeParser;

import java.util.Vector;

/**
 * Example how to expose a tree of objects
 */
public class TestTree {

    public static void main(String[] args) {
        XAppDbgServer server = new XAppDbgServer();
        server.addModule(new XAppDbgTreeModule(createTree(), new TreeParser()));
        server.start();
    }

    private static Object createTree() {
        Node root = new Node("Grandpa", 88);

        Node item1 = new Node("Daddy", 45);
        root.addChild(item1);
        Node item2 = new Node("Uncle", 42);
        root.addChild(item2);

        Node item3 = new Node("Johny", 20);
        item1.addChild(item3);
        Node item4 = new Node("Mary", 18);
        item1.addChild(item4);

        return root;
    }

    public static class Node {

        @XAppDbgPropDescr("The name of this person")
        public String mName;

        @XAppDbgPropDescr("The age of this person")
        public int mAge;

        private Vector<Node> mChildren= new Vector<Node>();

        public Node(String name, int age) {
            mName = name;
            mAge = age;
        }

        public void addChild(Node child) {
            mChildren.add(child);
        }

        public int getChildCount() {
            return mChildren.size();
        }

        public Node getChild(int idx) {
            return mChildren.get(idx);
        }

    }


    public static class TreeParser implements XAppDbgTreeParser {

        @Override
        public Object getChild(Object parent, int idx) {
            return ((Node)parent).getChild(idx);
        }

        @Override
        public int getChildCount(Object parent) {
            return ((Node)parent).getChildCount();
        }

        @Override
        public String getText(Object obj) {
            Node node = (Node)obj;
            return node.toString() + " - " + node.mAge + " years old";
        }

    }
}

