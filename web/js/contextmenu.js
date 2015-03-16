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
 * Context menu class dat alle rechtermuis clicks afhandelt.
 */
Ext.define("ContextMenu", {
    menuContext: null,
    editor: null,
    
    // menu's 
    rseq:null,
    uitmeldpunt:null,
    inmeldpunt:null,
    nonActivationPoint: null,
    
    defaultMenu:null,
    
    point_with_line:null,
    /**
     * 
     * @param editor A reference to the editor.
     *@constructor
     */
    constructor: function(editor) {
        this.editor = editor;
        this.editor.on("selectedObjectChanged", this.updateStates,this);
    },
    /**
     * Maak de menu's aan.
     */
    createMenus: function() {
        var me = this;
        
        // Context menu voor een klik in de kaart indien geen rseq actief is
        
        this.defaultMenu = Ext.create ("Ext.menu.Menu",{
            floating: true,
            renderTo: Ext.getBody(),
            items: [
            {
                id: 'addRseq',
                text: 'Hier verkeerssysteem toevoegen',
                icon: karTheme.crossing
            },
            {
                id: 'cancelSelecting',
                text: 'Annuleer selecteren bestaand punt',
                icon: karTheme.cursor_delete,
                hidden: true
            }
            ],
            listeners: {
                click: function(menu,item,e, opts) {
                    
                    // Gebruik huidige positie om om die plek wat te doen
                    var pos = {
                        x: menu.x - Ext.get(this.editor.domId).getX(),
                        y: menu.y
                    };
                    var lonlat = this.editor.olc.map.getLonLatFromPixel(pos);
                    
                    switch (item.id) {
                        case 'cancelSelecting':
                            this.editor.cancelSelection();
                            break;                            
                        case 'addRseq':
                            // Voeg op huidige positie nieuwe Rseq toe 
                            this.editor.addRseq(lonlat.lon, lonlat.lat);
                            break;                            
                    }
                },
                scope:me
            }
        });
                
        
        this.point_with_line = Ext.create ("Ext.menu.Menu",{
            floating: true,
            renderTo: Ext.getBody(),
            items: [
            {
                id: 'resetMeasure',
                text: 'Meten vanaf vorig punt',
                icon: contextPath + "/images/silk/ruler.png"
            }
            ],
            listeners: {
                click: function(menu,item,e, opts) {
                    switch (item.id) {
                        case 'resetMeasure':
                            this.editor.resetMeasure();
                            break;
                    }
                },
                scope:me
            }
        });
        
        // Context menu voor rseq
        
        this.rseq = Ext.create ("Ext.menu.Menu",{
            floating: true,
            renderTo: Ext.getBody(),
            items: [
            {
                id: 'editRseq',
                text: 'Bewerken...',
                icon: contextPath + "/images/silk/table_edit.png"
            },
            {
                id: 'saveRseq',
                text: 'Opslaan',
                icon: contextPath + "/images/silk/table_save.png"
            },
            
            {
                id: 'exportMenu',
                text: 'Exporteer',
                menu: {
                    items:[
                        {
                            id: 'exportXml',
                            text: 'Exporteer KV9 XML',
                            icon: contextPath + "/images/silk/transmit_go.png"
                        },
                        {
                            id: 'exportPtx',
                            text: 'Exporteer Incaa PTX',
                            icon: contextPath + "/images/silk/transmit_go.png"
                        }
                    ],
                    listeners: {
                        click:
                        function(menu,item,e, opts) {
                            switch (item.id) {
                                case 'exportXml':
                                    this.editor.exportXml();
                                    break;
                                case 'exportPtx':
                                    this.editor.exportPtx();
                                    break;
                            }
                        },
                        scope:me
                    }
                }
            },            
            {
                id: 'setCoordsRseq',
                text: 'Voer coördinaten in',
                icon: contextPath + "/images/icons/gps.png"
            },
            {
                id: 'removeRseq',
                text: 'Verwijderen',
                icon: contextPath + "/images/silk/table_delete.png"
            },
            {
                id: "addMemo",
                text: "Memo...",
                icon: contextPath + "/images/silk/attach.png"
            },
            {
                id: "showMessages",
                text: "Export informatie",
                icon: contextPath + "/images/silk/email_open.png"
            },{
                id: "uppervri",
                xtype: 'menuseparator'
            },
            {
                id: 'addUitmeldpunt',
                text: 'Voeg uitmeldpunt toe',
                icon: karTheme.uitmeldPunt
            }
            ],
            listeners: {
                click: function(menu,item,e, opts) {
                    // Huidige positie van muis wordt niet gebruikt voor dit context menu
                    
                    switch (item.id) {
                        case 'addUitmeldpunt':
                            this.editor.addUitmeldpunt();
                            break;
                        case 'editRseq':
                            this.editor.editSelectedObject();
                            break;
                        case 'removeRseq':
                            this.editor.removeRseq();
                            break;
                        case 'addMemo':
                            this.editor.addMemo();
                            break;
                        case 'saveRseq':
                            this.editor.saveOrUpdate();
                            break;
                        case 'setCoordsRseq':
                            this.editor.editForms.editCoordinates(this.editor.selectedObject);
                            break
                        case 'showMessages':
                            Ext.create("MessagesOverview").show(this.editor.selectedObject.id);
                            break;
                    }
                },
                scope: me
            }
        });
        
        // Context menu voor een uitmeldpunt
        
        this.uitmeldpunt = Ext.create ("Ext.menu.Menu",{
            floating: true,
            renderTo: Ext.getBody(),
            items: [
            {
                id: 'editUitmeldpunt',
                text: 'Bewerken...',
                icon: contextPath + "/images/silk/table_edit.png"
            },
            {
                id: 'removeUitmeldpunt',
                text: 'Verwijderen',
                icon: contextPath + "/images/silk/table_delete.png"
            },
            {
                id: 'setCoordsUitmeld',
                text: 'Voer coördinaten in',
                icon: contextPath + "/images/icons/gps.png"
            },{
                id: "uppercheckout",
                xtype: 'menuseparator'
            },
            {
                id: 'addEindpunt',
                text: 'Voeg nieuw eindpunt toe',
                icon: karTheme.eindPunt
            },
            {
                id: 'selectEindpunt',
                text: 'Selecteer bestaand eindpunt',
                icon: contextPath + "/images/silk/cursor.png"
            },
            {
                id: 'addInmeldpunt',
                text: 'Voeg nieuw inmeldpunt toe',
                disabled:true,
                icon: karTheme.inmeldPunt
            },
            {
                id: 'selectInmeldpunt',
                text: 'Selecteer bestaand inmeldpunt',
                disabled:true,
                icon: contextPath + "/images/silk/cursor.png"
            },
            {
                id : "lowercheckout",
                xtype: 'menuseparator'
            },
            {
                id: 'advancedCheckout',
                text: 'Geavanceerd',
                menu: {
                    items:[
                    {
                        id: 'addBeginpunt',
                        text: 'Voeg beginpunt toe',
                        icon: karTheme.startPunt
                    }
                    ],
                    listeners: {
                        click:
                        function(menu,item,e, opts) {
                            switch (item.id) {
                                case 'addBeginpunt':
                                    this.editor.addBeginpunt();
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
                        case 'editUitmeldpunt':
                            this.editor.editSelectedObject();
                            break;                        
                        case 'removeUitmeldpunt':
                            this.editor.removeCheckoutPoint();
                            break;                            
                        case 'addEindpunt':
                            editor.addEindpunt();
                            break;
                        case 'selectEindpunt':
                            editor.selectEindpunt();
                            break;
                        case 'addInmeldpunt':
                            editor.addInmeldpunt();
                            break;
                        case 'selectInmeldpunt':
                            editor.selectInmeldpunt();
                            break;              
                        case 'setCoordsUitmeld':
                            this.editor.editForms.editCoordinates(this.editor.selectedObject);
                            break              
                    }
                },
                scope:me
            }
        });
        
        // Context menu voor een inmeldpunt
        
        this.inmeldpunt = Ext.create ("Ext.menu.Menu",{
            floating: true,
            renderTo: Ext.getBody(),
            items: [
            {
                id: 'editInmeldpunt',
                text: 'Bewerken...',
                icon: contextPath + "/images/silk/table_edit.png"
            },
            {
                id: 'removeInmeldpunt',
                text: 'Verwijderen...',
                icon: contextPath + "/images/silk/table_delete.png"
            },
            {
                id: 'setCoordsInmeld',
                text: 'Voer coördinaten in',
                icon: contextPath + "/images/icons/gps.png"
            },{
                id: "upperIncheckin",
                xtype: 'menuseparator'
            },
            {
                id: 'addVoorinmeldpunt',
                text: 'Voeg nieuw voorinmeldpunt toe',
                icon: karTheme.voorinmeldPunt
            },/*
            {
                id: 'voegBeginpuntToecheckin',
                text: 'Voeg beginpunt toe',
                icon: karTheme.startPunt
            }*/
            ],
            listeners: {
                click: function(menu,item,e, opts) {
                    var pos = {
                        x: menu.x - Ext.get(editor.domId).getX(),
                        y: menu.y
                    }
                    var lonlat = editor.olc.map.getLonLatFromPixel(pos);
                    switch (item.id) {
                        case 'editInmeldpunt':
                            this.editor.editSelectedObject();
                            break;
                        case 'removeInmeldpunt':
                            this.editor.removeOtherPoint();
                            break;                               
                        case 'addVoorinmeldpunt':
                            editor.addVoorinmeldpunt();
                            break;
                        case 'setCoordsInmeld':
                            this.editor.editForms.editCoordinates(this.editor.selectedObject);
                            break
                    }
                },
                scope:me
            }
        });
        
        // Context menu voor punten die alleen bewerkt kunnen worden (begin, eind, voorinmeldpunt)
        
        this.nonActivationPoint = Ext.create ("Ext.menu.Menu",{
            floating: true,
            renderTo: Ext.getBody(),
            items: [
            {
                id: 'editNAPoint',
                text: 'Bewerken...',
                icon: contextPath + "/images/silk/table_edit.png"
            },{
                id: 'removeNAPoint',
                text: 'Verwijderen',
                icon: contextPath + "/images/silk/table_delete.png"
            },{
                id: 'setCoordsNon',
                text: 'Voer coördinaten in',
                icon: contextPath + "/images/icons/gps.png"
            }
            ],
            listeners: {
                click: function(menu,item,e, opts) {
                    switch (item.id) {
                        case 'editNAPoint':
                            editor.editSelectedObject();
                            break;
                        case 'removeNAPoint':
                            this.editor.removeOtherPoint();
                            break;
                        case 'setCoordsNon':
                            this.editor.editForms.editCoordinates(this.editor.selectedObject);
                            break
                    }
                },
                scope:me
            }
        });
        
        this.menuContext ={
            "standaard" : this.defaultMenu,
            "ACTIVATION_1" : this.inmeldpunt,
            "ACTIVATION_2" : this.uitmeldpunt ,
            "ACTIVATION_3" : this.inmeldpunt, // zelfde menu ook voor voorinmeldpunt
            
            "END" : this.nonActivationPoint,
            "BEGIN" : this.nonActivationPoint,
            
            "CROSSING" : this.rseq,
            "GUARD" : this.rseq,
            "BAR" : this.rseq,
            
            "ADDPOINT_WITH_LINE" : this.point_with_line
            
        //"onlyEdit" : this.onlyEdit // XXX
        };
        // Get control of the right-click event:
        document.oncontextmenu = function(e){
            e = e?e:window.event;
            
            var f = editor.olc.getFeatureFromEvent(e);
            
            if(f&& !f.cluster){
                var x = e.clientX;
                var y = e.clientY;
                if(f.layer.name== "RseqSelect"){
                    editor.loadRseqInfo({
                        rseq: f.data.id
                    },function(){
                        editor.contextMenu.show(x,y);
                    });
                }else{
                    editor.setSelectedObject(f);
                    editor.contextMenu.show(x,y);
                }
            }
            if (e.preventDefault) 
                e.preventDefault(); // For non-IE browsers.
            else {
                return false; // For IE browsers.
            }
        };
    },
    /**
     * Update de status van het menu bij verandering.
     */
    updateStates: function(selectedObject){
    // TODO update the states according to the selected object
    },
    /**
     * Toon het menu op de gewenste x en y pixel
     * @param x de x pixel
     * @param y de y pixel
     */
    show : function(x,y,forceDefault){
        var context = this.getMenuContext();
        if(forceDefault === true&& context != this.point_with_line){
            context = this.menuContext['standaard'];
        }
        if(context){
            context.showAt(x, y);
        }
    },
    /**
 * Haal het nodige menu op door te kijken wat het type is van het geselecteerde object
 * @return het gewenste menu
 */
    getMenuContext: function() {
        if(editor.selectedObject){
            var type = editor.selectedObject.getType();

            // Update state van disabled / enabled items op basis van selectedObject

            if(type == "ACTIVATION_2") {
                // Voor een uitmeldpunt kan alleen een inmeldpunt worden toegevoegd
                // indien voor dat uitmeldpunt in een movement een eindpunt aanwezig 
                // is
                
                var heeftEindpunt = editor.activeRseq.heeftUitmeldpuntEindpunt(editor.selectedObject);
                Ext.getCmp("addInmeldpunt").setDisabled(!heeftEindpunt);
                Ext.getCmp("selectInmeldpunt").setDisabled(!heeftEindpunt);
            }
            
            if(this.editor.olc.isMeasuring() ){
                type = "ADDPOINT_WITH_LINE";
            }
            var menu = this.menuContext[type];
            if(menu){
                return menu;
            }else{
                alert("Kan geen context menu vinden voor type " + type);
                return this.menuContext["onlyEdit"];
            }
        }else{
            return this.menuContext["standaard"];
        }
    },
    /**
    * Deactiveer het menu (verberg)
    */
    deactivateContextMenu: function() {
        for (var key in this.menuContext){
            var mc = this.menuContext[key];
            if(!mc) {
                alert("mc for key " + key + " is null");
            }
            mc.hide();
        }
    },
    showCancelSelecting: function(action){
        var cancel = this.defaultMenu.items.getByKey("cancelSelecting");
        cancel.show();
        this.editor.actionToCancel = action;
    },
    hideCancelSelecting: function(){
        var cancel = this.defaultMenu.items.getByKey("cancelSelecting");
        cancel.hide();
    }
});