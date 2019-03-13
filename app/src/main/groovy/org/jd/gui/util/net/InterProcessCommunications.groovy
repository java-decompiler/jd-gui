/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
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
