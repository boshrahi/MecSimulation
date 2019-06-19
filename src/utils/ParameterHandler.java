package utils;

import model.EdgeModel;
import model.GraphModel;
import model.SigmaModel;

import java.util.ArrayList;
import java.util.Arrays;
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
    private final double serviceRate = 40000; //3010; // 57
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
        int sum=0;
        for (String uss: users_region) {
            int ii = Integer.valueOf(uss);
            sum = sum + ii;
        }

        String[] landa_app = landaPerApp.split(",");
        long total = 0;
        for (int index = 0; index < users_region.length; index++) {
            for (int appIndex = 0; appIndex < numOfApps; appIndex++) {
                total = (long) (total + calculateRequestOfAppInRegionV(appIndex
                        , index));
            }
        }
//        long t =0;
//        for (int appIndex = 0 ; appIndex < numOfApps; appIndex++){
//            t = (long) (t + sum*getLandaPerApp(appIndex));
//        }
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
        String landaPerApp = "3,1"; // for two app for now
        return landaPerApp;
    }

    private String calculateNumberOfUsersPerRegion(int numOfUsers, GraphModel graphModel) {
        int num_node = graphModel.nodeNum;
        String userNumber = "";
        int max = 20;
        int min = 10;
        for (int i = 0; i < num_node; i++) {

            int randomNum = new Random().nextInt(max - min + 1) + min;
            int numberOfUserPerRegion = randomNum;
            userNumber = userNumber + numberOfUserPerRegion + ",";
        }
//       return userNumber;
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
                    + "," + 37 * numOfUsers+ "," + 34 * numOfUsers+ "," + 40 * numOfUsers+ "," + 32 * numOfUsers+ "," + 39 * numOfUsers+ "," + 37 * numOfUsers+ "," + 35 * numOfUsers+ "," + 34 * numOfUsers+ ",";
            return str;

        } else if (graphModel.graphName.equals(Graph.MISSOURI)) {
                    String str =
                    "" + 10 * numOfUsers + "," + 16 * numOfUsers + "," + 15 * numOfUsers + "," + 10 * numOfUsers + "," + 12 * numOfUsers + "," + 10 * numOfUsers +
                    "," + 11 * numOfUsers + "," + 16 * numOfUsers + "," + 15 * numOfUsers + "," + 14 * numOfUsers + "," + 15 * numOfUsers + "," + 14 * numOfUsers +
                    "," + 13 * numOfUsers + "," + 15 * numOfUsers + "," + 14 * numOfUsers + "," + 16 * numOfUsers + "," + 15 * numOfUsers + "," + 13 * numOfUsers +
                    "," + 12 * numOfUsers+ "," + 10 * numOfUsers + "," + 12 * numOfUsers+ "," + 13 * numOfUsers+ "," + 12 * numOfUsers+ "," + 10 * numOfUsers+
                    "," + 13 * numOfUsers+ "," + 16 * numOfUsers+ "," + 14 * numOfUsers+ "," + 15 * numOfUsers + "," + 11 * numOfUsers + "," + 13 * numOfUsers+
                    "," + 10 * numOfUsers+ "," + 15 * numOfUsers+ "," + 14 * numOfUsers+ "," + 16 * numOfUsers+ "," + 12 * numOfUsers+ "," + 10 * numOfUsers +
                    "," + 15 * numOfUsers + "," + 12 * numOfUsers+ "," + 10 * numOfUsers+ "," + 10 * numOfUsers+ "," + 12 * numOfUsers+ "," + 11 * numOfUsers+
                    "," + 10 * numOfUsers+ "," + 12 * numOfUsers+ "," + 10 * numOfUsers + "," + 11 * numOfUsers+ "," + 14 * numOfUsers+ "," + 16 * numOfUsers+
                    "," + 10 * numOfUsers+ "," + 13 * numOfUsers+ "," + 15 * numOfUsers+ "," + 11 * numOfUsers+ "," + 16 * numOfUsers + "," + 10 * numOfUsers+
                    "," + 16 * numOfUsers+ "," + 15 * numOfUsers+ "," + 14 * numOfUsers+ "," + 11 * numOfUsers+ "," + 16 * numOfUsers+ "," + 13 * numOfUsers+
                    "," + 13 * numOfUsers + "," + 15 * numOfUsers+ "," + 12 * numOfUsers+ "," + 13 * numOfUsers+ "," + 13 * numOfUsers+ "," + 16 * numOfUsers+
                     "," + 14 * numOfUsers;
            ;
            return str;

        } else if (graphModel.graphName.equals(Graph.NOEL)) {
            // Noel
            String str = "" + 50 * numOfUsers + "," + 50 * numOfUsers + "," + 48 * numOfUsers + "," + 40 * numOfUsers + "," + 43 * numOfUsers + "," + 59 * numOfUsers +
                    "," + 69 * numOfUsers + "," + 66 * numOfUsers + "," + 50 * numOfUsers + "," + 60 * numOfUsers + "," + 66 * numOfUsers + "," + 54 * numOfUsers +
                    "," + 68 * numOfUsers + "," + 42 * numOfUsers + "," + 60 * numOfUsers + "," + 55 * numOfUsers + "," + 47 * numOfUsers + "," + 66 * numOfUsers + "," + 48 * numOfUsers;
            return str;
        }else if (graphModel.graphName.equals(Graph.G105)){
            return "17,16,12,15,17,20,13,19,13,20,10,14,17,13,12,19,17,20,11,12,16,18,14,11,19,14,16,12,20,12,20,12,12,17,13,10,19,13,10,17,19,17,19,17,13,14,19,13,19,16," +
                    "17,18,15,15,17,13,16,20,11,16,20,20,19,18,15,12,17,16,14,12,16,19,17,10,18,15,17,13,10,12,18,16,15,17,13,15,17,13,16,14,13,10,14,19,14,19,19,19,140,20,20,20,11,10,13,";
        }else if (graphModel.graphName.equals(Graph.G200)){
            return "17,17,18,18,20,11,11,17,19,17,15,15,19,12,14,10,15,19,13,17,13,18,15,16,13,11,13,17,18,18,12,10,12,13,14,12,16,10,14,14,16,14,10,18,14,14,10,14,11,10,19,15,16,14,15,19,18,19,10,13" +
                    ",16,11,14,13,13,14,15,16,12,10,20,18,13,10,14,20,14,20,11,16,14,17,19,10,14,20,11,10,19,12,20,18,13,14,17,20,10,10,16,13,11,17,20,19,15,14,20,16,18,15,12,18,12,13,19,15,15,19,19,17,12" +
                    ",19,17,13,20,10,12,11,18,18,11,18,15,10,14,16,14,13,16,13,18,16,16,12,15,20,10,10,11,17,14,17,16,11,14,16,12,14,12,11,10,10,12,19,12,18,18,15,14,12,20,13,20,11,15,13,13,11,17,15,16,16,14," +
                    "15,13,12,12,11,15,20,18,11,20,19,14,11,19,13,13,13,";
        }else if (graphModel.graphName.equals(Graph.G306)){
            return "12,19,11,11,11,14,19,20,11,17,12,13,16,16,10,17,19,19,19,17,14,16,17,17,17,20,20,14,14,19,10,12,10,11,14,15,18,14,18,16,19,10,17,15,19,16,16,17,16,19,10,18,17,14,19,19" +
                    ",13,19,19,20,13,10,20,15,19,18,11,17,18,14,17,15,11,11,10,20,20,14,17,12,20,18,13,10,10,10,15,17,11,11,17,18,16,19,15,15,11,15,20,11,15,13,20,10,20,13,15,16,19,14,20,15,16" +
                    ",11,10,17,11,19,10,15,12,10,11,13,14,20,10,18,20,11,13,16,16,15,10,16,16,16,14,20,13,19,20,10,19,17,12,19,15,12,14,19,19,18,10,19,19,12,11,17,14,18,13,12,20,16,16,11,20,11," +
                    "15,19,19,19,17,13,13,10,20,12,18,11,20,20,17,20,17,13,12,13,13,16,14,16,20,11,16,19,11,16,15,20,20,20,17,15,20,14,20,11,13,15,15,16,17,13,20,11,19,15,10,11,13,11,18,14,10," +
                    "19,15,15,19,15,12,19,19,19,18,19,15,14,14,16,12,15,15,15,14,12,17,10,18,19,17,10,13,19,10,16,13,11,17,16,16,10,20,12,13,11,17,19,18,20,11,18,18,10,19,20,16,15,14,10,13,15," +
                    "20,14,10,12,18,16,10,12,15,14,17,19,10,10,12,17,12,16,16,20,14,14,";
        }else if (graphModel.graphName.equals(Graph.G406)){
            return "12,15,20,11,11,10,18,18,20,10,18,13,19,19,17,13,18,11,13,20,14,17,20,12,16,17,19,18,18,20,17,20,13,19,20,14,16,15,14,11,16,18,18,19,10,18,13,10,12,17,12,18,13,12,14,15,16,19,13," +
                    "15,20,10,12,15,16,19,17,20,10,12,18,12,10,20,20,20,10,14,13,15,20,11,14,15,13,16,17,16,19,10,19,19,20,12,16,14,11,17,15,12,12,16,10,19,16,20,20,13,12,16,18,16,16,13,17,17,20,13,15," +
                    "20,17,11,16,19,15,10,12,17,16,12,17,11,10,12,16,12,12,19,12,15,10,19,17,20,16,20,10,10,19,14,20,16,13,17,12,16,18,17,20,17,20,16,13,12,11,11,20,14,19,10,16,11,15,13,14,14,15,19,13," +
                    "11,11,17,14,11,18,10,13,11,20,15,20,16,15,12,16,12,16,18,19,20,11,18,16,16,20,20,14,11,12,20,16,20,10,12,13,10,13,20,13,11,15,20,18,20,17,19,20,18,14,14,10,15,18,10,13,17,19,10,18," +
                    "11,20,14,13,13,15,10,16,13,12,18,17,11,15,17,11,15,17,15,13,16,18,18,15,11,11,16,13,10,13,10,14,19,10,11,10,12,11,19,14,17,11,12,18,12,10,20,13,18,11,10,15,16,20,17,18,12,12,10,17," +
                    "14,14,12,11,13,18,10,16,20,12,19,12,16,13,10,15,11,17,12,16,13,18,17,18,12,12,18,20,10,19,12,10,11,16,10,20,14,11,20,12,18,13,14,12,10,16,19,17,15,16,14,16,18,17,13,18,13,16,16,11," +
                    "10,14,11,16,18,19,17,20,20,17,13,10,14,18,13,10,18,18,12,10,11,19,12,12,11,20,11,19,19,17,11,18,14,15,15,19,13,16,10,20,12,16,14,18,12,18,20,";
        }else if (graphModel.graphName.equals(Graph.G512)){
            return "11,13,14,16,10,12,18,10,10,10,19,17,20,11,10,14,10,11,19,11,18,20,15,16,11,14,17,15,17,20,15,18,11,11,11,14,12,17,20,17,20,20,13,12,10,20,16,19,14,19,20,14,14,14,16" +
                    ",10,16,18,18,12,11,11,12,10,14,11,19,17,14,12,12,12,20,20,11,18,18,11,15,19,19,10,19,18,19,13,12,14,11,15,13,17,10,20,10,13,10,12,19,17,14,20,17,10,10,12,19,19,15,12" +
                    ",20,15,15,20,15,12,13,20,11,12,11,10,13,15,16,13,16,17,14,20,20,17,12,20,12,14,12,20,18,16,10,15,16,11,14,10,13,17,18,18,14,11,14,10,16,19,12,13,20,19,20,17,13,15,19," +
                    "19,13,12,12,13,10,11,14,18,17,12,10,15,17,16,18,11,15,14,20,18,15,17,10,11,16,14,11,16,19,10,11,17,14,18,12,19,17,18,18,20,13,13,13,16,20,17,15,18,16,17,11,16,12,10," +
                    "12,19,11,18,14,12,11,16,18,16,13,20,11,14,19,10,13,16,15,10,12,16,11,19,14,16,19,11,12,20,19,16,14,15,18,20,18,11,10,19,12,15,20,16,10,17,14,13,12,11,11,17,20,16,15," +
                    "18,11,10,20,17,17,15,19,20,18,16,13,11,19,18,13,15,18,10,18,19,18,12,16,12,15,15,14,17,18,10,20,17,20,12,16,14,11,17,10,18,18,13,15,17,13,20,19,18,14,12,16,14,17,11," +
                    "14,19,11,19,11,18,16,19,20,12,15,11,16,12,16,19,11,15,10,10,10,15,10,12,18,12,19,18,20,13,13,15,12,18,13,16,18,18,19,19,19,15,17,12,12,18,12,18,15,15,16,18,10,14,18," +
                    "11,16,10,17,16,16,20,12,12,14,18,14,20,20,17,16,17,13,16,16,17,14,15,19,10,16,19,17,10,17,10,17,19,10,17,17,15,12,11,10,19,15,15,12,16,13,12,15,11,20,10,14,17,13,20," +
                    "20,10,16,11,10,14,16,10,14,19,18,20,17,11,13,15,18,18,11,15,20,11,13,17,19,17,17,15,18,12,12,16,20,16,16,20,15,19,10,10,10,12,11,11,13,15,20,15,20,20,18,13,13,19,20," +
                    "14,18,17,20,18,15,11,13,15,19,12,10,14,10,19,16,11,";
        }
        return userNumber;

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

    public double calculateMAXdelayOfRequest(int Vnode, int Unode, int app, List<SigmaModel> sigmaModels) {
        double service_t = 0;
        long distance = graphModel.dijkstra(graphModel.makeGraphMatrix(), Vnode).shorestDist[Unode];
        for (int sigma = 0; sigma < sigmaModels.size(); sigma++) {
            SigmaModel model = sigmaModels.get(sigma);
            if (model.source == Vnode && model.target == Unode && model.app == app) {
                return distance;
            }
        }
        return 0;
    }
}
