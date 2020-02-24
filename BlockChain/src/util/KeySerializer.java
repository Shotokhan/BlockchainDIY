package util;

import java.lang.reflect.Type;
import java.security.Key;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class KeySerializer implements JsonSerializer<Key>{

	@Override
	public JsonElement serialize(Key src, Type typeOfSrc, JsonSerializationContext context) {
		JsonObject jsonKey = new JsonObject();
		jsonKey.addProperty(src.getClass().getSimpleName(), StringUtil.getStringFromKey(src));
		return jsonKey;
	}
	
}
