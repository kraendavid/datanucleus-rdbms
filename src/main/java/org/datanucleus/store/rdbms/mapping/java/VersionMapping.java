/**********************************************************************
Copyright (c) 2004 Erik Bengtson and others. All rights reserved.
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License. 
 

Contributors:
2004 Andy Jefferson - added javadocs
2004 Andy Jefferson - changed to use Column spec from MetaData
    ...
**********************************************************************/
package org.datanucleus.store.rdbms.mapping.java;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.datanucleus.ExecutionContext;
import org.datanucleus.metadata.ColumnMetaData;
import org.datanucleus.metadata.VersionMetaData;
import org.datanucleus.store.rdbms.identifier.DatastoreIdentifier;
import org.datanucleus.store.rdbms.identifier.IdentifierFactory;
import org.datanucleus.store.rdbms.mapping.datastore.DatastoreMapping;
import org.datanucleus.store.rdbms.table.Column;
import org.datanucleus.store.rdbms.table.Table;

/**
 * Mapping class for mapping version state/timestamp columns in the database.
 * This class is for internal use only. It should not be used in user mappings.
 */
public class VersionMapping extends SingleFieldMapping
{
    private final JavaTypeMapping delegate;

    /**
     * Constructor.
     * @param table Datastore table
     * @param delegate The JavaTypeMapping to delegate the storage
     */
    public VersionMapping(Table table, JavaTypeMapping delegate)
    {
        initialize(table.getStoreManager(), delegate.getType());
        this.delegate = delegate;
        this.table = table;
        VersionMetaData vermd = table.getVersionMetaData();

        // Currently we only use a single column mapping for versioning.
        // The MetaData supports multiple columns and so we could extend this in the future
        // to use all MetaData information.
        ColumnMetaData versionColumnMetaData = vermd.getColumnMetaData();
        ColumnMetaData colmd;
        IdentifierFactory idFactory = table.getStoreManager().getIdentifierFactory();
        DatastoreIdentifier id = null;
        if (versionColumnMetaData == null)
        {
            // No column name so generate a default
            id = idFactory.newVersionFieldIdentifier();
            colmd = new ColumnMetaData();
            colmd.setName(id.getName());
            table.getVersionMetaData().setColumnMetaData(colmd);
        }
        else
        {
            // Column metadata defined
            colmd = versionColumnMetaData;
            if (colmd.getName() == null)
            {
                // No name defined so create one and set it
                id = idFactory.newVersionFieldIdentifier();
                colmd.setName(id.getName());
            }
            else
            {
                // Name defined so just generate identifier
                id = idFactory.newColumnIdentifier(colmd.getName());
            }
        }
        Column column = table.addColumn(getType(), id, this, colmd);
        table.getStoreManager().getMappingManager().createDatastoreMapping(delegate, column, getType());
    }

    /**
     * Accessor for whether to include this column in any fetch statement
     * @return Whether to include the column when fetching.
     */
    public boolean includeInFetchStatement()
    {
        return false;
    }

    /**
     * Accessor for the number of datastore fields.
     * @return Number of datastore fields.
     */
    public int getNumberOfDatastoreMappings()
    {
        return delegate.getNumberOfDatastoreMappings();
    }

    /**
     * Accessor for a datastore mapping.
     * @param index The mapping index
     * @return the datastore mapping
     */
    public DatastoreMapping getDatastoreMapping(int index)
    {
        return delegate.getDatastoreMapping(index);
    }

    /**
     * Accessor for the datastore mappings for this java type.
     * @return The datastore mapping(s)
     */
    public DatastoreMapping[] getDatastoreMappings()
    {
        return delegate.getDatastoreMappings();
    }

    /**
     * Method to add a datastore mapping.
     * @param datastoreMapping The mapping
     */
    public void addDatastoreMapping(DatastoreMapping datastoreMapping)
    {
        delegate.addDatastoreMapping(datastoreMapping);
    }

    /**
     * Accessor for the type represented here, returning the class itself
     * @return This class.
     */
    public Class getJavaType()
    {
        return VersionMapping.class;
    }

    /**
     * Mutator for the object in this column
     * @param ec execution context
     * @param ps The statement
     * @param exprIndex The indexes
     * @param value The value to set it to
     */
    public void setObject(ExecutionContext ec, PreparedStatement ps, int[] exprIndex, Object value)
    {
        delegate.setObject(ec, ps, exprIndex, value);
    }

    /**
     * Accessor for the object in this column
     * @param ec execution context
     * @param resultSet The ResultSet to get the value from
     * @param exprIndex The indexes
     * @return The object
     */
    public Object getObject(ExecutionContext ec, ResultSet resultSet, int[] exprIndex)
    {
        return delegate.getObject(ec, resultSet, exprIndex);
    }

    // These implementations extend VersionMapping so that we can have different query expression/literal for numeric and timestamp versions.

    /**
     * Version using a Timestamp delegate.
     */
    public final static class VersionTimestampMapping extends VersionMapping
    {
        public VersionTimestampMapping(Table table, JavaTypeMapping delegate)
        {
            super(table, delegate);
        }
    }

    /**
     * Version using a Long delegate.
     */
    public final static class VersionLongMapping extends VersionMapping
    {
        public VersionLongMapping(Table datastoreContainer, JavaTypeMapping delegate)
        {
            super(datastoreContainer, delegate);
        }
    }
}