package nasirov.yv.enums;

/**
 * Created by nasirov.yv
 */
public enum RequestParameters {
	HEADER("header"),
	COOKIE("cookie"),
	ACCEPT("accept");
	
	private String description;
	
	public String getDescription() {
		return description;
	}
	
	RequestParameters(String description) {
		this.description = description;
	}
}
