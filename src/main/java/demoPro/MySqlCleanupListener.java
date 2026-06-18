package demoPro;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.mysql.cj.jdbc.AbandonedConnectionCleanupThread;

public class MySqlCleanupListener implements ServletContextListener {

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		// No startup work required.
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		try {
			AbandonedConnectionCleanupThread.checkedShutdown();
		} catch (Exception e) {
			// Ignore cleanup failures during app shutdown.
		}
	}
}
