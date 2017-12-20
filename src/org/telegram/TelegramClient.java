package org.telegram;

import org.telegram.api.*;
import org.telegram.api.auth.TLAuthorization;
import org.telegram.api.auth.TLCheckedPhone;
import org.telegram.api.auth.TLSentCode;
import org.telegram.api.engine.ApiCallback;
import org.telegram.api.engine.AppInfo;
import org.telegram.api.engine.TelegramApi;
import org.telegram.api.messages.TLAbsDialogs;
import org.telegram.api.messages.TLAbsSentMessage;
import org.telegram.api.requests.*;
import org.telegram.tl.TLMethod;
import org.telegram.tl.TLObject;
import org.telegram.tl.TLVector;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class TelegramClient {

    private static final int API_ID = 33986;
    private static final String API_HASH = "cbf75f71f7b931f7d137a60d318590dd";
    private static final String PHONE_NUMBER = "+989123106718";
    private static final int myId = 106549455;
//    private static final String HASH = "f593011df04fb04285";
    private static final String HASH = "b3007393d6b530021c";
    private static byte[] key;

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

        boolean synced = false;
        TLConfig config = doRpc(api, new TLRequestHelpGetConfig(), false);
        state.updateSettings(config);
        api.resetConnectionInfo();

        TLCheckedPhone checkedPhone = doRpc(api, new TLRequestAuthCheckPhone(PHONE_NUMBER), false);

//        if (checkedPhone == null)
//            throw new RuntimeException();

//        String hash = HASH;
//        if("".equals(HASH))
        if(key != null) {
            state.putAuthKey(state.getPrimaryDc(), key);
        } else {
            TLAuthorization authorization = handleRegistration(api);
            key = state.getAuthKey(state.getPrimaryDc());
        }

        TLVector<TLAbsInputUser> tlAbsInputUsers = new TLVector<>();

        TLAbsDialogs tlAbsDialogs = doRpc(api, new TLRequestContactsGetContacts(), true);
        System.out.println(tlAbsDialogs.getChats().toString());
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
        T tlObject = null;
        try {
            if (!authorizationRequired) {
                tlObject = api.doRpcCallNonAuth(tlMethod);
            } else {
                tlObject = api.doRpcCall(tlMethod);
            }
        } catch (IOException e) {
            int[] knownDcs = state.getKnownDCs();
            for (int i = 0; i < knownDcs.length; i++) {
                if(knownDcs[i] == state.getPrimaryDc())
                    continue;
                try {
                    api.switchToDc(knownDcs[i]);
                } catch (Exception e1) {
//                    api.switchToDc(knownDcs[i]);
                }
                try {
                    if (!authorizationRequired) {
                        tlObject = api.doRpcCallNonAuth(tlMethod);
                    } else {
                        tlObject = api.doRpcCall(tlMethod);
                    }
                    break;
                } catch (IOException e1) {

                }
            }
        }

        return tlObject;
    }

}
