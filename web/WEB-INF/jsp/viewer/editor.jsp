<%@include file="/WEB-INF/taglibs.jsp" %>
<%@page errorPage="/WEB-INF/jsp/commons/errorpage.jsp" %>

<%@page pageEncoding="UTF-8"%>

<tiles:importAttribute/>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<html:html>
    <head>
        <title><fmt:message key="index.title"/></title>
        <script language="JavaScript" type="text/JavaScript" src="<html:rewrite page="/js/validation.jsp" module=""/>"></script>
        <link rel="stylesheet" href="<html:rewrite page="/styles/geo-ov.css" module=""/>" type="text/css" media="screen" />
        <link rel="stylesheet" href="<html:rewrite page="/styles/jquery-ui-1.7.2.custom.css" module=""/>" type="text/css" media="screen" />
        <script type="text/javascript" src="<html:rewrite page="/js/json2.js" module=""/>"></script>
        <script type="text/javascript" src="<html:rewrite page="/js/utils.js" module=""/>"></script>
        <script type="text/javascript" src="<html:rewrite page="/js/simple_treeview.js" module=""/>"></script>
        <script type="text/javascript" src="<html:rewrite page="/dwr/engine.js" module=""/>"></script>
        <script type="text/javascript" src="<html:rewrite page="/dwr/interface/Editor.js" module=""/>"></script>
        <script type="text/javascript" src="<html:rewrite page="/js/swfobject.js" module=""/>"></script>
        <script type="text/javascript" src="<html:rewrite page='/js/jquery-1.3.2.min.js' module=''/>"></script>
        <script type="text/javascript" src="<html:rewrite page='/js/jquery-ui-1.7.2.custom.min.js' module=''/>"></script>
        <script type="text/javascript" src="<html:rewrite page='/openlayers/OpenLayers.js' module=''/>"></script>
        <script type="text/javascript" src="<html:rewrite page="/js/OpenLayersController.js" module=""/>"></script>
        
        <!--[if lte IE 6]>
            <link href="<html:rewrite page="/styles/geo-ov-ie6.css" module=""/>" rel="stylesheet" media="screen" type="text/css" />
            <script type="text/javascript" src="<html:rewrite page="/js/ie6fixes.js" module=""/>"></script>
        <![endif]-->
        <!--[if IE 7]> <link href="<html:rewrite page="/styles/geo-ov-ie7.css" module=""/>" rel="stylesheet" media="screen" type="text/css" /> <![endif]-->

    </head>
    <body class="editor" id="editorBody">

        <tiles:insert definition="headerlinks"/>

<script type="text/javascript">

    function testSelecteerObject() {
        var obj = prompt("Geef object aan als type:idnr, dus rseq:1, ag:23, a:456", "rseq:67");
        if(obj == null) { /* no input, do nothing */
            return;
        }

        var type, id, valid = false;
        if(obj != null && obj.split(":").length == 2) {
            obj = obj.split(":");
            type = obj[0].toLowerCase();
            if(type == "rseq" || type == "ag" || type == "a") {
                id = parseInt(obj[1], 10);
                valid = !isNaN(id);
            }
        }
        if(!valid) {
            alert("Ongeldige invoer");
        } else {
            document.getElementById("loading").style.visibility = "visible";
            Editor.getObjectTree(type, id, dwr_treeInfoReceived);
        }        
    }


    function setStatus(what, status) {
        document.getElementById(what + "Status").innerHTML = escapeHTML(status);
    }

    function treeItemClick(item) {
        showSelected(roaEquId, actGroIds, actIds);
        tree_selectObject(item);
        form_editObject(item);
        if(document.getElementById("autoZoom").checked) {
            options_zoomToObject();
        }
    }

    <%-- beetje een hack, gaat er vanuit dat tree depth-first (dus na rseq de
         daar onder horende ag en a) opbouwt...
    --%>
    var editableGlobal = false;

    function createLabel(container, item) {
        container.className = "node";

        var a = document.createElement("a");
        a.href = "#";
        a.onclick = function() {
            treeItemClick(item);
        };

        var icon = document.createElement("span");
        icon.className = "icon icon_" + item.type;
        if(item.type == "rseq") {
            icon.className += "_" + item.rseqType;
            editableGlobal = item.editable;
            if(!editableGlobal) {
                a.className = "uneditable";
            }
        } else if(item.type == "ag") {
            icon.className += "_" + item.icon;
            if(!editableGlobal) {
                a.className = "uneditable";
            }
        } else if(item.type == "a") {
            icon.className += "_" + item.commandType;
            if(!editableGlobal) {
                a.className = "uneditable";
            }
        }
        
        a.appendChild(icon);
        var labelText = " ";
        if(item.type == "a") {
            labelText += item.index + " " + item.name;
        } else {
            labelText += item.name;
        }
        a.appendChild(document.createTextNode(labelText));
        container.appendChild(a);
    }

    var tree = {"id": "root"}; /* root van items */
    var objectTreeOpts; /* options van tree */
    var selectedObject;

    function clearTreeview() {
        var treeEl = document.getElementById("objectTree");
        while(treeEl.firstChild != undefined) {
            treeEl.removeChild(treeEl.firstChild);
        }
    }

    function createTreeview() {
        clearTreeview();
        treeview_create({
            "id": "objectTree",
            "root": tree,
            "rootChildrenAsRoots": true,
            "itemLabelCreatorFunction": createLabel,
            "toggleImages": {
                "collapsed": "<html:rewrite page="/images/treeview/plus.gif" module=""/>",
                "expanded": "<html:rewrite page="/images/treeview/minus.gif" module=""/>",
                "leaf": "<html:rewrite page="/images/treeview/leaft.gif" module=""/>"
            },
            "saveExpandedState": true,
            "saveScrollState": true,
            "expandAll": false
        });
        objectTreeOpts = globalTreeOptions["objectTree"];
    }

    function refreshTreeview() {
        createTreeview();
        treeview_restoreScrollStates();
    }

    function dwr_treeInfoReceived(info) {
        document.getElementById("loading").style.visibility = "hidden";

        if(info.toLowerCase().indexOf("error") == 0) {
            if(info.toLowerCase().indexOf("no objects found") != -1) {
                //alert("Geen objecten gevonden op deze locatie");
            } else {
                alert(info);
            }
            return;
        }
        
        var obj = eval("(" + info + ")");
        tree = obj.tree;

        deselectObject();
        window.frames["form"].location = "about:blank";
        createTreeview();

        if(obj.selectedObject != undefined) {
            //setStatus("tree", "Object op locatie geselecteerd");

            var object = treeview_findItem(tree, obj.selectedObject.id);
            tree_selectObject(object);
            form_editObject(object);
            if(document.getElementById("autoZoom").checked) {
                options_zoomToObject();
            }
        } else {
            //setStatus("tree", "Meerdere objecten op locatie");
        }
        if(obj.envelope != undefined) {
            zoomToEnvelope(eval("(" + obj.envelope + ")"));
        }
    }

    function tree_selectObject(object) {
        if(selectedObject != undefined) {
            container = treeview_getLabelContainerNodeForItemId("objectTree", selectedObject.id);
            container.className = "node";
        }

        selectedObject = object;
        document.getElementById("zoomButton").disabled = object.point == undefined;
        
        document.getElementById("newAg").disabled = false;
        document.getElementById("newA").disabled = object.type == "rseq";

        container = treeview_getLabelContainerNodeForItemId("objectTree", object.id);
        container.className = "selected node";
        treeview_expandItemParents("objectTree", object.id);
        treeview_expandItemChildren("objectTree", object.id);
    }

    function deselectObject() {
        if(selectedObject != undefined) {
            container = treeview_getLabelContainerNodeForItemId("objectTree", selectedObject.id);
            container.className = "node";
        }
        selectedObject = null;
        document.getElementById("zoomButton").disabled = true;
        document.getElementById("newAg").disabled = true;
        document.getElementById("newA").disabled = true;
    }

    function notEditable() {
        document.getElementById("newAg").disabled = true;
        document.getElementById("newA").disabled = true;
    }

    function clear() {
        deselectObject();
        clearTreeview();
    }

    function form_editObject(object) {
        var url;
        switch(object.type) {
            case "a" : url = "<html:rewrite page="/activation.do"/>"; break;
            case "ag": url = "<html:rewrite page="/activationGroup.do"/>"; break;
            case "rseq": url = "<html:rewrite page="/roadsideEquipment.do"/>"; break;
        }
        url = url + "?id=" + object.id.split(":")[1];
        //setStatus("form", "form laden voor object " + object.id + ": " + url);
        window.frames["form"].location = url;
    }

    function getZoomBorder() {
        var text = document.getElementById("zoomExtent").value;
        var zoomExtent = parseInt(text);
        if(isNaN(zoomExtent) || zoomExtent < 10 || zoomExtent > 9999) {
            zoomExtent = 375;
            document.getElementById("zoomExtent").value = zoomExtent + "";
        }
        return zoomExtent / 2.0;
    }

    function options_zoomToObject() {
        if(selectedObject != undefined && selectedObject != null) {
            if(selectedObject.point) {
                var zoomBorder = getZoomBorder();
                var xy = selectedObject.point.split(", ");
                var x = parseInt(xy[0]); var y = parseInt(xy[1]);
                oc.zoomToExtent(x - zoomBorder, y - zoomBorder, x + zoomBorder, y + zoomBorder);
            }
        }
    }

    function zoomToEnvelope(envelope) {
        var zoomBorder = getZoomBorder();
        oc.zoomToExtent(envelope.minX - zoomBorder, envelope.minY - zoomBorder, envelope.maxX + zoomBorder, envelope.maxY + zoomBorder);
    }

    /* Aangeroepen door form in iframe na crud actie */
    function treeUpdate(cmd) {
        cmd = eval("(" + cmd + ")");

        var parentItem;
        if(cmd.parentId == null) {
            parentItem = tree;
        } else {
            parentItem = treeview_findItem(tree, cmd.parentId);
        }

        if(cmd.action == "remove") {
            if(selectedObject.id == cmd.id) {
                deselectObject();
            }

            var item = treeview_findItem(tree, cmd.id);
            var index = parentItem.children.indexOf(item);
            parentItem.children.splice(index,1);
            if(item.type == "a") {
                <%-- Update indexes van activations in lijst van ActivationGroup,
                     dit is bij verwijderen van activation in Action ook gedaan
                --%>
                for(var i = index; i < parentItem.children.length;  i++) {
                    parentItem.children[i].index--;
                }
            }
            refreshTreeview();
        } else if(cmd.action == "update") {
            var item = treeview_findItem(tree, cmd.id);
            var index = parentItem.children.indexOf(item);
            /* children zitten niet in cmd.object, hou gewoon oude aan */
            var oldItemChildren = parentItem.children[index].children;
            cmd.object.children = oldItemChildren;
            parentItem.children[index] = cmd.object;
            refreshTreeview();
            tree_selectObject(cmd.object);
        } else if(cmd.action == "insert") {
            /* Altijd als laatste child inserten */
            if(parentItem.children == undefined) {
                parentItem.children = [];
            }
            parentItem.children[parentItem.children.length] = cmd.object;
            refreshTreeview();
            tree_selectObject(cmd.object);
        }
    }

    /* Aangeroepen door form in iframe */
    function selectLocationClicked(currentLocation, geometryType) {
        if(currentLocation != null) {
            addGeometry(geometryType, currentLocation);
        } else {
            drawNewGeometry( geometryType);
        }
    }

    function newRseq() {
        deselectObject();
        window.frames["form"].location = "<html:rewrite page="/roadsideEquipment.do?new=t"/>";
    }

    /* Zoek in een tree naar een item en return een array van parent items, beginnende
     * bij de parent het hoogst in de tree.
     * Aanroep met alleen de eerste twee argumenten, het derde argument wordt
     * gebruikt voor recursieve aanroep.
     */
    function findTreeItemParents(root, needle, crumbs) {
        if(root == needle) {
            /* needle gevonden, return array met parent items */
            return crumbs ? crumbs : []; /* indien eerste aanroep return lege array */
        }

        if(root == undefined || root.children == undefined) {
            return null;
        }

        if(crumbs == undefined) {
            /* eerste aanroep */
            crumbs = [root];
        } else {
            /* voeg root toe als parent item */
            crumbs.push(root);
        }

        for(var i = 0; i < root.children.length; i++) {
            var recursedCrumbs = findTreeItemParents(root.children[i], needle, crumbs.slice());
            if(recursedCrumbs != null) {
                return recursedCrumbs;
            }
        }
        return null;
    }

    function newAg() {
        if(selectedObject == "undefined") {
            alert("Geen object geselecteerd");
            return;
        }
        var rseq;
        if(selectedObject.type == "rseq") {
            rseq = selectedObject;
        } else {
            var parents = findTreeItemParents(tree, selectedObject);
            /* parents[0] is tree, daaronder moet altijd de rseq te vinden zijn */
            if(parents.length < 2) {
                alert("Interne fout: kan geen rseq parent vinden van selectedObject");
            }
            rseq = parents[1];
        }
        var rseqId = rseq.id.split(":")[1];
        deselectObject();
        window.frames["form"].location = "<html:rewrite page="/activationGroup.do?new=t"/>" + "&rseqId=" + rseqId;
    }

    function newA() {
        if(selectedObject == "undefined") {
            alert("Geen object geselecteerd");
            return;
        }
        if(selectedObject.type == "rseq") {
            alert("Geen signaalgroep geselecteerd");
            return;
        }
        var ag;
        if(selectedObject.type == "ag") {
            ag = selectedObject;

        } else {
            var parents = findTreeItemParents(tree, selectedObject);
            /* parents[0] is tree, daaronder rseq, daaronder moet altijd de ag te vinden zijn */
            if(parents.length < 3) {
                alert("Interne fout: kan geen ag parent vinden van selectedObject");
            }
            ag = parents[2];
        }
        var agId = ag.id.split(":")[1];
        deselectObject();
        window.frames["form"].location = "<html:rewrite page="/activation.do?new=t"/>" + "&agId=" + agId;
    }

    function onIdentifyData( layer, data) {
        if("map_kar_layer" == layer) {

            if(data.buslijnen != undefined){
                makeBuslijnenUnique(data);
            }
            if(data.bushaltes_symbol != undefined ) {
                generatePopupBushaltes(data.bushaltes_symbol);
            }
            
            if(data.triggerpunten != undefined || data.signaalgroepen != undefined || data.walapparatuur != undefined) {
                if(data.walapparatuur != undefined && document.getElementById("showSelected").checked && selectedObject != undefined){
                    data.walapparatuur = isRequestedIdFiltered("rseq", data.walapparatuur);
                }

                if(data.signaalgroepen != undefined && document.getElementById("showSelected").checked && selectedObject != undefined){
                    data.signaalgroepen = isRequestedIdFiltered("ag", data.signaalgroepen);
                }

                if(data.triggerpunten != undefined && document.getElementById("showSelected").checked && selectedObject != undefined){
                    data.triggerpunten = isRequestedIdFiltered("a", data.triggerpunten);
                }
                document.getElementById("loading").style.visibility = "visible";
                Editor.getIdentifyTree(JSON.stringify(data), dwr_treeInfoReceived);
            }
        }
    }

    function flamingo_drawMap_onCreatePointAtDistanceFinished(obj, geometry, pathLength) {
        //flamingo.callMethod("location", "show");
        window.frames["form"].flamingo_onCreatePointAtDistanceFinished(obj, geometry, pathLength);
    }

    function geometryDrawUpdate( geometry) {
        //flamingo.callMethod("location", "show");
        window.frames["form"].geometryDrawUpdate( geometry);
    }

    var editMap = "drawMap";

    // geometryType is Point, PointAtDistance, ...

    function addGeometry(geometryType, geometry) {
        if(geometryType == "Point"){
            this.oc.drawPoint(geometry);
        }else{
            this.oc.drawLine(geometry);
        }
    }

    function drawNewGeometry(geometryType) {
        if(geometryType == "Point"){
            this.oc.drawPoint();
        }else{
            this.oc.drawLine();
        }
    }

    function cancelEdit() {
        this.oc.removeAllFeatures();
        // hide location
    }

    function walapparaatnummerKeyPressed(e) {
        if(e.keyCode == 0xd) {
            zoekWalapparatuur();
        }
    }

    function zoekWalapparatuur() {
        var unitNumber = document.getElementById("walapparaatnummer").value;
        document.getElementById("loading").style.visibility = "visible";
        Editor.getRseqUnitNumberTree(unitNumber, dwr_treeInfoReceived);
    }

    setOnload(function() 
    {
        document.getElementById("walapparaatnummer").focus();
        if("${magWalapparaatMaken}" == "true"){
            document.getElementById("newRseq").disabled = false;
        }else{
            document.getElementById("newRseq").disabled = true;
        }
    });

    function isRequestedIdFiltered(type, lijst){
        if(type=="rseq"){
            for(var i = 0 ; i < lijst.length ; i++){
                if(lijst[i].id != roaEquId){
                    lijst.splice(i,1);
                    i--;
                }
            }
            return lijst;
        }

        if(type=="ag"){
            for(var i = 0 ; i < lijst.length ; i++){
                for( var j = 0 ; j < actGroIds.length ; j++ ){
                    if(lijst[i].id != actGroIds[i]){
                        lijst.splice(i,1);
                        i--;
                    }
                }
            }
            return lijst;
        }

        if(type=="a"){
            for(var i = 0 ; i < lijst.length ; i++){
                for( var j = 0 ; j < actIds.length ; j++ ){
                    if(lijst[i].id != actIds[i]){
                        lijst.splice(i,1);
                        i--;
                    }
                }
            }
            return lijst;
        }
    }

    var layer = null;
    var roaEquId = null;
    var actGroIds = null;
    var actIds = null;
    function showSelected(rseqId, agIds, aIds){
        var sldstring = "${absoluteURLPrefix}";
        sldstring += "<html:rewrite page="/SldServlet" module=""/>";//"http://localhost:8084/SldGeneratorGeo-ov/SldServlet"; // XXX servlet in webapp en hier html:rewrite gerbuiken

        if(rseqId != undefined){
            roaEquId = rseqId;
        }

        if(agIds != undefined){
            actGroIds = agIds;
        }

        if(aIds != undefined){
            actIds = aIds;
        }
         sldstring = "http://x13.b3p.nl:8082/geo-ov/SldServlet";
        if(document.getElementById("showSelected").checked) {
            // bouw de sld string op
            var walsld = addToSldstring(rseqId, "rseq", sldstring);
            var signsld = addToSldstring(agIds, "ag", sldstring);
            var trigsld = addToSldstring(aIds, "a", sldstring);
            oc.addSldToKargis(walsld,trigsld,signsld);
        }else{
            oc.removeSldFromKargis();
        }
    }

    // TODO: als leeg is, dan niet parameter vullen
    function addToSldstring(lijst, typeVis, sldstring){
        sldstring +="?"+ typeVis + "VisibleValues=";
      
        if(lijst instanceof Array){
            if(lijst.length >0){
                sldstring += lijst.join(',');
            }
        }else{
            sldstring += lijst;
        }

        return sldstring;
    }

    function toggleVisibleSelected(){
        
        if(!document.getElementById("showSelected").checked) {
            showSelected();
        }
        if(document.getElementById("showSelected").checked && selectedObject != undefined) {
     
            showSelected( roaEquId, actGroIds, actIds);
        }
        if(selectedObject != undefined){
           // oc.update();
        }
    }

    function makeBuslijnenUnique(data){
        var buslijnen = data.buslijnen;
        for(var i = 0 ; i < buslijnen.length; i++){
            for(var j = 0 ; j < buslijnen.length; j++){
                if( j != i){
                    if(buslijnen[j].destinationcode == buslijnen[i].destinationcode && buslijnen[j].lineplanningnumber == buslijnen[i].lineplanningnumber &&
                            buslijnen[j].name == buslijnen[i].name && buslijnen[j].direction == buslijnen[i].direction && buslijnen[j].publicnumber == buslijnen[i].publicnumber){
                            buslijnen.splice(i,1);
                            i--;
                            break;
                    }

                }
            }
        }
        generatePopupBuslijnen(buslijnen);
    }

    function generatePopupBuslijnen(buslijnen){

        if(!document.getElementById('popupWindow')) {
            //Root
            var popupDiv = document.createElement('div');
            popupDiv.styleClass = 'popup_Window';
            popupDiv.id = 'popupWindow';

            var popupWindowBackground = document.createElement('div');
            popupWindowBackground.styleClass = 'popupWindow_Windowbackground';
            popupWindowBackground.id = 'popupWindow_Windowbackground';

            $("body").append($(popupDiv));
            $("body").append($(popupWindowBackground));

            popupDiv.title = 'Buslijnen';
            popupDiv.innerHTML = generateBuslijnenHtml(buslijnen, popupDiv.innerHTML);
            //popupDiv.innerHTML = '<strong>Content</strong> van de div';
            $("#popupWindow").dialog({height: 350, width: 400});
        } else {
            var popupDiv = document.getElementById('popupWindow');
            popupDiv.title = 'Buslijnen';
            //popupDiv.innerHTML = '<strong>Content</strong> van de div';
            popupDiv.innerHTML = generateBuslijnenHtml(buslijnen, popupDiv.innerHTML);
            $("#popupWindow").dialog('open');
        }
    }

    function generateBuslijnenHtml(buslijnen, htmlObject){
        htmlObject = "";
        for( var i = 0 ; i < buslijnen.length ; i++){
            var buslijn = buslijnen[i];
            htmlObject += "<h2>Buslijn</h2><br>";
            htmlObject += buslijn.publicnumber + " (" + buslijn.direction + "): " + buslijn.name + "<br>&nbsp;";
        }

        return htmlObject;
    }

    function generatePopupBushaltes(bushaltes){

        if(!document.getElementById('popupWindowHaltes')) {
            //Root
            var popupDiv = document.createElement('div');
            popupDiv.styleClass = 'popup_Window';
            popupDiv.id = 'popupWindowHaltes';

            var popupWindowBackground = document.createElement('div');
            popupWindowBackground.styleClass = 'popupWindow_Windowbackground';
            popupWindowBackground.id = 'popupWindow_Windowbackground';

            $("body").append($(popupDiv));
            $("body").append($(popupWindowBackground));

            popupDiv.title = 'Bushaltes';
            popupDiv.innerHTML = generateBushalteHtml(bushaltes, popupDiv.innerHTML);
            $("#popupWindowHaltes").dialog({height: 350, width: 400, left: 400});
        } else {
            var popupDiv = document.getElementById('popupWindowHaltes');
            popupDiv.innerHTML = generateBushalteHtml(bushaltes, popupDiv.innerHTML);
            $("#popupWindowHaltes").dialog('open');
        }
    }

    function generateBushalteHtml(bushaltes, htmlObject){
        htmlObject = "";
        /*
         *  validfrom:\t\t[validfrom]
                    dataowner:\t[dataowner]
                    code:     \t\t[code]
                    name:     \t\t[name]</textformat></span>
         */
        for( var i = 0 ; i < bushaltes.length ; i++){
            var bushalte = bushaltes[i];
            htmlObject += "<h2>Bushalte</h2><br><table border='0'>";
            htmlObject += "<tr><td>validfrom:</td><td>" + bushalte.validfrom + "</td></tr>";
            htmlObject += "<tr><td>dataowner:</td><td>" + bushalte.dataowner + "</td></tr>";
            htmlObject += "<tr><td>code:</td><td>" + bushalte.code + "</td></tr>";
            htmlObject += "<tr><td>name:</td><td>" + bushalte.name + "</td></tr></table><br>&nbsp;";
        }

        return htmlObject;
    }


   
</script>

<div id="leftbar">

    <div id="tree">
        <div id="treeTop">
            <div id="treeTitel">Objectenboom</div>
            <div id="loading"><html:img page="/images/ajax-loader.gif" module=""/></div>
            <label title="asdfwe">Zoek op walapparaatnummer:</label> <input title="asdf" id="walapparaatnummer" type="text" size="10" onkeypress="walapparaatnummerKeyPressed(event);">
            <input type="button" name="zoekWalapparatuur" value="Zoeken" onclick="zoekWalapparatuur()">
        </div>
        <div id="objectTree"></div>
        <div id="options">
            <input id="zoomButton" type="button" value="Zoom naar object" title="Zoom in op een geselecteerd object uit de objectenboom. Met Auto-zoom aangevinkt wordt direct ingezoomd op een geselecteerd object. Het zoomniveau is instelbaar door de minimale diameter (in meters) op te geven van het gebied rond het object. Klik na het wijzigen van de diameter op Zoom naar object om te zoomen naar het opgegeven niveau." onclick="options_zoomToObject();">
            <label><input id="autoZoom" type="checkbox" value="autoZoom" checked="true">Auto-zoom</label>
            <input id="zoomExtent" name="zoomExtent" type="text" value="375" size="2" maxlength="4" style="text-align: right"> <label><input id="showSelected" type="checkbox" value="showSelected" onclick="toggleVisibleSelected()">Toon geselecteerden</label>
            <br>
            <b>Nieuwe:</b>
            <input id="newRseq" type="button" value="Walapparatuur" title="Selecteer deze optie voor het toevoegen van een nieuw walapparaat. Het is niet mogelijk signaalgroepen of triggerpunten aan te maken zonder eerst een walapparaat te selecteren of aan te maken." onclick="newRseq()">
            <input id="newAg" type="button" value="Signaalgroep" title="Selecteer deze optie voor het vastleggen van de stopstreep (van signaalgroep) bij de geselecteerde VRI. (Het is niet mogelijk een signaalgroep aan te maken zonder eerst een walapparaat te selecteren of aan te maken.)" disabled="true" onclick="newAg()">
            <input id="newA" type="button" value="Triggerpunt" title="Selecteer deze optie voor het koppelen van een nieuw triggerpunt aan de geselecteerde signaalgroep. (Het is niet mogelijk een triggerpunt aan te maken zonder eerst een signaalgroep te selecteren of aan te maken.)" disabled="true" onclick="newA()">
        </div>
    </div>

    <div id="form">
        <div id="formContainer">
            <span style="display: none">Status: <span id="formStatus" style="font-weight: bold">Geen object</span></span>
            <iframe frameborder="0" name="form" src="<html:rewrite page="/empty.jsp" module=""/>"></iframe>
        </div>
    </div>

</div>

<div id="kaart">
    <div id="map" style="width: 80%; height: 100%;float:left;"></div>
    <div id="overview" style="width:19%;border:1px solid #000; float:right; height:300px;overflow:hidden;"></div>
    <div id="legend" style="width:20%;float:right;"> 
        <br/>
        <strong>VRI-informatie</strong><br/>
        <input type="checkbox" checked="checked" onclick="toggleLayer('walapparatuur');"/>Walapparatuur<br/>
        <div id="walapparatuur"><img src="http://x13.b3p.nl/cgi-bin/mapserv?map=/home/matthijsln/geo-ov/transmodel_connexxion_edit.map&amp;version=1.1.1&amp;service=WMS&amp;request=GetLegendGraphic&amp;layer=walapparatuur&amp;format=image/png"/></div>
        <input type="checkbox" checked="checked" onclick="toggleLayer('signaalgroepen');"/>Signaalgroepen<br/>
        <div id="signaalgroepen"><img src="http://x13.b3p.nl/cgi-bin/mapserv?map=/home/matthijsln/geo-ov/transmodel_connexxion_edit.map&amp;version=1.1.1&amp;service=WMS&amp;request=GetLegendGraphic&amp;layer=signaalgroepen&amp;format=image/png"/></div>
        <input type="checkbox" checked="checked" onclick="toggleLayer('triggerpunten');"/>Triggerpunten<br/>
        <div id="triggerpunten"><img src="http://x13.b3p.nl/cgi-bin/mapserv?map=/home/matthijsln/geo-ov/transmodel_connexxion_edit.map&amp;version=1.1.1&amp;service=WMS&amp;request=GetLegendGraphic&amp;layer=triggerpunten&amp;format=image/png"/></div><br/>
        <strong>OV-informatie</strong><br/>
        <input type="checkbox" onclick="toggleLayer('buslijnen');"/>Buslijnen<br/>
        <div style="display:none;" id="buslijnen"><img src="http://x13.b3p.nl/cgi-bin/mapserv?map=/home/matthijsln/geo-ov/transmodel_connexxion_edit.map&amp;version=1.1.1&amp;service=WMS&amp;request=GetLegendGraphic&amp;layer=buslijnen&amp;format=image/png"/></div>
        <input type="checkbox" onclick="toggleLayer('bushaltes');"/>Bushaltes<br/>
        <div style="display:none;" id="bushaltes"><img src="http://x13.b3p.nl/cgi-bin/mapserv?map=/home/matthijsln/geo-ov/transmodel_connexxion_edit.map&amp;version=1.1.1&amp;service=WMS&amp;request=GetLegendGraphic&amp;layer=bushaltes_symbol&amp;format=image/png"/></div><br/>
        <strong>Achtergrond</strong><br/>
        <input type="checkbox" onclick="toggleLayer('Luchtfoto');"/>Luchtfoto<br/>
        <input type="checkbox" checked="checked" onclick="toggleLayer('BRT');"/>BRT<br/>
    </div>
    <div id="search" style="width:20%;float:left;">
        <br/>
        <hr style="border:1px #000 dotted;"/>
        <strong>Zoek</strong><br/>
        <input type="text" id="searchField"/>
    </div>
	<script type="text/javascript">
      var oc = new ol();
      oc.createMap('map');
      oc.addLayer("TMS","BRT",'http://geodata.nationaalgeoregister.nl/tiles/service/tms/1.0.0','brtachtergrondkaart', true, 'png8');
      oc.addLayer("TMS","Luchtfoto",'http://luchtfoto.services.gbo-provincies.nl/tilecache/tilecache.aspx/','IPOlufo', false,'png?LAYERS=IPOlufo');
      oc.addLayer("WMS","walapparatuur","http://x13.b3p.nl/cgi-bin/mapserv?map=/home/matthijsln/geo-ov/transmodel_connexxion_edit.map",'walapparatuur', true);
      oc.addLayer("WMS","signaalgroepen","http://x13.b3p.nl/cgi-bin/mapserv?map=/home/matthijsln/geo-ov/transmodel_connexxion_edit.map",'signaalgroepen', true);
      oc.addLayer("WMS","triggerpunten","http://x13.b3p.nl/cgi-bin/mapserv?map=/home/matthijsln/geo-ov/transmodel_connexxion_edit.map",'triggerpunten', true);
      oc.addLayer("WMS","buslijnen","http://x13.b3p.nl/cgi-bin/mapserv?map=/home/matthijsln/geo-ov/transmodel_connexxion_edit.map",'buslijnen', false);
      oc.addLayer("WMS","bushaltes","http://x13.b3p.nl/cgi-bin/mapserv?map=/home/matthijsln/geo-ov/transmodel_connexxion_edit.map",'bushaltes', false);
      
      function toggleLayer(layer){
          var legend = document.getElementById(layer);
          var visible = oc.isLayerVisible(layer);
          
          oc.setLayerVisible(layer,!visible);
          if(legend){
              var attr = !visible ?  'block' : 'none' ;
              legend.setAttribute("style", 'display:' +attr);
          }
      }
	</script>
</div>

 </body>
</html:html>

