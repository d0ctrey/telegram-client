package org.telegram.handler;

import org.telegram.api.*;
import org.telegram.api.engine.RpcCallbackEx;
import org.telegram.api.engine.TelegramApi;
import org.telegram.api.requests.TLRequestUsersGetFullUser;

import java.util.logging.Logger;

/**
 * Created by s_tayari on 12/24/2017.
 */
public class TLAbsUpdatesUserStatusHandler implements TLAbsUpdateHandler {

    private static final Logger LOGGER = Logger.getLogger(TLAbsUpdatesUserStatusHandler.class.getSimpleName());

    private TelegramApi api;

    public TLAbsUpdatesUserStatusHandler(TelegramApi api) {
        this.api = api;
    }

    @Override
    public boolean canProcess(int updateClassId) {
        return TLUpdateUserStatus.CLASS_ID == updateClassId;
    }

    @Override
    public void processUpdate(TLAbsUpdate update) {
        TLUpdateUserStatus updateUserStatus = (TLUpdateUserStatus) update;
        TLAbsUserStatus userStatus = updateUserStatus.getStatus();
        int userId = 0;
        boolean makeCall = false;
        String statusString = null;
        if (userStatus instanceof TLUserStatusOnline) {
            userId = updateUserStatus.getUserId();
            statusString = "online";
            makeCall = true;
        } else if (userStatus instanceof TLUserStatusOffline) {
            userId = updateUserStatus.getUserId();
            statusString = "offline";
            makeCall = true;
        }

        if (!makeCall)
            return;

        String finalStatusString = statusString;
        api.doRpcCall(new TLRequestUsersGetFullUser(new TLInputUserContact(userId)), new RpcCallbackEx<TLUserFull>() {
            @Override
            public void onConfirmed() {
            }

            @Override
            public void onResult(TLUserFull result) {
                if(result.getUser() instanceof TLUserContact)
                    System.out.println("### " + ((TLUserContact) result.getUser()).getFirstName() + " " + ((TLUserContact) result.getUser()).getLastName() + " is " + finalStatusString + " ###");
            }

            @Override
            public void onError(int errorCode, String message) {
               System.out.println(errorCode + " " + message);
            }
        });
    }
}
