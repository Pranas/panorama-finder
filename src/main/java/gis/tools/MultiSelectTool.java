package gis.tools;

import gis.MyMapContext;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;
import javax.swing.ImageIcon;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.geometry.Envelope2D;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.MapLayer;
import org.geotools.swing.event.MapMouseEvent;
import org.geotools.swing.tool.AbstractZoomTool;
import org.opengis.feature.Feature;
import org.opengis.feature.type.FeatureType;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.identity.FeatureId;


/**
 * Multi select info tool
 *
 * * For mouse clicks, features in 5x5 bounding box will be selected
 *
 * * The tool also responds to the user drawing a box on the map mapPane with
 * mouse click-and-drag to define the multi select area.
 *
 */

public class MultiSelectTool extends AbstractZoomTool {

    private static final ResourceBundle stringRes = ResourceBundle.getBundle("org/geotools/swing/Text");

    public static final String  TOOL_NAME      = stringRes.getString("tool_name_info");
    public static final String  TOOL_TIP       = stringRes.getString("tool_tip_info");
    public static final String  CURSOR_IMAGE   = "/org/geotools/swing/icons/mActionIdentify.png";
    public static final Point   CURSOR_HOTSPOT = new Point(14, 9);
    public static final String  ICON_IMAGE     = "/org/geotools/swing/icons/mActionIdentify.png";
    
    private FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(GeoTools.getDefaultHints());
    
    private Point2D startDragPos;
    private boolean dragged;
    private boolean ctrl;
    private Cursor cursor;

    /**
     * Constructor
     */
    public MultiSelectTool() {
        Toolkit tk = Toolkit.getDefaultToolkit();
        ImageIcon imgIcon = new ImageIcon(getClass().getResource(CURSOR_IMAGE));
        cursor = tk.createCustomCursor(imgIcon.getImage(), CURSOR_HOTSPOT, TOOL_NAME);

        startDragPos = new DirectPosition2D();
        dragged = false;
    }

    /**
     * Constructs 4x4 size bounding box around mouse click point
     * and calls for selectedFeaturesIn bounding_box
     *
     * @param e mouse event
     */
    @Override
    public void onMouseClicked(MapMouseEvent e) {
        if ((e.getModifiers() & ActionEvent.CTRL_MASK) != 0) {
            ctrl = true;
        }

        /*
         * Construct a 4x4 pixel rectangle centred on the mouse click position
         */
        Point screenPos = e.getPoint();
        Rectangle screenRect = new Rectangle(screenPos.x-2, screenPos.y-2, 4, 4);

        /*
         * Transform the screen rectangle into bounding box in the coordinate
         * reference system of our map context. Note: we are using a naive method
         * here but GeoTools also offers other, more accurate methods.
         */
        AffineTransform screenToWorld = getMapPane().getScreenToWorldTransform();
        Rectangle2D worldRect = screenToWorld.createTransformedShape(screenRect).getBounds2D();
        ReferencedEnvelope bbox = new ReferencedEnvelope(worldRect, getMapPane().getMapContext().getCoordinateReferenceSystem());

        selectFeaturesIn(bbox);

        ctrl = false;
    }

    /**
     * Records the map position of the mouse event in case this
     * button press is the beginning of a mouse drag
     *
     * @param ev the mouse event
     */
    @Override
    public void onMousePressed(MapMouseEvent ev) {
        startDragPos = new DirectPosition2D();
        startDragPos.setLocation(ev.getMapPosition());
    }

    /**
     * Records that the mouse is being dragged
     *
     * @param ev the mouse event
     */
    @Override
    public void onMouseDragged(MapMouseEvent ev) {
        dragged = true;
    }

    /**
     * If the mouse was dragged, determines the bounds of the
     * box that the user defined and passes this to the mapPane's
     * {@link org.geotools.swing.JMapPane#setDisplayArea(org.opengis.geometry.Envelope) }
     * method
     *
     * @param ev the mouse event
     */
    @Override
    public void onMouseReleased(MapMouseEvent ev) {
        if (dragged && !ev.getPoint().equals(startDragPos)) {
            if ((ev.getModifiers() & ActionEvent.CTRL_MASK) != 0) {
                ctrl = true;
            }

            Envelope2D env = new Envelope2D();
            env.setFrameFromDiagonal(startDragPos, ev.getMapPosition());
            
            ReferencedEnvelope renv = new ReferencedEnvelope(env, ev.getMapPosition().getCoordinateReferenceSystem());
            selectFeaturesIn(renv);
            
            dragged = false;
            ctrl = false;
        }
    }

    /**
     * Get the mouse cursor for this tool
     */
    @Override
    public Cursor getCursor() {
        return cursor;
    }

    /**
     * Returns true to indicate that this tool draws a box
     * on the map display when the mouse is being dragged to
     * select multiple features
     */
    @Override
    public boolean drawDragBox() {
        return true;
    }

    
    /**
     * Queries each MapLayer, filters features in selected area
     * 
     * @param selected - selection area
     */
    private void selectFeaturesIn(ReferencedEnvelope selected) {
        MyMapContext context = (MyMapContext) getMapPane().getMapContext();
        
        for (MapLayer layer : context.getLayers()) {
            if((!layer.isSelected()) || (!layer.isVisible())) continue;
            
            /*
             * usually "THE_GEOM" for shapefiles
             * 
             */
            FeatureType schema = layer.getFeatureSource().getSchema();
            String geometryPropertyName = schema.getGeometryDescriptor().getLocalName(); // "THE_GEOM"
            
             /*
             * Create a Filter to select features that intersect with
             * the bounding box
             */

            Filter filter = ff.bbox(ff.property(geometryPropertyName), selected);

            /*
             * Use the filter to identify the selected features
             */
            try {
                FeatureCollection selectedFeatures = layer.getFeatureSource().getFeatures(filter);

                FeatureIterator iter = selectedFeatures.features();
                Set<FeatureId> IDs = new HashSet<FeatureId>();
                try {
                    while (iter.hasNext()) {
                        Feature feature = iter.next();
                        IDs.add(feature.getIdentifier());
                        System.out.println(" * " + feature.getIdentifier());
                    }

                } finally {
                    iter.close();
                }

                if(ctrl) {
                    context.expandSelection(layer, IDs);
                } else {
                    context.selectFeatures(layer, IDs);
                }
                context.fireSelectionChanged();

            } catch (Exception ex) {
                ex.printStackTrace();
                return;
            }
        }
        
        context.resetStyles();
        getMapPane().repaint();
    }
    
    

}
