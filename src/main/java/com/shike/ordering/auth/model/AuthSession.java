package com.shike.ordering.auth.model;
import java.time.LocalDateTime;
public record AuthSession(Long principalId, PrincipalType principalType, Long shopId, LocalDateTime loginTime, Integer sessionVersion) { }
