
function objectSubset(object, keys) {
    var j = { };

    Ext.Object.each(object, function(key, value) {
        if(Ext.Array.contains(keys, key)) {
            j[key] = value;
        }
    });

    return j;
}

Ext.define('RSEQ', {
    config:{
        id: null,
        location:null,
        description:null,
        validFrom:null,
        movements:null,
        karAddress:null,
        crossingCode: null,
        dataOwner:null,
        points:null,
        town:null,
        type:null // ACTIVATION_1: Inmeldpunt, ACTIVATION_2: uitmeldpunt, ACTIVATION_3: voorinmeldpunt
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
    addMovement: function (movement){
        if(!this.movements){
            this.movements = new Array();
        }
        this.movements.push(movement);
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
                crossingCode: this.crossingCode,
                town:this.town,
                type:this.type,
                className: this.$className
            }
        });
        var json = {
            "type" : "FeatureCollection",
            "features" :points
        };
        return json;
    },
    toJSON: function() {
        var j = objectSubset(this, ["id", "description", "validFrom", 
            "karAddress", "dataOwner", "crossingCode", "town", "type", 
            "location"]);
        
        j.points = [];
        Ext.Array.each(this.points, function(point) {
            j.points.push(point.toJSON());
        });
        
        j.movements = [];
        Ext.Array.each(this.movements, function(mvmt) {
            j.movements.push(mvmt.toJSON());
        });
        
        return j;
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
                signalGroupNumbers:this.signalGroupNumbers,
                className: this.$className
            }
        };
        return json;
    },
    toJSON: function() {
        return objectSubset(this, ["id", "geometry", "nummer", "label"]);        
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
        virtualLocalLoopNumber: null,
        triggerType:null,
        vehicleTypes:null
    },
    constructor: function(config) {        
        this.initConfig(config);    
    },
    toJSON: function() {
        var keys = ["id", "beginEndOrActivation", "pointId"];
        if(this.beginEndOrActivation == "ACTIVATION") {
            keys = Ext.Array.merge(keys, ["commandType", "distanceTillStopLine", 
                "triggerType", "signalGroupNumber", "virtualLocalLoopNumber", 
                "vehicleTypes"]);
        }
        return objectSubset(this, keys);  
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
    },
    addMap : function (map){
        if(!this.maps){
            this.maps = new Array();
        }
        this.maps.push(map);
    },
    toJSON: function() {
        var j = objectSubset(this, ["id", "nummer"]);
        
        j.maps = [];
        Ext.Array.each(this.maps, function(map) {
            j.maps.push(map.toJSON());
        });
        return j;
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