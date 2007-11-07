// ----------------------------------------------------------------------------
// TSProductAnnotationProvider - class that provides annotations of a certain
//	type to a TSProduct.
// ----------------------------------------------------------------------------
// History:
//
// 2005-10-18	J. Thomas Sapienza, RTi	Initial version.
// ----------------------------------------------------------------------------

package RTi.GRTS;

import java.util.Vector;

import RTi.Util.IO.PropList;

/**
Interface for a class that provides annotations to a TSProduct graph.  
*/
public interface TSProductAnnotationProvider {

/**
Adds a type of annotation provided by this class to the internal Vector of 
annotation types provided.  This Vector is returned via 
getAnnotationProviderChoices().
@param name the name of a type of annotation provided by this class.
*/
public void addAnnotationProvider(String name);

/**
The method called when annotations are added to a product.  Classes must 
add the annotations when this method is called -- it is the only notification
they will receive that annotations are to be added to a TSProduct.<p>
<b>Note:</b>  Annotations are added after references to time series are 
determined because time series information may be needed by the 
annotation provider.
@param product the product to which to add annotations.
@param controlProps further properties that can be used to specify additional
data to the annotation provider about the annotations it will provide.
@throws Exception if there is an error adding annotations to the product.
*/
public void addAnnotations(TSProduct product, PropList controlProps)
throws Exception;

/**
Returns a Vector of Strings, each of which is one of the types of annotation
that is provided by this class.
@return a Vector of the annotations that this class provides.
*/
public Vector getAnnotationProviderChoices();

/**
Returns true if this class provides the given annotation type, false if not.
@param name the name of an annotation type.
@return true if this class provides the given type, false if not.
*/
public boolean provides(String name);

}
