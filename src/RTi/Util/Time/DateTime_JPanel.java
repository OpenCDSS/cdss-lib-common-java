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
Initial DateTime to be displayed.
*/
private DateTime __initial_DateTime = null;

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
Hour choices.
*/
SimpleJComboBox __hour_JComboBox = null;

/**
Minute choices.
*/
SimpleJComboBox __minute_JComboBox = null;

/**
Month choices.
*/
SimpleJComboBox __month_JComboBox = null;

/**
Title of the panel.  If specified, use a border to draw around the panel.
*/
private String __title = "";

/**
Year choices.
*/
SimpleJComboBox __year_JComboBox = null;

// FIXME SAM 2008-01-23 Change intervals from integers to enumeration when
// code base has moved to Java 1.5
/**
Constructor.
*/
public DateTime_JPanel ( String title, int interval_max, int interval_min, DateTime initial )
{   super ();
    setTitle ( title );
    setIntervalMax ( interval_max );
    setIntervalMin ( interval_min );
    setInitialDateTime ( initial );
    setupUI ();
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
    if ( __year_JComboBox != null &&
            (TimeInterval.YEAR <= __interval_max) && (TimeInterval.YEAR >= __interval_min) ) {
        int year = dt.getYear();
        if ( TimeUtil.isValidYear(year) ) {
            __year_JComboBox.select ( "" + year );
        }
        else {
            // Select blank
            __year_JComboBox.select ( "" );
        }
    }
    if ( __month_JComboBox != null &&
            (TimeInterval.YEAR <= __interval_max) && (TimeInterval.YEAR >= __interval_min) ) {
        int month = dt.getMonth();
        if ( TimeUtil.isValidMonth(month) ) {
            __month_JComboBox.select ( "" + month );
        }
         else {
             // Select blank
               __month_JComboBox.select ( "" );
         }
    }
    if ( __day_JComboBox != null &&
            (TimeInterval.YEAR <= __interval_max) && (TimeInterval.YEAR >= __interval_min) ) {
        int day = dt.getDay();
        if ( TimeUtil.isValidDay(day) ) {
            __day_JComboBox.select ( "" + day );
        }
        else {
              // Select blank
            __day_JComboBox.select ( "" );
        }
    }
    if ( __hour_JComboBox != null &&
            (TimeInterval.YEAR <= __interval_max) && (TimeInterval.YEAR >= __interval_min) ) {
        int hour = dt.getHour();
        if ( TimeUtil.isValidHour(hour) ) {
            __hour_JComboBox.select ( "" + hour );
        }
        else {
            // Select blank
            __hour_JComboBox.select ( "" );
        }       
    }
    if ( __minute_JComboBox != null &&
            (TimeInterval.YEAR <= __interval_max) && (TimeInterval.YEAR >= __interval_min) ) {
        int minute = dt.getMinute();
        if ( TimeUtil.isValidMinute(minute) ) {
            __minute_JComboBox.select ( "" + minute );
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
Set the initial DateTime displayed in the panel.  If specified
and the maximum and minimum intervals are not specified, the maximum and
minimum intervals will be determined by the DateTime precision.
If the maximum and minimum intervals are specified at construction, they will
be used to restrict what is displayed from the DateTime instance.
@param interval_max see TimeInterval definitions.
*/
private void setInitialDateTime ( DateTime initial_DateTime )
{
    // Make a copy.
    if ( initial_DateTime != null ) {
        __initial_DateTime = new DateTime ( initial_DateTime );
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
    
    Insets insetsTLBR = new Insets(2,2,2,2);
    setLayout ( new GridBagLayout() );
    
    int x = 0;  // X position in grid bag layout
    int y_label = 0;    // Positions of labels and entry fields.
    int y_entry = 1;
    if ( (TimeInterval.YEAR <= __interval_max) && (TimeInterval.YEAR >= __interval_min) ) {
        // Add the year...
        Vector year_Vector = new Vector();
        year_Vector.addElement ( "" );
        for ( int i = 1900; i <= 2100; i++ ) {
            year_Vector.addElement ( "" + i );
        }
        // Allow edits because may not have all years of interest in the list
        __year_JComboBox = new SimpleJComboBox ( year_Vector, true );
        JGUIUtil.addComponent(this, new JLabel("Year:"),
            x, y_label, 1, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
        JGUIUtil.addComponent(this, __year_JComboBox,
            x++, y_entry, 1, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    }
    if ( (TimeInterval.MONTH <= __interval_max) && (TimeInterval.MONTH >= __interval_min) ) {
        // Add the month...
        Vector month_Vector = new Vector();
        month_Vector.addElement ( "" );    // Select to ignore this information
        for ( int i = 1; i <= 12; i++ ) {
            month_Vector.addElement ( "" + i );
        }
        __month_JComboBox = new SimpleJComboBox ( month_Vector, false );
        JGUIUtil.addComponent(this, new JLabel("Month:"),
            x, y_label, 1, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
        JGUIUtil.addComponent(this, __month_JComboBox,
            x++, y_entry, 1, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    }
    if ( (TimeInterval.DAY <= __interval_max) && (TimeInterval.DAY >= __interval_min) ) {
        // Add the day...
        Vector day_Vector = new Vector();
        day_Vector.addElement ( "" );    // Select to ignore this information
        for ( int i = 1; i <= 31; i++ ) {
            day_Vector.addElement ( "" + i );
        }
        __day_JComboBox = new SimpleJComboBox ( day_Vector, false ); // Not editable
        JGUIUtil.addComponent(this, new JLabel("Day:"),
            x, y_label, 1, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
        JGUIUtil.addComponent(this, __day_JComboBox,
            x++, y_entry, 1, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    }
    if ( (TimeInterval.HOUR <= __interval_max) && (TimeInterval.HOUR >= __interval_min) ) {
        // Add the hour...
        Vector hour_Vector = new Vector();
        hour_Vector.addElement ( "" );    // Select to ignore this information
        for ( int i = 0; i <= 23; i++ ) {
            hour_Vector.addElement ( "" + StringUtil.formatString(i,"%02d") );
        }
        __hour_JComboBox = new SimpleJComboBox ( hour_Vector, false ); // Not editable
        JGUIUtil.addComponent(this, new JLabel("Hour:"),
            x, y_label, 1, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
        JGUIUtil.addComponent(this, __hour_JComboBox,
            x++, y_entry, 1, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    }
    if ( (TimeInterval.MINUTE <= __interval_max) && (TimeInterval.MINUTE >= __interval_min) ) {
        // Add the minute...
        Vector minute_Vector = new Vector();
        minute_Vector.addElement ( "" );    // Select to ignore this information
        for ( int i = 0; i <= 59; i++ ) {
            minute_Vector.addElement ( "" + StringUtil.formatString(i,"%02d") );
        }
        __minute_JComboBox = new SimpleJComboBox ( minute_Vector, true );
        JGUIUtil.addComponent(this, new JLabel("Minute:"),
            x, y_label, 1, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
        JGUIUtil.addComponent(this, __minute_JComboBox,
            x++, y_entry, 1, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    }
    if ( __initial_DateTime != null ) {
        // FIXME SAM 2008-01-23 Need to select the values.
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
    if ( __year_JComboBox != null ) {
        String year = __year_JComboBox.getSelected();
        if ( TimeUtil.isValidYear(year)) {
            d.setYear ( StringUtil.atoi(year) );
        }
    }
    if ( __month_JComboBox != null ) {
        String month = __month_JComboBox.getSelected();
        if ( TimeUtil.isValidMonth(month)) {
            d.setMonth ( StringUtil.atoi(month) );
        }
    }
    if ( __day_JComboBox != null ) {
        String day = __day_JComboBox.getSelected();
        if ( TimeUtil.isValidDay(day)) {
            d.setDay ( StringUtil.atoi(day) );
        }
    }
    if ( __hour_JComboBox != null ) {
        String hour = __hour_JComboBox.getSelected();
        if ( TimeUtil.isValidHour(hour)) {
            d.setHour ( StringUtil.atoi(hour) );
        }
    }
    if ( __minute_JComboBox != null ) {
        String minute = __minute_JComboBox.getSelected();
        if ( TimeUtil.isValidMinute(minute)) {
            d.setMinute ( StringUtil.atoi(minute) );
        }
    }
    return d.toString();
}

}
