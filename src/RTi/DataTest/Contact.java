// ----------------------------------------------------------------------------
// Contact - an object for holding data on contacts.
// ----------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
// ----------------------------------------------------------------------------
// History:
//
// 2006-04-25	J. Thomas Sapienza, RTi	Initial version.
// ----------------------------------------------------------------------------

package RTi.DataTest;

import RTi.DMI.DMIUtil;

/**
This class represents a single Contact, used by NotificationActions to contact 
users on a positive test result.
*/
public class Contact {

/**
The ID number that uniquely identifies the contact in the DataTest system.
*/
private int __contactNum = DMIUtil.MISSING_INT;

/**
String data members.
*/
private String
	__firstName = DMIUtil.MISSING_STRING,
	__lastName = DMIUtil.MISSING_STRING,
	__phoneNumber = DMIUtil.MISSING_STRING,
	__faxNumber = DMIUtil.MISSING_STRING,
	__pagerNumber = DMIUtil.MISSING_STRING,
	__emailAddress = DMIUtil.MISSING_STRING,
	__imAddress = DMIUtil.MISSING_STRING,
	__smsAddress = DMIUtil.MISSING_STRING,
	__skypeID = DMIUtil.MISSING_STRING,
	__properties = DMIUtil.MISSING_STRING;

/**
Constructor.
*/
public Contact() {}

/**
Cleans up data members.
*/
public void finalize() 
throws Throwable {
	__firstName = null;
	__lastName = null;
	__phoneNumber = null;
	__faxNumber = null;
	__pagerNumber = null;
	__emailAddress = null;
	__imAddress = null;
	__smsAddress = null;
	__skypeID = null;
	__properties = null;
	super.finalize();
}

/**
Returns __contactNum
@return __contactNum
*/
public int getContactNum() {
	return __contactNum;
}

/**
Returns __emailAddress
@return __emailAddress
*/
public String getEmailAddress() {
	return __emailAddress;
}

/**
Returns __faxNumber
@return __faxNumber
*/
public String getFaxNumber() {
	return __faxNumber;
}

/**
Returns __firstName
@return __firstName
*/
public String getFirstName() {
	return __firstName;
}

/**
Returns __imAddress
@return __imAddress
*/
public String getIMAddress() {
	return __imAddress;
}

/**
Returns __lastName
@return __lastName
*/
public String getLastName() {
	return __lastName;
}

/**
Returns __pagerNumber
@return __pagerNumber
*/
public String getPagerNumber() {
	return __pagerNumber;
}

/**
Returns __phoneNumber
@return __phoneNumber
*/
public String getPhoneNumber() {
	return __phoneNumber;
}

/**
Returns __properties
@return __properties
*/
public String getProperties() {
	return __properties;
}

/**
Returns __skypeID
@return __skypeID
*/
public String getSkypeID() {
	return __skypeID;
}

/**
Returns __smsAddress
@return __smsAddress
*/
public String getSmsAddress() {
	return __smsAddress;
}

/**
Sets __contactNum
@param contactNum value to put into __contactNum
*/
public void setContactNum(int contactNum) {
	__contactNum = contactNum;
}

/**
Sets __emailAddress
@param emailAddress value to put into __emailAddress
*/
public void setEmailAddress(String emailAddress) {
	__emailAddress = emailAddress;
}

/**
Sets __faxNumber
@param faxNumber value to put into __faxNumber
*/
public void setFaxNumber(String faxNumber) {
	__faxNumber = faxNumber;
}

/**
Sets __firstName
@param firstName value to put into __firstName
*/
public void setFirstName(String firstName) {
	__firstName = firstName;
}

/**
Sets __imAddress
@param imAddress value to put into __imAddress
*/
public void setIMAddress(String imAddress) {
	__imAddress = imAddress;
}

/**
Sets __lastName
@param lastName value to put into __lastName
*/
public void setLastName(String lastName) {
	__lastName = lastName;
}

/**
Sets __pagerNumber
@param pagerNumber value to put into __pagerNumber
*/
public void setPagerNumber(String pagerNumber) {
	__pagerNumber = pagerNumber;
}

/**
Sets __phoneNumber
@param phoneNumber value to put into __phoneNumber
*/
public void setPhoneNumber(String phoneNumber) {
	__phoneNumber = phoneNumber;
}

/**
Sets __properties
@param properties value to put into __properties
*/
public void setProperties(String properties) {
	__properties = properties;
}

/**
Sets __skypeID 
@param skypeID value to put into __skypeID
*/
public void setSkypeID(String skypeID) {
	__skypeID = skypeID;
}

/**
Sets __smsAddress
@param smsAddress value to put into __smsAddress
*/
public void setSMSAddress(String smsAddress) {
	__smsAddress = smsAddress;
}

/**
Returns a string representation of the Contact.
@return a string representation of the Contact.
*/
public String toString() {
	return "Contact {\n" +
		"ContactNum:    " + __contactNum + "\n" + 
		"FirstName:    '" + __firstName + "'\n" + 
		"LastName:     '" + __lastName + "'\n" + 
		"PhoneNumber:  '" + __phoneNumber + "'\n" + 
		"FaxNumber:    '" + __faxNumber + "'\n" + 
		"PagerNumber:  '" + __pagerNumber + "'\n" + 
		"EmailAddress: '" + __emailAddress + "'\n" + 
		"IMAddress:    '" + __imAddress + "'\n" + 
		"SMSAddress:   '" + __smsAddress + "'\n" + 
		"SkypeID:      '" + __skypeID + "'\n" + 
		"Properties:   '" + __properties + "'\n}\n";
}

}
