// TSViewWindowManager - window manager for TSView windows

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

package RTi.GRTS;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

// TODO SAM 2011-11-29 Need to add listener ability, for example to let the TSTool UI know when
// windows have been closed so that the "View - Close All View Windows" state can be updated.

/**
Window manager for TSView windows.  This is used, for example, to close all open windows with one method call.
TODO SAM 2011-11-29 could probably make this more agnostic but for now focus on JFrames.
*/
public class TSViewWindowManager
{
    
/**
The list of TSViewJFrame being managed.
*/
List<TSViewJFrame> __tsviewList = new ArrayList<TSViewJFrame>();
    
/**
Construct the window manager.
*/
public TSViewWindowManager ()
{   
}

/**
Add a window to the manager.
@param parent the TSViewJFrame that is managing the set of related views
@param frame an individual view in a group within a TSViewJFrame
*/
public void add ( TSViewJFrame parent, JFrame frame )
{
    // Currently only work with the parent since only the closeAll method is implemented.
    // If finer-grained behavior is needed, then start dealing with the individual windows separately.
    // Make sure to avoid duplicates, which complicate logic later when closing
    int pos = __tsviewList.indexOf ( parent );
    if ( pos < 0 ) {
        __tsviewList.add ( parent );
    }
}

/**
Close all the open views.
*/
public void closeAll ()
{
    TSViewJFrame v;
    int closed = 0;
    for ( int i = 0; i < __tsviewList.size(); i++ ) {
        v = __tsviewList.get(i);
        // Since this method closes everything, just close each possible window.
        // TODO SAM 2011-11-29 Maybe it would just be cleaner to have a TSViewJFrame.closeAll() method
        // that does the following?
        closed = v.closeGUI(TSViewType.GRAPH);
        if ( closed >= 0 ) {
            closed = v.closeGUI(TSViewType.PROPERTIES);
        }
        if ( closed >= 0 ) {
            closed = v.closeGUI(TSViewType.PROPERTIES_HIDDEN);
        }
        if ( closed >= 0 ) {
            closed = v.closeGUI(TSViewType.SUMMARY);
        }
        if ( closed >= 0 ) {
            closed = v.closeGUI(TSViewType.TABLE);
        }
        // The above calls will result in remove() being called, which will adjust the list, but need to
        // reposition the index to continue processing
        if ( closed < 0 ) {
            --i;
        }
    }
}

/**
Return the number of windows that are being managed.
*/
public int getWindowCount ()
{
    return __tsviewList.size();
}

/**
Remove a TSViewJFrame from the list because all of its windows have been closed
in direct interaction with the view windows.  That way the resources will no longer be
managed by this manager.
*/
public void remove ( TSViewJFrame tsview )
{
    // Go through the list and remove all matching instances
    TSViewJFrame v;
    for ( int i = 0; i < __tsviewList.size(); i++ ) {
        v = __tsviewList.get(i);
        if ( v == tsview ) {
            __tsviewList.remove(i);
            //Message.printStatus(2, "remove", "Removed view at " +  i + " size=" + __tsviewList.size() );
            --i;
        }
    }
}

}
