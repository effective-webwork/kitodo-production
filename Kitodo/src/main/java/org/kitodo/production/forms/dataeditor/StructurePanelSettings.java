package org.kitodo.production.forms.dataeditor;


import javax.enterprise.context.SessionScoped;
import javax.inject.Named;
import java.io.Serializable;

@Named("StructurePanelSettings")
@SessionScoped
public class StructurePanelSettings implements Serializable {

    private Boolean showPageRangeInLogicalTree = null;
    private Boolean showHierarchyLevel = null;
    private String structureViewMode = null;
    private String physicalViewMode = null;
    private String containersViewMode = "Typ";

    public Boolean getShowPageRangeInLogicalTree() {
        return showPageRangeInLogicalTree;
    }

    public void setShowPageRangeInLogicalTree(Boolean showPageRangeInLogicalTree) {
        this.showPageRangeInLogicalTree = showPageRangeInLogicalTree;
    }

    public Boolean getShowHierarchyLevel() {
        return showHierarchyLevel;
    }

    public void setShowHierarchyLevel(Boolean showHierarchyLevel) {
        this.showHierarchyLevel = showHierarchyLevel;
    }

    public String getStructureViewMode() {
        return structureViewMode;
    }

    public void setStructureViewMode(String structureViewMode) {
        this.structureViewMode = structureViewMode;
    }

    public String getPhysicalViewMode() {
        return physicalViewMode;
    }

    public void setPhysicalViewMode(String physicalViewMode) {
        this.physicalViewMode = physicalViewMode;
    }

    public String getContainersViewMode() {
        return containersViewMode;
    }

    public void setContainersViewMode(String containersViewMode) {
        this.containersViewMode = containersViewMode;
    }
}
