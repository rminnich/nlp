/*
 * Copyright (C) 2015 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package nlp;

import com.google.cloud.dataflow.sdk.Pipeline;
import com.google.cloud.dataflow.sdk.io.TextIO;
import com.google.cloud.dataflow.sdk.options.DataflowPipelineOptions;
import com.google.cloud.dataflow.sdk.options.Default;
import com.google.cloud.dataflow.sdk.options.DefaultValueFactory;
import com.google.cloud.dataflow.sdk.options.Description;
import com.google.cloud.dataflow.sdk.options.PipelineOptions;
import com.google.cloud.dataflow.sdk.options.PipelineOptionsFactory;
import com.google.cloud.dataflow.sdk.transforms.Aggregator;
import com.google.cloud.dataflow.sdk.transforms.Count;
import com.google.cloud.dataflow.sdk.transforms.DoFn;
import com.google.cloud.dataflow.sdk.transforms.PTransform;
import com.google.cloud.dataflow.sdk.transforms.ParDo;
import com.google.cloud.dataflow.sdk.transforms.Sum;
import com.google.cloud.dataflow.sdk.util.gcsfs.GcsPath;
import com.google.cloud.dataflow.sdk.values.KV;
import com.google.cloud.dataflow.sdk.values.PCollection;

/* for the NLP */
import java.io.*;
import java.util.*;

import edu.stanford.nlp.dcoref.CorefChain;
import edu.stanford.nlp.dcoref.CorefCoreAnnotations;
import edu.stanford.nlp.io.*;
import edu.stanford.nlp.ling.*;
import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.trees.*;
import edu.stanford.nlp.util.*;

/**
 * The NLP pipeline.
 * From Minnich, A. et. al; "TrueView: Harnessing the power of multiple review sites"
 */
public class NLP {

    static enum Output {
	PENNTREES, VECTORS, ROOT, PROBABILITIES
	    }
    public static interface NLPOptions extends PipelineOptions {
	@Description("Path of the file to read from")
	    String getInputFile();
	void setInputFile(String value);
	
	@Description("Path of the file to write to")
	    String getOutput();
	void setOutput(String value);
    }
    
    public static void main(String[] args) {
	NLPOptions options = PipelineOptionsFactory.fromArgs(args).withValidation()
	    .as(NLPOptions.class);
	
	Pipeline p = Pipeline.create(options);
	
	p.apply(TextIO.Read.from(options.getInputFile()))
	    .apply(ParDo.named("ExtractWords").of(new DoFn<String, String>() {
			private static final long serialVersionUID = 0;
			private StanfordCoreNLP tokenizer;
			
			@Override
			    public void startBundle(Context c) {
			    tokenizer = new StanfordCoreNLP();
  			}
			@Override
			    public void processElement(ProcessContext c) throws InterruptedException {
			    String word = c.element();
			    Annotation annotation = tokenizer.process(word);
			    tokenizer.annotate(annotation);
			    
			    c.output("DID ONE -- FORMAT OUTPUT HERE");
			}
		    }))
	    .apply(TextIO.Write.to(options.getOutput()));
	
	p.run();
    }
}
