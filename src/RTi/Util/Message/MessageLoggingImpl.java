// MessageLoggingImpl - message logging implementation

/* NoticeStart

CDSS Common Java Library
CDSS Common Java Library is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1994-2019 Colorado Department of Natural Resources

CDSS Common Java Library is free software:  you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    CDSS Common Java Library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with CDSS Common Java Library.  If not, see <https://www.gnu.org/licenses/>.

NoticeEnd */

/**
 *
 * Created on February 19, 2007, 10:50 AM
 *
 */

package RTi.Util.Message;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;

/**
 *
 * @author iws
 */
public class MessageLoggingImpl extends MessageImpl {
    
    private Logger status = Logger.getLogger("RTi.Util.Message.status");
    private Logger warning = Logger.getLogger("RTi.Util.Message.warning");
    private Logger debug = Logger.getLogger("RTi.Util.Message.debug");
    
    public static void install() {
        System.setProperty("RTi.Util.MessageImpl",MessageLoggingImpl.class.getName());
    }
    
    public MessageLoggingImpl() {
        status.setLevel(Level.OFF);
        warning.setLevel(Level.WARNING);
        debug.setLevel(Level.OFF);
    }
    
    protected void flushOutputFiles(int flag) {
        // do nothing
    }
    
    private Level translateWarningLevel(int level) {
        Level l;
        if (level <= 1) {
            l = Level.WARNING;
        } else if (level <= 10) {
            l = Level.FINE;
        } else {
            l = Level.FINER;
        }
        return l;
    }
    
    private Level translateDebugLevel(int level) {
        Level l;
        if (level <= 1) {
            l = Level.FINE;
        } else if (level <= 10) {
            l = Level.FINER;
        } else {
            l = Level.FINEST;
        }
        return l;
    }
    
    protected void printWarning(int l, String routine, Throwable e) {
        log(warning,translateWarningLevel(l),routine,e);
    }
    
    protected void printDebug(int l, String routine, Throwable e) {
        log(debug,translateDebugLevel(l),routine,e);
    }
    
    protected void printWarning(int l, String routine, String message, JFrame top_level) {
        printWarning(l,routine,message);
    }
    
    protected void printWarning(int l, String routine, String message) {
        log(warning,translateWarningLevel(l),routine,message);
    }
    
    protected void printStatus(int l, String routine, String message) {
        log(status,translateDebugLevel(l),routine,message);
    }
    
    protected void printWarning(int l, String tag, String routine, String message) {
        log(warning,translateWarningLevel(l),routine,message);
    }
    
    protected void printDebug(int l, String routine, String message) {
        log(debug,translateDebugLevel(l),routine,message);
    }
    
    protected void initStreams() {
        
    }
    
    private void log(Logger logger,Level level,String routine, String message) {
        if (logger.isLoggable(level)) {
            int idx = routine.indexOf('.');
            String clazz = null;
            String method = null;
            if (idx >= 0) {
                clazz = routine.substring(0,idx);
                method = routine.substring(idx +1 ,routine.length());
            } else {
                clazz = routine;
            }
            logger.logp(level,clazz,method,message);
        }
    }
    
    private void log(Logger logger,Level level,String routine, Throwable e) {
        if (logger.isLoggable(level)) {
            int idx = routine.indexOf('.');
            String clazz = null;
            String method = null;
            if (idx >= 0) {
                clazz = routine.substring(0,idx);
                method = routine.substring(idx +1 ,routine.length());
            } else {
                clazz = routine;
            }
            logger.logp(level,clazz,method,e.getMessage(),e);
        }
    }

    
}
