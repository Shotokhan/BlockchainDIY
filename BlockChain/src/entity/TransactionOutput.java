package entity;

import java.security.PublicKey;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import util.KeySerializer;
import util.StringUtil;

public class TransactionOutput implements Comparable<TransactionOutput> {

	public TransactionOutput() {
		super();
	}

	private String id;
	private PublicKey receiver; // nuovo owner di "value" criptovalute al momento della transazione
	private float value;
	private String parentTransactionID;
	
	public TransactionOutput(PublicKey receiver, float value, String parentTransactionID) {
		this.receiver = receiver;
		this.value = value;
		this.parentTransactionID = parentTransactionID;
		StringBuffer br = new StringBuffer();
		br.append(StringUtil.getStringFromKey(receiver));
		br.append(Float.toString(value));
		br.append(parentTransactionID);
		this.id = StringUtil.sha256(br.toString());
	}

	public boolean isMine(PublicKey publicKey) {
		return this.receiver == publicKey;
	}
	
	public String getId() {
		return id;
	}

	public PublicKey getReceiver() {
		return receiver;
	}

	public float getValue() {
		return value;
	}

	public String getParentTransactionID() {
		return parentTransactionID;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TransactionOutput other = (TransactionOutput) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	@Override
	public String toString() {
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.setPrettyPrinting();
		gsonBuilder.registerTypeAdapter(PublicKey.class, new KeySerializer());
		Gson gson = gsonBuilder.create();
		String json = gson.toJson(this);
		return json;
	}

	@Override
	public int compareTo(TransactionOutput other) {
		if (this == other)
			return 0;
		if (other == null)
			throw new RuntimeException("Compare with null object");
		return (int) (this.value - other.getValue());
	}
	
}
