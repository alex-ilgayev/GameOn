package com.gameon.shared.datatypes;

import com.gameon.shared.messaging.IMessage;

/**
 * Created by Alex on 4/11/2015.
 * Concrete classes of this interface will be passed as callback function, for even handling.
 */
public interface ICallback {
    public void receiveMessage(IMessage message);
}
