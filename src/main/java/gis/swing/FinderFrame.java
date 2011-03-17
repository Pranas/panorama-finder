package gis.swing;

import gis.MyMapContext;
import gis.panorama.Finder;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import org.geotools.map.MapLayer;

public class FinderFrame extends JFrame {

    private static class LayerWrapper {
        public MapLayer layer;

        public LayerWrapper(MapLayer layer) {
            this.layer = layer;
        }

        @Override
        public String toString() {
            return layer.getFeatureSource().getName().getLocalPart();
        }
    }
    
    private JComboBox selectSurfaceLayer;
    private JComboBox selectRiversLayer;
    private JComboBox selectTownsLayer;
    private JComboBox selectLakesLayer;
    private JComboBox selectForestLayer;
    
    private JTextField townNameField;
    private JTextField townRadiusField;
    private JTextField hillRadiusField;
    private JTextField hillHeightField;
    
    public MyMapContext mapContext;

    public FinderFrame(final MyMapContext mapContext) {
        this.mapContext = mapContext;
        setTitle("Finder");
        
        Container contentPane = getContentPane();
        contentPane.setLayout(new FlowLayout());

        selectSurfaceLayer = new JComboBox();
        selectRiversLayer = new JComboBox();
        selectTownsLayer = new JComboBox();
        selectLakesLayer = new JComboBox();
        selectForestLayer = new JComboBox();
        
        for(MapLayer layer : mapContext.getLayers()) {
            selectSurfaceLayer.addItem(new LayerWrapper(layer));
            selectRiversLayer.addItem(new LayerWrapper(layer));
            selectTownsLayer.addItem(new LayerWrapper(layer));
            selectLakesLayer.addItem(new LayerWrapper(layer));
            selectForestLayer.addItem(new LayerWrapper(layer));
        }

        selectTownsLayer.setSelectedIndex(0);
        selectRiversLayer.setSelectedIndex(1);
        selectLakesLayer.setSelectedIndex(2);
        selectSurfaceLayer.setSelectedIndex(3);
        selectForestLayer.setSelectedIndex(4);

        contentPane.add(new JLabel("Select surface layer:"));
        contentPane.add(selectSurfaceLayer);

        contentPane.add(new JLabel("Select rivers layer:"));
        contentPane.add(selectRiversLayer);

        contentPane.add(new JLabel("Select towns layer:"));
        contentPane.add(selectTownsLayer);

        contentPane.add(new JLabel("Select lakes layer:"));
        contentPane.add(selectLakesLayer);
        
        contentPane.add(new JLabel("Select forest layer:"));
        contentPane.add(selectForestLayer);
        
        contentPane.add(new JLabel("Town name:"));
        townNameField = new JTextField("Merkin_");
        townNameField.setColumns(10);
        contentPane.add(townNameField);

        contentPane.add(new JLabel("Radius from town:"));
        townRadiusField = new JTextField("10000");
        townRadiusField.setColumns(10);
        contentPane.add(townRadiusField);
        
        contentPane.add(new JLabel("Radius from hill:"));
        hillRadiusField = new JTextField("1000");
        hillRadiusField.setColumns(10);
        contentPane.add(hillRadiusField);
        
        contentPane.add(new JLabel("Min height diff:"));
        hillHeightField = new JTextField("5");
        hillHeightField.setColumns(10);
        contentPane.add(hillHeightField);
        
        JButton findButton = new JButton("Find!");
        findButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                try {                    
                    Finder f = new Finder(  mapContext, 
                                            getTowns().getFeatureSource(),
                                            getRivers().getFeatureSource(),
                                            getLakes().getFeatureSource(),
                                            getSurface().getFeatureSource(),
                                            getForest().getFeatureSource());
                    
                    f.setHillHeight(Double.parseDouble(hillHeightField.getText()));
                    f.setIntersectionRadius(Double.parseDouble(hillRadiusField.getText()));
                    f.setTownName(townNameField.getText());
                    f.setTownRadius(Double.parseDouble(townRadiusField.getText()));
                    f.find();
                } catch (Exception e) { 
                    JOptionPane.showMessageDialog(null, e.getMessage(), null, JOptionPane.INFORMATION_MESSAGE);
                    e.printStackTrace();
                }
            }
            
        });
        
        contentPane.add(findButton);
        
        setSize(200, 550);
    }

    
    private MapLayer getRivers() {
        return ((LayerWrapper) selectRiversLayer.getSelectedItem()).layer;
    }

    private MapLayer getLakes() {
        return ((LayerWrapper) selectLakesLayer.getSelectedItem()).layer;
    }

    private MapLayer getTowns() {
        return ((LayerWrapper) selectTownsLayer.getSelectedItem()).layer;
    }
    
    private MapLayer getSurface() {
        return ((LayerWrapper) selectSurfaceLayer.getSelectedItem()).layer;
    }
    
    private MapLayer getForest() {
        return ((LayerWrapper) selectForestLayer.getSelectedItem()).layer;
    }

}