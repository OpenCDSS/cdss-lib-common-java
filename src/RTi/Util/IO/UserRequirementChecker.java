package RTi.Util.IO;

import RTi.Util.String.StringUtil;

/**
 * Class to check whether a user requirement is met.
 */
public class UserRequirementChecker {

	/**
 	* Check that a user property adheres to a requirement.
 	* The requirement string in RequirementCheck is free format.
 	* @param requirement the full requirement string such as the following,
 	* which allows full handling of the syntax and logging messages:
 	*   "@require user == username"
 	*   "@require user != username"
 	* @param check a RequirementCheck object that has been initialized with the check text and
 	* will be updated in this method.
 	* @return whether the requirement condition is met, from call to check.isRequirementMet()
 	*/
	public boolean checkRequirement ( RequirementCheck check ) {
		// Parse the string into parts:
		// - calling code has already interpreted the first 2 parts to be able to do this call
		String requirement = check.getRequirementText();
		String [] parts = requirement.split(" ");
		String operator = parts[2];
		String checkValue = parts[3];
		// Get the user from the Java properties.
		String userName = System.getProperty("user.name");
		boolean verCheck = StringUtil.compareUsingOperator(userName, operator, checkValue);
		String message = "";
		if ( !verCheck ) {
			message = "User name (" + userName + ") does not meet the requirement.";
		}
		check.setIsRequirementMet(verCheck, message);
		// Indicate whether processing should exit if requirement is not met:
		// - currently always exit if the user is not as required
		// - TODO smalers 2021-10-10 in the future add another key word to require comment or set at runtime
		check.setShouldExitIfRequirementNotMet(true);
		return check.isRequirementMet();
	}

}