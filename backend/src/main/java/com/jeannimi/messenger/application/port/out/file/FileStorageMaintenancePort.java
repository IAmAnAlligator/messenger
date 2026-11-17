package com.jeannimi.messenger.application.port.out.file;

import java.util.List;

public interface FileStorageMaintenancePort {

  List<StoredFileInfo> listFiles();
}
