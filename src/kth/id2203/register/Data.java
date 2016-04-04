package kth.id2203.register;

public class Data {

	private Integer ts, wr;
	private String val;
	
	public Data(Integer ts, Integer wr, String val) {
		this.ts = ts;
		this.wr = wr;
		this.val = val;
	}

	public void incTs() {
		this.ts++;
	}
	public Integer getTs() {
		return ts;
	}

	public Integer getWr() {
		return wr;
	}

	public String getVal() {
		return val;
	}

	public void setTs(Integer ts) {
		this.ts = ts;
	}

	public void setWr(Integer wr) {
		this.wr = wr;
	}

	public void setVal(String val) {
		this.val = val;
	}
	
	
	
}
