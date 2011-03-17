package gis.panorama;

import org.geotools.data.DataUtilities;
import org.geotools.feature.DefaultFeatureCollections;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.SchemaException;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.graph.build.feature.FeatureGraphGenerator;
import org.geotools.graph.build.line.LineStringGraphGenerator;
import org.geotools.graph.structure.Graph;
import org.geotools.graph.structure.Node;
import org.opengis.feature.Feature;
import org.opengis.feature.FeatureVisitor;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

public class RiverVisitor implements FeatureVisitor {

    private FeatureCollection<SimpleFeatureType, SimpleFeature> intersectionPoints;
    private FeatureGraphGenerator graphGenerator;
    private SimpleFeatureType intersectionPointType;
    
    public RiverVisitor() {
        LineStringGraphGenerator lineStringGenerator = new LineStringGraphGenerator();
        graphGenerator = new FeatureGraphGenerator(lineStringGenerator);
        intersectionPoints = DefaultFeatureCollections.newCollection();
        try {
            intersectionPointType = DataUtilities.createType("RiverIntersectionPoints", "location:Point,");
        } catch (SchemaException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public void visit(Feature feature) {
        graphGenerator.add(feature);
    }
    
    public FeatureCollection<SimpleFeatureType, SimpleFeature> getIntersectionPoints() {
        Graph graph = graphGenerator.getGraph();
        SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(intersectionPointType);

        for (Object n : graph.getNodes()) {
            Node node = (Node) n;
            // search for nodes with > 2 edges
            if (node.getEdges().size() > 2) {
                featureBuilder.add(node.getObject());
                intersectionPoints.add(featureBuilder.buildFeature("Point." + (intersectionPoints.size() + 1)));
            }
        }
        
        return intersectionPoints;
    }
    
}
