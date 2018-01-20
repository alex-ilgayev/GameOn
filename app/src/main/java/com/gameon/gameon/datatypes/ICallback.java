package com.gameon.gameon.datatypes;

import com.gameon.gameon.messaging.IMessage;

/**
 * Created by Alex on 4/11/2015.
 * Concrete classes of this interface will be passed as callback function, for even handling.
 */
public interface ICallback {
    public void receiveMessage(IMessage message);
}
