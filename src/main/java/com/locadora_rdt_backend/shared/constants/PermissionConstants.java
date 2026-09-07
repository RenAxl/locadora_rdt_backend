package com.locadora_rdt_backend.shared.constants;

public class PermissionConstants {

    private PermissionConstants() {
    }

    // users
    public static final String USER_READ = "hasAuthority('USER_READ')";
    public static final String USER_WRITE = "hasAuthority('USER_WRITE')";
    public static final String USER_DELETE = "hasAuthority('USER_DELETE')";

    // roles
    public static final String ROLE_READ = "hasAuthority('ROLE_READ')";
    public static final String ROLE_WRITE = "hasAuthority('ROLE_WRITE')";

    // permissions
    public static final String PERMISSION_READ = "hasAuthority('PERMISSION_READ')";

}
