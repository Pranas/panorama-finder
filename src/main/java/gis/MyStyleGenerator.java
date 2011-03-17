package gis;

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import java.awt.Color;
import java.util.Random;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.geotools.map.MapLayer;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.Fill;
import org.geotools.styling.Graphic;
import org.geotools.styling.Mark;
import org.geotools.styling.Rule;
import org.geotools.styling.Stroke;
import org.geotools.styling.Style;
import org.geotools.styling.StyleFactory;
import org.geotools.styling.Symbolizer;
import org.opengis.feature.type.FeatureType;
import org.opengis.filter.FilterFactory2;


public class MyStyleGenerator {
    private static StyleFactory sf = CommonFactoryFinder.getStyleFactory(null);
    private static FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(GeoTools.getDefaultHints());

    private enum GeomType { POINT, LINE, POLYGON };
    private static final float LINE_WIDTH = 1.0f;
    private static final float POINT_SIZE = 10.0f;

    public static Style createStyleFor(MapLayer layer) {
        Color color = randomColor();

        Rule rule = createRule(layer, color, color, LINE_WIDTH);
        rule.setElseFilter(true);

        FeatureTypeStyle fts = sf.createFeatureTypeStyle();
        fts.rules().add(rule);

        Style style = sf.createStyle();
        style.featureTypeStyles().add(fts);

        return style;
    }



    /**
     * Creates a new Rule containing a Symbolizer tailored to the geometry type
     * of given layer
     */
    public static Rule createRule(MapLayer layer, Color outlineColor, Color fillColor, Float lineWidth) {
        /*
        * usually "THE_GEOM" for shapefiles
        *
        */
        FeatureType schema = layer.getFeatureSource().getSchema();
        String geometryPropertyName = schema.getGeometryDescriptor().getLocalName(); // "THE_GEOM"

        /*
        * Geometry type in current layer
        */

        GeomType geometryType;

        Class<?> clazz = schema.getGeometryDescriptor().getType().getBinding();
        if (Polygon.class.isAssignableFrom(clazz) || MultiPolygon.class.isAssignableFrom(clazz)) {
        geometryType = GeomType.POLYGON;
        } else if (LineString.class.isAssignableFrom(clazz) || MultiLineString.class.isAssignableFrom(clazz)) {
        geometryType = GeomType.LINE;
        } else {
        geometryType = GeomType.POINT;
        }

        Symbolizer symbolizer = null;
        Fill fill = null;
        Stroke stroke = sf.createStroke(ff.literal(outlineColor), ff.literal(lineWidth));

        switch (geometryType) {
            case POLYGON:
                symbolizer = sf.createPolygonSymbolizer(stroke, fill, geometryPropertyName);
                break;
            case LINE:
                symbolizer = sf.createLineSymbolizer(stroke, geometryPropertyName);
                break;
            case POINT:
                Mark mark = sf.getCircleMark();
                mark.setFill(fill);
                mark.setStroke(stroke);

                Graphic graphic = sf.createDefaultGraphic();
                graphic.graphicalSymbols().clear();
                graphic.graphicalSymbols().add(mark);
                graphic.setSize(ff.literal(POINT_SIZE));

                symbolizer = sf.createPointSymbolizer(graphic, geometryPropertyName);
        }

        Rule rule = sf.createRule();
        rule.symbolizers().add(symbolizer);
        return rule;
    }

    private static Color randomColor() {
        Random r = new Random();
        return new Color(r.nextInt(256), r.nextInt(256), r.nextInt(256));
    }
}
