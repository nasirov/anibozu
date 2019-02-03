package nasirov.yv.enums;

/**
 * Created by nasirov.yv
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
