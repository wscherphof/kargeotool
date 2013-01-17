

Ext.define("Editor", {
    domId: null,
    olc: null,
    contextMenu: null,
    
    startLocationHash: null,
    
    constructor: function(domId, mapfilePath) {
        this.domId = domId;
        
        this.startLocationHash = this.parseHash();
        
        this.createOpenLayersController(mapfilePath);   
        this.setCenterFromLocationHash();
        
        this.createContextMenu();
        
        if(this.startLocationHash.rseq) {
            this.loadRseqInfo(parseInt(this.startLocationHash.rseq));
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
        
        this.olc.map.events.on({
            "moveend": this.updateCenterInLocationHash,
            "zoomend": this.updateCenterInLocationHash,
            scope: this
        });        
    },
    
    createContextMenu: function() {
        this.contextMenu = new ContextMenu();
        this.contextMenu.createMenus(this.domId);        
        
        this.olc.map.events.register("moveend", this, function() {
            this.contextMenu.deactivateContextMenu();
        });
    },
    
    parseHash: function() {
        var hash = window.location.hash;
        hash = hash.charAt(0) == '#' ? hash.substring(1) : hash;
        return Ext.Object.fromQueryString(hash);
    },
    
    updateLocationHash: function(objToMerge) {
        var hash = this.parseHash();
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
        } else {
            this.olc.map.zoomToMaxExtent();
        }
    },
    
    /**
     * Called from GUI.
     */
    loadRseqInfo: function(id) {
        console.log("laad rseq met id " + id);
    }
    
});
