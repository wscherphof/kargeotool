function createForms (){
    Ext.create('Ext.tab.Panel',{
        width : 400,
        height : 400,
        renderTo : 'body',
        items : [{
                title : 'INCAA .ptx',
                items : [
                    {
                        id : "incaaForm",
                        xtype : "form",
                        bodyPadding : 5,
                        // The form will submit an AJAX request to this URL when submitted
                        url : importActionBeanUrl + '?importPtx',
                        standardSubmit : true,
                        // Fields will be arranged vertically, stretched to full width
                        layout : 'anchor',
                        defaults : {
                            anchor : '100%',
                            labelWidth : '120px'
                        },
                        // The fields
                        defaultType : 'textfield',
                        items : [
                            {
                                xtype : "filefield",
                                fieldLabel : 'INCAA .ptx bestand',
                                name : 'bestand',
                                allowBlank : false,
                                msgTarget : 'side',
                                anchor : '100%',
                                buttonText : 'Bladeren',
                                id : 'bestand'
                            }
                        ],
                        // Reset and Submit buttons
                        buttons : [{
                                text : 'Reset',
                                handler : function (){
                                    this.up('form').getForm().reset();
                                }
                            },{
                                text : 'Importeer',
                                formBind : true,//only enabled once the form is valid
                                disabled : true,
                                handler : function (){
                                    var form = this.up('form').getForm();
                                    form.submit();

                                }
                            }
                        ]
                    }
                ]
            },{
                title : 'BISON KV9 .xml',
                id: 'kv9tab',
                items : [
                    {
                        id : "kv9Form",
                        xtype : "form",
                        bodyPadding : 5,
                        // The form will submit an AJAX request to this URL when submitted
                        url : importActionBeanUrl + '?importXml',
                        standardSubmit : true,
                        // Fields will be arranged vertically, stretched to full width
                        layout : 'anchor',
                        defaults : {
                            anchor : '100%',
                            labelWidth : '120px'
                        },
                        // The fields
                        defaultType : 'textfield',
                        items : [
                            {
                                xtype : "filefield",
                                fieldLabel : 'KV9 xml-bestand',
                                name : 'bestand',
                                allowBlank : false,
                                msgTarget : 'side',
                                anchor : '100%',
                                buttonText : 'Bladeren',
                                id : 'bestands'
                            }
                        ],
                        // Reset and Submit buttons
                        buttons : [{
                                text : 'Reset',
                                handler : function (){
                                    this.up('form').getForm().reset();
                                }
                            },{
                                text : 'Importeer',
                                formBind : true,//only enabled once the form is valid
                                disabled : true,
                                handler : function (){
                                    var form = this.up('form').getForm();
                                    form.submit();

                                }
                            }
                        ]
                    }
                ]
            }]
    });
}
Ext.onReady(function (){
    createForms();
    loadGrid();
});


var store = null;
var grid = null;

function loadGrid (){
    if(imported.length == 0) {
        return;
    }
    store = Ext.create('Ext.data.Store',{
        storeId : 'rseqStore',
        fields : ['id','description','karAddress','town','type','validFrom','pointCount','movementCount'],
        data : {
            'items' : imported
        },
        proxy : {
            type : 'memory',
            reader : {
                type : 'json',
                root : 'items'
            }
        }
    });
    grid = Ext.create(Ext.grid.Panel,{
        title : 'Geimporteerde verkeerssystemen',
        xtype : "grid",
        id : "grid",
        store : store,
        columns : [
            {
                text : 'KAR Adres',
                dataIndex : 'karAddress'
            },
            {
                text : 'Omschrijving',
                dataIndex : 'description',
                flex: 1
            },
            {
                text : 'Plaats',
                dataIndex : 'town'
            },
            {
                text : 'Punten / Bewegingen',
                xtype: 'templatecolumn',
                tpl: '{pointCount} / {movementCount}'
            }            
        ],
        height : 300,
        width : 700,
        renderTo : 'importedDiv',
        listeners : {
            select : {
                scope : this,
                fn : rowSelected
            }

        }
    });
}
