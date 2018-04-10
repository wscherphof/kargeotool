<%--
 KAR Geo Tool - applicatie voor het registreren van KAR meldpunten               
                                                                           
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
            var wkt = "${actionBean.deelgebied.geom}";
            var mapfilePath = "${initParam['mapserver-url']}";
            var deelgebiedActionBeanUrl = "<stripes:url beanclass="nl.b3p.kar.stripes.DeelgebiedActionBean" />";
        </script>

        <script type="text/javascript" src="${contextPath}/js/profilestate.js"></script>
        <script type="text/javascript" src="${contextPath}/js/settings.js"></script>
        <script type="text/javascript" src="${contextPath}/js/deelgebied.js"></script>
        <div id="body">
            <stripes:form beanclass="nl.b3p.kar.stripes.DeelgebiedActionBean" class="flex-wrapper">
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
                <div id="map-buttons" class="add-padding">
                    <stripes:button name="draw" onclick="drawArea();return false;">Teken deelgebied</stripes:button>
                    <stripes:reset name="reset" onclick="resetArea();return false;">Reset</stripes:reset>
                    <stripes:submit name="saveDeelgebied">Opslaan</stripes:submit><br/>
                </div>
                <div id="kaart" class="flex">
                    <div id="map" style="width: 100%; height: 80%;border:1px solid #000;"></div>
                </div>
            </stripes:form>
        </div>
    </stripes:layout-component>
</stripes:layout-render>