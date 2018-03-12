/**
* KAR Geo Tool - applicatie voor het registreren van KAR meldpunten
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
 * Form voor editen instellingen die worden opgeslagen in het gebruikersprofiel.
 */

var savedProfile = Ext.JSON.encode(profile);

function saveProfile() {
    setTimeout(checkSaveProfile, 500);
}

function checkSaveProfile() {
    var encodedProfile = Ext.JSON.encode(profile);

    if(encodedProfile != savedProfile) {
        savedProfile = encodedProfile;
        Ext.Ajax.request({
            url: profileActionBeanUrl,
            method: 'POST',
            scope: this,
            params: {settings: Ext.JSON.encode(profile)},
            failure: function (response){
                Ext.MessageBox.show({title: "Ajax fout", msg: "Profiel niet opgeslagen. Probeer het opnieuw of neem contact op met de applicatie beheerder. " +response.responseText, buttons: Ext.MessageBox.OK, icon: Ext.MessageBox.ERROR});
            }
        });
    }
}

Ext.define("SettingsForm", {

    editor: null,

    window: null,

    newDefaultKarAttributes: null,

    /* XXX move to common place */
    karAttributes: [
        {n: 2, label: "Vehicle type", range: "0 - 99", desc: "0 - no information\n1 - bus\n2 - tram\netc."},
        {n: 3, label: "Line number PT", range: "0 - 9999", desc: "Line-number (internal line number PT-company)"},
        {n: 6, label: "Vehicle id", range: "0 - 32767", desc: "0 - no information\nFor public transport the grootwagennummer is used (practially in range 1 to 9999)"},
        {n: 7, label: "Direction at intersection/signal group number", range: "0 - 255", desc: "For the direction selection \n" +
                "at the intersection, it is suggested to use the signal group number. If this signal group number " +
                "is not available a right/left/straigt ahead switch may be used:\n" +
                "0 = no information\n1-200 = signal group number\n201 = right\n202 = left\n203 = straight ahead\n204-255 = reserved"},
        {n: 8, label: "Vehicle status", range: "0 - 99", desc: "0 = no information\n1 = driving\n2 = stopping\n" +
                "3 = departure from stop (start door close)\n4 = stand still (stop, not at bus stop)\n" +
                "5 - 99 = reserved"},
        {n: 11, label: "Punctuality [s]", range: "-3600 - +3600", desc: "- early (<0)\nlate (>0)"},
        {n: 13, label: "Actual vehicle speed [m/s]", range: "0 - 99", desc: "Actual speed when te message is sent [m/s]"},
        {n: 15, label: "Driving time till passage stop line", range: "0 - 255", desc: "Expected time until passage stop line " +
            "(without delay for other traffic, for example wait-row) in seconds for th efirst Traffic Light Controller on the route"},
        {n: 19, label: "Type of command", range: "0 - 99", desc: "0 - reserved\n1 - entering announcement\n" +
                "2 - leave announcement\n3 - pre-announcement\n4..99 - reserved"}
    ],

    constructor: function(editor) {
        this.editor = editor;
    },

    show: function() {
        if(this.window != null) {
            this.window.destroy();
            this.window = null;
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
                    text: 'HD',
                    checked: false,
                    iconCls: "noTreeIcon",
                    expanded:false,
                    leaf: false,
                    children: hulpdienst
                }
            ]
        }};

        Ext.Array.each(vehicleTypes, function(vt) {
            if( vt.omschrijving.indexOf('Gereserveerd') == -1) {
                var leaf = {
                    id: vt.nummer,
                    text: vt.omschrijving,
                    checked: false,
                    iconCls: "noTreeIcon",
                    leaf: true
                };
                if(vt.groep == "OV"){
                    ov.push(leaf);
                }else{
                    hulpdienst.push(leaf);
                }
            }
        });

        var vehicleTypesStore = Ext.create('Ext.data.TreeStore', vehicles);

        var carrierStore = Ext.create('Ext.data.Store', {
            storeId: 'carriersStore',
            fields: ['id', 'mail', 'username'],
            data: [],
            proxy: {
                type: 'memory',
                reader: {
                    type: 'json',
                    root: 'items'
                }
            }
        });

        var okFunction = function() {
            var form = Ext.getCmp('settingsForm').getForm();
            if (!form.isValid()) {
                Ext.Msg.alert('Ongeldige gegevens', 'Controleer aub de geldigheid van de ingevulde gegevens.')
                return;
            }

            Ext.Object.merge(profile, form.getValues());

            if (me.newDefaultKarAttributes != null) {
                profile.defaultKarAttributes = me.newDefaultKarAttributes;
            }
            saveProfile();

            me.window.destroy();
            me.window = null;
        };
        var me = this;
        me.window = Ext.create('Ext.window.Window', {
            title: 'Instellingen',
            height: 600,
            width: 550,
            modal: true,
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
                id: 'settingsForm',
                bodyStyle: 'padding: 5px 5px 0',
                fieldDefaults: {
                    msgTarget: 'side',
                    labelWidth: 180
                },
                defaultType: 'textfield',
                defaults: {
                   anchor: '100%'
                },
                items: [{
                    xtype: 'treecombo',
                    valueField: 'id',
                    editable:false,
                    value: profile.vehicleTypes ? profile.vehicleTypes.join(",") : [1,2,6,7,71].join(","),
                    fieldLabel: 'Voertuigtypes',
                    treeWidth:290,
                    treeHeight: 300,
                    name: 'vehicleTypes',
                    store: vehicleTypesStore
                },{
                    fieldLabel: 'Kies vervoerder(s) OV',
                    width: 400,
                    store: carrierStore,
                    queryMode: 'local',
                    name: "defaultCarriers",
                    id: "defaultCarriers",
                    multiSelect: true,
                    displayField: 'username',
                    valueField: 'id',
                    xtype: "combo",
                    anyMatch: true
                },{
                    fieldLabel: 'Kies vervoerder(s) HD',
                    width: 400,
                    store: carrierStore,
                    queryMode: 'local',
                    name: "defaultHelpCarriers",
                    id: "defaultHelpCarriers",
                    multiSelect: true,
                    displayField: 'username',
                    valueField: 'id',
                    xtype: "combo",
                    anyMatch: true
                },
                {
                    id: "karAttrBtn",
                    inputId: "karAttrBtn",
                    name: "karAttrBtn",
                    xtype: 'button',
                    layout:{
                        anchor: 100
                    },
                    text: 'Standaard KAR attributen voor nieuw verkeerssysteem',
                    handler: function() {
                       //showDefaultAttributes();
                        Ext.create(KarAttributesEditWindow,
                            "Standaard KAR attributen voor nieuw verkeersysteem",
                            "In dit scherm kan worden aangegeven welke KAR attributen standaard " +
                                "voor nieuw aangemaakte verkeerssystemen moeten worden aangevinkt.",
                            profile.defaultKarAttributes,
                            function(atts) {
                                profile.defaultKarAttributes = atts;
                            }
                        ).show();
                    }
                }],
                buttons: [{
                    text: 'Opslaan',
                    handler: okFunction
                },{
                    text: 'Annuleren',
                    handler: function() {
                        me.window.destroy();
                        me.window = null;
                    }
                }]
            }
        }).show();

        var combo = Ext.getCmp("defaultCarriers");
        var combo2 = Ext.getCmp("defaultHelpCarriers");
        combo.setLoading(true);
        combo2.setLoading(true);
        Ext.Ajax.request({
            url: editorActionBeanUrl,
            method: 'POST',
            scope: this,
            params: {
                'listCarriers': true
            },
            success: function (response) {
                var combo = Ext.getCmp("defaultCarriers");
                var combo2 = Ext.getCmp("defaultHelpCarriers");
                combo.setLoading(false);
                combo2.setLoading(false);
                var msg = Ext.JSON.decode(response.responseText);
                if (msg.success) {
                    var me = this;
                    var store = combo.getStore();
                    store.add(msg.carriers);

                    combo.setValue(profile.defaultCarriers);
                    combo2.setValue(profile.defaultHelpCarriers);
                } else {
                    Ext.Msg.alert('Fout', 'Er is een fout opgetreden. Vervoerders kunnen niet opgehaald worden. Probeer het opnieuw of neem contact op met de applicatie beheerder.' + msg.error);
                }
            },
            failure: function (response) {
                this.setLoading(false);

                var combo = Ext.getCmp("defaultCarriers");
                var combo2 = Ext.getCmp("defaultHelpCarriers");
                combo.setLoading(false);
                combo2.setLoading(false);
                Ext.Msg.alert('Fout', 'Er is een fout opgetreden. Vervoerders kunnen niet opgehaald worden. Probeer het opnieuw of neem contact op met de applicatie beheerder.');
            }
        });

    }
});
function showDefaultAttributes(){
   settingsForm.show();
}