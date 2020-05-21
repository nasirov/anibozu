package nasirov.yv.data.mal;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by nasirov.yv
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MALSearchResult {

	private List<MALSearchCategories> categories;
}
