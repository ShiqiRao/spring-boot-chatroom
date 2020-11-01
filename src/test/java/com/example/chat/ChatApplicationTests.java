package com.example.chat;

import com.example.chat.entity.TChatMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.jupiter.api.Assertions.assertEquals;


@SpringBootTest(classes = {ChatTestApplication.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(SocketSecurityTestConfig.class)
class ChatApplicationTests {

    @LocalServerPort
    private Integer port;

    @Autowired
    private ObjectMapper objectMapper;

    private WebSocketStompClient webSocketStompClient;

    @BeforeEach
    public void setup() {
        this.webSocketStompClient = new WebSocketStompClient(new SockJsClient(
                Collections.singletonList(new WebSocketTransport(new StandardWebSocketClient()))));
        this.webSocketStompClient.setMessageConverter(new MappingJackson2MessageConverter());
    }

    @Test
    public void subscribePublic_thenSend() throws InterruptedException, ExecutionException, TimeoutException {
        BlockingQueue<TChatMessage> blockingQueue = new ArrayBlockingQueue(1);

        StompSession session = webSocketStompClient
                .connect(getWsPath(), new StompSessionHandlerAdapter() {
                })
                .get(1, SECONDS);
        session.subscribe("/topic/public", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return TChatMessage.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                blockingQueue.add((TChatMessage) payload);
            }
        });
        session.send("/app/chat.sendMessage", new TChatMessage()
                .setContent("hello")
                .setSender("someone")
                .setType(TChatMessage.MessageType.CHAT));

        TChatMessage ret = blockingQueue.poll(1, SECONDS);
        assertEquals("hello", ret.getContent());
    }

    @Test
    public void subscribePrivate_thenSend() throws InterruptedException, ExecutionException, TimeoutException {
        BlockingQueue<TChatMessage> blockingQueue = new ArrayBlockingQueue(1);
        StompSession session = webSocketStompClient
                .connect(getWsPath(), new StompSessionHandlerAdapter() {
                })
                .get(1, SECONDS);
        session.subscribe(String.format("/user/%s/notification", "someone"), new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return TChatMessage.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                blockingQueue.add((TChatMessage) payload);
            }
        });
        session.send("/app/chat.sendMessage", new TChatMessage()
                .setContent("hello")
                .setSender("someone")
                .setReceiver("anyone")
                .setType(TChatMessage.MessageType.CHAT));

        TChatMessage ret = blockingQueue.poll(1, SECONDS);
        assertEquals("hello", ret.getContent());
    }

    private String getWsPath() {
        return String.format("ws://localhost:%d/ws", port);
    }

}
