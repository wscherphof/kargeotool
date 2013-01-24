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
 * In dit .js bestand worden de models gedefineerd.
 */
function objectSubset(object, keys) {
    var j = { };

    Ext.Object.each(object, function(key, value) {
        if(Ext.Array.contains(keys, key)) {
            j[key] = value;
        }
    });

    return j;
}
/**
 * Roadside Equipment model.
 */
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
    /**
     *@constructor
     * @param config.id het id van de RSEQ
     * @param config.location de locatie als geometry
     * @param config.description de omschrijving
     * @param config.validFrom geldig vanaf
     * @param config.movements de movements die bij deze RSEQ horen.
     * @param config.karAddress het kar adres
     * @param config.crossingCode het kruispunt nummer
     * @param config.dataOwner de eigenaar van de bijhorende data
     * @param config.points de punten die bij deze RSEQ horen
     * @param config.town de plaats waar deze RSEQ bij hoort
     * @param config.type het type van de RSEQ
     */
    constructor: function(config) {     
        if(!config.description){
            config.description = "";
        }
        if(!config.points) {
            config.points = [];
        }
        if(!config.movements) {
            config.movements = [];
        }
        if(!config.validFrom) {
            config.validFrom = Ext.Date.format(new Date(), "Y-m-d");
        }
        this.initConfig(config);    
    },
    /**
     * Haal het punt op met behulp van het id van het punt
     * @param id het id van het gewenste punt
     * @return het gevonden punt of null indien niet gevonden.
     */
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
    /**
     * Haal de movement op met behulp van het id.
     * @param id het id van de gewenste movement.
     * @return het movement object of null indien niet gevonden.
     */
    getMovementById : function (id){
        for (var i = 0 ; i < this.movements.length ;i++){
            if(this.movements[i].getId() == id){
                return this.movements[i];
            }
        }
        return null;
    },
    /**
     * Voegt een punt toe
     * @param point het toe te voegen punt
     */
    addPoint : function (point){
        if(!this.points){
            this.points = new Array();
        }
        this.points.push(point);
    },
    /**
     * Voegt een movement toe
     * @param movement de toe te voegen movement
     */
    addMovement: function (movement){
        if(!this.movements){
            this.movements = new Array();
        }
        this.movements.push(movement);
    },
    
    
    /**
     * Haal alle movements in deze rseq op waarin het gegeven point voorkomt
     * @return een array van movements waar het point in voorkomt.
     */
    findMovementsForPoint: function(point) {
        var movements = [];
        
        Ext.Array.each(this.getMovements(), function(movement) {
            Ext.Array.each(movement.getMaps(), function(map) {
                if(map.pointId == point.id) {
                    movements.push({
                        movement: movement,
                        map: map
                    });
                }
            });
        });
        return movements;
    },
    
    /**
     * Controleert of er in een movement van het gegeven uitmeldpunt een
     * eindpunt is.
     */
    heeftUitmeldpuntEindpunt: function(uitmeldpunt) {
        
        var pointMovements = this.findMovementsForPoint(uitmeldpunt);
        
        var result = false;
        Ext.Array.each(pointMovements, function(mvmt) {
            Ext.Array.each(mvmt.maps, function(map) {
                if(map.beginEndOrActivation == "END") {
                    result = true;
                    // Stop met Ext.Array.each() (break)
                    return false;
                }
                // Ga verder met volgende Ext.Array.each();
                return true;
            });
            // Indien nog geen eindpunt gevonden ga verder met volgende movement
            return result == false;
        });
        return result;
    },    
    
    addUitmeldpunt: function(uitmeldpunt, map) {
        this.addPoint(uitmeldpunt);
        this.addMovement(Ext.create(Movement, {
            id: Ext.id(),
            nummer: null,
            maps: [map]
        }));
    },

    
    addEindpunt: function(uitmeldpunt, eindpunt) {
        this.addPoint(eindpunt);
        
        var pointMovements = this.findMovementsForPoint(uitmeldpunt);

        // Indien maar een enkele movement zonder eindpunt, voeg alleen eindpunt
        // toe aan dat movement
        
        if(pointMovements.length == 0) {
            alert("Ongeldige state, moet altijd movement zijn!");
        }
        if(pointMovements.length == 1) {
            var movement = pointMovements[0];
            var hasEnd = false;
            Ext.Array.each(movement.maps, function(map) {
                if(map.beginEndOrActivation == "END") {
                    hasEnd = true;
                }
            });
            if(!hasEnd) {
                newMap = Ext.create(MovementActivationPoint, {
                    id: Ext.id(),
                    beginEndOrActivation: "END",
                    pointId: eindpunt.getId()
                });         

                movement.maps.push(newMap);                
                return;
            }
        }

        var newMovement = Ext.create(Movement, {
            id: Ext.id(),
            nummer: null,
            maps: []
        });
        
        var newMap;
        
        
        // Kopieer alle punten van andere movements voor dit uitmeldpunt behalve
        // eindpunten
        
        Ext.Array.each(pointMovements, function(mvmt) {
            Ext.Array.each(mvmt.maps, function(map) {
                if(map.beginEndOrActivation != "END") {
                    
                    var config = {
                        id: Ext.id(),
                        beginEndOrActivation: map.beginEndOrActivation,
                        pointId: map.pointId
                    };
                    if(map.beginEndOrActivation == "ACTIVATION") {
                        Ext.Object.merge(config, {
                            commandType: map.commandType,
                            distanceTillStopLine: map.distanceTillStopLine,
                            signalGroupNumber: map.signalGroupNumber,
                            virtualLocalLoopNumber: map.virtualLocalLoopNumber,
                            triggerType: map.triggerType,
                            vehicleTypes: map.vehicleTypes
                        });
                    }
                    newMap = Ext.create(MovementActivationPoint, config);
                    newMovement.maps.push(newMap);
                }
            });
        });
        
        newMap = Ext.create(MovementActivationPoint, {
            id: Ext.id(),
            beginEndOrActivation: "END",
            pointId: eindpunt.getId()
        });         
            
        newMovement.maps.push(newMap);
        
        this.addMovement(newMovement);
    },
    
    /**
     * Geeft een GeoJSON object terug dat deze RSEQ representeert
     * @return GeoJSON object
     */
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
    /**
     * Geeft een JSON object terug dat deze RSEQ representeert
     * @return json object
     */
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

/**
 * Class voor Point
 */
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
    /**
     * @constructor
     * @param config.id id van dit punt
     * @param config.geometry geometry van dit punt
     * @param config.label het label van dit punt
     * @param config.movementNumbers nummers van de bijhorende movements
     * @param config.type type van dit punt
     * @param config.nummer nummer van dit punt
     * @param config.signalGroupNumbers nummers van de bijhorende signaal groepen
     */
    constructor: function(config) {        
        if(!config.label){
            config.label = "";
        }
        if(!config.id) {
            config.id = Ext.id();
        }
        this.initConfig(config);    
    },
    /**
     * Geeft dit object als GeoJSON terug
     * @return een GeoJSON representatie van dit object.
     */
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
/**
 * Class van een activatie punt van een movement.
 */
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
    /**
     *@constructor
     *@param config.id id van dit activatie punt
     *@param config.beginEndOrActivation BEGIN,END of ACTIVATION 
     *@param config.commandType het commando type
     *@param config.distanceTillStopLine afstand tot stoplijn
     *@param config.pointId het id van het bijhorende punt
     *@param config.signalGroupNumber het signaal group nummer waar dit object bij hoort
     *@param config.virtualLocalLoopNumber 
     *@param config.triggerType type trigger
     *@param config.vehicleTypes typen voertuigen waar dit punt op moet reageren.
     */
    constructor: function(config) {      
        if(!config.triggerType) {
            config.triggerType = 'STANDARD';
        }
        if(!config.id) {
            config.id = Ext.id();
        }
        this.initConfig(config);    
    },
    /**
     * Geeft een JSON object terug van dit object
     * @return een JSON object dat dit object representeert.
     */
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
/**
 * Class voor de movement
 */
Ext.define('Movement', {
    config:{
        id: null,
        maps:null,
        nummer:null
    },
    /**
     * @constructor
     * @param config.id het id van de movement
     * @param config.maps een array van movement activatie punten
     * @param config.nummer nummer van de movement
     */
    constructor: function(config) {        
        this.initConfig(config);    
    },
    /**
     * voeg een Movement Activatie Punt toe.
     * @param map het Movement Activatie Punt dat toegevoegd moet worden.
     */
    addMap : function (map){
        if(!this.maps){
            this.maps = new Array();
        }
        this.maps.push(map);
    },
    /**
     * Geeft een JSON object van dit object terug
     * @return een JSON object dat dit object representeert
     */
    toJSON: function() {
        var j = objectSubset(this, ["id", "nummer"]);
        
        j.maps = [];
        Ext.Array.each(this.maps, function(map) {
            j.maps.push(map.toJSON());
        });
        return j;
    }
});
/**
 * Maak een Roadside Equipment object op basis van een JSON object
 * @param json het json object
 * @return een RSEQ object.
 */
function makeRseq (json){
    var rseq = Ext.create("RSEQ",json);
    var movements = makeMovements(json.movements);
    var points = makePoints(json.points);
    rseq.setMovements(movements);
    rseq.setPoints(points);
    return rseq;
}
/**
 * Maak movements objecten op basis van een JSON array
 * @param json de json array
 * @return een array van movement objecten.
 */
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
/**
 * Maak Movement Activatie Punt objecten op basis van een JSON array
 * @param json de json array
 * @return een array van Movement Activatie Punten
 */
function makeMAPs(json){
    var maps = new Array();
    for(var i = 0 ; i < json.length ; i++){
        var map = Ext.create("MovementActivationPoint",json[i]);
        maps.push(map);
    }
    return maps;
}

/**
 * Maak een Movement object op basis van een JSON object
 * @param json het json object
 * @return een movement object.
 */
function makeMovement(json){
    return Ext.create("Movement",json);
}

/**
 * Maak een array van Point objecten op basis van een JSON array
 * @param json de json array
 * @return een array van Points.
 */
function makePoints(json){
    var points = new Array();
    for(var i = 0 ; i < json.length ; i++){
        var point = Ext.create("Point",json[i]);
        points.push(point);
    }
    return points;
}