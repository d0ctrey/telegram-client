package org.telegram;

import org.telegram.api.TLAbsInputUser;
import org.telegram.api.TLAbsUpdates;
import org.telegram.api.TLConfig;
import org.telegram.api.TLInputUserSelf;
import org.telegram.api.auth.TLAuthorization;
import org.telegram.api.auth.TLSentCode;
import org.telegram.api.contacts.TLFound;
import org.telegram.api.engine.ApiCallback;
import org.telegram.api.engine.AppInfo;
import org.telegram.api.engine.TelegramApi;
import org.telegram.api.requests.TLRequestAuthSendCode;
import org.telegram.api.requests.TLRequestAuthSignIn;
import org.telegram.api.requests.TLRequestContactsSearch;
import org.telegram.api.requests.TLRequestHelpGetConfig;
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
    private static final String HASH = "f593011df04fb04285";

    public static void main(String[] args) {
        DefaultAbsApiState state = new DefaultAbsApiState(false);
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

//        TLCheckedPhone checkedPhone = doRpc(false, api, new TLRequestAuthCheckPhone(PHONE_NUMBER));

//        if (checkedPhone == null)
//            throw new RuntimeException();

        String hash = HASH;
        if("".equals(HASH))
            hash = handleRegistration(api);
        else
            state.setAuthenticated(state.getPrimaryDc(), true);

        TLVector<TLAbsInputUser> tlAbsInputUsers = new TLVector<>();
        tlAbsInputUsers.add(new TLInputUserSelf());

        TLFound tlFound = doRpc(api, new TLRequestContactsSearch("fermisk", 1), false);
        tlFound.getResults().toString();
    }

    private static String handleRegistration(TelegramApi api) {
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

        TLAuthorization authorization;
        if (sentCode.getPhoneRegistered())
            doRpc(api, new TLRequestAuthSignIn(PHONE_NUMBER,
                    sentCode.getPhoneCodeHash(), code), false);

        api.getState().setAuthenticated(api.getState().getPrimaryDc(), true);
        return sentCode.getPhoneCodeHash();
    }

    private static <T extends TLObject> T doRpc(TelegramApi api, TLMethod<T> tlMethod, boolean authorizationRequired) {
        DefaultAbsApiState state = (DefaultAbsApiState) api.getState();
        T tlObject = null;
        Integer dc = state.getPrimaryDc();
        try {
            if (!authorizationRequired) {
                tlObject = api.doRpcCallNonAuth(tlMethod, dc);
            } else {
                tlObject = api.doRpcCall(tlMethod, dc);
            }
        } catch (IOException e) {
            int[] knownDcs = state.getKnownDCs();
            for (int i = 0; i < knownDcs.length; i++) {
                try {
                    if (!authorizationRequired) {
                        tlObject = api.doRpcCallNonAuth(tlMethod, knownDcs[i]);
                        dc = knownDcs[i];
                    } else {
                        tlObject = api.doRpcCall(tlMethod, knownDcs[i]);
                        dc = knownDcs[i];
                    }
                    break;
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }

        state.setPrimaryDc(dc);
        return tlObject;
    }

}
