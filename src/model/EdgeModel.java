package model;

public class EdgeModel {

    public long source;
    public long target;
    public String id;
    public long distance;
    public EdgeModel(long source, long target, String id,long distance) {
        this.source = source;
        this.target= target;
        this.id = id;
        this.distance = distance;
    }
}
