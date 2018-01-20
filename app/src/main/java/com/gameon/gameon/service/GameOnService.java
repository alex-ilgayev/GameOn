package com.gameon.gameon.service;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.gameon.backend.gameonApi.GameonApi;
import com.gameon.backend.gameonApi.model.Packet;
import com.gameon.backend.gameonApi.model.PacketCollection;
import com.gameon.gameon.controller.Settings;
import com.gameon.gameon.datatypes.Client;
import com.gameon.gameon.datatypes.ConnectionStatus;
import com.gameon.gameon.datatypes.ICallback;
import com.gameon.gameon.messaging.IMessage;
import com.gameon.gameon.messaging.MessageCompression;
import com.gameon.gameon.messaging.MessageInnerConnectionStatus;
import com.gameon.gameon.messaging.MessageRequestPollMessageQueue;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.googleapis.services.AbstractGoogleClientRequest;
import com.google.api.client.googleapis.services.GoogleClientRequestInitializer;
import com.google.api.client.util.Base64;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

/**
 * Main service of the GameOn application.
 * It's design is bounding design.
 * multiple NetworkManagers instances can be bound to the service,
 * and activate polling.
 * The service will handle all it's connections, and return responses accordingly.
 */
public class GameOnService extends Service {
    private GameonApi _myApiService = null;
    private HashMap<Integer, ICallback> _callbackMap = null;
    private HashMap<Integer, pollMessageAsyncTask> _asyncTaskMap = null;
    private ConnectionStatus _currStatus = ConnectionStatus.CONNECTION_TIMED_OUT;

    private final IBinder _binder = new LocalBinder();

    @Override
    public void onCreate() {
        super.onCreate();
        if(_myApiService == null){
            GameonApi.Builder builder = new GameonApi.Builder(AndroidHttp.newCompatibleTransport(),
                    new AndroidJsonFactory(), null)
                    // options for running against local devappserver
                    // - 10.0.2.2 is localhost's IP address in Android emulator
                    // - turn off compression when running against local devappserver
                    .setRootUrl(Settings.ROOT_URL)
                    .setGoogleClientRequestInitializer(new GoogleClientRequestInitializer() {
                        @Override
                        public void initialize(AbstractGoogleClientRequest<?> abstractGoogleClientRequest) throws IOException {
                            abstractGoogleClientRequest.setDisableGZipContent(true);
                        }
                    });
            // end options for devappserver

            _myApiService = builder.build();
        }
        _callbackMap = new HashMap<Integer, ICallback>();
        _asyncTaskMap = new HashMap<Integer, pollMessageAsyncTask>();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return _binder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        for(pollMessageAsyncTask task: _asyncTaskMap.values()){
            task.cancel(true);
        }
    }

    public void startPolling(ICallback callback, Client client){
        _callbackMap.put(client.getId(), callback);
        pollMessageAsyncTask task = new pollMessageAsyncTask(client);
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        _asyncTaskMap.put(client.getId(), task);
    }

    public void stopPolling(int clientId){
        if(_callbackMap.containsKey(clientId))
            _callbackMap.remove(clientId);
        if(_asyncTaskMap.containsKey(clientId)) {
            pollMessageAsyncTask task = _asyncTaskMap.remove(clientId);
            task.cancel(true);
        }
    }

    public ConnectionStatus getCurrentConnectionStatus() {
        return _currStatus;
    }

    class pollMessageAsyncTask extends AsyncTask<Void, Object, Exception> {
        private Client _client;
        private MessageRequestPollMessageQueue _pollMsg;
        private String _payload;
        // checking for connection failure.
        private checkConnectionAsyncTask _task = null;

        protected pollMessageAsyncTask(Client client){
            _client = client;
            _pollMsg = new MessageRequestPollMessageQueue();
            _pollMsg.client = client;
            _pollMsg.id = Settings.getInstance().getPollRequestId();
            _payload = Base64.encodeBase64String(
                    MessageCompression.getInstance().compress(_pollMsg));
        }

        // if returns null then was cancelled quietly, if not then returns exception.
        @Override
        protected Exception doInBackground(Void... params) {
            try {
                while(true) {
                    if(isCancelled())
                        return null;
                    Log.d(Settings.tagLog, "Service for client: " + _client.getId() + " polling.");
                    Packet p = new Packet();
                    p.setDate(System.currentTimeMillis());
                    p.setPayload(_payload);

                    // creating new async task, if sending taking too long, sending retrying
                    // message to the activity above.
                    _task = new checkConnectionAsyncTask(_client);
                    publishProgress(_task);

                    // sending the actual packet.
                    PacketCollection pc = _myApiService.gameonApi().sendMessage(p).execute();

                    // stopping the async task.
                    _task.cancel(true);
                    _task = null;
                    List<Packet> items = pc.getItems();
                    if(items != null) {
                        for (int i = 0; i < pc.getItems().size(); i++) {
                            IMessage returnedMsg = MessageCompression.getInstance().decompress
                                    (Base64.decodeBase64(pc.getItems().get(i).getPayload()));
                            publishProgress(returnedMsg);
                        }
                    }
                    // creating inner connections status message.
                    MessageInnerConnectionStatus connMsg = new MessageInnerConnectionStatus();
                    connMsg.responseClient = _client;
                    connMsg.connStatus = ConnectionStatus.CONNECTION_OK;
                    _currStatus = ConnectionStatus.CONNECTION_OK;
                    publishProgress(connMsg);
                    if(!isCancelled())
                        Thread.sleep(Settings.POLL_TIMESTAMP);
                    else
                        return null;
                }
            } catch (InterruptedException e) {
                //Log.d(Settings.tagException, e.getMessage());
                return null;
            } catch (IOException e) {
                //Log.d(Settings.tagException, e.getMessage());
               if(_task != null)
                   _task.cancel(true);
                return e;
            }
        }

        @Override
        protected void onPostExecute(Exception exc) {
            // sending inner message of connection failure.
            if(exc != null) {
                MessageInnerConnectionStatus msg = new MessageInnerConnectionStatus();
                msg.responseClient = _client;
                msg.connStatus = ConnectionStatus.CONNECTION_TIMED_OUT;
                _currStatus = ConnectionStatus.CONNECTION_TIMED_OUT;
                if (_callbackMap.containsKey(new Integer(msg.getClient().getId())))
                    _callbackMap.get(msg.getClient().getId()).receiveMessage(msg);
            }
        }

        // can receive either async task or a IMessage.
        // if receives task, so executes the task.
        // if receives IMessage, then raising it up to the activity.
        @Override
        protected void onProgressUpdate(Object... values) {
            super.onProgressUpdate(values);
            for(Object obj: values){
                if(obj instanceof checkConnectionAsyncTask) {
                    checkConnectionAsyncTask task = (checkConnectionAsyncTask)obj;
                    task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                } else {
                    IMessage msg = (IMessage)obj;
                    if (_callbackMap.containsKey(new Integer(msg.getClient().getId())))
                        _callbackMap.get(msg.getClient().getId()).receiveMessage(msg);
                }
            }
        }
    }

    class checkConnectionAsyncTask extends AsyncTask<Void, IMessage, Void> {
        private Client _client;

        protected checkConnectionAsyncTask(Client client){
            _client = client;
        }

        // if returns null then was cancelled quietly, if not then returns exception.
        @Override
        protected Void doInBackground(Void... params) {
            try {
                Thread.sleep(Settings.DISCONNECTION_TIMESTAMP);
                if(isCancelled())
                    return null;

                // if it's not cancelled yet, sending RETRYING message to activity.
                MessageInnerConnectionStatus connMsg = new MessageInnerConnectionStatus();
                connMsg.responseClient = _client;
                connMsg.connStatus = ConnectionStatus.CONNECTION_RETRYING;
                _currStatus = ConnectionStatus.CONNECTION_RETRYING;
                publishProgress(connMsg);
                return null;
            } catch(InterruptedException e) {
                return null;
            }
        }

        @Override
        protected void onProgressUpdate(IMessage... values) {
            super.onProgressUpdate(values);
            for(IMessage msg: values){
                if (_callbackMap.containsKey(new Integer(msg.getClient().getId())))
                    _callbackMap.get(msg.getClient().getId()).receiveMessage(msg);
            }
        }
    }

    public class LocalBinder extends Binder {
        public GameOnService getService(){
            return GameOnService.this;
        }
    }
}
