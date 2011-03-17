package gis.actions;

import gis.MyMapContext;
import gis.swing.FinderFrame;
import java.awt.event.ActionEvent;
import javax.swing.Action;
import javax.swing.ImageIcon;
import org.geotools.swing.JMapPane;
import org.geotools.swing.action.MapAction;

public class FinderAction extends MapAction {
    public static final String TOOL_NAME = "New Layer";
    public static final String TOOL_TIP = "Add new layer";
    public static final String ICON_IMAGE = "src/main/resources/new_layer.png";

    public FinderAction(JMapPane mapPane) {
        this(mapPane, false);
    }

    public FinderAction(JMapPane mapPane, boolean showToolName) {
        String toolName = showToolName ? TOOL_NAME : null;
        super.init(mapPane, toolName, TOOL_TIP, null);
        this.putValue(Action.SMALL_ICON, new ImageIcon(ICON_IMAGE));
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        FinderFrame finderFrame = new FinderFrame((MyMapContext) getMapPane().getMapContext());
        finderFrame.setVisible(true);
    }

}
