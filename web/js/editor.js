

var Editor = Ext.extend(Ext.util.Observable, {
    domId: null,
    olc: null,
    contextMenu: null,
    
    startLocationHash: null,
    
    activeRseq: null,
    selectedObject:null,
    
    constructor: function(domId, mapfilePath) {
        
        this.addEvents(
            'activeRseqChanged'
        );
        Editor.constructor.call(this);
        
        this.domId = domId;
        
        this.startLocationHash = this.parseLocationHash();
        
        this.createOpenLayersController(mapfilePath);   
        var haveCenterInHash = this.setCenterFromLocationHash();
        
        this.createContextMenu();
        
        if(this.startLocationHash.rseq) {
            this.loadRseqInfo({rseq: parseInt(this.startLocationHash.rseq)});

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
    },
    
    createOpenLayersController: function() {
        this.olc = new ol();
        this.olc.createMap(this.domId);
        
        this.olc.addLayer("TMS","BRT",'http://geodata.nationaalgeoregister.nl/tiles/service/tms/1.0.0','brtachtergrondkaart', true, 'png8');
        this.olc.addLayer("TMS","Luchtfoto",'http://luchtfoto.services.gbo-provincies.nl/tilecache/tilecache.aspx/','IPOlufo', false,'png?LAYERS=IPOlufo');
        this.olc.addLayer("WMS","walapparatuur",mapfilePath,'walapparatuur', false);
        this.olc.addLayer("WMS","signaalgroepen",mapfilePath,'signaalgroepen', false);
        //this.olc.addLayer("WMS","roadside_equipment2",mapfilePath,'roadside_equipment2', false);
        //this.olc.addLayer("WMS","activation_point2",mapfilePath,'activation_point2', false);
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
    loadRseqInfo: function(query) {
        
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
                    this.setActiveRseq(rseq);
                    
                    // Dit misschien in listener
                    editor.olc.removeAllFeatures();
                    editor.olc.addFeatures(rseq.toGeoJSON());
                }else{
                    alert("Ophalen resultaten mislukt.");
                }
            },
            failure: function (response){
                alert("Ophalen resultaten mislukt.");
            }
        });
    },
    setActiveRseq : function (rseq){
        this.activeRseq = rseq;
        var olFeature = this.olc.vectorLayer.getFeaturesByAttribute("id",rseq.getId())[0];
        if(olFeature){
            this.olc.selectCtrl.select(olFeature)
        }
        this.fireEvent('activeRseqChanged', this.activeRseq);
        console.log("activeRseq: ", rseq);
    },
    setSelectedObject : function (id){
        if(this.activeRseq){
            if(this.activeRseq.getId() == id){
                this.selectedObject = this.activeRseq;
            }else{
                var point = this.activeRseq.getPointById(id);
                if (point){
                    this.selectedObject = point;
                }else{
                    this.selectedObject = null;
                }
            }
        }else{
            this.selectedObject = null;
        }
    },
    
    editRseq: function() {
        var rseq = this.activeRseq;
        if(rseq == null) {
            console.log("editRseq() maar geen activeRseq!");
            return;
        }
        var type = {
            "": "nieuw verkeerssysteem",
            "CROSSING": "VRI",
            "GUARD": "bewakingssysteem nadering voertuig",
            "BAR": "afsluittingssysteem"
        };
        Ext.create('Ext.window.Window', {
            title: 'Bewerken ' + type[rseq.type] + (rseq.karAddress == null ? "" : " met KAR adres " + rseq.karAddress),
            height: 330,
            width: 450,
            icon: karTheme['vri'],
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
                    value: rseq.type,
                    layout: 'vbox',               
                    items: [{
                        name: 'type',
                        inputValue: 'CROSSING',
                        checked: rseq.type == 'CROSSING',
                        boxLabel: type['CROSSING']
                    },{
                        name: 'type',
                        inputValue: 'GUARD',
                        checked: rseq.type == 'GUARD',
                        boxLabel: type['GUARD']
                    },{
                        name: 'type',
                        inputValue: 'BAR',
                        checked: rseq.type == 'BAR',
                        boxLabel: type['BAR']
                    }]
                },{
                    fieldLabel: 'Beheerder',
                    name: 'dataOwner',
                    allowBlank: false,
                    value: rseq.dataOwner
                },{
                    fieldLabel: 'Beheerdersaanduiding',
                    name: 'crossingCode',
                    value: rseq.crossingCode
                },{
                    fieldLabel: 'KAR adres',
                    name: 'karAddress',
                    value: rseq.karAddress
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
                        Ext.Object.merge(rseq, this.up('form').getForm().getValues());
                        this.up('window').close();
                    }
                },{
                    text: 'Annuleren'
                }]
            }
        }).show();
    },
    
    addObject : function (className, location,properties){
        var geomName = "location";
        if(className != "RSEQ"){
            geomName = "geometry";
        }
        properties[geomName] = location;
        var newObject = Ext.create(className,properties);
        var geo = newObject.toGeoJSON();
        this.olc.addFeatures(geo);
        if(newObject.$className == "RSEQ"){
            this.setActiveRseq(newObject);
        }else{
            this.activeRseq.addPoint(newObject);
        }
    }
    
});
