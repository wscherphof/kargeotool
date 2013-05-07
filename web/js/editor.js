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
 * Editor class is een alghele controller van de edit interface en delegeert
 * grote functionaliteiten naar andere classes, zoals de controle over OpenLayers,
 * ContextMenu, SearchManager, EditForms, Overview en ActiveRseqInfoPanel.
 * 
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
    helpPanel: null,
    overview: null,
    
    editForms: null,
    
    selectedObject:null,
    previousSelectedObject:null,
    
    currentEditAction: null,
    
    search:null,
    
    actionToCancel:null,
    
    changeManager:null,
    // === Initialisatie ===
    
    /**
     * @constructor
     */
    constructor: function(domId, mapfilePath, ovInfo) {

        this.mixins.observable.constructor.call(this, {
            listeners:{
                activeRseqChanged : function(rseq){
                    if(rseq){
                        this.loadAllRseqs(rseq.id);
                    }
                }
            //TODO wanneer het rseqopslaan klaar is, this.loadAllRseqs aanroepen voor de rseqlaag
            }
        });
        
        
        this.addEvents(
            'activeRseqChanged',
            'activeRseqUpdated',
            'selectedObjectChanged',
            'objectAdded',
            'movementAdded',
            'movementUpdated',
            'currentEditActionChanged'
        );
        
        this.domId = domId;
        
        this.helpPanel = Ext.create(HelpPanel, "rseqInfoPanel", this);
        this.editForms = Ext.create(EditForms, this);
        
        this.startLocationHash = this.parseLocationHash();
        
        this.createOpenLayersController(mapfilePath);   
        this.createOvInfoLayers(mapfilePath, ovInfo);
        
        this.overview = Ext.create(nl.b3p.kar.Overview,this, "rseqInfoPanel");
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
            };
        }
        this.loadAllRseqs();
        
        /* Indien de panelen die door layout.js zijn gemaakt worden geresized of
         * worden weggeklapt, informeer OpenLayers over de veranderde grootte.
         */
        var east = Ext.getCmp("east");
        var west = Ext.getCmp("west");
        east.on('resize', this.olc.resizeMap, this.olc);
        west.on('resize', this.olc.resizeMap, this.olc);
        east.on('collapse', this.olc.resizeMap, this.olc);
        west.on('collapse', this.olc.resizeMap, this.olc);
        east.on('expand', this.olc.resizeMap, this.olc);
        west.on('expand', this.olc.resizeMap, this.olc);
        
        this.on('activeRseqChanged', function(){
            var snapRoads = Ext.get("snapRoads");
            if(snapRoads.dom.checked){
                this.loadRoads();
            }
            this.handleSurroundingPoints();
        }, this);
      
        this.olc.map.events.register('moveend',this, function(){
            this.handleSurroundingPoints();
        });
        this.olc.on('measureChanged', function( length, unit){
            var measureIntField = Ext.get("measureInt");
            if(measureIntField ){
                measureIntField.setHTML(length + " " + unit);
            }
        },this);
        
        this.search = Ext.create(SearchManager,{searchField:'searchField',dom:'searchform',editor:this});
        this.search.on('searchResultClicked',this.searchResultClicked,this);
        this.changeManager = Ext.create(ChangeManager,this);
    },
    
    /**
     * Initialiseer de openlayers viewer.
     */
    createOpenLayersController: function() {
        this.olc = new ol(this);
        this.olc.createMap(this.domId);
        
        this.olc.addLayer("TMS","Luchtfoto",'http://luchtfoto.services.gbo-provincies.nl/tilecache/tilecache.aspx/','IPOlufo', getLayerVisibility("Luchtfoto"),'png?LAYERS=IPOlufo', getLayerOpacity("Luchtfoto"));
        this.olc.addLayer("TMS","BRT",'http://geodata.nationaalgeoregister.nl/tiles/service/tms/1.0.0','brtachtergrondkaart', getLayerVisibility("BRT"), 'png8', getLayerOpacity("BRT"));
        
        this.olc.map.events.register("moveend", this, this.updateCenterInLocationHash);
    },
    
    createOvInfoLayers: function(mapfilePath, ovInfos) {
        
        Ext.Array.each(ovInfos, function(ovInfo) {
            var bounds = new OpenLayers.Bounds(ovInfo.extent.xmin, ovInfo.extent.ymin, ovInfo.extent.xmax, ovInfo.extent.ymax);
            this.olc.addLayer("WMS","buslijnen_" + ovInfo.schema,Ext.String.format(mapfilePath,ovInfo.schema),'buslijnen', getLayerVisibility('buslijnen'),null, 1, 12, bounds);
            this.olc.addLayer("WMS","bushaltes_" + ovInfo.schema,Ext.String.format(mapfilePath,ovInfo.schema),'bushaltes', getLayerVisibility('bushaltes'),null, 1, 12, bounds);
        }, this);
    },
    
    setLayerOpacity: function(layer, opacity) {
        this.olc.map.getLayersByName(layer)[0].setOpacity(opacity);
    },
    
    /**
     * Maak het context menu
     */
    createContextMenu: function() {
        this.contextMenu = new ContextMenu(this);
        this.contextMenu.createMenus(this.domId);        
        
        this.olc.map.events.register("moveend", this, function() {
            this.contextMenu.deactivateContextMenu();
        });
    },
            
    // === Opstarten viewer ===
    
    /**
     * Parset de window.location.hash naar een object.
     * @return de location hash als object
     */
    parseLocationHash: function() {
        var hash = window.location.hash;
        hash = hash.charAt(0) == '#' ? hash.substring(1) : hash;
        return Ext.Object.fromQueryString(hash);
    },
    
    /**
     * Update de location hash met nieuwe waardes.
     * @param objToMerge het object dat moet worden gemerged met de waarden die reeds in de hash staan.
     */
    updateLocationHash: function(objToMerge) {
        var hash = this.parseLocationHash();
        window.location.hash = Ext.Object.toQueryString(Ext.Object.merge(hash, objToMerge));
    },
    
    /**
     * Plaats het huidige middelpunt van de map in de hash, inclusief het zoom niveau
     */
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
    
    // === Ajax calls ===
    
    /**
     * Laad de road side equipment. Wordt aangeroepen van uit de GUI.
     * @param query de query die gedaan moet worden.
     * @param successFunction functie die wordt aangeroepen nadat de rseq succesvol is geladen.
     */
    loadRseqInfo: function(query, successFunction) {
        var me = this;
        var doRequest = function(){
            Ext.Ajax.request({
                url: editorActionBeanUrl,
                method: 'GET',
                scope: me,
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
                        me.setActiveRseq(rseq);
                        if(successFunction) {
                            successFunction(rseq);
                        }
                    }else{
                        Ext.MessageBox.show({title: "Fout", msg: msg.error, buttons: Ext.MessageBox.OK, icon: Ext.MessageBox.ERROR});                    
                    }
                },
                failure: function (response){
                    Ext.MessageBox.show({title: "Ajax fout", msg: response.responseText, buttons: Ext.MessageBox.OK, icon: Ext.MessageBox.ERROR});                    
                }
            });
        };
        this.changeManager.rseqChanging(query.rseq, doRequest);
    },
    
    /**
     * Laad alle road side equipment.
     * @param id (optioneel) het id dat niet opgehaald moet worden
     */
    loadAllRseqs: function(id) {
        if(id == null || id == undefined || typeof id == "number"){
            Ext.Ajax.request({
                url:editorActionBeanUrl,
                method: 'GET',
                scope: this,
                params:  {
                    'allRseqJSON' : true,
                    rseq: id
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
                        Ext.MessageBox.show({title: "Fout", msg: msg.error, buttons: Ext.MessageBox.OK, icon: Ext.MessageBox.ERROR});                    
                    }
                },
                failure: function (response){
                    Ext.MessageBox.show({title: "Ajax fout", msg: response.responseText, buttons: Ext.MessageBox.OK, icon: Ext.MessageBox.ERROR});                    
                }
            });
        }
    },
    
    /**
     * Voert de Ajax call uit om de huidige Rseq op te slaan in de database.
     */
    saveOrUpdate: function(onSaved) {
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
                        this.changeManager.rseqSaved();
                        
                        var rseq = makeRseq(msg.roadsideEquipment);

                        // Dit misschien in listener
                        editor.olc.removeAllFeatures();
                        editor.olc.addFeatures(rseq.toGeoJSON());
                        this.setActiveRseq(rseq);
                        
                        if(onSaved) {
                            onSaved();
                        } else {
                            Ext.Msg.alert('Opgeslagen', 'Het verkeerssysteem is opgeslagen.');
                        }
                    }else{
                        Ext.Msg.alert('Fout', 'Fout bij opslaan: ' + msg.error);
                    }
                },
                failure: function (response){
                    Ext.Msg.alert('Fout', 'Kan gegevens niet opslaan!');
                }
            });
        }
    },
    
    exportXml: function() {
        var me = this;
        
        var exportIt = function() {
            window.open(exportActionBeanUrl + "?rseq=" + me.activeRseq.id, "exportwindow");
        };

        if(me.changeManager.changeDetected) {
            Ext.MessageBox.confirm(
                    "Opslaan verkeerssyteem",
                    "Alleen een opgeslagen verkeerssysteem kan worden gexporteerd. Wilt u nu opslaan?",
                    function (button){
                        if(button === 'yes') {
                            me.saveOrUpdate(exportIt);
                        }
                    }
            );
        } else {
            exportIt();
        }
    },
    
    removeRseq : function(){
        var rseq = this.activeRseq;
        if(rseq != null) {
            Ext.Ajax.request({
                url: editorActionBeanUrl,
                method: 'POST',
                scope: this,
                params: {
                    'removeRseq': true,
                    'rseq': rseq.getId()
                },
                success: function (response){
                    var msg = Ext.JSON.decode(response.responseText);
                    if(msg.success) {
                        this.changeManager.rseqSaved();
                        Ext.Msg.alert('Verwijderd', 'Het verkeerssysteem is verwijderd.');
                        
                        editor.olc.removeAllFeatures();
                        this.setActiveRseq(null);
                        
                    }else{
                        Ext.Msg.alert('Fout', 'Fout bij verwijderen: ' + msg.error);
                    }
                },
                failure: function (response){
                    Ext.Msg.alert('Fout', 'Kan roadside equipment niet verwijderen!');
                }
            });
        }
    },
    
    /**
     * Laad de wegen rondom de actieve rseq. Wordt gebruikt om de lijn bij het toevoegen van punten aan te snappen.
     */
    loadRoads : function(){
        if(this.activeRseq){
            Ext.Ajax.request({
                url:editorActionBeanUrl,
                method: 'GET',
                scope: this,
                params:  {
                    'roads' : true,
                    rseq: this.activeRseq.getId()
                },
                success: function (response){
                    var msg = Ext.JSON.decode(response.responseText);
                    if(msg.success){
                        var roads = msg.roads;
                        var featureCollection = {
                            type: "FeatureCollection",
                            features: roads
                        };

                        // Dit misschien in listener
                        this.olc.snapLayer.removeAllFeatures();
                        var features = this.olc.geojson_format.read(featureCollection);
                        this.olc.snapLayer.addFeatures(features);
                        this.olc.snap.activate();
                    }else{
                        Ext.MessageBox.show({title: "Fout: kan wegen niet laden", msg: msg.error, buttons: Ext.MessageBox.OK, icon: Ext.MessageBox.ERROR});                    
                    }
                },
                failure: function (response){
                    Ext.MessageBox.show({title: "Ajax fout", msg: response.responseText, buttons: Ext.MessageBox.OK, icon: Ext.MessageBox.ERROR});                    
                }
            });
        }
    },
    
    /**
     * Verwijder de snaplijnen
     */
    removeRoads : function(){
        this.olc.snapLayer.removeAllFeatures();
    },
    
    /**
     * Haal de punten binnen de extent (+buffer) op, minus de huidig geselecteerde RSEQ-punten. Ter referentie waar punten moeten komen.
     */
    handleSurroundingPoints : function(){
         if(this.activeRseq){
             var resLimit = 1;
             var resolution = this.olc.map.getResolution();
             var isNewRseq = typeof this.activeRseq.getId() != "number";
             if(resolution < resLimit && !isNewRseq){
                var extent = this.olc.map.getExtent().add(50,50).toGeometry().toString();
                Ext.Ajax.request({
                   url:editorActionBeanUrl,
                   method: 'GET',
                   scope: this,
                   params:  {
                       'surroundingPoints' : true,
                       rseq: this.activeRseq.getId(),
                       extent: extent
                   },
                   success: function (response){
                       var msg = Ext.JSON.decode(response.responseText);
                       if(msg.success){
                           var points = msg.points;
                           var geoJSON = new Array();
                           for (var i = 0 ; i < points.length ; i++){
                               var obj = Ext.create(Point, points[i]);
                               geoJSON.push(obj.toGeoJSON());
                           }
                           var featureCollection = {
                               type: "FeatureCollection",
                               features: geoJSON
                           };

                           this.olc.surroundingPointsLayer.removeAllFeatures();
                           var features = this.olc.geojson_format.read(featureCollection);
                           this.olc.surroundingPointsLayer.addFeatures(features);
                       }else{
                            Ext.MessageBox.show({title: "Fout: kan omliggende punten niet laden", msg: msg.error, buttons: Ext.MessageBox.OK, icon: Ext.MessageBox.ERROR});                    
                       }
                   },
                   failure: function (response){
                       Ext.MessageBox.show({title: "Ajax fout", msg: response.responseText, buttons: Ext.MessageBox.OK, icon: Ext.MessageBox.ERROR});                    
                   }
               });
             }else{
                 this.olc.surroundingPointsLayer.removeAllFeatures();
             }
        }
    },
    // === Edit functies ===
    
    /**
     * Laat de map zoomen naar de geactiveerde RoadSideEquipment.
     */
    zoomToActiveRseq: function() {
        if(this.activeRseq != null) {
            this.olc.map.setCenter(new OpenLayers.LonLat(
                this.activeRseq.location.coordinates[0],
                this.activeRseq.location.coordinates[1]), 
            14 /* TODO bepaal zoomniveau op basis van extent rseq location en alle point locations) */
            );            
        }
    },
     
    /**
     * Verander de actieve Rseq
     * @param rseq de nieuwe actieve Rseq
     */
    setActiveRseq: function (rseq){
        this.activeRseq = rseq;
        if(rseq){        
            this.olc.selectFeature(rseq.getId(),"RSEQ");
        }
        this.fireEvent('activeRseqChanged', this.activeRseq);
    },
    
    /**
     * Verander het geselecteerde object binnen de active Rseq
     * @param olFeature de OpenLayers feature;
     */
    setSelectedObject: function (olFeature) {
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
    
    /**
     * Wijzig het geselecteerde object. Opent een popup waarmee het actieve punt
     * kan worden gewijzigd
     */
    editSelectedObject: function() {
        if(this.selectedObject instanceof RSEQ) {
            this.editForms.editRseq();
        } else if(this.selectedObject instanceof Point) {
            
            var type = this.selectedObject.getType();
            
            if(type == null || type == "END" || type == "BEGIN") {
                this.editForms.editNonActivationPoint();
            } else {
                this.editForms.editActivationPoint();
            }
        } 
    },
    
    /**
     * Verander de geometry van het active Rseq of Point
     * @param className de className
     * @param id het id van het punt dat moet worden gewijzigd
     * @param x de nieuwe x coordinaat
     * @param y de nieuwe y coordinaat
     */
    changeGeom : function (className, id, x,y){
        if(className == "RSEQ"){
            this.activeRseq.location.coordinates = [x,y];
        }else{
            var point = this.activeRseq.getPointById(id);
            if(point){
                point.geometry.coordinates = [x,y];
            }
        }
        this.fireEvent("activeRseqUpdated", this.activeRseq);
    },
            
    /**
     * Reset het meten. Meet vanaf vorige punt
     */
    resetMeasure : function (){
        var lastPoint = this.olc.measureTool.handler.line.geometry.getVertices()[this.olc.measureTool.handler.line.geometry.getVertices().length-3];
        this.olc.line.deactivate();
        this.olc.drawLineFromPoint(lastPoint.x, lastPoint.y);
        this.olc.addMarker(lastPoint.x,lastPoint.y);
    },
    
    /**
     * Maak GeoJSON punt van x en y
     * @param x x coordinaat
     * @param y y coordinaat.
     */    
    createGeoJSONPoint: function(x, y) {
        return {
            type: "Point",
            coordinates: [x, y]
        };
    },
    
    /**
     * Voeg een Rseq toe
     * @param x x coordinaat
     * @param y y coordinaat
     */
    addRseq: function(x, y) {

        var newRseq = Ext.create("RSEQ", {
            location: this.createGeoJSONPoint(x, y),
            id: Ext.id(),
            type: ""
        });
            
        var me = this;
        this.editForms.editRseq(newRseq, function() {
            
            me.setActiveRseq(newRseq);
            me.selectedObject = newRseq;
            me.fireEvent("activeRseqUpdated", me.activeRseq);
        });
    },
    
    addMemo : function(){
        var memo = this.activeRseq.memo;
        var animId = this.olc.vectorLayer.getFeaturesByAttribute("className", "RSEQ")[0].geometry.id;
        Ext.Msg.show({
            title: 'Memo',
            msg: 'Voer een memo in:',
            width: 300,
            buttons: Ext.Msg.YESNOCANCEL,
            buttonText: {
                cancel: "Annuleren",
                no: "Verwijderen",
                yes: "Opslaan"
            },
            multiline: true,
            value: memo,
            fn: function(btn, text){
                if (btn == 'yes'){
                    this.activeRseq.memo = text;
                    this.fireEvent('activeRseqUpdated', this.activeRseq);
                }else if (btn == 'no') {
                    this.activeRseq.memo = '';
                    this.fireEvent('activeRseqUpdated', this.activeRseq);
                }
            },
            scope: this,
            animateTarget: animId,
            icon: Ext.window.MessageBox.INFO
        });
    },
    
    changeCurrentEditAction: function(action) {
        this.currentEditAction = action;
        this.fireEvent("currentEditActionChanged", action);
    },
    
    /**
     * Ga naar de modus dat een gebruiker een uitmeldpunt kan toevoegen aan de 
     * huidige rseq.
     * @param movementId (optioneel) Wanneer een movementId is opgegeven
     */
    addUitmeldpunt: function(movementId) {
        this.changeCurrentEditAction("ACTIVATION_2");
        
        var me = this;
        this.pointFinishedHandler = function(location) {

            var uitmeldpunt = Ext.create(Point, {
                type: "ACTIVATION_2",
                geometry: location
            });
            var distance = this.olc.measureTool.getBestLength( this.olc.vectorLayer.features[this.olc.vectorLayer.features.length-1].geometry);
            if(!distance){
                distance = new Array();
                distance[0] = 0;
            }
            var map = Ext.create(MovementActivationPoint, {
                beginEndOrActivation: "ACTIVATION",
                commandType: 2, 
                pointId: uitmeldpunt.getId(),
                distanceTillStopLine: distance[0].toFixed(0),
                vehicleTypes: [1,2,6,7,71]
            });
            
            me.editForms.editActivationPoint(uitmeldpunt, map, function() {
                
                if(movementId){
                    var currentUitmeldpunt = me.selectedObject.getId();
                    me.activeRseq.addUitmeldpuntToMovement(uitmeldpunt, map,movementId,currentUitmeldpunt);
                }else{
                    me.activeRseq.addUitmeldpunt(uitmeldpunt, map);
                }
                me.fireEvent("activeRseqUpdated", me.activeRseq);
                
            }, function() {
                me.fireEvent("activeRseqUpdated", me.activeRseq);
            });
        };
        
        this.addPoint(true);
    },
    
    selectExistingUitmeldpunt : function (movementId){
        this.changeCurrentEditAction("SELECT_EXISTING_UITMELDPUNT");
        this.contextMenu.showCancelSelecting(this.uitmeldpuntSelected);
        this.on('selectedObjectChanged',this.uitmeldpuntSelected,this,movementId);
    },
            
    uitmeldpuntSelected : function(uitmeldpunt,movementId){
        if(uitmeldpunt){
            
            var baseUitmeldpunt = this.selectedObject = this.previousSelectedObject;
            if(uitmeldpunt instanceof Point && uitmeldpunt.getType() == "ACTIVATION_2"){
                var destMovement = this.activeRseq.getMovementById(movementId);
                var mvmnts = this.activeRseq.findMovementsForPoint(uitmeldpunt);
                var srcMovement = mvmnts[0].movement;
                var map =  srcMovement.getMapForPoint(uitmeldpunt);
                destMovement.addMapAfter(map, baseUitmeldpunt.id);
                
                this.un('selectedObjectChanged',this.uitmeldpuntSelected,this);
                this.fireEvent("activeRseqUpdated", this.activeRseq);
            }
        }
    },
            
    addEindpunt: function() {
        this.changeCurrentEditAction("END");
        var me = this;
        var uitmeldpunt = this.selectedObject;
        this.pointFinishedHandler = function(location) {
            
            var eindpunt = Ext.create(Point, {
                type: "END",
                geometry: location                
            });
            
            me.editForms.editNonActivationPoint(eindpunt, function() {
                
                me.activeRseq.addEindpunt(uitmeldpunt, eindpunt, false);
                me.fireEvent("activeRseqUpdated", me.activeRseq);
                
            }, function() {
                me.fireEvent("activeRseqUpdated", me.activeRseq);
            });            
        };
        this.addPoint(true);        
    },
    
    /**
     * Selecteren bestaand eindpunt
     */
    selectEindpunt: function() {
        this.changeCurrentEditAction("SELECT_EINDPUNT");
        this.contextMenu.showCancelSelecting(this.eindpuntSelected);
        this.on('selectedObjectChanged',this.eindpuntSelected,this);
    },
    
    /**
     * Handler voor als een bestaand eindpunt is geselecteerd.
     */    
    eindpuntSelected: function(eindpunt) {
        if(eindpunt){
            var uitmeldpunt = this.selectedObject = this.previousSelectedObject;
            if(eindpunt instanceof Point && eindpunt.getType() == "END"){
                
                // TODO: Check of al gebruikt in movements voor uitmeldpunt
                
                var me = this;
                Ext.Msg.confirm(
                    'Eindpunt selecteren', 
                    'Wilt u eindpunt ' + eindpunt.getLabel() + " selecteren voor een beweging vanaf uitmeldpunt " + this.selectedObject.getLabel() + "?",
                    function(buttonId) {
                        if(buttonId == "yes") {
                            me.activeRseq.addEindpunt(uitmeldpunt, eindpunt, true);
                            me.fireEvent("activeRseqUpdated", me.activeRseq);
                        }
                    }
                    );
                
                this.un('selectedObjectChanged',this.eindpuntSelected,this);
                this.contextMenu.hideCancelSelecting();
            }else{
                Ext.Msg.alert("Kan punt niet selecteren", "Geselecteerd punt is geen eindpunt");
            }
            this.olc.selectFeature(this.selectedObject.getId(), "Point");
        }
    },
    
    /**
     * Ga naar de modus dat een gebruiker een inmeldpunt kan toevoegen aan de
     * movements voor het geselecteerde uitmeldpunt.
     */    
    addInmeldpunt: function() {
        this.changeCurrentEditAction("ACTIVATION_1");
        
        var me = this;
        var uitmeldpunt = this.selectedObject;        
        this.pointFinishedHandler = function(location) {

            var inmeldpunt = Ext.create(Point, {
                type: "ACTIVATION_1",
                geometry: location
            });
            var distance = this.olc.measureTool.getBestLength( this.olc.vectorLayer.features[this.olc.vectorLayer.features.length-1].geometry);
            if(!distance){
                distance = 0;
            }else{
                distance = parseInt(distance[0].toFixed(0));
            }
            var map = Ext.create(MovementActivationPoint, {
                beginEndOrActivation: "ACTIVATION",
                commandType: 1, 
                distanceTillStopLine:distance,
                pointId: inmeldpunt.getId()
            });
            
            me.editForms.editActivationPoint(inmeldpunt, map, function() {
                
                me.activeRseq.addInmeldpunt(uitmeldpunt, inmeldpunt, map, false);
                me.fireEvent("activeRseqUpdated", me.activeRseq);
                
            }, function() {
                me.fireEvent("activeRseqUpdated", me.activeRseq);
            });
        };
        
        this.addPoint(true);        
    },
    
    /**
     * Selecteren bestaand inmeldpunt
     */
    selectInmeldpunt: function() {
        this.changeCurrentEditAction("SELECT_INMELDPUNT");
        this.contextMenu.showCancelSelecting(this.inmeldpuntSelected);
        this.on('selectedObjectChanged',this.inmeldpuntSelected,this);
    },
    
    /**
     * Handler voor als een bestaand inmeldpunt is geselecteerd.
     */    
    inmeldpuntSelected: function(inmeldpunt) {
        if(inmeldpunt){
            
            var uitmeldpunt = this.selectedObject = this.previousSelectedObject;
            if(inmeldpunt instanceof Point && inmeldpunt.getType() == "ACTIVATION_1"){
                
                // TODO: Check of al gebruikt in movements voor uitmeldpunt
                
                var me = this;
                Ext.Msg.confirm(
                    'Inmeldpunt selecteren', 
                    'Wilt u inmeldpunt ' + inmeldpunt.getLabel() + " selecteren voor bewegingen naar uitmeldpunt " + this.selectedObject.getLabel() + "?",
                    function(buttonId) {
                        if(buttonId == "yes") {
                            var map = Ext.create(MovementActivationPoint, {
                                beginEndOrActivation: "ACTIVATION",
                                commandType: 1, 
                                pointId: inmeldpunt.getId()
                            });
                            me.activeRseq.addInmeldpunt(uitmeldpunt, inmeldpunt,map, true);
                            me.fireEvent("activeRseqUpdated", me.activeRseq);
                        }
                    },
                     me
                    );
                
                this.un('selectedObjectChanged',this.inmeldpuntSelected,this);
                this.contextMenu.hideCancelSelecting();
            }else{
                Ext.Msg.alert("Kan punt niet selecteren", "Geselecteerd punt is geen inmeldpunt");
            }
            this.olc.selectFeature(this.selectedObject.getId(), "Point");
        }
    },
    
    /**
     * Ga naar de modus dat een gebruiker een voorinmeldpunt kan toevoegen aan de
     * movements voor het geselecteerde uitmeldpunt.
     */    
    addVoorinmeldpunt: function() {
        this.changeCurrentEditAction("ACTIVATION_3");
        
        var me = this;

        var inmeldpunt = this.selectedObject; // kan ook voorinmeldpunt zijn
        
        this.pointFinishedHandler = function(location) {

            var voorinmeldpunt = Ext.create(Point, {
                type: "ACTIVATION_3",
                geometry: location
            });
            var mvmts = this.activeRseq.findMovementsForPoint( this.selectedObject);
            var inmeldMap = mvmts[0].map;
            var distanceMap = inmeldMap.distanceTillStopLine;
            var distance = this.olc.measureTool.getBestLength( this.olc.vectorLayer.features[this.olc.vectorLayer.features.length-1].geometry);
            if(!distance){
                distance = 0;
            }else{
                distance = parseInt(distance[0].toFixed(0));
            }
            if(!distanceMap){
                distanceMap =0;
            }else{
                distanceMap = parseInt(distanceMap);
            }
            distance += distanceMap;
            var map = Ext.create(MovementActivationPoint, {
                beginEndOrActivation: "ACTIVATION",
                commandType: 3, 
                pointId: voorinmeldpunt.getId(),
                distanceTillStopLine: distance
            });
            
            me.editForms.editActivationPoint(voorinmeldpunt, map, function() {
                
                me.activeRseq.addInmeldpunt(inmeldpunt, voorinmeldpunt, map, false, true);
                me.fireEvent("activeRseqUpdated", me.activeRseq);
                
            }, function() {
                me.fireEvent("activeRseqUpdated", me.activeRseq);
            });
        };
        
        this.addPoint(true);        
    },    
    
    /**
     * Ga naar de modus dat een gebruiker een beginpunt kan toevoegen aan de
     * movements voor het geselecteerde uitmeldpunt.
     */    
    addBeginpunt: function() {
        this.changeCurrentEditAction("BEGIN");
        
        var me = this;

        var uitmeldpunt = this.selectedObject;
        
        this.pointFinishedHandler = function(location) {

            var beginpunt = Ext.create(Point, {
                type: "BEGIN",
                geometry: location
            });
            
            me.editForms.editNonActivationPoint(beginpunt, function() {
                
                me.activeRseq.addBeginpunt(uitmeldpunt, beginpunt, false);
                me.fireEvent("activeRseqUpdated", me.activeRseq);
                
            }, function() {
                me.fireEvent("activeRseqUpdated", me.activeRseq);
            });
        };
        
        this.addPoint(true);        
    }, 
    
    /**
      * Ga naar punttoevoegen modus, optioneel met een lijn vanaf het gegeven punt.
      * @param withLine of vanaf het gegeven punt een lijn (met tussenpunten) moet worden getekend
      * @param piont punt van waar de lijn moet worden getekend indien withLine true is
      */
    addPoint: function(withLine, point) {
        if(withLine ){
            var geomName = this.selectedObject instanceof RSEQ ? "location" : "geometry";
            var startX = this.selectedObject[geomName].coordinates[0];
            var startY = this.selectedObject[geomName].coordinates[1];
            this.olc.drawLineFromPoint(startX,startY);
        }else{
            this.pointFinished(point);
        }
    },
    
    /**
     * Wordt aangeroepen door OpenLayersController indien de gebruiker een punt
     * heeft geplaatst door te dubbelklikken.
     */
    pointFinished: function(point) {
        var geom = {
            type: "Point",
            coordinates: [point.x,point.y]
        };

        this.changeCurrentEditAction(null);
        
        if(this.pointFinishedHandler) {
            this.pointFinishedHandler(geom);
        }
        this.olc.clearMarkers();
    },
    
    // ==== Search ==== ///
    searchResultClicked : function(searchResult){
        if(searchResult.getBounds() != null){
            var bounds = searchResult.getBounds();
            this.olc.map.zoomToExtent(bounds.toArray());
        }else if (searchResult.getX() != null && searchResult.getY() != null){
            this.olc.map.setCenter(searchResult.getLocation(), 12);
        }
        if(searchResult.getAddMarker()){
            this.olc.addMarker(searchResult.getLocation());
        }
    },  
    cancelSelection:function(){
        this.un('selectedObjectChanged',this.actionToCancel,this);
        this.changeCurrentEditAction(null);
    }
});

Ext.define("HelpPanel", {
    domId: null,
    editor: null,
    
    constructor: function(domId, editor) {
        this.domId = domId;
        this.editor = editor;
        editor.on("currentEditActionChanged", this.updateHelpPanel, this);
    },
    updateHelpPanel: function() {
        
        var action = this.editor.currentEditAction;
        var txt;
        switch(action) {
            case "ACTIVATION_1":
                txt = "Dubbelklik om het inmeldpunt te plaatsen voor signaalgroep(en) X. " +
                "<p>Met een enkele klik volgt u de buigpunten van de weg totaan de positie "+
                "van het inmeldpunt om de afstand te bepalen. " + 
                "<p>De afstand kan gemeten worden vanaf de stopstreep, door de stopstreep aan te klikken en dan rechtermuisknop <i>Meten vanaf vorig punt</i> te klikken. De afstand wordt dan berekend vanaf de stopstreep en ingevuld in het formulier."+
                "<p>Lengte <b><span id='measureInt'>0 m</span></b>";
                break;
            case "ACTIVATION_2":
                txt = "Dubbelklik om het uitmeldpunt te plaatsen."+
                "<p>De afstand kan gemeten worden vanaf de stopstreep, door de stopstreep aan te klikken en dan rechtermuisknop <i>Meten vanaf vorig punt</i> te klikken. De afstand wordt dan berekend vanaf de stopstreep en ingevuld in het formulier."+
                "<p>Lengte <b><span id='measureInt'>0 m</span></b>";
                break;
            case "ACTIVATION_3":
                txt = "Dubbelklik om het voorinmeldpunt te plaatsen voor signaalgroep(en) X." +
                "<p>Let op: De afstand vanaf de stopstreep tot inmeldpunt wordt opgeteld bij de afstand van inmeldpunt tot voorinmeldpunt."+
                "<p>Lengte <b><span id='measureInt'>0 m</span></b>";
                break;
            case "BEGIN":
                txt = "Dubbelklik om een beginpunt te plaatsen voor signaalgroep(en) "
                + "X" + ".";
                break;
            case "END":
                txt = "Dubbelklik om een eindpunt te plaatsen voor signaalgroep "
                + "X" + ".";
                break;
            case "MEASURE_STANDALONE":
                var length = this.editor.olc.standaloneMeasure.lastLength;
                txt = "De afstand is <b>" + length[0].toFixed(0) + ' ' + length[1] + '</b>. Druk op de lineaal om het meten te stoppen.';
                break;
            case "SELECT_EINDPUNT":
                txt = "Klik op een bestaand eindpunt (<img src='" +karTheme.eindPunt + "' width='20px'/>) om het te selecteren. Klik <a href='JavaScript: void(0);' onclick='editor.cancelSelection();'>hier</a> om deze actie te stoppen.";
                break;
            case "SELECT_INMELDPUNT":
                txt = "Klik op een bestaand inmeldpunt(<img src='" +karTheme.inmeldPunt+ "' width='20px'/>) om het te selecteren. Klik <a href='JavaScript: void(0);' onclick='editor.cancelSelection();'>hier</a> om deze actie te stoppen.";
                break;
            case "SELECT_EXISTING_UITMELDPUNT":
                txt = "Selecteer een bestaand uitmeldpunt";
                break;
            default:
                if(editor.activeRseq == null) {
                    txt = "Klik op een icoon van een verkeerssysteem om deze te selecteren" +
                "of klik rechts om een verkeerssysteem toe te voegen.";
                } else {
                    txt = "Klik rechts op het verkeerssysteem icoon om een uitmeldpunt voor " +
                "een signaalgroep toe te voegen of klik rechts op een punt om deze " +
                "te bewerken.";
                }
        }
        
        Ext.get("help").dom.innerHTML = txt;
    }
        
});
