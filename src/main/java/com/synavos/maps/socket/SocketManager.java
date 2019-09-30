package com.synavos.maps.socket;

import java.net.Socket;

/**
 * The Class SocketManager.
 */
public class SocketManager extends Thread {

    protected Socket socket = null;

    /**
     * Instantiates a new socket manager.
     *
     * @param socket
     *            the socket
     */
    public SocketManager(final Socket socket) {
	this.socket = socket;
    }

    @Override
    public void run() {
	
    }
}
