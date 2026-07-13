package com.shike.ordering.auth.model;
import com.shike.ordering.common.exception.UnauthorizedException;
import com.shike.ordering.common.exception.ForbiddenException;
public final class PrincipalContext {
    private static final ThreadLocal<CurrentPrincipal> HOLDER = new ThreadLocal<>();
    private PrincipalContext() { }
    public static void set(CurrentPrincipal principal) { HOLDER.set(principal); }
    public static CurrentPrincipal get() { return HOLDER.get(); }
    public static CurrentPrincipal require() {
        CurrentPrincipal principal = HOLDER.get();
        if (principal == null) throw new UnauthorizedException();
        return principal;
    }
    public static CurrentPrincipal require(PrincipalType expectedType) {
        CurrentPrincipal principal = require();
        if (principal.principalType() != expectedType) throw new ForbiddenException();
        return principal;
    }
    public static void clear() { HOLDER.remove(); }
}
