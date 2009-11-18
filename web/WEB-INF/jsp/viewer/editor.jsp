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
        <script type="text/javascript" src="<html:rewrite page="/js/json2.js" module=""/>"></script>
        <script type="text/javascript" src="<html:rewrite page="/js/utils.js" module=""/>"></script>
        <script type="text/javascript" src="<html:rewrite page="/js/simple_treeview.js" module=""/>"></script>
        <script type="text/javascript" src="<html:rewrite page="/dwr/engine.js" module=""/>"></script>
        <script type="text/javascript" src="<html:rewrite page="/dwr/interface/Editor.js" module=""/>"></script>
        <script type="text/javascript" src="<html:rewrite page="/js/swfobject.js" module=""/>"></script>
        <script type='text/javascript' src='<html:rewrite page="/js/flamingo/FlamingoController.js" module=""/>'></script>
        

        
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
        flamingo_hideIdentifyIcon();
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
            flamingo_hideIdentifyIcon();   
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
                flamingo_moveToExtent(x - zoomBorder, y - zoomBorder, x + zoomBorder, y + zoomBorder);
            }
        }
    }

    function zoomToEnvelope(envelope) {
        var zoomBorder = getZoomBorder();
        flamingo_moveToExtent(envelope.minX - zoomBorder, envelope.minY - zoomBorder, envelope.maxX + zoomBorder, envelope.maxY + zoomBorder);
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
    function selectLocationClicked(editLayer, currentLocation, geometryType) {

        if(currentLocation != null) {
            flamingo_editMapCreateNewGeometry(editLayer, geometryType, currentLocation);
        } else {
            flamingo_editMapDrawNewGeometry(editLayer, geometryType);
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

    /* Flamingo event handlers */

    function flamingo_map_onIdentifyData(map, layer, data, identifyextent, nridentified, total) {
        if("map_kar_layer" == layer) {
            if(data.bushaltes_symbol != undefined || data.buslijnen != undefined) {
                flamingo.callMethod("info", "show");
            }
            
            if(data.triggerpunten != undefined || data.signaalgroepen != undefined || data.walapparatuur != undefined) {
                document.getElementById("loading").style.visibility = "visible";
                    Editor.getIdentifyTree(JSON.stringify(data), dwr_treeInfoReceived);
            }
        }
    }

    function flamingo_drawMap_onGeometryDrawFinished(obj, geometry) {
        flamingo.callMethod("location", "show");
        window.frames["form"].flamingo_onGeometryDrawFinished(obj, geometry);
    }

    function flamingo_drawMap_onCreatePointAtDistanceFinished(obj, geometry, pathLength) {
        flamingo.callMethod("location", "show");
        window.frames["form"].flamingo_onCreatePointAtDistanceFinished(obj, geometry, pathLength);
    }

    function flamingo_drawMap_onGeometryDrawUpdate(obj, geometry) {
        flamingo.callMethod("location", "show");
        window.frames["form"].flamingo_onGeometryDrawUpdate(obj, geometry);
    }

    /* Flamingo callMethod wrappers */

    var editMap = "drawMap";

    // geometryType is Point, PointAtDistance, ...

    function flamingo_editMapCreateNewGeometry(editLayer, geometryType, geometry) {
        flamingo.callMethod(editMap, "editMapCreateNewGeometry", editLayer, geometryType, geometry);
    }

    function flamingo_editMapDrawNewGeometry(editLayer,geometryType) {
        flamingo.callMethod(editMap, "editMapDrawNewGeometry", editLayer, geometryType);
    }

    function flamingo_removeAllFeatures(editLayer) {
        flamingo.callMethod(editMap, "removeAllFeatures", editLayer);
    }

    function flamingo_moveToExtent(minx, miny, maxx, maxy){
        flamingo.callMethod("map", "moveToExtent", {minx: minx, miny: miny,  maxx: maxx, maxy: maxy}, 0);
    }

    function flamingo_cancelEdit() {
        flamingo_removeAllFeatures("draw_walapparatuur");
        flamingo_removeAllFeatures("draw_signaalgroepen");
        flamingo_removeAllFeatures("draw_triggerpunten");
        flamingo.callMethod("location", "hide");
        flamingo.callMethod("gis", "setCreateGeometry", null);
    }

    function flamingo_updateKarLayer() {
        flamingo.callMethod("map_kar_layer", "update", true);
    }

    function flamingo_hideIdentifyIcon() {
        flamingo.callMethod("map_identifyicon", "hide");
    }
    
    function flamingo_hideLayers(layers) {
        flamingo.callMethod("map_kar_layer", "setVisible", false, layers);
        <%-- niet nodig bij onInit; flamingo.callMethod("map_kar_layer", "update", true); --%>
    }

    function flamingo_map_kar_layer_onInit() {
        flamingo_hideLayers("bushaltes,buslijnen");
    }

    function  walapparaatnummerKeyPressed(e) {
        if(e.keyCode == 0xd) {
            zoekWalapparatuur();
        }
    }

    function zoekWalapparatuur() {
        var unitNumber = document.getElementById("walapparaatnummer").value;
        document.getElementById("loading").style.visibility = "visible";
        Editor.getRseqUnitNumberTree(unitNumber, dwr_treeInfoReceived);
    }

    setOnload(function() { document.getElementById("walapparaatnummer").focus(); });

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
        // sldstring = "http://localhost:8084/geo-ov/SldServlet";
        if(document.getElementById("showSelected").checked) {
            parameterGehad = false;

            // bouw de sld string op
            sldstring = addToSldstring(rseqId, "rseq", sldstring);
            sldstring = addToSldstring(agIds, "ag", sldstring);
            sldstring = addToSldstring(aIds, "a", sldstring);
        }
        console.log(sldstring);
        flamingo.callMethod("map_kar_layer","setConfig","<LayerOGWMS sld=\""+sldstring+"\"/>",true);
    }

    var parameterGehad = false;
    // HACK: Flamingo escaped de ampersands niet goed, dus worden ze nu hier alvast vervangen door %26
    // TODO: als leeg is, dan niet parameter vullen
    function addToSldstring(lijst, typeVis, sldstring){
        if(parameterGehad){
            sldstring +="%26"+ typeVis + "VisibleValues=";
        }else{
            sldstring +="?"+ typeVis + "VisibleValues=";
            parameterGehad = true;
        }
        if(lijst instanceof Array){
            if(lijst.length >0){
                var eerste = true;
                for(var i = 0 ; i < lijst.length ; i++){
                    if(eerste){
                        sldstring += lijst[i];
                        eerste = false;
                    }else{
                        sldstring += ","+lijst[i];
                    }
                }
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
        if(document.getElementById("showSelected").checked) {
     
            showSelected( roaEquId, actGroIds, actIds);
        }
        flamingo_updateKarLayer();
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
    <div id="flashcontent" style="width: 100%; height: 100%;"></div>
	<script type="text/javascript">
		var so = new SWFObject("<html:rewrite module="" page="/flamingo/flamingo.swf"/>?config=/config_editor.xml", "flamingo", "100%", "100%", "8", "#FFFFFF");
		so.write("flashcontent");
		var flamingo = document.getElementById("flamingo");
	</script>
</div>

 </body>
</html:html>

