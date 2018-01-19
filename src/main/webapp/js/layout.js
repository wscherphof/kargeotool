/* global Ext */

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
/**
 * Als het document klaar is met laden, zet de layout goed.
 */
Ext.onReady(function() {
    Ext.create('Ext.Viewport', {
        layout: 'border',
        defaultType: 'panel',
        defaults: {
            header: false,
            border: false,
            stateful: true
        },
        items: [
            {
                region: 'north',
                height: 30,
                contentEl: 'header',
                title: 'Header',
                header: false
            }, {
                region: 'east',
                stateId: 'east',
                id:"east",
                animCollapse: true,
                collapsible: true,
                split: true,
                width: 175,
                minWidth: 0,
                contentEl: 'rightbar',
                layout: {
                    type: 'fit'
                },
                defaultType: 'panel',
                defaults: {
                    border: 0,
                    width: '100%',
                    flex: 1
                },
                items: [
                    {
                        title: 'Legend',
                        header: false,
                        contentEl: 'legend'
                    }
                ],
                title: 'Legenda',
                header: true
            },
            {
                region: 'center',
                contentEl: 'kaart',
                title: 'Kaart',
                header: false
            }
        ]
    });
    Ext.create('CollapsibleWindow', {
        title: 'Zoeken',
        id: 'searchformWindow',
        height: 300
    });
    Ext.create('CollapsibleWindow', {
        title: 'Overzicht verkeerssysteem',
        id: 'rseqInfoWindow',
        itemId: 'rseqInfoWindow',
        y: 350,
        layout: {
            type: 'vbox',
            align: 'stretch'
        },
        items: [
            {
                xtype: 'container',
                contentEl: 'overzichtTitel',
                margin: 5
            },
            {
                xtype: 'container',
                flex: 1,
                itemId: 'overzichtContainer',
                layout: 'fit'
            },
            {
                xtype: 'container',
                contentEl: 'rseqOptions',
                margin: 5
            }
        ]
    });
    Ext.create('CollapsibleWindow', {
        title: 'Help',
        y: 560,
        height: 140,
        layout: 'fit',
        id: 'helpWindow',
        items: [
            {
                xtype: 'container',
                itemId: 'help',
                contentEl: 'form',
                padding: 5,
                scrollable: true
            }
        ]
    });
    
    Ext.create('TOC', {
        editor:editor
    });
});
