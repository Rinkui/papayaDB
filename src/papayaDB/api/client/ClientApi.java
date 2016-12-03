package papayaDB.api.client;

import java.util.List;
import java.util.stream.Stream;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpClientResponse;
import papayaDB.api.Api;
import papayaDB.structures.Tuple;

public class ClientApi implements Api{
	private final HttpClient httpclient;
	
	public ClientApi(HttpClient httpclient){
		this.httpclient = httpclient;
	}

	@Override
	public boolean createDb(String dbName) {
		httpclient.post("/" + dbName, new Handler<HttpClientResponse>() {
			@Override
			public void handle(HttpClientResponse response) {
				response.handler(new Handler<Buffer>() {
					@Override
					public void handle(Buffer b) {
						System.out.println(b.toString());
					}
				});
			}
		}).end();
		return false;
	}

	@Override
	public boolean deleteDb(String dbName) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Stream<Object> get(String dbName, List<Tuple<String, String>> filter) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Stream<Object> getAll(String dbName) {
		// TODO Auto-generated method stub
		return null;
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
	
	//name/"abc /def"/year/[2010;2015]/price/[;30]
	public static void main(String[] args) {
		// development option, avoid caching to see changes of
		// static files without having to reload the application,
		// obviously, this line should be commented in production
		//System.setProperty("vertx.disableFileCaching", "true");
		
		Vertx vertx = Vertx.vertx();
		HttpClientOptions hco = new HttpClientOptions().setDefaultHost("localhost").setDefaultPort(8080);
		ClientApi ca = new ClientApi(vertx.createHttpClient(hco));
		
//		List<Tuple<String, String>> filter = new ArrayList<>(3);
//		filter.add(new Tuple<>("name", "abc /def"));
//		filter.add(new Tuple<>("year", "[2010;2015]"));
//		filter.add(new Tuple<>("price", "[;30]"));
		ca.createDb("books");
	}
	
}
