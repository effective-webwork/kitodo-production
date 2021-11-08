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
import org.hibernate.search.engine.backend.document.IndexFieldReference;
import org.hibernate.search.engine.backend.document.IndexObjectFieldReference;
import org.hibernate.search.engine.backend.document.model.dsl.IndexSchemaObjectField;
import org.hibernate.search.engine.backend.types.IndexFieldType;
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
        IndexSchemaObjectField group = metadataField.objectField("group");
        IndexFieldType<String> stringIndexFieldType = context.typeFactory().asString().toIndexFieldType();

        context.bridge(List.class, new MetadataBridge(
                metadataField.multiValued().toReference(),
                metadataField.field("key", stringIndexFieldType).toReference(),
                metadataField.field("value", stringIndexFieldType).toReference(),
                group.multiValued().toReference(),
                group.field("key", stringIndexFieldType).toReference(),
                group.field("value", stringIndexFieldType).toReference()));
    }

    private static class MetadataBridge implements PropertyBridge<List> {
        private final IndexObjectFieldReference metadata;
        private final IndexFieldReference<String> key;
        private final IndexFieldReference<String> value;
        private final IndexObjectFieldReference group;
        private final IndexFieldReference<String> groupKey;
        private final IndexFieldReference<String> groupValue;

        public MetadataBridge(IndexObjectFieldReference metadata, IndexFieldReference<String> key,
                              IndexFieldReference<String> value, IndexObjectFieldReference group,
                              IndexFieldReference<String> groupKey, IndexFieldReference<String> groupValue) {
            this.metadata = metadata;
            this.key = key;
            this.value = value;
            this.group = group;
            this.groupKey = groupKey;
            this.groupValue = groupValue;
        }

        @Override
        public void write(DocumentElement documentElement, List metadataElements, PropertyBridgeWriteContext context) {
            List<Metadata> metadataList = (List<Metadata>) metadataElements;
            for (Metadata metadata : metadataList) {
                DocumentElement metadataElement = documentElement.addObject(this.metadata);
                metadataElement.addValue(key, metadata.getKey());
                if (metadata instanceof MetadataEntry) {
                    metadataElement.addValue(value, ((MetadataEntry) metadata).getValue());
                } else if (metadata instanceof MetadataGroup) {
                    for (Metadata meta : ((MetadataGroup) metadata).getGroup()) {
                        DocumentElement groupElement = metadataElement.addObject(group);
                        groupElement.addValue(groupKey, meta.getKey());
                        groupElement.addValue(groupValue, ((MetadataEntry) meta).getValue());
                    }
                }
            }
        }
    }
}
