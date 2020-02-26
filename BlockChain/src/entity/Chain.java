package entity;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;

import com.google.gson.*;

import util.KeySerializer;
import util.SignatureSerializer;
import util.TransactionException;

public class Chain {
	
	private ArrayList<Block> blockChain;
	private HashMap<String, TransactionOutput> UTXOs = new HashMap<String, TransactionOutput>();
	
	public static int difficulty = 4;
	public static float minimumTransaction = (float) 0.01;
	public static float transactionFee = (float) 0.01; // this is a percentage value
	
	public Chain() {
		blockChain = new ArrayList<Block>();
	}
	
	public void addBlock(Block newBlock, PublicKey miner) throws TransactionException {
		if(blockChain.size() == 0) {
			newBlock.setPreviousHash("0");
			Transaction transaction = newBlock.getTransactions().get(0);
			transaction.setBlockchain(this);
			while(newBlock.getTransactions().size() > 1) {
				newBlock.getTransactions().remove(1);
			}
			try {
				transaction.genesisTransaction(miner);
			} catch (TransactionException e) {
				// unreachable
				e.printStackTrace();
			}
			newBlock.mineBlock(difficulty, miner);
			blockChain.add(newBlock);
		} else {
			Block lastBlock = blockChain.get(blockChain.size() - 1);
			newBlock.setPreviousHash(lastBlock.getHash());
			HashMap<String, TransactionOutput> shadowUTXOs = new HashMap<String, TransactionOutput>(UTXOs);
			try {
				newBlock.mineBlock(difficulty, miner);
			} catch (TransactionException e) {
				this.UTXOs = shadowUTXOs;
				throw new TransactionException("Transactions not chained: " + e.getMessage());
			}
			blockChain.add(newBlock);
		}
	}
	
	public boolean isChainValid() {
		HashMap<String, TransactionOutput> tempUTXOs = new HashMap<String, TransactionOutput>();
		for(Block block : blockChain) {
			if(block.checkHash() == false) {
				return false;
			}
			if(block.checkProofOfWork(difficulty) == false) {
				return false;
			}
			for(Transaction transaction : block.getTransactions()) {
				if(!transaction.verifySignature()) {
					return false;
				}
				try {
					if(transaction.getInputsValue() != transaction.getOutputsValue()) {
						return false;
					}
				} catch (TransactionException e) {
					e.printStackTrace();
					return false;
				}
				if(!block.getPreviousHash().equals("0")) {
					for(TransactionInput input : transaction.getInputs()) {
						TransactionOutput output = tempUTXOs.get(input.getTransactionOutputID());
						if(output == null) {
							return false;
						}
						if(input.getUTXO().getValue() != output.getValue()) {
							return false;
						}
						tempUTXOs.remove(input.getTransactionOutputID());
					}
				}
				if(!transaction.getOutputs().get(0).getReceiver().equals(transaction.getReceiver())) {
					return false;
				}
				if(!transaction.getOutputs().get(1).getReceiver().equals(transaction.getMiner())) {
					return false;
				}
				if(transaction.getOutputs().size() > 2) {
					if(!transaction.getOutputs().get(2).getReceiver().equals(transaction.getSender())) {
						return false;
					}	
				}
				for(TransactionOutput output : transaction.getOutputs()) {
					if(!output.getParentTransactionID().equals(transaction.getTransactionID())) {
						return false;
					}
					tempUTXOs.put(output.getId(), output);
				}
			}
		}
		for(int i=1; i<blockChain.size(); i++) {
			if(blockChain.get(i).getPreviousHash() != blockChain.get(i-1).getHash()) {
				return false;
			}
		}
		return true;
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

	public ArrayList<Block> getBlockChain() {
		return blockChain;
	}

	public HashMap<String, TransactionOutput> getUTXOs() {
		return UTXOs;
	}

	public static int getDifficulty() {
		return difficulty;
	}

	public static float getMinimumTransaction() {
		return minimumTransaction;
	}
	
}
