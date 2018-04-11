package com.meiya.alarm.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * 类功能描述：以静态变量保存Spring ApplicationContext, 可在任何代码任何地方任何时候中取出ApplicaitonContext.
 * @author 1.chlingm
 *
 */
public class SpringContextHolder implements ApplicationContextAware, DisposableBean {

	/** The application context. */
	private static ApplicationContext applicationContext = null;

	/** The logger. */
	private static Log logger = LogFactory.getLog(SpringContextHolder.class);

	/**
	 * 实现ApplicationContextAware接口, 注入Context到静态变量中.
	 * 
	 * @param applicationContext
	 *            the new application context
	 */
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) {
		logger.debug("注入ApplicationContext到SpringContextHolder:" + applicationContext);

		if (SpringContextHolder.applicationContext != null) {
			logger.warn("SpringContextHolder中的ApplicationContext被覆盖,原有Context为:"
					+ SpringContextHolder.applicationContext);
		}

		SpringContextHolder.applicationContext = applicationContext; // NOSONAR
	}

	/**
	 * 实现DisposableBean接口,在Context关闭时清理静态变量.
	 * 
	 * @throws Exception
	 *             the exception
	 */
	@Override
	public void destroy() throws Exception {
		SpringContextHolder.cleanApplicationContext();
	}

	/**
	 * 取得存储在静态变量中的ApplicationContext.
	 * 
	 * @return the application context
	 */
	public static ApplicationContext getApplicationContext() {
		checkApplicationContext();
		return applicationContext;
	}

	/**
	 * 从静态变量ApplicationContext中取得Bean, 自动转型为所赋值对象的类型.
	 * 
	 * @param <T>
	 *            the generic type
	 * @param name
	 *            the name
	 * @return the bean
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getBean(String name) {
		checkApplicationContext();
		return (T) applicationContext.getBean(name);
	}

	/**
	 * 从静态变量ApplicationContext中取得Bean, 自动转型为所赋值对象的类型. 如果有多个Bean符合Class, 取出第一个.
	 * 
	 * @param <T>
	 *            the generic type
	 * @param name
	 *            the name
	 * @param requiredType
	 *            the required type
	 * @return the bean
	 */
	@SuppressWarnings("cast")
	public static <T> T getBean(String name, Class<T> requiredType) {
		checkApplicationContext();
		return (T) applicationContext.getBean(name, requiredType);
	}

	/**
	 * 清除applicationContext静态变量.
	 */
	public static void cleanApplicationContext() {
		logger.debug("清除SpringContextHolder中的ApplicationContext:" + applicationContext);
		applicationContext = null;
	}

	/**
	 * 检查ApplicationContext不为空.
	 */
	private static void checkApplicationContext() {
		if (applicationContext == null) {
			throw new IllegalStateException("applicaitonContext未注入,请在applicationContext.xml中定义SpringContextHolder");
		}
	}
}
