package nasirov.yv.service.context;

import java.lang.reflect.Method;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.service.annotation.PrintApplicationLogo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

/**
 * Created by nasirov.yv
 */
@Component
@Slf4j
public class LogoPrinterContextListener implements ApplicationListener<ContextRefreshedEvent> {

	private ConfigurableListableBeanFactory factory;

	@Autowired
	public LogoPrinterContextListener(ConfigurableListableBeanFactory factory) {
		this.factory = factory;
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
		ApplicationContext applicationContext = contextRefreshedEvent.getApplicationContext();
		String[] beanDefinitionNames = applicationContext.getBeanDefinitionNames();
		for (String bean : beanDefinitionNames) {
			BeanDefinition beanDefinition = factory.getBeanDefinition(bean);
			String beanClassName = beanDefinition.getBeanClassName();
			if (beanClassName != null) {
				try {
					Class<?> beanClass = Class.forName(beanClassName);
					if (beanClass.isAnnotationPresent(PrintApplicationLogo.class)) {
						Method[] methods = beanClass.getMethods();
						for (Method method : methods) {
							if (method.isAnnotationPresent(PrintApplicationLogo.class)) {
								Object currentBean = applicationContext.getBean(bean);
								Method currentMethod = currentBean.getClass().getMethod(method.getName(), method.getParameterTypes());
								currentMethod.invoke(currentBean);
							}
						}
					}
				} catch (Exception e) {
					log.error("Exception while printing application logo", e);
				}
			}
		}
	}
}
