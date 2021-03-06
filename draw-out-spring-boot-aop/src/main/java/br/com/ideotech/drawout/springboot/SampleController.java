/**
 * Copyright 2019 Adauto Martins <adauto.martin@ideotech.com.br>
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package br.com.ideotech.drawout.springboot;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import br.com.ideotech.drawout.model.Sample;

@RestController
@RequestMapping("/sample")
public class SampleController {

	@RequestMapping(method = RequestMethod.GET, value= "/**")
	public @ResponseBody String sample() {
		Sample s = new Sample();
		s.setId(19);
		s.setText("Client");
		
		Response response = ClientBuilder.newClient()
				.target("http://localhost:8080")
				.path("sample")
				.request(MediaType.APPLICATION_JSON).post(Entity.json(s));
		
		Sample ret = response.readEntity(Sample.class);
		return ret.getText();
	}
	
	@GetMapping(value= "/exception/**")
	public @ResponseBody String sampleDelete() throws SampleException {
		throw new SampleException("Some error!");
	}
	
	@RequestMapping(method = RequestMethod.GET, value= "/runtimeException/**")
	public @ResponseBody String samplePut() throws SampleException {
		throw new SampleRuntimeException("Some runtime error!");
	}
	
	@PostMapping(value= "/**")
	public @ResponseBody Sample sample(HttpServletRequest request, @RequestBody Sample sample) {
		Sample s = new Sample();
		s.setId(19 + sample.getId());
		s.setText("Text to test " + sample.getText());
		return s;
	}
}
