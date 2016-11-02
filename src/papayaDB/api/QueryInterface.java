package papayaDB.api;

import java.util.function.Consumer;

/**
 * Interface représentant une interface de requête de papayaDB. La manière dont la requête est traitée par l'interface n'est pas définie. (c'est le principe d'une interface, de faire des promesses sans les
 * préciser).
 *
 */
public interface QueryInterface {
	public void processQuery(String query,Consumer<QueryAnswer> callback);
}