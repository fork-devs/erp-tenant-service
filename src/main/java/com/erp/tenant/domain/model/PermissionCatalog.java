package com.erp.tenant.domain.model;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Atomic workshop-assignable permission catalog (excludes PLATFORM_ADMIN).
 */
public final class PermissionCatalog {

    public record PermissionItem(String code, String description) {}

    private static final Map<String, String> CATALOG = new LinkedHashMap<>();

    static {
        CATALOG.put("WORKORDER_READ", "View work orders and diagnostics");
        CATALOG.put("WORKORDER_EXECUTE", "Create, update, and execute work order tasks");
        CATALOG.put("WORKORDER_DELETE", "Delete work orders");
        CATALOG.put("INVENTORY_READ", "View products and stock batches");
        CATALOG.put("INVENTORY_MANAGE", "Create, update, delete products and manage stock batches");
        CATALOG.put("SERVICE_MANAGE", "Create, update, and delete workshop services");
        CATALOG.put("QUOTE_READ", "View quotes");
        CATALOG.put("QUOTE_CREATE", "Create and update draft quotes");
        CATALOG.put("QUOTE_APPROVE", "Approve quotes");
        CATALOG.put("QUOTE_CANCEL", "Cancel quotes");
        CATALOG.put("VOUCHER_READ", "View billing vouchers");
        CATALOG.put("CHECKOUT_PROCESS", "Generate vouchers, process checkouts, and record payments");
        CATALOG.put("APPOINTMENT_MANAGE", "Create, update, and cancel branch appointments");
        CATALOG.put("STAFF_READ", "View branch employee registrations");
        CATALOG.put("STAFF_MANAGE", "Create, update, approve, reject, and deactivate staff registrations");
        CATALOG.put("SPECIALTY_MANAGE", "Create, update, and deactivate workshop specialties");
        CATALOG.put("BRANCH_READ", "View branch details");
        CATALOG.put("BRANCH_MANAGE", "Create and update branches and manage branch subscriptions");
        CATALOG.put("MEDIA_UPLOAD", "Upload media assets for workshop operations");
        CATALOG.put("ANALYTICS_READ", "View branch operational and financial analytics");
        CATALOG.put("ROLE_MANAGE", "Create, update, deactivate, and assign custom workshop roles");
    }

    private PermissionCatalog() {}

    public static List<PermissionItem> listAssignable() {
        return CATALOG.entrySet().stream()
                .map(e -> new PermissionItem(e.getKey(), e.getValue()))
                .toList();
    }

    public static boolean isValidAssignable(String code) {
        return code != null && CATALOG.containsKey(code);
    }

    public static Set<String> allAssignableCodes() {
        return CATALOG.keySet();
    }
}
