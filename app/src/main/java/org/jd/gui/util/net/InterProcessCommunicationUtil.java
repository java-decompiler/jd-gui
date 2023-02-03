/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.util.net;

import org.jd.gui.util.exception.ExceptionUtil;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.function.Consumer;

public class InterProcessCommunicationUtil {

    static class FilterObjectInputStream extends ObjectInputStream {

        public FilterObjectInputStream(InputStream in) throws IOException {
            super(in);
        }

        @Override
        protected Class<?> resolveClass(final ObjectStreamClass classDesc) throws IOException, ClassNotFoundException {
            if (classDesc.getName().equals("[Ljava.lang.String;")) {
                return super.resolveClass(classDesc);
            }
            throw new RuntimeException(String.format("not support class: %s",classDesc.getName()));
        }
    }

    protected static final int PORT = 2015_6;

    public static void listen(final Consumer<String[]> consumer) throws Exception {
        final ServerSocket listener = new ServerSocket(PORT);

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try (Socket socket = listener.accept();
                         ObjectInputStream ois = new FilterObjectInputStream(socket.getInputStream())) {
                        // Receive args from another JD-GUI instance
                        String[] args = (String[])ois.readObject();
                        consumer.accept(args);
                    } catch (IOException|ClassNotFoundException e) {
                        assert ExceptionUtil.printStackTrace(e);
                    }
                }
            }
        };

        new Thread(runnable).start();
    }

    public static void send(String[] args) {
        try (Socket socket = new Socket(InetAddress.getLocalHost(), PORT);
             ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream())) {
            // Send args to the main JD-GUI instance
            oos.writeObject(args);
        } catch (IOException e) {
            assert ExceptionUtil.printStackTrace(e);
        }
    }
}