package org.telegram;

import org.telegram.api.TLConfig;
import org.telegram.api.TLDcOption;
import org.telegram.api.engine.storage.AbsApiState;
import org.telegram.mtproto.state.AbsMTProtoState;
import org.telegram.mtproto.state.ConnectionInfo;
import org.telegram.mtproto.state.KnownSalt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DefaultAbsApiState implements AbsApiState {

    private HashMap<Integer, ConnectionInfo[]> connections = new HashMap<Integer, ConnectionInfo[]>();
    private HashMap<Integer, byte[]> keys = new HashMap<Integer, byte[]>();
    private HashMap<Integer, Boolean> isAuth = new HashMap<Integer, Boolean>();
    private int[] knownDCs;
    private int primaryDc = 2;

    public DefaultAbsApiState(boolean isTest) {
        HashMap<Integer, String> initialTestDc = new HashMap<>();
        HashMap<Integer, String> initialProductionDc = new HashMap<>();
        initialTestDc.put(3, "149.154.175.117:443");
        initialProductionDc.put(2, "149.154.167.50:443");

        HashMap<Integer, String> knownDcMap = isTest ? initialTestDc : initialProductionDc;
        this.primaryDc = knownDcMap.entrySet().iterator().next().getKey();
        knownDCs = new int[knownDcMap.size()];
        String[] addressAndPort;
        int index = 0;
        for(Map.Entry<Integer, String> dc : knownDcMap.entrySet()) {
            addressAndPort = dc.getValue().split(":");
            connections.put(dc.getKey(), new ConnectionInfo[]{
                    new ConnectionInfo(1, 0, addressAndPort[0], Integer.valueOf(addressAndPort[1]))
            });
            knownDCs[index] = dc.getKey();
            index++;
        }


    }

    public int[] getKnownDCs() {
        return knownDCs;
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
        HashMap<Integer, ArrayList<ConnectionInfo>> tConnections = new HashMap<Integer, ArrayList<ConnectionInfo>>();
        int id = 1;
        for (TLDcOption option : config.getDcOptions()) {
            if (!tConnections.containsKey(option.getId())) {
                tConnections.put(option.getId(), new ArrayList<ConnectionInfo>());
            }
            tConnections.get(option.getId()).add(new ConnectionInfo(id++, 0, option.getIpAddress(), option.getPort()));
        }

        knownDCs = new int[tConnections.size()];
        int index = 0;
        for (Integer dc : tConnections.keySet()) {
            connections.put(dc, tConnections.get(dc).toArray(new ConnectionInfo[0]));
            knownDCs[index] = dc;
            index++;
        }
    }

}
