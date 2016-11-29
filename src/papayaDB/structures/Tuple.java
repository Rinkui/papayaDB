package papayaDB.structures;

public class Tuple implements Comparable<Tuple>{
	private final String req;
	private final String reqValue;

	public Tuple(String req, String reqValue) {
		this.req = req;
		this.reqValue = reqValue;
	}

	public String getReq() {
		return req;
	}

	public String getReqValue() {
		return reqValue;
	}

	@Override
	public int hashCode() {
		return req.hashCode()^reqValue.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (!(obj instanceof Tuple))
			return false;
		Tuple request = (Tuple) obj;
		return req.equals(request.req) && reqValue.equals(request.reqValue);
	}

	@Override
	public int compareTo(Tuple req2) {
		int reqRes = this.req.compareTo(req2.req);
		if( reqRes == 0)
			return this.reqValue.compareTo(req2.reqValue);
		return reqRes;
	}
	
	@Override
	public String toString() {
		return "(" + req +  ";" + reqValue + ")";
	}
}