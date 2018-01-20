package com.gameon.gameon.messaging;

import com.gameon.gameon.datatypes.Client;

import java.util.UUID;

/**
 * Created by Alex on 4/10/2015.
 * Abstraction of all the messages in this program. (inner, and outer)
 */
public interface IMessage {
    MessageType getMessageType();
    Client getClient();
    UUID getId();
}
