// JdbcTableMetadata - class for JDBC table metadata

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

package RTi.DMI;

/**
This class holds data from the DatabaseMetaData.getTables() method.
*/
public class JdbcTableMetadata
{

private String catalog = "";
private String schema = "";
private String name = "";
private String type = "";
private String remarks = "";
//private String typesCatalog;
//private String typesSchema;
//private String typeName;
private String selfRefCol = "";
private String selfRefColHowCreated = "";

/*
 * Table identifier used with supplemental metadata,
 * used when database does not provide all metadata,
 * such as SQLite table descriptions.
 */
private int metadataTableId = -1;

/**
Construct object.
*/
public JdbcTableMetadata ()
{
	
}

/**
Get the catalog (may be null)
*/
public String getCatalog ()
{
	return this.catalog;
}

/**
Get the supplemental metadata table ID.
*/
public int getMetadataTableId ()
{
	return this.metadataTableId;
}

/**
Get the name
*/
public String getName ()
{
	return this.name;
}

/**
Get the remarks
*/
public String getRemarks ()
{
	return this.remarks;
}

/**
Get the schema (may be null)
*/
public String getSchema ()
{
	return this.schema;
}

/**
Get the self referencing column (may be null)
*/
public String getSelfRefColumn ()
{
	return this.selfRefCol;
}

/**
Get the self referencing column, how created (may be null)
*/
public String getSelfRefColumnHowCreated ()
{
	return this.selfRefColHowCreated;
}

/**
Get the type (may be null)
*/
public String getType ()
{
	return this.type;
}

/**
Set the catalog.
*/
public void setCatalog ( String catalog )
{
	this.catalog = catalog;
}

/**
Set the supplemental metadata table identifier.
*/
public void setMetadataTableId ( int metadataTableId )
{
	this.metadataTableId = metadataTableId;
}

/**
Set the name.
*/
public void setName ( String name )
{
	this.name = name;
}

/**
Set the remarks.
*/
public void setRemarks ( String remarks )
{
	this.remarks = remarks;
}

/**
Set the schema.
*/
public void setSchema ( String schema )
{
	this.schema = schema;
}

/**
Set the self ref column.
*/
public void setSelfRefColumn ( String selfRefCol )
{
	this.selfRefCol = selfRefCol;
}

/**
Set the self ref column, how created.
*/
public void setSelfRefColumnHowCreated ( String selfRefColHowCreated )
{
	this.selfRefColHowCreated = selfRefColHowCreated;
}

/**
Set the type.
*/
public void setType ( String type )
{
	this.type = type;
}

}
