package nasirov.yv.data.request;

/**
 * Created by nasirov.yv
 */
public enum RequestParameters {
	HEADER("header"), COOKIE("cookie");

	private String description;

	RequestParameters(String description) {
		this.description = description;
	}
	public String getDescription() {
		return description;
	}
}
