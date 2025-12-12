import { Routes } from "@angular/router";
import { HomeComponent } from "./components/home/home.component";
import { AdminComponent } from "./components/admin/admin.component";
import { SettingsComponent } from "./components/settings/settings.component";
import { blockedRouteGuard } from "./guards/route.guard";

export const routes: Routes = [
	{ path: "", component: HomeComponent },
	{
		path: "admin",
		component: AdminComponent,
		canActivate: [blockedRouteGuard],
	},
	{
		path: "settings",
		component: SettingsComponent,
		canActivate: [blockedRouteGuard],
	},
	{ path: "**", redirectTo: "" },
];
