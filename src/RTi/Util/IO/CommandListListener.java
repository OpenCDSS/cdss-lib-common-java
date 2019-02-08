// CommandListListener - interface for a listener for basic changes to Command lists

/* NoticeStart

CDSS Common Java Library
CDSS Common Java Library is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1994-2019 Colorado Department of Natural Resources

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

package RTi.Util.IO;

/**
This interface provides a listener for basic changes to Command lists.  It
can be used, for example, to allow domain-specific classes to notify UI
classes when commands have been added, removed, or changed, to allow appropriate
display changes to occur.
*/
public interface CommandListListener {

/**
Indicate when one or more commands have been added.
@param index0 The index (0+) of the first command that is added.
@param index1 The index (0+) of the last command that is added.
*/
public void commandAdded ( int index0, int index1 );

/**
Indicate when one or more commands have changed, for example in definition
or status.
@param index0 The index (0+) of the first command that is changed.
@param index1 The index (0+) of the last command that is changed.
*/
public void commandChanged ( int index0, int index1 );

/**
Indicate when one or more commands have been removed.
@param index0 The index (0+) of the first command that is removed.
@param index1 The index (0+) of the last command that is removed.
*/
public void commandRemoved ( int index0, int index1 );
	
}
