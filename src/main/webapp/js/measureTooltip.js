/**
 * KAR Geo Tool - applicatie voor het registreren van KAR meldpunten
 *
 * Copyright (C) 2009-2018 B3Partners B.V.
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
 * Tooltip voor bij een meettool
 */
Ext.define("MeasureTooltip", {
    constructor : function(measure) {
        this.tooltipId = Ext.id();
        this.measure = measure;
    },
    showMouseTooltip: function(evt, measure, unit) {
        var measureValueDiv = document.getElementById(this.tooltipId);
        var measureValueText = document.getElementById(this.tooltipId + 'Text');
        if (measureValueDiv === null){
            measureValueDiv = document.createElement('div');
            measureValueDiv.id = this.tooltipId;
            measureValueDiv.style.position = 'absolute';
            document.querySelector('.olMap').appendChild(measureValueDiv);
            measureValueDiv.style.zIndex = "10000";
            measureValueDiv.className = "olControlMaptip";
            measureValueText = document.createElement('div');
            measureValueText.id = this.tooltipId + 'Text';
            measureValueDiv.appendChild(measureValueText);
        }
        var x = evt.x; var y = evt.y;
        if(!x && !y && evt.feature && evt.feature.geometry && evt.feature.geometry.components) {
            var lastComponent = evt.feature.geometry.components[evt.feature.geometry.components.length - 1];
            x = lastComponent.x;
            y = lastComponent.y;
        }
        var px = this.measure.map.getViewPortPxFromLonLat(new OpenLayers.LonLat(x, y));
        measureValueDiv.style.top = px.y +"px";
        measureValueDiv.style.left = px.x + 10 + 'px';
        measureValueDiv.style.display = "block";
        measureValueDiv.style["pointer-events"] = "none";
        measureValueText.innerHTML= measure + " " + unit;
    },
    hideMouseTooltip: function() {
        var measureValueDiv = document.getElementById(this.tooltipId);
        if(measureValueDiv) {
            measureValueDiv.style.display = 'none';
        }
    }
});