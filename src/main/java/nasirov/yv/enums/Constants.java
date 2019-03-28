package nasirov.yv.enums;

/**
 * Created by nasirov.yv
 */
public enum Constants {
	FIRST_EPISODE("1");

	private String description;

	public String getDescription() {
		return description;
	}

	Constants(String description) {
		this.description = description;
	}
}
