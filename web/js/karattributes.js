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

/**
 * Form voor editen KAR attributen.
 */

function karAttributeClick(event, row, column) {
    if(!event) {
        event = window.event;
    }
    globalKarAttributesEditWindow.karAttributeClick(row, column, event.target.checked);
}

var globalKarAttributesEditWindow = null;

Ext.define('KarAttributesEditWindow', {
    window: null, 
    attributes: null,
    editingData: null,
    label: null,
    title: null,
    onSave: null,
    
    karAttributes: [
        {n: 1, label: "Virtual local loop number", desc: "Unique number within the VRI-number"},
        {n: 2, label: "Vehicle type", desc: "0 = no information\n1 = bus\n2 = tram\netc."},
        {n: 3, label: "Line number PT", desc: "Line-number (internal line number PT-company)"},
        {n: 4, label: "Block number", desc: "Vehicle Service number/Block number"},
        {n: 5, label: "Company number", desc: "For unique identification vehicle id"},
        {n: 6, label: "Vehicle id", desc: "0 = no information\nFor public transport the " +
                "grootwagennummer is used (practially in range 1 to 9999)\nFor emergency services the last 4 " +
                "digits of the 14 digits C2000 number are used (the so called 'own number')"},
        {n: 7, label: "Direction at intersection/signal group number", desc: "For the direction selection \n" +
                "at the intersection, it is suggested to use the signal group number. If this signal group number " +
                "is not available a right/left/straigt ahead switch may be used:\n" +
                "0 = no information\n1-200 = signal group number\n201 = right\n202 = left\n203 = straight ahead\n204-255 = reserved"},
        {n: 8, label: "Vehicle status", desc: "0 = no information\n1 = driving\n2 = stopping\n" +
                "3 = departure from stop (start door close)\n4 = stand still (stop, not at bus stop)\n" +
                "5 - 99 = reserved"},
        {n: 9, label: "Priority class", desc: "0 = no information\n1 = no priority (f.e. only request)\n" +
                "2 = conditional\n3 = absolute\n4 = alarm light\n5 - 99 = reserved"},
        {n: 10, label: "Punctuality class", desc: "0 = not used, no priority none (absent)\n" +
                "1 = late\n2 = on time\n3 = early\n4 = off schedule\n5 - 99 = reserved"},
        {n: 11, label: "Punctuality [s]", desc: "- early (<0)\nlate (>0)"},
        {n: 12, label: "Vehicle / train length [m]", desc: "Vehicle / train length in meters"},
        {n: 13, label: "Actual vehicle speed [m/s]", desc: "Actual speed when te message is sent [m/s]"},
        {n: 14, label: "Distance till passage stop line [m]", desc: "Actual distance till passage stop line [meters]"},
        {n: 15, label: "Driving time till passage stop line", desc: "Expected time until passage stop line " +
                "(without delay for other traffic, for example wait-row) in seconds for th efirst Traffic Light Controller on the route"},
        {n: 16, label: "Journey number", desc: "Journey number"},
        {n: 17, label: "Type of Journey or Fortify seq number", desc: "0 = no information\n" +
                "1 - 9 = sequence number versterkingsrit. Sequence number for extra vehicles on the same public journey\n" +
                "10 = dienstregelingrit (public journey)\n11 = dead run\n12 = pull in journey (to remise/depot)\n" +
                "13 = pull out journey (from remise/depot)\n14 - 99 = reserved"},
        {n: 18, label: "Route Public Transport", desc: "0 = no information\n1 = route 1 (A-route, away direction)\n" +
                "2 = route 2 (B-route, back direction)\n3 - 99 = free to be used for other route descriptions"},
        {n: 19, label: "Type of command", range: "0 - 99", desc: "0 - reserved\n1 - entering announcement\n" +
                "2 - leave announcement\n3 - pre-announcement\n4..99 - reserved"},
        {n: 20, label: "Activation pointnr", desc: "Location-information (in database PT-company)\n0 = no information"},
        {n: 21, label: "Location in WGS84 (latitude and longitude)"},
        {n: 22, label: "Date and time when sending message in onboard computer"},
        {n: 23, label: "Reserve"},
        {n: 24, label: "Reserve"}
    ],
    
    constructor: function(title, label, attributes, onSave) {
        this.title = title;
        this.label = label;
        this.attributes = attributes;
        this.onSave = onSave;
    },
    
    karAttributeClick: function(row, column, checked) {
        var field = ["", "", "PT", "ES", "OT"][column];
        
        this.editingData[row][field] = checked;
    },
    
    show: function() {
        var me = this;
        var attributes = this.attributes;
        var data = [];
        for(var i = 1; i < 25; i++) {
            var attr = null;
            Ext.Array.each(me.karAttributes, function(attrInfo) {
                if(attrInfo.n == i) {
                    attr = Ext.clone(attrInfo);
                    return false;
                }
                return true;
            });
            if(attr == null) {
                attr = {
                    n: i,
                    label: "Onbekend",
                    range: "",
                    desc: ""
                };
            }
            
            // index is PT, ES, OT
            
            attr.PT = attributes["PT"][0][i-1] || attributes["PT"][1][i-1] || attributes["PT"][2][i-1];
            attr.ES = attributes["ES"][0][i-1] || attributes["ES"][1][i-1] || attributes["ES"][2][i-1];
            attr.OT = attributes["OT"][0][i-1] || attributes["OT"][1][i-1] || attributes["OT"][2][i-1];
            data.push(attr);
        }
        this.editingData = data;
        
        var store = Ext.create("Ext.data.Store", {
            storeId: "attributesStore",
            fields: ["n", "label", "range", "desc", "PT", "ES", "OT"],
            data: {items: data},
            proxy: {
                type: "memory",
                reader: { type: "json", root: "items" }
            }
        });
        
        var checkboxRenderer = function(p1,p2,record,row,column,store,grid) {
            var field = ["", "", "PT", "ES", "OT"][column];
            return Ext.String.format("<input type='checkbox' {0} onclick='karAttributeClick(event,{1},{2})'></input>",
                record.get(field) ?  "checked='checked'" : "",
                row,
                column
            );
        };        
        var okFunction = function() {

            attributes = {
                "PT": [[], [], []],
                "ES": [[], [], []],
                "OT": [[], [], []]
            };
            for (var i = 0; i < me.editingData.length; i++) {
                var PT = Boolean(me.editingData[i].PT);
                var ES = Boolean(me.editingData[i].ES);
                var OT = Boolean(me.editingData[i].OT);
                attributes["PT"][0].push(PT);
                attributes["PT"][1].push(PT);
                attributes["PT"][2].push(PT);
                attributes["ES"][0].push(ES);
                attributes["ES"][1].push(ES);
                attributes["ES"][2].push(ES);
                attributes["OT"][0].push(OT);
                attributes["OT"][1].push(OT);
                attributes["OT"][2].push(OT);
            }
            me.onSave(attributes);
            me.window.destroy();
            me.window = null;
            globalKarAttributesEditWindow = null;
        };
        
        this.window = Ext.create(Ext.window.Window, {
            title: me.title,
            height: 600,
            width: 680,
            modal: true,
            icon: karTheme.inmeldPunt,
            layout: 'fit',
            listeners: {
                afterRender: function(thisForm, options){
                    this.keyNav = Ext.create('Ext.util.KeyNav', this.el, {
                        enter: okFunction,
                        scope: this
                    });
                }
            },
            items: [{  
                xtype: 'form',
                bodyStyle: 'padding: 5px 5px 0',
                layout: 'vbox',
                defaults: {
                    width: '100%'
                },
                items: [{
                    xtype: 'label',
                    style: 'display: block; padding: 5px 0px 8px 0px',
                    border: false,
                    text: me.label
                },{
                    xtype: "grid",
                    store: store,
                    columns: [
                        {header: "Nr", dataIndex: "n", menuDisabled: true, draggable: false, sortable: false, width: 30 },
                        {header: "Attribuut", dataIndex: "label", menuDisabled: true, draggable: false, sortable: false, flex: 1},
                        {header: "Openbaar vervoer", dataIndex: "PT", menuDisabled: true, draggable: false, sortable: false, width: 100, renderer: checkboxRenderer },
                        {header: "Hulpdiensten", dataIndex: "ES", menuDisabled: true, draggable: false, sortable: false, width: 75, renderer: checkboxRenderer },
                        {header: "Overig", dataIndex: "OT", menuDisabled: true, draggable: false, sortable: false, width: 60, renderer: checkboxRenderer }
                    ],
                    flex: 1,
                    style: {
                        marginBottom: '5px'
                    }
                }],
                buttons: [{
                    text: 'OK',
                    handler: okFunction
                },{
                    text: 'Annuleren',
                    handler: function() {
                        me.window.destroy();
                        me.window = null;
                        globalKarAttributesEditWindow = null;
                    }
                }]
            }]
        });
        
        globalKarAttributesEditWindow = this;
        
        this.window.show();
    }
});