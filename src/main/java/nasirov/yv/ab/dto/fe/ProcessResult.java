package nasirov.yv.ab.dto.fe;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import nasirov.yv.starter.common.constant.FandubSource;

/**
 * @author Nasirov Yuriy
 */
@Data
@NoArgsConstructor
public class ProcessResult {

	@NotNull
	private List<Title> titles = new ArrayList<>();

	@NotNull
	private Map<FandubSource, String> fandubMap = new EnumMap<>(FandubSource.class);

	@NotNull
	private String errorMessage = "";

	public ProcessResult(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public ProcessResult(List<Title> titles, Map<FandubSource, String> fandubMap) {
		this.titles = titles;
		this.fandubMap = fandubMap;
	}
}
