package RTi.Util.Time;

import java.awt.Color;
import java.awt.event.ActionListener;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

import java.util.Vector;

import RTi.Util.GUI.JGUIUtil;
import RTi.Util.GUI.SimpleJComboBox;
import RTi.Util.String.StringUtil;

/**
A JPanel to edit a DateTime.
*/
public class DateTime_JPanel extends JPanel
{

/**
Maximum interval shown in the panel.  For example, specifying TimeInterval.MONTH
will omit display of year.
*/
private int __interval_max = TimeInterval.YEAR;

/**
Minimum interval shown in the panel.  For example, specifying TimeInterval.DAY
will omit display of hour, minute, etc.
*/
private int __interval_min = TimeInterval.SECOND;

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
@param interval_max The maximum interval shown in choices (use TimeInterva.YEAR, etc.).
@param interval_min The minimum interval shown in choices (use TimeInterva.HOUR, etc.).
@param initial Initial DateTime to be displayed.  The precision will override interval_min.
*/
public DateTime_JPanel ( String title, int interval_max, int interval_min, DateTime initial )
{   super ();
    setTitle ( title );
    setIntervalMax ( interval_max );
    setIntervalMin ( interval_min );
    setupUI ();
    // Set the date time after setting the interval_min because the DateTime may override.
    if ( initial != null ) {
        setDateTime ( initial );
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
    setIntervalMin ( dt.getPrecision() );
    // Update the visibility of components.
    setVisibility();
    if ( (TimeInterval.YEAR <= __interval_max) && (TimeInterval.YEAR >= __interval_min) ) {
        int year = dt.getYear();
        if ( TimeUtil.isValidYear(year) ) {
            __year_JComboBox.select ( "" + year );
        }
        else {
            // Select blank
            __year_JComboBox.select ( "" );
        }
    }
    if ( (TimeInterval.MONTH <= __interval_max) && (TimeInterval.MONTH >= __interval_min) ) {
        int month = dt.getMonth();
        if ( TimeUtil.isValidMonth(month) ) {
            __month_JComboBox.select ( "" + month );
        }
         else {
             // Select blank
               __month_JComboBox.select ( "" );
         }
    }
    if ( (TimeInterval.DAY <= __interval_max) && (TimeInterval.DAY >= __interval_min) ) {
        int day = dt.getDay();
        if ( TimeUtil.isValidDay(day) ) {
            __day_JComboBox.select ( "" + day );
        }
        else {
              // Select blank
            __day_JComboBox.select ( "" );
        }
    }
    if ( (TimeInterval.HOUR <= __interval_max) && (TimeInterval.HOUR >= __interval_min) ) {
        int hour = dt.getHour();
        if ( TimeUtil.isValidHour(hour) ) {
            __hour_JComboBox.select ( StringUtil.formatString(hour,"%02d") );
        }
        else {
            // Select blank
            __hour_JComboBox.select ( "" );
        }       
    }
    if ( (TimeInterval.MINUTE <= __interval_max) && (TimeInterval.MINUTE >= __interval_min) ) {
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
    __interval_max = interval_max;
}

/**
Set the minimum interval displayed in the panel.
@param interval_min see TimeInterval definitions.
*/
public void setIntervalMin ( int interval_min )
{
    __interval_min = interval_min;
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
    
    int x = 0;  // X position in grid bag layout
    int y_label = 0;    // Positions of labels and entry fields.
    int y_entry = 1;
    // Add the year...
    Vector year_Vector = new Vector();
    year_Vector.addElement ( "" );
    for ( int i = 1900; i <= 2100; i++ ) {
        year_Vector.addElement ( "" + i );
    }
    // Allow edits because may not have all years of interest in the list
    __year_JComboBox = new SimpleJComboBox ( year_Vector, true );
    __year_JLabel = new JLabel("Year:");
    JGUIUtil.addComponent(this, __year_JLabel,
        x, y_label, 1, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(this, __year_JComboBox,
        x++, y_entry, 1, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

    // Add the month...
    Vector month_Vector = new Vector();
    month_Vector.addElement ( "" );    // Select to ignore this information
    for ( int i = 1; i <= 12; i++ ) {
        month_Vector.addElement ( "" + i );
    }
    __month_JComboBox = new SimpleJComboBox ( month_Vector, false );
    __month_JLabel = new JLabel("Month:");
    JGUIUtil.addComponent(this, __month_JLabel,
        x, y_label, 1, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(this, __month_JComboBox,
        x++, y_entry, 1, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

    // Add the day...
    Vector day_Vector = new Vector();
    day_Vector.addElement ( "" );    // Select to ignore this information
    for ( int i = 1; i <= 31; i++ ) {
        day_Vector.addElement ( "" + i );
    }
    __day_JComboBox = new SimpleJComboBox ( day_Vector, false ); // Not editable
    __day_JLabel = new JLabel("Day:");
    JGUIUtil.addComponent(this, __day_JLabel,
        x, y_label, 1, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(this, __day_JComboBox,
        x++, y_entry, 1, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

    // Add the hour...
    Vector hour_Vector = new Vector();
    hour_Vector.addElement ( "" );    // Select to ignore this information
    for ( int i = 0; i <= 23; i++ ) {
        hour_Vector.addElement ( "" + StringUtil.formatString(i,"%02d") );
    }
    __hour_JComboBox = new SimpleJComboBox ( hour_Vector, false ); // Not editable
    __hour_JLabel = new JLabel("Hour:");
    JGUIUtil.addComponent(this, __hour_JLabel,
        x, y_label, 1, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(this, __hour_JComboBox,
        x++, y_entry, 1, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

    // Add the minute...
    Vector minute_Vector = new Vector();
    minute_Vector.addElement ( "" );    // Select to ignore this information
    for ( int i = 0; i <= 59; i++ ) {
        minute_Vector.addElement ( "" + StringUtil.formatString(i,"%02d") );
    }
    __minute_JComboBox = new SimpleJComboBox ( minute_Vector, true );
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
 */
private void setVisibility()
{
    if ( (TimeInterval.YEAR <= __interval_max) && (TimeInterval.YEAR >= __interval_min) ) {
        __year_JComboBox.setVisible ( true );
        __year_JLabel.setVisible ( true );
    }
    else {
        __year_JComboBox.setVisible ( false );
        __year_JLabel.setVisible ( false );
    }
    if ( (TimeInterval.MONTH <= __interval_max) && (TimeInterval.MONTH >= __interval_min) ) {
        __month_JComboBox.setVisible(true);
        __month_JLabel.setVisible(true);
    }
    else {
        __month_JComboBox.setVisible(false);
        __month_JLabel.setVisible(false);
    }
    if ( (TimeInterval.DAY <= __interval_max) && (TimeInterval.DAY >= __interval_min) ) {
        __day_JComboBox.setVisible(true);
        __day_JLabel.setVisible(true);
    }
    else {
        __day_JComboBox.setVisible(false);
        __day_JLabel.setVisible(false);
    }
    if ( (TimeInterval.HOUR <= __interval_max) && (TimeInterval.HOUR >= __interval_min) ) {
        __hour_JComboBox.setVisible ( true );
        __hour_JLabel.setVisible ( true );
    }
    else {
        __hour_JComboBox.setVisible ( false );
        __hour_JLabel.setVisible ( false );
    }
    if ( (TimeInterval.MINUTE <= __interval_max) && (TimeInterval.MINUTE >= __interval_min) ) {
        __minute_JComboBox.setVisible ( true );
        __minute_JLabel.setVisible ( true );
    }
    else {
        __minute_JComboBox.setVisible ( false );
        __minute_JLabel.setVisible ( false );
    }
}

/**
Return a string representation of the data in the panel, using ISO formats.
*/
public String toString()
{
    DateTime d = new DateTime(__interval_min|DateTime.DATE_FAST );
    // Initialize to all 9s to indicate value not relevant.
    // These can be checked for in other code.
    d.setYear( TimeUtil.BLANK_YEAR );
    d.setMonth ( TimeUtil.BLANK_MONTH );
    d.setDay ( TimeUtil.BLANK_DAY );
    d.setHour ( TimeUtil.BLANK_HOUR );
    d.setMinute ( TimeUtil.BLANK_MINUTE );
    if ( __year_JComboBox.isVisible() ) {
        String year = __year_JComboBox.getSelected();
        if ( TimeUtil.isValidYear(year)) {
            d.setYear ( StringUtil.atoi(year) );
        }
    }
    if ( __month_JComboBox.isVisible() ) {
        String month = __month_JComboBox.getSelected();
        if ( TimeUtil.isValidMonth(month)) {
            d.setMonth ( StringUtil.atoi(month) );
        }
    }
    if ( __day_JComboBox.isVisible() ) {
        String day = __day_JComboBox.getSelected();
        if ( TimeUtil.isValidDay(day)) {
            d.setDay ( StringUtil.atoi(day) );
        }
    }
    if ( __hour_JComboBox.isVisible() ) {
        String hour = __hour_JComboBox.getSelected();
        if ( TimeUtil.isValidHour(hour)) {
            d.setHour ( StringUtil.atoi(hour) );
        }
    }
    if ( __minute_JComboBox.isVisible() ) {
        String minute = __minute_JComboBox.getSelected();
        if ( TimeUtil.isValidMinute(minute)) {
            d.setMinute ( StringUtil.atoi(minute) );
        }
    }
    // Set the precision to ignore blank input, starting from most precise.
    // This will minimize showing invalid inputs like 99 for hour.
    
    int [] intervals = {
            TimeInterval.MINUTE,
            TimeInterval.HOUR,
            TimeInterval.DAY,
            TimeInterval.MONTH,
            TimeInterval.YEAR
    };
    SimpleJComboBox cb = null;
    for ( int i = 0; i < intervals.length; i++ ) {
        if ( intervals[i] == TimeInterval.MINUTE ) {
            cb = __minute_JComboBox;
        }
        else if ( intervals[i] == TimeInterval.HOUR ) {
            cb = __hour_JComboBox;
        }
        else if ( intervals[i] == TimeInterval.DAY ) {
            cb = __day_JComboBox;
        }
        else if ( intervals[i] == TimeInterval.MONTH ) {
            cb = __month_JComboBox;
        }
        if ( cb.isVisible() ) {
            String val = cb.getSelected();
            if ( val.length() != 0 ) {
                // Value is specified so assume precision
                d.setPrecision ( intervals[i] );
                break;
            }
        }
    }
    return d.toString();
}

}
