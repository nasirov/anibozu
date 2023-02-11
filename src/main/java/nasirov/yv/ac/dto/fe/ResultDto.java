package nasirov.yv.ac.dto.fe;

import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Nasirov Yuriy
 */
@Data
@NoArgsConstructor
public class ResultDto {

	@NotNull
	private String titlesJson = "";

	@NotNull
	private String fandubMapJson = "";

	@NotNull
	private String errorMessage = "";

	public ResultDto(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public ResultDto(String titlesJson, String fandubMapJson) {
		this.titlesJson = titlesJson;
		this.fandubMapJson = fandubMapJson;
	}
}
