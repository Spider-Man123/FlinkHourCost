package state;

public class result {
    String start;
    Integer cost;

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public Integer getCost() {
        return cost;
    }

    public void setCost(Integer cost) {
        this.cost = cost;
    }

    public result(String start, Integer cost) {
        this.start = start;
        this.cost = cost;
    }
}
