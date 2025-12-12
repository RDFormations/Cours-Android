import { Injectable, NgZone } from "@angular/core";
import { BehaviorSubject, Observable } from "rxjs";

declare global {
	interface Window {
		AndroidBridge?: {
			postMessage: (message: string) => void;
			requestNavigation: (route: string) => void;
			log: (message: string) => void;
		};
		receiveFromAndroid?: (data: string) => void;
	}
}

export interface AndroidMessage {
	action: string;
	value: string;
	timestamp: Date;
}

@Injectable({
	providedIn: "root",
})
export class AndroidBridgeService {
	private messagesSubject = new BehaviorSubject<AndroidMessage[]>([]);
	private connectedSubject = new BehaviorSubject<boolean>(false);
	private themeSubject = new BehaviorSubject<"light" | "dark">("light");

	messages$ = this.messagesSubject.asObservable();
	connected$ = this.connectedSubject.asObservable();
	theme$ = this.themeSubject.asObservable();

	private readonly messageHandlers: Record<string, (value: string) => void> = {
		update_theme: (value) => this.handleThemeUpdate(value),
		show_alert: (value) => this.handleShowAlert(value),
		update_data: (value) => this.handleDataUpdate(value),
	};

	constructor(private ngZone: NgZone) {
		this.initBridge();
	}

	private initBridge(): void {
		window.receiveFromAndroid = (data: string) => {
			this.ngZone.run(() => {
				this.processMessage(data);
			});
		};

		this.connectedSubject.next(this.isConnected());
		this.log("Angular Bridge initialized");
	}

	isConnected(): boolean {
		return !!window.AndroidBridge;
	}

	sendToAndroid(action: string, value: string = ""): void {
		const message = value ? `${action}:${value}` : action;

		this.addMessage({
			action: `→ ${action}`,
			value,
			timestamp: new Date(),
		});

		if (window.AndroidBridge) {
			window.AndroidBridge.postMessage(message);
		} else {
			console.warn("Android Bridge not available");
		}
	}

	requestNavigation(route: string): void {
		if (window.AndroidBridge) {
			window.AndroidBridge.requestNavigation(route);
		}
	}

	log(message: string): void {
		if (window.AndroidBridge) {
			window.AndroidBridge.log(message);
		}
		console.log("[Angular]", message);
	}

	private processMessage(data: string): void {
		const [action, ...valueParts] = data.split(":");
		const value = valueParts.join(":");

		this.addMessage({
			action: `← ${action}`,
			value,
			timestamp: new Date(),
		});

		const handler = this.messageHandlers[action];
		if (handler) {
			handler(value);
		}
	}

	private handleThemeUpdate(theme: string): void {
		const newTheme = theme as "light" | "dark";
		this.themeSubject.next(newTheme);
		document.documentElement.setAttribute("data-theme", newTheme);
	}

	private handleShowAlert(message: string): void {
		this.addMessage({
			action: "📢 Alert",
			value: message,
			timestamp: new Date(),
		});
	}

	private handleDataUpdate(timestamp: string): void {
		const date = new Date(parseInt(timestamp));
		this.addMessage({
			action: "📊 Data Update",
			value: date.toLocaleTimeString(),
			timestamp: new Date(),
		});
	}

	private addMessage(message: AndroidMessage): void {
		const current = this.messagesSubject.value;
		this.messagesSubject.next([...current, message]);
	}

	clearMessages(): void {
		this.messagesSubject.next([]);
	}
}
