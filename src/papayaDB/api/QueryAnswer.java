package papayaDB.api;

import io.vertx.core.json.JsonObject;

public class QueryAnswer {
	private JsonObject answer;
	private QueryAnswerStatus status;
	
	public QueryAnswer(JsonObject answer) {
		this.answer = answer;
		
		//TODO
	}
}