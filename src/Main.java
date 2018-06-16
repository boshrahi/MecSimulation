import utils.Graph;

public class Main {

    public static void main(String[] args) {

        Simulation2 simulation = new Simulation2(Graph.SAGO,2,36,2);  // alan faqat baraye 2 ta app javab midahad
        double T_MIN = simulation.optimalEnumerationPlacementAlgorithm();
    }

}
