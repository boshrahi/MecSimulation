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
    public static final String G105 = "G105";
    public static final String G200 = "G200";
    public static final String G306 = "G306";
    public static final String G406 = "G406";
    public static final String G512 = "G512";
    public GraphModel model;


    public Graph(String type) {
        model = new GraphModel();
        switch (type) {
            case SPIRALIGHT:
                model = creatGraph(SPIRALIGHT);
                model.graphName = SPIRALIGHT;
                break;
            case NOEL:
                model = creatGraph(NOEL);
                model.graphName = NOEL;
                break;
            case SAGO:
                model = creatGraph(SAGO);
                model.graphName = SAGO;
                break;
            case SHENTEL:
                model = creatGraph(SHENTEL);
                model.graphName = SHENTEL;
                break;
            case MISSOURI:
                model = creatGraph(MISSOURI);
                model.graphName = MISSOURI;
                break;
            case TEST:
                model = creatGraph(TEST);
                model.graphName = TEST;
                break;
            case G105:
                model = creatGraph(G105);
                model.graphName = G105;
                break;
            case G200:
                model = creatGraph(G200);
                model.graphName = G200;
                break;
            case G306:
                model = creatGraph(G306);
                model.graphName = G306;
                break;
            case G406:
                model = creatGraph(G406);
                model.graphName = G406;
                break;
            case G512:
                model = creatGraph(G512);
                model.graphName = G512;
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
                case G105:
                    jsonObject = (JSONObject) parser.parse(new FileReader("Graph_(n3c6-b1 105).json"));
                    break;
                case G200:
                    jsonObject = (JSONObject) parser.parse(new FileReader("Graph_(rdb200 200).json"));
                    break;
                case G306:
                    jsonObject = (JSONObject) parser.parse(new FileReader("Graph_(mesh2e1 306).json"));
                    break;
                case G406:
                    jsonObject = (JSONObject) parser.parse(new FileReader("Graph_(lshp_406 406).json"));
                    break;
                case G512:
                    jsonObject = (JSONObject) parser.parse(new FileReader("Graph_(dwa512 512).json"));
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
