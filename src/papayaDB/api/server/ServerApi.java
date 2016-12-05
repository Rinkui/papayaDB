package papayaDB.api.server;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;
import papayaDB.api.Api;
import papayaDB.db.FrontDataBase;
import papayaDB.structures.Tuple;

public class ServerApi extends AbstractVerticle implements Api{
	FrontDataBase fdb = new FrontDataBase();
	BlockingQueue<Runnable> queue = new ArrayBlockingQueue<>(100);
	ExecutorService executor = new ThreadPoolExecutor(10, 50, 10, TimeUnit.MINUTES, queue);
	
	@Override
	public void start() {
		Router router = Router.router(vertx);
		
		router.route().handler(BodyHandler.create());
		
		//For DB
		router.post("/:dbName/create").handler(routingContext -> executor.execute(runnableForCreateDb(routingContext)));
		router.delete("/:dbName/delete").handler(routingContext -> executor.execute(runnableForDeleteDb(routingContext)));

		//For Element
		router.get("/:dbName/all").handler(routingContext -> executor.execute(runnableForGetAll(routingContext)));
		router.get("/:dbName/*").handler(routingContext -> executor.execute(runnableForGet(routingContext)));
		router.post("/:dbName").handler(routingContext -> executor.execute(runnableForPost(routingContext)));
		router.delete("/:dbName/:id").handler(routingContext -> executor.execute(runnableForDelete(routingContext)));

		// otherwise serve static pages
		router.route().handler(StaticHandler.create());

		vertx.createHttpServer().requestHandler(router::accept).listen(8080);
		System.out.println("listen on port 8080");
	}

	private Runnable runnableForCreateDb(RoutingContext routingContext) {
		return new Runnable() {
			@Override
			public void run() {
				HttpServerResponse response = routingContext.response();
				HttpServerRequest request = routingContext.request();
				JsonArray ja = routingContext.getBodyAsJson().getJsonArray("fields");
				String base = request.getParam("dbName");
				response.setStatusCode(200).end(String.valueOf(createDb(base, ja.getList())));
			}
			
		};
	}
	
	private Runnable runnableForDeleteDb(RoutingContext routingContext) {
		return new Runnable() {
			@Override
			public void run() {
				HttpServerResponse response = routingContext.response();
				HttpServerRequest request = routingContext.request();
				String base = request.getParam("dbName");
				boolean success = deleteDb(base);
				if(success){
					response.setStatusCode(200).end();
				} else {
					response.setStatusCode(500).end();
				}
			}
		};
	}

	private Runnable runnableForGet(RoutingContext routingContext){
		return new Runnable() {
			@Override
			public void run() {
				HttpServerResponse response = routingContext.response();
				HttpServerRequest request = routingContext.request();
				System.out.println(request.path());
				String[] pathCut = splitRequest(request.path());
				
				if(pathCut.length%2 == 0){
					response.setStatusCode(400).end("{\"result\":[]}");
				}
				else{
					String base = request.getParam("dbName");
					List<Tuple<String, String>> filter = requestToList(pathCut);
					response.setStatusCode(200).end(get(base, filter).map(jsonObject -> jsonObject.encode()).collect(Collectors.joining(",", "{\"result\":[", "]}")));
				}
			}
		};
	}
	
	private Runnable runnableForGetAll(RoutingContext routingContext) {
		return new Runnable() {
			@Override
			public void run() {
				HttpServerResponse response = routingContext.response();
				HttpServerRequest request = routingContext.request();
				System.out.println(request.path());
				String base = request.getParam("dbName");
				response.setStatusCode(200).end(getAll(base).map(jsonObject -> jsonObject.encode()).collect(Collectors.joining(",", "{\"result\":[", "]}")));
			}
		};
	}
	
	private Runnable runnableForPost(RoutingContext routingContext){
		return new Runnable() {
			@Override
			public void run() {
				HttpServerResponse response = routingContext.response();
				HttpServerRequest request = routingContext.request();
				String base = request.getParam("dbName");
				List<Tuple<String, String>> fields = jsonToList(routingContext.getBodyAsJson());
				boolean success = post(base, fields);
				if(success){
					response.setStatusCode(200).end();
				} else {
					response.setStatusCode(500).end();
				}
			}
		};
	}

	private Runnable runnableForDelete(RoutingContext routingContext) {
		return new Runnable() {
			@Override
			public void run() {
				HttpServerResponse response = routingContext.response();
				HttpServerRequest request = routingContext.request();
				String base = request.getParam("dbName");
				int id = Integer.valueOf(request.getParam("id"));
				boolean success = delete(base, id);
				if(success){
					response.setStatusCode(200).end();
				} else {
					response.setStatusCode(500).end();
				}
			}
		};
	}
	
	private String[] splitRequest(String request){
		String path = request.replaceAll("%22", "\"").replaceAll("%20", " ");
		
		Pattern pattern = Pattern.compile("\\/(\"(\\[{0,1}(\\w|\\s|\\/|;)*\\]{0,1})\"|\\[{0,1}(\\w|\\s|;)*\\]{0,1})");
		Matcher m = pattern.matcher(path);
		
		return m.results().map(c -> c.group(1)).toArray(String[]::new);
	}

	private List<Tuple<String, String>> requestToList(String[] request){
		List<Tuple<String, String>> result = new ArrayList<>(request.length/2);
		int i;
		for(i = 1; i < request.length - 1; i = i+2){
			result.add(i/2, new Tuple<String, String>(request[i], request[i+1]));
		}
		return result;
	}
	
	private List<Tuple<String, String>> jsonToList(JsonObject json){
		List<Tuple<String, String>> result = new ArrayList<>(json.size());
		for(String name : json.fieldNames()){
			result.add(new Tuple<String, String>(name, json.getValue(name).toString()));
		}
		return result;
	}
	
	private JsonObject listToJson(List<Tuple<String, String>> list){
		JsonObject o = new JsonObject();
		list.forEach(tuple -> o.put(tuple.getKey(), tuple.getValue()));
		return o;
	}
	
	@Override
	public boolean createDb(String dbName, List<String> fields) {
		return fdb.createDb(dbName, fields);
	}

	@Override
	public boolean deleteDb(String dbName) {
		return fdb.deleteDb(dbName);
	}

	@Override
	public Stream<JsonObject> get(String dbName, List<Tuple<String, String>> filter) {
		return fdb.get(dbName, filter).stream().map(object -> listToJson(object));
	}
	
	@Override
	public Stream<JsonObject> getAll(String dbName) {
		return fdb.getAll(dbName).stream().map(object -> listToJson(object));
	}

	@Override
	public boolean post(String dbName, List<Tuple<String, String>> fields) {
		//System.out.println(fields);
		return fdb.post(dbName, fields);
	}

	@Override
	public boolean delete(String dbName, int id) {
		return fdb.delete(dbName, id);
	}

	public static void main(String[] args) {
		// development option, avoid caching to see changes of
		// static files without having to reload the application,
		// obviously, this line should be commented in production
		//System.setProperty("vertx.disableFileCaching", "true");
	
		Vertx vertx = Vertx.vertx();
		vertx.deployVerticle(new ServerApi());
	}
}
