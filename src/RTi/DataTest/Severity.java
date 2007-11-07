// ----------------------------------------------------------------------------
// Severity - an object for holding data on severity types.
// ----------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
// ----------------------------------------------------------------------------
// History:
//
// 2006-06-13	J. Thomas Sapienza, RTi	Initial version.
// ----------------------------------------------------------------------------

package RTi.DataTest;

import RTi.DMI.DMIUtil;

/**
*/
public class Severity {

private	int __severityType = DMIUtil.MISSING_INT;
private int __severityLevel = DMIUtil.MISSING_INT;
private String __displayColor = DMIUtil.MISSING_STRING;
private String __displayFont = DMIUtil.MISSING_STRING;
private String __displayIcon = DMIUtil.MISSING_STRING;

public Severity() {}

public void finalize()
throws Throwable {
	__displayColor = null;
	__displayFont = null;
	__displayIcon = null;
	super.finalize();
}

public String getDisplayColor() {
	return __displayColor;
}

public String getDisplayFont() {
	return __displayFont;
}

public String getDisplayIcon() {
	return __displayIcon;
}

public int getSeverityLevel() {
	return __severityLevel;
}

public int getSeverityType() {
	return __severityType;
}

public void setDisplayColor(String displayColor) {
	__displayColor = displayColor;
}

public void setDisplayFont(String displayFont) {
	__displayFont = displayFont;
}

public void setDisplayIcon(String displayIcon) {
	__displayIcon = displayIcon;
}

public void setSeverityLevel(int severityLevel) {
	__severityLevel = severityLevel;
}

public void setSeverityType(int severityType) {
	__severityType = severityType;
}

public String toString() {
	return "Severity {\n"
		+ "SeverityType:  " + __severityType + "\n"
		+ "SeverityLevel: " + __severityLevel + "\n"
		+ "DisplayColor: '" + __displayColor + "'\n"
		+ "DisplayFont:  '" + __displayFont + "'\n"
		+ "DisplayIcon:  '" + __displayIcon + "'\n}\n";
}

}
