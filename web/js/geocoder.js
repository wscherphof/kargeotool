/**
* Geo-OV - applicatie voor het registreren van KAR meldpunten
*
* Copyright (C) 2009-2013 B3Partners B.V.
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU Affero General Public License as
* published by the Free Software Foundation, either version 3 of the
* License, or (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU Affero General Public License for more details.
*
* You should have received a copy of the GNU Affero General Public License
* along with this program. If not, see <http://www.gnu.org/licenses/>.
*/

/**
 * Geocoder die resultaten weergeeft in de HTML pagina
 */
Ext.define("Geocoder", {
    mixins: {
        observable: 'Ext.util.Observable'
    },
    
    constructor: function() {
        this.mixins.observable.constructor.call(this);  
        
        this.addEvents("geocodeResultClick");
    },

    /**
     * Do a geocoding search and display the results.
     */
    geocode: function(address) {
        Ext.get('geocoderesults').dom.innerHTML = "Zoeken...";
        var me = this;
        Ext.Ajax.request({
            url: geocoderActionBeanUrl,
            params: {
                'search': address
            },
            method: 'GET',
            success: function(response) {
                var results = new OpenLayers.Format.XLS().read(response.responseXML);
                
                var resultblock = Ext.get('geocoderesults');
                resultblock.dom.innerHTML = "";
                
                var rl = results.responseLists[0];
                
                if(rl) {
                    Ext.Array.each(rl.features, function(feature) {
                        me.displayGeocodeResult(resultblock, feature);
                    });
                } else {
                    resultblock.dom.innerHTML = "Geen resultaten gevonden.";
                }
            },
            failure: function() {
                Ext.get('geocoderesults').dom.innerHTML = "Geen resultaten gevonden.";
            }
        });
    },
    
    displayGeocodeResult: function(element, feature) {
        var address = feature.attributes.address;

        var number = address.building && address.building.number ?
            " " + address.building.number : "";
        var label = address.street != "" ? address.street + number : "";
        if(address.postalCode != undefined) {
            label += (label != "" ? ", " : "") + address.postalCode;
        }
        // woonplaats
        if(address.place.MunicipalitySubdivision != undefined) {
            label += (label != "" ? ", " : "") + address.place.MunicipalitySubdivision;
        }
        // gemeente
        if(address.place.Municipality != undefined && address.place.Municipality != address.place.MunicipalitySubdivision) {
            label += (label != "" ? ", " : "") + address.place.Municipality;
        }
        // provincie
        if(label == "" && address.place.CountrySubdivision != undefined) {
            label = address.place.CountrySubdivision;
        }

        var addresslink = document.createElement('a');
        addresslink.href = '#';
        addresslink.className = 'geocoderesultlink';
        addresslink.innerHTML = Ext.util.Format.htmlEncode(label);
        var link = Ext.get(addresslink);
        var me = this;
        link.on('click', function() {
            me.fireEvent("geocodeResultClick", feature.geometry.x, feature.geometry.y);
        });
        element.appendChild(link);
    }
});
