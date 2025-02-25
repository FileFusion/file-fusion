package com.github.filefusion.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.undertow.UndertowServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

/**
 * WebServerFactory
 *
 * @author hackyo
 * @since 2022/4/1
 */
@Configuration
public class WebServerFactory implements WebServerFactoryCustomizer<UndertowServletWebServerFactory> {

    @Value("${server.ssl.enabled}")
    private Boolean serverSslEnabled;
    @Value("${server.http-port}")
    private Integer serverHttpPort;
    @Value("${server.ssl.port}")
    private Integer serverSslPort;
    @Value("${server.ssl.forced}")
    private Boolean serverSslForced;

    @Override
    public void customize(UndertowServletWebServerFactory server) {
        if (serverSslEnabled) {
            server.setPort(serverSslPort);
            server.addBuilderCustomizers(builder -> builder.addHttpListener(serverHttpPort, "0.0.0.0"));
        } else {
            server.setPort(serverHttpPort);
        }
    }

    public void setSslRedirect(HttpSecurity http) throws Exception {
        if (serverSslEnabled && serverSslForced) {
            http.portMapper(portMapper -> portMapper.http(serverHttpPort).mapsTo(serverSslPort))
                    .requiresChannel(requiresChannel -> requiresChannel.anyRequest().requiresSecure());
        }
    }

}
