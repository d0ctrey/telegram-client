package org.telegram;

import org.telegram.api.*;
import org.telegram.api.engine.RpcCallbackEx;
import org.telegram.api.engine.TelegramApi;
import org.telegram.api.messages.TLAffectedHistory;
import org.telegram.api.requests.TLRequestMessagesReadHistory;
import org.telegram.handler.TLUpdatesTooLongHandler;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * Created by Soheil on 12/24/17.
 */
public class MessageManager {

    private static final Logger LOGGER = Logger.getLogger(MessageManager.class.getSimpleName());

    private TelegramApi api;

    public MessageManager(TelegramApi api) {
        this.api = api;
    }

    public void markAsRead(TLAbsMessage absMessage) {
        int userId = 0;
        if (absMessage instanceof TLMessage) {
            userId = ((TLMessage) absMessage).getFromId();
        } else if (absMessage instanceof TLMessageForwarded) {
            userId = ((TLMessageForwarded) absMessage).getFromId();
        } 

//        if (userId != 0)
//            readHistory(absMessage, userId, 0);

    }

    private void readHistory(TLAbsMessage absMessage, int userId, int offset) {
        api.doRpcCall(new TLRequestMessagesReadHistory(new TLInputPeerContact(userId), absMessage.getId(), offset), new RpcCallbackEx<TLAffectedHistory>() {
            @Override
            public void onConfirmed() {

            }

            @Override
            public void onResult(TLAffectedHistory result) {
                if(result.getOffset() != 0)
                    readHistory(absMessage, userId, result.getOffset());
            }

            @Override
            public void onError(int errorCode, String message) {

            }
        });
    }
}
