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
 * Searchmanager managet alle zoekingangen. Dit is de klasse waar alle verzoeken binnenkomen en uitgaan.
 */
Ext.define("SearchManager", {
    mixins: {
        observable: 'Ext.util.Observable'
    },
    config:{
      searchField:null  
    },
    searchEntities:null,
    constructor: function() {
        this.searchEntities = new Array();
        this.mixins.observable.constructor.call(this);  
        var geocoder = Ext.create(nl.b3p.kar.SearchGeocoder,{
            resultDom: "geocoderesults",
            category: "geocoder"
        });
        this.addSearchEntity(geocoder);
        var rseq = Ext.create(nl.b3p.kar.SearchRSEQ,{
            resultDom: "rseqresults",
            category: "rseq"
        });
        this.addSearchEntity(rseq);
        
        this.addEvents('searchResultClicked');
        
        Ext.get('searchField').on('keypress', function(evt,form){
            if(evt.getKey() == Ext.EventObject.ENTER){
                this.search(form.value);
            }
        },this);
    },
    search : function (term){
        Ext.each(this.searchEntities,function(searchEntity, index){
            searchEntity.search(term);
        });
    },
    searchResultClicked : function(result){
        this.fireEvent('searchResultClicked',result);
    },
    addSearchEntity : function (entity){
        this.searchEntities.push(entity);
        entity.on('searchResultClicked',this.searchResultClicked,this);
    }
});

/**
 * Superclass voor zoekingangen. Elke zoekingang moet hier minimaal aan voldoen. 
 */
Ext.define("nl.b3p.kar.Search", {
    mixins: {
        observable: 'Ext.util.Observable'
    },
    config:{
        resultDom:null,
        category:null
    },
    constructor: function(config) {
        this.mixins.observable.constructor.call(this);  
        this.initConfig(config);
        
        this.addEvents('searchResultClicked');
    },
    search: function(term){
        alert("Search called on superclas. Must be implemented.");
    }
});

/**
 * Het generieke antwoord dat een zoekingang teruggeeft. 
 * 
 */
Ext.define("nl.b3p.kar.SearchResult", {
    config:{
        location:null,
        x:null,
        y:null
    },
    constructor: function(config) {
        this.initConfig(config);
        this.location = new OpenLayers.LonLat(this.x, this.y);
    }
});

/**
 * Implementatie van een Search class. Het implementeert een geocoder obv PDOK.
 *
 */
Ext.define("nl.b3p.kar.SearchGeocoder", {
    extend: "nl.b3p.kar.Search",
    constructor: function() {
        this.callParent(arguments);
    },
    /**
     * Do a geocoding search and display the results.
     */
    search: function(address) {
        Ext.get(this.resultDom).dom.innerHTML = "Zoeken...";
        var me = this;
        Ext.Ajax.request({
            url: searchActionBeanUrl,
            params: {
                'term': address,
                geocode:true
            },
            method: 'GET',
            scope:this,
            success: function(response) {
                var results = new OpenLayers.Format.XLS().read(response.responseXML);
                
                var resultblock = Ext.get(this.resultDom);
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
                Ext.get(this.resultDom).dom.innerHTML = "Geen resultaten gevonden.";
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
            var result = Ext.create(nl.b3p.kar.SearchResult,{
                x:feature.geometry.x, 
                y:feature.geometry.y
            });
            me.fireEvent("searchResultClicked",result );
        });
        element.appendChild(link);
    }
});

/**
 * Zoeken op RSEQs. Op dit moment wordt er door de beschrijving heen gezocht en op karAddress
 */
Ext.define("nl.b3p.kar.SearchRSEQ", {
    extend: "nl.b3p.kar.Search",
    constructor: function(config) {
        this.mixins.observable.constructor.call(this);  
        this.initConfig(config);
        
        this.addEvents('searchResultClicked');
    },
    search: function(term){
        Ext.get(this.resultDom).dom.innerHTML = "Zoeken...";
        var me = this;
        Ext.Ajax.request({
            url: searchActionBeanUrl,
            params: {
                'rseq': true,
                term: term
            },
            method: 'GET',
            scope:this,
            success: function(response) {
                var msg = Ext.JSON.decode(response.responseText);
                if(msg.success){
                    var rseqs = msg.rseqs;
                    if(rseqs.length > 0){
                        var resultblock = Ext.get(this.resultDom);
                        resultblock.dom.innerHTML = "";
                        for ( var i = 0 ; i < rseqs.length ; i++){
                            this.createResult(rseqs[i], resultblock);
                        }
                    }else{
                        Ext.get(this.resultDom).dom.innerHTML = "Geen resultaten gevonden.";
                    }
                }else{
                    alert("Ophalen resultaten mislukt.");
                }
            },
            failure: function() {
                Ext.get(this.resultDom).dom.innerHTML = "Geen resultaten gevonden.";
            }
        });
    },
    createResult : function (rseq, element){
        var label = rseq.properties.karAddress + " - " + rseq.properties.description;
        var addresslink = document.createElement('a');
        addresslink.href = '#';
        addresslink.className = 'geocoderesultlink';
        addresslink.innerHTML = Ext.util.Format.htmlEncode(label);
        var link = Ext.get(addresslink);
        var me = this;
        link.on('click', function() {
            var result = Ext.create(nl.b3p.kar.SearchResult,{
                x:rseq.geometry.coordinates[0], 
                y:rseq.geometry.coordinates[1]
            });
            me.fireEvent("searchResultClicked",result );
        });
        element.appendChild(link);
    }
});