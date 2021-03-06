/**
 * Copyright (C) 2015 The Gravitee team (http://gravitee.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.gravitee.rest.api.service.impl.swagger.parser;

import io.gravitee.rest.api.service.exceptions.SwaggerDescriptorException;
import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.core.util.Yaml;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.core.models.ParseOptions;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author GraviteeSource Team
 */
public class OAIParser extends AbstractSwaggerParser<OpenAPI> {

    private final Logger logger = LoggerFactory.getLogger(OAIParser.class);

    static {
        System.setProperty(String.format("%s.trustAll", io.swagger.v3.parser.util.RemoteUrl.class.getName()), Boolean.TRUE.toString());
    }

    @Override
    public OpenAPI parse(String content) {
        return this.parse(content, true, null);
    }

    private OpenAPI parse(String content, boolean reparse, ParseOptions options) {
        OpenAPIParser parser = new OpenAPIParser();
        SwaggerParseResult parseResult;
        String path = content;
        File temp = null;
        if (!isLocationUrl(content)) {
            // Swagger v1 supports only a URL to read swagger: create temporary file for Swagger parser
            temp = createTempFile(content);
            path = temp.getAbsolutePath();
        }

        parseResult = parser.readLocation(path, null, options);

        if (temp != null) {
            temp.delete();
        }

        /* Hack due to swagger v1 converting issue
         * See https://github.com/swagger-api/swagger-parser/issues/1451
         */
        final List<String> filteredMessages = parseResult.getMessages().stream()
                .filter(message -> !message.matches("^attribute info.contact.*"))
                .collect(Collectors.toList());
        parseResult.setMessages(filteredMessages);

        if (parseResult != null && parseResult.getOpenAPI() != null &&
                (parseResult.getMessages() != null && !parseResult.getMessages().isEmpty())) {
            throw new SwaggerDescriptorException(parseResult.getMessages());

        }


        if (parseResult != null && parseResult.getOpenAPI() != null && parseResult.getOpenAPI().getInfo() != null) {
            /* ugly hack for swagger v2 $ref resolving & avoid NPE in resolveFully
             * See https://github.com/swagger-api/swagger-parser/issues/1455
             */
            if (reparse) {
                ParseOptions reparseOptions = new ParseOptions();
                reparseOptions.setResolveFully(true);

                return this.parse(Yaml.pretty(parseResult.getOpenAPI()), false, reparseOptions);
            }
            return parseResult.getOpenAPI();
        }
        return null;
    }


    private File createTempFile(String content) {
        File temp = null;
        String fileName = "gio_swagger_" + System.currentTimeMillis();
        BufferedWriter bw = null;
        FileWriter out = null;
        try {
            temp = File.createTempFile(fileName, ".tmp");
            out = new FileWriter(temp);
            bw = new BufferedWriter(out);
            bw.write(content);
            bw.close();
        } catch (IOException ioe) {
            // Fallback to the new parser
        } finally {
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException e) {
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                }
            }
        }
        return temp;
    }

}
