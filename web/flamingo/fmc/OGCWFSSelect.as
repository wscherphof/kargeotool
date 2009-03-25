/*-----------------------------------------------------------------------------
Copyright (C) 2006 Roy Braam (www.b3partners.nl)

When you identify on the map it highlights all the identified Geometrys on the map.
It uses the Identify data to create a new layer with WMS and Styled Layer Descriptor (SLD). It only works for OG layers



Author: Roy Braam
*/
dynamic class OGCWFSSelect extends MovieClip {
	private var version:String = "0.1";
	private var postfix:String = "_selectedSldLayer";
	public var LAYEROGCWMS:String = "LayerOGWMS";
	//-------------------------------
	private var layers:String;
	private var layerKeys:Object= new Object();
	private var sldVersion:String;	
	private var geomProperties:String;
	private var record:Boolean=false;
	private var recordedValues:Object = new Object();
	private var listento:Object;
	private var DEBUG=false;		
	private var visualisationSelected:Object= new Object();
	private var ogcwmslayer:MovieClip;
	/**
	* The constructor
	*/
	function OGCWFSSelect(){
		log("construct");
		AsBroadcaster.initialize(this);
	}
	function getPostfix():String{
		return postfix;
	}
	function setOgcwmslayer(m:MovieClip){
		ogcwmslayer=m;
	}
	function getOgcwmslayer():MovieClip{
		return ogcwmslayer;
	}	
	function setSelectLayerName(s:String){
		log("setSelectLayerName");
		this.selectLayerName=s;
	}
	function getSelectLayerName():String{
		log("getSelectLayerName");
		return this.selectLayerName;
	}
	function setLayers(s:String){
		log("setLayers: "+s);
		this.layers=s;
	}
	function getLayers(){
		log("getLayers");
		return this.layers;
	}
	function setLayerKeys(o:Object){
		log("setLayerKeys");
		this.layerKeys=o;
	}
	function setRecord(b:Boolean){
		log("setRecord");
		this.record=b;
	}
	function setVisualisationSelected(visual:Object){
		log("setVisualisationSelected");
		this.visualisationSelected=visual;
	}
	function setRecorded(layerid:String, layerkey:String, values:Array){		
		log("setRecorded layerid: "+layerid+" layerkey: "+layerkey+" values: "+values);
		if (this.recordedValues[layerid]==undefined){
			this.recordedValues[layerid]=new Object();
		}		
		this.recordedValues[layerid][layerkey]=new Array();
		this.recordedValues[layerid][layerkey]=values;	
		this.broadcastMessage("onRecord", flamingo.getId(this.getOgcwmslayer()), this.recordedValues);
	}
	function addRecorded(layerid:String, layerkey:String, values:Array){		
		log("addRecorded layerid: "+layerid+" layerkey: "+layerkey+" values: "+values);
		if (this.recordedValues[layerid]==undefined){
			this.recordedValues[layerid]=new Object();
		}		
		if (recordedValues[layerid][layerkey]==undefined){
			this.recordedValues[layerid][layerkey]=new Array();
		}		
		for (var v=0; v < values.length; v++){
			var addValue:Boolean=true;
			for (var i=0; i < recordedValues[layerid][layerkey].length && addValue; i++){			
				if (recordedValues[layerid][layerkey][i]==values[v]){
					addValue=false;
				}
			}
			if (addValue){
				log(this.recordedValues[layerid][layerkey]);
				this.recordedValues[layerid][layerkey].push(values[v]);
			}
		}		
		this.broadcastMessage("onRecord", flamingo.getId(this.getOgcwmslayer()), this.recordedValues);
		
	}
	function setRecordedValues(o:Object){
		log("setRecordedValues");
		this.recordedValues=o;		
		this.broadcastMessage("onRecord", flamingo.getId(this.getOgcwmslayer()), this.recordedValues);
	}
	
	function getRecord(){
		log("getRecord");
		return record;
	}
	function getVisualisationSelected(){
		log("getVisualisationSelected");
		return visualisationSelected;
	}
	
	function getSldVersion():String{
		log("getVisualisationSelected");
		return sldVersion;
	}
	function setSldVersion(s:String){
		log("setSldVersion");
		sldVersion=s;
	}
	function getRecordedLayerString():String{
		var	layString:String="";
		var teller:Number=0;
		for (var lay in this.recordedValues){
			if (teller!=0)
				layString+=",";
			layString+=lay
			teller++;
		}
		return layString;
	}
	/**
	* sets creates a listener and ads it to the given Object o.
	* @attr o the object (map) on which this object needs to listen
	*/
	function setListento(o:Object){
		log("setListento");
		this.listento=o;
		//log("----------------------"+this.listento+" ID: "+flamingo.getComponent(this.listento));
		var thisObj=this;		
		var cListener:Object = new Object();
		cListener.onIdentifyData = function(map:MovieClip, maplayer:MovieClip, data:Object, extent:Object) {
			thisObj.log("on identify");
			var layerid=flamingo.getId(maplayer);		
			if (thisObj.layers==undefined){
				return;
			}
			if (flamingo.getType(maplayer)!="LayerOGWMS" || flamingo.getProperty(layerid,"query_layers")==undefined){
				return;
			}			
//			var identifyColorLayerLayers:Array = this.identifyColorLayer.split(",");
			var layersArray:Array = thisObj.layers.split(",");
			//var identifyColorLayerKeyArray:Array = this.identifyColorLayerKey.split(",");
			for (var i=0; i < layersArray.length; i++){
				//als de layer voorkomt in de te kleuren layers.
				//if record = false clean the recorded values.
				if (!thisObj.record){
					thisObj.recordedValues=new Object();					
				}
				for (var d in data){
					if (d==layersArray[i]){						
						var lk=thisObj.layerKeys[layersArray[i]];
						if (thisObj.record){	
							if (thisObj.recordedValues[layersArray[i]]==undefined){
								thisObj.recordedValues[layersArray[i]]=new Object();								
							}
							if (thisObj.recordedValues[layersArray[i]][lk]==undefined){
								thisObj.recordedValues[layersArray[i]][lk]= new Array();
							}
							//doorloop alle gevonden waarden.					
							for (var v in data[d]){						
								var pvalue=data[d][v][lk];
								var addValue:Boolean=true;
								for (var r=0; r < thisObj.recordedValues[layersArray[i]][lk].length && addValue; r++){				
									if (thisObj.recordedValues[layersArray[i]][lk][r]==pvalue){	
										thisObj.recordedValues[layersArray[i]][lk].splice(r,1);
										addValue=false;
									}
								}
								if (addValue){
									thisObj.recordedValues[layersArray[i]][lk].push(pvalue);
								}
							}		
						}else{											
							if (thisObj.recordedValues[layersArray[i]]==undefined)
								thisObj.recordedValues[layersArray[i]]=new Object();
							if(thisObj.recordedValues[layersArray[i]][lk]==undefined)
								thisObj.recordedValues[layersArray[i]][lk]=new Array();
							for (var v in data[d]){															
								var pvalue=data[d][v][lk];
								thisObj.recordedValues[layersArray[i]][lk].push(pvalue);
							}							
						}
					}			
				}
			}
			if (thisObj.record){
				thisObj.log("on record!");				
				thisObj.broadcastMessage("onRecord", layerid, thisObj.recordedValues);
				
			}
			thisObj.createSelectLayer(map,extent);
		};
		flamingo.addListener(cListener, this.listento, this);
	}
	/**
	*/
	function createSelectLayer(map:MovieClip, extent:Object){
		log("createSelectLayer()");
		var thisObj=this;		
		var layerid=flamingo.getId(this.getOgcwmslayer());								
		var sldDoc:XML;
		var sldLayer=flamingo.getComponent(layerid+this.getPostfix());
		var areLayers:Boolean=true;
		if (getRecordedLayerString().length==0){
			log("no layers!!");
			areLayers=false;
		}else{
			log("layers to color: "+getRecordedLayerString());
		}
		if(sldLayer==undefined && areLayers){
			var layerString:String = '<fmc:LayerOGWMS xmlns:fmc="';		
			layerString+=flamingo.getUrl(this.getOgcwmslayer()).slice(0,flamingo.getUrl(this.getOgcwmslayer()).indexOf("LayerOGWMS"));
			layerString+='" ';			
			layerString+='id="'+layerid.substring(layerid.indexOf("_")+1,layerId.length())+this.getPostfix()+'" ';
			if (flamingo.getProperty(layerid,"format")!=undefined){
				layerString+='format="'+flamingo.getProperty(layerid,"format")+'" ';
			}
			if (flamingo.getProperty(layerid,"exceptions")!=undefined){
				layerString+='exceptions="'+flamingo.getProperty(layerid,"exceptions")+'" ';
			}
			if (flamingo.getProperty(layerid,"srs")!=undefined){
				layerString+='srs="'+flamingo.getProperty(layerid,"srs")+'" ';
			}
			if (flamingo.getProperty(layerid,"transparent")){
				layerString+='transparent="'+flamingo.getProperty(layerid,"transparent")+'" ';
			}
			if (flamingo.getProperty(layerid,"timeout")){
				layerString+='timeout="'+flamingo.getProperty(layerid,"timeout")+'" ';
			}
			if (flamingo.getProperty(layerid,"retryonerror")){
				layerString+='retryonerror="'+flamingo.getProperty(layerid,"retryonerror")+'" ';
			}
			if (flamingo.getProperty(layerid,"showerrors")){
				layerString+='showerrors="'+flamingo.getProperty(layerid,"showerrors")+'" ';
			}
			if (flamingo.getProperty(layerid,"getcapabilitiesurl")){
				layerString+='getcapabilitiesurl="'+flamingo.getProperty(layerid,"getcapabilitiesurl")+'" ';
			}
			if (thisObj.getLayers()!=undefined){				
				layerString+='layers="';				
				var	layString:String=this.getRecordedLayerString();				
				if (layString.length > 0){
					layerString+=layString;
				}else{
					layerString+=thisObj.getLayers();
				}
				layerString+='" ';
				
			}
			var srs:String=flamingo.getProperty(layerid,"srs");
			sldDoc=thisObj.createSLD(thisObj.recordedValues,srs,extent);
			var urlString:String;
			urlString=flamingo.getProperty(layerid,"url");			
			layerString+='url="'+urlString;
			//iets nieuws voor doen!layerString+='getcapabilitiesurl="'+urlString+'" ';
			if (sldDoc!=null){
				if (flamingo.getProperty(layerid,"url").indexOf('?')>0){
					layerString+="&";
				}else{
					layerString+="?";
				}								
				layerString+='SLD_BODY='+escape(sldDoc.toString());
			}
			layerString+='"/>';
			//temp uit omdat het fouten geeft.
			thisObj.log("layerstring: "+layerString);
			map.addLayer(layerString);		
		}else{
			if (areLayers){
				var url:String =sldLayer.url;
				if (url.split('SLD_BODY=').length >=1){
					var sldParam:String=url.split('SLD_BODY=')[1];
					if (sldParam.indexOf('&',0)>0){
						sldParam=sldParam.split('&')[0];
						
					}
					//sldDoc.parseXML(unescape(sldParam));
					var newUrl:String;
					var token:Array = url.split('SLD_BODY='+sldParam);
					if (token.length>1){
						newUrl=token[0].substring(0,token[0].length-1);
						newUrl+=token[1];
					}else{
						newUrl=token[0].substring(0,token[0].length-1);
					}
					if (newUrl.indexOf('?')>0){
						newUrl+="&";
					}else{
						newUrl+="?";
					}
					var srs:String = sldLayer.srs;
					sldDoc=thisObj.createSLD(thisObj.recordedValues,srs,extent);					
					//sldBody=escape(sldBody);
					if (sldDoc!=null){
						newUrl+='SLD_BODY='+escape(sldDoc.toString());
					}
					thisObj.log("The New Url: "+newUrl);
					sldLayer.url=newUrl;
					//sldLayer.setLayerProperty(layString, "visible", true);
					sldLayer.layers=new Object();
					var	layString:String=this.getRecordedLayerString();
					var layTokens:Array=layString.split(",");
					for (var i=0; i < layTokens.length; i++){
						log("set new layer: "+layTokens[i]);
						sldLayer.layers[layTokens[i]]=new Object();
						sldLayer.layers[layTokens[i]]['visible']= true;
					}				
					sldLayer.show();
					//sldLayer.update(1,true);
				}
			}else{
				log("remove layer: "+sldLayer.id);
				flamingo.getParent(this).removeLayer(sldLayer.id);
			}
		}	
	}
	/**
	Log method
	*/
	
	function log(stringtolog:Object){
		if (DEBUG){
			trace(new Date()+this+" : "+stringtolog);
		}
	}
	/**
	Return a sld.
	*/
	function createSLD(_recordedValues:Object, srs:String, extent:Object):XML{
		log("createSLD");		
		var sldString:String = '<StyledLayerDescriptor version="'+this.getSldVersion()+'">';
			//sldDoc.parseXML(sldString);
		var namedLayers:String="";
		var layerCount=0;
		for (var recordedLayer in _recordedValues){	
			layerCount++
			var conditionPart:String="";
			var conditionCounts=0;
			for (var recordedKey in _recordedValues[recordedLayer]){
				for (var recordedValue in _recordedValues[recordedLayer][recordedKey]){
					conditionCounts++;
					conditionPart+='<ogc:PropertyIsEqualTo><ogc:PropertyName>'+recordedKey;
					conditionPart+='</ogc:PropertyName><ogc:Literal>'+_recordedValues[recordedLayer][recordedKey][recordedValue]+'</ogc:Literal>';
					conditionPart+='</ogc:PropertyIsEqualTo>';
				}			
			}
			namedLayers+='<NamedLayer><Name>'+recordedLayer+'</Name>';
			namedLayers+='<UserStyle><Title>clicked '+recordedLayer+'</Title>';
			namedLayers+='<FeatureTypeStyle><Rule><ogc:Filter>';
			if (conditionCounts>1){
				namedLayers+='<ogc:Or>';
			}
			namedLayers+=conditionPart;
			if (conditionCounts>1){
				namedLayers+='</ogc:Or>';
			}
			namedLayers+='</ogc:Filter>';
			if (visualisationSelected[recordedLayer]!=undefined){
				var symbolizer="PolygonSymbolizer";
				if (visualisationSelected[recordedLayer]["geometrytype"].toLowerCase()=="line"){
					symbolizer+='LineSymbolizer';
				}else if (visualisationSelected[recordedLayer]["geometrytype"].toLowerCase()=="point"){
					symbolizer+='PointSymbolizer';
				}
				
				namedLayers+='<'+symbolizer+'>';
				//add optional geometry object
				if (visualisationSelected[recordedLayer]["geomname"]!=undefined){
					namedLayers+='<Geometry><PropertyName>';
					namedLayers+=visualisationSelected[recordedLayer]["geomname"];
					namedLayers+='</PropertyName></Geometry>'
				}
				if (symbolizer == "PolygonSymbolizer"){
					namedLayers+=createFill(visualisationSelected[recordedLayer]);					
					namedLayers+=createStroke(visualisationSelected[recordedLayer]);
				}
				else if (symbolizer== "LineSymbolizer"){
					namedLayers+=createStroke(visualisationSelected[recordedLayer]);
				}else if (symbolizer=="PointSymbolizer"){
					namedLayers+='<Graphic>';
					namedLayers+='<Mark>';
					namedLayers+=createSymbolizerTag(visualisationSelected[recordedLayer],'WellKnownName');
					namedLayers+=createFill(visualisationSelected[recordedLayer]);
					namedLayers+='</Mark>';				
					namedLayers+=createSymbolizerTag(visualisationSelected[recordedLayer],'Size');
					namedLayers+='<Graphic>';
				}
				namedLayers+='</'+symbolizer+'>';
			}
			namedLayers+='</Rule></FeatureTypeStyle>';
			namedLayers+='</UserStyle>';
			namedLayers+='</NamedLayer>';
		}
		if (layerCount==0){
			return null;
		}
		sldString+=namedLayers;
		sldString+='</StyledLayerDescriptor>';
		log("the sldString: "+sldString);
		var sldDoc:XML = new XML();
		sldDoc.parseXML(sldString)
		flamingo.raiseEvent(thisObj, "onSelect", thisObj, getRecordedValues());
		return sldDoc;
	}	
	function createFill(visSelected:Object):String{
		var fillSymbol='<Fill>'
		fillSymbol+=createCssParameter(visSelected,"fill");
		fillSymbol+=createCssParameter(visSelected,"fill-opacity");
		fillSymbol+='</Fill>';
		return fillSymbol;
	}
	function createStroke(visSelected:Object):String{
		var strokeSymbol='<Stroke>';		
		strokeSymbol+=createCssParameter(visSelected,"stroke");			
		strokeSymbol+=createCssParameter(visSelected,"stroke-opacity");
		strokeSymbol+=createCssParameter(visSelected,"stroke-width");
		strokeSymbol+=createCssParameter(visSelected,"stroke-linejoin");
		strokeSymbol+=createCssParameter(visSelected,"stroke-linecap");
		strokeSymbol+=createCssParameter(visSelected,"stroke-dasharray");					
		strokeSymbol+=createCssParameter(visSelected,"stroke-dashoffset");
		strokeSymbol+='</Stroke>';		
		return strokeSymbol;
	}
	function createSymbolizerTag(visSelected:Object,tagName:String){
		if (visSelected[tagName.toLowerCase()]==undefined){
			return;
		}
		return '<'+tagName+'>'+visSelected[tagNametagName.toLowerCase()]+'</'+tagName+'>';
	}
	function createCssParameter(visSelected:Object,cssparam:String,symbolizer:String){
		if (visSelected[cssparam]==undefined){			
			return '';
		}else{		
			return '<CssParameter name="'+cssparam+'">'+visSelected[cssparam]+'</CssParameter>';
		}
	}
	function update(){
		log("do the update");
		createSelectLayer(flamingo.getParent(this.getOgcwmslayer()),null);
	}
}