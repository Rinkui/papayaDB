package papayaDB.server;

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
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.impl.Handlers;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import papayaDB.structures.Tuple;

public class TestServer extends AbstractVerticle{
	BlockingQueue<Runnable> queue = new ArrayBlockingQueue<>(100);
	Executor executor = new ThreadPoolExecutor(10, 50, 10, TimeUnit.MINUTES, queue);
	
	@Override
	public void start() {
		Router router = Router.router(vertx);

		// route to JSON REST APIs
		router.get("/*").handler(this::get);
		router.post("/*").handler(this::post);

		// otherwise serve static pages
		router.route().handler(StaticHandler.create());

		vertx.createHttpServer().requestHandler(router::accept).listen(8080);
		System.out.println("listen on port 8080");
	}

	//http://localhost:8080/books/name/%22abc%20/def%22/year/[2010;2015]/price/[;30]?filtre=ok
	private void get(RoutingContext routingContext){
		executor.execute(runnableForGet(routingContext));
	}
	
	private Runnable runnableForGet(RoutingContext routingContext){
		return new Runnable() {
			@Override
			public void run() {
				HttpServerResponse response = routingContext.response();
				HttpServerRequest request = routingContext.request();
				
				request.dataHandle(new Handler<Buffer>() {

					@Override
					public void handle(Buffer arg0) {
						// TODO Auto-generated method stub
						
					}
					
				});
			}
		};
	}
	
	private void post(RoutingContext routingContext){
		executor.execute(runnableForPost(routingContext));
	}
	
	private Runnable runnableForPost(RoutingContext routingContext){
		return new Runnable() {
			@Override
			public void run() {
				HttpServerResponse response = routingContext.response();
				HttpServerRequest request = routingContext.request();
				
				String[] pathCut = splitRequest(request.path());
				
				if(pathCut.length%2 == 0){
					response.setStatusCode(400).end("Request is not correct.");
				}
				else{
					String base = pathCut[0];
					List<Tuple<String, String>> filter = requestToArray(pathCut);
					response.setStatusCode(200).end("Request: \n\tBase: " + base + "\n\tFilter: " + filter);
				}
			}
		};
	}

	private List<Tuple<String, String>> requestToArray(String[] request){
		List<Tuple<String, String>> result = new ArrayList<>(request.length/2);
		int i;
		for(i = 1; i < request.length - 1; i = i+2){
			result.add(i/2, new Tuple<String, String>(request[i], request[i+1]));
		}
		return result;
	}
	
	private String[] splitRequest(String request){
		String path = request.replaceAll("%22", "\"").replaceAll("%20", " ");
		
		Pattern pattern = Pattern.compile("\\/(\"(\\[{0,1}(\\w|\\s|\\/|;)*\\]{0,1})\"|\\[{0,1}(\\w|\\s|;)*\\]{0,1})");
		Matcher m = pattern.matcher(path);
		
		return m.results().map(c -> c.group(1)).toArray(String[]::new);
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
