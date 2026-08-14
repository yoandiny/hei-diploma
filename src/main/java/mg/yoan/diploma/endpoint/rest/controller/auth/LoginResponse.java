package mg.yoan.diploma.endpoint.rest.controller.auth;

import mg.yoan.diploma.domain.Role;

public record LoginResponse(
    String accessToken, String tokenType, long expiresIn, String userId, Role role) {}
