package nasirov.yv.enums;

/**
 * Created by Хикка on 23.01.2019.
 */
public enum AnimeTypeOnAnimedia {
	MULTISEASONS("multi"),
	SINGLESEASON("single"),
	ANNOUNCEMENT("announcement");
	
	private String description;
	
	AnimeTypeOnAnimedia(String description) {
		this.description = description;
	}
	
	public String getDescription() {
		return description;
	}
}
