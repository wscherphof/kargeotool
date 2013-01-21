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
                            },{
                                type:"CROSSING",
                                id: Ext.id()
                            });
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
                            editor.addEndpoint();
                            break;
                        case 'addCheckinPoint':
                            editor.addCheckinPoint();
                            break;
                        case 'addCheckoutPoint':
                            editor.addCheckoutPoint();
                            break;
                        case 'editRseq':
                            this.editor.editSelectedObject();
                            break;
                    }
                },
                scope:me
            }
        });
        
        this.menuContext ={
            "standaard" : standaard,
            "ACTIVATION_1" : vri,
            "ACTIVATION_2" : vri,
            "ACTIVATION_3" : vri,
            "END" : vri,
            "BEGIN" : vri,
            "CROSSING" : vri
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