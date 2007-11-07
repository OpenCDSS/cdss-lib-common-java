// ----------------------------------------------------------------------------
// MessageAction - abstract base class for an action that sends messages.
// ----------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
// ----------------------------------------------------------------------------
// History:
//
// 2006-03-22	J. Thomas Sapienza, RTi	Initial version.
// ----------------------------------------------------------------------------

package RTi.DataTest;

/**
This class is an abstract base class for any action that sends messages.
*/
public abstract class MessageAction 
extends Action {

/**
Constructor.
@param dataModel the model containing the values for this object.
*/
public MessageAction(ActionDataModel dataModel) 
throws Exception {
	super(dataModel);
}

}
