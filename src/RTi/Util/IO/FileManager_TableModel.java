// FileManager_TableModel - table model for displaying FileManager data in a JWorksheet

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

import RTi.Util.GUI.JWorksheet_AbstractRowTableModel;
import RTi.Util.Time.DateTime;

/**
Table model for displaying data table data in a JWorksheet.
*/
@SuppressWarnings("serial")
public class FileManager_TableModel
extends JWorksheet_AbstractRowTableModel<FileManagerFile> {

	/**
	The FileManger displayed in the worksheet.
	*/
	private FileManager fileManager = null;

	/**
	Number of columns in the table model (with the alias).
	*/
	private int COLUMNS = 8;

	/**
	Absolute column indices, for column lookups.
	*/
	public final int COL_COMPONENT_SCOPE = 0;
	public final int COL_COMPONENT_ID = 1;
	public final int COL_DATA_TYPE = 2;
	public final int COL_FILE_TYPE = 3;
	public final int COL_EXPIRES = 4;
	public final int COL_IS_EXPIRED = 5;
	public final int COL_SIZE = 6;
	// Put pat ta the end since long.
	public final int COL_FILE_PATH = 7;

	/**
	Constructor.
	@param fileManager the FileManager to show in the worksheet.
	@throws NullPointerException if the dataTable is null.
	*/
	public FileManager_TableModel ( FileManager fileManager )
	throws Exception {
	    if ( fileManager == null ) {
	        _rows = 0;
	    }
	    else {
	        _rows = fileManager.size();
	    }
	    this.fileManager = fileManager;
	    _data = fileManager.getAll();
	}

	/**
	From AbstractTableModel.  Returns the class of the data stored in a given column.
	All values are treated as strings.
	@param columnIndex the column for which to return the data class.
	@return the column class
	*/
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Class getColumnClass ( int columnIndex ) {
	    switch (columnIndex) {
	    	case COL_COMPONENT_SCOPE: return String.class;
	    	case COL_COMPONENT_ID: return String.class;
	    	case COL_DATA_TYPE: return String.class;
	    	case COL_FILE_TYPE: return String.class;
	    	case COL_EXPIRES: return DateTime.class;
	    	case COL_IS_EXPIRED: return Boolean.class;
	    	case COL_SIZE: return Long.class;
	    	case COL_FILE_PATH: return String.class;
	        default: return String.class;
	    }
	}

	/**
	From AbstractTableMode.  Returns the number of columns of data.
	@return the number of columns of data.
	*/
	public int getColumnCount() {
	    return this.COLUMNS;
	}

	/**
	From AbstractTableMode.  Returns the name of the column at the given position.
	@return the name of the column at the given position.
	*/
	public String getColumnName ( int columnIndex ) {
	    switch (columnIndex) {
	    	case COL_COMPONENT_SCOPE: return "Scope";
	    	case COL_COMPONENT_ID: return "Name";
	    	case COL_DATA_TYPE: return "Data Type";
	    	case COL_FILE_TYPE: return "File Type";
	    	case COL_EXPIRES: return "Expires";
	    	case COL_IS_EXPIRED: return "Is Expired";
	    	case COL_SIZE: return "Size";
	    	case COL_FILE_PATH: return "Path";
	        default: return "";
	    }
	}

	/**
	Return tool tips for the columns.
	@return tool tips for the columns.
	*/
	public String[] getColumnToolTips() {
	    String [] tooltips = new String[this.COLUMNS];
	   	tooltips[COL_COMPONENT_SCOPE] = "Scope (App, Plugin, DataStore, Command, etc.)";
	   	tooltips[COL_COMPONENT_ID] = "Name (app name, plugin name, datastore name, command name";
	   	tooltips[COL_DATA_TYPE] = "Data type for contents of the file";
	   	tooltips[COL_FILE_TYPE] = "File Type (Temporary or Cache)";
	   	tooltips[COL_EXPIRES] = "Expires date/time";
	   	tooltips[COL_IS_EXPIRED] = "Is the file expired?";
	   	tooltips[COL_SIZE] = "Size in bytes";
	   	tooltips[COL_FILE_PATH] = "Path";
	    return tooltips;
	}

	/**
	Returns the format to display the specified column.
	@param column column for which to return the format.
	@return the format (as used by StringUtil.formatString()).
	*/
	public String getFormat ( int column ) {
	    switch (column) {
	        default: return "%s";
	    }
	}

	/**
	From AbstractTableMode.  Returns the number of rows of data in the table.
	@rReturn the number of rows of data in the FileManager.
	*/
	public int getRowCount() {
	    return _rows;
	}

	/**
	From AbstractTableMode.  Returns the data that should be placed in the JTable at the given row and column.
	@param row the row (0+) for which to return data.
	@param col the absolute column (0+) for which to return data.
	@return the data that should be placed in the JTable at the given row and column.
	*/
	public Object getValueAt ( int row, int col ) {
	    // Make sure the row numbers are never sorted.

	    if (_sortOrder != null) {
	        row = _sortOrder[row];
	    }

	    FileManagerFile managedFile = this.fileManager.get(row);
	    if ( managedFile == null ) {
	        return null;
	    }
	    File file = managedFile.getFile();
	    switch (col) {
	    	case COL_COMPONENT_SCOPE: return managedFile.getComponentScope();
	    	case COL_COMPONENT_ID: return managedFile.getComponentName();
	    	case COL_DATA_TYPE: return managedFile.getDataType();
	    	case COL_FILE_TYPE: return managedFile.getType().toString();
	    	case COL_EXPIRES: return managedFile.getExpirationTime();
	    	case COL_IS_EXPIRED:
	    		DateTime now = new DateTime ( DateTime.DATE_CURRENT | DateTime.DATE_FAST );
	    		return managedFile.isExpired ( now );
	    	case COL_SIZE: return file.length();
	    	case COL_FILE_PATH: return file.getPath();
	        default: return null;
	    }
	}

	/**
	Returns an array containing the column widths (in number of characters).
	@return an integer array containing the widths for each field.
	*/
	public int[] getColumnWidths() {
	    int[] widths = new int[this.COLUMNS];
	   	widths[COL_COMPONENT_SCOPE] = 8;
	   	widths[COL_COMPONENT_ID] = 8;
	   	widths[COL_DATA_TYPE] = 8;
	   	widths[COL_FILE_TYPE] = 8;
	   	widths[COL_EXPIRES] = 8;
	   	widths[COL_IS_EXPIRED] = 8;
	   	widths[COL_SIZE] = 8;
	   	widths[COL_FILE_PATH] = 30;
	    return widths;
	}

}