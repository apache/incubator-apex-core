/**
 * Copyright (C) 2015 DataTorrent, Inc.
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
package com.datatorrent.api;

import com.datatorrent.api.Context.ModuleContext;
import com.datatorrent.api.Operator.InputPort;
import com.datatorrent.api.Operator.OutputPort;
import org.apache.hadoop.conf.Configuration;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * <p>
 * AbstractModule class .</p>
 *
 */
public abstract class AbstractModule implements Module
{
  public Map<String, Operator> operators = new LinkedHashMap<String, Operator>();
  public Map<String, PortPair> streams = new LinkedHashMap<String, PortPair>();
  public Map<String, InputPort> inputPortsMap = new LinkedHashMap<String, InputPort>();
  public Map<String, OutputPort> outputPortsMap = new LinkedHashMap<String, OutputPort>();

  public Map<String, InputPort> getInputPorts()
  {
    return inputPortsMap;
  }

  public Map<String, OutputPort> getOutputPorts()
  {
    return outputPortsMap;
  }

  public void flattenDAG(DAG dag, Configuration conf)
  {

    try {
      /**
       * Add operators is the same.
       */
      for (Map.Entry<String, Operator> e : operators.entrySet()) {
        Object o = e.getValue();
        if (o instanceof Module) {
          Module m = (Module) o;
          m.flattenDAG(dag, conf);
        } else {
          dag.addOperator(e.getKey(), e.getValue());
        }
      }

      /**
       * Add connections
       */
      int idx = 0;
      for (Map.Entry<String, PortPair> stream : streams.entrySet()) {
        stream.getValue().connect(dag, "s" + stream.getKey() + "_" + idx);
        idx++;
      }
    } catch (Throwable th) {
      throw new RuntimeException(th);
    }
  }

  @Override public void setup(ModuleContext context)
  {
  }

  @Override public void teardown()
  {
  }

}

