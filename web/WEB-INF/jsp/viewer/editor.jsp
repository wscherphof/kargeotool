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

    <stripes:layout-component name="head">
        <script type="text/javascript" src="${contextPath}/js/proj4js/proj4js-compressed.js"></script>
        <script type="text/javascript">
            Proj4js.defs["EPSG:28992"] = "+proj=sterea +lat_0=52.15616055555555 +lon_0=5.38763888888889 +k=0.9999079 +x_0=155000 +y_0=463000 +ellps=bessel +towgs84=565.237,50.0087,465.658,-0.406857,0.350733,-1.87035,4.0812 +units=m +no_defs";
            Proj4js.defs["EPSG:4236"] = "+proj=longlat +ellps=WGS84 +datum=WGS84 +no_defs ";            
        </script>
    </stripes:layout-component>
    <stripes:layout-component name="headerlinks" >
        <%@include file="/WEB-INF/jsp/commons/headerlinks.jsp" %>

    </stripes:layout-component>
    <stripes:layout-component name="content">

        <script type="text/javascript" src="${contextPath}/js/layout.js"></script>

        <div id="leftbar">
            
            <div id="searchform" style="margin: 3px">
                Zoeken: <input id="searchField" name="searchField"><input type="button" value="Zoeken" onclick="editor.search.search(Ext.get('searchField').getValue());"><br/>
                <div id="geocoderesults"></div>
                <div id="rseqresults"></div>
            </div>
            
            <div id="rseqInfoPanel">
                <div style="margin: 3px">
                    Huidig geselecteerde VRI: <span id="context_vri"></span><img id="memo_vri" src="${contextPath}/images/silk/attach.png" OnMouseOut="this.style.cursor='default';" OnMouseOver="this.style.cursor='pointer';" style="visibility: hidden;" onclick="editor.addMemo()"/>
                    <p><br>
                    <div id="rseqOptions" style="visibility: hidden">
                        <input type="button" id="rseqSave" value="Opslaan" onclick="editor.saveOrUpdate()">
                    </div>
                    <p><br>
                    <div id="overzicht" style="margin-top: 5px; font-size: 10pt">
                    </div>
                </div>
            </div>
            
            <div id="form" style="margin: 3px">
                <div id="help">
                    Klik op een icoon van een verkeerssysteem om deze te selecteren
                    of klik rechts om een verkeerssysteem toe te voegen.
                </div>
            </div>

        </div>

        <div id="kaart">
            <div id="map" style="width: 100%; height: 100%;"></div>
            <!--div id="overview" style="width:19%;border:1px solid #000; float:right; height:300px;overflow:hidden;"></div-->
        </div>
        
        <div id="rightbar">
            <div id="legend">
                <div id="walapparatuur" class="legendseparator">
                    <b>Verkeerssystemen</b><br/>
                    <img src="<c:url value="/images/"/>icons/vri.png" alt="VRI" class="legendimg" /> VRI<br />
                    <img src="<c:url value="/images/"/>icons/wri.png" alt="Waarschuwingssysteem" class="legendimg" /> Waarschuwingssysteem<br />
                    <img src="<c:url value="/images/"/>icons/afsluitingssysteem.png" alt="Afsluitingssysteem" class="legendimg" /> Afsluitingssysteem<br />
                </div>
                <div id="triggerpunten" class="legendseparator">
                    <b>Punten</b><br/>
                    <img src="<c:url value="/images/"/>/icons/radio_zwart.png" alt="Onbekend" class="legendimg" /> Onbekend<br />
                    <img src="<c:url value="/images/"/>/icons/radio_groen.png" alt="Inmeldpunt" class="legendimg" /> Inmeldpunt<br />
                    <img src="<c:url value="/images/"/>/icons/radio_rood.png" alt="Uitmeldpunt" class="legendimg" /> Uitmeldpunt<br />
                    <img src="<c:url value="/images/"/>/icons/radio_blauw.png" alt="Voorinnmeldpunt" class="legendimg" /> Voorinnmeldpunt<br />
                </div>
                <div id="starteindpunten" class="legendseparator">
                    <b>Begin- en eindpunten</b><br/>
                    <img src="<c:url value="/images/"/>/icons/beginpunt.png" alt="Beginpunt" class="legendimg" /> Beginpunt<br />
                    <img src="<c:url value="/images/"/>/icons/eindpunt.png" alt="Eindpunt" class="legendimg" /> Eindpunt<br />
                </div><br/>
                <b>OV-informatie</b><br/>
                <input type="checkbox" onclick="toggleLayer('buslijnen');"/> Buslijnen<br/>
                <div style="display:none;" id="buslijnen"><img src="http://x13.b3p.nl/cgi-bin/mapserv?map=/home/matthijsln/geo-ov/transmodel_connexxion.map&amp;version=1.1.1&amp;service=WMS&amp;request=GetLegendGraphic&amp;layer=buslijnen&amp;format=image/png"/></div>
                <input type="checkbox" onclick="toggleLayer('bushaltes');"/> Bushaltes<br/>
                <div style="display:none;" id="bushaltes"><img src="http://x13.b3p.nl/cgi-bin/mapserv?map=/home/matthijsln/geo-ov/transmodel_connexxion.map&amp;version=1.1.1&amp;service=WMS&amp;request=GetLegendGraphic&amp;layer=bushaltes_symbol&amp;format=image/png"/></div><br/>
                <b>Achtergrond</b><br/>
                <input type="checkbox" onclick="toggleLayer('Luchtfoto');"/> Luchtfoto<br/>
                <input type="checkbox" checked="checked" onclick="toggleLayer('BRT');"/> BRT<br/>
                <br/>
                <b>Extra</b><br/>
                <input type="checkbox" id="snapRoads" onclick="toggleRoad(this)"/> Wegen<br/>
            </div>
                
                
            <script type="text/javascript" src="${contextPath}/js/forms.js"></script>
            <script type="text/javascript" src="<c:url value="/js/editor.js"/>"></script>
            <script type="text/javascript">

                var mapfilePath = "http://x13.b3p.nl/cgi-bin/mapserv?map=/home/matthijsln/geo-ov/transmodel_connexxion.map";

                var editorActionBeanUrl = "<stripes:url beanclass="nl.b3p.kar.stripes.EditorActionBean" />";
                var searchActionBeanUrl = "<stripes:url beanclass="nl.b3p.kar.stripes.SearchActionBean"/>";

                var contextPath = "${contextPath}";
                var editor = null;
                Ext.onReady(function() {
                    editor = Ext.create(Editor, "map", mapfilePath);    
                });
                
                var vehicleTypes = ${actionBean.vehicleTypesJSON};
                var dataOwners = ${actionBean.dataOwnersJSON};
                
                function toggleLayer(layer) {
                    var legend = document.getElementById(layer);
                    var visible = editor.olc.isLayerVisible(layer);

                    editor.olc.setLayerVisible(layer,!visible);
                    if(legend){
                        var attr = !visible ?  'block' : 'none' ;
                        legend.setAttribute("style", 'display:' +attr);
                    }
                }
                
                function toggleRoad(form){
                    var activate = form.checked;
                    if(activate){
                        editor.loadRoads();
                    }else{
                        editor.removeRoads();
                    }
                }
                    
            </script>
        </div>

    </stripes:layout-component>
</stripes:layout-render>