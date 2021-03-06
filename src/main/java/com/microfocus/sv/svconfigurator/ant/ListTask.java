/*
 *  Certain versions of software and/or documents ("Material") accessible here may contain branding from
 *  Hewlett-Packard Company (now HP Inc.) and Hewlett Packard Enterprise Company.  As of September 1, 2017,
 *  the Material is now offered by Micro Focus, a separately owned and operated company.  Any reference to the HP
 *  and Hewlett Packard Enterprise/HPE marks is historical in nature, and the HP and Hewlett Packard Enterprise/HPE
 *  marks are the property of their respective owners.
 * __________________________________________________________________
 * MIT License
 *
 * Copyright (c) 2012-2018 Micro Focus or one of its affiliates.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors ("Micro Focus") are set forth in the express warranty statements
 * accompanying such products and services. Nothing herein should be construed as
 * constituting an additional warranty. Micro Focus shall not be liable for technical
 * or editorial errors or omissions contained herein.
 * The information contained herein is subject to change without notice.
 * __________________________________________________________________
 *
 */
package com.microfocus.sv.svconfigurator.ant;

import java.io.File;
import java.net.URL;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.microfocus.sv.svconfigurator.LogConf;
import com.microfocus.sv.svconfigurator.core.IProject;
import com.microfocus.sv.svconfigurator.core.impl.exception.CommunicatorException;
import com.microfocus.sv.svconfigurator.core.impl.jaxb.atom.ServiceListAtom;
import com.microfocus.sv.svconfigurator.core.impl.processor.Credentials;
import com.microfocus.sv.svconfigurator.serverclient.ICommandExecutor;
import com.microfocus.sv.svconfigurator.serverclient.impl.CommandExecutor;
import com.microfocus.sv.svconfigurator.util.AntTaskUtil;
import com.microfocus.sv.svconfigurator.util.HttpUtils;
import com.microfocus.sv.svconfigurator.util.OutputUtil;
import com.microfocus.sv.svconfigurator.util.ProjectUtils;

/**
 * Ant task to list the services
 */
public class ListTask extends Task {
    //============================== STATIC ATTRIBUTES ========================================

    private static final Logger LOG = LoggerFactory.getLogger(ListTask.class);

    //============================== INSTANCE ATTRIBUTES ======================================
    private File projectFile;
    private String username;
    private String password;
    private String url;
    private String projectPassword;

    //============================== STATIC METHODS ===========================================

    //============================== CONSTRUCTORS =============================================

    //============================== ABSTRACT METHODS =========================================

    //============================== OVERRIDEN METHODS ========================================

    @Override
    public void execute() throws BuildException {
        LogConf.configure();
        this.validate();

        URL mgmtUri = AntTaskUtil.createUri(this.url, null);
        Credentials cred = ProjectUtils.createCredentials(this.username, this.password);

        try {
            ICommandExecutor exec = new CommandExecutor(HttpUtils.createServerManagementEndpointClient(mgmtUri, cred));

            IProject proj = this.projectFile != null ? AntTaskUtil.createProject(this.projectFile, projectPassword) : null;

            ServiceListAtom atom = exec.getServiceList(proj == null ? null : proj.getId());
            LOG.info(OutputUtil.createServiceListOutput(atom));
        } catch (CommunicatorException e) {
            throw new BuildException(e.getLocalizedMessage(), e);
        }
    }


    //============================== INSTANCE METHODS =========================================

    //============================== PRIVATE METHODS ==========================================

    private void validate() throws BuildException {
        if (url == null) {
            throw new BuildException("Server management URL (url) have to be set.");
        }
    }

    //============================== GETTERS / SETTERS ========================================

    public void setProjectFile(File projectFile) {
        this.projectFile = projectFile;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setProjectPassword(String projectPassword) {
        this.projectPassword = projectPassword;
    }

    //============================== INNER CLASSES ============================================

}
