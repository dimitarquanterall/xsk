/*
 * Copyright (c) 2022 SAP SE or an SAP affiliate company and XSK contributors
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License, v2.0
 * which accompanies this distribution, and is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * SPDX-FileCopyrightText: 2022 SAP SE or an SAP affiliate company and XSK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.sap.xsk.synchronizer;

import com.sap.xsk.XSJSTest;
import com.sap.xsk.engine.XSKJavascriptEngineExecutor;
import com.sap.xsk.exceptions.XSJSLibArtefactCleanerSQLException;
import com.sap.xsk.exceptions.XSJSLibExportsGenerationSourceNotFoundException;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.eclipse.dirigible.commons.api.scripting.ScriptingException;
import org.eclipse.dirigible.commons.config.Configuration;
import org.eclipse.dirigible.commons.config.StaticObjects;
import org.eclipse.dirigible.core.scheduler.api.ISchedulerCoreService;
import org.eclipse.dirigible.core.scheduler.service.definition.JobDefinition;
import org.eclipse.dirigible.core.test.AbstractDirigibleTest;
import org.eclipse.dirigible.engine.js.graalvm.debugger.GraalVMJavascriptDebugProcessor;
import org.eclipse.dirigible.engine.js.graalvm.processor.GraalVMJavascriptEngineExecutor;
import org.eclipse.dirigible.repository.api.IEntity;
import org.eclipse.dirigible.repository.api.IRepository;
import org.eclipse.dirigible.repository.local.LocalRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(JUnitParamsRunner.class)
public class ExportGenerationTest extends XSJSTest {

  @Before
  public void beforeTest() {
    cleanup();
  }

  @After
  public void afterTest() {
    cleanup();
  }

  private void cleanup() {
    dropTableIfExists(XSJSLibSynchronizer.XSJSLIB_SYNCHRONIZER_STATE_TABLE_NAME);
    dropTableIfExists("XSJSLIB_EXPORT_TEST_TABLE");
  }

  @Test
  public void synchronizerGetPriorityTest() {
    XSJSLibSynchronizer synchronizer = new XSJSLibSynchronizer();
    assertEquals("Unexpected XSJSLibSynchronizer Priority",
        666, synchronizer.getPriority());
  }

  @Test
  public void synchronizerJobTest() {
    XSJSLibSynchronizerJob job = new XSJSLibSynchronizerJob();

    assertEquals("Unexpected XSJSLib Job Name",
        "XSK XSJSLib Synchronizer Job", job.getName());

    assertEquals("Unexpected XSJSLib Job Synchronizer",
        XSJSLibSynchronizer.class, job.getSynchronizer().getClass());
  }

  @Test
  public void synchronizerJobDefinitionTest() {
    XSJSLibSynchronizerJob job = new XSJSLibSynchronizerJob();
    JobDefinition jobDefinition = job.getJobDefinition();

    assertEquals("Unexpected XSJSLib Job Definition Name",
        "dirigible-internal-xsk-xsjslib-synchronizer-job", jobDefinition.getName());

    assertEquals("Unexpected XSJSLib Job Definition Group",
        ISchedulerCoreService.JOB_GROUP_INTERNAL, jobDefinition.getGroup());

    assertEquals("Unexpected XSJSLib Job Definition Clazz",
        XSJSLibSynchronizerJob.class.getCanonicalName(), jobDefinition.getClazz());

    assertEquals("Unexpected XSJSLib Job Definition Description",
        "XSK XSJSLib Synchronizer Job", jobDefinition.getDescription());

    assertEquals("Unexpected XSJSLib Job Definition Expression",
        "0/55 * * * * ?", jobDefinition.getExpression());

    assertTrue("Unexpected XSJSLib Job Definition Singleton Flag",
        jobDefinition.isSingleton());
  }

  @Test
  public void artefactCleanerCreateAndCleanupStateTableTest() throws SQLException {
    // Run a script that creates a table with an Entry(location: "testFolder/abc.xsjslib", hash: "abc");
    runJs("/test/xsk/exports/utils/createTableHelper.mjs");

    String testFolder = "testFolder/";
    XSJSLibSynchronizerArtefactsCleaner cleaner = new XSJSLibSynchronizerArtefactsCleaner();
    cleaner.cleanup(testFolder);

    DataSource dataSource = (DataSource) StaticObjects.get(StaticObjects.SYSTEM_DATASOURCE);
    try (PreparedStatement selectStatement = dataSource.getConnection()
        .prepareStatement(
            "SELECT FROM \""
                + XSJSLibSynchronizer.XSJSLIB_SYNCHRONIZER_STATE_TABLE_NAME
                + "\" WHERE \"LOCATION\" LIKE '" + testFolder + "'")
    ) {
      ResultSet result = selectStatement.executeQuery();
      assertNotNull("Unexpected null result set after state table cleanup", result);

      result.last();
      int entries = result.getRow();
      assertEquals("Unexpected count of entries after state table cleanup", 0, entries);
    }
  }

  @Test
  @Parameters({
      "/test/xsk/exports/stateTableWriteTest.mjs",
      "/test/xsk/exports/stateTableUpdateTest.mjs",
      "/test/xsk/exports/stateTableFindTest.mjs",
      "/test/xsk/exports/contentChangeCheckTest.mjs",
      "/test/xsk/exports/contentModifierTest.mjs",
      "/test/xsk/exports/singleFileExportGenerationTest.mjs",
      "/test/xsk/exports/singleFileExportUpdateTest.mjs",
      "/test/xsk/exports/multiFileExportGenerationTest.mjs",
  })
  public void exportsGeneratorTest(String testModule) throws ScriptingException {
    runJsTest(testModule);
  }

  @Test
  public void importTest() throws ScriptingException {
    XSJSLibSynchronizer.forceSynchronization("../../test/xsk/import/"); // look two directories back as the test resources are outside the repository root

    Map<Object, Object> context = new HashMap<>();
    XSKJavascriptEngineExecutor xskJavascriptEngineExecutor = new XSKJavascriptEngineExecutor();
    Object result = xskJavascriptEngineExecutor.executeServiceModule(
        "/test/xsk/import/import.xsjs",
        context
    );

    assertNull("Unexpected xsjs execution result for import.xsjs", result);
  }

  private void runJsTest(String testModule) {
    Object executionResult = runJs(testModule);
    assertNull(
        "XSJSLib Export js test unexpected js execution result for " + testModule,
        executionResult
    );
  }

  private Object runJs(String testModule) throws ScriptingException {
    Map<Object, Object> context = new HashMap<>();
    GraalVMJavascriptEngineExecutor graalVMJavascriptEngineExecutor = new GraalVMJavascriptEngineExecutor();
    return graalVMJavascriptEngineExecutor.executeService(
        testModule,
        context,
        true,
        false
    );
  }

  private void dropTableIfExists(String tableName) {
    DataSource dataSource = (DataSource) StaticObjects.get(StaticObjects.SYSTEM_DATASOURCE);
    try (Connection connection = dataSource.getConnection();
        PreparedStatement dropStatement = connection.prepareStatement(
            "DROP TABLE \"" + tableName + "\""
        )
    ) {
      if (tableExists(tableName, connection)) {
        dropStatement.executeUpdate();
      }
    } catch (SQLException e) {
      throw new XSJSLibArtefactCleanerSQLException("Could not drop table after test", e);
    }
  }

  private boolean tableExists(String tableName, Connection connection) throws SQLException {
    ResultSet resultSet = connection.getMetaData().getTables(
        null,
        null,
        tableName,
        null
    );
    return resultSet.next();
  }
}
