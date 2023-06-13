package state;

public class result {
    String customerId;
    String start;
    Integer cost;

    public result(String customerId, String start, Integer cost) {
        this.customerId = customerId;
        this.start = start;
        this.cost = cost;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

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
}
