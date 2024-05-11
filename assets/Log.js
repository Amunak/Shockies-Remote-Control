export default class Log
{
	/**
	 * @param {HTMLElement} targetElement
	 * @param {URL} websocketUrl
	 */
	constructor(targetElement, websocketUrl) {
		this.targetElement = targetElement
		this.websocketUrl = websocketUrl
		this.createWebsocket(websocketUrl)
	}

	createWebsocket(websocketUrl) {
		this.websocket = new WebSocket(websocketUrl)
		this.websocket.onmessage = this.wsOnMessage.bind(this)
		this.websocket.onopen = this.wsOnOpen.bind(this)
		this.websocket.onclose = this.wsOnClose.bind(this)
		this.websocket.onerror = this.wsOnError.bind(this)
	}

	pushInfo(text, replace = false) {
		let message = `\n\n&lt;&lt;&lt; ${text} &gt;&gt;&gt;\n\n`

		if (replace) {
			this.targetElement.innerHTML = message
		} else {
			this.targetElement.innerHTML = message + this.targetElement.innerHTML
		}
	}

	wsOnMessage(event) {
		this.targetElement.innerHTML = event.data + this.targetElement.innerHTML
	}

	wsOnOpen() {
		this.pushInfo('Connection established.', true)
	}

	wsOnClose() {
		this.pushInfo('Connection closed. Retrying...')
		setTimeout(() => {
			this.createWebsocket(this.websocketUrl)
		}, 5000)
	}

	wsOnError() {
		this.pushInfo('Connection error. Retrying...')
		setTimeout(() => {
			this.createWebsocket(this.websocketUrl)
		}, 5000)
	}
}
