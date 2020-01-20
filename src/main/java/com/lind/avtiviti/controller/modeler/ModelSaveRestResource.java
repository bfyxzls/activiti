/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lind.avtiviti.controller.modeler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import lombok.extern.slf4j.Slf4j;
import org.activiti.editor.constants.ModelDataJsonConstants;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Model;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Tijs Rademakers
 */
@Slf4j
@RestController
@Transactional
public class ModelSaveRestResource implements ModelDataJsonConstants {

  @Autowired
  private RepositoryService repositoryService;

  @Autowired
  private ObjectMapper objectMapper;

  /**
   * 保存流程.
   *
   * @param modelId
   * @param name
   * @param description
   * @param jsonXml
   * @param svgXml
   */
  @RequestMapping(value = "/model/{modelId}/save", method = RequestMethod.PUT, produces = {"application/json"})
  @ResponseStatus(value = HttpStatus.OK)
  public void saveModel(@PathVariable String modelId,
                        @RequestParam("name") String name,
                        @RequestParam("description") String description,
                        @RequestParam("json_xml") String jsonXml,
                        @RequestParam("svg_xml") String svgXml) {

    try {
      Model model = repositoryService.getModel(modelId);
      ObjectNode modelJson = (ObjectNode) objectMapper.readTree(model.getMetaInfo());
      int newVersion = model.getVersion() + 1;
      modelJson.put(MODEL_NAME, name);
      modelJson.put(MODEL_DESCRIPTION, description);
      modelJson.put(MODEL_REVISION, newVersion);
      String key = StringUtils.substringBetween(jsonXml, "\"process_id\":\"", "\",\"name\"");
      model.setKey(key);
      model.setMetaInfo(modelJson.toString());
      model.setName(name);
      model.setVersion(newVersion);
      repositoryService.saveModel(model);
      repositoryService.addModelEditorSource(model.getId(), jsonXml.getBytes("utf-8"));

      InputStream svgStream = new ByteArrayInputStream(svgXml.getBytes("utf-8"));
      TranscoderInput input = new TranscoderInput(svgStream);
      PNGTranscoder transcoder = new PNGTranscoder();
      // Setup output
      ByteArrayOutputStream outStream = new ByteArrayOutputStream();
      TranscoderOutput output = new TranscoderOutput(outStream);
      // Do the transformation
      transcoder.transcode(input, output);
      final byte[] result = outStream.toByteArray();
      repositoryService.addModelEditorSourceExtra(model.getId(), result);
      outStream.close();

//      // 更新数据库
//      ActModel actModel = actModelService.get(modelId);
//      // 更新key
//      String key = StrUtil.subBetween(json_xml, "\"process_id\":\"", "\",\"name\"");
//      actModel.setModelKey(key);
//      actModel.setName(name);
//      actModel.setDescription(description);
//      actModel.setVersion(newVersion);
//      actModelService.update(actModel);
    } catch (Exception e) {
      log.error("保存模型出错", e);
      throw new ActivitiException("保存模型出错", e);
    }
  }
}
