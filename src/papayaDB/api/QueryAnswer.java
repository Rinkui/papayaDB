package papayaDB.api;

import io.vertx.core.json.JsonObject;

public class QueryAnswer {
	private JsonObject answer;
	private QueryAnswerStatus status;
	
	public QueryAnswer(JsonObject answer) {
		this.answer = answer;
		this.status = QueryAnswerStatus.OK;
		//TODO
	}
	
	@Override
	public String toString() {
		return status.name()+": "+answer.encodePrettily();
	}
}