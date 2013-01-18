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
                id: 'edit',
                text: 'Bewerkt punt'
            },{
                xtype: 'menuseparator'
            },
            {
                id: 'addCheckoutPoint',
                text: 'Voeg uitmeldpunt toe'
            },
            {
                id: 'addEndPoint',
                text: 'Voeg eindpunt toe'
            },
            {
                id: 'addCheckinPoint',
                text: 'Voeg inmeldpunt toe'
            },{
                xtype: 'menuseparator'
            },
            {
                id: 'showPath',
                text: 'Laat pad zien',
                xtype: 'menucheckitem',
                disabled:true
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
                            editor.addObject("Point", {
                                type: "Point",
                                coordinates: [lonlat.lon, lonlat.lat]
                            },{type:"END",id: Ext.id()});
                            break;
                        case 'addCheckinPoint':
                             editor.addObject("Point", {
                                type: "Point",
                                coordinates: [lonlat.lon, lonlat.lat]
                            },{type:"ACTIVATION_1",id: Ext.id()});
                            break;
                        case 'addCheckoutPoint':
                             editor.addObject("Point", {
                                type: "Point",
                                coordinates: [lonlat.lon, lonlat.lat]
                            },{type:"ACTIVATION_2",id: Ext.id()});
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
            "Point" : vri,
            "ACTIVATION_2" : vri,
            "ACTIVATION_3" : vri,
            "END" : vri,
            "BEGIN" : vri,
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