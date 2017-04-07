/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.datatorrent.stram.cli;

import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


/**
 *
 */
public class ApexCliMiscTest
{
  ApexCli cli;

  static Map<String, String> env = new HashMap<>();
  static String userHome;

  @Before
  public void startingTest()
  {
    try {

      cli = new ApexCli();
      cli.init();

    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @After
  public void finishedTest()
  {
    cli = null;
  }

  @Test
  public void testGetHighlightColorDefault() throws Exception
  {
    cli.conf.clear();
    Assert.assertEquals("\033[1m", cli.getHighlightColor());
  }

  @Test
  public void testGetHighlightColorFromConf() throws Exception
  {
    cli.conf.clear();
    cli.conf.set("apex.cli.color.highlight", "\033[0;93m");
    Assert.assertEquals("\033[0;93m", cli.getHighlightColor());
  }
}
