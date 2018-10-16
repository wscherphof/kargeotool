/* global Ext */

/**
 * KAR Geo Tool - applicatie voor het registreren van KAR meldpunten
 *
 * Copyright (C) 2009-2018 B3Partners B.V.
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

Ext.define("EditUsers", {
    defaultConfig: {
        dataOwners: [],
        gebruikers: [],
        addUrl:null,
        removeUrl:null,
        addDataOwner: function (id, code, name) {},
        isEditting: false
    },
    constructor: function (conf) {
        this.initConfig(conf);
        this.createGrid();
        if (this.isEditting) {
            this.createCombobox();
        }
    },
    createGrid: function () {
        Ext.define('User', {
            extend: 'Ext.data.Model',
            fields: ['id', 'username', 'fullname', 'mail', 'phone']
        });
        var userStore = Ext.create('Ext.data.Store', {
            model: 'User',
            data: this.gebruikers
        });
        Ext.create('Ext.grid.Panel', {
            renderTo: Ext.get("usergrid"),
            store: userStore,
            height: 500,
            title: 'Gebruikers (' + this.gebruikers.length + ")",
            listeners: {
                cellclick: {
                    fn: function (grid, td, cellIndex, record, tr, rowIndex, e) {
                        var target = e.getTarget();
                        if (!target || !target.className) {
                            return;
                        }
                        if (target.className.indexOf("removeobject") !== -1) {
                            e.preventDefault();
                            Ext.Msg.show({
                                title: "Weet u het zeker?",
                                msg: "Wilt u deze gebruiker verwijderen?",
                                fn: function (button) {
                                    if (button === 'yes') {
                                        document.location.href = this.deleteUrl +"&gebruiker=" + record.data.id;
                                    }
                                },
                                scope: this,
                                buttons: Ext.Msg.YESNO,
                                buttonText: {
                                    no: "Nee",
                                    yes: "Ja"
                                },
                                icon: Ext.Msg.WARNING

                            });
                        }
                        if (target.className.indexOf("editobject") !== -1) {
                            e.preventDefault();
                            document.location.href = this.editUrl +"&gebruiker=" + record.data.id;
                        }
                    },
                    scope: this
                }
            },
            columns: [
                {
                    text: 'Gebruikersnaam',
                    flex: 1,
                    dataIndex: 'username',
                    hideable: false,
                    menuDisabled: true,
                    filter: {
                        xtype: 'textfield'
                    }
                },
                {
                    text: 'Naam',
                    flex: 1,
                    hideable: false,
                    menuDisabled: true,
                    dataIndex: 'fullname',
                    filter: {
                        xtype: 'textfield'
                    }
                },
                {
                    text: 'E-mail',
                    flex: 1,
                    menuDisabled: true,
                    hideable: false,
                    dataIndex: 'mail',
                    filter: {
                        xtype: 'textfield'
                    }
                },
                {
                    text: 'Telefoonnummer',
                    flex: 1,
                    hideable: false,
                    menuDisabled: true,
                    dataIndex: 'phone',
                    filter: {
                        xtype: 'textfield'
                    }
                }, {
                    id: 'edit',
                    header: 'Verwijder',
                    dataIndex: 'id',
                    width: 375,
                    sortable: false,
                    hideable: false,
                    menuDisabled: true,
                    renderer: (function (value, metadata, record) {
                        var links = [Ext.String.format('<a href="#" class="editobject">Bewerken</a>', value)];
                        if(record.data.id !== this.currentUser){
                            links.push(Ext.String.format('<a href="#" class="removeobject">Verwijderen</a>', value));
                        }
                        return links.join(" | ");
                    }).bind(this)
                }
            ],
            bbar:{
                items:[
                    {
                        xtype:'button', 
                        text: "Nieuw account toevoegen",
                        disabled: this.isEditting,
                        listeners: {
                            click: function () {
                                document.location.href = this.addUrl;
                            },
                            scope: this
                        }
                    }]
            },
            plugins: [
                Ext.create('Ext.ux.grid.GridHeaderFilters', {
                    enableTooltip: false,
                    reloadOnChange: true
                })
            ]
        });
    },
    createCombobox: function () {
        this.readUsedDataOwners();
        this.dataOwnerStore = Ext.create('Ext.data.Store', {
            fields: [
                {name: 'id', type: 'string'},
                {name: 'code', type: 'string'},
                {name: 'name', type: 'string'},
                {name: 'label', calculate: function (data) {
                        return data.code + ' - ' + data.name;
                    }}
            ],
            data: this.getData()
        });
        this.combo = Ext.create('Ext.form.field.ComboBox', {
            fieldLabel: '',
            name: 'availableDataOwnersCombo',
            allowBlank: true,
            emptyText: 'Selecteer een wegbeheerder...',
            anyMatch: true,
            displayField: 'label',
            valueField: 'id',
            queryMode: 'local',
            triggerAction: 'all',
            width: 300,
            lastQuery: '',
            store: this.dataOwnerStore,
            renderTo: 'availableDataOwnersContainer',
            listeners: {
                scope: this,
                select: function (combo, record) {
                    this.dataOwnerSelected(record);
                }
            }
        });
    },
    readUsedDataOwners: function () {
        this.config.usedDataOwners = {};
        var usedOwnersChecks = document.querySelectorAll('.used-data-owner');
        if (usedOwnersChecks.length > 0) {
            for (var i = 0; i < usedOwnersChecks.length; i++) {
                var value = usedOwnersChecks[i].value;
                if (usedOwnersChecks[i].checked) {
                    this.config.usedDataOwners[value] = true;
                }
            }
        }
    },
    getData: function () {
        var dataOwners = [];
        for (var i = 0; i < this.config.dataOwners.length; i++) {
            var dao = this.config.dataOwners[i];
            if (this.config.usedDataOwners.hasOwnProperty("" + dao.id)) {
                continue;
            }
            dataOwners.push({
                code: dao.code,
                name: dao.name,
                id: dao.id
            });
        }
        return dataOwners;
    },
    insertDataOwner: function (id, code, name) {
        this.dataOwnerStore.add({
            code: code,
            name: name,
            id: id
        });
    },
    dataOwnerSelected: function (dataOwner) {
        if (!dataOwner) {
            return;
        }
        if (this.config.addDataOwner) {
            this.config.addDataOwner(dataOwner.get('id'), dataOwner.get('code'), dataOwner.get('name'));
        }
        this.combo.clearValue();
        this.dataOwnerStore.remove(dataOwner);
    }
});