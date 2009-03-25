/*
B3P Gisviewer is an extension to Flamingo MapComponents making      
it a complete webbased GIS viewer and configuration tool that    
works in cooperation with B3P Kaartenbalie.  
                    
Copyright 2006, 2007, 2008 B3Partners BV

This file is part of B3P Gisviewer.

B3P Gisviewer is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

B3P Gisviewer is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with B3P Gisviewer.  If not, see <http://www.gnu.org/licenses/>.
*/
function FMCController(fmcObject,name){
  this.queues = new Array();  
  this.fmc=fmcObject;  
  this.name=name;
  this.busy=false;  
}
/*Use this function to call a flamingo function with javascript.
 **/
FMCController.prototype.callCommand =function (fmcCall){
    if (typeof this.fmc.callMethod == 'function' && this.fmc.callMethod(this.fmc.id,'exists',fmcCall.id)==true){
        if (fmcCall.params.length==0){
            eval("setTimeout(\"flamingo.callMethod('"+fmcCall.id+"','"+fmcCall.method+"')\",10);");
        }else{
          var value=""
          for (var i=0; i < fmcCall.params.length; i++){              
              value+=",";
              var valueType=typeof(fmcCall.params[i]);
              if (valueType == 'boolean' || valueType == 'number' || valueType == 'array'){
                  value+=fmcCall.params[i];
              }else{
                value+="'"+fmcCall.params[i]+"'";
              }
          }          
          eval("setTimeout(\"flamingo.callMethod('"+fmcCall.id+"','"+fmcCall.method+"'"+value+")\",10);");
        }        
    }else{
        this.addToQueue(fmcCall);
    }
}
/*This function adds a call to the queue. It is used when a component not (yet) is loaded
 **/
FMCController.prototype.addToQueue = function(fmcCall){
    if (this.queues[fmcCall.id]==undefined || this.queues[fmcCall.id]==null){
        this.queues[fmcCall.id]= new Array();  
        eval(""+this.fmc.id+"_"+fmcCall.id+"_onInit = function(){"+this.name+".executeQueue('"+fmcCall.id+"');};");        
    }
    this.queues[fmcCall.id].push(fmcCall);
}
/*Executes the queue of a given component id.
 **/
FMCController.prototype.executeQueue = function(id){
    if (this.queues[id]==undefined || this.queues[id]==null || this.queues[id].length==0){
        return;
    }
    while (this.queues[id].length!=0){        
        var flamingoCall=this.queues[id].shift();        
        this.callCommand(flamingoCall);        
    }
}

/*Class FlamingoCall
 *Used to store the method call
 **/
function FlamingoCall(id,method,params){
    this.id = id;
    this.method = method;
    if (params==undefined || params==null){
        this.params=new Array();
    }else if (typeOf(params) == 'array'){
        this.params=params;
    }else {
        this.params=new Array();
        this.params.push(params);
    }
}
/*Returns the type of a object.
 **/
function typeOf(value) {
    var s = typeof value;
    if (s === 'object') {
        if (value) {
            if (value instanceof Array) {
                s = 'array';
            }
        } else {
            s = 'null';
        }
    }
    return s;
}
