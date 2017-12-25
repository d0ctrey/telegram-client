package org.telegram;

import org.telegram.api.updates.TLState;

import java.util.Date;

/**
 * Created by Soheil on 12/25/17.
 */
public class ApiState extends TLPersistence<TLState> {

    private static final String STATE_FILE_NAME = "state.bin";

    public ApiState() {
        super(STATE_FILE_NAME, TLState.class);
    }

    public void updateState(TLState tlState) {
        getObj().setDate(tlState.getDate());
        getObj().setPts(tlState.getPts());
        getObj().setQts(tlState.getQts());
        getObj().setSeq(tlState.getSeq());
        getObj().setUnreadCount(tlState.getUnreadCount());
        super.write(STATE_FILE_NAME);
    }

}
