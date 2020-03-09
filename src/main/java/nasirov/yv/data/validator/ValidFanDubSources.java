package nasirov.yv.data.validator;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

/**
 * Created by nasirov.yv
 */
@Target({FIELD})
@Retention(RUNTIME)
@Constraint(validatedBy = {FanDubSourcesValidator.class})
public @interface ValidFanDubSources {

	String message() default "Some FanDub Source is disabled now!";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}


