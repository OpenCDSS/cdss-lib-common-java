// GeoLayerGradutedSymbol - spatial data layer graduated symbol

/* NoticeStart

GeoView Java Library
GeoView Java Library is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 2017-2024 Open Water Foundation

GeoView Java Library is free software:  you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

GeoView Java Library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

You should have received a copy of the GNU General Public License
    along with GeoView Java Library.  If not, see <https://www.gnu.org/licenses/>.

NoticeEnd */

package org.openwaterfoundation.geoprocessor.core;

import java.util.SortedMap;

/**
 * Layer symbol.
 */
public class GeoLayerGraduatedSymbol extends GeoLayerSymbol {

	/**
	 * Constructor.
	 * @param name symbol name
	 * @param description symbol description
	 * @param properties properties for the symbol
	 */
	public GeoLayerGraduatedSymbol ( String name, String description, SortedMap<String,Object> properties ) {
		super ( name, description, properties );
		setClassificationType ( GeoLayerSymbolClassificationType.GRADUATED);
	}
}