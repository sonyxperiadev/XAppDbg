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
| |     Application        | |              | |     XAppDbg-Client     | |
| |                        | |              | |                        | | 
| | +--------------------+ | |              | |                        | |
| | |                    | | |     TCP/IP   | |                        | |
| | |  XAppDbg-Server  o------------------------o                      | |
| | |                    | | |              | |                        | |
| | +--------------------+ | |              | |                        | |
| +------------------------+ |              | +------------------------+ |
+----------------------------+              +----------------------------+
</code>
</pre>

Example
-------

