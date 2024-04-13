package model;

public class Transaction {
    private String buyer;
    private String seller;
    private Order order;

    public Transaction(String buyer, String seller, Order order) {
        this.buyer = buyer;
        this.seller = seller;
        this.order = order;
    }

    @Override
    public String toString() {
        return  " Broker comprador: " + buyer +
                " \n Broker vendedor: " + seller +
                " \n Ativo: " + order.getAsset() +
                " \n Quantidade: " + order.getAmount() +
                " \n Valor: " + order.getValue();
    }
}