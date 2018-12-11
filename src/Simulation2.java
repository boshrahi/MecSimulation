import model.*;
import cc.redberry.combinatorics.Combinatorics;
import utils.Graph;
import utils.ParameterHandler;

import java.text.DecimalFormat;
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
            List<SigmaModel> sigmaModels = assignmentProcedure(placement, alreadyDeployedApps, paramHandler.totalRequests);
            //calculate T_AVG equation 8
            T_AVG = calculateTimeAverage(sigmaModels);
            if (T_AVG <= T_MIN) {
                T_MIN = T_AVG;
                System.out.println(T_MIN);
                finalPLACEMENT = placement;
            }
        }
        System.out.println("Optiaml Placement : " + finalPLACEMENT);
        return T_MIN;
    }

    /*
     * calculate time average for equation 8 in paper
     * */
    private double calculateTimeAverage(List<SigmaModel> sigmaModels) {
        double time = 0;
        double T_CLOUD_M;
        double T_SERVICE_V ;
        double AVG_M_V ;

        for (int appIndex = 0; appIndex < numOfApps; appIndex++) {
            double SIGMA = 0;
            double t = 0;
            for (int nodeVIndex = 0; nodeVIndex < graph.nodeNum; nodeVIndex++) {
                double T_M_VU = 0;

                for (int nodeUIndex = 0; nodeUIndex < graph.nodeNum; nodeUIndex++) {
                    //double distance = paramHandler.distanceDelay(nodeVIndex,nodeUIndex,appIndex);
                    T_M_VU = T_M_VU + paramHandler.calculateNetworkDelayBetweenTwoRegions(nodeVIndex, nodeUIndex, appIndex, sigmaModels);
                }
                T_SERVICE_V = paramHandler.calculateServerDelay(nodeVIndex, sigmaModels);
                AVG_M_V = paramHandler.calculateAvrgRequestArrivalRate(appIndex, nodeVIndex, sigmaModels);
                time = time + T_M_VU + (T_SERVICE_V * AVG_M_V);
                if (time == Double.POSITIVE_INFINITY) {
                    System.out.println(T_M_VU);
                    System.out.println(T_SERVICE_V);
                    System.out.println(AVG_M_V);
                    throw new IllegalArgumentException();
                }

                if (T_M_VU < 0 || T_SERVICE_V < 0 || AVG_M_V < 0) {
                    System.out.println("T_M_VU :" + T_M_VU);
                    System.out.println("T_SERVICE_V :" + T_SERVICE_V);
                    System.out.println("AVG_M_V :" + AVG_M_V);
                    throw new IllegalArgumentException();
                }
            }
            T_CLOUD_M = paramHandler.calculateDelayOfCloudPerApp(appIndex);

            for (int nodeVIndex = 0; nodeVIndex < graph.nodeNum; nodeVIndex++) {
                for (int nodeUIndex = 0; nodeUIndex < graph.nodeNum; nodeUIndex++) {
                    SIGMA = SIGMA + getSigmaV_U_M(nodeVIndex, nodeUIndex, appIndex, sigmaModels);
                    if (SIGMA > 1) {
//                        for (int i = 0; i < sigmaModels.size(); i++) {
//                            System.out.println(sigmaModels.get(i).fraction);
//                            System.out.println(SIGMA);
//                        }
                        SIGMA = 1;
                    }
                }
                t = t +  (1 - SIGMA) * paramHandler.calculateRequestOfAppInRegionV(appIndex, nodeVIndex);

            }
            time = time + t * T_CLOUD_M;

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
                SIGMA = model.fraction;
                return SIGMA;
            }
        }
        return SIGMA;
    }

    /*
     * Procedure for assign all overall requests
     * */
    private List<SigmaModel> assignmentProcedure(String placement, HashSet<Integer> alreadyDeployedApps, long numberOfRequests) {
        String[] vm_place_str = placement.split(",");
        int[] vm_place = Arrays.stream(vm_place_str).mapToInt(Integer::parseInt).toArray();
        int choosen_Mec;
        int index_choosen;
        double umega_min;
        double umega = 0;
        int INDEX = 0; // esharegar be placement
        paramHandler.vm_placement = placement;
        List<SigmaModel> migratedRequests = new ArrayList<>();
        paramHandler.initializeCapacityOfMEC();
        long[][] matrix_request = new long[numOfApps][graph.nodeNum];

        for (int row_indicator = 0 ; row_indicator<numOfApps;row_indicator++){
            for (int column_indicator = 0 ; column_indicator<graph.nodeNum;column_indicator++){
                double Rm_v = paramHandler.calculateRequestOfAppInRegionV(row_indicator, column_indicator);
                matrix_request[row_indicator][column_indicator] = (int) Rm_v;
            }
        }
        while (numberOfRequests > 0) {
            //System.out.println(numberOfRequests);
            for (int appIndex = 0; appIndex < alreadyDeployedApps.size(); appIndex++) {

                for (int nodeIndex = 0; nodeIndex < graph.nodeNum; nodeIndex++) {
                        //System.out.println(numberOfRequests);
                        choosen_Mec = -1;
                        index_choosen = -1;
                        umega_min = Double.POSITIVE_INFINITY;
                        ShortestPath shortestPath = graph.dijkstra(graph.makeGraphMatrix(), nodeIndex);

                        for (int vmIndex = 0; vmIndex < numOfVRCPerApp; vmIndex++) {
                            int selectedMEC = vm_place[INDEX + vmIndex];
                            long dist = shortestPath.shorestDist[selectedMEC];
                            umega = paramHandler.calculateEdgeDelayForApp(appIndex, dist);

                            if (paramHandler.getCapacityOfMEC(vmIndex, appIndex) > 0 && umega < umega_min) {
                                choosen_Mec = selectedMEC;
                                index_choosen = vmIndex;
                                umega_min = umega;
                            }
                        }
                        if (choosen_Mec != -1 && matrix_request[appIndex][nodeIndex] > 0) {
                            long total = paramHandler.calculateRequestOfAppInRegionV(appIndex, nodeIndex);
                            matrix_request[appIndex][nodeIndex]--;
                            //System.out.println(matrix_request[appIndex][nodeIndex]);
                            numberOfRequests--;
                            if (numberOfRequests < 0) {
                                System.out.println(numberOfRequests);
                                throw new IllegalArgumentException();
                            }
                            paramHandler.updateCapacityOfMEC(index_choosen, appIndex);
                            migratedRequests = updateSigmaModelU_V(migratedRequests, nodeIndex, choosen_Mec, total, appIndex);
                        } else {

                            numberOfRequests = numberOfRequests -  matrix_request[appIndex][nodeIndex];
                            matrix_request[appIndex][nodeIndex] = 0;
                        }
                }

                if (INDEX < alreadyDeployedApps.size() - 1)
                    INDEX = INDEX + numOfVRCPerApp;
                else INDEX = 0;
            }


        }// end of while
        // Test of Sigma Model*****************
//        System.out.println("Sigma model size : " + migratedRequests.size());
//        for (int test = 0 ; test < migratedRequests.size() ; test++)
//        System.out.println("Source " + migratedRequests.get(test).source +" target "+ migratedRequests.get(test).target +" app "+ migratedRequests.get(test).app + " fraction " + migratedRequests.get(test).fraction);
        // End Test of Sigma Model***************
        return migratedRequests;
    }

    /*
     * Update the sigma model with new selected mec
     * */
    private List<SigmaModel> updateSigmaModelU_V(List<SigmaModel> migratedRequests, int nodeIndex, int selectedMEC, double total, int appIndex) {

        boolean isTest = false;
        DecimalFormat df = new DecimalFormat("#.####");
        for (int sigmaIndex = 0; sigmaIndex < migratedRequests.size(); sigmaIndex++) {
            SigmaModel sigmaModel = migratedRequests.get(sigmaIndex);
            double f1 = sigmaModel.fraction + 1/total;
            double frac = Double.valueOf(df.format(f1));
            if (sigmaModel.source == nodeIndex && sigmaModel.target == selectedMEC && sigmaModel.app == appIndex) {
            //TODO uncomment this
                if (frac>1 && frac <= 1.01){
                    frac = 1;
                }
                sigmaModel.fraction = frac;
                return migratedRequests;
            }
        }
        if (migratedRequests.size() == 0) {
            SigmaModel sigma = new SigmaModel();
            sigma.source = nodeIndex;
            sigma.target = selectedMEC;
            sigma.app = appIndex;
            sigma.fraction = Double.valueOf(df.format(1 / total));
            migratedRequests.add(sigma);
            return migratedRequests;
        } else {
            SigmaModel sigma = new SigmaModel();
            sigma.source = nodeIndex;
            sigma.target = selectedMEC;
            sigma.app = appIndex;
            sigma.fraction = Double.valueOf(df.format(1 / total));
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
        LAHPAmodel lahpAModel = getLAHPAplacement();
        System.out.println("LAHPA placement : " + lahpAModel.placement);
        List<SigmaModel> sigmaModelList = assignmentProcedure(lahpAModel.placement, lahpAModel.alreadyDeployedApps, paramHandler.totalRequests);
        return calculateTimeAverage(sigmaModelList);
    }

    private LAHPAmodel getLAHPAplacement() {
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
            long req_number = 0;
            for (int app = 0; app<alreadyDeployedApps.size() ; app++){
                req_number = req_number + paramHandler.calculateRequestOfApp(app);
            }
            sigmaList = assignmentProcedure(placement, alreadyDeployedApps, req_number);

            serviceTimes = new ArrayList<>();
            for (int serviceIndex = 0; serviceIndex < graph.nodeNum; serviceIndex++) {
                serviceTimes.add(paramHandler.calculateServerDelay(serviceIndex, sigmaList));
            }
        }// end for app index
        //overall request assignments
        String placement = makePlacement(placementCaseArray);
        return new LAHPAmodel(placement, alreadyDeployedApps);
    }

    /*
     * Procedure 2 in Paper
     * */
    private int searchForOneOptimalPlacement(List<Integer> candidateSet, int appIndex, List<SigmaModel> sigmaModels, List<Double> serviceTimes) {
        List<Double> avrgs = new ArrayList<>(candidateSet.size());
        int choosenMec = -1;
        for (int u = 0; u < candidateSet.size(); u++) {
            if (sigmaModels.size() == 0) { // if the size is zero it means that it is first app
                // i am not sure about Sigma status here. i set them all one
                for (int service = 0; service < candidateSet.size(); service++) {
                    serviceTimes.add(0.0);
                }
                for (int sigmaV = 0; sigmaV < graph.nodeNum; sigmaV++) {
                    SigmaModel model = new SigmaModel();
                    model.source = sigmaV;
                    model.target = candidateSet.get(u);
                    model.fraction = 1;
                    model.app = appIndex;
                    sigmaModels.add(model);
                }

            }
            double requestOfApp = 0;
            double T_net = 0;
            for (int v = 0; v < candidateSet.size(); v++) {
                requestOfApp = requestOfApp + paramHandler.calculateRequestOfAppInRegionV(appIndex, candidateSet.get(v));
                T_net = T_net + paramHandler.calculateNetworkDelayBetweenTwoRegions(candidateSet.get(v), candidateSet.get(u), appIndex, sigmaModels);
            }

            double T_avg = (T_net / requestOfApp) + serviceTimes.get(u);
            avrgs.add(u, T_avg);


        }
        double T_min = Double.POSITIVE_INFINITY;
        for (int u = 0; u < candidateSet.size(); u++) {
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

    private String makePlacement(String[] x_tild) {
        String str = "";
        for (int i = 0; i < x_tild.length; i++) {
            str = str + x_tild[i] + ",";
        }
        return str;
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
    /*
     *
     * Require G k N C M alpha L
     * Ensure T_min X_tild
     *
     * */
    public double clusteringEnhancedHeuristicPlacementAlgorithm() {
        CEHPAmodel cehpAmodel = getClusteringPlacement();
        List<SigmaModel> list = assignmentProcedure(cehpAmodel.placement, cehpAmodel.alreadyDeployedApps, paramHandler.totalRequests);
        return calculateTimeAverage(list);

    }

    private List<SigmaModel> makeSigmaModelListForCluster(int vrc, ArrayList<Integer> cluster, int appIndex) {
        List<SigmaModel> list = new ArrayList<>();
        for (int nodeIndex = 0; nodeIndex < cluster.size(); nodeIndex++) {
            SigmaModel sigmaModel = new SigmaModel();
            sigmaModel.fraction = 1;
            sigmaModel.source = cluster.get(nodeIndex);
            sigmaModel.target = vrc;
            sigmaModel.app = appIndex;
            //if (!(sigmaModel.source == sigmaModel.target))
            list.add(sigmaModel);
        }
        return list;
    }


    private String[] getPlacementOfApp(int appIndex, String placement) {

        String[] place = placement.split(",");
        String[] a = new String[numOfVRCPerApp];
        int index = appIndex * numOfVRCPerApp;
        for (int i = 0; i < numOfVRCPerApp; i++) {
            a[i] = place[index];
            index++;
        }
        return a;
    }

    /*
     *
     * Proc #3 of paper that find cluster of each VRCs
     *
     * */
    private List<ArrayList<Integer>> clusteringProcedure(String[] placement) {

        ArrayList<Integer> Y_tild = new ArrayList<>();
        List<ArrayList<Integer>> setOfClusters = new ArrayList<>();

        for (int vm = 0; vm < numOfVRCPerApp; vm++) {
            ArrayList<Integer> Y_tild_vm = new ArrayList<>();
            Y_tild_vm.add(Integer.valueOf(placement[vm]));
            setOfClusters.add(Y_tild_vm);
        }
        graph.getNhops(0); // for test and initial Maxlevel
        for (int l_hop = 1; l_hop <= graph.maxLevel; l_hop++) {
            for (int vmIndex = 0; vmIndex < numOfVRCPerApp; vmIndex++) {

                HashMap<Integer, String> hops = graph.getNhops(Integer.valueOf(placement[vmIndex]));
                String[] vm_of_level_l = hops.get(l_hop).split(",");

                for (int q = 0; q < vm_of_level_l.length; q++) {
                    if (!vm_of_level_l[q].equals("")) {
                        int q_server = Integer.valueOf(vm_of_level_l[q]);
                        if (!Y_tild.contains(q_server) && !isPlacementHaveQ(q_server, placement)) {
                            setOfClusters.get(vmIndex).add(q_server);
                            Y_tild.add(q_server);
                        }
                    }
                }
            }

        }

        return setOfClusters;
    }

    public boolean isPlacementHaveQ(int q_server, String[] placement) {
        for (int i = 0; i < placement.length; i++) {
            if (placement[i].equals(String.valueOf(q_server))) return true;
        }
        return false;
    }


    public double substitutionEnhancedHeuristicPlacementAlgorithm() {
        CEHPAmodel cehpAmodel = getClusteringPlacement();
        double T_min = Double.POSITIVE_INFINITY;
        String[] X = cehpAmodel.placement.split(",");
        String[] X_tild, X_hat = null;
        int INDEX = 0;
        HashSet<Integer> alreadyDeployedApp = new HashSet<>();
        for (int i = 0; i < numOfApps; i++) {
            alreadyDeployedApp.add(i);
        }

        for (int appIndex = 0; appIndex < numOfApps; appIndex++) {
            INDEX = appIndex * numOfVRCPerApp;
            List<ArrayList<Integer>> Y_cluster = clusteringProcedure(getPlacementOfApp(appIndex, cehpAmodel.placement));
            for (int vmIndex = 0; vmIndex < numOfVRCPerApp; vmIndex++) {
                ArrayList<Integer> cluster = Y_cluster.get(vmIndex);
                for (int VRC : cluster) {
                    X_tild = X;
                    X_tild[INDEX] = String.valueOf(VRC);
                    List<SigmaModel> sigmaModels = assignmentProcedure(makePlacement(X_tild), alreadyDeployedApp, paramHandler.totalRequests);
                    double T = calculateTimeAverage(sigmaModels);
                    if (T < T_min) {
                        T_min = T;
                        X_hat = X_tild;
                    }
                }
            }
        }
        List<SigmaModel> sigmaModels = assignmentProcedure(makePlacement(X_hat), alreadyDeployedApp, paramHandler.totalRequests);
        calculateTimeAverage(sigmaModels);
        return T_min;

    }


    private CEHPAmodel getClusteringPlacement() {
        LAHPAmodel lahpAmodel = getLAHPAplacement();
        List<ArrayList<Integer>> X_total = new ArrayList<>();
        HashSet<Integer> alreadyDeployedApp = new HashSet<Integer>();
        String X_total_str = "";
        for (int appIndex = 0; appIndex < numOfApps; appIndex++) {
            ArrayList<Integer> X_Tild = new ArrayList<>(); // each for one app
            String X_tild_str = "";
            String[] appPlace = getPlacementOfApp(appIndex, lahpAmodel.placement);
            List<ArrayList<Integer>> Y_cluster = clusteringProcedure(appPlace);
            for (int vmIndex = 0; vmIndex < numOfVRCPerApp; vmIndex++) {
                double T_MIN = Double.POSITIVE_INFINITY;
                int choosenMec = -1;
                ArrayList<Integer> cluster = Y_cluster.get(vmIndex);
                for (int VRC : cluster) {
                    double T_AVG = calculateTimeAverage(makeSigmaModelListForCluster(VRC, cluster, appIndex));
                    if (T_AVG < T_MIN) {
                        T_MIN = T_AVG;
                        choosenMec = VRC;
                    }
                }
                X_Tild.add(choosenMec);

                if (vmIndex == numOfVRCPerApp - 1) X_tild_str = X_tild_str + choosenMec;
                else X_tild_str = X_tild_str + choosenMec + ",";

            }
            X_total.add(X_Tild); // X_tild_i_m
            X_total_str = X_total_str + X_tild_str + ",";

            alreadyDeployedApp.add(appIndex);
        }
        System.out.println("Clustering placement : " + X_total_str);
        return new CEHPAmodel(X_total_str, alreadyDeployedApp);
    }
}
