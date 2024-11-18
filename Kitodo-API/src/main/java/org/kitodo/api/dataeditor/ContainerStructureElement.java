package org.kitodo.api.dataeditor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang.StringUtils;

public class ContainerStructureElement implements Comparable<ContainerStructureElement> {

    private static final String SIGNATURE_IDENTIFIER = "Signatur";
    private final String type;
    private List<ContainerStructureMetadata> metadata;
    /* Primarily used for structuring the container data in DocketData.java. */
    private List<ContainerStructureElement> children;

    /**
     * Default constructor.
     * @param type String containing the type of the element
     */
    public ContainerStructureElement(String type) {
        this.type = type;
        this.metadata = new ArrayList<>();
        this.children = new ArrayList<>();
    }

    public String getType() {
        return type;
    }

    public List<ContainerStructureMetadata> getMetadata() {
        return metadata;
    }

    public void setMetadata(List<ContainerStructureMetadata> metadata) {
        this.metadata = metadata;
    }

    public void addMetadata(ContainerStructureMetadata metadata) {
        this.metadata.add(metadata);
    }

    /**
     * Get children.
     *
     * @return value of children
     */
    public List<ContainerStructureElement> getChildren() {
        return children;
    }

    /**
     * Set children.
     *
     * @param children as java.util.List<org.kitodo.api.dataeditor.ContainerStructureElement>
     */
    public void setChildren(List<ContainerStructureElement> children) {
        this.children = children;
    }

    public String getSignature() {
        return metadata.stream()
                .filter(containerStructureMetadata -> containerStructureMetadata.getMetadata().equals(SIGNATURE_IDENTIFIER))
                .findFirst()
                .map(ContainerStructureMetadata::getValue)
                .orElse("");
    }

    /**
     * Compare containerStructureElement to another containerStructureElement object by their signatures.
     * @param containerStructureElement object
     * @return int representing result of comparison
     */
    public int compareTo(ContainerStructureElement containerStructureElement) {
        String[] firstSplitSignature = new String[] {};
        String[] secondSplitSignature = new String[] {};
        if (Objects.nonNull(getSignature())) {
            firstSplitSignature = getSignature().split("\\D+");
        }
        if (Objects.nonNull(containerStructureElement.getSignature())) {
            secondSplitSignature = containerStructureElement.getSignature().split("\\D+");
        }
        for (int i = 0; i < Math.max(firstSplitSignature.length, secondSplitSignature.length); i++) {
            int first = -1;
            int second = -1;
            if (i < firstSplitSignature.length && StringUtils.isNotEmpty(firstSplitSignature[i])) {
                first = Integer.parseInt(firstSplitSignature[i]);
            }
            if (i < secondSplitSignature.length && StringUtils.isNotEmpty(secondSplitSignature[i])) {
                second = Integer.parseInt(secondSplitSignature[i]);
            }
            if (first < second) {
                return -1;
            } else if (second < first) {
                return 1;
            }
        }
        return 0;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (object instanceof ContainerStructureElement) {
            ContainerStructureElement containerStructureElement = (ContainerStructureElement) object;
            return Objects.equals(type, containerStructureElement.getType())
                    && Objects.equals(getSignature(), containerStructureElement.getSignature())
                    && children.equals(containerStructureElement.getChildren());
        }

        return false;
    }
}
