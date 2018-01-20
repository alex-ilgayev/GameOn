package com.gameon.gameon;

import android.app.Activity;
import android.app.usage.UsageEvents;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.text.ICUCompatApi23;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;

import com.gameon.gameon.controller.NetworkManager;
import com.gameon.gameon.controller.Settings;
import com.gameon.gameon.datatypes.Client;
import com.gameon.gameon.datatypes.ConnectionStatus;
import com.gameon.gameon.datatypes.ICallback;
import com.gameon.gameon.messaging.IMessage;
import com.gameon.gameon.messaging.MessageInnerConnectionStatus;
import com.gameon.gameon.messaging.MessageResponseClientList;
import com.gameon.gameon.messaging.MessageResponseSession;
import com.gameon.gameon.sudoku.SudokuGameActivity;

public class BaseActivity extends Activity {
    protected GameOnApp _myApp;
    protected NetworkManager _networkManager = null;
    protected Client _client = Settings.getInstance().getThisClient();

    private Animation _rotateAnim;
    private Animation _zoomInAnim;
    private Animation _fadeOutAnim;
    private ImageButton _btnStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);

        _myApp = (GameOnApp) this.getApplicationContext();

        setupConnectivityAnimation();
    }

    @Override
    protected void onPause() {
        clearReferences();
        super.onPause();
        if(_networkManager.isPolling())
            _networkManager.disconnectFromServiceAndStopPolling();
    }

    public void setupBaseActivityNetwork(ICallback extendedActivityCallback) {
        _networkManager = new NetworkManager(new EventHandlerToBaseActivity(extendedActivityCallback), this, _client);
    }

    public void setupConnectivityAnimation(){
        _btnStatus = (ImageButton) findViewById(R.id.btnStatus);

        _rotateAnim = AnimationUtils.loadAnimation(_myApp, R.anim.anim_rotate);
        _zoomInAnim = AnimationUtils.loadAnimation(_myApp, R.anim.anim_zoomin);
        _fadeOutAnim = AnimationUtils.loadAnimation(_myApp, R.anim.anim_fadeout);
    }

    @Override
    protected void onDestroy() {
        clearReferences();
        super.onDestroy();
    }

    private void clearReferences(){
        Activity currActivity = _myApp.getCurrentActivity();
        if (this.equals(currActivity))
            _myApp.setCurrentActivity(null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        _myApp.setCurrentActivity(this);

        ConnectionStatus state = Settings.getInstance().getConnectivityState();
        if(state == ConnectionStatus.CONNECTION_NOT_TRIED_YET) {
            setConnectivityState(ConnectionStatus.CONNECTION_RETRYING);
        } else if(state == ConnectionStatus.CONNECTION_OK) {
            _btnStatus.clearAnimation();
        }
        else
            setConnectivityState(state);

        _networkManager.resetServiceConnection();
    }

    public void OnClickRetryConnection(View v) {

        if(Settings.getInstance().getConnectivityState() == ConnectionStatus.CONNECTION_TIMED_OUT) {
            setConnectivityState(ConnectionStatus.CONNECTION_RETRYING);
            _networkManager.resetServiceConnection();
        }
    }

    public void setConnectivityState(ConnectionStatus state){
        Settings.getInstance().setConnectivityState(state);
        switch(state){
            case CONNECTION_OK:
                _btnStatus.setBackgroundResource(R.drawable.connected);
                _btnStatus.clearAnimation();
                //_btnStatus.startAnimation(_fadeOutAnim);
                break;
            case CONNECTION_RETRYING:
                _btnStatus.setBackgroundResource(R.drawable.retrying);
                _btnStatus.clearAnimation();
                _btnStatus.startAnimation(_rotateAnim);
                break;
            case CONNECTION_TIMED_OUT:
                _btnStatus.setBackgroundResource(R.drawable.disconnected);
                _btnStatus.clearAnimation();
                _btnStatus.startAnimation(_zoomInAnim);
                break;
        }
    }

    public class EventHandlerToBaseActivity implements ICallback {

        private ICallback _extendedCallback;

        public EventHandlerToBaseActivity(ICallback extendedCallback) {
            _extendedCallback = extendedCallback;
        }

        @Override
        public void receiveMessage(IMessage message) {
            switch(message.getMessageType()) {
                case INNER_CONNECTION_STATUS:
                    MessageInnerConnectionStatus connStatus = (MessageInnerConnectionStatus)message;
                    setConnectivityState(connStatus.connStatus);
                    break;
            }
            _extendedCallback.receiveMessage(message);
        }
    }
}