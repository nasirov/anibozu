package nasirov.yv.service.context;

import java.lang.reflect.Method;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.service.annotation.LoadResources;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
public class LoadResourcesContextListener implements ApplicationListener<ContextRefreshedEvent> {

	@Value("${service.resourcesLoader.enabled}")
	private boolean isResourcesLoaderEnabled;

	private ConfigurableListableBeanFactory beanFactory;

	@Autowired
	public LoadResourcesContextListener(ConfigurableListableBeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
		if (isResourcesLoaderEnabled) {
			ApplicationContext applicationContext = contextRefreshedEvent.getApplicationContext();
			String[] beanDefinitionNames = applicationContext.getBeanDefinitionNames();
			for (String name : beanDefinitionNames) {
				BeanDefinition beanDefinition = beanFactory.getBeanDefinition(name);
				String beanClassName = beanDefinition.getBeanClassName();
				if (beanClassName != null) {
					try {
						Class<?> originalClass = Class.forName(beanClassName);
						if (originalClass.isAnnotationPresent(LoadResources.class)) {
							Method[] methods = originalClass.getDeclaredMethods();
							for (Method method : methods) {
								if (method.isAnnotationPresent(LoadResources.class)) {
									Object currentBean = applicationContext.getBean(name);
									Method currentMethod = currentBean.getClass().getDeclaredMethod(method.getName(), method.getParameterTypes());
									if (!currentMethod.isAccessible()) {
										currentMethod.setAccessible(true);
									}
									currentMethod.invoke(currentBean);
								}
							}
						}
					} catch (Exception e) {
						log.error("EXCEPTION WHILE HANDLE LOADRESOURCES ANNOTATION BEAN ", e);
					}
				}
			}
		}

	}
}
