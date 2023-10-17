package nasirov.yv.ab.dto.fe;

import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Nasirov Yuriy
 */
@Data
@NoArgsConstructor
public class ProcessResult {

	@NotNull
	private List<Anime> animeList = new ArrayList<>();

	@NotNull
	private String errorMessage = "";

	public ProcessResult(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public ProcessResult(List<Anime> animeList) {
		this.animeList = animeList;
	}
}
