

Ext.define("Editor", {
    domId: null,
    olc: null,
    contextMenu: null,
    
    constructor: function(domId, mapfilePath) {
        this.domId = domId;
        this.createOpenLayersController(mapfilePath);    
        this.createContextMenu();
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
               
        if(!this.olc.setCenterFromHash()) {
            this.olc.map.zoomToMaxExtent();
        }
    },
    
    createContextMenu: function() {
        this.contextMenu = new ContextMenu();
        this.contextMenu.createMenus(this.domId);        
        
        this.olc.map.events.register("moveend", this, function() {
            this.contextMenu.deactivateContextMenu();
        });
    },
    
    /**
     * Called from GUI.
     */
    loadRseqInfo: function(id) {
        
    }
    
});
