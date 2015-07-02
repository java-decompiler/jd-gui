/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package org.jd.gui.util.net

class InterProcessCommunications {

    protected static final int PORT = 2015_6

    void listen(Closure closure) throws Exception {
        def listener = new ServerSocket(PORT)

        new Thread().start {
            while (true) {
                listener.accept().withCloseable { Socket socket ->
                    closure(new ObjectInputStream(socket.inputStream).readObject())
                }
            }
        }
    }

    void send(Object obj) {
        new Socket(InetAddress.localHost, PORT).withCloseable { Socket socket ->
            new ObjectOutputStream(socket.outputStream).writeObject(obj)
        }
    }
}
