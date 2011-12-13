package RTi.Util.Time;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

import java.util.List;
import java.util.Vector;

import RTi.Util.GUI.JGUIUtil;
import RTi.Util.GUI.SimpleJComboBox;
import RTi.Util.String.StringUtil;

/**
<p>
A JPanel to edit a DateTime and retrieve a date/time as the corresponding DateTime object or string.
The constructor indicates the range of date/time fields to display, which should normally correspond
to the detail needed for input.  However, because of variability in data (such as time series intervals
only being known at run-time), the editor may show extra precision.  Also, in order to facilitate parsing
the date/time string after editing, it is necessary to include blank indicators (such as 99) in the string.
</p>
<p>
Currently the minimum precision allowed is TimeInterval.MINUTE.
</p>
*/
public class DateTime_JPanel extends JPanel implements ActionListener
{

/**
Maximum interval shown in the panel, where YEAR is the big and MINUTE is small.
For example, specifying TimeInterval.MONTH will omit display of year.
*/
private int __intervalMax = TimeInterval.YEAR;

/**
Minimum interval shown in the panel, where YEAR is the big and MINUTE is small.
For example, specifying TimeInterval.DAY will omit display of hour, minute, etc.
*/
private int __intervalMin = TimeInterval.MINUTE;

/**
Day choices.
*/
SimpleJComboBox __day_JComboBox = null;

/**
Day choices label.
*/
JLabel __day_JLabel = null;

/**
Hour choices.
*/
SimpleJComboBox __hour_JComboBox = null;

/**
Hour choices label.
*/
JLabel __hour_JLabel = null;

/**
Minute choices.
*/
SimpleJComboBox __minute_JComboBox = null;

/**
Minute choices label.
*/
JLabel __minute_JLabel = null;

/**
Month choices.
*/
SimpleJComboBox __month_JComboBox = null;

/**
Month choices label.
*/
JLabel __month_JLabel = null;

/**
Title of the panel.  If specified, use a border to draw around the panel.
*/
private String __title = "";

/**
Year choices.
*/
SimpleJComboBox __year_JComboBox = null;

/**
Year choices label.
*/
JLabel __year_JLabel = null;

// FIXME SAM 2008-01-23 Change intervals from integers to enumeration when
// code base has moved to Java 1.5
/**
Constructor.
@param title Title for the panel, shown in a border title.
@param intervalMax The maximum interval shown in choices (use TimeInterval.MONTH, etc.).
@param intervalMin The minimum interval shown in choices (use TimeInterval.HOUR, etc.).
@param initial Initial DateTime to be displayed.  The precision will override interval_min.
*/
public DateTime_JPanel ( String title, int intervalMax, int intervalMin, DateTime initial )
{   super ();
    setTitle ( title );
    setIntervalMax ( intervalMax );
    setIntervalMin ( intervalMin );
    setupUI ();
    // Set the date time after setting the interval_min because the DateTime may override.
    if ( initial != null ) {
        setDateTime ( initial );
    }
}

/**
Handle action events.
@param event action event to handle.
*/
@Override
public void actionPerformed(ActionEvent event)
{
    // If the event source is the month choice, update the day choices to be correct.
    if ( event.getSource() == __month_JComboBox ) {
        setDayChoices ( __year_JComboBox, __month_JComboBox, __day_JComboBox );
    }
    
}

/**
Add an action listener for all of the components.  The listeners will be notified if
any action events occur.
*/
public void addActionListener ( ActionListener listener )
{
    if ( __year_JComboBox != null ) {
        __year_JComboBox.addActionListener ( listener );
    }
    if ( __month_JComboBox != null ) {
        __month_JComboBox.addActionListener ( listener );
    }
    if ( __day_JComboBox != null ) {
        __day_JComboBox.addActionListener ( listener );
    }
    if ( __hour_JComboBox != null ) {
        __hour_JComboBox.addActionListener ( listener );
    }
    if ( __minute_JComboBox != null ) {
        __minute_JComboBox.addActionListener ( listener );
    }
}

/**
Set the DateTime that is selected in choices.
Any values that are 99, etc. are assumed to not be displayed
*/
public void setDateTime ( DateTime dt )
{
    //Message.printStatus ( 2, "", "Setting panel DateTime to " + dt );
    // TODO SAM 2011-12-12 Should the following be done?  The user may want to change and the
    // command handles blank date/time properly.  The problem is that the command parameter will not have
    // 99 saved (may have zeros) - setting the precision here prevents the zeros from propagating to output.
    setIntervalMin ( dt.getPrecision() );
    // Update the visibility of components.
    setVisibility();
    if ( (TimeInterval.YEAR <= __intervalMax) && (TimeInterval.YEAR >= __intervalMin) ) {
        int year = dt.getYear();
        if ( TimeUtil.isValidYear(year) ) {
            __year_JComboBox.select ( "" + year );
        }
        else {
            // Select blank
            __year_JComboBox.select ( "" );
        }
    }
    if ( (TimeInterval.MONTH <= __intervalMax) && (TimeInterval.MONTH >= __intervalMin) ) {
        int month = dt.getMonth();
        if ( TimeUtil.isValidMonth(month) ) {
            __month_JComboBox.select ( "" + month );
        }
        else {
            // Select blank
            __month_JComboBox.select ( "" );
        }
    }
    if ( (TimeInterval.DAY <= __intervalMax) && (TimeInterval.DAY >= __intervalMin) ) {
        int day = dt.getDay();
        if ( TimeUtil.isValidDay(day) ) {
            __day_JComboBox.select ( "" + day );
        }
        else {
            // Select blank
            __day_JComboBox.select ( "" );
        }
    }
    if ( (TimeInterval.HOUR <= __intervalMax) && (TimeInterval.HOUR >= __intervalMin) ) {
        int hour = dt.getHour();
        if ( TimeUtil.isValidHour(hour) ) {
            __hour_JComboBox.select ( StringUtil.formatString(hour,"%02d") );
        }
        else {
            // Select blank
            __hour_JComboBox.select ( "" );
        }       
    }
    if ( (TimeInterval.MINUTE <= __intervalMax) && (TimeInterval.MINUTE >= __intervalMin) ) {
        int minute = dt.getMinute();
        if ( TimeUtil.isValidMinute(minute) ) {
            __minute_JComboBox.select ( StringUtil.formatString(minute,"%02d") );
        }
        else {
            // Select blank
            __minute_JComboBox.select ( "" );
        }
    }
}

/**
Set up the daily choices based on the selected year.  If the year is not visible and the month is 2,
include 29 days.
*/
private void setDayChoices ( SimpleJComboBox year_JComboBox, SimpleJComboBox month_JComboBox,
    SimpleJComboBox day_JComboBox )
{
    List<String> dayList = new Vector();
    dayList.add ( "" ); // Select to ignore this information
    int maxDay = 31;
    int year = -9999;
    int month = -9999;
    if ( year_JComboBox.isVisible() ) {
        String yearSelection = year_JComboBox.getSelected().trim();
        if ( StringUtil.isInteger(yearSelection) ) {
            year = Integer.parseInt(yearSelection);
        }
    }
    if ( month_JComboBox.isVisible() ) {
        String monthSelection = month_JComboBox.getSelected().trim();
        if ( StringUtil.isInteger(monthSelection) ) {
            month = Integer.parseInt(monthSelection);
        }
    }
    if ( month != -9999 ) {
        // Try to be more specific with the number of days.
        if ( year != -9999 ) {
            maxDay = TimeUtil.numDaysInMonth(month, year);
        }
        else {
            // Assume a leap year...
            maxDay = TimeUtil.numDaysInMonth(month, 1976);
        }
    }
    for ( int i = 1; i <= maxDay; i++ ) {
        dayList.add( "" + i );
    }
    day_JComboBox.setData(dayList);
}

/**
Set the panel enabled/disabled by setting each component enabled/disabled.
@param enabled Indicates whether the panel components should be enabled or disabled.
*/
public void setEnabled ( boolean enabled )
{
    if ( __year_JComboBox != null ) {
        __year_JComboBox.setEnabled(enabled);
    }
    if ( __month_JComboBox != null ) {
        __month_JComboBox.setEnabled(enabled);
    }
    if ( __day_JComboBox != null ) {
        __day_JComboBox.setEnabled(enabled);
    }
    if ( __hour_JComboBox != null ) {
        __hour_JComboBox.setEnabled(enabled);
    }
    if ( __minute_JComboBox != null ) {
        __minute_JComboBox.setEnabled(enabled);
    }
}

/**
Set the maximum interval displayed in the panel.
@param interval_max see TimeInterval definitions.
*/
private void setIntervalMax ( int interval_max )
{
    __intervalMax = interval_max;
}

/**
Set the minimum interval displayed in the panel.
@param interval_min see TimeInterval definitions.
*/
public void setIntervalMin ( int interval_min )
{
    __intervalMin = interval_min;
}

/**
Set the title for the panel.
@param title 
*/
public void setTitle ( String title )
{
    __title = title;
}

/**
Setup the UI.
*/
private void setupUI ()
{
    if ( (__title != null) && (__title.length() > 0) ) {
        setBorder( BorderFactory.createTitledBorder (BorderFactory.createLineBorder(Color.black),__title ));
    }

    // Use SimpleJComboBox instances in the layout.
    // Create instances for all interval parts but only set visible what is requested.
    // If setDateTime() is called later, all necessary components can be set visible.
    
    Insets insetsTLBR = new Insets(2,2,2,2);
    setLayout ( new GridBagLayout() );
    
    int x = 0; // X position in grid bag layout
    int y_label = 0; // Positions of labels and entry fields.
    int y_entry = 1;
    // Add the year...
    List<String> yearList = new Vector();
    yearList.add( "" );
    for ( int i = 1900; i <= 2100; i++ ) {
        yearList.add( "" + i );
    }
    // Allow edits because may not have all years of interest in the list
    __year_JComboBox = new SimpleJComboBox ( yearList, true );
    __year_JLabel = new JLabel("Year:");
    JGUIUtil.addComponent(this, __year_JLabel,
        x, y_label, 1, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(this, __year_JComboBox,
        x++, y_entry, 1, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

    // Add the month...
    List<String> monthList = new Vector();
    monthList.add( "" );    // Select to ignore this information
    for ( int i = 1; i <= 12; i++ ) {
        monthList.add( "" + i );
    }
    __month_JComboBox = new SimpleJComboBox ( monthList, false );
    __month_JComboBox.addActionListener ( this );
    __month_JLabel = new JLabel("Month:");
    JGUIUtil.addComponent(this, __month_JLabel,
        x, y_label, 1, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(this, __month_JComboBox,
        x++, y_entry, 1, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

    // Add the day...
    __day_JComboBox = new SimpleJComboBox ( false ); // Not editable
    setDayChoices ( __year_JComboBox, __month_JComboBox, __day_JComboBox );
    __day_JLabel = new JLabel("Day:");
    JGUIUtil.addComponent(this, __day_JLabel,
        x, y_label, 1, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(this, __day_JComboBox,
        x++, y_entry, 1, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

    // Add the hour...
    List<String> hourList = new Vector();
    hourList.add ( "" );    // Select to ignore this information
    for ( int i = 0; i <= 23; i++ ) {
        hourList.add ( "" + StringUtil.formatString(i,"%02d") );
    }
    __hour_JComboBox = new SimpleJComboBox ( hourList, false ); // Not editable
    __hour_JLabel = new JLabel("Hour:");
    JGUIUtil.addComponent(this, __hour_JLabel,
        x, y_label, 1, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(this, __hour_JComboBox,
        x++, y_entry, 1, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

    // Add the minute...
    List<String> minuteList = new Vector();
    minuteList.add ( "" );    // Select to ignore this information
    for ( int i = 0; i <= 59; i++ ) {
        minuteList.add ( "" + StringUtil.formatString(i,"%02d") );
    }
    __minute_JComboBox = new SimpleJComboBox ( minuteList, true );
    __minute_JLabel = new JLabel("Minute:");
    JGUIUtil.addComponent(this, __minute_JLabel,
        x, y_label, 1, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(this, __minute_JComboBox,
        x++, y_entry, 1, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    // Set the visibility on the choices consistent with the min/max interval to display.
    setVisibility();
}

/**
Set the visibility on the choices consistent with the min/max interval to display.
For example, don't show the year if year interval is not within the min and max intervals.
*/
private void setVisibility()
{
    if ( (TimeInterval.YEAR <= __intervalMax) && (TimeInterval.YEAR >= __intervalMin) ) {
        __year_JComboBox.setVisible ( true );
        __year_JLabel.setVisible ( true );
    }
    else {
        __year_JComboBox.setVisible ( false );
        __year_JLabel.setVisible ( false );
    }
    if ( (TimeInterval.MONTH <= __intervalMax) && (TimeInterval.MONTH >= __intervalMin) ) {
        __month_JComboBox.setVisible(true);
        __month_JLabel.setVisible(true);
    }
    else {
        __month_JComboBox.setVisible(false);
        __month_JLabel.setVisible(false);
    }
    if ( (TimeInterval.DAY <= __intervalMax) && (TimeInterval.DAY >= __intervalMin) ) {
        __day_JComboBox.setVisible(true);
        __day_JLabel.setVisible(true);
    }
    else {
        __day_JComboBox.setVisible(false);
        __day_JLabel.setVisible(false);
    }
    if ( (TimeInterval.HOUR <= __intervalMax) && (TimeInterval.HOUR >= __intervalMin) ) {
        __hour_JComboBox.setVisible ( true );
        __hour_JLabel.setVisible ( true );
    }
    else {
        __hour_JComboBox.setVisible ( false );
        __hour_JLabel.setVisible ( false );
    }
    if ( (TimeInterval.MINUTE <= __intervalMax) && (TimeInterval.MINUTE >= __intervalMin) ) {
        __minute_JComboBox.setVisible ( true );
        __minute_JLabel.setVisible ( true );
    }
    else {
        __minute_JComboBox.setVisible ( false );
        __minute_JLabel.setVisible ( false );
    }
}

/**
Return a DateTime (DATE_FAST) representation for the data in the panel.
The date/time will be initialized to TimeUtil.BLANK values if not specified
and will have parts set for the enabled fields that are not blank.
@return a DateTime instance with values set to that of the input components.
*/
public DateTime toDateTime ()
{
    DateTime dt = new DateTime(DateTime.DATE_FAST);
    if ( __year_JComboBox.isEnabled() ) {
        String year = __year_JComboBox.getSelected().trim();
        if ( StringUtil.isInteger(year) ) {
            dt.setYear(Integer.parseInt(year));
        }
        else {
            dt.setYear ( TimeUtil.BLANK_YEAR );
        }
    }
    if ( __month_JComboBox.isEnabled() ) {
        String month = __month_JComboBox.getSelected().trim();
        if ( StringUtil.isInteger(month) ) {
            dt.setMonth(Integer.parseInt(month));
        }
        else {
            dt.setYear ( TimeUtil.BLANK_MONTH );
        }
    }
    if ( __day_JComboBox.isEnabled() ) {
        String day = __day_JComboBox.getSelected().trim();
        if ( StringUtil.isInteger(day) ) {
            dt.setYear(Integer.parseInt(day));
        }
        else {
            dt.setYear ( TimeUtil.BLANK_DAY );
        }
    }
    if ( __hour_JComboBox.isEnabled() ) {
        String hour = __hour_JComboBox.getSelected().trim();
        if ( StringUtil.isInteger(hour) ) {
            dt.setYear(Integer.parseInt(hour));
        }
        else {
            dt.setYear ( TimeUtil.BLANK_HOUR );
        }
    }
    if ( __minute_JComboBox.isEnabled() ) {
        String minute = __minute_JComboBox.getSelected().trim();
        if ( StringUtil.isInteger(minute) ) {
            dt.setYear(Integer.parseInt(minute));
        }
        else {
            dt.setYear ( TimeUtil.BLANK_MINUTE );
        }
    }
    return dt;
}

/**
Return a string representation of the data in the panel, using ISO format.
Enabled fields are included, with default (99) values to indicate blanks.
The year is always included - only parts on the small end are left off if not visible.
*/
public String toString ()
{
    return toString ( true, false );
}

// FIXME SAM 2009-11-13 stripping works for hour and minute but not other parts - probably OK
// for now to keep integrity of the date/time string
/**
Return a string representation of the data in the panel, using ISO format.
Enabled fields are included.  Left-most fields are always included to ensure a parse-able representation of
the string, with default (99) values to indicate blanks.  Right-most fields are only included if non-blank.
@param includeYear indicate whether the year should be included on front of the string.  If the year is not
included, it can be added later if necessary, for example using DateTimeWindow.WINDOW_YEAR.
@param stripBlanksFromEnd if true, strip blanks from the end of the string.
*/
public String toString ( boolean includeYear, boolean stripBlanksFromEnd )
{
    // The following indicate if the delimiter between date and time parts need to be added (because a
    // left-side data value has been encountered).  The intent is to avoid a left-most hanging leading delimiter.
    boolean doDateDelim = false;
    boolean doTimeDelim = false;
    
    String hour = __hour_JComboBox.getSelected().trim();
    String minute = __minute_JComboBox.getSelected().trim();
    StringBuffer yearString = new StringBuffer();
    StringBuffer monthString = new StringBuffer();
    StringBuffer dayString = new StringBuffer();
    StringBuffer hourString = new StringBuffer();
    StringBuffer minuteString = new StringBuffer();
    boolean monthSpecified = false;
    boolean daySpecified = false;
    boolean hourSpecified = false;
    boolean minuteSpecified = false;
    // First form the string parts going from left (year) to right (minute)
    // Year
    if ( includeYear ) {
        if ( __year_JComboBox.isVisible() ) {
            String year = __year_JComboBox.getSelected().trim();
            doDateDelim = true;
            if ( StringUtil.isInteger(year) ) {
                yearString.append ( StringUtil.formatString(Integer.parseInt(year),"%04d"));
            }
            else {
                yearString.append ( StringUtil.formatString(TimeUtil.BLANK_YEAR,"%04d"));
            }
        }
    }
    // Month...
    if ( doDateDelim ) {
        monthString.append ( "-" );
    }
    else {
        // Since month has now been included, need to add the date delimiter to following strings
        doDateDelim = true;
    }
    if ( __month_JComboBox.isVisible() ) {
        String month = __month_JComboBox.getSelected().trim();
        if ( StringUtil.isInteger(month) ) {
            monthString.append ( StringUtil.formatString(Integer.parseInt(month),"%02d"));
            monthSpecified = true;
        }
        else {
            monthString.append ( StringUtil.formatString(TimeUtil.BLANK_MONTH,"%02d"));
        }
    }
    else {
        // Always need to include month
        monthString.append ( StringUtil.formatString(TimeUtil.BLANK_MONTH,"%02d"));
    }
    // Day...
    if ( doDateDelim ) {
        dayString.append ( "-" );
    }
    if ( __day_JComboBox.isVisible() ) {
        String day = __day_JComboBox.getSelected().trim();
        if ( StringUtil.isInteger(day) ) {
            dayString.append ( StringUtil.formatString(Integer.parseInt(day),"%02d"));
            daySpecified = true;
        }
        else {
            dayString.append ( StringUtil.formatString(TimeUtil.BLANK_DAY,"%02d"));
        }
    }
    else {
        // Always need to include day
        dayString.append ( StringUtil.formatString(TimeUtil.BLANK_MONTH,"%02d"));
    }
    // Hour...
    if ( doDateDelim ) {
        hourString.append ( " " );
    }
    doTimeDelim = true;
    if ( __hour_JComboBox.isVisible() ) {
        if ( StringUtil.isInteger(hour) ) {
            hourString.append ( StringUtil.formatString(Integer.parseInt(hour),"%02d"));
            hourSpecified = true;
        }
        else {
            hourString.append ( StringUtil.formatString(TimeUtil.BLANK_HOUR,"%02d"));
        }
    }
    else {
        hourString.append ( StringUtil.formatString(TimeUtil.BLANK_HOUR,"%02d"));
    }
    // Minute...
    if ( doTimeDelim ) {
        minuteString.append ( ":" );
    }
    if ( __minute_JComboBox.isVisible() ) {
        if ( StringUtil.isInteger(minute) ) {
            minuteString.append ( StringUtil.formatString(Integer.parseInt(minute),"%02d"));
            minuteSpecified = true;
        }
        else {
            minuteString.append ( StringUtil.formatString(TimeUtil.BLANK_MINUTE,"%02d"));
        }
    }
    else {
        minuteString.append ( StringUtil.formatString(TimeUtil.BLANK_MINUTE,"%02d"));
    }
    // Now concatenate the strings for the final result - delimiters will have been included
    StringBuffer b = new StringBuffer();
    if ( includeYear ) {
        b.append ( yearString );
    }
    // Include the month only if month and something to the right of month is specified
    if ( monthSpecified || daySpecified || hourSpecified || minuteSpecified ) {
        b.append ( monthString );
    }
    // Include the day only if day and something to the right of day is specified
    if ( daySpecified || hourSpecified || minuteSpecified ) {
        b.append ( dayString );
    }
    // Include the hour only if hour and something to the right of hour is specified
    if ( hourSpecified || minuteSpecified ) {
        b.append ( hourString );
    }
    // Include the minute only if specified
    if ( minuteSpecified ) {
        b.append ( minuteString );
    }
    return b.toString();
}

}