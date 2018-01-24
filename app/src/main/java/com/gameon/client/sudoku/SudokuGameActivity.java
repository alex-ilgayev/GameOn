package com.gameon.client.sudoku;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.gameon.client.BaseActivity;
import com.gameon.client.controller.Settings;
import com.gameon.shared.datatypes.Client;
import com.gameon.shared.datatypes.ICallback;
import com.gameon.shared.datatypes.Session;
import com.gameon.shared.messaging.MessageRequestSetMove;
import com.gameon.shared.messaging.MessageRequestSms;
import com.gameon.shared.sudoku.SudokuGameData;
import com.gameon.shared.sudoku.datatypes.SudokuBoard;
import com.gameon.shared.sudoku.datatypes.SudokuMove;
import com.gameon.shared.sudoku.datatypes.SudokuResultType;
import com.gameon.shared.messaging.IMessage;
import com.gameon.shared.messaging.MessageResponseSession;
import com.gameon.shared.messaging.MessageResponseSms;
import com.gameon.gameon.R;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.UUID;


public class SudokuGameActivity extends BaseActivity {

    public static final String INTENT_TAG_SUDOKU_GAME_DATA = "gameData";
    public static final String INTENT_TAG_SESSION_ID_TO_JOIN = "sessionToJoin";

    private SudokuGameData _gameData = null;
    private View _viewIdBeingEdited = null;
    private LinkedList<Client> _connectedClients = null;
    private ListView _lvConnectedClients = null;
    private ConnectedClientsAdapter _adapter = null;

    private ScrollView _svConsole = null;
    private TextView _tvConsole = null;
    private EditText _etSms = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setupBaseActivityNetwork(new EventHandlerToSudokuActivity());

        RelativeLayout rlRootView = (RelativeLayout) findViewById( R.id.rlRootView);
        View view_child = getLayoutInflater().inflate(R.layout.activity_sudoku_game, null);
        rlRootView.addView(view_child, 0);
        for(int i=1; i<rlRootView.getChildCount(); i++){
            rlRootView.getChildAt(i).bringToFront();
        }

        TextView title = (TextView) findViewById(R.id.tvSudokuTitle);
        title.setTypeface(Settings.getInstance().getFontBold());

        _connectedClients = new LinkedList<>();
        _lvConnectedClients = (ListView) findViewById(R.id.lvConnectedClients);
        _adapter = new ConnectedClientsAdapter(this, _connectedClients);
        _lvConnectedClients.setAdapter(_adapter);

        _svConsole = (ScrollView) findViewById(R.id.svConsole);
        _tvConsole = (TextView) findViewById(R.id.tvConsole);
        _etSms = (EditText) findViewById(R.id.etSms);
        _etSms.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if(actionId == EditorInfo.IME_ACTION_DONE) {
                    onClickSendSms(v);
                    handled = true;
                }
                return handled;
            }
        });

        Serializable gameDataSerializable = getIntent().getSerializableExtra(INTENT_TAG_SUDOKU_GAME_DATA);
        Serializable sessionIdToJoin = getIntent().getSerializableExtra(INTENT_TAG_SESSION_ID_TO_JOIN);

        if(gameDataSerializable != null)
            _gameData = (SudokuGameData) gameDataSerializable;
        if(sessionIdToJoin != null)
            _client.setCurrSessionId((UUID) sessionIdToJoin);

        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                int value = _gameData.getBoard().getValue(i, j);
                setSudokuValueToActivity(i, j, value);

                if (_gameData.isChangeable(i, j))
                    setButtonPressableStyle(getButtonViewFromRowCol(i, j));
                else
                    setButtonUnPressableStyle(getButtonViewFromRowCol(i, j));
            }
        }

        // animation stuff
        TableRow row = (TableRow) findViewById(R.id.tblRowNumber);
        for(int i=0; i<row.getVirtualChildCount(); i++){
            Button btn = (Button) row.getVirtualChildAt(i);
            final TransitionDrawable trans = (TransitionDrawable) btn.getBackground();
            trans.startTransition(0);
            btn.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()){
                        case MotionEvent.ACTION_DOWN:
                            trans.startTransition(400);
                            break;
                    }
                    // sending the event to the parent.
                    return false;
                }
            });
        }
    }

    public void onClickOpenNumber(View v){
        if(_viewIdBeingEdited != null) {
            onClickCloseNumber(v);
            return;
        }

        SudokuMove move = getSudokuMoveFromButtonView((Button)v);
        if(!_gameData.isChangeable(move.row, move.col))
            return;
        if(_gameData.isFinished())
            return;
        // moving the table according the button clicked.
        TableLayout tbl = (TableLayout) findViewById(R.id.tblNumber);
        tbl.setVisibility(View.VISIBLE);
        _viewIdBeingEdited = v;
    }

    public void onClickCloseNumber(View v){
        TableLayout tbl = (TableLayout) findViewById(R.id.tblNumber);
        tbl.setVisibility(View.INVISIBLE);
        _viewIdBeingEdited = null;
    }

    public void onClickSetNumber(View v){
        switch(v.getId()){
            case R.id.btnNum_delete:
                setSudokuValueInViewToGameManager(_viewIdBeingEdited, 0);
                break;
            case R.id.btnNum_1:
                setSudokuValueInViewToGameManager(_viewIdBeingEdited, 1);
                break;
            case R.id.btnNum_2:
                setSudokuValueInViewToGameManager(_viewIdBeingEdited, 2);
                break;
            case R.id.btnNum_3:
                setSudokuValueInViewToGameManager(_viewIdBeingEdited, 3);
                break;
            case R.id.btnNum_4:
                setSudokuValueInViewToGameManager(_viewIdBeingEdited, 4);
                break;
            case R.id.btnNum_5:
                setSudokuValueInViewToGameManager(_viewIdBeingEdited, 5);
                break;
            case R.id.btnNum_6:
                setSudokuValueInViewToGameManager(_viewIdBeingEdited, 6);
                break;
            case R.id.btnNum_7:
                setSudokuValueInViewToGameManager(_viewIdBeingEdited, 7);
                break;
            case R.id.btnNum_8:
                setSudokuValueInViewToGameManager(_viewIdBeingEdited, 8);
                break;
            case R.id.btnNum_9:
                setSudokuValueInViewToGameManager(_viewIdBeingEdited, 9);
        }
        onClickCloseNumber(v);
    }

    public void onClickSendSms(View v){
        if(_etSms.getText().toString().equals(""))
            return;
        postTextToActivity(_client.getName(), _etSms.getText().toString());

        MessageRequestSms msg = new MessageRequestSms();
        msg.client = _client;
        msg.id = UUID.randomUUID();
        msg.text = _etSms.getText().toString();
        _networkManager.sendMessage(msg);
        
        _etSms.setText("");
        _svConsole.post(new Runnable() {

            @Override
            public void run() {
                _svConsole.fullScroll(View.FOCUS_DOWN);
            }
        });
    }

    /**
     * receives table layout, and returns button view according to index.
     * @param tbl input table layout
     * @param idx index in the table (0-8)
     * @return
     */
    public Button getButtonViewFromTable(TableLayout tbl, int idx){
        Button btn = null;
        switch (idx) {
            case 0:
                btn = (Button) tbl.findViewById(R.id.btnTbl_1);
                break;
            case 1:
                btn = (Button) tbl.findViewById(R.id.btnTbl_2);
                break;
            case 2:
                btn = (Button) tbl.findViewById(R.id.btnTbl_3);
                break;
            case 3:
                btn = (Button) tbl.findViewById(R.id.btnTbl_4);
                break;
            case 4:
                btn = (Button) tbl.findViewById(R.id.btnTbl_5);
                break;
            case 5:
                btn = (Button) tbl.findViewById(R.id.btnTbl_6);
                break;
            case 6:
                btn = (Button) tbl.findViewById(R.id.btnTbl_7);
                break;
            case 7:
                btn = (Button) tbl.findViewById(R.id.btnTbl_8);
                break;
            case 8:
                btn = (Button) tbl.findViewById(R.id.btnTbl_9);
                break;
        }
        return btn;
    }

    public Button getButtonViewFromRowCol(int rowIdx, int colIdx){
        TableLayout tbl = null;
        Button btn = null;
        // table number, goes from 1 to 9.
        switch((rowIdx/3)*3+(colIdx/3)+1){
            case 1:
                tbl = (TableLayout) findViewById(R.id.tblSudoku_1);
                btn = getButtonViewFromTable(tbl, (rowIdx % 3) * 3 + colIdx % 3);
                break;
            case 2:
                tbl = (TableLayout) findViewById(R.id.tblSudoku_2);
                btn = getButtonViewFromTable(tbl, (rowIdx % 3) * 3 + colIdx % 3);
                break;
            case 3:
                tbl = (TableLayout) findViewById(R.id.tblSudoku_3);
                btn = getButtonViewFromTable(tbl, (rowIdx % 3) * 3 + colIdx % 3);
                break;
            case 4:
                tbl = (TableLayout) findViewById(R.id.tblSudoku_4);
                btn = getButtonViewFromTable(tbl, (rowIdx % 3) * 3 + colIdx % 3);
                break;
            case 5:
                tbl = (TableLayout) findViewById(R.id.tblSudoku_5);
                btn = getButtonViewFromTable(tbl, (rowIdx % 3) * 3 + colIdx % 3);
                break;
            case 6:
                tbl = (TableLayout) findViewById(R.id.tblSudoku_6);
                btn = getButtonViewFromTable(tbl, (rowIdx % 3) * 3 + colIdx % 3);
                break;
            case 7:
                tbl = (TableLayout) findViewById(R.id.tblSudoku_7);
                btn = getButtonViewFromTable(tbl, (rowIdx % 3) * 3 + colIdx % 3);
                break;
            case 8:
                tbl = (TableLayout) findViewById(R.id.tblSudoku_8);
                btn = getButtonViewFromTable(tbl, (rowIdx % 3) * 3 + colIdx % 3);
                break;
            case 9:
                tbl = (TableLayout) findViewById(R.id.tblSudoku_9);
                btn = getButtonViewFromTable(tbl, (rowIdx % 3) * 3 + colIdx % 3);
                break;
        }
        return btn;
    }

    /**
     * receives button view, and returns it's row and col index in sudoku board.
     * @param v the button view
     * @return sudoku move with the row and col, with EMPTY_VALUE in value parameter.
     */
    public SudokuMove getSudokuMoveFromButtonView(View v){
        int btnNum = 0;
        int tblNum = 0;
        switch(v.getId()){
            case R.id.btnTbl_1:
                btnNum = 0;
                break;
            case R.id.btnTbl_2:
                btnNum = 1;
                break;
            case R.id.btnTbl_3:
                btnNum = 2;
                break;
            case R.id.btnTbl_4:
                btnNum = 3;
                break;
            case R.id.btnTbl_5:
                btnNum = 4;
                break;
            case R.id.btnTbl_6:
                btnNum = 5;
                break;
            case R.id.btnTbl_7:
                btnNum = 6;
                break;
            case R.id.btnTbl_8:
                btnNum = 7;
                break;
            case R.id.btnTbl_9:
                btnNum = 8;
                break;
        }

        switch(((TableLayout)v.getParent().getParent()).getId()){
            case R.id.tblSudoku_1:
                tblNum = 0;
                break;
            case R.id.tblSudoku_2:
                tblNum = 1;
                break;
            case R.id.tblSudoku_3:
                tblNum = 2;
                break;
            case R.id.tblSudoku_4:
                tblNum = 3;
                break;
            case R.id.tblSudoku_5:
                tblNum = 4;
                break;
            case R.id.tblSudoku_6:
                tblNum = 5;
                break;
            case R.id.tblSudoku_7:
                tblNum = 6;
                break;
            case R.id.tblSudoku_8:
                tblNum = 7;
                break;
            case R.id.tblSudoku_9:
                tblNum = 8;
                break;
        }
        int row = (tblNum/3)*3+(btnNum/3);
        int col = (tblNum%3)*3+(btnNum%3);
        return new SudokuMove(row, col, SudokuBoard.EMPTY_VAL);
    }

    public int getStringResourceIdFromNumber(int value) {
        switch(value){
            case SudokuBoard.EMPTY_VAL:
                return R.string.empty_value;
            case 1:
                return R.string.number_1;
            case 2:
                return R.string.number_2;
            case 3:
                return R.string.number_3;
            case 4:
                return R.string.number_4;
            case 5:
                return R.string.number_5;
            case 6:
                return R.string.number_6;
            case 7:
                return R.string.number_7;
            case 8:
                return R.string.number_8;
            case 9:
                return R.string.number_9;
        }
        return R.string.empty_value;
    }

    public int getNumberFromStringResourceId(int res){
        switch(res){
            case R.string.empty_value:
                return SudokuBoard.EMPTY_VAL;
            case R.string.number_1:
                return 1;
            case R.string.number_2:
                return 2;
            case R.string.number_3:
                return 3;
            case R.string.number_4:
                return 4;
            case R.string.number_5:
                return 5;
            case R.string.number_6:
                return 6;
            case R.string.number_7:
                return 7;
            case R.string.number_8:
                return 8;
            case R.string.number_9:
                return 9;
        }
        return SudokuBoard.EMPTY_VAL;
    }

    public void setSudokuValueToActivity(int rowIdx, int colIdx, int value){
        Button btn = null;
        // table number, goes from 1 to 9.
        btn = getButtonViewFromRowCol(rowIdx, colIdx);
        btn.setText(getStringResourceIdFromNumber(value));
        TransitionDrawable trans = (TransitionDrawable)getResources().getDrawable(R.drawable.transition_update_number);
        btn.setBackgroundDrawable(trans);
        trans.startTransition(1000);
    }

    public void setSudokuValueInViewToGameManager(View v, int value){
        SudokuMove move = getSudokuMoveFromButtonView(v);
        move.value = value;

        SudokuResultType result = _gameData.setMove(move);

        if(result == SudokuResultType.SUCCESS) {
            MessageRequestSetMove msg = new MessageRequestSetMove();
            msg.client = _client;
            msg.id = UUID.randomUUID();
            msg.move = move;
            _networkManager.sendMessage(msg);

            ((Button) v).setText(getStringResourceIdFromNumber(value));
            if (_gameData.isFinished())
                postTextToActivity(Settings.CHAT_SYSTEM_PREFIX,Settings.CHAT_SYSTEM_FINISHED);
            //TODO:
            // what to do when finished ?
        }
        else
            postTextToActivity(Settings.CHAT_SYSTEM_PREFIX, Settings.CHAT_SYSTEM_INVALID);
    }

    public void setButtonPressableStyle(View v){
        Button btn = (Button)v;
        btn.setBackgroundResource(R.drawable.state_gray_button);
        btn.setTextColor(Color.parseColor("#000000"));
    }

    public void setButtonUnPressableStyle(View v){
        Button btn = (Button)v;
        btn.setBackgroundResource(R.drawable.state_black_button);
        btn.setTextColor(getResources().getColorStateList(R.color.state_black_button));
    }

    public void postTextToActivity(String name, String text){
        String pref =  name + ": ";
        SpannableString spanPref = new SpannableString(pref);
        spanPref.setSpan(new StyleSpan(Typeface.BOLD), 0, spanPref.length(), 0);

        _tvConsole.append(spanPref);
        _tvConsole.append(text + "\n");
        _svConsole.post(new Runnable() {

            @Override
            public void run() {
                _svConsole.fullScroll(View.FOCUS_DOWN);
            }
        });
    }

    public class ConnectedClientsAdapter extends ArrayAdapter<Client> {
        public ConnectedClientsAdapter(Context context, LinkedList<Client> users) {
            super(context, 0, users);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // Get the data item for this position
            Client user = getItem(position);
            // Check if an existing view is being reused, otherwise inflate the view
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_connected_clients, parent, false);
            }
            // Lookup view for data population
            TextView tvName = (TextView) convertView.findViewById(R.id.tvClientName);
            // Populate the data into the template view using the data object
            tvName.setText(user.getName());
            // Return the completed view to render on screen
            return convertView;
        }
    }

    public class EventHandlerToSudokuActivity implements ICallback {
        @Override
        public void receiveMessage(IMessage message) {
            switch(message.getMessageType()) {
                case RESPONSE_SESSION:
                    if(message.getId() == null ||
                            !message.getId().equals(Settings.getInstance().getPollRequestId()))
                        break;
                    MessageResponseSession sessionInfo = (MessageResponseSession)message;
                    Session session = sessionInfo.activeSession;

                    SudokuMove[] diff = SudokuGameData.getDiff(_gameData, session.getGameData());
                    _gameData = new SudokuGameData(session.getGameData());
                    for(SudokuMove sMove: diff){
                        setSudokuValueToActivity(sMove.row, sMove.col, sMove.value);
                    }

                    _connectedClients.clear();
                    for(Client client: session.getClientList()) {
                        _connectedClients.add(client);
            }
                    _adapter.notifyDataSetChanged();
                    break;
                case RESPONSE_SMS:
                    if(message.getId() == null)
                        break;
                    MessageResponseSms textMsg = (MessageResponseSms) message;
            postTextToActivity(textMsg.name, textMsg.text);
                    break;
            }
        }
    }
}
