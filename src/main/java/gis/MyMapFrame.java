package gis;

import gis.actions.FinderAction;
import gis.actions.MultiSelectAction;
import gis.actions.NewLayerAction;
import gis.actions.ZoomSelectionAction;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import net.miginfocom.swing.MigLayout;
import org.geotools.map.DefaultMapContext;
import org.geotools.map.MapContext;
import org.geotools.swing.JMapFrame;
import org.geotools.swing.JMapPane;
import org.geotools.swing.StatusBar;
import org.geotools.swing.action.PanAction;
import org.geotools.swing.action.ResetAction;
import org.geotools.swing.action.ZoomInAction;
import org.geotools.swing.action.ZoomOutAction;

public class MyMapFrame extends JMapFrame {

    MyMapFrame(String string) {
        super();
        MapContext map = new MyMapContext();
        map.setTitle(string);
        setTitle(string);
        setMapContext(map);
    }

    MyMapFrame(MapContext map) {
        super(map);
    }
    
    /**
     * Creates and lays out the frame's components that have been
     * specified with the enable methods (e.g. {@linkplain #enableToolBar(boolean)} ).
     * If not called explicitly by the client this method will be invoked by
     * {@linkplain #setVisible(boolean) } when the frame is first shown.
     */
    @Override
    public void initComponents() {
        JMapPane mapPane = getMapPane();

        /*
         * We use the MigLayout manager to make it easy to manually code
         * our UI design
         */
        StringBuilder sb = new StringBuilder();
//        if (!toolSet.isEmpty()) {
            sb.append("[]"); // fixed size
//        }
        sb.append("[grow]"); // map pane and optionally layer table fill space
        sb.append("[30px::]"); // status bar height

        JPanel panel = new JPanel(new MigLayout(
                "wrap 1, insets 0", // layout constrains: 1 component per row, no insets

                "[grow]", // column constraints: col grows when frame is resized

                sb.toString() ));

        /*
         * A toolbar with buttons for zooming in, zooming out,
         * panning, and resetting the map to its full extent.
         * The cursor tool buttons (zooming and panning) are put
         * in a ButtonGroup.
         *
         * Note the use of the XXXAction objects which makes constructing
         * the tool bar buttons very simple.
         */
        JToolBar myToolBar = new JToolBar();
        myToolBar.setOrientation(JToolBar.HORIZONTAL);
        myToolBar.setFloatable(false);

        JButton newLayer = new JButton(new NewLayerAction(mapPane));
        myToolBar.add(newLayer);

        myToolBar.addSeparator();

        ButtonGroup cursorToolGrp = new ButtonGroup();

        JButton zoomInBtn = new JButton(new ZoomInAction(mapPane));
        myToolBar.add(zoomInBtn);
        cursorToolGrp.add(zoomInBtn);

        JButton zoomOutBtn = new JButton(new ZoomOutAction(mapPane));
        myToolBar.add(zoomOutBtn);
        cursorToolGrp.add(zoomOutBtn);

        myToolBar.addSeparator();

        JButton panBtn = new JButton(new PanAction(mapPane));
        myToolBar.add(panBtn);
        cursorToolGrp.add(panBtn);

        myToolBar.addSeparator();

        JButton resetBtn = new JButton(new ResetAction(mapPane));
        myToolBar.add(resetBtn);

        JButton zoomSelectionBtn = new JButton(new ZoomSelectionAction(mapPane));
        myToolBar.add(zoomSelectionBtn);

        myToolBar.addSeparator();

        JButton myInfoBtn = new JButton(new MultiSelectAction(mapPane));
        myToolBar.add(myInfoBtn);

        myToolBar.addSeparator();

        JButton finderBtn = new JButton(new FinderAction(mapPane));
        myToolBar.add(finderBtn);

        panel.add(myToolBar, "grow");
    	
    	MyMapLayerTable myMapLayerTable = new MyMapLayerTable(mapPane);
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, myMapLayerTable, mapPane);

        panel.add(splitPane, "grow");

        StatusBar myStatusBar = new StatusBar(mapPane);
        panel.add(myStatusBar);
        
        this.getContentPane().add(panel);
    }
    
    protected void errorMessage(String title, String text) {
		JOptionPane.showMessageDialog(this, text, title, JOptionPane.ERROR_MESSAGE);
	}
}
