package org.telegram;

import org.telegram.mtproto.state.ConnectionInfo;

/**
 * Created by s_tayari on 12/23/2017.
 */
public class SerializableConnectionInfo extends ConnectionInfo {

    public SerializableConnectionInfo(int id, int priority, String address, int port) {
        super(id, priority, address, port);
    }
}
