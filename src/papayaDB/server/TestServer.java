package papayaDB.server;

import java.util.Arrays;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.StaticHandler;

public class TestServer extends AbstractVerticle{
	@Override
	  public void start() {
	    Router router = Router.router(vertx);
	    
	    // route to JSON REST APIs
	    router.get("/*").handler(this::test);
//	    router.get("/all").handler(routingContext -> this.test(routingContext,router));
//	    router.get("/get/:name/:id").handler(routingContext -> this.test(routingContext,router));
	    
	    // otherwise serve static pages
	    router.route().handler(StaticHandler.create());

	    vertx.createHttpServer().requestHandler(router::accept).listen(8080);
	    System.out.println("listen on port 8080");
	  }
	  
	  private void test(RoutingContext routingContext){
		  HttpServerResponse response = routingContext.response();
		  HttpServerRequest request = routingContext.request();
		  String path = request.path().substring(1).replaceAll("%22", "\"").replaceAll("%20", " ");
		  System.out.println(path);
		  String[] pathCut = path.split("/");
		  System.out.println(Arrays.toString(pathCut));
		  if(pathCut.length%2 == 0){
			  response.setStatusCode(400).end("Request is not correct.");
		  }
		  else{
			  response.setStatusCode(200).end("Request: " + path);
		  }
//		  System.out.println(request.params().names());
//		  for(String name : request.params().names()){
//			  System.out.println(name + ": " + request.getParam(name));
//		  }
		  System.out.println("END PARAM");
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
