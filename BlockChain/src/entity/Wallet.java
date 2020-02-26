package entity;

import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import util.KeySerializer;
import util.TransactionException;

public class Wallet {

	private PrivateKey privateKey;
	private PublicKey publicKey;
	
	// for JSON
	private float balance = 0f;
	
	private HashMap<String, TransactionOutput> UTXOs = new HashMap<String, TransactionOutput>();
	private transient Chain blockchain;
	
	public Wallet() {
		this.generateKeyPair();
	}

	private void generateKeyPair() {
		try {
			KeyPairGenerator keyGen = KeyPairGenerator.getInstance("ECDSA","BC");
			SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
			ECGenParameterSpec ecSpec = new ECGenParameterSpec("prime192v1");
			// Initialize the key generator and generate a KeyPair
			keyGen.initialize(ecSpec, random);   //256 bytes provides an acceptable security level
	        KeyPair keyPair = keyGen.generateKeyPair();
        	// Set the public and private keys from the keyPair
        	privateKey = keyPair.getPrivate();
        	publicKey = keyPair.getPublic();
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

	public float updateBalance() {
		// TODO: mechanism to decide if transactions are already up to date
		this.fetchAllTransactions();
		float total = 0;
		for(Map.Entry<String, TransactionOutput> entry : UTXOs.entrySet()) {
			TransactionOutput output = entry.getValue();
			total += output.getValue();
		}
		balance = total;
		return balance;
	}
	
	private void fetchAllTransactions() {
		for(Map.Entry<String, TransactionOutput> entry : blockchain.getUTXOs().entrySet()) {
			TransactionOutput output = entry.getValue();
			if(output.isMine(publicKey)) {
				UTXOs.put(entry.getKey(), entry.getValue());
			}
		}
	}
	
	public Transaction sendFunds(PublicKey receiver, float value) throws TransactionException {
		value = value / (1 - Chain.transactionFee);
		if(this.updateBalance() < value) {
			throw new TransactionException("Not enough funds available");
		}
		ArrayList<TransactionInput> inputs = new ArrayList<TransactionInput>();
		float total = 0;
		for(Map.Entry<String, TransactionOutput> entry : UTXOs.entrySet()) {
			TransactionOutput output = entry.getValue();
			total += output.getValue();
			inputs.add(new TransactionInput(output.getId()));
			if(total >= value) {
				break;
			}
		}
		Transaction transaction = new Transaction(this.publicKey, receiver, value, inputs);
		transaction.setBlockchain(this.blockchain);
		transaction.generateSignature(this.privateKey);
		for(TransactionInput input : inputs) {
			this.UTXOs.remove(input.getTransactionOutputID());
		}
		return transaction;
	}
	
	public PrivateKey getPrivateKey() {
		return this.privateKey;
	}
	
	public PublicKey getPublicKey() {
		return this.publicKey;
	}

	public HashMap<String, TransactionOutput> getUTXOs() {
		return UTXOs;
	}

	public Chain getBlockchain() {
		return blockchain;
	}

	public void setBlockchain(Chain blockchain) {
		this.blockchain = blockchain;
	}

	@Override
	public String toString() {
		this.updateBalance();
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.setPrettyPrinting();
		gsonBuilder.registerTypeAdapter(PublicKey.class, new KeySerializer());
		gsonBuilder.registerTypeAdapter(PrivateKey.class, new KeySerializer());
		Gson gson = gsonBuilder.create();
		String json = gson.toJson(this);
		return json;
	}

	public float getBalance() {
		return balance;
	}
	
}
