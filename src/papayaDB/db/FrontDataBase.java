package papayaDB.db;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import io.vertx.ext.web.RoutingContext;

public class FrontDataBase {
	private ConcurrentHashMap<String, DataBase> dataBasePool = new ConcurrentHashMap();
	BlockingQueue<Runnable> queue = new ArrayBlockingQueue<>(100);
	Executor executor = new ThreadPoolExecutor(10, 50, 10, TimeUnit.MINUTES, queue);
	
	
}