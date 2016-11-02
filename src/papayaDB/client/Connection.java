package papayaDB.client;


import java.util.Objects;
import java.util.function.Consumer;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import papayaDB.api.QueryAnswer;
import papayaDB.api.chainable.AbstractChainableQueryInterface;

/**
 * Cette classe représente une connexion utilisateur (un "noeud de tête") pour faire des requêtes sur un noeud papayaDB.
 */
public class Connection extends AbstractChainableQueryInterface {
	private final HttpClient client;
	private final int port;
	private final String host;
	
	public Connection(String host, int port) {
		client = Vertx.vertx().createHttpClient();
		this.host = host;
		this.port = port;
	}
	
	public Connection(String host) {
		this(host, 80);
	}
	
	
	@Override
	public void processQuery(String query,Consumer<QueryAnswer> callback) {
		Objects.requireNonNull(callback);
		client.getNow(port, host, "/"+query, response -> {
			System.out.println("Received response with status code " + response.statusCode());
			
			response.bodyHandler(bodyBuffer -> { callback.accept(new QueryAnswer(bodyBuffer.toJsonObject())); });
		});
	}
}