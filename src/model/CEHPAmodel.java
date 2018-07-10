package model;

import java.util.HashSet;

public class CEHPAmodel {
    public String placement;
    public HashSet<Integer> alreadyDeployedApps;
    public CEHPAmodel(String placement, HashSet<Integer> alreadyDeployedApps) {
        this.placement = placement;
        this.alreadyDeployedApps = alreadyDeployedApps;
    }
}
