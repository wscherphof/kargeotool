
var globalTreeOptions = {};

/*** publieke functies ***/

/**
 * options: object met volgende properties:
 * - id
 * - root
 * - rootChildrenAsRoots
 * - itemLabelHtmlCreatorFunction
 * - itemLabelCreatorFunction
 * - toggleImages
 * - saveExpandedState
 * - saveScrollState
 * - expandAll (optioneel, default false)
 * - childrenPadding (optioneel: padding voor inspringen children, default "10px")
 */

var haveInnerHTML = document.createElement("div").innerHTML != undefined;

function treeview_create(options) {
	globalTreeOptions[options.id] = options;

	if(!options.root) {
		return;
	}

	options.containerNode = document.getElementById(options.id);
	if(!options.containerNode) {
		alert("treeview_create: no node with id \"" + options.id + '"');
		return;
	}

	if(!options.childrenPadding) {
		options.childrenPadding = "10px";
	}

	if(!options.rootChildrenAsRoots) {
		if(haveInnerHTML && options.itemHtmlLabelCreatorFunction) {
			var html = treeview_createItemHtml(options, options.root);
			options.containerNode.innerHTML = html;
		} else {
			var rootNode = treeview_createItemNode(options, options.root);
			options.containerNode.appendChild(rootNode);
		}
	} else {
		var children = options.root.children;
		if(children) {
			var html = "";
			for(var i = 0; i < children.length; i++) {
				if(haveInnerHTML && options.itemHtmlLabelCreatorFunction) {
					html += treeview_createItemHtml(options, children[i]);
				} else {
					var node = treeview_createItemNode(options, children[i]);
					options.containerNode.appendChild(node);
				}
			}
			if(haveInnerHTML && options.itemHtmlLabelCreatorFunction) {
				options.containerNode.innerHTML = html;
			}
		}
	}

	if(options.saveExpandedState) {
		treeview_restoreExpandedNodeStates(options);
	}
	if(options.saveScrollState) {
		treeview_registerScrollStateEvents();
	}
}

function treeview_getLabelContainerNodeForItemId(treeId, treeItemId) {
	var options = globalTreeOptions[treeId];
	return document.getElementById(treeview_getDOMItemId(options, treeItemId) + "_label");
}

function treeview_expandItemParents(treeId, treeItemId) {
	var options = globalTreeOptions[treeId];

	var itemNode = document.getElementById(treeview_getDOMItemId(options, treeItemId));
	/* stop indien parent de container div is van de hele tree (id == options.id) */
	while(itemNode != undefined && itemNode.id != options.id) {
		treeview_expandItemNodeChildren(itemNode);
		itemNode = itemNode.parentNode;
	}
}

function treeview_expandItemChildren(treeId, treeItemId) {
	var options = globalTreeOptions[treeId];
	var DOMItemId = treeview_getDOMItemId(options, treeItemId);
	var itemNode = document.getElementById(DOMItemId);
	treeview_expandItemNodeChildren(itemNode);
}

function treeview_findItem(root, itemId) {
	if(root.id == itemId) {
		return root;
	}

	if(root.children) {
		for(var i = 0; i < root.children.length; i++) {
			var item = treeview_findItem(root.children[i], itemId);
			if(item != null) {
				return item;
			}
		}
	}

	return null;
}

/*** private functies ***/

function treeview_expandItemNodeChildren(itemNode) {
	var childrenNode = document.getElementById(itemNode.id + "_children");
	if(childrenNode != undefined) {
		if(childrenNode.style.display == "none") {
			treeview_toggleItemChildren(itemNode.id);
		}
	}
}

function treeview_getOptions(DOMItemId) {
	/* conventie: tree id's bevatten geen underscores, id's van item nodes hebben
	 * als id het tree id + "_item_" + item.id.
	 * Deze functie zoekt in een globale associatieve array de options van de
	 * treeview op.
	 */
	 var treeId = DOMItemId.substring(0, DOMItemId.indexOf('_'));
	 return globalTreeOptions[treeId];
}

/**
 * Event handler voor click op toggle (+/- img)
 */
function treeview_toggleClick(e) {
	if(!e) {
		e = window.event;
	}
	var target = e.target ? e.target : e.srcElement;

	/* zoek naar de div welke alle div's van het item omvat, dit is de eerste
	 * DIV node die parent is van de target van het event (zie stukje HTML bij
	 * treeview_createContentNode)
	 */
	var itemNode = target;
	while(itemNode.nodeName != "DIV") {
		itemNode = itemNode.parentNode;
	}
	var DOMItemId = itemNode.id;

	treeview_toggleItemChildren(DOMItemId);

	return false;
}

function treeview_toggleItemChildren(DOMItemId) {
	var children = document.getElementById(DOMItemId + "_children");
	var toggle = document.getElementById(DOMItemId + "_toggle");
	if(children != undefined) {
		var options = treeview_getOptions(DOMItemId);

		/* nieuwe state, omgekeerd van huidige state */
		var expanded = children.style.display == "none";
		if(!expanded) {
			children.style.display = "none";
			treeview_displayToggle(options, toggle, true);
		} else {
			children.style.display = "block";
			treeview_displayToggle(options, toggle, false);
		}

		if(options.saveExpandedState) {
			treeview_saveNodeExpandedState(options, DOMItemId, expanded);
		}
	}
}

function treeview_displayToggle(options, toggle, collapsed) {
	toggle.src = options.toggleImages[collapsed ? "collapsed" : "expanded"]
}

function treeview_getDOMItemId(options, treeItemId) {
	return options.id + "_item_" + treeItemId;
}

/*** functies voor maken HTML van treeview, sneller (maar lelijker) dan met DOM ***/

function treeview_createItemHtml(options, treeItem) {
	var id = treeview_getDOMItemId(options, treeItem.id);
	var html = "<div style=\"position:static;float:none\" id=\"" + id + "\">";

	html += treeview_createContentHtml(options, id, treeItem);

	if(treeItem.children) {
		html += treeview_createChildrenHtml(id, options);
		for(var i = 0; i < treeItem.children.length; i++) {
			html += treeview_createItemHtml(options, treeItem.children[i]);
		}
		html += "</div>";
	}

	html += "</div>";
    return html;
}

/* maakt alleen de HTML voor een opening-div, geen sluit-tag */
function treeview_createChildrenHtml(id, options) {
	var display = options.expandAll ? "block" : "none";

	var html = "<div style=\"position:static;float:none;"
		 + "display:" + display + ";"
		 + "padding-left:" + options.childrenPadding
		 + "\" class=\"children\" "
		 + "id=\"" + id + "_children"
		 + "\">";
	return html;
}

function treeview_createContentHtml(options, id, item) {
	var html = "<table border=\"0\" cellspacing=\"0\" style=\"padding:0\"><tbody><tr>";

	html += "<td style=\"vertical-align:middle\"><a href=\"#\" onclick=\"treeview_toggleClick(event);\" ";
	if(!item.children) {
		html += "style=\"cursor:default\" ";
	}
	html += ">";

	var src;
	if(options.expandAll) {
		src = item.children ? options.toggleImages["expanded"] : options.toggleImages["leaf"];
	} else {
		src = item.children ? options.toggleImages["collapsed"] : options.toggleImages["leaf"];
	}
	html += "<img id=\"" + id + "_toggle\" src=\"" + src + "\" border=\"0\">";
	html += "</a></td>";

	html += "<td nowrap style=\"white-space:nowrap\"><div id=\"" + id + "_label\" style=\"position:static;float:none\">";
	if(options.itemHtmlLabelCreatorFunction) {
		/* FIXME niet dezelfde functionaliteit, kan geen class/style instellen op label div */
		html += options.itemHtmlLabelCreatorFunction(item);
	} else {
		html += escapeHTML(item.title ? item.title : item.id);
	}
	html += "</div></td>";

	html += "</tr></tbody></table>";

	return html;
}

/*** functies voor maken DOM tree van treeview ***/

function treeview_createItemNode(options, treeItem) {
	var itemNode = createNormalDiv();
	var id = treeview_getDOMItemId(options, treeItem.id);
	itemNode.id = id;

	var contentNode = treeview_createContentNode(options, id, treeItem);
	itemNode.appendChild(contentNode);

	if(treeItem.children) {
		var childrenNode = treeview_createChildrenNode(id, options);

		for(var i = 0; i < treeItem.children.length; i++) {
			childrenNode.appendChild(treeview_createItemNode(options, treeItem.children[i]));
		}
		itemNode.appendChild(childrenNode);
	}

	return itemNode;
}

function treeview_createContentNode(options, id, item) {
	/*
		<table border="0" cellspacing="0" cellpadding="0">
			<tbody>
				<tr>
					<td>
						<!-- indien geen children: -->
						<a href="#" onclick="treeview_toggleClick(event);" style="cursor: default">
							<img id="item_A_toggle" src="leaft.gif" border="0">
						</a>
						<!-- indien wel children: -->
						<a href="#" onclick="treeview_toggleClick(event);">
							<img id="item_A_toggle" src="plus.gif" border="0">
						</a>
					</td>
					<td nowrap style="white-space: nowrap">
						<!-- de volgende DIV wordt meegegeven aan de itemLabelCreatorFunction, deze
						     kan de style/className aanpassen en child nodes er aan toevoegen. Deze
							 DIV kan ook worden opgevraagd adv het item id met treeview_getLabelContainerNodeForItemId()
						-->
						<div id="item_A_label">
						</div>
					</td>
				</tr>
			</tbody>
		</table>
	*/

	var img = document.createElement("img");
	img.id = id + "_toggle";
	if(options.expandAll) {
		img.src = item.children ? options.toggleImages["expanded"] : options.toggleImages["leaf"];
	} else {
		img.src = item.children ? options.toggleImages["collapsed"] : options.toggleImages["leaf"];
	}
	img.setAttribute("border", "0");
	var a = document.createElement("a");
	a.href = "#";
	a.style.align = "center";
	a.onclick = treeview_toggleClick;
	if(!item.children) {
		a.style.cursor = "default";
	}
	a.appendChild(img);

	var td0 = document.createElement("td");
	td0.style.verticalAlign = "middle";
	td0.appendChild(a);

	var td1 = document.createElement("td");
	td1.setAttribute("nowrap", "nowrap");
	td1.style.whiteSpace = "nowrap";

	var labelContainer = createNormalDiv();
	labelContainer.id = id + "_label";
	if(options.itemLabelCreatorFunction) {
		options.itemLabelCreatorFunction(labelContainer, item);
	} else {
		treeview_defaultItemLabelCreatorFunction(labelContainer, item);
	}
	td1.appendChild(labelContainer);

	var tr = document.createElement("tr");
	tr.appendChild(td0);
	tr.appendChild(td1);
	var tbody = document.createElement("tbody");
	tbody.appendChild(tr);
	var table = document.createElement("table");
	table.border = 0;
	table.cellSpacing = 0;
	table.style.padding = 0;
	table.appendChild(tbody);

	return table;
}

function treeview_defaultItemLabelCreatorFunction(container, item) {
	container.appendChild(document.createTextNode(item.title ? item.title : item.id));
}

function treeview_createChildrenNode(id, options) {
	var node = createNormalDiv();
	node.id = id + "_children";
	node.style.paddingLeft = options.childrenPadding;
	node.className = "children";
	if(options.expandAll) {
		node.style.display = "block";
	} else {
		node.style.display = "none";
	}
	return node;
}

/*** functies voor opslaan/restoren welke items expanded zijn ***/

function treeview_saveNodeExpandedState(options, DOMItemId, expanded) {
	options.expandedStates[DOMItemId] = expanded;

	treeview_saveExpandedNodeStates(options);
}

function treeview_saveExpandedNodeStates(options) {
	setCookie(options.id + "_expanded", treeview_makeStatesString(options, options.expandedStates));
}

function treeview_getExpandedNodeStatesFromCookie(options) {
	var cookie = getCookie(options.id + "_expanded");
	return treeview_parseStatesString(options, cookie);
}

var cookieStateSeparator = "|";

function treeview_parseStatesString(options, stateString) {
	var state = {};
	var itemIdPrefix = options.id + "_item_";
	if(stateString) {
		var splitted = stateString.split(cookieStateSeparator);
		for(var i = 0; i < splitted.length; i++) {
			var itemId = itemIdPrefix + splitted[i];
			state[itemId] = true;
		}
	}
	return state;
}

function treeview_makeStatesString(options, states) {
	var stateString = "";
	var itemIdPrefixLength = (options.id + "_item_").length;
	var first = true;
	for(itemId in states) {
		var expanded = states[itemId];
		if(expanded) {
			if(first) {
				first = false;
			} else {
				stateString += cookieStateSeparator;
			}
			stateString += itemId.substring(itemIdPrefixLength, itemId.length);
		}
	}
	return stateString;
}

function treeview_restoreExpandedNodeStates(options) {
	options.expandedStates = treeview_getExpandedNodeStatesFromCookie(options);

	for(itemId in options.expandedStates) {
		if(itemId) {
			var itemNode = document.getElementById(itemId);
			if(itemNode) {
				treeview_expandItemNodeChildren(itemNode);
			}
		}
	}
}

/*** functies voor opslaan/restoren scroll positie ***/

var scrollStateEventsRegistered = false;

function treeview_registerScrollStateEvents() {
	if(!scrollStateEventsRegistered) {
		scrollStateEventsRegistered = true;

		/* bij mouseup sla scrollpositie op in cookie, bij onload van window
		 * restore de positie (bij Internet Explorer kan dit pas bij onload,
		 * gaat inline na opbouwen DOM tree niet correct) (zorgt wel voor
		 * flicker in Opera)
		 */

		if(document.addEventListener) {
			document.addEventListener("mouseup", treeview_saveScrollStates, false);
			window.addEventListener("load", treeview_restoreScrollStates, false);
		} else if(document.attachEvent) {
			document.attachEvent("onmouseup", treeview_saveScrollStates);
			window.attachEvent("onload", treeview_restoreScrollStates);
		}
	}
}

/**
 * Algemene onclick event handler voor window, slaat alle treeview scroll
 * positions op (indien enabled), adv de globalTreeOptions array.
 */
function treeview_saveScrollStates() {
	for(treeId in globalTreeOptions) {
		var options = globalTreeOptions[treeId];
		if(options.saveScrollState) {
			var tree = document.getElementById(options.id);
			setCookie(options.id + "_scroll", tree.scrollLeft + cookieStateSeparator + tree.scrollTop);
		}
	}
}

function treeview_restoreScrollStates() {
	for(treeId in globalTreeOptions) {
		var options = globalTreeOptions[treeId];

		var tree = document.getElementById(options.id);
		var scrollState = getCookie(options.id + "_scroll");
		if(scrollState) {
			scrollState = scrollState.split(cookieStateSeparator);
			if(scrollState[0]) {
				tree.scrollLeft = scrollState[0];
			}
			if(scrollState[1]) {
				tree.scrollTop = scrollState[1];
			}
		}
	}
}

/*** functie tegen vage stylesheet rules ***/

function createNormalDiv() {
	var div = document.createElement("div");
	div.style.position = "static";
	div.style.cssFloat = "none";
	div.style.styleFloat = "none";
	return div;
}

/*** algemene cookie functies ***/

function getCookie(name) {
	var cookies = document.cookie.split("; ");
	for(var i = 0; i < cookies.length; i++) {
		var cookie = cookies[i].split("=");
		if(cookie[0] == name) {
			return unescape(cookie[1]);
		}
	}
	return null;
}

function setCookie(name, value) {
	document.cookie = name + "=" + escape(value);
}

/*** escapeHTML ***/

function escapeHTML(str) {
    var div = document.createElement('div');
    var text = document.createTextNode(str);
    div.appendChild(text);
    return div.innerHTML;
}
