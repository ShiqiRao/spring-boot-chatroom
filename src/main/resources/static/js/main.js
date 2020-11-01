'use strict';

var usernamePage = document.querySelector('#username-page');
var chatPage = document.querySelector('#chat-page');
var usernameForm = document.querySelector('#usernameForm');
var messageForm = document.querySelector('#messageForm');
var messageInput = document.querySelector('#message');
var messageArea = document.querySelector('#messageArea');
var connectingElement = document.querySelector('.connecting');
var sendToElement = document.querySelector('.send-to');
var receiverElement = document.querySelector('.receiver');

var stompClient = null;
var username = null;
var password = null;
var receiver = null;

/*登陆函数*/
function connect(event) {
    username = document.querySelector('#name').value.trim();
    password = document.querySelector('#password').value.trim();

    if(username&&password) {
        loginWithRetry(username,password);
    }
    event.preventDefault();
}
/*登陆，失败后自动注册*/
function loginWithRetry(username,password){
    //发送登陆请求
    var xhr = new XMLHttpRequest();
    xhr.open('POST', '/login');
    xhr.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
    xhr.send(`username=${username}&password=${password}`);
    xhr.onload = () => {
        //处理返回结果
        var sessionResponse = JSON.parse(xhr.responseText);
        if(sessionResponse&&sessionResponse.code==0){
            //code为0，成功。建立STOMP连接。
            stompConnect();
        }else{
            //自动注册、登陆并简历STOMP连接
            registerThenLogin(username,password);
        }
    }
}

/*注册后自动登录*/
function registerThenLogin(username,password){
    //发送注册请求
    var registerXhr = new XMLHttpRequest();
    registerXhr.open('POST', '/session');
    registerXhr.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
    registerXhr.send(`username=${username}&password=${password}`);
    registerXhr.onload = () => {
        //发送登陆请求
        var sessionResponse = JSON.parse(registerXhr.responseText);
        if(sessionResponse&&sessionResponse.code==0){
                var xhr = new XMLHttpRequest();
                xhr.open('POST', '/login');
                xhr.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
                xhr.send(`username=${username}&password=${password}`);
                xhr.onload = () => {
                    var sessionResponse = JSON.parse(xhr.responseText);
                    if(sessionResponse&&sessionResponse.code==0){
                        //code为0，成功。建立STOMP连接。
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

/*建立STOMP连接*/
function stompConnect(){
    usernamePage.classList.add('hidden');
    chatPage.classList.remove('hidden');
    var socket = new SockJS('./ws');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, onConnected, onError);
}

/*STOMP连接成功*/
function onConnected() {
    //订阅群聊主题
    stompClient.subscribe('/topic/public', onMessageReceived);
    //订阅私聊聊主题
    stompClient.subscribe(`/user/${username}/notification`, onMessageReceived);
    //订阅获取历史记录的主题
    stompClient.subscribe('/app/chat.lastTenMessage', onMessageReceived);
    //发送加入群聊的消息
    stompClient.send("/app/chat.addUser",
        {},
        JSON.stringify({sender: username, type: 'JOIN'})
    )
    connectingElement.classList.add('hidden');
}

/*STOMP连接失败*/
function onError(error) {
    connectingElement.textContent = 'Could not connect to WebSocket server. Please refresh this page to try again!';
    connectingElement.style.color = 'red';
}

/*向服务端发送聊天信息*/
function sendMessage(event) {
    var messageContent = messageInput.value.trim();
    if(messageContent && stompClient) {
        var chatMessage = {
            receiver:receiver,
            sender: username,
            content: messageInput.value,
            type: 'CHAT'
        };
        stompClient.send("/app/chat.sendMessage", {}, JSON.stringify(chatMessage));
        messageInput.value = '';
    }
    event.preventDefault();
}

/*处理来自服务端的消息*/
function onMessageReceived(payload) {
    var body = JSON.parse(payload.body);
    //适配不同类型的返回结果。如果非数组则转为数组
    var message = body instanceof Array? body:[body];
    for(var i in message){
        var messageElement = document.createElement('li');
        if(message[i].type === 'JOIN') {
            //处理加入群聊信息
            messageElement.classList.add('event-message');
            message[i].content = message[i].sender + ' joined!';
            //处理离开群聊信息
        } else if (message[i].type === 'LEAVE') {
            messageElement.classList.add('event-message');
            message[i].content = message[i].sender + ' left!';
        } else {
            //处理聊天信息
            messageElement.classList.add('chat-message');
            var usernameElement = document.createElement('span');
            var usernameText = document.createTextNode((message[i].sender + ' :'));
            usernameElement.appendChild(usernameText);
            messageElement.appendChild(usernameElement);
        }
        var textElement = document.createElement('p');
        var messageText = document.createTextNode(message[i].content);
        textElement.appendChild(messageText);
        messageElement.setAttribute("sender",message[i].sender);
        messageElement.appendChild(textElement);
        messageArea.appendChild(messageElement);
        messageArea.scrollTop = messageArea.scrollHeight;

        if(message[i].sender!=username){
            messageElement.addEventListener('click', function(e){
                selectOrCancelReceiver(e.target.parentElement.getAttribute("sender"));
            }, true);
        }
    }
}

function selectOrCancelReceiver(o){
    if(receiver == o){
        cancelReceiver();
    }else {
        receiver = o;
        sendToElement.classList.remove('hidden');
    }
    receiverElement.innerHTML=receiver;
}

function cancelReceiver(){
    receiver = null;
    sendToElement.classList.add('hidden');
}


sendToElement.addEventListener('click',cancelReceiver,true);
usernameForm.addEventListener('submit', connect, true);
messageForm.addEventListener('submit', sendMessage, true);
