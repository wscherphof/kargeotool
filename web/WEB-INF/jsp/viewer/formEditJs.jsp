<%@include file="/WEB-INF/taglibs.jsp" %>

    <script type="text/javascript">
        var prevLocationStatusHtml = "${prevLocationStatusHtml}";
        var haveLocation = ${!empty point};
        var haveNewLocation = false;
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

        function cancelClick() {
            parent.flamingo_cancelEdit();
            document.getElementById("locationStatus").innerHTML = prevLocationStatusHtml;
            document.getElementById("ok").style.display = "none";
            document.getElementById("cancel").style.display = "none";
            if(haveLocation) {
                document.getElementById("changeLocation").style.display = "inline";
                document.getElementById("deleteLocation").style.display = "inline";
            } else {
                document.getElementById("newLocation").style.display = "inline";
            }
        }

        function newLocationClicked() {
            parent.selectLocationClicked('${layer}', null, '${geometryType}');
            prevLocationStatusHtml = document.getElementById("locationStatus").innerHTML;
            document.getElementById("locationStatus").innerHTML = "Klik op een plek op de kaart...";
            document.getElementById("ok").style.display = "inline";
            document.getElementById("cancel").style.display = "inline";
            document.getElementById("newLocation").style.display = "none";
        }

        function changeLocationClicked() {
            var loc = oldLocation;
            if(haveNewLocation) {
                loc = [newLocation.x, newLocation.y];
            }
            parent.selectLocationClicked('${layer}', loc, '${geometryType}');
            prevLocationStatusHtml = document.getElementById("locationStatus").innerHTML;
            document.getElementById("locationStatus").innerHTML = "Verplaats het punt in de kaart...";
            document.getElementById("ok").style.display = "inline";
            document.getElementById("cancel").style.display = "inline";
            document.getElementById("changeLocation").style.display = "none";
            document.getElementById("deleteLocation").style.display = "none";
        }

        function deleteLocationClicked() {
            document.getElementById("locationStatus").innerHTML = "Coordinaten: <span id='location' class='changed'>gewist</span>";
            prevLocationStatusHtml = document.getElementById("locationStatus").innerHTML;
            document.forms[0].location.value = "delete";
            haveLocation = false;
            haveNewLocation = false;
            document.getElementById("changeLocation").style.display = "none";
            document.getElementById("deleteLocation").style.display = "none";
            document.getElementById("newLocation").style.display = "inline";
        }

        function okClicked() {
            if(newLocation) {
                haveLocation = true;
                haveNewLocation = true;
                prevLocationStatusHtml =
                    "Coordinaten: <span id='location' class='changed'>"
                    + newLocation.x + ", " + newLocation.y + "</span>";
                document.forms[0].location.value = newLocation.x + " " + newLocation.y;
            }
            cancelClick();
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
                document.getElementById("locationStatus").innerHTML =
                    "Coordinaten: <span id='location' class='changed'>"
                    + newLocation.x + ", " + newLocation.y + "</span>";
            }
        }

    </script>
