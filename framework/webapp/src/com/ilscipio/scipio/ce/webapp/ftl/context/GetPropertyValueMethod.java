/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *******************************************************************************/
package com.ilscipio.scipio.ce.webapp.ftl.context;

import java.util.List;

import org.ofbiz.base.util.UtilProperties;

import com.ilscipio.scipio.ce.webapp.ftl.lang.LangFtlUtil;

import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateScalarModel;

/**
 * SCIPIO: GetPropertyValueMethod - Freemarker Method for getting properties
 */
public class GetPropertyValueMethod implements TemplateMethodModelEx {

    //private static final Debug.OfbizLogger module = Debug.getOfbizLogger(java.lang.invoke.MethodHandles.lookup().lookupClass());

    /*
     * @see freemarker.template.TemplateMethodModel#exec(java.util.List)
     */
    @Override
    public Object exec(@SuppressWarnings("rawtypes") List args) throws TemplateModelException {
        if (args == null || args.size() != 2)
            throw new TemplateModelException("Invalid number of arguements");
        if (!(args.get(0) instanceof TemplateScalarModel))
            throw new TemplateModelException("First argument not an instance of TemplateScalarModel (string)");
        if (!(args.get(1) instanceof TemplateScalarModel))
            throw new TemplateModelException("Second argument not an instance of TemplateScalarModel (string)");

        String resource = LangFtlUtil.getAsStringNonEscaping(((TemplateScalarModel) args.get(0)));
        String name = LangFtlUtil.getAsStringNonEscaping(((TemplateScalarModel) args.get(1)));
        
        String res = UtilProperties.getPropertyValue(resource, name);

        // here we do opposite of UtilProperties and return null if empty, so ! operator can work
        if (res != null && res.isEmpty()) {
            res = null;
        }
        // Prevent escaping by EscapingObjectWrapper - values from properties are always coded
        // by trusted people
        return LangFtlUtil.wrapNonEscaping(res);
    }

}
