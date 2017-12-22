package org.telegram;

import org.telegram.api.TLAbsUpdates;
import org.telegram.api.TLConfig;
import org.telegram.api.TLInputPeerSelf;
import org.telegram.api.TLNearestDc;
import org.telegram.api.auth.TLAuthorization;
import org.telegram.api.auth.TLSentCode;
import org.telegram.api.engine.ApiCallback;
import org.telegram.api.engine.AppInfo;
import org.telegram.api.engine.RpcException;
import org.telegram.api.engine.TelegramApi;
import org.telegram.api.messages.TLAbsSentMessage;
import org.telegram.api.requests.*;
import org.telegram.tl.TLMethod;
import org.telegram.tl.TLObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

public class TelegramClient {

    private static final int API_ID = 33986;
    private static final String API_HASH = "cbf75f71f7b931f7d137a60d318590dd";
    private static final String PHONE_NUMBER = "+989123106718";
//    private static final int myId = 106549455;
//    private static final String HASH = "f593011df04fb04285";
//    private static final String HASH = "b3007393d6b530021c";
    private int primaryDc = 2;
    private static String key="[-69, -23, 111, -35, 81, -5, -54, -47, -17, -94, 127, -107, 88, 109, 69, -67, 13, 93, 81, -63, 79, -16, -65, -86, 55, -33, -24, -72, -24, -6, 92, 33, 112, -36, 104, 116, -16, -27, -33, -30, 56, 43, 9, -87, 42, 106, 84, 63, -89, 59, 20, 100, -80, 2, 25, -53, 87, 40, 32, 92, 122, -62, -110, 119, -39, 35, -121, -75, -61, -83, 95, -78, 7, 28, 41, -97, 15, -108, 85, -80, 72, 63, -126, -38, 124, -62, -121, 33, 27, 11, 75, 57, -115, 92, -99, 70, -22, 116, -18, 109, 105, -125, -63, -79, -49, -127, -43, 114, -106, 13, 55, -53, -87, 77, 17, -16, 58, -68, 9, -78, -6, 116, 104, 3, -69, -15, 68, 14, 122, 121, 58, 105, -40, -92, 104, 72, 107, -31, 33, -75, -10, 3, 85, 3, -113, -74, 31, 126, 68, 89, 20, -65, -102, 119, 74, 13, -58, -38, -83, 103, -10, -21, 12, 81, 124, 123, 118, -78, 105, 105, -50, 68, 83, 118, -91, -71, -49, -115, -120, 100, -72, 125, -73, -7, 7, 10, -8, -76, -63, -17, -109, 74, -6, -97, 72, 27, -67, -116, -15, -88, -72, -15, -53, -118, -71, 77, -31, 71, 7, -50, 70, 23, 86, -84, -17, -16, -38, -68, -82, -109, -29, 0, 100, 73, 77, 77, 6, -37, -7, 53, -112, -95, 62, 110, 123, -71, -111, 79, -110, 119, 102, -14, 39, 44, -91, -28, 62, -122, 72, 9, 123, 83, -97, -84, -46, 23]";// = "[49, -58, -48, 34, -3, 91, -124, 37, 93, 112, 87, -16, -73, 63, -89, -90, -3, 101, -84, -41, 22, 15, -60, 95, 114, 3, 40, 89, -69, -19, -125, 99, -64, 67, -101, -57, -18, 76, 10, 13, 45, -76, 8, -86, -31, 50, 60, -67, -121, -110, -110, -108, -29, 113, 60, -51, -95, -103, -89, -41, 33, -66, -118, -91, -62, 106, 23, 107, 97, -65, 108, -49, 29, -82, 11, -117, 51, 122, 78, 0, -45, 8, -71, -71, -113, -66, 25, 116, 2, 112, -16, -95, -78, -60, -121, -102, 115, -101, 62, 69, 52, 0, 105, 5, -120, 87, -15, 84, -123, -14, -88, -114, -16, -92, 90, -124, -61, -109, 126, -89, -79, 38, 36, -39, 101, -121, 68, 108, 101, 31, 112, 3, -128, 36, 127, -19, -85, -118, -55, 78, 94, -79, 57, -104, 25, 53, -94, 81, -41, 120, 17, -9, 119, 86, -51, -24, 121, -92, -24, -120, -49, -10, -84, -93, 17, 65, -100, 100, 4, 30, 27, 78, 120, 18, -83, 92, -83, 122, 52, -3, 91, -56, 2, -58, 59, 95, 95, 51, 27, -5, -75, -125, 91, -93, 108, -65, -67, -61, 35, -30, 76, -4, 4, 72, 98, 17, -16, 108, 115, 125, -8, 101, 80, 10, 15, 4, 12, -108, -65, 72, 60, -32, -58, -9, 75, 40, -88, -79, -17, 111, 33, 55, 127, 100, -101, 78, -123, -62, 35, -103, -113, -26, 47, -30, -37, 80, -35, -87, 51, -118, 120, 82, -46, 103, 47, 10]";

    public static void main(String[] args) {
        DefaultAbsApiState state = new DefaultAbsApiState(true);
        final TelegramApi api = new TelegramApi(state, new AppInfo(API_ID,
                "Test Client", "0.0.1", "0.0.1", "en"), new ApiCallback() {

            @Override
            public void onUpdatesInvalidated(TelegramApi _api) {

            }

            @Override
            public void onAuthCancelled(TelegramApi _api) {
                System.out.println(_api);
            }

            @Override
            public void onUpdate(TLAbsUpdates updates) {
                System.out.println(updates);
            }
        });

        TLConfig config = doRpc(api, new TLRequestHelpGetConfig(), false);
        state.updateSettings(config);
        api.resetConnectionInfo();

        TLNearestDc tlNearestDc = doRpc(api, new TLRequestHelpGetNearestDc(), false);
        switchToDc(api, tlNearestDc.getNearestDc());

        if (key == null) {
            TLAuthorization authorization = handleRegistration(api);
            api.getState().setAuthenticated(api.getState().getPrimaryDc(), true);
            key = Arrays.toString(state.getAuthKey(state.getPrimaryDc()));
        } else {
            state.putAuthKey(state.getPrimaryDc(), buildKeyFromString());
            state.setAuthenticated(api.getState().getPrimaryDc(), true);
        }

        TLAbsSentMessage tlAbsSentMessage = doRpc(api, new TLRequestMessagesSendMessage(new TLInputPeerSelf(), "Wizzo F baby?", 123123123), true);
        System.out.println(tlAbsSentMessage.getDate());
    }

    private static byte[] buildKeyFromString() {
        String[] byteValues = key.replace("[", "").replace("]", "").split(",");
        byte[] byteArray = new byte[byteValues.length];
        for(int i = 0; i < byteValues.length; i++) {
            byteArray[i] = Byte.parseByte(byteValues[i].trim());
        }

        return byteArray;
    }

    private static TLAuthorization handleRegistration(TelegramApi api) {
        TLSentCode sentCode = doRpc(api, new TLRequestAuthSendCode(PHONE_NUMBER,
                5, API_ID, API_HASH, "en"), false);
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                System.in));
        System.out.println("Enter Code: ");
        String code = null;
        try {
            code = reader.readLine();
        } catch (IOException e) {

        } finally {
            try {
                reader.close();
            } catch (IOException e) {

            }
        }

        TLAuthorization authorization = null;
        if (sentCode.getPhoneRegistered())
            authorization = doRpc(api, new TLRequestAuthSignIn(PHONE_NUMBER,
                    sentCode.getPhoneCodeHash(), code), false);
        else
            authorization = doRpc(api, new TLRequestAuthSignUp(PHONE_NUMBER,
                    sentCode.getPhoneCodeHash(), code, "Soheil", "Tayari"), false);

        api.getState().setAuthenticated(api.getState().getPrimaryDc(), true);
        return authorization;
    }

    private static <T extends TLObject> T doRpc(TelegramApi api, TLMethod<T> tlMethod, boolean authorizationRequired) {
        DefaultAbsApiState state = (DefaultAbsApiState) api.getState();
        try {
            if (!authorizationRequired) {
                return api.doRpcCallNonAuth(tlMethod);
            } else {
                return api.doRpcCall(tlMethod);
            }
        } catch (IOException e) {
            e.printStackTrace();
            if(e instanceof RpcException) {
                int errorCode = ((RpcException) e).getErrorCode();
                String errorTag = ((RpcException) e).getErrorTag();
                if(errorCode == 303) {
                    String dcToSwitch = errorTag.substring(errorTag.length() - 1);
                    switchToDc(api, Integer.valueOf(dcToSwitch));
                }
            }
            try {
                Thread.sleep(20000);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
            // call till success
            return doRpc(api, tlMethod, authorizationRequired);
        }
    }


    private static void switchToDc(TelegramApi api, int dc) {
        try {
            api.switchToDc(dc);
        } catch (Exception e) {
            e.printStackTrace();
            switchToDc(api, dc);
        }
    }
}
