package nasirov.yv.data.front;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import nasirov.yv.fandub.service.spring.boot.starter.constant.FandubSource;

/**
 * @author Nasirov Yuriy
 */
@Data
@NoArgsConstructor
public class ResultDto {

	@NotNull
	private List<TitleDto> titles = new ArrayList<>();

	@NotNull
	private Set<FandubSource> fandubSources = new HashSet<>();

	@NotNull
	private String errorMessage = "";

	public ResultDto(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public ResultDto(Set<FandubSource> fandubSources) {
		this.fandubSources = fandubSources;
	}
}
