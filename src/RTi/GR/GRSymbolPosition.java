// GRSymbolPosition - symbol position (orientation) mask values

/* NoticeStart

CDSS Common Java Library
CDSS Common Java Library is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1994-2023 Colorado Department of Natural Resources

CDSS Common Java Library is free software:  you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

CDSS Common Java Library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

You should have received a copy of the GNU General Public License
    along with CDSS Common Java Library.  If not, see <https://www.gnu.org/licenses/>.

NoticeEnd */

package RTi.GR;

/**
Symbol position (orientation) types.
*/
public abstract class GRSymbolPosition {

	/**
	Left side of the symbol is at the coordinate.
	*/
	public static final int LEFT = 0x01;

	/**
	Centered around the X point of the coordinate
	*/
	public static final int CENTER_X = 0x02;

	/**
	Right side of the symbol is at the coordinate.
	*/
	public static final int RIGHT = 0x04;

	/**
	Bottom edge of the symbol is at the coordinate.
	*/
	public static final int BOTTOM = 0x08;

	/**
	Top edge of the symbol is at the coordinate.
	*/
	public static final int TOP = 0x10;

	/**
	Centered around the Y point of the coordinate.
	*/
	public static final int CENTER_Y = 0x20;
}