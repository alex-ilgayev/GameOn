package com.gameon.gameon.messaging;

import com.gameon.gameon.datatypes.Client;

import java.io.Serializable;
import java.util.UUID;

/**
 * Created by Alex on 7/23/2015.
 */
public class MessageRequestSms implements IMessage, Serializable{
    private static final long serialVersionUID = 1L;

    public Client client;
    public UUID id;

    public String text;

    @Override
    public MessageType getMessageType() {
        return MessageType.REQUEST_SMS;
    }

    @Override
    public Client getClient() {
        return client;
    }

    @Override
    public UUID getId(){
        return id;
    }
}
