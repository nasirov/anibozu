package nasirov.yv.ab.properties;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author Nasirov Yuriy
 */
@Data
public class MalProps {

	@NotNull
	private Integer offsetStep;

	@NotBlank
	private String url;
}
