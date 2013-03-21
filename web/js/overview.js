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
 * Class for representing the currently selected trafficsystem
 * 
 * 
 */
Ext.define("nl.b3p.kar.Overview",{
    editor : null,
    domId : null,
    tree : null,
    constructor : function (editor,domId){
        this.editor = editor;
        this.domId = domId;
        this.editor.on("activeRseqUpdated",this.updateOverview,this);
        this.editor.on("activeRseqChanged",this.updateOverview,this);
        this.editor.on('selectedObjectChanged',this.updateSelection,this);
        var panel = viewport.items.items[3].items.items[1];
        panel.on('collapse',this.updateSize,this);
        panel.on('expand',this.updateSize,this);
        panel.on('resize',this.updateSize,this);
        this.editor.olc.highlight.events.register('featurehighlighted',this,function (evt){
            if (this.isPoint(evt.feature)){
                this.highlight(evt.feature.data.id);
            }
        });
        this.editor.olc.highlight.events.register('featureunhighlighted',this,function (evt){
            if (this.isPoint(evt.feature)){
                this.unhighlight(evt.feature.data.id);
            }
        });
    },
    updateOverview : function (rseq){
        Ext.get("context_vri").setHTML(rseq == null ? "" :
                (rseq.description + " (" + rseq.karAddress + ")"));
        Ext.get("rseqOptions").setVisible(rseq != null);
        var memoIcon = Ext.get("memo_vri");
        if (rseq.memo && rseq.memo != ""){
            memoIcon.setVisible(true);
        } else{
            memoIcon.setVisible(false);
        }


        var overzicht = Ext.get("overzicht");
        var tree = Ext.get("tree");
        if (tree){
            tree.remove();
        }
        var root = this.createRootNode(rseq.getOverviewJSON());
        var store = Ext.create('Ext.data.TreeStore',root);
        this.tree = Ext.create('Ext.tree.Panel',{
            border : false,
            width : "100%",
            header : false,
            id : "tree",
            selModel : {
                mode : "MULTI"
            },
            height : "60%",
            store : store,
            rootVisible : false,
            renderTo : overzicht,
            listeners : {
                scope : this,
                select : function (tree,record){
                    if (record.raw.type == "point"){
                        var id = record.raw.pointId;
                        var vectorLayer = this.editor.olc.vectorLayer;
                        var features = vectorLayer.getFeaturesByAttribute("id",id);
                        if (features != null && features.length > 0){
                            var feature = features[0];
                            this.editor.setSelectedObject(feature);
                        }
                    }
                },
                itemmouseenter : function (tree,record){
                    if (record.raw.type == "point"){
                        var id = record.raw.pointId;
                        var vectorLayer = this.editor.olc.vectorLayer;
                        var features = vectorLayer.getFeaturesByAttribute("id",id);
                        if (features != null && features.length > 0){
                            var feat = features[0];
                            if (feat.renderIntent != "select"){
                                feat.renderIntent = "temporary";
                                vectorLayer.redraw();
                            }
                        }
                    }
                },
                itemmouseleave : function (tree,record){
                    if (record.raw.type == "point"){
                        var id = record.raw.pointId;
                        var currentSelectedObject = this.editor.selectedObject;
                        if (currentSelectedObject == null || currentSelectedObject.getId() != id){
                            var vectorLayer = this.editor.olc.vectorLayer;
                            var features = vectorLayer.getFeaturesByAttribute("id",id);
                            if (features != null && features.length > 0){
                                var feat = features[0];
                                feat.renderIntent = "default";
                                vectorLayer.redraw();
                            }
                        }
                    }
                },
                itemcontextmenu : function (view,record,item,index,event,eOpts){
                    this.editor.contextMenu.show(event.xy[0], event.xy[1],false);
                }
            }
        });
        this.editor.helpPanel.updateHelpPanel();
    },
    updateSelection : function (point){
        if (this.tree != null){
            var sm = this.tree.getSelectionModel();
            if (point != null && point instanceof Point){
                this.unhighlight(point.getId());
                var root = this.tree.getRootNode();
                var nodes = new Array();
                this.findChildrenByPointId(root,point.getId(),nodes);
                sm.select(nodes,false,true);
            } else{
                sm.deselectAll();
            }
        }
    },
    highlight : function (id){
        if (this.tree != null){
            var view = this.tree.getView();
            var root = this.tree.getRootNode();
            var nodes = new Array();
            this.findChildrenByPointId(root,id,nodes);
            for (var i = 0;i < nodes.length;i++){
                var myNode = this.tree.getStore().getNodeById(nodes[i].internalId);
                var nodeElement = view.getNode(myNode);
                Ext.get(nodeElement).addCls(view.overItemCls);
            }
        }
    },
    unhighlight : function (id){
        if (this.tree != null){
            var view = this.tree.getView();
            var root = this.tree.getRootNode();
            var nodes = new Array();
            this.findChildrenByPointId(root,id,nodes);
            for (var i = 0;i < nodes.length;i++){
                var myNode = this.tree.getStore().getNodeById(nodes[i].internalId);
                var nodeElement = view.getNode(myNode);
                Ext.get(nodeElement).removeCls(view.overItemCls);
            }
        }
    },
    createRootNode : function (json){
        var store = {
            root : {
                expanded : true,
                children : []
            }
        };
        for (var key in json){
            var signalGroup = json[key];
            var bewegingen = new Array();
            for (var bKey in signalGroup){
                var mv = signalGroup[bKey];
                var mvNode = this.createMovementNode(mv,bKey);
                bewegingen.push(mvNode);
            }
            var node = {
                text : "Signaalgroep " + key,
                id : Ext.id(),//"sign-" + key,
                expanded : true,
                iconCls : "noTreeIcon",
                type : "signalGroup",
                children : bewegingen
            };
            store.root.children.push(node);
        }

        return store;
    },
    createMovementNode : function (json,key){
        var points = new Array();
        for (var i = 0;i < json.points.length;i++){
            var pt = this.createPointNode(json.points[i]);
            points.push(pt);
        }
        var label = this.getBewegingLabel(this.editor.activeRseq.getMovementById(json.id));
        var node = {
            text : "Beweging " + key + ": " + label,
            id : Ext.id(),//"mvmt-" + json.id,
            expanded : true,
            icon : karTheme.richting,
            iconCls : 'overviewTree',
            type : "movement",
            children : points
        };
        return node;

    },
    createPointNode : function (point){
        var node = {
            text : point.getLabel(),
            id : Ext.id(),//"point-" + point.getId(),
            leaf : true,
            pointId : point.getId(),
            type : "point",
            iconCls : 'overviewTree',
            icon : this.getIconForPoint(point)
        };
        return node;
    },
    getIconForPoint : function (point){
        switch (point.getType()){
            case "ACTIVATION_1" :
                return karTheme.inmeldPunt;
                break;
            case "ACTIVATION_2" :
                return karTheme.uitmeldPunt;
                break;
            case "ACTIVATION_3" :
                return karTheme.voorinmeldPunt;
                break;
            case "BEGIN" :
                return karTheme.startPunt;
                break;
            case "END" :
                return karTheme.eindPunt;
                break;
        }

    },
    getBewegingLabel : function (mvmnt){
        var begin = null;
        var eind = null;
        for (var i = 0;i < mvmnt.maps.length;i++){
            var map = mvmnt.maps[i];
            var point = this.editor.activeRseq.getPointById(map.pointId);
            if (point.getType() == "END"){
                eind = this.getLabelFromPoint(point);
            }

            if (point.getType() == "ACTIVATION_1"){
                begin = this.getLabelFromPoint(point);
            }
            if (begin == null){
                if (point.getType() == "ACTIVATION_2"){
                    begin = this.getLabelFromPoint(point);
                }
            }
        }

        var label = "Van " + begin + " naar " + eind;
        return label;
    },
    getLabelFromPoint : function (point){
        var label = "";
        if (point.getLabel() != ""){
            label = point.getLabel();
        } else{
            label = point.getId();
        }
        return label;
    },
    updateSize : function (){
        if (this.tree != null){
            this.tree.update();
        }
    },
    findChildrenByPointId : function (record,pointId,result){
        var me = this;
        record.eachChild(function (child){
            if (child.raw.pointId == pointId){
                result.push(child);
            }
            if (child.hasChildNodes()){
                me.findChildrenByPointId(child,pointId,result);
            }
        });
    },
    isPoint : function (object){
        if ((object.cluster == null || object.cluster == undefined) && object.data.type != "CROSSING" && object.data.type != "GUARD" && object.data.type != "BAR"){
            return true;
        } else{
            return false;
        }
    }
});