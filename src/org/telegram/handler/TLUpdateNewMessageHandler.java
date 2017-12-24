package org.telegram.handler;

import org.telegram.api.*;
import org.telegram.api.engine.RpcCallbackEx;
import org.telegram.api.engine.TelegramApi;
import org.telegram.api.messages.TLAffectedHistory;
import org.telegram.api.requests.TLRequestMessagesReadHistory;

import java.util.logging.Logger;

/**
 * Created by s_tayari on 12/24/2017.
 */
public class TLUpdateNewMessageHandler implements TLAbsUpdateHandler {

    private static final Logger LOGGER = Logger.getLogger(TLUpdateNewMessageHandler.class.getSimpleName());

    private TelegramApi api;

    public TLUpdateNewMessageHandler(TelegramApi api) {
        this.api = api;
    }

    @Override
    public boolean canProcess(int updateClassId) {
        return TLUpdateNewMessage.CLASS_ID == updateClassId;
    }

    @Override
    public void processUpdate(TLAbsUpdate update) {
        TLUpdateNewMessage tlUpdateNewMessage = (TLUpdateNewMessage) update;
        int userId = 0;
        if(tlUpdateNewMessage.getMessage() instanceof TLMessage) {
            userId = ((TLMessage) tlUpdateNewMessage.getMessage()).getFromId();
        } else if(tlUpdateNewMessage.getMessage() instanceof TLMessageForwarded) {
            userId = ((TLMessageForwarded) tlUpdateNewMessage.getMessage()).getFromId();
        }

        api.doRpcCall(new TLRequestMessagesReadHistory(new TLInputPeerContact(userId), tlUpdateNewMessage.getMessage().getId(), 0), new RpcCallbackEx<TLAffectedHistory>() {
            @Override
            public void onConfirmed() {

            }

            @Override
            public void onResult(TLAffectedHistory result) {
                // TODO: 12/24/2017 what to do here?
                System.out.println(result);
            }

            @Override
            public void onError(int errorCode, String message) {

            }
        });
    }
}
