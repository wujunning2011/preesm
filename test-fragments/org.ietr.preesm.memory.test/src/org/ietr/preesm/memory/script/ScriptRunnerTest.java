package org.ietr.preesm.memory.script;

import java.io.File;
import org.junit.Assert;
import org.junit.Test;

/**
 */
public class ScriptRunnerTest {
  public static final String SCRIPT_FOLDER_PATH = "resources/Code/Script/";

  /**
   * Make sure the tests will be able to find the test files
   */
  @Test
  public void testFindScripts() {
    final File dir = new File(ScriptRunnerTest.SCRIPT_FOLDER_PATH);
    final String[] list = dir.list();
    Assert.assertEquals(14, list.length);
  }
}