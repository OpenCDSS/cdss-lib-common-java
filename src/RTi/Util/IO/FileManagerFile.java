// FileManagerFile - class to manage an a single file used with the FileManager

/* NoticeStart

CDSS Common Java Library
CDSS Common Java Library is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1994-2025 Colorado Department of Natural Resources

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

import java.io.File;

import RTi.Util.Time.DateTime;

/**
 * The FileManagerFile class corresponds to a file resource that is managed by the FileManager, including:
 * <ul>
 * <li>  File instance corresponding to a file.</li>
 * <li>  Whether a temporary or cached file.</li>
 * <li>  If a cached file, the expiration time.</li>
 * </ul>
 * The files that are managed can be persisted to a file so that the state of the FileManager can be initialized in the next session.
 * The FileManagerUI can be used to display and handle files, such as clearing temporary or cached files.
 * Information fields are meant to display information but are not used internally for any functionality.
 */
public class FileManagerFile {

	/**
	 * Component scope, can be any string but the string values from FileManagerScopeType are recommended.
	 */
	private String componentScope = "";
	
	/**
	 * Component name, for example the application, datastore, plugin, or command name.
	 */
	private String componentName = "";

	/**
	 * Data type for the file.
	 */
	private String dataType = "";
	
	/**
	 * File being managed.
	 */
	private File file = null;

	/**
	 * Type of file resource
	 */
	private FileManagerFileType type = null;

	/**
	 * Expiration date/time.
	 */
	private DateTime expirationTime = null;
	
	/**
	 * Create an instance of the managed file.
	 * @param file the file instance being managed
	 * @param type the type of the file
	 * @param expirationTime the time when the file expires, used when the file is cached
	 */
	public FileManagerFile ( File file, FileManagerFileType type, DateTime expirationTime ) {
		this (
			file,
			type,
			expirationTime,
			null, // componentScope
			null, // componentName
			null // dataType
		);
	}

	/**
	 * Create an instance of the managed file for all data.
	 * @param file the file instance being managed, using the full path
	 * @param type the type of the file
	 * @param expirationTime the time when the file expires, used when the file is cached
	 * @param componentScope the scope for the component (see FileManager.SCOPE_*)
	 * @param componentName the component name, such as application, datastore, plugin or command name
	 * @param dataType the data type stored in the file
	 */
	public FileManagerFile (
		File file,
		FileManagerFileType type,
		DateTime expirationTime,
		String componentScope,
		String componentName,
		String dataType ) {
		this.file = file;
		this.type = type;
		this.expirationTime = expirationTime;
		this.componentScope = componentScope;
		this.componentName = componentName;
		this.dataType = dataType;
	}

	/**
	 * Return the component name for the file
	 * @return the component name for the file
	 */
	public String getComponentName () {
		return this.componentName;
	}

	/**
	 * Return the component scope for the file
	 * @return the component scope for the file
	 */
	public String getComponentScope () {
		return this.componentScope;
	}

	/**
	 * Return the data type stored in the file
	 * @return the data type stored in the file
	 */
	public String getDataType () {
		return this.dataType;
	}

	/**
	 * Return the expiration time.
	 * @return the expiration time
	 */
	public DateTime getExpirationTime () {
		return this.expirationTime;
	}

	/**
	 * Return the file.
	 * @return the file
	 */
	public File getFile () {
		return this.file;
	}

	/**
	 * Return the file type.
	 * @return the file type
	 */
	public FileManagerFileType getType () {
		return this.type;
	}
	
	/**
	 * Indicate whether the file is expired.
	 * @param timeToCheck the time to check against the file's expiration time
	 * @return true if the file's expiration time is greater than 'timeToCheck', false otherwise
	 */
	public boolean isExpired ( DateTime timeToCheck ) {
		if ( (this.expirationTime == null) || (timeToCheck == null) ) {
			return false;
		}
		if ( this.expirationTime.greaterThan(timeToCheck) ) {
			return true;
		}
		else {
			return false;
		}
	}
}