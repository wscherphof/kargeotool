function ol (){
    this.map = null;
    this.panel = null;
    this.createMap = function(domId){
        this.panel = new OpenLayers.Control.Panel();
        var maxBounds = new OpenLayers.Bounds(12000,304000,280000,620000);
        
        var opt = {
            projection: new OpenLayers.Projection("EPSG:28992"),
            maxExtent: maxBounds,
            srs: 'epsg:28992', 
            allOverlays: true,
            theme: OpenLayers._getScriptLocation()+'theme/b3p/style.css',
            units : 'meters',
            controls : [new OpenLayers.Control.PanZoomBar(), new OpenLayers.Control.Navigation(), this.panel]
        };
        
        this.map = new OpenLayers.Map(domId,opt);
        this.panel.addControls(new OpenLayers.Control.DragPan()); 
        this.panel.addControls(new OpenLayers.Control.ZoomBox()); 
        this.panel.addControls( new OpenLayers.Control.ZoomToMaxExtent()); 
        var navHist = new OpenLayers.Control.NavigationHistory();
        this.map.addControl(navHist);
        this.panel.addControls( navHist.previous);
        this.panel.addControls( navHist.next);
        this.map.addControl( new OpenLayers.Control.MousePosition({numDigits: 2}));
       
    },
    this.addLayer = function (name, url, layers){
         var wms = new OpenLayers.Layer.WMS(
            "OpenLayers WMS",
            "http://x13.b3p.nl/cgi-bin/mapserv?map=/home/matthijsln/geo-ov/transmodel_connexxion_edit.map",
            {
                'layers':'buslijnen,bushaltes,triggerpunten,walapparatuur,signaalgroepen',
                'transparent': true
            },
            {
                singleTile: true,
                ratio: 1,
                transitionEffect: 'resize'
            }
        );
        this.map.addLayer(wms);
        this.map.zoomToMaxExtent();
    }
}