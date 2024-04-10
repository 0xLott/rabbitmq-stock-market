package exchange;

import java.util.List;
import java.util.ArrayList;

public class TransactionBook {
    private final List<Transaction> transactions;

    public TransactionBook(List<Transaction> orders) {
        this.transactions = new ArrayList<>(orders);
    }

    public void register(Transaction transaction) {
        transactions.add(transaction);
        System.out.println("\n Transação registrada!");
    }

    public void print() {
        transactions.forEach(System.out::println);
    }
}