package kth.id2203.register;


public class ReadInfo {
    private final Integer ts, wr, key, nodeid;
    private final String val;

    public ReadInfo(Integer ts, Integer wr, Integer key, String val, Integer nodeId) {
        this.ts = ts;
        this.wr = wr;
        this.key = key;
        this.val = val;
        this.nodeid = nodeId;
    }

    public Integer getTs() {
        return ts;
    }

    public Integer getWr() {
        return wr;
    }

	public Integer getKey() {
		return key;
	}

	public String getVal() {
        return val;
    }

    public Integer getNodeid() {
        return nodeid;
    }
}
