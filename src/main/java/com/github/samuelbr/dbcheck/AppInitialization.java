package com.github.samuelbr.dbcheck;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class AppInitialization implements ServletContextListener {

	public void contextInitialized(ServletContextEvent sce) {
		ApplicationContext.register(sce.getServletContext());
	}

	public void contextDestroyed(ServletContextEvent sce) {
		ApplicationContext.unregister(sce.getServletContext());
	}

}
