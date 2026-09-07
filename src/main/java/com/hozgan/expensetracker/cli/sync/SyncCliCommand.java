package com.hozgan.expensetracker.cli.sync;

import picocli.CommandLine.Command;

@Command(
    name = "sync",
    mixinStandardHelpOptions = true,
    description = "Synchronize expenses with S3-compatible storage.")
public final class SyncCliCommand implements Runnable {

  @Override
  public void run() {
    System.out.println("Use 'sync push' or 'sync pull'. See --help for details.");
  }
}
