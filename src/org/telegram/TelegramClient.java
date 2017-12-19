package org.telegram;

import org.telegram.api.TLAbsUpdates;
import org.telegram.api.TLConfig;
import org.telegram.api.TLInputContact;
import org.telegram.api.auth.TLAuthorization;
import org.telegram.api.auth.TLCheckedPhone;
import org.telegram.api.auth.TLSentCode;
import org.telegram.api.contacts.TLImportedContacts;
import org.telegram.api.engine.ApiCallback;
import org.telegram.api.engine.AppInfo;
import org.telegram.api.engine.TelegramApi;
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
    private static final String PHONE_NUMBER = "+8613024680741";

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
        TLConfig config = doRpc(api, new TLRequestHelpGetConfig());
        state.updateSettings(config);
        api.resetConnectionInfo();

        TLCheckedPhone checkedPhone = doRpc(api, new TLRequestAuthCheckPhone(PHONE_NUMBER));

        if(checkedPhone == null)
            throw new RuntimeException();

        TLSentCode sentCode = doRpc(api, new TLRequestAuthSendCode(PHONE_NUMBER,
                5, API_ID, API_HASH, "en"));
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
        if (checkedPhone.getPhoneRegistered())
            doRpc(api, new TLRequestAuthSignIn(PHONE_NUMBER,
                    sentCode.getPhoneCodeHash(), code));


        state.setAuthenticated(state.getPrimaryDc(), true);
        TLVector<TLInputContact> contacts = new TLVector<>();
        TLImportedContacts importedContacts = doRpc(api, new TLRequestContactsImportContacts(contacts, true));
        //TLAbsStatedMessage statedMessage = api.doRpcCallSide(new TLRequestMessagesCreateChat(users, "a Chat"));
        System.out.println(importedContacts);
    }

    private static  <T extends TLObject> T doRpc(TelegramApi api, TLMethod<T> tlMethod) {
        DefaultAbsApiState state = (DefaultAbsApiState) api.getState();
        T tlObject = null;
        try {
            tlObject = api.doRpcCallNonAuth(tlMethod);
        } catch (IOException e) {
            int[] knownDcs = state.getKnownDCs();
            for (int i = 0; i < knownDcs.length; i++) {
                try {
                    tlObject = api.doRpcCallNonAuth(tlMethod, knownDcs[i]);
                    break;
                } catch (IOException e1) {
                }
            }
        }

        return tlObject;
    }

}
