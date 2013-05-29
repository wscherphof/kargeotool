/*
 Geo-OV - applicatie voor het registreren van KAR meldpunten               
 
 Copyright (C) 2009-2013 B3Partners B.V.                                   
 
 This program is free software: you can redistribute it and/or modify      
 it under the terms of the GNU Affero General Public License as            
 published by the Free Software Foundation, either version 3 of the        
 License, or (at your option) any later version.                           
 
 This program is distributed in the hope that it will be useful,           
 but WITHOUT ANY WARRANTY; without even the implied warranty of            
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the              
 GNU Affero General Public License for more details.                       
 
 You should have received a copy of the GNU Affero General Public License  
 along with this program. If not, see <http://www.gnu.org/licenses/>.      
 */
var store = null;
var grid = null;

function loadGrid (){
    store = Ext.create('Ext.data.Store',{
        storeId : 'rseqStore',
        fields : ['id','naam','dataowner','type','karAddress'],
        data : {
            'items' : rseqs
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
        title : 'Geselecteerde verkeerssystemen',
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
                dataIndex : 'naam'
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
        height : 300,
        width : 700,
        renderTo : 'body',
        listeners : {
            select : {
                scope : this,
                fn : rowSelected
            }

        }
    });
}

function rowSelected (rowmodel,record){
    var selectedId = record.data.id;
    var iframe = Ext.get("VRIDetail").dom;
    iframe.src = righsUrl + "&rseq=" + selectedId;
}
var rseqType = {
    "" : "nieuw verkeerssysteem",
    "CROSSING" : "VRI",
    "GUARD" : "bewakingssysteem nadering voertuig",
    "BAR" : "afsluittingssysteem"
};

var rightsStore;
var rightsGrid;

function rseqFrame (){
    var me = this;
    Ext.create('Ext.form.Panel',{
        title : 'Bewerken ' + me.rseqType[rseq.type] + (rseq.karAddress == null ? "" : " met KAR adres " + rseq.karAddress),
        height : 320,
        width : 450,
        renderTo : 'rseq',
        icon : rseq.type == "" ? karTheme.crossing : karTheme[rseq.type.toLowerCase()],
        layout : 'fit',
        items : {
            id : 'rseqForm',
            xtype : 'form',
            bodyStyle : 'padding: 5px 5px 0',
            fieldDefaults : {
                msgTarget : 'side',
                disabled : true,
                disabledCls : 'myDisabledClass',
                labelWidth : 150
            },
            defaultType : 'textfield',
            defaults : {
                anchor : '100%'
            },
            items : [{
                    xtype : 'fieldcontainer',
                    fieldLabel : 'Soort verkeerssysteem',
                    defaultType : 'radiofield',
                    layout : 'vbox',
                    items : [{
                            name : 'type',
                            inputValue : 'CROSSING',
                            checked : rseq.type == 'CROSSING',
                            boxLabel : me.rseqType['CROSSING']
                        },{
                            name : 'type',
                            inputValue : 'GUARD',
                            checked : rseq.type == 'GUARD',
                            boxLabel : me.rseqType['GUARD']
                        },{
                            name : 'type',
                            inputValue : 'BAR',
                            checked : rseq.type == 'BAR',
                            boxLabel : me.rseqType['BAR']
                        }]
                }
                ,{
                    fieldLabel : 'Beheerder',
                    //xtype: 'text',
                    name : 'dataOwner',
                    value : dataOwner
                }
                ,{
                    fieldLabel : 'Beheerdersaanduiding',
                    name : 'crossingCode',
                    value : rseq.crossingCode
                },{
                    xtype : 'numberfield',
                    fieldLabel : 'KAR adres',
                    name : 'karAddress',
                    allowBlank : false,
                    minValue : 0,
                    value : rseq.karAddress,
                    listeners : {
                        change : function (field,value){
                            value = parseInt(value,10);
                            field.setValue(value);
                        }
                    }
                },{
                    fieldLabel : 'Plaats',
                    name : 'town',
                    value : rseq.town
                },{
                    fieldLabel : 'Locatie',
                    name : 'description',
                    value : rseq.description
                },{
                    xtype : 'datefield',
                    format : 'Y-m-d',
                    fieldLabel : 'Geldig vanaf',
                    name : 'validFrom',
                    value : rseq.validFrom
                },{
                    xtype : 'datefield',
                    format : 'Y-m-d',
                    fieldLabel : 'Geldig tot',
                    name : 'validUntil',
                    value : rseq.validUntil
                }]
        }
    });

    var checkboxRenderer = function (p1,p2,record,row,column,store,grid){
        var field = ["","read","write"][column];
        return Ext.String.format("<input type='checkbox' {0} onclick='rightChangedClick(event,{1},{2})'></input>",
                record.get(field) ? "checked='checked'" : "",
                row,
                column
                );
    };

    rightsStore = Ext.create('Ext.data.Store',{
        storeId : 'rightsStore',
        fields : ['id','user','read','write'],
        data : {
            'items' : [{
                    user : 'meine',
                    read : true,
                    write : false
                }]
        },
        proxy : {
            type : 'memory',
            reader : {
                type : 'json',
                root : 'items'
            }
        }
    });
    rightsGrid = Ext.create(Ext.grid.Panel,{
        title : 'Geselecteerde verkeerssystemen',
        xtype : "grid",
        id : "grid",
        store : rightsStore,
        columns : [
            {
                text : 'Gebruiker',
                dataIndex : 'user',
                flex : 1
            },
            {
                text : 'Lezen',
                dataIndex : 'read',
                renderer : checkboxRenderer
            },
            {
                text : 'Schrijven',
                dataIndex : 'write',
                renderer : checkboxRenderer
            }
        ],
        height : 300,
        width : 400,
        renderTo : 'rseqRights',
        dockedItems : [{
                xtype : 'toolbar',
                dock : 'bottom',
                ui : 'footer',
                defaults : {
                    minWidth : 100
                },
                items : [
                    {
                        xtype : "combo",
                        id: "gebruikerCombo",
                        fieldLabel : 'Gebruiker',
                        allowBlank : false,
                        store : Ext.create('Ext.data.Store',{
                            fields : ['fullname','id'],
                            data : me.gebruikers
                        }),
                        queryMode : 'local',
                        displayField : 'fullname',
                        valueField : 'id',
                        layout : {
                            width : 150
                        },
                        name : 'gebruikerCombo',
                        emptyText : "Selecteer",
                        flex : 1
                    },
                     {
                        xtype : 'button',
                        text : 'Voeg toe',
                        handler: function(){
                            var a = 0;
                        }
                    }
                ]
            }]
    });

}