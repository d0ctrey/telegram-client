package org.telegram.handler;

import org.telegram.DefaultAbsApiState;
import org.telegram.api.TLAbsMessage;
import org.telegram.api.TLAbsUpdates;
import org.telegram.api.TLUpdateNewMessage;
import org.telegram.api.TLUpdatesTooLong;
import org.telegram.api.engine.RpcCallbackEx;
import org.telegram.api.engine.TelegramApi;
import org.telegram.api.requests.TLRequestUpdatesGetDifference;
import org.telegram.api.requests.TLRequestUpdatesGetState;
import org.telegram.api.updates.TLAbsDifference;
import org.telegram.api.updates.TLDifferenceSlice;
import org.telegram.api.updates.TLState;
import org.telegram.tl.TLVector;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by s_tayari on 12/24/2017.
 */
public class TLUpdatesTooLongHandler implements TLAbsUpdatesHandler {

    private static final Logger LOGGER = Logger.getLogger(TLUpdatesTooLongHandler.class.getSimpleName());

    private List<TLAbsUpdateHandler> updateHandlers = new ArrayList<>();
    private TelegramApi api;

    public TLUpdatesTooLongHandler(TelegramApi api) {
        this.api = api;
        updateHandlers.add(new TLUpdateNewMessageHandler(api));
    }

    @Override
    public boolean canProcess(int updateClassId) {
        return TLUpdatesTooLong.CLASS_ID == updateClassId;
    }

    @Override
    public void processUpdates(TLAbsUpdates updates) {
        DefaultAbsApiState state = (DefaultAbsApiState) api.getState();
        TLState tlState = state.getTlState();
        if(tlState == null) {
            try {
                tlState = api.doRpcCall(new TLRequestUpdatesGetState());
            } catch (IOException e) {
                LOGGER.severe("Failed to get the state from server.");
                return;
            }
        }

        api.doRpcCall(new TLRequestUpdatesGetDifference(tlState.getPts(), tlState.getDate(), tlState.getQts()), new RpcCallbackEx<TLAbsDifference>() {
            @Override
            public void onConfirmed() {

            }

            @Override
            public void onResult(TLAbsDifference result) {
                // TODO: 12/24/2017 what to do with the result?
                if(result instanceof TLDifferenceSlice) {
                    ((DefaultAbsApiState) api.getState()).setTlState(((TLDifferenceSlice) result).getIntermediateState());
                    TLVector<TLAbsMessage> newMessages = ((TLDifferenceSlice) result).getNewMessages();
                    for(TLAbsMessage newMessage : newMessages) {
                        TLUpdateNewMessage tlUpdateNewMessage = new TLUpdateNewMessage(newMessage, 0);
                        for (TLAbsUpdateHandler updateHandler : updateHandlers) {
                            if(updateHandler.canProcess(tlUpdateNewMessage.getClassId()))
                                updateHandler.processUpdate(tlUpdateNewMessage);
                        }
                    }
                }
            }

            @Override
            public void onError(int errorCode, String message) {

            }
        });

    }
}
