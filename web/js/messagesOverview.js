/**
* Geo-OV - applicatie voor het registreren van KAR meldpunten
*
* Copyright (C) 2009-2015 B3Partners B.V.
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

Ext.define("MessagesOverview", {
    window: null,

    constructor: function() {
    },

    show: function(id) {
        var me = this;

        Ext.Ajax.request({
            url: contextPath + '/action/overview?rseq=' + id,
            method: 'GET',
            scope: this,
            success: function (response){
                me.showWithHtml(response.responseText);
            }
        });
    },

    showWithHtml: function(html) {
        var me = this;
        me.window = Ext.create('Ext.window.Window', {
            title: 'Export informatie',
            width: 769,
            height: 500,
            modal: true,
            icon: contextPath + '/images/silk/information.png',
            layout: 'fit',
            items: [{
                xtype: 'panel',
                autoScroll: true,
                html: html
            }],
            buttons: [{
                text: 'Sluiten',
                handler: function() {

                    profile.firstRun = false;
                    saveProfile();

                    me.window.destroy();
                    me.window = null;
                }
            }]
        }).show();
    }
});