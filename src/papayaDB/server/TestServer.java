package papayaDB.server;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.StaticHandler;

public class TestServer extends AbstractVerticle{
	@Override
	  public void start() {
	    Router router = Router.router(vertx);
	    
	    // route to JSON REST APIs
	    router.get("/test").handler(routingContext -> this.test(routingContext,router));
	    router.get("/all").handler(routingContext -> this.test(routingContext,router));
	    router.get("/get/:name/:id").handler(routingContext -> this.test(routingContext,router));
	    
	    // otherwise serve static pages
	    router.route().handler(StaticHandler.create());

	    vertx.createHttpServer().requestHandler(router::accept).listen(8080);
	    System.out.println("listen on port 8080");
	  }
	  
	  private void test(RoutingContext routingContext, Router router){
		  HttpServerRequest r = routingContext.request();
		  System.out.println(r.params().names());
		  for(String name : r.params().names()){
			  System.out.println(name + ": " + r.getParam(name));
			  routingContext.reroute(r.getParam(name));
		  }
		  for(Route route : router.getRoutes()){
			  System.out.println(route.getPath());
		  }
		  System.out.println("END");
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
