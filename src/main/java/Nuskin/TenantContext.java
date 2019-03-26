package Nuskin;

public class TenantContext {

	// ThreadLocal<T> get/set refer to a thread-specific instance of T
    private static ThreadLocal<String> tenant = new ThreadLocal<>();

    public static void setTenantName(String tenantName) {
        tenant.set(tenantName);
    }

    public static String getTenantName() {
        return tenant.get();
    }

}