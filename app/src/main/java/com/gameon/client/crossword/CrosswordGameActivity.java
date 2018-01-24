package com.gameon.client.crossword;

import android.app.Activity;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gameon.gameon.R;
import com.gameon.client.controller.Settings;
import com.gameon.client.crossword.adapter.GameGridAdapter;
import com.gameon.client.crossword.datatypes.Grid;
import com.gameon.client.crossword.datatypes.Word;
import com.gameon.client.crossword.keyboard.KeyboardView;
import com.gameon.client.crossword.keyboard.KeyboardViewInterface;

import java.util.ArrayList;

public class CrosswordGameActivity extends Activity implements View.OnTouchListener, KeyboardViewInterface {

    public enum GRID_MODE {NORMAL, CHECK, SOLVE};
    public GRID_MODE                    currentMode = GRID_MODE.NORMAL;

    private GridView                    mGridView;
    private KeyboardView                mKeyboardView;
    private GameGridAdapter             mGridAdapter;
    private TextView                    mTxtDescription;
    private TextView                    mKeyboardOverlay;

    private Grid                        mGrid;
    private ArrayList<Word>             mEntries;
    private ArrayList<View>             mSelectedArea = new ArrayList<View>();

    private boolean                     mDownIsPlayable;
    private int                         mDownPos;
    private int                         mDownX;
    private int                         mDownY;
    private int                         mCurrentPos;
    private int                         mCurrentX;
    private int                         mCurrentY;
    private Word                        mCurrentWord;
    private boolean                     mHorizontal;

    private int                         mWidth;
    private int                         mHeight;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crossword_game);

        this.mGrid = new Grid(1, "01/01/2016", "me", 5, 5);
        this.mEntries = new ArrayList<>();
        mEntries.add(new Word(2, 0, "aaa", "z", true, 0, 0, Word.DescriptionType.PIC_2x2, null));
        mEntries.add(new Word(4, 2, "aba", "c", false, 4, 1, Word.DescriptionType.NORMAL, null));
        mEntries.add(new Word(3, 2, "aba", "d", false, 3, 1, Word.DescriptionType.NORMAL, null));
        mEntries.add(new Word(2, 2, "aba", "e", false, 2, 1, Word.DescriptionType.NORMAL, null));
        mEntries.add(new Word(0, 2, "ababa", "h", true, 0, 3, Word.DescriptionType.PIC_2x2, null));



//        mEntries.add(new Word(1, 0, "aaa", "z", true, 0, 0, Word.DescriptionType.NORMAL, null));
//        mEntries.add(new Word(0, 2, "ab", "a", false, 0, 1, Word.DescriptionType.NORMAL, null));
//        mEntries.add(new Word(1, 4, "abab", "b", true, 0, 4, Word.DescriptionType.NORMAL, null));
//        mEntries.add(new Word(4, 2, "aba", "c", false, 4, 1, Word.DescriptionType.NORMAL, null));
//        mEntries.add(new Word(3, 2, "aba", "d", false, 3, 1, Word.DescriptionType.NORMAL, null));
//        mEntries.add(new Word(2, 2, "aba", "e", false, 2, 1, Word.DescriptionType.NORMAL, null));
//        mEntries.add(new Word(1, 2, "a", "f", false, 1, 1, Word.DescriptionType.NORMAL, null));
//        mEntries.add(new Word(2, 3, "aba", "g", true, 1, 3, Word.DescriptionType.NORMAL, null));

        this.mWidth = this.mGrid.getWidth();
        this.mHeight = this.mGrid.getHeight();

        Display display = getWindowManager().getDefaultDisplay();
        int height = display.getHeight();
        int keyboardHeight = (int)(height / 4.4);

        this.mTxtDescription = (TextView)findViewById(R.id.description);

        this.mGridView = (GridView)findViewById(R.id.grid);
        this.mGridView.setOnTouchListener(this);
        this.mGridView.setNumColumns(this.mWidth);
        android.view.ViewGroup.LayoutParams gridParams = this.mGridView.getLayoutParams();
        gridParams.height = height - keyboardHeight - this.mTxtDescription.getLayoutParams().height;
        this.mGridView.setLayoutParams(gridParams);
        this.mGridView.setVerticalScrollBarEnabled(false);
        this.mGridAdapter = new GameGridAdapter(this, this.mEntries, this.mWidth, this.mHeight);
        this.mGridView.setAdapter(this.mGridAdapter);

        this.mKeyboardView = (KeyboardView)findViewById(R.id.keyboard);
        this.mKeyboardView.setDelegate(this);
        android.view.ViewGroup.LayoutParams KeyboardParams = this.mKeyboardView.getLayoutParams();
        KeyboardParams.height = keyboardHeight;
        this.mKeyboardView.setLayoutParams(KeyboardParams);

        this.mKeyboardOverlay = (TextView)findViewById(R.id.keyboard_overlay);

        // making some ugly hack here.
        // going over all the pictures and overwriting them on relatvie layout on top of grid view.
        int singleHeight = display.getWidth() / this.mWidth;

        RelativeLayout pics = (RelativeLayout) findViewById(R.id.gridPics);

        int imageHeight, imageWidth;
        for(Word w: mEntries) {
            switch(w.getDescriptionType()) {
                case PIC_2x2:
                    imageHeight = imageWidth = singleHeight * 2;
                case PIC_3x3:
                    imageHeight = imageWidth = singleHeight * 3;
                case PIC_4x4:
                    imageHeight = imageWidth = singleHeight * 2;

                    ImageView iv = new ImageView(this);
                    RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(imageWidth, imageHeight);
                    lp.setMargins(w.getDescriptionX() * singleHeight, w.getDescriptionY() * singleHeight, 0, 0);
                    iv.setLayoutParams(lp);
                    iv.setImageDrawable(getResources().getDrawable(R.drawable.pic));
                    iv.setBackgroundResource(R.drawable.area_block);
                    iv.setRotationY(180);
                    pics.addView(iv);
            }
        }
    }

    @Override
    public void onKeyDown(String value, int[] location, int width) {
        System.out.println("onKeyDown: " + value + ", insert in: " + mCurrentX + "x" + mCurrentY);

        // Deplace l'overlay du clavier
        if (value.equals(" ") == false) {
            int offsetX = (this.mKeyboardOverlay.getWidth() - width) / 2;
            int offsetY = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, Settings.KEYBOARD_OVERLAY_OFFSET, getResources().getDisplayMetrics());
            FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams)this.mKeyboardOverlay.getLayoutParams();
            lp.leftMargin = location[0] - offsetX;
            lp.topMargin = location[1] - offsetY;
            this.mKeyboardOverlay.setLayoutParams(lp);
            this.mKeyboardOverlay.setText(value);
            this.mKeyboardOverlay.clearAnimation();
            this.mKeyboardOverlay.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onKeyUp(String value) {
        System.out.println("onKeyUp: " + value + ", insert in: " + mCurrentX + "x" + mCurrentY);

        if (value.equals(" ") == false) {
            this.mKeyboardOverlay.setAnimation(AnimationUtils.loadAnimation(this, R.anim.keyboard_overlay_fade_out));
            this.mKeyboardOverlay.setVisibility(View.INVISIBLE);
        }

        if (this.mCurrentWord == null)
            return;

        int x = this.mCurrentX;
        int y = this.mCurrentY;

        if (this.mGridAdapter.isBlock(x, y))
            return;

        this.mGridAdapter.setValue(x, y, value);
        this.mGridAdapter.notifyDataSetChanged();

        if (value.equals(" ")) {
            x = (this.mHorizontal ? x - 1 : x);
            y = (this.mHorizontal ? y: y - 1);
        }
        else
        {
            x = (this.mHorizontal ? x + 1 : x);
            y = (this.mHorizontal ? y: y + 1);
        }

        if (x >= 0 && x < this.mWidth
                && y >= 0 && y < this.mHeight
                && this.mGridAdapter.isBlock(x, y) == false) {
            this.mGridView.getChildAt(y * this.mWidth + x).setBackgroundResource(R.drawable.area_current);
            this.mGridView.getChildAt(this.mCurrentY * this.mWidth + this.mCurrentX).setBackgroundResource(R.drawable.area_selected);
            this.mCurrentX = x;
            this.mCurrentY = y;
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        switch (motionEvent.getAction())
        {
            case MotionEvent.ACTION_DOWN:
            {
                int position = this.mGridView.pointToPosition((int)motionEvent.getX(), (int)motionEvent.getY());
                View child = this.mGridView.getChildAt(position);

                // Si pas de mot sur cette case (= case noire), aucun traitement
                if (child == null || child.getTag().equals(GameGridAdapter.AREA_BLOCK)) {
                    clearSelection();
                    this.mGridAdapter.notifyDataSetChanged();

                    this.mDownIsPlayable = false;
                    return true;
                }
                this.mDownIsPlayable = true;

                this.mDownPos = position;
                this.mDownX = this.mDownPos % this.mWidth;
                this.mDownY = this.mDownPos / this.mWidth;
                System.out.println("ACTION_DOWN, x:" + this.mDownX + ", y:" + this.mDownY + ", position: " + this.mDownPos);

                clearSelection();

                child.setBackgroundResource(R.drawable.area_selected);
                mSelectedArea.add(child);

                this.mGridAdapter.notifyDataSetChanged();
                break;
            }

            case MotionEvent.ACTION_UP:
            {
                // Si le joueur à appuyé sur une case noire, aucun traitement
                if (this.mDownIsPlayable == false)
                    return true;

                int position = this.mGridView.pointToPosition((int)motionEvent.getX(), (int)motionEvent.getY());
                int x = position % this.mWidth;
                int y = position / this.mWidth;
                System.out.println("ACTION_DOWN, x:" + x + ", y:" + y + ", position: " + position);

                // Si clique sur la case, inversion horizontale <> verticale
                // Si clique sur une autre case (= mouvement) calcul en fonction de la gesture
                if (this.mDownPos == position && this.mCurrentPos == position)
                {
                    this.mHorizontal = !this.mHorizontal;
                }
                else if (this.mDownPos != position)
                {
                    this.mHorizontal = (Math.abs(this.mDownX - x) > Math.abs(this.mDownY - y));
                }

                // Test si un mot se trouve sur cette case
                this.mCurrentWord = getWord(this.mDownX, this.mDownY, this.mHorizontal);
                if (this.mCurrentWord == null)
                    break;

                // Force la direction a etre dans le meme sens que le mot
                this.mHorizontal = this.mCurrentWord.getHorizontal();

                // Si clique sur la case, place le curseur sur le mot
                // Sinon place le curseur au debut du mot
                if (this.mDownPos == position)
                {
                    this.mCurrentX = this.mDownX;
                    this.mCurrentY = this.mDownY;
                    this.mCurrentPos = position;
                }
                else
                {
                    this.mCurrentX = this.mCurrentWord.getX();
                    this.mCurrentY = this.mCurrentWord.getY();
                    this.mCurrentPos = this.mCurrentY * this.mWidth + this.mCurrentX;
                }

                this.mTxtDescription.setText(this.mCurrentWord.getDescription());

                // Set background color
                boolean horizontal = this.mCurrentWord.getHorizontal();
                for (int l = 0; l < this.mCurrentWord.getLength(); l++) {
                    int index = this.mCurrentWord.getY() * this.mWidth + this.mCurrentWord.getX() + (l * (horizontal ? 1 : this.mWidth));
                    View currentChild = this.mGridView.getChildAt(index);
                    if (currentChild != null) {
                        currentChild.setBackgroundResource(index == this.mCurrentPos ? R.drawable.area_current : R.drawable.area_selected);
                        mSelectedArea.add(currentChild);
                    }
                }

                this.mGridAdapter.notifyDataSetChanged();
                break;
            }
        }
        // if you return false, these actions will not be recorded
        return true;
    }

    private void clearSelection() {
        for (View selected: mSelectedArea)
            selected.setBackgroundResource(R.drawable.area_empty);
        mSelectedArea.clear();
    }

    private Word getWord(int x, int y, boolean horizontal)
    {
        Word horizontalWord = null;
        Word verticalWord = null;
        for (Word entry: this.mEntries) {
            if (x >= entry.getX() && x <= entry.getXMax())
                if (y >= entry.getY() && y <= entry.getYMax()) {
                    if (entry.getHorizontal())
                        horizontalWord = entry;
                    else
                        verticalWord = entry;
                }
        }

        if (horizontal)
            return (horizontalWord != null) ? horizontalWord : verticalWord;
        else
            return (verticalWord != null) ? verticalWord : horizontalWord;
    }
}
