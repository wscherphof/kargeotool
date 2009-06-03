

var resizeIE = function() {
    var viewport = getViewport();
    document.getElementById('body_editor').style.width = viewport[0] + 'px';
    document.getElementById('leftbar').style.height = (viewport[1] - 30) + 'px';
    var treeheight = viewport[1] - 487;
    document.getElementById('tree').style.height = treeheight + 'px';
    document.getElementById('objectTree').style.height = (treeheight - 140) + 'px';
    document.getElementById('form').style.top = (treeheight + 16) + 'px';
    document.getElementById('kaart').style.height = (viewport[1] - 46) + 'px';
    document.getElementById('kaart').style.width = (viewport[0] - 402) + 'px';
}

var getViewport = function() {
    var viewportwidth;
    var viewportheight;
    if (typeof window.innerWidth != 'undefined') {
        viewportwidth = window.innerWidth,
        viewportheight = window.innerHeight
    } else if (typeof document.documentElement != 'undefined' && typeof document.documentElement.clientWidth != 'undefined' && document.documentElement.clientWidth != 0) {
        viewportwidth = document.documentElement.clientWidth,
        viewportheight = document.documentElement.clientHeight
    } else {
        viewportwidth = document.getElementsByTagName('body')[0].clientWidth,
        viewportheight = document.getElementsByTagName('body')[0].clientHeight
    }
    return new Array(viewportwidth,viewportheight);
}

window.onresize = resizeIE;
setOnload(resizeIE);