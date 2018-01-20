package com.gameon.gameon.messaging;

import com.gameon.gameon.datatypes.Client;

import java.io.Serializable;
import java.util.UUID;

/**
 * Created by Alex on 7/23/2015.
 */
public class MessageResponseSms implements IMessage, Serializable{
    private static final long serialVersionUID = 1L;

    public Client responseClient;
    public UUID responseId;

    public String name;
    public String text;

    @Override
    public MessageType getMessageType() {
        return MessageType.RESPONSE_SMS;
    }

    @Override
    public Client getClient() {
        return responseClient;
    }

    @Override
    public UUID getId() {
        return responseId;
    }
}
