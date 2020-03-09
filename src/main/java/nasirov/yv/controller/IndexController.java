package nasirov.yv.controller;

import static nasirov.yv.data.constants.FanDubSource.ANIMEDIA;

import nasirov.yv.data.mal.MALUser;
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
		model.addAttribute("malUser", new MALUser());
		model.addAttribute("animedia", ANIMEDIA);
		return "index";
	}
}
