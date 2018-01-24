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

public class Square {
	public enum SquareType
	{
		CLEAN,
		UP,
		RIGHT,
		UPC,
		RIGHTC,
		DOWNC,
		LEFTC,
		UP_RIGHT,
		BLOCK,
		DOUBLE_BLOCK,
		PICTURE
	};


	private String description;
	private String secondDescription;
	private String tmp;
	private String correct;
	private SquareType type;

	public Square(String description, String secondDescription, String tmp, String correct, SquareType type) {
		this.description = description;
		this.secondDescription = secondDescription;
		this.tmp = tmp;
		this.correct = correct;
		this.type = type;
	}

	public void setDescription(String value) { this.description = value; }
	public String getDescription() { return this.description; }

	public void setSecondDescription(String value) { this.secondDescription = value; }
	public String getSecondDescription() { return this.secondDescription; }

	public void setTmp(String value) { this.tmp = value; }
	public String getTmp() { return this.tmp; }

	public void setCorrect(String value) { this.correct = value; }
	public String getCorrect() { return this.correct; }

	public SquareType	getSquareType() { return this.type; }
}
