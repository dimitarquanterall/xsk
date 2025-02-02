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
package com.sap.xsk.integration.tests.applications.deployment;

import com.sap.xsk.integration.tests.core.client.PublisherClient;
import com.sap.xsk.integration.tests.core.client.WorkspaceClient;
import com.sap.xsk.integration.tests.core.client.http.XSKHttpClient;
import com.sap.xsk.integration.tests.core.client.http.kyma.KymaXSKHttpClient;
import com.sap.xsk.integration.tests.core.client.http.local.LocalXSKHttpClient;
import org.apache.http.HttpResponse;
import org.eclipse.dirigible.commons.config.Configuration;

import java.net.URI;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class XSKProjectDeployer {

  private final WorkspaceClient workspaceClient;
  private final PublisherClient publisherClient;

  public XSKProjectDeployer(XSKProjectDeploymentType XSKProjectDeploymentType) {
    XSKHttpClient xskHttpClient = createXSKHttpClient(XSKProjectDeploymentType);
    this.workspaceClient = new WorkspaceClient(xskHttpClient);
    this.publisherClient = new PublisherClient(xskHttpClient);
  }

  public XSKHttpClient createXSKHttpClient(XSKProjectDeploymentType XSKProjectDeploymentType) {
    if (XSKProjectDeploymentType == com.sap.xsk.integration.tests.applications.deployment.XSKProjectDeploymentType.KYMA) {
      String host = Configuration.get("KYMA_HOST");
      var uri = URI.create(host);
      return KymaXSKHttpClient.create(uri);
    } else {
      var uri = URI.create("http://localhost:8080");
      return LocalXSKHttpClient.create(uri);
    }
  }

  public void deploy(String applicationName, Path applicationFolderPath) throws XSKProjectDeploymentException {
    try {
      deployAsync(applicationName, applicationFolderPath).get();
    } catch (InterruptedException | ExecutionException e) {
      String errorMessage = "Publish " + applicationName + " to " + applicationFolderPath + " failed";
      throw new XSKProjectDeploymentException(errorMessage, e);
    }
  }

  public CompletableFuture<HttpResponse> deployAsync(String projectName, Path projectFolderPath) throws XSKProjectDeploymentException {
    return workspaceClient.createWorkspace(projectName)
        .thenCompose(x -> workspaceClient.importProjectInWorkspace(projectName, projectName, projectFolderPath))
        .thenCompose(x -> publisherClient.publishProjectInWorkspace(projectName, projectName));
  }

  public void undeploy(String applicationName, String applicationFolderPath) {
    try {
      undeployAsync(applicationName).get();
    } catch (InterruptedException | ExecutionException e) {
      throw new XSKProjectDeploymentException("Unpublish " + applicationName + " from " + applicationFolderPath + " failed", e);
    }
  }

  public CompletableFuture<HttpResponse> undeployAsync(String projectName) throws XSKProjectDeploymentException {
    return publisherClient.unpublishProjectFromWorkspace(projectName, projectName)
        .thenCompose(x -> workspaceClient.deleteWorkspace(projectName));

  }


}
