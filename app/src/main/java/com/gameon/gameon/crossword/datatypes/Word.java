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

package com.gameon.gameon.crossword.datatypes;

import android.graphics.Bitmap;
import android.util.EventLogTags;

public class Word {
	public enum DescriptionType {
		NORMAL,
		PIC_2x2,
		PIC_3x3,
		PIC_4x4,
	}

	private int 			mX;
	private int 			mY;
	private int 			mLength;
	private String 			mTmp;
	private String 			mText;
	private String 			mDescription;
	private boolean 		mHorizontal;
	private int 			mDescriptionX;
	private int 			mDescriptionY;
	private DescriptionType mDescType;
	private Bitmap			mPicture;

	public Word(int x, int y, String text, String description, boolean isHoriz,
				int descriptionX, int descriptionY, DescriptionType descType,
				Bitmap picture) {
		this.mX = x;
		this.mY = y;
		this.mText = text;
		this.mDescription = description;
		this.mHorizontal = isHoriz;
		this.mLength = text.length();
		this.mDescriptionX = descriptionX;
		this.mDescriptionY = descriptionY;
		this.mDescType = descType;
		this.mPicture = picture;
	}

	public void 	setText(String value) { this.mText = value; this.mLength = value.length(); }
	public String 	getmText() { return this.mText; }
	
	public void 	setTmp(String value) { this.mTmp = value; }
	public String 	getTmp() { return this.mTmp; }
	
	public void 	setDescription(String value) { this.mDescription = value; }
	public String 	getDescription() { return this.mDescription; }

	public void 	setDescriptionX(int value) { this.mDescriptionX = value; }
	public int 		getDescriptionX() { return this.mDescriptionX; }

	public void 	setDescriptionY(int value) { this.mDescriptionY = value; }
	public int 		getDescriptionY() { return this.mDescriptionY; }

	public boolean 	getHorizontal() { return this.mHorizontal; }
	public void 	setHorizontal(boolean value) { this.mHorizontal = value; }

	public void 	setX(int value) { this.mX = value; }
	public int 		getX() { return this.mX; }
	public int 		getXMax() { return this.mHorizontal ? this.mX + this.mLength - 1: this.mX; }
	
	public void 	setY(int value) { this.mY = value; }
	public int 		getY() { return this.mY; }
	public int 		getYMax() { return this.mHorizontal ? this.mY : this.mY + this.mLength - 1; }

	public int 		getLength() { return this.mLength; }

	public DescriptionType getDescriptionType() { return this.mDescType; }
	public void 	setHorizontal(DescriptionType value) { this.mDescType = value; }

	public Bitmap 	getPicture() { return this.mPicture; }
	public void 	setPicture(Bitmap value) { this.mPicture = value; }

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Word word = (Word) o;

		if (mLength != word.mLength) return false;
		return mText != null ? mText.equals(word.mText) : word.mText == null;

	}

	@Override
	public int hashCode() {
		int result = mLength;
		result = 31 * result + (mText != null ? mText.hashCode() : 0);
		return result;
	}
}
