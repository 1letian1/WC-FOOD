package com.shike.ordering.auth.model;
public record CurrentPrincipal(Long principalId, PrincipalType principalType, Long shopId) { }
