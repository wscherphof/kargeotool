function ol (){
    this.map = null;
    this.panel = null;
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
            controls : [new OpenLayers.Control.PanZoomBar(), new OpenLayers.Control.Navigation(), this.panel]
        };
        
        this.map = new OpenLayers.Map(domId,opt);
        this.createControls();
       
    },
    /**
     * Private nethod which adds all the controls
     */
    this.createControls = function (){
        this.panel.addControls(new OpenLayers.Control.DragPan()); 
        this.panel.addControls(new OpenLayers.Control.ZoomBox()); 
        this.panel.addControls( new OpenLayers.Control.ZoomToMaxExtent()); 
        var navHist = new OpenLayers.Control.NavigationHistory();
        this.map.addControl(navHist);
        this.panel.addControls( navHist.previous);
        this.panel.addControls( navHist.next);
        this.map.addControl( new OpenLayers.Control.MousePosition({numDigits: 2}));
        
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
    },
    /**
     * Add a layer. Assumed is that everything is in epsg:28992, units in meters and the maxextent is The Netherlands
     * @param type The type of the layer [WMS/TMS]
     * @param name The name of the layer
     * @param url The url to the service
     * @param layers The layers of the service which must be retrieved
     * @param extension Optional parameter to indicate the extension (type)
     */
    this.addLayer = function (type,name, url, layers,extension){
        var layer;
        if(type == 'WMS'){
            layer = new OpenLayers.Layer.WMS(name,url,{'layers':layers,'transparent': true},{singleTile: true,ratio: 1,transitionEffect: 'resize'});
        }else if (type == "TMS" ){
            if(!extension){
                extension = 'png';
            }
            layer = new OpenLayers.Layer.TMS(name, url,{
                layername:layers, 
                type: extension,
                serverResolutions: [3440.64,1720.32,860.16,430.08,215.04,107.52,53.76,26.88,13.44,6.72,3.36,1.68,0.84,0.42,0.21],
                tileOrigin:new OpenLayers.LonLat(-285401.920000,22598.080000)
            });
        }else{
            console.log("Type " + type + " not known.");
        }
        if(layer){
            this.map.addLayer(layer);
            this.map.zoomToMaxExtent();
        }
    }
}