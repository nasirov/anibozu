package nasirov.yv.service.impl;

import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.data.animedia.TitleReference;
import nasirov.yv.data.mal.UserMALTitleInfo;
import nasirov.yv.repository.NotFoundAnimeOnAnimediaRepository;
import nasirov.yv.service.ReferencesServiceI;
import nasirov.yv.service.RepositoryCheckerServiceI;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RepositoryCheckerService implements RepositoryCheckerServiceI {

	private final ReferencesServiceI referencesService;

	private final NotFoundAnimeOnAnimediaRepository notFoundAnimeOnAnimediaRepository;

	@Override
	@Scheduled(cron = "${application.cron.resources-check-cron-expression}")
	public void checkNotFoundTitlesOnAnimedia() {
		log.info("START CHECKING NOT FOUND ANIME ON ANIMEDIA REPOSITORY ...");
		Set<TitleReference> allReferences = referencesService.getReferences();
		List<UserMALTitleInfo> notFoundAnimeOnAnimedia = notFoundAnimeOnAnimediaRepository.findAll();
		for (UserMALTitleInfo notFoundTitle : notFoundAnimeOnAnimedia) {
			if (allReferences.stream()
					.anyMatch(ref -> ref.getTitleNameOnMAL()
							.equals(notFoundTitle.getTitle()))) {
				log.info("{} HAS REMOVED FROM NotFoundAnimeOnAnimediaRepository", notFoundTitle.getTitle());
				notFoundAnimeOnAnimediaRepository.delete(notFoundTitle);
			}
		}
		log.info("END CHECKING NOT FOUND ANIME ON ANIMEDIA REPOSITORY.");
	}
}
