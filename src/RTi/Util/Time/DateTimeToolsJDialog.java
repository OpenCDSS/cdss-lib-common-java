package RTi.Util.Time;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import RTi.Util.GUI.JGUIUtil;
import RTi.Util.GUI.SimpleJButton;
import RTi.Util.GUI.SimpleJComboBox;
import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;

/**
Dialog to provide useful date/time tools to help understand date/time data.
*/
@SuppressWarnings("serial")
public class DateTimeToolsJDialog extends JDialog
implements ActionListener, ItemListener, KeyListener, WindowListener
{

// Date/time <=> UNIX time components
private JTextField dateToUnixYear_JTextField = null;
private JTextField dateToUnixMonth_JTextField = null;
private JTextField dateToUnixDay_JTextField = null;
private JTextField dateToUnixHour_JTextField = null;
private JTextField dateToUnixMinute_JTextField = null;
private JTextField dateToUnixSecond_JTextField = null;
private SimpleJComboBox dateToUnixTimeZoneIDReq_JComboBox = null; // Requested, with leading GMT offset (default = computer local)
private JTextField dateToUnixTimeZoneIDReq_JTextField = null; // Requested, without leading GMT offset
private JTextField dateToUnixTimeZoneIDUsed_JTextField = null; // What was actually used
private JTextField dateToUnixTimeZoneIDUsedDisplayNameLong_JTextField = null;
private JTextField dateToUnixTimeZoneIDUsedDisplayNameShort_JTextField = null;
private JTextField dateToUnixTimeZoneIDUsedIsDST_JTextField = null;

private JTextField dateToUnixUnixMs_JTextField = null;
private JTextField dateToUnixUnixSeconds_JTextField = null;
private JTextField dateToUnixUnixMsComputed_JTextField = null;

// Right side

private JTextField unixToDateUnixMs_JTextField = null;
private JTextField unixToDateUnixSeconds_JTextField = null;
private JTextField unixToDateDateTime_JTextField = null;
private JTextField unixToDateDateTimeComputed_JTextField = null;

//Date/time information components

private SimpleJComboBox timeZones_JComboBox = null;

private JTextField timeZoneID_JTextField = null;
private JTextField timeZoneDisplayName_JTextField = null;
private JTextField timeZoneDisplayNameShort_JTextField = null;
private JTextField timeZoneDisplayNameLong_JTextField = null;
private JTextField timeZoneDisplayNameDSTShort_JTextField = null;
private JTextField timeZoneDisplayNameDSTLong_JTextField = null;
private JTextField timeZoneDSTSavings_JTextField = null;
private JTextField timeZoneRawOffset_JTextField = null;
private JTextField timeZoneObservesDST_JTextField = null;
	
private SimpleJButton __close_JButton = null;

/**
DateTimeConverterJDialog constructor.
@param parent JFrame class instantiating this class.
*/
public DateTimeToolsJDialog ( JFrame parent )
{	super(parent, false);
	initialize ();
}

/**
Responds to ActionEvents.
@param event ActionEvent object
*/
public void actionPerformed( ActionEvent event )
{	Object o = event.getSource();

	if ( o == __close_JButton ) {
		closeDialog ();
	}
}

/**
Calculate date/time from UNIX time and display.
*/
private void calculateDateFromUnixTime () {
	String unixToDateTime = "";
	String unixToDateTimeComputed = "";
	//String timeZoneUsed = "";
	//String timeZoneUsedDisplayNameLong = "";
	//String timeZoneUsedDisplayNameShort = "";
	//String timeZoneUsedIsDST = "";
	try {
		long unixTimeSeconds = 0;
		long unixTimeMs = 0;
		// Get the text fields, convert to numerical date/time values and calculate output
		String unixMsString = this.unixToDateUnixMs_JTextField.getText().trim();
		String unixSecondsString = this.unixToDateUnixSeconds_JTextField.getText().trim();
		//String timeZone = this.dateToUnixTimeZoneIDReq_JComboBox.getSelected();
		int inputCount = 0;
		if ( !unixMsString.isEmpty() ) {
			unixTimeMs = Long.parseLong(unixMsString.trim());
			unixTimeSeconds = unixTimeMs/1000;
			++inputCount;
		}
		if ( !unixSecondsString.isEmpty() ) {
			unixTimeSeconds = Long.parseLong(unixSecondsString.trim());
			unixTimeMs = unixTimeSeconds*1000;
			++inputCount;
		}
		if ( inputCount == 1 ) {
			// Can calculate
			// Compute using Java internals
			Date d = new Date(unixTimeMs); // Date will use local time zone so for MST the time will show -7 hours (Date.getTimeZoneOffset() = 420)
			//Message.printStatus(2, "", "New Date:"+ d + ", offset=" + d.getTimezoneOffset());
			DateTime dt = new DateTime(d); // Will use local time zone from original Date so for a MST time will show 7+ hours more time
			dt.addMinute(d.getTimezoneOffset()); // Therefore adjust back to GMT
			dt.setTimeZone("GMT");
			//Message.printStatus(2, "", "DateTime in GMT:"+ dt);
			unixToDateTime = dt.toString();
			// Compute date/time using TSTool library
			DateTime dtComputed = TimeUtil.fromUnixTime(unixTimeMs, null);
			unixToDateTimeComputed = dtComputed.toString();
		}
		else {
			// Don't have enough data
			unixToDateTime = "Enter UNIX time ms or seconds";
			unixToDateTimeComputed = "Enter UNIX time ms or seconds";
		}
	}
	catch ( Exception e ) {
		unixToDateTime = "Error (" + e + ")";
		unixToDateTimeComputed = "Error (" + e + ")";
		Message.printWarning(3,"",e);
	}
	this.unixToDateDateTime_JTextField.setText(unixToDateTime);
	this.unixToDateDateTimeComputed_JTextField.setText(unixToDateTimeComputed);
}

/**
Calculate UNIX time from the date and display.
*/
private void calculateUnixTimeFromDate () {
	String timeZoneUsed = "";
	String timeZoneUsedDisplayNameLong = "";
	String timeZoneUsedDisplayNameShort = "";
	String timeZoneUsedIsDST = "";
	String unixTimeMsString = "";
	String unixTimeSecondsString = "";
	String unixTimeComputedMsString = "";
	try {
		long unixTimeSeconds = 0;
		long unixTimeMs = 0;
		// Get the text fields, convert to numerical date/time values and calculate output
		String yearString = this.dateToUnixYear_JTextField.getText().trim();
		String monthString = this.dateToUnixMonth_JTextField.getText().trim();
		String dayString = this.dateToUnixDay_JTextField.getText().trim();
		String hourString = this.dateToUnixHour_JTextField.getText().trim();
		String minuteString = this.dateToUnixMinute_JTextField.getText().trim();
		String secondString = this.dateToUnixSecond_JTextField.getText().trim();
		// First get the time zone that is requested with leading "(GMT-07:00)", etc.
		String timeZone = this.dateToUnixTimeZoneIDReq_JComboBox.getSelected();
		// Strip off to just get the time zone ID
		int pos = timeZone.indexOf(")");
		timeZone = timeZone.substring(pos+1).trim();
		this.dateToUnixTimeZoneIDReq_JTextField.setText(timeZone);
		int year = 1970, month = 1, day = 1, hour = 0, minute = 0, second = 0;
		int inputCount = 0;
		if ( !yearString.isEmpty() ) {
			year = Integer.parseInt(yearString);
			++inputCount;
		}
		if ( !monthString.isEmpty() ) {
			month = Integer.parseInt(monthString);
			++inputCount;
		}
		if ( !dayString.isEmpty() ) {
			day = Integer.parseInt(dayString);
			++inputCount;
		}
		if ( !hourString.isEmpty() ) {
			hour = Integer.parseInt(hourString);
			++inputCount;
		}
		if ( !minuteString.isEmpty() ) {
			minute = Integer.parseInt(minuteString);
			++inputCount;
		}
		if ( !secondString.isEmpty() ) {
			second = Integer.parseInt(secondString);
			++inputCount;
		}
		if ( inputCount == 6 ) {
			// Have all the data input and can calculate
			boolean doDateTime = true;
			if ( doDateTime ) {
				DateTime dt = new DateTime(DateTime.DATE_ZERO);
				dt.setYear(year);
				dt.setMonth(month);
				dt.setDay(day);
				dt.setHour(hour);
				dt.setMinute(minute);
				dt.setSecond(second);
				dt.setTimeZone(timeZone);
				Date d = null;
				TimeZone tz = null;
				if ( !timeZone.isEmpty() ) {
					// Use the requested time zone
					d = dt.getDate(timeZone);
					unixTimeMs = d.getTime();
					timeZoneUsed = timeZone;
					tz = TimeZone.getTimeZone(timeZoneUsed);
				}
				else {
					// Requested time zone is empty so default to the local time
					d = dt.getDate(TimeZoneDefaultType.LOCAL);
					unixTimeMs = d.getTime();
					// Don't have an actual time zone in Date, only have offset
					// Could display as follows...
					//timeZoneUsed = "-" + StringUtil.formatString(d.getTimezoneOffset()/60,"%02d") +
					//	StringUtil.formatString(d.getTimezoneOffset()%60,"%02d") + " default local, hhmm offset from GMT";
					// However, better to actually get the time zone
					tz = TimeZone.getDefault(); // This is what DateTime.getDate(TimeZoneDefaultType.LOCAL) does
					timeZoneUsed = tz.getID();
				}
				boolean isDST = tz.inDaylightTime(d);
				timeZoneUsedDisplayNameLong = tz.getDisplayName(isDST,TimeZone.LONG);
				timeZoneUsedDisplayNameShort = tz.getDisplayName(isDST,TimeZone.SHORT);
				timeZoneUsedIsDST = "" + isDST;
				unixTimeComputedMsString = "" + TimeUtil.toUnixTime(dt,true);
			}
			unixTimeSeconds = unixTimeMs/1000; 
			unixTimeSecondsString = "" + unixTimeSeconds;
			unixTimeMsString = "" + unixTimeMs;
		}
		else {
			// Don't have enough data
			timeZoneUsed = "Enter Year...Seconds";
			timeZoneUsedDisplayNameLong = "Enter Year...Seconds";
			timeZoneUsedDisplayNameShort = "Enter Year...Seconds";
			timeZoneUsedIsDST = "Enter Year...Seconds";
			unixTimeSecondsString = "Enter Year...Seconds";
			unixTimeMsString = "Enter Year...Seconds";
			unixTimeComputedMsString = "Enter Year...Seconds";
		}
	}
	catch ( Exception e ) {
		timeZoneUsed = "Error (" + e + ")";
		timeZoneUsed = "Error (" + e + ")";
		timeZoneUsedDisplayNameLong = "Error (" + e + ")";
		timeZoneUsedDisplayNameShort = "Error (" + e + ")";
		unixTimeSecondsString = "Error (" + e + ")";
		unixTimeMsString = "Error (" + e + ")";
	}
	this.dateToUnixTimeZoneIDUsed_JTextField.setText(timeZoneUsed);
	this.dateToUnixTimeZoneIDUsedDisplayNameLong_JTextField.setText(timeZoneUsedDisplayNameLong);
	this.dateToUnixTimeZoneIDUsedDisplayNameShort_JTextField.setText(timeZoneUsedDisplayNameShort);
	this.dateToUnixTimeZoneIDUsedIsDST_JTextField.setText(timeZoneUsedIsDST);
	this.dateToUnixUnixMs_JTextField.setText(unixTimeMsString);
	this.dateToUnixUnixSeconds_JTextField.setText(unixTimeSecondsString);
	this.dateToUnixUnixMsComputed_JTextField.setText(unixTimeComputedMsString);
}

/**
Close the dialog.
*/
private void closeDialog ()
{
	setVisible( false );
	dispose();
}

/**
Instantiates the UI components.
*/
private void initialize ()
{	addWindowListener( this );

    Insets insetsTLBR = new Insets(2,2,2,2);

	JTabbedPane main_JTabbedPane = new JTabbedPane();
	GridBagLayout gbl = new GridBagLayout();
	JPanel main_JPanel = new JPanel();
	main_JPanel.setLayout( gbl );
        JGUIUtil.addComponent(main_JPanel, main_JTabbedPane,
		0, 0, 1, 1, 1, 1, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.CENTER);
	getContentPane().add ( "North", main_JPanel );
	
	// UNIX time ...
	JPanel unix_JPanel = new JPanel();
	unix_JPanel.setLayout ( gbl );
	main_JTabbedPane.addTab ( "Date/time to/from UNIX", unix_JPanel );

    int yUnixToDate = -1;
    JGUIUtil.addComponent(unix_JPanel,
        new JLabel("UNIX time is the number of seconds since midnight, January 1, 1970 (see: https://en.wikipedia.org/wiki/Unix_time)."),
        0, ++yUnixToDate, 11, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(unix_JPanel,
        new JLabel("Each day is 86400 seconds.  Computer programs often use this representation for date/time conversions and time math."),
        0, ++yUnixToDate, 11, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(unix_JPanel,
        new JLabel("UNIX time does not reflect leap seconds - it is calculated based on the number of days and seconds in the day."),
        0, ++yUnixToDate, 11, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(unix_JPanel,
        new JSeparator(SwingConstants.HORIZONTAL),
        0, ++yUnixToDate, 11, 1, 0, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    
    // Add panel on left to go from date to UNIX time

    int yDateToUnitDate = -1;
    JPanel dateToUnix_JPanel = new JPanel();
    dateToUnix_JPanel.setLayout ( gbl );
    JGUIUtil.addComponent(unix_JPanel, dateToUnix_JPanel,
        0, ++yUnixToDate, 5, 1, 1, 1, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    
    JGUIUtil.addComponent(dateToUnix_JPanel, new JLabel("Enter a date/time to see UNIX time."),
        0, ++yDateToUnitDate, 5, 1, 1, 1, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(dateToUnix_JPanel, new JLabel("Year (YYYY, 1970+):"),
        0, ++yDateToUnitDate, 1, 1, 1, 1, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    this.dateToUnixYear_JTextField = new JTextField(5);
    this.dateToUnixYear_JTextField.setToolTipText("4-digit year for date/time to convert to UNIX time.");
    this.dateToUnixYear_JTextField.addKeyListener(this);
    JGUIUtil.addComponent(dateToUnix_JPanel, this.dateToUnixYear_JTextField,
        1, yDateToUnitDate, 4, 1, 1, 1, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(dateToUnix_JPanel, new JLabel("Month (1-12):"),
        0, ++yDateToUnitDate, 1, 1, 1, 1, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    this.dateToUnixMonth_JTextField = new JTextField(5);
    this.dateToUnixMonth_JTextField.setToolTipText("Month (1-12, 1=January) for date/time to convert to UNIX time.");
    this.dateToUnixMonth_JTextField.addKeyListener(this);
    JGUIUtil.addComponent(dateToUnix_JPanel, this.dateToUnixMonth_JTextField,
        1, yDateToUnitDate, 4, 1, 1, 1, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(dateToUnix_JPanel, new JLabel("Day (1-31):"),
        0, ++yDateToUnitDate, 1, 1, 1, 1, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    this.dateToUnixDay_JTextField = new JTextField(5);
    this.dateToUnixDay_JTextField.setToolTipText("Day of month (1-31) for date/time to convert to UNIX time.");
    this.dateToUnixDay_JTextField.addKeyListener(this);
    JGUIUtil.addComponent(dateToUnix_JPanel, this.dateToUnixDay_JTextField,
        1, yDateToUnitDate, 4, 1, 1, 1, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(dateToUnix_JPanel, new JLabel("Hour (0-23):"),
        0, ++yDateToUnitDate, 1, 1, 1, 1, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    this.dateToUnixHour_JTextField = new JTextField(5);
    this.dateToUnixHour_JTextField.setToolTipText("Hour of day (0-23) for date/time to convert to UNIX time.");
    this.dateToUnixHour_JTextField.addKeyListener(this);
    JGUIUtil.addComponent(dateToUnix_JPanel, this.dateToUnixHour_JTextField,
        1, yDateToUnitDate, 4, 1, 1, 1, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(dateToUnix_JPanel, new JLabel("Minute (0-59):"),
        0, ++yDateToUnitDate, 1, 1, 1, 1, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    this.dateToUnixMinute_JTextField = new JTextField(5);
    this.dateToUnixMinute_JTextField.setToolTipText("Minute of hour (0-59) for date/time to convert to UNIX time.");
    this.dateToUnixMinute_JTextField.setToolTipText("Specify blank to use default time zone from computer.");
    this.dateToUnixMinute_JTextField.addKeyListener(this);
    JGUIUtil.addComponent(dateToUnix_JPanel, this.dateToUnixMinute_JTextField,
        1, yDateToUnitDate, 4, 1, 1, 1, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(dateToUnix_JPanel, new JLabel("Second (0-59):"),
        0, ++yDateToUnitDate, 1, 1, 1, 1, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    this.dateToUnixSecond_JTextField = new JTextField(5);
    this.dateToUnixSecond_JTextField.setToolTipText("Second of minute (0-59) for date/time to convert to UNIX time.");
    this.dateToUnixSecond_JTextField.addKeyListener(this);
    JGUIUtil.addComponent(dateToUnix_JPanel, this.dateToUnixSecond_JTextField,
        1, yDateToUnitDate, 4, 1, 1, 1, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(dateToUnix_JPanel, new JLabel("Time zone (requested):"),
        0, ++yDateToUnitDate, 1, 1, 1, 1, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    // JComboBox has (GMT+HH:MM) in front sorted to make it easier to find time zone
    this.dateToUnixTimeZoneIDReq_JComboBox = new SimpleJComboBox(false);
    String [] ids = TimeZone.getAvailableIDs();
    List<String> timeZoneIDs = new ArrayList<String>();
    long hours, minutes;
    TimeZone tz;
    // First add the positive time zones (east of GMT or equivalent to GMT)
    for ( int i = 0; i < ids.length; i++ ) {
    	tz = TimeZone.getTimeZone(ids[i]);
    	if ( tz.getRawOffset() >= 0 ) {
    		hours = TimeUnit.MILLISECONDS.toHours(tz.getRawOffset());
    		// Make minutes positive since negative is prefixed to hour
    		minutes = Math.abs(TimeUnit.MILLISECONDS.toMinutes(tz.getRawOffset()) - TimeUnit.HOURS.toMinutes(hours));
    		timeZoneIDs.add(String.format("(GMT%+03d:%02d) %s", hours, minutes, tz.getID()));
    	}
    }
    // Sort descending
    timeZoneIDs = StringUtil.sortStringList(timeZoneIDs,StringUtil.SORT_DESCENDING,null,false,true);
    // Now do the same for the negative zones but sort ascending
    List<String> timeZoneIDs2 = new ArrayList<String>();
    for ( int i = 0; i < ids.length; i++ ) {
    	tz = TimeZone.getTimeZone(ids[i]);
    	if ( tz.getRawOffset() < 0 ) {
    		hours = TimeUnit.MILLISECONDS.toHours(tz.getRawOffset());
    		// Make minutes positive since negative is prefixed to hour
    		minutes = Math.abs(TimeUnit.MILLISECONDS.toMinutes(tz.getRawOffset()) - TimeUnit.HOURS.toMinutes(hours));
    		timeZoneIDs2.add(String.format("(GMT%+03d:%02d) %s", hours, minutes, tz.getID()));
    	}
    }
    timeZoneIDs2 = StringUtil.sortStringList(timeZoneIDs2,StringUtil.SORT_ASCENDING,null,false,true);
    timeZoneIDs.addAll(timeZoneIDs2);
    // Add a blank at the front to default to local time
    timeZoneIDs.add(0,""); // Default
    this.dateToUnixTimeZoneIDReq_JComboBox.setData(timeZoneIDs);
    this.dateToUnixTimeZoneIDReq_JComboBox.setToolTipText("Recognized time zone ID, prepended with raw offset from GMT (no daylight savings).");
    this.dateToUnixTimeZoneIDReq_JComboBox.addItemListener(this);
    JGUIUtil.addComponent(dateToUnix_JPanel, this.dateToUnixTimeZoneIDReq_JComboBox,
        1, yDateToUnitDate, 4, 1, 1, 1, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(dateToUnix_JPanel, new JSeparator(SwingConstants.HORIZONTAL),
        0, ++yDateToUnitDate, 5, 1, 1, 1, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

    JGUIUtil.addComponent(dateToUnix_JPanel, new JLabel("Time zone (requested) ID:"),
        0, ++yDateToUnitDate, 1, 1, 1, 1, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    this.dateToUnixTimeZoneIDReq_JTextField = new JTextField(25);
    this.dateToUnixTimeZoneIDReq_JTextField.setToolTipText("Requested time zone ID.");
    this.dateToUnixTimeZoneIDReq_JTextField.setEditable(false);
    JGUIUtil.addComponent(dateToUnix_JPanel, this.dateToUnixTimeZoneIDReq_JTextField,
        1, yDateToUnitDate, 4, 1, 1, 1, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    
    JGUIUtil.addComponent(dateToUnix_JPanel, new JLabel("Time zone (used) ID:"),
        0, ++yDateToUnitDate, 1, 1, 1, 1, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    this.dateToUnixTimeZoneIDUsed_JTextField = new JTextField(25);
    this.dateToUnixTimeZoneIDUsed_JTextField.setToolTipText("Time zone ID that is used (will default to system if requested time zone is not specified).");
    this.dateToUnixTimeZoneIDUsed_JTextField.setEditable(false);
    JGUIUtil.addComponent(dateToUnix_JPanel, this.dateToUnixTimeZoneIDUsed_JTextField,
        1, yDateToUnitDate, 4, 1, 1, 1, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    
    JGUIUtil.addComponent(dateToUnix_JPanel, new JLabel("Time zone (used) display name (long):"),
        0, ++yDateToUnitDate, 1, 1, 1, 1, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    this.dateToUnixTimeZoneIDUsedDisplayNameLong_JTextField = new JTextField(25);
    this.dateToUnixTimeZoneIDUsedDisplayNameLong_JTextField.setToolTipText("Long time zone name that is in effect (may reflect daylight savings time).");
    this.dateToUnixTimeZoneIDUsedDisplayNameLong_JTextField.setEditable(false);
    JGUIUtil.addComponent(dateToUnix_JPanel, this.dateToUnixTimeZoneIDUsedDisplayNameLong_JTextField,
        1, yDateToUnitDate, 4, 1, 1, 1, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    
    JGUIUtil.addComponent(dateToUnix_JPanel, new JLabel("Time zone (used) display name (short):"),
        0, ++yDateToUnitDate, 1, 1, 1, 1, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    this.dateToUnixTimeZoneIDUsedDisplayNameShort_JTextField = new JTextField(25);
    this.dateToUnixTimeZoneIDUsedDisplayNameShort_JTextField.setToolTipText("Short time zone name that is in effect (may reflect daylight savings time).");
    this.dateToUnixTimeZoneIDUsedDisplayNameShort_JTextField.setEditable(false);
    JGUIUtil.addComponent(dateToUnix_JPanel, this.dateToUnixTimeZoneIDUsedDisplayNameShort_JTextField,
        1, yDateToUnitDate, 4, 1, 1, 1, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    
    JGUIUtil.addComponent(dateToUnix_JPanel, new JLabel("Time zone is daylight savings time?:"),
        0, ++yDateToUnitDate, 1, 1, 1, 1, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    this.dateToUnixTimeZoneIDUsedIsDST_JTextField = new JTextField(25);
    this.dateToUnixTimeZoneIDUsedIsDST_JTextField.setToolTipText("Is time zone in daylight savings for indicated date/time?");
    this.dateToUnixTimeZoneIDUsedIsDST_JTextField.setEditable(false);
    JGUIUtil.addComponent(dateToUnix_JPanel, this.dateToUnixTimeZoneIDUsedIsDST_JTextField,
        1, yDateToUnitDate, 4, 1, 1, 1, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    
    JGUIUtil.addComponent(dateToUnix_JPanel, new JLabel("UNIX time, from system (ms):"),
    	0, ++yDateToUnitDate, 1, 1, 1, 1, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	this.dateToUnixUnixMs_JTextField = new JTextField(15);
	this.dateToUnixUnixMs_JTextField.setEditable(false);
	JGUIUtil.addComponent(dateToUnix_JPanel, this.dateToUnixUnixMs_JTextField,
	    1, yDateToUnitDate, 4, 1, 1, 1, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

    JGUIUtil.addComponent(dateToUnix_JPanel, new JLabel("UNIX time, from system (seconds):"),
    	0, ++yDateToUnitDate, 1, 1, 1, 1, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	this.dateToUnixUnixSeconds_JTextField = new JTextField(15);
	this.dateToUnixUnixSeconds_JTextField.setEditable(false);
	JGUIUtil.addComponent(dateToUnix_JPanel, this.dateToUnixUnixSeconds_JTextField,
	    1, yDateToUnitDate, 4, 1, 1, 1, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	
    JGUIUtil.addComponent(dateToUnix_JPanel, new JLabel("UNIX time using GMT, computed (ms):"),
    	0, ++yDateToUnitDate, 1, 1, 1, 1, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	this.dateToUnixUnixMsComputed_JTextField = new JTextField(15);
	this.dateToUnixUnixMsComputed_JTextField.setToolTipText("UNIX time computed as days since Jan 1, 1970 00:00:00 * 86400 + seconds in day, ignoring time zone.");
	this.dateToUnixUnixMsComputed_JTextField.setEditable(false);
	JGUIUtil.addComponent(dateToUnix_JPanel, this.dateToUnixUnixMsComputed_JTextField,
	    1, yDateToUnitDate, 4, 1, 1, 1, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	
	// Cause output to update
	calculateUnixTimeFromDate();
    
    // Vertical separator
    
    JGUIUtil.addComponent(unix_JPanel, new JSeparator(SwingConstants.VERTICAL),
            5, yUnixToDate, 1, 1, 1, 1, insetsTLBR, GridBagConstraints.VERTICAL, GridBagConstraints.WEST);
    
    // Add panel on left to go from UNIX time to date
    
    JPanel unixToDate_JPanel = new JPanel();
    unixToDate_JPanel.setLayout ( gbl );
    JGUIUtil.addComponent(unix_JPanel, unixToDate_JPanel,
        6, yUnixToDate, 5, 1, 1, 1, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    
    JGUIUtil.addComponent(unixToDate_JPanel, new JLabel("Enter UNIX time as ms or seconds to convert to date/time."),
            0, ++yUnixToDate, 5, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(unixToDate_JPanel, new JLabel("UNIX time (ms):"),
    	0, ++yUnixToDate, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	this.unixToDateUnixMs_JTextField = new JTextField(15);
	this.unixToDateUnixMs_JTextField.addKeyListener(this);
	JGUIUtil.addComponent(unixToDate_JPanel, this.unixToDateUnixMs_JTextField,
	    1, yUnixToDate, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

    JGUIUtil.addComponent(unixToDate_JPanel, new JLabel("UNIX time (seconds):"),
    	0, ++yUnixToDate, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	this.unixToDateUnixSeconds_JTextField = new JTextField(15);
	this.unixToDateUnixSeconds_JTextField.addKeyListener(this);
	JGUIUtil.addComponent(unixToDate_JPanel, this.unixToDateUnixSeconds_JTextField,
	    1, yUnixToDate, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	
	JGUIUtil.addComponent(unixToDate_JPanel, new JLabel("Date/time, GMT, from system:"),
    	0, ++yUnixToDate, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	this.unixToDateDateTime_JTextField = new JTextField(20);
	this.unixToDateDateTime_JTextField.setToolTipText("Date/time, from system using Java packages");
	this.unixToDateDateTime_JTextField.setEditable(false);
	JGUIUtil.addComponent(unixToDate_JPanel, this.unixToDateDateTime_JTextField,
	    1, yUnixToDate, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	
    JGUIUtil.addComponent(unixToDate_JPanel, new JLabel("Date/time, GMT, computed:"),
    	0, ++yUnixToDate, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	this.unixToDateDateTimeComputed_JTextField = new JTextField(20);
	this.unixToDateDateTimeComputed_JTextField.setToolTipText("Date/time calculated by TSTool using days since Jan 1, 1970 * 86400 seconds per day");
	this.unixToDateDateTimeComputed_JTextField.setEditable(false);
	JGUIUtil.addComponent(unixToDate_JPanel, this.unixToDateDateTimeComputed_JTextField,
	    1, yUnixToDate, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	
	// Cause output to update
	calculateDateFromUnixTime();
	
	// Initial date/time information panel

    initializeTimeInfoPanel(main_JTabbedPane);
   
	// South Panel: North
	JPanel button_JPanel = new JPanel();
	button_JPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        JGUIUtil.addComponent(main_JPanel, button_JPanel, 
		0, 1, 1, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER);

	__close_JButton = new SimpleJButton("Close", this);
	button_JPanel.add ( __close_JButton );

	setTitle ( JGUIUtil.getAppNameForWindows() + " - Date/time Tools" );
	setResizable ( true );
    pack();
    JGUIUtil.center( this );
    super.setVisible( true );
}

/**
 * Initialize time and time zone information to help understand system.
 * @param main_JTabbedPane
 */
private void initializeTimeInfoPanel ( JTabbedPane main_JTabbedPane ) {

    Insets insetsTLBR = new Insets(2,2,2,2);
    GridBagLayout gbl = new GridBagLayout();
	JPanel info_JPanel = new JPanel();
	info_JPanel.setLayout ( gbl );
	main_JTabbedPane.addTab ( "Time Information", info_JPanel );

    int yInfo = -1;
    DateTime current = new DateTime(DateTime.DATE_CURRENT);
	TimeZone tz = TimeZone.getDefault();
	Date now = new Date();
    JGUIUtil.addComponent(info_JPanel,
        new JLabel("Current time from Java Date:  " + current ),
        0, ++yInfo, 11, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(info_JPanel,
        new JLabel("Time zone from Java default time zone ID:  " + TimeZone.getDefault().getID() ),
        0, ++yInfo, 11, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(info_JPanel,
        new JLabel("Time zone from Java default time zone display name:  " + TimeZone.getDefault().getDisplayName() ),
        0, ++yInfo, 11, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(info_JPanel,
        new JLabel("Long time zone from Java default time zone display name (if not in savings):  " +
        TimeZone.getDefault().getDisplayName(false,TimeZone.LONG) ),
        0, ++yInfo, 11, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(info_JPanel,
        new JLabel("Short time zone from Java default time zone display name (if not in savings):  " +
        TimeZone.getDefault().getDisplayName(false,TimeZone.SHORT) ),
        0, ++yInfo, 11, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(info_JPanel,
        new JLabel("Long time zone from Java default time zone display name (if in daylight savings):  " +
        TimeZone.getDefault().getDisplayName(true,TimeZone.LONG) ),
        0, ++yInfo, 11, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(info_JPanel,
        new JLabel("Short time zone from Java default time zone display name (if in daylight savings):  " +
        TimeZone.getDefault().getDisplayName(true,TimeZone.SHORT) ),
        0, ++yInfo, 11, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(info_JPanel,
        new JLabel("Is current time in daylight savings?  " + tz.inDaylightTime(now) ),
        0, ++yInfo, 11, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    if ( tz.inDaylightTime(now) ) {
    	String hours = "";
    	if ( ((tz.getOffset(now.getTime())/1000) % 3600) == 0 ) {
    		// Only show hours if evenly divides
    		hours = "" + (tz.getOffset(now.getTime())/1000/3600);
    	}
    	else {
    		hours = "" + StringUtil.formatString(((double)tz.getOffset(now.getTime())/1000/3600),"%.2f");
    	}
        JGUIUtil.addComponent(info_JPanel,
            new JLabel("Shift from UTC to get to current local time (ms/s/m/h) = " + tz.getOffset(now.getTime()) +
            	" / " + (tz.getOffset(now.getTime())/1000) + " / " + (tz.getOffset(now.getTime())/1000/60) +
            	" / " + hours),
            0, ++yInfo, 11, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    }
    JGUIUtil.addComponent(info_JPanel,
        new JSeparator(SwingConstants.HORIZONTAL),
        0, ++yInfo, 11, 1, 0, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    
    // Add panel on left to select available time zone

    int yDateToUnitDate = -1;
    JPanel timeZoneList_JPanel = new JPanel();
    timeZoneList_JPanel.setLayout ( gbl );
    JGUIUtil.addComponent(info_JPanel, timeZoneList_JPanel,
        0, ++yInfo, 5, 1, 1, 1, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    
    JGUIUtil.addComponent(timeZoneList_JPanel, new JLabel("Select a time zone to see information about the time zone."),
        0, ++yDateToUnitDate, 5, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(timeZoneList_JPanel, new JLabel("Time zone ID:"),
        0, ++yDateToUnitDate, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    this.timeZones_JComboBox = new SimpleJComboBox(false);
    String [] ids = TimeZone.getAvailableIDs();
    List<String> timeZoneIDs = new ArrayList<String>();
    for ( int i = 0; i < ids.length; i++ ) {
    	timeZoneIDs.add(ids[i]);
    }
    timeZoneIDs = StringUtil.sortStringList(timeZoneIDs);
    this.timeZones_JComboBox.setData(timeZoneIDs);
    this.timeZones_JComboBox.addItemListener(this);
    JGUIUtil.addComponent(timeZoneList_JPanel, this.timeZones_JComboBox,
        1, yDateToUnitDate, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    
    // Vertical separator
    
    JGUIUtil.addComponent(info_JPanel, new JSeparator(SwingConstants.VERTICAL),
            5, yInfo, 1, 1, 1, 1, insetsTLBR, GridBagConstraints.VERTICAL, GridBagConstraints.WEST);
    
    // Add panel on right to list time zone information
    
    JPanel timeZoneInfo_JPanel = new JPanel();
    timeZoneInfo_JPanel.setLayout ( gbl );
    JGUIUtil.addComponent(info_JPanel, timeZoneInfo_JPanel,
        6, yInfo, 5, 1, 1, 1, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);

    int yZone = -1;
    JGUIUtil.addComponent(timeZoneInfo_JPanel, new JLabel("Time zone ID:"),
        0, ++yZone, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    this.timeZoneID_JTextField = new JTextField(20);
    this.timeZoneID_JTextField.setToolTipText("Time zone ID is recognized by the system - may have standard and daylight savings time names (see below).");
    this.timeZoneID_JTextField.setEditable(false);
    this.timeZoneID_JTextField.addKeyListener(this);
    JGUIUtil.addComponent(timeZoneInfo_JPanel, this.timeZoneID_JTextField,
        1, yZone, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    
    JGUIUtil.addComponent(timeZoneInfo_JPanel, new JLabel("Time zone display name:"),
        0, ++yZone, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    this.timeZoneDisplayName_JTextField = new JTextField(35);
    this.timeZoneDisplayName_JTextField.setToolTipText("Time zone display name corresponding to the time zone ID.");
    this.timeZoneDisplayName_JTextField.setEditable(false);
    this.timeZoneDisplayName_JTextField.addKeyListener(this);
    JGUIUtil.addComponent(timeZoneInfo_JPanel, this.timeZoneDisplayName_JTextField,
        1, yZone, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

    JGUIUtil.addComponent(timeZoneInfo_JPanel, new JLabel("Time zone display name (long):"),
        0, ++yZone, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    this.timeZoneDisplayNameLong_JTextField = new JTextField(35);
    this.timeZoneDisplayNameLong_JTextField.setToolTipText("Time zone display name (long, no DST) corresponding to the time zone ID.");
    this.timeZoneDisplayNameLong_JTextField.setEditable(false);
    this.timeZoneDisplayNameLong_JTextField.addKeyListener(this);
    JGUIUtil.addComponent(timeZoneInfo_JPanel, this.timeZoneDisplayNameLong_JTextField,
        1, yZone, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    
    JGUIUtil.addComponent(timeZoneInfo_JPanel, new JLabel("Time zone display name (short):"),
        0, ++yZone, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    this.timeZoneDisplayNameShort_JTextField = new JTextField(35);
    this.timeZoneDisplayNameShort_JTextField.setToolTipText("Time zone display name (short, no DST) corresponding to the time zone ID.");
    this.timeZoneDisplayNameShort_JTextField.setEditable(false);
    this.timeZoneDisplayNameShort_JTextField.addKeyListener(this);
    JGUIUtil.addComponent(timeZoneInfo_JPanel, this.timeZoneDisplayNameShort_JTextField,
        1, yZone, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    
    JGUIUtil.addComponent(timeZoneInfo_JPanel, new JLabel("Time zone display name (long, if in DST):"),
        0, ++yZone, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    this.timeZoneDisplayNameDSTLong_JTextField = new JTextField(35);
    this.timeZoneDisplayNameDSTLong_JTextField.setToolTipText("Time zone display name (long, in DST) corresponding to the time zone ID (even if ID has no DST).");
    this.timeZoneDisplayNameDSTLong_JTextField.setEditable(false);
    this.timeZoneDisplayNameDSTLong_JTextField.addKeyListener(this);
    JGUIUtil.addComponent(timeZoneInfo_JPanel, this.timeZoneDisplayNameDSTLong_JTextField,
        1, yZone, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    
    JGUIUtil.addComponent(timeZoneInfo_JPanel, new JLabel("Time zone display name (short, if in DST):"),
        0, ++yZone, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    this.timeZoneDisplayNameDSTShort_JTextField = new JTextField(35);
    this.timeZoneDisplayNameDSTShort_JTextField.setToolTipText("Time zone display name (short, in DST) corresponding to the time zone ID (even if ID has no DST).");
    this.timeZoneDisplayNameDSTShort_JTextField.setEditable(false);
    this.timeZoneDisplayNameDSTShort_JTextField.addKeyListener(this);
    JGUIUtil.addComponent(timeZoneInfo_JPanel, this.timeZoneDisplayNameDSTShort_JTextField,
        1, yZone, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    
    JGUIUtil.addComponent(timeZoneInfo_JPanel, new JLabel("Raw shift from UTC to get to time zone (ms/s/m/h):"),
        0, ++yZone, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    this.timeZoneRawOffset_JTextField = new JTextField(20);
    this.timeZoneRawOffset_JTextField.setToolTipText("Shift from UTC to get to time zone, in standard time (no DST considered).");
    this.timeZoneRawOffset_JTextField.setEditable(false);
    this.timeZoneRawOffset_JTextField.addKeyListener(this);
    JGUIUtil.addComponent(timeZoneInfo_JPanel, this.timeZoneRawOffset_JTextField,
        1, yZone, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    
    JGUIUtil.addComponent(timeZoneInfo_JPanel, new JLabel("Does time zone observe daylight savings time (DST)?:"),
        0, ++yZone, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    this.timeZoneObservesDST_JTextField = new JTextField(15);
    this.timeZoneObservesDST_JTextField.setToolTipText("Whether the time zone observes daylight savings time.");
    this.timeZoneObservesDST_JTextField.setEditable(false);
    this.timeZoneObservesDST_JTextField.addKeyListener(this);
    JGUIUtil.addComponent(timeZoneInfo_JPanel, this.timeZoneObservesDST_JTextField,
        1, yZone, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    
    JGUIUtil.addComponent(timeZoneInfo_JPanel, new JLabel("Daylight savings shift forward (ms/s/m/h):"),
        0, ++yZone, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    this.timeZoneDSTSavings_JTextField = new JTextField(20);
    this.timeZoneDSTSavings_JTextField.setToolTipText("Time added to standard time to shift to daylight savings time.");
    this.timeZoneDSTSavings_JTextField.setEditable(false);
    this.timeZoneDSTSavings_JTextField.addKeyListener(this);
    JGUIUtil.addComponent(timeZoneInfo_JPanel, this.timeZoneDSTSavings_JTextField,
        1, yZone, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    
    showTimeZoneInfo();

}

/**
 * Respond to ItemEvents
 */
public void itemStateChanged ( ItemEvent event ) {
	if ( (event.getSource() == this.timeZones_JComboBox) && (event.getStateChange() == ItemEvent.SELECTED) ) {
		// Update the time zone information that is displayed
		showTimeZoneInfo();
	}
	else if ( (event.getSource() == this.dateToUnixTimeZoneIDReq_JComboBox) && (event.getStateChange() == ItemEvent.SELECTED) ) {
		// Update the time zone information that is displayed
		calculateUnixTimeFromDate();
	}
}

/**
Respond to KeyEvents.
*/
public void keyPressed ( KeyEvent event )
{
	// Update the UNIX time output (handles incomplete input)
	calculateUnixTimeFromDate();
	calculateDateFromUnixTime();
}

public void keyReleased ( KeyEvent event )
{	// Update the UNIX time output (handles incomplete input)
	calculateUnixTimeFromDate();
	calculateDateFromUnixTime();
}

public void keyTyped ( KeyEvent event )
{	// Update the UNIX time output (handles incomplete input)
	calculateUnixTimeFromDate();
	calculateDateFromUnixTime();
}

/**
Show time zone information.
*/
private void showTimeZoneInfo () {
	// Get the selected time zone
	String id = this.timeZones_JComboBox.getSelected();
	TimeZone tz = TimeZone.getTimeZone(id);
	this.timeZoneID_JTextField.setText(id);
	this.timeZoneDisplayName_JTextField.setText(tz.getDisplayName());
	this.timeZoneDisplayNameLong_JTextField.setText(tz.getDisplayName(false,TimeZone.LONG));
	this.timeZoneDisplayNameShort_JTextField.setText(tz.getDisplayName(false,TimeZone.SHORT));
	this.timeZoneDisplayNameDSTLong_JTextField.setText(tz.getDisplayName(true,TimeZone.LONG));
	this.timeZoneDisplayNameDSTShort_JTextField.setText(tz.getDisplayName(true,TimeZone.SHORT));
	String hours = "";
	String shift = "";
	if ( ((tz.getDSTSavings()/1000) % 3600) == 0 ) {
		// Show integer if evenly divides
		hours = "" + (tz.getDSTSavings()/1000/3600);
	}
	else {
		hours = "" + StringUtil.formatString(((double)tz.getDSTSavings()/1000/3600),"%.2f");
	}
	shift = "" + tz.getDSTSavings() + " / " + (tz.getDSTSavings()/1000) + " / " + (tz.getDSTSavings()/1000/60) + " / " + hours;
	this.timeZoneDSTSavings_JTextField.setText(""+shift);
	hours = "";
	String rawOffset = "";
	if ( ((tz.getRawOffset()/1000) % 3600) == 0 ) {
		// Show integer if evenly divides
		hours = "" + (tz.getRawOffset()/1000/3600);
	}
	else {
		hours = "" + StringUtil.formatString(((double)tz.getRawOffset()/1000/3600),"%.2f");
	}
    rawOffset = "" + tz.getRawOffset() + " / " + (tz.getRawOffset()/1000) + " / " + (tz.getRawOffset()/1000/60) + " / " + hours;
	this.timeZoneRawOffset_JTextField.setText(rawOffset);
	this.timeZoneObservesDST_JTextField.setText(""+tz.observesDaylightTime());
}

/**
Responds to WindowEvents.
@param event WindowEvent object
*/
public void windowClosing( WindowEvent event )
{	closeDialog ();
}

public void windowActivated( WindowEvent evt ){;}
public void windowClosed( WindowEvent evt ){;}
public void windowDeactivated( WindowEvent evt ){;}
public void windowDeiconified( WindowEvent evt ){;}
public void windowIconified( WindowEvent evt ){;}
public void windowOpened( WindowEvent evt ){;}

}
