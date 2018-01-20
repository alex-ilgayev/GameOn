package com.gameon.gameon.datatypes;

import com.gameon.gameon.sudoku.SudokuGameData;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.UUID;

/**
 * Created by Alex on 7/19/2015.
 */
public class Session implements Serializable{
    private static final long serialVersionUID = 1L;

    private UUID _sessionId;
    private LinkedList<Client> _clientList;
    private GameType _gameType;
    private SudokuGameData _gameData;

    public Session(UUID sessionId, LinkedList<Client> clientList, GameType gameType
            , SudokuGameData gameData){
        this._sessionId = sessionId;
        this._clientList = clientList;
        this._gameType = gameType;
        this._gameData = gameData;
    }

    public Session(UUID sessionId, GameType gameType, SudokuGameData gameData){
        this(sessionId, new LinkedList<Client>(), gameType, gameData);
    }

    public Session(GameType gameType, SudokuGameData gameData){
        this(UUID.randomUUID(), gameType, gameData);
    }

    public UUID getSessionId(){
        return _sessionId;
    }

    public LinkedList<Client> getClientList(){
        return _clientList;
    }

    public GameType getGameType() {
        return _gameType;
    }

    public SudokuGameData getGameData() {
        return _gameData;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Session session = (Session) o;

        return _sessionId.equals(session._sessionId);
    }

    @Override
    public int hashCode() {
        return _sessionId.hashCode();
    }
}
