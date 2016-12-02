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
import papayaDB.structures.Tuple;

public class TestServer extends AbstractVerticle{
	BlockingQueue<Runnable> queue = new ArrayBlockingQueue<>(100);
	Executor executor = new ThreadPoolExecutor(10, 50, 10, TimeUnit.MINUTES, queue);
	
	@Override
	public void start() {
		Router router = Router.router(vertx);
		
		//For DB
		router.post("/:dbNAme").handler(this::createDb);
		router.delete("/:dbName/confirm").handler(this::deleteDb);

		//For Element
		router.get("/:dbName/all").handler(this::getAll);
		router.get("/:dbName/*").handler(this::get);
		router.post("/:dbName").handler(this::post);
		router.delete("/:dbName/:id").handler(this::delete);

		// otherwise serve static pages
		router.route().handler(StaticHandler.create());

		vertx.createHttpServer().requestHandler(router::accept).listen(8080);
		System.out.println("listen on port 8080");
	}
	
	private void createDb(RoutingContext routingContext){
		executor.execute(runnableForCreateDb(routingContext));
	}
	
	private void deleteDb(RoutingContext routingContext){
		executor.execute(runnableForDeleteDb(routingContext));
	}

	//http://localhost:8080/books/name/%22abc%20/def%22/year/[2010;2015]/price/[;30]?filtre=ok
	private void get(RoutingContext routingContext){
		executor.execute(runnableForGet(routingContext));
	}
	
	private void getAll(RoutingContext routingContext){
		executor.execute(runnableForGetAll(routingContext));
	}

	private void post(RoutingContext routingContext){
		executor.execute(runnableForPost(routingContext));
	}
	
	private void delete(RoutingContext routingContext) {
		executor.execute(runnableForDelete(routingContext));
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
					response.setStatusCode(200).end("Request: \n\tBase: " + base + "\n\tFilter: " + filter);
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
				System.out.println("Do getAll");
				response.setStatusCode(200).end("TODO getAll");
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


	public static void main(String[] args) {
		// development option, avoid caching to see changes of
		// static files without having to reload the application,
		// obviously, this line should be commented in production
		//System.setProperty("vertx.disableFileCaching", "true");

		Vertx vertx = Vertx.vertx();
		vertx.deployVerticle(new TestServer());
	}
}
