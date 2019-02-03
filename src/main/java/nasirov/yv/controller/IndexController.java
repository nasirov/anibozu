package nasirov.yv.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created by nasirov.yv
 */
@Controller
@RequestMapping(value = {"/", "/index"})
public class IndexController {
	@GetMapping
	public String index() {
		return "index.html";
	}
}
