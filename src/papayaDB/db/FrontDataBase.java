package papayaDB.db;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import papayaDB.structures.Tuple;

public class FrontDataBase {
	private ConcurrentHashMap<String, DataBase> dataBasePool = new ConcurrentHashMap<>();
	
	public boolean createDb(String dbName, List<String> fields){
		try {
			dataBasePool.put(dbName, new DataBase(dbName, fields));
		} catch (IOException e) {
			return false;
		}
		return true;
	}
	
	public boolean deleteDb(String dbName){
		dataBasePool.remove(dbName);
		return true;
	}

	public List<List<Tuple<String, String>>> get(String dbName, List<Tuple<String, String>> filter) {
		DataBase db = dataBasePool.get(dbName);
		if(db == null){
			return null;
		}
		return db.get(filter);
	}
	
	public List<List<Tuple<String, String>>> getAll(String dbName) {
		DataBase db = dataBasePool.get(dbName);
		if(db == null){
			return null;
		}
		return db.getAll();
	}

	public boolean post(String dbName, List<Tuple<String, String>> fields) {
		DataBase db = dataBasePool.get(dbName);
		if(db == null){
			return false;
		}
		try {
			db.add(fields);
		} catch (IOException e) {
			return false;
		}
		return true;
	}

	public boolean delete(String dbName, int id) {
		DataBase db = dataBasePool.get(dbName);
		if(db == null){
			return false;
		}
		try {
			return db.remove(id);
		} catch (IOException e) {
			return false;
		}
	}

}