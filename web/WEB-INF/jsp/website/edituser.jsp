<%-- 
    Document   : edituser
    Created on : Jan 19, 2009, 2:28:00 PM
    Author     : Jytte
--%>

<%@include file="/WEB-INF/taglibs.jsp" %>
<%@page errorPage="/WEB-INF/jsp/commons/errorpage.jsp" %>

<tiles:importAttribute/>

<h1>Gebruikers profiel</h1>

<c:set var="form" value="${edituserForm.map}"/>

<tiles:insert definition="infoblock"/>
<html:form action="/edituser" method="POST">

    <table>
        <tr>
            <td class="veldlabel" valign="top">
                <label for="username">
                    <fmt:message key="edituser.username"/>
                </label>
            </td>
            <td class="veldcontent">
                <html:text property="username" styleId="username" readonly="readonly" styleClass="rdonly" />
            </td>
        </tr>
        <tr>
            <td class="veldlabel" valign="top">
                <label for="volledigenaam">
                    <fmt:message key="edituser.fullname"/>
                </label>
            </td>
            <td class="veldcontent">
                <html:text property="volledigenaam" styleId="volledigenaam" />
            </td>
        </tr>
        <tr>
            <td class="veldlabel" valign="top">
                <label for="email">
                    <fmt:message key="edituser.email"/>
                </label>
            </td>
            <td class="veldcontent">
                <html:text property="email" styleId="email" />
            </td>
        </tr>
        <tr>
            <td class="veldlabel" valign="top">
                <label for="telefoon">
                    <fmt:message key="edituser.phone"/>
                </label>
            </td>
            <td class="veldcontent">
                <html:text property="telefoon" styleId="telefoon" />
            </td>
        </tr>
        <tr>
            <td class="veldlabel" valign="top">
                <label for="positie">
                    <fmt:message key="edituser.position"/>
                </label>
            </td>
            <td class="veldcontent">
                <html:text property="positie" styleId="positie" />
            </td>
        </tr>
        <tr>
            <td class="veldlabel" valign="top">
                <label for="wachtwoord">
                    <fmt:message key="edituser.password"/>
                </label>
            </td>
            <td class="veldcontent">
                <html:text property="wachtwoord" styleId="wachtwoord" />
                <i>Laat dit veld leeg om het wachtwoord niet te wijzigen.</i>
            </td>
        </tr>
        <tr>
            <td class="veldlabel" valign="top">
                <label for="gemeente">
                    <fmt:message key="edituser.gemeente"/>
                </label>
            </td>
            <td class="veldcontent">
                <html:text property="gemeente" styleId="gemeente" readonly="readonly" styleClass="rdonly" />
            </td>
        </tr>
        <tr>
            <td class="veldlabel" valign="top">
                <label for="regio">
                    <fmt:message key="edituser.regio"/>
                </label>
            </td>
            <td class="veldcontent">
                <html:text property="regio" styleId="regio" readonly="readonly" styleClass="rdonly" />
            </td>
        </tr>
        <tr>
            <td class="veldlabel" valign="top">
                <label for="provincie">
                    <fmt:message key="edituser.provincie"/>
                </label>
            </td>
            <td class="veldcontent">
                <html:text property="provincie" styleId="provincie" readonly="readonly" styleClass="rdonly" />
            </td>
        </tr>
        <tr>
            <td colspan="2">
                <html:submit property="save"><fmt:message key="button.save"/></html:submit>
            </td>
        </tr>
    </table>

</html:form>