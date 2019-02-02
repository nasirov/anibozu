package nasirov.yv.enums;

/**
 * Общие константы
 * Created by Хикка on 23.12.2018.
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
