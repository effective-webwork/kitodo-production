/*
 * (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
 *
 * This file is part of the Kitodo project.
 *
 * It is licensed under GNU General Public License version 3 or later.
 *
 * For the full copyright and license information, please read the
 * GPL3-License.txt file that was distributed with this source code.
 */

package org.kitodo.data.elasticsearch.bridges;

import java.util.List;

import org.hibernate.search.engine.backend.document.DocumentElement;
import org.hibernate.search.engine.backend.document.IndexObjectFieldReference;
import org.hibernate.search.engine.backend.document.model.dsl.IndexSchemaObjectField;
import org.hibernate.search.mapper.pojo.bridge.PropertyBridge;
import org.hibernate.search.mapper.pojo.bridge.binding.PropertyBindingContext;
import org.hibernate.search.mapper.pojo.bridge.mapping.programmatic.PropertyBinder;
import org.hibernate.search.mapper.pojo.bridge.runtime.PropertyBridgeWriteContext;
import org.kitodo.api.Metadata;
import org.kitodo.api.MetadataEntry;
import org.kitodo.api.MetadataGroup;

public class MetadataBinder implements PropertyBinder {

    @Override
    public void bind(PropertyBindingContext context) {
        context.dependencies().use("key");

        IndexSchemaObjectField metadataField = context.indexSchemaElement().objectField("metadata");
        metadataField.objectFieldTemplate("metadataGroup").matchingPathGlob("*group").multiValued();
        metadataField.fieldTemplate("metadataTemplate_key", f -> f.asString()).matchingPathGlob("*key");
        metadataField.fieldTemplate("metadataTemplate_value", f -> f.asString()).matchingPathGlob("*value");

        context.bridge(List.class, new MetadataBridge(metadataField.multiValued().toReference()));
    }

    private static class MetadataBridge implements PropertyBridge<List> {
        private final IndexObjectFieldReference metadataObject;

        public MetadataBridge(IndexObjectFieldReference metadata) {
            this.metadataObject = metadata;
        }

        @Override
        public void write(DocumentElement documentElement, List metadataElements, PropertyBridgeWriteContext context) {
            for (Object listElement : metadataElements) {
                if (listElement instanceof Metadata) {
                    DocumentElement rootElement = documentElement.addObject(metadataObject);
                    evaluateMetadataElement(rootElement, (Metadata) listElement);
                }
            }
        }

        private void evaluateMetadataElement(DocumentElement containerElement, Metadata metadata) {
            containerElement.addValue("key", metadata.getKey());
            if (metadata instanceof MetadataEntry) {
                containerElement.addValue("value", ((MetadataEntry) metadata).getValue());
            } else if (metadata instanceof MetadataGroup) {
                for (Metadata meta : ((MetadataGroup) metadata).getMetadata()) {
                    DocumentElement groupElement = containerElement.addObject("group");
                    evaluateMetadataElement(groupElement, meta);
                }
            }
        }
    }
}
