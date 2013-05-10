<%--
 Geo-OV - applicatie voor het registreren van KAR meldpunten               
                                                                           
 Copyright (C) 2009-2013 B3Partners B.V.                                   
                                                                           
 This program is free software: you can redistribute it and/or modify      
 it under the terms of the GNU Affero General Public License as            
 published by the Free Software Foundation, either version 3 of the        
 License, or (at your option) any later version.                           
                                                                           
 This program is distributed in the hope that it will be useful,           
 but WITHOUT ANY WARRANTY; without even the implied warranty of            
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the              
 GNU Affero General Public License for more details.                       
                                                                           
 You should have received a copy of the GNU Affero General Public License  
 along with this program. If not, see <http://www.gnu.org/licenses/>.      
--%>

<%@include file="/WEB-INF/jsp/taglibs.jsp" %>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page errorPage="/WEB-INF/jsp/commons/errorpage.jsp" %>

<stripes:layout-render name="/WEB-INF/jsp/commons/siteTemplate.jsp">

    <stripes:layout-component name="headerlinks" >
        <%@include file="/WEB-INF/jsp/commons/headerlinks.jsp" %>

    </stripes:layout-component>
    <stripes:layout-component name="content">


        <script type="text/javascript">
            var profile = {
            };

            <c:if test="${!empty actionBean.gebruiker.profile}">
            profile = ${actionBean.gebruiker.profile};
            </c:if>
        </script>

        <script type="text/javascript" src="${contextPath}/js/profilestate.js"></script>
        <script type="text/javascript" src="${contextPath}/js/settings.js"></script>
        <div id="body">
            <c:choose>
                <c:when test="${actionBean.deelgebied.id == null}">
                    <h1>Nieuw deelgebied</h1>
                </c:when>
                <c:otherwise>
                    <h1>Deelgebied bewerken</h1>
                </c:otherwise>
            </c:choose>

            <stripes:form beanclass="nl.b3p.kar.stripes.ExportActionBean">
                <stripes:hidden name="deelgebied" />
                <stripes:hidden name="geom" id="geom"/>
                Naam: <stripes:text name="deelgebied.name">asdf</stripes:text> <br/>
                Definieer een gebied:
                <div id="kaart" style="width:400px;height:400px;">
                    <div id="map" style="width: 100%; height: 100%;"></div>
                </div>
                <button onclick="drawArea();
                    return false;">Teken deelgebied</button>
                <button onclick="resetArea();
                    return false;">Reset</button> <br/>
                <stripes:submit name="saveDeelgebied">Opslaan</stripes:submit>
            </stripes:form>

        </div>

        <script>
                var vector = new OpenLayers.Layer.Vector("deelgebied");
                var map = new OpenLayers.Map({
                    div : "map",
                    layers : [
                        new OpenLayers.Layer.TMS(
                                "osm-rd-TMS",
                                "http://openbasiskaart.nl/mapcache/tms/",
                                {
                                    layername : 'osm@rd',
                                    type : "png",
                                    serviceVersion : "1.0.0",
                                    gutter : 0,
                                    buffer : 0,
                                    isBaseLayer : true,
                                    transitionEffect : 'resize',
                                    tileOrigin : new OpenLayers.LonLat(-285401.920000,22598.080000),
                                    resolutions : [3440.63999999999987267074,1720.31999999999993633537,860.15999999999996816769,430.07999999999998408384,215.03999999999999204192,107.51999999999999602096,53.75999999999999801048,26.87999999999999900524,13.43999999999999950262,6.71999999999999975131,3.35999999999999987566,1.67999999999999989342,0.84000000000000003553,0.42000000000000001776,0.21000000000000000888],
                                    zoomOffset : 0,
                                    units : "m",
                                    maxExtent : new OpenLayers.Bounds(-285401.920000,22598.080000,595401.920000,903401.920000),
                                    projection : new OpenLayers.Projection("epsg:28992".toUpperCase()),
                                    sphericalMercator : false
                                }
                        ),
                        vector
                    ]
                });
                //map.addLayer(osm_rd_tms_layer);
                var draw = new OpenLayers.Control.DrawFeature(vector,OpenLayers.Handler.Polygon,{
                    displayClass : 'olControlDrawFeaturePoint',
                    featureAdded : featureAdded
                }
                );
                map.addControl(draw);
                if(!map.getCenter())
                    map.zoomToMaxExtent();
                map.zoomTo(2);


                function drawArea (){
                    draw.activate();
                }

                function resetArea (){
                    vector.removeAllFeatures();
                }

                function featureAdded (feature){
                    var geom = feature.geometry.toString();
                    Ext.get("geom").dom.value = geom;
                    draw.deactivate();
                }
                var testgeom = new OpenLayers.Geometry.fromWKT("POLYGON((92208.32 447517.12,85327.04 405369.28,123174.08 407949.76,141237.44 449237.44,92208.32 447517.12))");
                draw.drawFeature(testgeom);
        </script>
    </stripes:layout-component>
</stripes:layout-render>