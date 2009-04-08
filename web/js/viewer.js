var wktgeom="";
var enabledToolId="";
var selectExtent;
function flamingo_map_onIdentifyData(map,layer,data,identifyextent,nridentified,total){    
    
}
function flamingo_drawMap_onGeometryDrawFinished(comp,wktGeom){
    document.getElementById("wktgeomfield").value=wktGeom;
    document.getElementById("idfield").value="";
    document.getElementById("activationForm").submit();
}
function flamingo_map_onIdentify(map,extent){
    if (isGemeente){
        enableDelete();
    }
    enableExport();
    selectExtent=extent;
}
function flamingo_toolgroup_onSetTool(toolgroup,toolid){
    enabledToolId=toolid;
}
function flamingo_map_onInit(){    
    
}
/*Save the selected geom.*/
function saveActiveGeom(){
    document.getElementById("wktgeomfield").value="";
    document.getElementById("idfield").value="";
    var activeFeature=flamingo.call("drawMap",'getActiveFeature');
    if (activeFeature!=undefined){
        document.getElementById("wktgeomfield").value=activeFeature["wktgeom"];
        var featureId=activeFeature["id"];        
        if (isNaN(featureId)){
            if(featureId.indexOf(".")>0 && featureId.split(".").length > 1){
                featureId=featureId.split(".")[1];
            }
        }
        if (!isNaN(featureId)){
            document.getElementById("idfield").value=featureId;
        }        
        document.getElementById("activationForm").submit();
        removeDrawing();
    }else{
        alert("Selecteer eerst een bouwplan door met de bouwplanselectietool \n op een bouwplan te klikken of teken een nieuw bouwplan.");
    }
}
function removeDrawing(){
    flamingo.call("drawMap",'removeAllFeatures');
}
function createInputField(name,id){
    var inputElement=document.createElement("input");
    inputElement.setAttribute("type","hidden");
    inputElement.setAttribute("name",name);
    inputElement.setAttribute("id",id);
    document.getElementById("activationForm").appendChild(inputElement);
}
function useSelection(aktie){
    if (selectExtent==undefined){
        alert("Er is geen selectie gemaakt");
        return;
    }
    window.open('selectBouwplan.do?aktie='+aktie+'&selectionBbox='+
        selectExtent.minx+
        ","+selectExtent.miny+
        ","+selectExtent.maxx+
        ","+selectExtent.maxy, '', 'width='+screen.width+', height='+screen.height+', left=0, top=0, scrollbars=yes, menubar=yes, status=yes, resizable=yes');
    flamingo.callMethod("map","clearDrawings");
    selectExtent=undefined;
    disableExportDelete();
}
function setLayers(layers,httpRequestParams){
    if (layers!=null && layers!=undefined){
        flamingo.call('map','removeLayer','layerBouwplan');
        if (httpRequestParams==undefined){
            httpRequestParams="";
        }
        var newLayer='<fmc:LayerOGWMS xmlns:fmc="fmc" id="layerBouwplan" url="'+wmsUrl+'Styles=&amp;Service=WMS&amp;'+httpRequestParams+'" srs="EPSG:28992" layers="grens_woningbouwregios,'+layers+'" query_layers="'+layers+'">'+
        '</fmc:LayerOGWMS>';
        flamingo.call('map','addLayer',newLayer);
    }
}
function refreshFlamingo(){
    flamingo.call('map','update', 0 , true);
}
function disableExportDelete(){
    flamingo.call("exportButton",'hide');
    flamingo.call("deleteButton",'hide');
}
function enableDelete(){    
    flamingo.call("deleteButton",'show');
}
function enableExport(){
    flamingo.call("exportButton",'show');
}