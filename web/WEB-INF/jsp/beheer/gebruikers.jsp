<%@include file="/WEB-INF/taglibs.jsp" %>
<%@page errorPage="/WEB-INF/jsp/commons/errorpage.jsp" %>

<h1>Gebruikersbeheer</h1>

<c:set var="form" value="${gebruikersForm.map}"/>

<html:javascript formName="gebruikersForm" staticJavascript="false"/>
<script type="text/javascript" src="<html:rewrite module="" page="/js/table.js"/>"></script>

<html:form action="/gebruikers.do" method="POST" onsubmit="return validateGebruikersForm(this)">

<c:set var="focus" value="username" scope="request"/>
<tiles:insert definition="setFocus"/>

    <html:hidden property="id"/>
    <c:if test="${form.id != -1}">
        <html:hidden property="role"/>
        <html:hidden property="loc"/>
    </c:if>

<c:if test="${!empty gebruikers}">
    Aantal gebruikers: <b>${fn:length(gebruikers)}</b>
    <p>
    <table border="1" cellpadding="3" style="border-collapse: collapse" class="table-autosort:0 table-stripeclass:alternate">
        <thead>
            <tr>
                <th class="table-sortable:default">Gebruikersnaam</th>
                <th class="table-sortable:default">Naam</th>
                <th class="table-sortable:default">E-mail</th>
                <th class="table-sortable:numeric">Telefoonnummer</th>                
                <th></th>
            </tr>
        </thead>
        <tbody>
            <c:forEach var="g" varStatus="status" items="${gebruikers}">
                <c:set var="editLink"><html:rewrite page="/gebruikers.do?edit=t&amp;id=${g.id}"/></c:set>
                <c:set var="col" value=""/>
                <c:if test="${g.id == form.id}">
                    <c:set var="col" value="#cccccc"/>
                </c:if>
                <tr bgcolor="${col}">
                    <td><html:link href="${editLink}"><c:out value="${g.username}"/></html:link></td>
                    <td><c:out value="${g.fullname}"/></td>
                    <td><c:out value="${g.email}"/></td>
                    <td><c:out value="${g.phone}"/></td>
                    <td>
                        <%-- niet gebruiker zichzelf laten verwijderen --%>
                        <c:if test="${g.username != pageContext.request.userPrincipal.username}">
                            <html:link page="/gebruikers.do?delete=t&amp;id=${g.id}">
                                <html:img page="/images/delete.gif" altKey="button.remove" module="" border="0" vspace="2"/>
                            </html:link>
                        </c:if>
                    </td>
                </tr>
            </c:forEach>
        </tbody>
    </table>
<p>

    <c:if test="${!empty form.id}">
        <p>
        <c:choose>
            <c:when test="${!empty form.confirmAction}">
                <html:hidden property="confirmAction"/>
                <html:submit property="${form.confirmAction}"><fmt:message key="button.ok"/></html:submit>
                <html:cancel><fmt:message key="button.cancel"/></html:cancel>
            </c:when>
            <c:otherwise>
                <html:submit property="save"><fmt:message key="button.save"/></html:submit>
                <c:if test="${form.id != -1}">
                    <%-- niet gebruiker zichzelf laten verwijderen --%>
                    <c:if test="${form.id != pageContext.request.userPrincipal.id}">
                        <html:submit property="delete" onclick="bCancel=true;"><fmt:message key="button.remove"/></html:submit>
                    </c:if>
                </c:if>
                <html:cancel><fmt:message key="button.cancel"/></html:cancel>
            </c:otherwise>
        </c:choose>
        <p>
        <table>
            <tr>
                <td><fmt:message key="gebruiker.username"/></td>
                <td><html:text property="username" size="20" maxlength="30"/></td>
            </tr>
            <tr>
                <td><fmt:message key="gebruiker.fullName"/></td>
                <td><html:text property="fullName" size="30" maxlength="50"/></td>
            </tr>
            <tr>
                <td><fmt:message key="gebruiker.email"/></td>
                <td><html:text property="email" size="30" maxlength="50"/></td>
            </tr>
            <tr>
                <td><fmt:message key="gebruiker.password"/></td>
                <td>
                    <html:password property="password" size="20" maxlength="50"/>
                </td>
            </tr>
            <c:if test="${form.id != -1}">
                <tr>
                    <td></td>
                    <td><i>Laat dit veld leeg om het wachtwoord niet te wijzigen.</i></td>
                </tr>
            </c:if>
            <tr>
                <td><fmt:message key="gebruiker.phone"/></td>
                <td><html:text property="phone" size="15" maxlength="15"/></td>
            </tr>            
            <tr>
                <td style="vertical-align: top"><fmt:message key="gebruiker.role"/></td>
                <td>
                    <c:forEach var="r" items="${availableRoles}">
                        <html:multibox property="roles" value="${r.id}"/><c:out value="${r.role}"/><br>
                    </c:forEach>
                </td>

            </tr>
        </table>
    </c:if>
    <c:if test="${empty form.id}">
        <html:submit property="create" onclick="bCancel=true;">Nieuw account toevoegen</html:submit>
    </c:if>

</c:if>

</html:form>