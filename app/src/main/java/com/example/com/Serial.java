package com.example.com;

import java.io.FileDescriptor;

public class Serial {
    private static final String LIB_NAME = "serial_port";

    private FileDescriptor mFd = null;

    public FileDescriptor Open(int devNo, int bps, int flags) {
        mFd = open(devNo, bps, flags);
        return mFd;
    }

    public void Close() {
        close();
    }


    native FileDescriptor open(int devNo, int baudrate, int flags);
    native void close();

    static {
        System.loadLibrary( LIB_NAME );
    }
}
