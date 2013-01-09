function ol (){
    this.map = null;
    this.panel = null;
    this.vectorLayer = null;
    this.gfi = null;
    this.dragFeature = null;
    this.point = null;
    this.line = null;
    this.identifyButton = null;
    this.overview = null;
    // Make the map
    this.createMap = function(domId){
        this.panel = new OpenLayers.Control.Panel();
        var maxBounds = new OpenLayers.Bounds(12000,304000,280000,620000);
        
        var opt = {
            projection: new OpenLayers.Projection("EPSG:28992"),
            maxExtent: maxBounds,
            srs: 'epsg:28992', 
            allOverlays: true,
            resolutions: [3440.64,1720.32,860.16,430.08,215.04,107.52,53.76,26.88,13.44,6.72,3.36,1.68,0.84,0.42,0.21],
            theme: OpenLayers._getScriptLocation()+'theme/b3p/style.css',
            units : 'm',
            controls : [this.panel]
        };
        
        this.map = new OpenLayers.Map(domId,opt);
        this.vectorLayer = new OpenLayers.Layer.Vector('edit',{
            geometryTypes : ["Point"]
        });
        this.map.addLayer(this.vectorLayer);
        this.createControls();
    },
    /**
     * Private nethod which adds all the controls
     */
    this.createControls = function (){
        var dg = new OpenLayers.Control.DragPan();
        var zb = new OpenLayers.Control.ZoomBox()
        this.panel.addControls(dg);
        this.panel.addControls(zb);  
        var nav = new OpenLayers.Control.Navigation({
            dragPan: dg,
            zoomBox: zb
        });
        dg.activate();
        this.map.addControl(nav);
        
        this.panel.addControls( new OpenLayers.Control.ZoomToMaxExtent()); 
        var navHist = new OpenLayers.Control.NavigationHistory();
        this.map.addControl(navHist);
        this.panel.addControls( navHist.previous);
        this.panel.addControls( navHist.next);
        this.map.addControl( new OpenLayers.Control.MousePosition({numDigits: 2}));        
        this.map.addControl(new OpenLayers.Control.PanZoomBar());
        
        var options = new Object();
        options["persist"]=true;
        options["callbacks"]={
            modify: function (evt){
                //make a tooltip with the measured length
                if (evt.parent){
                    var measureValueDiv=document.getElementById("olControlMeasureValue");
                    if (measureValueDiv==undefined){
                        measureValueDiv=document.createElement('div');
                        measureValueDiv.id="olControlMeasureValue";
                        measureValueDiv.style.position='absolute';
                        this.map.div.appendChild(measureValueDiv);
                        measureValueDiv.style.zIndex="10000";
                        measureValueDiv.className="olControlMaptip";
                        var measureValueText=document.createElement('div');
                        measureValueText.id='olControlMeasureValueText';
                        measureValueDiv.appendChild(measureValueText);
                    }
                    var px= this.map.getViewPortPxFromLonLat(new OpenLayers.LonLat(evt.x,evt.y));
                    measureValueDiv.style.top=px.y+"px";
                    measureValueDiv.style.left=px.x+25+'px'
                    measureValueDiv.style.display="block";
                    var measureValueText=document.getElementById('olControlMeasureValueText');
                    var bestLengthTokens=this.getBestLength(evt.parent);
                    measureValueText.innerHTML= bestLengthTokens[0].toFixed(3)+" "+bestLengthTokens[1];
                }
            }
        }

        var measureTool= new OpenLayers.Control.Measure( OpenLayers.Handler.Path, options);
        measureTool.events.register('measure',measureTool,function(){
            var measureValueDiv=document.getElementById("olControlMeasureValue");
            if (measureValueDiv){                
                measureValueDiv.style.display="none";
            }
            this.cancel();
        });
        measureTool.events.register('deactivate',measureTool,function(){
            var measureValueDiv=document.getElementById("olControlMeasureValue");
            if (measureValueDiv){
                measureValueDiv.style.display="none";
            }
        });
        this.panel.addControls (measureTool);
        this.gfi = new OpenLayers.Control.WMSGetFeatureInfo({
            drillDown: true,
            infoFormat: "application/vnd.ogc.gml"
        });
        this.gfi.events.register("getfeatureinfo",this,this.raiseOnDataEvent);
        this.map.addControl(this.gfi);
        
        var frameworkOptions = {
            displayClass: "olControlIdentify",
            type: OpenLayers.Control.TYPE_TOOL,
            title: "Selecteer een feature"
        };        
        this.identifyButton= new OpenLayers.Control(frameworkOptions);
        this.panel.addControls(this.identifyButton);
        
        this.identifyButton.events.register("activate",this,function(){
            this.gfi.activate();
        });
        this.identifyButton.events.register("deactivate",this,function(){
            this.gfi.deactivate();
        });
        
        var me = this;
        this.point =  new OpenLayers.Control.DrawFeature(this.vectorLayer, OpenLayers.Handler.Point, {
            displayClass: 'olControlDrawFeaturePoint',
            featureAdded: function(feature ) {
                me.drawFeature(feature);
            }
        });
        this.line = new OpenLayers.Control.DrawFeature(this.vectorLayer, OpenLayers.Handler.Path, {
            displayClass: 'olControlDrawFeaturePath',
            featureAdded: function (feature){
                me.drawFeature(feature);
            }
        });
        
        // The modifyfeature control allows us to edit and select features.
        this.dragFeature= new OpenLayers.Control.DragFeature(this.vectorLayer,{
            onComplete : this.dragComplete
        });
        
        this.map.addControl(this.point);
        this.map.addControl(this.line);
        this.map.addControl(this.dragFeature);
        
        var ovmLayer = new OpenLayers.Layer.TMS('BRTOverviewLayer', 'http://geodata.nationaalgeoregister.nl/tiles/service/tms/1.0.0',{
            layername:'brtachtergrondkaart', 
            type: 'png8',
            isBaseLayer:true,
            serverResolutions: [3440.64,1720.32,860.16,430.08,215.04,107.52,53.76],
            tileOrigin:new OpenLayers.LonLat(-285401.920000,22598.080000)
        });
        var maxBounds = new OpenLayers.Bounds(12000,304000,280000,620000);
        var ovEl = $('#overview');
        var height = ovEl.height() > 300 ? 300 : ovEl.height() ;
        this.overview = new OpenLayers.Control.OverviewMap({
            layers: [ovmLayer],
            mapOptions: {
                projection: new OpenLayers.Projection("EPSG:28992"),
                maxExtent: maxBounds,
                srs: 'epsg:28992', 
                resolutions: [3440.64,1720.32,860.16,430.08,215.04,107.52,53.76,26.88],
                theme: OpenLayers._getScriptLocation()+'theme/b3p/style.css',
                units : 'm'
            },
            size: new OpenLayers.Size( ovEl.width(),  height),
            div: ovEl[0]
        });
        this.map.addControl(this.overview);

        
    },
    this.raiseOnDataEvent = function(evt){
        var stub = new Object();          
        var walapparatuur = new Array();
        walapparatuur[0] = {
            id: "424"
        };
        
        stub.walapparatuur = walapparatuur;
        onIdentifyData("map_kar_layer",stub);
        this.identifyButton.deactivate();
    },
    /**
     * Add a layer. Assumed is that everything is in epsg:28992, units in meters and the maxextent is The Netherlands
     * @param type The type of the layer [WMS/TMS]
     * @param name The name of the layer
     * @param url The url to the service
     * @param layers The layers of the service which must be retrieved
     * @param visible Indicates whether or not the layer must be visible from start
     * @param extension Optional parameter to indicate the extension (type)
     */
    this.addLayer = function (type,name, url, layers,visible,extension){
        var layer;
        if(type == 'WMS'){
            layer = new OpenLayers.Layer.WMS(name,url,{
                'layers':layers,
                'transparent': true
            },{
                singleTile: true,
                ratio: 1,
                isBaseLayer: false,
                transitionEffect: 'resize'
            });
        }else if (type == "TMS" ){
            if(!extension){
                extension = 'png';
            }
            layer = new OpenLayers.Layer.TMS(name, url,{
                layername:layers, 
                type: extension,
                isBaseLayer:false,
                serverResolutions: [3440.64,1720.32,860.16,430.08,215.04,107.52,53.76,26.88,13.44,6.72,3.36,1.68,0.84,0.42,0.21],
                tileOrigin:new OpenLayers.LonLat(-285401.920000,22598.080000)
            });
        }else{
            console.log("Type " + type + " not known.");
        }
        if(layer){
            layer.setVisibility(visible);
            this.map.addLayer(layer);
            this.map.setLayerIndex(this.vectorLayer, this.map.getLayerIndex(layer)+1);
            this.map.zoomToMaxExtent();
        }
    },
    this.isLayerVisible = function (name){
        var lyrs = this.map.getLayersByName(name);
        if(lyrs && lyrs.length > 0){
            return lyrs[0].visibility;
        }
        return false;
    },
    this.setLayerVisible = function (name,vis){
        var lyrs = this.map.getLayersByName(name);
        if(lyrs && lyrs.length > 0){
            var layer = lyrs[0];
            layer.setVisibility(vis);
        }
    },
    this.zoomToExtent = function (minx,miny,maxx,maxy){
        this.map.zoomToExtent([minx,miny,maxx,maxy]);
    },
    this.update = function (){
        for ( var key in this.map.layers ){
            var layer = this.map.layers[key];
            layer.redraw(true);
        }
    },
    this.addSldToKargis = function (walsld,trigsld, signsld){
        var wal = this.map.getLayersByName("walapparatuur")[0];
        var trig = this.map.getLayersByName("triggerpunten")[0];
        var sign = this.map.getLayersByName("signaalgroepen")[0];
        wal.mergeNewParams({
            sld:walsld
        });
        trig.mergeNewParams({
            sld:trigsld
        });
        sign.mergeNewParams({
            sld:signsld
        });
    },
    this.removeSldFromKargis = function (){
        var wal = this.map.getLayersByName("walapparatuur")[0];
        var trig = this.map.getLayersByName("triggerpunten")[0];
        var sign = this.map.getLayersByName("signaalgroepen")[0];
        wal.mergeNewParams({
            sld:null
        });
        trig.mergeNewParams({
            sld:null
        });
        sign.mergeNewParams({
            sld:null
        });
        
    },
    /**
     * All the vectorlayer functions
     * 
     */
    this.drawPoint = function(wkt){
        if(wkt){
            var olFeature = new OpenLayers.Geometry.Point(wkt[0],wkt[1]);
            geometryDrawUpdate(olFeature.toString());
            this.point.drawFeature(olFeature);
        }else{
            this.point.activate();
        }
        this.dragFeature.activate();
    },
    this.drawLine = function(wkt){
        if(wkt){
            var olFeature = new OpenLayers.Geometry.fromWKT(wkt);
            this.line.drawFeature(olFeature);
        }else{
            this.line.activate();
        }
        this.dragFeature.activate();
    },
    this.removeAllFeatures = function(){
        this.vectorLayer.removeAllFeatures();
        this.dragFeature.deactivate();
    },
    this.dragComplete = function (feature){
        geometryDrawUpdate(feature.geometry.toString());
    },
    this.drawFeature = function (feature){
        geometryDrawUpdate (feature.geometry.toString());
        this.point.deactivate();
    }
    
}