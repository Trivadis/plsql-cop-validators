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

import com.trivadis.oracle.plsql.validation.PLSQLValidatorPreferences;
import com.trivadis.tvdcc.genmodel.GenRulesXml;
import com.trivadis.tvdcc.genmodel.GenSqaleXml;
import com.trivadis.tvdcc.validators.GLP;

public class GLPGenmodel {

    public static void genPlsqlcopModelXml() {
        GenSqaleXml gen = new GenSqaleXml();
        gen.generate("./src/main/resources/GLP");
    }

    public static void genRulesXml() {
        GenRulesXml gen = new GenRulesXml();
        String targetDir = "./src/main/resources/GLP";
        gen.generate(targetDir, "./src/main/resources/GLP/sample");
    }

    public static void main(String[] args) {
        PLSQLValidatorPreferences.INSTANCE.setValidatorClass(GLP.class);
        genPlsqlcopModelXml();
        genRulesXml();
    }

}
