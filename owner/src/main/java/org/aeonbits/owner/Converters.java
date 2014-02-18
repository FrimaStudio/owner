/*
 * Copyright (c) 2013, Luigi R. Viggiano
 * All rights reserved.
 *
 * This software is distributable under the BSD license.
 * See the terms of the BSD license in the documentation provided with this software.
 */

package org.aeonbits.owner;

import static java.lang.reflect.Modifier.isStatic;
import static org.aeonbits.owner.Util.expandUserHome;
import static org.aeonbits.owner.Util.unreachableButCompilerNeedsThis;
import static org.aeonbits.owner.Util.unsupported;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.aeonbits.owner.Config.ConverterClass;

/**
 * Converter class from {@link java.lang.String} to property types.
 *
 * @author Luigi R. Viggiano
 */
enum Converters {

    ARRAY {
        @Override
        Object tryConvert(Method targetMethod, Class<?> targetType, Object value) {
            if (!targetType.isArray())
                return null;

            if (value.getClass().isArray())
                return value;

            if (value instanceof String) {
                String text = (String) value;
                Class<?> type = targetType.getComponentType();

                if (text.trim().isEmpty())
                    return Array.newInstance(type, 0);

                Tokenizer tokenizer = TokenizerResolver.resolveTokenizer(targetMethod);
                String[] chunks = tokenizer.tokens(text);

                Converters converter = doConvert(targetMethod, type, chunks[0]).getConverter();
                Object result = Array.newInstance(type, chunks.length);

                for (int i = 0; i < chunks.length; i++) {
                    String chunk = chunks[i];
                    Object item = converter.tryConvert(targetMethod, type, chunk);
                    Array.set(result, i, item);
                }

                return result;
            }

            return null;
        }
    },

    METHOD_WITH_CONVERTER_CLASS_ANNOTATION {
        @Override
        Object tryConvert(Method targetMethod, Class<?> targetType, Object value) {
            ConverterClass annotation = targetMethod.getAnnotation(ConverterClass.class);
            if (annotation == null)
                return null;

            Class<? extends Converter<?>> converterClass = annotation.value();
            Converter<?> converter;
            try {
                converter = converterClass.newInstance();
            } catch (InstantiationException e) {
                throw unsupported(e, "Converter class %s can't be instantiated: %s", converterClass.getCanonicalName(),
                        e.getMessage());
            } catch (IllegalAccessException e) {
                throw unsupported(e, "Converter class %s can't be accessed: %s", converterClass.getCanonicalName(),
                        e.getMessage());
            }
            Object result = converter.convert(targetMethod, value);
            if (result == null)
                return NULL;
            return result;
        }
    },

    COLLECTION {
        @Override
        Object tryConvert(Method targetMethod, Class<?> targetType, Object value) {
            if (!Collection.class.isAssignableFrom(targetType))
                return null;

            Collection<Object> collection;

            if (value.getClass().isArray() || List.class.isAssignableFrom(value.getClass())) {
                collection = Arrays.asList(value);
            } else if (value instanceof String) {
                collection = Arrays.asList(convertToArray(targetMethod, (String) value));
            } else {
                return null;
            }

            Collection<Object> result = instantiateCollection(targetType);
            result.addAll(collection);
            return result;
        }

        private Object[] convertToArray(Method targetMethod, String text) {
            Class<?> type = getGenericType(targetMethod);
            Object stub = Array.newInstance(type, 0);
            return (Object[]) ARRAY.tryConvert(targetMethod, stub.getClass(), text);
        }

        private Class<?> getGenericType(Method targetMethod) {
            if (targetMethod.getGenericReturnType() instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) targetMethod.getGenericReturnType();
                return (Class<?>) parameterizedType.getActualTypeArguments()[0];
            }
            // Default generic type for raw collections.
            return String.class;
        }

        private <T> Collection<T> instantiateCollection(Class<? extends T> targetType) {
            if (targetType.isInterface())
                return instantiateCollectionFromInterface(targetType);
            return instantiateCollectionFromClass(targetType);
        }

        @SuppressWarnings("unchecked")
        private <T> Collection<T> instantiateCollectionFromClass(Class<? extends T> targetType) {
            try {
                return (Collection<T>) targetType.newInstance();
            } catch (Exception e) {
                throw unsupported(e, "Cannot instantiate collection of type '%s'", targetType.getCanonicalName());
            }
        }

        private <T> Collection<T> instantiateCollectionFromInterface(Class<? extends T> targetType) {
            if (List.class.isAssignableFrom(targetType))
                return new ArrayList<T>();
            else if (SortedSet.class.isAssignableFrom(targetType))
                return new TreeSet<T>();
            else if (Set.class.isAssignableFrom(targetType))
                return new LinkedHashSet<T>();
            return new ArrayList<T>();
        }
    },

    PROPERTY_EDITOR {
        @Override
        Object tryConvert(Method targetMethod, Class<?> targetType, Object value) {
            if (!(value instanceof String))
                return null;

            PropertyEditor editor = PropertyEditorManager.findEditor(targetType);

            if (editor == null)
                return null;

            editor.setAsText((String) value);
            return editor.getValue();
        }
    },

    FILE {
        @Override
        Object tryConvert(Method targetMethod, Class<?> targetType, Object value) {
            if (!(value instanceof String))
                return null;

            if (targetType != File.class)
                return null;

            return new File(expandUserHome((String) value));
        }
    },

    CLASS {
        @Override
        Object tryConvert(Method targetMethod, Class<?> targetType, Object value) {
            if (targetType != Class.class)
                return null;

            if (!(value instanceof String))
                return null;

            String className = (String) value;

            try {
                return Class.forName(className);
            } catch (ClassNotFoundException ex) {
                throw unsupported(ex, "Cannot convert '%s' to %s", className, targetType.getCanonicalName());
            }
        }
    },

    CLASS_WITH_STRING_CONSTRUCTOR {
        @Override
        Object tryConvert(Method targetMethod, Class<?> targetType, Object value) {
            if (!(value instanceof String))
                return null;

            try {
                Constructor<?> constructor = targetType.getConstructor(String.class);
                return constructor.newInstance((String) value);
            } catch (Exception e) {
                return null;
            }
        }
    },

    CLASS_WITH_VALUE_OF_METHOD {
        @Override
        Object tryConvert(Method targetMethod, Class<?> targetType, Object value) {
            try {
                Method method = targetType.getMethod("valueOf", String.class);
                if (isStatic(method.getModifiers()))
                    return method.invoke(null, value);
                return null;
            } catch (Exception e) {
                return null;
            }
        }
    },

    CLASS_WITH_OBJECT_CONSTRUCTOR {
        @Override
        Object tryConvert(Method targetMethod, Class<?> targetType, Object value) {
            try {
                Constructor<?> constructor = targetType.getConstructor(Object.class);
                return constructor.newInstance(value);
            } catch (Exception e) {
                return null;
            }
        }
    },

    UNSUPPORTED {
        @Override
        Object tryConvert(Method targetMethod, Class<?> targetType, Object value) {
            throw unsupportedConversion(targetType, value);
        }
    };

    abstract Object tryConvert(Method targetMethod, Class<?> targetType, Object value);

    static Object convert(Method targetMethod, Class<?> targetType, Object value) {
        return doConvert(targetMethod, targetType, value).getConvertedValue();
    }

    private static ConversionResult doConvert(Method targetMethod, Class<?> targetType, Object text) {
        for (Converters converter : values()) {
            Object convertedValue = converter.tryConvert(targetMethod, targetType, text);
            if (convertedValue != null)
                return new ConversionResult(converter, convertedValue);
        }
        return unreachableButCompilerNeedsThis();
    }

    private static UnsupportedOperationException unsupportedConversion(Class<?> targetType, Object value) {
        return unsupported("Cannot convert '%s' to %s", value, targetType.getCanonicalName());
    }

    private static class ConversionResult {
        private final Converters converter;
        private final Object convertedValue;

        public ConversionResult(Converters converter, Object convertedValue) {
            this.converter = converter;
            this.convertedValue = convertedValue;
        }

        public Converters getConverter() {
            return converter;
        }

        public Object getConvertedValue() {
            return convertedValue;
        }
    }

    /**
     * The NULL object: when tryConvert returns this object, the conversion result is null.
     */
    static final Object NULL = new Object();

}
