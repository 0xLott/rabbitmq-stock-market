package exchange;

public class BuyOrder extends Order {
    public BuyOrder() {
        super();
    }

    public void addStocks(int addAmount) {
        this.asset.addStocks(addAmount);
    }
}
