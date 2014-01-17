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
                        url : importActionBeanUrl + '?validateImportXml&debug=true',
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
                                text : 'Importeren...',
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


var store = null;
var grid = null;

function loadGrid() {
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
        renderTo : 'importedDiv'
    });
}

function rseqCheckboxClick(event, row, column) {
    if(!event) {
        event = window.event;
    }
    console.log("rseqCheckboxClick", event, row, column);
}

function createValidationResultsGrid() {
    
    var panel = Ext.create('Ext.panel.Panel', {
        width: '100%',
        height: '90%',
        border: false,
        padding: 2,
        layout: {type: 'vbox', align: 'left'},
        suspendLayout: true,
        items: [{
            width: '100%',
            xtype: 'panel',
            padding: '3 0 3 0',
            border: false,
            html: 'Het XML bestand is gecontroleerd op de eisen uit het KV9. Klik op een verkeerssysteem om de details te bekijken en selecteer verkeerssystemen om te importeren.'
        },{
            xtype: 'button',
            margin: '5 0 10 0',
            text: 'Geselecteerde verkeerssystemen importeren',
            handler: function() {
                alert('okidoki');
            }
        }],
        renderTo: 'body'
    });
    
    store = Ext.create('Ext.data.Store',{
        storeId : 'rseqStore',
        fields : ['checked','position','karAddress','description','fatal','errorCount'],
        data : {
            'items' : allRseqErrors
        },
        proxy : {
            type : 'memory',
            reader : {
                type : 'json',
                root : 'items'
            }
        }
    });
        
    var checkboxRenderer = function(p1,p2,record,row,column,store,grid) {
        return Ext.String.format("<input type='checkbox' {0} {3} onclick='rseqCheckboxClick(event,{1},{2})'></input>",
            record.get("checked") ?  "checked='checked'" : "",
            row,
            column,
            record.get("fatal") ? "disabled='disabled'" : ""
        );
    };   
    
    var detailsPanel = Ext.create('Ext.panel.Panel', {
        width: '100%',
        flex: 1,
        autoScroll: true,
        border: false,
        //margin: 3,
        padding: 3
    });
    
    grid = Ext.create(Ext.grid.Panel,{
        title : 'Validatieresultaten XML bestand',
        xtype : "grid",
        id : "grid",
        store : store,
        columns : [
            {
                text : 'Importeren?',
                dataIndex : 'checked',
                renderer: checkboxRenderer
            },
            {
                text : 'Positie in bestand',
                dataIndex : 'position'
            },
            {
                text : 'KAR adres',
                dataIndex : 'karAddress'
            },
            {
                text : 'Omschrijving',
                dataIndex : 'description',
                flex: 1
            },
            {
                text : 'Fatale controlefout',
                dataIndex : 'fatal',
                xtype: 'booleancolumn',
                trueText: 'Ja',
                falseText: 'Nee'
            },
            {
                text : 'Aantal meldingen',
                dataIndex: 'errorCount'
            }            
        ],
        width: '100%',
        height: 300, 
        listeners: {
            itemclick: {
                fn: function(grid, record, item, index, e, eOpts) { 
                    
                    var rseqDetails = allRseqErrors[index];
                    
                    if(rseqDetails.errorCount == 0) {
                        detailsPanel.update("Geen meldingen over dit verkeerssysteem");
                    } else {
                        detailsPanel.update("Aantal meldingen: " + rseqDetails.errorCount);
                        
                        var t = new Ext.Template([
                            "<div class=\"kv9error\"><table>",
                                "<tr><td class=\"wnb\">Code:</td><td class=\"code\">{code}</td></tr>",
                                "<tr><td class=\"wnb\">Fatale controlefout:</td><td class=\"fatal\">{fatal}</td></tr>",
                                "<tr><td class=\"wnb\">Context:</td><td class=\"context\">{context}</td></tr>",
                                "<tr><td class=\"wnb\">Waarde:</td><td class=\"value\">{value}</td></tr>",
                                "<tr><td class=\"wnb\">Melding:</td><td class=\"message\">{message}</td></tr>",
                                "<tr><td class=\"wnb\">Context in XML:</td><td class=\"xmlcontext\">{xmlContext}</td></tr>",
                            "</table></div>"]).compile();
                        
                        Ext.Array.each(rseqDetails.errors, function(error) {
                            t.append(detailsPanel.body, error);                        
                        });
                        
                    }
                    panel.doLayout();                    
                }
            }
        }
    });
    
    panel.add(grid);
    panel.add(detailsPanel);
    
    panel.suspendLayout = false;
    panel.doLayout();
}
