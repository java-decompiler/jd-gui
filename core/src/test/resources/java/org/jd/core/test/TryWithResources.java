/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.test;

import java.io.*;

public class TryWithResources {

    public void try1Resource(String path) throws IOException {
        try(FileInputStream input = new FileInputStream(path)) {
            int data = input.read();

            while(data != -1) {
                System.out.print((char) data);
                data = input.read();
            }
        }
    }

    public void tryCatch1Resource(String path) {
        try(FileInputStream input = new FileInputStream(path)) {
            int data = input.read();

            while(data != -1) {
                System.out.print((char) data);
                data = input.read();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void tryFinally1Resource(String path) throws IOException {
        try(FileInputStream input = new FileInputStream(path)) {
            int data = input.read();

            while(data != -1) {
                System.out.print((char) data);
                data = input.read();
            }
        } finally {
            System.out.println("finally");
        }
    }

    public void tryCatchFinally1Resource(String path) {
        try(FileInputStream input = new FileInputStream(path)) {
            int data = input.read();

            while(data != -1) {
                System.out.print((char) data);
                data = input.read();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            System.out.println("finally");
        }
    }

    public void try2Resources(String pathIn, String pathOut) throws IOException {
        try(FileInputStream input = new FileInputStream(pathIn);
            FileOutputStream output = new FileOutputStream(pathOut)) {
            int data = input.read();

            while(data != -1) {
                output.write(data);
                data = input.read();
            }
        }
    }

    public void tryCatch2Resources(String pathIn, String pathOut) {
        try(FileInputStream input = new FileInputStream(pathIn);
            FileOutputStream output = new FileOutputStream(pathOut)) {
            int data = input.read();

            while(data != -1) {
                output.write(data);
                data = input.read();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void tryFinally2Resources(String pathIn, String pathOut) throws IOException {
        try(FileInputStream input = new FileInputStream(pathIn);
            FileOutputStream output = new FileOutputStream(pathOut)) {
            int data = input.read();

            while(data != -1) {
                output.write(data);
                data = input.read();
            }
        } finally {
            System.out.println("finally");
        }
    }

    public void tryCatchFinally2Resources(String pathIn, String pathOut) {
        try(FileInputStream input = new FileInputStream(pathIn);
            FileOutputStream output = new FileOutputStream(pathOut)) {
            int data = input.read();

            while(data != -1) {
                output.write(data);
                data = input.read();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            System.out.println("finally");
        }
    }

    public int tryCatchFinally4Resources(String pathIn, String pathOut) throws Exception {
        try {
            try (FileInputStream input = new FileInputStream(pathIn);
                 BufferedInputStream bufferedInput = new BufferedInputStream(input);
                 FileOutputStream output = new FileOutputStream(pathOut);
                 BufferedOutputStream bufferedOutput = new BufferedOutputStream(output)) {
                int data = bufferedInput.read();

                while(data != -1) {
                    bufferedOutput.write(data);
                    data = bufferedInput.read();
                }

                if (data == -7) {
                    return 1;
                }

                try {
                    System.out.println("in try");
                } finally {
                    System.out.println("in finally");
                }

                return 2;
            } catch (ClassCastException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            } catch (Throwable e) {
                e.printStackTrace();
            } finally {
                System.out.println("finally, before loop");
                for (int i=0; i<10; i++) {
                    System.out.println("finally " + i);
                }
                System.out.println("finally, after loop");
            }
        } finally {
            System.out.println("finally");
        }

        return 3;
    }
}
