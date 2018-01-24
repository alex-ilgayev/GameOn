package com.gameon.shared.messaging;

import com.gameon.shared.datatypes.Client;
import com.gameon.shared.sudoku.datatypes.SudokuMove;

import java.io.Serializable;
import java.util.UUID;

/**
 * Created by Alex on 4/28/2015.
 */
public class MessageRequestSetMove implements IMessage, Serializable{
    private static final long serialVersionUID = 1L;

    public Client client;
    public UUID id;

    public SudokuMove move;

    @Override
    public MessageType getMessageType() {
        return MessageType.REQUEST_SET_MOVE;
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
