/*
 * Copyright (c) 2014 The Ontario Institute for Cancer Research. All rights reserved.
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
package org.icgc.dcc.release.job.index.transform;

import static org.icgc.dcc.release.job.index.model.CollectionFieldAccessors.getGeneSetId;
import lombok.NonNull;
import lombok.val;

import org.apache.spark.api.java.function.Function;
import org.icgc.dcc.release.job.index.context.DefaultDocumentContext;
import org.icgc.dcc.release.job.index.core.Document;
import org.icgc.dcc.release.job.index.core.DocumentContext;
import org.icgc.dcc.release.job.index.core.DocumentTransform;
import org.icgc.dcc.release.job.index.core.IndexJobContext;
import org.icgc.dcc.release.job.index.model.DocumentType;

import com.fasterxml.jackson.databind.node.ObjectNode;

public class GeneSetDocumentTransform implements DocumentTransform, Function<ObjectNode, Document> {

  private final DocumentContext documentContext;

  public GeneSetDocumentTransform(IndexJobContext indexJobContext) {
    this.documentContext = new DefaultDocumentContext(DocumentType.GENE_SET_TYPE, indexJobContext);
  }

  @Override
  public Document call(ObjectNode geneSet) throws Exception {
    return transformDocument(geneSet, documentContext);
  }

  @Override
  public Document transformDocument(@NonNull ObjectNode geneSet, @NonNull DocumentContext context) {
    val geneSetId = getGeneSetId(geneSet);

    return new Document(context.getType(), geneSetId, geneSet);
  }

}
