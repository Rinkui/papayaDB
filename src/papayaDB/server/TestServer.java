package papayaDB.server;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.StaticHandler;
import papayaDB.structures.Request;

public class TestServer extends AbstractVerticle{
	@Override
	public void start() {
		Router router = Router.router(vertx);

		// route to JSON REST APIs
		router.get("/*").handler(this::get);

		// otherwise serve static pages
		router.route().handler(StaticHandler.create());

		vertx.createHttpServer().requestHandler(router::accept).listen(8080);
		System.out.println("listen on port 8080");
	}

	//http://localhost:8080/books/name/%22abc%20/def%22/year/[2010;2015]/price/[;30]?filtre=ok
	private void get(RoutingContext routingContext){
		HttpServerResponse response = routingContext.response();
		HttpServerRequest request = routingContext.request();
		
		String[] pathCut = splitRequest(request.path());
		System.out.println(Arrays.toString(pathCut));
		
		if(pathCut.length%2 == 0){
			response.setStatusCode(400).end("Request is not correct.");
		}
		else{
			String base = pathCut[0];
			Request[] filter = requestToArray(pathCut);

			response.setStatusCode(200).end("Request: \n\tBase: " + base + "\n\tFilter: " + Arrays.toString(filter));
		}
		System.out.println("END PARAM");
	}

	private Request[] requestToArray(String[] request){
		Request[] result = new Request[request.length/2];
		int i;
		for(i = 1; i < request.length - 1; i = i+2){
			result[i/2] = new Request(request[i], request[i+1]);
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
