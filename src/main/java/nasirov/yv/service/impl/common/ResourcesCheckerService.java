package nasirov.yv.service.impl.common;

import static java.nio.charset.StandardCharsets.UTF_8;
import static nasirov.yv.data.constants.BaseConstants.FIRST_DATA_LIST;
import static nasirov.yv.data.constants.BaseConstants.FIRST_EPISODE;
import static nasirov.yv.util.AnimediaUtils.isAnnouncement;
import static nasirov.yv.util.AnimediaUtils.isTitleNotFoundOnMAL;

import feign.template.UriUtils;
import java.io.File;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.data.animedia.AnimediaSearchListTitle;
import nasirov.yv.data.animedia.AnimediaTitle;
import nasirov.yv.data.properties.GitHubResourceProps;
import nasirov.yv.data.properties.ResourcesNames;
import nasirov.yv.parser.WrappedObjectMapperI;
import nasirov.yv.service.AnimediaServiceI;
import nasirov.yv.service.GitHubResourcesServiceI;
import nasirov.yv.service.MALServiceI;
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

	private final GitHubResourcesServiceI githubResourcesService;

	private final AnimediaServiceI animediaService;

	private final MALServiceI malService;

	private final ResourcesNames resourcesNames;

	private final WrappedObjectMapperI wrappedObjectMapper;

	private final GitHubResourceProps githubResourceProps;

	@Override
	@Scheduled(cron = "${application.cron.resources-check-cron-expression}")
	public void checkAnimediaTitlesExistenceOnMal() {
		log.info("START CHECKING ANIMEDIA TITLES EXISTENCE ON MAL ...");
		Set<AnimediaTitle> animediaTitles = githubResourcesService.getResource(githubResourceProps.getAnimediaTitles(), AnimediaTitle.class);
		List<AnimediaTitle> notFoundOnMal = new LinkedList<>();
		for (AnimediaTitle animediaTitle : animediaTitles) {
			String titleOnMAL = animediaTitle.getTitleNameOnMAL();
			Integer titleIdOnMAL = animediaTitle.getTitleIdOnMal();
			if (!isTitleNotFoundOnMAL(animediaTitle)) {
				boolean titleExist = malService.isTitleExist(titleOnMAL, titleIdOnMAL);
				if (!titleExist) {
					log.error("NOT FOUND TITLE [{}] WITH ID [{}] ON MAL!", titleOnMAL, titleIdOnMAL);
					notFoundOnMal.add(AnimediaTitle.builder()
							.urlOnAnimedia(animediaTitle.getUrlOnAnimedia())
							.animeIdOnAnimedia(animediaTitle.getAnimeIdOnAnimedia())
							.dataListOnAnimedia(animediaTitle.getDataListOnAnimedia())
							.minOnAnimedia(animediaTitle.getMinOnAnimedia())
							.titleNameOnMAL(titleOnMAL)
							.titleIdOnMal(titleIdOnMAL)
							.build());
				}
			}
		}
		marshallToTempFolder(resourcesNames.getTempAnimediaTitlesNotFoundOnMal(), notFoundOnMal);
		log.info("END CHECKING ANIMEDIA TITLES EXISTENCE ON MAL.");
	}

	@Override
	@Scheduled(cron = "${application.cron.resources-check-cron-expression}")
	public void checkAnimediaTitlesOnAnimedia() {
		log.info("START CHECKING ANIMEDIA TITLES ON ANIMEDIA ...");
		Set<AnimediaSearchListTitle> animediaSearchList = animediaService.getAnimediaSearchList();
		String animediaTitlesResourceName = githubResourceProps.getAnimediaTitles();
		Set<AnimediaTitle> animediaTitles = githubResourcesService.getResource(animediaTitlesResourceName, AnimediaTitle.class);
		List<AnimediaTitle> missedAnimediaTitles = new LinkedList<>();
		for (AnimediaSearchListTitle titleSearchInfo : animediaSearchList) {
			List<AnimediaTitle> matchedAnimediaTitles = getMatchedAnimediaTitles(animediaTitles, titleSearchInfo);
			if (isAnnouncement(titleSearchInfo)) {
				if (matchedAnimediaTitles.isEmpty()) {
					log.error("ANNOUNCEMENT MUST BE PRESENT IN [{}] {}", animediaTitlesResourceName, titleSearchInfo);
					missedAnimediaTitles.add(buildTempAnnouncementAnimediaTitle(titleSearchInfo));
				}
			} else {
				List<String> dataLists = titleSearchInfo.getDataLists();
				for (String dataList : dataLists) {
					boolean titleIsNotPresentInAnimediaTitlesResources = matchedAnimediaTitles.stream()
							.noneMatch(x -> x.getDataListOnAnimedia()
									.equals(dataList));
					if (titleIsNotPresentInAnimediaTitlesResources) {
						log.error("TITLE IS NOT PRESENT IN [{}] {}/{}", animediaTitlesResourceName, titleSearchInfo.getUrl(), dataList);
						missedAnimediaTitles.add(buildTempAnimediaTitle(titleSearchInfo, dataList));
					}
				}
			}
		}
		marshallToTempFolder(resourcesNames.getTempMissedAnimediaTitles(), missedAnimediaTitles);
		log.info("END CHECKING ANIMEDIA TITLES ON ANIMEDIA.");
	}

	private AnimediaTitle buildTempAnimediaTitle(AnimediaSearchListTitle titleSearchInfo, String dataList) {
		return AnimediaTitle.builder()
				.urlOnAnimedia(titleSearchInfo.getUrl())
				.animeIdOnAnimedia(titleSearchInfo.getAnimeId())
				.dataListOnAnimedia(dataList)
				.build();
	}

	private AnimediaTitle buildTempAnnouncementAnimediaTitle(AnimediaSearchListTitle titleSearchInfo) {
		return AnimediaTitle.builder()
				.urlOnAnimedia(titleSearchInfo.getUrl())
				.dataListOnAnimedia(FIRST_DATA_LIST)
				.minOnAnimedia(FIRST_EPISODE)
				.build();
	}

	private List<AnimediaTitle> getMatchedAnimediaTitles(Set<AnimediaTitle> animediaTitles, AnimediaSearchListTitle titleSearchInfo) {
		return animediaTitles.stream()
				.filter(x -> titleSearchInfo.getUrl()
						.equals(UriUtils.decode(x.getUrlOnAnimedia(), UTF_8)))
				.collect(Collectors.toList());
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
