package gis;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.geotools.feature.FeatureCollection;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.DefaultMapContext;
import org.geotools.map.MapLayer;
import org.geotools.map.event.MapLayerListEvent;
import org.geotools.map.event.MapLayerListListener;
import org.geotools.styling.Rule;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.identity.FeatureId;

public class MyMapContext extends DefaultMapContext {

    private FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(GeoTools.getDefaultHints());

    HashMap<MapLayer, Set<FeatureId>> selectedFeatures = new HashMap();
    private ArrayList<FeatureDetailFrame> featureDetailFrames = new ArrayList<FeatureDetailFrame>();

    MyMapContext() {
        super();
        addMapLayerListListener(new MapLayerListListener() {
            @Override
            public void layerAdded(MapLayerListEvent event) {
                MapLayer layer = event.getLayer();
                selectedFeatures.put(layer, new HashSet<FeatureId>());
                layer.setStyle(MyStyleGenerator.createStyleFor(layer));
            }

            @Override
            public void layerRemoved(MapLayerListEvent event) {
                selectedFeatures.remove(event.getLayer());
            }

            @Override
            public void layerChanged(MapLayerListEvent event) {
                return;
            }

            @Override
            public void layerMoved(MapLayerListEvent event) {
                return;
            }
        });
    }
    
    public void addFeatureDetailFrame(FeatureDetailFrame frame) {
    	featureDetailFrames.add(frame);
    }
    
    public void removeFeatureDetailFrame(FeatureDetailFrame frame) {
    	featureDetailFrames.remove(frame);
    }

    public final void resetSelection() {
        Collection c = selectedFeatures.values();
        Iterator itr = c.iterator();

        while(itr.hasNext()) ((HashSet) itr.next()).clear();

        resetStyles();
    }

    public void selectFeatures(MapLayer layer, Set<FeatureId> IDs) {
        selectedFeatures.put(layer, IDs);
    }
    
    public void fireSelectionChanged() {
    	for (FeatureDetailFrame frame : featureDetailFrames) {
    		frame.selectSelectedFeatures();
    	}
    }

    public void expandSelection(MapLayer layer, Set<FeatureId> IDs) {
        Set<FeatureId> selected = selectedFeatures.get(layer);
        Set<FeatureId> intersection = new HashSet<FeatureId>(selected);
        intersection.retainAll(IDs);

        selected.addAll(IDs);
        selected.removeAll(intersection);
    }

    public ReferencedEnvelope selectionArea() {
        ReferencedEnvelope area = null;

        try {
            for (MapLayer layer : getLayers()) {
                Filter filter = ff.id(selectedFeatures.get(layer));
                FeatureCollection selectedInLayer = layer.getFeatureSource().getFeatures(filter);
                if(area == null)
                    area = selectedInLayer.getBounds();
                else {
                    area.expandToInclude(selectedInLayer.getBounds());
                }
            }
        } catch(Exception e) { e.printStackTrace(); }

        return area;
    }

    public void resetStyles() {
        for (MapLayer layer : getLayers()) {
            Rule selected_rule = MyStyleGenerator.createRule(layer, Color.red, Color.pink, 2.0f);
            selected_rule.setFilter(ff.id(selectedFeatures.get(layer)));

            if(layer.getStyle().featureTypeStyles().get(0).rules().size() == 1) {
                layer.getStyle().featureTypeStyles().get(0).rules().add(selected_rule);
            } else {
                layer.getStyle().featureTypeStyles().get(0).rules().set(1, selected_rule);
            }
        }
    }

    public HashMap<MapLayer, Set<FeatureId>> getSelectedFeatures() {
            return selectedFeatures;
    }
}
