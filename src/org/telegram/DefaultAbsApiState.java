package org.telegram;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.HashMap;

import com.sun.org.apache.xml.internal.security.exceptions.Base64DecodingException;
import com.sun.org.apache.xml.internal.security.utils.Base64;
import org.telegram.api.TLConfig;
import org.telegram.api.TLDcOption;
import org.telegram.api.engine.storage.AbsApiState;
import org.telegram.mtproto.state.AbsMTProtoState;
import org.telegram.mtproto.state.ConnectionInfo;
import org.telegram.mtproto.state.KnownSalt;

public class DefaultAbsApiState implements AbsApiState {

    private HashMap<Integer, ConnectionInfo[]> connections = new HashMap<Integer, ConnectionInfo[]>();
    private HashMap<Integer, byte[]> keys = new HashMap<Integer, byte[]>();
    private HashMap<Integer, Boolean> isAuth = new HashMap<Integer, Boolean>();

    private int primaryDc = 1;

    public DefaultAbsApiState() {
        connections.put(1, new ConnectionInfo[]{
//                new ConnectionInfo(1, 0, "149.154.167.40", 443) // Test
                new ConnectionInfo(1, 0, "149.154.167.50", 443) // Production
        });
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

        for (Integer dc : tConnections.keySet()) {
            connections.put(dc, tConnections.get(dc).toArray(new ConnectionInfo[0]));
        }
    }

}
