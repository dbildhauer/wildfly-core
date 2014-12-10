/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 */
package org.jboss.as.controller.operations.validation;

import java.util.HashSet;
import java.util.List;

import org.jboss.as.controller.logging.ControllerLogger;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;

/**
 * Validates parameters of type {@link ModelType#LIST}.
 *
 * @author Brian Stansberry (c) 2011 Red Hat Inc.
 *
 */
public class ListValidator extends ModelTypeValidator implements ParameterValidator {

    private final int min;
    private final int max;
    private final ParameterValidator elementValidator;
    private final boolean allowDuplicates;

    /**
     * Constructs a new {@code ListValidator}
     *
     * @param elementValidator validator for list elements
     */
    public ListValidator(ParameterValidator elementValidator) {
        this(elementValidator, false, 1, Integer.MAX_VALUE, false);
    }

    /**
     * @param elementValidator validator for list elements
     * @param nullable {@code true} if the model node for the list can be {@code null} or {@link ModelType#UNDEFINED}
     */
    public ListValidator(ParameterValidator elementValidator, boolean nullable) {
        this(elementValidator, nullable, 1, Integer.MAX_VALUE, false);
    }
/**
     * @param elementValidator validator for list elements
     * @param nullable {@code true} if the model node for the list can be {@code null} or {@link ModelType#UNDEFINED}
     */
    public ListValidator(ParameterValidator elementValidator, boolean nullable,boolean allowDuplicates) {
        this(elementValidator, nullable, 1, Integer.MAX_VALUE, allowDuplicates);
    }

    /**
     * @param elementValidator validator for list elements
     * @param nullable         {@code true} if the model node for the list can be {@code null} or {@link ModelType#UNDEFINED}
     * @param minSize          minimum number of elements in the list
     * @param maxSize          maximum number of elements in the list
     */
    public ListValidator(ParameterValidator elementValidator, boolean nullable, int minSize, int maxSize) {
        this(elementValidator, nullable, minSize, maxSize, true);
    }

    /**
     * @param elementValidator validator for list elements
     * @param nullable         {@code true} if the model node for the list can be {@code null} or {@link ModelType#UNDEFINED}
     * @param minSize          minimum number of elements in the list
     * @param maxSize          maximum number of elements in the list
     * @param allowDuplicates  validate duplicates in list or not
     */
    public ListValidator(ParameterValidator elementValidator, boolean nullable, int minSize, int maxSize, boolean allowDuplicates) {
        super(ModelType.LIST, nullable, false, true);
        if (elementValidator == null) { throw ControllerLogger.ROOT_LOGGER.nullVar("elementValidator"); }
        this.min = minSize;
        this.max = maxSize;
        this.elementValidator = elementValidator;
        this.allowDuplicates = allowDuplicates;
    }

    @Override
    public void validateParameter(String parameterName, ModelNode value) throws OperationFailedException {
        super.validateParameter(parameterName, value);
        if (value.isDefined()) {
            List<ModelNode> list = value.asList();
            int size = list.size();
            if (size < min) {
                throw new OperationFailedException(ControllerLogger.ROOT_LOGGER.invalidMinSize(size, parameterName, min));
            }
            else if (size > max) {
                throw new OperationFailedException(ControllerLogger.ROOT_LOGGER.invalidMaxSize(size, parameterName, max));
            }
            else {
                if (!allowDuplicates){
                    HashSet<ModelNode> dups = new HashSet<>();
                    for (ModelNode element : list) {
                        if (!dups.add(element)){
                            throw new OperationFailedException(ControllerLogger.ROOT_LOGGER.duplicateElementsInList(parameterName));
                        }
                    }
                    dups.clear();
                }
                for (ModelNode element : list) {
                    elementValidator.validateParameter(parameterName, element);
                }
            }
        }
    }

}
