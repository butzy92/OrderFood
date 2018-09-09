package ro.butzi.order.food.model;

public class Result {
    private final String action;
    private final String result;

    public Result(String action, String result) {
        this.action = action;
        this.result = result;
    }

    public String getAction() {
        return action;
    }

    public String getResult() {
        return result;
    }
}
