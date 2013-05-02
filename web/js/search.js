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
        searchField:null,
        dom:null
    },
    searchEntities:null,
    searchBox:null,
    constructor: function(config) {
        this.initConfig(config);
        this.mixins.observable.constructor.call(this);  
        this.searchEntities = new Array();
        this.createForm();
        var geocoder = Ext.create(nl.b3p.kar.SearchGeocoder,{
            dom: this.dom
        });
        this.addSearchEntity(geocoder);
        
        var rseq = Ext.create(nl.b3p.kar.SearchRSEQ,{
            dom: this.dom
        });
        this.addSearchEntity(rseq);
        
        var road = Ext.create(nl.b3p.kar.SearchRoad,{
            dom: this.dom
        });
        this.addSearchEntity(road);
        
        var bus = Ext.create(nl.b3p.kar.SearchBusline,{
            dom: this.dom
        });
        this.addSearchEntity(bus);
        
        this.addEvents('searchResultClicked');
        
    },
    createForm : function(){
        Ext.create(Ext.panel.Panel,{
            renderTo:this.dom,
            border:false,
            layout:'hbox',
            items:[
            {
                xtype: 'textfield',
                id: 'searchField' ,
                width: 200,
                enableKeyEvents:true,
                listeners:{
                    keypress: {
                        fn: function(form,evt){
                            if(evt.getKey() == Ext.EventObject.ENTER){
                                this.search(form.value);
                            }
                        },
                        scope:this
                    }
                }
            },
            {
                xtype: 'button',
                text: "Zoek" ,
                listeners:{
                    click:{
                        fn: function(){
                            var term = Ext.getCmp('searchField').getValue();
                            this.search(term);
                        },
                        scope: this

                    }
                }
            }
            ]
        });
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
        dom:null
    },
    resultDom:null,
    container:null,
    title:null,
    category:null,
    constructor: function(config) {
        this.mixins.observable.constructor.call(this);  
        this.initConfig(config);
        
        this.dom = Ext.get(this.dom);
        this.container = document.createElement('div');
        this.container.setAttribute("id",this.category + "Container" + Ext.id());
        this.dom.appendChild(this.container);
        
        this.resultDom = document.createElement('div');
        this.resultDom.setAttribute("id",this.category + "results" + Ext.id());
        this.resultDom.innerHTML = "&nbsp;<br/>";
        
        this.title = document.createElement('div');
        this.title.setAttribute("id",this.category + "title" + Ext.id());
        this.title.innerHTML = "<b>" + this.category + "</b><br/>";
        
        this.container.appendChild(this.title);
        this.container.appendChild(this.resultDom);
        
        this.addEvents('searchResultClicked');
    },
    search: function(term){
        alert("Search called on superclas. Must be implemented.");
    },
    addResult: function (result){
        var resultblock = Ext.get(this.resultDom);
        resultblock.appendChild(result);
        var breakLine = document.createElement('br');
        resultblock.appendChild(breakLine);
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
        y:null,
        bounds:null,
        addMarker:null
    },
    constructor: function(config) {
        this.initConfig(config);
        if(this.x != null && this.y != null){
            this.location = new OpenLayers.LonLat(this.x, this.y);
        }
    }
});

/**
 * Implementatie van een Search class. Het implementeert een geocoder obv PDOK.
 *
 */
Ext.define("nl.b3p.kar.SearchGeocoder", {
    extend: "nl.b3p.kar.Search",

    category : "Adressen",
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
                        me.displayGeocodeResult( feature);
                    });
                } else {
                    resultblock.dom.innerHTML += "Geen resultaten gevonden.";
                }
            },
            failure: function() {
                Ext.get(this.resultDom).dom.innerHTML = "Geen resultaten gevonden.";
            }
        });
    },
    
    displayGeocodeResult: function( feature) {
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
        addresslink.className = '.resultlink';
        addresslink.innerHTML = Ext.util.Format.htmlEncode(label);
        var link = Ext.get(addresslink);
        var me = this;
        link.on('click', function() {
            var result = Ext.create(nl.b3p.kar.SearchResult,{
                x:feature.geometry.x, 
                y:feature.geometry.y,
                addMarker:true
            });
            me.fireEvent("searchResultClicked",result );
        });
        this.addResult(link);
    }
});

/**
 * Zoeken op RSEQs. Op dit moment wordt er door de beschrijving heen gezocht en op karAddress
 */
Ext.define("nl.b3p.kar.SearchRSEQ", {
    extend: "nl.b3p.kar.Search",
    category: "Verkeerssystemen",
    constructor: function(config) {
        this.callParent(arguments);
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
                            this.createResult(rseqs[i]);
                        }
                    }else{
                        Ext.get(this.resultDom).dom.innerHTML = "Geen resultaten gevonden.";
                    }
                }else{
                    Ext.MessageBox.show({title: "Fout", msg: msg.error, buttons: Ext.MessageBox.OK, icon: Ext.MessageBox.ERROR});                    
                }
            },
            failure: function() {
                Ext.MessageBox.show({title: "Ajax fout", msg: response.responseText, buttons: Ext.MessageBox.OK, icon: Ext.MessageBox.ERROR});                    
            }
        });
    },
    createResult : function (rseq){
        var label = rseq.properties.karAddress + " - " + rseq.properties.description;
        if( rseq.properties.crossingCode){
            label+= " (" + rseq.properties.crossingCode + ")";
        }
        var addresslink = document.createElement('a');
        addresslink.href = '#';
        addresslink.className = '.resultlink';
        addresslink.innerHTML = Ext.util.Format.htmlEncode(label);
        var link = Ext.get(addresslink);
        var me = this;
        link.on('click', function() {
            var result = Ext.create(nl.b3p.kar.SearchResult,{
                x:rseq.geometry.coordinates[0], 
                y:rseq.geometry.coordinates[1],
                addMarker:false
            });
            me.fireEvent("searchResultClicked",result );
            editor.loadRseqInfo({
                rseq:  rseq.properties.id
            });
        });
        this.addResult(link);
    }
});

/**
* Zoeken op wegen
*/
Ext.define("nl.b3p.kar.SearchRoad", {
    extend: "nl.b3p.kar.Search",
    category: "Wegen",
    constructor: function(config) {
        this.callParent(arguments);
    },
    search: function(term){
        Ext.get(this.resultDom).dom.innerHTML = "Zoeken...";
        var me = this;
        Ext.Ajax.request({
            url: searchActionBeanUrl,
            params: {
                'road': true,
                term: term
            },
            method: 'GET',
            scope:this,
            success: function(response) {
                var msg = Ext.JSON.decode(response.responseText);
                if(msg.success){
                    var roads = msg.roads;
                    if(roads.length > 0){
                        var resultblock = Ext.get(this.resultDom);
                        resultblock.dom.innerHTML = "";
                        for ( var i = 0 ; i < roads.length ; i++){
                            this.createResult(roads[i]);
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
    createResult : function (road){
        if(road.envelope){
            var label = road.weg;
            if(road.name){
                label += " - " + road.name;
            }
            var addresslink = document.createElement('a');
            addresslink.href = '#';
            addresslink.className = '.resultlink';
            addresslink.innerHTML = Ext.util.Format.htmlEncode(label);
            var link = Ext.get(addresslink);
            var me = this;
            link.on('click', function() {
                var env = road.envelope;
                var bounds = new OpenLayers.Bounds([env.minx, env.miny, env.maxx, env.maxy]);
                var location = bounds.getCenterLonLat();
                var result = Ext.create(nl.b3p.kar.SearchResult,{
                    bounds: bounds,
                    location: location,
                    addMarker:true
                });
                me.fireEvent("searchResultClicked",result );
                
            });
            
            this.addResult(link);
        }
    }
});


/**
* Zoeken op buslijnen. Zoekt op basis van het publicnumber (lijnnummer) en de name (naam van de lijn).
*/
Ext.define("nl.b3p.kar.SearchBusline", {
    extend: "nl.b3p.kar.Search",
    category: "Buslijnen",
    constructor: function(config) {
        this.callParent(arguments);
    },
    search: function(term){
        Ext.get(this.resultDom).dom.innerHTML = "Zoeken...";
        var me = this;
        Ext.Ajax.request({
            url: searchActionBeanUrl,
            params: {
                'busline': true,
                term: term
            },
            method: 'GET',
            scope:this,
            success: function(response) {
                var msg = Ext.JSON.decode(response.responseText);
                if(msg.success){
                    var buslines = msg.buslines;
                    if(buslines.length > 0){
                        var resultblock = Ext.get(this.resultDom);
                        resultblock.dom.innerHTML = "";
                        for ( var i = 0 ; i < buslines.length ; i++){
                            this.createResult(buslines[i]);
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
    createResult : function (busline){
        if(busline.envelope){
            var label = busline.publicnumber;
            if(busline.name){
                label += " - " + busline.name;
            }
            var addresslink = document.createElement('a');
            addresslink.href = '#';
            addresslink.className = '.resultlink';
            addresslink.innerHTML = Ext.util.Format.htmlEncode(label);
            var link = Ext.get(addresslink);
            var me = this;
            link.on('click', function() {
                var sld = sldActionBeanUrl + '?publicnumber=';
                sld+= busline.publicnumber;
                editor.olc.addSldToKargis(sld);
                var env = busline.envelope;
                var bounds = new OpenLayers.Bounds([env.minx, env.miny, env.maxx, env.maxy]);
                var location = bounds.getCenterLonLat();
                var result = Ext.create(nl.b3p.kar.SearchResult,{
                    bounds: bounds,
                    location: location,
                    addMarker:false
                });
                me.fireEvent("searchResultClicked",result );
                
            });
            
            this.addResult(link);
        }
    }
});