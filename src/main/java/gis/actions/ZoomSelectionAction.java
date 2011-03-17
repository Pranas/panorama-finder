package gis.actions;

import gis.MyMapContext;
import java.awt.event.ActionEvent;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.swing.JMapPane;
import org.geotools.swing.action.MapAction;

public class ZoomSelectionAction extends MapAction {
    public static final String TOOL_NAME = "Zoom Selection";
    public static final String TOOL_TIP = "Zoom to selection";
    public static final String ICON_IMAGE = "/org/geotools/swing/icons/mActionZoomFullExtent.png";

    public ZoomSelectionAction(JMapPane mapPane) {
        this(mapPane, false);
    }

    public ZoomSelectionAction(JMapPane mapPane, boolean showToolName) {
        String toolName = showToolName ? TOOL_NAME : null;

        String iconImagePath = null;
        super.init(mapPane, toolName, TOOL_TIP, ICON_IMAGE);
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        ReferencedEnvelope area = null;
        area = ((MyMapContext) getMapPane().getMapContext()).selectionArea();
        if(area == null) getMapPane().reset();
        else getMapPane().setDisplayArea(area);
    }

}