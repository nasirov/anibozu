package nasirov.yv.data.front;

import java.util.ArrayList;
import java.util.List;
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
	private List<TitleDto> availableTitles = new ArrayList<>();

	@NotNull
	private List<TitleDto> notAvailableTitles = new ArrayList<>();

	@NotNull
	private List<TitleDto> notFoundTitles = new ArrayList<>();

	@NotNull
	private String errorMessage = "";

	public ResultDto(String errorMessage) {
		this.errorMessage = errorMessage;
	}
}
