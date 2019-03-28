package nasirov.yv.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.resource.ResourceUrlProvider;

/**
 * Created by nasirov.yv
 */
@ControllerAdvice
public class ResourceUrlAdvice {

	private ResourceUrlProvider resourceUrlProvider;

	@Autowired
	public ResourceUrlAdvice(ResourceUrlProvider resourceUrlProvider) {
		this.resourceUrlProvider = resourceUrlProvider;
	}

	@ModelAttribute("versionedUrls")
	public ResourceUrlProvider getResourceUrlProvider() {
		return this.resourceUrlProvider;
	}
}
