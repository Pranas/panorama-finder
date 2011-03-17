package gis.panorama;

import com.vividsolutions.jts.algorithm.distance.EuclideanDistanceToPoint;
import com.vividsolutions.jts.algorithm.distance.PointPairDistance;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
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
import org.opengis.feature.Feature;
import org.opengis.feature.FeatureVisitor;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.FilterFactory2;


public class InterestingPointVisitor implements FeatureVisitor {
    
    private FeatureCollection<SimpleFeatureType, SimpleFeature> viewAngles;
    private SimpleFeatureType viewAngleType;
    private FeatureSource surfaceFeatureSource;
    
    private double radius;
    private double hillHeight;
    
    private GeometryFactory gf = new GeometryFactory();
    private FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(GeoTools.getDefaultHints());

    public InterestingPointVisitor(FeatureSource surface, double radius, double hillHeight) {
        surfaceFeatureSource = surface;
        this.radius = radius;
        this.hillHeight = hillHeight;
        
        viewAngles = DefaultFeatureCollections.newCollection();
        
        try {
            viewAngleType = DataUtilities.createType("ViewAngles", "geometry:Polygon,daukstis:Double,");
        } catch (SchemaException e) {
            e.printStackTrace();
        }
    }
    
    public FeatureCollection<SimpleFeatureType, SimpleFeature> getViewAngles() {
        return viewAngles;
    }
    
    @Override
    public void visit(Feature feature) {
        try {
            System.out.println("****************");
            SimpleFeature sf = (SimpleFeature) feature;

            double height = getHeight(sf);

            System.out.println("Intersection at height " + height);

            FeatureCollection polygonsInRadius = Finder.featuresInRadius(surfaceFeatureSource, sf, radius);
            FeatureIterator<SimpleFeature> polygons = polygonsInRadius.features();

            SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(viewAngleType);

            while(polygons.hasNext()) {
                SimpleFeature poly = polygons.next();
                double poly_height = (Double) poly.getAttribute("Aukstis");
                if (poly_height > (height + hillHeight)) {
                    Point p = (Point) sf.getDefaultGeometry();

                    PointPairDistance ppd = new PointPairDistance(); 
                    EuclideanDistanceToPoint.computeDistance((Geometry) poly.getDefaultGeometry(), p.getCoordinate(), ppd);

                    System.out.println("View height: " + poly_height + " at distance " + ppd.getDistance());

                    Coordinate a = p.getCoordinate();
                    Coordinate b = ppd.getCoordinates()[0];

                    double l = 50;
                    double C = ppd.getDistance();

                    Coordinate temp = b;

                    b = a;
                    a = temp;

                    // http://answers.google.com/answers/threadview/id/419874.html

                    Coordinate c = new Coordinate(b.x + (l * (a.y - b.y)) / C, b.y + (l * (b.x - a.x)) / C);
                    Coordinate d = new Coordinate(b.x + ((-1) * l * (a.y - b.y)) / C, b.y + ((-1) * l * (b.x - a.x)) / C);

                    Coordinate[] cords = { a, c, d, a };

                    LinearRing ring = gf.createLinearRing(cords);
                    Polygon pol = gf.createPolygon(ring, null);
                    featureBuilder.add(pol);
                    featureBuilder.add(poly_height - height);
                    SimpleFeature viewAngle = featureBuilder.buildFeature("Polygon." + (viewAngles.size() + 1));
                    viewAngles.add(viewAngle);
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    public double getHeight(SimpleFeature feature) throws IOException {
        FeatureCollection surfacePolygonCollection = Finder.featuresInGeometry((Geometry) feature.getDefaultGeometry(), surfaceFeatureSource);
        SimpleFeature sf = (SimpleFeature) surfacePolygonCollection.features().next();
        return (Double) sf.getAttribute("Aukstis");
    }
    
    
}
