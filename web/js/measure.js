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
 * Een meettool die toelaat om de getekende geometriën aan te passen
 */
Ext.define("Measure", {
    mixins: {
        observable: 'Ext.util.Observable'
    },
    measure:null,
    map:null,
    vectorLayer:null,
    button:null,
    line:null,
    modify:null,
    lastLength:null,
    constructor : function(options){
        this.mixins.observable.constructor.call(this);  
        
        
        this.addEvents('measureChanged');
        this.map = options.map;
        var panel = options.panel;
        var styleMap = new OpenLayers.StyleMap (
        {
            "default" :{
                strokeColor: "#99BCE8",
                fillColor: "#99BCE8",
                strokeLinecap: "butt",
                strokeDashstyle: "longdash",
                'pointRadius': 6
        
            },
            "select":{
                strokeColor: "#99BCE8",
                strokeLinecap: "butt",
                fillColor: "#99BCE8",
                strokeDashstyle: "longdash",
                'pointRadius': 6
            },
            "temporary":{
                strokeColor: "#99BCE8",
                fillColor: "#99BCE8",
                strokeLinecap: "butt",
                strokeDashstyle: "longdash",
                'pointRadius': 6
            }
        });
        this.vectorLayer = new OpenLayers.Layer.Vector("tempMeasure",{
            styleMap: styleMap
        });
        this.map.addLayer(this.vectorLayer);
        var measureOptions = {
            callbacks:{
                modify: this.modify
            },
            persist:true
        };
        this.measure = new OpenLayers.Control.Measure( OpenLayers.Handler.Path, measureOptions);
        this.map.addControl(this.measure);
        
        this.button= new OpenLayers.Control( {
            displayClass: "olControlMeasure",
            type: OpenLayers.Control.TYPE_TOOL,
            title: "Meten"
        });
        this.button.events.register("activate",this,function(){
            this.activate();
        });
        this.button.events.register("deactivate",this,function(){
            this.deactivate();
        });
        panel.addControls(this.button);
        
        var me =this;
        this.line = new OpenLayers.Control.DrawFeature(this.vectorLayer, OpenLayers.Handler.Path, {
            displayClass: 'olControlDrawFeaturePath',
            callbacks:{
                modify : function(evt){
                    me.featureModified(evt);
                }
            }
        });
        this.line.events.register('featureadded', this, this.featureFinished);
        this.map.addControl(this.line);
        
        this.modify = new OpenLayers.Control.ModifyFeature(this.vectorLayer,{standAlone:false, clickout:true, toggle:true});

        this.vectorLayer.events.register('featuremodified',this, this.featureModified);
        
        this.map.addControl(this.modify);
        this.map.events.register('addlayer', this, this.resetLayerIndex);
    },
    activate : function(){
        this.line.activate();
    },
    deactivate : function(){
        this.line.deactivate();
        this.vectorLayer.removeAllFeatures();
        this.modify.deactivate();
        editor.changeCurrentEditAction(null);
    },
    featureFinished: function(evt){
        var feature = evt.feature;
        this.modify.activate();
        this.modify.selectFeature(feature);
        this.line.deactivate();
    },
    
    featureModified : function (evt){
        var geom = null;
        if (evt.parent){
            geom = evt.parent;
        }
        if(evt.feature){
            geom = evt.feature.geometry;
        }
        
        if(geom){
            var bestlengthtokens = this.measure.getBestLength(geom);         
            this.lastLength = bestlengthtokens;
            this.fireEvent('measureChanged', bestlengthtokens[0].toFixed(0), bestlengthtokens[1]);
        }
    },
    resetLayerIndex : function(evt){
        this.map.setLayerIndex(this.vectorLayer, this.map.getLayerIndex(evt.layer)+3);
    }
});
