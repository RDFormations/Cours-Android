import { Component, inject } from "@angular/core";
import { CommonModule } from "@angular/common";
import { RouterLink } from "@angular/router";
import { AndroidBridgeService } from "../../services/android-bridge.service";

@Component({
	selector: "app-home",
	standalone: true,
	imports: [CommonModule, RouterLink],
	template: `
		<div class="container">
			<!-- Actions Angular → Android -->
			<div class="card">
				<h2 class="card-title">📤 Angular → Android</h2>
				<div class="btn-group">
					<button
						class="btn btn-primary"
						(click)="sendEvent('button_click', 'primary')"
					>
						Click Event
					</button>
					<button
						class="btn btn-primary"
						(click)="sendEvent('form_submit', 'user_data')"
					>
						Form Submit
					</button>
					<button
						class="btn btn-primary"
						(click)="sendEvent('user_action', 'navigation')"
					>
						User Action
					</button>
				</div>
			</div>

			<!-- Données reçues -->
			<div class="card">
				<h2 class="card-title">📥 Données Android</h2>
				<div class="data-display">
					<p>
						Theme actuel: <strong>{{ theme$ | async }}</strong>
					</p>
					<p>
						Dernier message: <strong>{{ lastMessage }}</strong>
					</p>
				</div>
			</div>

			<!-- Test Routes Guard -->
			<div class="card">
				<h2 class="card-title">🔗 Test Routes (Guard)</h2>
				<div class="routes-list">
					<a routerLink="/" class="route-item allowed">
						<span class="route-icon">✅</span>
						<span>/ (Home - autorisé)</span>
					</a>
					<a routerLink="/admin" class="route-item blocked">
						<span class="route-icon">🚫</span>
						<span>/admin (bloqué)</span>
					</a>
					<a routerLink="/settings" class="route-item blocked">
						<span class="route-icon">🚫</span>
						<span>/settings (bloqué)</span>
					</a>
					<a
						href="https://google.com"
						class="route-item external"
						(click)="openExternal($event, 'https://google.com')"
					>
						<span class="route-icon">🌐</span>
						<span>google.com (externe)</span>
					</a>
				</div>
			</div>

			<!-- Console -->
			<div class="card console-card">
				<div class="console-header">
					<h2 class="card-title">📋 Console</h2>
					<button class="btn-clear" (click)="clearMessages()">Clear</button>
				</div>
				<div class="console">
					@for (msg of messages$ | async; track msg.timestamp) {
					<div
						class="console-line"
						[class.incoming]="msg.action.startsWith('←')"
						[class.outgoing]="msg.action.startsWith('→')"
					>
						<span class="time">{{ msg.timestamp | date : "HH:mm:ss" }}</span>
						<span class="action">{{ msg.action }}</span>
						<span class="value">{{ msg.value }}</span>
					</div>
					}
				</div>
			</div>
		</div>
	`,
	styles: [
		`
			.data-display {
				background: rgba(103, 58, 183, 0.08);
				padding: 12px;
				border-radius: 8px;
			}

			.data-display p {
				margin: 4px 0;
				color: var(--text-secondary);
			}

			.data-display strong {
				color: var(--text);
			}

			.routes-list {
				display: flex;
				flex-direction: column;
				gap: 8px;
			}

			.route-item {
				display: flex;
				align-items: center;
				gap: 12px;
				padding: 12px;
				border-radius: 8px;
				border: 1px solid var(--border);
				text-decoration: none;
				color: var(--text);
				transition: all 0.2s;
			}

			.route-item:hover {
				background: rgba(103, 58, 183, 0.05);
				text-decoration: none;
			}

			.route-item.blocked {
				border-color: var(--error);
				opacity: 0.8;
			}

			.route-item.external {
				border-color: var(--warning);
			}

			.route-icon {
				font-size: 1.2rem;
			}

			.console-card {
				padding: 0;
				overflow: hidden;
			}

			.console-header {
				display: flex;
				justify-content: space-between;
				align-items: center;
				padding: 12px 16px;
				border-bottom: 1px solid var(--border);
			}

			.console-header .card-title {
				margin: 0;
			}

			.btn-clear {
				background: none;
				border: 1px solid var(--border);
				padding: 4px 12px;
				border-radius: 4px;
				cursor: pointer;
				font-size: 0.8rem;
				color: var(--text-secondary);
			}

			.console {
				background: #1a1a2e;
				padding: 12px;
				max-height: 200px;
				overflow-y: auto;
				font-family: "Fira Code", "Courier New", monospace;
				font-size: 0.8rem;
			}

			.console-line {
				display: flex;
				gap: 8px;
				padding: 4px 0;
				color: #a0a0a0;
			}

			.console-line.incoming {
				color: #64b5f6;
			}

			.console-line.outgoing {
				color: #81c784;
			}

			.time {
				color: #666;
			}

			.action {
				font-weight: 500;
			}

			.value {
				color: #ffb74d;
			}
		`,
	],
})
export class HomeComponent {
	private bridge = inject(AndroidBridgeService);
	messages$ = this.bridge.messages$;
	theme$ = this.bridge.theme$;
	lastMessage = "Aucun";

	constructor() {
		this.messages$.subscribe((messages) => {
			if (messages.length > 0) {
				const last = messages[messages.length - 1];
				this.lastMessage = `${last.action} ${last.value}`;
			}
		});
	}

	sendEvent(action: string, value: string): void {
		this.bridge.sendToAndroid(action, value);
	}

	openExternal(event: Event, url: string): void {
		event.preventDefault();
		this.bridge.requestNavigation(url);
	}

	clearMessages(): void {
		this.bridge.clearMessages();
	}
}
