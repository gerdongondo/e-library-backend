package com.luv2code.springbootlibrary.exception;

public class ResourceInUseException extends RuntimeException {

    private String resourceName;
    private String dependentResource;
    private Long usageCount;

    public ResourceInUseException(String message) {
        super(message);
    }

    public ResourceInUseException(String resourceName, String dependentResource, Long usageCount) {
        super(String.format("Impossible de supprimer %s car il est utilisé par %d %s",
                resourceName, usageCount, dependentResource));
        this.resourceName = resourceName;
        this.dependentResource = dependentResource;
        this.usageCount = usageCount;
    }

    public ResourceInUseException(String message, Throwable cause) {
        super(message, cause);
    }

    // Getters
    public String getResourceName() {
        return resourceName;
    }

    public String getDependentResource() {
        return dependentResource;
    }

    public Long getUsageCount() {
        return usageCount;
    }
}