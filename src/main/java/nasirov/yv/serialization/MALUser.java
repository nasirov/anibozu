package nasirov.yv.serialization;

import javax.validation.constraints.Pattern;
import lombok.Data;

/**
 * Created by nasirov.yv
 */
@Data
public class MALUser {

	@Pattern(regexp = "^[\\w-]{2,16}$", message = "Please enter a valid mal username between 2 and 16 characters(latin letters, numbers, underscores "
			+ "and dashes only)")
	private String username;

}
