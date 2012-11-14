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

import com.sonymobile.tools.xappdbg.properties.ClassItems;
import com.sonymobile.tools.xappdbg.properties.Item;
import com.sonymobile.tools.xappdbg.properties.XAppDbgTreeModule;
import com.sonymobile.tools.xappdbg.properties.XAppDbgTreeParser;

import android.view.View;
import android.view.ViewGroup.LayoutParams;

public class XAppDbgTreeModuleAndroid extends XAppDbgTreeModule {

    public XAppDbgTreeModuleAndroid(Object tree, XAppDbgTreeParser treeParser) {
        super(tree, treeParser);
    }

    @Override
    protected ClassItems getPropertiesOf(Object obj) {
        ClassItems ret = super.getPropertiesOf(obj);
        if (obj instanceof View) {
            LayoutParams lp = ((View)obj).getLayoutParams();
            if (lp != null) {
                ClassItems lpItems = ClassItems.scanClass(lp.getClass());
                for (int i = 0; i < lpItems.getItemCount(); i++) {
                    Item item = lpItems.getItem(i);
                    LPItem ditem = new LPItem(item);
                    ret.addItem(ditem);
                }
            }
        }
        return ret;
    }

}
