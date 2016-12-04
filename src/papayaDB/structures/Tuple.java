package papayaDB.structures;

public class Tuple<T, V>{
	private final T key;
	private final V value;

	public Tuple(T req, V reqValue) {
		this.key = req;
		this.value = reqValue;
	}

	public T getKey() {
		return key;
	}

	public V getValue() {
		return value;
	}

	@Override
	public int hashCode() {
		return key.hashCode() ^ value.hashCode();
	}

	@SuppressWarnings("unchecked") // on est sur que obj est un tuple
	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (!(obj instanceof Tuple))
			return false;
		Tuple<T, V> request = (Tuple<T, V>) obj;
		return key.equals(request.key) && value.equals(request.value);
	}

	@Override
	public String toString() {
		return "\"" + key + "\"=" + value;
	}
}
