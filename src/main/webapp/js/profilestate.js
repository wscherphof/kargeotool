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

/**
 * A Ext Provider implementation which saves and retrieves state via Ajax in the
 * user profile.
 */
Ext.define('ProfileStateProvider', {
    extend: 'Ext.state.Provider',

    /**
     * Creates a new CookieProvider.
     * @param {Object} [config] Config object.
     */
    constructor : function(config){
        var me = this;
        me.callParent(arguments);
        if(profile.state == undefined) {
            profile.state = {};
        }
        me.state = profile.state;
    },

    // private
    set: function(name, value){
        var me = this;

        if(typeof value == "undefined" || value === null){
            me.clear(name);
            return;
        }
        profile.state[name] = value;
        saveProfile();
        me.callParent(arguments);
    },

    // private
    clear: function(name){
        delete profile.state[name];
        saveProfile();
        this.callParent(arguments);
    }
});

Ext.state.Manager.setProvider(Ext.create('ProfileStateProvider'));
