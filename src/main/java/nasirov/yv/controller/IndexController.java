package nasirov.yv.controller;


import static nasirov.yv.fandub.dto.constant.FanDubSource.ANIDUB;
import static nasirov.yv.fandub.dto.constant.FanDubSource.ANILIBRIA;
import static nasirov.yv.fandub.dto.constant.FanDubSource.ANIMEDIA;
import static nasirov.yv.fandub.dto.constant.FanDubSource.ANIMEPIK;
import static nasirov.yv.fandub.dto.constant.FanDubSource.JISEDAI;

import nasirov.yv.data.mal.MalUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created by nasirov.yv
 */
@Controller
@RequestMapping(value = {"/", "/index"})
public class IndexController {

	@GetMapping
	public String index(Model model) {
		model.addAttribute("malUser", new MalUser());
		model.addAttribute(ANIMEDIA.getName(), ANIMEDIA);
		model.addAttribute(ANIDUB.getName(), ANIDUB);
		model.addAttribute(JISEDAI.getName(), JISEDAI);
		model.addAttribute(ANIMEPIK.getName(), ANIMEPIK);
		model.addAttribute(ANILIBRIA.getName(), ANILIBRIA);
		return "index";
	}
}
