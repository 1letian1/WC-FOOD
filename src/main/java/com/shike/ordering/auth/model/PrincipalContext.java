package com.shike.ordering.auth.model;
public final class PrincipalContext {
    private static final ThreadLocal<CurrentPrincipal> HOLDER = new ThreadLocal<>();
    private PrincipalContext() { }
    public static void set(CurrentPrincipal principal) { HOLDER.set(principal); }
    public static CurrentPrincipal get() { return HOLDER.get(); }
    public static void clear() { HOLDER.remove(); }
}
