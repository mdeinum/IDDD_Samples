//   Copyright 2012,2013 Vaughn Vernon
//
//   Licensed under the Apache License, Version 2.0 (the "License");
//   you may not use this file except in compliance with the License.
//   You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.

package com.saasovation.common.port.adapter.persistence;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ResultSetObjectMapper<T> {

    private String columnPrefix;
    private final JoinOn joinOn;
    private final ResultSet resultSet;
    private final Class<? extends T> resultType;

    public ResultSetObjectMapper(
            ResultSet aResultSet,
            Class<T> aResultType,
            JoinOn aJoinOn) {

        super();

        this.joinOn = aJoinOn;
        this.resultSet = aResultSet;
        this.resultType = aResultType;
    }

    public ResultSetObjectMapper(
            ResultSet aResultSet,
            Class<? extends T> aResultType,
            String aColumnPrefix,
            JoinOn aJoinOn) {

        super();

        this.columnPrefix = aColumnPrefix;
        this.joinOn = aJoinOn;
        this.resultSet = aResultSet;
        this.resultType = aResultType;
    }

    public T mapResultToType() {
        T object = this.createFrom(this.resultType());

        Set<String> associationsToMap = new HashSet<String>();

        Field[] fields = this.resultType().getDeclaredFields();

        for (Field field : fields) {
            String columnName = this.fieldNameToColumnName(field.getName());

            if (this.hasColumn(this.resultSet(), columnName)) {
                Object columnValue = this.columnValueFrom(columnName, field.getType());

                this.joinOn().saveCurrentLeftQualifier(columnName, columnValue);

                try {
                    field.setAccessible(true);
                    field.set(object, columnValue);
                } catch (Exception e) {
                    throw new IllegalStateException(
                            "Cannot map to: "
                            + this.resultType.getSimpleName()
                            + "#"
                            + columnName);
                }
            } else {
                String objectPrefix = this.toObjectPrefix(columnName);

                if (!associationsToMap.contains(objectPrefix) &&
                        this.hasAssociation(this.resultSet(), objectPrefix)) {

                    associationsToMap.add(field.getName());
                }
            }
        }

        if (!associationsToMap.isEmpty()) {
            this.mapAssociations(object, this.resultSet(), associationsToMap);
        }

        return object;
    }

    private String columnPrefix() {
        return this.columnPrefix;
    }

    private boolean hasColumnPrefix() {
        return this.columnPrefix != null;
    }

    private Object columnValueFrom(String aColumnName, Class<?> aType) {

        Object value = null;
        String tempStr = null;

        try {
            String typeName = aType.getName();

            if (aType.isPrimitive()) {

                // boolean, byte, char, short, int, long, float, double

              switch (typeName) {
                case "int" -> value = this.resultSet().getInt(aColumnName);
                case "long" -> value = this.resultSet().getLong(aColumnName);
                case "boolean" -> {
                  int oneOrZero = this.resultSet().getInt(aColumnName);
                  value = oneOrZero == 1 ? Boolean.TRUE : Boolean.FALSE;
                }
                case "short" -> value = this.resultSet().getShort(aColumnName);
                case "float" -> value = this.resultSet().getFloat(aColumnName);
                case "double" -> value = this.resultSet().getDouble(aColumnName);
                case "byte" -> value = this.resultSet().getByte(aColumnName);
                case "char" -> {
                  String charStr = this.resultSet().getString(aColumnName);
                  if (charStr == null) {
                    value = (char) 0;
                  } else {
                    value = charStr.charAt(0);
                  }
                }
              }

            } else {
              switch (typeName) {
                case "java.lang.String" -> value = this.resultSet().getString(aColumnName);
                case "java.lang.Integer" -> {
                  tempStr = this.resultSet().getString(aColumnName);
                  value = tempStr == null ? null : Integer.parseInt(tempStr);
                }
                case "java.lang.Long" -> {
                  tempStr = this.resultSet().getString(aColumnName);
                  value = tempStr == null ? null : Long.parseLong(tempStr);
                }
                case "java.lang.Boolean" -> {
                  int oneOrZero = this.resultSet().getInt(aColumnName);
                  value = oneOrZero == 1 ? Boolean.TRUE : Boolean.FALSE;
                }
                case "java.util.Date" -> {
                  java.sql.Timestamp timestamp = this.resultSet().getTimestamp(aColumnName);
                  if (timestamp != null) {
                    value = new java.util.Date(timestamp.getTime() + timestamp.getNanos());
                  }
                }
                case "java.lang.Short" -> {
                  tempStr = this.resultSet().getString(aColumnName);
                  value = tempStr == null ? null : Short.parseShort(tempStr);
                }
                case "java.lang.Float" -> {
                  tempStr = this.resultSet().getString(aColumnName);
                  value = tempStr == null ? null : Float.parseFloat(tempStr);
                }
                case "java.lang.Double" -> {
                  tempStr = this.resultSet().getString(aColumnName);
                  value = tempStr == null ? null : Double.parseDouble(tempStr);
                }
              }
            }

        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Cannot map "
                            + aColumnName
                            + " because: "
                            + e.getMessage(),
                    e);
        }

        return value;
    }

    private Collection<Object> createCollectionFrom(Class<?> aType) {

        Collection<Object> newCollection = null;

        if (List.class.isAssignableFrom(aType)) {
            newCollection = new ArrayList<>();
        } else if (Set.class.isAssignableFrom(aType)) {
            newCollection = new HashSet<>();
        }

        return newCollection;
    }

    private T createFrom(Class<? extends T> aClass) {
        try {
            Constructor<? extends T> ctor = aClass.getDeclaredConstructor();
            return ctor.newInstance();

        } catch (Exception e) {
            throw new IllegalArgumentException("Cannot create instance of: " + aClass.getName());
        }
    }

    private String fieldNameToColumnName(String aFieldName) {
        StringBuilder buf = new StringBuilder();

        if (this.hasColumnPrefix()) {
            buf.append(this.columnPrefix());
        }

        for (char ch : aFieldName.toCharArray()) {
            if (Character.isAlphabetic(ch) && Character.isUpperCase(ch)) {
                buf.append('_').append(Character.toLowerCase(ch));
            } else {
                buf.append(ch);
            }
        }

        return buf.toString();
    }

    private boolean hasAssociation(
            ResultSet aResultSet,
            String anObjectPrefix) {

        try {
            ResultSetMetaData metaData = aResultSet.getMetaData();
            int totalColumns = metaData.getColumnCount();

            for (int idx = 1; idx <= totalColumns; ++idx) {
                String columnName = metaData.getColumnLabel(idx);

                if (columnName.startsWith(anObjectPrefix) &&
                        this.joinOn().isJoinedOn(aResultSet)) {

                    return true;
                }
            }

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Cannot read result metadata because: "
                        + e.getMessage(),
                    e);
        }

        return false;
    }

    private boolean hasColumn(ResultSet aResultSet, String aColumnName) {
        try {
            aResultSet.findColumn(aColumnName);

            return true;

        } catch (Exception e) {
            return false;
        }
    }

    private JoinOn joinOn() {
        return this.joinOn;
    }

    private void mapAssociations(
            T anObject,
            ResultSet aResultSet,
            Set<String> anAssociationsToMap) {

        Map<String, Collection<Object>> mappedCollections = new HashMap<>();

        String currentAssociationName = null;

        try {
            for (boolean hasResult = true; hasResult; hasResult = aResultSet.next()) {

                if (!this.joinOn().hasCurrentLeftQualifier(aResultSet)) {
                    aResultSet.relative(-1);

                    return;
                }

                for (String fieldName : anAssociationsToMap) {

                    currentAssociationName = fieldName;

                    Field associationField = anObject.getClass().getDeclaredField(fieldName);

                    associationField.setAccessible(true);

                    Class<?> associationFieldType;

                    Collection<Object> collection = null;

                    if (Collection.class.isAssignableFrom(associationField.getType())) {
                        collection = mappedCollections.get(fieldName);

                        if (collection == null) {
                            collection = this.createCollectionFrom(associationField.getType());
                            mappedCollections.put(fieldName, collection);
                            associationField.set(anObject, collection);
                        }

                        ParameterizedType parameterizeType = (ParameterizedType) associationField.getGenericType();
                        associationFieldType = (Class<?>) parameterizeType.getActualTypeArguments()[0];

                    } else {
                        associationFieldType = associationField.getType();
                    }

                    String columnName = this.fieldNameToColumnName(fieldName);

                    ResultSetObjectMapper<Object> mapper =
                        new ResultSetObjectMapper<>(
                            aResultSet,
                            associationFieldType,
                            this.toObjectPrefix(columnName),
                            this.joinOn());

                    Object associationObject = mapper.mapResultToType();

                    if (collection != null) {
                        collection.add(associationObject);
                    } else {
                        associationField.set(anObject, associationObject);
                    }
                }
            }

        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Cannot map object association for "
                            + currentAssociationName
                            + " because: "
                            + e.getMessage(),
                    e);
        }
    }

    private ResultSet resultSet() {
        return this.resultSet;
    }

    private Class<? extends T> resultType() {
        return this.resultType;
    }

    private String toObjectPrefix(String aColumnName) {

      return "o_"+aColumnName+"_";
    }
}
