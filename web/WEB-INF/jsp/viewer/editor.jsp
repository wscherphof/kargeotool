<%@include file="/WEB-INF/taglibs.jsp" %>
<%@page errorPage="/WEB-INF/jsp/commons/errorpage.jsp" %>

<tiles:importAttribute/>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<html:html>
    <head>
        <title>KAR in GIS</title>
        <script language="JavaScript" type="text/JavaScript" src="<html:rewrite page="/js/validation.jsp" module=""/>"></script>
        <link rel="stylesheet" href="<html:rewrite page="/styles/kar-gis.css" module=""/>" type="text/css" media="screen" />
		<link rel="stylesheet" href="<html:rewrite page="/styles/kar-gis-design.css" module=""/>" type="text/css" media="screen" />
        <script type="text/javascript" src="<html:rewrite page="/js/json2.js" module=""/>"></script>
        <script type="text/javascript" src="<html:rewrite page="/js/utils.js" module=""/>"></script>
        <script type="text/javascript" src="<html:rewrite page="/js/simple_treeview.js" module=""/>"></script>
        <script type="text/javascript" src="<html:rewrite page="/dwr/engine.js" module=""/>"></script>
        <script type="text/javascript" src="<html:rewrite page="/dwr/interface/Editor.js" module=""/>"></script>
        <script type="text/javascript" src="<html:rewrite page="/js/swfobject.js" module=""/>"></script>
        
        <!--[if lte IE 6]>
            <link href="<html:rewrite page="/styles/kar-gis-design-ie6.css" module=""/>" rel="stylesheet" media="screen" type="text/css" />
            <script type="text/javascript" src="<html:rewrite page="/js/ie6fixes.js" module=""/>"></script>
        <![endif]-->
        <!--[if IE 7]> <link href="<html:rewrite page="/styles/kar-gis-design-ie7.css" module=""/>" rel="stylesheet" media="screen" type="text/css" /> <![endif]-->

    </head>
    <body class="editor" id="body_editor">
        <div id="header">
            <div id="headerTitle">KAR in GIS</div>
        </div>

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
            Editor.getObjectInfo(type, id, dwr_objectInfoReceived);
        }        
    }

    function treeItemClick(item) {
        tree_selectObject(item);
        form_editObject(item);
        if(document.getElementById("autoZoom").checked) {
            options_zoomToObject();
        }
    }

    function createLabel(container, item) {
        container.className = "node";

        var a = document.createElement("a");
        a.href = "#";
        a.onclick = function() {
            treeItemClick(item);
        };

        var idSpan = document.createElement("span");
        idSpan.className = "code";
        idSpan.appendChild(document.createTextNode(item.type.toUpperCase()));
        a.appendChild(idSpan);
        var labelText = " " + item.name;
        a.appendChild(document.createTextNode(labelText));
        container.appendChild(a);
    }

    var tree;
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
            "saveExpandedState": false,
            "saveScrollState": false,
            "expandAll": false
        });
    }

    function dwr_objectInfoReceived(info) {
        if(info.toLowerCase().indexOf("error") == 0) {
            alert(info);
            return;
        }
        
        var obj = eval("(" + info + ")");
        tree = eval("(" + obj.tree + ")");

        deselectObject();
        createTreeview();

        if(obj.object != undefined) {
            setStatus("tree", "Object op locatie geselecteerd");

            var object = treeview_findItem(tree, obj.object);
            tree_selectObject(object);
            form_editObject(object);
            if(document.getElementById("autoZoom").checked) {
                options_zoomToObject();
            }
        } else {
            setStatus("tree", "Meerdere objecten op locatie");
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
        
        document.getElementById("newAg").disabled = object.type != "rseq";
        document.getElementById("newA").disabled = object.type != "ag";

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
        window.frames["form"].location = "about:blank";
    }

    function clear() {
        deselectObject();
        clearTreeview();
    }

    function flamingo_map_onIdentifyData(map, layer, data, identifyextent,nridentified, total) {
        if("map_kar_punten" == layer) {
            var features = data["kar_punten"];
            if(features != undefined) {
                Editor.getMultipleKarPuntInfo(JSON.stringify(features), dwr_objectInfoReceived);
            }
        }
    }

    function flamingo_moveToExtent(minx, miny, maxx, maxy){
        flamingo.callMethod("map", "moveToExtent", {
                minx: minx,
                miny: miny,
                maxx: maxx,
                maxy: maxy}
        , 0);
    }

    function setStatus(what, status) {
        document.getElementById(what + "Status").innerHTML = escapeHTML(status);
    }

    function form_editObject(object) {
        var url;
        switch(object.type) {
            case "a" : url = "<html:rewrite page="/activation.do"/>"; break;
            case "ag": url = "<html:rewrite page="/activationGroup.do"/>"; break;
            case "rseq": url = "<html:rewrite page="/roadsideEquipment.do"/>"; break;
        }
        url = url + "?id=" + object.id.split(":")[1];
        setStatus("form", "form laden voor object " + object.id + ": " + url);
        window.frames["form"].location = url;
    }

    var zoomBorder = 80;

    function options_zoomToObject() {
        if(selectedObject != undefined && selectedObject != null) {
            if(selectedObject.point) {
                var xy = selectedObject.point.split(", ");
                var x = parseInt(xy[0]); var y = parseInt(xy[1]);
                flamingo_moveToExtent(x - zoomBorder, y - zoomBorder, x + zoomBorder, y + zoomBorder);
            }
        }
    }

    function zoomToEnvelope(envelope) {
        flamingo_moveToExtent(envelope.minX - zoomBorder, envelope.minY - zoomBorder, envelope.maxX + zoomBorder, envelope.maxY + zoomBorder);
    }

    function treeUpdate(cmd) {
        console.log("treeUpdate: " + cmd);

        var split = cmd.split(" ", 2);
        var action = split[0];
        cmd = split[1];

        if(action == "remove") {
            split = cmd.split(" ", 2);
            var id = split[0]
            cmd = split[1];
            if(selectedObject.id == id) {
                deselectObject();
            }
            
            alert("verwijder object " + id);
        }
    }

    function newRseq() {
        deselectObject();
        window.frames["form"].location = "<html:rewrite page="/roadsideEquipment.do?new=t"/>";
    }

    function newAg() {
        if(selectedObject.type != "rseq") {
            alert("Geen walapparatuur geselecteerd");
            return;
        }
        var rseqId = selectedObject.id.split(":")[1];
        deselectObject();
        window.frames["form"].location = "<html:rewrite page="/activationGroup.do?new=t"/>" + "&rseqId=" + rseqId;
    }

    function newA() {
        if(selectedObject.type != "ag") {
            alert("Geen signaalgroep geselecteerd");
            return;
        }
        var agId = selectedObject.id.split(":")[1];
        deselectObject();
        window.frames["form"].location = "<html:rewrite page="/activation.do?new=t"/>" + "&agId=" + agId;
    }

</script>

<div id="leftbar">

    <div id="tree">
		<div id="treeTop">
			<div id="treeTitel">Objectenboom</div>
            Status: <span id="treeStatus" style="font-weight: bold">Geen objecten geselecteerd</span><br />
			<input type="button" value="Test: Selecteer/zoek een object" onclick="testSelecteerObject()">
		</div>
        <div id="objectTree"></div>
        <div id="options">
            <input id="zoomButton" type="button" value="Zoom naar object" onclick="options_zoomToObject();">
            <label><input id="autoZoom" type="checkbox" value="autoZoom" checked="true">Auto-zoom</label>
            <input id="newRseq" type="button" value="Nieuwe walapparatuur" onclick="newRseq()">
            <br>
            <input id="newAg" type="button" value="Nieuwe signaalgroep" disabled="true" onclick="newAg()">
            <input id="newA" type="button" value="Nieuw inmeldpunt" disabled="true" onclick="newA()">
        </div>
    </div>

    <div id="form">
		<div id="form_container">
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

