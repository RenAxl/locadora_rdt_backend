package com.locadora_rdt_backend.shared.constants;

public class PermissionConstants {

    private PermissionConstants() {
    }

    // users
    public static final String USER_READ = "hasAuthority('USER_READ')";
    public static final String USER_WRITE = "hasAuthority('USER_WRITE')";
    public static final String USER_DELETE = "hasAuthority('USER_DELETE')";
    public static final String USER_PROFILE_READ = "hasAuthority('USER_PROFILE_READ')";
    public static final String USER_PROFILE_WRITE = "hasAuthority('USER_PROFILE_WRITE')";

    // customers
    public static final String CUSTOMER_READ = "hasAuthority('CUSTOMER_READ')";
    public static final String CUSTOMER_WRITE = "hasAuthority('CUSTOMER_WRITE')";
    public static final String CUSTOMER_DELETE = "hasAuthority('CUSTOMER_DELETE')";

    // roles
    public static final String ROLE_READ = "hasAuthority('ROLE_READ')";
    public static final String ROLE_WRITE = "hasAuthority('ROLE_WRITE')";

    // permissions
    public static final String PERMISSION_READ = "hasAuthority('PERMISSION_READ')";

    // system_settings
    public static final String SYSTEM_SETTING_READ = "hasAuthority('SYSTEM_SETTING_READ')";
    public static final String SYSTEM_SETTING_WRITE = "hasAuthority('SYSTEM_SETTING_WRITE')";

}
