package nasirov.yv.aop;

import nasirov.yv.response.HttpResponse;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * Created by nasirov.yv
 */
@Component
@Aspect
public class CheckHttpResponseAspect {
	@Around(value = "@annotation(CheckHttpResponse)")
	public Object checkHttpResponse(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
		Object proceedObject = null;
		Object[] methodArgs = proceedingJoinPoint.getArgs();
		if (methodArgs != null && methodArgs.length >= 1) {
			Object httpResponse = methodArgs[0];
			if (httpResponse instanceof HttpResponse) {
				if (((HttpResponse) httpResponse).getContent() == null) {
					throw new NullPointerException("Content is null!");
				} else if (((HttpResponse) httpResponse).getContent().equals("")) {
					throw new NullPointerException("Content is empty!");
				} else {
					proceedObject = proceedingJoinPoint.proceed();
				}
			} else {
				throw new NullPointerException("HttpResponse is null!");
			}
		} else {
			throw new NullPointerException("HttpResponse is null!");
		}
		return proceedObject;
	}
}
