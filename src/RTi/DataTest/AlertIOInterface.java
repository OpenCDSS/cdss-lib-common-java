// ----------------------------------------------------------------------------
// AlertIOInterface - interface for classes that will read and write data for
//	alerts.
// ----------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
// ----------------------------------------------------------------------------
// History:
//
// 2006-03-13	J. Thomas Sapienza, RTi	Initial version.
// 2006-03-20	JTS, RTi		Added methods for reading DataTest data.
// 2006-03-21	JTS, RTi		Added methods for reading/writing
//					Action data.
// 2006-03-22	JTS, RTi		Added readAllActionDataModels().
// 2006-04-17	JTS, RTi		Methods all throw Exceptions now.
// 2006-04-24	JTS, RTi		Added readContact().
// 2006-05-15	JTS, RTi		Added deleteDataTest(), 
//					deleteDataTestExpression(),
//					deleteDataTestFunction(), 
//					deleteAction(), deleteContact().
// 2006-05-16	JTS, RTi		Added readAllDataTestDataModels().
// ----------------------------------------------------------------------------

package RTi.DataTest;

import java.util.List;

/**
This interface defines methods that IO classes will use to read and write data
for alert information.
*/
public interface AlertIOInterface {

public void deleteAction(int actionNum)
throws Exception;

public void deleteContact(int contactNum)
throws Exception;

public void deleteDataTest(int dataTestNum)
throws Exception;

public void deleteDataTestExpression(int expressionNum)
throws Exception;

public void deleteDataTestFunction(int functionNum)
throws Exception;

/**
Reads all the action data models from the data source.
@return a Vector of data models for all the actions in the system.
@throws Exception if there is an error reading the action data models.
*/
public List readAllActionDataModels()
throws Exception;

public List readAllDataTestDataModels()
throws Exception;

/**
Reads an ActionDataModel with the given ID number.
@param actionNum the id number of the data model to be read.
@return the ActionDataModel with the information read from the data provider.
@throws Exception if there is an error reading the action data model.
*/
public ActionDataModel readActionDataModel(int actionNum)
throws Exception;

/**
Reads a contact with the given ID number.
@param contactNum the id number of the contact to be read.
@return the Contact that was read.
@throws Exception if there is an error reading the contact.
*/
public Contact readContact(int contactNum) 
throws Exception;

/**
Reads a DataTestDataModel with the given ID number.
@param dataTestNum the id number of the data model to be read.
@return the DataTestDataModel with the information read from the data provider.
@throws Exception if there is an error reading the data test data model.
*/
public DataTestDataModel readDataTestDataModel(int dataTestNum)
throws Exception;

/**
Reads a DataTestExpressionDataModel with the given ID number.
@param expressionNum the id number of the data model to be read.
@return a DataTestExpressionDataModel with the information read from the 
data provider.
@throws Exception if there is an error reading the data test expression data 
model.
*/
public DataTestExpressionDataModel readDataTestExpressionDataModel(
int expressionNum)
throws Exception;

/**
Reads a DataTestFunctionDataModel with the given ID number.
@param functionNum the id number of the data model to be read.
@return a DataTestFunctionDataModel with the information read from the 
data provider.
@throws Exception if there is an error reading the data test function data 
model.
*/
public DataTestFunctionDataModel readDataTestFunctionDataModel(int functionNum)
throws Exception;

public List readSeverityTypes()
throws Exception;

/**
REVISIT (JTS - 2006-05-07)
once reading data tests is figured out, fill this back in.  I think that
more information will be necessary -- for instance, why not change this to
return a Vector and read all the results for the given data test at the
given date/time?  Or will it be necessary to add another parameter that 
specifies the level of result to read?
*/
//public DataTestResult readDataTestResult(int dataTestNum, DateTime date) 
//throws Exception;

/**
Writes Action data.
@param dataModel the ActionDataModel containing the data to be written.
@throws Exception if an error occurs while writing the action data model.
*/
public int writeActionDataModel(ActionDataModel dataModel)
throws Exception;

/**
Writes data test data.
@param dataModel the DataTestDataModel containing the data to be written.
@throws Exception if an error occurs while writing the data test data model.
*/
public int writeDataTestDataModel(DataTestDataModel dataModel)
throws Exception;

/**
Writes expression data.
@param dataModel the DataTestExpressionDataModel containing the data to be
written.
@throws Exception if an error occurs while writing the expression data model.
*/
public int writeDataTestExpressionDataModel(
DataTestExpressionDataModel dataModel)
throws Exception;

/**
Writes expression data.
@param dataModel the DataTestFunctionDataModel containing the data to be
written.
@throws Exception if an error occurs while writing the function data model.
*/
public int writeDataTestFunctionDataModel(DataTestFunctionDataModel dataModel)
throws Exception;

/**
Writes the given data test result.
@param result the DataTestResult to write.
@throws Exception if an error occurs while writing the data test result.
*/
public int writeDataTestResult(DataTestResult result)
throws Exception;

public void writeDataTestStatusData(DataTest test) 
throws Exception;

}
