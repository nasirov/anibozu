package nasirov.yv.service;


import java.util.List;
import java.util.Map;
import nasirov.yv.fandub.dto.constant.FanDubSource;
import nasirov.yv.fandub.dto.fandub.common.CommonTitle;
import nasirov.yv.fandub.dto.mal.MalTitle;

/**
 * Created by nasirov.yv
 */
public interface TitlesServiceI {

	/**
	 * Groups list of {@link CommonTitle} by {@link CommonTitle#getMalId()} for O(1) search by {@link MalTitle#getId()}
	 *
	 * @return a map which key - {@link CommonTitle#getMalId()}, value - list of titles with matched {@link CommonTitle#getMalId()}
	 */
	Map<Integer, List<CommonTitle>> getTitles(FanDubSource fanDubSource);
}
