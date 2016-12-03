package papayaDB.api;

import io.vertx.core.Verticle;
import io.vertx.ext.web.RoutingContext;

public interface Api extends Verticle{
	
	public void createDb(RoutingContext routingContext);
	
	public void deleteDb(RoutingContext routingContext);

	public void get(RoutingContext routingContext);
	
	public void getAll(RoutingContext routingContext);

	public void post(RoutingContext routingContext);
	
	public void delete(RoutingContext routingContext);
}
