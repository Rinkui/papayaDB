package papayaDB.structures;

public class Tuple<T, V>{
	private final T req;
	private final V reqValue;

	public Tuple(T req, V reqValue) {
		this.req = req;
		this.reqValue = reqValue;
	}

	public T getKey() {
		return req;
	}

	public V getValue() {
		return reqValue;
	}

	@Override
	public int hashCode() {
		return req.hashCode() ^ reqValue.hashCode();
	}

	@SuppressWarnings("unchecked") // on est sur que obj est un tuple
	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (!(obj instanceof Tuple))
			return false;
		Tuple<T, V> request = (Tuple<T, V>) obj;
		return req.equals(request.req) && reqValue.equals(request.reqValue);
	}

	@Override
	public String toString() {
		return "(" + req + ";" + reqValue + ")";
	}
}
