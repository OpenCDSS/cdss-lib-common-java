// Validator - determines whether some state is valid or not by returning a Status object.

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

/*****************************************************************************
Validators.java - 2007-03-26
******************************************************************************
Revisions
2007-03-21	Ian Schneider, RTi		Initial Version.
*****************************************************************************/
package RTi.Util.IO;
/**
 * A Validator determines whether some state is valid or not by returning a Status object.
 */
public interface Validator {
  
    Status validate(Object value);
    
}
