<?xml version="1.0" encoding="ISO-8859-1"?>
<StyledLayerDescriptor version="1.0.0" 
                       xsi:schemaLocation="http://www.opengis.net/sld StyledLayerDescriptor.xsd" 
                       xmlns="http://www.opengis.net/sld" 
                       xmlns:ogc="http://www.opengis.net/ogc" 
                       xmlns:xlink="http://www.w3.org/1999/xlink" 
                       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
  <NamedLayer>
    <Name>Buslijnen</Name>
    <UserStyle>
      <FeatureTypeStyle>
        <Rule>
          <MinScaleDenominator>0</MinScaleDenominator>
          <MaxScaleDenominator>10000</MaxScaleDenominator>
          <TextSymbolizer>
            <VendorOption name="followLine">true</VendorOption>
            <VendorOption name="maxAngleDelta">90</VendorOption>
            <VendorOption name="maxDisplacement">20</VendorOption>
            <VendorOption name="repeat">450</VendorOption>
            <VendorOption name="forceLeftToRight">false</VendorOption>
            <Label>
              <ogc:Literal>></ogc:Literal>
            </Label>
            <Font>
              <CssParameter name="font-family">Lucida Sans</CssParameter>
              <CssParameter name="font-size">18</CssParameter>
            </Font>
            <Fill>
              <CssParameter name="fill">#ff0000</CssParameter>
            </Fill>
          </TextSymbolizer>
          <TextSymbolizer>
            <VendorOption name="repeat">620</VendorOption>
            <VendorOption name="group">yes</VendorOption>
            <VendorOption name="followLine">true</VendorOption>
            <VendorOption name="maxDisplacement">120</VendorOption>
            <VendorOption name="maxAngleDelta">90</VendorOption>
            <VendorOption name="spaceAround">110</VendorOption>
            <VendorOption name="forceLeftToRight">true</VendorOption>

            <Label>
              <ogc:PropertyName>linepublicnumber</ogc:PropertyName>
            </Label>
            <LabelPlacement>
              <LinePlacement>
                <PerpendicularOffset>
                  10
                </PerpendicularOffset>
              </LinePlacement>
            </LabelPlacement>
            <Font>
              <CssParameter name="font-family">Lucida Sans</CssParameter>
              <CssParameter name="font-size">13</CssParameter>
            </Font>
            <Fill>
              <CssParameter name="fill">#ff0000</CssParameter>
            </Fill>
          </TextSymbolizer>
        </Rule>
        <Rule>
          <LineSymbolizer>
            <Stroke>
              <CssParameter name="stroke">#ff0000</CssParameter>
              <CssParameter name="stroke-width">1</CssParameter>
            </Stroke>
          </LineSymbolizer>
        </Rule>
      </FeatureTypeStyle>
    </UserStyle>
  </NamedLayer>
</StyledLayerDescriptor>