package exchange;

public class SellOrder extends Order {
    public SellOrder() {
        super();
    }

    public void decreaseStocks(int decAmount) {
        this.asset.decreaseStocks(decAmount);
    }
}
