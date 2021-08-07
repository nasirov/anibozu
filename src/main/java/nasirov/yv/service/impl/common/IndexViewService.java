package nasirov.yv.service.impl.common;

import static nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource.ANIDUB;
import static nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource.ANILIBRIA;
import static nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource.ANIMEDIA;
import static nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource.ANIMEPIK;
import static nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource.JAMCLUB;
import static nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource.JISEDAI;
import static nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource.JUTSU;
import static nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource.NINEANIME;
import static nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource.SHIZAPROJECT;
import static nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource.SOVETROMANTICA;

import lombok.extern.slf4j.Slf4j;
import nasirov.yv.data.front.UserInputDto;
import nasirov.yv.service.IndexViewServiceI;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import reactor.core.publisher.Mono;

/**
 * @author Nasirov Yuriy
 */
@Slf4j
@Service
public class IndexViewService implements IndexViewServiceI {

	@Override
	public Mono<String> getIndexView(Model model) {
		return Mono.just(model)
				.doOnNext(this::enrichModel)
				.then(Mono.just("index"))
				.doOnSuccess(x -> log.info("Got [{}] view.", x));
	}

	private void enrichModel(Model model) {
		model.addAttribute("userInputDto", new UserInputDto());
		model.addAttribute(ANIMEDIA.getName(), ANIMEDIA);
		model.addAttribute(ANIDUB.getName(), ANIDUB);
		model.addAttribute(JISEDAI.getName(), JISEDAI);
		model.addAttribute(ANIMEPIK.getName(), ANIMEPIK);
		model.addAttribute(ANILIBRIA.getName(), ANILIBRIA);
		model.addAttribute(JUTSU.getName(), JUTSU);
		model.addAttribute(NINEANIME.getName(), NINEANIME);
		model.addAttribute(SOVETROMANTICA.getName(), SOVETROMANTICA);
		model.addAttribute(SHIZAPROJECT.getName(), SHIZAPROJECT);
		model.addAttribute(JAMCLUB.getName(), JAMCLUB);
	}
}
