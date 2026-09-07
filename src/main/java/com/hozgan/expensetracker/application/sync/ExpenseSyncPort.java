package com.hozgan.expensetracker.application.sync;

import java.nio.file.Path;

public interface ExpenseSyncPort {

  void upload(Path localFile);

  void download(Path targetFile, boolean overwrite);

  String getBucket();

  String getObjectKey();
}
