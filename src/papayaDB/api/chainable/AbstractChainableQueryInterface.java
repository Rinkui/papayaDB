package papayaDB.api.chainable;

import java.util.function.Consumer;

import io.vertx.core.Vertx;
import papayaDB.api.QueryAnswer;
import papayaDB.api.QueryInterface;

/**
 * Cette classe abstraite implémente les concepts communs aux "interfaces chainées" : des interfaces de requête pouvant traiter celles qu'elles reçoivent en les renvoyant vers d'autres (ou pas).
 * On peut considérer cette classe comme la classe la plus adaptée à étendre pour créer ses propres QueryInterface.
 * Cette implémentation définit le fonctionnement d'une AbstractChainableQueryInterface.
 */
public abstract class AbstractChainableQueryInterface implements QueryInterface {
	private final Vertx vertx;
	
	public AbstractChainableQueryInterface() {
		vertx = Vertx.vertx();
	}
	
	@Override
	public void processQuery(String query,Consumer<QueryAnswer> callback) {
		throw new UnsupportedOperationException();
	}
	
	public void close() {
		vertx.close();
	}
	
	public Vertx getVertx() {
		return vertx;
	}
}