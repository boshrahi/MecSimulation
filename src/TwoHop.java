import model.*;
import utils.ParameterHandler;

import java.util.*;

public class TwoHop {
    private GraphModel graphModel;
    private ParameterHandler parameterHandler;
    private LinkedList<Integer> adjacency[]; //Adjacency Lists
    List<DemandModel[][]> Demands;
    List<List<DistanceModel>> distance;
    Simulation2 simulation2;

    public TwoHop(Simulation2 simulation2) {
        this.parameterHandler = simulation2.paramHandler;
        this.graphModel = simulation2.graph;
        this.adjacency = graphModel.getAdjacency();
        //List<DemandModel[][]> Demands = new ArrayList<>();
        this.Demands = calculateDemandMatrices();
        List<List<DistanceModel>> distance = new ArrayList<>();
        this.distance = calculateDistanceMatrices(this.Demands);
        this.simulation2 = simulation2;
    }

    private List<DemandModel[][]> calculateDemandMatrices() {
        //Demand of One Hop neighbors
        List<DemandModel[][]> list = new ArrayList<>();
        DemandModel[][] demand_i = null;

        for (int nodeIndex = 0; nodeIndex < graphModel.nodeNum; nodeIndex++) {
            int two_hop_counter = 0;
            LinkedList<Integer> neighbours = adjacency[nodeIndex];
            // find two hop neighbours counter
            for (int neIndex = 0; neIndex < neighbours.size(); neIndex++) {
                int neighb = neighbours.get(neIndex);
                two_hop_counter = two_hop_counter + adjacency[neighb].size() - 1;
            }
            demand_i = new DemandModel[parameterHandler.numOfApps][neighbours.size() + 1 + two_hop_counter]; // #App #neighbours+1
            for (int appIndex = 0; appIndex < parameterHandler.numOfApps; appIndex++) { // 2 apps for now
                // 0 column is demand of own node
                DemandModel demandModel = new DemandModel(parameterHandler.getDemandOfNode(nodeIndex, appIndex), nodeIndex, nodeIndex);
                demand_i[appIndex][0] = demandModel;
                for (int neIndex = 0; neIndex < neighbours.size(); neIndex++) {
                    int neighb = neighbours.get(neIndex);
                    demandModel = new DemandModel(parameterHandler.getDemandOfNode(neighb, appIndex), neighb, nodeIndex);
                    demand_i[appIndex][neIndex + 1] = demandModel;
                }
                // Two hop
                int indexCounterTwoHop = neighbours.size() + 1;
                for (int neIndex = 0; neIndex < neighbours.size(); neIndex++) {
                    int neighb = neighbours.get(neIndex);
                    LinkedList<Integer> neighbours_two = adjacency[neighb];
                    for (int neIndex2 = 0; neIndex2 < neighbours_two.size(); neIndex2++) {
                        int neighb2 = neighbours_two.get(neIndex2);
                        if (nodeIndex != neighb2) {
                            demandModel = new DemandModel(parameterHandler.getDemandOfNode(neighb2, appIndex), neighb2, neighb);
                            demand_i[appIndex][indexCounterTwoHop] = demandModel;
                            indexCounterTwoHop++;
                        }
                    }
                }
            }
            list.add(demand_i);
        }//
        return list;
    }

    private List<List<DistanceModel>> calculateDistanceMatrices(List<DemandModel[][]> demands) {
        List<List<DistanceModel>> list1 = new ArrayList<>();
        for (int nodeIndex = 0; nodeIndex < graphModel.nodeNum; nodeIndex++) {
            List<DistanceModel> singleNodeList = new ArrayList<>();
            DemandModel[][] matrix = Demands.get(nodeIndex);
            int rowSize = matrix[0].length;
            for (int demandIndex = 0; demandIndex < rowSize; demandIndex++) {
                DemandModel demandModels = matrix[0][demandIndex]; // app 0 for example
                DistanceModel distanceModel = new DistanceModel(getDistanceToNeighbour(demandModels.nodeName, demandModels.mediatorName) +
                        getDistanceToNeighbour(demandModels.mediatorName, nodeIndex), demandModels.nodeName);
                singleNodeList.add(distanceModel);
            }
            list1.add(singleNodeList);
        }
        return list1;

//        for (int nodeIndex = 0; nodeIndex < graphModel.nodeNum; nodeIndex++) {
//            LinkedList<Integer> neighbours = adjacency[nodeIndex];
//            List<DistanceModel> list2 = new ArrayList<>();
//            DistanceModel distanceModel = new DistanceModel(0, nodeIndex); //initilize
//            list2.add(distanceModel);
//
//            for (int neIndex = 0; neIndex < neighbours.size(); neIndex++) {
//                int neighb = neighbours.get(neIndex);
//                distanceModel = new DistanceModel(getDistanceToNeighbour(neighb, nodeIndex), neighb);
//                list2.add(distanceModel);
//            }
//            DemandModel[][] demandMatrix = demands.get(0);
//            int rowSize = demandMatrix[0].length;
//            for (int demandIndex = neighbours.size() + 1; demandIndex < rowSize; demandIndex++) {
//                DemandModel demandModels = demandMatrix[0][demandIndex];
//                distanceModel = new DistanceModel(getDistanceToNeighbour(demandModels.nodeName, demandModels.mediatorName) +
//                        getDistanceToNeighbour(demandModels.mediatorName, nodeIndex), demandModels.nodeName);
//                list2.add(distanceModel);
//            }
//            list1.add(list2);
//        }

    }

    private double getDistanceToNeighbour(int neighb, int nodeIndex) {
        for (int edgeIndex = 0; edgeIndex < graphModel.linkNum; edgeIndex++) {
            EdgeModel edgeModel = graphModel.edgeModelList.get(edgeIndex);
            if (edgeModel.source == nodeIndex && edgeModel.target == neighb ||
                    edgeModel.target == nodeIndex && edgeModel.source == neighb) {
                return edgeModel.distance;
            }
        }
        return 0;
    }

    public double TwoHopAlgorithm() {
        return calculateAvgResponseTime(findVMplacement());
    }

    public String findVMplacement() {
        String placement = null;
        List<int[]> list = new ArrayList<>();
        for (int appIndex = 0; appIndex < parameterHandler.numOfApps; appIndex++) {
            int[] votes = new int[graphModel.nodeNum];
            for (int nodeIndex = 0; nodeIndex < graphModel.nodeNum; nodeIndex++) {
                DemandModel[][] Demands_i = Demands.get(nodeIndex);
                List<DistanceModel> Distance_i = distance.get(nodeIndex);
//                double nodeSelfDemand = Demands_i[appIndex][0]; // node itself
//                double maximum = nodeSelfDemand;
//                int whichNodeToVote = Distance_i.get(0).nodeName; //vote for itself
                double distance = Distance_i.get(1).distance;
                double maximum = (Demands_i[appIndex][1].demand) / distance;
                int whichNodeToVote = Distance_i.get(1).nodeName;;
                for (int index = 1; index < Distance_i.size(); index++) {
                    distance = Distance_i.get(index).distance;
                    double relation = (Demands_i[appIndex][index].demand) / distance;
                    if (maximum < relation) {
                        maximum = relation;
                        whichNodeToVote = Distance_i.get(index).nodeName;
                    }
                }
                votes[nodeIndex] = whichNodeToVote;
            }
            list.add(votes); // votes for differnt apps
        }

        return getPlacementFromVotes(list);
    }
    private String getPlacementFromVotes(List<int[]> list) {
        String total_placement= "";
        List<MaxApp> list2 = new ArrayList<>();
        for (int appIndex = 0; appIndex < parameterHandler.numOfApps; appIndex++) {
            int[] appVotes = list.get(appIndex);
            Arrays.sort(appVotes);
            int maxCounter = 1;
            int currentVal = appVotes[0];
            for (int arrayIndex = 1; arrayIndex < appVotes.length; arrayIndex++) {
                int node1 = appVotes[arrayIndex];

                if (currentVal == node1){
                    maxCounter++;
                    currentVal =node1;
                }else {
                    MaxApp maxApp = new MaxApp(currentVal,maxCounter,appIndex);
                    maxCounter=1;
                    currentVal = node1;
                    list2.add(maxApp);
                    if (arrayIndex==appVotes.length-1){
                        maxApp = new MaxApp(node1,maxCounter,appIndex);
                        list2.add(maxApp);
                    }
                }
            }
        }
        for (int appIndex = 0; appIndex < parameterHandler.numOfApps; appIndex++) {
            String app_placement = "";
            for (int vmIndex = 0; vmIndex <parameterHandler.numOfVRCPerApp; vmIndex++){
                int max_vote =0;
                int candidateNode=0;
                int candidateIndex=-1;
                for (int indexList=0; indexList < list2.size(); indexList++){
                    MaxApp maxApp = list2.get(indexList);
                    if (maxApp.appNum == appIndex && maxApp.numberOfVotes>max_vote){
                        candidateNode = maxApp.nodeName;
                        max_vote = maxApp.numberOfVotes;
                        candidateIndex = indexList;
                    }
                }
                if (candidateIndex!=-1) {
                    list2.remove(candidateIndex);
                    app_placement = app_placement + candidateNode + ",";
                }
            }
            app_placement = checkMissingVmsAdd(app_placement);
            total_placement = total_placement + app_placement;
        }
        System.out.println("Two Hop Placement : " + total_placement);
        return total_placement;
    }
    private String checkMissingVmsAdd(String placement) {

        String[] splited = placement.split(",");
        int[] splited_int = new int[splited.length];
        for (int i=0; i<splited.length; i++){
            splited_int[i] = Integer.valueOf(splited[i]);
        }
        //--------------------
        if (splited.length == parameterHandler.numOfVRCPerApp) return placement;
        else {
            for (int vmIndex=0 ;vmIndex < parameterHandler.numOfVRCPerApp; vmIndex++){
                Arrays.sort(splited_int);
                int index = Arrays.binarySearch(splited_int, vmIndex);
                if (index < 0) {
                    placement = placement + vmIndex + ",";
                }
            }
            return placement;
        }
    }
    public double calculateAvgResponseTime(String placement) {
        double T_AVG = 0;
        HashSet<Integer> alreadyDeployedApps = new HashSet<>(parameterHandler.numOfApps);
        for (int appIndex = 0; appIndex < parameterHandler.numOfApps; appIndex++) {
            alreadyDeployedApps.add(appIndex);
        }
        List<SigmaModel> sigmaModels = simulation2.assignmentProcedure(placement, alreadyDeployedApps, parameterHandler.totalRequests);
        //calculate T_AVG equation 8
        T_AVG = simulation2.calculateTimeAverage(sigmaModels);
        return T_AVG;
    }
}