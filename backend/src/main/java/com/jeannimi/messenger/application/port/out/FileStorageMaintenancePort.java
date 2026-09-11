package com.jeannimi.messenger.application.port.out;

import java.util.List;

public interface FileStorageMaintenancePort {

  List<StoredFileInfo> listFiles();

}