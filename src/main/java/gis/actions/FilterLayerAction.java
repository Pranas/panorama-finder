package gis.actions;

import gis.FeatureDetailFrame;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;


import org.geotools.feature.FeatureCollection;
import org.geotools.filter.text.cql2.CQL;
import org.geotools.filter.text.cql2.CQLException;
import org.geotools.swing.table.FeatureCollectionTableModel;

public class FilterLayerAction implements ActionListener {

    private FeatureDetailFrame frame;

    public FilterLayerAction(FeatureDetailFrame frame) {
        this.frame = frame;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        System.out.println("Filtering.");
        frame.textField.getText();
        org.opengis.filter.Filter filter = null;
        try {
            filter = CQL.toFilter(frame.textField.getText());
        } catch (CQLException e1) {
            e1.printStackTrace();
        }
        FeatureCollection features = null;
        if (frame.isSelected()) {
            features = frame.getSelectedFeatures(filter);
        } else {
            try {
                features = frame.featureSource.getFeatures(filter);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
        
        FeatureCollectionTableModel model = new FeatureCollectionTableModel(features);
        frame.table.setModel(model);
        frame.filterLabel.setVisible(true);
    }
}
