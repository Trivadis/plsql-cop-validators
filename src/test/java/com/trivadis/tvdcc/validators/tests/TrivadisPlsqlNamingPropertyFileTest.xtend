package com.trivadis.tvdcc.validators.tests

import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import org.junit.AfterClass
import org.junit.Assert
import org.junit.BeforeClass
import org.junit.Test

class TrivadisPlsqlNamingPropertyFileTest extends AbstractValidatorTest {

	@BeforeClass
	static def void setupTest() {
		TrivadisPlsqlNamingTest.stashPropertiesFile
		createTestPropertiesFile
		TrivadisPlsqlNamingTest.setupValidator
	}

	// create a simple properties file to test with	
	static def void createTestPropertiesFile() {
		val file = new File(TrivadisPlsqlNamingTest.FULL_PROPERTY_FILE_NAME)
		val fileWriter = new FileWriter(file, true)
		val bufferedWriter = new BufferedWriter(fileWriter)
		bufferedWriter.write("PREFIX_LOCAL_VARIABLE_NAME = loc_")
		bufferedWriter.newLine()
		bufferedWriter.close()
		fileWriter.close()
	}

	// check that old prefix is now not accepted
	@Test
	def void LocalVariableNok() {
		val stmt = '''
			CREATE OR REPLACE PACKAGE BODY example AS
			   PROCEDURE a IS
			      l_some_name INTEGER;
			   BEGIN
			      NULL;
			   END a;
			END example;
		'''
		val issues = stmt.issues
		Assert.assertEquals(1, issues.filter[it.code == "G-9002"].size)
	}

	// check that new prefix from file is accepted
	@Test
	def void LocalVariableOk() {
		val stmt = '''
			CREATE OR REPLACE PACKAGE BODY example AS
			   PROCEDURE a IS
			      loc_some_name INTEGER;
			   BEGIN
			      NULL;
			   END a;
			END example;
		'''
		val issues = stmt.issues
		Assert.assertEquals(0, issues.filter[it.code == "G-9002"].size)
	}

	// check that defaults are used if not specified in the properties-file
	@Test
	def void GlobalVariableNok() {
		val stmt = '''
			CREATE OR REPLACE PACKAGE example AS
			   some_name INTEGER;
			END example;
			/
		'''
		val issues = stmt.issues
		Assert.assertEquals(1, issues.filter[it.code == "G-9001"].size)
	}

	// check that defaults are used if not specified in the properties-file
	@Test
	def void GlobalVariableOk() {
		val stmt = '''
			CREATE OR REPLACE PACKAGE example AS
			   g_some_name INTEGER;
			END example;
		'''
		val issues = stmt.issues
		Assert.assertEquals(0, issues.filter[it.code == "G-9001"].size)

	}

	@AfterClass
	static def void restorePropertiesFile() {
		TrivadisPlsqlNamingTest.restorePropertiesFile
	}
}
