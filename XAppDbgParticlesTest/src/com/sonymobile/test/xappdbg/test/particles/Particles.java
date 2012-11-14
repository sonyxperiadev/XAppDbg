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
package com.sonymobile.test.xappdbg.test.particles;

import android.app.Activity;
import android.os.Bundle;

public class Particles extends Activity {

    private ParticlesCanvas mCanvas;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCanvas = new ParticlesCanvas(this);
        setContentView(mCanvas);
    }

    @Override
    protected void onPause() {
        mCanvas.onPause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCanvas.onResume();
    }

}
