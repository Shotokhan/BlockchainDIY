package main;

import java.security.Security;
import java.util.ArrayList;

import entity.Chain;
import entity.Transaction;
import entity.Wallet;
import util.TransactionException;

public class Main {

	public static void main(String[] args) {		
		Chain blockchain = new Chain();
		
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
		Wallet walletA = new Wallet();
		Wallet walletB = new Wallet();
		Wallet walletC = new Wallet();
		Wallet walletD = new Wallet();
		walletA.setBlockchain(blockchain);
		walletB.setBlockchain(blockchain);
		walletC.setBlockchain(blockchain);
		walletD.setBlockchain(blockchain);
		
		// genesis transaction
		Transaction genesis = new Transaction(null, walletA.getPublicKey(), 0, null);
		genesis.setBlockchain(blockchain);
		ArrayList<Transaction> transactions = new ArrayList<Transaction>();
		transactions.add(genesis);
		try {
			blockchain.addBlock(transactions);
		} catch (TransactionException e1) {
			e1.printStackTrace();
			return;
		}
		transactions.remove(0);
		
		// simple transaction
		Transaction transaction;
		try {
			transaction = walletA.sendFunds(walletB.getPublicKey(), 20);
		} catch (TransactionException e) {
			e.printStackTrace();
			return;
		}
		transactions.add(transaction);
		try {
			blockchain.addBlock(transactions);
		} catch (TransactionException e1) {
			e1.printStackTrace();
			return;
		}
		transactions.remove(0);
		
		// simple transaction
		try {
			transaction = walletB.sendFunds(walletA.getPublicKey(), 2);
		} catch (TransactionException e) {
			e.printStackTrace();
			return;
		}
		transactions.add(transaction);
		try {
			blockchain.addBlock(transactions);
		} catch (TransactionException e1) {
			e1.printStackTrace();
			return;
		}
		transactions.remove(0);
		
		// multiple transactions in a single block
		// TODO: how could a single wallet send funds more times in a single block?
		try {
			transaction = walletB.sendFunds(walletC.getPublicKey(), 10);
		} catch (TransactionException e) {
			e.printStackTrace();
			return;
		}
		transactions.add(transaction);
		try {
			transaction = walletA.sendFunds(walletD.getPublicKey(), 5);
		} catch (TransactionException e) {
			e.printStackTrace();
			return;
		}
		transactions.add(transaction);
		try {
			blockchain.addBlock(transactions);
		} catch (TransactionException e) {
			e.printStackTrace();
			return;
		}
		
		System.out.println("\nBlock chain: \n" + blockchain.toString());
		System.out.println("\nA's wallet : \n" + walletA.toString());
		System.out.println("\nB's wallet : \n" + walletB.toString());
		System.out.println("\nC's wallet : \n" + walletC.toString());
		System.out.println("\nD's wallet : \n" + walletD.toString());
		System.out.println("\nBlock chain is valid: " + blockchain.isChainValid());
	}

}
