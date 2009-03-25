// $Id: calendar.js 209 2005-03-18 14:18:05Z Matthijs $

function DoCal(htmf, target) {

    calendarOptions = new Object();

    calendarOptions.resultControl = target;

    // Check if IE showModalDialog function is available
    haveModalDialogFunc = (typeof window.showModalDialog != 'undefined');

    dWidth = 500;
    dHeight = 200;
    dFile = htmf;
 
    if (haveModalDialogFunc) {
        window.showModalDialog(dFile, calendarOptions, "dialogWidth=" + dWidth + "pt;dialogHeight=" + dHeight + "pt");
    } else {
        dLeft = (window.innerWidth-dWidth)/2;
        dTop = (window.innerHeight-dHeight)/2;

        window.open(dFile, "", "width="+dWidth+",height="+dHeight+",dialog=yes,modal=yes,left="+dLeft+",top="+dTop);
    }
}
