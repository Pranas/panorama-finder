package gis;

import gis.actions.NewLayerAction;
import java.io.File;

public class App {
    public static void main(String[] args) throws Exception {
        MyMapFrame mf = new MyMapFrame("Demo");
        
        mf.enableLayerTable(true);
        mf.enableToolBar(true);

        mf.setSize(900, 600);

        mf.setVisible(true);

        (new NewLayerAction(mf.getMapPane())).loadLayer(new File("src/main/resources/gyvenvie.shp"));
        (new NewLayerAction(mf.getMapPane())).loadLayer(new File("src/main/resources/upes.shp"));
        (new NewLayerAction(mf.getMapPane())).loadLayer(new File("src/main/resources/ezerai.shp"));
        (new NewLayerAction(mf.getMapPane())).loadLayer(new File("src/main/resources/pavirs_lt_p.shp"));
        (new NewLayerAction(mf.getMapPane())).loadLayer(new File("src/main/resources/miskai.shp"));
    }
}
