import { Component } from "@angular/core";
import { CommonModule } from "@angular/common";

@Component({
	selector: "app-admin",
	standalone: true,
	imports: [CommonModule],
	template: `
		<div class="container">
			<div class="card blocked-card">
				<h2>🚫 Zone Admin</h2>
				<p>Cette page est bloquée par le RouteGuard.</p>
				<p>Vous ne devriez pas voir cette page.</p>
			</div>
		</div>
	`,
	styles: [
		`
			.blocked-card {
				text-align: center;
				border: 2px solid var(--error);
			}

			.blocked-card h2 {
				color: var(--error);
				margin-bottom: 12px;
			}
		`,
	],
})
export class AdminComponent {}
