package nasirov.yv.data.mal;

import javax.validation.constraints.Pattern;
import lombok.Data;
import org.springframework.validation.annotation.Validated;

/**
 * Created by nasirov.yv
 */
@Data
@Validated
public class MALUser {

	@Pattern(regexp = "^[\\w-]{2,16}$", message = "Please enter a valid mal username between 2 and 16 characters(latin letters, numbers, underscores "
			+ "and dashes only)")
	private String username;

}
