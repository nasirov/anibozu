package nasirov.yv.data.validator;

import static java.util.Objects.nonNull;

import java.util.Set;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import nasirov.yv.data.properties.FandubSupportProps;
import nasirov.yv.fandub.service.spring.boot.starter.constant.FandubSource;
import org.springframework.stereotype.Component;

/**
 * @author Nasirov Yuriy
 */
@Component
@RequiredArgsConstructor
public class FandubSourcesValidator implements ConstraintValidator<ValidFandubSources, Set<FandubSource>> {

	private final FandubSupportProps fanDubSupportProps;

	@Override
	public boolean isValid(Set<FandubSource> input, ConstraintValidatorContext context) {
		Set<FandubSource> disabledFandub = fanDubSupportProps.getDisabledFandub();
		return nonNull(input) && disabledFandub.stream().noneMatch(input::contains);
	}
}
