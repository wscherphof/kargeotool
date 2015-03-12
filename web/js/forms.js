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
 * Forms om KAR objecten te editen in een popup window met ExtJS form.
 */

Ext.define("EditForms", {

    editor: null,

    rseqEditWindow: null,
    karAttributesEditWindow: null,
    pointEditWindow: null,
    activationPointEditWindow: null,
    editCoords:null,
    editDirectionWindow:null,
    carriersWindow:null,

    rseqType: {
        "": "nieuw verkeerssysteem",
        "CROSSING": "VRI",
        "GUARD": "bewakingssysteem nadering voertuig",
        "BAR": "afsluittingssysteem"
    },

    constructor: function(editor) {
        this.editor = editor;
    },

    /**
     * Opent een popup waarmee de gebruiker een rseq kan wijzigen
     * @param newRseq (optioneel) de rseq die de init waardes invult in het formulier
     * als de newRseq niet wordt meegegeven wordt het geselecteerde object gebruikt
     * @param okHanlder de functie die wordt aangeroepen nadat er op 'ok' is geklikt
     * De functie krijg als parameter de gewijzigde rseq mee.
     */
    editRseq: function(newRseq, okHandler) {
        var rseq = newRseq || editor.selectedObject;

        if(this.rseqEditWindow != null) {
            this.rseqEditWindow.destroy();
            this.rseqEditWindow = null;
        }

        var okFunction = function (form) {
            var form = Ext.getCmp ("rseqForm").getForm();

            if(!form.isValid()) {
                Ext.Msg.alert('Ongeldige gegevens', 'Controleer aub de geldigheid van de ingevulde gegevens.')
                return;
            }

            Ext.Object.merge(rseq, form.getValues());
            if(rseq == me.editor.activeRseq) {
                me.editor.fireEvent("activeRseqUpdated", rseq);
            }
            me.rseqEditWindow.destroy();
            me.rseqEditWindow = null;

            if(okHandler) {
                okHandler(rseq);
            }
        };

        var me = this;
        var theType = rseq.type == "" ? "CROSSING" : rseq.type; // default voor nieuw
        me.rseqEditWindow = Ext.create('Ext.window.Window', {
            title: 'Bewerken ' + me.rseqType[rseq.type] + (rseq.karAddress == null ? "" : " met KAR adres " + rseq.karAddress),
            height: 372,
            width: 450,
            modal: true,
            icon: rseq.type == "" ? karTheme.crossing : karTheme[rseq.type.toLowerCase()],
            layout: 'fit',
            listeners: {
                afterRender: function(thisForm, options){
                    this.keyNav = Ext.create('Ext.util.KeyNav', this.el, {
                        enter: okFunction,
                        scope: this
                    });
                }
            },
            items: {
                id: 'rseqForm',
                disabled: !rseq.editable,
                xtype: 'form',
                bodyStyle: 'padding: 5px 5px 0',
                fieldDefaults: {
                    msgTarget: 'side',
                    labelWidth: 150
                },
                defaultType: 'textfield',
                defaults: {
                    anchor: '100%'
                },
                items: [{
                    xtype: 'fieldcontainer',
                    fieldLabel: 'Soort verkeerssysteem',
                    defaultType: 'radiofield',
                    layout: 'vbox',
                    items: [{
                        name: 'type',
                        inputValue: 'CROSSING',
                        checked: theType == 'CROSSING',
                        boxLabel: me.rseqType['CROSSING']
                    },{
                        name: 'type',
                        inputValue: 'GUARD',
                        checked: theType == 'GUARD',
                        boxLabel: me.rseqType['GUARD']
                    },{
                        name: 'type',
                        inputValue: 'BAR',
                        checked: theType == 'BAR',
                        boxLabel: me.rseqType['BAR']
                    }]
                },{
                    xtype: 'combo',
                    fieldLabel: 'Beheerder',
                    validator:function(value){
                        var list =this.store.proxy.data;
                        var found = false;
                        for (var i = 0 ; i < list.length ;i++){
                            var entry = list[i];
                            if(entry.omschrijving == value){
                                found = true;
                                break;
                            }
                        }
                        if(found){
                            return true;
                        }else{
                            return "Waarde " + value + " is geen bestaande beheerder.";
                        }
                    },
                    name: 'dataOwner',
                    allowBlank: false,
                    blankText: 'Selecteer een optie',
                    displayField: 'omschrijving',
                    queryMode:'local',
                    typeAhead:true,
                    minChars:2,
                    valueField: 'code',
                    value: rseq.dataOwner,
                    store: Ext.create('Ext.data.Store', {
                        fields: ['code', 'classificatie', 'companyNumber', 'omschrijving'],
                        data: dataOwners
                    }),
                    listeners: {
                      buffer: 50,
                      change: function() {
                        var store = this.store;
                        //store.suspendEvents();
                        store.clearFilter();
                        //store.resumeEvents();
                        store.filter({
                            property: 'omschrijving',
                            anyMatch: true,
                            value   : this.getValue()
                        });
                      }
                    }
                },{
                    fieldLabel: 'Beheerdersaanduiding',
                    name: 'crossingCode',
                    value: rseq.crossingCode
                },{
                    xtype: 'numberfield',
                    fieldLabel: 'KAR adres',
                    name: 'karAddress',
                    allowBlank: false,
                    minValue: 0,
                    value: rseq.karAddress,
                    listeners: {
                        change: function(field, value) {
                            value = parseInt(value, 10);
                            field.setValue(value);
                        }
                    }
                },{
                    fieldLabel: 'Plaats',
                    name: 'town',
                    allowBlank: false,
                    value: rseq.town
                },{
                    fieldLabel: 'Locatie',
                    name: 'description',
                    value: rseq.description
                },{
                    xtype: 'datefield',
                    format: 'Y-m-d',
                    fieldLabel: 'Geldig vanaf',
                    name: 'validFrom',
                    value: rseq.validFrom
                },{
                    xtype: 'datefield',
                    format: 'Y-m-d',
                    fieldLabel: 'Geldig tot',
                    name: 'validUntil',
                    value: rseq.validUntil
                },{
                    xtype: 'panel',
                    border: false,
                    items: [{
                        xtype: 'button',
                        name: 'karAttributes',
                        width: 100,
                        text: 'KAR attributen...',
                        handler: function() {
                            me.editKarAttributes(rseq);
                        }
                    }]
                }],
                buttons: [{
                    text: 'OK',
                    handler: okFunction
                },{
                    text: 'Annuleren',
                    handler: function() {
                        me.rseqEditWindow.destroy();
                        me.rseqEditWindow = null;
                    }
                }]
            }
        }).show();
    },

    /**
     * KAR attributen edit form voor een rseq.
     */
    editKarAttributes: function(rseq) {
        if(this.karAttributesEditWindow != null) {
            this.karAttributesEditWindow.destroy();
            this.karAttributesEditWindow = null;
        }

        this.karAttributesEditWindow = Ext.create(KarAttributesEditWindow,
            "Bewerken KAR attributen voor " + this.rseqType[rseq.type] +
                (rseq.karAddress == null ? "" : " met KAR adres " + rseq.karAddress),
            "In dit scherm kan worden aangegeven welke KAR attributen in KAR " +
                "berichten die aan dit verkeerssysteem worden verzonden moeten " +
                "worden gevuld. Dit geldt voor alle soorten berichten " +
                "(voorinmeldpunt, inmeldpunt en uitmeldpunt).",
            rseq.attributes,
            function(atts) {
                rseq.attributes = atts;
            }
        );

        this.karAttributesEditWindow.show();
    },

    /**
     * Opent een popup waarmee een non activation punt kan worden bewerkt.
     */
    editNonActivationPoint: function(newPoint, okHandler, cancelHandler) {
        var rseq = this.editor.activeRseq;
        var point = newPoint || this.editor.selectedObject;

        if(this.pointEditWindow != null) {
            this.pointEditWindow.destroy();
            this.pointEditWindow = null;
        }
        var okFunction =function() {
            var form = Ext.getCmp("nonActivationForm").getForm();
            if(!form.isValid()) {
                Ext.Msg.alert('Ongeldige gegevens', 'Controleer aub de geldigheid van de ingevulde gegevens.')
                return;
            }

            Ext.Object.merge(point, form.getValues());
            if(rseq == me.editor.activeRseq) {
                me.editor.fireEvent("activeRseqUpdated", rseq);
            }
            me.pointEditWindow.destroy();
            me.pointEditWindow = null;

            if(okHandler) {
                okHandler(point);
            }
        };
        var me = this;
        var label = point.getLabel() == null ? "" : point.getLabel();
        me.pointEditWindow = Ext.create('Ext.window.Window', {
            title: 'Bewerken ' + (point.getType() == null ? "ongebruikt punt " : (point.getType() == "BEGIN" ? "beginpunt " : "eindpunt ")) + label,
            height: 96,
            width: 250,
            modal: true,
            icon: point.getType() == null ? karTheme.punt : (point.getType() == 'BEGIN' ? karTheme.beginPunt :karTheme.eindPunt),
            layout: 'fit',
            listeners: {
                afterRender: function(thisForm, options){
                    this.keyNav = Ext.create('Ext.util.KeyNav', this.el, {
                        enter: okFunction,
                        scope: this
                    });
                }
            },
            items: {
                xtype: 'form',
                id:'nonActivationForm',
                bodyStyle: 'padding: 5px 5px 0',
                fieldDefaults: {
                    msgTarget: 'side',
                    labelWidth: 150
                },
                defaultType: 'textfield',
                defaults: {
                    anchor: '100%'
                },
                items: [{
                    fieldLabel: 'Label',
                    name: 'label',
                    value: point.label,
                    id: 'labelEdit'
                }],
                buttons: [{
                    text: 'OK',
                    handler: okFunction
                },{
                    text: 'Annuleren',
                    handler: function() {
                        me.pointEditWindow.destroy();
                        me.pointEditWindow = null;

                        if(cancelHandler) {
                            cancelHandler();
                        }
                    }
                }]
            }
        }).show();
        Ext.getCmp("labelEdit").selectText(0);
        Ext.getCmp("labelEdit").focus(false, 100);
    },

    /**
     * Opent een window waarmee een activation punt kan worden gewijzgd.
     */
    editActivationPoint: function(newUitmeldpunt, newMap, okHandler, cancelHandler) {
        var rseq = this.editor.activeRseq;
        var point = newUitmeldpunt || this.editor.selectedObject;

        if(this.activationPointEditWindow != null) {
            this.activationPointEditWindow.destroy();
            this.activationPointEditWindow = null;
        }

        var me = this;
        var label = point.getLabel() == null ? "" : point.getLabel();
        var apType = point.getType().split("_")[1];
        var apName = (apType == "1" ? "inmeld" : (apType == "2" ? "uitmeld" : "voorinmeld")) + "Punt";

        var map = newMap;
        var movements = null;
        if(!map) {
            if(this.editor.activeMovement){
                var mvmnt = this.editor.activeRseq.getMovementById(this.editor.activeMovement);

                map = rseq.findMapForPoint(mvmnt.id, point.id);
                if (!mvmnt || !map) {
                    alert("Kan geen movements vinden voor activation point!");
                    return;
                }
                movements = [{
                    movement: mvmnt,
                    map:map
                }];
            } else {
                movements = rseq.findMovementsForPoint(point);
                if (movements.length === 0) {
                    alert("Kan geen movements vinden voor activation point!");
                    return;
                }
                map = movements[0].map;
            }

         
        }

        var ov = [];
        var hulpdienst = [];
        var vehicles = {root:{
            text: 'Alle',
            id: 'root',
            iconCls: "noTreeIcon",
            expanded: true,
            checked: false,
            children :[
                {
                    id: 'ov-node',
                    text: 'OV',
                    checked: false,
                    iconCls: "noTreeIcon",
                    expanded:false,
                    leaf: false,
                    children: ov
                },
                {
                    id: 'hulpdienst-node',
                    text: 'Hulpdiensten',
                    checked: false,
                    iconCls: "noTreeIcon",
                    expanded:false,
                    leaf: false,
                    children: hulpdienst
                }
            ]
        }};
        var selectedVehicleTypes = [];
        if(!map.vehicleTypes){
            map.vehicleTypes = new Array();
        }
        Ext.Array.each(vehicleTypes, function(vt) {
            var selected = map.vehicleTypes.indexOf(vt.nummer) != -1;
            if(selected) {
                selectedVehicleTypes.push(vt.nummer);
            }
            if(selected || vt.omschrijving.indexOf('Gereserveerd') == -1) {
                var leaf = {
                        id: vt.nummer,
                        text: vt.omschrijving,
                        checked: false,
                        iconCls: "noTreeIcon",
                        leaf: true};
                if(vt.groep == "OV"){
                    ov.push(leaf);
                }else{
                    hulpdienst.push(leaf);
                }
            }
        });

        var vehicleTypesStore = Ext.create('Ext.data.TreeStore', vehicles);

        var editingUitmeldpunt = map.commandType === 2;
        var edittingInmeldpunt = map.commandType === 1;
        var signalItems = [{
            xtype: 'numberfield',
            fieldLabel: 'Afstand tot stopstreep',
            name: 'distanceTillStopLine',
            value: map.distanceTillStopLine
        },
        {
            xtype: 'numberfield',
            minValue: 0,
            listeners: {
                change: function(field, value) {
                    value = parseInt(value, 10);
                    field.setValue(value);
                }
            },
            fieldLabel: 'Virtual local loop number',
            name: 'virtualLocalLoopNumber',
            value: map.virtualLocalLoopNumber
        },{
            xtype: 'treecombo',
            valueField: 'id',
            editable:false,
            value:  selectedVehicleTypes.join(","),
            fieldLabel: 'Voertuigtypes',
            treeWidth:290,
            treeHeight: 300,
            name: 'vehicleTypes',
            store: vehicleTypesStore
        },
        {
            xtype: 'combo',
            fieldLabel: 'Triggertype',
            name: 'triggerType',
            allowBlank: false,
            blankText: 'Selecteer een optie',
            editable: false,
            hidden:!edittingInmeldpunt,
            displayField: 'desc',
            valueField: 'value',
            value: map.triggerType,
            store: Ext.create('Ext.data.Store', {
                fields: ['value', 'desc'],
                data: [
                    {value: 'STANDARD', desc: 'Standaard: door vervoerder bepaald'},
                    {value: 'FORCED', desc: 'Automatisch: altijd melding'},
                    {value: 'MANUAL', desc: 'Handmatig: handmatig door chauffeur'}
                ]
            })
        }];
        if(editingUitmeldpunt) {
            var dirs = { root:{
                text: 'Alle',
                id: 'root',
                iconCls: "noTreeIcon",
                expanded: true,
                checked: false,
                children :[
                    {
                        id: 1,
                        text: 'Linksaf',
                        checked: false,
                        leaf: true,
                        iconCls: 'noTreeIcon'
                    },
                    {
                        id: 2,
                        text: 'Rechtsaf',
                        checked: false,
                        leaf: true,
                        iconCls: 'noTreeIcon'
                    },
                    {
                        id: 3,
                        text: 'Rechtdoor',
                        checked: false,
                        leaf: true,
                        iconCls: 'noTreeIcon'
                    }
                ]
            }};

            var directionStore = Ext.create('Ext.data.TreeStore', dirs);
            var direction = map.direction ? map.direction : "";
            var dir = direction.join ? direction.join(",") : direction;
            signalItems = Ext.Array.merge(signalItems, [{
                xtype: 'numberfield',
                minValue: 0,
                listeners: {
                    change: function(field, value) {
                        value = parseInt(value, 10);
                        field.setValue(value);
                    }
                },
                fieldLabel: 'Signaalgroep',
                allowBlank: false,
                name: 'signalGroupNumber',
                value: map.signalGroupNumber
            },
            {
                xtype: 'treecombo',
                valueField: 'id',
                editable:false,
                value:  dir,
                fieldLabel: 'Richting(en)',
                treeWidth:290,
                treeHeight: 150,
                name: 'direction',
                store: directionStore
            }
        ]);
        }

        var okFunction = function() {
            var form = Ext.getCmp('activationForm').getForm();
            if(!form.isValid()) {
                Ext.Msg.alert('Ongeldige gegevens', 'Controleer aub de geldigheid van de ingevulde gegevens.')
                return;
            }

            // merge label naar point
            var formValues = form.getValues();
            Ext.Object.merge(point, objectSubset(formValues, ["label"]));

            // deze waardes naar alle maps van movements die dit
            // pointId gebruiken
            var pointSignalValues = objectSubset(formValues, ["distanceTillStopLine", "virtualLocalLoopNumber", "vehicleTypes", "triggerType"]);

            // Alleen bij uitmeldpunt kunnen deze worden ingesteld
            // maar moeten voor alle movements worden toegepast op
            // alle signals
            if(editingUitmeldpunt) {
                allSignalValues = objectSubset(formValues, ["signalGroupNumber","direction"]);
            }

            // nieuw punt, alleen naar map mergen
            if(!movements) {
                Ext.Object.merge(map, pointSignalValues);
                if(editingUitmeldpunt) {
                    Ext.Object.merge(map, allSignalValues);
                }
            } else {

                // merge naar alle movements

                Ext.Array.each(movements, function(mvmtAndMap) {
                    //console.log("movement nummer " + mvmtAndMap.movement.nummer);
                    if(mvmtAndMap.map) {
                        //console.log("merging distanceTillStopLine and triggerType values to movement nummer " + mvmtAndMap.movement.nummer + " map pointId " + mvmtAndMap.map.pointId);
                        Ext.Object.merge(mvmtAndMap.map, pointSignalValues);
                    }
                    if(editingUitmeldpunt) {
                        // merge allSignalValues naar alle MovementActivationPoints
                        // van movements die dit pointId gebruiken
                        Ext.each(mvmtAndMap.movement.maps, function(theMap) {
                            if(theMap.beginEndOrActivation == "ACTIVATION") {
                                //console.log("merging signalGroupNumber, virtualLocalLoopNumber and vehicleType values point signal values to movement nummer " + mvmtAndMap.movement.nummer + " map pointId " + theMap.pointId);
                                Ext.Object.merge(theMap, allSignalValues);
                            }
                        });
                    }
                });
            }

            if(rseq == me.editor.activeRseq) {
                me.editor.fireEvent("activeRseqUpdated", rseq);
            }
            me.activationPointEditWindow.destroy();
            me.activationPointEditWindow = null;

            if(okHandler) {
                okHandler(point);
            }
        };

        this.activationPointEditWindow = Ext.create('Ext.window.Window', {
            title: 'Bewerken ' + apName.toLowerCase() + " " + label,
            height: map.commandType == 2 ? 310 : 247,
            width: 490,
            modal: true,
            icon: karTheme[apName],
            layout: 'fit',
             listeners: {
                afterRender: function(thisForm, options){
                    this.keyNav = Ext.create('Ext.util.KeyNav', this.el, {
                        enter: okFunction,
                        scope: this
                    });
                }
            },
            items: {
                xtype: 'form',
                id: 'activationForm',
                bodyStyle: 'padding: 5px 5px 0',
                fieldDefaults: {
                    msgTarget: 'side',
                    labelWidth: 150
                },
                defaultType: 'textfield',
                defaults: {
                    anchor: '100%'
                },
                items: [{
                    xtype:'fieldset',
                    title: 'KAR signaal',
                    collapsible: false,
                    defaultType: 'textfield',
                    layout: 'anchor',
                    defaults: {
                        anchor: '100%'
                    },
                    items: signalItems
                },{
                    fieldLabel: 'Label',
                    name: 'label',
                    value: point.label,
                    maxLength: 4,
                    maxLengthText: "Maximale lengte is 4 karakters",
                    id: 'labelEdit'
                }],
                buttons: [{
                    text: 'OK',
                    handler: okFunction
                },{
                    text: 'Annuleren',
                    handler: function() {
                        me.activationPointEditWindow.destroy();
                        me.activationPointEditWindow = null;

                        if(cancelHandler) {
                            cancelHandler();
                        }
                    }
                }]
            }
        }).show();
        Ext.getCmp("labelEdit").selectText(0);
        Ext.getCmp("labelEdit").focus(false, 100);
    },

    editCoordinates : function (pointObject,okHandler,cancelHandler){
        var me = this;
        var coords = "";
        if(pointObject instanceof RSEQ){
            coords = pointObject.getLocation().coordinates;
        }else{
            coords = pointObject.getGeometry().coordinates;
        }
        var rdX = coords[0];
        var rdY = coords[1];
        var point = new Proj4js.Point(rdX, rdY);

        var wgs = new Proj4js.Proj("EPSG:4236");
        var rd = new Proj4js.Proj("EPSG:28992");
        Proj4js.transform(rd, wgs, point);
        var wgsX = point.x;
        var wgsY = point.y;

        var okFunction = function() {
            var form = Ext.getCmp('coordinatesForm').getForm();
            if(!form.isValid()) {
                Ext.Msg.alert('Ongeldige gegevens', 'Controleer aub de geldigheid van de ingevulde gegevens.');
                return;
            }

            var formValues= form.getValues();
            if(rdX == formValues.rdX && rdY == formValues.rdY){
                if(wgsX != formValues.wgsX && wgsY == formValues.wgsY){
                    // converteer nieuwe wgs naar rd en sla ze op
                    var point = new Proj4js.Point(formValues.wgsX, formValues.wgsY);
                    Proj4js.transform(wgs, rd, point);
                    rdX = point.x;
                    rdY = point.y;
                }
            }else{
                rdX = formValues.rdX;
                rdY = formValues.rdY;
            }

            var coordObj = {coordinates:[parseFloat(rdX),parseFloat(rdY)], type: 'Point'};
            if(pointObject instanceof RSEQ){
                pointObject.setLocation(coordObj);
            }else{
                pointObject.setGeometry(coordObj);
            }

            me.editor.fireEvent("activeRseqUpdated", me.editor.activeRseq);
            me.editCoords.destroy();
            me.editCoords = null;

            if(okHandler) {
                okHandler(point);
            }
        };
        this.editCoords = Ext.create('Ext.window.Window', {
            title: 'Voer coördinaten',
            height: 240,
            width: 400,
            modal: true,
            icon: karTheme.gps,
            listeners: {
                afterRender: function(thisForm, options){
                    this.keyNav = Ext.create('Ext.util.KeyNav', this.el, {
                        enter: okFunction,
                        scope: this
                    });
                }
            },
            layout: 'fit',
            items: {
                xtype: 'form',
                id: 'coordinatesForm',
                bodyStyle: 'padding: 5px 5px 0',
                fieldDefaults: {
                    msgTarget: 'side',
                    labelWidth: 150
                },
                defaultType: 'textfield',
                defaults: {
                    anchor: '100%'
                },
                items: [
                    {
                        xtype: 'fieldset',
                        defaultType: 'textfield',
                        text: 'Rijksdriehoek',
                        items:[
                            {
                                fieldLabel: 'RD x-coordinaat',
                                name: 'rdX',
                                value: rdX,
                                id: 'rdX'
                            },{
                                fieldLabel: 'RD y-coordinaat',
                                name: 'rdY',
                                value: rdY,
                                id: 'rdY'
                            }]
                    },
                    {
                        xtype: 'fieldset',
                        text: 'GPS',
                        defaultType: 'textfield',
                        items:[{
                                fieldLabel: 'GPS y-coordinaat',
                                name: 'wgsY',
                                value: wgsY,
                                id: 'wgsY'
                            },
                            {
                                fieldLabel: 'GPS x-coordinaat',
                                name: 'wgsX',
                                value: wgsX,
                                id: 'wgsX'
                            }]
                    }
                    ],
                buttons: [{
                    text: 'OK',
                    handler:okFunction
                },{
                    text: 'Annuleren',
                    handler: function() {
                        me.editCoords.destroy();
                        me.editCoords = null;

                        if(cancelHandler) {
                            cancelHandler();
                        }
                    }
                }]
            }
        }).show();
    },

    showCarriers: function (carriers, okFunction) {
        var carrierStore = Ext.create('Ext.data.Store', {
            storeId: 'carriersStore',
            fields: ['id', 'mail', 'username'],
            data: {'items': carriers},
            proxy: {
                type: 'memory',
                reader: {
                    type: 'json',
                    root: 'items'
                }
            }
        });
        var me = this;

        this.carriersWindow = Ext.create('Ext.window.Window', {
            title: 'Selecteer vervoerder(s)',
           // height: 200,
            width: 420,
            items: [{
                    fieldLabel: 'Kies vervoerder(s)',
                    labelWidth: 150,
                    width: 400,
                    store: carrierStore,
                    queryMode: 'local',
                    name: "carrierIds",
                    id: "carrierIds",
                    multiSelect: true,
                    displayField: 'username',
                    valueField: 'id',
                    xtype: "combo"
                }],
            bbar: [
                {xtype: 'button', text: 'Informeren', handler: function(){
                        var ids = Ext.getCmp("carrierIds").getValue();
                        me.carriersWindow.destroy();
                        me.carriersWindow = null;
                        okFunction(ids);
                }},
                {xtype: 'button', text: 'Annuleren', handler: function(){
                    me.carriersWindow.destroy();
                    me.carriersWindow = null;
                }}
            ]
        });
        this.carriersWindow.show();
    },

    hasOpenWindows : function(){
        var windows = new Array();
        windows.push(this.rseqEditWindow);
        windows.push(this.karAttributesEditWindow);
        windows.push(this.pointEditWindow);
        windows.push(this.activationPointEditWindow);
        windows.push(this.editCoords);
        windows.push(this.editDirectionWindow);

        for( var i = 0 ; i < windows.length; i++){
            var window = windows[i];
            if(window && !window.isHidden() ){
                return true;
            }
        }

        return false;
    }

});