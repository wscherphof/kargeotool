<%@include file="/WEB-INF/taglibs.jsp" %>
<%@page errorPage="/WEB-INF/jsp/commons/errorpage.jsp" %>

<div id="header">
    <div id="headerLinks">
        <html:link page="/editor.do" module="/viewer">Beheer VRI-informatie</html:link> |
        <c:if test="${f:isUserInRole(pageContext.request, 'beheerder')}">
            <html:link page="/gebruikers.do" module="/beheer">Beheer gebruikers</html:link> |
        </c:if>
        <span style="font-weight: normal">Ingelogd als: </span>
        <c:out value="${pageContext.request.remoteUser}"/> | 
        <html:link page="/logout.do" module="">Uitloggen</html:link>
    </div>
    <div id="headerTitle">Geo OV platform</div>
</div>
