package nasirov.yv.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by nasirov.yv
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HttpResponse {
	/**
	 * Response content
	 */
	private String content;
	
	/**
	 * Http status
	 */
	private Integer status;
}
