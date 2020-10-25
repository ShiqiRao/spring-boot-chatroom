'use strict';

var usernamePage = document.querySelector('#username-page');
var chatPage = document.querySelector('#chat-page');
var usernameForm = document.querySelector('#usernameForm');
var messageForm = document.querySelector('#messageForm');
var messageInput = document.querySelector('#message');
var messageArea = document.querySelector('#messageArea');
var connectingElement = document.querySelector('.connecting');

var stompClient = null;
var username = null;
var password = null;

function connect(event) {
    username = document.querySelector('#name').value.trim();
    password = document.querySelector('#password').value.trim();

    if(username&&password) {
        loginWithRetry(username,password);
    }
    event.preventDefault();
}

function loginWithRetry(username,password){
    var xhr = new XMLHttpRequest();
    xhr.open('POST', '/login');
    xhr.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
    xhr.send(`username=${username}&password=${password}`);
    xhr.onload = () => {
        var sessionResponse = JSON.parse(xhr.responseText);
        if(sessionResponse&&sessionResponse.code==0){
            stompConnect();
        }else{
            registerThenLogin(username,password);
        }
    }
}

function registerThenLogin(username,password){
    var registerXhr = new XMLHttpRequest();
    registerXhr.open('POST', '/session');
    registerXhr.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
    registerXhr.send(`username=${username}&password=${password}`);
    registerXhr.onload = () => {
        var sessionResponse = JSON.parse(registerXhr.responseText);
        if(sessionResponse&&sessionResponse.code==0){
                var xhr = new XMLHttpRequest();
                xhr.open('POST', '/login');
                xhr.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
                xhr.send(`username=${username}&password=${password}`);
                xhr.onload = () => {
                    var sessionResponse = JSON.parse(xhr.responseText);
                    if(sessionResponse&&sessionResponse.code==0){
                        stompConnect();
                    }else{
                        alert(sessionResponse.message);
                    }
                }
        }else{
            alert(sessionResponse.message);
        }
    }
}

function stompConnect(){
    usernamePage.classList.add('hidden');
    chatPage.classList.remove('hidden');
    var socket = new SockJS('./ws');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, onConnected, onError);
}

function onConnected() {
    // Subscribe to the Public Topic
    stompClient.subscribe('/topic/public', onMessageReceived);

    stompClient.subscribe('/app/chat.lastTenMessage', onMessageReceived);


    // Tell your username to the server
    stompClient.send("/app/chat.addUser",
        {},
        JSON.stringify({sender: username, type: 'JOIN'})
    )

    connectingElement.classList.add('hidden');
}


function onError(error) {
    connectingElement.textContent = 'Could not connect to WebSocket server. Please refresh this page to try again!';
    connectingElement.style.color = 'red';
}


function sendMessage(event) {
    var messageContent = messageInput.value.trim();

    if(messageContent && stompClient) {
        var chatMessage = {
            sender: username,
            content: messageInput.value,
            type: 'CHAT'
        };
        stompClient.send("/app/chat.sendMessage", {}, JSON.stringify(chatMessage));
        messageInput.value = '';
    }
    event.preventDefault();
}


function onMessageReceived(payload) {
//    var message = JSON.parse(payload.body);
    var body = JSON.parse(payload.body);
    var message = body instanceof Array? body:[body];
    for(var i in message){
        var messageElement = document.createElement('li');
        if(message[i].type === 'JOIN') {
            messageElement.classList.add('event-message');
            message[i].content = message[i].sender + ' joined!';
        } else if (message[i].type === 'LEAVE') {
            messageElement.classList.add('event-message');
            message[i].content = message[i].sender + ' left!';
        } else {
            messageElement.classList.add('chat-message');
            var usernameElement = document.createElement('span');
            var usernameText = document.createTextNode((message[i].sender + ' :'));
            usernameElement.appendChild(usernameText);
            messageElement.appendChild(usernameElement);
        }

        var textElement = document.createElement('p');
        var messageText = document.createTextNode(message[i].content);
        textElement.appendChild(messageText);
        messageElement.appendChild(textElement);
        messageArea.appendChild(messageElement);
        messageArea.scrollTop = messageArea.scrollHeight;
    }
}



usernameForm.addEventListener('submit', connect, true)
messageForm.addEventListener('submit', sendMessage, true)
