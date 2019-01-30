package nasirov.yv.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created by Хикка on 21.01.2019.
 */
@Controller
@RequestMapping(value = {"/index", "/"})
public class IndexController {
	@GetMapping
	public String index() {
		return "index.html";
	}
}
