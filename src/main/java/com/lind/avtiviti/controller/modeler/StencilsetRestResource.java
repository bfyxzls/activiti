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

import java.io.InputStream;
import org.activiti.engine.ActivitiException;
import org.apache.commons.io.IOUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 控制器.
 *
 * @author Tijs Rademakers.
 */
@RestController
public class StencilsetRestResource {

  /**
   * 配置.
   */
  @RequestMapping(
      value = "/editor/stencilset",
      method = RequestMethod.GET,
      produces = "application/json;charset=utf-8")
  @ResponseBody
  public String getStencilset() {
    InputStream stencilsetStream = this.getClass().getClassLoader()
        .getResourceAsStream("static/stencilset.json");
    try {
      return IOUtils.toString(stencilsetStream, "utf-8");
    } catch (Exception e) {
      throw new ActivitiException("加载stencil配置出错", e);
    }
  }
}
