package org.telegram.mtproto.state;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: ex3ndr
 * Date: 07.11.13
 * Time: 7:26
 */
public class ConnectionInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    private int id;
    private int priority;
    private String address;
    private int port;

    public ConnectionInfo(int id, int priority, String address, int port) {
        this.id = id;
        this.priority = priority;
        this.address = address;
        this.port = port;
    }

    public int getPriority() {
        return priority;
    }

    public String getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }

    public int getId() {
        return id;
    }
}
