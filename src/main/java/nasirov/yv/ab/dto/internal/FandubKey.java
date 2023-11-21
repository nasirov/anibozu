package nasirov.yv.ab.dto.internal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import nasirov.yv.starter.common.constant.FandubSource;

/**
 * @author Nasirov Yuriy
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FandubKey {

	private FandubSource fandubSource;

	private Integer malId;
}
