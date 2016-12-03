package papayaDB.api.server;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.StaticHandler;
import papayaDB.api.Api;
import papayaDB.structures.Tuple;

public class ServerApi extends AbstractVerticle implements Api{
	BlockingQueue<Runnable> queue = new ArrayBlockingQueue<>(100);
	Executor executor = new ThreadPoolExecutor(10, 50, 10, TimeUnit.MINUTES, queue);
	
	@Override
	public void start() {
		Router router = Router.router(vertx);
		
		//For DB
		router.post("/:dbNAme").handler(routingContext -> executor.execute(runnableForCreateDb(routingContext)));
		router.delete("/:dbName/confirm").handler(routingContext -> executor.execute(runnableForDeleteDb(routingContext)));

		//For Element
		//http://localhost:8080/books/name/"abc /def"/year/[2010;2015]/price/[;30]
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
		//TODO
		return new Runnable() {
			@Override
			public void run() {
				HttpServerResponse response = routingContext.response();
				System.out.println("Do cretae Db");
				response.setStatusCode(200).end("TODO create Db");
			}
		};
	}
	
	private Runnable runnableForDeleteDb(RoutingContext routingContext) {
		//TODO
		return new Runnable() {
			@Override
			public void run() {
				HttpServerResponse response = routingContext.response();
				System.out.println("Do delete Db");
				response.setStatusCode(200).end("TODO delete Db");
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
					response.setStatusCode(400).end("Request is not correct.");
				}
				else{
					String base = request.getParam("dbName");
					List<Tuple<String, String>> filter = requestToList(pathCut);
					response.setStatusCode(200).end(get(base, filter).map(jsonObject -> jsonObject.encode()).collect(Collectors.joining(",", "[", "]")));
					//response.setStatusCode(200).end("Request: \n\tBase: " + base + "\n\tFilter: " + filter);
				}
			}
		};
	}
	
	private Runnable runnableForGetAll(RoutingContext routingContext) {
		//TODO
		return new Runnable() {
			@Override
			public void run() {
				HttpServerResponse response = routingContext.response();
				HttpServerRequest request = routingContext.request();
				System.out.println(request.path());
				String base = request.getParam("dbName");
				response.setStatusCode(200).end(getAll(base).map(jsonObject -> jsonObject.encode()).collect(Collectors.joining(",", "[", "]")));
			}
		};
	}
	
	private Runnable runnableForPost(RoutingContext routingContext){
		return new Runnable() {
			@Override
			public void run() {
				HttpServerResponse response = routingContext.response();
				HttpServerRequest request = routingContext.request();
				
				request.handler(new Handler<Buffer>() {
					@Override
					public void handle(Buffer arg0) {
						String base = request.getParam("dbName");
						List<Tuple<String, String>> fields = jsonToList(new JsonObject(arg0.toString()));
						response.setStatusCode(200).end("Request: \n\tBase: " + base + "\n\tFields: " + fields);
					}
				});
			}
		};
	}

	private Runnable runnableForDelete(RoutingContext routingContext) {
		//TODO
		return new Runnable() {
			@Override
			public void run() {
				HttpServerResponse response = routingContext.response();
				System.out.println("Do delete");
				response.setStatusCode(200).end("TODO delete");
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
	public boolean createDb(String dbName) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean deleteDb(String dbName) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Stream<JsonObject> get(String dbName, List<Tuple<String, String>> filter) {
		return IntStream.range(0, 50).mapToObj(i -> listToJson(test(i)));
	}
	
	private List<Tuple<String, String>> test(int i){
		ArrayList<Tuple<String, String>> result = new ArrayList<>(1);
		result.add(new Tuple<String, String>("id", String.valueOf(i)));
		return result;
	}

	@Override
	public Stream<JsonObject> getAll(String dbName) {
		return IntStream.range(0, 50).mapToObj(i -> listToJson(test(i)));
	}

	@Override
	public boolean post(String dbName, List<Tuple<String, String>> fields) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean delete(String dbName, int id) {
		// TODO Auto-generated method stub
		return false;
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
