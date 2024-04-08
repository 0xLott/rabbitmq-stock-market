package exchange;

public class Asset {
    private String id;
    private int amount;

    public Asset(String id, int amount) {
        this.id = id;
        this.amount = amount;
    }

    public void addStocks(int addAmount) {
        this.amount =+ addAmount;
    }

    public void decreaseStocks(int decAmount) {
        this.amount =- decAmount;
    }
}
