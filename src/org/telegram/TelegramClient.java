package org.telegram;

import org.telegram.api.*;
import org.telegram.api.auth.TLAuthorization;
import org.telegram.api.auth.TLSentCode;
import org.telegram.api.engine.ApiCallback;
import org.telegram.api.engine.AppInfo;
import org.telegram.api.engine.RpcException;
import org.telegram.api.engine.TelegramApi;
import org.telegram.api.engine.storage.AbsApiState;
import org.telegram.api.messages.TLAbsSentMessage;
import org.telegram.api.messages.TLAffectedHistory;
import org.telegram.api.requests.*;
import org.telegram.api.updates.TLAbsDifference;
import org.telegram.api.updates.TLState;
import org.telegram.tl.TLMethod;
import org.telegram.tl.TLObject;

import java.io.*;
import java.util.concurrent.ThreadLocalRandom;

public class TelegramClient {

    private static TelegramApi api;
    private static final int API_ID = 33986;
    private static final String API_HASH = "cbf75f71f7b931f7d137a60d318590dd";
    private static final String PHONE_NUMBER = "+989123106718";

    public static void main(String[] args) {
        DefaultAbsApiState apiState;
        boolean stateLoaded = false;
        try {
            apiState = (DefaultAbsApiState) restoreState();
            stateLoaded = true;
        } catch (IOException | ClassNotFoundException e) {
            apiState = new DefaultAbsApiState(false);
        }

        DefaultAbsApiState finalApiState = apiState;
        api = new TelegramApi(apiState, new AppInfo(API_ID,
                "Test Client", "0.0.1", "0.0.1", "en"), new ApiCallback() {

            @Override
            public void onUpdatesInvalidated(TelegramApi _api) {
                System.out.println(_api);
            }

            @Override
            public void onAuthCancelled(TelegramApi _api) {
                System.out.println(_api);
            }

            @Override
            public void onUpdate(TLAbsUpdates updates) {
                TLState tlState = finalApiState.getTlState();
                if(updates instanceof TLUpdateShortMessage) {
                    TLUpdateShortMessage shortMessage = (TLUpdateShortMessage) updates;
                    TLAbsSentMessage tlAbsSentMessage = doRpc(new TLRequestMessagesSendMessage(new TLInputPeerContact(shortMessage.getFromId()), "Don't text me asshole.", generateRandomId()), true);
                    System.out.println(tlAbsSentMessage);
                } else if(updates instanceof TLUpdatesTooLong) {
                    TLAbsDifference tlAbsDifference = doRpc(new TLRequestUpdatesGetDifference(tlState.getPts(), tlState.getDate(), tlState.getQts()), true);
                    System.out.println(tlAbsDifference);
                } else if(updates instanceof TLUpdates) {
                    TLUpdates tlUpdates = (TLUpdates) updates;
                    for(TLAbsUpdate tlUpdate : tlUpdates.getUpdates()) {
                        if(tlUpdate instanceof TLUpdateNewMessage) {
                            TLUpdateNewMessage tlUpdateNewMessage = (TLUpdateNewMessage) tlUpdate;
                            int userId = 0;
                            if(tlUpdateNewMessage.getMessage() instanceof TLMessage) {
                                userId = ((TLMessage) tlUpdateNewMessage.getMessage()).getFromId();
                            } else if(tlUpdateNewMessage.getMessage() instanceof TLMessageForwarded) {
                                userId = ((TLMessageForwarded) tlUpdateNewMessage.getMessage()).getFromId();
                            }
                            TLAffectedHistory tlAffectedHistory = doRpc(new TLRequestMessagesReadHistory(new TLInputPeerContact(userId), tlUpdateNewMessage.getMessage().getId(), 0), true);
                            System.out.println(tlAffectedHistory);
                        }

                    }

                }
            }
        });

        if (!stateLoaded) {
            TLConfig config = doRpc(new TLRequestHelpGetConfig(), false);
            apiState.updateSettings(config);
            api.resetConnectionInfo();

            TLNearestDc tlNearestDc = doRpc(new TLRequestHelpGetNearestDc(), false);
            switchToDc(tlNearestDc.getNearestDc());

            TLAuthorization authorization = handleRegistration();
            api.getState().setAuthenticated(api.getState().getPrimaryDc(), true);
            try {
                saveState(apiState);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        TLState tlState = doRpc(new TLRequestUpdatesGetState(), true);
        apiState.setTlState(tlState);

    }

    private static void saveState(AbsApiState apiState) throws IOException {
        FileOutputStream outputStream = null;
        try {
            File stateFile = new File(System.getProperty("user.home") + "/.telegram/security", "state.ser");
            if(!stateFile.exists()) {
                stateFile.getParentFile().mkdirs();
                stateFile.createNewFile();
            }
            outputStream = new FileOutputStream(stateFile);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
            objectOutputStream.writeObject(apiState);
        } finally {
            if(outputStream != null)
                outputStream.close();
        }
    }

    private static AbsApiState restoreState() throws IOException, ClassNotFoundException {
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(System.getProperty("user.home") + "/.telegram/security/state.ser");
            ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
            return (AbsApiState) objectInputStream.readObject();
        } finally {
            if(inputStream != null)
                inputStream.close();
        }
    }

    private static TLAuthorization handleRegistration() {
        TLSentCode sentCode = doRpc(new TLRequestAuthSendCode(PHONE_NUMBER,
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
            authorization = doRpc(new TLRequestAuthSignIn(PHONE_NUMBER,
                    sentCode.getPhoneCodeHash(), code), false);
        else
            authorization = doRpc(new TLRequestAuthSignUp(PHONE_NUMBER,
                    sentCode.getPhoneCodeHash(), code, "Soheil", "Tayari"), false);

        api.getState().setAuthenticated(api.getState().getPrimaryDc(), true);
        return authorization;
    }

    private static <T extends TLObject> T doRpc(TLMethod<T> tlMethod, boolean authorizationRequired) {
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
                    switchToDc(Integer.valueOf(dcToSwitch));
                }
            }
            try {
                Thread.sleep(20000);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
            // call till success
            return doRpc(tlMethod, authorizationRequired);
        }
    }


    private static void switchToDc(int dc) {
        try {
            api.switchToDc(dc);
        } catch (Exception e) {
            e.printStackTrace();
            switchToDc(dc);
        }
    }

    private static int generateRandomId() {
        return ThreadLocalRandom.current().nextInt(8, 16 + 1);
    }
}
