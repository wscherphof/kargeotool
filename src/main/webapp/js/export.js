/* global Ext, deelgebieden, dataowners, exportActionBeanUrl, deelgebiedActionBeanUrl */

/**
 * KAR Geo Tool - applicatie voor het registreren van KAR meldpunten
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

var store = null;
var grid = null;
var unselectedVRIs = [];
var menuState = true;
Ext.onReady(function (){
    store = Ext.create('Ext.data.Store',{
        storeId : 'rseqStore',
        fields : ['id','naam','dataowner','type','karAddress'],
        data : {
            'items' : []
        },
        proxy : {
            type : 'memory',
            reader : {
                type : 'json',
                rootProperty : 'items'
            }
        }
    });
    var dgStore = Ext.create('Ext.data.Store',{
        fields : ['id','name'],
        data : deelgebieden
    });
    var dataownerStore = Ext.create('Ext.data.Store',{
        fields : ['id', 'code','omschrijving'],
        data : dataowners
    });
    var form = Ext.create(Ext.form.Panel,{
        title : 'Exporteer verkeerssystemen',
        bodyPadding : 5,
        width : 950,
        // The form will submit an AJAX request to this URL when submitted
        url : exportActionBeanUrl + '?export',
        standardSubmit : true,
        // Fields will be arranged vertically, stretched to full width
        layout : 'anchor',
        defaults : {
            anchor : '100%',
            labelWidth : 190
        },
        // The fields
        defaultType : 'textfield',
        items : [
            {
                xtype : "hidden",
                name : "rseqs",
                id : "rseqs"
            },
            {
                xtype : "hidden",
                name : "doAsync",
                id : "doAsync",
                value:true
            },
            {
                xtype : 'radiogroup',
                fieldLabel : 'Filter type',
                // Arrange radio buttons into two columns, distributed vertically
                columns : 1,
                vertical : true,
                id: 'filterType',
                items : [
                    {
                        boxLabel : 'Alle verkeerssystemen',
                        name : 'filterType',
                        inputValue : 'all',
                        checked : true,
                        handler : function (item,checked){
                            if(checked){
                                reloadVRIs();
                            }
                        }
                    },
                    {
                        boxLabel : 'Deelgebied',
                        name : 'filterType',
                        inputValue : 'deelgebied',
                        handler : function (checkbox,checked){
                            var field = Ext.getCmp('deelgebied');
                            field.setDisabled(!checked);
                            if(checked) {
                                field.focus();
                            }
                            var rd = Ext.getCmp('removeDeelgebied');
                            rd.setDisabled(!checked);

                            var ed = Ext.getCmp('editDeelgebied');
                            ed.setDisabled(!checked);

                        }
                    },
                    {
                        boxLabel : 'Beheerder',
                        name : 'filterType',
                        inputValue : 'dataowner',
                        handler : function (checkbox,checked){
                            var field = Ext.getCmp('dataowner');
                            field.setDisabled(!checked);
                            if(checked) {
                                field.focus();
                            }
                        }
                    },
                    {
                        boxLabel : 'Karadres',
                        name : 'filterType',
                        inputValue : 'rseqByKarAddress',
                        handler : function (checkbox,checked){
                            var field = Ext.getCmp('rseqByKarAddress');
                            field.setDisabled(!checked);
                            if(checked) {
                                field.focus();
                            }
                        }
                    }]
            },
            {
                xtype : 'container',
                layout : {
                    type : 'hbox'
                },
                margin: '5 0',
                items : [
                    {
                        labelWidth: 190,
                        id : 'deelgebied',
                        xtype : "combo",
                        fieldLabel : 'Kies een deelgebied',
                        store : dgStore,
                        queryMode : 'local',
                        displayField : 'name',
                        disabled : true,
                        anyMatch: true,
                        valueField : 'id',
                        emptyText : "Selecteer",
                        flex : 1,
                        listeners : {
                            scope : this,
                            select : {
                                fn : function (combo,record,index){
                                    reloadVRIs();
                                }
                            }
                        }
                    },
                    {
                        xtype : "button",
                        text : 'Nieuw',
                        handler : function (){
                            var url = deelgebiedActionBeanUrl + "?maakDeelgebied=true";
                            document.location.href = url;
                        }
                    },
                    {
                        xtype : "button",
                        text : 'Bewerk',
                        disabled : true,
                        id: "editDeelgebied",
                        handler : function (){
                            var dgId = Ext.getCmp("deelgebied").getValue();
                            if(dgId){
                                var url = deelgebiedActionBeanUrl + "?bewerkDeelgebied=true&filter=" + dgId;
                                document.location.href = url;
                            }else{
                                Ext.getCmp("deelgebied").setActiveError(true);
                            }
                        }
                    },
                    {
                        xtype : "button",
                        text : 'Verwijder',
                        id: "removeDeelgebied",
                        disabled : true,
                        handler: function () {
                            var dgId = Ext.getCmp("deelgebied").getValue();
                            if (dgId) {
                                var url = deelgebiedActionBeanUrl + "?removeDeelgebied=true&filter=" + dgId;
                                document.location.href = url;
                            } else {
                                Ext.getCmp("deelgebied").setActiveError(true);
                            }
                        }
                    }
                ]
            },
            {
                labelWidth: 190,
                id: 'dataowner',
                xtype: "tagfield",
                fieldLabel: 'Kies een beheerder',
                store: dataownerStore,
                queryMode: 'local',
                displayField: 'omschrijving',
                disabled: true,
                anyMatch: true,
                valueField: 'id',
                emptyText: "Selecteer",
                flex: 1,
                listeners: {
                    scope: this,
                    select: {
                        fn: function (combo, record, index) {
                           reloadVRIs();
                           combo.inputEl.dom.value = '';
                        }
                    }
                }
            },
            {
                xtype : 'container',
                layout : {
                    type : 'hbox'
                },
                margin: '5 0',
                items : [
                    {
                        labelWidth: 190,
                        id : 'rseqByKarAddress',
                        xtype : "textfield",
                        fieldLabel : 'Zoek op karadres',
                        disabled : true,
                        flex : 1
                    },
                    {
                        xtype : "button",
                        text : 'Zoeken',
                        handler : function (){
                            reloadVRIs();
                        }
                    }
                ]
            },
            {
                title : 'Geselecteerde verkeerssystemen',
                xtype : "grid",
                id: "grid",
                store: store,
                columns : [
                    {
                        xtype: 'checkcolumn',
                        text: '',
                        dataIndex: 'selected',
                        width: 35,
                        headerCheckbox: true,
                        stopSelection: true,
                        sortable: false,
                        draggable: false,
                        resizable: false,
                        menuDisabled: true,
                        hideable: false,
                        listeners: {
                            'checkchange': function(cell, rowindex, checked, record, event, opts){ 
                                var id = record.data.id;
                                var index = indexInUnselectedArray(id);
                                if(index !== -1){
                                    unselectedVRIs.splice(index,1);
                                }else{
                                    unselectedVRIs.push(id);
                                }
                            }
                        }
                    },
                    {
                        text : 'Omschrijving',
                        dataIndex : 'naam',
                        flex : 2
                    },
                    {
                        text : 'KAR Adres',
                        dataIndex : 'karAddress'
                    },
                    {
                        text : 'Dataowner',
                        dataIndex : 'dataowner',
                        flex : 1
                    },
                    {
                        text : 'Type',
                        dataIndex : 'type'
                    }
                ],
                height : 200,
                width : 800,
                listeners:{
                    scope:this,
                    headerclick: function(header,column,c,d,e){
                        if(column.dataIndex === "selected"){
                            menuState = !menuState;
                            store.each(function(record){
                                var id = record.data.id;
                                if(menuState){
                                    var index = indexInUnselectedArray(id);
                                    unselectedVRIs.splice(index,1);
                                }else{
                                    unselectedVRIs.push(id);
                                }
                                record.data.selected = menuState;
                            });
                        }
                    }
                }
                
            },
            {
                xtype : "combo",
                fieldLabel : 'Type export',
                allowBlank : false,
                value:"kv9",
                store : Ext.create('Ext.data.Store',{
                    fields : ['type','label'],
                    data : [
                         {
                            "type" : "kv9",
                            "label" : "KV9 XML"
                        },{
                            "type" : "incaa",
                            "label" : "INCAA .ptx"
                        },
                        {
                            "type" : "csvextended",
                            "label" : "CSV - bewegingen"
                        },
                        {
                            "type" : "csvsimple",
                            "label" : "CSV - VRI-informatie"
                        }
                    ]
                }),
                queryMode : 'local',
                displayField : 'label',
                valueField : 'type',
                layout : {
                    width : 150
                },
                listeners: {
                    scope: this,
                    select: function(combo, record) {
                        var vehicleTypeCombo = Ext.ComponentQuery.query('#vehicleType')[0];
                        var disableVT = false;
                        var type = record.get('type');
                        if(type === 'incaa') {
                            vehicleTypeCombo.setValue('Hulpdiensten');
                            disableVT = true;
                        } else {
                            vehicleTypeCombo.setValue('gemixt');
                            disableVT = type.indexOf("csv") !== -1;
                        }
                        
                        vehicleTypeCombo.setDisabled(disableVT);
                        reloadVRIs();
                    }
                },
                name : 'exportType',
                id: 'exportType',
                emptyText : "Selecteer"
            },{
                xtype : "checkbox",
                name : 'onlyValid',
                id : 'onlyValid',
                fieldLabel : 'Alleen geldige verkeerssystemen',
                listeners:{
                    change:{
                        fn:function(){
                            reloadVRIs();
                        },
                        scope:this
                    }
                }
            },
            {
                xtype : "checkbox",
                name : 'onlyReady',
                id : 'onlyReady',
                checked:true,
                fieldLabel : 'Alleen verkeerssystemen met vinkje "Gereed voor export"',
                listeners:{
                    change:{
                        fn:function(){
                            reloadVRIs();
                        },
                        scope:this
                    }
                }
            },
            {
                xtype : "combo",
                fieldLabel : 'Voertuigtypes',
                store : Ext.create('Ext.data.Store',{
                    fields : ['type','label'],
                    data : [
                        {
                            "type" : "gemixt",
                            "label" : "Beide"
                        },
                        {
                            "type" : "Hulpdiensten",
                            "label" : "HD"
                        },
                        {
                            "type" : "OV",
                            "label" : "OV"
                        }
                    ]
                }),
                queryMode : 'local',
                displayField : 'label',
                valueField : 'type',
                layout : {
                    width : 150
                },
                name : 'vehicleType',
                id : 'vehicleType',
                itemId : 'vehicleType',
                emptyText : "Selecteer",
                listeners : {
                    select : {
                        fn : function (){
                            reloadVRIs();
                        },
                        scope : this
                    }
                }
            }
        ],
        // Reset and Submit buttons
        buttons: [{
                text: 'Reset',
                handler: function () {
                    unselectedVRIs = [];
                    this.up('form').getForm().reset();
                    reloadVRIs ();
                }
            }, {
                text: 'Exporteer',
                formBind: true, //only enabled once the form is valid
                disabled: true,
                handler: function () {
                    var form = this.up('form').getForm();
                    addRseqIds();
                    if (form.isValid()) {
                        form.submit({
                            target: '_self',
                            success: function (form, action) {
                                Ext.Msg.alert('Success', action.result.msg);
                            },
                            failure: function (form, action) {
                                Ext.Msg.alert('Failed', action.result.msg);
                            },
                            params:{
                                onlyReady: Ext.getCmp("onlyReady").getValue()
                            }
                        });
                    }
                }
            }],
        renderTo: 'body'
    });
    grid = Ext.getCmp("grid");
    reloadVRIs();
});

function reloadVRIs (){
    store.removeAll();
    var filtertypeCombo = Ext.getCmp("filterType").getValue();
    var filtertype = filtertypeCombo.filterType;
    if(Ext.isArray(filtertypeCombo.filterType)){
        filtertype = filtertypeCombo.filterType[0];
    }
    var params = {};
    var text = "";
    var filter = null;
    if(filtertype === "deelgebied") {
        filter = Ext.getCmp("deelgebied").getValue();
        var deelgebiedText = Ext.getCmp("deelgebied").getValue();
        params ['rseqByDeelgebied'] = true;
        params['filter'] = filter;
        text = deelgebiedText;
    } else if(filtertype === "dataowner") {
        filter = Ext.Array.map(Ext.getCmp("dataowner").getValueRecords(), function(record) {
            return record.get('id');
        });
        var dataownerText = Ext.getCmp("dataowner").getDisplayValue();
        params ['rseqByDataowner'] = true;
        params['dataowner'] = filter.join(',');
        text = dataownerText;
    } else if(filtertype === "rseqByKarAddress") {
        filter = Ext.getCmp("rseqByKarAddress").getValue();
        params ['rseqByKarAddress'] = true;
        params['karAddress'] = filter;
        text = filter;
    } else {
        // Default all
        filter = "ALL";
        params['allRseqs'] = true;
        text = "gebruiker";
    }

    var onlyReady = Ext.getCmp("onlyReady").getValue();
    params["onlyReady"] = onlyReady;


    var exportType = Ext.getCmp("exportType").getValue();
    params["exportType"] = exportType;
    
    if(filter){
        doRseqRequest(params,text);
    }else{
        rseqsReceived([],"...");
    }
}

function doRseqRequest (params,text){
    grid.setLoading("Verkeerssystemen ophalen...");
    var me = this;
    var onlyValid = Ext.getCmp("onlyValid").getValue();
    params.onlyValid = onlyValid;
    var vehicleType = Ext.getCmp("vehicleType").getValue();
    params.vehicleType = vehicleType;
    Ext.Ajax.request({
        url : exportActionBeanUrl,
        method : 'GET',
        scope : me,
        timeout: 120000,
        params : params,
        success : function (response){
            var msg = Ext.JSON.decode(response.responseText);
            if(msg.success){
                var rseqsJson = msg.rseqs;
                rseqsReceived(rseqsJson,text);
            } else{
                Ext.MessageBox.show({
                    title : "Fout",
                    msg : "Kan VRI's niet ophalen. Probeer het opnieuw of neem contact op met de applicatie beheerder."+msg.error,
                    buttons : Ext.MessageBox.OK,
                    icon : Ext.MessageBox.ERROR
                });
            }
            grid.setLoading(false);
        },
        failure : function (response){
            Ext.MessageBox.show({
                title : "Ajax fout",
                msg : "Kan VRI's niet ophalen. Probeer het opnieuw of neem contact op met de applicatie beheerder."+response.responseText,
                buttons : Ext.MessageBox.OK,
                icon : Ext.MessageBox.ERROR
            });
            grid.setLoading(false);
        }
    });
}

function addRseqIds() {
    var ids = [];
    store.each(function(record) {
        if(record.get('selected')) {
            ids.push(record.get('id'));
        }
    });
    Ext.getCmp("rseqs").setValue(ids.join(', '));
}

function rseqsReceived(rseqs,naam) {
    for (var i = 0;i < rseqs.length;i++){
        rseqs[i].selected = indexInUnselectedArray(rseqs[i].id) === -1;
        store.add(rseqs[i]);
    }
    grid.setTitle(rseqs.length +' geselecteerde verkeerssystemen voor ' + naam);

}

function indexInUnselectedArray(id) {
    var index = -1;
    for (var i = 0; i < unselectedVRIs.length; i++) {
        if (unselectedVRIs[i] === id) {
            index = i;
            break;
        }
    }
    return index;
}
