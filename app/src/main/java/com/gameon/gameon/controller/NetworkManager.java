package com.gameon.gameon.controller;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;

import com.gameon.gameon.datatypes.Client;
import com.gameon.gameon.datatypes.ConnectionStatus;
import com.gameon.gameon.datatypes.ICallback;
import com.gameon.gameon.messaging.IMessage;
import com.gameon.gameon.messaging.MessageCompression;
import com.gameon.gameon.service.GameOnService;

import java.io.IOException;

/**
 * Created by Alex on 4/11/2015.
 * The manager of all the networking operation for all games.
 */
public class NetworkManager {

//    private GameonApi _myApiService = null;
    private GameOnService _service = null;
    private boolean _isBoundToService = false;
    private ICallback _eventToGameManager = null;
    private Context _ctx = null;
    private Client _client = null;
    private ConnectionStatus _currState = ConnectionStatus.CONNECTION_TIMED_OUT;

    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection _connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            GameOnService.LocalBinder binder = (GameOnService.LocalBinder) service;
            _service = binder.getService();
            _service.startPolling(_eventToGameManager ,_client);
            _isBoundToService = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            _isBoundToService = false;
            _service.stopPolling(_client.getId());
        }
    };

    public NetworkManager(ICallback eventToGameManager, Context ctx, Client client) {
        this._eventToGameManager = eventToGameManager;
        this._ctx = ctx;
        this._client = client;
        // TODO
//        if(_myApiService == null){
//            GameonApi.Builder builder = new GameonApi.Builder(AndroidHttp.newCompatibleTransport(),
//                    new AndroidJsonFactory(), null)
//                    // options for running against local devappserver
//                    // - 10.0.2.2 is localhost's IP address in Android emulator
//                    // - turn off compression when running against local devappserver
//                    .setRootUrl(Settings.ROOT_URL)
//                    .setGoogleClientRequestInitializer(new GoogleClientRequestInitializer() {
//                        @Override
//                        public void initialize(AbstractGoogleClientRequest<?> abstractGoogleClientRequest) throws IOException {
//                            abstractGoogleClientRequest.setDisableGZipContent(true);
//                        }
//                    });
//            //TODO: GZIP
//            // end options for devappserver
//
//            _myApiService = builder.build();
//        }
        if(!isPolling())
            connectServiceAndStartPolling();
    }

    public boolean isPolling(){
        return _isBoundToService;
    }

    public void connectServiceAndStartPolling(){
        Intent intent = new Intent(_ctx, GameOnService.class);
        _ctx.bindService(intent, _connection, Context.BIND_AUTO_CREATE);
    }

    public void disconnectFromServiceAndStopPolling(){
        if(_isBoundToService) {
            _ctx.unbindService(_connection);
            _isBoundToService = false;
        }
    }

    // will be called the case of polling failure due do network problem.
    public void resetServiceConnection() {
        disconnectFromServiceAndStopPolling();
        connectServiceAndStartPolling();
    }

    public void sendMessage(IMessage message) {
        if(!isPolling())
            connectServiceAndStartPolling();
        new sendMessageAsyncTask().execute(message);
    }

    public ConnectionStatus getCurrentConnectionStatus(){
        if(_service == null)
            return ConnectionStatus.CONNECTION_TIMED_OUT;
        return _service.getCurrentConnectionStatus();
    }

    class sendMessageAsyncTask extends AsyncTask<IMessage, Void, Boolean> {
        private IMessage msg;

        @Override
        protected Boolean doInBackground(IMessage... params) {
            msg = params[0];
            // TODO
//            Packet p = new Packet();
//
//            byte[] payload = MessageCompression.getInstance().compress(msg);
//
//            p.setPayload(Base64.encodeBase64String(payload));
//            p.setDate(System.currentTimeMillis());
//
//            try {
//                _myApiService.gameonApi().sendMessage(p).execute();
//                return true;
//            } catch (IOException e) {
//                Log.d(Settings.tagException,e.getMessage());
//                return false;
//            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            //if(aBoolean == false)
        }
    }
}
