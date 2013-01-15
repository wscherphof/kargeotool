Ext.define('VRI', {
    extend:  'Ext.data.Model',
    fields: [
        {name: 'id',  type: 'int'},
        {name: 'type',  type: 'string'},
        {name: 'description',  type: 'string'},
        {name: 'dataOwner',  type: 'string'}
    ]
});

Ext.define('SignalGroup', {
    extend:  'Ext.data.Model',
    fields: [
        {name: 'id',  type: 'int'},
        {name: 'type',  type: 'string'},
        {name: 'description',  type: 'string'},
        {name: 'dataOwner',  type: 'string'}
    ]
});

Ext.define('ActivationGroup', {
    extend:  'Ext.data.Model',
    fields: [
        {name: 'id',  type: 'int'},
        {name: 'type',  type: 'string'},
        {name: 'description',  type: 'string'},
        {name: 'dataOwner',  type: 'string'}
    ]
});