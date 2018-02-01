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
        addDataOwner: function(id, code, name) {}
    },
    constructor: function(conf) {
        this.initConfig(conf);
        this.createCombobox();
    },
    createCombobox: function() {
        this.readUsedDataOwners();
        this.dataOwnerStore = Ext.create('Ext.data.Store', {
            fields: [
                { name: 'id', type: 'string' },
                { name: 'code', type: 'string' },
                { name: 'name', type: 'string' },
                { name: 'label', calculate: function(data) { return data.code + ' - ' + data.name; }}
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
                select: function(combo, record) {
                    this.dataOwnerSelected(record);
                }
            }
        });
    },
    readUsedDataOwners: function() {
        this.config.usedDataOwners = {};
        var usedOwnersChecks = document.querySelectorAll('.used-data-owner');
        if(usedOwnersChecks.length > 0) {
            for(var i = 0; i < usedOwnersChecks.length; i++) {
                var value = usedOwnersChecks[i].value;
                if(usedOwnersChecks[i].checked) {
                    this.config.usedDataOwners[value] = true;
                }
            }
        }
    },
    getData: function() {
        var dataOwners = [];
        for(var i = 0; i < this.config.dataOwners.length; i++) {
            var dao = this.config.dataOwners[i];
            if(this.config.usedDataOwners.hasOwnProperty("" + dao.id)) {
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
    insertDataOwner: function(id, code, name) {
        this.dataOwnerStore.add({
            code: code,
            name: name,
            id: id
        });
    },
    dataOwnerSelected: function(dataOwner) {
        if(!dataOwner) {
            return;
        }
        if(this.config.addDataOwner) {
            this.config.addDataOwner(dataOwner.get('id'), dataOwner.get('code'), dataOwner.get('name'));
        }
        this.combo.clearValue();
        this.dataOwnerStore.remove(dataOwner);
    }
});