// package test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import java.util.Date;
import java.util.List;
import java.text.ParseException;
import java.lang.IllegalArgumentException;

import org.junit.Before;
import org.junit.Test;

import controller.ExpenseTrackerController;
import model.ExpenseTrackerModel;
import model.Transaction;
import model.Filter.AmountFilter;
import model.Filter.CategoryFilter;
import model.Filter.TransactionFilter;
import view.ExpenseTrackerView;


public class TestExample {
  
  private ExpenseTrackerModel model;
  private ExpenseTrackerView view;
  private ExpenseTrackerController controller;

  @Before
  public void setup() {
    model = new ExpenseTrackerModel();
    view = new ExpenseTrackerView();
    controller = new ExpenseTrackerController(model, view);
  }

    public double getTotalCost() {
        double totalCost = 0.0;
        List<Transaction> allTransactions = model.getTransactions(); // Using the model's getTransactions method
        for (Transaction transaction : allTransactions) {
            totalCost += transaction.getAmount();
        }
        return totalCost;
    }


    public void checkTransaction(double amount, String category, Transaction transaction) {
	assertEquals(amount, transaction.getAmount(), 0.01);
        assertEquals(category, transaction.getCategory());
        String transactionDateString = transaction.getTimestamp();
        Date transactionDate = null;
        try {
            transactionDate = Transaction.dateFormatter.parse(transactionDateString);
        }
        catch (ParseException pe) {
            pe.printStackTrace();
            transactionDate = null;
        }
        Date nowDate = new Date();
        assertNotNull(transactionDate);
        assertNotNull(nowDate);
        // They may differ by 60 ms
        assertTrue(nowDate.getTime() - transactionDate.getTime() < 60000);
    }


    @Test
    public void testAddTransaction() {
        // Pre-condition: List of transactions is empty
        assertEquals(0, model.getTransactions().size());
    
        // Perform the action: Add a transaction
	    double amount = 50.0;
	    String category = "food";
        assertTrue(controller.addTransaction(amount, category));
    
        // Post-condition: List of transactions contains only
	    //                 the added transaction	
        assertEquals(1, model.getTransactions().size());
    
        // Check the contents of the list
        Transaction firstTransaction = model.getTransactions().get(0);
        checkTransaction(amount, category, firstTransaction);
        
	    // Check the total amount
        assertEquals(amount, getTotalCost(), 0.01);
    }


    @Test
    public void testRemoveTransaction() {
        // Pre-condition: List of transactions is empty
        assertEquals(0, model.getTransactions().size());
    
        // Perform the action: Add and remove a transaction
        double amount = 50.0;
        String category = "food";
        Transaction addedTransaction = new Transaction(amount, category);
        model.addTransaction(addedTransaction);
    
        // Pre-condition: List of transactions contains only
        //                the added transaction
        assertEquals(1, model.getTransactions().size());
        Transaction firstTransaction = model.getTransactions().get(0);
        checkTransaction(amount, category, firstTransaction);

        assertEquals(amount, getTotalCost(), 0.01);
	
	    // Perform the action: Remove the transaction
        model.removeTransaction(addedTransaction);
    
        // Post-condition: List of transactions is empty
        List<Transaction> transactions = model.getTransactions();
        assertEquals(0, transactions.size());
    
        // Check the total cost after removing the transaction
        double totalCost = getTotalCost();
        assertEquals(0.00, totalCost, 0.01);
    }

    @Test
    public void addTransaction() {
        // Pre-condition: List of transactions is empty
        assertEquals(0, model.getTransactions().size());
    
        // Perform the action: Add a transaction
        double amount = 50.0;
        String category = "food";
        assertTrue(controller.addTransaction(amount, category));
    
        // Post-condition: List of transactions contains only
	    //                 the added transaction	
        assertEquals(1, model.getTransactions().size());
    
        // Check the contents of the list
        Transaction firstTransaction = model.getTransactions().get(0);
        checkTransaction(amount, category, firstTransaction);
	
	    // Check the total amount
        assertEquals(amount, getTotalCost(), 0.01);
    }

    @Test(expected=IllegalArgumentException.class)
    public void invalidInputHandling() {
        // Pre-condition: List of transactions is 0
        assertEquals(0, model.getTransactions().size());
    
        // Perform the action: Add a transaction
        String wrong_category = "doof";
        int amount = 0;
        // will throw an exception
        Transaction errTransaction = new Transaction(amount, wrong_category);
        controller.addTransaction(amount, wrong_category);    
        // Post-condition: List of transactions and total cost do not change	
        assertEquals(0, model.getTransactions().size());
    }

    @Test
    public void filterByAmount() {    
        // Pre-condition: List of transactions is empty
        assertEquals(0, model.getTransactions().size());
    
        // Perform the action: Add and remove a transaction
        double amount = 50.0;
        String category = "food";
        controller.addTransaction(amount, category);
        category = "bills";
        controller.addTransaction(amount, category);
        category = "travel";
        controller.addTransaction(amount, category);
        amount = 10.0;
        category = "food";
        controller.addTransaction(amount, category);
        category = "bills";
        controller.addTransaction(amount, category);  
        AmountFilter amountFilter = new AmountFilter(50.0);        
        List<Transaction> filterList = amountFilter.filter(model.getTransactions());
        
        // Post-condition: Check the contents of the list
        assertEquals(3, filterList.size());
        double expectedAmount = 50.0;
        Transaction firstTransaction = model.getTransactions().get(0);
        checkTransaction(expectedAmount, "food", firstTransaction);
        Transaction secondTransaction = model.getTransactions().get(1);
        checkTransaction(expectedAmount, "bills", secondTransaction);
        Transaction thirdTransaction = model.getTransactions().get(2);
        checkTransaction(expectedAmount, "travel", thirdTransaction);
    }

    @Test
    public void filterByCategory() {    
        // Pre-condition: List of transactions is empty
        assertEquals(0, model.getTransactions().size());
    
        // Perform the action: Add and remove a transaction
        double amount = 50.0;
        String category = "food";
        String expectedCategory = "food";
        controller.addTransaction(amount, category);
        category = "bills";
        controller.addTransaction(amount, category);
        category = "travel";
        controller.addTransaction(amount, category);
        amount = 10.0;
        category = "food";
        controller.addTransaction(amount, category);
        category = "bills";
        controller.addTransaction(amount, category);  
        CategoryFilter filter = new CategoryFilter(expectedCategory);        
        List<Transaction> filterList = filter.filter(model.getTransactions());
        
        // Post-condition: Check the contents of the list
        assertEquals(2, filterList.size());
        Transaction firstTransaction = model.getTransactions().get(0);
        checkTransaction(50.0, expectedCategory, firstTransaction);
        Transaction thirdTransaction = model.getTransactions().get(3);
        checkTransaction(10.0, expectedCategory, thirdTransaction);
    }

    @Test(expected = IllegalArgumentException.class)
    public void undoDisallowed() {
        // Pre-condition: List of transactions is empty
        assertEquals(0, model.getTransactions().size());
    
        // Perform the action: undo 
        // expect to throw an exception here
        controller.applyUndo(0);
    
        // Post-condition: List of transactions does not chance	
        assertEquals(0, model.getTransactions().size());
    }

    @Test()
    public void undoAllowed() {
        // Pre-condition: List of transactions is empty
        assertEquals(0, model.getTransactions().size());
    
        // Perform the action: undo 
        // expect to throw an exception here
        double amount = 50.0;
        String category = "food";
        controller.addTransaction(amount, category);
        // Check the total amount
        assertEquals(amount, getTotalCost(), 0.01);
        assertEquals(1, model.getTransactions().size());
        controller.applyUndo(0);
    
        // Post-condition: List of transactions does not change	
        assertEquals(0, model.getTransactions().size());
        // Check the total amount
        assertEquals(0.0, getTotalCost(), 0.00);
    }
    
    
}
