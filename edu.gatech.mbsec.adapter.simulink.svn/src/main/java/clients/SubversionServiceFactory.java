package clients;

/** Creates the configured implementation without making SVNKit a default-build dependency. */
public final class SubversionServiceFactory {
    private SubversionServiceFactory() {
    }

    public static SubversionService create() {
        final String implementation = System.getProperty("subversion.client.impl", "standalone");
        if ("subversion".equalsIgnoreCase(implementation) || "svnkit".equalsIgnoreCase(implementation)) {
            try {
                return (SubversionService) Class.forName("clients.SubversionServiceSvnkitImpl")
                        .getDeclaredConstructor().newInstance();
            } catch (ReflectiveOperationException exception) {
                throw new IllegalStateException(
                        "The SVNKit implementation is not available. Build and run with -Pfull.", exception);
            }
        }
        return new SubversionServiceStandaloneImpl();
    }
}
