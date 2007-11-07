// ----------------------------------------------------------------------------
// CorrectionAction - abstract base class for an action that corrects data 
//	errors.
// ----------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
// ----------------------------------------------------------------------------
// History:
//
// 2006-03-22	J. Thomas Sapienza, RTi	Initial version.
// ----------------------------------------------------------------------------

package RTi.DataTest;

/**
This class is the abstract base class for any actions that perform data
correction.
REVISIT (JTS - 2006-03-22)
What sorts of CorrectionAction-specific data are going to go in here?
*/
public abstract class CorrectionAction 
extends Action {

/**
Constructor.
@param dataModel the data model to use for filling values in this object.
@throws Exception if an error occurs.
*/
public CorrectionAction(ActionDataModel dataModel) 
throws Exception {
	super(dataModel);
}

}
