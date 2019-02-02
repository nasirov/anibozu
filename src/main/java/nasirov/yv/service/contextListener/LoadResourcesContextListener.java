package nasirov.yv.service.contextListener;

import lombok.extern.slf4j.Slf4j;
import nasirov.yv.service.annotation.LoadResources;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * Created by Хикка on 23.01.2019.
 */
@Component
@Slf4j
public class LoadResourcesContextListener implements ApplicationListener<ContextRefreshedEvent> {
	private ConfigurableListableBeanFactory beanFactory;
	
	@Autowired
	public LoadResourcesContextListener(ConfigurableListableBeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}
	
	@Override
	public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
		ApplicationContext applicationContext = contextRefreshedEvent.getApplicationContext();
		String[] beanDefinitionNames = applicationContext.getBeanDefinitionNames();
		for (String name : beanDefinitionNames) {
			BeanDefinition beanDefinition = beanFactory.getBeanDefinition(name);
			String beanClassName = beanDefinition.getBeanClassName();
			if (beanClassName != null) {
				try {
					Class<?> originalClass = Class.forName(beanClassName);
					if (originalClass.isAnnotationPresent(LoadResources.class)) {
						Method[] methods = originalClass.getMethods();
						for (Method method : methods) {
							if (method.isAnnotationPresent(LoadResources.class)) {
								Object currentBean = applicationContext.getBean(name);
								Method currentMethod = currentBean.getClass().getMethod(method.getName(), method.getParameterTypes());
								currentMethod.invoke(currentBean);
							}
						}
					}
				} catch (Exception e) {
					log.error("Exception while handle LoadResources annotation bean ", e);
				}
			}
		}
	}
}
