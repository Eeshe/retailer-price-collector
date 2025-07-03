package me.eeshe.retailerpricecollector.util;

import java.io.File;

import tech.tablesaw.api.Table;

public class TableUtil {

  /**
   * Loads the Table from the passed File.
   *
   * @param file File to load the table from.
   * @return Table stored in the passed File. Null if the file doesn't exist.
   */
  public static Table loadTable(File file) {
    File rawTableFile = new File("../output/raw_products.csv");
    if (!rawTableFile.exists()) {
      System.out.println("Couldn't find raw_products.csv");
      return null;
    }
    return Table.read().csv(rawTableFile);
  }

  /**
   * Writes the passed Table to a file in the passed path.
   *
   * @param table Table to write.
   * @param file  Path of the file to write.
   */
  public static void writeTableFile(Table table, File file) {
    file.getParentFile().mkdirs();

    table.write().csv(file);
  }
}
