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
Ext.define("ContextMenu", {
    menuContext: null,
    editor: null,
    
    // menu's 
    vri:null,
    checkout:null,
    checkin:null,
    defaultMenu:null,
    
    constructor: function(editor) {
        this.editor = editor;
        this.editor.on("selectedObjectChanged", this.updateStates,this);
    },
    
    createMenus: function() {
        var me = this;
        this.defaultMenu= Ext.create ("Ext.menu.Menu",{
            floating: true,
            renderTo: Ext.getBody(),
            items: [
            {
                id: 'addRseq',
                text: 'Hier verkeerssysteem toevoegen',
                icon: karTheme.crossing
            }
            ],
            listeners: {
                click: function(menu,item,e, opts) {
                    var pos = {
                        x: menu.x - Ext.get(editor.domId).getX(),
                        y: menu.y
                    }
                    var lonlat = editor.olc.map.getLonLatFromPixel(pos);
                    switch (item.id) {
                        case 'addRseq':
                            editor.addRseq(lonlat.lon, lonlat.lat);
                            break;
                    }
                },
                scope:me
            }
        });
        
        
        this.vri = Ext.create ("Ext.menu.Menu",{
            floating: true,
            renderTo: Ext.getBody(),
            items: [
            {
                id: 'editRseqvri',
                text: 'Bewerken...',
                icon: contextPath + "/images/silk/table_edit.png"
            },{
                id: "uppervri",
                xtype: 'menuseparator'
            },
            {
                id: 'addCheckoutPointvri',
                text: 'Voeg uitmeldpunt toe',
                icon: karTheme.uitmeldPunt
            },
            {
                id: 'addCheckinPointvri',
                text: 'Voeg inmeldpunt toe',
                icon: karTheme.inmeldPunt
            },
            {
                id: 'addEndPointvri',
                text: 'Voeg eindpunt toe',
                icon: karTheme.eindPunt
            }
            ],
            listeners: {
                click: function(menu,item,e, opts) {
                    var pos = {
                        x: menu.x - Ext.get(editor.domId).getX(),
                        y: menu.y
                    }
                    var lonlat = editor.olc.map.getLonLatFromPixel(pos);
                    var point= new OpenLayers.Geometry.Point(lonlat.lon,lonlat.lat);
                    switch (item.id) {
                        case 'addEndPointvri':
                            editor.addEndpoint(false,point);
                            break;
                        case 'addCheckinPointvri':
                            editor.addCheckinPoint(false,point);
                            break;
                        case 'addCheckoutPointvri':
                            editor.addCheckoutPoint(false,point);
                            break;
                        case 'editRseqvri':
                            this.editor.editSelectedObject();
                            break;
                    }
                },
                scope:me
            }
        });
        
        this.checkout = Ext.create ("Ext.menu.Menu",{
            floating: true,
            renderTo: Ext.getBody(),
            items: [
            {
                id: 'editCheckoutcheckout',
                text: 'Bewerk...',
                icon: contextPath + "/images/silk/table_edit.png"
            },{
                id: "uppercheckout",
                xtype: 'menuseparator'
            },
            {
                id: 'addEndPointcheckout',
                text: 'Voeg eindpunt toe',
                icon: karTheme.eindPunt
            },
            {
                id: 'selectEndPointcheckout',
                text: 'Selecteer eindpunt',
                icon: contextPath + "/images/silk/cursor.png"
            },
            {
                id: 'addCheckinPointcheckout',
                text: 'Voeg inmeldpunt toe',
                disabled:true,
                icon: karTheme.inmeldPunt
            },{
                id : "lowercheckout",
                xtype: 'menuseparator'
            },
            {
                id: 'showPathcheckout',
                text: 'Laat pad zien',
                xtype: 'menucheckitem',
                disabled:true
            },
            {
                id: 'advancedCheckout',
                text: 'Geavanceerd',
                menu: {
                    items:[
                    {
                        id: 'selectOtherCheckout',
                        text: 'Selecteer uitmeldpunt van andere fasecyclus',
                        icon: contextPath + "/images/silk/cursor.png"
                    },
                    {
                        id: 'voegBeginpuntToecheckout',
                        text: 'Voeg beginpunt toe',
                        icon: karTheme.startPunt
                    }
                    ],
                    listeners: {
                        click:
                        function(menu,item,e, opts) {
                            switch (item.id) {
                                case 'voegBeginpuntToecheckout':
                                    this.editor.addBeginpoint(true);
                                    break;
                            }
                        },
                        scope:me
                    }
                }
            }
            ],
            listeners: {
                click: function(menu,item,e, opts) {
                    var pos = {
                        x: menu.x - Ext.get(editor.domId).getX(),
                        y: menu.y
                    }
                    var lonlat = editor.olc.map.getLonLatFromPixel(pos);
                    switch (item.id) {
                        case 'addEndPointcheckout':
                            editor.addEndpoint(true);
                            Ext.Array.each(menu.items.items,function(name, idx, ori){
                                if(name.id == "addCheckinPoint"){
                                    name.setDisabled (false);
                                }
                            });
                            break;
                        case 'addCheckinPointcheckout':
                            editor.addCheckinPoint(true);
                            break;
                        case 'selectEndPointcheckout':
                            editor.selectEindpunt();
                            break;
                        case 'editCheckoutcheckout':
                            this.editor.editSelectedObject();
                            break;
                    }
                },
                scope:me
            }
        });
        
        this.checkin = Ext.create ("Ext.menu.Menu",{
            floating: true,
            renderTo: Ext.getBody(),
            items: [
            {
                id: 'editCheckincheckin',
                text: 'Bewerk...',
                icon: contextPath + "/images/silk/table_edit.png"
            },{
                id: "upperIncheckin",
                xtype: 'menuseparator'
            },
            {
                id: 'voegVoorinmeldTocheckin',
                text: 'Voeg voorinmeldpunt toe',
                icon: karTheme.voorinmeldPunt
            },
            {
                id: 'voegBeginpuntToecheckin',
                text: 'Voeg beginpunt toe',
                icon: karTheme.startPunt
            }
            ],
            listeners: {
                click: function(menu,item,e, opts) {
                    var pos = {
                        x: menu.x - Ext.get(editor.domId).getX(),
                        y: menu.y
                    }
                    var lonlat = editor.olc.map.getLonLatFromPixel(pos);
                    switch (item.id) {
                        case 'voegVoorinmeldTocheckin':
                            editor.addPreCheckinPoint(true);
                            break;
                        case 'voegBeginpuntToecheckin':
                            editor.addBeginpoint(true);
                            break;
                        case 'editCheckincheckin':
                            this.editor.editSelectedObject();
                            break;
                    }
                },
                scope:me
            }
        });
        
        this.unkown = Ext.create ("Ext.menu.Menu",{
            floating: true,
            renderTo: Ext.getBody(),
            items: [
            {
                id: 'edit',
                text: 'Bewerk...',
                icon: contextPath + "/images/silk/table_edit.png"
            }
            ],
            listeners: {
                click: function(menu,item,e, opts) {
                    switch (item.id) {
                        case 'edit':
                            editor.editSelectedObject();
                            break;
                    }
                },
                scope:me
            }
        });
        
        this.menuContext ={
            "standaard" : this.defaultMenu,
            "ACTIVATION_1" : this.checkin,
            "ACTIVATION_2" : this.checkout ,
            "ACTIVATION_3" : this.checkin,
            
            "END" : this.vri,
            "BEGIN" : this.checkin,
            
            "CROSSING" : this.vri,
            "GUARD" : this.vri,
            "BAR" : this.vri,
            
            "UNKOWN" : this.unkown
        };
        // Get control of the right-click event:
        document.oncontextmenu = function(e){
            e = e?e:window.event;
            
            var f = editor.olc.vectorLayer.getFeatureFromEvent(e);
            if(f){
                editor.setSelectedObject(f);
                var x = e.clientX;
                var y = e.clientY;
                editor.contextMenu.show(x,y);
            }
            if (e.preventDefault) 
                e.preventDefault(); // For non-IE browsers.
            else {
                return false; // For IE browsers.
            }
        };
    },
    updateStates: function(selectedObject){
    // TODO update the states according to the selected object
    },
    show : function(x,y){
        var context = this.getMenuContext();
        if(context){
            context.showAt(x, y);
        }
    },
    getMenuContext: function() {
        if(editor.selectedObject){
            var type = editor.selectedObject.getType();
            var menu = this.menuContext[type];
            if(menu){
                return menu;
            }else{
                return this.menuContext["UNKOWN"];
            }
        }else{
            return this.menuContext["standaard"];
        }
    },
    deactivateContextMenu: function() {
        for (var key in this.menuContext){
            var mc = this.menuContext[key];
            mc.hide();
        }
    },
    openPopup: function() {
        Ext.create('Ext.window.Window', {
            title: 'Hello',
            height: 200,
            width: 400,
            layout: 'fit',
            items: {  // Let's put an empty grid in just to illustrate fit layout
                xtype: 'grid',
                border: false,
                columns: [{
                    header: 'World'
                }],                 // One header just for show. There's no data,
                store: Ext.create('Ext.data.ArrayStore', {}) // A dummy empty data store
            }
        }).show();
    }
});