package nasirov.yv.data.mal;

import java.util.List;
import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import nasirov.yv.fandub.service.spring.boot.starter.dto.mal.MalTitle;

/**
 * Created by nasirov.yv
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

	@Nullable
	private String errorMessage;
}
