
/* XXX hardcoded op forms[0] */
function submitRetainingScrollPosition() {

    saveScrollPosition();

    document.forms[0].submit();
}

function saveScrollPosition() {
    var form = document.forms[0];

    var e = document.createElement("input");
    e.type = "hidden";
    e.name = "scroll";
    var pageScroll = getPageScroll();
    e.value = pageScroll.scrollX + "," + pageScroll.scrollY;
    form.appendChild(e);
}

var _setX;
var _setY;

/* XXX hardcoded op eerste form, omdat wanneer deze functie wordt aangeroepen
 * de pagina nog niet geheel gerenderd is (daarom ook alleen het aanmaken van
 * onload handlers, doLoadScrollPosition() gebruikt document.forms[0])
 * Eventueel aan te passen door form names/id's te gebruiken
 */
function loadScrollPosition(setX, setY) {
    _setX = setX;
    _setY = setY;

    setOnload(doLoadScrollPosition);
}


function doLoadScrollPosition() {  
    var qs = window.location.search;

    var scroll = getParameter(window.location.search, "scroll");
    if(scroll) {
        var coords = scroll.split(",");
        var x = coords[0];
        var y = coords[1];
        if(x && y) {
            window.scrollTo(_setX ? x : 0, _setY ? y : 0);
        }
    }
}

function getPageScroll() {
    var x, y;
    if(typeof window.pageXOffset == 'number'){
        x = window.pageXOffset;
        y = window.pageYOffset;
    } else {
        if((window.document.compatMode) && (window.document.compatMode == 'CSS1Compat')) {
            x = window.document.documentElement.scrollLeft;
            y = window.document.documentElement.scrollTop;
        } else {
            x = window.document.body.scrollLeft;
            y = window.document.body.scrollTop;
        }
    }
    return {scrollX: x, scrollY: y};
} 

function getParameter(queryString, parameterName) {
    parameterName = parameterName + "=";
    if(queryString.length > 0) {
        var begin = queryString.indexOf(parameterName);
        if(begin != -1) {
            begin += parameterName.length;
            end = queryString.indexOf("&", begin);
            if(end == -1) {
                end = queryString.length;
            }
            return unescape(queryString.substring(begin, end));
        }
    }
    return null;
}

function cancelEvent(e) {
    if(!e) e = window.event;
    e.cancelBubble = true;
    if(e.stopPropagation) e.stopPropagation();
 }

function setOnload(func) {
    /* Dit zou eventueel ook kunnen met window.addEventListener/attachEvent, 
     * maar dan verschilt natuurlijk de volgorde van meerdere events tussen IE/Firefox
     */

    var oldOnload = window.onload;
    window.onload = function() {
        if(oldOnload) { 
            oldOnload();
        }
        func();
    }
}    