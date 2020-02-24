package entity;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class TransactionInput {

	public TransactionInput() {
		super();
	}
	
	private String transactionOutputID; // riferimento a TransactionOutput -> id
	private transient TransactionOutput UTXO;
	
	public TransactionInput(String transactionOutputID) {
		this.transactionOutputID = transactionOutputID;
	}

	public String getTransactionOutputID() {
		return transactionOutputID;
	}

	public TransactionOutput getUTXO() {
		return UTXO;
	}

	public void setUTXO(TransactionOutput uTXO) {
		UTXO = uTXO;
	}

	@Override
	public String toString() {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		String json = gson.toJson(this);
		return json;
	}
}
