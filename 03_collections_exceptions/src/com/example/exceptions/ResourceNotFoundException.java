package com.example.exceptions;

/**
 * Concrete Unchecked Custom Domain Exception for missing resources.
 * Extends BaseDomainException (RuntimeException).
 */
public class ResourceNotFoundException extends BaseDomainException {

    private final String resourceType;
    private final String resourceId;

    public ResourceNotFoundException(String resourceType, String resourceId) {
        super(ErrorCode.RESOURCE_NOT_FOUND, 
              String.format("Resource '%s' with identifier '%s' could not be found.", resourceType, resourceId));
        this.resourceType = resourceType;
        this.resourceId = resourceId;
    }

    public ResourceNotFoundException(String resourceType, String resourceId, Throwable cause) {
        super(ErrorCode.RESOURCE_NOT_FOUND, 
              String.format("Resource '%s' with identifier '%s' could not be found.", resourceType, resourceId), cause);
        this.resourceType = resourceType;
        this.resourceId = resourceId;
    }

    public String getResourceType() {
        return resourceType;
    }

    public String getResourceId() {
        return resourceId;
    }
}
