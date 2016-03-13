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
import RTi.Util.String.StringUtil;

/**
Dialog to provide useful date/time tools to help understand date/time data.
*/
public class DateTimeToolsJDialog extends JDialog
implements ActionListener, ItemListener, KeyListener, WindowListener
{

private JTextField dateToUnixYear_JTextField = null;
private JTextField dateToUnixMonth_JTextField = null;
private JTextField dateToUnixDay_JTextField = null;
private JTextField dateToUnixHour_JTextField = null;
private JTextField dateToUnixMinute_JTextField = null;
private JTextField dateToUnixSecond_JTextField = null;
private SimpleJComboBox dateToUnixTimeZoneIDReq_JComboBox = null; // Requested (default = computer local)
private JTextField dateToUnixTimeZoneIDUsed_JTextField = null; // What was actually used
private JTextField dateToUnixTimeZoneIDUsedDisplayNameLong_JTextField = null;
private JTextField dateToUnixTimeZoneIDUsedDisplayNameShort_JTextField = null;
private JTextField dateToUnixTimeZoneIDUsedIsDST_JTextField = null;

private JTextField dateToUnixUnixMs_JTextField = null;
private JTextField dateToUnixUnixSeconds_JTextField = null;
private JTextField dateToUnixUnixMsComputed_JTextField = null;

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
		String timeZone = this.dateToUnixTimeZoneIDReq_JComboBox.getSelected();
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
					timeZoneUsed = tz.getDisplayName();
				}
				boolean isDST = tz.inDaylightTime(d);
				timeZoneUsedDisplayNameLong = tz.getDisplayName(isDST,TimeZone.LONG);
				timeZoneUsedDisplayNameShort = tz.getDisplayName(isDST,TimeZone.SHORT);
				timeZoneUsedIsDST = "" + isDST;
				unixTimeComputedMsString = "" + TimeUtil.toUnixTime(dt);
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
		timeZoneUsed = "Error";
		timeZoneUsed = "Error";
		timeZoneUsedDisplayNameLong = "Error";
		timeZoneUsedDisplayNameShort = "Error";
		unixTimeSecondsString = "Error";
		unixTimeMsString = "Error";
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

    int yUnix = -1;
    JGUIUtil.addComponent(unix_JPanel,
        new JLabel("UNIX time is the number of seconds since midnight, January 1, 1970 (see: https://en.wikipedia.org/wiki/Unix_time)."),
        0, ++yUnix, 11, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(unix_JPanel,
        new JLabel("Each day is 86400 seconds.  Computer programs often use this representation for date/time conversions and time math."),
        0, ++yUnix, 11, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(unix_JPanel,
        new JLabel("UNIX time does not reflect leap seconds - it is calculated based on the number of days and seconds in the day."),
        0, ++yUnix, 11, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(unix_JPanel,
        new JSeparator(SwingConstants.HORIZONTAL),
        0, ++yUnix, 11, 1, 0, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    
    // Add panel on left to go from date to UNIX time

    int yDateToUnitDate = -1;
    JPanel dateToUnix_JPanel = new JPanel();
    dateToUnix_JPanel.setLayout ( gbl );
    JGUIUtil.addComponent(unix_JPanel, dateToUnix_JPanel,
        0, ++yUnix, 5, 1, 1, 1, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    
    JGUIUtil.addComponent(dateToUnix_JPanel, new JLabel("Enter a date/time to see UNIX time."),
        0, ++yDateToUnitDate, 5, 1, 1, 1, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(dateToUnix_JPanel, new JLabel("Year (YYYY, 1970+):"),
        0, ++yDateToUnitDate, 1, 1, 1, 1, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    this.dateToUnixYear_JTextField = new JTextField(5);
    this.dateToUnixYear_JTextField.addKeyListener(this);
    JGUIUtil.addComponent(dateToUnix_JPanel, this.dateToUnixYear_JTextField,
        1, yDateToUnitDate, 4, 1, 1, 1, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(dateToUnix_JPanel, new JLabel("Month (1-12):"),
        0, ++yDateToUnitDate, 1, 1, 1, 1, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    this.dateToUnixMonth_JTextField = new JTextField(5);
    this.dateToUnixMonth_JTextField.addKeyListener(this);
    JGUIUtil.addComponent(dateToUnix_JPanel, this.dateToUnixMonth_JTextField,
        1, yDateToUnitDate, 4, 1, 1, 1, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(dateToUnix_JPanel, new JLabel("Day (1-31):"),
        0, ++yDateToUnitDate, 1, 1, 1, 1, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    this.dateToUnixDay_JTextField = new JTextField(5);
    this.dateToUnixDay_JTextField.addKeyListener(this);
    JGUIUtil.addComponent(dateToUnix_JPanel, this.dateToUnixDay_JTextField,
        1, yDateToUnitDate, 4, 1, 1, 1, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(dateToUnix_JPanel, new JLabel("Hour (0-23):"),
        0, ++yDateToUnitDate, 1, 1, 1, 1, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    this.dateToUnixHour_JTextField = new JTextField(5);
    this.dateToUnixHour_JTextField.addKeyListener(this);
    JGUIUtil.addComponent(dateToUnix_JPanel, this.dateToUnixHour_JTextField,
        1, yDateToUnitDate, 4, 1, 1, 1, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(dateToUnix_JPanel, new JLabel("Minute (0-59):"),
        0, ++yDateToUnitDate, 1, 1, 1, 1, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    this.dateToUnixMinute_JTextField = new JTextField(5);
    this.dateToUnixMinute_JTextField.setToolTipText("Specify blank to use default time zone from computer.");
    this.dateToUnixMinute_JTextField.addKeyListener(this);
    JGUIUtil.addComponent(dateToUnix_JPanel, this.dateToUnixMinute_JTextField,
        1, yDateToUnitDate, 4, 1, 1, 1, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(dateToUnix_JPanel, new JLabel("Second (0-59):"),
        0, ++yDateToUnitDate, 1, 1, 1, 1, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    this.dateToUnixSecond_JTextField = new JTextField(5);
    this.dateToUnixSecond_JTextField.addKeyListener(this);
    JGUIUtil.addComponent(dateToUnix_JPanel, this.dateToUnixSecond_JTextField,
        1, yDateToUnitDate, 4, 1, 1, 1, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(dateToUnix_JPanel, new JLabel("Time zone (requested):"),
        0, ++yDateToUnitDate, 1, 1, 1, 1, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    this.dateToUnixTimeZoneIDReq_JComboBox = new SimpleJComboBox(false);
    String [] ids = TimeZone.getAvailableIDs();
    List<String> timeZoneIDs = new ArrayList<String>();
    timeZoneIDs.add(""); // Default
    for ( int i = 0; i < ids.length; i++ ) {
    	timeZoneIDs.add(ids[i]);
    }
    timeZoneIDs = StringUtil.sortStringList(timeZoneIDs);
    this.dateToUnixTimeZoneIDReq_JComboBox.setData(timeZoneIDs);
    this.dateToUnixTimeZoneIDReq_JComboBox.addItemListener(this);
    JGUIUtil.addComponent(dateToUnix_JPanel, this.dateToUnixTimeZoneIDReq_JComboBox,
        1, yDateToUnitDate, 4, 1, 1, 1, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(dateToUnix_JPanel, new JSeparator(SwingConstants.HORIZONTAL),
        0, ++yDateToUnitDate, 5, 1, 1, 1, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    
    JGUIUtil.addComponent(dateToUnix_JPanel, new JLabel("Time zone (used):"),
        0, ++yDateToUnitDate, 1, 1, 1, 1, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    this.dateToUnixTimeZoneIDUsed_JTextField = new JTextField(25);
    dateToUnixTimeZoneIDUsed_JTextField.setEnabled(false);
    this.dateToUnixTimeZoneIDUsed_JTextField.addKeyListener(this);
    JGUIUtil.addComponent(dateToUnix_JPanel, this.dateToUnixTimeZoneIDUsed_JTextField,
        1, yDateToUnitDate, 4, 1, 1, 1, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    
    JGUIUtil.addComponent(dateToUnix_JPanel, new JLabel("Time zone (used) display name (long):"),
        0, ++yDateToUnitDate, 1, 1, 1, 1, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    this.dateToUnixTimeZoneIDUsedDisplayNameLong_JTextField = new JTextField(25);
    dateToUnixTimeZoneIDUsedDisplayNameLong_JTextField.setEnabled(false);
    this.dateToUnixTimeZoneIDUsedDisplayNameLong_JTextField.addKeyListener(this);
    JGUIUtil.addComponent(dateToUnix_JPanel, this.dateToUnixTimeZoneIDUsedDisplayNameLong_JTextField,
        1, yDateToUnitDate, 4, 1, 1, 1, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    
    JGUIUtil.addComponent(dateToUnix_JPanel, new JLabel("Time zone (used) display name (short):"),
        0, ++yDateToUnitDate, 1, 1, 1, 1, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    this.dateToUnixTimeZoneIDUsedDisplayNameShort_JTextField = new JTextField(25);
    dateToUnixTimeZoneIDUsedDisplayNameShort_JTextField.setEnabled(false);
    this.dateToUnixTimeZoneIDUsedDisplayNameShort_JTextField.addKeyListener(this);
    JGUIUtil.addComponent(dateToUnix_JPanel, this.dateToUnixTimeZoneIDUsedDisplayNameShort_JTextField,
        1, yDateToUnitDate, 4, 1, 1, 1, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    
    JGUIUtil.addComponent(dateToUnix_JPanel, new JLabel("Time zone is daylight savings time?:"),
        0, ++yDateToUnitDate, 1, 1, 1, 1, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    this.dateToUnixTimeZoneIDUsedIsDST_JTextField = new JTextField(25);
    dateToUnixTimeZoneIDUsedIsDST_JTextField.setEnabled(false);
    this.dateToUnixTimeZoneIDUsedIsDST_JTextField.addKeyListener(this);
    JGUIUtil.addComponent(dateToUnix_JPanel, this.dateToUnixTimeZoneIDUsedIsDST_JTextField,
        1, yDateToUnitDate, 4, 1, 1, 1, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    
    JGUIUtil.addComponent(dateToUnix_JPanel, new JLabel("UNIX time, from system (ms):"),
    	0, ++yDateToUnitDate, 1, 1, 1, 1, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	this.dateToUnixUnixMs_JTextField = new JTextField(15);
	this.dateToUnixUnixMs_JTextField.setEnabled(false);
	JGUIUtil.addComponent(dateToUnix_JPanel, this.dateToUnixUnixMs_JTextField,
	    1, yDateToUnitDate, 4, 1, 1, 1, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

    JGUIUtil.addComponent(dateToUnix_JPanel, new JLabel("UNIX time, from system (seconds):"),
    	0, ++yDateToUnitDate, 1, 1, 1, 1, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	this.dateToUnixUnixSeconds_JTextField = new JTextField(15);
	this.dateToUnixUnixSeconds_JTextField.setEnabled(false);
	JGUIUtil.addComponent(dateToUnix_JPanel, this.dateToUnixUnixSeconds_JTextField,
	    1, yDateToUnitDate, 4, 1, 1, 1, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	
    JGUIUtil.addComponent(dateToUnix_JPanel, new JLabel("UNIX time using GMT, computed (ms):"),
    	0, ++yDateToUnitDate, 1, 1, 1, 1, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	this.dateToUnixUnixMsComputed_JTextField = new JTextField(15);
	this.dateToUnixUnixMsComputed_JTextField.setToolTipText("UNIX time computed as days since Jan 1, 1970 00:00:00 * 86400 + seconds in day, ignoring time zone.");
	this.dateToUnixUnixMsComputed_JTextField.setEnabled(false);
	JGUIUtil.addComponent(dateToUnix_JPanel, this.dateToUnixUnixMsComputed_JTextField,
	    1, yDateToUnitDate, 4, 1, 1, 1, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	
	// Cause output to update
	calculateUnixTimeFromDate();
    
    // Vertical separator
    
    JGUIUtil.addComponent(unix_JPanel, new JSeparator(SwingConstants.VERTICAL),
            5, yUnix, 1, 1, 1, 1, insetsTLBR, GridBagConstraints.VERTICAL, GridBagConstraints.WEST);
    
    // Add panel on left to go from UNIX time to date
    
    JPanel unixToDate_JPanel = new JPanel();
    unixToDate_JPanel.setLayout ( gbl );
    JGUIUtil.addComponent(unix_JPanel, unixToDate_JPanel,
        6, yUnix, 5, 1, 1, 1, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);

    initializeTimeInfoPanel(main_JTabbedPane);
   
	// South Panel: North
	JPanel button_JPanel = new JPanel();
	button_JPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        JGUIUtil.addComponent(main_JPanel, button_JPanel, 
		0, 1, 1, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER);

	__close_JButton = new SimpleJButton("Close", this);
	button_JPanel.add ( __close_JButton );

	setTitle ( JGUIUtil.getAppNameForWindows() + " - Date/time Converter" );
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
}

public void keyReleased ( KeyEvent event )
{	// Update the UNIX time output (handles incomplete input)
	calculateUnixTimeFromDate();
}

public void keyTyped ( KeyEvent event )
{	// Update the UNIX time output (handles incomplete input)
	calculateUnixTimeFromDate();
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
