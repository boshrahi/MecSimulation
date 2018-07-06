package utils;

import model.EdgeModel;
import model.GraphModel;
import model.SigmaModel;

import java.util.ArrayList;
import java.util.List;

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

    private int numOfVRCPerApp;
    private int numOfUsers;
    private int numOfApps;
    private  String numberOfUsersPerRegion;
    public double totalRequests;
    private final double alpha = 1;
    private final double cloudDelay = 400; //ms
    final double serviceDelayPerRegion = 0.01; //ms
    private final double serviceRate = 57;
    private final String capacityOfMec = "114"; //should be 1000
    //------------------- demand of every request of app
    private final int demandOfRequest = 2;
    private String landaPerApp;


    private GraphModel graphModel;
    public String vm_placement;

    private List<String> vmCapacityPerApp = new ArrayList<>();

    public ParameterHandler(int numOfVRCPerApp, int numOfUsers, int numOfApps, GraphModel graphModel) {
        this.numOfApps = numOfApps;
        this.numOfUsers = numOfUsers;
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
        appCap[whichRegion] = appCap[whichRegion] - demandOfRequest;

        String updatedCap = "";
        for (int i = 0; i < appCap.length; i++) {
            updatedCap = updatedCap + appCap[i] + ",";
        }
        vmCapacityPerApp.remove(whichApp);
        vmCapacityPerApp.add(whichApp, updatedCap);
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

    public double calculateAvrgRequestArrivalRate(int whichApp, int whichRegion, List<SigmaModel> sigmaList, int appIndex) {
        // need Sigma m u-->v here
        int indicator = 0;
        double average = 0;
        String[] users_region = numberOfUsersPerRegion.split(",");
        for (int nodeIndex = 0; nodeIndex < graphModel.nodeNum; nodeIndex++) {
            if (vm_placement.contains(String.valueOf(whichRegion))) {
                indicator = 1;
            }
            for (int sigmaIndex = 0; sigmaIndex < sigmaList.size(); sigmaIndex++) {
                SigmaModel model = sigmaList.get(sigmaIndex);
                if (model.source == nodeIndex && model.target == whichRegion && model.app == appIndex) {
                    average = average + indicator * model.fraction * Double.valueOf(users_region[whichRegion])
                            * getTotalLandaPerApp(appIndex,whichRegion,users_region) * alpha;
                }
            }
        }
        return average;
    }

    public double calculateServerDelay(int whichRegion, List<SigmaModel> list) {// T s -- v

        double A_region_V = 0;
        for (int appIndex = 0; appIndex < numOfApps; appIndex++) {
            A_region_V = A_region_V + calculateAvrgRequestArrivalRate(appIndex, whichRegion, list, appIndex);
            //System.out.println("A_region_V : " + A_region_V);
        }
        if (serviceRate < A_region_V) throw new IllegalArgumentException();
        return 1 / (serviceRate - A_region_V);
    }

    private double delayCalculator(int whichApp, double delay) {
        if (whichApp == 0) return 0.4 * delay;
        else if (whichApp == 1) return 0.6 * delay;
        else if (whichApp == 2) return 0.8 * delay;
        else return delay;
    }

    public double calculateRequestOfAppInRegionV(int whichApp, int whichRegion) {
        //R v-m
        String[] users_region = numberOfUsersPerRegion.split(",");
        double request = Long.valueOf(users_region[whichRegion]) * alpha * getLandaPerApp(whichApp); // or getTotalLandaPerApp()
        //double request = Long.valueOf(users_region[whichRegion]) * alpha * getTotalLandaPerApp(whichApp, whichRegion, users_region);
        return request;

    }

    private int getLandaPerApp(int whichApp) {
        // Landa
        String[] landa_app = landaPerApp.split(",");
        int app_landa = Integer.valueOf(landa_app[whichApp]);
        return app_landa;
    }

    private long getTotalLandaPerApp(int whichApp, int whichRegion, String[] users_region) {
        // Landa
        String[] landa_app = landaPerApp.split(",");
        int app_landa = Integer.valueOf(landa_app[whichApp]);
        long user_number_in_region = Long.valueOf(users_region[whichRegion]);
        return app_landa * user_number_in_region;
    }

    private double calculateTotalRequests(String numberOfUsersPerRegion, String landaPerApp) {
        String[] users_region = numberOfUsersPerRegion.split(",");
        String[] landa_app = landaPerApp.split(",");
        double total = 0;
        for (int index = 0; index < users_region.length; index++) {
            for (int appIndex = 0; appIndex < landa_app.length; appIndex++) {
                total = total + calculateRequestOfAppInRegionV(appIndex
                        , index);
            }
        }
        return total;
    }

    public double calculateEdgeDelayForApp(int whichApp, double delay) {
        return delayCalculator(whichApp, delay);
    }

    public double calculateNetworkDelayBetweenTwoRegions(int fromRegion, int toRegion, int whichApp, List<SigmaModel> sigmaModels) { //Tm v--->u
        double time = 0;
        for (int sigma = 0 ; sigma < sigmaModels.size() ; sigma ++){
            SigmaModel model = sigmaModels.get(sigma);
            if (model.source == fromRegion && model.target == toRegion && model.app == whichApp ){
                return model.fraction * calculateRequestOfAppInRegionV(whichApp, fromRegion) * distanceDelay(fromRegion, toRegion, whichApp);
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
//        int  n =  + 1;
//        for (int i = 0 ; i < numOfApps ; i ++){
//            landaPerApp = landaPerApp + (rand.nextInt(3) +1) + ",";
//        }
        String landaPerApp = "2,3"; // felan 2 ta app darim
        return landaPerApp;
    }

    private String calculateNumberOfUsersPerRegion(int numOfUsers, GraphModel graphModel) {
        int num_node = graphModel.nodeNum;
        // tedad user ha be sorat yeksan dar kol graph tozi shode and
        int numberOfUserPerRegion = numOfUsers / num_node;
        String userNumber = "";
        for (int i = 0; i < num_node; i++) {
            userNumber = userNumber + numberOfUserPerRegion + ",";
        }
        return userNumber;

    }


}
