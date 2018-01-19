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
            <stripes:form beanclass="nl.b3p.kar.stripes.ExportActionBean" class="flex-wrapper">
                <stripes:messages/>
                <stripes:errors/>
                <stripes:hidden name="deelgebied" value="${actionBean.deelgebied.id}" />
                <stripes:hidden name="geom" id="geom" value="${actionBean.deelgebied.geom}"/>
                <c:choose>
                    <c:when test="${actionBean.deelgebied.id == null}">
                        <h1>Nieuw deelgebied</h1>
                    </c:when>
                    <c:otherwise>
                        <h1>Deelgebied bewerken</h1>
                    </c:otherwise>
                </c:choose>
                <div class="add-padding">
                    Naam: <stripes:text name="deelgebied.name"></stripes:text> <br/>
                    Definieer een gebied:
                </div>
                <div id="kaart" class="flex">
                    <div id="map" style="width: 100%; height: 100%;border:1px solid #000;"></div>
                </div>
                <div id="map-buttons" class="add-padding">
                    <button onclick="drawArea();return false;">Teken deelgebied</button>
                    <button onclick="resetArea();return false;">Reset</button> <br/>
                    <stripes:submit name="saveDeelgebied">Opslaan</stripes:submit>
                </div>
            </stripes:form>

        </div>

        <script>
                var draw =null;
                var map = null;
                var vector = null;
                var wkt = "${actionBean.deelgebied.geom}";
                function loadMap (){
                    var brt = new OpenLayers.Layer.TMS('BRT','http://geodata.nationaalgeoregister.nl/tiles/service/tms/',{
                        layername : 'brtachtergrondkaart',
                        type : 'png8',
                        maxExtent : new OpenLayers.Bounds(-285401.920000,22598.080000,595401.920000,903401.920000),
                        projection : new OpenLayers.Projection("epsg:28992".toUpperCase()),
                        isBaseLayer : true,
                        serverResolutions : [3440.64,1720.32,860.16,430.08,215.04,107.52,53.76,26.88,13.44,6.72,3.36,1.68,0.84,0.42,0.21],
                        tileOrigin : new OpenLayers.LonLat(-285401.920000,22598.080000)
                    });

                    vector = new OpenLayers.Layer.Vector("deelgebied");
                    map = new OpenLayers.Map({
                        resolutions : [860.16,430.08,215.04,107.52,53.76,26.88,13.44,6.72,3.36,1.68,0.84,0.42,0.21,0.105,0.0525],
                        units : 'm',
                        div : "map",
                        layers : [
                            brt,
                            vector
                        ]
                    });
                    draw = new OpenLayers.Control.DrawFeature(vector,OpenLayers.Handler.Polygon,{
                        displayClass : 'olControlDrawFeaturePoint',
                        featureAdded : featureAdded
                    });
                    
                    map.addControl(draw);
                    if(!map.getCenter()){
                        map.zoomToMaxExtent();
                    }
                    if(wkt){
                        var feat = new OpenLayers.Geometry.fromWKT(wkt);
                        draw.drawFeature(feat);
                    }
                }

                function drawArea (){
                    resetArea();
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
                
                Ext.onReady(function (){
                    loadMap();
                });
        </script>
    </stripes:layout-component>
</stripes:layout-render>