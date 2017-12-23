package org.telegram;

import org.telegram.api.TLConfig;
import org.telegram.api.TLDcOption;
import org.telegram.api.engine.storage.AbsApiState;
import org.telegram.api.updates.TLState;
import org.telegram.mtproto.state.AbsMTProtoState;
import org.telegram.mtproto.state.ConnectionInfo;
import org.telegram.mtproto.state.KnownSalt;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DefaultAbsApiState implements AbsApiState, Serializable {

    private static final long serialVersionUID = 1L;

    private HashMap<Integer, SerializableConnectionInfo[]> connections = new HashMap<>();
    private HashMap<Integer, byte[]> keys = new HashMap<>();
    private HashMap<Integer, Boolean> isAuth = new HashMap<>();
    private int primaryDc;

    private TLState tlState;

    public DefaultAbsApiState(boolean isTest) {
        HashMap<Integer, String> initialTestConnections = new HashMap<>();
        HashMap<Integer, String> initialProductionConnections = new HashMap<>();
        initialTestConnections.put(3, "149.154.175.117:443");
        initialProductionConnections.put(2, "149.154.167.50:443");

        HashMap<Integer, String> knownDcMap = isTest ? initialTestConnections : initialProductionConnections;
        primaryDc = knownDcMap.entrySet().iterator().next().getKey();
        String[] addressAndPort;
        for(Map.Entry<Integer, String> dc : knownDcMap.entrySet()) {
            addressAndPort = dc.getValue().split(":");
            connections.put(dc.getKey(), new SerializableConnectionInfo[]{
                    new SerializableConnectionInfo(1, 0, addressAndPort[0], Integer.valueOf(addressAndPort[1]))
            });
        }
    }

    @Override
    public byte[] getAuthKey(int dcId) {
        return keys.get(dcId);
    }

    @Override
    public ConnectionInfo[] getAvailableConnections(int dcId) {
        if (!connections.containsKey(dcId)) {
            return new ConnectionInfo[0];
        }
        return connections.get(dcId);
    }

    @Override
    public AbsMTProtoState getMtProtoState(final int dcId) {
        return new AbsMTProtoState() {
            private KnownSalt[] knownSalts = new KnownSalt[0];

            @Override
            public byte[] getAuthKey() {
                return DefaultAbsApiState.this.getAuthKey(dcId);
            }

            @Override
            public ConnectionInfo[] getAvailableConnections() {
                return DefaultAbsApiState.this.getAvailableConnections(dcId);
            }

            @Override
            public KnownSalt[] readKnownSalts() {
                return knownSalts;
            }

            @Override
            protected void writeKnownSalts(KnownSalt[] salts) {
                knownSalts = salts;
            }
        };
    }

    @Override
    public int getPrimaryDc() {
        return primaryDc;
    }

    @Override
    public boolean isAuthenticated(int dcId) {
        if (isAuth.containsKey(dcId)) {
            return isAuth.get(dcId);
        }
        return false;
    }

    @Override
    public void putAuthKey(int dcId, byte[] key) {
        keys.put(dcId, key);
    }

    @Override
    public void reset() {
        isAuth.clear();
        keys.clear();
    }

    @Override
    public void resetAuth() {
        isAuth.clear();
    }

    @Override
    public void setAuthenticated(int dcId, boolean auth) {
        isAuth.put(dcId, auth);
    }

    @Override
    public void setPrimaryDc(int dc) {
        primaryDc = dc;
    }

    @Override
    public void updateSettings(TLConfig config) {
        connections.clear();
        HashMap<Integer, ArrayList<SerializableConnectionInfo>> tConnections = new HashMap<>();
        int id = 1;
        for (TLDcOption option : config.getDcOptions()) {
            if (!tConnections.containsKey(option.getId())) {
                tConnections.put(option.getId(), new ArrayList<>());
            }
            tConnections.get(option.getId()).add(new SerializableConnectionInfo(id++, 0, option.getIpAddress(), option.getPort()));
        }

        for (Integer dc : tConnections.keySet()) {
            connections.put(dc, tConnections.get(dc).toArray(new SerializableConnectionInfo[0]));
        }
    }

    public TLState getTlState() {
        return tlState;
    }

    public void setTlState(TLState tlState) {
        this.tlState = tlState;
    }
}
