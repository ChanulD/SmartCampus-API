// Name : W.A.C.D.Wijesinghe / IIT ID : 20241938 / UoW ID : w2151956
package com.smartcampus.exception;

/**
 * Thrown when a requested resource cannot be found (maps to HTTP 404).
 */
public class ResourceNotFoundException extends RuntimeException {

    private final String resourceType;
    private final String resourceId;

    public ResourceNotFoundException(String resourceType, String resourceId) {
        super(resourceType + " with id '" + resourceId + "' was not found.");
        this.resourceType = resourceType;
        this.resourceId   = resourceId;
    }

    public String getResourceType() { return resourceType; }
    public String getResourceId()   { return resourceId; }
}
