package com.synavos.maps.socket;

import java.net.ServerSocket;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.stereotype.Service;

import com.synavos.maps.properties.GoogleMapProperties;
import com.synavos.maps.utils.CommonUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * The Class ConnectionManager.
 *
 * @author Ibraheem Faiq
 * @since Apr 12, 2018
 */
@Service
@Slf4j
public class ConnectionManager {

    private static AtomicBoolean init = new AtomicBoolean(false);

    /**
     * Instantiates a new connection manager.
     */
    public ConnectionManager() {
	super();
    }

    /**
     * Inits the.
     */
    public void init() {
	if (!init.get() && GoogleMapProperties.SERVER_SOCKET_PORT > 0) {
	    openServerSocket();
	    init.set(true);
	}
    }

    private void openServerSocket() {
	ServerSocket serverSocket = null;
	try {
	    serverSocket = new ServerSocket(GoogleMapProperties.SERVER_SOCKET_PORT);
	    while (true) {
		serverSocket.accept();
	    }
	}
	catch (final Exception ex) {
	    log.error("##Exception## occurred while opening/listening to socket connections", ex);
	}
	finally {
	    CommonUtils.closeResources(serverSocket);
	}
    }
}
