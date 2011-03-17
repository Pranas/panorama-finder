package gis.actions;

import java.awt.event.ActionEvent;
import java.io.File;
import javax.swing.Action;
import javax.swing.ImageIcon;
import org.geotools.data.CachingFeatureSource;
import org.geotools.data.FeatureSource;
import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.swing.JMapPane;
import org.geotools.swing.action.MapAction;
import org.geotools.swing.data.JFileDataStoreChooser;

public class NewLayerAction extends MapAction {
    public static final String TOOL_NAME = "New Layer";
    public static final String TOOL_TIP = "Add new layer";
    public static final String ICON_IMAGE = "src/main/resources/new_layer.png";

    public NewLayerAction(JMapPane mapPane) {
        this(mapPane, false);
    }

    public NewLayerAction(JMapPane mapPane, boolean showToolName) {
        String toolName = showToolName ? TOOL_NAME : null;
        super.init(mapPane, toolName, TOOL_TIP, null);
        this.putValue(Action.SMALL_ICON, new ImageIcon(ICON_IMAGE));
    }

    public void loadLayer(File f) {
        FileDataStore store;
        try {
            store = FileDataStoreFinder.getDataStore(f);
            FeatureSource featureSource = store.getFeatureSource();
//            CachingFeatureSource cache = new CachingFeatureSource(featureSource);
            getMapPane().getMapContext().addLayer(featureSource, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        File file = JFileDataStoreChooser.showOpenFile("shp", null);
        if (file == null) {
            return;
        } else {
            loadLayer(file);
        }
    }

}