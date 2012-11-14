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

import java.util.Random;

import com.sonymobile.tools.xappdbg.XAppDbgServer;
import com.sonymobile.tools.xappdbg.properties.XAppDbgPropDescr;
import com.sonymobile.tools.xappdbg.properties.XAppDbgPropertiesModule;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.os.SystemClock;
import android.view.View;

/**
 * Simple particle effects
 */
public class ParticlesCanvas extends View {

    /**
     * All the coefficients are collected here, for easier handling
     */
    static class Consts {
        // Begin Coefficients
        @XAppDbgPropDescr("Number of particles created per second")
        public static int RATE = 10; // number of new particles per second
        @XAppDbgPropDescr("Lifetime of particles in second")
        public static float LIFE = 1.0f;
        @XAppDbgPropDescr("Lower value of color range (0xRRGGBB)")
        public static int MIN_COLOR = 0x804020;
        @XAppDbgPropDescr("Higher value of color range (0xRRGGBB)")
        public static int MAX_COLOR = 0xc08060;
        @XAppDbgPropDescr("Gravitational force")
        public static float G = 9.6f;
        @XAppDbgPropDescr("Minimum X coordinate of newly created particle (0..1)")
        public static float MIN_X = 0.0f;
        @XAppDbgPropDescr("Maximum X coordinate of newly created particle (0..1)")
        public static float MAX_X = 1.0f;
        @XAppDbgPropDescr("Minimum Y coordinate of newly created particle (0..1)")
        public static float MIN_Y = 1.0f;
        @XAppDbgPropDescr("Maximum Y coordinate of newly created particle (0..1)")
        public static float MAX_Y = 1.0f;
        @XAppDbgPropDescr("Minimum default horizontal initial speed")
        public static float MIN_VX = -0.1f;
        @XAppDbgPropDescr("MAximum default horizontal initial speed")
        public static float MAX_VX = +0.1f;
        @XAppDbgPropDescr("Minimum default vertical initial speed")
        public static float MIN_VY = -0.2f;
        @XAppDbgPropDescr("Maximum default vertical initial speed")
        public static float MAX_VY = -0.3f;
        // End Coefficients
    }

    /**
     * Info about one particle
     */
    public static class Particle {

        public float x, y;
        public float vx, vy;
        public int color;
        public int time;

    }

    /** Max number of particles at the same time on the screen */
    public static final int MAX_COUNT = 1024;

    /** Set to true while the animation is running */
    private boolean mRunning;

    /** The particles */
    private Particle[] mParticles = new Particle[MAX_COUNT];

    /** Random number generator */
    private Random mRnd = new Random();

    /** Paint used to render the particles */
    private Paint mPaint = new Paint();

    /** The timestamp of the last render */
    private long mLastTime = 0;

    /** The time elapsed since the last particle was created */
    private float mTimeToCreate = 0;

    /** Our debugging server */
    private XAppDbgServer mServer;

    public ParticlesCanvas(Context context) {
        super(context);
        mPaint.setXfermode(new PorterDuffXfermode(Mode.ADD));
    }

    public void onResume() {
        // Create and start the debug server
        mServer = new XAppDbgServer();
        mServer.addModule(new XAppDbgPropertiesModule(Consts.class));
        mServer.start();

        // Start the animation
        mRunning = true;
        mLastTime = SystemClock.elapsedRealtime();
        mTimeToCreate = 0.0f;
        postInvalidateDelayed(33);
    }

    public void onPause() {
        // Stop animation
        mRunning = false;

        // Stop debug server
        mServer.stop();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // Calculate how many new particles to create
        long now = SystemClock.elapsedRealtime();
        int delta = (int) (now - mLastTime);
        mLastTime = now;
        float createPeriod = 1000.0f / Consts.RATE;
        mTimeToCreate += delta;

        // Render the particles
        for (int i = 0; i < MAX_COUNT; i++) {
            if (mParticles[i] != null) {
                // Particle already exists, so step it
                if (step(mParticles[i], delta)) {
                    draw(canvas, mParticles[i]);
                } else {
                    // Particle died
                    mParticles[i] = null;
                }
            } else {
                // Free slot, should we create a new one
                if (createPeriod <= mTimeToCreate) {
                    mTimeToCreate -= createPeriod;
                    mParticles[i] = createNew();
                }
            }
        }

        // Schedule the next rendering
        if (mRunning) postInvalidateDelayed(33);
    }

    private void draw(Canvas canvas, Particle p) {
        mPaint.setColor(p.color);
        canvas.drawCircle(p.x, p.y, 20, mPaint);
    }

    private boolean step(Particle p, int delta) {
        p.time += delta;
        if (p.time >= Consts.LIFE * 1000f) {
            return false;
        }
        p.x += p.vx * delta / 1000f;
        p.y += p.vy * delta / 1000f;
        p.vy += Consts.G * delta / 1000f;
        return true;
    }

    private Particle createNew() {
        Particle ret = new Particle();
        ret.x = rnd(Consts.MIN_X, Consts.MAX_X) * getWidth();
        ret.y = rnd(Consts.MIN_Y, Consts.MAX_Y) * getHeight();
        ret.vx = rnd(Consts.MIN_VX, Consts.MAX_VX) * getWidth();
        ret.vy = rnd(Consts.MIN_VY, Consts.MAX_VY) * getHeight();
        ret.color = 0xff000000 | rndColor(Consts.MIN_COLOR, Consts.MAX_COLOR);
        ret.time = 0;
        return ret;
    }

    private int rndColor(int minColor, int maxColor) {
        int r = (int)rnd(rof(minColor), rof(maxColor)) & 0xff;
        int g = (int)rnd(gof(minColor), gof(maxColor)) & 0xff;
        int b = (int)rnd(bof(minColor), bof(maxColor)) & 0xff;
        return (r << 16) | (g << 8) | b;
    }

    private float rof(int rgb) {
        return (rgb >> 16) & 0xff;
    }

    private float gof(int rgb) {
        return (rgb >> 8) & 0xff;
    }

    private float bof(int rgb) {
        return (rgb >> 0) & 0xff;
    }

    private float rnd(float min, float max) {
        if (min == max) {
            return min;
        }
        if (min > max) {
            float tmp = min;
            min = max;
            max = tmp;
        }
        return min + (max - min) * mRnd.nextFloat();
    }

}
