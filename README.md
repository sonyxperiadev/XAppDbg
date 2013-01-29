XAppDbg (Extra application debugging interface)
===============================================

This library is a simple client-server solution for easily controlling your android or java application.
It works by linking the server part into an application, which thus can expose certain fields and methods to the client.
The user can change these fields/methods during runtime and see the effect of the change immediately on the device.


Architecture
------------

<pre>
<code>
+----------------------------+              +----------------------------+
|      Android device        |              |             PC             |
|                            |              |                            |
| +------------------------+ |              | +------------------------+ |
| |     Application        | |              | |                        | |
| |                        | |              | |                        | | 
| | mField                 | |              | |  +------------------+  | |
| |   ^                    | |              | |  | JTextField       |  | |
| |   | (reflection)       | |              | |  +------------------+  | |
| |   |                    | |              | |      ^                 | |
| | +-|------------------+ | |              | |      |                 | |
| | | v                  | | |     TCP/IP   | |      v                 | |
| | | XAppDbg-Server   o------------------------o  XAppDbg-Client      | |
| | |                    | | |              | |                        | |
| | +--------------------+ | |              | |                        | |
| +------------------------+ |              | +------------------------+ |
+----------------------------+              +----------------------------+
</code>
</pre>

Example
-------

    // NOTE: not real constants yet, need to remove the final keyword now
    static class Consts {
        public static int RATE = 10;
        public static float LIFE = 1.0f;
        public static int MIN_COLOR = 0x804020;
        public static int MAX_COLOR = 0xc08060;
        public static float G = 9.6f;
        public static float MIN_X = 0.0f;
        public static float MAX_X = 1.0f;
        public static float MIN_Y = 1.0f;
        public static float MAX_Y = 1.0f;
        public static float MIN_VX = -0.1f;
        public static float MAX_VX = +0.1f;
        public static float MIN_VY = -0.2f;
        public static float MAX_VY = -0.3f;
    }
    
    ...
    
    public void onCreate() {
        super.onCreate();
        ...
        // Create and start the debug server
        mServer = new XAppDbgServer();
        mServer.addModule(new XAppDbgPropertiesModule(Consts.class));
        mServer.start();
        ...
    }

Links
-----

* Blog post: http://developer.sonymobile.com/2013/01/29/debug-and-fine-tune-apps-with-the-open-sourced-xappdbg-tool-tool/
* XDA developers forum thread: http://forum.xda-developers.com/showthread.php?p=37329350
* Javadoc: http://sonyxperiadev.github.com/XAppDbg/doc/index.html
