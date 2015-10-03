package com.github.samuelbr.dbcheck;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Map.Entry;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class ResultInfoFormatter {

	private Gson gson;
	
	private SimpleDateFormat dateFormater = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
	
	public ResultInfoFormatter() {
		gson = new GsonBuilder()
			.registerTypeAdapter(ResultInfo.class, new ResultInfoAdapter())
			.create();
	}
	
	public String format(Collection<ResultInfo> resultInfos) {
		return gson.toJson(resultInfos);
	}

	private final class ResultInfoAdapter implements JsonSerializer<ResultInfo> {

		public JsonElement serialize(ResultInfo src, Type typeOfSrc, JsonSerializationContext context) {
			JsonObject object = new JsonObject();
			
			for (Entry<String, Object> entry: src.getResult().entrySet()) {
				object.add(entry.getKey(), context.serialize(entry.getValue()));
			}
			
			object.add("@tags", context.serialize(src.getTags()));
			String formatedTimestamp = dateFormater.format(new Date(src.getTimestamp()));
			object.add("@timestamp", context.serialize(formatedTimestamp));
			return object;
		}
		
	}
	
}
