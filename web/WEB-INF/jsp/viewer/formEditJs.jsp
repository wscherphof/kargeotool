<%@include file="/WEB-INF/taglibs.jsp" %>

<div style="margin-top: 4px; height: 60px">
    <c:set var="savedLocationStatusHtml">
        <c:if test="${empty point}">Nog geen locatie.</c:if>
        <c:if test="${!empty point}">Coordinaten: <span id='location'>${point}</span></c:if>
    </c:set>
    <b>Locatie</b>${notEditable}<br>
    <br>
    <span id="locationStatus" style="font-weight: bold">${savedLocationStatusHtml}</span>
    <input title="Zoom in op de locatie van het walapparaat waarvoor u data wilt invoeren en wijs aan waar het walapparaat zich bevindt. Voor VRI's kunt u ook het midden van het kruispunt aanwijzen. Na het selecteren van een locatie op de kaart is het ook mogelijk coördinaten in te voeren in het venster Locatie." id="newLocation" ${notEditable ? 'disabled="disabled"' : ''} type="button" ${!empty point ? 'style="display: none"' : ""} value="Locatie aanwijzen in kaart" onclick="newLocationClicked();">
    <input id="reset" type="button" title="Selecteer deze optie om de oorspronkelijke locatie te herstellen." style="display: none" value="Reset" onclick="resetClicked();">
    <input id="changeLocation" title="Selecteer deze optie om de locatie te wijzigen. Wijzigen kan vervolgens door het punt in de kaart te verplaatsen of door de coördinaten in het venster Locatie in te vullen. De locatie wissen en daarna opnieuw aanwijzen in de kaart is ook een manier om de locatie te wijzigen." ${notEditable ? 'disabled="disabled"' : ''} type="button" ${empty point ? 'style="display: none"' : ""} value="Locatie wijzigen" onclick="changeLocationClicked();">
    <input id="deleteLocation" title="Selecteer deze optie om de locatie te wissen. Vervolgens zal de optie Locatie aanwijzen in kaart weer beschikbaar komen." ${notEditable ? 'disabled="disabled"' : ''} type="button" ${empty point ? 'style="display: none"' : ""} value="Wissen" onclick="deleteLocationClicked();">
    <br>
</div>

    <script type="text/javascript">
        var savedLocationStatusHtml = "${savedLocationStatusHtml}";
        <c:if test="${point == null}">
            var oldLocation = null;
        </c:if>
        <c:if test="${point != null}">
            var oldLocation = [${point}];
        </c:if>
        var newLocation;

        <c:if test="${locationUpdated}">
            parent.flamingo.callMethod("map_kar_layer", "update");
        </c:if>

        parent.flamingo_cancelEdit();

        function resetClicked() {
            parent.flamingo_cancelEdit();
            newLocation = null;
            document.forms[0].location.value = "";
            document.getElementById("locationStatus").innerHTML = savedLocationStatusHtml;
            document.getElementById("reset").style.display = "none";
            document.getElementById("newLocation").style.display = "none";
            if(oldLocation != null) {
                document.getElementById("changeLocation").style.display = "inline";
                document.getElementById("deleteLocation").style.display = "inline";
            } else {
                document.getElementById("newLocation").style.display = "inline";
            }
        }

        function newLocationClicked() {
            parent.selectLocationClicked('${layer}', null, '${geometryType}');
            document.getElementById("locationStatus").innerHTML = "Klik op een plek op de kaart...";
            document.getElementById("reset").style.display = "inline";
            document.getElementById("newLocation").style.display = "none";
        }

        function changeLocationClicked() {
            var loc = oldLocation;
            if(newLocation != null) {
                loc = [newLocation.x, newLocation.y];
            }
            parent.selectLocationClicked('${layer}', loc, '${geometryType}');
            document.getElementById("locationStatus").innerHTML = "Verplaats het punt in de kaart...";
            document.getElementById("reset").style.display = "inline";
            document.getElementById("changeLocation").style.display = "none";
            document.getElementById("deleteLocation").style.display = "none";
        }

        function deleteLocationClicked() {
            newLocation = null;
            document.getElementById("locationStatus").innerHTML = "Coordinaten: <span id='location' class='changed'>gewist</span>";
            document.forms[0].location.value = "delete";
            document.getElementById("changeLocation").style.display = "none";
            document.getElementById("deleteLocation").style.display = "none";
            document.getElementById("newLocation").style.display = "inline";
            document.getElementById("reset").style.display = "inline";
        }

        function flamingo_onCreatePointAtDistanceFinished(obj, geometry, pathLength) {
            <c:if test="${geometryType == 'PointAtDistance'}">
                document.forms[0].karDistanceTillStopLine.value = "" + pathLength.toFixed();
                document.getElementById("distanceLabel").style.color = "red";
            </c:if>
        }

        function flamingo_onGeometryDrawFinished(obj, geometry) {
            flamingo_onGeometryDrawUpdate(obj, geometry);
        }

        function flamingo_onGeometryDrawUpdate(obj, geometry) {
            if(geometry.indexOf("POINT") == 0) {
                var xy = geometry.slice(6, geometry.length-1);
                xy = xy.split(' ');
                var x = Number(xy[0]).toFixed();
                var y = Number(xy[1]).toFixed();
                newLocation = {x: x, y: y};
                document.forms[0].location.value = newLocation.x + " " + newLocation.y;
                document.getElementById("locationStatus").innerHTML =
                    "Coordinaten: <span id='location' class='changed'>"
                    + newLocation.x + ", " + newLocation.y + "</span>";
            }
        }

    </script>
