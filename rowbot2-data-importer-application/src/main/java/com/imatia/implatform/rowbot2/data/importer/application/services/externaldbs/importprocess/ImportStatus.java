package com.imatia.implatform.rowbot2.data.importer.application.services.externaldbs.importprocess;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ImportStatus {

        int nextPageIndex;
        long alreadyImportedRows;

        public ImportStatus(int nextPageIndex, long alreadyImportedRows) {
            this.nextPageIndex = nextPageIndex;
            this.alreadyImportedRows = alreadyImportedRows;
        }
}
