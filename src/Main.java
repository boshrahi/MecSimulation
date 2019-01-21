import cc.redberry.combinatorics.Combinatorics;
import model.GraphModel;
import model.VRCnodeModel;
import utils.Graph;

import java.util.ArrayList;
import java.util.List;

public class Main {

    private static List<VRCnodeModel> placementsForSingleApp;
    private static double T_MIN = Double.POSITIVE_INFINITY;
    private static double T_AVG = 0;

    private static String finalPLACEMENT = "";

    public static void main(String[] args) {

        final int numOfVRCPerApp = 15;
        final int numOfUsers = 1000;
        final int numOfApps = 2;
        final String graph = Graph.NOEL;
        initialAllPlacementsAndCalcOptimalTimes(numOfVRCPerApp, numOfApps, graph, numOfUsers);

        Simulation2 simulation = new Simulation2(graph, numOfVRCPerApp, numOfUsers, numOfApps);
        double T_MIN_LAHPA = 0;
        double T_MIN_CEHPA = 0;
        double T_MIN_SEHPA = 0;
        //LAHPA-------------------------------------------------------------------
        T_MIN_LAHPA = simulation.latencyAwareHeuristicPlacementAlgorithm();
        //CEHPA-------------------------------------------------------------------
        T_MIN_CEHPA = simulation.clusteringEnhancedHeuristicPlacementAlgorithm();
        //SEHPA-------------------------------------------------------------------
        T_MIN_SEHPA = simulation.substitutionEnhancedHeuristicPlacementAlgorithm();

        System.out.println("T_MIN_LAHPA : ------>  " + T_MIN_LAHPA);
        System.out.println("T_MIN_CEHPA : ------>  " + T_MIN_CEHPA);
        System.out.println("T_MIN_SEHPA : ------>  " + T_MIN_SEHPA);

    }

    // Placement Enumeration--------------------------------------------------------------------------------------------
    public static void initialAllPlacementsAndCalcOptimalTimes(int numOfVRCPerApp, int numOfApps, String graphS, int numberOfUsers) {

        Graph graph = new Graph(graphS);
        GraphModel graphModel = graph.getGraphModel();

        // first app ---------------------------
        placementsForSingleApp = new ArrayList<>();
        Combinatorics.combinations(graphModel.nodeNum, numOfVRCPerApp)
                .stream()
                .map(Main::combinChangeIntToString)
                .forEach(Main::combinWritePlacementToList);
        VRCnodeModel model;
        VRCnodeModel[] set1 = new VRCnodeModel[placementsForSingleApp.size()];

        for (int i = 0; i < placementsForSingleApp.size(); i++) { // firstApp
            model = placementsForSingleApp.get(i);
            set1[i] = model;
        }
        // second app ---------------------------------
        placementsForSingleApp = new ArrayList<>();
        Combinatorics.combinations(graphModel.nodeNum, numOfVRCPerApp)
                .stream()
                .map(Main::combinChangeIntToString)
                .forEach(Main::combinWritePlacementToList);

        VRCnodeModel model2;
        VRCnodeModel[] set2 = new VRCnodeModel[placementsForSingleApp.size()];
        for (int i = 0; i < placementsForSingleApp.size(); i++) { // second App
            model2 = placementsForSingleApp.get(i);
            set2[i] = model2;
        }

        Combinatorics.tuples(set1, set2)
                .stream()
                .map(Main::tupleCombine)
                .forEach(Main::tupleWriteToList);
        // calculate times of algorithms and find placements
        System.out.println("T_MIN_OPEA : ------>  " + T_MIN);
        System.out.println("Optiaml Placement : " + finalPLACEMENT);


    }

    private static void tupleWriteToList(String numbers) {
        final int numOfVRCPerApp = 10;
        final int numOfUsers = 1000;
        final int numOfApps = 2;
        final String graph = Graph.NOEL;

        //optimal----------------------------------------------------------------
        Simulation2 simulation = new Simulation2(graph, numOfVRCPerApp, numOfUsers, numOfApps);  // just works for 2 apps for now
        T_AVG = simulation.optimalEnumerationPlacementAlgorithm(numbers);
        if (T_AVG <= T_MIN) {
            T_MIN = T_AVG;
            System.out.println(T_MIN);
            finalPLACEMENT = numbers;
        }

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
}
