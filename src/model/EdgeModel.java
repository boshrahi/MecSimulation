package model;

public class EdgeModel {

    public long source;
    public long target;
    public String id;
    public EdgeModel(long source, long target, String id) {
        this.source = source;
        this.target= target;
        this.id = id;
    }
}
