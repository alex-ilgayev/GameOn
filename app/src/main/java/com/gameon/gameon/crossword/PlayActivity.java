/**
 * This file is part of Words With Crosses.
 *
 * Copyright (C) 2009-2010 Robert Cooper
 * Copyright (C) 2013 Adam Rosenfield
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.gameon.gameon.crossword;

import static com.gameon.gameon.crossword.WordsWithCrossesApplication.BOARD;
import static com.gameon.gameon.crossword.WordsWithCrossesApplication.RENDERER;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.inputmethodservice.KeyboardView.OnKeyboardActionListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.gameon.gameon.R;
import com.gameon.gameon.crossword.io.IO;
import com.gameon.gameon.crossword.puz.MovementStrategy;
import com.gameon.gameon.crossword.puz.Playboard;
import com.gameon.gameon.crossword.puz.Puzzle;
import com.gameon.gameon.crossword.puz.Playboard.Clue;
import com.gameon.gameon.crossword.puz.Playboard.OnBoardChangedListener;
import com.gameon.gameon.crossword.puz.Playboard.Position;
import com.gameon.gameon.crossword.puz.Playboard.Word;
import com.gameon.gameon.crossword.view.CrosswordImageView;
import com.gameon.gameon.crossword.view.PlayboardRenderer;
import com.gameon.gameon.crossword.view.SeparatedListAdapter;
import com.gameon.gameon.crossword.view.CrosswordImageView.ClickListener;
import com.gameon.gameon.crossword.view.CrosswordImageView.RenderScaleListener;

public class PlayActivity extends WordsWithCrossesActivity {

    /** Extra data tag required by this activity */
    public static final String EXTRA_PUZZLE_ID = "puzzle_id";

    /** Playable non-rebus characters */
    public static final String PLAYABLE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    private static final Logger LOG = Logger.getLogger("com.gameon.gameon.crossword");

    private static final int INFO_DIALOG = 0;
    private static final int NOTES_DIALOG = 1;
    private static final int REVEAL_PUZZLE_DIALOG = 2;

    private ProgressDialog loadProgressDialog;

    @SuppressWarnings("rawtypes")
    private AdapterView across;
    @SuppressWarnings("rawtypes")
    private AdapterView down;
    private ListView allClues;
    private ClueListAdapter acrossAdapter;
    private ClueListAdapter downAdapter;
    private SeparatedListAdapter allCluesAdapter;
    private Configuration configuration;
    private File baseFile;
    private Handler handler = new Handler();
    private KeyboardView keyboardView = null;
    private Puzzle puz;
    private long puzzleId;
    private CrosswordImageView boardView;
    private TextView clue;
    private View clueContainer;
    private boolean maybeShowClueContainerOnPuzzleLoad = false;
    private boolean showingProgressBar = false;

    private boolean showCount = false;
    private boolean showErrors = false;
    private boolean useNativeKeyboard = false;
    private long lastKey;

    private DisplayMetrics metrics;

    // Saved scale from before we fit to screen
    private float lastBoardScale = 1.0f;
    private boolean fitToScreen = false;

    private boolean hasSetInitialZoom = false;

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        this.configuration = newConfig;

        if (shouldShowKeyboard(configuration)) {
            if (this.useNativeKeyboard) {
                keyboardView.setVisibility(View.GONE);

                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);

                if (imm != null) {
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_NOT_ALWAYS);
                }
            } else {
                this.keyboardView.setVisibility(View.VISIBLE);
            }
        } else {
            this.keyboardView.setVisibility(View.GONE);
        }
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        this.configuration = getBaseContext().getResources().getConfiguration();

        if (prefs.getBoolean("showProgressBar", false)) {
            requestWindowFeature(Window.FEATURE_PROGRESS);
            showingProgressBar = true;
        }

        // Must happen after all calls to requestWindowFeature()
        setContentView(R.layout.play);

        setDefaultKeyMode(Activity.DEFAULT_KEYS_DISABLE);

        if (prefs.getBoolean("fullScreen", false)) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }

        // Initialize this here so that onPause() has a reference to a View
        // even if the puzzle hasn't finished loading yet
        clue = (TextView)this.findViewById(R.id.clueLine);

        // Hide the clue container while we load the puzzle, then maybe
        // show it again later
        clueContainer = findViewById(R.id.clueContainer);
        maybeShowClueContainerOnPuzzleLoad = (clueContainer.getVisibility() != View.GONE);
        clueContainer.setVisibility(View.GONE);

        if (BOARD != null && BOARD.getPuzzleID() == puzzleId) {
            puz = BOARD.getPuzzle();
        }

        if (puz != null) {
            postPuzzleLoaded();
        } else {
            // Show a progress dialog while the puzzle is loaded
            loadProgressDialog = new ProgressDialog(this);
            loadProgressDialog.setMessage(getResources().getString(R.string.loading_puzzle));
            loadProgressDialog.setCancelable(false);
            loadProgressDialog.show();

            // Load the puzzle on a background thread
            new Thread(new Runnable() {
                public void run() {
                    final Puzzle newPuzzle = loadPuzzle();

                    // Do stuff on the UI thread after the puzzle is loaded
                    handler.post(new Runnable() {
                        public void run() {
                            if (loadProgressDialog != null) {
                                loadProgressDialog.dismiss();
                                loadProgressDialog = null;

                                if (newPuzzle != null) {
                                    puz = newPuzzle;
                                    postPuzzleLoaded();
                                } else {

                                }
                            }
                        }
                    });
                }
            }).start();
        }
    }

    private Puzzle loadPuzzle() {
        String path = "/storage/sdcard/Android/data/com.adamrosenfield.wordswithcrosses/files/crosswords/2015-11-25-JosephCrosswords.puz";
        File file = new File(path);
        try {
            return IO.load(file);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void postPuzzleLoaded() {

        initPlayboard();
        initKeyboard();
        initClueLists();
        handleOnResume();
    }

    private void initPlayboard() {
        BOARD = new Playboard(puz, puzzleId, getMovementStrategy());
        RENDERER = new PlayboardRenderer(BOARD);

        BOARD.setOnBoardChangedListener(new OnBoardChangedListener() {
            public void onBoardChanged() {
                updateProgressBar();
            }
        });

        if (maybeShowClueContainerOnPuzzleLoad && android.os.Build.VERSION.SDK_INT < 11) {
            clueContainer.setVisibility(View.VISIBLE);
        } else {
            clueContainer.setVisibility(View.GONE);
//            View clueLine = utils.onActionBarCustom(this, R.layout.clue_line_only);
//            if (clueLine != null) {
//                clue = (TextView)clueLine.findViewById(R.id.clueLine);
//            }
        }
        clue.setClickable(true);
        clue.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                Intent i = new Intent(PlayActivity.this, ClueListActivity.class);
                i.setData(Uri.fromFile(baseFile));
                PlayActivity.this.startActivityForResult(i, 0);
            }
        });

        if (clueContainer.getVisibility() != View.GONE &&
            !TextUtils.isEmpty(puz.getNotes()))
        {
            View notesButton = findViewById(R.id.notesButton);
            notesButton.setVisibility(View.VISIBLE);
        }

        boardView = (CrosswordImageView)findViewById(R.id.board);
        boardView.setBoard(BOARD, metrics);

        this.registerForContextMenu(boardView);

        boardView.setClickListener(new ClickListener() {
            public void onClick(Position pos) {
                Word prevWord = null;
                if (pos != null) {
                    prevWord = BOARD.setHighlightLetter(pos);
                }
                render(prevWord);
            }

            public void onDoubleClick(Position pos) {
                if (prefs.getBoolean("doubleTap",  false)) {
                    if (fitToScreen) {
                        boardView.setRenderScale(lastBoardScale);

                        Word prevWord = null;
                        if (pos != null) {
                            prevWord = BOARD.setHighlightLetter(pos);
                        }
                        render(prevWord);
                    } else {
                        lastBoardScale = boardView.getRenderScale();
                        boardView.fitToScreen();
                        render();
                    }

                    fitToScreen = !fitToScreen;
                } else {
                    onClick(pos);
                }
            }

            public void onLongClick(Position pos) {
                Word prevWord = null;
                if (pos != null) {
                    prevWord = BOARD.setHighlightLetter(pos);
                }
                boardView.render(prevWord);
                openContextMenu(boardView);
            }
        });

        boardView.setRenderScaleListener(new RenderScaleListener() {
            public void onRenderScaleChanged(float renderScale) {
                fitToScreen = false;
            }
        });
    }

    private void initKeyboard() {
        updateKeyboardFromPrefs();
        keyboardView.setOnKeyboardActionListener(new OnKeyboardActionListener() {
            private long lastSwipe = 0;

            public void onKey(int primaryCode, int[] keyCodes) {
                long eventTime = System.currentTimeMillis();

                if ((eventTime - lastSwipe) < 500) {
                    return;
                }

                KeyEvent event =
                    new KeyEvent(eventTime,
                                 eventTime,
                                 KeyEvent.ACTION_DOWN,
                                 primaryCode,
                                 0, 0, 0, 0,
                                 KeyEvent.FLAG_SOFT_KEYBOARD | KeyEvent.FLAG_KEEP_TOUCH_MODE);
                PlayActivity.this.onKeyUp(primaryCode, event);
            }

            public void onPress(int primaryCode) {
            }

            public void onRelease(int primaryCode) {
            }

            public void onText(CharSequence text) {
            }

            public void swipeDown() {
                long eventTime = System.currentTimeMillis();
                lastSwipe = eventTime;

                KeyEvent event =
                    new KeyEvent(eventTime, eventTime,
                                 KeyEvent.ACTION_DOWN,
                                 KeyEvent.KEYCODE_DPAD_DOWN,
                                 0, 0, 0, 0,
                                 KeyEvent.FLAG_SOFT_KEYBOARD | KeyEvent.FLAG_KEEP_TOUCH_MODE);
                PlayActivity.this.onKeyUp(KeyEvent.KEYCODE_DPAD_DOWN, event);
            }

            public void swipeLeft() {
                long eventTime = System.currentTimeMillis();
                lastSwipe = eventTime;

                KeyEvent event =
                    new KeyEvent(eventTime,
                                 eventTime,
                                 KeyEvent.ACTION_DOWN,
                                 KeyEvent.KEYCODE_DPAD_LEFT,
                                 0, 0, 0, 0,
                                 KeyEvent.FLAG_SOFT_KEYBOARD | KeyEvent.FLAG_KEEP_TOUCH_MODE);
                PlayActivity.this.onKeyUp(KeyEvent.KEYCODE_DPAD_LEFT, event);
            }

            public void swipeRight() {
                long eventTime = System.currentTimeMillis();
                lastSwipe = eventTime;

                KeyEvent event =
                    new KeyEvent(eventTime,
                                 eventTime,
                                 KeyEvent.ACTION_DOWN,
                                 KeyEvent.KEYCODE_DPAD_RIGHT,
                                 0, 0, 0, 0,
                                 KeyEvent.FLAG_SOFT_KEYBOARD | KeyEvent.FLAG_KEEP_TOUCH_MODE);
                PlayActivity.this.onKeyUp(KeyEvent.KEYCODE_DPAD_RIGHT, event);
            }

            public void swipeUp() {
                long eventTime = System.currentTimeMillis();
                lastSwipe = eventTime;

                KeyEvent event =
                    new KeyEvent(eventTime,
                                 eventTime,
                                 KeyEvent.ACTION_DOWN,
                                 KeyEvent.KEYCODE_DPAD_UP,
                                 0, 0, 0, 0,
                                 KeyEvent.FLAG_SOFT_KEYBOARD | KeyEvent.FLAG_KEEP_TOUCH_MODE);
                PlayActivity.this.onKeyUp(KeyEvent.KEYCODE_DPAD_UP, event);
            }
        });
    }

    private void updateKeyboardFromPrefs() {
        int keyboardType = getKeyboardTypePreference();
        useNativeKeyboard = (keyboardType == -1);
        keyboardView = (KeyboardView)findViewById(R.id.playKeyboard);

        if (!useNativeKeyboard) {
            Keyboard keyboard = new Keyboard(this, keyboardType);
            keyboardView.setKeyboard(keyboard);
        } else {
            keyboardView.setVisibility(View.GONE);
        }

        boardView.setUseNativeKeyboard(useNativeKeyboard);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void initClueLists() {
        this.across = (AdapterView) this.findViewById(R.id.acrossList);
        this.down = (AdapterView) this.findViewById(R.id.downList);

        if ((this.across == null) && (this.down == null)) {
            this.across = (AdapterView) this.findViewById(R.id.acrossListGal);
            this.down = (AdapterView) this.findViewById(R.id.downListGal);
        }

        if ((across != null) && (down != null)) {
            across.setAdapter(this.acrossAdapter = new ClueListAdapter(this,
                    BOARD.getAcrossClues(), true));
            down.setAdapter(this.downAdapter = new ClueListAdapter(this, BOARD
                    .getDownClues(), false));
            across.setOnItemClickListener(new OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    parent.setSelected(true);
                    BOARD.jumpTo(position, true);
                    render();
                }
            });
            across.setOnItemSelectedListener(new OnItemSelectedListener() {
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (!BOARD.isAcross() || (BOARD.getCurrentClueIndex() != position)) {
                        BOARD.jumpTo(position, true);
                        render();
                    }
                }

                public void onNothingSelected(AdapterView<?> view) {
                }
            });
            down.setOnItemClickListener(new OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    parent.setSelected(true);
                    BOARD.jumpTo(position, false);
                    render();
                }
            });

            down.setOnItemSelectedListener(new OnItemSelectedListener() {
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (BOARD.isAcross() || (BOARD.getCurrentClueIndex() != position)) {
                        BOARD.jumpTo(position, false);
                        render();
                    }
                }

                public void onNothingSelected(AdapterView<?> view) {
                }
            });
            down.scrollTo(0, 0);
            across.scrollTo(0, 0);
        }
        this.allClues = (ListView) this.findViewById(R.id.allClues);
        if (this.allClues != null) {
            this.allCluesAdapter = new SeparatedListAdapter(this);
            this.allCluesAdapter.addSection(
                    getResources().getString(R.string.across),
                    this.acrossAdapter = new ClueListAdapter(this, BOARD
                            .getAcrossClues(), true));
            this.allCluesAdapter.addSection(
                    getResources().getString(R.string.down),
                    this.downAdapter = new ClueListAdapter(this, BOARD
                            .getDownClues(), false));
            allClues.setAdapter(this.allCluesAdapter);

            allClues.setOnItemClickListener(new OnItemClickListener() {
                public void onItemClick(AdapterView<?> arg0, View arg1,
                        int clickIndex, long arg3) {
                    boolean across = clickIndex <= BOARD.getAcrossClues().length +1;
                    int index = clickIndex -1;
                    if(index > BOARD.getAcrossClues().length ){
                        index = index - BOARD.getAcrossClues().length - 1;
                    }
                    arg0.setSelected(true);
                    BOARD.jumpTo(index, across);
                    render();
                }
            });
            allClues.setOnItemSelectedListener(new OnItemSelectedListener() {
                public void onItemSelected(AdapterView<?> arg0, View arg1,
                        int clickIndex, long arg3) {
                        boolean across = clickIndex <= BOARD.getAcrossClues().length +1;
                        int index = clickIndex -1;
                        if(index > BOARD.getAcrossClues().length ){
                            index = index - BOARD.getAcrossClues().length - 1;
                        }
                            if(!BOARD.isAcross() == across && BOARD.getCurrentClueIndex() != index){
                            arg0.setSelected(true);
                            BOARD.jumpTo(index, across);
                            render();
                        }
                }

                public void onNothingSelected(AdapterView<?> view) {
                }
            });

        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        // Ignore double-presses and bad hardware which report key events for
        // both the software keyboard and the hardware keyboard
        long now = System.currentTimeMillis();
        if (now < lastKey + 50) {
            return true;
        }
        lastKey = now;

        Word previous;

        switch (keyCode) {
        case KeyEvent.KEYCODE_SEARCH:
            BOARD.setMovementStrategy(MovementStrategy.MOVE_NEXT_CLUE);
            previous = BOARD.nextWord();
            BOARD.setMovementStrategy(getMovementStrategy());
            render(previous);

            return true;

        case KeyEvent.KEYCODE_BACK:
            this.finish();

            return true;

        case KeyEvent.KEYCODE_MENU:
            return false;

        case KeyEvent.KEYCODE_DPAD_DOWN:
            previous = BOARD.moveDown();
            render(previous);
            return true;

        case KeyEvent.KEYCODE_DPAD_UP:
            previous = BOARD.moveUp();
            render(previous);
            return true;

        case KeyEvent.KEYCODE_DPAD_LEFT:
            previous = BOARD.moveLeft();
            render(previous);
            return true;

        case KeyEvent.KEYCODE_DPAD_RIGHT:
            previous = BOARD.moveRight();
            render(previous);
            return true;

        case KeyEvent.KEYCODE_DPAD_CENTER:
            previous = BOARD.toggleDirection();
            render(previous);
            return true;

        case KeyEvent.KEYCODE_SPACE:
            if (prefs.getBoolean("spaceChangesDirection", true)) {
                previous = BOARD.toggleDirection();
                render(previous);
            } else {
                previous = BOARD.playLetter(' ');
                render(previous);
            }

            return true;

        case KeyEvent.KEYCODE_ENTER:
            if (prefs.getBoolean("enterChangesDirection", true)) {
                previous = BOARD.toggleDirection();
                render(previous);
            } else {
                previous = BOARD.nextWord();
                render(previous);
            }

            return true;

        case KeyEvent.KEYCODE_DEL:
            previous = BOARD.deleteLetter();
            render(previous);
            return true;
        }

        char c = Character
                .toUpperCase(((this.configuration.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_NO) || this.useNativeKeyboard) ? event
                        .getDisplayLabel() : ((char) keyCode));

        if (PLAYABLE_CHARS.indexOf(c) != -1) {
            previous = BOARD.playLetter(c);
            this.render(previous);

            return true;
        }

        return super.onKeyUp(keyCode, event);
    }

    //TODO: zoom
//    case R.id.context_zoom_in:
//            boardView.zoomIn();
//    fitToScreen = false;
//    render();
//    return true;
//
//    case R.id.context_zoom_out:
//            boardView.zoomOut();
//    fitToScreen = false;
//    render();
//    return true;
//
//    case R.id.context_fit_to_screen:
//    lastBoardScale = boardView.getRenderScale();
//    boardView.fitToScreen();
//    fitToScreen = true;
//    this.render();

    @SuppressWarnings("deprecation")
    private void deprecatedShowDialog(int dialog) {
        showDialog(dialog);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        this.render();
    }

    @Override
    protected void onPause() {

        if ((this.configuration.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_YES)
                || (this.configuration.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_UNDEFINED)) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(clue.getWindowToken(), 0);
            }
        }

        super.onPause();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (puz != null) {
            handleOnResume();
        }
    }

    private void handleOnResume() {
        setTitle("Words With Crosses - " + puz.getTitle() + " - " + puz.getAuthor() + " -  " + puz.getCopyright());

        BOARD.setSkipCompletedLetters(prefs.getBoolean("skipFilled", false));
        BOARD.setMovementStrategy(getMovementStrategy());

        showErrors = prefs.getBoolean("showErrors", false);
        BOARD.setShowErrors(showErrors);

        RENDERER.setHintHighlight(prefs.getBoolean("showRevealedLetters", true));

        updateClueSize();

        String clickSlopStr = prefs.getString("touchSensitivity", "3");
        try {
            int clickSlop = Integer.parseInt(clickSlopStr);
            boardView.setClickSlop(clickSlop);
        } catch (NumberFormatException e) {
            // Ignore
        }

        updateKeyboardFromPrefs();

        this.showCount = prefs.getBoolean("showCount", false);
        this.onConfigurationChanged(this.configuration);

        updateProgressBar();
        render();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (loadProgressDialog != null) {
            loadProgressDialog.dismiss();
            loadProgressDialog = null;
        }

        super.onDestroy();
    }

    public void onNotesClicked(View notesButton) {
        if (!TextUtils.isEmpty(puz.getNotes())) {
            deprecatedShowDialog(NOTES_DIALOG);
        }
    }

    private Dialog createNotesDialog() {
        String notes = puz.getNotes();

        AlertDialog.Builder notesDialogBuilder = new AlertDialog.Builder(this);
        notesDialogBuilder
            .setTitle(getResources().getString(R.string.dialog_notes_title))
            .setMessage(notes)
            .setPositiveButton(
                android.R.string.ok,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        return notesDialogBuilder.create();
    }

    private Dialog createRevealPuzzleDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder
            .setTitle(getResources().getString(R.string.reveal_puzzle_title))
            .setMessage(getResources().getString(R.string.reveal_puzzle_body))
            .setPositiveButton(
                android.R.string.ok,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        BOARD.revealPuzzle();
                        render();
                    }
                })
            .setNegativeButton(
                getResources().getString(android.R.string.cancel),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        return dialogBuilder.create();
    }

    private void updateClueSize() {
        String clueSizeStr = prefs.getString("clueSize", "12");
        try
        {
            setClueSize(Integer.parseInt(clueSizeStr));
        }
        catch (NumberFormatException e)
        {
            e.printStackTrace();
        }
    }

    private void setClueSize(int dps) {
        this.clue.setTextSize(TypedValue.COMPLEX_UNIT_SP, dps);

        if ((acrossAdapter != null) && (downAdapter != null)) {
            acrossAdapter.textSize = dps;
            acrossAdapter.notifyDataSetInvalidated();
            downAdapter.textSize = dps;
            downAdapter.notifyDataSetInvalidated();
        }
    }

    private MovementStrategy getMovementStrategy() {
        String stratName = prefs.getString("movementStrategy", "MOVE_NEXT_ON_AXIS");

        if (stratName.equals("MOVE_NEXT_ON_AXIS")) {
            return MovementStrategy.MOVE_NEXT_ON_AXIS;
        } else if (stratName.equals("STOP_ON_END")) {
            return MovementStrategy.STOP_ON_END;
        } else if (stratName.equals("MOVE_NEXT_CLUE")) {
            return MovementStrategy.MOVE_NEXT_CLUE;
        } else if (stratName.equals("MOVE_PARALLEL_WORD")) {
            return MovementStrategy.MOVE_PARALLEL_WORD;
        } else {
            LOG.warning("Invalid movement strategy: " + stratName);
            return MovementStrategy.MOVE_NEXT_ON_AXIS;
        }
    }

    private void render() {
        render(null);
    }

    private void render(Word previous) {
        if (puz == null) {
            return;
        }

        if (shouldShowKeyboard(configuration)) {
            if (this.useNativeKeyboard) {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);

                if (imm != null) {
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
                }
            } else {
                this.keyboardView.setVisibility(View.VISIBLE);
            }
        } else {
            this.keyboardView.setVisibility(View.GONE);
        }

        Clue c = BOARD.getClue();

        if (c.hint == null) {
            BOARD.toggleDirection();
            c = BOARD.getClue();
        }

        this.boardView.render(previous);
        this.boardView.requestFocus();

        // If we jumped to a new word, ensure the first letter is visible.
        // Otherwise, insure that the current letter is visible. Only necessary
        // if the cursor is currently off screen.
        if (this.prefs.getBoolean("ensureVisible", true)) {
            if ((previous != null) && previous.equals(BOARD.getCurrentWord())) {
                boardView.ensureVisible(BOARD.getHighlightLetter());
            } else {
                boardView.ensureVisible(BOARD.getCurrentWordStart());
            }
        }

        String dirStr = getResources().getString(BOARD.isAcross() ? R.string.across : R.string.down);
        StringBuilder clueText = new StringBuilder();
        clueText.append("(")
                .append(dirStr)
                .append(") ")
                .append(c.number)
                .append(". ")
                .append(c.hint);

        if (showCount) {
            clueText.append(" [")
                    .append(BOARD.getCurrentWord().length)
                    .append("]");
        }
        clue.setText(clueText.toString());

        if (this.allClues != null) {
            if (BOARD.isAcross()) {
                ClueListAdapter cla = (ClueListAdapter) this.allCluesAdapter.sections
                        .get(0);
                cla.setActiveDirection(BOARD.isAcross());
                cla.setHighlightClue(c);
                this.allCluesAdapter.notifyDataSetChanged();
                this.allClues.setSelectionFromTop(cla.indexOf(c) + 1,
                        (this.allClues.getHeight() / 2) - 50);
            } else {
                ClueListAdapter cla = (ClueListAdapter) this.allCluesAdapter.sections
                        .get(1);
                cla.setActiveDirection(!BOARD.isAcross());
                cla.setHighlightClue(c);
                this.allCluesAdapter.notifyDataSetChanged();
                this.allClues.setSelectionFromTop(
                        cla.indexOf(c) + BOARD.getAcrossClues().length + 2,
                        (this.allClues.getHeight() / 2) - 50);
            }
        }

        if (this.down != null) {
            this.downAdapter.setHighlightClue(c);
            this.downAdapter.setActiveDirection(!BOARD.isAcross());
            this.downAdapter.notifyDataSetChanged();

            if (!BOARD.isAcross() && !c.equals(this.down.getSelectedItem())) {
                if (this.down instanceof ListView) {
                    ((ListView) this.down).setSelectionFromTop(
                            this.downAdapter.indexOf(c),
                            (down.getHeight() / 2) - 50);
                } else {
                    // Gallery
                    this.down.setSelection(this.downAdapter.indexOf(c));
                }
            }
        }

        if (this.across != null) {
            this.acrossAdapter.setHighlightClue(c);
            this.acrossAdapter.setActiveDirection(BOARD.isAcross());
            this.acrossAdapter.notifyDataSetChanged();

            if (BOARD.isAcross() && !c.equals(this.across.getSelectedItem())) {
                if (across instanceof ListView) {
                    ((ListView) this.across).setSelectionFromTop(
                            this.acrossAdapter.indexOf(c),
                            (across.getHeight() / 2) - 50);
                } else {
                    // Gallery view
                    this.across.setSelection(this.acrossAdapter.indexOf(c));
                }
            }
        }

        if (puz.isSolved()) {
            Intent intent = new Intent(PlayActivity.this, PuzzleFinishedActivity.class);
            startActivity(intent);

        }
        this.boardView.requestFocus();
    }

    private void updateProgressBar() {
        if (showingProgressBar) {
            getWindow().setFeatureInt(Window.FEATURE_PROGRESS, (int)(puz.getFractionComplete() * 10000));
        }
    }
}
