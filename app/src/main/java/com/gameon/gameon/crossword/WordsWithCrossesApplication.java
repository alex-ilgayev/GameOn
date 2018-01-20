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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;

import com.gameon.gameon.crossword.puz.Playboard;
import com.gameon.gameon.crossword.view.PlayboardRenderer;

public class WordsWithCrossesApplication extends Application {

    public static File CROSSWORDS_DIR;
    public static File NON_CROSSWORD_DATA_DIR;
    public static File TEMP_DIR;
    public static File QUARANTINE_DIR;

    public static File CACHE_DIR;
    public static File DEBUG_DIR;

    private static final Logger LOG = Logger.getLogger("import com.gameon.gameon.crossword");

    private static final String PREFERENCES_VERSION_PREF = "preferencesVersion";
    private static final int PREFERENCES_VERSION = 5;

    private static Context mContext;

    public static Playboard BOARD;
    public static PlayboardRenderer RENDERER;

    @Override
    public void onCreate() {
        super.onCreate();

        mContext = this;

        File externalStorageDir = new File(
            Environment.getExternalStorageDirectory(),
            "Android/data/" + getPackageName() + "/files");

        CROSSWORDS_DIR = new File(externalStorageDir, "crosswords");
        NON_CROSSWORD_DATA_DIR = new File(externalStorageDir, "data");
        TEMP_DIR = new File(externalStorageDir, "temp");
        QUARANTINE_DIR = new File(externalStorageDir, "quarantine");

        CACHE_DIR = getCacheDir();
        DEBUG_DIR = new File(CACHE_DIR, "debug");

        if (DEBUG_DIR.isDirectory() || DEBUG_DIR.mkdirs()) {
            File infoFile = new File(DEBUG_DIR, "device.txt");
            try {
                PrintWriter writer = new PrintWriter(infoFile);
                try {
                    writer.println("VERSION INT: " + android.os.Build.VERSION.SDK_INT);
                    writer.println("VERSION RELEASE: " + android.os.Build.VERSION.RELEASE);
                    writer.println("MODEL: " + android.os.Build.MODEL);
                    writer.println("DEVICE: " + android.os.Build.DEVICE);
                    writer.println("DISPLAY: " + android.os.Build.DISPLAY);
                    writer.println("MANUFACTURER: " + android.os.Build.MANUFACTURER);
                } finally {
                    writer.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            LOG.warning("Failed to create directory tree: " + DEBUG_DIR);
        }
    }

    public static Context getContext() {
        return mContext;
    }
}
