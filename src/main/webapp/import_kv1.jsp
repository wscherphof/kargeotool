<%@include file="/WEB-INF/jsp/taglibs.jsp" %>
<%@page errorPage="/WEB-INF/jsp/commons/errorpage.jsp" %>

<!DOCTYPE html>

<html>
    <head>
        <title>Geo-OV Import OV-gegevens koppelvlak 1</title>
    </head>
    <body>
        
        <h1>Importeren OV-gegevens koppelvlak 1</h1>
        
        <stripes:form beanclass="nl.b3p.kar.stripes.ImportTmiActionBean">
            <stripes:messages/>
            <stripes:errors/>
            <fieldset>
                <legend>Import</legend>
                <table>
                    <tr><td>ZIP-bestand met TMI bestanden:</td><td><stripes:file name="bestand"/></td></tr>
                    <tr>
                        <td>Encoding TMI bestanden:</td>
                        <td>
                            <stripes:select name="encoding">
                                <stripes:option>UTF-8</stripes:option>
                                <stripes:option>ISO-8859-1</stripes:option>
                            </stripes:select>
                        </td>
                    </tr>
                    <tr><td>JNDI naam database:</td><td><stripes:text name="jndi" value="java:comp/env/jdbc/transmodel" size="30"/></td></tr>
                    <tr><td>Database schema:</td><td><stripes:text name="schema" value="automatisch" size="30"/></td></tr>
                    <tr><td>SQL insert batch size:</td><td><stripes:text name="batch" value="500" size="3"/></td></tr>
                    <tr><td>Database transactie:</td><td><label><stripes:checkbox name="transaction"/> gebruiken</label></td></tr>
                    <tr><td>Titel voor weergave:</td><td><stripes:text name="titel" size="30"/></td></tr>
                    <tr><td>Geldig vanaf:</td><td><stripes:text formatPattern="yyyy-MM-dd" formatType="date" name="validFrom"/></td></tr>
                    <tr><td>Geldig tot:</td><td><stripes:text formatPattern="yyyy-MM-dd" formatType="date" name="validUntil"/></td></tr>
                    <tr><td>Omschrijving:</td><td><stripes:text name="description" size="30"/></td></tr>
                </table>
            </fieldset>
            <p>
            <stripes:submit name="import" value="Importeren"/>
        </stripes:form>
    </body>
</html>