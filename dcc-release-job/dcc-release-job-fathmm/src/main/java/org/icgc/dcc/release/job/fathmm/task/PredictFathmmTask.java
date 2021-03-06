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
package org.icgc.dcc.release.job.fathmm.task;

import java.io.Closeable;
import java.io.IOException;

import lombok.NonNull;

import org.apache.spark.api.java.JavaRDD;
import org.icgc.dcc.release.core.job.FileType;
import org.icgc.dcc.release.core.task.GenericProcessTask;
import org.icgc.dcc.release.job.fathmm.function.PredictFathmm;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.BiMap;

public class PredictFathmmTask extends GenericProcessTask implements Closeable {

  private final String jdbcUrl;
  private final BiMap<String, String> transcripts;
  private PredictFathmm fathmmPredictor;

  public PredictFathmmTask(@NonNull String jdbcUrl, @NonNull BiMap<String, String> transcripts) {
    super(FileType.OBSERVATION, FileType.OBSERVATION_FATHMM);
    this.jdbcUrl = jdbcUrl;
    this.transcripts = transcripts;
  }

  @Override
  protected JavaRDD<ObjectNode> process(JavaRDD<ObjectNode> input) {
    return input.map(fathmmPredictor());
  }

  private PredictFathmm fathmmPredictor() {
    if (fathmmPredictor == null) {
      fathmmPredictor = new PredictFathmm(jdbcUrl, transcripts);
    }

    return fathmmPredictor;
  }

  @Override
  public void close() throws IOException {
    if (fathmmPredictor != null) {
      fathmmPredictor.close();
    }
  }

}
