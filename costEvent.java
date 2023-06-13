package state;

public class costEvent {
    private String customerId;
    private Integer cost;

    private Long timeStamp;

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public void setCost(Integer cost) {
        this.cost = cost;
    }

    public void setTimeStamp(Long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public Integer getCost() {
        return cost;
    }

    public Long getTimeStamp() {
        return timeStamp;
    }

    public costEvent(String customerId, Integer cost, Long timeStamp) {
        this.customerId = customerId;
        this.cost = cost;
        this.timeStamp = timeStamp;
    }


}
