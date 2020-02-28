package main;

import java.security.PublicKey;
import java.security.Security;
import java.util.ArrayList;

import entity.Block;
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
		Wallet miner = new Wallet();
		walletA.setBlockchain(blockchain);
		walletB.setBlockchain(blockchain);
		walletC.setBlockchain(blockchain);
		walletD.setBlockchain(blockchain);
		miner.setBlockchain(blockchain);
		
		// genesis transaction
		// client
		Transaction genesis = new Transaction(null, walletA.getPublicKey(), 0, null);
		Block newBlock = new Block();
		try {
			newBlock.addTransaction(genesis);
		} catch (TransactionException e2) {
			// to user interface
			e2.printStackTrace();
			return;
		}
		// broadcast()
		// server (other peers)
		try {
			blockchain.addBlock(newBlock, miner.getPublicKey());
		} catch (TransactionException e1) {
			e1.printStackTrace();
			return;
		}
		
		// simple transaction
		// client
		Transaction transaction;
		try {
			transaction = walletA.sendFunds(walletB.getPublicKey(), 20);
		} catch (TransactionException e) {
			e.printStackTrace();
			return;
		}
		newBlock = new Block();
		try {
			newBlock.addTransaction(transaction);
		} catch (TransactionException e2) {
			// to user interface
			e2.printStackTrace();
			return;
		}
		// broadcast()
		// server (other peers)
		try {
			blockchain.addBlock(newBlock, miner.getPublicKey());
		} catch (TransactionException e1) {
			e1.printStackTrace();
			return;
		}
		
		// simple transaction
		// client
		try {
			transaction = walletB.sendFunds(walletA.getPublicKey(), 2);
		} catch (TransactionException e) {
			e.printStackTrace();
			return;
		}
		newBlock = new Block();
		try {
			newBlock.addTransaction(transaction);
		} catch (TransactionException e2) {
			// to user interface
			e2.printStackTrace();
			return;
		}
		// broadcast()
		// server (other peers)
		try {
			blockchain.addBlock(newBlock, miner.getPublicKey());
		} catch (TransactionException e1) {
			e1.printStackTrace();
			return;
		}
		
		// multiple transactions in a single block
		// client
		newBlock = new Block();
		try {
			transaction = walletB.sendFunds(walletC.getPublicKey(), 10);
		} catch (TransactionException e) {
			e.printStackTrace();
			return;
		}
		try {
			newBlock.addTransaction(transaction);
		} catch (TransactionException e1) {
			// to user interface
			e1.printStackTrace();
			return;
		}
		try {
			transaction = walletA.sendFunds(walletD.getPublicKey(), 5);
		} catch (TransactionException e) {
			e.printStackTrace();
			return;
		}
		try {
			newBlock.addTransaction(transaction);
		} catch (TransactionException e1) {
			// to user interface
			e1.printStackTrace();
			return;
		}
		// broadcast()
		// server (other peers)
		try {
			blockchain.addBlock(newBlock, miner.getPublicKey());
		} catch (TransactionException e) {
			e.printStackTrace();
			return;
		}
		
		// one wallet sending multiple transactions in the same block
		// client
		newBlock = new Block();
		ArrayList<Transaction> transactions;
		ArrayList<PublicKey> receivers = new ArrayList<PublicKey>();
		ArrayList<Float> values = new ArrayList<Float>();
		receivers.add(walletC.getPublicKey());
		values.add(Float.valueOf(1));
		receivers.add(walletD.getPublicKey());
		values.add(Float.valueOf(20));
		try {
			transactions = walletA.sendFunds(receivers, values);
		} catch (TransactionException e) {
			e.printStackTrace();
			return;
		}
		try {
			for(Transaction tx : transactions) {
				newBlock.addTransaction(tx);
			}
		} catch (TransactionException e) {
			// to user interface
			e.printStackTrace();
			return;
		}
		// broadcast()
		// server (other peers)
		try {
			blockchain.addBlock(newBlock, miner.getPublicKey());
		} catch (TransactionException e) {
			e.printStackTrace();
			return;
		}
		
		System.out.println("\nBlock chain: \n" + blockchain.toString());
		System.out.println("\nA's wallet : \n" + walletA.toString());
		System.out.println("\nB's wallet : \n" + walletB.toString());
		System.out.println("\nC's wallet : \n" + walletC.toString());
		System.out.println("\nD's wallet : \n" + walletD.toString());
		System.out.println("\nMiner's wallet : \n" + miner.toString());
		System.out.println("\nBlock chain is valid: " + blockchain.isChainValid());
	}

}
