/**
* Geo-OV - applicatie voor het registreren van KAR meldpunten
*
* Copyright (C) 2009-2013 B3Partners B.V.
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU Affero General Public License as
* published by the Free Software Foundation, either version 3 of the
* License, or (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU Affero General Public License for more details.
*
* You should have received a copy of the GNU Affero General Public License
* along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
/*
 * Hieronder volgen een aantal styles die worden gebruikt om features op de vectorlayers
 * van OpenLayers te tekenen.
 */
var style = new OpenLayers.Style(
{
    graphicWidth: 28,
    graphicHeight: 28,
    graphicYOffset: -14, // shift graphic up 28 pixels
    labelYOffset: -15,
    label: "${label}" // label will be foo attribute value
},
{
    rules: [
    // CROSSING memo
    new OpenLayers.Rule({
        filter: new OpenLayers.Filter.Function({
            evaluate: function (attributes){ return hasMemo(attributes, "CROSSING");}
        }),
        symbolizer: {
            externalGraphic: karTheme.crossing_attachment,
            graphicYOffset: -26,
            label: "${description}"
        }
    }),
    
    // CROSSING no memo
    new OpenLayers.Rule({
        filter: new OpenLayers.Filter.Function({
            evaluate: function (attributes){ return hasNoMemo(attributes, "CROSSING");}
        }),
        // if a feature matches the above filter, use this symbolizer
        symbolizer: {
            externalGraphic: karTheme.crossing,
            graphicYOffset: -26,
            label: "${description}"
        }
    }),
    
    // GUARD MEMO
    new OpenLayers.Rule({
        // a rule contains an optional filter
        filter:  new OpenLayers.Filter.Function({
            evaluate: function (attributes){ return hasMemo(attributes, "GUARD");}
        }),
        // if a feature matches the above filter, use this symbolizer
        symbolizer: {
            externalGraphic: karTheme.guard_attachment,
            graphicYOffset: -16,
            label: "${description}"
        }
    }),
    // GUARD no memo
    new OpenLayers.Rule({
        filter: new OpenLayers.Filter.Function({
            evaluate: function (attributes){ return hasNoMemo(attributes, "GUARD");}
        }),
        // if a feature matches the above filter, use this symbolizer
        symbolizer: {
            externalGraphic: karTheme.guard,
            graphicYOffset: -26,
            label: "${description}"
        }
    }),
    // BAR memo
    new OpenLayers.Rule({
        // a rule contains an optional filter
        filter:  new OpenLayers.Filter.Function({
            evaluate: function (attributes){ return hasMemo(attributes, "BAR");}
        }),
        // if a feature matches the above filter, use this symbolizer
        symbolizer: {
            externalGraphic: karTheme.bar_attachment,
            graphicYOffset: -16,
            label: "${description}"
        }
    }),
    // BAR No memo
    new OpenLayers.Rule({
        filter: new OpenLayers.Filter.Function({
            evaluate: function (attributes){ return hasNoMemo(attributes, "BAR");}
        }),
        // if a feature matches the above filter, use this symbolizer
        symbolizer: {
            externalGraphic: karTheme.bar,
            graphicYOffset: -26,
            label: "${description}"
        }
    }),
    new OpenLayers.Rule({
        // a rule contains an optional filter
        elseFilter: true,
        // if a feature matches the above filter, use this symbolizer
        symbolizer: {
            externalGraphic: karTheme.punt,
            label: "",
            strokeColor: "#99BCE8",
            strokeLinecap: "butt",
            strokeDashstyle: "longdash"
        }
    }),
    new OpenLayers.Rule({   
        filter: new OpenLayers.Filter.Comparison({
            type: OpenLayers.Filter.Comparison.EQUAL_TO,
            property: "type",
            value:"ACTIVATION_1"
        }),
        symbolizer: {
            externalGraphic: karTheme.inmeldPunt
        }
    }),
    new OpenLayers.Rule({   
        filter: new OpenLayers.Filter.Comparison({
            type: OpenLayers.Filter.Comparison.EQUAL_TO,
            property: "type",
            value:"ACTIVATION_2"
        }),
        symbolizer: {
            externalGraphic: karTheme.uitmeldPunt
        }
    }),
    new OpenLayers.Rule({   
        filter: new OpenLayers.Filter.Comparison({
            type: OpenLayers.Filter.Comparison.EQUAL_TO,
            property: "type",
            value:"ACTIVATION_3"
        }),
        symbolizer: {
            externalGraphic: karTheme.voorinmeldPunt
        }
    }),
    new OpenLayers.Rule({   
        filter: new OpenLayers.Filter.Comparison({
            type: OpenLayers.Filter.Comparison.EQUAL_TO,
            property: "type",
            value:"END"
        }),
        symbolizer: {
            externalGraphic: karTheme.eindPunt,
            graphicYOffset: -25,
            graphicXOffset: -5
        }
    }),
    new OpenLayers.Rule({   
        filter: new OpenLayers.Filter.Comparison({
            type: OpenLayers.Filter.Comparison.EQUAL_TO,
            property: "type",
            value:"BEGIN"
        }),
        symbolizer: {
            externalGraphic: karTheme.startPunt,
            graphicYOffset: -25,
            graphicXOffset: -5
        }
    })
    ]
}
);

var selectstyle = new OpenLayers.Style(
// the first argument is a base symbolizer
// all other symbolizers in rules will extend this one
{
    graphicWidth: 37,
    graphicHeight: 39,
    graphicYOffset: -21, 
    labelYOffset: -20,
    label: "${label}" 
},
// the second argument will include all rules
{
    rules: [
    new OpenLayers.Rule({
        filter: new OpenLayers.Filter.Function({
            evaluate: function (attributes){ return hasMemo(attributes, "CROSSING");}
        }),
        symbolizer: {
            externalGraphic: karTheme.crossing_selected_attachment,
            graphicYOffset: -26,
            label: "${description}"
        }
    }),
    new OpenLayers.Rule({
        filter: new OpenLayers.Filter.Function({
            evaluate: function (attributes){ return hasNoMemo(attributes, "CROSSING");}
        }),
        // if a feature matches the above filter, use this symbolizer
        symbolizer: {
            externalGraphic: karTheme.crossing_selected,
            graphicYOffset: -26,
            label: "${description}"
        }
    }),

    // BAR
    new OpenLayers.Rule({
        filter:  new OpenLayers.Filter.Function({
            evaluate: function (attributes){ return hasMemo(attributes, "BAR");}
        }),
        symbolizer: {
            externalGraphic: karTheme.bar_selected_attachment,
            graphicYOffset: -26,
            label: "${description}"
        }
    }),
    new OpenLayers.Rule({
        filter: new OpenLayers.Filter.Function({
            evaluate: function (attributes){ return hasNoMemo(attributes, "BAR");}
        }),
        // if a feature matches the above filter, use this symbolizer
        symbolizer: {
            externalGraphic: karTheme.bar_selected,
            graphicYOffset: -26,
            label: "${description}"
        }
    }),
    
    // GUARD
    new OpenLayers.Rule({
        filter:  new OpenLayers.Filter.Function({
            evaluate: function (attributes){ return hasMemo(attributes, "GUARD");}
        }),
        symbolizer: {
            externalGraphic: karTheme.guard_selected_attachment,
            graphicYOffset: -26,
            label: "${description}"
        }
    }),
    new OpenLayers.Rule({
        filter: new OpenLayers.Filter.Function({
            evaluate: function (attributes){ return hasNoMemo(attributes, "GUARD");}
        }),
        // if a feature matches the above filter, use this symbolizer
        symbolizer: {
            externalGraphic: karTheme.guard_selected,
            graphicYOffset: -26,
            label: "${description}"
        }
    }),
    new OpenLayers.Rule({
        // a rule contains an optional filter
        elseFilter: true,
        // if a feature matches the above filter, use this symbolizer
        symbolizer: {
            externalGraphic: karTheme.punt_selected,
            label: "",
            strokeColor: "#99BCE8",
            strokeLinecap: "butt",
            strokeDashstyle: "longdash"
        }
    }),
    new OpenLayers.Rule({   
        filter: new OpenLayers.Filter.Comparison({
            type: OpenLayers.Filter.Comparison.EQUAL_TO,
            property: "type",
            value:"ACTIVATION_1"
        }),
        symbolizer: {
            externalGraphic: karTheme.inmeldPunt_selected
        }
    }),
    new OpenLayers.Rule({   
        filter: new OpenLayers.Filter.Comparison({
            type: OpenLayers.Filter.Comparison.EQUAL_TO,
            property: "type",
            value:"ACTIVATION_2"
        }),
        symbolizer: {
            externalGraphic: karTheme.uitmeldPunt_selected
        }
    }),
    new OpenLayers.Rule({   
        filter: new OpenLayers.Filter.Comparison({
            type: OpenLayers.Filter.Comparison.EQUAL_TO,
            property: "type",
            value:"ACTIVATION_3"
        }),
        symbolizer: {
            externalGraphic: karTheme.voorinmeldPunt_selected
        }
    }),
    new OpenLayers.Rule({   
        filter: new OpenLayers.Filter.Comparison({
            type: OpenLayers.Filter.Comparison.EQUAL_TO,
            property: "type",
            value:"END"
        }),
        symbolizer: {
            externalGraphic: karTheme.eindPunt_selected,
            graphicYOffset: -33,
            graphicXOffset: -6
        }
    }),
    new OpenLayers.Rule({   
        filter: new OpenLayers.Filter.Comparison({
            type: OpenLayers.Filter.Comparison.EQUAL_TO,
            property: "type",
            value:"BEGIN"
        }),
        symbolizer: {
            externalGraphic: karTheme.startPunt_selected,
            graphicYOffset: -33,
            graphicXOffset: -6
        }
    })
    ]
}
);


var tempstyle = new OpenLayers.Style(
// the first argument is a base symbolizer
// all other symbolizers in rules will extend this one
{
    graphicWidth: 37,
    graphicHeight: 39,
    graphicYOffset: -21, 
    labelYOffset: -20,
    label: "${label}" 
},
// the second argument will include all rules
{
    rules: [
    new OpenLayers.Rule({
        filter: new OpenLayers.Filter.Function({
            evaluate: function (attributes){ return hasMemo(attributes, "CROSSING");}
        }),
        symbolizer: {
            externalGraphic: karTheme.crossing_attachment,
            graphicYOffset: -26,
            label: "${description}"
        }
    }),
    new OpenLayers.Rule({
        filter: new OpenLayers.Filter.Function({
            evaluate:  function (attributes){ return hasNoMemo(attributes, "CROSSING");}
        }),
        // if a feature matches the above filter, use this symbolizer
        symbolizer: {
            externalGraphic: karTheme.crossing,
            graphicYOffset: -26,
            label: "${description}"
        }
    }),
    
    // GUARD
    new OpenLayers.Rule({
        // a rule contains an optional filter
        filter:  new OpenLayers.Filter.Function({
            evaluate: function (attributes){ return hasMemo(attributes, "GUARD");}
        }),
        // if a feature matches the above filter, use this symbolizer
        symbolizer: {
            externalGraphic: karTheme.guard_attachment,
            graphicYOffset: -26,
            label: "${description}"
        }
    }),
    new OpenLayers.Rule({
        // a rule contains an optional filter
        filter: new OpenLayers.Filter.Function({
            evaluate: function (attributes){ return hasNoMemo(attributes, "GUARD");}
        }),
        // if a feature matches the above filter, use this symbolizer
        symbolizer: {
            externalGraphic: karTheme.guard,
            graphicYOffset: -26,
            label: "${description}"
        }
    }),
    new OpenLayers.Rule({
        // a rule contains an optional filter
        filter:  new OpenLayers.Filter.Function({
            evaluate: function (attributes){ return hasMemo(attributes, "BAR");}
        }),
        // if a feature matches the above filter, use this symbolizer
        symbolizer: {
            externalGraphic: karTheme.bar_attachment,
            graphicYOffset: -26,
            label: "${description}"
        }
    }),
    new OpenLayers.Rule({
        filter:  new OpenLayers.Filter.Function({
            evaluate: function (attributes){ return hasNoMemo(attributes,"BAR");}
        }),
        // if a feature matches the above filter, use this symbolizer
        symbolizer: {
            externalGraphic: karTheme.bar,
            graphicYOffset: -26,
            label: "${description}"
        }
    }),
    new OpenLayers.Rule({
        elseFilter: true,
        symbolizer: {
            externalGraphic: karTheme.point,
            label: "",
            strokeColor: "#99BCE8",
            strokeLinecap: "butt",
            strokeDashstyle: "longdash"
        }
    }),
    new OpenLayers.Rule({   
        filter: new OpenLayers.Filter.Comparison({
            type: OpenLayers.Filter.Comparison.EQUAL_TO,
            property: "type",
            value:"ACTIVATION_1"
        }),
        symbolizer: {
            externalGraphic: karTheme.signInPoint
        }
    }),
    new OpenLayers.Rule({   
        filter: new OpenLayers.Filter.Comparison({
            type: OpenLayers.Filter.Comparison.EQUAL_TO,
            property: "type",
            value:"ACTIVATION_2"
        }),
        symbolizer: {
            externalGraphic: karTheme.signOutPoint
        }
    }),
    new OpenLayers.Rule({   
        filter: new OpenLayers.Filter.Comparison({
            type: OpenLayers.Filter.Comparison.EQUAL_TO,
            property: "type",
            value:"ACTIVATION_3"
        }),
        symbolizer: {
            externalGraphic: karTheme.preSignInPoint
        }
    }),
    new OpenLayers.Rule({   
        filter: new OpenLayers.Filter.Comparison({
            type: OpenLayers.Filter.Comparison.EQUAL_TO,
            property: "type",
            value:"END"
        }),
        symbolizer: {
            externalGraphic: karTheme.endPoint,
            graphicYOffset: -33,
            graphicXOffset: -6
        }
    }),
    new OpenLayers.Rule({   
        filter: new OpenLayers.Filter.Comparison({
            type: OpenLayers.Filter.Comparison.EQUAL_TO,
            property: "type",
            value:"BEGIN"
        }),
        symbolizer: {
            externalGraphic: karTheme.startPoint,
            graphicYOffset: -33,
            graphicXOffset: -6
        }
    })
    ]
}
);

var snap = new OpenLayers.Style(
// the first argument is a base symbolizer
// all other symbolizers in rules will extend this one
{
   // strokeColor: "#99BCE8",
    strokeLinecap: "butt",
    strokeDashstyle: "dash",
    strokeColor: "#9E9E9E",
    stokeOpacity: 0.7,
    strokeWidth: 1
});

function hasMemo(attributes, type){
    if(attributes.type == type){
        var memo = attributes.memo;
        if(memo && memo != ""){
            return true;
        }else{
            return false;
        }
    }
    return false;
}

function hasNoMemo(attributes, type){
    if(attributes.type == type){
        var memo = attributes.memo;
        if(memo && memo != ""){
            return false;
        }else{
            return true;
        }
    }
    return false;
}