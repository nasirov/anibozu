package nasirov.yv.ac.dto.mal;

import java.util.List;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import nasirov.yv.starter.common.dto.mal.MalTitle;

/**
 * @author Nasirov Yuriy
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MalUserInfo {

	@NotNull
	private String username;

	@NotNull
	private List<MalTitle> malTitles;

	@NotNull
	private String errorMessage;
}
