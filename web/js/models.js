Ext.define('RSEQ', {
    config:{
        id: null,
        location:null,
        description:null,
        validFrom:null,
        movements:null,
        karAddress:null,
        dataOwner:null,
        points:null,
        town:null,
        type:null
    },
    constructor: function(config) {        
        this.initConfig(config);    
    }
});

Ext.define('Point', {
    config:{
        id: null,
        geometry:null,
        label:null,
        movementNumbers:null,
        type:null,
        nummer:null,
        signalGroupNumbers:null
    },
    constructor: function(config) {        
        this.initConfig(config);    
    }
});

Ext.define('MovementActivationPoint', {
    config:{
        id: null,
        beginEndOrActivation:null,
        commandType:null,
        distanceTillStopLine:null,
        pointId:null,
        signalGroupNumber:null,
        triggerType:null,
        vehicleTypes:null
    },
    constructor: function(config) {        
        this.initConfig(config);    
    }
});

Ext.define('Movement', {
    config:{
        id: null,
        maps:null,
        nummer:null
    },
    constructor: function(config) {        
        this.initConfig(config);    
    }
});

function testModels(){
    Ext.Ajax.request({
        url:editorActionBeanUrl,
        method: 'GET',
        scope: this,
        params: {
            'rseqJSON' : true,
            unitNumber :9999
        },
        success: function (response){
            var msg = Ext.JSON.decode(response.responseText);
            if(msg.success){
                var rJson = msg.roadsideEquipment;
                var rseq = makeRseq(rJson);
                var a = 0;
            }else{
                alert("Ophalen resultaten mislukt.");
            }
        },
        failure: function (response){
            alert("Ophalen resultaten mislukt.");
        }
    });
}

function makeRseq (json){
    var rseq = Ext.create("RSEQ",json);
    var movements = makeMovements(json.movements);
    var points = makePoints(json.points);
    rseq.setMovements(movements);
    rseq.setPoints(points);
    return rseq;
}

function makeMovements(json){
    var movements = new Array();
    for (var i = 0 ; i < json.length; i++){
        var mvJson = json[i];
        var movement = makeMovement(mvJson);
        var maps = makeMAPs(mvJson.maps);
        movement.setMaps(maps);
        movements.push(movement);
    }
    
    return movements;
}

function makeMAPs(json){
    var maps = new Array();
    for(var i = 0 ; i < json.length ; i++){
        var map = Ext.create("MovementActivationPoint",json[i]);
        maps.push(map);
    }
    return maps;
}

function makeMovement(json){
    return Ext.create("Movement",json);
}

function makePoints(json){
    var points = new Array();
    for(var i = 0 ; i < json.length ; i++){
        var point = Ext.create("Point",json[i]);
        points.push(point);
    }
    return points;
}