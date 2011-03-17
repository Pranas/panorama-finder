package gis.panorama;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import java.io.IOException;
import org.geotools.data.DataUtilities;
import org.geotools.data.FeatureSource;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.geotools.feature.DefaultFeatureCollections;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.SchemaException;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.filter.function.StaticGeometry;
import org.opengis.feature.Feature;
import org.opengis.feature.FeatureVisitor;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;

public class LakeVisitor implements FeatureVisitor {

    private FeatureCollection<SimpleFeatureType, SimpleFeature> intersectionPoints;
    private SimpleFeatureType intersectionPointType;
    private FeatureSource riversFeatureSource;
    private GeometryFactory gf = new GeometryFactory();
    private FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(GeoTools.getDefaultHints());

    public LakeVisitor(FeatureSource rivers) {
        riversFeatureSource = rivers;
        
        intersectionPoints = DefaultFeatureCollections.newCollection();
        
        try {
            intersectionPointType = DataUtilities.createType("LakeRiverIntersectionPoints", "location:Point,");
        } catch (SchemaException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public void visit(Feature feature) {
        SimpleFeature lake = (SimpleFeature) feature;
        
        Geometry lakeGeometry = (Geometry) lake.getDefaultGeometry();
        
        String geometryPropertyName = riversFeatureSource.getSchema().getGeometryDescriptor().getLocalName();
        Filter filter = ff.intersects(ff.property(geometryPropertyName), ff.literal(lakeGeometry));
        FeatureCollection riversUnderLakes = null;
        try {
            riversUnderLakes = riversFeatureSource.getFeatures(filter);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
        FeatureIterator<SimpleFeature> rivers = riversUnderLakes.features();
        
        while (rivers.hasNext()) {
            SimpleFeature river = rivers.next();
            Geometry geom = StaticGeometry.intersection((Geometry) river.getDefaultGeometry(), lakeGeometry);

            Coordinate tempC = StaticGeometry.startPoint(geom).getCoordinate();
            SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(intersectionPointType);
            featureBuilder.add(gf.createPoint(tempC));
            intersectionPoints.add( featureBuilder.buildFeature("Point." + (intersectionPoints.size() + 1)) );

            double tempX = tempC.x;
            double tempY = tempC.y;

            tempC = StaticGeometry.endPoint(geom).getCoordinate();

            if ((tempX != tempC.x) || (tempY != tempC.y)) {
                featureBuilder = new SimpleFeatureBuilder(intersectionPointType);
                featureBuilder.add(gf.createPoint(tempC));
                intersectionPoints.add( featureBuilder.buildFeature("Point." + (intersectionPoints.size() + 1)) );
            }
        }

    }
    public FeatureCollection<SimpleFeatureType, SimpleFeature> getIntersectionPoints() {
        return intersectionPoints;
    }
}
