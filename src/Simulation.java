import cc.redberry.combinatorics.Combinatorics;
import model.*;
import utils.Dijkstra;
import utils.Edge;
import utils.Graph;
import utils.Vertex;

import java.util.ArrayList;
import java.util.List;

/**
 * @param : graphType we have 4 type of graphs that user selected -------> G paper
 * @param : numOfVRCPerApp virtual machine replica per app        -------> k paper
 * @param : numOfUsers number of overall users                    ------ > N paper
 * @param : capOfMec capacity of each mec server                  ------ > C paper
 * @param : numOfAppshow many app we support                     ------ > M paper
 * @param : alpha ratio of request for application m from the     ------ > a paper
 *          region of mec server u
 * @author Boshra
 */

class Simulation {

    private String graphType;
    private int numOfVRCPerApp;
    private int numOfUsers;
    private double capOfMec;
    private int numOfApps;
    private double alpha;
    private GraphModel graph;
    private static List<VRCnodeModel> placementsForSingleApp;
    //------------------------request

    double totalRequests;
    //------------------- demand of every request of app
    double demandOfRequest = 2;
    double[] capacityOfMec ;


    Simulation(String graphType, int numOfVRCPerApp, int numOfUsers, int numOfApps, double capOfMec, double alpha) {

        this.graphType = graphType;
        Graph graph = new Graph(graphType);
        this.graph = graph.getGraphModel();
        this.numOfApps = numOfApps;
        this.numOfUsers = numOfUsers;
        this.numOfVRCPerApp = numOfVRCPerApp;
        this.capOfMec = capOfMec;
        this.alpha = alpha;
        this.capacityOfMec = new double[graph.getGraphModel().nodeNum];
    }

    double optimalEnumerationPlacementAlgorithm() {


        List<List<Double>> requestOfApps = initialRequests();

        //initialize S denote all placements
        List<List> PlacementsForAllApps = initialAllPlacements(numOfVRCPerApp, numOfApps, graph);
        List<FinalAssignmentModel> finalList = new ArrayList<>();
        double T_MIN = Double.POSITIVE_INFINITY;
        double T_AVG = 0;
        for (int index = 0; index < PlacementsForAllApps.size(); index++) { // for every app
            FinalAssignmentModel fAssign = assignmentProcedure(PlacementsForAllApps.get(index), requestOfApps.get(index)); //send single app and requests of that app tp assign procedure
            finalList.add(fAssign);

        }
        //T_MIN = evaluateAvgResponseTime(finalList);
        return T_MIN;
    }

    private List<List<Double>> initialRequests() {
        List<Double> requestOfRegionList = new ArrayList<>();
        List<List<Double>> requestOfApps = new ArrayList<>();
        totalRequests = 0;
        for (int i = 0; i < numOfApps; i++) {
            for (int j = 0; j < graph.nodeNum; j++) {
                requestOfRegionList.add(evalRequestOfRegionAndTotal());
            }
            requestOfApps.add(requestOfRegionList);

            for (Double requ : requestOfRegionList) {
                totalRequests = totalRequests + requ;
            }
            requestOfRegionList = new ArrayList<>();

        }
        return requestOfApps;
    }

    private FinalAssignmentModel assignmentProcedure(List<VRCnodeModel> singleApp, List<Double> requestOfApp) { // requestOfApps = request of app M1 in all regions

        double W_MIN = Double.POSITIVE_INFINITY;
        VRCnodeModel model = null;
        for (int placementIndex = 0; placementIndex < singleApp.size(); placementIndex++) {
            double allRequestOfsingleApp = 0;
            int cloud = 0;
            for (Double reqRegion : requestOfApp) {
                allRequestOfsingleApp = allRequestOfsingleApp + reqRegion;
            }

            VRCnodeModel vrCnodeModel = singleApp.get(placementIndex);
            ArrayList<Long> vrcs = new ArrayList<>();
            for (int i = 0; i < numOfVRCPerApp; i++) {
                vrcs.add(vrCnodeModel.map.get(i));
            }

            updateCapacityArray(vrcs);

            double W = 0;


            for (int nodeIndex = 0; nodeIndex < graph.nodeNum; nodeIndex++) {
                for (int reqIndex = 0; reqIndex < requestOfApp.get(nodeIndex); reqIndex++) {
                    if (isAnyContainerMecHasCapacity()) {

                        ShortestPath shortestPath = getDelayForRequest(graph.nodeModelList.get(nodeIndex), getCorrectVRCs(vrcs));
                        //W = W + shortestPath.pathLen;
                        //TODO uncomment above line for ShortestPath class


                            // V0 vali roye MEC1
                        // long correctMec =  vrCnodeModel.map.get((int) shortestPath.id);


                       // capacityOfMec[(int) shortestPath.id] = capacityOfMec[(int) shortestPath.id] - demandOfRequest;
                        //TODO uncomment above line for ShortestPath class
                    } else {
                        cloud++;
                        W = W + 100; // 100 ms for cloud
                    }
                    allRequestOfsingleApp--;
                }
            }
            if (W_MIN > W) {
                W_MIN = W;
                model = vrCnodeModel;
            }
        }
        System.out.println(W_MIN);
        System.out.println(model);
        return null;
    }

    private void updateCapacityArray(ArrayList<Long> vrcs) {
        int i = 0;
       while (i < graph.nodeNum) {
           if (vrcs.contains(Long.valueOf(i))) {
               capacityOfMec[i] = capOfMec;
               i++;
           } else {
               capacityOfMec[i] = 0;
               i++;
           }
       }

    }

    private ArrayList<Long> getCorrectVRCs(List<Long> vrcs) {
        ArrayList<Long> list = new ArrayList<>();
        for (int index = 0; index < vrcs.size() ; index ++ ){
            int vm_place = Math.toIntExact(vrcs.get(index));
            if (capacityOfMec[vm_place] > 0) {
                list.add(vrcs.get(index));
            }
        }
        return list;
    }

    private boolean isAnyContainerMecHasCapacity() {
        for (int mecIndex = 0; mecIndex < capacityOfMec.length; mecIndex++) {
            if (capacityOfMec[mecIndex] > 0) return true;
        }
        return false;
    }

    private ShortestPath getDelayForRequest(NodeModel nodeModel, ArrayList<Long> vrcs) {
        long node_id = nodeModel.id; // id means that wich VM
        ShortestPath shortestPath = new ShortestPath();
        boolean isLocal = true;
            for (int vm = 0 ; vm < vrcs.size() ; vm ++){
                int vm_place = Math.toIntExact(vrcs.get(vm));
                if (vm_place == node_id && capacityOfMec[vm_place] > 0){
                   // shortestPath.pathLen = 0;
                    //shortestPath.id = node_id;
                    //TODO uncomment above line for ShortestPath class
                    return shortestPath;
                }
            }
            for (int index = 0; index < graph.linkNum; index++) {
                long source = graph.edgeModelList.get(index).source;
                long target = graph.edgeModelList.get(index).target;
                if (source == node_id && vrcs.contains(target)) {
                   // shortestPath.id = target;
                   // shortestPath.pathLen = graph.edgeModelList.get(index).distance;
                    //TODO uncomment above line for ShortestPath class
                    return shortestPath;
                } else if (target == node_id && vrcs.contains(source)) {
                    //shortestPath.id = source;
                   // shortestPath.pathLen = graph.edgeModelList.get(index).distance;
                    //TODO uncomment above line for ShortestPath class
                    return shortestPath;
                }

            }
        return findShortestPath(node_id, vrcs);

    }


    private ShortestPath findShortestPath(long node_id, ArrayList<Long> vrcs) {
        List<Vertex> vertexs = new ArrayList<>();
        for (int index = 0; index < graph.nodeNum; index++) { // makeing vertex
            Vertex vertex = new Vertex((String.valueOf(graph.nodeModelList.get(index).id)));
            vertexs.add(vertex);
        }
        //making neighbours
        for (int vertexIndex = 0; vertexIndex < vertexs.size(); vertexIndex++) {
            Vertex vertex = vertexs.get(vertexIndex);
            for (int edgeIndex = 0; edgeIndex < graph.linkNum; edgeIndex++) {
                EdgeModel edge = graph.edgeModelList.get(edgeIndex);
                String source = String.valueOf(edge.source);
                if (vertex.name.equals(source)) {
                    String target = String.valueOf(edge.target);
                    for (int targetIndex = 0; targetIndex < vertexs.size(); targetIndex++) {
                        Vertex vertexx = vertexs.get(targetIndex);
                        if (vertexx.name.equals(target)) {
                            // find target vertexes here
                            vertex.adjacencies.add(new Edge(vertexx, edge.distance));
                        }
                    }
                }
            }
        }
        // compute paths
        Vertex node = null;
        for (int j = 0; j < vertexs.size(); j++) {

            if (vertexs.get(j).name.equals(String.valueOf(node_id))) {
                node = vertexs.get(j);
                break;
            }
        }
        Dijkstra.computePaths(node);

        // find VM in vertexs
        double min = Double.POSITIVE_INFINITY;
        String choosenVertex = null;
        for (int vertexIndex = 0; vertexIndex < vertexs.size(); vertexIndex++) {
            Vertex vertex = vertexs.get(vertexIndex);
            for (int vmIndex = 0; vmIndex < vrcs.size(); vmIndex++) {
                Long vm_id = vrcs.get(vmIndex);
                if (vertex.name.equals(String.valueOf(vm_id))) {
                    List<Vertex> path = Dijkstra.getShortestPathTo(vertex);
                    //find shortest among vm
                    double currentMin = vertex.minDistance;

                    if (currentMin < min) {
                        min = currentMin;
                        choosenVertex = vertex.name;
                    }

                }
            }
        }
        ShortestPath shortestPath = new ShortestPath();
        //shortestPath.id = Long.valueOf(choosenVertex);
        //shortestPath.pathLen = min;
        //TODO uncomment above line for ShortestPath class

        return shortestPath;
    }

    private double getDelayAppOnEdge(EdgeModel edgeModel) {

        return edgeModel.distance;
    }

    private double evalRequestOfRegionAndTotal() {

        //double alpha_region_v = Math.random(); // in ra dar hale asli random konim
        double alpha_region_v = 0.5;
        double sigma_region_u_to_v = 1 - alpha_region_v;
        double landa = 2;
        double request_region_v_app_m = numOfUsers / graph.nodeNum * alpha_region_v * landa; // tedad karbar ha zaribi az node ha bashad
        return request_region_v_app_m;

    }


    private List<List> initialAllPlacements(int numOfVRCPerApp, int numOfApps, GraphModel graph) {

        // System.out.println(enumeratePlacementCounts());
        //---------------------------------
        List<List> all = new ArrayList<>();

        for (int j = 0; j < numOfApps; j++) {

            placementsForSingleApp = new ArrayList<>();

            Combinatorics.combinationsWithPermutations(graph.nodeNum, numOfVRCPerApp)
                    .stream()
                    .map(Simulation::combinChangeIntToString)
                    .forEach(Simulation::combinWritePlacementToList);
            all.add(placementsForSingleApp);

        }
        return all;
    }


    private static String combinChangeIntToString(int[] ints) {
        String newString = "";
        for (int index = 0; index < ints.length; index++) {
            if (index == ints.length - 1) newString = newString + ints[index];
            else newString = newString + ints[index] + " ";
        }
        return newString;
    }

    private static void combinWritePlacementToList(String s) {
        String[] array = s.split(" ");
        VRCnodeModel placement = new VRCnodeModel();
        for (int vrcIndex = 0; vrcIndex < array.length; vrcIndex++) {
            placement.map.put(vrcIndex, Long.valueOf(array[vrcIndex]));
        }
        placementsForSingleApp.add(placement);

    }
//
//    private long enumeratePlacementCounts() {
//        long nodes = graph.nodeNum;
//        long multi = 1;
//        for (int i = 1; i <= numOfVRCPerApp; i++) {
//            multi = multi * nodes;
//            nodes--;
//        } // enumerate places form VRCs in utils.Graph
//        multi = (long) Math.pow(multi, numOfApps);
//        return multi;
//    }

}
