package util;

import java.lang.reflect.Type;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class SignatureSerializer implements JsonSerializer<byte[]>{

	@Override
	public JsonElement serialize(byte[] src, Type typeOfSrc, JsonSerializationContext context) {
		JsonObject jsonSignature = new JsonObject();
		jsonSignature.addProperty("signature", StringUtil.bytesToHex(src));
		return jsonSignature;
	}

}
