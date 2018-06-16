import cc.redberry.combinatorics.Combinatorics;
import model.FinalAssignmentModel;
import model.GraphModel;
import model.VRCnodeModel;
import utils.Graph;
import utils.ParameterHandler;

import java.util.*;

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

public class Simulation2 {

    private String graphType;
    private static int numOfVRCPerApp;
    private int numOfUsers;
    private static int numOfApps;
    private GraphModel graph;
    private static List<VRCnodeModel> placementsForSingleApp;
    private static List<String> allVMS;
    //------------------------request

    double totalRequests;
    //------------------- demand of every request of app
    double demandOfRequest = 2;


    Simulation2(String graphType, int numOfVRCPerApp, int numOfUsers, int numOfApps) {

        this.graphType = graphType;
        Graph graph = new Graph(graphType);
        this.graph = graph.getGraphModel();
        this.numOfApps = numOfApps;
        this.numOfUsers = numOfUsers;
        this.numOfVRCPerApp = numOfVRCPerApp;
        ParameterHandler paramHandler = new ParameterHandler(numOfVRCPerApp,numOfUsers,numOfApps,graph.getGraphModel());
    }
    double optimalEnumerationPlacementAlgorithm() {

        List<String> all = initialAllPlacements(numOfVRCPerApp,numOfApps,graph);
        double T_MIN = 0 ;
        for (String placement : all) {
            //assignmentProcedure(placement);
        }


        return 0;
    }

    // Placement Enumeration--------------------------------------------------------------------------------------------
    private List<String> initialAllPlacements(int numOfVRCPerApp, int numOfApps, GraphModel graph) {

        // System.out.println(enumeratePlacementCounts());
        //---------------------------------
        java.util.List<java.util.List> all = new ArrayList<>();

        for (int j = 0; j < numOfApps; j++) {

            placementsForSingleApp = new ArrayList<>();

            Combinatorics.combinationsWithPermutations(graph.nodeNum, numOfVRCPerApp)
                    .stream()
                    .map(Simulation2::combinChangeIntToString)
                    .forEach(Simulation2::combinWritePlacementToList);
            all.add(placementsForSingleApp);



        }

        List<VRCnodeModel> list =  all.get(0);
        VRCnodeModel model ;
        VRCnodeModel[] set1 = new VRCnodeModel[list.size()];

        for (int i = 0 ; i <list.size();i++){ // firstApp
            model = list.get(i);
            set1[i] = model;
        }
        List<VRCnodeModel> list2 =  all.get(1);
        VRCnodeModel model2 ;
        VRCnodeModel[] set2 = new VRCnodeModel[list2.size()];
        for (int i = 0 ; i <list2.size();i++){ // second App
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
        for (int i = 0 ; i< vrCnodeModels.length ; i++){
           VRCnodeModel model = vrCnodeModels[i];
           for (int j= 0 ; j < model.map.size() ; j ++ ){
               long vm_placement = model.map.get(j);
               allVms = allVms + vm_placement +",";

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
}
