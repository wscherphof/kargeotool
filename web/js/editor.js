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
 * Editor class. 
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
    
    editForms: null,
    
    selectedObject:null,
    previousSelectedObject:null,
    
    currentEditAction: null,
    /**
     *@constructor
     */
    constructor: function(domId, mapfilePath) {

        this.mixins.observable.constructor.call(this, {
            listeners:{
                activeRseqChanged : function(rseq){
                    this.loadAllRseqs(rseq.karAddress);
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
            'movementUpdated'
            );
        
        this.domId = domId;
        
        this.activeRseqInfoPanel = Ext.create(ActiveRseqInfoPanel, "rseqInfoPanel", this);
        this.editForms = Ext.create(EditForms, this);
        
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
    /**
     * Initieer de openlayers viewer.
     */
    createOpenLayersController: function() {
        this.olc = new ol(this);
        this.olc.createMap(this.domId);
        
        this.olc.addLayer("TMS","BRT",'http://geodata.nationaalgeoregister.nl/tiles/service/tms/1.0.0','brtachtergrondkaart', true, 'png8');
        this.olc.addLayer("TMS","Luchtfoto",'http://luchtfoto.services.gbo-provincies.nl/tilecache/tilecache.aspx/','IPOlufo', false,'png?LAYERS=IPOlufo');
        this.olc.addLayer("WMS","buslijnen",mapfilePath,'buslijnen', false);
        this.olc.addLayer("WMS","bushaltes",mapfilePath,'bushaltes', false);
        
        this.olc.map.events.register("moveend", this, this.updateCenterInLocationHash);
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
    /**
     * Haalt de window.location.hash op.
     * @return de location hash als object
     */
    parseLocationHash: function() {
        var hash = window.location.hash;
        hash = hash.charAt(0) == '#' ? hash.substring(1) : hash;
        return Ext.Object.fromQueryString(hash);
    },
    /**
     *Update de location hash.
     *@param objToMerge het object dat moet worden gemerged met de waarden die reeds in de hash staan.
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
     * @return true;
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
     * Laad de road side equipment. Wordt aangeroepen van uit de GUI.
     * @param query de query die gedaan moet worden.
     * @param successFunction functie die wordt aangeroepen nadat de rseq succesvol is geladen.
     */
    loadRseqInfo: function(query, successFunction) {
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
                    this.fireEvent('activeRseqChanged', this.activeRseq);
                }else{
                    alert("Ophalen resultaten mislukt.");
                }
            },
            failure: function (response){
                alert("Ophalen resultaten mislukt.");
            }
        });
    },
    /**
     * Laad alle road side equipment.
     * @param karAddress (optioneel) het kar adres
     */
    loadAllRseqs : function(karAddress){
        Ext.Ajax.request({
            url:editorActionBeanUrl,
            method: 'GET',
            scope: this,
            params:  {
                'allRseqJSON' : true,
                karAddress: karAddress
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
    /**
     * Functie voor het opslaan/updaten van de ingevoerde data.
     */
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
    /**
     * Laat de map zoomen naar de geactiveerde Road Side Equipment.
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
     * Haal alle movements op op basis het id van de putn
     * @param rseq het rseq object waar in gezocht moet worden
     * @param point het punt waarvoor de movements moeten worden opgehaald.
     * @return een array van movements.
     */
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
    /**
     * Voegt een movement toe.
     * @param checkout de checkout waar het over gaat
     * @param end het eind punt.
     */
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
    /**
     * Voeg een inmeld punt toe aan de movement van een uitmeld punt.
     */
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
    /**
     * verander de actieve Rseq
     * @param rseq de nieuwe actieve Rseq
     */
    setActiveRseq : function (rseq){
        this.activeRseq = rseq;
        this.olc.selectFeature(rseq.getId(),"RSEQ");
        this.fireEvent('activeRseqChanged', this.activeRseq);
        //console.log("activeRseq: ", rseq);
    },
    /**
     * verander het geselecteerde object
     * @param olFeature de OpenLayers feature;
     */
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
    /**
     * Wijzig het geselecteerde object. Opent een popup waarmee het actieve punt
     * kan worden gewijzigd
     */
    editSelectedObject: function() {
        if(this.selectedObject instanceof RSEQ) {
            this.editForms.editRseq();
        } else if(this.selectedObject instanceof Point) {
            
            var type = this.selectedObject.getType();
            
            if(type == null || type == "END") {
                this.editForms.editNonActivationPoint();
            } else {
                this.editForms.editActivationPoint();
            }
        } 
    },
    
    /**
     * Verander de geometry van het active Rseq
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
            // Dit misschien in listener
            editor.olc.removeAllFeatures();
            editor.olc.addFeatures(newRseq.toGeoJSON());            
            
            me.setActiveRseq(newRseq);
        });
    },
    /**
     * Voeg een nieuw object toe
     * @param newObject Nieuw object
     */
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
    /**
     * Handler voor als een eindpunt is geselecteerd.
     */    
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
    /**
     * Voeg een eind punt toe aan een lijn
     * @param withLine de lijn waar aan het punt moet worden toegevoegd
     * @param piont het toe te voegen punt
     */
    addEndpoint : function(withLine,point){
        this.currentEditAction = "END";
        this.addPoint(withLine,point);
    },
    
    /**
     * Voeg een begin punt toe aan een lijn
     * @param withLine de lijn waar aan het punt moet worden toegevoegd
     * @param piont het toe te voegen punt
     */
    addBeginpoint : function(withLine,point){
        this.currentEditAction = "BEGIN";
        this.addPoint(withLine,point);
    },    
    /**
     * Voeg een in check punt toe aan een lijn
     * @param withLine de lijn waar aan het punt moet worden toegevoegd
     * @param piont het toe te voegen punt
     */
    addCheckinPoint : function(withLine,point){
        this.currentEditAction = "ACTIVATION_1";
        this.addPoint(withLine,point);
    },    
    /**
     * Voeg een uit check toe aan een lijn
     * @param withLine de lijn waar aan het punt moet worden toegevoegd
     * @param piont het toe te voegen punt
     */
    addCheckoutPoint : function(withLine,point){
        this.currentEditAction = "ACTIVATION_2";
        this.addPoint(withLine,point);
    },    
    /**
     * Voeg een voor check in punt toe aan een lijn
     * @param withLine de lijn waar aan het punt moet worden toegevoegd
     * @param piont het toe te voegen punt
     */
    addPreCheckinPoint : function(withLine,point){
        this.currentEditAction = "ACTIVATION_3";
        this.addPoint(withLine,point);
    },
     /**
     * Voeg een punt toe aan een lijn
     * @param withLine de lijn waar aan het punt moet worden toegevoegd
     * @param piont het toe te voegen punt
     */
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
