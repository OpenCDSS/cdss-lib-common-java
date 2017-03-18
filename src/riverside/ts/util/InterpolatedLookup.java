package riverside.ts.util;

import java.util.HashMap;
import java.util.Iterator;

/**
 *
 * @author rla
 * 
 * Provides a simple means of interpolation on a set of key-value pairs. 
 * 
 * This class provides an easy-to-use wrapper for the riverside.ts.util.Table 
 * class.
 * 
 * The interpolation mode is Linear by default. 
 */
public class InterpolatedLookup {
    
    private Table _table;
    private Table.InterpolationMode _interpolationMode;
    
    public static enum InterpolationMode {
        LINEAR, LOGARITHMIC;
    }
    
    public InterpolatedLookup() {
        _table = new Table();
        _interpolationMode = Table.InterpolationMode.LINEAR;
    }
    
    
    public boolean isEmpty() {
        return (_table == null || _table.getNRows() == 0);
    }
    
    public void setInterpolationMode(InterpolationMode interpolationMode) {
        switch (interpolationMode) {
            case LINEAR:
                _interpolationMode = Table.InterpolationMode.LINEAR;
                break;
            case LOGARITHMIC:
                _interpolationMode = Table.InterpolationMode.LOGARITHMIC;
                break;
        }
    }
    
    /**
     * 
     * @param list You will normally create a HashMap containing the key value
     *             pairs to be interpolated. You will need to call this method 
     *             before calling lookupValue() or lookupKey().
     */
    public void setBasis(HashMap<Double,Double> list) {
        _table = new Table(list.size());
        int i = 0;
        for (Iterator<Double> iter = list.keySet().iterator(); iter.hasNext(); ) {
            double key = iter.next();
            double value = list.get(key);
            _table.populate(i, Table.GETCOLUMN_1, key);
            _table.populate(i, Table.GETCOLUMN_2, value);
            i++;
        }
        _table.sort(Table.GETCOLUMN_1);
    }
    
    /**
     * 
     * @param key the key of the key-value relation.
     * @return the interpolated value of the key-value relation.
     */
    public double lookupValue(double key) {
        return _table.lookup(key, Table.GETCOLUMN_1, true, _interpolationMode);
    }
    
    /**
     * 
     * @param value the value of the key-value relation.
     * @return the interpolated key of the key-value relation.
     */
    public double lookupKey(double value) {
        return _table.lookup(value, Table.GETCOLUMN_2, true, _interpolationMode);
    }
    
}


