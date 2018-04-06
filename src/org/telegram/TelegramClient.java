package org.telegram;

import org.telegram.api.*;
import org.telegram.api.auth.TLAuthorization;
import org.telegram.api.auth.TLSentCode;
import org.telegram.api.engine.*;
import org.telegram.api.messages.TLAbsDialogs;
import org.telegram.api.requests.*;
import org.telegram.api.updates.TLAbsDifference;
import org.telegram.api.updates.TLState;
import org.telegram.handler.TLAbsUpdatesHandler;
import org.telegram.handler.TLUpdateShortHandler;
import org.telegram.handler.TLUpdatesHandler;
import org.telegram.handler.TLUpdatesTooLongHandler;
import org.telegram.tl.TLMethod;
import org.telegram.tl.TLObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class TelegramClient {

    static TelegramApi api;
    static List<TLAbsUpdatesHandler> updatesHandlers = new ArrayList<>();

    private static final int API_ID = 33986;
    private static final String API_HASH = "cbf75f71f7b931f7d137a60d318590dd";
    private static final String PHONE_NUMBER = "+989123106718";

    private static ScheduledExecutorService executorService;

    public static class DefaultApiCallback implements ApiCallback {

        @Override
        public void onUpdatesInvalidated(TelegramApi _api) {
        }

        @Override
        public void onAuthCancelled(TelegramApi _api) {
        }

        @Override
        public void onUpdate(TLAbsUpdates updates) {
//            processUpdates(updates);
        }
    }

    public static void main(String[] args) {
        ApiStorage apiStorage = new ApiStorage(PHONE_NUMBER.replaceAll("\\+", ""));
        api = new TelegramApi(apiStorage, new AppInfo(API_ID,
                System.getenv("TL_DEVICE_MODEL"), System.getenv("TL_DEVICE_VERSION"), "0.0.1", "en"), new DefaultApiCallback());

        if (!apiStorage.isAuthenticated()) {
            TLConfig config = doRpc(new TLRequestHelpGetConfig(), false);
            apiStorage.updateSettings(config);
            api.resetConnectionInfo();

            TLNearestDc tlNearestDc = doRpc(new TLRequestHelpGetNearestDc(), false);
            switchToDc(tlNearestDc.getNearestDc());

            TLAuthorization authorization = handleRegistration();
            apiStorage.doAuth(authorization);
            apiStorage.setAuthenticated(api.getState().getPrimaryDc(), true);

        }

        ApiState apiState = new ApiState(PHONE_NUMBER.replaceAll("\\+", ""));
        if (apiState.getObj().getDate() == 0) {
            try {
                TLState tlState = api.doRpcCall(new TLRequestUpdatesGetState());
                apiState.updateState(tlState);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        String hash;
        if("https://telegram.me/joinchat/Cs1ppj6BBdQNe9LTcafLqg".startsWith("https://telegram.me/joinchat/"))
            hash = "https://telegram.me/joinchat/Cs1ppj6BBdQNe9LTcafLqg".substring("https://telegram.me/joinchat/".length() - 1);
//        doRpc(new TLRequestUsersGetFullUser(new TLInputUserForeign());
        api.doRpcCall(new TLRequestMessagesGetDialogs(0, Integer.MAX_VALUE, 100), new RpcCallbackEx<TLAbsDialogs>() {
            @Override
            public void onConfirmed() {

            }

            @Override
            public void onResult(TLAbsDialogs result) {
                System.out.println(result);
            }

            @Override
            public void onError(int errorCode, String message) {

            }
        });

        /*updatesHandlers.add(new TLUpdatesHandler(api));
        updatesHandlers.add(new TLUpdateShortHandler(api));
        updatesHandlers.add(new TLUpdatesTooLongHandler(api));
        // check for updates since restart
        processUpdates(new TLUpdatesTooLong());

        executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(() -> processUpdates(new TLUpdatesTooLong()), 5, 30, TimeUnit.SECONDS);*/
    }

    private synchronized static void processUpdates(TLAbsUpdates updates) {
        for (TLAbsUpdatesHandler updatesHandler : updatesHandlers) {
            if (updatesHandler.canProcess(updates.getClassId()))
                updatesHandler.processUpdates(updates);
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
                e.printStackTrace();
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
        try {
            if (!authorizationRequired) {
                return api.doRpcCallNonAuth(tlMethod);
            } else {
                return api.doRpcCall(tlMethod);
            }
        } catch (IOException e) {
            e.printStackTrace();
            if (e instanceof RpcException) {
                int errorCode = ((RpcException) e).getErrorCode();
                String errorTag = ((RpcException) e).getErrorTag();
                if (errorCode == 303) {
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
