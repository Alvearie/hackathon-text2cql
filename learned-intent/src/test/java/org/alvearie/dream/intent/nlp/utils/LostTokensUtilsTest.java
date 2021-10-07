/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.alvearie.dream.intent.nlp.utils;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.HashSet;

import org.alvearie.dream.intent.nlp.text.Configuration;
import org.alvearie.dream.intent.nlp.text.Document;
import org.junit.BeforeClass;
import org.junit.Test;


public class LostTokensUtilsTest {

    private static Configuration configuration;
    private static Document document;

    @BeforeClass
    public static void setUp() {

        configuration = Configuration.getDefault();

        document = new Document("The quick brown foxes jumped over the lazy dogs.");
    }

    @Test
    public void getLostTokens() {

        configuration.setLemmatize(false);

        assertEquals(
                new HashSet<>(Arrays.asList(".", "over", "the")), new LostTokensUtils(configuration).getLostTokens(document));
    }
}
