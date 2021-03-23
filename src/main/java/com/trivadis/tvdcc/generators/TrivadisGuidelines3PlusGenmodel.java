/*
 * Copyright 2021 Philipp Salvisberg <philipp.salvisberg@trivadis.com>
 * 
 * Licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 
 * Unported License (the "License"); you may not use this file except 
 * in compliance with the License. You may obtain a copy of the License at
 * 
 *     https://creativecommons.org/licenses/by-nc-nd/3.0/
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.trivadis.tvdcc.generators;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import com.trivadis.oracle.plsql.validation.PLSQLValidatorPreferences;
import com.trivadis.tvdcc.genmodel.GenRulesXml;
import com.trivadis.tvdcc.genmodel.GenSqaleXml;
import com.trivadis.tvdcc.validators.TrivadisGuidelines3Plus;

public class TrivadisGuidelines3PlusGenmodel {

	public static void genPlsqlcopModelXml() {
		GenSqaleXml gen = new GenSqaleXml();
		gen.generate("./src/main/resources/TrivadisGuidelines3Plus");
	}
	
	public static void copy(String sourceDir, String targetDir) throws IOException  {
		Files.walk(Paths.get(sourceDir)).forEach(sourceFile -> {
			Path targetFile = Paths.get(targetDir, sourceFile.toString().substring(sourceDir.length()));
			try {
				if (sourceFile.toFile().isFile()) {
					Files.copy(sourceFile, targetFile, StandardCopyOption.REPLACE_EXISTING);
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		});
	}

	public static void genRulesXml() throws URISyntaxException, IOException {
		GenRulesXml gen = new GenRulesXml();
		File tvdccDir = new File(GenRulesXml.class.getProtectionDomain().getCodeSource().getLocation().toURI())
				.getParentFile();
		String tvdccSampleDir = tvdccDir.getAbsolutePath() + File.separator + "sample" + File.separator + "guidelines";
		String tempDir = Files.createTempDirectory("genmodel_").toString();
		copy(tvdccSampleDir, tempDir);
		copy("./src/main/resources/TrivadisGuidelines3Plus/sample", tempDir);
		String targetDir = "./src/main/resources/TrivadisGuidelines3Plus";
		gen.generate(targetDir, tempDir.toString());
	}

	public static void main(String[] args) throws URISyntaxException, IOException {
		PLSQLValidatorPreferences.INSTANCE.setValidatorClass(TrivadisGuidelines3Plus.class);
		genPlsqlcopModelXml();
		genRulesXml();
	}

}
