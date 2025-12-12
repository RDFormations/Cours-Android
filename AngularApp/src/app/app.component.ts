import { Component, OnInit, inject } from "@angular/core";
import { CommonModule } from "@angular/common";
import { RouterOutlet } from "@angular/router";
import { AndroidBridgeService } from "./services/android-bridge.service";

@Component({
	selector: "app-root",
	standalone: true,
	imports: [CommonModule, RouterOutlet],
	template: `
		<div class="app-container" [attr.data-theme]="theme$ | async">
			<header class="header">
				<h1>🅰️ Angular App</h1>
				<div class="connection-status" [class.connected]="connected$ | async">
					<span class="status-dot"></span>
					<span>{{ (connected$ | async) ? "Connecté" : "Non connecté" }}</span>
				</div>
			</header>
			<main>
				<router-outlet></router-outlet>
			</main>
		</div>
	`,
	styles: [
		`
			.app-container {
				min-height: 100vh;
			}

			.header {
				background: linear-gradient(135deg, #673ab7 0%, #512da8 100%);
				color: white;
				padding: 16px;
				display: flex;
				justify-content: space-between;
				align-items: center;
			}

			.header h1 {
				font-size: 1.25rem;
				font-weight: 600;
			}

			.connection-status {
				display: flex;
				align-items: center;
				gap: 8px;
				font-size: 0.85rem;
				opacity: 0.9;
			}

			.status-dot {
				width: 8px;
				height: 8px;
				border-radius: 50%;
				background: #f44336;
			}

			.connection-status.connected .status-dot {
				background: #4caf50;
			}

			main {
				padding: 16px;
			}
		`,
	],
})
export class AppComponent implements OnInit {
	private bridge = inject(AndroidBridgeService);
	connected$ = this.bridge.connected$;
	theme$ = this.bridge.theme$;

	ngOnInit(): void {
		this.bridge.log("App Angular démarrée");
	}
}
