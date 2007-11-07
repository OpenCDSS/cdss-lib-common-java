/*****************************************************************************
Validators.java - 2007-03-26
******************************************************************************
Revisions
2007-03-21	Ian Schneider, RTi		Initial Version.
2007-03-26	Kurt Tometich, RTi		Added new methods and inner classes
								for RangeValidation, BlankStringValidation
								and StringLengthValidator.
*****************************************************************************/
package RTi.Util.IO;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
This class returns a Validator based on the method called.  Each
validator implements the Validator interface.  Once a validator is
returned only the validate() method needs to be called.  A Status
object is returned from running a Validator.  If the object value
is validated then a Status of Status.OK will be returned; otherwise a
Status of the given level will be returned with a status message
describing the reason the value was not valid.  If the level is
not specified then it defaults to Status.ERROR which is the error level.
See the Status class for more info on status levels.
 */
public class Validators {
    
    public Validators() {}
    
    /** simple validators, which are inner classes **/
    // each method can be called w/ or w/o a level.
    // The level is used as the log level if the validation fails.
    // If a level is not specified it defaults to error.
    public static Validator and(Validator[] rules) {
        return and(rules,Status.ERROR);
    }
    public static Validator and(Validator[] rules,int level) {
        return new AndValidator(rules,level);
    }
    public static Validator or(Validator[] rules) {
        return or(rules,Status.ERROR);
    }
    public static Validator or(Validator[] rules,int level) {
        return new OrValidator(rules,level);
    }
    public static Validator lessThan(double value) {
        return lessThan(value,Status.ERROR);
    }
    public static Validator lessThan(double value,int level) {
        return new LogicalValidator(LogicalValidator.LT,value,level);
    }
    public static Validator greaterThan(double value) {
        return greaterThan(value,Status.ERROR);
    }
    public static Validator greaterThan(double value,int level) {
        return new LogicalValidator(LogicalValidator.GT,value,level);
    }
    public static Validator isDate(String format) {
        return isDateValidator(format, Status.ERROR);
    }
    public static Validator isEquals( Object value ) {
        return isEqualsValidator( value, Status.ERROR);
    }
    public static Validator not(double value) {
        return not(value,Status.ERROR);
    }
    public static Validator not(double value,int level) {
        return new LogicalValidator(LogicalValidator.NOT,value,level);
    }
    
    /** More complex validators that build on the simple ones **/
    
    // Regular Expression Validator
    public static Validator regexValidator( String regex ) {
    	return new RegexValidator( Status.ERROR, regex );
    }
    public static Validator regexValidator( String regex, int level ) {
    	return new RegexValidator( level, regex );
    }
    // RangeValidator
    public static Validator rangeValidator( double min, double max ) {
    	return new RangeValidator( Status.ERROR, min, max );
    }
    public static Validator rangeValidator( double min, double max,
    int level ) {
    	return new RangeValidator( level, min, max );
    }
    // String Length Validator
    public static Validator stringLengthValidator( int maxLen)
    {
    	return new StringLengthValidator( maxLen, Status.ERROR );
    }
    public static Validator stringLengthValidator( int maxLen, int level )
    {
    	return new StringLengthValidator( maxLen, level );
    }
    
    // Blank String Validator
    public static Validator notBlankValidator()
    {
    	return new NotBlankValidator( Status.ERROR );
    }
    public static Validator notBlankValidator( int level )
    {
    	return new NotBlankValidator( level );
    }
    
 
    /**
    Checks the given date to see if it is valid
    based on a given format.
    @param format String format to check for.
    @param level Status level to log the Status message as if this
    check fails.
    @return Returns a Validator object.
    */
    // TODO KAT 2007-04-09
    // Need to update this method to check valid dates from DateTime
    // Might need to add a new data validator to validate specific dates
    // created from the DateTime class.
    public static Validator isDateValidator(final String format,final int level) {
        return new Validator() {
            ParsePosition p = new ParsePosition(0);
            DateFormat f = new SimpleDateFormat(format);
           /**
           Validates the given object value for a date string format.
           @param value The object value to check.
           @return Status object. 
           */
            public Status validate(Object value) {
                p.setIndex(0);
                Status status = Status.OKAY;
                if (value == null) {
                    status = Status.status("Date is not set",level);
                } else {
                    try {
                        f.parse(value.toString());
                    } catch (ParseException pe) {
                        status = Status.status("Not a valid date",level);
                    }
                }
                return status;
            }
            /**
            Returns the validator rules.
            @return List of validator rules.
             */
            public String toString() 
            {
            	return "Value must match date format " + format;
            }
        };
    }
    
    /**
     Checks whether an object equals another.
     @param object Object value to check.
     @param level Status level to log the Status message as if this
     check fails.
     @return Returns a validator object.
     */
    public static Validator isEqualsValidator(final Object object,
    final int level) {
    	final Object value1;
    	if (object == null) {
            throw new NullPointerException("object");
        }
    	value1 = object;
        return new Validator() {
        	/**
        	Validates whether one object is equal to another.
        	@param value Object value to check.
        	@return Status object.
        	*/
            public Status validate(Object value) {
                Status status = Status.OKAY;
                if (value == null) {
                    status = Status.status("Value is not set",level);
                } else {
                    if (! object.equals(value)) {
                        status = Status.status(value.toString() +
                        	" is not equal to " + object.toString(),level);
                    }
                }
                return status;
            }
            /**
            Returns the validator rules.
            @return List of validator rules.
             */
            public String toString()
            {
            	String str = "Value must be equal to " + 
            	value1.toString() + "\n";
            	return str;
            }
        };
    }
    
    // Inner classes follow
    
    /**
    Simple "And" validator that checks whether all values pass
    the specified rules.
    */
    private static final class AndValidator implements Validator {
        final Validator[] rules;
        final int level;
        AndValidator(Validator[] rules,int level) {
            this.rules = rules;
            this.level = level;
        }
        /**
        Validates whether the object value is valid using the
        rules specified. 
        @param value The object value to check.
        @return Status object.
        */
        public Status validate(Object value) {
            ArrayList results = new ArrayList();
            Status status = Status.OKAY;
            for (int i = 0; i < rules.length; i++) {
                status = rules[i].validate(value);
                if (status != Status.OKAY) {
                    results.add(status);
                }
            }
            if (results.size() > 0) {
                StringBuffer sb = new StringBuffer(
                	"The following rules were not valid:\n");
                for (int i = 0; i < results.size(); i++) {
                    sb.append(results.get(i).toString());
                    if (i + 1 < results.size()) {
                        sb.append('\n');
                    }
                }
                status = Status.status(sb.toString(),level);
            }
            return status;
        }
        /**
        Overriden method to return the validator and its rules
        @return String with validator type and associated rules.
         */
        public String toString()
        {
        	String str = "All of the following rules must be valid:\n";
        	for ( int i = 0; i < rules.length; i++ ) {
        		str = str + " - " + rules[i];
        	}
        	return str;
        }
    }
    
    /**
    Simple "Or" validator that checks whether any of the values
    pass the rules specified.
    */
    private static final class OrValidator implements Validator {
        final Validator[] rules;
        final int level;
        /**
        Initializes the OrValidator object.
        @param rules The list of rules to use when checking for
        validity.
        @param Level The Status level to use if the value is not valid.
         */
        OrValidator(Validator[] rules,int level) {
            this.rules = rules;
            this.level = level;
        }
        /**
        Validates whether the rules specified are valid.
        @param value The object value to check.
        @return Status object.
         */
        public Status validate(Object value) {
            Status[] results = new Status[rules.length];
            boolean ok = false;
            Status status = Status.OKAY;
            for (int i = 0; i < rules.length && !ok; i++) {
                status = rules[i].validate(value);
                ok = status == Status.OKAY;
                results[i] = status;
            }
            if (! ok) {
                StringBuffer sb = new StringBuffer("None of the rules were valid:\n");
                for (int i = 0; i < results.length; i++) {
                    sb.append(results[i].toString());
                    if (i + 1 < results.length) {
                        sb.append('\n');
                    }
                }
                status = Status.status(sb.toString(),level);
            }
            return status;
        }
        /**
        Overriden method to return the validator and its rules
        @return String with validator type and associated rules.
         */
        public String toString()
        {
        	String str = "One of the following rules must be valid:\n";
        	for ( int i = 0; i < rules.length; i++ ) {
        		str = str + " - " + rules[i];
        	}
        	return str;
        }
    }
    
    /**
    Validates simple logical checks.  This is used by several other
    validators as a base for checking low level logical operations.
     */
    private static final class LogicalValidator implements Validator {
        final int type;
        final int level;
        final double value;
        static final int LT = 1;
        static final int GT = LT << 1;
        static final int NOT = GT << 1;
        /**
        Initializes the Logical object.
        @param type The type of the logical check.  LT for less than, GT for
        greater than and NOT for negation.
        @param value The value to validate. 
        @param Level The Status level to use if the value is not valid.
         */
        LogicalValidator( int type, double value, int level ) {
            this.type = type;
            this.level = level;
            this.value = value;
        }
        /**
        Validates whether the given object value passes the specified
        logical rules.
         */
        public Status validate(Object value) {
            Status status = Status.OKAY;
            Number number = null;
            if (value instanceof Number) {
                number = (Number) value;
            } else if (value != null) {
                String svalue = value.toString();
                try {
                    number = Double.valueOf(svalue);
                } catch (NumberFormatException nfe) {
                    status = Status.status("Could not use " + svalue + 
                    	" as a number",level);
                }
            } else {
                status = Status.status("Value is not set",level);
            }
            if (number != null) {
                double nval = number.doubleValue();
                String message = null;
                switch (type) {
                    case LT: if (nval >= this.value) message =  
                    	"Value must be less than " + 
                    	this.value + " but was " + nval; break;
                    case GT: if (nval <= this.value) message = 
                    	"Value must be greater than " + this.value +
                    	" but was " + nval; break;
                    case NOT: if (nval >= this.value) message = nval + 
                    " is not " + this.value; break;
                }
                if (message != null) {
                    status = Status.status(message,level);
                }
            }
            return status;
        }
        public String toString()
        {
        	switch (type) {
            case LT: 	return "Value must be less than " + this.value;
            case GT:  	return "Value must be greater than " + this.value;
            case NOT:   return "Value must not equal " + this.value; 
            default:	return "Invalid type: " + type + "." +
            " Valid types are LT(" + LT + "), GT(" + GT + ") and NOT(" +
            NOT + ").";
        	}
        }
    }
    
    /**
    Validates whether the given value matches a regular expression.
    */
   private static final class RegexValidator implements Validator
   {
   	final int level;
   	String regex;
   	
   	/**
   	Initializes the RegexValidator object.
   	@param Level The Status level to use if the value is not valid.
   	@param Regex The regular expression to match the value with.
   	 */
   	RegexValidator( int Level, String Regex )
   	{
   		this.level = Level;
   		this.regex = Regex;
   	}
   	
   	/**
   	Validates the given value is matched by the given regular expression.
   	@return Status object.
   	 */
   	public Status validate( Object value )
   	{
   		Status status = Status.OKAY;
   		if ( value == null ) {
   			status = Status.status( "Value is not set", level );
   		}
   		else if ( regex == null ) {
   			status = Status.status ( "The regular expression to check" +
   				" was not set", level );
   		}
   		else {
	   		if ( value instanceof String ) {
	   			Pattern pattern = Pattern.compile( regex );
	   			CharSequence inputStr = value.toString();
	   			Matcher matcher = pattern.matcher(inputStr);
	   			boolean matchFound = matcher.find();
		    	if ( !matchFound ) {
		        	status = Status.status( "String " + inputStr + 
		        		" must match regular expression: " + 
		        		regex, level);
		        }
	   		}
		   	else {
		   		status = Status.status( "Value must be a String", level);
		   	}
	   	}
   		
   		return status;
   	}
    /**
    Overriden method to return the validator and its rules
    @return String with validator type and associated rules.
     */
    public String toString()
    {
    	String str = "Value must match regular expression: " + regex + "\n";
    	return str;
    }
   }
    
    /**
     Validates whether the given value is greater than a minimum and
     less than a maximum value.  If the value is valid then a status of OKAY
     is returned; otherwise, an message is appended to the Status.
     */
    private static final class RangeValidator implements Validator
    {
    	final int level;
    	double min;
    	double max;
    	
    	/**
    	Initializes the RangeValidator object.
    	@param Level The Status level to use if the value is not valid.
    	@param Min Valid value must be greater than this number.
    	@param Max Valid value must be less than this number.
    	 */
    	RangeValidator( int Level, double Min, double Max )
    	{
    		this.level = Level;
    		this.min = Min;
    		this.max = Max;
    	}
    	
    	/**
    	Validates the given value is in the range of values.
    	@return Status object.
    	 */
    	public Status validate( Object value )
    	{
    		Status status = Status.OKAY;
    		if ( value == null ) {
    			status = Status.status( "Value is not set", level );
    		}
    		// check if greater than the minimum value
    		// AND if less than the maximum value
    		status = Validators.and( new Validator [] {
    			Validators.greaterThan( this.min, this.level ),
    			Validators.lessThan( this.max, this.level )
				}).validate(value);
    		return status;
    	}
    	 /**
        Overriden method to return the validator and its rules
        @return String with validator type and associated rules.
         */
        public String toString()
        {
        	String str = "Value must be greater than " + min + 
        		" and less than " + max + "\n";
        	return str;
        }
    }
   
    /**
    Validates whether a given object is a blank string.  Useful
    for checking ID's or Strings that must not be blank.
     */
    private static final class NotBlankValidator implements Validator
    {
    	final int level;
    	/**
    	Initializes the BlankValidator object.
    	@param Level The Status level to use if the value is not valid.
    	 */
    	NotBlankValidator( int Level )
    	{
    		level = Level;
    	}
    	
    	/**
    	Validates whether the given object value is blank.
    	@return Status object.
    	 */
    	public  Status validate ( Object value )
    	{
    		Status status = Status.OKAY;
    		if ( value == null ) {
    			status = Status.status( "Value is not set", level );
    		}
    		else if ( value instanceof String && 
    				  value.toString().length() == 0 ) {
    			status = Status.status("Value cannot be blank", level);
    		}
    		return status;
    	}
    	 /**
        Overriden method to return the validator and its rules
        @return String with validator type and associated rules.
         */
        public String toString()
        {
        	String str = "Value cannot be blank\n";
        	return str;
        }
    }
    
    /**
    Validates the max length of a specified String.
     */
    private static final class StringLengthValidator implements Validator
    {
    	final int level;
    	final int maxLen;
    	/**
    	Initializes the StringLengthValidator object.
    	@param MaxLen The mamixum number of characters the String must
    	be in order to be considered valid.
    	@param Level The Status level to use if the value is not valid.
    	*/
    	StringLengthValidator( int MaxLen, int Level )
    	{
    		level = Level;
    		maxLen = MaxLen;
    	}
    	
    	/**
    	Validates whether the given String objects length is less than
 		or equal to the maximum length.
    	@return Status object.
    	 */
    	public  Status validate ( Object value )
    	{
    		Status status = Status.OKAY;
    		if ( value == null ) {
    			status = Status.status( "Value is not set", level );
    		}
    		else if ( value instanceof String && 
    				  value.toString().length() > maxLen ) {
    			status = Status.status("Value length must not exceed " +
    					maxLen, level);
    		}
    		return status;
    	}
    	 /**
        Overriden method to return the validator and its rules
        @return String with validator type and associated rules.
         */
        public String toString()
        {
        	String str = "Value length must not exceed " + maxLen + "\n";
        	return str;
        }
    }
    
    
}
