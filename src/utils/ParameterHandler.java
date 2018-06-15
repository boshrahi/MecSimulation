package utils;

import model.GraphModel;

import java.util.Random;

public class ParameterHandler {

    int numOfVRCPerApp;
    int numOfUsers;
    int numOfApps;
    double totalRequests;
    final double alpha = 0.8;
    String numberOfUsersPerRegion;
    String landaPerApp;

    GraphModel graphModel;

    public ParameterHandler(int numOfVRCPerApp, int numOfUsers, int numOfApps, GraphModel graphModel){
        this.numOfApps = numOfApps;
        this.numOfUsers = numOfUsers;
        this.numOfVRCPerApp = numOfVRCPerApp;
        this.graphModel = graphModel;
        this.numberOfUsersPerRegion = calculateNumberOfUsersPerRegion(numOfUsers,graphModel);
        this.landaPerApp = calculateLandaPerAppForUsers(numOfApps);
        this.totalRequests = calculateTotalRequests(numberOfUsersPerRegion,landaPerApp);

    }

    public double calculateRequestOfAppInRegionV(int whichApp , int whichRegion){
        //R v-m
        String[] users_region = numberOfUsersPerRegion.split(",");
        double request = Long.valueOf(users_region[whichRegion]) * alpha * getTotalLandaPerApp(whichApp,whichRegion,users_region);
        return request;

    }

    private long getTotalLandaPerApp(int whichApp,int whichRegion,String[] users_region) {
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
        for (int index = 0 ; index < users_region.length ; index++){
            for (int appIndex =0 ; appIndex < landa_app.length;appIndex++){
               total = total + calculateRequestOfAppInRegionV(appIndex
                        ,index);
            }
        }
        return total;
    }


    public double calculateEdgeDelayForApp(int whichApp , long delay){
        int[] params = new int[numOfApps];
        if (whichApp == 0) return 0.4*delay;
        else if (whichApp == 1) return 0.6*delay;
        else if (whichApp == 2) return 0.8*delay;
        else return delay;
    }

    private String calculateLandaPerAppForUsers(int numOfApps) {
        String landaPerApp = "";
        Random rand = new Random();
        int  n =  + 1;
        for (int i = 0 ; i < numOfApps ; i ++){
            landaPerApp = landaPerApp + (rand.nextInt(3) +1) + ",";
        }
        return landaPerApp;
    }

    private String calculateNumberOfUsersPerRegion(int numOfUsers, GraphModel graphModel) {
        int num_node = graphModel.nodeNum;
        // tedad user ha be sorat yeksan dar kol graph tozi shode and
        int numberOfUserPerRegion = numOfUsers / num_node ;
        String userNumber = "";
        for (int i = 0 ; i < num_node ; i ++){
            userNumber = userNumber + numberOfUserPerRegion + ",";
        }
        return userNumber;

    }

}
