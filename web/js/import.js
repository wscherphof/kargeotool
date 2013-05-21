function createForms (){
    Ext.create('Ext.tab.Panel',{
        width : 400,
        height : 400,
        activeItem: 'kv9tab',
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
});
