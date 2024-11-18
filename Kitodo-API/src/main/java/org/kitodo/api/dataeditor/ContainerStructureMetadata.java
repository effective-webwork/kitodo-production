package org.kitodo.api.dataeditor;

public class ContainerStructureMetadata {

    private String metadata;
    private String value;

    public ContainerStructureMetadata(String metadata, String value) {
        this.metadata = metadata;
        this.value = value;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
