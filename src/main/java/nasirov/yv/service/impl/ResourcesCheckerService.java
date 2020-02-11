package nasirov.yv.service.impl;

import static nasirov.yv.data.constants.BaseConstants.FIRST_DATA_LIST;
import static nasirov.yv.data.constants.BaseConstants.FIRST_EPISODE;
import static nasirov.yv.util.AnimediaUtils.isAnnouncement;
import static nasirov.yv.util.AnimediaUtils.isTitleNotFoundOnMAL;

import feign.template.UriUtils;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.data.animedia.AnimediaSearchListTitle;
import nasirov.yv.data.animedia.TitleReference;
import nasirov.yv.data.animedia.api.Response;
import nasirov.yv.data.animedia.api.Season;
import nasirov.yv.data.properties.ResourcesNames;
import nasirov.yv.parser.WrappedObjectMapperI;
import nasirov.yv.service.AnimediaApiServiceI;
import nasirov.yv.service.MALServiceI;
import nasirov.yv.service.ReferencesServiceI;
import nasirov.yv.service.ResourcesCheckerServiceI;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Created by nasirov.yv
 */
@Slf4j
@Service
@Profile(value = {"local", "test"})
@RequiredArgsConstructor
public class ResourcesCheckerService implements ResourcesCheckerServiceI {

	private final ReferencesServiceI referencesService;

	private final AnimediaApiServiceI animediaApiService;

	private final MALServiceI malService;

	private final ResourcesNames resourcesNames;

	private final WrappedObjectMapperI wrappedObjectMapper;

	@Override
	@Scheduled(cron = "${application.cron.resources-check-cron-expression}")
	public void checkReferencesNames() {
		log.info("START CHECKING REFERENCES NAMES ON MAL ...");
		Set<TitleReference> allReferences = referencesService.getReferences();
		List<TitleReference> referencesWithInvalidMALTitleName = new LinkedList<>();
		for (TitleReference reference : allReferences) {
			String titleOnMAL = reference.getTitleNameOnMAL();
			Integer titleIdOnMAL = reference.getTitleIdOnMAL();
			if (!isTitleNotFoundOnMAL(reference)) {
				boolean titleExist = malService.isTitleExist(titleOnMAL, titleIdOnMAL);
				if (!titleExist) {
					log.error("TITLE {} WITH ID {} DOESN'T EXIST!", titleOnMAL, titleIdOnMAL);
					referencesWithInvalidMALTitleName.add(TitleReference.builder()
							.urlOnAnimedia(reference.getUrlOnAnimedia())
							.animeIdOnAnimedia(reference.getAnimeIdOnAnimedia())
							.dataListOnAnimedia(reference.getDataListOnAnimedia())
							.minOnAnimedia(reference.getMinOnAnimedia())
							.titleNameOnMAL(titleOnMAL)
							.titleIdOnMAL(titleIdOnMAL)
							.build());
				}
			}
		}
		marshallToTempFolder(resourcesNames.getTempReferencesWithInvalidMALTitleName(), referencesWithInvalidMALTitleName);
		log.info("END CHECKING REFERENCES NAMES ON MAL.");
	}

	@Override
	@Scheduled(cron = "${application.cron.resources-check-cron-expression}")
	public void checkReferences() {
		log.info("START CHECKING REFERENCES ...");
		Set<AnimediaSearchListTitle> animediaSearchListFromGitHub = animediaApiService.getAnimediaSearchList();
		Set<TitleReference> allReferences = referencesService.getReferences();
		List<TitleReference> notFoundInReferences = new LinkedList<>();
		for (AnimediaSearchListTitle titleSearchInfo : animediaSearchListFromGitHub) {
			// TODO: 18.12.2019 remove after fix invalid json object from animedia api
			if ("16521".equals(titleSearchInfo.getAnimeId())) {
				continue;
			}
			List<TitleReference> references = getMatchedReferences(allReferences, titleSearchInfo);
			if (isAnnouncement(titleSearchInfo)) {
				if (references.isEmpty()) {
					log.error("ANNOUNCEMENT MUST BE PRESENT IN ONE REFERENCE {}", titleSearchInfo);
					notFoundInReferences.add(buildTempAnnouncementReference(titleSearchInfo));
				}
			} else {
				Response response = animediaApiService.getTitleInfo(titleSearchInfo.getAnimeId());
				List<Season> seasons = response.getSeasons();
				for (Season season : seasons) {
					boolean titleIsNotPresentInReferences = references.stream()
							.noneMatch(x -> x.getDataListOnAnimedia()
									.equals(season.getDataList()));
					if (titleIsNotPresentInReferences) {
						log.error("TITLE IS NOT PRESENT IN REFERENCES {}/{}", titleSearchInfo.getUrl(), season.getDataList());
						notFoundInReferences.add(buildTempReference(titleSearchInfo, season));
					}
				}
			}
		}
		marshallToTempFolder(resourcesNames.getTempRawReferences(), notFoundInReferences);
		log.info("END CHECKING REFERENCES.");
	}

	private TitleReference buildTempReference(AnimediaSearchListTitle titleSearchInfo, Season season) {
		return TitleReference.builder()
				.urlOnAnimedia(titleSearchInfo.getUrl())
				.animeIdOnAnimedia(titleSearchInfo.getAnimeId())
				.dataListOnAnimedia(season.getDataList())
				.build();
	}

	private TitleReference buildTempAnnouncementReference(AnimediaSearchListTitle titleSearchInfo) {
		return TitleReference.builder()
				.urlOnAnimedia(titleSearchInfo.getUrl())
				.dataListOnAnimedia(FIRST_DATA_LIST)
				.minOnAnimedia(FIRST_EPISODE)
				.build();
	}

	private List<TitleReference> getMatchedReferences(Set<TitleReference> allReference, AnimediaSearchListTitle titleSearchInfo) {
		List<TitleReference> references;
		references = allReference.stream()
				.filter(x -> titleSearchInfo.getUrl()
						.equals(UriUtils.decode(x.getUrlOnAnimedia(), StandardCharsets.UTF_8)))
				.collect(Collectors.toList());
		return references;
	}

	private void marshallToTempFolder(String tempFileName, Collection<?> content) {
		if (!content.isEmpty()) {
			wrappedObjectMapper.marshal(buildResultFile(tempFileName), content);
		}
	}

	private File buildResultFile(String tempFileName) {
		return new File(new File(resourcesNames.getTempFolder()), tempFileName);
	}
}
