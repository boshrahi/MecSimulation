import utils.Graph;

public class Main {

    public static void main(String[] args) {

        Simulation2 simulation = new Simulation2(Graph.NOEL, 2, 38, 2);  // just works for 2 apps for now
//        double T_MIN_OPEA = simulation.optimalEnumerationPlacementAlgorithm();
//        System.out.println("T_MIN_OPEA : ------>  " + T_MIN_OPEA);


        double T_MIN_LAHPA = simulation.latencyAwareHeuristicPlacementAlgorithm();
        System.out.println("T_MIN_LAHPA : ------>  " + T_MIN_LAHPA);



        double T_MIN_CEHPA = simulation.clusteringEnhancedHeuristicPlacementAlgorithm();
        System.out.println("T_MIN_CEHPA : ------>  " + T_MIN_CEHPA);

        long time = System.currentTimeMillis();

        double T_MIN_SEHPA = simulation.substitutionEnhancedHeuristicPlacementAlgorithm();
        System.out.println("T_MIN_SEHPA : ------>  " + T_MIN_SEHPA);

        long time2 = System.currentTimeMillis();
        System.out.println(time2 - time);
    }
}
