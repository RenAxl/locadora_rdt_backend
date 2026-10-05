package com.locadora_rdt_backend.shared.constants;

public class PermissionConstants {

    private PermissionConstants() {
    }

    // users
    public static final String USER_READ = "hasAuthority('USER_READ')";
    public static final String USER_WRITE = "hasAuthority('USER_WRITE')";
    public static final String USER_DELETE = "hasAnyAuthority('ROLE_ADMINISTRADOR', 'USER_DELETE')";
    public static final String USER_PROFILE_READ = "hasAuthority('USER_PROFILE_READ')";
    public static final String USER_PROFILE_WRITE = "hasAuthority('USER_PROFILE_WRITE')";

    // roles
    public static final String ROLE_READ = "hasAuthority('ROLE_READ')";
    public static final String ROLE_WRITE = "hasAuthority('ROLE_WRITE')";

    // permissions
    public static final String PERMISSION_READ = "hasAuthority('PERMISSION_READ')";

    // customers
    public static final String CUSTOMER_READ = "hasAuthority('CUSTOMER_READ')";
    public static final String CUSTOMER_WRITE = "hasAuthority('CUSTOMER_WRITE')";
    public static final String CUSTOMER_DELETE = "hasAuthority('CUSTOMER_DELETE')";

    // suppliers
    public static final String SUPPLIER_READ = "hasAuthority('SUPPLIER_READ')";
    public static final String SUPPLIER_WRITE = "hasAuthority('SUPPLIER_WRITE')";
    public static final String SUPPLIER_DELETE = "hasAuthority('SUPPLIER_DELETE')";

    // employees
    public static final String EMPLOYEE_READ = "hasAuthority('EMPLOYEE_READ')";
    public static final String EMPLOYEE_WRITE = "hasAuthority('EMPLOYEE_WRITE')";
    public static final String EMPLOYEE_DELETE = "hasAuthority('EMPLOYEE_DELETE')";

    // positions
    public static final String POSITION_READ = "hasAuthority('POSITION_READ')";
    public static final String POSITION_WRITE = "hasAuthority('POSITION_WRITE')";
    public static final String POSITION_DELETE = "hasAuthority('POSITION_DELETE')";

    // departments
    public static final String DEPARTMENT_READ = "hasAuthority('DEPARTMENT_READ')";
    public static final String DEPARTMENT_WRITE = "hasAuthority('DEPARTMENT_WRITE')";
    public static final String DEPARTMENT_DELETE = "hasAuthority('DEPARTMENT_DELETE')";

    // payment_frequencies
    public static final String FREQUENCY_READ = "hasAuthority('FREQUENCY_READ')";
    public static final String FREQUENCY_WRITE = "hasAuthority('FREQUENCY_WRITE')";
    public static final String FREQUENCY_DELETE = "hasAuthority('FREQUENCY_DELETE')";

    // payment_methods
    public static final String METHODS_READ = "hasAuthority('METHODS_READ')";
    public static final String METHODS_WRITE = "hasAuthority('METHODS_WRITE')";
    public static final String METHODS_DELETE = "hasAuthority('METHODS_DELETE')";

    // payables
    public static final String PAYABLE_READ = "hasAuthority('PAYABLE_READ')";
    public static final String PAYABLE_WRITE = "hasAuthority('PAYABLE_WRITE')";
    public static final String PAYABLE_DELETE = "hasAuthority('PAYABLE_DELETE')";

    // receivables
    public static final String RECEIVABLE_READ = "hasAuthority('RECEIVABLE_READ')";
    public static final String RECEIVABLE_WRITE = "hasAuthority('RECEIVABLE_WRITE')";
    public static final String RECEIVABLE_DELETE = "hasAuthority('RECEIVABLE_DELETE')";

    // financial_reports
    public static final String FINANCIAL_REPORTS_READ = "hasAuthority('FINANCIAL_REPORTS_READ')";

    // system_settings
    public static final String SYSTEM_SETTING_READ = "hasAuthority('SYSTEM_SETTING_READ')";
    public static final String SYSTEM_SETTING_WRITE = "hasAuthority('SYSTEM_SETTING_WRITE')";

    // categories
    public static final String CATEGORY_READ = "hasAuthority('CATEGORY_READ')";
    public static final String CATEGORY_WRITE = "hasAuthority('CATEGORY_WRITE')";
    public static final String CATEGORY_DELETE = "hasAuthority('CATEGORY_DELETE')";

    // items
    public static final String ITEM_READ = "hasAuthority('ITEM_READ')";
    public static final String ITEM_WRITE = "hasAuthority('ITEM_WRITE')";
    public static final String ITEM_DELETE = "hasAuthority('ITEM_DELETE')";

    // item_units
    public static final String ITEM_UNIT_READ = "hasAuthority('ITEM_UNIT_READ')";
    public static final String ITEM_UNIT_WRITE = "hasAuthority('ITEM_UNIT_WRITE')";
    public static final String ITEM_UNIT_DELETE = "hasAuthority('ITEM_UNIT_DELETE')";

    // stock_balances
    public static final String STOCK_BALANCES_READ = "hasAuthority('STOCK_BALANCES_READ')";
    public static final String STOCK_BALANCES_WRITE = "hasAuthority('STOCK_BALANCES_WRITE')";

    // financial_settings
    public static final String FINANCIAL_SETTINGS_READ = "hasAuthority('FINANCIAL_SETTINGS_READ')";
    public static final String FINANCIAL_SETTINGS_WRITE = "hasAuthority('FINANCIAL_SETTINGS_WRITE')";

}
