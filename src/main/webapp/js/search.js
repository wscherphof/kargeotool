/* global Ext, searchActionBeanUrl, ovInfo, editor */

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
 * along witthis program. If not, see <http://www.gnu.org/licenses/>.
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
        dom:null,
        editor:null
    },
    searchEntities:null,
    searchBox:null,
    searchPanel:null,
    constructor: function(config) {
        this.initConfig(config);
        this.mixins.observable.constructor.call(this);
        this.searchEntities = new Array();
        this.createForm();
        var geocoder = Ext.create(nl.b3p.kar.SearchGeocoder,{editor:this.editor});
        this.addSearchEntity(geocoder);

        var rseq = Ext.create(nl.b3p.kar.SearchRSEQ,{editor:this.editor});
        this.addSearchEntity(rseq);

        var road = Ext.create(nl.b3p.kar.SearchRoad,{editor:this.editor});
        this.addSearchEntity(road);

        var bus = Ext.create(nl.b3p.kar.SearchBusline,{editor:this.editor});
        this.addSearchEntity(bus);

        //this.addEvents('searchResultClicked');

    },
    createForm : function(){
        var searchFormPanel = Ext.ComponentQuery.query('#searchformWindow')[0];
        var panel = Ext.create(Ext.panel.Panel,{
            border:false,
            layout:'hbox',
            items:[
            {
                xtype: 'textfield',
                id: 'searchField' ,
                fieldLabel: 'Zoekwoord',
                flex:1,
                enableKeyEvents:true,
                listeners:{
                    keypress: {
                        fn: function(form,evt){
                            if(evt.getKey() === Ext.EventObject.ENTER){
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
        searchFormPanel.add(panel);
        this.searchPanel = Ext.create('Ext.panel.Panel', {
            id: 'searchPanel',
            flex: 1,
            defaults: {
                // applied to each contained panel
                height:150
            },
            layout: {
                // layout-specific configs go here
                type: 'accordion',
                titleCollapse: true,
                animate: false,
                flex:1,
                height: 300,
                activeOnTop: true,
                multi:true,
                collapsed:true
            },
            items: [{
                xtype: 'panel', // << fake hidden panel
                hidden: true,
                collapsed: false
            }]
        });
        searchFormPanel.add(this.searchPanel);
    },
    search : function (term){
        Ext.each(this.searchEntities,function(searchEntity, index){
            searchEntity.panel.setLoading("Zoeken...");
            searchEntity.resultDom.innerHTML = "";
            searchEntity.search(term);
        });
    },
    searchResultClicked : function(result){
        this.fireEvent('searchResultClicked',result);
    },
    addSearchEntity : function (entity){
        this.searchEntities.push(entity);
        this.searchPanel.add(entity.getPanel());
        entity.on('searchResultClicked',this.searchResultClicked,this);
    },
    getTerm : function (){
        return Ext.getCmp("searchField").getValue();
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
        editor:null
    },
    resultDom:null,
    category:null,
    panel:null,
    constructor: function(config) {
        this.mixins.observable.constructor.call(this);
        this.initConfig(config);

        this.resultDom = document.createElement('div');
        this.resultDom.setAttribute("id",this.category + "results" + Ext.id());

        this.panel = Ext.create(Ext.panel.Panel,{
            id:this.category + "Container" + Ext.id(),
            title: this.category,
            collapsed:true,
            autoScroll:true,
            contentEl : this.resultDom,
            cls: 'search-accordion'
        });

      //  this.addEvents('searchResultClicked');
    },
    search: function(term){
        alert("Search called on superclas. Must be implemented.");
    },
    addResult: function (result){
        var resultblock = Ext.get(this.resultDom);
        resultblock.appendChild(result);
        var breakLine = document.createElement('br');
        resultblock.appendChild(breakLine);
    },
    getPanel : function(){
        return this.panel;
    },
    searchFinished:function(numResults,optionalText){
        this.panel.setTitle(this.category + " (" + numResults + ")");
        if(numResults === 0){
            var text = "Geen resultaten gevonden.";
            if(optionalText){
                text = optionalText;
            }
            Ext.get(this.resultDom).dom.innerHTML = text;
        }
        this.panel.setLoading(false);
        this.fireEvent('searchFinished', numResults, this);
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
        if(this.x !== null && this.y !== null){
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
                var res = Ext.JSON.decode(response.responseText);

                if(res) {
                    Ext.Array.each(res.results, function(result) {
                        me.displayGeocodeResult( result);
                    });
                    this.searchFinished(res.results.length);
                } else{
                    this.searchFinished(0);
                }
            },
            failure: function() {
                this.searchFinished(0, "Ophalen resultaten mislukt.");
            }
        });
    },

    displayGeocodeResult: function( feature) {     
        var addresslink = document.createElement('a');
        addresslink.href = '#';
        addresslink.className = '.resultlink';
        addresslink.innerHTML = Ext.util.Format.htmlEncode(feature.label);
        var link = Ext.get(addresslink);
        var me = this;
        var bounds = new OpenLayers.Bounds([feature.location.minx, feature.location.miny, feature.location.maxx, feature.location.maxy]);
                var location = bounds.getCenterLonLat();
        link.on('click', function() {
            var result = Ext.create(nl.b3p.kar.SearchResult,{
                bounds:bounds,
                location: location,
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
    vehicleType:null,
    category: "Verkeerssystemen",
    constructor: function(config) {
        this.callParent(arguments);
        var me = this;
        this.vehicleType =  Ext.create(Ext.form.ComboBox,{
            fieldLabel: 'Voertuigtype',
            id:Ext.id(),
            name: 'vehicleType',
            allowBlank: true,
            blankText: 'Selecteer een optie',
            displayField: 'title',
            editable:false,
            valueField: 'vehicleType',
            store: Ext.create('Ext.data.Store', {
                fields: ['title', 'vehicleType'],
                data:[{title:"Beide", vehicleType: "beide"},{title:"OV", vehicleType: "OV"},{title:"Hulpdiensten", vehicleType: "Hulpdiensten"}]
            }),
            listeners: {
                buffer: 50,
                change: function() {
                    if(this.isValid()){
                        var f = function(numResults, entity){
                            me.panel.expand();
                            me.panel.setLoading(false);
                            me.un('searchFinished',f,me);
                        };
                        me.on('searchFinished',f,me);
                        me.resultDom.innerHTML = "";
                        var term = me.editor.search.getTerm();
                        me.panel.setLoading("Zoeken..");
                        me.search(term);
                    }
                }
            }
        });
        this.panel.addDocked([this.vehicleType]);
    },
    search: function(term){
        var me = this;
        var params = {
            'rseq': true,
            term: term
        };
        if(this.vehicleType.isValid()){
            params.vehicleType = this.vehicleType.getValue();
        }
        Ext.Ajax.request({
            url: searchActionBeanUrl,
            params: params,
            method: 'GET',
            scope:this,
            success: function(response) {
                var msg = Ext.JSON.decode(response.responseText);
                if(msg.success){
                    var rseqs = msg.rseqs;
                    if(rseqs.length > 0){
                        for ( var i = 0 ; i < rseqs.length ; i++){
                            this.createResult(rseqs[i]);
                        }
                    }
                    this.searchFinished(rseqs.length);
                }else{
                    this.searchFinished(0,"Ophalen resultaten mislukt.");
                }
            },
            failure: function() {
                this.searchFinished(0, "Ophalen resultaten mislukt.");
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
                        for ( var i = 0 ; i < roads.length ; i++){
                            this.createResult(roads[i]);
                        }
                    }
                    this.searchFinished(roads.length);

                }else{
                    this.searchFinished(0,"Ophalen resultaten mislukt.");
                }
            },
            failure: function() {
                this.searchFinished(0,"Ophalen resultaten mislukt.");
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
    category: "OV-lijnen",
    beheerder:null,
    constructor: function(config) {
        this.callParent(arguments);
        var me = this;
        this.beheerder = Ext.create(Ext.form.ComboBox,{
            fieldLabel: 'Beheerder',
            id:Ext.id(),
            name: 'dataOwner',
            allowBlank: true,
            blankText: 'Selecteer een optie',
            displayField: 'omschrijving',
            editable:false,
            valueField: 'code',
            store: Ext.create('Ext.data.Store', {
                fields: ['omschrijving','code', 'id'],
                data: dataOwners
            }),
            listeners: {
                buffer: 50,
                change: function() {
                    if(this.isValid()){
                        var f = function(numResults, entity){
                            me.panel.expand();
                            me.panel.setLoading(false);
                            me.un('searchFinished',f,me);
                        };
                        me.on('searchFinished',f,me);
                        me.resultDom.innerHTML = "";
                        var term = editor.search.getTerm();
                        me.panel.setLoading("Zoeken..");
                        me.search(term);
                    }
                }
            }
        });
        this.panel.addDocked([this.beheerder]);
    },
    search: function(term){
        var me = this;
        var params = {
            'busline': true,
            term: term
        };
        if(this.beheerder.isValid()){
            params.dataOwner = this.beheerder.getValue();
        }
        Ext.Ajax.request({
            url: searchActionBeanUrl,
            params: params,
            method: 'GET',
            scope:this,
            success: function(response) {
                var msg = Ext.JSON.decode(response.responseText);
                if(msg.success){
                    var schemas = msg.buslines;
                    if(schemas.length > 0){
                        var totalLines = 0;
                        for(var j = 0 ; j < schemas.length ; j++ ){
                            var schema = schemas[j];
                            var buslines = schema.lines;
                            totalLines += schema.lines.length;
                            for ( var i = 0 ; i < buslines.length ; i++){
                                this.createResult(buslines[i].dataowner,buslines[i], schema.schema);
                            }
                        }
                        this.searchFinished(totalLines);
                    }else{
                        this.searchFinished(0);
                    }
                }else{
                    this.searchFinished(0,"Ophalen resultaten mislukt.");
                }
            },
            failure: function() {
                this.searchFinished(0,"Ophalen resultaten mislukt.");
            }
        });
    },
    createResult : function (company,busline,schema){
        if(busline.envelope){
            var label = company + ": " + busline.publicnumber+ " - " +busline.name;
            var addresslink = document.createElement('a');
            addresslink.href = '#';
            addresslink.className = '.resultlink';
            addresslink.innerHTML = Ext.util.Format.htmlEncode(label);
            var link = Ext.get(addresslink);
            var me = this;
            link.on('click', function() {
                var env = busline.envelope;
                var bounds = new OpenLayers.Bounds([env.minx, env.miny, env.maxx, env.maxy]);
                var location = bounds.getCenterLonLat();
                var result = Ext.create(nl.b3p.kar.SearchResult,{
                    bounds: bounds,
                    location: location,
                    addMarker:false
                });


                var layerName = "buslijnen_" +schema;

                Ext.Array.each(ovInfo, function(ov) {
                    var currentName = "buslijnen_" + ov.schema;
                    var visible = false;
                    if(currentName === layerName){
                        visible = true;
                    }
                    editor.olc.setLayerVisible(currentName, visible);
                });

                editor.olc.addFilterToKargis(busline.publicnumber,layerName);
                Ext.getCmp("buslijnen_filter").setText('Verwijder filter \'' + busline.publicnumber + '\'');
                Ext.getCmp("buslijnen_filter").setHidden(false);


                me.fireEvent("searchResultClicked",result );

            });

            this.addResult(link);
        }
    }
});