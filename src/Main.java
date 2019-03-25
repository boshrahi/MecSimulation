import cc.redberry.combinatorics.Combinatorics;
import model.GraphModel;
import model.VRCnodeModel;
import utils.Graph;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {

    private static List<VRCnodeModel> placementsForSingleApp;
    private static double T_MIN = Double.POSITIVE_INFINITY;
    private static double T_AVG = 0;

    private static String finalPLACEMENT = "";

    public static void main(String[] args) throws IOException {

        final int numOfApps = 2;
        int numOfVRCPerApp = 2;

        String graph;
        List<String> list;
        Path file = Paths.get("results_1" + ".txt");
        double T_MIN_LAHPA = 0;
        double T_MIN_CEHPA = 0;
        double T_MIN_SEHPA = 0;


        graph = Graph.TEST;
        Simulation2 simulation = new Simulation2(graph, numOfVRCPerApp, 1, numOfApps);
        initialAllPlacementsAndCalcOptimalTimes(numOfVRCPerApp, numOfApps, graph, 1);
        TwoHop twoHop = new TwoHop(simulation);
        double time = twoHop.TwoHopAlgorithm();
        OneHop oneHop = new OneHop(simulation);
        System.out.println("Two Hop response time : " + time);
        double one_hop_time = oneHop.OneHopAlgorithm();
        System.out.println("One Hop response time : " + one_hop_time);
        T_MIN_LAHPA = simulation.latencyAwareHeuristicPlacementAlgorithm();
        System.out.println("T_MIN_LAHPA : ------>  " + T_MIN_LAHPA);
        T_MIN_CEHPA = simulation.clusteringEnhancedHeuristicPlacementAlgorithm();
        System.out.println("T_MIN_CEHPA : ------>  " + T_MIN_CEHPA);
        T_MIN_SEHPA = simulation.substitutionEnhancedHeuristicPlacementAlgorithm();
        System.out.println("T_MIN_SEHPA : ------>  " + T_MIN_SEHPA);
        list = Arrays.asList("VRC number : " + numOfVRCPerApp + " L_Time : " + T_MIN_LAHPA + "\n" +
                " C_Time : " + T_MIN_LAHPA + "\n" + "S_Time : "+ T_MIN_SEHPA + "\n" +
                "OneHop : " + one_hop_time + "\n" );
        Files.write(file, list, StandardOpenOption.APPEND);
        //------------------------------------------------------------------------------------------
//        simulation = new Simulation2(graph, numOfVRCPerApp, 2, numOfApps);
//        initialAllPlacementsAndCalcOptimalTimes(numOfVRCPerApp, numOfApps, graph, 2);
//        oneHop = new OneHop(simulation);
//        one_hop_time = oneHop.OneHopAlgorithm();
//        System.out.println("One Hop response time : " + one_hop_time);
//        T_MIN_LAHPA = simulation.latencyAwareHeuristicPlacementAlgorithm();
//        System.out.println("T_MIN_LAHPA : ------>  " + T_MIN_LAHPA);
//        T_MIN_CEHPA = simulation.clusteringEnhancedHeuristicPlacementAlgorithm();
//        System.out.println("T_MIN_CEHPA : ------>  " + T_MIN_CEHPA);
//        T_MIN_SEHPA = simulation.substitutionEnhancedHeuristicPlacementAlgorithm();
//        System.out.println("T_MIN_SEHPA : ------>  " + T_MIN_SEHPA);
//        list = Arrays.asList("VRC number : " + numOfVRCPerApp + " L_Time : " + T_MIN_LAHPA + "\n" +
//                " C_Time : " + T_MIN_LAHPA + "\n" + "S_Time : "+ T_MIN_SEHPA + "\n" +
//                "OneHop : " + one_hop_time + "\n" );
//        Files.write(file, list, StandardOpenOption.APPEND);
//        //------------------------------------------------------------------------------------------
//        simulation = new Simulation2(graph, numOfVRCPerApp, 3, numOfApps);
//        initialAllPlacementsAndCalcOptimalTimes(numOfVRCPerApp, numOfApps, graph, 3);
//        oneHop = new OneHop(simulation);
//        one_hop_time = oneHop.OneHopAlgorithm();
//        System.out.println("One Hop response time : " + one_hop_time);
//        T_MIN_LAHPA = simulation.latencyAwareHeuristicPlacementAlgorithm();
//        System.out.println("T_MIN_LAHPA : ------>  " + T_MIN_LAHPA);
//        T_MIN_CEHPA = simulation.clusteringEnhancedHeuristicPlacementAlgorithm();
//        System.out.println("T_MIN_CEHPA : ------>  " + T_MIN_CEHPA);
//        T_MIN_SEHPA = simulation.substitutionEnhancedHeuristicPlacementAlgorithm();
//        System.out.println("T_MIN_SEHPA : ------>  " + T_MIN_SEHPA);
//        list = Arrays.asList("VRC number : " + numOfVRCPerApp + " L_Time : " + T_MIN_LAHPA + "\n" +
//                " C_Time : " + T_MIN_LAHPA + "\n" + "S_Time : "+ T_MIN_SEHPA + "\n" +
//                "OneHop : " + one_hop_time + "\n" );
//        Files.write(file, list, StandardOpenOption.APPEND);
//        //-------------------------------------------------------------------------------------------
//        simulation = new Simulation2(graph, numOfVRCPerApp, 4, numOfApps);
//        initialAllPlacementsAndCalcOptimalTimes(numOfVRCPerApp, numOfApps, graph, 4);
//        oneHop = new OneHop(simulation);
//        one_hop_time = oneHop.OneHopAlgorithm();
//        System.out.println("One Hop response time : " + one_hop_time);
//        T_MIN_LAHPA = simulation.latencyAwareHeuristicPlacementAlgorithm();
//        System.out.println("T_MIN_LAHPA : ------>  " + T_MIN_LAHPA);
//        T_MIN_CEHPA = simulation.clusteringEnhancedHeuristicPlacementAlgorithm();
//        System.out.println("T_MIN_CEHPA : ------>  " + T_MIN_CEHPA);
//        T_MIN_SEHPA = simulation.substitutionEnhancedHeuristicPlacementAlgorithm();
//        System.out.println("T_MIN_SEHPA : ------>  " + T_MIN_SEHPA);
//        list = Arrays.asList("VRC number : " + numOfVRCPerApp + " L_Time : " + T_MIN_LAHPA + "\n" +
//                " C_Time : " + T_MIN_LAHPA + "\n" + "S_Time : "+ T_MIN_SEHPA + "\n" +
//                "OneHop : " + one_hop_time + "\n" );
//        Files.write(file, list, StandardOpenOption.APPEND);
//        //--------------------------------------------------------------------------------------------
//        simulation = new Simulation2(graph, numOfVRCPerApp, 10, numOfApps);
//        initialAllPlacementsAndCalcOptimalTimes(numOfVRCPerApp, numOfApps, graph, 10);
//        oneHop = new OneHop(simulation);
//        one_hop_time = oneHop.OneHopAlgorithm();
//        System.out.println("One Hop response time : " + one_hop_time);
//        T_MIN_LAHPA = simulation.latencyAwareHeuristicPlacementAlgorithm();
//        System.out.println("T_MIN_LAHPA : ------>  " + T_MIN_LAHPA);
//        T_MIN_CEHPA = simulation.clusteringEnhancedHeuristicPlacementAlgorithm();
//        System.out.println("T_MIN_CEHPA : ------>  " + T_MIN_CEHPA);
//        T_MIN_SEHPA = simulation.substitutionEnhancedHeuristicPlacementAlgorithm();
//        System.out.println("T_MIN_SEHPA : ------>  " + T_MIN_SEHPA);
//        list = Arrays.asList("VRC number : " + numOfVRCPerApp + " L_Time : " + T_MIN_LAHPA + "\n" +
//                " C_Time : " + T_MIN_LAHPA + "\n" + "S_Time : "+ T_MIN_SEHPA + "\n" +
//                "OneHop : " + one_hop_time + "\n" );
//        Files.write(file, list, StandardOpenOption.APPEND);
        //----------------------------------------------------------------------------------------------
//        long startTime = System.nanoTime();
//.         ....your program....
//        long endTime = System.nanoTime();
//        long totalTime = endTime - startTime;
//        System.out.println(totalTime);

    }

    // Placement Enumeration--------------------------------------------------------------------------------------------
    public static void initialAllPlacementsAndCalcOptimalTimes(int numOfVRCPerApp, int numOfApps, String graphS, int numberOfUsers) throws IOException {

        Graph graph = new Graph(graphS);
        GraphModel graphModel = graph.getGraphModel();
        List<String> list;
        T_MIN = Double.POSITIVE_INFINITY;
        T_AVG = 0;
        Path file = Paths.get("results_1" + ".txt");


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
                .forEach(numbers -> tupleWriteToList(numbers, numOfVRCPerApp, numberOfUsers, numOfApps, graphS));
        // calculate times of algorithms and find placements
        System.out.println("T_MIN_OPEA_" + graphS + " : ------>  " + T_MIN);
        System.out.println("Optiaml Placement_" + graphS + " : " + finalPLACEMENT);

        list = Arrays.asList("T_MIN_OPEA_" + graphS + " : ------>  " + T_MIN + " " +
                "Optiaml Placement_" + graphS + " : " + finalPLACEMENT + " vrc : " + numOfVRCPerApp + "\n");
        Files.write(file, list, StandardOpenOption.APPEND);


    }

    private static void tupleWriteToList(String numbers, int numOfVRCPerApp, int numOfUsers, int numOfApps, String graph) {
//        final int numOfVRCPerApp = 10;
//        final int numOfUsers = 1000;
//        final int numOfApps = 2;
//        final String graph = Graph.NOEL;

        //optimal----------------------------------------------------------------
        Simulation2 simulation = new Simulation2(graph, numOfVRCPerApp, numOfUsers, numOfApps);  // just works for 2 apps for now
        T_AVG = simulation.optimalEnumerationPlacementAlgorithm(numbers);
        if (T_AVG <= T_MIN) {
            T_MIN = T_AVG;
            //System.out.println(T_MIN);
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
