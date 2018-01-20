package com.gameon.backend.controller;

import com.gameon.gameon.datatypes.Client;
import com.gameon.gameon.datatypes.Session;


import java.util.LinkedList;
import java.util.UUID;

/**
 * Created by Alex on 4/28/2015.
 */
public class TemporaryDB {
    private static TemporaryDB _ins = null;

    private LinkedList<Client> _clients;
    private LinkedList<Session> _sessions;

    private TemporaryDB(){
        _clients = new LinkedList<Client>();
        _sessions = new LinkedList<Session>();
    }

    public static TemporaryDB getInstance(){
        if(_ins == null)
            _ins = new TemporaryDB();
        return _ins;
    }

    public Client[] getPlayingClients(){
        if(_clients.size() == 0)
            return new Client[0];
        LinkedList<Client> clone = new LinkedList<Client>();
        for(Client c: _clients){
            if(c.getCurrSessionId() != null)
                clone.add(c);
        }
        return clone.toArray(new Client[0]);
    }

    public void addAndReplaceClient(Client client){
        if(_clients.contains(client)){
            _clients.remove(client);
        }
        _clients.add(client);
    }

    public void removeClient(Client client){
        if(_clients.contains(client)) {
            _clients.remove(client);
        }

        for(Session session: _sessions){
            if(session.getClientList().contains(client))
                session.getClientList().remove(client);
        }
    }

    public Client findClient(int clientId){
        for(Client client: _clients)
            if(client.getId() == clientId)
                return client;
        return null;
    }

    public void addAndReplaceSession(Session session){
        if(_sessions.contains(session)){
            _sessions.remove(session);
        }
        _sessions.add(session);
    }

    public void removeSession(Session session){
        if(_sessions.contains(session)) {
            _sessions.remove(session);
        }
    }

    public Session findSession(UUID sessionId){
        for(Session session: _sessions) {
            if (session.getSessionId().equals(sessionId))
                return session;
        }
        return null;
    }

    public Session[] getAllSessions(){
        if(_sessions.size() == 0)
            return new Session[0];
        return _sessions.toArray(new Session[0]);
    }

//    /**
//     * Search for specific client out of client pool.
//     *
//     * @param clientId
//     * @return return the client, or null otherwise.
//     */
//    public Client findClient(int clientId){
//        for(Client client: _clients){
//            if(client.getId() == clientId)
//                return client;
//        }
//        return null;
//    }
}
