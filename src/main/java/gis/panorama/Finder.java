package gis.panorama;

import com.vividsolutions.jts.geom.Geometry;
import gis.MyMapContext;
import java.io.IOException;
import org.geotools.data.FeatureSource;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.geotools.feature.FeatureCollection;
import org.geotools.filter.text.cql2.CQL;
import org.geotools.filter.text.cql2.CQLException;
import org.geotools.map.DefaultMapLayer;
import org.geotools.map.MapContext;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;

public class Finder {
    private MapContext mapContext;
    
    private FeatureSource townFeatureSource;
    private FeatureSource riverFeatureSource;
    private FeatureSource lakeFeatureSource;
    private FeatureSource surfaceFeatureSource;
    private FeatureSource forestFeatureSource;
    
    private double townRadius = 10000.0d;
    private double intersectionRadius = 1000.0d;
    private double hillHeight = 5.0d;
    
    private String townName;
    
    public Finder(MapContext mapContext, FeatureSource townFeatureSource, FeatureSource riverFeatureSource, FeatureSource lakeFeatureSource, FeatureSource surfaceFeatureSource, FeatureSource forestFeatureSource) {
        this.mapContext = mapContext;
        this.townFeatureSource = townFeatureSource;
        this.riverFeatureSource = riverFeatureSource;
        this.lakeFeatureSource = lakeFeatureSource;
        this.surfaceFeatureSource = surfaceFeatureSource;
        this.forestFeatureSource = forestFeatureSource;
    }
    
    public void setTownRadius(double radius) {
        this.townRadius = radius;
    }
    
    public void setIntersectionRadius(double radius) {
        this.intersectionRadius = radius;
    }

        
    public void setHillHeight(double height) {
        this.hillHeight = height;
    }
    
    public void setTownName(String name) {
        this.townName = name;
    }
    
    public void find() throws Exception {
        System.out.println("Check " + townName + " with radius " + townRadius);
        
        // find town by name attribute
        SimpleFeature town = Finder.featureByAttribute(townFeatureSource, "GYVVARDAS", townName);

        if(town == null) {
            throw new Exception("Can't find '" + townName + "' town");
        }

        // filter features in town radius
        FeatureCollection riversInRadius = featuresInRadius(riverFeatureSource, town, townRadius);
        FeatureCollection lakesInRadius = featuresInRadius(lakeFeatureSource, town, townRadius);

        System.out.println("Found: " + riversInRadius.size() + " rivers, " + lakesInRadius.size() + " lakes");

        // check rivers using graph intersection finder
        RiverVisitor riverVisitor = new RiverVisitor();
        riversInRadius.accepts(riverVisitor, null);
        
        FeatureCollection riverIntersectionPoints = riverVisitor.getIntersectionPoints();
        
        if (!riverIntersectionPoints.isEmpty()) {        
            mapContext.addLayer(riverIntersectionPoints, null);
        }

        // check lake intersection points with rivers
        LakeVisitor lakeVisitor = new LakeVisitor(riverFeatureSource);
        lakesInRadius.accepts(lakeVisitor, null);
        
        FeatureCollection lakeRiverIntersectionPoints = lakeVisitor.getIntersectionPoints();
        
        if (!lakeRiverIntersectionPoints.isEmpty()) {
            mapContext.addLayer(lakeRiverIntersectionPoints, null);
        }
        
        if (lakeRiverIntersectionPoints.isEmpty() && riverIntersectionPoints.isEmpty()) {
            throw new Exception("No interesting points found in given area");
        }
        
        System.out.println("Interesting points: " + (riverIntersectionPoints.size() + lakeRiverIntersectionPoints.size()));
        System.out.println("Check for views: " + hillHeight + " high within " + intersectionRadius);
        
        // check all interesting points and form view angles
        InterestingPointVisitor ipv = new InterestingPointVisitor(surfaceFeatureSource, intersectionRadius, hillHeight);
        riverIntersectionPoints.accepts(ipv, null);
        lakeRiverIntersectionPoints.accepts(ipv, null);
        
        FeatureCollection viewAngles = ipv.getViewAngles();

        if (viewAngles.isEmpty()) {
            throw new Exception("No spots for panorama like you asked");
        }
        
        DefaultMapLayer viewAnglesLayer = new DefaultMapLayer(viewAngles, null);
        mapContext.addLayer(viewAnglesLayer);
        
        // check view angles if view is clear
        System.out.println("****************");
        System.out.println("Clear view angles:");
        
        ClearViewVisitor cvv = new ClearViewVisitor(forestFeatureSource);
        viewAngles.accepts(cvv, null);
        
        ((MyMapContext) mapContext).selectFeatures(viewAnglesLayer, cvv.getClearViewAngles());
        ((MyMapContext) mapContext).resetStyles();
        
        System.out.println("Done");
        
    }

    
    /*
     * Static helper methods
     * 
     */
        
    public static SimpleFeature featureByAttribute(FeatureSource featureSource, String attribute, String value) throws CQLException, IOException {
        Filter filter = CQL.toFilter(attribute + " = '" + value + "'");
        FeatureCollection fc = featureSource.getFeatures(filter);
        
        if(fc.size() > 1) {
            System.out.println("WARNING: More than 1 town found (using first in list)...");
        }
        
        if(fc.size() > 0) {
            return (SimpleFeature) fc.features().next();
        } else {
            return null;
        }
    }

    
    public static FeatureCollection<SimpleFeatureType, SimpleFeature> featuresInGeometry(Geometry geometry, FeatureSource featureSource) throws IOException {
        FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(GeoTools.getDefaultHints());        
        String geometryPropertyName = featureSource.getSchema().getGeometryDescriptor().getLocalName();

        Filter filter = ff.intersects(ff.property(geometryPropertyName), ff.literal(geometry));

        return featureSource.getFeatures(filter);
    }
    
    public static FeatureCollection<SimpleFeatureType, SimpleFeature> featuresInRadius(FeatureSource featureSource, SimpleFeature centerPoint, double radius) throws IOException {
        FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(GeoTools.getDefaultHints());        
        String geometryPropertyName = featureSource.getSchema().getGeometryDescriptor().getLocalName();

        Filter filter = ff.dwithin(ff.property(geometryPropertyName), ff.literal(centerPoint.getDefaultGeometry()), radius, "meters");

        return featureSource.getFeatures(filter);
    }
}
