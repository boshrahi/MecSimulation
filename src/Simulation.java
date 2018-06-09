import cc.redberry.combinatorics.Combinatorics;
import model.GraphModel;
import model.VRCnodeModel;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Boshra
 * @param  : graphType we have 4 type of graphs that user selected -------> G paper
 * @param  : numOfVRCPerApp virtual machine replica per app        -------> k paper
 * @param  : numOfUsers number of overall users                    ------ > N paper
 * @param  : capOfMec capacity of each mec server                  ------ > C paper
 * @param  : numOfAppshow many app we support                     ------ > M paper
 * @param  : alpha ratio of request for application m from the     ------ > a paper
 *              region of mec server u
 *
 *
 *
 *
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


    Simulation(String graphType, int numOfVRCPerApp, int numOfUsers, int numOfApps, double capOfMec, double alpha){

        this.graphType = graphType;
        Graph graph = new Graph(graphType);
        this.graph = graph.getGraphModel();
        this.numOfApps = numOfApps;
        this.numOfUsers = numOfUsers;
        this.numOfVRCPerApp = numOfVRCPerApp;
        this.capOfMec = capOfMec;
        this.alpha = alpha;
    }

    double optimalEnumerationPlacementAlgorithm() {

        //initialize S denote all placements
        List<List> PlacementsForAllApps = initialAllPlacements(numOfVRCPerApp,numOfApps,graph);
        return 0;
    }

    private List<List> initialAllPlacements(int numOfVRCPerApp, int numOfApps, GraphModel graph) {

       // System.out.println(enumeratePlacementCounts());
        //---------------------------------
    List<List> all = new ArrayList<>();

        for (int j = 0 ; j< numOfApps ; j ++){

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
        for (int index = 0 ; index < ints.length ; index++){
            if (index == ints.length-1) newString = newString + ints[index] ;
            else  newString = newString + ints[index] + " " ;
        }
        return newString;
    }

    private static void combinWritePlacementToList(String s) {
        String[] array = s.split(" ");
        VRCnodeModel placement = new VRCnodeModel();
        for (int vrcIndex = 0 ; vrcIndex < array.length; vrcIndex++){
            placement.map.put(vrcIndex + 1,Long.valueOf(array[vrcIndex]));
        }
        placementsForSingleApp.add(placement);

    }

    private  long enumeratePlacementCounts(){
        long nodes = graph.nodeNum;
        long multi = 1 ;
        for (int i=1 ; i <= numOfVRCPerApp ; i++){
            multi = multi * nodes;
            nodes --;
        } // enumerate places form VRCs in Graph
        multi = (long) Math.pow(multi,numOfApps);
        return multi;
    }

}
