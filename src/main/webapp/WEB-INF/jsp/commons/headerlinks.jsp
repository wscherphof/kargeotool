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
<%@page errorPage="/WEB-INF/jsp/commons/errorpage.jsp" %>

<div id="header">
    <div id="headerLinks">
        <div class="headerlink">
            <a href="<stripes:url beanclass="nl.b3p.kar.stripes.EditorActionBean"/>"><img src="<c:url value="/images/"/>/pencil.png" alt="icon" class="navimg" /> Beheer VRI-informatie</a>
        </div>
        <div class="headerlink">
            <a href="<stripes:url beanclass="nl.b3p.kar.stripes.RightsActionBean"/>">Beheer VRI-toegang</a>
        </div>
        <c:if test="${f:isUserInRole(pageContext.request, 'beheerder')}">
            <div class="headerlink">
                <stripes:link beanclass="nl.b3p.kar.stripes.GebruikersActionBean"><img src="<c:url value="/images/"/>/users.png" alt="icon" class="navimg" /> Beheer gebruikers</stripes:link>
            </div>
        </c:if>
        <div class="headerlink">
            <a href="<stripes:url beanclass="nl.b3p.kar.stripes.ExportActionBean"/>">Exporteer</a>
        </div>
        <div class="headerlink">
            <a href="<stripes:url beanclass="nl.b3p.kar.stripes.ImportActionBean"/>">Importeer</a>
        </div>
        <div class="headerlink">
            <a href="<stripes:url beanclass="nl.b3p.kar.stripes.OverviewActionBean"/>">Berichtoverzicht</a>
        </div>
        <div class="headerlink">
            <a href="#" onclick="showDefaultAttributes();">Instellingen</a>
        </div>
        <div class="headerlink">
            <a href="#" onclick="showWelcome()"><img src="<c:url value="/images/silk/information.png"/>" alt="icon" class="navimg" /> Informatie</a>
        </div>
        <div class="headerlink">
            <span style="font-weight: normal">Ingelogd als: </span>
            <c:out value="${pageContext.request.remoteUser}"/>
            <c:if test="${f:isUserInRole(pageContext.request, 'beheerder')}">                                    
                <img src="${contextPath}/images/star.png" style="vertical-align: bottom">
            </c:if>
        </div>
        <div class="headerlink">
            <stripes:link href="/logout.jsp"><img src="<c:url value="/images/"/>/exit.png" alt="icon" class="navimg" /> Uitloggen</stripes:link>
        </div>
    </div>
    <div id="headerTitle">Kar Geo Tool</div>
</div>
