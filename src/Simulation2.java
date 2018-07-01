import cc.redberry.combinatorics.Combinatorics;
import model.GraphModel;
import model.SigmaModel;
import model.VRCnodeModel;
import utils.Graph;
import utils.ParameterHandler;

import java.util.*;

/**
 * @param : graphType we have 4 type of graphs that user selected -------> G paper
 * @param : numOfVRCPerApp virtual machine replica per app        -------> k paper
 * @param : numOfUsers number of overall users                    ------ > N paper
 * @param : capOfMec capacity of each mec server                  ------ > C paper
 * @param : numOfApp show many app we support                     ------ > M paper
 * @param : alpha ratio of request for application m from the     ------ > a paper
 *          region of mec server u
 * @author Boshra
 */

public class Simulation2 {

    private String graphType;
    private int numOfVRCPerApp;
    private int numOfUsers;
    private int numOfApps;
    private GraphModel graph;
    private static List<VRCnodeModel> placementsForSingleApp;
    private static List<String> allVMS;
    ParameterHandler paramHandler;
    //------------------------request


    Simulation2(String graphType, int numOfVRCPerApp, int numOfUsers, int numOfApps) {

        this.graphType = graphType;
        Graph graph = new Graph(graphType);
        this.graph = graph.getGraphModel();
        this.numOfApps = numOfApps;
        this.numOfUsers = numOfUsers;
        this.numOfVRCPerApp = numOfVRCPerApp;
        paramHandler = new ParameterHandler(numOfVRCPerApp, numOfUsers, numOfApps, graph.getGraphModel());
    }

    /*
     * Algorithm 1 in paper
     * */
    double optimalEnumerationPlacementAlgorithm() {

        List<String> all = initialAllPlacements(numOfVRCPerApp, numOfApps, graph);
        double T_MIN = Double.POSITIVE_INFINITY;
        double T_AVG = 0;
        String finalPLACEMENT = "";
        HashSet<Integer> alreadyDeployedApps = new HashSet<>(numOfApps);
        for (int appIndex = 0; appIndex < numOfApps; appIndex++) {
            alreadyDeployedApps.add(appIndex);
        }
        for (String placement : all) {
            List<SigmaModel> sigmaModels = assignmentProcedure(placement, alreadyDeployedApps);
            //calculate T_AVG equation 8
            T_AVG = calculateTimeAverage(sigmaModels);
            if (T_AVG <= T_MIN) {
                T_MIN = T_AVG;
                finalPLACEMENT = placement;
            }
        }
        System.out.println(finalPLACEMENT);
        return T_MIN;
    }

    /*
     * calculate time average for equation 8 in paper
     * */
    private double calculateTimeAverage(List<SigmaModel> sigmaModels) {
        double time = 0;
        double T_M_VU = 0;
        double T_SERVICE_V = 0;
        double AVG_M_V = 0;
        double T_CLOUD_M = 0;
        double SIGMA = 0;
        for (int appIndex = 0; appIndex < numOfApps; appIndex++) {
            for (int nodeVIndex = 0; nodeVIndex < graph.nodeNum; nodeVIndex++) {
                for (int nodeUIndex = 0; nodeUIndex < graph.nodeNum; nodeUIndex++) {
                    //double distance = paramHandler.distanceDelay(nodeVIndex,nodeUIndex,appIndex);
                    T_M_VU = paramHandler.calculateNetworkDelayBetweenTwoRegions(nodeVIndex,nodeUIndex,appIndex,sigmaModels);
                    T_SERVICE_V = paramHandler.calculateServerDelay(nodeVIndex, sigmaModels);
                    AVG_M_V = paramHandler.calculateAvrgRequestArrivalRate(appIndex, nodeVIndex, sigmaModels, appIndex);
                    time = time + T_M_VU + (T_SERVICE_V * AVG_M_V);

                }
            }
            T_CLOUD_M = paramHandler.calculateDelayOfCloudPerApp(appIndex);
            for (int nodeVIndex = 0; nodeVIndex < graph.nodeNum; nodeVIndex++) {
                for (int nodeUIndex = 0; nodeUIndex < graph.nodeNum; nodeUIndex++) {
                    SIGMA = getSigmaV_U_M(nodeVIndex, nodeUIndex, appIndex, sigmaModels);
                    time = time + T_CLOUD_M * (SIGMA) * paramHandler.calculateRequestOfAppInRegionV(appIndex, nodeVIndex);
                }

            }

        }
        time = time / paramHandler.totalRequests;
        return time;
    }

    /*
     * return total sigma for app m from region V to region U
     * */
    private double getSigmaV_U_M(int nodeVIndex, int nodeUIndex, int appIndex, List<SigmaModel> sigmaModels) {
        double SIGMA = 0;
        for (int sigma = 0; sigma < sigmaModels.size(); sigma++) {
            SigmaModel model = sigmaModels.get(sigma);
            if (model.app == appIndex && model.source == nodeVIndex && model.target == nodeUIndex) {
                SIGMA = SIGMA + (1 - model.fraction);
            }
        }
        return SIGMA;
    }

    /*
     * Procedure for assign all overall requests
     * */
    private List<SigmaModel> assignmentProcedure(String placement, HashSet<Integer> alreadyDeployedApps) {
        double totalRequests = paramHandler.totalRequests;
        String[] vm_place_str = placement.split(",");
        int[] vm_place = Arrays.stream(vm_place_str).mapToInt(Integer::parseInt).toArray();
        int choosen_Mec;
        int index_choosen;
        double umega_min;
        double Rm_v;
        double umega = 0;
        int INDEX = 0; // esharegar be placement
        paramHandler.vm_placement = placement;
        List<SigmaModel> migratedRequests = new ArrayList<>();
        paramHandler.initializeCapacityOfMEC();
        while (totalRequests > 0) {
            for (int appIndex = 0; appIndex < alreadyDeployedApps.size(); appIndex++) {

                for (int nodeIndex = 0; nodeIndex < graph.nodeNum; nodeIndex++) {
                    choosen_Mec = -1;
                    index_choosen = -1;
                    umega_min = Double.POSITIVE_INFINITY;
                    Rm_v = 0;
                    Rm_v = paramHandler.calculateRequestOfAppInRegionV(appIndex, nodeIndex);
                    for (int vmIndex = 0; vmIndex < numOfVRCPerApp; vmIndex++) {
                        int selectedMEC = vm_place[INDEX + vmIndex];

                        umega = 0;
                        int state = -1;
                        boolean CanChangeChooseMec = false;
                        for (int edgeIndex = 0; edgeIndex < graph.linkNum; edgeIndex++) {
                            state = paramHandler.isEdgeVtoN(nodeIndex, selectedMEC, edgeIndex);
                            if (state != -1) {
                                CanChangeChooseMec = true;
                                umega = umega + paramHandler.calculateEdgeDelayForApp(appIndex,
                                        graph.edgeModelList.get(edgeIndex).distance) * state;
                            }
                        }
                        if (paramHandler.getCapacityOfMEC(vmIndex, appIndex) > 0 && umega < umega_min) {
                            if (CanChangeChooseMec) {
                                choosen_Mec = selectedMEC;
                                index_choosen = vmIndex;
                                umega_min = umega;

                            }
                        }
                    }
                    if (choosen_Mec != -1 && totalRequests > 0) {
                        Rm_v = Rm_v - 1;
                        //System.out.println(Rm_v);
                        totalRequests--;
                        paramHandler.updateCapacityOfMEC(index_choosen, appIndex);
                        double total = paramHandler.calculateRequestOfAppInRegionV(appIndex, nodeIndex);
                        if (nodeIndex != choosen_Mec)
                            migratedRequests = updateSigmaModelU_V(migratedRequests, nodeIndex, choosen_Mec, total, appIndex);
                        //find sigma
                    } else {
                        Rm_v = 0;
                        //System.out.println(Rm_v);
                        totalRequests--;
                    }


                }

                if (INDEX < alreadyDeployedApps.size() - 1)
                    INDEX = INDEX + numOfVRCPerApp;
                else INDEX = 0;
            }


        }// end of while
        System.out.println(placement);
        return migratedRequests;
    }

    /*
     * Update the sigma model with new selected mec
     * */
    private List<SigmaModel> updateSigmaModelU_V(List<SigmaModel> migratedRequests, int nodeIndex, int selectedMEC, double total, int appIndex) {

        for (int sigmaIndex = 0; sigmaIndex < migratedRequests.size(); sigmaIndex++) {
            SigmaModel sigmaModel = migratedRequests.get(sigmaIndex);
            if (sigmaModel.source == nodeIndex && sigmaModel.target == selectedMEC && sigmaModel.app == appIndex) {
                migratedRequests.get(sigmaIndex).fraction = sigmaModel.fraction + (1 / total);
               // if (migratedRequests.get(sigmaIndex).fraction >1) throw new IllegalArgumentException();
                return migratedRequests;
            }
        }
        if (migratedRequests.size() == 0) {
            SigmaModel sigma = new SigmaModel();
            sigma.source = nodeIndex;
            sigma.target = selectedMEC;
            sigma.app = appIndex;
            sigma.fraction = 1 / total;
            migratedRequests.add(sigma);
            return migratedRequests;
        } else {
            SigmaModel sigma = new SigmaModel();
            sigma.source = nodeIndex;
            sigma.target = selectedMEC;
            sigma.app = appIndex;
            sigma.fraction = 1 / total;
            migratedRequests.add(sigma);
            return migratedRequests;

        }
    }

    // Placement Enumeration--------------------------------------------------------------------------------------------
    private List<String> initialAllPlacements(int numOfVRCPerApp, int numOfApps, GraphModel graph) {

        // System.out.println(enumeratePlacementCounts());
        //---------------------------------
        java.util.List<java.util.List> all = new ArrayList<>();

        for (int j = 0; j < numOfApps; j++) {

            placementsForSingleApp = new ArrayList<>();

            Combinatorics.combinations(graph.nodeNum, numOfVRCPerApp)
                    .stream()
                    .map(Simulation2::combinChangeIntToString)
                    .forEach(Simulation2::combinWritePlacementToList);
            all.add(placementsForSingleApp);


        }

        List<VRCnodeModel> list = all.get(0);
        VRCnodeModel model;
        VRCnodeModel[] set1 = new VRCnodeModel[list.size()];

        for (int i = 0; i < list.size(); i++) { // firstApp
            model = list.get(i);
            set1[i] = model;
        }
        List<VRCnodeModel> list2 = all.get(1);
        VRCnodeModel model2;
        VRCnodeModel[] set2 = new VRCnodeModel[list2.size()];
        for (int i = 0; i < list2.size(); i++) { // second App
            model2 = list2.get(i);
            set2[i] = model2;
        }
        allVMS = new ArrayList<>();
        Combinatorics.tuples(set1, set2)
                .stream()
                .map(Simulation2::tupleCombine)
                .forEach(Simulation2::tupleWriteToList);
        return allVMS;
    }

    private static void tupleWriteToList(String numbers) {
        allVMS.add(numbers);
    }

    private static String tupleCombine(VRCnodeModel[] vrCnodeModels) {
        String allVms = "";
        for (int i = 0; i < vrCnodeModels.length; i++) {
            VRCnodeModel model = vrCnodeModels[i];
            for (int j = 0; j < model.map.size(); j++) {
                long vm_placement = model.map.get(j);
                allVms = allVms + vm_placement + ",";

            }

        }
        String[] arr = allVms.split(",");
        return allVms;
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

    private long enumeratePlacementCounts() {
        long nodes = graph.nodeNum;
        long multi = 1;
        for (int i = 1; i <= numOfVRCPerApp; i++) {
            multi = multi * nodes;
            nodes--;
        } // enumerate places form VRCs in utils.Graph
        multi = (long) Math.pow(multi, numOfApps);
        return multi;
    }

    //Latency Aware Heuristic Placement Algo ---------------------------------------------------------------------------

    /*
     * Algorithm 2 in paper
     * */
    public double latencyAwareHeuristicPlacementAlgorithm() {
        ArrayList<List<Integer>> candidateSetArray = new ArrayList<>();
        for (int appIndex = 0; appIndex < numOfApps; appIndex++) {
            List<Integer> candidateMecSet = initializeCandidateMec(); // H-m
            candidateSetArray.add(candidateMecSet);
        }
        ArrayList<List<Integer>> placementCaseArray = new ArrayList<>();
        for (int appIndex = 0; appIndex < numOfApps; appIndex++) {
            List<Integer> placementSet = new ArrayList<>(); // X-m
            placementCaseArray.add(placementSet);
        }
        ArrayList<Double> processingDelay = initializeProcessingDelay();
        HashSet<Integer> alreadyDeployedApps = new HashSet<>(numOfApps); // M`

        List<SigmaModel> sigmaList = new ArrayList<>();
        List<Double> serviceTimes = new ArrayList<>();
        for (int appIndex = 0; appIndex < numOfApps; appIndex++) {

            for (int vmIndex = 0; vmIndex < numOfVRCPerApp; vmIndex++) {
                int choosenMec = searchForOneOptimalPlacement(candidateSetArray.get(appIndex), appIndex, sigmaList, serviceTimes);
                //---------------
                placementCaseArray.get(appIndex).add(choosenMec);
                //------------------
                List<Integer> candidate = candidateSetArray.get(appIndex);
                int index = candidate.indexOf(choosenMec);
                candidateSetArray.get(appIndex).remove(index);
            }
            alreadyDeployedApps.add(appIndex);
            // assignment process 1
            String placement = makePlacement(placementCaseArray);
            sigmaList = assignmentProcedure(placement, alreadyDeployedApps);

            serviceTimes = new ArrayList<>();
            for (int serviceIndex = 0; serviceIndex < graph.nodeNum; serviceIndex++) {
                serviceTimes.add(paramHandler.calculateServerDelay(serviceIndex, sigmaList));
            }
        }// end for app index
        //overall request assignments
        String placement = makePlacement(placementCaseArray);
        List<SigmaModel> sigmaModelList = assignmentProcedure(placement, alreadyDeployedApps);
        return calculateTimeAverage(sigmaModelList);
    }

    /*
     * Procedure 2 in Paper
     * */
    private int searchForOneOptimalPlacement(List<Integer> candidateSet, int appIndex, List<SigmaModel> sigmaModels, List<Double> serviceTimes) {
        List<Double> avrgs = new ArrayList<>(candidateSet.size());
        int choosenMec = -1;

        for (int u = 0; u < candidateSet.size(); u++) {
            double requestOfApp = 0;
            double T_net = 0;
            if (sigmaModels.size() == 0) { // if the size is zero it means that it is first app
                // i am not sure about Sigma status here. i set them all one
                for (int sigmaV = 1; sigmaV < graph.nodeNum; sigmaV++) {
                    SigmaModel model = new SigmaModel();
                    model.source = sigmaV;
                    model.target = candidateSet.get(u);
                    model.fraction = 1;
                    sigmaModels.add(model);
                }
                for (int service = 0; service < candidateSet.size(); service++) {
                    serviceTimes.add(0.0);
                }
            }
            for (int v = 0; v < candidateSet.size(); v++) {
                requestOfApp = requestOfApp + paramHandler.calculateRequestOfAppInRegionV(appIndex, candidateSet.get(v));
                T_net = T_net + paramHandler.calculateNetworkDelayBetweenTwoRegions(candidateSet.get(v), candidateSet.get(u), appIndex, sigmaModels);
            }

            double T_avg = T_net / requestOfApp + serviceTimes.get(u);

            avrgs.add(u,T_avg);

        }
        double T_min = Double.POSITIVE_INFINITY;
        for (int u = 0; u < candidateSet.size() ; u ++){
            if (avrgs.get(u) < T_min) {
                T_min = avrgs.get(u);
                choosenMec = candidateSet.get(u);
            }
        }

        return choosenMec;
    }

    /*
     * make string of placement
     * */
    private String makePlacement(ArrayList<List<Integer>> placementCaseArray) {
        String place = "";
        for (int p = 0; p < placementCaseArray.size(); p++) {
            List<Integer> hashSet = placementCaseArray.get(p);
            for (Integer vm_place : hashSet) {
                place = place + vm_place + ",";
            }
        }
        return place;
    }

    /*
     * initialize first processing delays with 0
     * */
    private ArrayList<Double> initializeProcessingDelay() {

        ArrayList<Double> processArr = new ArrayList<>();
        for (int mecIndex = 0; mecIndex < graph.nodeNum; mecIndex++) {
            processArr.add(0.0);
        }
        return null;
    }

    /*
     * initialize candidate mec servers to deploy VRCs
     * */
    private List<Integer> initializeCandidateMec() {
        List<Integer> list = new ArrayList<>();
        for (int mecIndex = 0; mecIndex < graph.nodeNum; mecIndex++) {
            list.add(mecIndex);
        }
        return list;
    }

    //------Clustering Enhanced Heuristic Placement ---------------------------------------
}
