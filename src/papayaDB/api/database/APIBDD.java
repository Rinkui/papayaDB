package papayaDB.api.database;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import papayaDB.api.Api;
import papayaDB.db.DataBase;

public class APIBDD implements Api {
	private ConcurrentHashMap<String, DataBase> dataBasePool = new ConcurrentHashMap();
	BlockingQueue<Runnable> queue = new ArrayBlockingQueue<>(100);
	Executor executor = new ThreadPoolExecutor(10, 50, 10, TimeUnit.MINUTES, queue);

	@Override
	public Vertx getVertx() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void init(Vertx arg0, Context arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void start(Future<Void> arg0) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void stop(Future<Void> arg0) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void start() {
		// TODO Auto-generated method stub

	}

}
