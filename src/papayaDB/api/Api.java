package papayaDB.api;

import java.util.List;
import java.util.stream.Stream;

import papayaDB.structures.Tuple;

public interface Api{
	
	public boolean createDb(String dbName);
	
	public boolean deleteDb(String dbName);

	public Stream<Object> get(String dbName, List<Tuple<String,String>> filter);
	
	public Stream<Object> getAll(String dbName);

	public boolean post(String dbName, List<Tuple<String, String>> fields);
	
	public boolean delete(String dbName, int id);
}
