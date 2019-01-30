package nasirov.yv.enums;

/**
 * Параметры запроса
 * Created by Хикка on 20.12.2018.
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
