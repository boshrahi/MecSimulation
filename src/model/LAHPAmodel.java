package model;

import java.util.HashSet;

public class LAHPAmodel {
    public String placement;
    public HashSet<Integer> alreadyDeployedApps;
    public LAHPAmodel(String placement, HashSet<Integer> alreadyDeployedApps) {
        this.placement = placement;
        this.alreadyDeployedApps = alreadyDeployedApps;
    }
}
