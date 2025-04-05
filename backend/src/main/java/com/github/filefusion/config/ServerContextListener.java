package com.github.filefusion.config;

import com.github.filefusion.util.ExecUtil;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import org.springframework.context.annotation.Configuration;

/**
 * ServerContextListener
 *
 * @author hackyo
 * @since 2022/4/1
 */
@Configuration
public class ServerContextListener implements ServletContextListener {

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        ExecUtil.close();
    }

}
