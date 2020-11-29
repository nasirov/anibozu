package nasirov.yv.data.validator;

import static java.util.Objects.nonNull;

import java.util.Set;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import nasirov.yv.data.properties.FanDubSupportProps;
import nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource;
import org.springframework.stereotype.Component;

/**
 * @author Nasirov Yuriy
 */
@Component
@RequiredArgsConstructor
public class FanDubSourcesValidator implements ConstraintValidator<ValidFanDubSources, Set<FanDubSource>> {

	private final FanDubSupportProps fanDubSupportProps;

	@Override
	public boolean isValid(Set<FanDubSource> input, ConstraintValidatorContext context) {
		Set<FanDubSource> disabledFandub = fanDubSupportProps.getDisabledFandub();
		return nonNull(input) && disabledFandub.stream()
				.noneMatch(input::contains);
	}
}
