package nasirov.yv.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.resource.ResourceUrlProvider;

/**
 * Created by nasirov.yv
 */
@ControllerAdvice
@RequiredArgsConstructor
public class ResourceUrlAdvice {

	private final ResourceUrlProvider resourceUrlProvider;

	@ModelAttribute("versionedUrls")
	public ResourceUrlProvider getResourceUrlProvider() {
		return this.resourceUrlProvider;
	}
}
