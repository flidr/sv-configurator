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
package com.microfocus.sv.svconfigurator.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.microfocus.sv.svconfigurator.core.IDataModel;
import com.microfocus.sv.svconfigurator.core.IPerfModel;
import com.microfocus.sv.svconfigurator.core.IService;
import com.microfocus.sv.svconfigurator.core.impl.exception.CommandExecutorException;
import com.microfocus.sv.svconfigurator.core.impl.jaxb.ServiceRuntimeConfiguration;
import com.microfocus.sv.svconfigurator.core.impl.jaxb.ServiceRuntimeReport;
import com.microfocus.sv.svconfigurator.core.impl.jaxb.atom.ServiceListAtom;

public class OutputUtil {
    //============================== STATIC ATTRIBUTES ========================================

    private static final String TABLE_FORMAT_INFO = "  %-25s %s\n";
    private static final String TABLE_FORMAT_REPORT = "  %-27s %s\n";
    private static final String MODEL_NONE = "None";

    //============================== INSTANCE ATTRIBUTES ======================================

    //============================== STATIC METHODS ===========================================

    public static String createServiceInfoOutput(IService svc, ServiceRuntimeConfiguration conf) throws CommandExecutorException {
        StringBuilder sb = new StringBuilder();
        sb.append("Service Runtime Information:\n");
        sb.append(String.format(TABLE_FORMAT_INFO, "Service Name", svc.getName()));
        sb.append(String.format(TABLE_FORMAT_INFO, "Service ID", conf.getServiceId()));
        sb.append(String.format(TABLE_FORMAT_INFO, "Runtime Mode", conf.getDisplayRuntimeMode()));
        sb.append(String.format(TABLE_FORMAT_INFO, "Runtime Issue(s)", svc.getRuntimeIssues()));
        sb.append(String.format(TABLE_FORMAT_INFO, "Client Lock", conf.getClientId() == null ? "" : conf.getClientId()));

        String dmId = conf.getDataModelId();
        String dmName = dmId == null ? MODEL_NONE : ProjectUtils.findProjElem(svc.getDataModels(), dmId,
                ProjectUtils.ENTITY_DATA_MODEL).getName();
        sb.append(String.format(TABLE_FORMAT_INFO, "Data Model Name", dmName));
        sb.append(String.format(TABLE_FORMAT_INFO, "Data Model ID", dmId == null ? MODEL_NONE : dmId));

        String pmId = conf.getPerfModelId();
        String pmName = pmId == null ? MODEL_NONE : ProjectUtils.findProjElem(svc.getPerfModels(), pmId,
                ProjectUtils.ENTITY_PERFORMANCE_MODEL).getName();
        sb.append(String.format(TABLE_FORMAT_INFO, "Performance Model Name", pmName));
        sb.append(String.format(TABLE_FORMAT_INFO, "Performance Model ID", pmId == null ? MODEL_NONE : pmId));

        sb.append("\nData Models:\n");
        List<List<String>> dataModelTableData = new ArrayList<List<String>>();
        for (IDataModel dm : svc.getDataModels()) {
            dataModelTableData.add(Arrays.asList("", dm.getName(), dm.getId()));
        }
        sb.append(StringUtils.createTable(dataModelTableData));

        sb.append("\nPerformance Models:\n");
        List<List<String>> perfModelTableData = new ArrayList<List<String>>();
        for (IPerfModel pm : svc.getPerfModels()) {
            perfModelTableData.add(Arrays.asList("", pm.getName(), pm.getId()));
        }
        sb.append(StringUtils.createTable(perfModelTableData));

        return sb.toString();
    }

    public static String createServiceReportOutput(ServiceRuntimeReport rep) {
        ServiceRuntimeReport.SimulationStatistics stat = rep.getSimulationStats();

        StringBuilder sb = new StringBuilder();
        sb.append("Service Runtime Report:\n");
        sb.append(String.format(TABLE_FORMAT_REPORT, "Number of Errors", rep.getErrorCount()));
        sb.append(String.format(TABLE_FORMAT_REPORT, "Number of Warnings", rep.getWarningCount()));
        sb.append("\n");

        if (stat != null) {
            sb.append(String.format(TABLE_FORMAT_REPORT, "Data Model Accuracy", stat.getSimulationQualityPercentage()));
            sb.append(String.format(TABLE_FORMAT_REPORT, "Total Requests", stat.getRequestsCount()));
            sb.append(String.format(TABLE_FORMAT_REPORT, "Requests Outside Track", stat.getRequestsCount() - stat.getStatefulResponsesReturnedCount()));
            sb.append(String.format(TABLE_FORMAT_REPORT, "Stateful Responses", stat.getStatefulResponsesReturnedCount()));
            sb.append(String.format(TABLE_FORMAT_REPORT, "Default Rule Used", stat.getStatefulResponsesReturnedCount()));
            sb.append("\n");
        }

        sb.append(String.format(TABLE_FORMAT_REPORT, "Performance Model Accuracy", rep.getPerfModelAccuracy()));
        sb.append("\n");

        sb.append(String.format(TABLE_FORMAT_REPORT, "Processed Messages Count", rep.getMessageCount()));
        sb.append(String.format(TABLE_FORMAT_REPORT, "Processed Messages Size", rep.getMessageSize()));
        sb.append(String.format(TABLE_FORMAT_REPORT, "Unique Messages", rep.getUniqueMsgCount()));
        sb.append("\n");

        List<String> clients = rep.getClientIds();
        if (clients != null) {
            sb.append(String.format(TABLE_FORMAT_REPORT, "Clients Count", clients.size()));
            sb.append(String.format(TABLE_FORMAT_REPORT, "Clients", StringUtils.joinWithDelim(", ", clients.toArray())));
        }
        
        return sb.toString();
    }

    public static String createServiceListOutput(ServiceListAtom atom) {
        Collection<Collection<String>> rows = new ArrayList<Collection<String>>();
        for (ServiceListAtom.ServiceEntry e : atom.getEntries()) {
            Collection<String> row = new ArrayList<String>(4);
            row.add(e.getTitle());
            // Show "Offline" if not in ready state
            row.add((ServiceRuntimeConfiguration.XmlConstants.STATE_READY.equals(e.getDeployState()))
                    ? e.getServiceMode() : ServiceRuntimeConfiguration.XmlConstants.RUNTIME_OFFLINE);
            row.add(e.getId());
            row.add(e.getRuntimeIssuesParsed());
            rows.add(row);
        }

        Collection<String> headers = Arrays.asList("Name", "Mode", "ID", "Runtime Issue(s)");

        return StringUtils.createTable(rows, headers);
    }

    //============================== CONSTRUCTORS =============================================

    //============================== ABSTRACT METHODS =========================================

    //============================== OVERRIDEN METHODS ========================================

    //============================== INSTANCE METHODS =========================================

    //============================== PRIVATE METHODS ==========================================

    //============================== GETTERS / SETTERS ========================================

    //============================== INNER CLASSES ============================================

}
