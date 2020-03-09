package nasirov.yv.data.validator;

import static java.util.Objects.nonNull;

import java.util.Set;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import nasirov.yv.data.constants.FanDubSource;
import nasirov.yv.data.properties.FanDubProps;
import org.springframework.stereotype.Component;

/**
 * Created by nasirov.yv
 */
@Component
@RequiredArgsConstructor
public class FanDubSourcesValidator implements ConstraintValidator<ValidFanDubSources, Set<FanDubSource>> {

	private final FanDubProps fanDubProps;

	@Override
	public boolean isValid(Set<FanDubSource> input, ConstraintValidatorContext context) {
		Set<FanDubSource> disabled = fanDubProps.getDisabled();
		return nonNull(input) && disabled.stream()
				.noneMatch(input::contains);
	}
}
