export default class Log
{
	lastWasInfo = false
	retryCount = 0

	/**
	 * @param {HTMLElement} targetElement
	 * @param {URL} websocketUrl
	 */
	constructor(targetElement, websocketUrl) {
		this.targetElement = targetElement
		this.websocketUrl = websocketUrl
		this.connect()
	}

	connect() {
		if (this.retryCount > 5) {
			this.pushInfo('Too many retries. Giving up.')
			return
		}

		this.retryCount++
		this.websocket = new WebSocket(this.websocketUrl)
		this.websocket.onmessage = this.wsOnMessage.bind(this)
		this.websocket.onopen = this.wsOnOpen.bind(this)
		this.websocket.onclose = this.wsOnClose.bind(this)
		this.websocket.onerror = this.wsOnError.bind(this)
	}

	pushInfo(text, replace = false) {
		let message = `&lt;&lt;&lt; ${text} &gt;&gt;&gt;\n`

		if (!this.lastWasInfo) {
			message = `\n\n${message}\n`
		}

		if (replace) {
			this.targetElement.innerHTML = message
		} else {
			this.targetElement.innerHTML = message + this.targetElement.innerHTML
		}

		this.lastWasInfo = true
	}

	wsOnMessage(event) {
		this.targetElement.innerHTML = event.data + this.targetElement.innerHTML
		this.lastWasInfo = false
	}

	wsOnOpen() {
		this.retryCount = 0
		this.pushInfo('Connection established.', true)
	}

	wsOnClose() {
		this.pushInfo('Disconnected. Reconnecting...')
		setTimeout(() => {
			this.connect()
		}, 5000)
	}

	wsOnError() {
		this.pushInfo('Connection error.')
	}
}
