package entity;

import java.security.PublicKey;
import java.sql.Timestamp;
import java.util.ArrayList;

import com.google.gson.*;

import util.KeySerializer;
import util.SignatureSerializer;
import util.StringUtil;
import util.TransactionException;

public class Block {

	private String hash;
	private String previousHash;
	private String merkleRoot;
	private Timestamp timeStamp;
	private int nonce;
	
	private ArrayList<Transaction> transactions = new ArrayList<Transaction>();
	
	public Block() {
		this.timeStamp = new Timestamp(java.lang.System.currentTimeMillis());
		this.nonce = 0;
	}

	public void mineBlock(int difficulty, PublicKey miner) throws TransactionException {
		if(this.previousHash != "0") {
			for(Transaction transaction : transactions) {
				transaction.processTransaction(miner);
			}
		}
		this.merkleRoot = StringUtil.getMerkleRoot(transactions);
		this.applySha256();
		while(this.checkProofOfWork(difficulty) == false) {
			this.nonce += 1;
			this.applySha256();
		}
	}
	
	private void applySha256() {
		StringBuffer toHash = new StringBuffer();
		toHash.append(previousHash);
		toHash.append(merkleRoot);
		toHash.append(timeStamp.toString());
		toHash.append(nonce);
		this.hash = StringUtil.sha256(toHash.toString());
	}
	
	public boolean checkProofOfWork(int difficulty) {
		for(int i=0; i<difficulty; i++) {
			if(hash.charAt(i) != '0') {
				return false;
			}
		}
		return true;
	}
	
	public boolean checkHash() {
		String merkle = StringUtil.getMerkleRoot(transactions);
		if(!merkle.equals(this.merkleRoot)) {
			return false;
		}
		StringBuffer toHash = new StringBuffer();
		toHash.append(previousHash);
		toHash.append(merkleRoot);
		toHash.append(timeStamp.toString());
		toHash.append(nonce);
		return this.hash.equals(StringUtil.sha256(toHash.toString()));
	}
	
	public void addTransaction(Transaction transaction) throws TransactionException {
		if(transaction == null) {
			throw new TransactionException("Null transaction");
		}
		transactions.add(transaction);
	}
	
	public String getHash() {
		return hash;
	}

	public String getPreviousHash() {
		return previousHash;
	}

	public String getMerkleRoot() {
		return merkleRoot;
	}

	public Timestamp getTimeStamp() {
		return timeStamp;
	}

	@Override
	public String toString() {
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.setPrettyPrinting();
		gsonBuilder.registerTypeAdapter(PublicKey.class, new KeySerializer());
		gsonBuilder.registerTypeAdapter(byte[].class, new SignatureSerializer());
		Gson gson = gsonBuilder.create();
		String json = gson.toJson(this);
		return json;
	}

	public int getNonce() {
		return nonce;
	}

	public ArrayList<Transaction> getTransactions() {
		return transactions;
	}

	public void setPreviousHash(String previousHash) {
		this.previousHash = previousHash;
	}
}
