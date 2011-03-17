package gis;

import gis.actions.FilterLayerAction;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.geotools.data.FeatureSource;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.filter.Filter;
import org.geotools.map.MapLayer;
import org.geotools.swing.JMapPane;
import org.geotools.swing.table.FeatureCollectionTableModel;
import org.opengis.feature.Feature;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.identity.FeatureId;

public class FeatureDetailFrame extends JFrame {
	public JTable table = new JTable();
	public JButton filterButton = new JButton("Filter");
	public JButton allButton = new JButton("All");
	public JButton selectedButton = new JButton("Selected");
	public JTextField textField = new JTextField();
	public FeatureSource featureSource = null;
	public JLabel statusLabel = new JLabel();
	public JLabel filterLabel = new JLabel("[FILTERED]");
	
	private String allText = "Showing all features";
	private String selectedText = "Showing selected features";
	
	private MapLayer layer;
	private JMapPane pane;
	private ArrayList<Integer> selectedIndexes = new ArrayList<Integer>();
	private MyMapContext context;
	
	private boolean selected = false;
	
	public FeatureDetailFrame(final JMapPane pane, MapLayer layer) throws Exception {
		this.layer = layer;
		this.pane = pane;
		this.context = (MyMapContext) pane.getMapContext();
		this.featureSource = layer.getFeatureSource();
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	    table.setModel(new FeatureCollectionTableModel(this.featureSource.getFeatures()));
	    // This table model is lazy and is loading features in the background,
	    // so we add a listener which is invoked on every change
	    table.getModel().addTableModelListener(new TableModelListener() {
            @Override
	        public void tableChanged(TableModelEvent e) {
	        	selectSelectedFeatures();
	        }
	    });
	    
	    // This listener updates the map context with selection changes made in the table
	    table.addMouseListener(new MouseAdapter() {
            @Override
			public void mouseReleased(MouseEvent e) {
				adjustSelection();
			}
		});
	    
	    filterButton.addActionListener(new FilterLayerAction(this));
	    context.addFeatureDetailFrame(this);
	    addWindowListener(new WindowAdapter() {
            @Override
	        public void windowClosing(WindowEvent e) {
	        	onClose();
	        }
	    });
	    
	    selectedButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				showSelectedOnly();
			}
		});
	    
	    allButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				showAll();
			}
		});
	    
	    layoutInit();
	}
	
	public FeatureCollection getSelectedFeatures() {
		FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(GeoTools.getDefaultHints());
		Set<FeatureId> ids = context.selectedFeatures.get(layer);
		FeatureCollection selectedFeatures = null;
		try {
			selectedFeatures = featureSource.getFeatures(ff.id(ids));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return selectedFeatures;
	}
	
	public FeatureCollection getSelectedFeatures(org.opengis.filter.Filter filter) {
		FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(GeoTools.getDefaultHints());
		Set<FeatureId> ids = context.selectedFeatures.get(layer);
		FeatureCollection selectedFeatures = null;
		try {
			selectedFeatures = featureSource.getFeatures(ff.and(ff.id(ids), filter));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return selectedFeatures;
	}
	
	private void showSelectedOnly() {
		selected = true;
		statusLabel.setText(selectedText);
		filterLabel.setVisible(false);
		FeatureCollectionTableModel model = new FeatureCollectionTableModel(getSelectedFeatures());
		table.setModel(model);
	}
	
	private void showAll() {
		try {
			table.setModel(new FeatureCollectionTableModel(featureSource.getFeatures()));
			selected = false;
			statusLabel.setText(allText);
			filterLabel.setVisible(false);
			selectSelectedFeatures();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void adjustSelection() {
		Set<FeatureId> ids = new HashSet<FeatureId>();
		FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(GeoTools.getDefaultHints());
		FeatureCollection features = null;
		try {
			features = featureSource.getFeatures();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// pick out feature id strings
		ArrayList<String> rows = new ArrayList<String>();
		for (int row = 0; row < table.getRowCount(); row++) {
			if (table.isRowSelected(row)) {
				rows.add((String) table.getValueAt(row, 0));
			}
		}
		
		// build actual feature ids
		Set<FeatureId> fids = new HashSet<FeatureId>();
		for (String id : rows) {
			FeatureId fid = ff.featureId(id);
			fids.add(fid);
		}
		
		// fetch only the required features
		Filter filter = (Filter) ff.id(fids);
		try {
			FeatureCollection filteredFeatures = featureSource.getFeatures(filter);
			FeatureIterator iter = filteredFeatures.features();
			try {
				while (iter.hasNext()) {
					Feature feature = iter.next();
					ids.add(feature.getIdentifier());
				}
			} finally {
				iter.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		context.selectFeatures(layer, ids);
		context.resetStyles();
		pane.repaint();
	}
	
	private void onClose() {
		context.removeFeatureDetailFrame(this);
	}
	
	public void selectSelectedFeatures() {
		HashMap<MapLayer, Set<FeatureId>> selectedFeatures = context.getSelectedFeatures();
		Set<FeatureId> ids = selectedFeatures.get(layer);

		table.clearSelection();
		selectedIndexes.clear();
		for (int row = 0; row < table.getRowCount(); row++) {
			String next = (String) table.getValueAt(row, 0);
			for (FeatureId target : ids) {
				if (next.equals(target.toString())) {
					selectedIndexes.add(row);
				}
			}
		}
		// using invokelater, otherwise it does not update
		SwingUtilities.invokeLater(new Runnable() { public void run() {
			updateSelection();
		}});
	}
	
	private void updateSelection() {
		for (int row : selectedIndexes) {
			table.addRowSelectionInterval(row, row);
		}
	}
	
	private void layoutInit() {
		this.setLayout(new GridBagLayout());
		
		statusLabel.setText(allText);
		
	    JScrollPane scrollPane = new JScrollPane(table);
	    scrollPane.setVisible(true);
	    
	    GridBagConstraints c = new GridBagConstraints();
	    
	    c.insets = new Insets(6, 3, 3, 3);
	    
	    c.gridx = 0;
	    c.gridy = 0;
	    c.gridwidth = 1;
	    c.weightx = 1.0;
	    c.weighty = 0.0;
	    c.fill = GridBagConstraints.HORIZONTAL;
	    c.anchor = GridBagConstraints.CENTER;
	    this.add(textField, c);
	    
	    c.gridx = 1;
	    c.gridy = 0;
	    c.gridwidth = 1;
	    c.weightx = 0.0;
	    c.weighty = 0.0;
	    c.fill = GridBagConstraints.NONE;
	    c.anchor = GridBagConstraints.EAST;	   
	    this.add(filterButton, c);
	    
	    c.gridx = 0;
	    c.gridy = 1;
	    c.weightx = 1.0;
	    c.weighty = 1.0;
	    c.gridwidth = 2;
	    c.fill = GridBagConstraints.BOTH;
	    table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
	    table.setFillsViewportHeight(true);
	    this.add(scrollPane, c);
	    
	    c.gridx = 0;
	    c.gridy = 2;
	    c.gridwidth = 2;
	    c.weightx = 0.0;
	    c.weighty = 0.0;
	    c.fill = GridBagConstraints.NONE;
	    c.anchor = GridBagConstraints.EAST;
	    JPanel labelPanel= new JPanel();
	    labelPanel.setLayout(new FlowLayout());
	    labelPanel.add(statusLabel);
	    labelPanel.add(filterLabel);
	    labelPanel.add(allButton);
	    labelPanel.add(selectedButton);
	    this.add(labelPanel, c);
	    	    
	    this.setSize(900, 600);
	    this.setVisible(true);
	    filterLabel.setVisible(false);
	}

	public boolean isSelected() {
		return selected;
	}
}
