package nasirov.yv.service;

import java.util.List;
import java.util.Map;
import nasirov.yv.data.front.TitleDto;
import nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.common.CommonTitle;
import nasirov.yv.fandub.service.spring.boot.starter.dto.mal.MalTitle;

/**
 * @author Nasirov Yuriy
 */
public interface TitleServiceI {

	TitleDto buildTitle(MalTitle watchingTitle, Map<FanDubSource, List<CommonTitle>> commonTitlesByFandubSource);
}
