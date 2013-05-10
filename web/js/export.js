/**
 * Geo-OV - applicatie voor het registreren van KAR meldpunten
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
Ext.onReady(function (){
    store = Ext.create('Ext.data.Store',{
        storeId : 'rseqStore',
        fields : ['naam','dataowner','type'],
        data : {
            'items' : []
        },
        proxy : {
            type : 'memory',
            reader : {
                type : 'json',
                root : 'items'
            }
        }
    });

    grid = Ext.create('Ext.grid.Panel',{
        title : 'Geselecteerde verkeerssystemen',
        store : store,
        columns : [
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
        height : 200,
        width : 400,
        renderTo : 'rseqGrid'
    });
});

function deelgebiedChanged (){
    var select =Ext.get("deelgebied").dom; 
    var index = select.selectedIndex;
    if(index != 0){
        var me =this;
        var deelgebied = select.options[index].value;
        var deelgebiedText = select.options[index].text;
        
        Ext.Ajax.request({
            url: exportActionBeanUrl,
            method: 'GET',
            scope: me,
            params: {
                'rseqByDeelgebied' : true,
                'deelgebied': deelgebied
            },
            success: function (response){
                var msg = Ext.JSON.decode(response.responseText);
                if(msg.success){
                    var rseqsJson = msg.rseqs;
                    rseqsReceived(rseqsJson,deelgebiedText);
                }else{
                    Ext.MessageBox.show({title: "Fout", msg: msg.error, buttons: Ext.MessageBox.OK, icon: Ext.MessageBox.ERROR});                    
                }
            },
            failure: function (response){
                Ext.MessageBox.show({title: "Ajax fout", msg: response.responseText, buttons: Ext.MessageBox.OK, icon: Ext.MessageBox.ERROR});                    
            }
        });
    }
}

function rseqsReceived (rseqs,naam){
    store.loadData(rseqs);
    grid.setTitle('Geselecteerde verkeerssystemen voor ' + naam);
}