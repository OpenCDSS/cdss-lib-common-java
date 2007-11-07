package RTi.Util.IO;


/**
 * Provides support for creating the HTML string for Command Status.
 * <p>
 * 
 * @author dre
 */
public class HTMLStatusAssembler
{
  
  private final StringBuffer TRAILER = new StringBuffer("</body></html>");
  private StringBuffer buf = new StringBuffer("<html><body>");
  private final StringBuffer TABLE_START= new StringBuffer(""
          + "<table border=1><TR bgcolor=\"CCCCFF\"><th align=left>Phase</th><th align=left>Severity</th>"
          + "<th align=left>Problem</th><th>Recommendation</th></TR>");
  
  private final StringBuffer SUMMARY_TABLE_START = new StringBuffer(""
         + "<table border=1>"
         + "<TR bgcolor=\"CCCCFF\"><th align=left>Phase</th><th align=left>Status/Max Severity</th></TR>");
  
  
  private final StringBuffer TABLE_END = new StringBuffer("</table>");
  
  /**
   * Creates a new HTML assembler for command status assembly.
   */
  public HTMLStatusAssembler()
  {
  }

  /**
   * Returns a HTML string ready for display in a browser.
   * 
   * @return HTML string 
   */
  public String getHTML()
  {
    buf.append(TRAILER);
    return buf.toString();
  }

  /**
   * Adds entry for a phase in HTML.
   * 
   * @param phase One of: INITIALIZATION,DISCOVERY,RUN
   * @param severity One of : WARNING,ERROR
   * @param color color associated with severity
   * @param problem problem encountered
   * @param recommendation recommended solution
   */
  public void addPhase(String phase, String severity,String color,
          String problem,
          String recommendation)
  {

    String bgcolor = "<td bgcolor=" + color + ">";
    
    buf.append("<tr><td valign=top>" + phase
            + bgcolor + severity
            +"<td>" + problem + "</tr>"
            +"<td>" + recommendation + "</tr>"
            );
  }

  
  
  /**
   * Adds an entry for a command in HTML.
   * <p>
   * Note for each addCommand(), a endCommand() is required.
   * @param commandString
   */
  public void addCommand(String commandString)
  {
    buf.append("<p><font bgcolor=\"CC99CC\">").append("<strong>Command: "+commandString).append("</strong></font>");
  }
  
  /**
   * Add HTML to start status table
   */
  public void startCommandStatusTable()
  {
    buf.append("<p><b>Command Status Details:");
    buf.append(TABLE_START); 
  }
  
  /**
   * Add HTML to terminate a command initiated with addCommand()
   */
  public void endCommand()
  {
    buf.append(TABLE_END);
  }

  public void addNotACommandStatusProvider()
  {
    buf.append("<tr><td> Not a CommandStatusProvider</td></tr>");
  }

  /**
   * Add the command status summary table
   * @param commandStatus1
   * @param commandStatus2
   * @param commandStatus3
   */
  public void addCommandStatusSummary(CommandStatusType commandStatus1,
          CommandStatusType commandStatus2,
          CommandStatusType commandStatus3)
  {
    
    String bgColor1 = "<td bgcolor=" +CommandStatusUtil.getStatusColor(commandStatus1) + ">";
    String bgColor2 = "<td bgcolor=" +CommandStatusUtil.getStatusColor(commandStatus2) + ">";
    String bgColor3 = "<td bgcolor=" +CommandStatusUtil.getStatusColor(commandStatus3) + ">";
    buf.append("<p><b>Command Status Summary</b> (see below for details if necessary):");

    buf.append(SUMMARY_TABLE_START)
    .append("<tr><td>INITIALIZATION").append(bgColor1).append(commandStatus1.toString()).append("</tr>")
    .append("<tr><td>DISCOVERY").append(bgColor2).append(commandStatus2.toString()).append("</tr>")
    .append("<tr><td>RUN").append(bgColor3).append(commandStatus3.toString()).append("</tr>")
    .append(TABLE_END);

  }
  public String toString()
  {
    return buf.toString();
  }
/**
 * Adds a message to the HTML indicating no problems found .
 */
  public void addNoProblems()
  {
    buf.append("<strong> No issues were found for the selected commands</strong>");
  }
} // eof class HTMLAssembler