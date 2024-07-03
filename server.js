const SocketServer = require('websocket').server
const http = require('http')

const server = http.createServer((req, res) => {})

server.listen(6557, () => {
    console.log("Listening on port 6557...")
})

wsServer = new SocketServer({httpServer : server})

const connections = []

wsServer.on('request', (req) => {
    const connection = req.accept()
    console.log('New connection/ Listener changed.')
    connections.push(connection)

    connection.on('message', (message) => {
        const clientMessage = JSON.parse(message.utf8Data);
        let responseMessage = { name: 'Server', message: '' };
    
        switch (clientMessage.action) {
            case 'enterRoom':
                const name = clientMessage.data;
                console.log(`Entering chat page...`);
                responseMessage.message = 'Chat page entered by: ${name}}';
                break;
            case 'fetchRandomVideos':
                console.log(`Fetching random videos...`);
                responseMessage.message = 'Fetched random videos';
                break;
            case 'subscribe':
                console.log(`User subscribed.`);
                responseMessage.message = 'User subscribed';
                break;
            case 'like':
                console.log(`Video liked.`);
                responseMessage.message = 'Video liked';
                break;
            case 'dislike':
                console.log(`Video disliked.`);
                responseMessage.message = 'Video disliked';
                break;
            case 'comment':
                console.log(`Commenting on video...`);
                responseMessage.message = `Commented on video`;
                break;
            case 'fetchChannelInfo':
                console.log(`Fetching channel info...`);
                responseMessage.message = 'Fetched channel info';
                break;
            case 'updateLikeCount':
                console.log(`Fetching number of likes...`);
                responseMessage.message = 'Fetched number of likes';
                break;
            case 'displayVideos':
                const videos = clientMessage.data;
                console.log(`Displaying videos: ${videos}.`);
                responseMessage.message = `Displayed videos: ${videos}`;
                break;
            case 'loadMainPage':
                console.log(`Loading main page...`);
                responseMessage.message = 'Main page loaded';
                break;
            case 'loadChatPage':
                console.log(`Loading chat page...`);
                responseMessage.message = 'Chat page loaded';
                break;
            case 'searchStarted':
                const searchQuery = clientMessage.data;
                console.log(`Searching for: ${searchQuery}...`);
                responseMessage.message = `Searched for video: ${searchQuery}`;
                break;
            case 'loadVideos':
                console.log('Videos are loading...');
                responseMessage.message = 'Videos loaded';
                break;
            case 'videoTitleClicked':
                const videoId = clientMessage.data;
                console.log(`Video title clicked: ${videoId}.`);
                responseMessage.message = `Video title clicked: ${videoId}`;
                break;
            case 'fetchVideoInfo':
                const videoIdInfo = clientMessage.data;
                console.log(`Loading information for video: ${videoIdInfo}...`);
                responseMessage.message = `Loaded info for video: ${videoIdInfo}`;
                break;
            case 'fetchComments':
                const videoIdComments = clientMessage.data;
                console.log(`Fetching comments for video: ${videoIdComments}...`);
                responseMessage.message = 'Comments fetched';
                break;
            case 'openChannel':
                const channelId = clientMessage.data;
                console.log(`Opening channel: ${channelId}...`);
                responseMessage.message = `Opened channel: ${channelId}`;
                break;
            default:
            if (clientMessage.image) {
                responseMessage.message = 'Nice image!';
            } else if (clientMessage.message && clientMessage.message.toLowerCase() === 'hello') {
                responseMessage.message = 'Hello!';
            } else {
                responseMessage.message = 'I received your message: ' + clientMessage.message;
            }
            break;
        }
    
        connection.sendUTF(JSON.stringify(responseMessage));
    })
    
    connection.on('close', (resCode, des) => {
        console.log('Connection closed.')
        connections.slice(connections.indexOf(connection), 1)   
    })
})