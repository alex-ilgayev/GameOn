package com.gameon.shared.messaging;

import com.gameon.shared.datatypes.Client;
import com.gameon.shared.datatypes.GameType;
import com.gameon.shared.datatypes.DifficultyType;

import java.io.Serializable;
import java.util.UUID;

/**
 * Created by Alex on 4/28/2015.
 */
public class MessageRequestNewGame implements IMessage, Serializable{
    private static final long serialVersionUID = 1L;

    public Client client;
    public UUID id;

    public GameType gameType;
    public DifficultyType difficultyType;

    @Override
    public MessageType getMessageType() {
        return MessageType.REQUEST_NEW_GAME;
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
