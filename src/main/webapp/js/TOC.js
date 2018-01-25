/* global Ext, profile, editor, ovInfo */

/**
 * Geo-OV - applicatie voor het registreren van KAR meldpunten
 *
 * Copyright (C) 2018 B3Partners B.V.
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

Ext.define("TOC", {
    config: {
        editor: null,
        domId: null
    },
    constructor: function (config) {
        this.initConfig(config);
        this.createWindow();
        var me = this;
        setTimeout(function () {
            me.restoreProfile();
        }, 100);
    },
    createWindow: function () {
        Ext.create('CollapsibleWindow', {
            title: 'Filters en kaartlagen',
            y: 710,
            layout: 'fit',
            id: 'tocWindow',
            items: [
                {
                    xtype: "container",
                    items: [
                        {
                            xtype: "label",
                            text: "Filters",
                            style: {
                                fontWeight: "bold"
                            }
                        },
                        {
                            xtype: "checkbox",
                            boxLabel: "KV9 validatie OK",
                            id: "kv9valid",
                            listeners: [
                                {
                                    scope: this,
                                    change: function (obj, newValue) {
                                        this.setFilter(newValue, 'kv9', 'valid');
                                    }
                                }
                            ]
                        },
                        {
                            xtype: "checkbox",
                            boxLabel: "KV9 validatie niet OK",
                            id: "kv9invalid",
                            listeners: [
                                {
                                    scope: this,
                                    change: function (obj, newValue) {
                                        this.setFilter(newValue, 'kv9', 'invalid');
                                    }
                                }
                            ]
                        },
                        {
                            xtype: "radio",
                            boxLabel: "OV",
                            value: false,
                            inputValue: "OV",
                            name: "vehicleType",
                            id: "layerOV",
                            listeners: [
                                {
                                    scope: this,
                                    change: function (obj, newValue) {
                                        this.changeVehicleType('OV');
                                    }
                                }
                            ]
                        },
                        {
                            xtype: "radio",
                            name: "vehicleType",
                            boxLabel: "Hulpdiensten",
                            inputValue: "Hulpdiensten",
                            value: false,
                            id: 'layerHulpdiensten',
                            listeners: [
                                {
                                    scope: this,
                                    change: function () {
                                        this.changeVehicleType('Hulpdiensten');
                                    }
                                }
                            ]
                        },
                        {
                            xtype: "checkbox",
                            boxLabel: "Laat ander voertuigtype zien",
                            id: "showOtherVehicleType",
                            listeners: [
                                {
                                    scope: this,
                                    change: function (obj, newValue) {
                                        editor.fireEvent("otherVehicleTypeChanged");
                                    }
                                }
                            ]
                        },
                        {
                            xtype: "label",
                            text: "OV-Informatie",
                            style: {
                                fontWeight: "bold"
                            }
                        },
                        {
                            xtype: "checkbox",
                            boxLabel: "Buslijnen",
                            id: "buslijnen_visible",
                            listeners: [
                                {
                                    scope: this,
                                    change: function (obj, newValue) {
                                        this.toggleOvInfoLayer("buslijnen", newValue);
                                    }
                                }
                            ]
                        },
                        {
                            xtype: "button",
                            text: "Stop filter",
                            id:"buslijnen_filter",
                            hidden:true,
                            listeners:{
                                scope:this,
                                click: function(){
                                    this.removeFilter();
                                    Ext.getCmp("buslijnen_filter").setHidden(true);
                                }
                            }
                        },
                        {
                            xtype: "checkbox",
                            boxLabel: "Bushaltes",
                            id: "bushaltes_visible",
                            listeners: [
                                {
                                    scope: this,
                                    change: function (obj, newValue) {
                                        this.toggleOvInfoLayer("bushaltes", newValue);
                                    }
                                }
                            ]
                        },
                        {
                            xtype: "label",
                            text: "Grenzen",
                            style: {
                                fontWeight: "bold"
                            }
                        },
                        {
                            xtype: "checkbox",
                            boxLabel: "Gemeentes",
                            id: "gemeentes_visible",
                            listeners: [
                                {
                                    scope: this,
                                    change: function (obj, newValue) {
                                        this.toggleBorderLayer("gemeentes", newValue);
                                    }
                                }
                            ]
                        },
                        {
                            xtype: "checkbox",
                            boxLabel: "Provincies",
                            id: "provincies_visible",
                            listeners: [
                                {
                                    scope: this,
                                    change: function (obj, newValue) {
                                        this.toggleBorderLayer("provincies", newValue);
                                    }
                                }
                            ]
                        },
                        {
                            xtype: "label",
                            text: "Achtergrond",
                            style: {
                                fontWeight: "bold"
                            }
                        },
                        {
                            xtype: "checkbox",
                            boxLabel: "Luchtfoto",
                            id: "Luchtfoto_visible",
                            listeners: {
                                scope: this,
                                change: function (obj, value) {
                                    this.toggleLayer("Luchtfoto", value);
                                }
                            }
                        },
                        {
                            xtype: "slider",
                            value: 10,
                            id: 'Luchtfoto_slider',
                            increment: 5,
                            width: 200,
                            minValue: 0,
                            maxValue: 100,
                            listeners: {
                                change: this.sliderChanged
                            }
                        },
                        {
                            xtype: "checkbox",
                            boxLabel: "Topografie (OSM)",
                            id: "Openbasiskaart_visible",
                            listeners: {
                                scope: this,
                                change: function (obj, value) {
                                    this.toggleLayer("Openbasiskaart", value);
                                }
                            }


                        },
                        {
                            id: 'Openbasiskaart_slider',
                            xtype: "slider",
                            increment: 5,
                            value: 10,
                            width: 200,
                            minValue: 0,
                            maxValue: 100,
                            listeners: {
                                change: this.sliderChanged
                            }
                        }, {
                            xtype: "label",
                            text: "Extra",
                            style: {
                                fontWeight: "bold"
                            }
                        },
                        {
                            xtype: "checkbox",
                            id: "snapRoads",
                            boxLabel: "Snap op wegen",
                            listeners: {
                                change: this.toggleRoad
                            }
                        }
                    ]
                }
            ]
        });
    },

    restoreProfile: function () {
        var checkboxes = ['buslijnen', 'bushaltes', 'Luchtfoto', 'Openbasiskaart', 'gemeentes', 'provincies'];
        Ext.Array.each(checkboxes, function (checkbox) {
            Ext.getCmp(checkbox + "_visible").setValue(editor.getLayerVisibility(checkbox));
        });

        Ext.getCmp("kv9valid").setValue(profile.state["kv9valid"] !== undefined ? profile.state["kv9valid"] : true);
        Ext.getCmp("kv9invalid").setValue(profile.state["kv9invalid"] !== undefined ? profile.state["kv9invalid"] : true);

        Ext.getCmp("layerOV").setValue(profile.state["layerOV"] !== undefined ? profile.state["layerOV"] : true);
        Ext.getCmp("layerHulpdiensten").setValue(profile.state["layerHulpdiensten"] !== undefined ? profile.state["layerHulpdiensten"] : true);
        
        Ext.getCmp("Openbasiskaart_slider").setValue(editor.getLayerOpacity("Openbasiskaart") * 100);
        Ext.getCmp("Luchtfoto_slider").setValue(editor.getLayerOpacity("Luchtfoto") * 100);
    },
    setFilter: function (checked, prefix, type) {
        editor.updateFilteredRseqs();
        profile.state[prefix + type] = checked;
        saveProfile();
    },
    changeVehicleType: function (vehicleType){
        editor.fireEvent("vehicleTypeChanged", vehicleType);
    },
    toggleOvInfoLayer: function (layer, visible) {
        Ext.Array.each(ovInfo, function (ov) {
            editor.olc.setLayerVisible(layer + "_" + ov.schema, visible);
        });
        profile.state[layer + "_visible"] = visible;
        saveProfile();
    },
    toggleBorderLayer:function(layer, visible){
        profile.state[layer + "_visible"] = visible;
        if(layer === "provincies"){
            editor.olc.setLayerVisible('provincies_border', visible);
            editor.olc.setLayerVisible('provincies_hasrseq', visible);
            editor.olc.setLayerVisible('provincies_hasnorseq', visible);
        }else{
            editor.olc.setLayerVisible('gemeentes_hasrseq', visible);
            editor.olc.setLayerVisible('gemeentes_hasnorseq', visible);
        }
        saveProfile();
    },
    sliderChanged: function (slider, newValue) {

        profile.state[slider.id] = newValue;
        saveProfile();
        editor.setLayerOpacity(slider.id.substr(0, slider.id.indexOf('_')), newValue / 100.0);
    },
    toggleLayer: function (layer, visible) {
        editor.olc.setLayerVisible(layer, visible);
        profile.state[layer + "_visible"] = visible;
        saveProfile();
    },
    toggleRoad: function (obj, activate) {
        if (activate) {
            editor.loadRoads();
        } else {
            editor.removeRoads();
        }
    },
    removeFilter: function(){
        Ext.Array.each(ovInfo, function (ov) {
            var currentName = "buslijnen_" + ov.schema;
            // Prevent old image from showing
            editor.olc.setLayerVisible(currentName, false);
            editor.olc.removeFilterFromKargis(currentName);
            editor.olc.setLayerVisible(currentName, true);
        });
        Ext.getCmp("buslijnen_filter").setHidden(true);
    }

});