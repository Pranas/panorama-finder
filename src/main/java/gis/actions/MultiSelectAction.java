package gis.actions;

import gis.MyMapContext;
import gis.tools.MultiSelectTool;
import java.awt.event.ActionEvent;
import org.geotools.swing.JMapPane;
import org.geotools.swing.action.MapAction;

public class MultiSelectAction extends MapAction {
    /**
     * Constructor. The associated control will be labelled with an icon.
     *
     * @param mapPane the map pane being serviced by this action
     */
    public MultiSelectAction(JMapPane mapPane) {
        this(mapPane, false);
    }

    /**
     * Constructor. The associated control will be labelled with an icon and,
     * optionally, the tool name.
     *
     * @param mapPane the map pane being serviced by this action
     * @param showToolName set to true for the control to display the tool name
     */
    public MultiSelectAction(JMapPane mapPane, boolean showToolName) {
        String toolName = showToolName ? MultiSelectTool.TOOL_NAME : null;
        super.init(mapPane, toolName, MultiSelectTool.TOOL_TIP, MultiSelectTool.ICON_IMAGE);
    }

    /**
     * Called when the associated control is activated. Leads to the
     * map pane's cursor tool being set to a PanTool object
     *
     * @param ev the event (not used)
     */
    @Override
    public void actionPerformed(ActionEvent ev) {
        ((MyMapContext) getMapPane().getMapContext()).resetSelection();
        getMapPane().repaint();
        getMapPane().setCursorTool(new MultiSelectTool());
    }

}
