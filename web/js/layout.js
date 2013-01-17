Ext.onReady(function() {
    var viewport = Ext.create('Ext.Viewport', {
        layout: 'border',
        defaultType: 'panel',
        defaults: {
            header: false,
            border: false
        },
        items: [
            {
                region: 'north',
                height: 30,
                contentEl: 'header'
            }, {
                region: 'east',
                animCollapse: true,
                collapsible: true,
                split: true,
                width: 175,
                minWidth: 0,
                maxWidth: 200,
                contentEl: 'rightbar'
            }, {
                region: 'west',
                animCollapse: true,
                collapsible: true,
                split: true,
                width: 375,
                minWidth: 0,
                maxWidth: 500,
                contentEl: 'leftbar',
                layout: 'vbox',
                defaultType: 'container',
                items: [
                    {
                        contentEl: 'tree',
                        flex: 1,
                        width: '100%',
                        layout: 'vbox',
                        defaultType: 'container',
                        items: [
                            {
                                contentEl: 'treeTop',
                                height: 50,
                                width: '100%'
                            },
                            {
                                contentEl: 'objectTree',
                                flex: 1,
                                width: '100%'
                            },
                            {
                                contentEl: 'options',
                                height: 50,
                                width: '100%'
                            }
                        ]
                    },
                    {
                        contentEl: 'form',
                        height: 426,
                        width: '100%'
                    }
                ]
            },
            {
                region: 'center',
                contentEl: 'kaart'
            }
        ]
    });
});