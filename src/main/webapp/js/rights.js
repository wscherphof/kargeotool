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
    iframe.src = rightsUrl + "&rseq=" + selectedId;
}