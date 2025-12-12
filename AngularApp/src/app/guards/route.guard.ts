import { inject } from "@angular/core";
import { CanActivateFn, Router } from "@angular/router";
import { AndroidBridgeService } from "../services/android-bridge.service";

export const blockedRouteGuard: CanActivateFn = (route, state) => {
	const router = inject(Router);
	const bridge = inject(AndroidBridgeService);

	const blockedRoutes = ["/admin", "/settings", "/private", "/external"];

	if (blockedRoutes.includes(state.url)) {
		bridge.sendToAndroid("route_blocked", state.url);
		bridge.log(`Route bloquée: ${state.url}`);

		// Rediriger vers la page d'accueil
		router.navigate(["/"]);
		return false;
	}

	return true;
};
