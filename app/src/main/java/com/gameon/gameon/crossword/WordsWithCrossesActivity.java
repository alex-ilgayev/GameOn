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

import java.util.logging.Logger;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.preference.PreferenceManager;

import com.gameon.gameon.R;

public abstract class WordsWithCrossesActivity extends Activity {
    protected SharedPreferences prefs;

    private boolean useUserOrientation = true;

    protected static final Logger LOG = Logger.getLogger("com.gameon.gameon.crossword");

    // Preference key for the time of the last database sync
    protected static final String PREF_LAST_DB_SYNC_TIME = "last_db_sync_time";

    public WordsWithCrossesActivity() {
        // No-op
    }

    public WordsWithCrossesActivity(boolean useUserOrientation) {
        this.useUserOrientation = useUserOrientation;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.prefs = PreferenceManager.getDefaultSharedPreferences(this);

        if (useUserOrientation) {
            doOrientation();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (useUserOrientation) {
            doOrientation();
        }
    }

    private void doOrientation() {
        if ("PORT".equals(prefs.getString("orientationLock", "UNLOCKED"))) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else if ("LAND"
                .equals(prefs.getString("orientationLock", "UNLOCKED"))) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        }
    }

    public SharedPreferences getPrefs()
    {
        return prefs;
    }

    protected boolean shouldShowKeyboard(Configuration config) {
        String showKeyboard = prefs.getString("showKeyboard", "AUTO");
        if ("AUTO".equals(showKeyboard)) {
            return (config.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_YES ||
                    config.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_UNDEFINED);
        } else if ("SHOW".equals(showKeyboard)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Gets the resource ID of the user's preferred keyboard type, based on
     * the current preference setting
     */
    protected int getKeyboardTypePreference() {
        String keyboardPref = prefs.getString("keyboardType", "");
        if ("CONDENSED".equals(keyboardPref)) {
            return R.xml.keyboard;
        } else if ("CONDENSED_ARROWS".equals(keyboardPref)) {
            return R.xml.keyboard_dpad;
        } else if ("ARROWS_AND_NUMBERS".equals(keyboardPref)) {
            return R.xml.keyboard_dpad_nums;
        } else {
            return -1;
        }
    }

    /**
     * Updates our record of the last time we synced the database with the
     * file system
     */
    public void updateLastDatabaseSyncTime() {
        long folderTimestamp = WordsWithCrossesApplication.CROSSWORDS_DIR.lastModified();
        Editor e = prefs.edit();
        e.putLong(PREF_LAST_DB_SYNC_TIME, folderTimestamp);
        e.commit();
    }
}
