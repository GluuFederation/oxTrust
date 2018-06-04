package org.gluu.oxtrust.model.log;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.AndFileFilter;
import org.apache.commons.io.filefilter.FileFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.gluu.oxtrust.model.GluuAppliance;
import org.gluu.oxtrust.model.LogViewerConfig;
import org.xdi.model.SimpleCustomProperty;
import org.xdi.service.JsonService;
import org.xdi.util.StringHelper;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class LogFiles {
    public LogViewerConfig config(GluuAppliance appliance, JsonService jsonService) throws IOException {
        String oxLogViewerConfig = appliance.getOxLogViewerConfig();
        if (StringHelper.isNotEmpty(oxLogViewerConfig)) {
            return jsonService.jsonToObject(appliance.getOxLogViewerConfig(), LogViewerConfig.class);
        }

        return null;
    }

    public Map<Integer, String> filesIndexedById(LogViewerConfig config) {
        Map<Integer, String> logFiles = new HashMap<Integer, String>();

        int fileIndex = 0;
        for (SimpleCustomProperty logTemplate : config.getLogTemplates()) {
            String logTemplatePattern = logTemplate.getValue2();
            if (StringHelper.isEmpty(logTemplatePattern)) {
                continue;
            }

            String logTemplatePath = FilenameUtils.getFullPath(logTemplatePattern);
            String logTemplateFile = FilenameUtils.getName(logTemplatePattern);

            File logTemplateBaseDir = new File(logTemplatePath);

            FileFilter fileFilter = new AndFileFilter(FileFileFilter.FILE, new WildcardFileFilter(logTemplateFile));
            File[] files = logTemplateBaseDir.listFiles(fileFilter);
            if (files == null) {
                continue;
            }

            for (File file : files) {
                logFiles.put(fileIndex++, file.getPath());
            }
        }

        return logFiles;
    }
}