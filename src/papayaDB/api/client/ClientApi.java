package papayaDB.api.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.core.Future;
import papayaDB.api.Api;
import papayaDB.structures.Tuple;

public class ClientApi implements Api{
	private final HttpClient httpclient;
	private final ReentrantLock lock = new ReentrantLock();
	private final Condition waitResult = lock.newCondition();
	
	public ClientApi(HttpClient httpclient){
		this.httpclient = httpclient;
	}

	@Override
	public boolean createDb(String dbName, List<String> fields) {
		Future<Boolean> future = postProcess("/" + dbName + "/create", fieldsToJson(fields));
		lock.lock();
		try{
			while(!future.isComplete()){
				waitResult.await();
			}
		} catch (InterruptedException e) {
			future.fail(e);
		} finally {
			lock.unlock();
		}
		return future.result();
	}

	@Override
	public boolean deleteDb(String dbName) {
		Future<Boolean> future = deleteProcess("/" + dbName + "/delete");
		lock.lock();
		try{
			while(!future.isComplete()){
				waitResult.await();
			}
		} catch (InterruptedException e) {
			future.fail(e);
		} finally {
			lock.unlock();
		}
		return future.result();
	}

	@Override
	public Stream<JsonObject> get(String dbName, List<Tuple<String, String>> filter) {
		String url = formatUrl(dbName, filter);
		Future<Stream<JsonObject>> future = getProcess(url);
		lock.lock();
		try{
			while(!future.isComplete()){
				waitResult.await();
			}
		} catch (InterruptedException e) {
			future.fail(e);
		} finally {
			lock.unlock();
		}
		return future.result();
	}

	@Override
	public Stream<JsonObject> getAll(String dbName){
		Future<Stream<JsonObject>> future = getProcess("/" + dbName + "/all");
		lock.lock();
		try{
			while(!future.isComplete()){
				waitResult.await();
			}
		} catch (InterruptedException e) {
			future.fail(e);
		} finally {
			lock.unlock();
		}
		return future.result();
	}
	
	@Override
	public boolean post(String dbName, List<Tuple<String, String>> fields) {
		Future<Boolean> future = postProcess("/" + dbName, listToJsonObject(fields));
		lock.lock();
		try{
			while(!future.isComplete()){
				waitResult.await();
			}
		} catch (InterruptedException e) {
			future.fail(e);
		} finally {
			lock.unlock();
		}
		return future.result();
	}

	private JsonObject listToJsonObject(List<Tuple<String, String>> fields) {
		JsonObject o = new JsonObject();
		fields.forEach(tuple -> o.put(tuple.getKey(), tuple.getValue()));
		return o;
	}
	
	

	@Override
	public boolean delete(String dbName, int id) {
		Future<Boolean> future = deleteProcess("/" + dbName + "/" + id);
		lock.lock();
		try{
			while(!future.isComplete()){
				waitResult.await();
			}
		} catch (InterruptedException e) {
			future.fail(e);
		} finally {
			lock.unlock();
		}
		return future.result();
	}
	
	private String formatUrl(String dbName, List<Tuple<String, String>> filter){
		Objects.requireNonNull(filter);
		return "/" + dbName + "/" + filter.stream().map(t -> t.getKey() + "/" + t.getValue()).collect(Collectors.joining("/"));
	}
	
	private Future<Boolean> postProcess(String url, JsonObject data) {
		Future<Boolean> future = Future.future();
		httpclient.post(url, response -> {
			lock.lock();
			try{
				future.complete(response.statusCode() == 200);
				waitResult.signalAll();
			} finally {
				lock.unlock();
			}
		}).end(data.encode());
		return future;
	}

	private Future<Boolean> deleteProcess(String url) {
		Future<Boolean> future = Future.future();
		httpclient.delete(url, response -> {
			lock.lock();
			try{
				future.complete(response.statusCode() == 200);
				waitResult.signalAll();
			} finally {
				lock.unlock();
			}
		}).end();
		return future;
	}

	private Future<Stream<JsonObject>> getProcess(String url) {
		Future<Stream<JsonObject>> future = Future.future();
		httpclient.getNow(url, response -> {
			response.handler(data -> {
				lock.lock();
				try{
					future.complete(data.toJsonObject().getJsonArray("result").stream().map(c -> (JsonObject) c));
					waitResult.signalAll();
				} finally {
					lock.unlock();
				}
			});
		});
		return future;
	}
	
	private JsonObject fieldsToJson(List<String> list){
		JsonObject o = new JsonObject();
		JsonArray array = new JsonArray(list);
		o.put("fields", array);
		return o;
	}
	
	//name/"abc /def"/year/[2010;2015]/price/[;30]
	public static void main(String[] args) {
		// development option, avoid caching to see changes of
		// static files without having to reload the application,
		// obviously, this line should be commented in production
		//System.setProperty("vertx.disableFileCaching", "true");
		
		Vertx vertx = Vertx.vertx();
		HttpClientOptions hco = new HttpClientOptions().setDefaultHost("localhost").setDefaultPort(8080);
		ClientApi ca = new ClientApi(vertx.createHttpClient(hco));
		
		List<Tuple<String, String>> filter = new ArrayList<>(3);
		filter.add(new Tuple<>("name", "abc"));
		filter.add(new Tuple<>("year", "[2010;2015]"));
		filter.add(new Tuple<>("price", "[;30]"));
		Stream<JsonObject> sjo = ca.get("books", filter);
		System.out.println(sjo.count());
		
		ArrayList<String> list = new ArrayList<String>();
		list.add("author");
		list.add("price");
	
		String dbName = "Books";
		System.out.println(ca.createDb(dbName, list));
		
		List<Tuple<String, String>> object = new ArrayList<>();
		object.add(new Tuple<String, String>("author", "X"));
		object.add(new Tuple<String, String>("price", "12"));
		List<Tuple<String, String>> object2 = new ArrayList<>();
		object2.add(new Tuple<String, String>("price", "52"));
		object2.add(new Tuple<String, String>("author", "Y"));
		List<Tuple<String, String>> object3 = new ArrayList<>();
		object3.add(new Tuple<String, String>("price", "0"));
		object3.add(new Tuple<String, String>("author", "a"));
		
		System.out.println(ca.post(dbName, object));
		System.out.println(ca.post(dbName, object2));
		System.out.println(ca.post(dbName, object3));
		
		System.out.println("\nGetAll");
		ArrayList<Integer> id = new ArrayList<>(1);
		ca.getAll(dbName).forEach(o -> {
			System.out.println(o);
			id.add(0, Integer.valueOf(o.getString("id")));
		});
		
		
		//System.out.println("\nGetAll After Delete");
		//ca.delete(dbName, id.get(0));
		
		//ca.getAll(dbName).forEach(System.out::println);
	
		
//		List<Tuple<String, String>> requests = new ArrayList<>();
//		requests.add(new Tuple<String, String>("price", "[;50]"));
//		Stream<JsonObject> resultForServer = ca.get(dbName, requests);
//		System.out.println("\nResult for server");
//		resultForServer.forEach(System.out::println);

	}
	
}
