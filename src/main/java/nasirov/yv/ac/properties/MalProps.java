package nasirov.yv.ac.properties;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
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
