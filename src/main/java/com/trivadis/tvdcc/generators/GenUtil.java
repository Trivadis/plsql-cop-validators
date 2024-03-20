/*
 * Copyright 2024 Philipp Salvisberg <philipp.salvisberg@accenture.com>
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

import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;


public class GenUtil {
    public static String getPath(String dirName) throws IOException {
        String ret = null;
        List<Path> dirs = Files.walk(Paths.get(""), FileVisitOption.FOLLOW_LINKS).filter(f -> f.toFile().getAbsolutePath().contains(dirName.substring(2)))
            .collect(Collectors.toList());
        if (dirs.size() > 0) {
            ret = dirs.get(0).toFile().getAbsolutePath();
        }
        return ret;
    }
}
