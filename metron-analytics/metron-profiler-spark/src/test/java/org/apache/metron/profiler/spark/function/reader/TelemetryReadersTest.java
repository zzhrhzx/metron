/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.apache.metron.profiler.spark.function.reader;

import org.apache.metron.profiler.spark.reader.ColumnEncodedTelemetryReader;
import org.apache.metron.profiler.spark.reader.TelemetryReaders;
import org.apache.metron.profiler.spark.reader.TextEncodedTelemetryReader;
import org.junit.Assert;
import org.junit.Test;

import static org.apache.metron.profiler.spark.reader.TelemetryReaders.COLUMNAR;
import static org.apache.metron.profiler.spark.reader.TelemetryReaders.JSON;
import static org.apache.metron.profiler.spark.reader.TelemetryReaders.ORC;
import static org.apache.metron.profiler.spark.reader.TelemetryReaders.PARQUET;
import static org.apache.metron.profiler.spark.reader.TelemetryReaders.TEXT;

public class TelemetryReadersTest {

  @Test
  public void testJsonReader() {
    String key = JSON.toString();
    Assert.assertTrue(TelemetryReaders.create(key) instanceof TextEncodedTelemetryReader);
  }

  @Test
  public void testJsonReaderLowerCase() {
    String key = JSON.toString().toLowerCase();
    Assert.assertTrue(TelemetryReaders.create(key) instanceof TextEncodedTelemetryReader);
  }

  @Test
  public void testOrcReader() {
    String key = ORC.toString();
    Assert.assertTrue(TelemetryReaders.create(key) instanceof ColumnEncodedTelemetryReader);
  }


  @Test
  public void testOrcReaderLowerCase() {
    String key = ORC.toString().toLowerCase();
    Assert.assertTrue(TelemetryReaders.create(key) instanceof ColumnEncodedTelemetryReader);
  }

  @Test
  public void testParquetReader() {
    String key = PARQUET.toString();
    Assert.assertTrue(TelemetryReaders.create(key) instanceof ColumnEncodedTelemetryReader);
  }

  @Test
  public void testParquetReaderLowerCase() {
    String key = PARQUET.toString().toLowerCase();
    Assert.assertTrue(TelemetryReaders.create(key) instanceof ColumnEncodedTelemetryReader);
  }

  @Test
  public void testTextReader() {
    String key = TEXT.toString();
    Assert.assertTrue(TelemetryReaders.create(key) instanceof TextEncodedTelemetryReader);
  }

  @Test
  public void testColumnReader() {
    String key = COLUMNAR.toString();
    Assert.assertTrue(TelemetryReaders.create(key) instanceof ColumnEncodedTelemetryReader);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidReader() {
    TelemetryReaders.create("invalid");
    Assert.fail("exception expected");
  }
}
