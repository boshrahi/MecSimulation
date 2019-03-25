package utils;

import model.EdgeModel;
import model.GraphModel;
import model.SigmaModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * This class handles all math parameters in paper and calculate them
 * number of VRC per application
 * number of applications
 * number of users per region
 * total number of users
 * capacity of virtual machine in mobile edge server per app
 * service rate and service delay
 * cloud delay
 * alpha
 * graph of servers and cloud
 * landa per app
 * total landa
 *
 * @author Boshra
 */

public class ParameterHandler {

    public int numOfVRCPerApp;
    public int numOfApps;
    private String numberOfUsersPerRegion;
    public long totalRequests;
    private final double alpha = 1; //for all
    private final double cloudDelay = 1000; //ms
    final double serviceDelayPerRegion = 0.01; //ms
    private final double serviceRate = 10000; //3010; // 57
    private final String capacityOfMec = "1000"; //should be 1000
    //------------------- demand of every request of app
    private final int demandOfRequest = 2;
    private String landaPerApp;


    private GraphModel graphModel;
    public String vm_placement;

    private List<String> vmCapacityPerApp = new ArrayList<>();

    public ParameterHandler(int numOfVRCPerApp, int numOfUsers, int numOfApps, GraphModel graphModel) {
        this.numOfApps = numOfApps;
        this.numOfVRCPerApp = numOfVRCPerApp;
        this.graphModel = graphModel;
        this.numberOfUsersPerRegion = calculateNumberOfUsersPerRegion(numOfUsers, graphModel);
        this.landaPerApp = calculateLandaPerAppForUsers(numOfApps);
        this.totalRequests = calculateTotalRequests(numberOfUsersPerRegion, landaPerApp);
        initializeCapacityOfMEC();

    }

    public void initializeCapacityOfMEC() {
        vmCapacityPerApp.clear();
        for (int app = 0; app < numOfApps; app++) {
            String perApp = "";
            for (int vm = 0; vm < numOfVRCPerApp; vm++) {
                perApp = perApp + capacityOfMec + ",";
            }
            vmCapacityPerApp.add(perApp);
        }
    }

    public void updateCapacityOfMEC(int whichRegion, int whichApp) {
        String appCap_str = vmCapacityPerApp.get(whichApp);
        String[] arr = appCap_str.split(",");
        int[] appCap = new int[arr.length];
        for (int i = 0; i < arr.length; i++) {
            appCap[i] = Integer.valueOf(arr[i]);
        }
        appCap[whichRegion] = appCap[whichRegion] - getDemandOfApp(whichApp);

        String updatedCap = "";
        for (int i = 0; i < appCap.length; i++) {
            updatedCap = updatedCap + appCap[i] + ",";
        }
        vmCapacityPerApp.remove(whichApp);
        vmCapacityPerApp.add(whichApp, updatedCap);
    }

    private int getDemandOfApp(int whichApp) {
        if (whichApp == 0) return demandOfRequest;
        else if (whichApp == 1) return 2 * demandOfRequest;
        else return 3 * demandOfRequest;
    }

    public double getCapacityOfMEC(int whichRegion, int whichApp) {
        String appCap_str = vmCapacityPerApp.get(whichApp);
        String[] arr = appCap_str.split(",");
        int[] appCap = new int[arr.length];
        for (int i = 0; i < arr.length; i++) {
            appCap[i] = Integer.valueOf(arr[i]);
        }
        return appCap[whichRegion];
    }

    public int isEdgeVtoN(int V, int N, int edgeIndex) {
        if (V == N) return 0; // node == selectedMec

        EdgeModel edgeModel = graphModel.edgeModelList.get(edgeIndex);
        long source = edgeModel.source;
        long target = edgeModel.target;
        if ((source == V && target == N) || (source == N && target == V)) {
            return 1;

        }
        return -1;
    }

    public double calculateDelayOfCloudPerApp(int whichApp) {
        return delayCalculator(whichApp, cloudDelay);
    }

    public double calculateAvrgRequestArrivalRate(int whichApp, int whichRegion, List<SigmaModel> sigmaList) {
        // need Sigma m u-->v here
        int indicator = 0;
        double average = 0;
        String[] users_region = numberOfUsersPerRegion.split(",");
        for (int nodeIndex = 0; nodeIndex < graphModel.nodeNum; nodeIndex++) {
            if (isPlacementHaveQ(whichRegion, vm_placement.split(","))) {
                indicator = 1;
            }
            for (int sigmaIndex = 0; sigmaIndex < sigmaList.size(); sigmaIndex++) {
                SigmaModel model = sigmaList.get(sigmaIndex);
                if (model.source == nodeIndex && model.target == whichRegion && model.app == whichApp) {
                    average = average + indicator * model.fraction * Double.valueOf(users_region[whichRegion])
                            * getLandaPerApp(whichApp) * alpha;
                }
            }
        }
        return average;
    }

    public double calculateServerDelay(int whichRegion, List<SigmaModel> list) {// T s -- v

        double A_region_V = 0;
        double t = 0;
        for (int appIndex = 0; appIndex < numOfApps; appIndex++) {
            A_region_V = A_region_V + calculateAvrgRequestArrivalRate(appIndex, whichRegion, list);
            //System.out.println("A_region_V : " + A_region_V);
        }
        if (serviceRate <= A_region_V) {
            System.out.println("A_region_V : " + A_region_V);
            throw new IllegalArgumentException();
        } else {
            t = 1 / (serviceRate - A_region_V);
            //System.out.println("service time : " + t);
            return t;
        }
    }

    private double delayCalculator(int whichApp, double delay) {
        if (whichApp == 0) return 0.4 * delay;
        else if (whichApp == 1) return 0.6 * delay;
        else if (whichApp == 2) return 0.8 * delay;
        else return delay;
    }

    public long calculateRequestOfAppInRegionV(int whichApp, int whichRegion) {
        //R v-m
        String[] users_region = numberOfUsersPerRegion.split(",");
        long request = (long) (Long.valueOf(users_region[whichRegion]) * alpha * getLandaPerApp(whichApp)); // or getTotalLandaPerApp()
        //double request = Long.valueOf(users_region[whichRegion]) * alpha * getTotalLandaPerApp(whichApp, whichRegion, users_region);
        return request;

    }

    private double getLandaPerApp(int whichApp) {
        // Landa
        String[] landa_app = landaPerApp.split(",");
        double app_landa = Double.parseDouble(landa_app[whichApp]);
        return app_landa;
    }

    private double getTotalLandaPerApp(int whichApp, int whichRegion, String[] users_region) {
        // Landa
        String[] landa_app = landaPerApp.split(",");
        double app_landa = Double.valueOf(landa_app[whichApp]);
        long user_number_in_region = Long.valueOf(users_region[whichRegion]);
        return app_landa * user_number_in_region;
    }

    private long calculateTotalRequests(String numberOfUsersPerRegion, String landaPerApp) {
        String[] users_region = numberOfUsersPerRegion.split(",");
        String[] landa_app = landaPerApp.split(",");
        long total = 0;
        for (int index = 0; index < graphModel.nodeNum; index++) {
            for (int appIndex = 0; appIndex < landa_app.length; appIndex++) {
                total = (long) (total + calculateRequestOfAppInRegionV(appIndex
                        , index));
            }
        }
        return total;
    }

    public long calculateRequestOfApp(int whichApp) {
        String[] users_region = numberOfUsersPerRegion.split(",");
        long total = 0;
        for (int index = 0; index < users_region.length; index++) {
            total = (long) (total + calculateRequestOfAppInRegionV(whichApp
                    , index));
        }
        return total;
    }

    public double calculateEdgeDelayForApp(int whichApp, double delay) {
        return delayCalculator(whichApp, delay);
    }

    public double calculateNetworkDelayBetweenTwoRegions(int fromRegion, int toRegion, int whichApp, List<SigmaModel> sigmaModels) { //Tm v--->u
        double time = 0;
        long distance = graphModel.dijkstra(graphModel.makeGraphMatrix(), fromRegion).shorestDist[toRegion];
        for (int sigma = 0; sigma < sigmaModels.size(); sigma++) {
            SigmaModel model = sigmaModels.get(sigma);
            if (model.source == fromRegion && model.target == toRegion && model.app == whichApp) {
                return model.fraction * calculateRequestOfAppInRegionV(whichApp, fromRegion) * distance;
            }
        }
        return time;

    }

    public double distanceDelay(int fromRegion, int toRegion, int whichApp) {
        double edgeDelay = 0;
        for (int edgeIndex = 0; edgeIndex < graphModel.linkNum; edgeIndex++) {
            edgeDelay = calculateEdgeDelayForApp(whichApp, graphModel.edgeModelList.get(edgeIndex).distance);
            int state = isEdgeVtoN(fromRegion, toRegion, edgeIndex);
            if (state != -1) edgeDelay = edgeDelay + edgeDelay * state;
        }
        return edgeDelay;
    }

    private String calculateLandaPerAppForUsers(int numOfApps) {
//        String landaPerApp = "";
//        Random rand = new Random();
//        int randomNum = (1+ new Random().nextInt(5));
//        for (int i = 0 ; i < numOfApps ; i ++){
//            landaPerApp = landaPerApp + randomNum + ",";
//        }
        String landaPerApp = "2,3"; // for two app for now
        return landaPerApp;
    }

    private String calculateNumberOfUsersPerRegion(int numOfUsers, GraphModel graphModel) {
//        int num_node = graphModel.nodeNum;
//        String userNumber = "";
//        int max = 16;
//        int min = 10;
//        for (int i = 0; i < num_node; i++) {
//
//            int randomNum = new Random().nextInt(max - min + 1) + min;
//            int numberOfUserPerRegion = randomNum;
//            userNumber = userNumber + numberOfUserPerRegion + ",";
//        }
//        return userNumber;
        if (graphModel.graphName.equals(Graph.SPIRALIGHT)) {
            String str = "" + 53 * numOfUsers + "," + 61 * numOfUsers + "," + 50 * numOfUsers + "," + 55 * numOfUsers + "," + 68 * numOfUsers + "," + 61 * numOfUsers +
                    "," + 53 * numOfUsers + "," + 68 * numOfUsers + "," + 58 * numOfUsers + "," + 57 * numOfUsers + "," + 54 * numOfUsers + "," + 52 * numOfUsers +
                    "," + 68 * numOfUsers + "," + 69 * numOfUsers + "," + 69 * numOfUsers;
            return str;
        } else if (graphModel.graphName.equals(Graph.SAGO)) {
            String str = "" + 51 * numOfUsers + "," + 48 * numOfUsers + "," + 51 * numOfUsers + "," + 48 * numOfUsers + "," + 51 * numOfUsers + "," + 50 * numOfUsers +
                    "," + 51 * numOfUsers + "," + 46 * numOfUsers + "," + 56 * numOfUsers + "," + 44 * numOfUsers + "," + 53 * numOfUsers + "," + 60 * numOfUsers +
                    "," + 59 * numOfUsers + "," + 40 * numOfUsers + "," + 43 * numOfUsers + "," + 46 * numOfUsers + "," + 58 * numOfUsers + "," + 49 * numOfUsers;
            return str;

        } else if (graphModel.graphName.equals(Graph.SHENTEL)) {
            String str = "" + 35 * numOfUsers + "," + 39 * numOfUsers + "," + 37 * numOfUsers + "," + 31 * numOfUsers + "," + 33 * numOfUsers + "," + 37 * numOfUsers +
                    "," + 36 * numOfUsers + "," + 40 * numOfUsers + "," + 32 * numOfUsers + "," + 37 * numOfUsers + "," + 34 * numOfUsers + "," + 40 * numOfUsers +
                    "," + 36 * numOfUsers + "," + 37 * numOfUsers + "," + 34 * numOfUsers + "," + 40 * numOfUsers + "," + 36 * numOfUsers + "," + 36 * numOfUsers + "," + 36 * numOfUsers+ "," + 36 * numOfUsers
                    + "," + 37 * numOfUsers+ "," + 34 * numOfUsers+ "," + 40 * numOfUsers+ "," + 32 * numOfUsers+ "," + 39 * numOfUsers+ "," + 37 * numOfUsers+ "," + 35 * numOfUsers+ "," + 34 * numOfUsers+ ","
                    + 34 * numOfUsers;
            return str;

        } else if (graphModel.graphName.equals(Graph.MISSOURI)) {
                    String str = "" + 10 * numOfUsers + "," + 16 * numOfUsers + "," + 15 * numOfUsers + "," + 10 * numOfUsers + "," + 12 * numOfUsers + "," + 10 * numOfUsers +
                    "," + 11 * numOfUsers + "," + 16 * numOfUsers + "," + 15 * numOfUsers + "," + 14 * numOfUsers + "," + 15 * numOfUsers + "," + 14 * numOfUsers +
                    "," + 13 * numOfUsers + "," + 15 * numOfUsers + "," + 14 * numOfUsers + "," + 16 * numOfUsers + "," + 15 * numOfUsers + "," + 13 * numOfUsers + "," + 12 * numOfUsers+ "," + 10 * numOfUsers
                    + "," + 12 * numOfUsers+ "," + 13 * numOfUsers+ "," + 12 * numOfUsers+ "," + 10 * numOfUsers+ "," + 13 * numOfUsers+ "," + 16 * numOfUsers+ "," + 14 * numOfUsers+ "," + 15 * numOfUsers
                    + "," + 11 * numOfUsers + "," + 13 * numOfUsers+ "," + 10 * numOfUsers+ "," + 15 * numOfUsers+ "," + 14 * numOfUsers+ "," + 16 * numOfUsers+ "," + 12 * numOfUsers+ "," + 10 * numOfUsers
                            + "," + 15 * numOfUsers
                    + "," + 12 * numOfUsers+ "," + 10 * numOfUsers+ "," + 10 * numOfUsers+ "," + 12 * numOfUsers+ "," + 11 * numOfUsers+ "," + 10 * numOfUsers+ "," + 12 * numOfUsers+ "," + 10 * numOfUsers
                    + "," + 11 * numOfUsers+ "," + 14 * numOfUsers+ "," + 16 * numOfUsers+ "," + 10 * numOfUsers+ "," + 13 * numOfUsers+ "," + 15 * numOfUsers+ "," + 11 * numOfUsers+ "," + 16 * numOfUsers
                    + "," + 10 * numOfUsers+ "," + 16 * numOfUsers+ "," + 15 * numOfUsers+ "," + 14 * numOfUsers+ "," + 11 * numOfUsers+ "," + 16 * numOfUsers+ "," + 13 * numOfUsers+ "," + 13 * numOfUsers
                    + "," + 15 * numOfUsers+ "," + 12 * numOfUsers+ "," + 13 * numOfUsers+ "," + 13 * numOfUsers+ "," + 16 * numOfUsers+ "," + 14 * numOfUsers+ "," + 16 * numOfUsers;
            ;
            return str;
            // Missouri
//        return "3985,6807,6557,6779,3909,3673,3641,3321,3148,4639,5350,5783,3585,3213,6655,4301,3262,5825,4743,4663,6338,6588,5335,3330,6897,4976,6971," +
//                "6201,5570,4785,4972,6779,5466,3310,5407,4210,4170,5024,3092,6396,4556,4155,3972,4999,4346,3437,3509,6406,4675,6572,4162,4311,4362,4908,6254," +
//                "5885,6857,5947,5472,6254,5455,3885,3437,3843,5813,5759,6817,";//uniform 3000-7000
//        return "483,337,164,170,138,244,441,489,54,279,347,123,104,470,117,295,412,173,441,413,301,499,85," +
//                "134,222,83,62,437,445,109,373,66,82,191,248,174,67,121,349,80,348,88,344,218,395,95,366,420," +
//                "216,421,139,403,90,303,192,220,233,376,407,471,78,366,75,53,478,200,346,"; //uniform 50 500

        } else if (graphModel.graphName.equals(Graph.NOEL)) {
            // Noel
            String str = "" + 50 * numOfUsers + "," + 50 * numOfUsers + "," + 48 * numOfUsers + "," + 40 * numOfUsers + "," + 43 * numOfUsers + "," + 59 * numOfUsers +
                    "," + 69 * numOfUsers + "," + 66 * numOfUsers + "," + 50 * numOfUsers + "," + 60 * numOfUsers + "," + 66 * numOfUsers + "," + 54 * numOfUsers +
                    "," + 68 * numOfUsers + "," + 42 * numOfUsers + "," + 60 * numOfUsers + "," + 55 * numOfUsers + "," + 47 * numOfUsers + "," + 66 * numOfUsers + "," + 48 * numOfUsers;
            return str;
            //return "483,337,164,170,138,244,441,489,54,279,347,123,104,470,117,295,412,173,441,"; //5281 uniform 50 500
            //return "50,50,48,40,43,59,69,66,50,60,66,54,68,42,60,55,47,66,48,"; //1041 uniform 40 70
        }
        return null;

    }

    private double getNumberOfUserPerRegion(int node) {
        // Landa
        String[] users = numberOfUsersPerRegion.split(",");
        double app_landa = Double.parseDouble(users[node]);
        return app_landa;
    }

    public boolean isPlacementHaveQ(int q_server, String[] placement) {
        for (int i = 0; i < placement.length; i++) {
            if (placement[i].equals(String.valueOf(q_server))) return true;
        }
        return false;
    }

    public double getDemandOfNode(int nodeIndex, int appIndex) {
        double numberPerRegion = getNumberOfUserPerRegion(nodeIndex);
        double landaPerApp = getLandaPerApp(appIndex);
        return numberPerRegion * landaPerApp;
    }
}
