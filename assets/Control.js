export default class Control {
	retryCount = 0

	/**
	 * @param {HTMLFormElement} bindForm
	 * @param {URL} websocketUrl
	 */
	constructor(bindForm, websocketUrl) {
		this.bindForm = bindForm
		this.websocketUrl = websocketUrl
		this.connect()

		bindForm.querySelectorAll('button#shock').forEach((button) => button.addEventListener('mousedown', this.trigger.bind(this, "S", this.bindForm.querySelector('input#shockIntensity').value)))
		bindForm.querySelectorAll('button#vibrate').forEach((button) => button.addEventListener('mousedown', this.trigger.bind(this, "V", this.bindForm.querySelector('input#vibrateIntensity').value)))
		bindForm.querySelectorAll('button#beep').forEach((button) => button.addEventListener('mousedown', this.trigger.bind(this, "B")))
		bindForm.querySelectorAll('button#light').forEach((button) => button.addEventListener('mousedown', this.trigger.bind(this, "L")))
		bindForm.querySelectorAll('button#stop').forEach((button) => button.addEventListener('mousedown', this.trigger.bind(this, "X")))

		bindForm.querySelectorAll('button').forEach((button) => button.addEventListener('mouseup', this.trigger.bind(this, "R")))
	}

	trigger(action, argument) {
		const args = []
		args.push(action)

		// device id
		if (action !== 'X' && action !== 'R') {
			args.push('0')
		}

		if (typeof argument === 'string' && argument.length > 0) {
			args.push(argument)
		}

		let accessKey = this.bindForm.querySelector('input#accessKey').value
		if (accessKey) {
			args.push(accessKey)
		}

		this.websocket.send(args.join(' '))
	}

	logSelf(message) {
		console.log.apply(console, [`[${this.constructor.name}] ${message}`])
	}

	connect() {
		if (this.retryCount > 5) {
			this.logSelf('Too many retries. Giving up.')
			return
		}

		this.retryCount++
		this.websocket = new WebSocket(this.websocketUrl)
		this.websocket.onmessage = this.logSelf.bind(this, 'Message: ')
		this.websocket.onopen = this.wsOnOpen.bind(this)
		this.websocket.onclose = this.wsOnClose.bind(this)
		this.websocket.onerror = this.wsOnError.bind(this)
	}

	wsOnOpen() {
		this.retryCount = 0
		this.logSelf('Connection established.')
	}

	wsOnClose() {
		this.logSelf('Connection closed. Retrying...')
		setTimeout(() => {
			this.connect()
		}, 5000)
	}

	wsOnError() {
		this.logSelf('Connection error.')
	}
}
