package com.shike.ordering.auth.model;
import com.shike.ordering.common.exception.UnauthorizedException;
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
    public static void clear() { HOLDER.remove(); }
}
