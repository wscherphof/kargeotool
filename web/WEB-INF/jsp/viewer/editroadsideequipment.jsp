<%@include file="/WEB-INF/taglibs.jsp" %>
<%@page errorPage="/WEB-INF/jsp/commons/errorpage.jsp" %>

<c:set var="form" value="${roadsideEquipmentForm.map}"/>

<tiles:insert definition="infoblock"/>

<html:javascript formName="roadsideEquipmentForm" staticJavascript="false"/>

<html:form styleId="roadsideEquipmentForm" action="/roadsideEquipment" onsubmit="return validateRoadsideEquipmentForm(this)">
    <html:hidden property="id"/>
    <html:submit property="save">Opslaan</html:submit>
    <input type="button" value="Verwijderen" onclick="alert('Nog niet geimplementeerd');">
    <input type="button" value="Valideren" onclick="alert('Nog niet geimplementeerd');">

<div style="margin-top: 4px; height: 60px">
    <b>Locatie</b><br>
    <br>
    <c:choose>
        <c:when test="${empty activationGroup.point}">
            <b>Nog geen locatie. </b>
            <input type="button" value="Locatie aanwijzen in kaart" onclick="alert('Nog niet geimplementeerd');">
            <br>
        </c:when>
        <c:otherwise>
            <b>Coordinaten: </b> <span style="font-family: 'courier new', courier, serif"> <c:out value="${roadsideEquipment.point}"/></span>
            <input type="button" value="Locatie wijzigen in kaart" onclick="alert('Nog niet geimplementeerd');">
            <br>
            <%--<br>
            In de kaart kan het punt worden versleept om de locatie te wijzigen.
            <br><br>
            Op dit punt zijn [nnn]/[geen] andere signaalgroepen aanwezig. Bij het
            wijzigen van de locatie worden deze ook verplaatst.
            --%>
        </c:otherwise>
    </c:choose>
    <br>
</div>

<div>
    <b>Eigenschappen walapparatuur</b><br>
    <br>
    <div class="formTableContainer">
    <table class="form" style="width: 98%" border="1" cellspacing="0" cellpadding="2">
        <tr>
            <td style="width: 130px"><fmt:message key="rseq.dataOwner"/></td>
            <td>
                <html:select property="dataOwner" style="width: 100%">
                    <c:if test="${empty form.id}">
                        <html:option value=""/>
                    </c:if>
                    <c:set var="optgroup" value=""/>
                    <c:forEach var="do" items="${dataOwners}">
                        <c:if test="${do.type != optgroup}">
                            <c:if test="${optgroup != ''}">
                                </optgroup>
                            </c:if>
                            <c:set var="optgroup" value="${do.type}"/>
                            <c:set var="label"><fmt:message key="do.type.${do.type}"/></c:set>
                            <optgroup label="${label}">
                        </c:if>
                        <html:option value="${do.code}"><c:out value="${do.name}"/></html:option>
                    </c:forEach>
                </html:select>
            </td>
        </tr>
        <tr>
            <td><fmt:message key="rseq.unitNumber"/></td>
            <td>
                <html:text property="unitNumber" style="border: none; width: 98%"/>
            </td>
        </tr>
        <tr>
            <td><fmt:message key="rseq.type"/></td>
            <td>
                <html:select property="type">
                    <c:if test="${empty form.id}">
                        <html:option value=""/>
                    </c:if>
                    <html:option value="CROSS">CROSS - VRI</html:option>
                    <html:option value="CLOSE">CLOSE - selectieve afsluiting</html:option>
                    <html:option value="PIU">PIU - halteprocessor</html:option>
                </html:select>
            </td>
        </tr>
        <tr>
            <td><fmt:message key="rseq.radioAddress"/></td>
            <td>
                <html:text property="radioAddress" style="border: none; width: 98%"/>
            </td>
        </tr>
        <tr>
            <td style="vertical-align: top"><fmt:message key="rseq.description"/></td>
            <td class="number">
                <html:textarea rows="2" style="width: 100%; border: none; font-family: tahoma, sans-serif; font-size: 8pt" property="description"/>
            </td>
        </tr>
        <tr>
            <td style="vertical-align: top"><fmt:message key="rseq.supplier"/></td>
            <td>
                 <html:text property="supplier" style="border: none; width: 98%"/>
            </td>
        </tr>
        <tr>
            <td style="vertical-align: top"><fmt:message key="rseq.supplierTypeNumber"/></td>
            <td>
                 <html:text property="supplierTypeNumber" style="border: none; width: 98%"/>
            </td>
        </tr>
        <tr>
            <td style="vertical-align: top"><fmt:message key="rseq.installationDate"/></td>
            <td>
                 <html:text property="installationDate" style="border: none; width: 98%"/>
            </td>
        </tr>
        <tr>
            <td><fmt:message key="rseq.selectiveDetectionLoop"/></td>
            <td>
                <html:select property="selectiveDetectionLoop">
                    <html:option value="false">Nee</html:option>
                    <html:option value="true">Ja</html:option>
                </html:select>
            </td>
        </tr>
        <tr>
            <td><fmt:message key="rseq.inactiveFrom"/></td>
            <td class="disabled">
                <c:if test="${!empty roadsideEquipment.inactiveFrom}">
                    <fmt:formatDate pattern="dd-MM-yyyy" value="${roadsideEquipment.inactiveFrom}"/>
                </c:if>
            </td>
        </tr>
        <tr>
            <td><fmt:message key="updater"/></td>
            <td class="disabled">
                <c:if test="${!empty roadsideEquipment.updater}">
                    <c:out value="${roadsideEquipment.updater}"/> op
                    <fmt:formatDate pattern="dd-MM-yyyy" value="${roadsideEquipment.updateTime}"/>
                </c:if>
            </td>
        </tr>
        <tr>
            <td><fmt:message key="validator"/></td>
            <td class="disabled">
                <c:if test="${!empty roadsideEquipment.validator}">
                    <c:out value="${roadsideEquipment.validator}"/> op
                    <fmt:formatDate pattern="dd-MM-yyyy" value="${roadsideEquipment.validatorTime}"/>
                </c:if>
            </td>
        </tr>
    </table>
    </div>
</div>

</html:form>