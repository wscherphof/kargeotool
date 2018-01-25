/* global Ext, editor */

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
            },
            {
                region: 'west',
                title: 'Overzicht verkeerssysteem',
                id: 'rseqInfoWindow',
                itemId: 'rseqInfoWindow',
                split: true,
                header: true,
                collapsible: true,
                width: 300,
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
    Ext.create('CollapsibleWindow', {
        title: 'Legenda',
        y: 360,
        height: 300,
        layout: 'fit',
        id: 'legendaWindow',
        items: [
            {
                xtype: 'container',
                itemId: 'legend',
                contentEl: 'legend',
                padding: 5,
                scrollable: true
            }
        ]
    });
    Ext.create('TOC', {
        editor:editor
    });
});
