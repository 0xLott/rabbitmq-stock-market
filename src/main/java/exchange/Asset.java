package exchange;

public class Asset {
    private String id;
    private int amount;

    public Asset(String id, int amount) {
        this.id = id;
        amount = 0;
    }

    public void addStocks(int addAmount) {
        this.amount =+ addAmount;
    }

    public void decreaseStocks(int decAmount) {
        this.amount =- decAmount;
    }
}
