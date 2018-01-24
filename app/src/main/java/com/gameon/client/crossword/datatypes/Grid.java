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

package com.gameon.client.crossword.datatypes;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class Grid implements Comparable<Grid> {
	private int 		mId;
	private Date 		mDate;
	private String 		mAuthor;
	private int 		mWidth;
	private int 		mHeight;

	public Grid(int id, String rawDate, String author, int width, int height) {
		this.mId = id;
		this.mAuthor = author;
		this.mWidth = width;
		this.mHeight = height;
		try {
			this.mDate = new SimpleDateFormat("dd/MM/yyyy").parse(rawDate);
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	public Date getDate() {
		return this.mDate;
	}

	public void setDate(Date mDate) {
		this.mDate = mDate;
	}

	public String getAuthor() {
		return mAuthor;
	}

	public void setAuthor(String mAuthor) {
		this.mAuthor = mAuthor;
	}

	public void setWidth(int value) {
		this.mWidth = value;
	}

	public void setHeight(int value) {
		this.mHeight = value;
	}

	public int getWidth() {
		return this.mWidth;
	}

	public int getHeight() {
		return this.mHeight;
	}

	@Override
	public int compareTo(Grid arg) {
		if (arg.getDate() == null)
			return -1;
		if (this.mDate == null)
			return 1;

		return this.mDate.before(arg.getDate()) ? 1 : -1;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Grid grid = (Grid) o;

		return mId == grid.mId;

	}

	@Override
	public int hashCode() {
		return mId;
	}
}
