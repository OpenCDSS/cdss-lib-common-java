package riverside.ts.routing.lagk;

import RTi.TS.TS;
import RTi.Util.IO.DataUnits;
import RTi.Util.IO.DataUnitsConversion;
import RTi.Util.Message.Message;
import RTi.Util.Time.DateTime;
import RTi.Util.Time.TimeInterval;
import riverside.ts.util.Table;

/**
This class provides the builder for a LagK object.
*/
public class LagKBuilder {
    
    private LagK lk = createDefault();
    private int _n_kval = 1;
    private int _n_lagval = 1;
    private double valueOfK = 0;
    private static final int MAXISEGS = 20;
    
    public LagKBuilder(TS inflows) {
        lk._t_mult = inflows.getDataIntervalMult();
        lk._t_int = inflows.getDataIntervalBase();
        DateTime dt = new DateTime(inflows.getDate1());
        lk.forecastDate1 = dt;
        lk.totalInflows = inflows;
    }
    
    public static void update(LagK lagK, TS inflows) {
        DateTime dt = new DateTime(inflows.getDate1());
//        dt.addInterval(inflows.getDataIntervalBase(),1);
        lagK.forecastDate1 = dt;
    }
    
    public LagK create() {
        String routine = "LagK.create";
        
        // Size of carryover array includes lag window divided by the interval, plus 1 to be
        // inclusive of the end-points (?maybe?), plus 2 for K.
        lk._sizeInflowCO = (lk._lagMax + lk._lagMin)/lk._t_mult + 3;

        if ( Message.isDebugOn ) {
            Message.printDebug(1, routine, "lk._lag=" + lk._lag + " lk._t_mult=" + lk._t_mult +
                " lk._sizeInflowCO =" + lk._sizeInflowCO ); 
        }
        
        lk.variableK = _n_kval != 1;
        // FIXME SAM 2009-04-16 Negative lag requires variable lag and K and need > 1 row in lag table
        lk.variableLag = _n_lagval != 1;
        if (lk._in_lag_tbl.getNRows() > lk._sizeInflowCO) {
            lk._sizeInflowCO = lk._in_lag_tbl.getNRows();
        }
        if (lk._co_inflow == null) {
            lk._co_inflow = new double[lk._sizeInflowCO];
        } else {
            //Arrays.fill(lk._co_inflow,0);
        }
        
        if (lk.variableLag) {
            if (lk._co_inflow == null) {
                throw new IllegalArgumentException("Must have carry over values");
            }
        } else {
            //@todo - removed this because the returned _co_inflow array is size 2, but when passing it back,
            // it barfs and says it only wants 1, not 2!
            
//            if (lk._sizeInflowCO != lk._co_inflow.length) {
//                throw new IllegalArgumentException("Must have " + lk._sizeInflowCO + " carry over values, not " + lk._co_inflow.length);
//            }
        }
        
        if (lk.variableK) {
            makeStorageVSOutflowTable();
            makeStorageVSOutflowTableQuarter();
        }
        
        // Check to see if a single K value of 0 is set.  If so, ensure that
        // INITIALOUTFLOW is consistent with the first value (and second,
        // if applicable) in the COINFLOW section.
        if ( _n_kval == 1 && valueOfK == 0 ) {
            if ( (lk._lag % lk._t_mult) != 0 ) {
                double tmpP = lk._co_inflow[0] ;
                double tmpN = lk._co_inflow[1] ;
                double diff = lk._t_mult * ( (int)lk._lag / lk._t_mult + 1 ) - lk._lag ;
                // Interpolate
                double LagdQin1 = tmpP + ( tmpN - tmpP ) *
                    ( diff / (double)lk._t_mult ) ;
                // Test equality using inequalities
                double tolerance = 0.0001*lk._outflowCO ;
                if ( LagdQin1 > lk._outflowCO+tolerance ||
                    LagdQin1 < lk._outflowCO-tolerance ) {
                    // Print Warning and revise _outflowCO
                    lk.logger.warning( String.format("INITIALOUTFLOW " +
                        "value \"%f\" not consistent with " +
                        "COINFLOW values \"%f and %f\" when " +
                        "lagged resulting in \"%f\""  +
                        "Instability may result.  Revising " +
                        "INITIALOUTFLOW value to \"%f\".", 
//                        _outflowCO/ffactor, tmpP/ffactor,
//                        tmpN/ffactor, LagdQin1/ffactor,
//                        LagdQin1/ffactor) ;
                        lk._outflowCO, tmpP,
                        tmpN, LagdQin1,
                        LagdQin1));
                    lk._outflowCO = LagdQin1 ;
//                    _owner->_outflow = _outflowCO ;
                }
            } else {
                double LagdQin1 = lk._co_inflow[0] ;
                // Test equality using inequalities
                double tolerance = 0.0001*lk._outflowCO ;
                if ( LagdQin1 > lk._outflowCO+tolerance ||
                    LagdQin1 < lk._outflowCO-tolerance ) {
                    // Print Warning and revise _outflowCO
                    lk.logger.warning( String.format( "INITIALOUTFLOW " +
                        "value \"%f\" not consistent with " +
                        "COINFLOW value \"%f\".  Instability " +
                        "may result.  Revising INITIALOUTFLOW " +
//                        "value to \"%f\".", _outflowCO/ffactor,
//                        LagdQin1/ffactor, LagdQin1/ffactor) );
                        "value to \"%f\".", lk._outflowCO,
                        LagdQin1, LagdQin1) );
                    lk._outflowCO = LagdQin1 ;
//                    _owner->_outflow = _outflowCO ;
                }
            }
        }
        
        return lk;
    }
    
    public void setCarryOverInfows(double[] d) {
        lk._co_inflow = d;
    }
    
    public void setTransLossCoef(double d) {
        lk._transLossCoef = d;
    }
    public void setTransLossLevel(double d) {
        lk._transLossLevel = d;
    }
    
    public void setLag(int lag) {
        lk._lag = lag;
    }
    
    public void setInitialLaggedInflow(double inflow) {
        lk._laggedInflow = inflow;
    }
    
    public void setInitialOutflow(double initialOutflow) {
        lk._outflowCO = initialOutflow;
    }
    
    public void setInitialStorage(double initialStorage) {
        lk._storageCO = initialStorage;;
    }
    
    public void setK(double k) {
        lk._out_k_tbl.allocateDataSpace(2);
        lk._out_k_tbl.populate(0,LagK.FLOWCOLUMN, 0.);
        lk._out_k_tbl.populate(0,LagK.KCOLUMN, k);
        lk._out_k_tbl.populate(1,LagK.FLOWCOLUMN, Double.MAX_VALUE );
        lk._out_k_tbl.populate(1,LagK.KCOLUMN, k);
        _n_kval = 2;        
        valueOfK = k;
        lk.variableK = true;// @verify ??T
    }
    
    public void setLagIn(Table table) {
        String routine = getClass().getName() + ".setLagIn";
        lk._in_lag_tbl = table;
        int l = 0;
        for (int i = 0; i < table.getNRows(); i++) {
            double v = table.lookup(i,Table.GETCOLUMN_2);
            if (v > l) {
                l = (int) ( v + .5 );
                // Needed for negative lag...
                lk._lagMax = l;
            }
            // Needed for negative lag...
            if ( (v < lk._lagMin) && (v < 0) ) {
                lk._lagMin = (int) ( -v + .5 );;
            }
        }
        Message.printStatus(2,routine, "lagMin=" + lk._lagMin + " lagMax="+lk._lagMax );
        _n_lagval = table.getNRows();
        lk._lag = l;
    }
    
    public void setKOut(Table outKTable) {
        // @todo check sorting
        // This next section adds to the K table if the table either
        // starts with non-zero flow or ends with a small flow < 1e6.
        int flagNeedZero = 0 ;
        int flagNeedInf = 0 ;
        double zeroValue = 0.0 ;
        double infValue = 0.0 ;
        
        // REVISIT: when table has getFirst and getLast, update
        // this code to use it.  (Table.getFirst, Table.getLast)
        if ( outKTable.lookup( 0, LagK.FLOWCOLUMN ) > .001 ) {
            flagNeedZero = 1 ;
            zeroValue = outKTable.lookup( 0, LagK.KCOLUMN ) ;
        }
        // Detect if the last value is below 1e6, keeping last value
        if ( outKTable.lookup( 0, LagK.FLOWCOLUMN ) < 1000000.0 ) {
            flagNeedInf = 1 ;
            infValue = outKTable.lookup( outKTable.getNRows( ) - 1,
                LagK.KCOLUMN ) ;
        }
        
        // REVISIT: when table has addFirst and addLast, update
        // this code to use it.  (Table.addFirst, Table.addLast)
        Table tmpTable = new Table();
        tmpTable.allocateDataSpace( outKTable.getNRows() +
            flagNeedInf + flagNeedZero );
        
        tmpTable.populate( 0, LagK.FLOWCOLUMN, 0.0 ) ;
        tmpTable.populate( 0, LagK.KCOLUMN, zeroValue ) ;
        
        for (int j = 0 + flagNeedZero;
        j < outKTable.getNRows( ) + flagNeedZero; j++ ) {
            tmpTable.populate( j, LagK.FLOWCOLUMN,
                outKTable.lookup( j - flagNeedZero, LagK.FLOWCOLUMN ) ) ;
            tmpTable.populate( j, LagK.KCOLUMN,
                outKTable.lookup( j - flagNeedZero, LagK.KCOLUMN ) ) ;
        }
        
        if ( flagNeedInf > 0 ) {
            int tablePosition = outKTable.getNRows( ) + flagNeedZero ;
            tmpTable.populate( tablePosition, LagK.KCOLUMN , infValue ) ;
            tmpTable.populate( tablePosition, LagK.FLOWCOLUMN, 1000000.0 ) ;
        }
        
        lk._out_k_tbl = tmpTable;
        _n_kval = tmpTable.getNRows();
    }
    
    private LagK createDefault() {
        LagK lk = new LagK();
//        lk._out_k_tbl.allocateDataSpace(1);
//        lk._out_k_tbl.populate( 0, 0, 1000000.0f );
//        lk._out_k_tbl.populate( 0, 1, 0 );
//        lk._transLossCoef = 0;
//        lk._transLossLevel = 0;
        
        return lk;
    }
    
    private void makeStorageVSOutflowTable() {
        makeStorageVSOutflowTableBase(lk._stor_out_tbl,1.0);
    }
    
    private void makeStorageVSOutflowTableQuarter() {
        makeStorageVSOutflowTableBase(lk._stor_out_tbl4,4.0);
    }
    
    
    void makeStorageVSOutflowTableBase( Table outTable, double divisor ) {
        // This routine will loop through and construct a O2 vs.
        // 2S/dt+O2 table for as many O2 values as specified in the
        // _k_out_tbl.
        
        double c1 = 12 ;          // Constant for intermediate points
        double c2 = 100 ;         // Constant for intermediate points
        double deltaK = 0.0 ;     // Delta K for i and i+1
        double deltaQ = 0.0 ;     // Delta Q for i and i+1
        int i ;                   // loop variable
        int ipart = 0 ;           // loop variable for intermediate point
        int isegs = 0 ;           // total number of intermediate points
        int npkq = _n_kval ;      // number of k vs. q pairs
        int nPos = 0 ;            // current number of K/Outflow pairs
        double q1 = 0.0 ;         // flow at i
        double q2 = 0.0 ;         // flow at i+1
        double qbar = 0.0 ;       // average flow across period
        Table result_table;       // Results storage before copy to outTable
        double storage = 0.0 ;    // Storage term
        double xita = lk._t_mult / divisor; // time step
//
//        PrintDebug( 10, routine, "Lag-K Storage-Outflow Table &"
//            " Outflow-K table." );
        
        // To avoid pre-counting, allocate maximum possible space for local
        // results table
        result_table = new Table();
        result_table.allocateDataSpace( npkq * MAXISEGS + 2 );
        
        // q1/q2 are used in the algorithm in place of o1/o2 in the
        // external documentation.
        for ( i = 0 ; i < npkq ; i++ ) {
            if ( i < npkq - 1 ) {
                deltaK = Math.abs( lk._out_k_tbl.lookup( i, LagK.KCOLUMN ) -
                    lk._out_k_tbl.lookup( i + 1 , LagK.KCOLUMN ) ) ;
                deltaQ = Math.abs( lk._out_k_tbl.lookup( i, LagK.OUTFLOWCOLUMN ) -
                    lk._out_k_tbl.lookup( i + 1 , LagK.OUTFLOWCOLUMN ) ) ;
                
                isegs = 1 ;
                if ( deltaK != 0 ) isegs = (int)( ( ( deltaQ + ( c1 * deltaK ) ) / c2 ) +
                    1.5 ) ;
                if ( isegs > MAXISEGS ) isegs = MAXISEGS ;
                
                
//                Line20:
                // For each sample point, interpolate to get the 2S/dt+O2 vs.
                // O2 table.
                for ( ipart = 0 ; ipart < isegs ; ipart++ ) {
                    q2 = lk._out_k_tbl.lookup( i, LagK.OUTFLOWCOLUMN ) +
                        deltaQ * ipart / isegs ;
                    result_table.populate( nPos, LagK.STOR_OUTFLOWCOLUMN, q2 ) ;
                    qbar = ( q2 + q1 ) / 2.0 ;
                    storage = lk._out_k_tbl.lookup( qbar, LagK.KCOLUMN, true ) *
                        ( q2 - q1 ) + storage ;
                    result_table.populate( nPos, LagK.STOR_SDTCOLUMN,
                        ( 2.0 * storage / xita ) + q2 ) ;
                    
                    q1 = q2 ;
                    nPos++ ;
                }
            } else {
                q2 = lk._out_k_tbl.lookup( i, LagK.OUTFLOWCOLUMN ) ;
                result_table.populate( nPos, LagK.STOR_OUTFLOWCOLUMN, q2 ) ;
                qbar = ( q2 + q1 ) / 2 ;
                storage = lk._out_k_tbl.lookup( qbar, LagK.KCOLUMN, true ) *
                    ( q2 - q1 ) + storage ;
                result_table.populate( nPos, LagK.STOR_SDTCOLUMN,
                    ( 2.0 * storage / xita ) + q2 ) ;
                
                q1 = q2 ;
                nPos++ ;
            }
        }
        
//        Line100:
        result_table.populate( nPos, LagK.STOR_OUTFLOWCOLUMN, q2 ) ;
        qbar = ( q2 + q1 ) / 2 ;
        storage = lk._out_k_tbl.lookup( qbar, LagK.KCOLUMN, true ) * ( q2 - q1 ) +
            storage ;
        result_table.populate( nPos, LagK.STOR_SDTCOLUMN, ( 2.0 * storage / xita ) +
            q2 ) ;
        
        // Verify this - A table without zero should return a non-zero value
        // or a failure for this to work.
        if ( result_table.lookup( nPos, LagK.STOR_SDTCOLUMN, true ) == 0.0 ) {
//                goto Line200;
        } else
            
            // Add a (0, 0) point to the result_table.  Allocate and copy because
            // table resizing is not available.
            
            if ( ( result_table.lookup( 0, LagK.STOR_SDTCOLUMN ) > 0.01 ) ||
            ( result_table.lookup( 0, LagK.STOR_OUTFLOWCOLUMN ) > 0.01 ) ) {
            for ( i = nPos - 1 ; i >= 0 ; i-- ) {
                result_table.populate( i + 1, LagK.STOR_SDTCOLUMN,
                    result_table.lookup( i, LagK.STOR_SDTCOLUMN ) ) ;
                result_table.populate( i + 1, LagK.STOR_OUTFLOWCOLUMN,
                    result_table.lookup( i, LagK.STOR_OUTFLOWCOLUMN ) ) ;
            }
            result_table.populate( 0, LagK.STOR_SDTCOLUMN, 0.0 ) ;
            result_table.populate( 0, LagK.STOR_OUTFLOWCOLUMN, 0.0 ) ;
            nPos++;
            }
        
//            Line200:
        q2 = 1.0e+6 ;
        result_table.populate( nPos, LagK.STOR_OUTFLOWCOLUMN, q2 ) ;
        qbar = ( q2 + q1 ) / 2 ;
        storage = lk._out_k_tbl.lookup( qbar, LagK.KCOLUMN, true ) * ( q2 - q1 ) +
            storage ;
        result_table.populate( nPos, LagK.STOR_SDTCOLUMN, ( 2.0 * storage / xita ) +
            q2 ) ;
        nPos++;
        
        
        // For the final result, copy to the outTable
        outTable.allocateDataSpace( nPos ) ;
        for ( i = 0 ; i < nPos ; i++ ) {
            outTable.populate( i, LagK.STOR_OUTFLOWCOLUMN,
                result_table.lookup( i, LagK.STOR_OUTFLOWCOLUMN ) ) ;
            outTable.populate( i, LagK.STOR_SDTCOLUMN,
                result_table.lookup( i, LagK.STOR_SDTCOLUMN ) ) ;
//            PrintDebug( 10, "", "Stor:  %f  Out:  %f",
//                outTable.lookup( i, LagK.STOR_SDTCOLUMN ),
//                outTable.lookup( i, LagK.STOR_OUTFLOWCOLUMN ) ) ;
        }
        
    }


    /**
     * Ensure that the values in a Table are normalized to a time series with respect to interval and units.
     * @param originalTable The Table to check
     * @param lagInterval The interval of the Table
     * @param flowUnits The units of the Table
     * @param ts The time series to use for units and interval
     * @return A new normalized Table or the original if no changes were needed
     * @throws IllegalArgumentException if either the interval or units could not be converted.
     */
    public static Table normalizeTable(Table originalTable,TimeInterval lagInterval,String flowUnits,TS ts) {
        if (originalTable.getNRows() == 0) {
            return originalTable;
        }

        final boolean intervalBaseExact = lagInterval.getBase() == ts.getDataIntervalBase();
        DataUnitsConversion unitsConversion = null;
        boolean unitsExact = DataUnits.areUnitsStringsCompatible(ts.getDataUnits(), flowUnits, true);
        if (!unitsExact) {
            // Verify that conversion factors can be obtained.
            boolean unitsCompatible = DataUnits.areUnitsStringsCompatible(ts.getDataUnits(), flowUnits, false);
            if (unitsCompatible) {
                // Get the conversion factor on the units.
                try {
                    unitsConversion = DataUnits.getConversion(flowUnits, ts.getDataUnits());
                } catch (Exception e) {
                    throw new RuntimeException("Obtaining data units conversion failed", e);
                }
            } else if (!unitsExact && unitsCompatible) {
                // This is a problem
                throw new IllegalArgumentException(String.format(
                        "The input time series units \"%s\" " +
                        "are not compatible with table flow values \"%s\" - cannot convert to use for routing.",
                        ts.getDataUnits(),
                        flowUnits
                        ));

            }
        }
        if ( unitsExact && intervalBaseExact ) {
            return originalTable;
        }
        
        Table newTable = new Table();
        newTable.allocateDataSpace(originalTable.getNRows());
        double flowAddFactor = unitsConversion == null ? 0 : unitsConversion.getAddFactor();
        double flowMultFactor = unitsConversion == null ? 1 : unitsConversion.getMultFactor();
        double timeMultFactor; // Convert from original table to time series base interval, 1.0 for no change
        if ( intervalBaseExact ) {
            timeMultFactor = 1.0;
        }
        else if ( (lagInterval.getBase() == TimeInterval.DAY) && (ts.getDataIntervalBase() == TimeInterval.HOUR)) {
           timeMultFactor = lagInterval.getMultiplier()*24.0;
        }
        else if ( (lagInterval.getBase() == TimeInterval.DAY) && (ts.getDataIntervalBase() == TimeInterval.MINUTE)) {
            timeMultFactor = lagInterval.getMultiplier()*24.0*60.0/ts.getDataIntervalMult();
        }
        else if ( (lagInterval.getBase() == TimeInterval.HOUR) && (ts.getDataIntervalBase() == TimeInterval.MINUTE)) {
            timeMultFactor = lagInterval.getMultiplier()*60.0/ts.getDataIntervalMult();
        }
        else if ( (lagInterval.getBase() == TimeInterval.HOUR) && (ts.getDataIntervalBase() == TimeInterval.DAY)) {
            timeMultFactor = lagInterval.getMultiplier()/ts.getDataIntervalMult()*24.0;
        }
        else if ( (lagInterval.getBase() == TimeInterval.MINUTE) && (ts.getDataIntervalBase() == TimeInterval.DAY)) {
            timeMultFactor = lagInterval.getMultiplier()/ts.getDataIntervalMult()*24.0*60.0;
        }
        else if ( (lagInterval.getBase() == TimeInterval.MINUTE) && (ts.getDataIntervalBase() == TimeInterval.HOUR)) {
            timeMultFactor = lagInterval.getMultiplier()/ts.getDataIntervalMult()*60.0;
        }
        else {
            throw new IllegalArgumentException(String.format(
                    "The input time series interval \"%s\" cannot be converted to internal values of \"%s\"",
                    ts.getIdentifier().getInterval(),
                    lagInterval
                    ));
        }
        for ( int i = 0; i < originalTable.getNRows(); i++ ) {
            double newFlowValue = flowAddFactor + flowMultFactor*originalTable.get(i,0);
            double newTimeValue = timeMultFactor*originalTable.get(i,1);
            newTable.set(i, newFlowValue, newTimeValue );
        }
        return newTable;
    }
    
}
