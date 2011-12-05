package RTi.TS;

import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import RTi.Util.Math.NumberOfEquationsType;
import RTi.Util.Message.Message;
import RTi.Util.Table.DataTable;
import RTi.Util.Table.TableField;
import RTi.Util.Table.TableRecord;

/**
Helper class to help with FillRegression and FillMOVE2 commands.
*/
public class TSUtil_FillRegression
{
    
/**
Constructor.
TODO SAM 2011-12-04 currently this is a very simple class - need to migrate the TSTUtil regression code to here.
*/
public void TSTool_FillRegression ()
{
    
}

/**
Save the statistics from a regression analysis to a table object.
@param ts dependent time series
@param regressionResults results of the regression analysis
@param table the table to save the results
@param tableTSIDColumnName the name of the table column containing the TSID/alias to match
@param tableTSIDFormat the format string for the time series - to allow matching the contents of the
tableTSIDColumnName
@param numberOfEquations the number of equations in the analysis
*/
public void saveStatisticsToTable ( TS ts, TSRegression regressionResults, DataTable table,
    String tableTSIDColumnName, String tableTSIDFormat, NumberOfEquationsType numberOfEquations )
throws Exception
{   String routine = getClass().getName() + ".saveStatisticsToTable";
    // Verify that the TSID table columns are available for dependent and independent time series
    String tableTSIDColumnNameIndependent = tableTSIDColumnName + "_Independent";
    int tableTSIDColumnNumber = -1;
    int tableTSIDColumnNumberIndependent = -1;
    // If the column name does not exist, add it to the table
    try {
        tableTSIDColumnNumber = table.getFieldIndex(tableTSIDColumnName);
    }
    catch ( Exception e2 ) {
        // Automatically add to the table, initialize with null (not nonValue)
        table.addField(new TableField(TableField.DATA_TYPE_STRING,tableTSIDColumnName,-1,-1), null );
        // Get the corresponding column number for row-edits below
        tableTSIDColumnNumber = table.getFieldIndex(tableTSIDColumnName);
    }
    try {
        tableTSIDColumnNumberIndependent = table.getFieldIndex(tableTSIDColumnNameIndependent);
    }
    catch ( Exception e2 ) {
        // Automatically add to the table, initialize with null (not nonValue)
        table.addField(new TableField(TableField.DATA_TYPE_STRING,tableTSIDColumnNameIndependent,-1,-1), null );
        // Get the corresponding column number for row-edits below
        tableTSIDColumnNumberIndependent = table.getFieldIndex(tableTSIDColumnNameIndependent);
    }
    // Loop through the statistics, creating table column names if necessary
    // Do this first so that all columns are fully defined.  Then process the row values below.
    int numEquations = 1;
    if ( numberOfEquations == NumberOfEquationsType.MONTHLY_EQUATIONS ) {
        numEquations = 12;
    }
    // List in a reasonable order - see the command documentation for more
    // X=independent
    // Y=dependent
    //String [] statistics = { };
    String [] statistics = {
        "N1", "MeanX1", "SX1", "N2", "MeanX2", "SX2",
        "MeanY1", "SY1", "NY", "MeanY", "SY", "a", "b", "R", "R2", "MeanY1est", "SY1est",
        "RMSE", "SEE", "SEP", "SESlope", "TestScore", "TestQuantile", "TestRelated",
        // Include for comparison with mixed station analysis
        "NYfilled", "MeanYfilled", "SYfilled", "Skew"
        };
    // The following comments parallel the statistics names are used to create comments in the table header
    String [] mainComments = {
        "",
        "The following statistics are computed to determine and evaluate the the regression relationships.",
        "The regression type performed was:  " + regressionResults.getAnalysisMethod(),
        "X indicates the independent time series and Y indicates the dependent time series.",
        "Some statistics are ignored for some regression approaches, but are provided for comparison.",
        ""
    };
    String [] mainCommentsMonthly = {
            "",
            "Monthly statistics (for case where NumberOfEquations=MonthlyEquatations) will have a _M subscript, " +
            "where M is the month (1=January, 12=December).",
            ""
        };
    String [] statisticComments = {
        "Count of non-missing data points overlapping in the dependent and independent time series", // "N1"
        "Mean of the independent N1 values", // MeanX1
        "Standard deviation of the independent N1 values", // SX1
        "Count of the non-missing data points in the independent time series outside of N1", // N2
        "Mean of the independent N2 values", // MeanX2
        "Standard deviation of the independent N2 values", // SX2
        "Mean of the dependent N1 values", // MeanY1
        "Standard deviation of the dependent N1 values", // SY1
        "Count of the non-missing dependent values", // NY
        "Mean of the NY values", // MeanY
        "Standard deviation of the NY values", // SY
        "The intercept for the relationship equation", // a
        "The slope of the relationship equation", // b
        "The correlation coefficient for N1 values", // R
        "R-squared, coefficient of determination for N1 values", // R2
        "Mean of N1 values computed from the relationship (estimate dependent values where previously known)", // MeanY1est
        "Standard deviation of N1 values computed from the relationship (estimate dependent values where previously known", // SY1est
        "Root mean squared error for N1 values, computed from regression relationship estimated values", // RMSE
        "Standard error of estimate for N1 values, computed from regression relationship estimated values", // SEE
        "Standard error of prediction for N1 values, computed from regression relationship estimated values", // SEP
        "Standard error (SE) of the slope (b) for N1 values, computed from regression relationship estimated values", // SESlope
        "b/SE", // TestScore
        "From the Student's T-test, function of confidence interval and degrees of freedom, DF (N1 - 2)", // TestQuantile
        "Yes if TestScore < TestQuantile, false if otherwise.",
        // Put these in for comparison with mixed station analysis
        "NYfilled",
        "MeanYfilled",
        "SYfilled",
        "Skew"
        };
    // Add comments to the table header
    table.addToComments(Arrays.asList(mainComments));
    if ( numberOfEquations == NumberOfEquationsType.MONTHLY_EQUATIONS ) {
        table.addToComments(Arrays.asList(mainCommentsMonthly));
    }
    int i = 0;
    for ( String comment: statisticComments ) {
        table.addToComments(statistics[i++] + " - " + comment);
    }
    table.addToComments ( "" );
    int countStatisticTotal = statistics.length*numEquations; // The total number of statistics columns to add
    String [] statisticColumnNames = new String[countStatisticTotal]; // names in table
    int [] statisticFieldType = new int[countStatisticTotal]; // value types
    // Arrays for the statistics.  Using multiple arrays will result in some statistic
    // values being null; however this is easier than dealing with casts later in the code
    Double [] statisticValueDouble = new Double[countStatisticTotal];
    Integer [] statisticValueInteger = new Integer[countStatisticTotal];
    //String [] statisticValueString = new String[countStatisticTotal];
    // The count of statistics added (0-index), necessary because when dealing with monthly statistics
    // the 12 months are flattened into a linear array matching column headings
    int countStatistic = -1;
    for ( int iEquation = 1; iEquation <= numEquations; iEquation++ ) {
        for ( int iStatistic = 0; iStatistic < statistics.length; iStatistic++ ) {
            // Set statistics to null (one will be set below).
            ++countStatistic;
            statisticValueDouble[countStatistic] = null;
            statisticValueInteger[countStatistic] = null;
            // Column name for the statistic (list alphabetically)...
            if ( numEquations == 1 ) {
                statisticColumnNames[countStatistic] = statistics[iStatistic];
            }
            else {
                statisticColumnNames[countStatistic] = statistics[iStatistic] + "_" + iEquation;
            }
            if ( statistics[iStatistic].equals("a") ) {
                statisticFieldType[countStatistic] = TableField.DATA_TYPE_DOUBLE;
                try {
                    if ( numEquations == 1 ) {
                        statisticValueDouble[countStatistic] = new Double(regressionResults.getA());
                    }
                    else {
                        statisticValueDouble[countStatistic] = new Double(regressionResults.getA(iEquation));
                    }
                }
                catch ( Exception e ) {
                    // No value computed.  Leave as null for output.
                }
            }
            else if ( statistics[iStatistic].equals("b") ) {
                statisticFieldType[countStatistic] = TableField.DATA_TYPE_DOUBLE;
                try {
                    if ( numEquations == 1 ) {
                        statisticValueDouble[countStatistic] = new Double(regressionResults.getB());
                    }
                    else {
                        statisticValueDouble[countStatistic] = new Double(regressionResults.getB(iEquation));
                    }
                }
                catch ( Exception e ) {
                    // No value computed.  Leave as null for output.
                }
            }
            else if ( statistics[iStatistic].equals("MeanX") ) {
                statisticFieldType[countStatistic] = TableField.DATA_TYPE_DOUBLE;
                try {
                    if ( numEquations == 1 ) {
                        statisticValueDouble[countStatistic] = new Double(regressionResults.getMeanX());
                    }
                    else {
                        statisticValueDouble[countStatistic] = new Double(regressionResults.getMeanX(iEquation));
                    }
                }
                catch ( Exception e ) {
                    // No value computed.  Leave as null for output.
                }
            }
            else if ( statistics[iStatistic].equals("MeanY") ) {
                statisticFieldType[countStatistic] = TableField.DATA_TYPE_DOUBLE;
                try {
                    if ( numEquations == 1 ) {
                        statisticValueDouble[countStatistic] = new Double(regressionResults.getMeanY());
                    }
                    else {
                        statisticValueDouble[countStatistic] = new Double(regressionResults.getMeanY(iEquation));
                    }
                }
                catch ( Exception e ) {
                    // No value computed.  Leave as null for output.
                }
            }
            else if ( statistics[iStatistic].equals("MeanY1") ) {
                statisticFieldType[countStatistic] = TableField.DATA_TYPE_DOUBLE;
                try {
                    if ( numEquations == 1 ) {
                        statisticValueDouble[countStatistic] = new Double(regressionResults.getMeanY1());
                    }
                    else {
                        statisticValueDouble[countStatistic] = new Double(regressionResults.getMeanY1(iEquation));
                    }
                }
                catch ( Exception e ) {
                    // No value computed.  Leave as null for output.
                }
            }
            else if ( statistics[iStatistic].equals("MeanY1est") ) {
                statisticFieldType[countStatistic] = TableField.DATA_TYPE_DOUBLE;
                try {
                    if ( numEquations == 1 ) {
                        statisticValueDouble[countStatistic] = new Double(regressionResults.getMeanY1Estimated());
                    }
                    else {
                        statisticValueDouble[countStatistic] = new Double(regressionResults.getMeanY1Estimated(iEquation));
                    }
                }
                catch ( Exception e ) {
                    // No value computed.  Leave as null for output.
                }
            }
            else if ( statistics[iStatistic].equals("NX") ) {
                statisticFieldType[countStatistic] = TableField.DATA_TYPE_INT;
                if ( numEquations == 1 ) {
                    statisticValueInteger[countStatistic] = new Integer(regressionResults.getN1() +
                        regressionResults.getN2());
                }
                else {
                    statisticValueInteger[countStatistic] = new Integer(regressionResults.getN1(iEquation) +
                        regressionResults.getN2(iEquation));
                }
            }
            else if ( statistics[iStatistic].equals("N1") ) {
                statisticFieldType[countStatistic] = TableField.DATA_TYPE_INT;
                if ( numEquations == 1 ) {
                    statisticValueInteger[countStatistic] = new Integer(regressionResults.getN1());
                }
                else {
                    statisticValueInteger[countStatistic] = new Integer(regressionResults.getN1(iEquation));
                }
            }
            else if ( statistics[iStatistic].equals("N2") ) {
                statisticFieldType[countStatistic] = TableField.DATA_TYPE_INT;
                if ( numEquations == 1 ) {
                    statisticValueInteger[countStatistic] = new Integer(regressionResults.getN2());
                }
                else {
                    statisticValueInteger[countStatistic] = new Integer(regressionResults.getN2(iEquation));
                }
            }
            else if ( statistics[iStatistic].equals("R") ) {
                statisticFieldType[countStatistic] = TableField.DATA_TYPE_DOUBLE;
                try {
                    if ( numEquations == 1 ) {
                        statisticValueDouble[countStatistic] =
                            new Double(regressionResults.getCorrelationCoefficient());
                    }
                    else {
                        statisticValueDouble[countStatistic] =
                            new Double(regressionResults.getCorrelationCoefficient(iEquation));
                    }
                }
                catch ( Exception e ) {
                    // No value computed.  Leave as null for output.
                }
            }
            else if ( statistics[iStatistic].equals("R2") ) {
                statisticFieldType[countStatistic] = TableField.DATA_TYPE_DOUBLE;
                try {
                    double r = regressionResults.getCorrelationCoefficient();
                    if ( numEquations == 12 ) {
                        r = regressionResults.getCorrelationCoefficient(iEquation);
                    }
                    Double r2 = new Double(r*r);
                    statisticValueDouble[countStatistic] = r2;
                }
                catch ( Exception e ) {
                    // No value computed.  Leave as null for output.
                }
            }
            else if ( statistics[iStatistic].equals("RMSE") ) {
                statisticFieldType[countStatistic] = TableField.DATA_TYPE_DOUBLE;
                try {
                    if ( numEquations == 1 ) {
                        statisticValueDouble[countStatistic] = new Double(regressionResults.getRMSE());
                    }
                    else {
                        statisticValueDouble[countStatistic] = new Double(regressionResults.getRMSE(iEquation));
                    }
                }
                catch ( Exception e ) {
                    // No value computed.  Leave as null for output.
                }
            }
            else if ( statistics[iStatistic].equals("SEE") ) {
                statisticFieldType[countStatistic] = TableField.DATA_TYPE_DOUBLE;
                try {
                    if ( numEquations == 1 ) {
                        statisticValueDouble[countStatistic] = new Double(regressionResults.getStandardErrorOfEstimate());
                    }
                    else {
                        statisticValueDouble[countStatistic] = new Double(regressionResults.getStandardErrorOfEstimate(iEquation));
                    }
                }
                catch ( Exception e ) {
                    // No value computed.  Leave as null for output.
                }
            }
            /* FIXME SAM 2011-01-06
            else if ( statistics[iStatistic].equals("SESlope") ) {
                statisticFieldType[countStatistic] = TableField.DATA_TYPE_DOUBLE;
                try {
                    if ( numEquations == 1 ) {
                        statisticValueDouble[countStatistic] = new Double(regressionResults.getStandardErrorOfSlope());
                    }
                    else {
                        statisticValueDouble[countStatistic] = new Double(regressionResults.getStandardErrorOfSlope(iEquation));
                    }
                }
                catch ( Exception e ) {
                    // No value computed.  Leave as null for output.
                }
            }
            else if ( statistics[iStatistic].equals("TestScore") ) {
                statisticFieldType[countStatistic] = TableField.DATA_TYPE_DOUBLE;
                try {
                    if ( numEquations == 1 ) {
                        statisticValueDouble[countStatistic] = new Double(regressionResults.getTestScore());
                    }
                    else {
                        statisticValueDouble[countStatistic] = new Double(regressionResults.getTestScore(iEquation));
                    }
                }
                catch ( Exception e ) {
                    // No value computed.  Leave as null for output.
                }
            }
            else if ( statistics[iStatistic].equals("TestQuantile") ) {
                statisticFieldType[countStatistic] = TableField.DATA_TYPE_DOUBLE;
                try {
                    if ( numEquations == 1 ) {
                        statisticValueDouble[countStatistic] = new Double(regressionResults.getTestQuantile());
                    }
                    else {
                        statisticValueDouble[countStatistic] = new Double(regressionResults.getTestQuantile(iEquation));
                    }
                }
                catch ( Exception e ) {
                    // No value computed.  Leave as null for output.
                }
            }
            else if ( statistics[iStatistic].equals("TestIsRelated") ) {
                statisticFieldType[countStatistic] = TableField.DATA_TYPE_STRING;
                try {
                    boolean related = regressionResults.getTestIsRelated();
                    if ( numEquations == 12 ) {
                        related = regressionResults.getTestIsRelated(iEquation);
                    }
                    if ( related ) {
                        statisticValueString[countStatistic] = "Yes";
                    }
                    else {
                        statisticValueString[countStatistic] = "No";
                    }
                }
                catch ( Exception e ) {
                    // No value computed.  Leave as null for output.
                }
            }
            else if ( statistics[iStatistic].equals("SX") ) {
                statisticFieldType[countStatistic] = TableField.DATA_TYPE_DOUBLE;
                try {
                    if ( numEquations == 1 ) {
                        statisticValueDouble[countStatistic] = new Double(regressionResults.getStandardDeviationX());
                    }
                    else {
                        statisticValueDouble[countStatistic] = new Double(regressionResults.getStandardDeviationX(iEquation));
                    }
                }
                catch ( Exception e ) {
                    // No value computed.  Leave as null for output.
                }
            }
            else if ( statistics[iStatistic].equals("SX1") ) {
                statisticFieldType[countStatistic] = TableField.DATA_TYPE_DOUBLE;
                try {
                    if ( numEquations == 1 ) {
                        statisticValueDouble[countStatistic] = new Double(regressionResults.getStandardDeviationX1());
                    }
                    else {
                        statisticValueDouble[countStatistic] = new Double(regressionResults.getStandardDeviationX1(iEquation));
                    }
                }
                catch ( Exception e ) {
                    // No value computed.  Leave as null for output.
                }
            }
            else if ( statistics[iStatistic].equals("SX2") ) {
                statisticFieldType[countStatistic] = TableField.DATA_TYPE_DOUBLE;
                try {
                    if ( numEquations == 1 ) {
                        statisticValueDouble[countStatistic] = new Double(regressionResults.getStandardDeviationX2());
                    }
                    else {
                        statisticValueDouble[countStatistic] = new Double(regressionResults.getStandardDeviationX2(iEquation));
                    }
                }
                catch ( Exception e ) {
                    // No value computed.  Leave as null for output.
                }
            }
            else if ( statistics[iStatistic].equals("SY") ) {
                statisticFieldType[countStatistic] = TableField.DATA_TYPE_DOUBLE;
                try {
                    if ( numEquations == 1 ) {
                        statisticValueDouble[countStatistic] = new Double(regressionResults.getStandardDeviationY());
                    }
                    else {
                        statisticValueDouble[countStatistic] = new Double(regressionResults.getStandardDeviationY(iEquation));
                    }
                }
                catch ( Exception e ) {
                    // No value computed.  Leave as null for output.
                }
            }
            else if ( statistics[iStatistic].equals("SY1") ) {
                statisticFieldType[countStatistic] = TableField.DATA_TYPE_DOUBLE;
                try {
                    if ( numEquations == 1 ) {
                        statisticValueDouble[countStatistic] = new Double(regressionResults.getStandardDeviationY1());
                    }
                    else {
                        statisticValueDouble[countStatistic] = new Double(regressionResults.getStandardDeviationY1(iEquation));
                    }
                }
                catch ( Exception e ) {
                    // No value computed.  Leave as null for output.
                }
            }
            else if ( statistics[iStatistic].equals("SY1est") ) {
                try {
                    if ( numEquations == 1 ) {
                        statisticValueDouble[countStatistic] = new Double(regressionResults.getStandardDeviationY1Estimated());
                    }
                    else {
                        statisticValueDouble[countStatistic] = new Double(regressionResults.getStandardDeviationY1Estimated(iEquation));
                    }
                }
                catch ( Exception e ) {
                    // No value computed.  Leave as null for output.
                }
            }
            */
        }
    }
    // By here the statistics will have been computed and are matched with the column name array
    // Now loop through again and process the row for the dependent and independent time series
    // First format the dependent and independent time series identifiers for to match the table...
    // Dependent time series identifier is configurable from parameter
    String tableTSIDDependent = null;
    if ( (tableTSIDFormat != null) && !tableTSIDFormat.equals("") ) {
        // Format the TSID using the specified format
        tableTSIDDependent = ts.formatLegend ( tableTSIDFormat );
    }
    else {
        // Use the alias if available and then the TSID
        tableTSIDDependent = ts.getAlias();
        if ( (tableTSIDDependent == null) || tableTSIDDependent.equals("") ) {
            tableTSIDDependent = ts.getIdentifierString();
        }
    }
    // Get the independent time series identifier
    String tableTSIDIndependent = null;
    if ( (tableTSIDFormat != null) && !tableTSIDFormat.equals("") ) {
        // Format the TSID using the specified format
        tableTSIDIndependent = regressionResults.getIndependentTS().formatLegend ( tableTSIDFormat );
    }
    else {
        // Use the alias if available and then the TSID
        tableTSIDIndependent = regressionResults.getIndependentTS().getAlias();
        if ( (tableTSIDIndependent == null) || tableTSIDIndependent.equals("") ) {
            tableTSIDIndependent = regressionResults.getIndependentTS().getIdentifierString();
        }
    }
    // Need to make sure that the table has the statistic columns of the correct type,
    // and look up the column numbers from the names in order to do the insert...
    int [] statisticColumnNumbers = new int[countStatisticTotal]; // columns in table
    countStatistic = -1;
    for ( int iEquation = 0; iEquation < numEquations; iEquation++ ) {
        for ( int iStatistic = 0; iStatistic < statistics.length; iStatistic++ ) {
            ++countStatistic;
            try {
                statisticColumnNumbers[countStatistic] = table.getFieldIndex ( statisticColumnNames[countStatistic] );
            }
            catch ( Exception e ) {
                statisticColumnNumbers[countStatistic] = -1; // Indicates no column name matched in table
            }
            if ( statisticColumnNumbers[countStatistic] < 0 ) {
                // The statistic column does not exist, so add and initialize with null (not nonValue)
                // The value will be set below.
                if ( statisticFieldType[countStatistic] == TableField.DATA_TYPE_DOUBLE ) {
                    // Use precision of 8, which should cover most statistics without roundoff
                    // (although this may be too many significant digits for some intput).
                    statisticColumnNumbers[countStatistic] =
                        table.addField(new TableField(
                            TableField.DATA_TYPE_DOUBLE,statisticColumnNames[countStatistic],-1,8), null );
                }
                else if ( statisticFieldType[countStatistic] == TableField.DATA_TYPE_INT ) {
                    statisticColumnNumbers[countStatistic] =
                        table.addField(new TableField(
                            TableField.DATA_TYPE_INT,statisticColumnNames[countStatistic],-1,-1), null );
                }
                else if ( statisticFieldType[countStatistic] == TableField.DATA_TYPE_STRING ) {
                    statisticColumnNumbers[countStatistic] =
                        table.addField(new TableField(
                            TableField.DATA_TYPE_STRING,statisticColumnNames[countStatistic],-1,-1), null );
                }
                Message.printStatus(2,routine,"Added column \"" + statisticColumnNames[countStatistic] +"\" at index "
                     + statisticColumnNumbers[countStatistic] );
            }
        }
    }
    // Next, find the record that has the dependent and independent identifiers...
    // Find the record that matches the dependent and independent identifiers (should only be one but
    // handle multiple matches)
    List<String> tableColumnNames = new Vector(); // The dependent and independent TSID column names
    tableColumnNames.add ( tableTSIDColumnName );
    tableColumnNames.add ( tableTSIDColumnNameIndependent );
    List<String> tableColumnValues = new Vector(); // The dependent and independent TSID values
    tableColumnValues.add ( tableTSIDDependent );
    tableColumnValues.add ( tableTSIDIndependent );
    List<TableRecord> recList = table.getRecords ( tableColumnNames, tableColumnValues );
    Message.printStatus(2,routine,"Searched for records with columns matching \"" +
        tableTSIDColumnName + "\"=\"" + tableTSIDDependent + "\" " +
        tableTSIDColumnNameIndependent + "\"=\"" + tableTSIDIndependent + "\"... found " + recList.size() );
    if ( recList.size() == 0 ) {
        // No record in the table so add one with TSID column values and blank statistic values...
        TableRecord rec = null;
        table.addRecord(rec=table.emptyRecord());
        rec.setFieldValue(tableTSIDColumnNumber, tableTSIDDependent);
        rec.setFieldValue(tableTSIDColumnNumberIndependent, tableTSIDIndependent);
        recList.add ( rec );
    }
    // Finally loop through the statistics and insert into the rows matched above.  Although multiple
    // records may have been matched, the normal case will be that one record is matched.  Offset the column
    // number by 2 to account for the dependent and independent time series identifier columns
    for ( TableRecord rec : recList ) {
        countStatistic = -1;
        for ( int iEquation = 0; iEquation < numEquations; iEquation++ ) {
            for ( int iStatistic = 0; iStatistic < statistics.length; iStatistic++ ) {
                // Set the value based on the object type for the statistic...
                ++countStatistic;
                if ( statisticValueDouble[countStatistic] != null ) {
                    rec.setFieldValue(statisticColumnNumbers[countStatistic],
                         statisticValueDouble[countStatistic]);
                }
                if ( statisticValueInteger[countStatistic] != null ) {
                    rec.setFieldValue(statisticColumnNumbers[countStatistic],
                         statisticValueInteger[countStatistic]);
                }
            }
        }
    }
}

}