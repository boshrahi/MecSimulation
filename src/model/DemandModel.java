package model;

public class DemandModel {
    public double demand ;
    public int nodeName;
    public int mediatorName;
    public DemandModel(double demand, int nodeName , int mediatorName){
        this.demand = demand;
        this.nodeName = nodeName;
        this.mediatorName = mediatorName;
    }
}
