
import model.EdgeModel;
import model.GraphModel;
import model.NodeModel;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.FileReader;

/**
 * @author boshra
 * this class is for making graph of mec server: we use graphs of real world obtained from http://www.topology-zoo.org/explore.html
 * Graph 1 : Spiralight               model.NodeModel = 15 Link = 16
 * Graph 2 : Sago                     model.NodeModel = 18 Link = 17
 * Graph 3 : Shentel                  model.NodeModel = 28 Link = 35
 * Graph 4 : Missouri                 model.NodeModel = 67 Link = 83
 *
 */

public class Graph {
    public static final String SPIRALIGHT = "SPIRALIGHT";
    public static final String SAGO = "SAGO";
    public static final String SHENTEL = "SHENTEL";
    public static final String MISSOURI = "MISSOURI";
    public static final String TEST = "TEST";
    public GraphModel model;


    public Graph(String type){
        model = new GraphModel();
        switch (type){
            case SPIRALIGHT:
                model = creatGraph(SPIRALIGHT);
                break;
            case SAGO:
                model = creatGraph(SAGO);
                break;
            case SHENTEL:
                model = creatGraph(SHENTEL);
                break;
            case MISSOURI:
                model = creatGraph(MISSOURI);
                break;
            case TEST:
                model = creatGraph(TEST);
                break;
        }


    }
    public GraphModel getGraphModel(){
        return model;
    }

    private GraphModel creatGraph(String type) {
        JSONParser parser = new JSONParser();
        JSONObject jsonObject = null;
        try {

            switch (type) {
                case TEST:
                    jsonObject = (JSONObject) parser.parse(new FileReader("test.json"));
                    break;
                case SPIRALIGHT:
                    jsonObject = (JSONObject) parser.parse(new FileReader("Spiralight2.json"));
                    break;
                case SAGO:
                    jsonObject = (JSONObject) parser.parse(new FileReader("Sago2.json"));
                    break;
                case SHENTEL:
                    jsonObject = (JSONObject) parser.parse(new FileReader("Shentel2.json"));
                    break;
                case MISSOURI:
                    jsonObject = (JSONObject) parser.parse(new FileReader("Missouri2.json"));
                    break;
            }
        }catch (Exception e){}
        GraphModel graphModel = parseJsonFile(jsonObject);
        return graphModel;
    }

    private GraphModel parseJsonFile(JSONObject jsonObject) {

        GraphModel graphModel = new GraphModel();
        JSONArray nodesArr = (JSONArray) jsonObject.get("nodes");
        graphModel.nodeNum = nodesArr.size();

        JSONArray edgeArr = (JSONArray) jsonObject.get("edges");
        graphModel.linkNum = edgeArr.size();

        for (Object aNodesArr : nodesArr) {
            JSONObject object = (JSONObject) aNodesArr;
            String name = (String) object.get("label");
            long id = (long) object.get("id");
            NodeModel nodeModel = new NodeModel(name, id);
            graphModel.nodeModelList.add(nodeModel);

        }

        for (int i = 0; i < graphModel.nodeNum; i++){
            for (int j = 0; j < graphModel.linkNum ; j++){
                JSONObject obj = (JSONObject) edgeArr.get(j);
                long source = (long) obj.get("source");
                long target = (long) obj.get("target");
                String id = (String) obj.get("id");
                double distance = (double) obj.get("distance");

                if (source==i){
                    EdgeModel edgeModel = new EdgeModel(source,target,id,distance);
                    graphModel.edgeModelList.add(edgeModel);
                }

            }

        }
        return graphModel;
    }
}
