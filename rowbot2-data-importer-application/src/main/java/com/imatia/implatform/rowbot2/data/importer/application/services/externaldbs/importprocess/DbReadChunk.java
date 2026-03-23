package com.imatia.implatform.rowbot2.data.importer.application.services.externaldbs.importprocess;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class DbReadChunk<T> {
    List<T> items;
    long totalItems;
}
