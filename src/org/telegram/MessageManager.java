package org.telegram;

import org.telegram.api.*;
import org.telegram.api.engine.RpcCallbackEx;
import org.telegram.api.engine.TelegramApi;
import org.telegram.api.messages.TLAffectedHistory;
import org.telegram.api.requests.TLRequestMessagesReadHistory;

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
        TLAbsInputPeer absInputPeer = null;
        if (absMessage instanceof TLMessage) {
            TLMessage message = (TLMessage) absMessage;
            if(message.getToId() instanceof TLPeerUser)
                return;
//                absInputPeer = new TLInputPeerContact(message.getFromId());
            else if(message.getToId() instanceof TLPeerChat) {
                if(((TLPeerChat) message.getToId()).getChatId() != 240638145)
                    return;
                absInputPeer = new TLInputPeerChat(((TLPeerChat) message.getToId()).getChatId());
            }
        } else if (absMessage instanceof TLMessageForwarded) {
            TLMessageForwarded messageForwarded = (TLMessageForwarded) absMessage;
            if(messageForwarded.getToId() instanceof TLPeerUser)
                return;
//                absInputPeer = new TLInputPeerContact(messageForwarded.getFromId());
            else if(messageForwarded.getToId() instanceof TLPeerChat) {
                if(((TLPeerChat) messageForwarded.getToId()).getChatId() != 240638145)
                    return;
                absInputPeer = new TLInputPeerChat(((TLPeerChat) messageForwarded.getToId()).getChatId());
            }
        } 

        if (absInputPeer != null)
            readHistory(absMessage.getId(), absInputPeer, 0);

    }

    private void readHistory(int messageId, TLAbsInputPeer inputPeer, int offset) {
        api.doRpcCall(new TLRequestMessagesReadHistory(inputPeer, messageId, offset), new RpcCallbackEx<TLAffectedHistory>() {
            @Override
            public void onConfirmed() {

            }

            @Override
            public void onResult(TLAffectedHistory result) {
                if(result.getOffset() != 0)
                    readHistory(messageId, inputPeer, result.getOffset());
            }

            @Override
            public void onError(int errorCode, String message) {

            }
        });
    }
}
