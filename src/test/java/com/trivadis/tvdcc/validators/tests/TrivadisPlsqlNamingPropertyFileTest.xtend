package com.trivadis.tvdcc.validators.tests

import com.trivadis.oracle.plsql.validation.PLSQLValidatorPreferences
import com.trivadis.tvdcc.validators.TrivadisGuidelines3Plus
import org.junit.Assert
import org.junit.BeforeClass
import org.junit.Test
import java.nio.file.Files
import java.nio.file.Paths
import java.io.File
import org.junit.AfterClass
import java.io.FileWriter
import java.io.BufferedWriter

// To ensure that everything works correctly run TrivadisGuidelines3PlusTest before running TrivadisGuidelines3PlusPropertyFileTest.
class TrivadisGuidelines3PlusPropertyFileTest extends AbstractValidatorTest {
	
	static File file
	
	static FileWriter fileWriter
	
	static BufferedWriter bufferedWriter
	
	static String backupFileSuffix			= ".backup"

	static String propertyPathString 		= System.getProperty("user.home") + File.separator + "TrivadisGuidelines3Plus.properties"
	static String backupPropertyPathString 	= propertyPathString + backupFileSuffix
	
	@BeforeClass
	static def void setupValidator() {
		PLSQLValidatorPreferences.INSTANCE.validatorClass = TrivadisGuidelines3Plus
	}	
	
	//save the users properties
	@BeforeClass
	static def void stashPropertiesFile() {
		if(Files.exists(Paths.get(propertyPathString))){
				Files.copy(Paths.get(propertyPathString), Paths.get(backupPropertyPathString))
				Files.delete(Paths.get(propertyPathString))
		}
	}	
	
	//create a simple property-file to test with	
	@BeforeClass
	static def void createTestPropertyFile() {
		file = new File(propertyPathString)
		fileWriter = new FileWriter(file, true)
    	bufferedWriter = new BufferedWriter(fileWriter)
    	bufferedWriter.write("PREFIX_LOCAL_VARIABLE_NAME = loc_")
    	bufferedWriter.newLine()
    	bufferedWriter.close()
    	fileWriter.close()
    
	}	
	
	//check that old prefix is now not accepted
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

	//check that new prefix from file is accepted
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
	
	//check that defaults are used if not specified in the properties-file
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

	//check that defaults are used if not specified in the properties-file
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
		//delete the test property-file 
		if(Files.exists(Paths.get(propertyPathString))){
			Files.delete(Paths.get(propertyPathString))
		}
		//restore the users properties after the test
		if(Files.exists(Paths.get(backupPropertyPathString))){
				Files.copy(Paths.get(backupPropertyPathString),Paths.get(propertyPathString))
				Files.delete(Paths.get(backupPropertyPathString))
		}
	}	
}