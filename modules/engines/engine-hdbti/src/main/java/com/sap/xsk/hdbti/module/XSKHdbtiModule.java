/*
 * Copyright (c) 2021 SAP SE or an SAP affiliate company and XSK contributors
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License, v2.0
 * which accompanies this distribution, and is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * SPDX-FileCopyrightText: 2021 SAP SE or an SAP affiliate company and XSK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.sap.xsk.hdbti.module;

import org.eclipse.dirigible.commons.api.module.AbstractDirigibleModule;
import org.eclipse.dirigible.commons.config.StaticObjects;

import com.sap.xsk.hdbti.dao.XSKCSVRecordDao;
import com.sap.xsk.hdbti.dao.XSKCsvToHdbtiRelationDao;
import com.sap.xsk.hdbti.dao.XSKImportedCSVRecordDao;
import com.sap.xsk.hdbti.dao.XSKTableImportArtifactDao;
import com.sap.xsk.hdbti.processors.XSKHDBTIProcessor;
import com.sap.xsk.hdbti.service.XSKHDBTICoreService;
import com.sap.xsk.hdbti.service.XSKTableImportParser;
import com.sap.xsk.hdbti.transformer.XSKTableImportArtifactFactory;
import com.sap.xsk.parser.hdbti.custom.XSKHDBTIParser;

public class XSKHdbtiModule extends AbstractDirigibleModule {

    @Override
    public void configure() {
    	
    	StaticObjects.set("xskcsvRecordDao", new XSKCSVRecordDao());
    	StaticObjects.set("xskCsvToHdbtiRelationDao", new XSKCsvToHdbtiRelationDao());
    	StaticObjects.set("xskImportedCSVRecordDao", new XSKImportedCSVRecordDao());
    	StaticObjects.set("xskHdbtiProcessor", new XSKHDBTIProcessor());
    	StaticObjects.set("xskHdbtiParser", new XSKHDBTIParser());
    	StaticObjects.set("xskTableImportArtifactFactory", new XSKTableImportArtifactFactory());
    	StaticObjects.set("xskTableImportParser", new XSKTableImportParser());
    	StaticObjects.set("xskTableImportArtifactDao", new XSKTableImportArtifactDao());
    	StaticObjects.set("xskHdbtiCoreService", new XSKHDBTICoreService());
    	
//        bind(IXSKCSVRecordDao.class).annotatedWith(Names.named("xskcsvRecordDao")).to(XSKCSVRecordDao.class).in(Scopes.SINGLETON);
//        bind(IXSKCsvToHdbtiRelationDao.class).annotatedWith(Names.named("xskCsvToHdbtiRelationDao")).to(XSKCsvToHdbtiRelationDao.class).in(Scopes.SINGLETON);
//        bind(IXSKImportedCSVRecordDao.class).annotatedWith(Names.named("xskImportedCSVRecordDao")).to(XSKImportedCSVRecordDao.class).in(Scopes.SINGLETON);
//        bind(IXSKHDBTIProcessor.class).annotatedWith(Names.named("xskHdbtiProcessor")).to(XSKHDBTIProcessor.class).in(Scopes.SINGLETON);
//        bind(IXSKHDBTIParser.class).annotatedWith(Names.named("xskHdbtiParser")).to(XSKHDBTIParser.class).in(Scopes.SINGLETON);
//        bind(IXSKTableImportArtifactFactory.class).annotatedWith(Names.named("xskTableImportArtifactFactory")).to(XSKTableImportArtifactFactory.class).in(Scopes.SINGLETON);
//        bind(IXSKTableImportParser.class).annotatedWith(Names.named("xskTableImportParser")).to(XSKTableImportParser.class).in(Scopes.SINGLETON);
//        bind(IXSKTableImportArtifactDao.class).annotatedWith(Names.named("xskTableImportArtifactDao")).to(XSKTableImportArtifactDao.class).in(Scopes.SINGLETON);
//        bind(IXSKHDBTICoreService.class).annotatedWith(Names.named("xskHdbtiCoreService")).to(XSKHDBTICoreService.class).in(Scopes.SINGLETON);
    }

    @Override
    public String getName() {
        return "XSK Hdbti Module";
    }
}
