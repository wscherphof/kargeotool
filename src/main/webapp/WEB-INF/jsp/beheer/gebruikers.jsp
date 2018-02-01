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
<%@page errorPage="/WEB-INF/jsp/commons/errorpage.jsp" %>

<stripes:layout-render name="/WEB-INF/jsp/commons/siteTemplate.jsp">

    <stripes:layout-component name="headerlinks" >
        <%@include file="/WEB-INF/jsp/commons/headerlinks.jsp" %>
        <script type="text/javascript" src="<c:url value="/js/gebruikers.js"/>"></script>
        <script type="text/javascript" src="<c:url value="/js/utils.js"/>"></script>
    </stripes:layout-component>
    <stripes:layout-component name="content">

    <h2>Gebruikersbeheer</h2>

<stripes:form beanclass="nl.b3p.kar.stripes.GebruikersActionBean">
    <stripes:hidden name="gebruiker"/>

    <c:if test="${!empty actionBean.gebruikers}">
        <script type="text/javascript">
            function loadScroll() {
                var scroll = getCookie("gebruikersScroll");
                if(scroll != undefined) {
                    scroll = parseInt(scroll);
                    if(!isNaN(scroll)) {
                        document.getElementById("gebruikerScroller").scrollTop = scroll;
                    }
                }
            }

            function saveScroll() {
                setCookie("gebruikersScroll", document.getElementById("gebruikerScroller").scrollTop);
            }

            function scrollToBottom() {
                var div = document.getElementById("gebruikerScroller");
                div.scrollTop = div.scrollHeight;
                saveScroll();
            }

            if(document.addEventListener) {
                    document.addEventListener("mouseup", saveScroll, false);
                    window.addEventListener("load", loadScroll, false);
            } else if(document.attachEvent) {
                    document.attachEvent("onmouseup", saveScroll);
                    window.attachEvent("onload", loadScroll);
            }
        </script>
        <div class="description">
        Aantal gebruikers: <b>${fn:length(actionBean.gebruikers)}</b>
        </div>
        <table class="tableheader">
            <tr>
                <th style="width: 150px">Gebruikersnaam</th>
                <th style="width: 200px">Naam</th>
                <th style="width: 200px">E-mail</th>
                <th style="width: 100px">Telefoonnummer</th>
                <th></th>
            </tr>
        </table>
        <div id="gebruikerScroller">
            <table style="width: 100%;">
                <tbody>
                    <c:forEach var="g" varStatus="status" items="${actionBean.gebruikers}">
                        <c:set var="col" value=""/>
                        <c:if test="${g.id == actionBean.gebruiker.id}">
                            <c:set var="col" value=" style=\"background-color: #cccccc\""/>
                        </c:if>
                        <tr${col}>
                            <td style="width: 150px">
                                <stripes:link beanclass="nl.b3p.kar.stripes.GebruikersActionBean" event="edit">
                                    <stripes:param name="gebruiker" value="${g.id}"/>
                                    <c:out value="${g.username}"/>
                                </stripes:link>
                                <c:if test="${g.beheerder}">
                                    <img src="${contextPath}/images/star.png">
                                </c:if>
                            </td>
                            <td style="width: 200px"><c:out value="${g.fullname}"/></td>
                            <td style="width: 200px"><c:out value="${g.email}"/></td>
                            <td style="width: 100px"><c:out value="${g.phone}"/></td>
                            <td>
                                <%-- niet gebruiker zichzelf laten verwijderen --%>
                                <c:if test="${g.username != pageContext.request.userPrincipal.username}">
                                    <c:set var="delaccount" value="${g.username}"/>
                                    <stripes:link beanclass="nl.b3p.kar.stripes.GebruikersActionBean" event="delete" onclick="return confirm('Weet u zeker dat u het account ${delaccount} wilt verwijderen?');">
                                        <stripes:param name="gebruiker" value="${g.id}"/>
                                        <img src="${contextPath}/images/delete.gif" border="0">
                                    </stripes:link>
                                </c:if>
                            </td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
        </div>
    </c:if>

    <div class="formedit">
        <stripes:errors/>
        <stripes:messages/>
        <c:if test="${empty actionBean.gebruiker}">
            <stripes:submit name="add" onclick="scrollToBottom();">Nieuw account toevoegen</stripes:submit>
        </c:if>

        <c:if test="${!empty actionBean.gebruiker}">
            <stripes:submit name="save">Opslaan</stripes:submit>
            <c:if test="${actionBean.gebruiker.id != null}">
                <%-- niet gebruiker zichzelf laten verwijderen --%>
                <c:if test="${actionBean.gebruiker.id != pageContext.request.userPrincipal.id}">
                    <stripes:submit name="delete" onclick="return confirm('Weet u zeker dat u deze gebruiker wilt verwijderen?');">Verwijderen</stripes:submit>
                </c:if>
            </c:if>
            <stripes:url var="cancelPage" beanclass="nl.b3p.kar.stripes.GebruikersActionBean"/>
            <stripes:button name="cancel" onclick="window.location.href='${cancelPage}'">Annuleren</stripes:button>
            <div class="edittable">
                <table style="font-size: 8pt">
                    <tr>
                        <td valign="top" class="left">
                            <table style="font-size: 8pt">
                                <tr>
                                    <td><fmt:message key="gebruiker.username"/></td>
                                    <td><stripes-dynattr:text name="gebruiker.username" size="20" autofocus="true"/></td>
                                </tr>
                                <tr>
                                    <td><fmt:message key="gebruiker.fullName"/></td>
                                    <td><stripes:text name="gebruiker.fullname" size="30"/></td>
                                </tr>
                                <tr>
                                    <td><fmt:message key="gebruiker.email"/></td>
                                    <td><stripes-dynattr:text name="gebruiker.email" size="30" type="email"/></td>
                                </tr>
                                <tr>
                                    <td><fmt:message key="gebruiker.password"/></td>
                                    <td>
                                        <stripes:password name="password" size="20"/>
                                    </td>
                                </tr>
                                <c:if test="${actionBean.gebruiker.id != null}">
                                    <tr>
                                        <td></td>
                                        <td><i>Laat dit veld leeg om het wachtwoord niet te wijzigen.</i></td>
                                    </tr>
                                </c:if>
                                <tr>
                                    <td><fmt:message key="gebruiker.phone"/></td>
                                    <td><stripes-dynattr:text name="gebruiker.phone" size="15" type="tel"/></td>
                                </tr>
                                <tr>
                                    <td style="vertical-align: top"><fmt:message key="gebruiker.role"/></td>
                                    <td>
                                        <script type="text/javascript">
                                            function checkRole() {
                                                var beheerder = document.getElementById("role_beheerder").checked || document.getElementById("role_vervoerder").checked  ;
                                                document.getElementById("beheerder").style.display = beheerder ? "block" : "none";
                                                document.getElementById("nietBeheerder").style.display = !beheerder ? "block" : "none";
                                                document.getElementById("daoedit").style.display = !beheerder ? "block" : "none";
                                            }
                                        </script>
                                        <c:forEach var="r" items="${actionBean.allRoles}">
                                            <label><stripes:radio name="role" value="${r.id}" id="role_${r.role}" onclick="blur();" onchange="checkRole(event);"/><c:out value="${r.role}"/></label><br>
                                        </c:forEach>
                                    </td>

                                </tr>
                            </table>
                        </td>
                        <td valign="top" class="right">
                            <c:set var="isBeheerder" value="${actionBean.gebruiker.beheerder}"/>
                            <div id="roListHeader">
                                <div id="beheerder" style="display: ${isBeheerder ? 'block' : 'none'} ">
                                    Een beheerder kan van alle wegbeheerders gegevens lezen en bewerken. Een vervoerder kan van alle wegbeheerder gegevens lezen.
                                </div>
                                <div id="nietBeheerder" style="display: ${isBeheerder ? 'none' : 'block'}">
                                    Gebruiker kan gegevens van de onderstaande wegbeheerders lezen of bewerken:
                                </div>
                            </div>
                            <div id="daoedit" style="display: ${isBeheerder ? 'none' : 'block'}">
                                <div id="roList">
                                    <table id="roListTable" style="font-size: 8pt;">
                                        <tr>
                                            <th style="width: 35px">Code</th>
                                            <th style="width: 160px">Naam</th>
                                            <th style="width: 60px">Lezen</th>
                                            <th style="width: 60px">bewerken</th>
                                        </tr>
                                        <c:forEach var="mapEntry" items="${actionBean.gebruiker.dataOwnerRights}">
                                            <c:set var="dor" value="${mapEntry.value}"/>
                                            <tr>
                                                <td>${dor.dataOwner.code}</td>
                                                <td>${dor.dataOwner.omschrijving}</td>
                                                <td style="text-align: center"><stripes:checkbox name="dataOwnersReadable" class="used-data-owner" value="${dor.dataOwner.code}" onclick="this.blur()" onchange="checkDORemove(event)"/></td>
                                                <td style="text-align: center"><stripes:checkbox name="dataOwnersEditable" class="used-data-owner" value="${dor.dataOwner.code}" onclick="this.blur()" onchange="checkDORemove(event)"/></td>
                                            </tr>
                                        </c:forEach>
                                    </table>
                                </div><br />
                                Toevoegen wegbeheerder:<br />
                                <div id="availableDataOwnersContainer"></div>
                            </div>
                        </td>
                    </tr>
                </table>
            </div>
            <script type="text/javascript">
                setOnload(checkRole);

                var editUsers = Ext.create('EditUsers', {
                    dataOwners: ${actionBean.dataOwnersJson},
                    addDataOwner: addDataOwner
                });

                function checkDORemove(e) {
                    if(!e) e = window.event;
                    var target = e.target ? e.target : e.srcElement;

                    var id = target.value;

                    var bothUnchecked = !isChecked("dataOwnersEditable", id) && !isChecked("dataOwnersReadable", id);

                    if(bothUnchecked) {
                        if(confirm("Wilt u deze wegbeheerder uit de lijst verwijderen?")) {
                            removeDataOwner(id);
                        } else {
                            target.checked = true;
                        }
                    }
                }

                function isChecked(name, value) {
                    var options = document.forms[0][name];
                    if(options.length == undefined) {
                        return options.value == value && options.checked;
                    } else {
                        for(var i = 0; i < options.length; i++) {
                            if(options[i].value == value) {
                                return options[i].checked;
                            }
                        }
                        return false;
                    }
                }

                function addDataOwner(id, code, name) {
                    /* zoek positie waarop table row geinsert moet worden */
                    var table = document.getElementById("roListTable");
                    var index = table.rows.length;
                    for(var i = 0; i < table.rows.length; i++) {
                        var rowValue = table.rows[i].cells[2].firstChild.value;
                        if(id < rowValue) {
                            index = i;
                            break;
                        }
                    }

                    var row = table.insertRow(index);
                    /* helaas is row.innerHTML in IE read-only... */
                    var cell = row.insertCell(0);
                    cell.appendChild(document.createTextNode(code));
                    cell = row.insertCell(1);
                    cell.appendChild(document.createTextNode(name));
                    cell = row.insertCell(2);
                    cell.style.textAlign = "center";
                    var input = document.createElement("input");
                    input.name = "dataOwnersReadable";
                    input.type = "checkbox";
                    input.value = id + "";
                    input.checked = true;
                    input.onchange = checkDORemove;
                    input.onclick = function() { this.blur() };
                    cell.appendChild(input);
                    cell = row.insertCell(3);
                    cell.style.textAlign = "center";
                    input = document.createElement("input");
                    input.name = "dataOwnersEditable";
                    input.type = "checkbox";
                    input.value = id + "";
                    input.checked = false;
                    input.onchange = checkDORemove;
                    input.onclick = function() { this.blur() };
                    cell.appendChild(input);
                }

                function removeDataOwner(id) {
                    var code, name;
                    var table = document.getElementById("roListTable");
                    for(var i = 0; i < table.rows.length; i++) {
                        var rowValue = table.rows[i].cells[2].firstChild.value;
                        if(id == rowValue) {
                            code = table.rows[i].cells[0].firstChild.nodeValue;
                            name = table.rows[i].cells[1].firstChild.nodeValue;
                            table.deleteRow(i);
                            break;
                        }
                    }
                    editUsers.insertDataOwner(id, code, name);
                }

            </script>
        </c:if>
    </div>

</stripes:form>

</stripes:layout-component>

</stripes:layout-render>