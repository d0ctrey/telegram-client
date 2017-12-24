package org.telegram.handler;

import org.telegram.api.TLAbsUpdate;
import org.telegram.api.TLAbsUpdates;
import org.telegram.api.TLUpdates;
import org.telegram.api.engine.TelegramApi;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by s_tayari on 12/24/2017.
 */
public class TLUpdatesHandler implements TLAbsUpdatesHandler {

    private static final Logger LOGGER = Logger.getLogger(TLUpdatesHandler.class.getSimpleName());

    private List<TLAbsUpdateHandler> updateHandlers = new ArrayList<>();
    private TelegramApi api;

    public TLUpdatesHandler(TelegramApi api) {
        this.api = api;
        updateHandlers.add(new TLUpdateNewMessageHandler(api));
    }

    @Override
    public boolean canProcess(int updateClassId) {
        return TLUpdates.CLASS_ID == updateClassId;
    }

    @Override
    public void processUpdates(TLAbsUpdates updates) {
        TLUpdates tlUpdates = (TLUpdates) updates;
        for(TLAbsUpdate update : tlUpdates.getUpdates()) {
            for (TLAbsUpdateHandler updateHandler : updateHandlers) {
                if(updateHandler.canProcess(update.getClassId()))
                    updateHandler.processUpdate(update);
            }
        }
    }
}
