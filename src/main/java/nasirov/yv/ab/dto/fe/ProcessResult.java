package nasirov.yv.ab.dto.fe;

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
public class ProcessResult {

	@NotNull
	private List<Title> titles = new ArrayList<>();

	@NotNull
	private String errorMessage = "";

	public ProcessResult(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public ProcessResult(List<Title> titles) {
		this.titles = titles;
	}
}
