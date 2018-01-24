/*
 * Copyright 2011 Alexis Lauper <alexis.lauper@gmail.com>
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of 
 * the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.gameon.client.crossword.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.preference.PreferenceManager;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.gameon.gameon.R;
import com.gameon.client.crossword.CrosswordGameActivity;
import com.gameon.client.crossword.datatypes.Square;
import com.gameon.client.crossword.datatypes.Word;

import java.util.ArrayList;
import java.util.HashMap;

public class GameGridAdapter extends BaseAdapter {

	public static final int 			AREA_BLOCK = -1;
	public static final int 			AREA_WRITABLE = 0;

	private HashMap<Integer, TextView> mViews = new HashMap<Integer, TextView>();
	private Context 					mContext;
	private Square[][] 					mSquareArea;
	private int 						mDisplayHeight;
	private int 						mWidth;
	private int 						mHeight;

	public GameGridAdapter(Activity act, ArrayList<Word> entries, int width, int height) {
		final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(act);
		this.mContext = (Context)act;
		this.mWidth = width;
		this.mHeight = height;

        Display display = act.getWindowManager().getDefaultDisplay();
        this.mDisplayHeight = display.getWidth() / this.mWidth;

		this.mSquareArea = new Square[this.mHeight][this.mWidth];

	    for (Word entry: entries) {
	    	String tmp = entry.getTmp();
	    	String text = entry.getmText();
	    	boolean horizontal = entry.getHorizontal();
	    	int x = entry.getX();
	    	int y = entry.getY();

			int descX = entry.getDescriptionX();
			int descY = entry.getDescriptionY();

	    	for (int i = 0; i < entry.getLength(); i++) {
	    		if (horizontal)
	    		{
	    			if (y >= 0 && y < this.mHeight && x+i >= 0 && x+i < this.mWidth)
	    			{
						if(i != 0) {
							mSquareArea[y][x+i] = new Square(null, null, (tmp != null ? String.valueOf(tmp.charAt(i)) : " "),
									String.valueOf(text.charAt(i)), Square.SquareType.CLEAN);
						}
	    			}
	    		}
	    		else
	    		{
	    			if (y+i >= 0 && y+i < this.mHeight && x >= 0 && x < this.mWidth)
	    			{
						if(i != 0) {
							mSquareArea[y+i][x] = new Square(null, null, (tmp != null ? String.valueOf(tmp.charAt(i)) : " "),
									String.valueOf(text.charAt(i)), Square.SquareType.CLEAN);
						}
	    			}
	    		}
	    	}

			//for i==0
			Square.SquareType type;
			if(mSquareArea[y][x] == null || mSquareArea[y][x].getSquareType() == Square.SquareType.CLEAN) {
				if (horizontal) {
					if (descY == y)
						type = Square.SquareType.RIGHT;
					else if (descY < y)
						type = Square.SquareType.UPC;
					else
						type = Square.SquareType.DOWNC;
				} else {
					if (descX == x)
						type = Square.SquareType.UP;
					else if (descX < x)
						type = Square.SquareType.RIGHTC;
					else
						type = Square.SquareType.LEFTC;
				}
			} else {
				type = Square.SquareType.UP_RIGHT;
			}
			mSquareArea[y][x] = new Square(null, null, (tmp != null ? String.valueOf(tmp.charAt(0)) : " "),
					String.valueOf(text.charAt(0)), type);

			switch(entry.getDescriptionType()) {
				case NORMAL:
					mSquareArea[descY][descX] = new Square(entry.getDescription(), null, null, null, Square.SquareType.BLOCK);
					break;
				case PIC_4x4:
					mSquareArea[descY+3][descX+3] = new Square("", null, null, null, Square.SquareType.BLOCK);
					mSquareArea[descY+3][descX+2] = new Square("", null, null, null, Square.SquareType.BLOCK);
					mSquareArea[descY+3][descX+1] = new Square("", null, null, null, Square.SquareType.BLOCK);
					mSquareArea[descY+3][descX] = new Square("", null, null, null, Square.SquareType.BLOCK);
					mSquareArea[descY+2][descX+3] = new Square("", null, null, null, Square.SquareType.BLOCK);
					mSquareArea[descY+1][descX+3] = new Square("", null, null, null, Square.SquareType.BLOCK);
					mSquareArea[descY][descX+3] = new Square("", null, null, null, Square.SquareType.BLOCK);
				case PIC_3x3:
					mSquareArea[descY+2][descX+2] = new Square("", null, null, null, Square.SquareType.BLOCK);
					mSquareArea[descY+2][descX+1] = new Square("", null, null, null, Square.SquareType.BLOCK);
					mSquareArea[descY+2][descX] = new Square("", null, null, null, Square.SquareType.BLOCK);
					mSquareArea[descY+1][descX+2] = new Square("", null, null, null, Square.SquareType.BLOCK);
					mSquareArea[descY][descX+2] = new Square("", null, null, null, Square.SquareType.BLOCK);
				case PIC_2x2:
					mSquareArea[descY][descX] = new Square("", null, null, null, Square.SquareType.BLOCK);
					mSquareArea[descY][descX+1] = new Square("", null, null, null, Square.SquareType.BLOCK);
					mSquareArea[descY+1][descX] = new Square("", null, null, null, Square.SquareType.BLOCK);
					mSquareArea[descY+1][descX+1] = new Square("", null, null, null, Square.SquareType.BLOCK);
			}
	    }
	}
	
	@Override
	public int getCount() {
		return this.mHeight * this.mWidth;
	}

	@Override
	public Object getItem(int position) {
		int y = (int)(position / this.mWidth);
		int x = (int)(position % this.mWidth);
		return mSquareArea[y][x];
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		TextView tv = this.mViews.get(position);
		int y = (int)(position / this.mWidth);
		int x = (int)(position % this.mWidth);
		Square square = this.mSquareArea[y][x];
		String data = this.mSquareArea[y][x].getTmp();
		String correction = this.mSquareArea[y][x].getCorrect();

		if (tv == null)
		{
			RelativeLayout v = new RelativeLayout(mContext);
			v.setLayoutParams(new GridView.LayoutParams(GridView.LayoutParams.FILL_PARENT, this.mDisplayHeight));
			v.setGravity(Gravity.CENTER);
			v.setRotationY(180);

			tv = new TextView(mContext);
			tv.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
			tv.setTextSize((mContext.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == 4 ? 30 : 20);
			tv.setGravity(Gravity.CENTER);
			v.addView(tv);

			if (data != null) {
				v.setBackgroundResource(R.drawable.area_empty);
				v.setTag(AREA_WRITABLE);

				if(square.getSquareType() != Square.SquareType.CLEAN) {
					ImageView iv = new ImageView(mContext);
					iv.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

					int drawable;
					switch (square.getSquareType()) {
						case UP:
							drawable = R.drawable.up;
							break;
						case RIGHT:
							drawable = R.drawable.right;
							break;
						case UPC:
							drawable = R.drawable.upc;
							break;
						case RIGHTC:
							drawable = R.drawable.rightc;
							break;
						case DOWNC:
							drawable = R.drawable.downc;
							break;
						case LEFTC:
							drawable = R.drawable.leftc;
							break;
						case UP_RIGHT:
							drawable = R.drawable.up_right;
							break;
						default:
							drawable = R.drawable.up;
					}

					iv.setImageDrawable(mContext.getResources().getDrawable(drawable));
					v.addView(iv);
				}
			} else {
				v.setBackgroundResource(R.drawable.area_block);
				v.setTag(AREA_BLOCK);

				if(square.getSquareType() == Square.SquareType.BLOCK) {
					tv.setTextColor(mContext.getResources().getColor(R.color.block_text_normal));
					tv.setText(square.getDescription());
				}
			}


			this.mViews.put(position, tv);
		}

		if (((CrosswordGameActivity) mContext).currentMode == CrosswordGameActivity.GRID_MODE.CHECK)
		{
			if (data != null) {
				tv.setTextColor(mContext.getResources().getColor(data.equalsIgnoreCase(correction) ? R.color.normal : R.color.wrong));
				tv.setText(data);
			}
		}
		else if (((CrosswordGameActivity)this.mContext).currentMode == CrosswordGameActivity.GRID_MODE.SOLVE)
		{
			if (data != null && data.equalsIgnoreCase(correction)) {
				tv.setTextColor(mContext.getResources().getColor(R.color.normal));
				tv.setText(data);
			} else if (correction != null) {
				tv.setTextColor(mContext.getResources().getColor(R.color.right));
				tv.setText(correction);
			}
		}
		else
		{
			if (data != null) {
				tv.setTextColor(mContext.getResources().getColor(R.color.normal));
				tv.setText(data);
			}
		}

		return (View) tv.getParent();
	}

	public int getPercent() {
		int filled = 0;
		int empty = 0;

		for (int y = 0; y < this.mHeight; y++)
			for (int x = 0; x < this.mWidth; x++)
				if (this.mSquareArea[y][x].getTmp() != null) {
					if (this.mSquareArea[y][x].getTmp().equals(" "))
						empty++;
					else
						filled++;
				}
		return filled * 100 / (empty + filled);
	}

	public boolean isBlock(int x, int y) {
		return this.mSquareArea[y][x].getSquareType() == Square.SquareType.BLOCK;
	}

	public void setValue(int x, int y, String value) {
		if(mSquareArea[y][x].getSquareType() != Square.SquareType.BLOCK) {
			mSquareArea[y][x].setTmp(value);
		}
	}

	public String getWord(int x, int y, int length, boolean isHorizontal) {
    	StringBuffer word = new StringBuffer();
    	for (int i = 0; i < length; i++) {
    		if (isHorizontal) {
    			if (y < this.mHeight && x+i < this.mWidth)
    				word.append(this.mSquareArea[y][x+i].getTmp() != null ?
							this.mSquareArea[y][x+i].getTmp() : " ");
    		}
    		else {
    			if (y+i < this.mHeight && x < this.mWidth)
    				word.append(this.mSquareArea[y+i][x].getTmp() != null ?
							this.mSquareArea[y+i][x].getTmp() : " ");
    		}
    	}
    	return word.toString();
	}
}
