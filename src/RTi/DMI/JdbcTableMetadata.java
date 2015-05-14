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