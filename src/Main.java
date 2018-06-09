public class Main {

    public static void main(String[] args) {

        Simulation simulation = new Simulation(Graph.SPIRALIGHT,3,4,2,12,0.5);
        double T_MIN = simulation.optimalEnumerationPlacementAlgorithm();
    }

}
