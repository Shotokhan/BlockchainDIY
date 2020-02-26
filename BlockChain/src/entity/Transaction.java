package entity;

import java.security.*;
import java.util.ArrayList;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import util.KeySerializer;
import util.SignatureSerializer;
import util.StringUtil;
import util.TransactionException;

public class Transaction {

	public Transaction() {
		super();
	}

	private String transactionID;
	
	private PublicKey sender;
	
	private PublicKey receiver;
	
	private PublicKey miner;
	
	private float value;
	
	private byte[] signature;
	
	private ArrayList<TransactionInput> inputs;
	private ArrayList<TransactionOutput> outputs = new ArrayList<TransactionOutput>();
	
	private transient Chain blockchain;
	
	private static int sequence = 0;
	
	public Transaction(PublicKey sender, PublicKey receiver, float value,
			ArrayList<TransactionInput> inputs) {
		this.sender = sender;
		this.receiver = receiver;
		this.value = value;
		this.inputs = inputs;
	}

	public void genesisTransaction(PublicKey miner) throws TransactionException {
		if(blockchain.getBlockChain().size() > 0) {
			throw new TransactionException("Genesis transaction can be done only one time");
		}
		this.value = 100; // hard-coded
		this.inputs = new ArrayList<TransactionInput>();
		// only receiver is specified, using the constructor
		ArrayList<TransactionOutput> outputs = new ArrayList<TransactionOutput>();
		Wallet coinbase = new Wallet();
		this.sender = coinbase.getPublicKey();
		float reward = 1; // hard-coded
		this.miner = miner;
		this.transactionID = this.calculateHash();
		outputs.add(new TransactionOutput(receiver, value, transactionID));
		outputs.add(new TransactionOutput(miner, reward, transactionID));
		this.outputs = outputs;
		inputs.add(new TransactionInput(outputs.get(0).getId()));
		inputs.get(0).setUTXO(outputs.get(0));
		inputs.add(new TransactionInput(outputs.get(1).getId()));
		inputs.get(1).setUTXO(outputs.get(1));
		this.generateSignature(coinbase.getPrivateKey());
		this.blockchain.getUTXOs().put(outputs.get(0).getId(), outputs.get(0));
		this.blockchain.getUTXOs().put(outputs.get(1).getId(), outputs.get(1));
	}
	
	public void processTransaction(PublicKey miner) throws TransactionException {
		if(this.verifySignature() == false) {
			throw new TransactionException("Signature not valid");
		}
		for(TransactionInput input : inputs) {
			TransactionOutput output = null;
			if((output = blockchain.getUTXOs().get(input.getTransactionOutputID())) == null) {
				throw new TransactionException("Some input already spent");
			}
			input.setUTXO(output);
		}
		float total = this.getInputsValue();
		if(this.value < Chain.minimumTransaction) {
			throw new TransactionException("Transaction amount is below minimum threshold");
		}
		float transactionFee = this.value * Chain.transactionFee; 
		float leftAmount = total - this.value;
		float adjustedValue = this.value - transactionFee;
		this.miner = miner;
		this.transactionID = this.calculateHash();
		this.outputs.add(new TransactionOutput(this.receiver, adjustedValue, this.transactionID));
		this.outputs.add(new TransactionOutput(this.miner, transactionFee, this.transactionID));
		if(leftAmount > 0) {
			this.outputs.add(new TransactionOutput(this.sender, leftAmount, this.transactionID));
		}
		for(TransactionOutput output : outputs) {
			blockchain.getUTXOs().put(output.getId(), output);
		}
		for(TransactionInput input : inputs) {
			blockchain.getUTXOs().remove(input.getUTXO().getId());
		}
	}
	
	public float getInputsValue() throws TransactionException {
		float sum = 0;
		for(TransactionInput input : inputs) {
			try {
				sum += input.getUTXO().getValue();
			} catch(NullPointerException ex) {
				throw new TransactionException("Input refers to a not valid output");
			}
		}
		return sum;
	}
	
	public float getOutputsValue() {
		float sum = 0;
		for(TransactionOutput output : outputs) {
			sum += output.getValue();
		}
		return sum;
	}
	
	public void generateSignature(PrivateKey privateKey) {
		this.signature = this.createSignature(privateKey);
	}
	
	private byte[] createSignature(PrivateKey privateKey) {
		StringBuffer br = new StringBuffer();
		br.append(StringUtil.getStringFromKey(sender));
		br.append(StringUtil.getStringFromKey(receiver));
		br.append(Float.toString(value));
		br.append(StringUtil._getMerkleRoot(inputs));
		return StringUtil.applyECDSASig(privateKey, br.toString());
	}
	
	public boolean verifySignature() {
		StringBuffer br = new StringBuffer();
		br.append(StringUtil.getStringFromKey(sender));
		br.append(StringUtil.getStringFromKey(receiver));
		br.append(Float.toString(value));
		br.append(StringUtil._getMerkleRoot(inputs));
		return StringUtil.verifyECDSASig(sender, br.toString(), signature);
	}
	
	private String calculateHash() {
		sequence += 1;
		StringBuffer br = new StringBuffer();
		br.append(sender.toString());
		br.append(receiver.toString());
		br.append(miner.toString());
		br.append(Float.toString(value));
		return StringUtil.sha256(br.toString());
	}

	public String getTransactionID() {
		return transactionID;
	}

	public PublicKey getSender() {
		return sender;
	}

	public PublicKey getReceiver() {
		return receiver;
	}

	public float getValue() {
		return value;
	}

	public byte[] getSignature() {
		return signature;
	}

	public ArrayList<TransactionInput> getInputs() {
		return inputs;
	}

	public ArrayList<TransactionOutput> getOutputs() {
		return outputs;
	}

	public static int getSequence() {
		return sequence;
	}

	public Chain getBlockchain() {
		return blockchain;
	}

	public void setBlockchain(Chain blockchain) {
		this.blockchain = blockchain;
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

	public PublicKey getMiner() {
		return miner;
	}
	

}
