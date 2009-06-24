<%@include file="/WEB-INF/taglibs.jsp" %>
<%@page errorPage="/WEB-INF/jsp/commons/errorpage.jsp" %>

<c:set var="form" value="${roadsideEquipmentForm.map}"/>

<tiles:insert definition="infoblock"/>

<html:javascript formName="roadsideEquipmentForm" staticJavascript="false"/>

<c:if test="${!empty treeUpdate}">
    <script type="text/javascript">
        parent.treeUpdate('${fn:replace(treeUpdate,"'","\\'")}');
    </script>
</c:if>

<script type="text/javascript">
    parent.flamingo_updateKarLayer();
    parent.flamingo_cancelEdit();
</script>

<c:if test="${!hideForm}">

<html:form styleId="roadsideEquipmentForm" action="/roadsideEquipment" onsubmit="return validateRoadsideEquipmentForm(this)">
    <html:hidden property="id"/>
    <html:hidden property="validated"/>
    <html:hidden property="location"/>
    <html:submit property="save">Opslaan</html:submit>
    <c:if test="${activationGroupCount == 1}">
        <c:set var="extraMsg">Let op! De signaalgroep (en alle triggerpunten hiervan) van deze walapparatuur wordt ook verwijderd!</c:set>
    </c:if>
    <c:if test="${activationGroupCount > 1}">
        <c:set var="extraMsg">Let op! De ${activationGroupCount} signaalgroepen (en alle triggerpunten daarvan) van deze walapparatuur worden ook verwijderd!</c:set>
    </c:if>
    <c:if test="${!empty form.id}">
        <html:submit property="delete" onclick="bCancel = true; return confirm('Weet u zeker dat u deze walapparatuur wilt verwijderen? ${extraMsg}')">Verwijderen</html:submit>
        <html:submit property="validate" onclick="bCancel = true; document.forms[0].validated.value = confirm('Wilt u dit object valideren voor opname in de TMI export?');">Valideren</html:submit>
    </c:if>
    
<c:set var="point" value="${roadsideEquipment.locationString}" scope="request"/>
<c:set var="geometryType" value="Point" scope="request"/>
<c:set var="layer" value="draw_walapparatuur" scope="request"/>
<tiles:insert page="/WEB-INF/jsp/viewer/formEditJs.jsp"/>

<c:set var="focus" value="unitNumber" scope="request"/>
<tiles:insert definition="setFocus"/>

<div>
    <b>Eigenschappen walapparatuur</b>
    <div class="formTableContainer">
    <table class="form">
        <tr>
            <td style="width: 130px"><fmt:message key="rseq.dataOwner"/></td>
            <td>
                <html:select property="dataOwner" style="width: 100%">
                    <c:if test="${empty form.id}">
                        <html:option value=""/>
                    </c:if>
                    <c:forEach var="do" items="${dataOwners}">
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
                    <html:img page="/images/checkmark.gif" module="" style="display: block; float: left;"/>
                    <c:out value="${roadsideEquipment.validator}"/> op
                    <fmt:formatDate pattern="dd-MM-yyyy" value="${roadsideEquipment.validationTime}"/>
                </c:if>
            </td>
        </tr>
    </table>
    </div>
</div>

</html:form>

</c:if>