package papayaDB.db;

public class Request {
	private final String req;
	private final String reqValue;

	public Request(String req, String reqValue) {
		this.req = req;
		this.reqValue = reqValue;
	}

	public String getReq() {
		return req;
	}

	public String getReqValue() {
		return reqValue;
	}
	
	// auto générée si t'es pas content O:)

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((req == null) ? 0 : req.hashCode());
		result = prime * result + ((reqValue == null) ? 0 : reqValue.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Request other = (Request) obj;
		if (req == null) {
			if (other.req != null)
				return false;
		} else if (!req.equals(other.req))
			return false;
		if (reqValue == null) {
			if (other.reqValue != null)
				return false;
		} else if (!reqValue.equals(other.reqValue))
			return false;
		return true;
	}
}
