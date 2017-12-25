package org.telegram.handler;

import org.telegram.MessageManager;
import org.telegram.api.*;
import org.telegram.api.engine.TelegramApi;

import java.util.logging.Logger;

/**
 * Created by s_tayari on 12/24/2017.
 */
public class TLUpdateNewMessageHandler implements TLAbsUpdateHandler {

    private static final Logger LOGGER = Logger.getLogger(TLUpdateNewMessageHandler.class.getSimpleName());

    private TelegramApi api;
    private MessageManager messageManager;

    public TLUpdateNewMessageHandler(TelegramApi api) {
        this.api = api;
        messageManager = new MessageManager(api);
    }

    @Override
    public boolean canProcess(int updateClassId) {
        return TLUpdateNewMessage.CLASS_ID == updateClassId;
    }

    @Override
    public void processUpdate(TLAbsUpdate update) {
        TLUpdateNewMessage tlUpdateNewMessage = (TLUpdateNewMessage) update;
        messageManager.markAsRead(tlUpdateNewMessage.getMessage());


    }
}
