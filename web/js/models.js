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
        if(!config.description){
            config.description = "";
        }
        this.initConfig(config);    
    },
    getPointById : function (id){
        if(this.points){
            for (var i = 0 ; i < this.points.length ;i++){
                if(this.points[i].getId() == id){
                    return this.points[i];
                }
            }
        }
        return null;
    },
    getMovementById : function (id){
        for (var i = 0 ; i < this.movements.length ;i++){
            if(this.movements[i].getId() == id){
                return this.movements[i];
            }
        }
        return null;
    },
    addPoint : function (point){
        if(!this.points){
            this.points = new Array();
        }
        this.points.push(point);
    },
    toGeoJSON : function (){
        var points = new Array();
        if(this.points){
            for (var i = 0 ; i < this.points.length; i++){
                var point = this.points[i].toGeoJSON();
                points.push(point);
            }
        }
        points.push({
            type: "Feature",
            geometry: this.location,
            properties:{
                id: this.id,
                description:this.description,
                validFrom:this.validFrom,
                karAddress:this.karAddress,
                dataOwner:this.dataOwner,
                town:this.town,
                type:this.type
            }
        });
        var json = {
            "type" : "FeatureCollection",
            "features" :points
        };
        return json;
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
        if(!config.label){
            config.label = "";
        }
        this.initConfig(config);    
    },
    toGeoJSON : function(){
        var json = {
            type: "Feature",
            geometry : this.geometry,
            properties:{
                id: this.id,
                label:this.label,
                movementNumbers:this.movementNumbers,
                type:this.type,
                nummer:this.nummer,
                signalGroupNumbers:this.signalGroupNumbers
            }
        };
        return json;
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