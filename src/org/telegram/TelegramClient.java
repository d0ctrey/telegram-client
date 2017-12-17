package org.telegram;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.telegram.api.*;
import org.telegram.api.auth.TLAuthorization;
import org.telegram.api.auth.TLCheckedPhone;
import org.telegram.api.auth.TLSentCode;
import org.telegram.api.contacts.TLImportedContacts;
import org.telegram.api.engine.ApiCallback;
import org.telegram.api.engine.AppInfo;
import org.telegram.api.engine.RpcCallback;
import org.telegram.api.engine.TelegramApi;
import org.telegram.api.requests.*;
import org.telegram.tl.TLVector;

public class TelegramClient {

    private static final int API_ID = 33986;
    private static final String API_HASH = "cbf75f71f7b931f7d137a60d318590dd";
    private static final String PHONE_NUMBER = "+989123106718";

    public static void main(String[] args) {
        DefaultAbsApiState state = new DefaultAbsApiState();
        final TelegramApi api = new TelegramApi(state, new AppInfo(12345,
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

        try {
            TLConfig config = api
                    .doRpcCallNonAuth(new TLRequestHelpGetConfig());
            state.updateSettings(config);
            api.doRpcCallNonAuth(new TLRequestAuthCheckPhone(PHONE_NUMBER), 15000, new RpcCallback<TLCheckedPhone>() {
                        @Override
                        public void onResult(TLCheckedPhone tlCheckedPhone) {

                        }

                        @Override
                        public void onError(int i, String s) {
                            api.switchToDc(4);
                        }
                    });
/*            TLSentCode sentCode = api
                    .doRpcCallNonAuth(new TLRequestAuthSendCode(PHONE_NUMBER,
                            0, API_ID, API_HASH, "en"));
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    System.in));
            System.out.println("Enter Code: ");
            String code = reader.readLine();
            TLAuthorization authorization;
            if (!checkedPhone.getPhoneRegistered()) {
                authorization = api.doRpcCallNonAuth(new TLRequestAuthSignUp(PHONE_NUMBER,
                        sentCode.getPhoneCodeHash(), code, "Soheil", "Tayari"));
            } else {
                authorization = api
                        .doRpcCallNonAuth(new TLRequestAuthSignIn(PHONE_NUMBER,
                                sentCode.getPhoneCodeHash(), code));
            }

            state.setAuthenticated(state.getPrimaryDc(), true);
            TLVector<TLInputContact> contacts = new TLVector<>();
            TLImportedContacts importedContacts = api.doRpcCallSide(new TLRequestContactsImportContacts(contacts, true));
            //TLAbsStatedMessage statedMessage = api.doRpcCallSide(new TLRequestMessagesCreateChat(users, "a Chat"));
            System.out.println(importedContacts);*/
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
