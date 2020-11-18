package nasirov.yv.service;

import java.util.List;
import nasirov.yv.data.mal.MalUserInfo;
import nasirov.yv.exception.mal.AbstractMalException;
import nasirov.yv.fandub.service.spring.boot.starter.dto.mal.MalTitle;

/**
 * Created by nasirov.yv
 */
public interface MalServiceI {

	List<MalTitle> getWatchingTitles(String username) throws AbstractMalException;

	MalUserInfo getMalUserInfo(String username);
}
