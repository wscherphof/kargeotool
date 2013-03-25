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
    Ext.state.Manager.setProvider(Ext.create('Ext.state.CookieProvider'));
    viewport = Ext.create('Ext.Viewport', {
        layout: 'border',
        defaultType: 'panel',
        defaults: {
            header: false,
            border: false
        },
        items: [
            {
                region: 'north',
                height: 30,
                contentEl: 'header'
            }, {
                region: 'east',
                animCollapse: true,
                collapsible: true,
                split: true,
                width: 175,
                minWidth: 0,
                contentEl: 'rightbar',
                layout: {
                    type: 'accordion',
                    align: 'stretch',
                    multi: true,
                    hideCollapseTool: true
                },
                defaultType: 'panel',
                defaults: {
                    border: 0,
                    width: '100%',
                    flex: 1
                },
                items: [
                    {
                        title: 'Legenda',
                        contentEl: 'legend'
                    }
                ]
            }, {
                region: 'west',
                animCollapse: true,
                collapsible: true,
                split: true,
                width: 275,
                minWidth: 0,
                contentEl: 'leftbar',
                layout: {
                    type: 'accordion',
                    align: 'stretch',
                    multi: true
                },
                defaultType: 'panel',
                defaults: {
                    border: 0,
                    width: '100%',
                    flex: 1
                },
                items: [
                    {
                        contentEl: 'searchform',
                        autoScroll: true,
                        title: 'Zoeken'
                    },
                    {
                        id:'rseqInfoPanel',
                        title: 'Overzicht verkeerssysteem',
                        defaultType: 'container',
                        layout: 'vbox',
                        defaults: {
                            border: 0,
                            width: '100%'
                        },
                        items: [
                            {
                                contentEl: 'overzichtTitel',
                                height: 40,
                                margin: '3px'
                            },
                            {
                               id:'overzicht',
                                flex: 1
                            },
                            {
                                contentEl: 'rseqOptions',
                                height: 25
                            }
                        ]
                    },
                    {
                        contentEl: 'form',
                        title: 'Help'
                    }
                ]
            },
            {
                region: 'center',
                contentEl: 'kaart'
            }
        ]
    });
});
