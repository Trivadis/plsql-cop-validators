package com.trivadis.tvdcc.validators.tests

import com.trivadis.oracle.plsql.validation.PLSQLValidatorPreferences
import com.trivadis.tvdcc.validators.TrivadisPlsqlNaming
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import org.junit.Assert
import org.junit.BeforeClass
import org.junit.Test

class TrivadisPlsqlNamingPropertiesFileTest extends AbstractValidatorTest {

	@BeforeClass
	static def void commonSetup() {
		stashPropertiesFile
		createTestPropertiesFile
		PLSQLValidatorPreferences.INSTANCE.validatorClass = TrivadisPlsqlNaming
	}
	
	// create a simple properties file to test with	
	static def void createTestPropertiesFile() {
		val file = new File(TrivadisPlsqlNamingTest.FULL_PROPERTIES_FILE_NAME)
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
		Assert.assertEquals(1, issues.filter[it.code == "G-9102"].size)
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
		Assert.assertEquals(0, issues.filter[it.code == "G-9102"].size)
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
		Assert.assertEquals(1, issues.filter[it.code == "G-9101"].size)
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
		Assert.assertEquals(0, issues.filter[it.code == "G-9101"].size)

	}

}
