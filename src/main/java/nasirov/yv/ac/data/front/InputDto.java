package nasirov.yv.ac.data.front;

import javax.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;

/**
 * @author Nasirov Yuriy
 */
@Data
@Builder
@Validated
@NoArgsConstructor
@AllArgsConstructor
public class InputDto {

	@Pattern(regexp = "^[\\w-]{2,16}$", message =
			"Please enter a valid mal username between 2 and 16 characters(latin letters, numbers, underscores "
					+ "and dashes only)")
	private String username;

	public void setUsername(String username) {
		this.username = StringUtils.lowerCase(username);
	}
}
