package com.example.chat;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.security.config.annotation.web.messaging.MessageSecurityMetadataSourceRegistry;
import org.springframework.security.config.annotation.web.socket.AbstractSecurityWebSocketMessageBrokerConfigurer;

@TestConfiguration
public class SocketSecurityTestConfig extends AbstractSecurityWebSocketMessageBrokerConfigurer {

    @Override
    protected void configureInbound(MessageSecurityMetadataSourceRegistry messages) {

    }

    @Override
    protected boolean sameOriginDisabled() {
        return true;
    }
}