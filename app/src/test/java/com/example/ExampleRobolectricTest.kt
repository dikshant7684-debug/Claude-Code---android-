package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.files.ProjectFileManager
import com.example.data.model.ProjectType
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun readStringFromContext() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Claude Code", appName)
  }

  @Test
  fun testProjectCreationAndFileResolution() {
    runBlocking {
      val context = ApplicationProvider.getApplicationContext<Context>()
      val fileManager = ProjectFileManager(context)

      val project = fileManager.createProject("TestKotlinApp", ProjectType.KOTLIN_ANDROID, "Testing project")
      assertNotNull(project)
      assertEquals("TestKotlinApp", project.name)

      val files = fileManager.listFiles(project)
      assertTrue(files.any { it.name == "README.md" })

      // Path traversal attempt should throw SecurityException
      assertThrows(SecurityException::class.java) {
        fileManager.resolveSafeFile(project, "../../etc/passwd")
      }
    }
  }
}
