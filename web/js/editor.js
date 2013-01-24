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
Ext.define("Editor", {
    mixins: {
        observable: 'Ext.util.Observable'
    },
    
    domId: null,
    olc: null,
    contextMenu: null,
    
    startLocationHash: null,
    
    activeRseq: null,
    activeRseqInfoPanel: null,
    rseqEditWindow: null,
    
    pointEditWindow: null,
    activationPointEditWindow: null,
    
    selectedObject:null,
    previousSelectedObject:null,
    
    currentEditAction: null,
    
    constructor: function(domId, mapfilePath) {

        this.mixins.observable.constructor.call(this, {
            listeners:{
                 //TODO wanneer het rseqopslaan klaar is, this.loadAllRseqs aanroepen voor de rseqlaag
                 // TODO wanneer active rseq veranderd, loadAllRseqs doen, behalve actieve
                 
            }
        });
        
        this.addEvents(
            'activeRseqChanged',
            'activeRseqUpdated',
            'selectedObjectChanged',
            'objectAdded',
            'movementAdded',
            'movementUpdated'
            );
        
        this.domId = domId;
        
        this.activeRseqInfoPanel = Ext.create(ActiveRseqInfoPanel, "rseqInfoPanel", this);
        
        this.startLocationHash = this.parseLocationHash();
        
        this.createOpenLayersController(mapfilePath);   
        var haveCenterInHash = this.setCenterFromLocationHash();
        
        this.createContextMenu();
        
        if(this.startLocationHash.rseq) {
            this.loadRseqInfo({
                rseq: parseInt(this.startLocationHash.rseq)
                });

            // Toekomstige code voor aanroep met alleen rseq in hash zonder x,y,zoom
            var onRseqLoaded = function() {
                if(!haveCenterInHash) {
                    this.olc.map.setCenter(new OpenLayers.LonLat(
                        this.rseq.location.x,
                        this.rseq.location.y), 
                    14 /* bepaal zoomniveau op basis van extent rseq location en alle point locations) */
                    );
                }
            }
        }
        this.loadAllRseqs();
        
        var east = viewport.items.items[2];
        var west = viewport.items.items[3];
        east.on('resize', this.olc.resizeMap, this.olc);
        west.on('resize', this.olc.resizeMap, this.olc);
    },
    
    createOpenLayersController: function() {
        this.olc = new ol(this);
        this.olc.createMap(this.domId);
        
        this.olc.addLayer("TMS","BRT",'http://geodata.nationaalgeoregister.nl/tiles/service/tms/1.0.0','brtachtergrondkaart', true, 'png8');
        this.olc.addLayer("TMS","Luchtfoto",'http://luchtfoto.services.gbo-provincies.nl/tilecache/tilecache.aspx/','IPOlufo', false,'png?LAYERS=IPOlufo');
        this.olc.addLayer("WMS","walapparatuur",mapfilePath,'walapparatuur', false);
        this.olc.addLayer("WMS","signaalgroepen",mapfilePath,'signaalgroepen', false);
        //this.olc.addLayer("WMS","roadside_equipment2",mapfilePath,'roadside_equipment2', true);
        //this.olc.addLayer("WMS","activation_point2",mapfilePath,'activation_point2', true);
        this.olc.addLayer("WMS","triggerpunten",mapfilePath,'triggerpunten', false);
        this.olc.addLayer("WMS","buslijnen",mapfilePath,'buslijnen', false);
        this.olc.addLayer("WMS","bushaltes",mapfilePath,'bushaltes', false);
        
        this.olc.map.events.register("moveend", this, this.updateCenterInLocationHash);
    },
    
    createContextMenu: function() {
        this.contextMenu = new ContextMenu(this);
        this.contextMenu.createMenus(this.domId);        
        
        this.olc.map.events.register("moveend", this, function() {
            this.contextMenu.deactivateContextMenu();
        });
    },
    
    parseLocationHash: function() {
        var hash = window.location.hash;
        hash = hash.charAt(0) == '#' ? hash.substring(1) : hash;
        return Ext.Object.fromQueryString(hash);
    },
    
    updateLocationHash: function(objToMerge) {
        var hash = this.parseLocationHash();
        window.location.hash = Ext.Object.toQueryString(Ext.Object.merge(hash, objToMerge));
    },
    
    updateCenterInLocationHash: function() {
        this.updateLocationHash({
            x: this.olc.map.getCenter().lon,
            y: this.olc.map.getCenter().lat,
            zoom: this.olc.map.getZoom()
        });
    },          

    /**
     * Aanroepen na het toevoegen van layers. De window.location.hash is 
     * opgeslagen voordat deze na zoomend en moveend events is aangepast.
     */
    setCenterFromLocationHash: function() {
        var hash = this.startLocationHash;
        if(hash.x && hash.y && hash.zoom) {
            this.olc.map.setCenter(new OpenLayers.LonLat(hash.x, hash.y), hash.zoom);
            return true;
        } else {
            this.olc.map.zoomToMaxExtent();
            return true;
        }
    },
    
    /**
     * Called from GUI.
     */
    loadRseqInfo: function(query, successFunction) {
        // Clear huidige geselecteerde
        this.activeRseq = null;
        this.fireEvent('activeRseqChanged', this.activeRseq);
        
        Ext.Ajax.request({
            url:editorActionBeanUrl,
            method: 'GET',
            scope: this,
            params: Ext.Object.merge(query, {
                'rseqJSON' : true
            }),
            success: function (response){
                var msg = Ext.JSON.decode(response.responseText);
                if(msg.success){
                    var rJson = msg.roadsideEquipment;
                    var rseq = makeRseq(rJson);
                    
                    // Dit misschien in listener
                    editor.olc.removeAllFeatures();
                    editor.olc.addFeatures(rseq.toGeoJSON());
                    this.setActiveRseq(rseq);
                    if(successFunction) {
                        successFunction(rseq);
                    }
                }else{
                    alert("Ophalen resultaten mislukt.");
                }
            },
            failure: function (response){
                alert("Ophalen resultaten mislukt.");
            }
        });
    },
    loadAllRseqs : function(){
        Ext.Ajax.request({
            url:editorActionBeanUrl,
            method: 'GET',
            scope: this,
            params:  {
                'allRseqJSON' : true
            },
            success: function (response){
                var msg = Ext.JSON.decode(response.responseText);
                if(msg.success){
                    var rseqs = msg.rseqs;
                    var featureCollection = {
                        type: "FeatureCollection",
                        features: rseqs
                    };
                    
                    // Dit misschien in listener
                    editor.olc.removeAllRseqs();
                    editor.olc.addRseqs(featureCollection);
                }else{
                    alert("Ophalen resultaten mislukt.");
                }
            },
            failure: function (response){
                alert("Ophalen resultaten mislukt.");
            }
        });
    },
    saveOrUpdate: function() {
        var rseq = this.activeRseq;
        if(rseq != null) {
            Ext.Ajax.request({
                url: editorActionBeanUrl,
                method: 'POST',
                scope: this,
                params: {
                    'saveOrUpdateRseq': true,
                    'json': Ext.JSON.encode(editor.activeRseq.toJSON())
                },
                success: function (response){
                    var msg = Ext.JSON.decode(response.responseText);
                    if(msg.success) {
                        Ext.Msg.alert('Opgeslagen', 'Het verkeerssysteem is opgeslagen.')
                    }else{
                        Ext.Msg.alert('Fout', 'Fout bij opslaan: ' + msg.error)
                    }
                },
                failure: function (response){
                    Ext.Msg.alert('Fout', 'Kan gegevens niet opslaan!')
                }
            });
        }
    },
    
    zoomToActiveRseq: function() {
        if(this.activeRseq != null) {
            this.olc.map.setCenter(new OpenLayers.LonLat(
                this.activeRseq.location.coordinates[0],
                this.activeRseq.location.coordinates[1]), 
                14 /* TODO bepaal zoomniveau op basis van extent rseq location en alle point locations) */
            );            
        }
    },
    
    findMovementsForPoint: function(rseq, point) {
        var movements = [];
        
        Ext.Array.each(rseq.getMovements(), function(movement) {
            Ext.Array.each(movement.getMaps(), function(map) {
                if(map.pointId == point.id) {
                    movements.push({
                        movement: movement,
                        map: map
                    });
                }
            });
        });
        return movements;
    },
    addMovement: function (checkout, end){
        var mapEnd = Ext.create(MovementActivationPoint,{
            beginEndOrActivation:"END",
            pointId: end.getId()
        });
        var mapCheckout= Ext.create(MovementActivationPoint,{
            beginEndOrActivation:"END",
            pointId: checkout.getId()
        });
        var movement= Ext.create(Movement,{maps:[mapEnd,mapCheckout]});
        this.activeRseq.addMovement(movement);
        this.fireEvent('movementAdded', movement);
        
    },
    voegInmeldAanMovement : function(uitmeld, inmeld){
        var mvnts = this.findMovementsForPoint(this.activeRseq, uitmeld);
        for ( var i = 0 ; i < mvnts.length ; i++ ){
            var movement = mvnts[i].movement;
            var map = Ext.create(MovementActivationPoint,{
                pointId: inmeld.getId(),
                beginEndOrActivation: "ACTIVATION"
            });
            movement.addMap(map);
            this.fireEvent('movementUpdated', movement);
        }
    },
    
    setActiveRseq : function (rseq){
        this.activeRseq = rseq;
        this.olc.selectFeature(rseq.getId(),"RSEQ");
        this.fireEvent('activeRseqChanged', this.activeRseq);
        console.log("activeRseq: ", rseq);
    },
    setSelectedObject : function (olFeature){;
        if(!olFeature){
            if(this.selectedObject){
                this.previousSelectedObject = this.selectedObject;
            }
            this.selectedObject = null;
        }else{
            if(this.activeRseq){
                if(olFeature.data.className == "RSEQ"){
                    if(this.selectedObject){
                        this.previousSelectedObject = this.selectedObject;
                    }
                    this.selectedObject = this.activeRseq;
                }else { // Point
                    var point = this.activeRseq.getPointById(olFeature.data.id);
                    if (point){
                        if(this.selectedObject && this.selectedObject.getId() == olFeature.data.id ){ // Check if there are changes to the selectedObject. If not, then return
                            return;
                        }else{
                            if(this.selectedObject){
                                this.previousSelectedObject = this.selectedObject;
                            }
                            this.selectedObject = point;
                        }
                    }else{
                        alert("Selected object bestaat niet");
                    }
                }
                if(this.selectedObject){
                    this.olc.selectFeature(olFeature.data.id, olFeature.data.className);
                }
            }
        }
        this.fireEvent('selectedObjectChanged', this.selectedObject);
    },
    
    editSelectedObject: function() {
        if(this.selectedObject instanceof RSEQ) {
            this.editRseq();
        } else if(this.selectedObject instanceof Point) {
            
            var type = this.selectedObject.getType();
            
            if(type == null || type == "END") {
                this.editNonActivationPoint();
            } else {
                this.editActivationPoint();
            }
        } 
        
    },
    editRseq: function(newRseq, okHandler) {
        var rseq = newRseq || this.selectedObject;
        
        if(this.rseqEditWindow != null) {
            this.rseqEditWindow.destroy();
            this.rseqEditWindow = null;
        }   
        
        var type = {
            "": "nieuw verkeerssysteem",
            "CROSSING": "VRI",
            "GUARD": "bewakingssysteem nadering voertuig",
            "BAR": "afsluittingssysteem"
        };
        var me = this;
        var theType = rseq.type == "" ? "CROSSING" : rseq.type; // default voor nieuw
        me.rseqEditWindow = Ext.create('Ext.window.Window', {
            title: 'Bewerken ' + type[rseq.type] + (rseq.karAddress == null ? "" : " met KAR adres " + rseq.karAddress),
            height: 330,
            width: 450,
            icon: rseq.type == "" ? karTheme.crossing : karTheme[rseq.type.toLowerCase()],
            layout: 'fit',
            items: {  
                xtype: 'form',
                bodyStyle: 'padding: 5px 5px 0',
                fieldDefaults: {
                    msgTarget: 'side',
                    labelWidth: 150
                },
                defaultType: 'textfield',
                defaults: {
                    anchor: '100%'
                },
                items: [{
                    xtype: 'fieldcontainer',
                    fieldLabel: 'Soort verkeerssysteem',
                    defaultType: 'radiofield',
                    layout: 'vbox',               
                    items: [{
                        name: 'type',
                        inputValue: 'CROSSING',
                        checked: theType == 'CROSSING',
                        boxLabel: type['CROSSING']
                    },{
                        name: 'type',
                        inputValue: 'GUARD',
                        checked: theType == 'GUARD',
                        boxLabel: type['GUARD']
                    },{
                        name: 'type',
                        inputValue: 'BAR',
                        checked: theType == 'BAR',
                        boxLabel: type['BAR']
                    }]
                },{
                    xtype: 'combo',
                    fieldLabel: 'Beheerder',
                    name: 'dataOwner',
                    allowBlank: false,
                    blankText: 'Selecteer een optie',
                    editable: false,
                    displayField: 'omschrijving',
                    valueField: 'code',
                    value: rseq.dataOwner,
                    store: Ext.create('Ext.data.Store', {
                        fields: ['code', 'classificatie', 'companyNumber', 'omschrijving'],
                        data: dataOwners
                    })
                },{
                    fieldLabel: 'Beheerdersaanduiding',
                    name: 'crossingCode',
                    value: rseq.crossingCode
                },{
                    xtype: 'numberfield',
                    fieldLabel: 'KAR adres',
                    name: 'karAddress',
                    minValue: 0,
                    value: rseq.karAddress,
                    listeners: {
                        change: function(field, value) {
                            value = parseInt(value, 10);
                            field.setValue(value);
                        }
                    }                    
                },{
                    fieldLabel: 'Omschrijving',
                    name: 'description',
                    value: rseq.description,
                    allowBlank: false
                },{
                    fieldLabel: 'Plaats',
                    name: 'town',
                    value: rseq.town
                },{
                    xtype: 'datefield',
                    format: 'Y-m-d',
                    fieldLabel: 'Geldig vanaf',
                    name: 'validFrom',
                    value: rseq.validFrom
                },{
                    xtype: 'datefield',
                    format: 'Y-m-d',
                    fieldLabel: 'Geldig tot',
                    name: 'validUntil',
                    value: rseq.validUntil
                }],
                buttons: [{
                    text: 'OK',
                    handler: function() {
                        var form = this.up('form').getForm();
                        if(!form.isValid()) {
                            Ext.Msg.alert('Ongeldige gegevens', 'Controleer aub de geldigheid van de ingevulde gegevens.')
                            return;
                        }

                        Ext.Object.merge(rseq, form.getValues());
                        if(rseq == me.activeRseq) {
                            me.fireEvent("activeRseqUpdated", rseq);
                        }
                        me.rseqEditWindow.destroy();
                        me.rseqEditWindow = null;
                        
                        if(okHandler) {
                            okHandler(rseq);
                        }
                    }
                },{
                    text: 'Annuleren',
                    handler: function() {
                        me.rseqEditWindow.destroy();
                        me.rseqEditWindow = null;
                    }
                }]
            }
        }).show();
    },
    
    editNonActivationPoint: function() {
        var rseq = this.activeRseq;
        var point = this.selectedObject;
        
        if(this.pointEditWindow != null) {
            this.pointEditWindow.destroy();
            this.pointEditWindow = null;
        }   
        
        var me = this;
        var label = point.getLabel() == null ? "" : point.getLabel();
        me.pointEditWindow = Ext.create('Ext.window.Window', {
            title: 'Bewerken ' + (point.getType() == null ? "ongebruikt punt " : "eindpunt ") + label,
            height: 130,
            width: 250,
            icon: point.getType() == null ? karTheme.punt : (point.getType() == 'BEGIN' ? karTheme.beginPunt :karTheme.eindPunt),
            layout: 'fit',
            items: {  
                xtype: 'form',
                bodyStyle: 'padding: 5px 5px 0',
                fieldDefaults: {
                    msgTarget: 'side',
                    labelWidth: 150
                },
                defaultType: 'textfield',
                defaults: {
                    anchor: '100%'
                },
                items: [{
                    xtype: 'numberfield',                        
                    fieldLabel: 'Nummer',
                    name: 'nummer',
                    minValue: 0,                    
                    value: point.nummer,
                    listeners: {
                        change: function(field, value) {
                            value = parseInt(value, 10);
                            field.setValue(value);
                        }
                    }                      
                },{
                    fieldLabel: 'Label',
                    name: 'label',
                    value: point.label,
                    id: 'nummerEdit'
                }],
                buttons: [{
                    text: 'OK',
                    handler: function() {
                        var form = this.up('form').getForm();
                        if(!form.isValid()) {
                            Ext.Msg.alert('Ongeldige gegevens', 'Controleer aub de geldigheid van de ingevulde gegevens.')
                            return;
                        }
                        
                        // TODO check of nummer al gebruikt
                        
                        Ext.Object.merge(point, form.getValues());
                        if(rseq == me.activeRseq) {
                            me.fireEvent("activeRseqUpdated", rseq);
                        }
                        me.pointEditWindow.destroy();
                        me.pointEditWindow = null;
                    }
                },{
                    text: 'Annuleren',
                    handler: function() {
                        me.pointEditWindow.destroy();
                        me.pointEditWindow = null;
                    }
                }]
            }
        }).show();
        Ext.getCmp("nummerEdit").selectText(0);
        Ext.getCmp("nummerEdit").focus(false, 100);
    },
    
    editActivationPoint: function() {
        var rseq = this.activeRseq;
        var point = this.selectedObject;
        
        if(this.activationPointEditWindow != null) {
            this.activationPointEditWindow.destroy();
            this.activationPointEditWindow = null;
        }   
        
        var me = this;
        var label = point.getLabel() == null ? "" : point.getLabel();
        var apType = point.getType().split("_")[1];
        var apName = (apType == "1" ? "inmeld" : (apType == "2" ? "uitmeld" : "voorinmeld")) + "Punt";
        
        var movements = this.findMovementsForPoint(rseq, point);
        
        if(movements.length == 0) {
            alert("Kan geen movements vinden voor activation point!");
            return;
        }
        var map = movements[0].map;
        
        var editingUitmeldpunt = map.commandType == 2;
        
        var signalItems = [{
            xtype: 'numberfield',
            fieldLabel: 'Afstand tot stopstreep',
            name: 'distanceTillStopLine',
            value: map.distanceTillStopLine
        },{
            xtype: 'combo',
            fieldLabel: 'Triggertype',
            name: 'triggerType',
            allowBlank: false,
            blankText: 'Selecteer een optie',
            editable: false,
            displayField: 'desc',
            valueField: 'value',
            value: map.triggerType,
            store: Ext.create('Ext.data.Store', {
                fields: ['value', 'desc'],
                data: [
                    {value: 'STANDARD', desc: 'STANDARD: door vervoerder bepaald'},
                    {value: 'FORCED', desc: 'FORCED: altijd melding'},
                    {value: 'MANUAL', desc: 'MANUAL: handmatig door chauffeur'}
                ]
            })   
        }];
        if(editingUitmeldpunt) {
            Ext.define('VehicleType', {
                extend: 'Ext.data.Model',
                fields: [
                    {name: 'nummer', type: 'int'},
                    {name: 'omschrijving', type: 'string'}
                ]
            });

            var data = [];
            var selectedVehicleTypes = [];
            Ext.Array.each(vehicleTypes, function(vt) {
                var selected = Ext.Array.contains(map.vehicleTypes, vt.nummer);
                if(selected) {
                    selectedVehicleTypes.push(vt.nummer);
                }
                if(selected || vt.omschrijving.indexOf('Gereserveerd') == -1) {
                    data.push(vt);
                }
            });
            var vehicleTypesStore = Ext.create('Ext.data.Store', {proxy: 'memory', model: 'VehicleType', data: data});

            signalItems = Ext.Array.merge(signalItems, [{
                xtype: numberfield,
                minValue: 0,
                listeners: {
                    change: function(field, value) {
                        value = parseInt(value, 10);
                        field.setValue(value);
                    }
                },
                fieldLabel: 'Signaalgroep',
                name: 'signalGroupNumber',
                value: map.signalGroupNumber,
                disabled: map.commandType != 2
            },{
                fieldLabel: 'Virtual local loop number',
                name: 'virtualLocalLoopNumber',
                disabled: map.commandType != 2
            },{
                xtype: 'combo',
                multiSelect: true,
                disabled: map.commandType != 2,
                allowBlank: false,
                editable: false,
                blankText: 'Selecteer een optie',
                displayField: 'omschrijving',
                valueField: 'nummer',
                id: 'pietje',
                value:  selectedVehicleTypes,
                fieldLabel: 'Voertuigtypes',
                name: 'vehicleTypes',
                store: vehicleTypesStore
            }]);
        }
                
        me.activationPointEditWindow = Ext.create('Ext.window.Window', {
            title: 'Bewerken ' + apName.toLowerCase() + " " + label,
            height: map.commandType == 2 ? 300 : 225,
            width: 450,
            icon: karTheme[apName],
            layout: 'fit',
            items: {  
                xtype: 'form',
                bodyStyle: 'padding: 5px 5px 0',
                fieldDefaults: {
                    msgTarget: 'side',
                    labelWidth: 150
                },
                defaultType: 'textfield',
                defaults: {
                    anchor: '100%'
                },
                items: [{
                    xtype: 'numberfield',                            
                    fieldLabel: 'Nummer',
                    name: 'nummer',
                    value: point.nummer,
                    minValue: 0,
                    listeners: {
                        change: function(field, value) {
                            value = parseInt(value, 10);
                            field.setValue(value);
                        }
                    }                      
                },{
                    fieldLabel: 'Label',
                    name: 'label',
                    value: point.label,
                    id: 'nummerEdit'
                },{
                    xtype:'fieldset',
                    title: 'KAR signaal',
                    collapsible: false,
                    defaultType: 'textfield',
                    layout: 'anchor',
                    defaults: {
                        anchor: '100%'
                    },
                    items: signalItems
                }],
                buttons: [{
                    text: 'OK',
                    handler: function() {
                        var form = this.up('form').getForm();
                        if(!form.isValid()) {
                            Ext.Msg.alert('Ongeldige gegevens', 'Controleer aub de geldigheid van de ingevulde gegevens.')
                            return;
                        }
                        
                        // TODO check of nummer al gebruikt
                        
                        // merge nummer en label naar point
                        var formValues = form.getValues();
                        Ext.Object.merge(point, objectSubset(formValues, ["nummer", "label"]));

                        // deze waardes naar alle maps van movements die dit
                        // pointId gebruiken
                        var pointSignalValues = objectSubset(formValues, ["distanceTillStopLine", "triggerType"]);
                        
                        // Alleen bij uitmeldpund kunnen deze worden ingesteld
                        // maar moeten voor alle movements worden toegepast op
                        // alle signals
                        if(editingUitmeldpunt) {
                            allSignalValues = objectSubset(formValues, ["signalGroupNumber", "virtualLocalLoopNumber", "vehicleTypes"]);
                        }
                        
                        Ext.Array.each(movements, function(mvmtAndMap) {
                            console.log("movement nummer " + mvmtAndMap.movement.nummer);
                            if(mvmtAndMap.map) {
                                console.log("merging distanceTillStopLine and triggerType values to movement nummer " + mvmtAndMap.movement.nummer + " map pointId " + mvmtAndMap.map.pointId);
                                Ext.Object.merge(mvmtAndMap.map, pointSignalValues);
                            }
                            if(editingUitmeldpunt) {
                                // merge allSignalValues naar alle MovementActivationPoints
                                // van movements die dit pointId gebruiken
                                Ext.each(mvmtAndMap.movement.maps, function(theMap) {
                                    if(theMap.beginEndOrActivation == "ACTIVATION") {
                                        console.log("merging signalGroupNumber, virtualLocalLoopNumber and vehicleType values point signal values to movement nummer " + mvmtAndMap.movement.nummer + " map pointId " + theMap.pointId);
                                        Ext.Object.merge(theMap, allSignalValues);
                                    }
                                });
                            }
                        });
                        
                        if(rseq == me.activeRseq) {
                            me.fireEvent("activeRseqUpdated", rseq);
                        }
                        me.activationPointEditWindow.destroy();
                        me.activationPointEditWindow = null;
                    }
                },{
                    text: 'Annuleren',
                    handler: function() {
                        me.activationPointEditWindow.destroy();
                        me.activationPointEditWindow = null;
                    }
                }]
            }
        }).show();
        Ext.getCmp("nummerEdit").selectText(0);
        Ext.getCmp("nummerEdit").focus(false, 100);
    },    
    changeGeom : function (className, id, x,y){
        if(className == "RSEQ"){
            this.activeRseq.location.coordinates = [x,y];
        }else{
            var point = this.activeRseq.getPointById(id);
            if(point){
                point.geometry.coordinates = [x,y];
            }
        }
    },
    
    createGeoJSONPoint: function(x, y) {
        return {
            type: "Point",
            coordinates: [x, y]
        };
    },
    
    addRseq: function(x, y) {

        var newRseq = Ext.create("RSEQ", {
            location: this.createGeoJSONPoint(x, y),
            id: Ext.id(),
            type: ""
        });
            
        var me = this;
        this.editRseq(newRseq, function() {
            // Dit misschien in listener
            editor.olc.removeAllFeatures();
            editor.olc.addFeatures(newRseq.toGeoJSON());            
            
            me.setActiveRseq(newRseq);
        });
    },
    
    addObject : function (newObject){
        if(newObject instanceof RSEQ){
            this.setActiveRseq(newObject);
        }else{
            this.activeRseq.addPoint(newObject);
            if(newObject instanceof Point && newObject.getType() == "END" && this.selectedObject.getType() =="ACTIVATION_2"){
                this.addMovement(this.selectedObject, newObject);
            }
            if(newObject instanceof Point && newObject.getType() == "ACTIVATION_1" && this.selectedObject.getType() =="ACTIVATION_2"){
                this.voegInmeldAanMovement(this.selectedObject, newObject);
            }
        }
        this.fireEvent('objectAdded', newObject);
    },
    selectEindpunt : function (){
        this.on('selectedObjectChanged',this.eindpuntSelected,this);
    },
    eindpuntSelected : function (eindpunt){
        if(eindpunt){
            this.selectedObject = this.previousSelectedObject;
            if(eindpunt instanceof Point && eindpunt.getType() == "END"){
                this.addObject(eindpunt);
                this.un('selectedObjectChanged',this.eindpuntSelected,this);
            }else{
                alert("Geselecteerd punt is geen eindpunt");
            }
            this.olc.selectFeature(this.selectedObject.getId(), "Point");
        }
    },
    addEndpoint : function(withLine,point){
        this.currentEditAction = "END";
        this.addPoint(withLine,point);
    },
    addBeginpoint : function(withLine,point){
        this.currentEditAction = "BEGIN";
        this.addPoint(withLine,point);
    },
    addCheckinPoint : function(withLine,point){
        this.currentEditAction = "ACTIVATION_1";
        this.addPoint(withLine,point);
    },
    addCheckoutPoint : function(withLine,point){
        this.currentEditAction = "ACTIVATION_2";
        this.addPoint(withLine,point);
    },
    addPreCheckinPoint : function(withLine,point){
        this.currentEditAction = "ACTIVATION_3";
        this.addPoint(withLine,point);
    },
    addPoint : function(withLine,point){
        if(withLine ){
            var geomName = this.selectedObject instanceof RSEQ ? "location" : "geometry";
            var startX = this.selectedObject[geomName].coordinates[0];
            var startY = this.selectedObject[geomName].coordinates[1];
            this.olc.drawLineFromPoint(startX,startY);
        }else{
            this.pointFinished(point);
        }
    },
    pointFinished : function(point){
        var geom = {
            type: "Point",
            coordinates: [point.x,point.y]
        };
        var properties = {
            type: this.currentEditAction,
            id: Ext.id(),
            "geometry" : geom
        };
        
        var newObject = Ext.create("Point",properties);
        var geo = newObject.toGeoJSON();
        this.olc.addFeatures(geo);
        
        this.addObject(newObject);
        
        this.currentEditAction = null;
        
        this.olc.removeAllFeatures();
        this.olc.updateVectorLayer();
    },
        
    /**
     * Geocode search
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
                        link.on('click', function() {
                            me.olc.map.setCenter(new OpenLayers.LonLat(feature.geometry.x, feature.geometry.y), 12);
                        });
                        resultblock.appendChild(link);
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
    
    /**
     * Na klik op gevonden adres, zoom naar de lokatie ervan.
     */
    zoomToAddress: function(data) {
        
        var source = new Proj4js.Proj("EPSG:4236");
        var dest = new Proj4js.Proj("EPSG:28992");
        var point = new Proj4js.Point(data.geometry.location.lng,data.geometry.location.lat);
        Proj4js.transform(source,dest,point);

        this.olc.map.setCenter(new OpenLayers.LonLat(
            point.x,
            point.y), 
            14 
        );
    },
    
    parseAddressComponent: function(addressComponent) {
        var address = {
            street: '',
            zipcode: '',
            city: ''
        };
        Ext.Array.each(addressComponent, function(data) {
            if(Ext.Array.contains(data.types, "route")) address.street = data.long_name;
            if(Ext.Array.contains(data.types, "postcode_code")) address.zipcode = data.long_name;
            if(Ext.Array.contains(data.types, "locality")) address.city = data.long_name;
        });
        return address;
    },
    createLinkText: function(address) {
        var linktext = [];
        if(address.street !== '') linktext.push(address.street);
        if(address.zipcode !== '') linktext.push(address.zipcode);
        if(address.city !== '') linktext.push(address.city);
        return linktext.join(', ');
    }
});

Ext.define("ActiveRseqInfoPanel", {
    domId: null,
    editor: null,
    
    constructor: function(domId, editor) {
        this.domId = domId;
        this.editor = editor;
        editor.on("activeRseqChanged", this.updateRseqInfoPanel, this);
        editor.on("activeRseqUpdated", this.updateRseqInfoPanel, this);
    },
    
    updateRseqInfoPanel: function(rseq) {
        Ext.get("context_vri").setHTML(rseq == null ? "" : rseq.karAddress);
        
        Ext.get("rseqSave").setVisible(rseq != null);
    }
        
});