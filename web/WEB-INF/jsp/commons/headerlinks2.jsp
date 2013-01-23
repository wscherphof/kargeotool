<%@include file="/WEB-INF/jsp/taglibs.jsp" %>
<%@page errorPage="/WEB-INF/jsp/commons/errorpage.jsp" %>

<div id="header">
    <div id="headerLinks">
        <div class="headerlink">
            <a href="<stripes:url beanclass="nl.b3p.kar.stripes.EditorActionBean"/>"><img src="<c:url value="/images/"/>/pencil.png" alt="icon" class="navimg" /> Beheer VRI-informatie</a>
        </div>
        <c:if test="${f:isUserInRole(pageContext.request, 'beheerder')}">
            <div class="headerlink">
                <stripes:link beanclass="nl.b3p.kar.stripes.GebruikersActionBean"><img src="<c:url value="/images/"/>/users.png" alt="icon" class="navimg" /> Beheer gebruikers</stripes:link>
            </div>
        </c:if>
        <div class="headerlink">
            <span style="font-weight: normal">Ingelogd als: </span>
            <c:out value="${pageContext.request.remoteUser}"/>
        </div>
        <div class="headerlink">
            <stripes:link href="/logout.jsp"><img src="<c:url value="/images/"/>/exit.png" alt="icon" class="navimg" /> Uitloggen</stripes:link>
        </div>
    </div>
    <div id="headerTitle">Geo OV platform</div>
</div>
