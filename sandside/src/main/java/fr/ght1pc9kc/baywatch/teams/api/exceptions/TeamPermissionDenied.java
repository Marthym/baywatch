package fr.ght1pc9kc.baywatch.teams.api.exceptions;

import fr.ght1pc9kc.baywatch.common.api.exceptions.UnauthorizedException;

public class TeamPermissionDenied extends UnauthorizedException {
    public TeamPermissionDenied() {
        super();
    }

    public TeamPermissionDenied(String message) {
        super(message);
    }
}
