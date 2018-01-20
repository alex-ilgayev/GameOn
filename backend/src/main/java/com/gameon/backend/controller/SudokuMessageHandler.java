package com.gameon.backend.controller;

import com.gameon.backend.networkingtypes.Packet;
import com.gameon.gameon.datatypes.Client;
import com.gameon.gameon.datatypes.Session;
import com.gameon.gameon.messaging.MessageCompression;
import com.gameon.gameon.messaging.IMessage;
import com.gameon.gameon.messaging.MessageRequestAvailableClients;
import com.gameon.gameon.messaging.MessageRequestJoin;
import com.gameon.gameon.messaging.MessageRequestNewGame;
import com.gameon.gameon.messaging.MessageRequestPollMessageQueue;
import com.gameon.gameon.messaging.MessageRequestSetMove;
import com.gameon.gameon.messaging.MessageRequestSms;

import java.util.LinkedList;

/**
 * Created by Alex on 4/28/2015.
 *
 * Receives packet, deserialize to a message, and calls the appropriate
 * function in SudokuServerGameManager.
 */
public class SudokuMessageHandler {
    private static long _LAST_TIME_CLEANED_QUEUES = System.currentTimeMillis();

    private static SudokuMessageHandler _ins = null;

    private SudokuMessageHandler(){

    }

    public static SudokuMessageHandler getInstance(){
        if(_ins == null)
            _ins = new SudokuMessageHandler();
        return _ins;
    }

    public Packet[] handleMessage(byte[] payload, long date){
        IMessage msg = MessageCompression.getInstance().decompress(payload);
        // creating new client in required lists.
        //TODO
        System.out.println("handling message number: " + msg.getMessageType());
        cleanQueues();
        handleClient(msg.getClient());
        // clean queues once a while. when someone polling clean queues after polling (not to lose messages)
        // when sending message first cleaning then sending (not to receive incorrect information).
        try {
            switch (msg.getMessageType()){
                case REQUEST_POLL_MESSAGE_QUEUE:
                    Packet[] result =  pollMessageQueue((MessageRequestPollMessageQueue)msg);
                    return result;
                case REQUEST_AVAILABLE_CLIENTS:
                    SudokuServerGameManager.getInstance().getUsers((MessageRequestAvailableClients)msg);
                    break;
                case REQUEST_JOIN:
                    SudokuServerGameManager.getInstance().askJoinGame((MessageRequestJoin) msg);
                    break;
                case REQUEST_SET_MOVE:
                    SudokuServerGameManager.getInstance().sendSetMoveToGame((MessageRequestSetMove)msg);
                    break;
                case REQUEST_SMS:
                    SudokuServerGameManager.getInstance().sendTextMessage((MessageRequestSms)msg);
                    break;
                case REQUEST_NEW_GAME:
                    SudokuServerGameManager.getInstance().startNewGame((MessageRequestNewGame)msg);
                    break;
            }
            // INSERT TO DB.
        } catch(Exception e) {
            System.out.println("ERROR !!!");
            return new Packet[0];
        }
        return new Packet[0];
    }

    private Packet[] pollMessageQueue(MessageRequestPollMessageQueue msg){
        // adding the client to the session pool.
//        UUID sessionId = msg.client.getGameId();
//        if(sessionId != null) {
//            Session session = TemporaryDB.getInstance().findSession(sessionId);
//            if(session == null) { // session exist.
//                TemporaryDB.getInstance().addAndReplaceSession(new Session(sessionId, new LinkedList<Client>()));
//            }
//            if(!session.getClientList().contains(msg.client))
//                session.getClientList().add(msg.client);
//        }

        if(msg.client == null || msg.id == null)
            return new Packet[0];

        Packet[] packets = MessageQueues.getInstance().getAwaitingPackets(msg.getClient(), msg.getId());
        return packets;
    }

    private void handleClient(Client client){

        MessageQueues.getInstance().addAndReplaceClient(client);

        //TODO:
        // change logic.
        // int his logic the client decides in which session he belongs to.
        // checking the validity of the session db on the server. (and update accordingly)
        for(Session session: TemporaryDB.getInstance().getAllSessions()) {
            if(session.getClientList().contains(client)) {
                // if another session contains that client. (or no session at all)
                if(client.getCurrSessionId() == null ||
                        (client.getCurrSessionId() != null &&
                        !client.getCurrSessionId().equals(session.getSessionId()))) {
                    //TODO
                    System.out.println("remove client: " + client.getName() + " from: " +
                    session.getSessionId() + " to: ");
                    if(client.getCurrSessionId() == null)
                        System.out.println(" NONE");
                    else
                        System.out.println(client.getCurrSessionId());
                    session.getClientList().remove(client);
                }
            }
        }

        if(client.getCurrSessionId() == null)
            return;
        // now updating the session data
        Session session = TemporaryDB.getInstance().findSession(client.getCurrSessionId());
        if(session == null) {
            //TODO:
//            System.out.println("creating new session");
//            session = new Session(client.getCurrSession().getSessionId(), client.getCurrSession().getGameType());
//            session.getClientList().add(client);
//            TemporaryDB.getInstance().addAndReplaceSession(session);
            System.out.println("ERROR !");
            return;
        }

        LinkedList<Client> clients = session.getClientList();
        if(!clients.contains(client)) {
            System.out.println("Adding client to a active session");
            System.out.println("the client is active in :" + client.getCurrSessionId() + " session");
            System.out.println("Now there is " + clients.size() + " clients in the session");
            System.out.println("Session id: " + session.getSessionId() + " and client id: " + client.getId());
            clients.add(client);
        }
    }

    // clean the information once a "TIME_TO_CLEAN_QUEUES_MILLIS" time.
    // remove unreporting clients.
    // after removing all clients from the session,
    // removing empty sessions.
    private void cleanQueues(){
        long currTime = System.currentTimeMillis();
        if((currTime - _LAST_TIME_CLEANED_QUEUES) > Settings._TIME_TO_CLEAN_QUEUE_MILLIS){
            MessageQueues.getInstance().removeOldQueues();
            for(Session session: TemporaryDB.getInstance().getAllSessions()) {
                if(session.getClientList().size() == 0) {
                    System.out.println("removing empty session");
                    TemporaryDB.getInstance().removeSession(session);
                }
            }
            _LAST_TIME_CLEANED_QUEUES = currTime;
        }
    }
}
