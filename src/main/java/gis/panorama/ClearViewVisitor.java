package gis.panorama;

import com.vividsolutions.jts.geom.Geometry;
import java.util.HashSet;
import java.util.Set;
import org.geotools.data.FeatureSource;
import org.geotools.feature.FeatureCollection;
import org.opengis.feature.Feature;
import org.opengis.feature.FeatureVisitor;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.filter.identity.FeatureId;

public class ClearViewVisitor implements FeatureVisitor {
    private FeatureSource featureSource;
    private Set<FeatureId> selectedFeatures;
    
    public ClearViewVisitor(FeatureSource featureSource) {
        this.featureSource = featureSource;
        
        this.selectedFeatures = new HashSet<FeatureId>();
    }

    @Override
    public void visit(Feature feature) {
        try {
            SimpleFeature sf = (SimpleFeature) feature;
            FeatureCollection inSight = Finder.featuresInGeometry((Geometry) sf.getDefaultGeometry(), featureSource);
            if(inSight.isEmpty()) {
                System.out.println(sf.getIdentifier());
                selectedFeatures.add(sf.getIdentifier());
            }
        } catch (Exception e) { e.printStackTrace(); }
    }
    
    public Set<FeatureId> getClearViewAngles() {
        return selectedFeatures;
    }
    
}
