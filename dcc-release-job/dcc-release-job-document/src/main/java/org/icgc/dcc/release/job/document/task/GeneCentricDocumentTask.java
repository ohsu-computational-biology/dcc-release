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
package org.icgc.dcc.release.job.document.task;

import static org.icgc.dcc.release.core.util.Tuples.tuple;
import static org.icgc.dcc.release.job.document.model.CollectionFieldAccessors.getGeneId;
import lombok.val;

import org.apache.spark.api.java.JavaRDD;
import org.icgc.dcc.release.core.document.DocumentType;
import org.icgc.dcc.release.core.document.Document;
import org.icgc.dcc.release.core.task.TaskContext;
import org.icgc.dcc.release.core.task.TaskType;
import org.icgc.dcc.release.job.document.core.DocumentJobContext;
import org.icgc.dcc.release.job.document.function.PairGeneIdObservation;
import org.icgc.dcc.release.job.document.transform.GeneCentricDocumentTransform;

import com.fasterxml.jackson.databind.node.ObjectNode;

public class GeneCentricDocumentTask extends AbstractDocumentTask {

  private final DocumentJobContext indexJobContext;

  public GeneCentricDocumentTask(DocumentJobContext indexJobContext) {
    super(DocumentType.GENE_CENTRIC_TYPE);
    this.indexJobContext = indexJobContext;
  }

  @Override
  public TaskType getType() {
    return TaskType.FILE_TYPE;
  }

  @Override
  public void execute(TaskContext taskContext) {
    val genes = readGenesPivoted(taskContext);
    val observations = readObservations(taskContext);

    // TODO: This should be configured to give optimum results
    // val splitSize = Long.toString(48 * 1024 * 1024);
    // conf.set("mapred.min.split.size", splitSize);
    // conf.set("mapred.max.split.size", splitSize);
    //
    // return ObjectNodeRDDs.combineObjectNodeFile(sparkContext, taskContext.getPath(inputFileType) + path, conf);
    //
    val output = transform(taskContext, genes, observations);
    writeDocOutput(taskContext, output);
  }

  private JavaRDD<Document> transform(TaskContext taskContext,
      JavaRDD<ObjectNode> genes, JavaRDD<ObjectNode> observations) {
    val genePairs = genes.mapToPair(gene -> tuple(getGeneId(gene), gene));
    val observationPairs = observations
        .flatMapToPair(new PairGeneIdObservation())
        .groupByKey();

    val geneObservationsPairs = genePairs.leftOuterJoin(observationPairs);
    val transformed = geneObservationsPairs.map(new GeneCentricDocumentTransform(indexJobContext));

    return transformed;
  }

}