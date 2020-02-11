package nasirov.yv.service;

import java.util.List;
import java.util.Set;
import nasirov.yv.data.animedia.AnimediaSearchListTitle;
import nasirov.yv.data.animedia.api.Response;

/**
 * Created by nasirov.yv
 */
public interface AnimediaApiServiceI {

	Set<AnimediaSearchListTitle> getAnimediaSearchList();

	Response getTitleInfo(String animeId);

	List<Response> getDataListInfo(String animeId, String dataList);
}
