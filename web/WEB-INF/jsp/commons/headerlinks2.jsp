<%@include file="/WEB-INF/jsp/taglibs.jsp" %>
<%@page errorPage="/WEB-INF/jsp/commons/errorpage.jsp" %>

<div id="header">
    <div id="headerLinks">
        
        <a href="#" onclick="alert('Nog niet beschikbaar')">Beheer VRI-informatie</a> |
        <c:if test="${f:isUserInRole(pageContext.request, 'beheerder')}">
            <stripes:link beanclass="nl.b3p.kar.stripes.GebruikersActionBean">Beheer gebruikers</stripes:link> |
        </c:if>
        <span style="font-weight: normal">Ingelogd als: </span>
        <c:out value="${pageContext.request.remoteUser}"/> | 
        <stripes:link href="/logout.jsp">Uitloggen</stripes:link>
    </div>
    <div id="headerTitle">Geo OV platform</div>
</div>
