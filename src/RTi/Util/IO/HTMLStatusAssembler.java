// HTMLStatusAssembler - provides support for creating the HTML string for CommandStatus.

/* NoticeStart

CDSS Common Java Library
CDSS Common Java Library is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1994-2024 Colorado Department of Natural Resources

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

/**
 * Provides support for creating the HTML string for Command Status.
 * HTML is used because it allows styling that makes the content more readable. 
 */
public class HTMLStatusAssembler {
	/**
	 * Text to add at the end of the HTML.
	 */
	private final String TRAILER = "</body></html>";

	/**
	 * String builder used to build the HTML string.
	 */
	private StringBuilder buf = new StringBuilder("<html><body>");

	/**
	 * HTML content to start the status table.
	 * Add some spaces around the row count because the Java HTML viewer smashes together.
	 */
	private final String TABLE_START =
          "<table border=1 width=650><tr bgcolor=\"CCCCFF\"><th align=left>"
          + "&nbsp;&nbsp;&nbsp#&nbsp;&nbsp;&nbsp;</th>"
          + "<th align=left>Phase</th><th align=left>Status/Severity</th>"
          + "<th align=left width=300>Problem</th><th>Recommendation</th></tr>";
  
	/**
	 * HTML content to start the summary table.
	 */
	private final String SUMMARY_TABLE_START =
         "<table border=1>"
         + "<tr bgcolor=\"CCCCFF\"><th align=left>Phase</th><th align=left>Status/Max Severity</th></tr>";
  
	/**
	 * HTML for the table.
	 */
	private final String TABLE_END = "</table>";
  
  /**
   * Creates a new HTML assembler for command status assembly.
   */
  public HTMLStatusAssembler() {
  }

  /**
   * Adds an entry for a command in HTML.
   * <p>
   * Note for each addCommand(), a endCommand() is required.
   * @param commandString command string to show at the top of the summary
   */
  public void addCommand(String commandString) {
    buf.append("<p><font bgcolor=white").append("<strong>Command: "+commandString).append("</strong></font>");
  }
  
  /**
   * Add the command status summary table with status for each command phase.
   * @param commandStatusInitialization
   * @param commandStatusDiscovery
   * @param commandStatusRun
   */
  public void addCommandStatusSummary (
	CommandStatusType commandStatusInitialization,
    CommandStatusType commandStatusDiscovery,
    CommandStatusType commandStatusRun) {
    String bgColor1 = "<td bgcolor=" + CommandStatusUtil.getStatusColor(commandStatusInitialization) + ">";
    String bgColor2 = "<td bgcolor=" + CommandStatusUtil.getStatusColor(commandStatusDiscovery) + ">";
    String bgColor3 = "<td bgcolor=" + CommandStatusUtil.getStatusColor(commandStatusRun) + ">";
    buf.append("<p><b>Command Status Summary</b> (see below for details if problems exist):");

    buf.append(SUMMARY_TABLE_START)
    .append("<tr><td>INITIALIZATION</td>").append(bgColor1).append(commandStatusInitialization.toString()).append("</td></tr>")
    .append("<tr><td>DISCOVERY</td>").append(bgColor2).append(commandStatusDiscovery.toString()).append("</td></tr>")
    .append("<tr><td>RUN</td>").append(bgColor3).append(commandStatusRun.toString()).append("</td></tr>")
    .append(TABLE_END);
  }

/**
 * Adds a summary table indicating no problems found.
 */
  public void addNoProblems() {
    String bgColor1 = "<td bgcolor=" +CommandStatusUtil.getStatusColor(CommandStatusType.SUCCESS) + ">";
    buf.append(SUMMARY_TABLE_START)
    .append("<tr><td>INITIALIZATION").append(bgColor1).append(CommandStatusType.SUCCESS.toString()).append("</tr>")
    .append("<tr><td>DISCOVERY").append(bgColor1).append(CommandStatusType.SUCCESS.toString()).append("</tr>")
    .append("<tr><td>RUN").append(bgColor1).append(CommandStatusType.SUCCESS.toString()).append("</tr>")
    .append(TABLE_END);
  }

  /**
   * Add a message if the command is not a CommandStatusProvider (should not happen).
   */
  public void addNotACommandStatusProvider() {
    buf.append("<tr><td>Not a CommandStatusProvider</td></tr>");
  }

  /**
   * Adds command log output entry for a phase in HTML.
   * If formatting line breaks are not as expected, make sure that NL characters are not being swallowed somewhere else.
   * 
   * @param phase One of: INITIALIZATION,DISCOVERY,RUN
   * @param severity One of : WARNING,ERROR
   * @param color color associated with severity
   * @param problem problem encountered
   * @param recommendation recommended solution
   */
  public void addPhase( int count, String phase, String severity, String color, String problem, String recommendation ) {
    String bgcolor = "</td><td valign=top bgcolor=" + color + ">";
    
    // Translate special characters into form that will display, for example &, <.
    problem = HTMLUtil.text2html(problem, false);
    // Replace two spaces with &emsp; to ensure spacing, such as JSON formatting.
    // - otherwise HTML browser tends to compress
    problem = problem.replace("  ", "&emsp;");
    buf.append("<tr><td valign=top>" + count + "</td><td valign=top>" + phase + bgcolor + severity
        +"</td><td valign=top>" + problem + "</td>"
        +"<td valign=top>" + HTMLUtil.text2html(recommendation,false) + "</td></tr>"
        );
  }
  
  /**
   * Add HTML to terminate a command initiated with addCommand()
   */
  public void endCommand() {
    buf.append(TABLE_END);
  }

  /**
   * Returns a HTML string ready for display in a browser.
   * 
   * @return HTML string 
   */
  public String getHTML() {
    buf.append(TRAILER);
    return buf.toString();
  }

  /**
   * Add HTML to start status table
   * @param nWarn the number of warning messages that will be shown.
   * @param nFail the number of failure messages that will be shown.
   * @param nNotification the number of notification messages that will be shown.
   */
  public void startCommandStatusTable( int nWarn, int nFail, int nNotification ) {
    buf.append("<p><b>Command Status Details (" + nWarn + " warnings, " + nFail + " failures, " + nNotification + " notifications):");
    buf.append(TABLE_START); 
  }
  
  /**
   * Return the formatted HTML string for the summary.
   * @return the formatted HTML string
   */
  public String toString() {
    return buf.toString();
  }

}