/*
 * Copyright (c) 2014, Cloudera, Inc. All Rights Reserved.
 *
 * Cloudera, Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"). You may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * This software is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for
 * the specific language governing permissions and limitations under the
 * License.
 */

package com.cloudera.oryx.common.pmml;

import javax.xml.bind.JAXBException;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.dmg.pmml.Application;
import org.dmg.pmml.Header;
import org.dmg.pmml.PMML;
import org.dmg.pmml.Timestamp;
import org.jpmml.model.JAXBUtil;

/**
 * PMML-related utility methods.
 */
public final class PMMLUtils {

  public static final String VERSION = "4.2.1";

  private PMMLUtils() {
  }

  /**
   * @return {@link PMML} with common {@link Header} fields like {@link Application},
   *  {@link Timestamp}, and version filled out
   */
  public static PMML buildSkeletonPMML() {
    String formattedDate =
        new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZ", Locale.ENGLISH).format(new Date());
    Header header = new Header()
        .setTimestamp(new Timestamp().addContent(formattedDate))
        .setApplication(new Application("Oryx"));
    return new PMML(VERSION, header, null);
  }

  /**
   * @param pmml {@link PMML} model to write
   * @param path file to write the model to
   * @throws IOException if an error occurs while writing the model to storage
   */
  public static void write(PMML pmml, Path path) throws IOException {
    try (OutputStream out = Files.newOutputStream(path)) {
      write(pmml, out);
    }
  }

  /**
   * @param pmml {@link PMML} model to write
   * @param out stream to write the model to
   * @throws IOException if an error occurs while writing the model to storage
   */
  public static void write(PMML pmml, OutputStream out) throws IOException {
    try {
      JAXBUtil.marshalPMML(pmml, new StreamResult(out));
    } catch (JAXBException e) {
      throw new IOException(e);
    }
  }

  /**
   * @param path file to read PMML from
   * @return {@link PMML} model file from path
   * @throws IOException if an error occurs while reading the model from storage
   */
  public static PMML read(Path path) throws IOException {
    try (InputStream in = Files.newInputStream(path)) {
      return read(in);
    }
  }

  /**
   * @param in stream to read PMML from
   * @return {@link PMML} model file from stream
   * @throws IOException if an error occurs while reading the model from the stream
   */
  public static PMML read(InputStream in) throws IOException {
    try {
      return JAXBUtil.unmarshalPMML(new StreamSource(in));
    } catch (JAXBException e) {
      throw new IOException(e);
    }
  }

  /**
   * @param pmml model
   * @return model serialized as an XML document as a string
   */
  public static String toString(PMML pmml) {
    try (StringWriter out = new StringWriter()) {
      JAXBUtil.marshalPMML(pmml, new StreamResult(out));
      return out.toString();
    } catch (JAXBException | IOException e) {
      // IOException should not be possible; JAXBException would only happen with XML
      // config problems.
      throw new IllegalStateException(e);
    }
  }

  /**
   * @param pmmlString PMML model encoded as an XML doc in a string
   * @return {@link PMML} object representing the model
   * @throws JAXBException if XML can't be unserialized
   */
  public static PMML fromString(String pmmlString) throws JAXBException {
    return JAXBUtil.unmarshalPMML(new StreamSource(new StringReader(pmmlString)));
  }

}
