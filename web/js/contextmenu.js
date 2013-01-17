function ContextMenu (){
    this.menuContext = null;
    this.createMenus = function(){
        var me = this;
        var standaard = Ext.create ("Ext.menu.Menu",{
            floating: true,
            renderTo: Ext.getBody(),
            items: [
            {
                id: 'addVRI',
                text: 'Voeg wallapparaat toe'
            }
            ],
            listeners: {
                click: function(menu,item,e, opts) {
                    var pos = {
                        x: menu.x,
                        y: menu.y
                    }
                    var lonlat = this.map.getLonLatFromPixel(pos);
                    switch (item.id) {
                        case 'addVRI':
                            alert("VRI toevoegen op " + lonlat.lon + ", " + lonlat.lat);
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
                id: 'addSignalGroup',
                text: 'Voeg signaalgroep toe'
            }
            ],
            listeners: {
                click: function(menu,item,e, opts) {
                    var pos = {
                        x: menu.x,
                        y: menu.y
                    }
                    var lonlat = this.map.getLonLatFromPixel(pos);
                    switch (item.id) {
                        case 'addSignalGroup':
                            alert("Signaalgroep toevoegen op " + lonlat.lon + ", " + lonlat.lat)
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
                        x: menu.x,
                        y: menu.y
                    }
                    var lonlat = this.map.getLonLatFromPixel(pos);
                    switch (item.id) {
                        case 'addEndPoint':
                            alert("Eindpunt toevoegen op " + lonlat.lon + ", " + lonlat.lat)
                            break;
                        case 'addCheckinPoint':
                            alert("Inmeldpunt toevoegen op " + lonlat.lon + ", " + lonlat.lat)
                            break;
                        case 'addCheckoutPoint':
                            alert("Uitmeldpunt toevoegen op " + lonlat.lon + ", " + lonlat.lat)
                            break;
                    }
                },
                scope:me
            }
        });
        
        this.menuContext ={
            "standaard" : standaard,
            "ACTIVATION_1" : signalGroup,
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
    this.getMenuContext = function (){
        if(oc.activeFeature){
            var type = oc.activeFeature.data.type;
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
    this.deactivateContextMenu = function(){
        for (var key in this.menuContext){
            var mc = this.menuContext[key];
            mc.hide();
        }
        
    }
}