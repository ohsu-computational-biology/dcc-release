/*
 * Copyright (c) 2015 The Ontario Institute for Cancer Research. All rights reserved.                             
 *                                                                                                               
 * This program and the accompanying materials are made available under the terms of the GNU Public License v3.0.
 * You should have received a copy of the GNU General Public License along with                                  
 * this program. If not, see <http://www.gnu.org/licenses/>.                                                     
 *                                                                                                               
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY                           
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES                          
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT                           
 * SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,                                
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED                          
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;                               
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER                              
 * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN                         
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.icgc.dcc.release.core.hadoop;

import static org.icgc.dcc.release.core.util.JacksonFactory.SMILE_READER;
import static org.icgc.dcc.release.core.util.JacksonFactory.SMILE_WRITER;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import lombok.extern.slf4j.Slf4j;

import org.apache.hadoop.io.serializer.Deserializer;
import org.apache.hadoop.io.serializer.Serialization;
import org.apache.hadoop.io.serializer.Serializer;

import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Base class for providing {@code ObjectNode} serialization to Smile format.
 * <p>
 * The reason for this class is that BSON is a terribly inefficient format in both space and time. It turns out
 * ElasticSearch is also using Smile for the same reasons. It also has the advantage of accepting Smile.
 */
@Slf4j
public class ObjectNodeSerialization implements Serialization<ObjectNode> {

  @Override
  public boolean accept(Class<?> c) {
    return ObjectNode.class.isAssignableFrom(c);
  }

  @Override
  public Deserializer<ObjectNode> getDeserializer(Class<ObjectNode> c) {
    log.info("Creating {}...", ObjectNodeDeserializer.class.getSimpleName());
    return new ObjectNodeDeserializer(c);
  }

  @Override
  public Serializer<ObjectNode> getSerializer(Class<ObjectNode> c) {
    log.info("Creating {}...", ObjectNodeSerializer.class.getSimpleName());
    return new ObjectNodeSerializer(c);
  }

  class ObjectNodeSerializer implements Serializer<ObjectNode> {

    OutputStream out;

    ObjectNodeSerializer(Class<ObjectNode> clazz) {
    }

    @Override
    public void open(OutputStream out) throws IOException {
      this.out = out;
    }

    @Override
    public void close() throws IOException {
      out.close();
    }

    @Override
    public void serialize(ObjectNode objectNode) throws IOException {
      SMILE_WRITER.writeValue(out, objectNode);
    }
  }

  class ObjectNodeDeserializer implements Deserializer<ObjectNode> {

    InputStream in;

    ObjectNodeDeserializer(Class<ObjectNode> clazz) {
    }

    @Override
    public void open(InputStream in) throws IOException {
      this.in = in;
    }

    @Override
    public void close() throws IOException {
      in.close();
    }

    @Override
    public ObjectNode deserialize(ObjectNode next) throws IOException {
      return SMILE_READER.readValue(in);
    }

  }

}
