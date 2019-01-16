package utils;

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
 * utils.Graph 1 : Spiralight               model.NodeModel = 15 Link = 16
 * utils.Graph 2 : Sago                     model.NodeModel = 18 Link = 17
 * utils.Graph 3 : Shentel                  model.NodeModel = 28 Link = 35
 * utils.Graph 4 : Missouri                 model.NodeModel = 67 Link = 83
 */

public class Graph {
    public static final String SPIRALIGHT = "spiralight";
    public static final String SAGO = "sago";
    public static final String SHENTEL = "shentel";
    public static final String MISSOURI = "missouri";
    public static final String TEST = "test";
    public static final String NOEL = "noel";
    public GraphModel model;


    public Graph(String type) {
        model = new GraphModel();
        switch (type) {
            case SPIRALIGHT:
                model = creatGraph(SPIRALIGHT);
                break;
            case NOEL:
                model = creatGraph(NOEL);
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

    public GraphModel getGraphModel() {
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
                case NOEL:
                    jsonObject = (JSONObject) parser.parse(new FileReader("Noel.json"));
                    break;
                case SPIRALIGHT:
                    jsonObject = (JSONObject) parser.parse(new FileReader("Spiralight.json"));
                    break;
                case SAGO:
                    jsonObject = (JSONObject) parser.parse(new FileReader("Sago.json"));
                    break;
                case SHENTEL:
                    jsonObject = (JSONObject) parser.parse(new FileReader("Shentel.json"));
                    break;
                case MISSOURI:
                    jsonObject = (JSONObject) parser.parse(new FileReader("Missouri.json"));
                    break;
            }
        } catch (Exception e) {
        }
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

        graphModel.prepareAdjacencyList();
        for (int i = 0; i < graphModel.nodeNum; i++) {
            for (int j = 0; j < graphModel.linkNum; j++) {
                JSONObject obj = (JSONObject) edgeArr.get(j);
                long source = (long) obj.get("source");
                long target = (long) obj.get("target");
                String id = (String) obj.get("id");
                long distance = (long) obj.get("distance");

                if (source == i) {
                    EdgeModel edgeModel = new EdgeModel(source, target, id, distance);
                    graphModel.addAdjacencyEdge((int) source, (int) target);
                    graphModel.edgeModelList.add(edgeModel);
                }

            }

        }

        return graphModel;
    }
}
