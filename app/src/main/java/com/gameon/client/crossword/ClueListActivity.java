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

package com.gameon.client.crossword;

import java.io.File;
import java.util.logging.Logger;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.inputmethodservice.KeyboardView.OnKeyboardActionListener;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.Toast;

import com.gameon.gameon.R;
import com.gameon.client.crossword.puz.Puzzle;
import com.gameon.client.crossword.puz.Playboard.Clue;
import com.gameon.client.crossword.puz.Playboard.Position;
import com.gameon.client.crossword.puz.Playboard.Word;
import com.gameon.client.crossword.view.ClueImageView;
import com.gameon.client.crossword.view.CrosswordImageView.ClickListener;

public class ClueListActivity extends WordsWithCrossesActivity {
    private Configuration configuration;
    private File baseFile;
    private KeyboardView keyboardView = null;
    private ListView across;
    private ListView down;
    private Puzzle puz;
    private ClueImageView imageView;
    private TabHost tabHost;
    private boolean useNativeKeyboard = false;
    private boolean hasSetInitialZoom = false;

    private static final Logger LOG = Logger.getLogger("com.gameon.client.crossword");

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        this.configuration = newConfig;
        try {
            if (this.prefs.getBoolean("forceKeyboard", false)
                    || (this.configuration.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_YES)
                    || (this.configuration.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_UNDEFINED)) {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);

                if (imm != null) {
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_NOT_ALWAYS);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        try {
            configuration = getBaseContext().getResources().getConfiguration();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, getResources().getString(R.string.device_configuration_error),
                    Toast.LENGTH_LONG).show();
            finish();
        }

        // Not sure how this can happen, but it's happened at least once
        if (WordsWithCrossesApplication.BOARD == null) {
            LOG.warning("ClueListActivity: BOARD is null!");
            finish();
            return;
        }

        Uri u = this.getIntent().getData();

        if (u != null) {
            if (u.getScheme().equals("file")) {
                baseFile = new File(u.getPath());
            }
        }

        puz = WordsWithCrossesApplication.BOARD.getPuzzle();
        setContentView(R.layout.clue_list);

        int keyboardType = getKeyboardTypePreference();
        useNativeKeyboard = (keyboardType == -1);
        keyboardView = (KeyboardView)this.findViewById(R.id.clueKeyboard);

        if (!useNativeKeyboard) {
            Keyboard keyboard = new Keyboard(this, keyboardType);
            keyboardView.setKeyboard(keyboard);
        } else {
            keyboardView.setVisibility(View.GONE);
        }

        keyboardView
                .setOnKeyboardActionListener(new OnKeyboardActionListener() {
                    private long lastSwipe = 0;

                    public void onKey(int primaryCode, int[] keyCodes) {
                        long eventTime = System.currentTimeMillis();

                        if ((eventTime - lastSwipe) < 500) {
                            return;
                        }

                        KeyEvent event = new KeyEvent(eventTime, eventTime,
                                KeyEvent.ACTION_DOWN, primaryCode, 0, 0, 0, 0,
                                KeyEvent.FLAG_SOFT_KEYBOARD
                                        | KeyEvent.FLAG_KEEP_TOUCH_MODE);
                        ClueListActivity.this.onKeyDown(primaryCode, event);
                    }

                    public void onPress(int primaryCode) {}

                    public void onRelease(int primaryCode){}

                    public void onText(CharSequence text) {}

                    public void swipeDown() {}

                    public void swipeLeft() {
                        long eventTime = System.currentTimeMillis();
                        lastSwipe = eventTime;

                        KeyEvent event = new KeyEvent(eventTime, eventTime,
                                KeyEvent.ACTION_DOWN,
                                KeyEvent.KEYCODE_DPAD_LEFT, 0, 0, 0, 0,
                                KeyEvent.FLAG_SOFT_KEYBOARD
                                        | KeyEvent.FLAG_KEEP_TOUCH_MODE);
                        ClueListActivity.this.onKeyDown(
                                KeyEvent.KEYCODE_DPAD_LEFT, event);
                    }

                    public void swipeRight() {
                        long eventTime = System.currentTimeMillis();
                        lastSwipe = eventTime;

                        KeyEvent event = new KeyEvent(eventTime, eventTime,
                                KeyEvent.ACTION_DOWN,
                                KeyEvent.KEYCODE_DPAD_RIGHT, 0, 0, 0, 0,
                                KeyEvent.FLAG_SOFT_KEYBOARD
                                        | KeyEvent.FLAG_KEEP_TOUCH_MODE);
                        ClueListActivity.this.onKeyDown(
                                KeyEvent.KEYCODE_DPAD_RIGHT, event);
                    }

                    public void swipeUp() {
                    }
                });

        imageView = (ClueImageView)this.findViewById(R.id.miniboard);
        imageView.setUseNativeKeyboard(useNativeKeyboard);

        imageView.setClickListener(new ClickListener() {
            public void onClick(Position pos) {
                if (pos == null) {
                    return;
                }
                Word current = WordsWithCrossesApplication.BOARD.getCurrentWord();
                int newAcross = current.start.across;
                int newDown = current.start.down;
                int box = pos.across;

                if (box >= current.length) {
                    return;
                }

                if (tabHost.getCurrentTab() == 0) {
                    newAcross += box;
                } else {
                    newDown += box;
                }

                Position newPos = new Position(newAcross, newDown);

                if (!newPos.equals(WordsWithCrossesApplication.BOARD.getHighlightLetter())) {
                    WordsWithCrossesApplication.BOARD.setHighlightLetter(newPos);
                    render();
                }
            }

            public void onDoubleClick(Position pos) {
                // No-op
            }

            public void onLongClick(Position pos) {
                // No-op
            }
        });

        this.tabHost = (TabHost)this.findViewById(R.id.tabhost);
        this.tabHost.setup();

        TabSpec ts = tabHost.newTabSpec("TAB1");

        ts.setIndicator(getResources().getString(R.string.across),
            getResources().getDrawable(R.drawable.across));

        ts.setContent(R.id.acrossList);

        this.tabHost.addTab(ts);

        ts = this.tabHost.newTabSpec("TAB2");

        ts.setIndicator(getResources().getString(R.string.down),
            getResources().getDrawable(R.drawable.down));

        ts.setContent(R.id.downList);
        this.tabHost.addTab(ts);

        this.tabHost.setCurrentTab(WordsWithCrossesApplication.BOARD.isAcross() ? 0 : 1);

        this.across = (ListView) this.findViewById(R.id.acrossList);
        this.down = (ListView) this.findViewById(R.id.downList);

        across.setAdapter(new ArrayAdapter<Clue>(this,
                android.R.layout.simple_list_item_1, WordsWithCrossesApplication.BOARD
                        .getAcrossClues()));
        across.setFocusableInTouchMode(true);
        down.setAdapter(new ArrayAdapter<Clue>(this,
                android.R.layout.simple_list_item_1, WordsWithCrossesApplication.BOARD
                        .getDownClues()));
        across.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                    long arg3) {
                arg0.setSelected(true);
                WordsWithCrossesApplication.BOARD.jumpTo(arg2, true);
                imageView.setTranslate(0.0f, 0.0f);
                render();

                if (prefs.getBoolean("snapClue", false)) {
                    across.setSelectionFromTop(arg2, 5);
                    across.setSelection(arg2);
                }
            }
        });
        across.setOnItemSelectedListener(new OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                    int arg2, long arg3) {
                if (!WordsWithCrossesApplication.BOARD.isAcross()
                        || (WordsWithCrossesApplication.BOARD.getCurrentClueIndex() != arg2)) {
                    WordsWithCrossesApplication.BOARD.jumpTo(arg2, true);
                    imageView.setTranslate(0.0f, 0.0f);
                    render();

                    if (prefs.getBoolean("snapClue", false)) {
                        across.setSelectionFromTop(arg2, 5);
                        across.setSelection(arg2);
                    }
                }
            }

            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
        down.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> arg0, View arg1,
                    final int arg2, long arg3) {
                WordsWithCrossesApplication.BOARD.jumpTo(arg2, false);
                imageView.setTranslate(0.0f, 0.0f);
                render();

                if (prefs.getBoolean("snapClue", false)) {
                    down.setSelectionFromTop(arg2, 5);
                    down.setSelection(arg2);
                }
            }
        });

        down.setOnItemSelectedListener(new OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                    int arg2, long arg3) {
                if (WordsWithCrossesApplication.BOARD.isAcross()
                        || (WordsWithCrossesApplication.BOARD.getCurrentClueIndex() != arg2)) {
                    WordsWithCrossesApplication.BOARD.jumpTo(arg2, false);
                    imageView.setTranslate(0.0f, 0.0f);
                    render();

                    if (prefs.getBoolean("snapClue", false)) {
                        down.setSelectionFromTop(arg2, 5);
                        down.setSelection(arg2);
                    }
                }
            }

            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
        this.render();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (!hasSetInitialZoom) {
            imageView.fitToHeight();
            hasSetInitialZoom = true;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Word w = WordsWithCrossesApplication.BOARD.getCurrentWord();
        Position last = new Position(w.start.across
                + (w.across ? (w.length - 1) : 0), w.start.down
                + ((!w.across) ? (w.length - 1) : 0));

        switch (keyCode) {
        case KeyEvent.KEYCODE_MENU:
            return false;

        case KeyEvent.KEYCODE_BACK:
            this.setResult(0);

            return true;

        case KeyEvent.KEYCODE_DPAD_LEFT:

            if (!WordsWithCrossesApplication.BOARD.getHighlightLetter().equals(
                    WordsWithCrossesApplication.BOARD.getCurrentWord().start)) {
                WordsWithCrossesApplication.BOARD.previousLetter();

                this.render();
            }

            return true;

        case KeyEvent.KEYCODE_DPAD_RIGHT:

            if (!WordsWithCrossesApplication.BOARD.getHighlightLetter().equals(last)) {
                WordsWithCrossesApplication.BOARD.nextLetter();
                this.render();
            }

            return true;

        case KeyEvent.KEYCODE_DEL:
            w = WordsWithCrossesApplication.BOARD.getCurrentWord();
            WordsWithCrossesApplication.BOARD.deleteLetter();

            Position p = WordsWithCrossesApplication.BOARD.getHighlightLetter();

            if (!w.checkInWord(p.across, p.down)) {
                WordsWithCrossesApplication.BOARD.setHighlightLetter(w.start);
            }

            this.render();

            return true;

        case KeyEvent.KEYCODE_SPACE:

            if (!prefs.getBoolean("spaceChangesDirection", true)) {
                WordsWithCrossesApplication.BOARD.playLetter(' ');

                Position curr = WordsWithCrossesApplication.BOARD.getHighlightLetter();

                if (!WordsWithCrossesApplication.BOARD.getCurrentWord().equals(w)
                        || (WordsWithCrossesApplication.BOARD.getBoxes()[curr.down][curr.across] == null)) {
                    WordsWithCrossesApplication.BOARD.setHighlightLetter(last);
                }

                this.render();

                return true;
            }
        }

        char c = Character
                .toUpperCase(((this.configuration.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_NO) || this.useNativeKeyboard) ? event
                        .getDisplayLabel() : ((char) keyCode));

        if (PlayActivity.PLAYABLE_CHARS.indexOf(c) != -1) {
            WordsWithCrossesApplication.BOARD.playLetter(c);

            Position p = WordsWithCrossesApplication.BOARD.getHighlightLetter();

            if (!WordsWithCrossesApplication.BOARD.getCurrentWord().equals(w)
                    || (WordsWithCrossesApplication.BOARD.getBoxes()[p.down][p.across] == null)) {
                WordsWithCrossesApplication.BOARD.setHighlightLetter(last);
            }

            this.render();

            if (puz.isSolved()) {
                Intent i = new Intent(ClueListActivity.this, PuzzleFinishedActivity.class);
                this.startActivity(i);

            }

            return true;
        }

        return super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
        case KeyEvent.KEYCODE_BACK:
            this.finish();

            return true;
        }

        return super.onKeyUp(keyCode, event);
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (shouldShowKeyboard(configuration)) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(this.imageView.getWindowToken(), 0);
            }
        }
    }

    private void render() {
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

        imageView.render();
    }
}
