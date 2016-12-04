package papayaDB.api;

import java.util.List;
import java.util.stream.Stream;

import io.vertx.core.json.JsonObject;
import papayaDB.structures.Tuple;

public interface Api{

	public boolean createDb(String dbName, List<Tuple<String, String>> fields);
	
	public boolean deleteDb(String dbName);

	public Stream<JsonObject> get(String dbName, List<Tuple<String,String>> filter);
	
	public Stream<JsonObject> getAll(String dbName);

	public boolean post(String dbName, List<Tuple<String, String>> fields);
	
	public boolean delete(String dbName, int id);
}
