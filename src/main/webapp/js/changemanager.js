/* global Ext */

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

Ext.define("ChangeManager",{
    changeDetected : false,
    editor : null,
    constructor : function (editor){
        this.editor = editor;
        this.editor.on("activeRseqUpdated",this.changeOccured,this);
        
        
        var me = this;
        window.onbeforeunload = function(e) {
            if(me.editor.activeRseq != null && me.changeDetected) {
                // TODO alleen indien er niet opgeslagen wijzigingen zijn
                return "Weet u zeker dat u deze applicatie wilt verlaten? Wijzigingen aan '" + me.editor.activeRseq.description + "' worden niet opgeslagen.";
            }
            return undefined;
        };
        

    },
    changeOccured : function (){
        this.changeDetected = true;
        this.editTitle();
        if(!this.editor.activeRseq.readyIsSet){
            this.editor.activeRseq.readyForExport = false;
        }
    },
    rseqSaved : function (){
        this.changeDetected = false;
        this.editTitle();
    },
    rseqChanging : function (oldId,continueFunction){
        if (this.editor.activeRseq == null || this.editor.activeRseq.id == oldId){
            continueFunction();
        } else if (this.changeDetected){
            Ext.Msg.show({
                title:"Er zijn niet opgeslagen veranderingen",
                msg: "Er zijn niet opgeslagen veranderingen. Wilt u deze weggooien?",
                fn: function (button){
                    if (button === 'yes'){
                        this.changeDetected = false;
                        this.editor.restorePreviousRseq();
                        this.editTitle();
                        continueFunction();
                    } else{
                        this.editor.olc.selectFeature(oldId,"RSEQ");
                    }
                },
                scope:this,
                buttons: Ext.Msg.YESNO,
                buttonText: {
                    no: "Nee",
                    yes: "Ja"
                },
                icon: Ext.Msg.WARNING
                
            });
                    
        } else{
            continueFunction();
        }
        this.editTitle();
    },
    editTitle : function(){
        var title = Ext.getDoc().dom.title;
        var asteriksIndex = title.indexOf("*");
        if(this.changeDetected){
            if(asteriksIndex < 0){
                Ext.getDoc().dom.title += "*";
                Ext.get("context_vri").dom.innerHTML += "*";
            }
        }else{
            if(asteriksIndex >= 0){
                title = title.substring(0,asteriksIndex);
                Ext.getDoc().dom.title = title;
            }
            var overviewTitle = Ext.get("context_vri").dom.innerHTML;
            if(overviewTitle.indexOf("*") !== -1){
                overviewTitle = overviewTitle.substring(0,overviewTitle.indexOf("*"));
            }
            Ext.get("context_vri").setHtml(overviewTitle);
        }
    }
});