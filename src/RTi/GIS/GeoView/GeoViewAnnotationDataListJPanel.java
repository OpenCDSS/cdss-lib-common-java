package RTi.GIS.GeoView;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import RTi.Util.GUI.JGUIUtil;
import RTi.Util.Message.Message;

/**
A panel to hold a list of GeoViewAnnotationData, to allow interaction such as clearing the list
of annotations.
*/
public class GeoViewAnnotationDataListJPanel extends JPanel implements ActionListener
{

/**
The JList that manages the list of annotations (labels).
*/
private JList __annotationJList = null;

/**
Data for the list.
*/
private DefaultListModel __annotationJListModel = new DefaultListModel();

/**
Indicate whether the component should be set invisible when the list is empty.
*/
private boolean __hideIfEmpty = false;

/**
The list of annotations maintained in the GeoView.
*/
private List<GeoViewAnnotationData> __annotationDataList = null;

/**
The component that actually renders the annotations - need this if the popup menu changes the
list of displayed annotations (such as clearing the list).
*/
private GeoViewJComponent __geoView = null;

/**
Menu items.
*/
private String __RemoveAllAnnotationsString = "Remove All Annotations";

/**
Constructor.
@param annotationDataList list of annotation data, if available (can pass null and reset the list
later by calling setAnnotationData()).
@param hideIfEmpty if true, set the panel to not visible if the list is empty - this may be appropriate
if UI real estate is in short supply and annotations should only be shown if used
*/
public GeoViewAnnotationDataListJPanel ( List<GeoViewAnnotationData> annotationDataList,
	GeoViewJComponent geoView, boolean hideIfEmpty )
{	super();
	// Set up the layout manager
	this.setLayout(new GridBagLayout());
	this.setBorder(BorderFactory.createTitledBorder("Annotations"));
	int y = 0;
	Insets insetsTLBR = new Insets ( 0, 0, 0, 0 );
	__annotationJList = new JList();
	if ( annotationDataList != null ) {
		setAnnotationData ( annotationDataList );
	}
	JGUIUtil.addComponent ( this, new JScrollPane(__annotationJList),
		0, y, 1, 1, 1.0, 1.0,
		insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.SOUTH );
	__hideIfEmpty = hideIfEmpty;
	__geoView = geoView;
	
	// Add popup for actions on annotations
	
	final JPopupMenu popupMenu = new JPopupMenu();
	JMenuItem removeAllAnnotationsJMenuItem = new JMenuItem(__RemoveAllAnnotationsString);
	removeAllAnnotationsJMenuItem.addActionListener(this);
	popupMenu.add(removeAllAnnotationsJMenuItem);
	__annotationJList.addMouseListener(new MouseAdapter() {
	     public void mouseClicked(MouseEvent me) {
	         // if right mouse button clicked (or me.isPopupTrigger())
	         if ( SwingUtilities.isRightMouseButton(me)
	             //&& !__annotationJList.isSelectionEmpty()
	             //&& __annotationJList.locationToIndex(me.getPoint())
	             //== __annotationJList.getSelectedIndex()
	             	) {
	                 popupMenu.show(__annotationJList, me.getX(), me.getY());
	             }
	         }
	     }
	);

	checkVisibility();
}

/**
Handle action events.
*/
public void actionPerformed ( ActionEvent event )
{
	String action = event.getActionCommand();
	if ( action.equals(__RemoveAllAnnotationsString) ) {
		// Remove from the list and the original data that was passed in
		__annotationJListModel.clear();
		if ( __annotationDataList != null ) {
			if ( __geoView != null ) {
				__geoView.clearAnnotations(); // This will modify __annotationDataList
			}
		}
		checkVisibility();
	}
}

/**
Add an annotation to the list.
*/
public void addAnnotation ( GeoViewAnnotationData annotationData )
{
	// For now just add at the end...
	if ( annotationData != null ) {
		__annotationJListModel.addElement ( annotationData.getLabel() );
	}
	checkVisibility();
}

/**
Check the annotation list visibility.  If hideIfEmpty=true, then set to not visible if the list is
empty.
*/
private void checkVisibility ()
{
	if ( __hideIfEmpty && __annotationJListModel.size() == 0 ) {
		setVisible(false);
	}
	else {
		setVisible(true);
	}
}

/**
Set the annotation data and repopulate the list.
*/
public void setAnnotationData ( List<GeoViewAnnotationData> annotationDataList )
{
	__annotationDataList = annotationDataList;
	List<String> annotationLabelList = new Vector<String>(annotationDataList.size());
	for ( GeoViewAnnotationData annotationData : annotationDataList ) {
		annotationLabelList.add(annotationData.getLabel());
	}
	// Sort the array before adding
	Collections.sort(annotationLabelList);
	__annotationJListModel = new DefaultListModel();
	for ( String annotationLabel: annotationLabelList ) {
		__annotationJListModel.addElement(annotationLabel);
	}
	__annotationJList.setModel(__annotationJListModel);
	checkVisibility();
}

/**
Set the GeoView that is rendering the map.
*/
public void setGeoView ( GeoViewJComponent geoView )
{
	__geoView = geoView;
}

}