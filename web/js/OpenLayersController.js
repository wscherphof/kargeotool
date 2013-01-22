Ext.define("ol", {
    editor : null,
    map : null,
    panel : null,
    vectorLayer : null,
    geojson_format : null,
    gfi : null,
    dragFeature : null,
    point : null,
    line : null,
    identifyButton : null,
    overview : null,
    activeFeature : null,
    selectCtrl : null,
    constructor : function(editor){
        this.editor = editor;
        this.editor.on('activeRseqUpdated', this.updateVectorLayer, this);
        this.editor.on('selectedObjectChanged', this.toggleDragfeature, this);
    },
    // Make the map
    createMap : function(domId){
        
        this.panel = new OpenLayers.Control.Panel();
        var maxBounds = new OpenLayers.Bounds(12000,304000,280000,620000);
                
        var opt = {
            projection: new OpenLayers.Projection("EPSG:28992"),
            maxExtent: maxBounds,
            srs: 'epsg:28992', 
            allOverlays: true,
            resolutions: [3440.64,1720.32,860.16,430.08,215.04,107.52,53.76,26.88,13.44,6.72,3.36,1.68,0.84,0.42,0.21,0.105,0.0525],
            theme: OpenLayers._getScriptLocation()+'theme/b3p/style.css',
            units : 'm',
            controls : [this.panel]
        };
        
        this.map = new OpenLayers.Map(domId,opt);
        this.vectorLayer = new OpenLayers.Layer.Vector("Points", {
            styleMap: new OpenLayers.StyleMap( {
                "default": style,
                "select": selectstyle,
                "temporary" : tempstyle
            })
        }
        );
            
        this.geojson_format = new OpenLayers.Format.GeoJSON();
        this.map.addLayer(this.vectorLayer);
        this.createControls(domId);
        
        OpenLayers.IMAGE_RELOAD_ATTEMPTS = 2;
        OpenLayers.Util.onImageLoadErrorColor = "transparent"; 
    },
    /**
     * Private nethod which adds all the controls
     */
    createControls : function (domId){
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
        this.map.addControl( new OpenLayers.Control.MousePosition({
            numDigits: 2
        }));        
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
            //drillDown: true,
            url: "localhost:8084/geo-ov/action/viewer/editor?gfi=true",
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
            displayClass: 'olControlDrawFeaturePath'
        });
        this.line.events.register('featureadded', me, me.drawFeature);
        
        this.dragFeature= new OpenLayers.Control.DragFeature(this.vectorLayer,{
            onComplete : me.dragComplete,
            featureCallbacks:{
                over: function(feature){
                    if(editor.selectedObject && feature.data.id == editor.selectedObject.getId()){
                        this.overFeature(feature);
                    }
                }
            }
        });
        
        this.dragFeature.handlers['drag'].stopDown = false;
        this.dragFeature.handlers['drag'].stopUp = false;
        this.dragFeature.handlers['drag'].stopClick = false;
        this.dragFeature.handlers['feature'].stopDown = false;
        this.dragFeature.handlers['feature'].stopUp = false;
        this.dragFeature.handlers['feature'].stopClick = false;

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
        
        this.overview = new OpenLayers.Control.OverviewMap({
            layers: [ovmLayer],
            mapOptions: {
                projection: new OpenLayers.Projection("EPSG:28992"),
                maxExtent: maxBounds,
                srs: 'epsg:28992', 
                resolutions: [3440.64,1720.32,860.16,430.08,215.04,107.52,53.76,26.88],
                theme: OpenLayers._getScriptLocation()+'theme/b3p/style.css',
                units : 'm'
            }
        });
        this.map.addControl(this.overview);
           
        var oClick = new OpenLayers.Control.Click({
            rightclick: function (evt){
                var x = evt.clientX;
                var y = evt.clientY;
                editor.contextMenu.show(x,y);
           
                return false;
            },
            click: function (evt){
                editor.contextMenu.deactivateContextMenu();
            },
            includeXY:true
        });
        
        this.map.addControl(oClick);
        oClick.activate();
        
        var highlight = new OpenLayers.Control.SelectFeature(this.vectorLayer, {
            highlightOnly: true,
            renderIntent: "temporary",
            hover:true
        });
        
        this.map.addControl(highlight);
        highlight.activate();
        this.selectCtrl = new OpenLayers.Control.SelectFeature(this.vectorLayer,{
            clickout: true,
            scope:this,
            onSelect : function (feature){
                editor.setSelectedObject(feature);
            },
            onUnselect : function(){
                editor.setSelectedObject(null);
            }
        });
        this.map.addControl(this.selectCtrl);
        this.selectCtrl.activate();
    },
    updateVectorLayer : function(){
        this.vectorLayer.removeAllFeatures();
        var geoJson = this.editor.activeRseq.toGeoJSON();
        this.addFeatures(geoJson);
        var selected = this.editor.selectedObject;
        this.selectFeature(selected.getId(), selected.$className);
        
    },
    selectFeature : function(id,className){
        var olFeature = null;
        if(className=="RSEQ"){
            olFeature = this.vectorLayer.getFeaturesByAttribute("className",className)[0];
        }else{
            // Haal alle features op voor het id: dit kunnen punten en een rseq zijn
            var all =this.vectorLayer.getFeaturesByAttribute("id",id);
            for(var i = 0 ; i < all.length ;i++){
                var f = all[i];
                if(f.data.className == "Point"){
                    // Eerste zal altijd de goede zijn vanwege serial id in db
                    olFeature = f;
                    break;
                }
            }
        }
        
        if(olFeature && (this.vectorLayer.selectedFeatures.length==0||this.vectorLayer.selectedFeatures[0].data.id != id)){
            this.selectCtrl.unselectAll();
            this.selectCtrl.select(olFeature)
        }
    },
    toggleDragfeature : function (feature){
        if(feature){
            this.dragFeature.activate();
        }else{
            this.dragFeature.deactivate();
        }
    },
    raiseOnDataEvent : function(evt){
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
    addLayer : function (type,name, url, layers,visible,extension){
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
        }
    },
    isLayerVisible : function (name){
        var lyrs = this.map.getLayersByName(name);
        if(lyrs && lyrs.length > 0){
            return lyrs[0].visibility;
        }
        return false;
    },
    setLayerVisible : function (name,vis){
        var lyrs = this.map.getLayersByName(name);
        if(lyrs && lyrs.length > 0){
            var layer = lyrs[0];
            layer.setVisibility(vis);
        }
    },
    zoomToExtent : function (minx,miny,maxx,maxy){
        this.map.zoomToExtent([minx,miny,maxx,maxy]);
    },
    update : function (){
        for ( var i = 0 ; i< this.map.layers.length ;i++ ){
            var layer = this.map.layers[i];
            layer.redraw(true);
        }
    },
    addSldToKargis : function (walsld,trigsld, signsld){
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
    removeSldFromKargis : function (){
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
    drawPoint : function(wkt){
        if(wkt){
            var olFeature = new OpenLayers.Geometry.Point(wkt[0],wkt[1]);
            geometryDrawUpdate(olFeature.toString());
            this.point.drawFeature(olFeature);
        }else{
            this.point.activate();
        }
        this.dragFeature.activate();
    },
    drawLine : function(wkt){
        if(wkt){
            var olFeature = new OpenLayers.Geometry.fromWKT(wkt);
            this.line.drawFeature(olFeature);
        }else{
            this.line.activate();
        }
        this.dragFeature.activate();
    },
    drawLineFromPoint : function (x,y){
        var lonlat = new OpenLayers.LonLat (x,y);
        var pixel = this.map.getPixelFromLonLat(lonlat);
        var evt = {
            xy: pixel
        }
        this.line.activate();
        this.line.handler.mousedown(evt);
        this.line.handler.mouseup(evt);
    },
    removeAllFeatures : function(){
        this.vectorLayer.removeAllFeatures();
        this.dragFeature.deactivate();
    },
    dragComplete : function (feature){
        var x = feature.geometry.x;
        var y = feature.geometry.y;
        editor.changeGeom(feature.data.className, feature.data.id, x,y);
    },
    drawFeature : function (object){
        var feature =object.feature;
        var lastPoint = feature.geometry.components[feature.geometry.components.length-1];
        this.point.deactivate();
        this.line.deactivate();
        this.editor.pointFinished(lastPoint);
        // TODO fire event geometry updated
    },
    setActiveFeature : function (feature){
        this.activeFeature = feature;
    },
    addFeatures : function(features){
        this.vectorLayer.addFeatures(this.geojson_format.read(features));
    }
});
var style = new OpenLayers.Style(
// the first argument is a base symbolizer
// all other symbolizers in rules will extend this one
{
    graphicWidth: 28,
    graphicHeight: 28,
    graphicYOffset: -14, // shift graphic up 28 pixels
    labelYOffset: -15,
    label: "${label}" // label will be foo attribute value
},
// the second argument will include all rules
{
    rules: [
    new OpenLayers.Rule({
        // a rule contains an optional filter
        filter: new OpenLayers.Filter.Comparison({
            type: OpenLayers.Filter.Comparison.EQUAL_TO,
            property: "type", // the "foo" feature attribute
            value: "CROSSING"
        }),
        // if a feature matches the above filter, use this symbolizer
        symbolizer: {
            externalGraphic: karTheme.crossing,
            graphicYOffset: -16,
            label: "${description}"
        }
    }),
    new OpenLayers.Rule({
        // a rule contains an optional filter
        filter: new OpenLayers.Filter.Comparison({
            type: OpenLayers.Filter.Comparison.EQUAL_TO,
            property: "type", // the "foo" feature attribute
            value: "GUARD"
        }),
        // if a feature matches the above filter, use this symbolizer
        symbolizer: {
            externalGraphic: karTheme.guard,
            graphicYOffset: -16,
            label: "${description}"
        }
    }),
    new OpenLayers.Rule({
        // a rule contains an optional filter
        filter: new OpenLayers.Filter.Comparison({
            type: OpenLayers.Filter.Comparison.EQUAL_TO,
            property: "type", // the "foo" feature attribute
            value: "BAR"
        }),
        // if a feature matches the above filter, use this symbolizer
        symbolizer: {
            externalGraphic: karTheme.bar,
            graphicYOffset: -16,
            label: "${description}"
        }
    }),
    new OpenLayers.Rule({
        // a rule contains an optional filter
        elseFilter: true,
        // if a feature matches the above filter, use this symbolizer
        symbolizer: {
            externalGraphic: karTheme.punt
        }
    }),
    new OpenLayers.Rule({   
        filter: new OpenLayers.Filter.Comparison({
            type: OpenLayers.Filter.Comparison.EQUAL_TO,
            property: "type",
            value:"ACTIVATION_1"
        }),
        symbolizer: {
            externalGraphic: karTheme.inmeldPunt
        }
    }),
    new OpenLayers.Rule({   
        filter: new OpenLayers.Filter.Comparison({
            type: OpenLayers.Filter.Comparison.EQUAL_TO,
            property: "type",
            value:"ACTIVATION_2"
        }),
        symbolizer: {
            externalGraphic: karTheme.uitmeldPunt
        }
    }),
    new OpenLayers.Rule({   
        filter: new OpenLayers.Filter.Comparison({
            type: OpenLayers.Filter.Comparison.EQUAL_TO,
            property: "type",
            value:"ACTIVATION_3"
        }),
        symbolizer: {
            externalGraphic: karTheme.voorinmeldPunt
        }
    }),
    new OpenLayers.Rule({   
        filter: new OpenLayers.Filter.Comparison({
            type: OpenLayers.Filter.Comparison.EQUAL_TO,
            property: "type",
            value:"END"
        }),
        symbolizer: {
            externalGraphic: karTheme.eindPunt,
            graphicYOffset: -25,
            graphicXOffset: -5
        }
    }),
    new OpenLayers.Rule({   
        filter: new OpenLayers.Filter.Comparison({
            type: OpenLayers.Filter.Comparison.EQUAL_TO,
            property: "type",
            value:"BEGIN"
        }),
        symbolizer: {
            externalGraphic: karTheme.startPunt,
            graphicYOffset: -25,
            graphicXOffset: -5
        }
    })
    ]
}
);

var selectstyle = new OpenLayers.Style(
// the first argument is a base symbolizer
// all other symbolizers in rules will extend this one
{
    graphicWidth: 37,
    graphicHeight: 37,
    graphicYOffset: -21, 
    labelYOffset: -20,
    label: "${label}" 
},
// the second argument will include all rules
{
    rules: [
    new OpenLayers.Rule({
        // a rule contains an optional filter
        filter: new OpenLayers.Filter.Comparison({
            type: OpenLayers.Filter.Comparison.EQUAL_TO,
            property: "type", // the "foo" feature attribute
            value: "CROSSING"
        }),
        // if a feature matches the above filter, use this symbolizer
        symbolizer: {
            externalGraphic: karTheme.vri_selected,
            label: "${description}",
            graphicYOffset: -26
        }
    }),
    new OpenLayers.Rule({
        // a rule contains an optional filter
        elseFilter: true,
        // if a feature matches the above filter, use this symbolizer
        symbolizer: {
            externalGraphic: karTheme.point_selected
        }
    }),
    new OpenLayers.Rule({   
        filter: new OpenLayers.Filter.Comparison({
            type: OpenLayers.Filter.Comparison.EQUAL_TO,
            property: "type",
            value:"ACTIVATION_1"
        }),
        symbolizer: {
            externalGraphic: karTheme.signInPoint_selected
        }
    }),
    new OpenLayers.Rule({   
        filter: new OpenLayers.Filter.Comparison({
            type: OpenLayers.Filter.Comparison.EQUAL_TO,
            property: "type",
            value:"ACTIVATION_2"
        }),
        symbolizer: {
            externalGraphic: karTheme.signOutPoint_selected
        }
    }),
    new OpenLayers.Rule({   
        filter: new OpenLayers.Filter.Comparison({
            type: OpenLayers.Filter.Comparison.EQUAL_TO,
            property: "type",
            value:"ACTIVATION_3"
        }),
        symbolizer: {
            externalGraphic: karTheme.preSignInPoint_selected
        }
    }),
    new OpenLayers.Rule({   
        filter: new OpenLayers.Filter.Comparison({
            type: OpenLayers.Filter.Comparison.EQUAL_TO,
            property: "type",
            value:"END"
        }),
        symbolizer: {
            externalGraphic: karTheme.endPoint_selected,
            graphicYOffset: -33,
            graphicXOffset: -6
        }
    }),
    new OpenLayers.Rule({   
        filter: new OpenLayers.Filter.Comparison({
            type: OpenLayers.Filter.Comparison.EQUAL_TO,
            property: "type",
            value:"BEGIN"
        }),
        symbolizer: {
            externalGraphic: karTheme.startPoint_selected,
            graphicYOffset: -33,
            graphicXOffset: -6
        }
    })
    ]
}
);


var tempstyle = new OpenLayers.Style(
// the first argument is a base symbolizer
// all other symbolizers in rules will extend this one
{
    graphicWidth: 37,
    graphicHeight: 37,
    graphicYOffset: -21, 
    labelYOffset: -20,
    label: "${label}" 
},
// the second argument will include all rules
{
    rules: [
    new OpenLayers.Rule({
        // a rule contains an optional filter
        filter: new OpenLayers.Filter.Comparison({
            type: OpenLayers.Filter.Comparison.EQUAL_TO,
            property: "type", // the "foo" feature attribute
            value: "CROSSING"
        }),
        // if a feature matches the above filter, use this symbolizer
        symbolizer: {
            externalGraphic: karTheme.vri,
            label: "${description}",
            graphicYOffset: -26
        }
    }),
    new OpenLayers.Rule({
        // a rule contains an optional filter
        elseFilter: true,
        // if a feature matches the above filter, use this symbolizer
        symbolizer: {
            externalGraphic: karTheme.point
        }
    }),
    new OpenLayers.Rule({   
        filter: new OpenLayers.Filter.Comparison({
            type: OpenLayers.Filter.Comparison.EQUAL_TO,
            property: "type",
            value:"ACTIVATION_1"
        }),
        symbolizer: {
            externalGraphic: karTheme.signInPoint
        }
    }),
    new OpenLayers.Rule({   
        filter: new OpenLayers.Filter.Comparison({
            type: OpenLayers.Filter.Comparison.EQUAL_TO,
            property: "type",
            value:"ACTIVATION_2"
        }),
        symbolizer: {
            externalGraphic: karTheme.signOutPoint
        }
    }),
    new OpenLayers.Rule({   
        filter: new OpenLayers.Filter.Comparison({
            type: OpenLayers.Filter.Comparison.EQUAL_TO,
            property: "type",
            value:"ACTIVATION_3"
        }),
        symbolizer: {
            externalGraphic: karTheme.preSignInPoint
        }
    }),
    new OpenLayers.Rule({   
        filter: new OpenLayers.Filter.Comparison({
            type: OpenLayers.Filter.Comparison.EQUAL_TO,
            property: "type",
            value:"END"
        }),
        symbolizer: {
            externalGraphic: karTheme.endPoint,
            graphicYOffset: -33,
            graphicXOffset: -6
        }
    }),
    new OpenLayers.Rule({   
        filter: new OpenLayers.Filter.Comparison({
            type: OpenLayers.Filter.Comparison.EQUAL_TO,
            property: "type",
            value:"BEGIN"
        }),
        symbolizer: {
            externalGraphic: karTheme.startPoint,
            graphicYOffset: -33,
            graphicXOffset: -6
        }
    })
    ]
}
);
/**
 * Create a Click controller
 * @param options
 * @param options.handlerOptions options passed to the OpenLayers.Handler.Click
 * @param options.click the function that is called on a single click (optional)
 * @param options.dblclick the function that is called on a dubble click (optional)
 */
OpenLayers.Control.Click = OpenLayers.Class(OpenLayers.Control,{
    defaultHandlerOptions: {
        'single': true,
        'double': false,
        'stopSingle': false,
        'stopDouble': false
    },
    handleRightClicks:true,
    initialize: function(options) {
        this.handlerOptions = OpenLayers.Util.extend(
        {}, this.defaultHandlerOptions
            );
        Ext.apply(this.handlerOptions,options.handlerOptions);
        OpenLayers.Control.prototype.initialize.apply(
            this, arguments
            );
        if (options.click){
            this.onClick=options.click;
        }
        if (options.dblclick){
            this.onDblclick=options.dblclick;
        }
        if (options.rightclick){
            this.onRightclick=options.rightclick;
        }
        this.handler = new OpenLayers.Handler.Click(
            this, {
                'click': this.onClick,
                'dblclick': this.onDblclick,
                'rightclick' : this.onRightclick
            }, this.handlerOptions
            );
    },
    onClick: function(evt) {        
    },
    onDblclick: function(evt) {          
    },
    onRightclick : function (evt){
    }
});