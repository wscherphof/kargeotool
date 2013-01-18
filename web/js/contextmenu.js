
Ext.define("ContextMenu", {
    
    menuContext: null,
    editor: null,
    
    constructor: function(editor) {
        this.editor = editor;
    },
    
    createMenus: function() {
        var me = this;
        var standaard = Ext.create ("Ext.menu.Menu",{
            floating: true,
            renderTo: Ext.getBody(),
            items: [
            {
                id: 'addRseq',
                text: 'Hier verkeerssysteem toevoegen',
                icon: karTheme.vri
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
                            editor.addObject("RSEQ", {
                                type: "Point",
                                coordinates: [lonlat.lon, lonlat.lat]
                            },{type:"CROSSING",id: Ext.id()});
                            break;
                    }
                },
                scope:me
            }
        });
        
        var vri = Ext.create ("Ext.menu.Menu",{
            floating: true,
            renderTo: Ext.getBody(),
            items: [
            {
                id: 'editRseq',
                text: 'Bewerken...',
                icon: contextPath + "/images/silk/table_edit.png"
            },{
                xtype: 'menuseparator'
            },
            {
                id: 'addSignalGroup',
                text: 'Voeg signaalgroep toe'
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
                        case 'addSignalGroup':
                            alert("Signaalgroep toevoegen op " + lonlat.lon + ", " + lonlat.lat);
                            break;
                        case 'editRseq':
                            this.editor.editRseq();
                            break;
                    }
                },
                scope:me
            }
        });
        
        var signalGroup = Ext.create ("Ext.menu.Menu",{
            floating: true,
            renderTo: Ext.getBody(),
            items: [
                
            {
                id: 'edit',
                text: 'Bewerkt punt'
            },{
                xtype: 'menuseparator'
            },
            {
                id: 'addEndPoint',
                text: 'Voeg eindpunt toe'
            },
            {
                id: 'addCheckinPoint',
                text: 'Voeg inmeldpunt toe'
            },
            {
                id: 'addCheckoutPoint',
                text: 'Voeg uitmeldpunt toe'
            },
            {
                id: 'showPath',
                text: 'Laat pad zien',
                xtype: 'menucheckitem'
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
                        case 'addEndPoint':
                            alert("Eindpunt toevoegen op " + lonlat.lon + ", " + lonlat.lat);
                            break;
                        case 'addCheckinPoint':
                            alert("Inmeldpunt toevoegen op " + lonlat.lon + ", " + lonlat.lat);
                            break;
                        case 'addCheckoutPoint':
                            alert("Uitmeldpunt toevoegen op " + lonlat.lon + ", " + lonlat.lat);
                            break;
                        case 'edit':
                            this.openPopup();
                            break;
                    }
                },
                scope:me
            }
        });
        
        this.menuContext ={
            "standaard" : standaard,
            "Point" : signalGroup,
            "ACTIVATION_2" : signalGroup,
            "ACTIVATION_3" : signalGroup,
            "END" : signalGroup,
            "BEGIN" : signalGroup,
            "CROSSING" : vri
        };
        // Get control of the right-click event:
        document.oncontextmenu = function(e){
            e = e?e:window.event;
            if (e.preventDefault) 
                e.preventDefault(); // For non-IE browsers.
            else return false; // For IE browsers.
        };
    },
    getMenuContext: function() {
        if(editor.selectedObject){
            var type = editor.selectedObject.getType();
            var menu = this.menuContext[type];
            if(menu){
                return menu;
            }else{
                return this.menuContext["standaard"];
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